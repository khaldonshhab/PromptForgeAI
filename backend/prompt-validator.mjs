const MIDJOURNEY_FLAGS = new Set([
  "ar","aspect","v","version","style","stylize","s","chaos","c","quality","q",
  "weird","w","seed","iw","no","tile","repeat","stop","exp","sref","sw","oref",
  "ow","cref","cw","profile","p","fast","relax","turbo","draft","raw","niji",
  "video","motion","bs","hd"
]);

const VALUE_RULES = {
  ar: /^\\d+(?:\\.\\d+)?:\\d+(?:\\.\\d+)?$/,
  aspect: /^\\d+(?:\\.\\d+)?:\\d+(?:\\.\\d+)?$/,
  v: /^(?:\\d+(?:\\.\\d+)?|latest)$/i,
  version: /^(?:\\d+(?:\\.\\d+)?|latest)$/i,
  stylize: /^\\d{1,4}$/,
  s: /^\\d{1,4}$/,
  chaos: /^\\d{1,3}$/,
  c: /^\\d{1,3}$/,
  quality: /^(?:0\\.25|0\\.5|1|2)$/,
  q: /^(?:0\\.25|0\\.5|1|2)$/,
  weird: /^\\d{1,4}$/,
  w: /^\\d{1,4}$/,
  seed: /^\\d{1,10}$/,
  iw: /^\\d+(?:\\.\\d+)?$/,
  sw: /^\\d{1,4}$/,
  ow: /^\\d{1,4}$/,
  cw: /^\\d{1,3}$/,
  stop: /^\\d{1,3}$/,
  exp: /^\\d{1,3}$/,
  repeat: /^\\d{1,3}$/,
  bs: /^\\d{1,3}$/,
  motion: /^(?:low|high)$/i
};

const NO_VALUE_FLAGS = new Set(["tile","fast","relax","turbo","draft","raw","niji","video","hd","p"]);
const VERSION_VALUES = new Set(["1","2","3","4","5","5.1","5.2","6","6.0","6.1","6.2","7","latest"]);

export function validateFinalPrompt(prompt, platform, profile = "") {
  const text = String(prompt || "").trim();
  const tool = String(platform || "").trim().toLowerCase();
  const issues = [];
  if (text.length < 20) issues.push("prompt_too_short");
  if (/--[a-z][a-z0-9-]*\\s*$/.test(text)) issues.push("dangling_parameter");
  const flags = [...text.matchAll(/--([a-z][a-z0-9-]*)(?:\\s+([^\\s]+))?/gi)];
  const isMidjourney = tool === "midjourney";
  const isStableDiffusion = tool === "stable diffusion";
  if (isMidjourney) {
    for (const match of flags) {
      const flag = match[1].toLowerCase();
      const value = match[2];
      if (!MIDJOURNEY_FLAGS.has(flag)) {
        issues.push("unsupported_midjourney_parameter:" + flag);
        continue;
      }
      if (NO_VALUE_FLAGS.has(flag)) continue;
      if (!value || value.startsWith("--")) {
        issues.push("incomplete_midjourney_parameter:" + flag);
        continue;
      }
      const rule = VALUE_RULES[flag];
      if (rule && !rule.test(value)) issues.push("invalid_midjourney_parameter_value:" + flag);
      if ((flag === "v" || flag === "version") && !VERSION_VALUES.has(value.toLowerCase())) {
        issues.push("unsupported_midjourney_version:" + value);
      }
      if (flag === "style" && !/^(?:raw|cute|expressive|scenic|4a|4b|4c)$/i.test(value)) {
        issues.push("unsupported_midjourney_style:" + value);
      }
    }
    if (/\\bnegative prompt\\b|stable diffusion/i.test(text)) issues.push("cross_tool_syntax");
  } else if (isStableDiffusion) {
    if (flags.some(m => ["ar","stylize","chaos","sref","niji","cref","sw","ow"].includes(m[1].toLowerCase()))) {
      issues.push("midjourney_syntax_in_stable_diffusion");
    }
  } else if (flags.some(m => ["stylize","chaos","sref","niji","cref","cw","sw","ow"].includes(m[1].toLowerCase()))) {
    issues.push("midjourney_syntax_in_other_tool");
  }
  return { ok: issues.length === 0, issues: [...new Set(issues)] };
}
