package com.promptforge.ai;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.*;
import android.view.*;
import android.widget.*;
import org.json.*;

public class AdminActivity extends Activity {
    private static final String BASE="https://promptforge-backend-2p4q.onrender.com";
    private final int BG=Color.rgb(10,12,16), PANEL=Color.rgb(22,25,32), BORDER=Color.rgb(55,60,70), WHITE=Color.WHITE, MUTED=Color.rgb(170,176,188), ACCENT=Color.rgb(92,150,255);
    private LinearLayout root, list;
    private String token;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        token=getIntent().getStringExtra("admin_token");
        build();
        loadUsers();
    }

    private void build(){
        ScrollView sv=new ScrollView(this);
        root=box(20);
        sv.addView(root);
        setContentView(sv);

        TextView title=txt("لوحة تحكم الأدمن",26,WHITE,true);
        title.setGravity(Gravity.RIGHT);
        root.addView(title,new LinearLayout.LayoutParams(-1,dp(55)));

        TextView sub=txt("إدارة الحسابات والصلاحيات",14,MUTED,false);
        sub.setGravity(Gravity.RIGHT);
        root.addView(sub,new LinearLayout.LayoutParams(-1,dp(35)));
        gap(12);

        TextView add=button("＋  إضافة حساب",true);
        add.setOnClickListener(v->addDialog());
        root.addView(add,new LinearLayout.LayoutParams(-1,dp(58)));
        gap(12);

        TextView refresh=button("↻  تحديث الحسابات",false);
        refresh.setOnClickListener(v->loadUsers());
        root.addView(refresh,new LinearLayout.LayoutParams(-1,dp(52)));
        gap(18);

        list=box(0);
        root.addView(list,new LinearLayout.LayoutParams(-1,-2));
    }

    private void loadUsers(){
        if(token==null||token.isEmpty()){toast("جلسة الأدمن غير صالحة");finish();return;}
        new RemotePromptClient().adminUsers(BASE,token,(ok,val)->runOnUiThread(()->{
            if(!ok){toast(error(val,"تعذر تحميل الحسابات"));return;}
            try{
                JSONArray a=new JSONObject(val).optJSONArray("users");
                list.removeAllViews();
                if(a==null||a.length()==0){
                    TextView e=txt("لا توجد حسابات مضافة بعد.",15,MUTED,false);
                    e.setGravity(Gravity.CENTER);
                    list.addView(e,new LinearLayout.LayoutParams(-1,dp(80)));
                    return;
                }
                for(int i=0;i<a.length();i++) addUserRow(a.getJSONObject(i));
            }catch(Exception e){toast("تعذر قراءة بيانات الحسابات");}
        }));
    }

    private void addUserRow(JSONObject u){
        String name=u.optString("username","");
        boolean enabled=u.optBoolean("enabled",true);
        String role=u.optString("role","user");
        LinearLayout card=box(16);
        TextView nameTv=txt(name,19,WHITE,true);
        nameTv.setGravity(Gravity.RIGHT);
        card.addView(nameTv,new LinearLayout.LayoutParams(-1,dp(40)));

        TextView info=txt((role.equals("admin")?"أدمن":"مستخدم عادي")+"  •  "+(enabled?"نشط":"متوقف"),14,enabled?Color.rgb(100,210,140):Color.rgb(240,130,130),true);
        info.setGravity(Gravity.RIGHT);
        card.addView(info,new LinearLayout.LayoutParams(-1,dp(34)));
        gap(card,8);

        LinearLayout actions=new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);

        TextView toggle=button(enabled?"إيقاف الحساب":"تفعيل الحساب",false);
        toggle.setOnClickListener(v->{
            toggle.setEnabled(false);
            new RemotePromptClient().updateAdminUser(BASE,token,name,!enabled,null,null,(ok,val)->runOnUiThread(()->{
                toggle.setEnabled(true);
                if(!ok){toast(error(val,"تعذر تعديل حالة الحساب"));return;}
                loadUsers();
            }));
        });
        actions.addView(toggle,new LinearLayout.LayoutParams(-1,dp(50)));
        gap(actions,6);

        TextView type=button("نوع الحساب: "+(role.equals("admin")?"أدمن":"مستخدم عادي"),false);
        type.setOnClickListener(v->changeRole(name,role));
        actions.addView(type,new LinearLayout.LayoutParams(-1,dp(50)));
        gap(actions,6);

        TextView pass=button("إعادة ضبط كلمة المرور",false);
        pass.setOnClickListener(v->resetPassword(name));
        actions.addView(pass,new LinearLayout.LayoutParams(-1,dp(50)));
        gap(actions,6);

        TextView del=button("حذف الحساب",false);
        del.setTextColor(Color.rgb(255,120,120));
        del.setOnClickListener(v->confirmDelete(name));
        actions.addView(del,new LinearLayout.LayoutParams(-1,dp(50)));

        card.addView(actions,new LinearLayout.LayoutParams(-1,-2));
        list.addView(card,new LinearLayout.LayoutParams(-1,-2));
        gap(list,10);
    }

    private void addDialog(){
        LinearLayout box=box(0);
        EditText user=input("اسم المستخدم");
        EditText pass=input("كلمة المرور (8 أحرف على الأقل)");
        pass.setInputType(0x00000081);
        box.addView(user,new LinearLayout.LayoutParams(-1,dp(55))); gap(box,8);
        box.addView(pass,new LinearLayout.LayoutParams(-1,dp(55))); gap(box,10);

        Spinner role=new Spinner(this);
        String[] roles={"مستخدم عادي","أدمن"};
        ArrayAdapter<String> ad=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,roles);
        role.setAdapter(ad);
        box.addView(role,new LinearLayout.LayoutParams(-1,dp(52)));

        AlertDialog d=new AlertDialog.Builder(this).setTitle("إضافة حساب").setView(box)
            .setPositiveButton("إضافة",null).setNegativeButton("إلغاء",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            String u=user.getText().toString().trim(), p=pass.getText().toString();
            if(u.length()<3||p.length()<8){toast("اسم المستخدم 3 أحرف على الأقل وكلمة المرور 8 أحرف على الأقل");return;}
            String r=role.getSelectedItemPosition()==1?"admin":"user";
            d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            new RemotePromptClient().createAdminUser(BASE,token,u,p,r,(ok,val)->runOnUiThread(()->{
                d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                if(!ok){toast(error(val,"تعذر إنشاء الحساب"));return;}
                d.dismiss(); toast("تم إنشاء الحساب"); loadUsers();
            }));
        }));
        d.show();
    }

    private void changeRole(String username,String current){
        String[] items={"مستخدم عادي","أدمن"};
        int checked=current.equals("admin")?1:0;
        new AlertDialog.Builder(this).setTitle("نوع الحساب")
            .setSingleChoiceItems(items,checked,(d,w)->{
                String r=w==1?"admin":"user";
                d.dismiss();
                new RemotePromptClient().updateAdminUser(BASE,token,username,null,r,null,(ok,val)->runOnUiThread(()->{
                    if(!ok){toast(error(val,"تعذر تغيير نوع الحساب"));return;}
                    toast("تم تغيير نوع الحساب"); loadUsers();
                }));
            }).setNegativeButton("إلغاء",null).show();
    }

    private void resetPassword(String username){
        EditText p=input("كلمة المرور الجديدة (8 أحرف على الأقل)");
        p.setInputType(0x00000081);
        new AlertDialog.Builder(this).setTitle("إعادة ضبط كلمة المرور").setMessage("الحساب: "+username).setView(p)
            .setPositiveButton("حفظ",(d,w)->{
                String pass=p.getText().toString();
                if(pass.length()<8){toast("كلمة المرور يجب أن تكون 8 أحرف على الأقل");return;}
                new RemotePromptClient().updateAdminUser(BASE,token,username,null,null,pass,(ok,val)->runOnUiThread(()->toast(ok?"تم تغيير كلمة المرور":error(val,"تعذر تغيير كلمة المرور"))));
            }).setNegativeButton("إلغاء",null).show();
    }

    private void confirmDelete(String username){
        new AlertDialog.Builder(this).setTitle("حذف الحساب")
            .setMessage("هل تريد حذف الحساب «"+username+"» نهائياً؟")
            .setPositiveButton("حذف",(d,w)->new RemotePromptClient().deleteAdminUser(BASE,token,username,(ok,val)->runOnUiThread(()->{
                if(ok){toast("تم حذف الحساب");loadUsers();}else toast(error(val,"تعذر حذف الحساب"));
            }))).setNegativeButton("إلغاء",null).show();
    }

    private EditText input(String hint){
        EditText e=new EditText(this);
        e.setHint(hint); e.setTextColor(WHITE); e.setHintTextColor(MUTED); e.setTextSize(15);
        e.setPadding(dp(14),0,dp(14),0); e.setSingleLine(true);
        e.setBackgroundColor(PANEL); e.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        return e;
    }

    private TextView button(String s,boolean primary){
        TextView t=txt(s,15,WHITE,true);
        t.setGravity(Gravity.CENTER); t.setPadding(dp(12),0,dp(12),0);
        t.setBackgroundColor(primary?ACCENT:PANEL);
        return t;
    }

    private TextView txt(String s,float size,int color,boolean bold){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL); t.setFontFeatureSettings("kern");
        return t;
    }

    private LinearLayout box(int pad){
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(pad),dp(pad),dp(pad),dp(pad)); l.setBackgroundColor(pad>0?PANEL:Color.TRANSPARENT); return l;
    }
    private void gap(int h){gap(root,h);}
    private void gap(ViewGroup p,int h){Space s=new Space(this);p.addView(s,new LinearLayout.LayoutParams(1,dp(h)));}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private String error(String raw,String fallback){try{return new JSONObject(raw).optString("error",fallback);}catch(Exception e){return fallback;}}
}
