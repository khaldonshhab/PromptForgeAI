import http from "node:http";
import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
const PORT=Number(process.env.PORT||8787);
const AI_PROVIDER=String(process.env.AI_PROVIDER||"gemini").toLowerCase();
const AI_API_URL=process.env.AI_API_URL||(AI_PROVIDER==="gemini"?"https://generativelanguage.googleapis.com/v1beta":"");
const AI_API_KEY=process.env.AI_API_KEY||"";
const AI_MODEL=process.env.AI_MODEL||(AI_PROVIDER==="gemini"?"gemini-3.8-flash":"");
const AUTH_SECRET=process.env.PF_AUTH_SECRET||"",ADMIN_USER=process.env.PF_ADMIN_USER||"admin",ADMIN_PASSWORD_HASH=process.env.PF_ADMIN_PASSWORD_HASH||"";
const DATA_DIR=process.env.PF_DATA_DIR||path.join(process.cwd(),"data"),USERS_FILE=path.join(DATA_DIR,"users.json");
fs.mkdirSync(DATA_DIR,{recursive:true});
let USERS=[];
try{USERS=fs.existsSync(USERS_FILE)?JSON.parse(fs.readFileSync(USERS_FILE,"utf8")):JSON.parse(process.env.PF_USERS_JSON||"[]");}catch(e){USERS=[];}
function saveUsers(){fs.writeFileSync(USERS_FILE,JSON.stringify(USERS,null,2));}

export function buildGeminiRequest({ apiUrl, apiKey, model, system, user }) {
  const base=String(apiUrl||"").replace(/\/+$/,'');
  const withoutResource=base.replace(/\/models(?:\/.*)?$/,'');
  const versioned=/\/v1(?:beta)?$/.test(withoutResource) ? withoutResource : withoutResource+"/v1beta";
  const endpoint=versioned+"/models/"+encodeURIComponent(model)+":generateContent?key="+encodeURIComponent(apiKey);
  return {
    url: endpoint,
    body: {
      systemInstruction: { role: 'system', parts: [{ text: String(system || '') }] },
      contents: [{ role: 'user', parts: [{ text: String(user || '') }] }]
    }
  };
}


export function buildGeminiModelUrl({ apiUrl, apiKey, model }) {
  const base=String(apiUrl||"").replace(/\/+$/,'');
  const withoutResource=base.replace(/\/models(?:\/.*)?$/,'');
  const versioned=/\/v1(?:beta)?$/.test(withoutResource) ? withoutResource : withoutResource+"/v1beta";
  return versioned+"/models/"+encodeURIComponent(model)+"?key="+encodeURIComponent(apiKey);
}

export function extractGeminiText(payload) {
  const candidate = payload?.candidates?.[0];
  const parts = candidate?.content?.parts ?? [];
  const text = parts.map(part => typeof part?.text === 'string' ? part.text : '').join('')
    || payload?.output_text
    || payload?.text
    || candidate?.outputText
    || '';
  return String(text || '').trim();
}

export function geminiErrorMessage(responseBody, status, model) {
  let message="";
  let code="";
  try {
    const error=JSON.parse(responseBody)?.error;
    message=String(error?.message||"");
    code=String(error?.status||"");
  } catch(e) {}
  if(code==="API_KEY_INVALID"||/api key.{0,30}(invalid|not valid)|invalid api key/i.test(message))return "Gemini API key is invalid or unauthorized. Check the AI_API_KEY setting.";
  if(/no longer available|model.{0,40}(not found|unavailable|not supported)/i.test(message))return `Gemini model ${model} is unavailable for this API key. Set AI_MODEL to gemini-3.8-flash or another supported GenerateContent model.`;
  if(status===429||code==="RESOURCE_EXHAUSTED")return "Gemini quota or rate limit reached. Try again later.";
  return `Gemini request failed (HTTP ${status}). Check AI_API_URL, AI_API_KEY, and AI_MODEL.`;
}

function httpError(message,statusCode){const error=new Error(message);error.statusCode=statusCode;return error;}

