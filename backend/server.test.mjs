import test from "node:test";
import assert from "node:assert/strict";
import http from "node:http";
import crypto from "node:crypto";
import { spawn } from "node:child_process";
import { setTimeout as delay } from "node:timers/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.dirname(fileURLToPath(import.meta.url));
const SECRET = "promptforge-test-secret-not-for-production";

async function freePort() {
  const server = http.createServer();
  await new Promise((resolve, reject) => {
    server.once("error", reject);
    server.listen(0, "127.0.0.1", resolve);
  });
  const port = server.address().port;
  await new Promise(resolve => server.close(resolve));
  return port;
}

function tokenFor(username = "integration-user") {
  const payload = Buffer.from(JSON.stringify({
    u: username, p: false, r: "user", e: Math.floor(Date.now() / 1000) + 300
  })).toString("base64url");
  const signature = crypto.createHmac("sha256", SECRET).update(payload).digest("base64url");
  return payload + "." + signature;
}

async function startBackend(extraEnv = {}) {
  const port = await freePort();
  const child = spawn(process.execPath, ["server.mjs"], {
    cwd: root,
    env: {
      ...process.env,
      PORT: String(port),
      PF_AUTH_SECRET: SECRET,
      PF_DB_URL: "",
      PF_DB_KEY: "",
      AI_API_URL: "",
      AI_API_KEY: "",
      AI_MODEL: "",
      AI_API_MODE: "gemini",
      ...extraEnv
    },
    stdio: ["ignore", "pipe", "pipe"]
  });
  let output = "";
  child.stdout.on("data", chunk => output += chunk.toString());
  child.stderr.on("data", chunk => output += chunk.toString());

  const base = "http://127.0.0.1:" + port;
  let ready = false;
  for (let i = 0; i < 60; i++) {
    if (child.exitCode !== null) break;
    try {
      const response = await fetch(base + "/health");
      if (response.ok) { ready = true; break; }
    } catch {}
    await delay(100);
  }
  if (!ready) {
    child.kill("SIGTERM");
    throw new Error("Backend failed to start. Output: " + output);
  }
  return {
    base,
    output: () => output,
    stop: async () => {
      if (child.exitCode === null) child.kill("SIGTERM");
      await Promise.race([
        new Promise(resolve => child.once("exit", resolve)),
        delay(1500)
      ]);
    }
  };
}

async function postPrompt(base, idea = "صمّم صورة لمسرح مهجور بعد نهاية العالم", platform = "Gemini", task = "Image") {
  return fetch(base + "/v1/prompt", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + tokenFor()
    },
    body: JSON.stringify({ idea, platform, task, language: "ar" })
  });
}

function assertNoInternalScaffold(prompt) {
  assert.doesNotMatch(prompt, /\b(?:role\s*(?:\/|:|[-–—]|and)\s*expertise|objective\s*(?:\/|:|[-–—]|and)\s*task|target\s*(?:\/|:|[-–—]|and)\s*context|required\s+approach|tool[\s-]+specific\s+guidance|working\s+rules|output\s*(?:\/|:|[-–—]|and)\s*acceptance\s+criteria)\b/i);
}

test("health reports provider configuration status without exposing secret values", async t => {
  const backend = await startBackend();
  t.after(() => backend.stop());
  const response = await fetch(backend.base + "/health");
  const data = await response.json();
  assert.equal(response.status, 200);
  assert.equal(data.configured, false);
  assert.deepEqual(data.aiConfiguration, {
    urlConfigured: false,
    keyConfigured: false,
    modelConfigured: false,
    mode: "gemini"
  });
  assert.equal(JSON.stringify(data).includes(SECRET), false);
});

