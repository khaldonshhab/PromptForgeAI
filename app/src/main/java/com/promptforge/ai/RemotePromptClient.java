package com.promptforge.ai;
import java.io.*; import java.net.*; import java.nio.charset.StandardCharsets; import org.json.*;

public final class RemotePromptClient{
 public interface CB{void done(boolean ok,String value);}
 private void request(String method,String url,String token,String body,CB cb){
  new Thread(()->{try{
   HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
   c.setRequestMethod(method); c.setConnectTimeout(10000); c.setReadTimeout(30000);
   c.setRequestProperty("Content-Type","application/json");
   if(token!=null&&!token.isEmpty()) c.setRequestProperty("Authorization","Bearer "+token);
   if(body!=null){c.setDoOutput(true);c.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));}
   int code=c.getResponseCode(); InputStream in=code>=200&&code<300?c.getInputStream():c.getErrorStream();
   BufferedReader br=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8)); StringBuilder sb=new StringBuilder(); String line;
   while((line=br.readLine())!=null)sb.append(line).append("\n");
   cb.done(code>=200&&code<300,sb.toString());
  }catch(Exception e){cb.done(false,e.getMessage()==null?"network error":e.getMessage());}}).start();
 }
 private void post(String url,String body,CB cb){request("POST",url,null,body,cb);}
 public void updatePassword(String base,String token,String password,CB cb){try{JSONObject j=new JSONObject();j.put("password",password);request("PATCH",base.replaceAll("/$","")+"/v1/auth/account",token,j.toString(),cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void health(String base,CB cb){new Thread(()->{try{HttpURLConnection c=(HttpURLConnection)new URL(base.replaceAll("/$","")+"/health").openConnection();c.setConnectTimeout(8000);c.setReadTimeout(8000);int code=c.getResponseCode();cb.done(code==200,""+code);}catch(Exception e){cb.done(false,e.getMessage());}}).start();}
 public void login(String base,String username,String password,String deviceId,CB cb){try{JSONObject j=new JSONObject();j.put("username",username);j.put("password",password);j.put("deviceId",deviceId);post(base.replaceAll("/$","")+"/v1/auth/login",j.toString(),cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void adminLogin(String base,String username,String password,CB cb){try{JSONObject j=new JSONObject();j.put("username",username);j.put("password",password);post(base.replaceAll("/$","")+"/admin/login",j.toString(),cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void adminUsers(String base,String token,CB cb){request("GET",base.replaceAll("/$","")+"/admin/users",token,null,cb);}
 public void createAdminUser(String base,String token,String username,String password,String role,CB cb){try{JSONObject j=new JSONObject();j.put("username",username);j.put("password",password);j.put("role",role);request("POST",base.replaceAll("/$","")+"/admin/users",token,j.toString(),cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void updateAdminUser(String base,String token,String username,Boolean enabled,String role,String password,CB cb){try{JSONObject j=new JSONObject();if(enabled!=null)j.put("enabled",enabled);if(role!=null)j.put("role",role);if(password!=null)j.put("password",password);request("PATCH",base.replaceAll("/$","")+"/admin/users/"+URLEncoder.encode(username,"UTF-8"),token,j.toString(),cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void deleteAdminUser(String base,String token,String username,CB cb){try{request("DELETE",base.replaceAll("/$","")+"/admin/users/"+URLEncoder.encode(username,"UTF-8"),token,null,cb);}catch(Exception e){cb.done(false,e.getMessage());}}
 public void generate(String base,String idea,String platform,String task,String lang,String token,CB cb){try{JSONObject j=new JSONObject();j.put("idea",idea);j.put("platform",platform);j.put("task",task);j.put("language",lang);if(token!=null&&!token.isEmpty())j.put("token",token);post(base.replaceAll("/$","")+"/v1/prompt",j.toString(),(ok,s)->{if(!ok){cb.done(false,s);return;}try{cb.done(true,new JSONObject(s).optString("prompt",s));}catch(Exception e){cb.done(true,s);}});}catch(Exception e){cb.done(false,e.getMessage());}}
}
