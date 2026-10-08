package com.promptforge.ai;
import android.content.*; import org.json.JSONArray; import java.util.*;
public final class Storage{
 private final SharedPreferences p; public Storage(Context c){p=c.getSharedPreferences("pf",0);}
 public String get(String k,String d){return p.getString(k,d);} public void put(String k,String v){p.edit().putString(k,v).apply();}
 public String lang(){return get("lang","");} public void lang(String s){put("lang",s);}
 public boolean billingPremium(){return false;} public void billingPremium(boolean b){}
 public boolean accountPremium(){return false;} public void accountPremium(boolean b){}
 public boolean premium(){return false;}
 public void account(String u,String t){account(u,t,false);}
 public void account(String u,String t,boolean admin){p.edit().putString("account_user",u).putString("account_token",t).putBoolean("account_admin",admin).apply();}
 public boolean isAdmin(){return p.getBoolean("account_admin",false);}
 public String accountUser(){return get("account_user","");} public String accountToken(){return get("account_token","");}
 public void logout(){p.edit().remove("account_user").remove("account_token").remove("account_admin").apply();}
 public List<String> list(String k){List<String> o=new ArrayList<>();try{JSONArray a=new JSONArray(get(k,"[]"));for(int i=0;i<a.length();i++)o.add(a.getString(i));}catch(Exception ignored){}return o;}
 public void add(String k,String v){List<String> a=list(k);a.remove(v);a.add(0,v);if(a.size()>100)a=a.subList(0,100);JSONArray j=new JSONArray();for(String s:a)j.put(s);put(k,j.toString());}
 public boolean consume(){return true;}
 public void bonus(){}
 public int remaining(){return 999999;}
}