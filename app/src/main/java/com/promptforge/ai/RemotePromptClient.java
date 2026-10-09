package com.promptforge.ai;
import java.io.*; import java.net.*; import java.nio.charset.StandardCharsets; import org.json.*;

public final class RemotePromptClient{
 public interface CB{void done(boolean ok,String value);}
 private void request(String method,String url,String token,String body,CB cb){
  new Thread(()->{
   HttpURLConnection c=null;
   try{
    c=(HttpURLConnection)new URL(url).openConnection();
    c.setRequestMethod(method);
    c.setConnectTimeout(10000);
    c.setReadTimeout(30000);
    c.setUseCaches(false);
    c.setDoInput(true);
    c.setRequestProperty("Accept","application/json");
    c.setRequestProperty("Content-Type","application/json; charset=UTF-8");
    if(token!=null&&!token.isEmpty()) c.setRequestProperty("Authorization","Bearer "+token);
    if(body!=null){
     byte[] bytes=body.getBytes(StandardCharsets.UTF_8);
     c.setDoOutput(true);
     c.setFixedLengthStreamingMode(bytes.length);
     try(OutputStream out=c.getOutputStream()){out.write(bytes);}
    }
    int code=c.getResponseCode();
    InputStream raw=code>=200&&code<300?c.getInputStream():c.getErrorStream();
    StringBuilder sb=new StringBuilder();
    if(raw!=null){
     try(BufferedReader br=new BufferedReader(new InputStreamReader(raw,StandardCharsets.UTF_8))){
      String line;
      while((line=br.readLine())!=null)sb.append(line).append('\n');
     }
    }
    cb.done(code>=200&&code<300,code>=200&&code<300?sb.toString().trim():"http_"+code);
   }catch(Exception e){
    cb.done(false,e.getMessage()==null?"network error":e.getMessage());
   }finally{
    if(c!=null)c.disconnect();
   }
  }).start();
 }
 private void post(String url,String token,String body,CB cb){request("POST",url,token,body,cb);}
 private void postPrompt(String url,String token,String body,CB cb){request("POST",url,token,body,(ok,s)->{if(!ok){cb.done(false,s);return;}try{JSONObject response=new JSONObject(s);if("local".equalsIgnoreCase(response.optString("mode",""))){cb.done(false,"ai_unconfigured");return;}String prompt=response.optString("prompt","").trim();if(prompt.isEmpty()){cb.done(false,"empty_prompt");return;}cb.done(true,prompt);}catch(Exception e){cb.done(false,"invalid_prompt_response");}});}
 public void updatePassword(String base,String token,String password,CB cb){try{JSONObject j=new JSONObject();j.put("password",password);request("PATCH",base.replaceAll("/$","")+"/v1/auth/account",token,j.toString(),cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void health(String base,CB cb){new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(base.replaceAll("/$","")+"/health").openConnection();c.setConnectTimeout(8000);c.setReadTimeout(8000);int code=c.getResponseCode();cb.done(code==200,""+code);}catch(Exception e){cb.done(false,e.getMessage());}}).start();}
 public void login(String base,String username,String password,String deviceId,CB cb){try{JSONObject j=new JSONObject();j.put("username",username);j.put("password",password);j.put("deviceId",deviceId);post(base.replaceAll("/$","")+"/v1/auth/login",null,j.toString(),cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void adminLogin(String base,String username,String password,CB cb){try{JSONObject j=new JSONObject();j.put("username",username);j.put("password",password);post(base.replaceAll("/$","")+"/admin/login",null,j.toString(),cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void adminUsers(String base,String token,CB cb){request("GET",base.replaceAll("/$","")+"/admin/users",token,null,cb);}
 public void createAdminUser(String base,String token,String username,String password,String role,CB cb){try{JSONObject j=new JSONObject();j.put("username",username);j.put("password",password);j.put("role",role);request("POST",base.replaceAll("/$","")+"/admin/users",token,j.toString(),cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void updateAdminUser(String base,String token,String username,Boolean enabled,String role,String password,CB cb){try{JSONObject j=new JSONObject();if(enabled!=null)j.put("enabled",enabled);if(role!=null)j.put("role",role);if(password!=null)j.put("password",password);request("PATCH",base.replaceAll("/$","")+"/admin/users/"+URLEncoder.encode(username,"UTF-8"),token,j.toString(),cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void deleteAdminUser(String base,String token,String username,CB cb){try{request("DELETE",base.replaceAll("/$","")+"/admin/users/"+URLEncoder.encode(username,"UTF-8"),token,null,cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void generate(String base,String idea,String platform,String task,String lang,String token,CB cb){try{JSONObject j=new JSONObject();j.put("idea",idea);j.put("platform",platform);j.put("task",task);j.put("language",lang);postPrompt(base.replaceAll("/$","")+"/v1/prompt",token,j.toString(),cb);}catch(Exception e){cb.done(false,e.getMessage());}}
}