test("Gemini returns an English image prompt tailored to an abandoned post-apocalyptic theatre", async t => {
  let captured = null;
  const provider = http.createServer(async (req, res) => {
    let body = "";
    for await (const chunk of req) body += chunk;
    captured = { method: req.method, url: req.url, headers: req.headers, body: JSON.parse(body) };
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({
      candidates: [{
        content: { parts: [{ text: "Create a cinematic image of an abandoned theatre after the apocalypse, with torn velvet curtains, a collapsed balcony, dust-filled shafts of light, and nature reclaiming the stage. Use a dramatic wide composition, muted ash tones, and one warm practical light to establish a haunting but hopeful mood." }] }
      }]
    }));
  });
  await new Promise((resolve, reject) => {
    provider.once("error", reject);
    provider.listen(0, "127.0.0.1", resolve);
  });
  const providerPort = provider.address().port;
  const backend = await startBackend({
    AI_API_URL: "http://127.0.0.1:" + providerPort + "/generateContent",
    AI_API_KEY: "integration-test-key",
    AI_MODEL: "gemini-test-model",
    AI_API_MODE: "gemini"
  });
  t.after(async () => {
    await backend.stop();
    await new Promise(resolve => provider.close(resolve));
  });

  const response = await postPrompt(backend.base);
  const data = await response.json();
  assert.equal(response.status, 200, JSON.stringify(data));
  assert.match(data.prompt, /cinematic image of an abandoned theatre after the apocalypse/i);
  assert.match(data.prompt, /torn velvet curtains/i);
  assert.match(data.prompt, /nature reclaiming the stage/i);
  assertNoInternalScaffold(data.prompt);
  assert.equal(captured.headers["x-goog-api-key"], "integration-test-key");
  assert.equal(captured.body.contents[0].parts[0].text.includes("صمّم صورة"), true);
  assert.equal(captured.body.systemInstruction.parts[0].text.includes("entirely in English"), true);
});

test("provider authentication failure returns an explicit error and no prompt", async t => {
  const provider = http.createServer((req, res) => {
    res.writeHead(401, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ error: { message: "invalid test key" } }));
  });
  await new Promise((resolve, reject) => {
    provider.once("error", reject);
    provider.listen(0, "127.0.0.1", resolve);
  });
  const backend = await startBackend({
    AI_API_URL: "http://127.0.0.1:" + provider.address().port + "/generateContent",
    AI_API_KEY: "invalid-test-key",
    AI_MODEL: "gemini-test-model",
    AI_API_MODE: "gemini"
  });
  t.after(async () => {
    await backend.stop();
    await new Promise(resolve => provider.close(resolve));
  });

  const response = await postPrompt(backend.base, "Create a cinematic image of an abandoned theatre after the apocalypse", "Gemini", "Image");
  const data = await response.json();
  assert.equal(response.status, 502, JSON.stringify(data));
  assert.deepEqual(data, { error: "provider_auth_failed" });
  assert.equal("prompt" in data, false);
});

test("missing Gemini configuration returns an explicit error and no prompt", async t => {
  const backend = await startBackend();
  t.after(() => backend.stop());

  const response = await postPrompt(backend.base, "Create an image of a quiet greenhouse at sunrise", "Gemini", "Image");
  const data = await response.json();
  assert.equal(response.status, 503, JSON.stringify(data));
  assert.deepEqual(data, { error: "provider_not_configured" });
  assert.equal("prompt" in data, false);
});

test("provider failure returns an explicit error and no local prompt", async t => {
  const provider = http.createServer((req, res) => {
    res.writeHead(500, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ error: { message: "temporary provider outage" } }));
  });
  await new Promise((resolve, reject) => {
    provider.once("error", reject);
    provider.listen(0, "127.0.0.1", resolve);
  });
  const backend = await startBackend({
    AI_API_URL: "http://127.0.0.1:" + provider.address().port + "/generateContent",
    AI_API_KEY: "integration-test-key",
    AI_MODEL: "gemini-test-model",
    AI_API_MODE: "gemini"
  });
  t.after(async () => {
    await backend.stop();
    await new Promise(resolve => provider.close(resolve));
  });

  const response = await postPrompt(backend.base, "Create a short research brief about urban tree canopies", "Gemini", "Research");
  const data = await response.json();
  assert.equal(response.status, 502, JSON.stringify(data));
  assert.deepEqual(data, { error: "provider_unavailable" });
  assert.equal("prompt" in data, false);
});

