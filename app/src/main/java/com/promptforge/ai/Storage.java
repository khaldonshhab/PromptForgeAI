package com.promptforge.ai;
import android.content.*; import org.json.JSONArray; import java.text.*; import java.util.*;
public final class Storage{
 private final SharedPreferences p; public Storage(Context c){p=c.getSharedPreferences("pf",0);}
 public String get(String k,String d){return p.getString(k,d);} public void put(String k,String v){p.edit().putString(k,v).apply();}
 public String lang(){return get("lang","");} public void lang(String s){put("lang",s);} public boolean premium(){return p.getBoolean("premium",false);} public void premium(boolean b){p.edit().putBoolean("premium",b).apply();}
 public List<String> list(String k){List<String> o=new ArrayList<>();try{JSONArray a=new JSONArray(get(k,"[]"));for(int i=0;i<a.length();i++)o.add(a.getString(i));}catch(Exception ignored){}return o;}
 public void add(String k,String v){List<String> a=list(k);a.remove(v);a.add(0,v);if(a.size()>100)a=a.subList(0,100);JSONArray j=new JSONArray();for(String s:a)j.put(s);put(k,j.toString());}
 public boolean consume(){if(premium())return true;String d=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());String sd=get("usage_day","");int n=sd.equals(d)?p.getInt("usage",0):0;if(n>=5)return false;p.edit().putString("usage_day",d).putInt("usage",n+1).apply();return true;}
 public int remaining(){if(premium())return 999999;String d=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());int n=get("usage_day","").equals(d)?p.getInt("usage",0):0;return Math.max(0,5-n);}
}
