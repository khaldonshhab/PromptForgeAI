package com.promptforge.ai;
import java.io.*; import java.net.*; import java.nio.charset.StandardCharsets; import org.json.*;
public final class RemotePromptClient{
 public interface CB{void done(boolean ok,String value);}
 private void post(String url,String body,CB cb){
  new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setRequestMethod("POST");c.setConnectTimeout(10000);c.setReadTimeout(30000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");c.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));int code=c.getResponseCode();InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();java.io.BufferedReader br=new java.io.BufferedReader(new java.io.InputStreamReader(in,StandardCharsets.UTF_8));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null){sb.append(line).append("\n");}String s=sb.toString();cb.done(code>=200&&code<300,s);}catch(Exception e){cb.done(false,e.getMessage()==null?"network error":e.getMessage());}}).start();
 }
 public void health(String base,CB cb){new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(base.replaceAll("/$","")+"/health").openConnection();c.setConnectTimeout(8000);c.setReadTimeout(8000);int code=c.getResponseCode();cb.done(code==200,""+code);}catch(Exception e){cb.done(false,e.getMessage());}}).start();}
 public void generate(String base,String idea,String platform,String task,String lang,CB cb){try{JSONObject j=new JSONObject();j.put("idea",idea);j.put("platform",platform);j.put("task",task);j.put("language",lang);post(base.replaceAll("/$","")+"/v1/prompt",j.toString(),(ok,s)->{if(!ok){cb.done(false,s);return;}try{cb.done(true,new JSONObject(s).optString("prompt",s));}catch(Exception e){cb.done(true,s);}});}catch(Exception e){cb.done(false,e.getMessage());}}
}
