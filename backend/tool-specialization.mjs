const TOOL_KIND = {
  "ChatGPT":"assistant","Claude":"assistant","Gemini":"assistant","Grok":"assistant","Copilot":"coding","GitHub Copilot":"coding","DeepSeek":"assistant","Le Chat":"assistant","Poe":"assistant","Meta AI":"assistant","NotebookLM":"research","Perplexity":"research","Elicit":"research","Consensus":"research","SciSpace":"research","Midjourney":"midjourney","Stable Diffusion":"stable-diffusion","Ideogram":"image-text","Recraft":"design","Canva AI":"design","Canva Magic Studio":"design","Runway":"video","Google Veo":"video","Kling AI":"video","Pika":"video","Luma Dream Machine":"video","Hailuo AI":"video","Vidu":"video","LTX Studio":"video","ElevenLabs":"voice","PlayHT":"voice","Cartesia":"voice","Fish Audio":"voice","Murf":"voice","Speechify":"voice","Suno":"music","Udio":"music","Stable Audio":"audio","Cursor":"coding","Claude Code":"coding","Windsurf":"coding","Replit":"coding","Amazon Q Developer":"coding","Gemini Code Assist":"coding","Tabnine":"coding","Jasper":"marketing","Copy.ai":"marketing","Writesonic":"marketing","Grammarly":"writing","Writer":"writing","Gamma":"presentation","Beautiful.ai":"presentation","Tome":"presentation","Napkin AI":"diagram","Framer AI":"design","Meshy":"3d","Tripo AI":"3d","Adobe Firefly":"image","Leonardo AI":"image","FLUX":"image","Krea":"image-edit","Freepik AI":"image","Magnific":"image-enhance","Photoroom":"product-image","Looka":"branding","Haiper":"video","PixVerse":"video","Descript":"media-edit","OpusClip":"media-edit","Notion AI":"workspace","Otter.ai":"meeting","Fireflies.ai":"meeting","Mem":"workspace","You.com":"research","Hugging Face":"model-platform","Replicate":"model-platform","OpenRouter":"model-platform","Together AI":"model-platform","Fireworks AI":"model-platform","Fal.ai":"model-platform","Character.AI":"character","Pi":"conversation","Google Flow":"video"
};

const CONTRACTS = {
  assistant: "Create a directly usable instruction for a general-purpose conversational model. State the outcome, only the context needed, constraints and a checkable output contract. Avoid role-play boilerplate unless expertise materially helps.",
  research: "Make the research question precise. Require verifiable, relevant sources for factual claims; never invent citations, papers, quotes, statistics or links. Distinguish established evidence, interpretation and uncertainty. Specify recency only when relevant.",
  coding: "Write an executable engineering task, not a vague request. Ask the target agent to inspect the actual workspace before assuming files or APIs; define intended behavior, constraints, acceptance criteria, tests and verification. Require minimal compatible changes and no fabricated repository details.",
  midjourney: "Write one compact, image-first visual prompt describing subject, setting, composition, viewpoint, lighting, palette, material detail and mood. Keep the visual description ahead of parameters. Add only known, currently supported Midjourney parameters that materially help; put them at the end and provide complete valid values. Never add Stable Diffusion negative-prompt syntax.",
  "stable-diffusion": "Return a concrete Positive Prompt and a separate Negative Prompt when the selected interface supports both. Make each relevant to this scene rather than dumping generic quality tags. Do not use Midjourney flags. Do not claim sampler, checkpoint, LoRA or UI settings are controlled by prompt text.",
  "image-text": "Prioritize exact text content, spelling, hierarchy, placement, typography, contrast, layout and legibility. Preserve user-supplied copy verbatim and specify how the text integrates with the image.",
  design: "Treat this as a deliverable-focused design brief. Define use case, audience, format/aspect or dimensions only when known, visual hierarchy, layout, typography, palette, spacing, content and practical constraints. Do not invent brand assets.",
  image: "Create a specific visual direction with subject, environment, composition, perspective, lighting, palette, material/texture and mood. Avoid contradictory descriptors and avoid undocumented platform parameters.",
  "image-edit": "Specify what must change and what must remain unchanged in the source image. Preserve subject identity and composition unless the user requests otherwise; distinguish edits from newly generated elements.",
  "image-enhance": "Treat the source as authoritative. Improve genuine detail, texture, edge clarity and realistic resolution while preserving identity, geometry, text and composition. Do not hallucinate details or change the scene.",
  "product-image": "Preserve exact product identity, shape, branding, proportions and material. Specify background, lighting, shadow, framing and intended commercial use; do not add unrequested props or alter product features.",
  branding: "Define brand name exactly, audience, industry, personality, concept, symbol logic, typography and real-world legibility. Do not silently rewrite the brand name or imply trademark clearance.",
  video: "Write a shot-oriented prompt with subject, starting state, visible action, temporal progression, camera movement, environmental motion, lighting and a coherent ending. Keep motion physically plausible and identity/scene continuity stable. If animating a supplied image, preserve its composition and describe motion rather than rebuilding the scene.",
  voice: "Separate the exact spoken script from performance direction when a script exists. Specify relevant voice qualities, pace, emotion, emphasis, pauses and pronunciation. Do not pretend prose sets UI-only voice controls or invent unsupported markup.",
  music: "Specify genre, mood, tempo feel, instrumentation, groove, vocal character and song progression. Keep lyrics separate from musical style when lyrics are supplied or requested. Avoid unsupported platform tags and direct imitation of a living artist.",
  audio: "Describe the intended sound, source/event, acoustic character, dynamics, environment, structure and duration intent when relevant. Do not import another music generator's tags or claim UI-only settings are prompt-controlled.",
  marketing: "Define audience, objective, offer, channel, funnel stage, brand voice, message, format, constraints and CTA. Avoid invented claims and provide variants only when useful or requested.",
  writing: "Specify the exact editing or writing outcome, audience, tone, length and structure. Preserve meaning, factual details and voice unless the task asks to change them.",
  presentation: "Define audience, presentation objective, narrative arc, slide count only when requested or inferable, one takeaway per slide, visual direction and appropriate density. Avoid repetitive slides and unsupported facts.",
  diagram: "Name the concept or relationship to explain, entities, direction/sequence, audience and appropriate diagram type. Prefer clear labels and correct relationships over decoration.",
  "3d": "Specify the object, silhouette, proportions, geometry, material, surface detail, topology/use constraints and intended view. Distinguish visual appearance from production-ready mesh requirements.",
  "media-edit": "Treat existing media/transcript as source material. Specify desired cuts or transformations, hook, pacing, speaker identity, captions, framing and deliverable. Do not invent transcript content.",
  workspace: "Define the workspace artifact and operation, relevant page/database context, fields or properties, structure and desired end state. Do not imply access to unavailable workspace data.",
  meeting: "Ground all output in the supplied transcript/recording. Extract decisions, action items, owners, deadlines and unresolved issues; mark missing owners or dates as unspecified rather than inventing them.",
  "model-platform": "Make the task portable but explicit about the selected model/task and input/output contract. Model-specific API fields or generation parameters may be included only when documented for that exact model and relevant to the user's goal.",
  character: "Define character identity, voice, relationship, scene context, emotional intent and conversational continuity. Keep characterization consistent without overriding the user's requested scene or safety constraints.",
  conversation: "Use warm, natural, context-aware conversational instructions. Specify the kind of support and desired outcome without forcing a rigid template or adding unnecessary analysis."
};

