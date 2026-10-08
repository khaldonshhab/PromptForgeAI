import fs from "node:fs";
import path from "node:path";

const LOCAL_FILE_CANDIDATES=[path.join(process.cwd(),"prompt-engine.json"),path.join(process.cwd(),"backend","prompt-engine.json")];
const DEFAULT_REMOTE_URL="https://raw.githubusercontent.com/khaldonshhab/PromptForgeAI/main/backend/prompt-engine.json";
const REMOTE_URL=String(process.env.PF_PROMPT_KNOWLEDGE_URL||DEFAULT_REMOTE_URL).trim();
const REFRESH_MS=Math.max(30000,Number(process.env.PF_PROMPT_KNOWLEDGE_REFRESH_MS||600000));
let cache=null;
let loadedAt=0;

function readLocal(){
  for(const file of LOCAL_FILE_CANDIDATES){
    try{
      const data=JSON.parse(fs.readFileSync(file,"utf8"));
      if(data&&typeof data==="object")return data;
    }catch{}
  }
  return {version:"fallback-1",principles:[],tool_updates:[]};
}

async function readRemote(){
  if(!REMOTE_URL)return null;
  const controller=new AbortController();
  const timer=setTimeout(()=>controller.abort(),5000);
  try{
    const r=await fetch(REMOTE_URL,{headers:{"Cache-Control":"no-cache"},signal:controller.signal});
    if(!r.ok)return null;
    const j=await r.json();
    if(!j||typeof j!=="object"||Array.isArray(j))return null;
    if(!Array.isArray(j.principles)&&!Array.isArray(j.tool_updates))return null;
    return j;
  }catch{return null;}
  finally{clearTimeout(timer);}
}

export async function getPromptKnowledge(){
  const now=Date.now();
  if(cache&&now-loadedAt<REFRESH_MS)return JSON.stringify(cache);
  const remote=await readRemote();
  cache=remote||readLocal();
  loadedAt=now;
  return JSON.stringify(cache);
}
