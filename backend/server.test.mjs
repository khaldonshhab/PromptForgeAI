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

async function postPrompt(base, idea = "صمّم صورة لمسرح مهجور بعد نهاية العالم") {
  return fetch(base + "/v1/prompt", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + tokenFor()
    },
    body: JSON.stringify({ idea, platform: "Gemini", task: "Image", language: "ar" })
  });
}

test("Gemini mode sends the native request and returns the model's finished prompt", async t => {
  let captured = null;
  const provider = http.createServer(async (req, res) => {
    let body = "";
    for await (const chunk of req) body += chunk;
    captured = { method: req.method, url: req.url, headers: req.headers, body: JSON.parse(body) };
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({
      candidates: [{
        content: { parts: [{ text: "ROLE / EXPERTISE\nAct as a cinematic visual director.\n\nOBJECTIVE\nCreate an abandoned theatre after the apocalypse." }] }
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
  assert.match(data.prompt, /cinematic visual director/i);
  assert.doesNotMatch(data.prompt, /TARGET TOOL|TOOL-SPECIFIC GUIDANCE/i);
  assert.equal(captured.headers["x-goog-api-key"], "integration-test-key");
  assert.equal(captured.body.contents[0].parts[0].text.includes("صمّم صورة"), true);
  assert.equal(captured.body.systemInstruction.parts[0].text.includes("entirely in English"), true);
});

test("provider authentication failure returns an explicit error, never a local template", async t => {
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

  const response = await postPrompt(backend.base);
  const data = await response.json();
  assert.equal(response.status, 502);
  assert.deepEqual(data, { error: "provider_auth_failed" });
  assert.equal("prompt" in data, false);
});

test("missing provider configuration returns 503 instead of pretending to generate", async t => {
  const backend = await startBackend();
  t.after(() => backend.stop());

  const response = await postPrompt(backend.base);
  const data = await response.json();
  assert.equal(response.status, 503);
  assert.deepEqual(data, { error: "provider_not_configured" });
  assert.equal("prompt" in data, false);
});