const b64u=x=>Buffer.from(x).toString("base64url");
function passwordHash(password){
 const salt=crypto.randomBytes(16);
 const derived=crypto.scryptSync(String(password),salt,64,{N:131072,r:8,p:1,maxmem:256*1024*1024});
 return "scrypt$131072$8$1$"+salt.toString("base64url")+"$"+derived.toString("base64url");
}
function verifyPassword(password,stored){
 try{
  const a=String(stored||"").split("$"); if(a.length!==6||a[0]!=="scrypt")return false;
  const N=Number(a[1]),r=Number(a[2]),p=Number(a[3]);
  const salt=Buffer.from(a[4],"base64url"),expected=Buffer.from(a[5],"base64url");
  const actual=crypto.scryptSync(String(password),salt,expected.length,{N,r,p,maxmem:256*1024*1024});
  return expected.length===actual.length&&crypto.timingSafeEqual(expected,actual);
 }catch(e){return false;}
}
function signToken(user,days=30){const exp=Math.floor(Date.now()/1000)+days*86400;const payload=b64u(JSON.stringify({u:user.username,p:!!user.premium,e:exp}));const sig=crypto.createHmac("sha256",AUTH_SECRET).update(payload).digest("base64url");return payload+"."+sig;}
function adminToken(){const exp=Math.floor(Date.now()/1000)+86400;const payload=b64u(JSON.stringify({u:ADMIN_USER,r:"admin",e:exp}));const sig=crypto.createHmac("sha256",AUTH_SECRET).update(payload).digest("base64url");return payload+"."+sig;}
function adminAuth(token){const p=auth(token);return p&&p.r==="admin"?p:null;}
function auth(token){if(!AUTH_SECRET||typeof token!=="string")return null;const a=token.split(".");if(a.length!==2)return null;const expected=crypto.createHmac("sha256",AUTH_SECRET).update(a[0]).digest("base64url");const got=Buffer.from(a[1]);const exp=Buffer.from(expected);if(got.length!==exp.length||!crypto.timingSafeEqual(got,exp))return null;try{const p=JSON.parse(Buffer.from(a[0],"base64url").toString());return p.e>Date.now()/1000?p:null;}catch(e){return null;}}
const PROFILES={
 ChatGPT:"chatgpt",Claude:"claude",Gemini:"gemini",Grok:"grok",Copilot:"copilot",DeepSeek:"deepseek","Le Chat":"lechat",Poe:"poe","Meta AI":"meta","NotebookLM":"notebook",
 Perplexity:"research",Elicit:"research",Consensus:"research",SciSpace:"research",
 Midjourney:"image-midjourney","Stable Diffusion":"image-sd",Ideogram:"image-ideogram",Recraft:"design", "Canva AI":"design","Canva Magic Studio":"design",
 Runway:"video","Google Veo":"video","Kling AI":"video",Pika:"video","Luma Dream Machine":"video","Hailuo AI":"video",Vidu:"video","LTX Studio":"video",
 ElevenLabs:"voice",PlayHT:"voice",Cartesia:"voice","Fish Audio":"voice",Murf:"voice",Speechify:"voice",
 Suno:"music",Udio:"music","Stable Audio":"music",
 Cursor:"coding","GitHub Copilot":"coding","Claude Code":"coding",Windsurf:"coding",Replit:"coding","Amazon Q Developer":"coding","Gemini Code Assist":"coding",Tabnine:"coding",
 Jasper:"marketing","Copy.ai":"marketing",Writesonic:"marketing",Grammarly:"writing",Writer:"writing",
 Gamma:"presentation","Beautiful.ai":"presentation",Tome:"presentation","Napkin AI":"presentation","Framer AI":"design",
 Meshy:"3d","Tripo AI":"3d"
,"Adobe Firefly":"firefly","Leonardo AI":"leonardo","FLUX":"flux","Krea":"krea","Freepik AI":"freepik","Magnific":"magnific","Photoroom":"photoroom","Looka":"looka","Haiper":"haiper","PixVerse":"pixverse","Descript":"descript","OpusClip":"opusclip","Notion AI":"notion","Otter.ai":"otter","Fireflies.ai":"fireflies","Mem":"mem","You.com":"you","Hugging Face":"huggingface","Replicate":"replicate","OpenRouter":"openrouter","Together AI":"together","Fireworks AI":"fireworks","Fal.ai":"fal","Character.AI":"character","Pi":"pi"};
const RULES={
 chatgpt:"Use a clear objective/context/constraints/output contract. Preserve user intent, flag material assumptions, and return the final answer in the most useful format.",
 claude:"Use XML-style sections such as <role>, <context>, <task>, <constraints>, and <output_format>. Keep context separated from instructions and emphasize careful interpretation.",
 gemini:"Use direct, concise structured prompting with Markdown headings or XML-style delimiters. Put critical instructions early, define ambiguous terms, and place the final task clearly after context.",
 grok:"Use direct, concise natural language. Reduce filler, separate facts from opinions, and flag time-sensitive claims requiring current verification.",
 copilot:"Frame the request as an engineering task. Reference the repository/workspace context, existing APIs and project conventions; request affected files, runnable changes and tests.",
 deepseek:"Frame explicit requirements, edge cases, validation criteria and final implementation. Never ask for or expose private chain-of-thought.",
 lechat:"Use concise, structured instructions with objective, context and desired output.",
 poe:"Keep the prompt model-agnostic so it works with the selected bot, while defining objective, context, constraints and output format.",
 meta:"Use natural, conversational instructions with enough context to preserve intent; avoid unnecessary prompt-engineering jargon.",
 notebook:"Ground answers in supplied notebook sources, distinguish source evidence from inference, and request source-linked support for important claims.",
 research:"Use evidence-first research. Separate facts, inferences and opinions. Never invent citations. Flag uncertainty and date-stamp time-sensitive claims.",
 "image-midjourney":"Describe subject, environment, composition, camera/perspective, lighting, material, mood and aesthetic. Use Midjourney parameters only when justified; never use SD negative-prompt syntax.",
 "image-sd":"Return a detailed Positive Prompt and a separate Negative Prompt. Use weighting only when useful; never add Midjourney flags.",
 "image-ideogram":"Prioritize composition, layout and text-in-image. Preserve required text exactly and emphasize legibility.",
 design:"Specify deliverable, dimensions, layout/grid, hierarchy, typography, palette, components, spacing and final-use constraints.",
 video:"Describe framing, camera movement, subject action, environmental motion, temporal order, lighting, continuity and ending.",
 voice:"Specify voice identity, age impression, tone, pace, rhythm, emotion, pronunciation and delivery direction.",
 music:"Specify genre, mood, tempo, instrumentation, structure, vocal character and production; separate lyrics from style when needed.",
 coding:"Act as a senior engineer. Inspect existing structure and APIs, preserve compatibility, identify affected files, provide runnable code and tests, and never invent APIs.",
 marketing:"Specify audience, offer, awareness stage, channel, core message, tone, CTA, constraints and measurable outputs.",
 writing:"Preserve meaning and author voice unless change is requested. Specify audience, length, structure, tone and quality criteria.",
 presentation:"Define audience, objective, slide count, narrative sequence, one message per slide, visuals and speaker intent.",
 "3d":"Specify geometry, proportions, materials, surface detail, lighting, camera, background, view angle and intended use.",
 "firefly":"Use a natural design brief: subject, composition, style, lighting, color, background and intended asset use; never import Midjourney or SD syntax.",
 "leonardo":"Specify subject, composition, model/style intent, lighting, materials, camera and fidelity; do not invent unsupported flags.",
 "flux":"Use concise natural-language visual description with strong subject, composition, lighting, style and text requirements; avoid assuming SD syntax.",
 "krea":"Describe generation/edit/enhancement intent, source image if any, target composition, style, lighting and desired transformation.",
 "freepik":"Specify commercial visual brief, subject, composition, style, lighting, palette and asset purpose; preserve exact text when requested.",
 "magnific":"Treat the request as image enhancement/upscaling when appropriate: source, detail recovery, texture, sharpness and realism.",
 "photoroom":"Specify product/object, background, composition, lighting and commercial asset purpose; preserve product identity.",
 "looka":"Specify brand, industry, audience, personality, logo concept, typography, symbol geometry and brand-use context.",
 "canva":"Frame a design brief with format, dimensions, audience, hierarchy, copy, visual style and editable layout.",
 "runwayspecific":"Prioritize motion, camera movement, subject movement, timing and image-to-video continuity; avoid static image prompt syntax.",
 "veo":"Prioritize realistic physics, coherent temporal progression, camera language, dialogue/audio when relevant and cinematic continuity.",
 "kling":"Prioritize subject identity, controlled motion, camera movement, temporal consistency and physical interaction.",
 "pika":"Prioritize concise action, transformation/effect, timing and camera framing.",
 "luma":"Prioritize cinematic camera movement, natural motion, spatial continuity and shot progression.",
 "haiper":"Prioritize clear subject motion, camera movement, scene continuity and concise temporal instructions.",
 "pixverse":"Prioritize motion effect, subject consistency, camera direction and concise timing cues.",
 "vidu":"Prioritize character/object consistency, controlled movement, shot continuity and explicit temporal sequence.",
 "ltx":"Treat the request as a shot-planning brief: scene, shot, camera, action, dialogue/audio, transitions and continuity.",
 "eleven":"Specify voice design or TTS delivery, including identity, emotion, pacing, pronunciation and pauses; keep spoken text separate from direction.",
 "playht":"Specify voice style, delivery, pacing, emotion, pronunciation and intended use; keep spoken script explicit.",
 "cartesia":"Specify voice character, emotional state, pacing, prosody, pronunciation and delivery context.",
 "fishaudio":"Specify voice identity, style, emotion, pacing, pronunciation and exact spoken content.",
 "murf":"Specify narration role, audience, tone, pace, emphasis, pronunciation and delivery context.",
 "speechify":"Specify reading style, voice character, pacing, pronunciation and listening context.",
 "suno":"Specify genre/style, mood, tempo, instrumentation, vocal character, song structure and lyrics separately when needed.",
 "udio":"Use a compact music description plus relevant genre/mood/instrument tags; keep lyrics separate and use guidance tags only when useful.",
 "stableaudio":"Describe audio event, genre/mood, instrumentation, texture, structure and duration intent; avoid Suno/Udio-specific syntax.",
 "cursor":"Frame the task for an IDE agent: repository context, relevant files, current behavior, requested change, constraints, plan and verification.",
 "copilot":"Frame the task with workspace/repository context, relevant files, existing APIs, exact change, acceptance criteria and tests.",
 "claudeCode":"Frame the task as an agentic repository change with goals, constraints, files/context to inspect, verification commands and stopping conditions.",
 "windsurf":"Frame the task for an IDE agent: workspace context, intended change, affected files, constraints, verification and safe execution.",
 "replit":"Frame the task around the current Replit project, runtime, components, user-visible behavior, implementation and verification.",
 "amazonq":"Frame the task with project context, AWS/runtime constraints, affected resources, security considerations, implementation and validation.",
 "geminicodeassist":"Frame the request around IDE/project context, selected code/files, desired change, compatibility and tests.",
 "tabnine":"Provide precise coding intent, local context, expected behavior, language/framework constraints and compatibility.",
 "perplexity":"Request web-grounded synthesis with current sources, explicit source support, uncertainty handling and concise structure.",
 "elicit":"Frame literature research with research question, inclusion criteria, evidence fields, synthesis method and limitations.",
 "consensus":"Frame scientific literature questions precisely and request study-level support, evidence strength and limitations.",
 "scispace":"Frame the task around the supplied academic paper/document, section or question, with evidence extraction and citation-aware explanation.",
 "notion":"Frame as a workspace productivity operation: page/database context, desired transformation, fields, structure and final action.",
 "otter":"Frame around meeting/transcript context, speakers, desired extraction, decisions, action items and output.",
 "fireflies":"Frame around meeting intelligence: transcript/context, decisions, action items, owners, deadlines and output.",
 "mem":"Frame as knowledge retrieval/organization: relevant context, desired connection, summary or action and output.",
 "you":"Frame for web-assisted research with clear objective, currentness expectations and concise synthesis.",
 "jasper":"Frame as a brand/content brief: voice, audience, campaign objective, channel, format and CTA.",
 "copyai":"Frame as a structured marketing workflow with audience, offer, funnel stage, channel, copy format, constraints and variants.",
 "writesonic":"Specify SEO/content or marketing objective, target query/audience, structure, tone, evidence and conversion goal.",
 "grammarly":"Specify source text, editing objective, audience, tone, degree of change and what must remain unchanged.",
 "writer":"Specify brand voice, audience, terminology constraints, content objective, structure and quality criteria.",
 "gamma":"Frame as a presentation generation brief: audience, objective, narrative, slide count, content density and visual direction.",
 "beautiful":"Frame around presentation goal, audience, slide hierarchy, concise copy, visual consistency and layout intent.",
 "tome":"Frame as a narrative presentation/storytelling brief with audience, arc, slide intent and visual direction.",
 "napkin":"Frame around the idea/relationship to visualize, audience, diagram type and key concepts.",
 "framer":"Frame as a web/design brief with page goal, sections, hierarchy, responsive behavior, copy and visual style.",
 "meshy":"Describe 3D asset, geometry, proportions, materials, surface detail, topology/use intent and view requirements.",
 "tripo":"Describe target 3D object with geometry, proportions, materials, topology/detail level and intended use.",
 "huggingface":"Specify target model/task, inputs, expected output and evaluation target; do not assume one model's syntax fits all models.",
 "replicate":"Specify target model/task, input schema, expected output and constraints; keep model-specific parameters explicit.",
 "openrouter":"Keep the prompt portable across the selected model while allowing model-specific instructions only when known.",
 "together":"Specify model/task context, input requirements, desired output and evaluation criteria without inventing provider controls.",
 "fireworks":"Specify model/task context, structured input/output expectations and implementation constraints without unsupported syntax.",
 "fal":"Specify generation task, model/input context, output requirements and relevant generation controls without inventing parameters."};
