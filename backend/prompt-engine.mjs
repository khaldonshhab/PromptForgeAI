import fs from "node:fs";
import path from "node:path";

const LOCAL_FILE_CANDIDATES=[path.join(process.cwd(),"prompt-engine.json"),path.join(process.cwd(),"backend","prompt-engine.json")];
const REMOTE_URL=String(process.env.PF_PROMPT_KNOWLEDGE_URL||"").trim();
const REFRESH_MS=Number(process.env.PF_PROMPT_KNOWLEDGE_REFRESH_MS||600000);
let cache=null;
let loadedAt=0;

function readLocal(){
  try{for(const file of LOCAL_FILE_CANDIDATES){try{return JSON.parse(fs.readFileSync(file,"utf8"));}catch{}}return {version:"fallback-1",principles:[],tool_updates:[]};}
}

async function readRemote(){
  if(!REMOTE_URL)return null;
  try{
    const r=await fetch(REMOTE_URL,{headers:{"Cache-Control":"no-cache"}});
    if(!r.ok)return null;
    const j=await r.json();
    return j&&typeof j==="object"?j:null;
  }catch{return null;}
}

export async function getPromptKnowledge(){
  const now=Date.now();
  if(cache&&now-loadedAt<REFRESH_MS)return JSON.stringify(cache);
  const remote=await readRemote();
  cache=remote||readLocal();
  loadedAt=now;
  return JSON.stringify(cache);
}