test("wrapped and multiline Gemini scaffold headings are retried then rejected", async t => {
  const scaffolds = [
    "1. ROLE / EXPERTISE\nAct as a visual director.",
    "ROLE / EXPERTISE — additional instructions\nCreate the requested image.",
    "## TARGET / CONTEXT\nCreate the requested image.",
    "**TOOL-SPECIFIC GUIDANCE:**\nUse the internal tool rules.",
    "ROLE /\nEXPERTISE\nCreate the requested image.",
    "OUTPUT / ACCEPTANCE\nCRITERIA — return the image prompt."
  ];
  let attempts = 0;
  let currentScaffold = scaffolds[0];
  const provider = http.createServer((req, res) => {
    attempts++;
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({
      candidates: [{ content: { parts: [{ text: currentScaffold }] } }]
    }));
  });
  await new Promise((resolve, reject) => {
    provider.once("error", reject);
    provider.listen(0, "127.0.0.1", resolve);
  });
  const backend = await startBackend({
    AI_API_URL: "http://127.0.0.1:" + provider.address().port + "/generateContent",
    AI_API_KEY: "integration-test-key",
    AI_MODEL: "gemini-test-model",
    AI_API_MODE: "gemini"
  });
  t.after(async () => {
    await backend.stop();
    await new Promise(resolve => provider.close(resolve));
  });

  for(const scaffold of scaffolds){
    currentScaffold=scaffold;
    attempts=0;
    const response = await postPrompt(backend.base, "Create an image of a lighthouse in a winter storm", "Gemini", "Image");
    const data = await response.json();
    assert.equal(response.status, 502, JSON.stringify(data));
    assert.equal(attempts, 2, "the internal heading must trigger a retry");
    assert.deepEqual(data, { error: "provider_invalid_output" });
    assert.equal("prompt" in data, false);
    assert.notEqual(data.prompt, scaffold);
  }
});

test("natural English mentions of role and context are not mistaken for scaffold", async t => {
  const expected = "Explain the role of urban trees in the context of neighborhood cooling, using practical examples for city planners.";
  let attempts = 0;
  const provider = http.createServer((req, res) => {
    attempts++;
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ candidates: [{ content: { parts: [{ text: expected }] } }] }));
  });
  await new Promise((resolve, reject) => {
    provider.once("error", reject);
    provider.listen(0, "127.0.0.1", resolve);
  });
  const backend = await startBackend({
    AI_API_URL: "http://127.0.0.1:" + provider.address().port + "/generateContent",
    AI_API_KEY: "integration-test-key",
    AI_MODEL: "gemini-test-model",
    AI_API_MODE: "gemini"
  });
  t.after(async () => {
    await backend.stop();
    await new Promise(resolve => provider.close(resolve));
  });

  const response = await postPrompt(backend.base, "Write about urban trees", "Gemini", "Research");
  const data = await response.json();
  assert.equal(response.status, 200, JSON.stringify(data));
  assert.equal(attempts, 1);
  assert.equal(data.prompt, expected);
});

test("unconfigured provider never returns local prompts for any target tool", async t => {
  const backend = await startBackend();
  t.after(() => backend.stop());

  const idea = "Create an image of a red bicycle beside a brick wall at dusk";
  for (const platform of ["Midjourney", "Stable Diffusion", "Gemini"]) {
    const response = await postPrompt(backend.base, idea, platform, "Image");
    const data = await response.json();
    assert.equal(response.status, 503, JSON.stringify(data));
    assert.deepEqual(data, { error: "provider_not_configured" });
    assert.equal("prompt" in data, false);
  }
});

test("non-English requests still fail clearly when no AI provider is configured", async t => {
  const backend = await startBackend();
  t.after(() => backend.stop());

  const response = await postPrompt(backend.base);
  const data = await response.json();
  assert.equal(response.status, 503);
  assert.deepEqual(data, { error: "provider_not_configured" });
  assert.equal("prompt" in data, false);
});