function send(res,code,obj){res.writeHead(code,{"Content-Type":"application/json","Access-Control-Allow-Origin":"*","Access-Control-Allow-Headers":"Content-Type"});res.end(JSON.stringify(obj));}
function body(req){return new Promise((resolve,reject)=>{let s="";req.on("data",c=>{s+=c;if(s.length>1200000)reject(new Error("body_too_large"));});req.on("end",()=>{try{resolve(JSON.parse(s||"{}"))}catch(e){reject(new Error("invalid_json"))}});});}
function profileFor(platform,task){if(PROFILES[platform])return PROFILES[platform];const t=String(task||"").toLowerCase();if(t.includes("video"))return"video";if(t.includes("image"))return"image";if(t.includes("voice")||t.includes("tts"))return"voice";if(t.includes("coding"))return"coding";if(t.includes("research"))return"research";if(t.includes("marketing"))return"marketing";return"general";}
async function generate(x){
 const missing=[!AI_API_URL&&"AI_API_URL",!AI_API_KEY&&"AI_API_KEY",!AI_MODEL&&"AI_MODEL"].filter(Boolean);
 if(missing.length)throw httpError("AI backend is missing required configuration: "+missing.join(", "),503);
 if(typeof x.platform!=="string"||!x.platform.trim())throw httpError("platform_required",400);
 const session=auth(x.token);
 const profile=profileFor(x.platform,x.task);
 const rule=RULES[profile]||"Understand the intent, preserve it, add only relevant constraints, and define a useful output format.";
 const system="You are PromptForge's platform compiler. Write the prompt that the TARGET platform should receive; do not answer the user's task yourself. TARGET PLATFORM: "+x.platform+"\nPROFILE: "+profile+"\nOUTPUT LANGUAGE: English\nNATIVE PROMPT RULES: "+rule+"\nThe user's idea may be written in any language; understand it directly without translating it as a separate step. Always write the finished prompt in English. Never output a translation or explanation. Never mention PromptForge, this compiler, or other platforms. Do not copy a generic template. Return only the finished prompt.";
 const user="USER IDEA:\n"+x.idea+"\n\nTASK TYPE:\n"+(x.task||"General"); const provider=AI_PROVIDER; let payload={model:AI_MODEL,instructions:system,input:user};
 if(process.env.AI_API_MODE==="chat")payload={model:AI_MODEL,messages:[{role:"system",content:system},{role:"user",content:user}]};
 let requestUrl=AI_API_URL;
 let requestBody=payload;
 if(provider==="gemini"){
  const gemini=buildGeminiRequest({apiUrl:AI_API_URL,apiKey:AI_API_KEY,model:AI_MODEL,system,user});
  requestUrl=gemini.url;
  requestBody=gemini.body;
 }
 let r;
 try{r=await fetch(requestUrl,{method:"POST",headers:provider==="gemini"?{"Content-Type":"application/json"}:{"Content-Type":"application/json","Authorization":"Bearer "+AI_API_KEY},body:JSON.stringify(requestBody),signal:AbortSignal.timeout(25000)});}
 catch(e){throw httpError(e.name==="TimeoutError"?"Gemini request timed out. Try again.":"AI provider could not be reached.",502);}
 const t=await r.text();if(!r.ok){const message=provider==="gemini"?geminiErrorMessage(t,r.status,AI_MODEL):`AI provider request failed (HTTP ${r.status}).`;throw httpError(message,502);}
 let d=JSON.parse(t),out="";
 if(provider==="gemini")out=extractGeminiText(d); else out=d.output_text||d?.choices?.[0]?.message?.content||"";
 if(!out&&Array.isArray(d.output))for(const i of d.output)for(const c of(i.content||[]))if(typeof c.text==="string")out+=c.text;
 if(!out)throw httpError("provider_no_output",502);
 const prompt=out.trim();
 if(prompt.length<20)throw httpError("provider_prompt_too_short",502);
 const forbidden=profile==="image-midjourney" && /negative prompt|stable diffusion/i.test(prompt);
 if(forbidden)throw httpError("platform_syntax_mismatch_midjourney",502);
 const sdMismatch=profile==="image-sd" && /--ar|--stylize|--chaos|--sref/i.test(prompt);
 if(sdMismatch)throw httpError("platform_syntax_mismatch_sd",502);
 return{prompt,profile};
}
if(process.argv[1]&&path.resolve(process.argv[1])===path.resolve(fileURLToPath(import.meta.url))){
const server=http.createServer(async(req,res)=>{
 if(req.method==="OPTIONS"){res.writeHead(204,{"Access-Control-Allow-Origin":"*","Access-Control-Allow-Headers":"Content-Type"});return res.end();}
 if(req.method==="GET"&&req.url==="/health"){
  const missing=[];
  if(!AI_API_URL)missing.push("AI_API_URL");
  if(!AI_API_KEY)missing.push("AI_API_KEY");
  if(!AI_MODEL)missing.push("AI_MODEL");
  return send(res,200,{ok:true,configured:missing.length===0,provider:AI_PROVIDER,model:AI_MODEL||null,missing});
 }
 if(req.method==="GET"&&req.url==="/health/ai"){
  const missing=[];
  if(!AI_API_URL)missing.push("AI_API_URL");
  if(!AI_API_KEY)missing.push("AI_API_KEY");
  if(!AI_MODEL)missing.push("AI_MODEL");
  if(missing.length)return send(res,503,{ok:false,configured:false,provider:AI_PROVIDER,model:AI_MODEL||null,missing});
  if(AI_PROVIDER!=="gemini")return send(res,501,{ok:false,configured:true,provider:AI_PROVIDER,error:"provider_connection_test_not_supported"});
  try{
  const probe=buildGeminiRequest({apiUrl:AI_API_URL,apiKey:AI_API_KEY,model:AI_MODEL,system:"Return only the word OK.",user:"Reply with OK."});
  const r=await fetch(probe.url,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(probe.body),signal:AbortSignal.timeout(15000)});
   const t=await r.text();
   if(!r.ok){
   return send(res,502,{ok:false,configured:true,connected:false,provider:AI_PROVIDER,model:AI_MODEL,error:geminiErrorMessage(t,r.status,AI_MODEL)});
   }
  if(!extractGeminiText(JSON.parse(t)))return send(res,502,{ok:false,configured:true,connected:false,provider:AI_PROVIDER,model:AI_MODEL,error:"Gemini returned no text for the connectivity check."});
  return send(res,200,{ok:true,configured:true,connected:true,provider:AI_PROVIDER,model:AI_MODEL,check:"generateContent"});
  }catch(e){return send(res,502,{ok:false,configured:true,connected:false,provider:AI_PROVIDER,model:AI_MODEL,error:e.name==="TimeoutError"?"Gemini connectivity check timed out.":"Gemini connectivity check failed."});}
 }
 if(req.method==="POST"&&req.url==="/v1/auth/login")try{const x=await body(req);const u=String(x.username||"").trim();const pass=String(x.password||"");const found=USERS.find(v=>String(v.username||"")===u&&v.enabled!==false&&verifyPassword(pass,v.passwordHash));if(!found)return send(res,401,{error:"invalid_credentials"});if(!AUTH_SECRET)return send(res,503,{error:"auth_not_configured"});return send(res,200,{token:signToken(found),premium:!!found.premium,username:found.username});}catch(e){return send(res,400,{error:e.message||"login_failed"});}
 if(req.method==="PATCH"&&req.url==="/v1/auth/account")try{const p=auth(String(req.headers.authorization||"").replace(/^Bearer\\s+/i,""));if(!p||!p.u)return send(res,401,{error:"unauthorized"});const x=await body(req),found=USERS.find(v=>String(v.username||"")===p.u);if(!found||found.enabled===false)return send(res,401,{error:"account_disabled"});if(typeof x.password!=="string"||x.password.length<8)return send(res,400,{error:"password_too_short"});found.passwordHash=passwordHash(x.password);saveUsers();return send(res,200,{ok:true,token:signToken(found),username:found.username});}catch(e){return send(res,400,{error:e.message||"account_update_failed"});}
 if(req.method==="POST"&&req.url==="/admin/login")try{const x=await body(req);if(String(x.username||"")!==ADMIN_USER||!ADMIN_PASSWORD_HASH||!verifyPassword(String(x.password||""),ADMIN_PASSWORD_HASH))return send(res,401,{error:"invalid_admin_credentials"});if(!AUTH_SECRET)return send(res,503,{error:"auth_not_configured"});return send(res,200,{token:adminToken(),username:ADMIN_USER});}catch(e){return send(res,400,{error:e.message||"admin_login_failed"});}
 if(req.method==="GET"&&req.url==="/admin/users") {const p=adminAuth(String(req.headers.authorization||"").replace(/^Bearer\\s+/i,""));if(!p)return send(res,401,{error:"unauthorized"});return send(res,200,{users:USERS.map(u=>({username:u.username,enabled:u.enabled!==false,createdAt:u.createdAt||null}))});}
 if(req.method==="POST"&&req.url==="/admin/users")try{const p=adminAuth(String(req.headers.authorization||"").replace(/^Bearer\\s+/i,""));if(!p)return send(res,401,{error:"unauthorized"});const x=await body(req),u=String(x.username||"").trim(),pass=String(x.password||"");if(!/^[A-Za-z0-9_.-]{3,40}$/.test(u)||pass.length<8)return send(res,400,{error:"invalid_user"});if(USERS.some(v=>v.username===u))return send(res,409,{error:"user_exists"});USERS.push({username:u,passwordHash:passwordHash(pass),premium:false,enabled:true,createdAt:new Date().toISOString()});saveUsers();return send(res,200,{ok:true,username:u});}catch(e){return send(res,400,{error:e.message||"create_user_failed"});}
 if(req.method==="PATCH"&&req.url.startsWith("/admin/users/"))try{const p=adminAuth(String(req.headers.authorization||"").replace(/^Bearer\\s+/i,""));if(!p)return send(res,401,{error:"unauthorized"});const u=decodeURIComponent(req.url.slice("/admin/users/".length));const x=await body(req),found=USERS.find(v=>v.username===u);if(!found)return send(res,404,{error:"user_not_found"});if(typeof x.enabled==="boolean")found.enabled=x.enabled;if(typeof x.password==="string"&&x.password.length>=8)found.passwordHash=passwordHash(x.password);saveUsers();return send(res,200,{ok:true});}catch(e){return send(res,400,{error:e.message||"update_user_failed"});}
 if(req.method==="DELETE"&&req.url.startsWith("/admin/users/"))try{const p=adminAuth(String(req.headers.authorization||"").replace(/^Bearer\\s+/i,""));if(!p)return send(res,401,{error:"unauthorized"});const u=decodeURIComponent(req.url.slice("/admin/users/".length));const before=USERS.length;USERS=USERS.filter(v=>v.username!==u);if(USERS.length===before)return send(res,404,{error:"user_not_found"});saveUsers();return send(res,200,{ok:true});}catch(e){return send(res,400,{error:e.message||"delete_user_failed"});}
 if(req.method==="GET"&&req.url==="/admin") {res.writeHead(200,{"Content-Type":"text/html; charset=utf-8"});return res.end(fs.readFileSync(path.join(process.cwd(),"public","admin.html"),"utf8"));}
 if(req.method==="POST"&&req.url==="/v1/prompt")try{const x=await body(req);if(typeof x.idea!=="string"||x.idea.trim().length<3)return send(res,400,{error:"idea_required"});if(x.idea.length>12000)return send(res,413,{error:"idea_too_long"});if(typeof x.platform!=="string"||!x.platform.trim())return send(res,400,{error:"platform_required"});if(x.platform.length>100||x.task!=null&&(typeof x.task!=="string"||x.task.length>100)||x.language!=null&&(typeof x.language!=="string"||x.language.length>20))return send(res,400,{error:"invalid_request_fields"});return send(res,200,await generate(x));}catch(e){return send(res,e.statusCode||500,{error:e.message||"generation_failed"});}
 send(res,404,{error:"not_found"});
});
if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url)){
 server.listen(PORT,"0.0.0.0",()=>console.log("PromptForge backend on "+PORT));
}
}