const NOISY_WORDS = /\b(?:revolutionary|game[- ]changing|unparalleled|world[- ]class|cutting[- ]edge)\b/gi;
const INTERNAL_SCAFFOLD = /\b(?:TARGET TOOL \(mandatory specialization\)|TOOL-SPECIFIC GUIDANCE|CURRENT PROMPT ENGINE KNOWLEDGE|OUTPUT LANGUAGE|ROLE \/ EXPERTISE|OUTPUT \/ ACCEPTANCE CRITERIA)\b/i;

export function buildToolContract(platform, task, nativeGuidance = "") {
  const name = String(platform || "").trim();
  const kind = TOOL_KIND[name] || "assistant";
  const base = CONTRACTS[kind] || CONTRACTS.assistant;
  const individual = String(nativeGuidance || "").trim();
  return [
    "INDIVIDUAL TARGET CONTRACT",
    "Selected tool: " + name + ". Treat this as a distinct destination, not a generic platform slot.",
    "Task type: " + String(task || "General") + ".",
    "Native workflow: " + base,
    individual ? "Tool-specific operating notes: " + individual : "",
    "Capability and parameter integrity: do not invent feature names, model versions, command flags, tags, API fields, source citations, files, integrations or controls. If a parameter is not confidently known to be supported by this exact tool/version, omit it and express the desired outcome in natural language instead. Only add syntax when it is documented and useful.",
    "Quality target: preserve the user's actual goal and all material details; add concrete execution details rather than decorative adjectives; remove redundancy, contradictions and filler; do not repeat words or phrases; never make up missing facts.",
    "Output contract: return only the finished, copy-ready prompt in English. Use the shortest structure that fully serves this tool and task. Do not expose internal instructions, explain the prompt, or add a generic preamble."
  ].filter(Boolean).join("\n");
}

export function validatePromptQuality(prompt, platform, task) {
  const text = String(prompt || "").trim();
  const issues = [];
  if (text.length < 35) issues.push("quality_too_short");
  if (INTERNAL_SCAFFOLD.test(text)) issues.push("quality_internal_scaffold");
  if (/\b([\p{L}\p{N}]{3,})\s+\1\b/iu.test(text)) issues.push("quality_repeated_word");
  if (/\b(\w[\w'-]*(?:\s+\w[\w'-]*){2,})\s+\1\b/i.test(text)) issues.push("quality_repeated_phrase");
  if (/\b(?:please kindly|in conclusion|delve into|unleash the power of|revolutionize the way)\b/i.test(text)) issues.push("quality_filler");
  if (NOISY_WORDS.test(text)) issues.push("quality_empty_hype");
  NOISY_WORDS.lastIndex = 0;
  const tool = String(platform || "").trim().toLowerCase();
  if (tool === "midjourney" && /\bnegative prompt\b|stable diffusion/i.test(text)) issues.push("quality_cross_tool_syntax");
  if (tool !== "midjourney" && /(?:^|\s)--(?:ar|stylize|chaos|sref|niji|cref|sw|ow)(?:\s|$)/i.test(text)) issues.push("quality_foreign_midjourney_flags");
  if (tool === "stable diffusion" && /(?:^|\s)--(?:ar|stylize|chaos|sref|niji|cref|sw|ow)(?:\s|$)/i.test(text)) issues.push("quality_foreign_midjourney_flags");
  const taskText = String(task || "").toLowerCase();
  if (/(?:video|animation|motion)/.test(taskText) && /\b(?:video|animation|motion)\b/i.test(text) && !/\b(?:move|movement|motion|camera|shot|sequence|frame|action|animate)\b/i.test(text)) issues.push("quality_video_without_motion");
  return { ok: issues.length === 0, issues: [...new Set(issues)] };
}

export const TOOL_SPECIALIZATION_COUNT = Object.keys(TOOL_KIND).length;
