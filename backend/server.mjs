import http from "node:http";
const PORT=Number(process.env.PORT||8787),AI_API_URL=process.env.AI_API_URL||"",AI_API_KEY=process.env.AI_API_KEY||"",AI_MODEL=process.env.AI_MODEL||"";
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
};
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
 "3d":"Specify geometry, proportions, materials, surface detail, lighting, camera, background, view angle and intended use."
};
function send(res,code,obj){res.writeHead(code,{"Content-Type":"application/json","Access-Control-Allow-Origin":"*","Access-Control-Allow-Headers":"Content-Type"});res.end(JSON.stringify(obj));}
function body(req){return new Promise((resolve,reject)=>{let s="";req.on("data",c=>{s+=c;if(s.length>1200000)reject(new Error("body_too_large"));});req.on("end",()=>{try{resolve(JSON.parse(s||"{}"))}catch(e){reject(new Error("invalid_json"))}});});}
function profileFor(platform,task){if(PROFILES[platform])return PROFILES[platform];const t=String(task||"").toLowerCase();if(t.includes("video"))return"video";if(t.includes("image"))return"image";if(t.includes("voice")||t.includes("tts"))return"voice";if(t.includes("coding"))return"coding";if(t.includes("research"))return"research";if(t.includes("marketing"))return"marketing";return"chatgpt";}
async function generate(x){
 if(!AI_API_URL||!AI_API_KEY||!AI_MODEL)throw new Error("AI backend is not configured");
 if(typeof x.platform!=="string"||!x.platform.trim())throw new Error("platform_required");
 const profile=profileFor(x.platform,x.task),lang=x.language==="ar"?"Arabic":"the user's requested language";
 const rule=RULES[profile]||"Understand the intent, preserve it, add only relevant constraints, and define a useful output format.";
 const system="You are PromptForge's platform compiler. Write the prompt that the TARGET platform should receive; do not answer the user's task yourself. TARGET PLATFORM: "+x.platform+"\nPROFILE: "+profile+"\nLANGUAGE: "+lang+"\nNATIVE PROMPT RULES: "+rule+"\nNever mention PromptForge, this compiler, or other platforms. Do not copy a generic template. Return only the finished prompt.";
 const user="USER IDEA:\n"+x.idea+"\n\nTASK TYPE:\n"+(x.task||"General");
 let payload={model:AI_MODEL,instructions:system,input:user};
 if(process.env.AI_API_MODE==="chat")payload={model:AI_MODEL,messages:[{role:"system",content:system},{role:"user",content:user}]};
 const r=await fetch(AI_API_URL,{method:"POST",headers:{"Content-Type":"application/json","Authorization":"Bearer "+AI_API_KEY},body:JSON.stringify(payload)});
 const t=await r.text();if(!r.ok)throw new Error("provider_http_"+r.status);
 let d=JSON.parse(t),out=d.output_text||d?.choices?.[0]?.message?.content||"";
 if(!out&&Array.isArray(d.output))for(const i of d.output)for(const c of(i.content||[]))if(typeof c.text==="string")out+=c.text;
 if(!out)throw new Error("provider_no_output");
 return{prompt:out.trim(),profile};
}
http.createServer(async(req,res)=>{
 if(req.method==="OPTIONS"){res.writeHead(204,{"Access-Control-Allow-Origin":"*","Access-Control-Allow-Headers":"Content-Type"});return res.end();}
 if(req.method==="GET"&&req.url==="/health")return send(res,200,{ok:true,configured:Boolean(AI_API_URL&&AI_API_KEY&&AI_MODEL)});
 if(req.method==="POST"&&req.url==="/v1/prompt")try{const x=await body(req);if(typeof x.idea!=="string"||x.idea.trim().length<3)return send(res,400,{error:"idea_required"});return send(res,200,await generate(x));}catch(e){return send(res,500,{error:e.message||"generation_failed"});}
 send(res,404,{error:"not_found"});
}).listen(PORT,"0.0.0.0",()=>console.log("PromptForge backend on "+PORT));