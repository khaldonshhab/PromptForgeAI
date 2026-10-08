package com.promptforge.ai;

import android.content.*;
import android.provider.Settings;
import android.util.Base64;
import org.json.JSONArray;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class Storage{
 private static final String KEYSTORE="AndroidKeyStore";
 private static final String KEY_ALIAS="PromptForgeAccountKey";
 private static final String TOKEN_KEY="account_token_enc";
 private final SharedPreferences p;
 private final Context context;

 public Storage(Context c){
  context=c.getApplicationContext();
  p=context.getSharedPreferences("pf",0);
 }

 public String get(String k,String d){return p.getString(k,d);}
 public void put(String k,String v){p.edit().putString(k,v).apply();}
 public String lang(){return get("lang","");}
 public void lang(String s){put("lang",s);}
 public boolean billingPremium(){return false;}
 public void billingPremium(boolean b){}
 public boolean accountPremium(){return false;}
 public void accountPremium(boolean b){}
 public boolean premium(){return false;}

 public void account(String u,String t){account(u,t,false);}
 public void account(String u,String t,boolean admin){
  String encrypted=encrypt(t);
  SharedPreferences.Editor e=p.edit().putString("account_user",u).putBoolean("account_admin",admin);
  e.putString("account_token",t==null?"":t);
  if(encrypted!=null)e.putString(TOKEN_KEY,encrypted);
  else e.remove(TOKEN_KEY);
  e.apply();
 }

 public boolean isAdmin(){return p.getBoolean("account_admin",false);}
 public String accountUser(){return get("account_user","");}

 public String accountToken(){
  String encrypted=get(TOKEN_KEY,"");
  String token=decrypt(encrypted);
  if(token!=null&&!token.isEmpty())return token;
  return get("account_token","");
 }

 public String deviceId(){return Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);}

 public void logout(){
  p.edit().remove("account_user").remove("account_token").remove(TOKEN_KEY).remove("account_admin").apply();
 }

 private SecretKey key(){
  try{
   KeyStore ks=KeyStore.getInstance(KEYSTORE);
   ks.load(null);
   if(ks.containsAlias(KEY_ALIAS)){
    KeyStore.Entry entry=ks.getEntry(KEY_ALIAS,null);
    if(entry instanceof KeyStore.SecretKeyEntry)return ((KeyStore.SecretKeyEntry)entry).getSecretKey();
   }
   KeyGenerator generator=KeyGenerator.getInstance("AES",KEYSTORE);
   generator.init(128);
   return generator.generateKey();
  }catch(Exception e){
   return null;
  }
 }

 private String encrypt(String plain){
  if(plain==null||plain.isEmpty())return "";
  try{
   SecretKey k=key();
   if(k==null)return null;
   Cipher c=Cipher.getInstance("AES/GCM/NoPadding");
   c.init(Cipher.ENCRYPT_MODE,k);
   byte[] cipher=c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
   byte[] iv=c.getIV();
   return Base64.encodeToString(iv,Base64.NO_WRAP)+":"+Base64.encodeToString(cipher,Base64.NO_WRAP);
  }catch(Exception e){
   return null;
  }
 }

 private String decrypt(String packed){
  if(packed==null||packed.isEmpty())return "";
  try{
   String[] parts=packed.split(":",2);
   if(parts.length!=2)return "";
   byte[] iv=Base64.decode(parts[0],Base64.NO_WRAP);
   byte[] cipher=Base64.decode(parts[1],Base64.NO_WRAP);
   SecretKey k=key();
   if(k==null)return "";
   Cipher c=Cipher.getInstance("AES/GCM/NoPadding");
   c.init(Cipher.DECRYPT_MODE,k,new GCMParameterSpec(128,iv));
   return new String(c.doFinal(cipher),StandardCharsets.UTF_8);
  }catch(Exception e){
   return "";
  }
 }

 public List<String> list(String k){
  List<String> o=new ArrayList<>();
  try{JSONArray a=new JSONArray(get(k,"[]"));for(int i=0;i<a.length();i++)o.add(a.getString(i));}
  catch(Exception ignored){}
  return o;
 }

 public void add(String k,String v){
  List<String> a=list(k);
  a.remove(v);a.add(0,v);
  if(a.size()>100)a=a.subList(0,100);
  JSONArray j=new JSONArray();
  for(String s:a)j.put(s);
  put(k,j.toString());
 }

 public boolean consume(){return true;}
 public void bonus(){}
 public int remaining(){return 999999;}
}
