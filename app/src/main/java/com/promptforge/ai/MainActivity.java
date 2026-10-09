package com.promptforge.ai;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.text.style.ForegroundColorSpan;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final String BASE_URL="https://promptforge-backend-2p4q.onrender.com";
    private Storage s;
    private List<Platform> ps;
    private String lang="", sel="ChatGPT", task="General", last="";
    private String currentScreen="login";

    private final int BG=Color.rgb(3,7,19);
    private final int PANEL=Color.rgb(9,17,37);
    private final int PANEL2=Color.rgb(11,22,45);
    private final int PANEL3=Color.rgb(15,28,57);
    private final int BORDER=Color.rgb(34,52,88);
    private final int BORDER2=Color.rgb(48,69,112);
    private final int BLUE=Color.rgb(22,190,255);
    private final int CYAN=Color.rgb(52,215,255);
    private final int PURPLE=Color.rgb(139,76,255);
    private final int VIOLET=Color.rgb(112,76,255);
    private final int WHITE=Color.rgb(246,248,255);
    private final int MUTED=Color.rgb(164,179,211);
    private final int MUTED2=Color.rgb(119,139,177);
    private final int SUCCESS=Color.rgb(88,226,166);

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(true);
        s=new Storage(this);
        ps=PlatformRepository.all();
        lang=s.lang();
        if(lang.isEmpty()){lang="ar";s.lang(lang);}
        showSplash();
    }

    @Override public void onBackPressed(){
        if("home".equals(currentScreen)||"login".equals(currentScreen)||"splash".equals(currentScreen)){
            confirmExit();
        }else{
            home();
        }
    }

    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private boolean ar(){return "ar".equals(lang);}
    private String tr(String a,String e){return ar()?a:e;}

    private String bidi(String x){
        if(x==null||!ar())return x;
        StringBuilder out=new StringBuilder();
        boolean latin=false;
        for(int i=0;i<x.length();i++){
            char ch=x.charAt(i);
            boolean now=(ch<128&&(Character.isLetterOrDigit(ch)||".:/_-@+#%()".indexOf(ch)>=0));
            if(now&&!latin)out.append('\u2066');
            if(!now&&latin)out.append('\u2069');
            out.append(ch);
            latin=now;
        }
        if(latin)out.append('\u2069');
        return out.toString();
    }

    private GradientDrawable rounded(int fill,int stroke,int radius){
        GradientDrawable d=new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radius));
        if(stroke!=0)d.setStroke(dp(1),stroke);
        return d;
    }

    private GradientDrawable gradient(int radius){
        GradientDrawable d=new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{BLUE,VIOLET,PURPLE});
        d.setCornerRadius(dp(radius));
        return d;
    }

    private LinearLayout column(){
        LinearLayout r=new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setBackgroundColor(Color.TRANSPARENT);
        r.setPadding(dp(18),dp(10),dp(18),dp(24));
        r.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        return r;
    }

    private ScrollView scroll(View child){
        ScrollView sc=new ScrollView(this);
        sc.setFillViewport(true);
        sc.setClipToPadding(false);
        sc.setVerticalScrollBarEnabled(false);
        sc.addView(child);
        return sc;
    }

    private FrameLayout screenFrame(){
        FrameLayout f=new FrameLayout(this);
        f.setBackgroundColor(Color.TRANSPARENT);
        return f;
    }

    private void showRoot(View v){
        FrameLayout host=new FrameLayout(this);
        host.setBackgroundColor(BG);
        host.addView(new AmbientBackgroundView(this),new FrameLayout.LayoutParams(-1,-1));
        host.addView(v,new FrameLayout.LayoutParams(-1,-1));
        setContentView(host);

        host.setOnApplyWindowInsetsListener((view,insets)->{
            int top,bottom;
            if(Build.VERSION.SDK_INT>=30){
                android.graphics.Insets z=insets.getInsets(WindowInsets.Type.systemBars());
                top=z.top;bottom=z.bottom;
            }else{
                top=insets.getSystemWindowInsetTop();bottom=insets.getSystemWindowInsetBottom();
            }
            v.setPadding(v.getPaddingLeft(),v.getPaddingTop()+top,v.getPaddingRight(),v.getPaddingBottom()+bottom);
            return insets;
        });
        host.requestApplyInsets();
    }

    private TextView text(String value,float size,int color,boolean bold){
        TextView t=new TextView(this);
        t.setText(bidi(value));
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));
        t.setIncludeFontPadding(true);
        t.setTextDirection(ar()?View.TEXT_DIRECTION_FIRST_STRONG:View.TEXT_DIRECTION_LTR);
        t.setGravity(ar()?Gravity.RIGHT:Gravity.LEFT);
        t.setMaxLines(40);
        return t;
    }

    private TextView brandText(float size){
        TextView t=new TextView(this);
        SpannableString ss=new SpannableString("PromptForgeAI");
        ss.setSpan(new ForegroundColorSpan(WHITE),0,11,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(PURPLE),11,13,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        t.setText(ss);
        t.setTextSize(size);
        t.setTypeface(Typeface.create("sans",Typeface.BOLD));
        t.setGravity(Gravity.CENTER);
        t.setTextDirection(View.TEXT_DIRECTION_LTR);
        return t;
    }

    private TextView pill(String value){
        TextView t=text(value,12,MUTED,false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12),0,dp(12),0);
        t.setBackground(rounded(PANEL2,BORDER,18));
        return t;
    }

    private TextView button(String value,boolean primary){
        TextView b=text(value,15,WHITE,true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(12),0,dp(12),0);
        b.setClickable(true);
        b.setFocusable(true);
        b.setBackground(primary?gradient(21):rounded(PANEL2,BORDER2,21));
        return b;
    }

    private TextView iconButton(String kind,String label,boolean primary,View.OnClickListener click){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        row.setPadding(dp(10),0,dp(10),0);
        row.setBackground(primary?gradient(22):rounded(PANEL2,BORDER2,22));
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(click);

        PFIconView ic=new PFIconView(this,kind,WHITE);
        row.addView(ic,new LinearLayout.LayoutParams(dp(28),dp(28)));
        TextView t=text(label,14,WHITE,true);
        t.setGravity(Gravity.CENTER);
        row.addView(t,new LinearLayout.LayoutParams(0,dp(52),1));
        return makeButtonContainer(row);
    }

    private TextView makeButtonContainer(LinearLayout row){
        TextView dummy=new TextView(this);
        dummy.setVisibility(View.GONE);
        return dummy;
    }

    private FrameLayout panel(){
        FrameLayout f=new FrameLayout(this);
        f.setBackground(rounded(PANEL,BORDER,26));
        return f;
    }

    private FrameLayout panel(int radius){
        FrameLayout f=new FrameLayout(this);
        f.setBackground(rounded(PANEL,BORDER,radius));
        return f;
    }

    private void addGap(ViewGroup r,int h){
        Space sp=new Space(this);
        r.addView(sp,new LinearLayout.LayoutParams(1,dp(h)));
    }

    private void titleBar(LinearLayout root,String arTitle,String enTitle){
        FrameLayout bar=new FrameLayout(this);

        TextView t=text(tr(arTitle,enTitle),24,WHITE,true);
        t.setGravity(Gravity.CENTER);
        bar.addView(t,new FrameLayout.LayoutParams(-1,dp(54)));

        // Explicit in-app Back control. It uses the same navigation action as the
        // existing Android back handling: return to Home from secondary screens.
        LinearLayout back=new LinearLayout(this);
        back.setGravity(Gravity.CENTER);
        back.setOrientation(LinearLayout.HORIZONTAL);
        back.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        back.setPadding(dp(10),0,dp(10),0);
        back.setBackground(rounded(PANEL2,BORDER2,18));
        back.setClickable(true);
        back.setFocusable(true);
        back.setContentDescription(tr("رجوع","Back"));
        PFIconView bic=new PFIconView(this,ar()?"arrowRight":"arrowLeft",WHITE);
        back.addView(bic,new LinearLayout.LayoutParams(dp(24),dp(24)));
        TextView bt=text(tr("رجوع","Back"),13,WHITE,true);
        bt.setGravity(Gravity.CENTER);
        back.addView(bt,new LinearLayout.LayoutParams(-2,dp(44)));
        back.setOnClickListener(v->MainActivity.this.onBackPressed());

        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(dp(88),dp(44));
        bp.gravity=(ar()?Gravity.LEFT:Gravity.RIGHT)|Gravity.CENTER_VERTICAL;
        bp.setMargins(dp(2),0,dp(2),0);
        bar.addView(back,bp);

        TextView mini=text("PROMPTFORGE",10,MUTED2,true);
        mini.setTextDirection(View.TEXT_DIRECTION_LTR);
        mini.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams mp=new FrameLayout.LayoutParams(dp(86),dp(24));
        mp.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        bar.addView(mini,mp);

        View line=new View(this);
        line.setBackgroundColor(Color.rgb(24,38,68));
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,dp(1));
        lp.gravity=Gravity.BOTTOM;
        bar.addView(line,lp);
        root.addView(bar,new LinearLayout.LayoutParams(-1,dp(56)));
    }

    private View loginHeader(){
        FrameLayout top=new FrameLayout(this);
        top.setPadding(0,0,0,dp(3));

        TextView brand=brandText(19);
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-2,dp(40));
        bp.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
        top.addView(brand,bp);

        View langBtn=languageControl();
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(40),dp(40));
        lp.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
        top.addView(langBtn,lp);

        View line=new View(this);
        line.setBackgroundColor(Color.rgb(23,37,66));
        FrameLayout.LayoutParams dl=new FrameLayout.LayoutParams(-1,dp(1));
        dl.gravity=Gravity.BOTTOM;
        top.addView(line,dl);
        return top;
    }

    private View languageControl(){
        FrameLayout wrap=new FrameLayout(this);
        wrap.setBackground(rounded(PANEL2,BORDER2,13));
        wrap.setClickable(true);
        wrap.setFocusable(true);

        View flag=ar()?new SyrianFlagView(this):new UKFlagView(this);
        wrap.addView(flag,new FrameLayout.LayoutParams(dp(28),dp(18),Gravity.CENTER));
        wrap.setContentDescription(ar()?"تغيير لغة التطبيق":"Change app language");
        wrap.setOnClickListener(v->languageDialog());
        return wrap;
    }

    private void languageDialog(){
        final Dialog d=designDialog();
        LinearLayout box=dialogBox();
        TextView title=text(tr("لغة التطبيق","App language"),20,WHITE,true);
        title.setGravity(Gravity.CENTER);
        box.addView(title,new LinearLayout.LayoutParams(-1,dp(42)));
        addGap(box,10);

        LinearLayout arRow=languageRow(new SyrianFlagView(this),"العربية");
        arRow.setOnClickListener(v->{lang="ar";s.lang(lang);d.dismiss();refreshScreen();});
        box.addView(arRow,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(box,8);

        LinearLayout enRow=languageRow(new UKFlagView(this),"English");
        enRow.setOnClickListener(v->{lang="en";s.lang(lang);d.dismiss();refreshScreen();});
        box.addView(enRow,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(box,12);

        TextView cancel=button(tr("إلغاء","Cancel"),false);
        cancel.setOnClickListener(v->d.dismiss());
        box.addView(cancel,new LinearLayout.LayoutParams(-1,dp(52)));

        d.setContentView(box);
        sizeDialog(d,0.88f);
        d.show();
        sizeDialog(d,0.88f);
    }

    private void refreshScreen(){
        if("settings".equals(currentScreen))settings();
        else if("account".equals(currentScreen))account();
        else if("home".equals(currentScreen))home();
        else loginScreen();
    }

    private LinearLayout dialogBox(){
        LinearLayout b=new LinearLayout(this);
        b.setOrientation(LinearLayout.VERTICAL);
        b.setPadding(dp(20),dp(20),dp(20),dp(20));
        b.setBackground(rounded(PANEL,0,28));
        return b;
    }

    private Dialog designDialog(){
        final Dialog d=new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window w=d.getWindow();
        if(w!=null)w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        d.setCanceledOnTouchOutside(true);
        return d;
    }

    private void sizeDialog(Dialog d,float width){
        Window w=d.getWindow();
        if(w!=null){
            WindowManager.LayoutParams p=w.getAttributes();
            p.width=(int)(getResources().getDisplayMetrics().widthPixels*width);
            p.height=WindowManager.LayoutParams.WRAP_CONTENT;
            p.dimAmount=0.76f;
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            w.setAttributes(p);
        }
    }

    private LinearLayout languageRow(View flag,String label){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12),0,dp(12),0);
        row.setBackground(rounded(PANEL2,BORDER,18));
        row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        row.addView(flag,new LinearLayout.LayoutParams(dp(30),dp(20)));
        TextView t=text(label,16,WHITE,true);
        t.setGravity(Gravity.CENTER);
        t.setTextDirection(label.equals("English")?View.TEXT_DIRECTION_LTR:View.TEXT_DIRECTION_RTL);
        row.addView(t,new LinearLayout.LayoutParams(0,dp(52),1));
        return row;
    }

    private void showSplash(){
        setScreen("splash");
        FrameLayout root=screenFrame();

        LinearLayout center=new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER_HORIZONTAL);

        PromptForgeLogoView logo=new PromptForgeLogoView(this);
        center.addView(logo,new LinearLayout.LayoutParams(dp(170),dp(170)));
        addGap(center,18);

        TextView title=brandText(31);
        center.addView(title,new LinearLayout.LayoutParams(-1,dp(46)));

        TextView sub=text(tr("هندسة برومبتات دقيقة","Precision Prompt Engineering"),14,MUTED,false);
        sub.setGravity(Gravity.CENTER);
        center.addView(sub,new LinearLayout.LayoutParams(-1,dp(34)));

        addGap(center,28);
        GradientLine progress=new GradientLine(this);
        center.addView(progress,new LinearLayout.LayoutParams(dp(160),dp(4)));

        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,-2);
        cp.gravity=Gravity.CENTER;
        cp.leftMargin=dp(22);cp.rightMargin=dp(22);
        root.addView(center,cp);
        showRoot(root);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Restore the saved session; credentials remain encrypted in Storage.
            if (!s.accountUser().trim().isEmpty() && !s.accountToken().trim().isEmpty()) {
                home();
            } else {
                loginScreen();
            }
        }, 250);
    }

    private void loginScreen(){
        setScreen("login");
        LinearLayout r=column();
        r.setGravity(Gravity.CENTER_HORIZONTAL);

        r.addView(loginHeader(),new LinearLayout.LayoutParams(-1,dp(54)));
        addGap(r,16);

        PromptForgeLogoView logo=new PromptForgeLogoView(this);
        LinearLayout holder=new LinearLayout(this);
        holder.setGravity(Gravity.CENTER);
        holder.addView(logo,new LinearLayout.LayoutParams(dp(104),dp(104)));
        r.addView(holder,new LinearLayout.LayoutParams(-1,dp(110)));

        addGap(r,10);
        TextView head=text(tr("مرحباً بعودتك","Welcome back"),27,WHITE,true);
        head.setGravity(Gravity.CENTER);
        r.addView(head,new LinearLayout.LayoutParams(-1,dp(42)));

        TextView sub=text(tr("ادخل إلى مساحة هندسة البرومبتات الخاصة بك","Enter your prompt-engineering workspace"),13,MUTED,false);
        sub.setGravity(Gravity.CENTER);
        r.addView(sub,new LinearLayout.LayoutParams(-1,dp(34)));
        addGap(r,18);

        LinearLayout form=new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(16),dp(16),dp(16),dp(16));
        form.setBackground(rounded(PANEL,BORDER2,28));

        EditText user=editor("اسم المستخدم","Username",1);
        user.setSingleLine(true);
        form.addView(user,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(form,10);

        EditText pass=editor("كلمة المرور","Password",1);
        pass.setSingleLine(true);
        pass.setInputType(0x00000081);
        form.addView(pass,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(form,14);

        TextView login=button(tr("دخول","Sign in"),true);

        LinearLayout loginProgress=new LinearLayout(this);
        loginProgress.setGravity(Gravity.CENTER);
        loginProgress.setVisibility(View.GONE);
        loginProgress.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);

        ProgressBar loginSpinner=new ProgressBar(this);
        loginSpinner.setIndeterminate(true);
        loginSpinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(CYAN));
        loginProgress.addView(loginSpinner,new LinearLayout.LayoutParams(dp(18),dp(18)));

        TextView loginStatus=text(tr("جارٍ تسجيل الدخول…","Signing in…"),12,MUTED,false);
        loginStatus.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusLp=new LinearLayout.LayoutParams(-2,dp(28));
        statusLp.setMargins(dp(8),0,0,0);
        loginProgress.addView(loginStatus,statusLp);

        login.setOnClickListener(v->{
            String u=user.getText().toString().trim(),p=pass.getText().toString();
            if(u.isEmpty()||p.isEmpty()){toast(tr("أدخل اسم المستخدم وكلمة المرور","Enter username and password"));return;}
            login.setEnabled(false);
            login.setAlpha(0.65f);
            loginProgress.setVisibility(View.VISIBLE);
            new RemotePromptClient().login(BASE_URL,u,p,s.deviceId(),(ok,val)->runOnUiThread(()->{
                login.setEnabled(true);
                login.setAlpha(1f);
                loginProgress.setVisibility(View.GONE);
                if(!ok){
                    String err=val==null?"":val.trim();
                    if(err.contains("auth_not_configured"))toast(tr("الخادم غير مهيأ للمصادقة","Server authentication is not configured"));
                    else if(err.contains("invalid_credentials"))toast(tr("اسم المستخدم أو كلمة المرور غير صحيحين","Invalid username or password"));
                    else if(err.contains("device_already_bound"))toast(tr("هذا الحساب مستخدم على جهاز آخر","This account is already bound to another device"));
                    else if(err.contains("device_id_required"))toast(tr("تعذر التعرف على الجهاز","Could not identify this device"));
                    else if(err.isEmpty())toast(tr("تعذر الاتصال بالخادم","Could not reach the server"));
                    else toast(tr("تعذر تسجيل الدخول: "+err,"Sign-in failed: "+err));
                    return;
                }
                try{
                    org.json.JSONObject j=new org.json.JSONObject(val);
                    boolean admin=j.optBoolean("admin",false);
                    s.account(j.optString("username",""),j.optString("token",""),admin);
                    s.accountPremium(false);
                    toast(tr("تم تسجيل الدخول","Signed in"));
                    home();
                }catch(Exception e){
                    toast(tr("تعذر قراءة استجابة الخادم","Invalid server response"));
                }
            }));
        });
        form.addView(login,new LinearLayout.LayoutParams(-1,dp(60)));
        form.addView(loginProgress,new LinearLayout.LayoutParams(-1,dp(32)));

        TextView hint=text(tr("مساحتك الخاصة لصناعة برومبتات أدق وأقوى","Your private workspace for better prompts"),12,MUTED2,false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0,dp(10),0,0);
        form.addView(hint,new LinearLayout.LayoutParams(-1,dp(30)));

        r.addView(form,new LinearLayout.LayoutParams(-1,-2));
        addGap(r,12);

        showRoot(scroll(r));
    }

    private void home(){
        setScreen("home");
        FrameLayout root=screenFrame();

        ScrollView sc=new ScrollView(this);
        sc.setFillViewport(true);
        sc.setVerticalScrollBarEnabled(false);

        LinearLayout r=column();
        r.setPadding(dp(18),dp(10),dp(18),dp(108));

        FrameLayout header=homeHeader();
        r.addView(header,new LinearLayout.LayoutParams(-1,dp(56)));
        addGap(r,14);

        r.addView(welcomeCard(),new LinearLayout.LayoutParams(-1,dp(94)));
        addGap(r,18);

        TextView quickHead=text(tr("مساحة العمل","Workspace"),18,WHITE,true);
        r.addView(quickHead,new LinearLayout.LayoutParams(-1,dp(30)));
        addGap(r,8);

        LinearLayout quickRow=new LinearLayout(this);
        quickRow.setOrientation(LinearLayout.HORIZONTAL);
        quickRow.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        View create=quickAction(tr("إنشاء برومبت","Create Prompt"),tr("ابدأ من فكرة جديدة","Start from an idea"),"spark",true,v->create());
        View improve=quickAction(tr("تحسين برومبت","Improve Prompt"),tr("طوّر برومبت موجوداً","Upgrade an existing prompt"),"edit",false,v->improve());
        quickRow.addView(create,new LinearLayout.LayoutParams(0,dp(96),1));
        Space qg=new Space(this); quickRow.addView(qg,new LinearLayout.LayoutParams(dp(9),1));
        quickRow.addView(improve,new LinearLayout.LayoutParams(0,dp(96),1));
        r.addView(quickRow,new LinearLayout.LayoutParams(-1,dp(96)));

        addGap(r,18);
        View random=wideAction(tr("برومبت عشوائي","Random Prompt"),tr("صورة، قصيدة، نكتة أو أغنية — مع اسمك أو اسم أي شخص","Image, poem, joke or song — with your name or anyone's"),"spark",v->randomPrompt());
        r.addView(random,new LinearLayout.LayoutParams(-1,dp(72)));

        addGap(r,14);
        View fun=wideAction(tr("اختبارات وترفيه","Fun & Compatibility"),tr("العمر المتوقع للتسلية، توافق الحبيبين، وتوقع الثروة بعد 10 سنوات","For-fun life reading, couple compatibility and 10-year wealth prediction"),"spark",v->funCenter());
        r.addView(fun,new LinearLayout.LayoutParams(-1,dp(72)));

        addGap(r,20);
        TextView ph=text(tr("اختر الأداة التي تريد استخدامها","Choose the tool you want to use"),19,WHITE,true);
        r.addView(ph,new LinearLayout.LayoutParams(-1,dp(32)));
        addGap(r,4);
        TextView psNote=text(tr("أدوات مختارة للوصول السريع","Curated tools for quick access"),12,MUTED2,false);
        r.addView(psNote,new LinearLayout.LayoutParams(-1,dp(26)));
        addGap(r,8);

        LinearLayout[] rows=new LinearLayout[4];
        String[][] featured={{"ChatGPT","Claude"},{"Gemini","Midjourney"},{"Perplexity","Stable Diffusion"},{"ElevenLabs","Runway"}};
        for(int i=0;i<4;i++){
            rows[i]=new LinearLayout(this);
            rows[i].setOrientation(LinearLayout.HORIZONTAL);
            rows[i].setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
            View a=platformTile(featured[i][0],v->{sel=featured[0][0];create();});
            View b=platformTile(featured[i][1],v->{sel=featured[0][1];create();});
            final String na=featured[i][0], nb=featured[i][1];
            a.setOnClickListener(v->{sel=na;create();});
            b.setOnClickListener(v->{sel=nb;create();});
            rows[i].addView(a,new LinearLayout.LayoutParams(0,dp(82),1));
            Space g=new Space(this);rows[i].addView(g,new LinearLayout.LayoutParams(dp(9),1));
            rows[i].addView(b,new LinearLayout.LayoutParams(0,dp(82),1));
            r.addView(rows[i],new LinearLayout.LayoutParams(-1,dp(82)));
            if(i<3)addGap(r,8);
        }

        addGap(r,12);
        View all=wideAction(tr("كل المنصات والأدوات","All AI Platforms"),tr("تصفّح كامل الكتالوج — "+ps.size()+" أداة ومنصة","Browse the full catalog — "+ps.size()+" tools"),"grid",v->platforms());
        r.addView(all,new LinearLayout.LayoutParams(-1,dp(68)));

        addGap(r,20);
        TextView recentHead=text(tr("المحادثات الأخيرة","Recent prompts"),18,WHITE,true);
        r.addView(recentHead,new LinearLayout.LayoutParams(-1,dp(30)));
        addGap(r,8);

        List<String> history=s.list("history");
        int count=Math.min(3,history.size());
        if(count==0){
            FrameLayout empty=emptyState();
            r.addView(empty,new LinearLayout.LayoutParams(-1,dp(86)));
        }else{
            for(int i=0;i<count;i++){
                final String value=history.get(i);
                FrameLayout row=recentRow(value,i);
                r.addView(row,new LinearLayout.LayoutParams(-1,dp(78)));
                if(i<count-1)addGap(r,8);
            }
        }

        r.addView(bottomSpacer(),new LinearLayout.LayoutParams(-1,dp(8)));
        sc.addView(r);
        FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,-1);
        root.addView(sc,sp);

        View nav=bottomNav();
        FrameLayout.LayoutParams np=new FrameLayout.LayoutParams(-1,dp(82));
        np.gravity=Gravity.BOTTOM;
        root.addView(nav,np);
        showRoot(root);
    }

    private FrameLayout homeHeader(){
        FrameLayout top=new FrameLayout(this);

        PFIconView menu=new PFIconView(this,"menu",WHITE);
        menu.setClickable(true);
        menu.setOnClickListener(v->menuDialog());
        FrameLayout.LayoutParams mp=new FrameLayout.LayoutParams(dp(44),dp(44));
        mp.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        top.addView(menu,mp);

        TextView brand=brandText(20);
        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-2,dp(48));
        bp.gravity=Gravity.CENTER;
        top.addView(brand,bp);

        FrameLayout userDot=new FrameLayout(this);
        userDot.setBackground(gradient(16));
        PFIconView ui=new PFIconView(this,"user",WHITE);
        userDot.addView(ui,new FrameLayout.LayoutParams(dp(22),dp(22),Gravity.CENTER));
        FrameLayout.LayoutParams up=new FrameLayout.LayoutParams(dp(40),dp(40));
        up.gravity=(ar()?Gravity.LEFT:Gravity.RIGHT)|Gravity.CENTER_VERTICAL;
        top.addView(userDot,up);
        return top;
    }

    private FrameLayout welcomeCard(){
        FrameLayout box=panel(25);
        box.setBackground(gradientPanel(25));

        PFIconView spark=new PFIconView(this,"spark",WHITE);
        FrameLayout orb=new FrameLayout(this);
        orb.setBackground(rounded(Color.argb(70,67,105,255),0,22));
        orb.addView(spark,new FrameLayout.LayoutParams(dp(25),dp(25),Gravity.CENTER));
        FrameLayout.LayoutParams op=new FrameLayout.LayoutParams(dp(48),dp(48));
        op.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        op.rightMargin=ar()?dp(16):0;
        op.leftMargin=ar()?0:dp(16);
        box.addView(orb,op);

        LinearLayout texts=new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setGravity(Gravity.CENTER_VERTICAL);
        TextView h=text(tr("أهلاً بعودتك!","Welcome back!"),17,WHITE,true);
        TextView d=text(tr("أنشئ، حسّن، ولّد. كل شيء في مساحة واحدة.","Create, improve and generate — all in one workspace."),12,MUTED,false);
        texts.addView(h,new LinearLayout.LayoutParams(-1,dp(28)));
        texts.addView(d,new LinearLayout.LayoutParams(-1,dp(32)));

        FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,-1);
        if(ar()){tp.rightMargin=dp(76);tp.leftMargin=dp(70);}else{tp.leftMargin=dp(76);tp.rightMargin=dp(70);}
        box.addView(texts,tp);

        PFIconView star=new PFIconView(this,"star",PURPLE);
        FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(dp(32),dp(32));
        sp.gravity=(ar()?Gravity.LEFT:Gravity.RIGHT)|Gravity.CENTER_VERTICAL;
        sp.rightMargin=ar()?0:dp(14);
        sp.leftMargin=ar()?dp(14):0;
        box.addView(star,sp);
        return box;
    }

    private FrameLayout quickAction(String title,String desc,String icon,boolean primary,View.OnClickListener click){
        FrameLayout holder=new FrameLayout(this);
        holder.setBackground(primary?gradient(22):rounded(PANEL,BORDER2,22));
        holder.setClickable(true);
        holder.setFocusable(true);
        holder.setOnClickListener(click);

        PFIconView ic=new PFIconView(this,icon,primary?WHITE:CYAN);
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(30),dp(30));
        ip.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        ip.rightMargin=ar()?dp(12):0;
        ip.leftMargin=ar()?0:dp(12);
        holder.addView(ic,ip);

        LinearLayout tx=new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setGravity(Gravity.CENTER_VERTICAL);
        tx.addView(text(title,14,WHITE,true),new LinearLayout.LayoutParams(-1,dp(24)));
        tx.addView(text(desc,10,primary?Color.argb(225,255,255,255):MUTED,false),new LinearLayout.LayoutParams(-1,dp(24)));
        FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,dp(58));
        tp.gravity=Gravity.CENTER_VERTICAL;
        if(ar())tp.rightMargin=dp(52);else tp.leftMargin=dp(52);
        holder.addView(tx,tp);
        return holder;
    }

    private FrameLayout platformTile(String name,View.OnClickListener click){
        return platformCard(name,click);
    }

    private FrameLayout platformCard(String name,View.OnClickListener click){
        FrameLayout box=panel(20);
        box.setClickable(true);
        box.setFocusable(true);
        box.setOnClickListener(click);

        PlatformIconView ic=new PlatformIconView(this,name);
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(40),dp(40));
        ip.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        ip.rightMargin=ar()?dp(12):0;
        ip.leftMargin=ar()?0:dp(12);
        box.addView(ic,ip);

        TextView label=text(name,12,WHITE,true);
        label.setGravity(ar()?Gravity.RIGHT|Gravity.CENTER_VERTICAL:Gravity.LEFT|Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,dp(34));
        if(ar())lp.rightMargin=dp(60);else lp.leftMargin=dp(60);
        lp.gravity=Gravity.CENTER_VERTICAL;
        box.addView(label,lp);

        return box;
    }

    private FrameLayout wideAction(String title,String desc,String icon,View.OnClickListener click){
        FrameLayout box=panel(22);
        box.setClickable(true);
        box.setFocusable(true);
        box.setOnClickListener(click);

        PFIconView ic=new PFIconView(this,icon,BLUE);
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(28),dp(28));
        ip.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        ip.rightMargin=ar()?dp(14):0;
        ip.leftMargin=ar()?0:dp(14);
        box.addView(ic,ip);

        LinearLayout tx=new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setGravity(Gravity.CENTER_VERTICAL);
        tx.addView(text(title,14,WHITE,true),new LinearLayout.LayoutParams(-1,dp(25)));
        tx.addView(text(desc,11,MUTED,false),new LinearLayout.LayoutParams(-1,dp(22)));
        FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,dp(56));
        tp.gravity=Gravity.CENTER_VERTICAL;
        if(ar())tp.rightMargin=dp(54);else tp.leftMargin=dp(54);
        box.addView(tx,tp);

        PFIconView arrow=new PFIconView(this,ar()?"arrowLeft":"arrowRight",MUTED);
        FrameLayout.LayoutParams ap=new FrameLayout.LayoutParams(dp(22),dp(22));
        ap.gravity=(ar()?Gravity.LEFT:Gravity.RIGHT)|Gravity.CENTER_VERTICAL;
        ap.rightMargin=ar()?0:dp(12);
        ap.leftMargin=ar()?dp(12):0;
        box.addView(arrow,ap);
        return box;
    }

    private FrameLayout emptyState(){
        FrameLayout box=panel(22);
        PFIconView ic=new PFIconView(this,"chat",MUTED2);
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(28),dp(28));
        ip.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        ip.rightMargin=ar()?dp(14):0;ip.leftMargin=ar()?0:dp(14);
        box.addView(ic,ip);
        TextView t=text(tr("ابدأ بإنشاء أول برومبت لتظهر محادثاتك هنا.","Create your first prompt to see your recent activity here."),12,MUTED,false);
        t.setGravity(ar()?Gravity.RIGHT|Gravity.CENTER_VERTICAL:Gravity.LEFT|Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,-1);
        if(ar())tp.rightMargin=dp(54);else tp.leftMargin=dp(54);
        box.addView(t,tp);
        return box;
    }

    private FrameLayout recentRow(String value,int index){
        FrameLayout box=panel(20);
        box.setClickable(true);box.setFocusable(true);
        box.setOnClickListener(v->{last=value;result();});

        PFIconView ic=new PFIconView(this,index==0?"spark":"chat",index==0?PURPLE:BLUE);
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(38),dp(38));
        ip.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        ip.rightMargin=ar()?dp(12):0;ip.leftMargin=ar()?0:dp(12);
        box.addView(ic,ip);

        String title=historyTitle(value);
        TextView h=text(title,13,WHITE,true);
        TextView d=text(tr("برومبت محفوظ في السجل","Saved in recent activity"),10,MUTED2,false);
        LinearLayout tx=new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setGravity(Gravity.CENTER_VERTICAL);
        tx.addView(h,new LinearLayout.LayoutParams(-1,dp(28)));
        tx.addView(d,new LinearLayout.LayoutParams(-1,dp(20)));
        FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,dp(54));
        tp.gravity=Gravity.CENTER_VERTICAL;
        if(ar())tp.rightMargin=dp(62);else tp.leftMargin=dp(62);
        box.addView(tx,tp);

        PFIconView arrow=new PFIconView(this,ar()?"arrowLeft":"arrowRight",MUTED);
        FrameLayout.LayoutParams ap=new FrameLayout.LayoutParams(dp(20),dp(20));
        ap.gravity=(ar()?Gravity.LEFT:Gravity.RIGHT)|Gravity.CENTER_VERTICAL;
        ap.rightMargin=ar()?0:dp(12);ap.leftMargin=ar()?dp(12):0;
        box.addView(arrow,ap);
        return box;
    }

    private String historyTitle(String value){
        if(value==null||value.trim().isEmpty())return tr("برومبت جديد","New prompt");
        String v=value.replaceAll("\\s+"," ").trim();
        if(v.length()>44)v=v.substring(0,44)+"…";
        return v;
    }

    private View bottomNav(){
        FrameLayout bar=new FrameLayout(this);
        GradientDrawable bg=rounded(Color.argb(244,7,14,30),Color.rgb(27,46,80),24);
        bar.setBackground(bg);

        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        row.setPadding(dp(4),dp(5),dp(4),dp(5));

        row.addView(navItem("home",tr("الرئيسية","Home"),"home",true),new LinearLayout.LayoutParams(0,-1,1));
        row.addView(navItem("tools",tr("الأدوات","Tools"),"grid",false),new LinearLayout.LayoutParams(0,-1,1));
        row.addView(navItem("history",tr("المحادثات","Chats"),"chat",false),new LinearLayout.LayoutParams(0,-1,1));
        row.addView(navItem("settings",tr("الإعدادات","Settings"),"settings",false),new LinearLayout.LayoutParams(0,-1,1));
        bar.addView(row,new FrameLayout.LayoutParams(-1,-1));
        return bar;
    }

    private FrameLayout navItem(String id,String label,String icon,boolean active){
        FrameLayout holder=new FrameLayout(this);
        holder.setClickable(true);
        holder.setFocusable(true);
        holder.setOnClickListener(v->{
            if("home".equals(id))home();
            else if("tools".equals(id))platforms();
            else if("history".equals(id))listScreen("history");
            else settings();
        });

        LinearLayout item=new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.addView(new PFIconView(this,icon,active?PURPLE:MUTED),new LinearLayout.LayoutParams(dp(28),dp(28)));
        TextView t=text(label,9,active?WHITE:MUTED,true);
        t.setGravity(Gravity.CENTER);
        item.addView(t,new LinearLayout.LayoutParams(-1,dp(20)));
        holder.addView(item,new FrameLayout.LayoutParams(-1,-1));
        return holder;
    }

    private View bottomSpacer(){return new Space(this);}

    private void menuDialog(){
        final Dialog d=designDialog();
        LinearLayout box=dialogBox();

        LinearLayout top=new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        PromptForgeLogoView logo=new PromptForgeLogoView(this);
        top.addView(logo,new LinearLayout.LayoutParams(dp(50),dp(50)));
        LinearLayout tt=new LinearLayout(this);
        tt.setOrientation(LinearLayout.VERTICAL);
        tt.setPadding(dp(10),0,dp(10),0);
        tt.addView(text(tr("قائمة PromptForge","PromptForge menu"),17,WHITE,true),new LinearLayout.LayoutParams(-1,dp(28)));
        tt.addView(text(s.accountUser().isEmpty()?tr("مساحة عملك الذكية","Your smart workspace"):s.accountUser(),11,MUTED,false),new LinearLayout.LayoutParams(-1,dp(24)));
        top.addView(tt,new LinearLayout.LayoutParams(0,dp(52),1));
        box.addView(top,new LinearLayout.LayoutParams(-1,dp(54)));
        addGap(box,12);

        addMenuRow(box,tr("إنشاء برومبت","Create Prompt"),"spark",v->{d.dismiss();create();});
        addMenuRow(box,tr("تحسين برومبت","Improve Prompt"),"edit",v->{d.dismiss();improve();});
        addMenuRow(box,tr("برومبت عشوائي","Random Prompt"),"spark",v->{d.dismiss();randomPrompt();});
        addMenuRow(box,tr("اختبارات وترفيه","Fun & Compatibility"),"spark",v->{d.dismiss();funCenter();});
        addMenuRow(box,tr("كل المنصات والأدوات","All AI Platforms"),"grid",v->{d.dismiss();platforms();});
        addMenuRow(box,tr("المحادثات المحفوظة","Saved & History"),"chat",v->{d.dismiss();listScreen("history");});
        addMenuRow(box,tr("حسابي","My Account"),"user",v->{d.dismiss();account();});
        addMenuRow(box,tr("الإعدادات","Settings"),"settings",v->{d.dismiss();settings();});
        addGap(box,8);
        TextView close=button(tr("إغلاق","Close"),false);
        close.setOnClickListener(v->d.dismiss());
        box.addView(close,new LinearLayout.LayoutParams(-1,dp(52)));

        d.setContentView(box);
        sizeDialog(d,0.90f);
        d.show();sizeDialog(d,0.90f);
    }

    private void addMenuRow(LinearLayout box,String label,String icon,View.OnClickListener click){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        row.setPadding(dp(12),0,dp(12),0);
        row.setBackground(rounded(PANEL2,BORDER,18));
        row.setClickable(true);row.setOnClickListener(click);

        PFIconView ic=new PFIconView(this,icon,BLUE);
        row.addView(ic,new LinearLayout.LayoutParams(dp(28),dp(28)));
        TextView t=text(label,14,WHITE,true);
        t.setGravity(ar()?Gravity.RIGHT|Gravity.CENTER_VERTICAL:Gravity.LEFT|Gravity.CENTER_VERTICAL);
        row.addView(t,new LinearLayout.LayoutParams(0,dp(52),1));
        PFIconView a=new PFIconView(this,ar()?"arrowLeft":"arrowRight",MUTED);
        row.addView(a,new LinearLayout.LayoutParams(dp(22),dp(22)));
        box.addView(row,new LinearLayout.LayoutParams(-1,dp(56)));
        addGap(box,7);
    }

    private EditText editor(String arHint,String enHint,int minLines){
        EditText e=new EditText(this);
        e.setTextColor(WHITE);
        e.setHintTextColor(MUTED2);
        e.setHint(bidi(tr(arHint,enHint)));
        e.setTextSize(15);
        e.setGravity(ar()?Gravity.TOP|Gravity.RIGHT:Gravity.TOP|Gravity.LEFT);
        e.setTextDirection(ar()?View.TEXT_DIRECTION_FIRST_STRONG:View.TEXT_DIRECTION_LTR);
        e.setPadding(dp(16),dp(15),dp(16),dp(15));
        e.setMinLines(minLines);
        e.setBackground(rounded(PANEL2,BORDER2,22));
        return e;
    }

    private void randomPrompt(){
        setScreen("random");
        LinearLayout r=column();
        titleBar(r,"برومبت عشوائي","Random Prompt");
        addGap(r,8);

        TextView intro=text(tr("اختر نوع البرومبت، ويمكنك إضافة اسمك أو اسم أي شخص ليصبح البرومبت مخصصاً.","Choose a type and optionally add your name or anyone's name to personalize the prompt."),12,MUTED,false);
        intro.setGravity(Gravity.CENTER);
        r.addView(intro,new LinearLayout.LayoutParams(-1,dp(48)));

        addGap(r,6);
        EditText name=editor("اسمك أو اسم شخص آخر (اختياري)","Your name or someone else's (optional)",1);
        name.setSingleLine(true);
        r.addView(name,new LinearLayout.LayoutParams(-1,dp(56)));

        addGap(r,12);
        TextView typeLabel=text(tr("نوع البرومبت","Prompt type"),16,WHITE,true);
        r.addView(typeLabel,new LinearLayout.LayoutParams(-1,dp(28)));
        addGap(r,7);

        LinearLayout grid=new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        final String[] selectedType={ar()?"صور":"Image"};
        randomPromptCards(grid,selectedType);
        r.addView(grid,new LinearLayout.LayoutParams(-1,dp(144)));

        addGap(r,12);
        TextView generate=button(tr("✦  ولّد برومبت عشوائي","✦  Generate Random Prompt"),true);
        r.addView(generate,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(r,10);

        FrameLayout result=panel(22);
        TextView resultTitle=text(tr("النتيجة","Result"),16,WHITE,true);
        resultTitle.setGravity(Gravity.CENTER);
        result.addView(resultTitle,new FrameLayout.LayoutParams(-1,dp(34),Gravity.TOP|Gravity.CENTER_HORIZONTAL));

        EditText output=editor("اضغط توليد لإظهار البرومبت...","Press generate to create a prompt...",5);
        output.setGravity(ar()?Gravity.TOP|Gravity.RIGHT:Gravity.TOP|Gravity.LEFT);
        output.setTextIsSelectable(true);
        output.setFocusable(false);
        output.setClickable(true);
        FrameLayout.LayoutParams op=new FrameLayout.LayoutParams(-1,dp(150));
        op.setMargins(dp(10),dp(38),dp(10),dp(10));
        result.addView(output,op);
        r.addView(result,new LinearLayout.LayoutParams(-1,dp(210)));

        addGap(r,10);
        TextView copy=button(tr("نسخ البرومبت","Copy Prompt"),false);
        copy.setEnabled(false);
        copy.setOnClickListener(v->{
            android.content.ClipboardManager cm=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("PromptForgeAI",output.getText().toString()));
            toast(tr("تم نسخ البرومبت","Prompt copied"));
        });
        r.addView(copy,new LinearLayout.LayoutParams(-1,dp(54)));

        generate.setOnClickListener(v->{
            String n=name.getText().toString().trim();
            output.setText(randomPromptText(selectedType[0],n));
            copy.setEnabled(true);
        });

        showRoot(scroll(r));
    }

    private void randomPromptCards(LinearLayout grid,String[] selectedType){
        grid.removeAllViews();
        String[] arTypes={"صور","قصيدة","نكتة","أغنية"};
        String[] enTypes={"Image","Poem","Joke","Song"};
        String[] icons={"spark","edit","chat","music"};
        for(int row=0;row<2;row++){
            LinearLayout rr=new LinearLayout(this);
            rr.setOrientation(LinearLayout.HORIZONTAL);
            rr.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
            for(int col=0;col<2;col++){
                int idx=row*2+col;
                String label=ar()?arTypes[idx]:enTypes[idx];
                FrameLayout card=panel(20);
                card.setClickable(true);
                card.setFocusable(true);
                final String chosen=label;
                card.setOnClickListener(v->{
                    selectedType[0]=chosen;
                    randomPromptCards(grid,selectedType);
                });
                if(label.equals(selectedType[0]))card.setBackground(gradient(20));

                PFIconView ic=new PFIconView(this,icons[idx],idx==0?CYAN:PURPLE);
                FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(30),dp(30));
                ip.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
                ip.rightMargin=ar()?dp(12):0;
                ip.leftMargin=ar()?0:dp(12);
                card.addView(ic,ip);

                TextView tx=text(label,14,WHITE,true);
                tx.setGravity(Gravity.CENTER);
                FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,dp(54));
                if(ar())tp.rightMargin=dp(52);else tp.leftMargin=dp(52);
                card.addView(tx,tp);

                rr.addView(card,new LinearLayout.LayoutParams(0,dp(68),1));
                if(col==0){
                    Space g=new Space(this);
                    rr.addView(g,new LinearLayout.LayoutParams(dp(8),1));
                }
            }
            grid.addView(rr,new LinearLayout.LayoutParams(-1,dp(68)));
            if(row==0)addGap(grid,8);
        }
    }

    private String randomPromptText(String type,String name){
        Random rnd=new Random();
        String person=name==null?"":name.trim();
        String who=person.isEmpty()?tr("شخصية خيالية","a fictional character"):person;

        String[] imageAr={
            "أنشئ صورة سينمائية عالية التفاصيل لـ "+who+" في مدينة مستقبلية مهجورة بعد منتصف الليل، إضاءة درامية، ضباب خفيف، تكوين احترافي وعمق ميدان سينمائي.",
            "صمّم بورتريه فني لـ "+who+" وسط مسرح قديم مهجور، ضوء مسرحي واحد من الأعلى، ظلال عميقة، تفاصيل واقعية ومزاج غامض.",
            "أنشئ مشهداً ملحمياً لـ "+who+" يقف أمام بحر هائج تحت سماء عاصفة، تكوين سينمائي، إضاءة طبيعية درامية وتفاصيل فائقة."
        };
        String[] poemAr={
            "اكتب قصيدة أصلية عن "+who+"، تتناول العزلة والأمل في آن واحد، بصور شعرية واضحة وإيقاع متماسك، من دون اقتباس أو تقليد شاعر بعينه.",
            "اكتب قصيدة قصيرة باسم "+who+" عن الوقوف في وجه الزمن، بلغة عربية فصيحة، مكثفة وعاطفية، مع خاتمة قوية.",
            "اكتب قصيدة وجدانية عن "+who+" والمدينة التي لا تنام، بأسلوب حديث وصور غير مبتذلة، مع الحفاظ على وحدة الموضوع."
        };
        String[] jokeAr={
            "اكتب نكتة أصلية خفيفة وذكية يكون بطلها "+who+"، مناسبة للمشاركة العائلية ومن دون إساءة أو تنمر.",
            "اكتب موقفاً كوميدياً قصيراً جداً عن "+who+" يحدث بشكل غير متوقع وينتهي بقفلة مضحكة.",
            "اكتب نكتة حوارية بين "+who+" وشخص آخر، تعتمد على سوء فهم بسيط وتنتهي بمفارقة مضحكة."
        };
        String[] songAr={
            "اكتب فكرة أغنية أصلية باسم "+who+" عن بداية جديدة بعد مرحلة صعبة، مع مقطع مميز ولازمة سهلة التذكر، من دون تقليد أغنية موجودة.",
            "اكتب كلمات أغنية أصلية يكون "+who+" محورها، بطابع سينمائي وحالم، مع مقدمة ومقطع ولازمة وخاتمة.",
            "اكتب أغنية عربية أصلية عن "+who+" والبحث عن مكانه في العالم، بإيقاع معاصر وصور بسيطة ولازمة قوية، من دون تقليد فنان محدد."
        };

        if(type.equals(ar()?"صور":"Image"))return imageAr[rnd.nextInt(imageAr.length)];
        if(type.equals(ar()?"قصيدة":"Poem"))return poemAr[rnd.nextInt(poemAr.length)];
        if(type.equals(ar()?"نكتة":"Joke"))return jokeAr[rnd.nextInt(jokeAr.length)];
        return songAr[rnd.nextInt(songAr.length)];
    }

    private void funCenter(){
        setScreen("fun");
        LinearLayout r=column();
        titleBar(r,"اختبارات وترفيه","Fun & Compatibility");
        addGap(r,8);
        TextView note=text(tr("هذه النتائج للتسلية فقط وليست تنبؤات علمية أو طبية أو مالية.","For entertainment only — these are not scientific, medical or financial predictions."),12,MUTED,false);
        note.setGravity(Gravity.CENTER);
        r.addView(note,new LinearLayout.LayoutParams(-1,dp(48)));

        LinearLayout grid=new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        addFunCard(grid,tr("متى أموت؟","When will I die?"),tr("حساب ترفيهي رمزي من الاسم واسم الأم وتاريخ الميلاد والبرج — بدون ادعاء معرفة موعد الوفاة الحقيقي.","A symbolic entertainment reading from names, birth date and zodiac — never a real death prediction."),"spark",v->funDeath());
        addFunCard(grid,tr("نسبة توافق حبيبين","Couple Compatibility"),tr("احسب نسبة توافق ترفيهية بين شخصين.","Calculate a for-fun compatibility percentage for two people."),"heart",v->funLove());
        addFunCard(grid,tr("كم ستكون ثروتي بعد 10 سنوات؟","My Wealth in 10 Years"),tr("توقع ترفيهي مبني على بيانات تدخلها، وليس نصيحة مالية.","An entertainment estimate based on your inputs, not financial advice."),"star",v->funWealth());
        r.addView(grid,new LinearLayout.LayoutParams(-1,-2));
        showRoot(scroll(r));
    }

    private void addFunCard(LinearLayout root,String title,String sub,String icon,View.OnClickListener click){
        FrameLayout card=panel(20);
        card.setClickable(true); card.setFocusable(true); card.setOnClickListener(click);
        PFIconView ic=new PFIconView(this,icon,PURPLE);
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(34),dp(34));
        ip.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        if(ar())ip.rightMargin=dp(14);else ip.leftMargin=dp(14);
        card.addView(ic,ip);
        TextView t=text(title,16,WHITE,true); t.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,dp(34));
        if(ar())tp.rightMargin=dp(54);else tp.leftMargin=dp(54);
        card.addView(t,tp);
        TextView s1=text(sub,11,MUTED,false); s1.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,dp(44));
        sp.setMargins(ar()?dp(54):dp(54),dp(34),dp(12),0);
        card.addView(s1,sp);
        root.addView(card,new LinearLayout.LayoutParams(-1,dp(88)));
        addGap(root,10);
    }

    private void funDeath(){
        setScreen("death");
        LinearLayout r=column(); titleBar(r,"متى أموت؟","When will I die?");
        addGap(r,8);
        TextView n=text(tr("للتسلية فقط: لا يمكن للتطبيق معرفة موعد وفاة أي شخص.","For entertainment only: the app cannot know anyone's actual time of death."),12,MUTED,false);
        n.setGravity(Gravity.CENTER); r.addView(n,new LinearLayout.LayoutParams(-1,dp(44)));
        EditText name=editor("اسمك","Your name",1), mother=editor("اسم الأم","Mother's name",1), birth=editor("تاريخ الميلاد: YYYY-MM-DD","Birth date: YYYY-MM-DD",1), zodiac=editor("البرج (اختياري)","Zodiac (optional)",1);
        r.addView(name,new LinearLayout.LayoutParams(-1,dp(52))); addGap(r,7);
        r.addView(mother,new LinearLayout.LayoutParams(-1,dp(52))); addGap(r,7);
        r.addView(birth,new LinearLayout.LayoutParams(-1,dp(52))); addGap(r,7);
        r.addView(zodiac,new LinearLayout.LayoutParams(-1,dp(52))); addGap(r,12);
        TextView go=button(tr("اعطني نتيجة ترفيهية","Give me a fun result"),true); r.addView(go,new LinearLayout.LayoutParams(-1,dp(56)));
        addGap(r,10);
        EditText out=editor("", "",5); out.setFocusable(false); out.setTextIsSelectable(true); r.addView(out,new LinearLayout.LayoutParams(-1,dp(170)));
        go.setOnClickListener(v->{
            String seed=name.getText().toString().trim()+"|"+mother.getText().toString().trim()+"|"+birth.getText().toString().trim()+"|"+zodiac.getText().toString().trim();
            int score=Math.abs(seed.hashCode())%100;
            int age=72+(score%24);
            out.setText(tr("النتيجة الترفيهية: يبدو أنك من أصحاب الأعمار الطويلة، مع عمر رمزي تقريبي "+age+" سنة. هذه ليست نبوءة ولا يمكن استخدامها لمعرفة موعد الوفاة الحقيقي.","For-fun result: you appear to have a long-life profile, with a symbolic age of about "+age+" years. This is not a prediction and cannot determine your actual death date."));
        });
        showRoot(scroll(r));
    }

    private void funLove(){
        setScreen("love");
        LinearLayout r=column(); titleBar(r,"توافق حبيبين","Couple Compatibility"); addGap(r,8);
        EditText a=editor("اسم الشخص الأول","First name",1), b=editor("اسم الشخص الثاني","Second name",1);
        r.addView(a,new LinearLayout.LayoutParams(-1,dp(56))); addGap(r,8);
        r.addView(b,new LinearLayout.LayoutParams(-1,dp(56))); addGap(r,12);
        TextView go=button(tr("احسب التوافق","Calculate Compatibility"),true); r.addView(go,new LinearLayout.LayoutParams(-1,dp(56))); addGap(r,10);
        TextView out=text("",22,WHITE,true); out.setGravity(Gravity.CENTER); r.addView(out,new LinearLayout.LayoutParams(-1,dp(80)));
        go.setOnClickListener(v->{
            String x=a.getText().toString().trim(), y=b.getText().toString().trim();
            int score=45+Math.abs((x+"♥"+y).hashCode())%56;
            out.setText(tr("نسبة التوافق: "+score+"%","Compatibility: "+score+"%"));
        });
        showRoot(scroll(r));
    }

    private void funWealth(){
        setScreen("wealth");
        LinearLayout r=column(); titleBar(r,"ثروتي بعد 10 سنوات","My Wealth in 10 Years"); addGap(r,8);
        TextView n=text(tr("نتيجة ترفيهية فقط وليست توقعاً مالياً حقيقياً.","For entertainment only — not a real financial forecast."),12,MUTED,false);
        n.setGravity(Gravity.CENTER); r.addView(n,new LinearLayout.LayoutParams(-1,dp(42)));
        EditText name=editor("اسمك","Your name",1), current=editor("ثروتك أو دخلك الحالي (اختياري)","Current wealth or income (optional)",1), goal=editor("هدفك المالي","Financial goal",1);
        r.addView(name,new LinearLayout.LayoutParams(-1,dp(56))); addGap(r,8);
        r.addView(current,new LinearLayout.LayoutParams(-1,dp(56))); addGap(r,8);
        r.addView(goal,new LinearLayout.LayoutParams(-1,dp(56))); addGap(r,12);
        TextView go=button(tr("توقع ترفيهي بعد 10 سنوات","Fun 10-year prediction"),true); r.addView(go,new LinearLayout.LayoutParams(-1,dp(56))); addGap(r,10);
        TextView out=text("",18,WHITE,true); out.setGravity(Gravity.CENTER); r.addView(out,new LinearLayout.LayoutParams(-1,dp(110)));
        go.setOnClickListener(v->{
            int score=Math.abs((name.getText().toString()+"|"+current.getText().toString()+"|"+goal.getText().toString()).hashCode())%100;
            String level=score<30?tr("بداية متواضعة مع فرصة نمو","a modest start with room to grow"):score<70?tr("نمو مالي جيد إذا حافظت على الانضباط","solid financial growth if you stay disciplined"):tr("احتمال ترفيهي لمرحلة مالية قوية","a fun scenario suggesting a strong financial future");
            out.setText(tr("بعد 10 سنوات: "+level+".","In 10 years: "+level+"."));
        });
        showRoot(scroll(r));
    }

    private void create(){
        setScreen("create");
        LinearLayout r=column();
        titleBar(r,"إنشاء برومبت","Create Prompt");
        addGap(r,10);

        LinearLayout selected=new LinearLayout(this);
        selected.setGravity(Gravity.CENTER_VERTICAL);
        selected.setPadding(dp(12),0,dp(12),0);
        selected.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        selected.setBackground(rounded(PANEL,BORDER,18));
        PlatformIconView pic=new PlatformIconView(this,sel);
        selected.addView(pic,new LinearLayout.LayoutParams(dp(36),dp(36)));
        TextView st=text(tr("الأداة المحددة: ","Selected tool: ")+sel,13,WHITE,true);
        st.setGravity(Gravity.CENTER);
        selected.addView(st,new LinearLayout.LayoutParams(0,dp(48),1));
        selected.setClickable(true);
        selected.setOnClickListener(v->platforms());
        r.addView(selected,new LinearLayout.LayoutParams(-1,dp(52)));

        addGap(r,10);
        TextView label=text(tr("ما الذي تريد إنجازه؟","What do you want to achieve?"),16,WHITE,true);
        r.addView(label,new LinearLayout.LayoutParams(-1,dp(28)));
        addGap(r,6);

        EditText idea=editor("اكتب فكرتك هنا...","Describe your idea...",7);
        r.addView(idea,new LinearLayout.LayoutParams(-1,dp(205)));

        addGap(r,10);
        TextView taskBtn=button(taskLabel(),false);
        taskBtn.setOnClickListener(v->taskDialog(taskBtn));
        r.addView(taskBtn,new LinearLayout.LayoutParams(-1,dp(56)));
        addGap(r,10);

        TextView hint=text(tr("سنحافظ على فكرتك ونبني حولها برومبتاً ملائماً للأداة المختارة.","We preserve your intent and shape it for the selected tool."),11,MUTED2,false);
        r.addView(hint,new LinearLayout.LayoutParams(-1,dp(34)));

        addGap(r,8);
        TextView gen=button(tr("✦  توليد البرومبت","✦  Generate Prompt"),true);
        gen.setOnClickListener(v->{
            String ideaText=idea.getText().toString();
            if(ideaText.trim().length()<3){toast(tr("اكتب فكرتك أولاً","Describe your idea first"));return;}
            gen.setEnabled(false);
            generate(ideaText,gen);
        });
        r.addView(gen,new LinearLayout.LayoutParams(-1,dp(62)));

        addGap(r,8);
        TextView remain=text(tr("الاستخدام المتاح: ","Available usage: ")+s.remaining(),11,MUTED2,false);
        remain.setGravity(Gravity.CENTER);
        r.addView(remain,new LinearLayout.LayoutParams(-1,dp(26)));

        showRoot(scroll(r));
    }

    private String taskName(){
        if(!ar())return task;
        if(task.equals("General"))return"عام";
        if(task.equals("Marketing"))return"تسويق";
        if(task.equals("Image prompt"))return"برومبت صورة";
        if(task.equals("Video prompt"))return"برومبت فيديو";
        if(task.equals("Voice / TTS"))return"صوت / TTS";
        if(task.equals("Coding"))return"برمجة";
        if(task.equals("Research"))return"بحث";
        if(task.equals("Education"))return"تعليم";
        if(task.equals("Social media"))return"سوشيال ميديا";
        if(task.equals("Business"))return"أعمال";
        return task;
    }

    private String taskLabel(){return tr("نوع المهمة: ","Task type: ")+bidi(taskName());}

    private void taskDialog(TextView target){
        final Dialog d=designDialog();
        LinearLayout box=dialogBox();
        TextView title=text(tr("اختر نوع المهمة","Choose task type"),20,WHITE,true);
        title.setGravity(Gravity.CENTER);
        box.addView(title,new LinearLayout.LayoutParams(-1,dp(42)));
        addGap(box,8);

        String[] items=ar()
            ?new String[]{"عام","تسويق","برومبت صورة","برومبت فيديو","صوت / TTS","برمجة","بحث","تعليم","سوشيال ميديا","أعمال"}
            :new String[]{"General","Marketing","Image prompt","Video prompt","Voice / TTS","Coding","Research","Education","Social media","Business"};
        String[] canonical={"General","Marketing","Image prompt","Video prompt","Voice / TTS","Coding","Research","Education","Social media","Business"};

        for(int i=0;i<items.length;i++){
            final int idx=i;
            TextView row=button(items[i],canonical[i].equals(task));
            row.setGravity(ar()?Gravity.RIGHT|Gravity.CENTER_VERTICAL:Gravity.LEFT|Gravity.CENTER_VERTICAL);
            row.setPadding(dp(16),0,dp(16),0);
            row.setOnClickListener(v->{task=canonical[idx];target.setText(bidi(taskLabel()));d.dismiss();});
            box.addView(row,new LinearLayout.LayoutParams(-1,dp(50)));
            if(i<items.length-1)addGap(box,6);
        }
        d.setContentView(box);
        sizeDialog(d,0.90f);d.show();sizeDialog(d,0.90f);
    }

    private void generate(String idea,TextView sourceButton){
        toast(tr("جارٍ تجهيز الفكرة وتوليد البرومبت…","Preparing the idea and generating the prompt…"));
        String token=s.accountToken();
        // Send the original idea to the server. On-device ML Kit translation was
        // distorting Arabic descriptions before the prompt engine could interpret them.
        new RemotePromptClient().generate(BASE_URL,idea,sel,task,lang,token,(ok,val)->runOnUiThread(()->{
            sourceButton.setEnabled(true);
            if(ok){
                last=val;
                s.add("history",last);
                result();
                return;
            }
            if("http_401".equals(val)||"http_403".equals(val)){
                s.logout();
                toast(tr("انتهت جلسة الدخول، سجّل الدخول من جديد","Your session expired. Please sign in again."));
                loginScreen();
                return;
            }
            // Only use on-device translation as a fallback when the server is unavailable.
            new FreeTranslator().translateArabicToEnglish(idea,(translatedOk,translated)->runOnUiThread(()->{
                String source=translatedOk&&translated!=null&&!translated.trim().isEmpty()?translated.trim():idea;
                last=PromptEngine.generate(source,find(sel),task,false);
                s.add("history",last);
                result();
                toast(tr("تعذر الوصول للخادم؛ تم استخدام المحرك المحلي","Server unavailable; used the local engine"));
            }));
        }));
    }

    private void result(){
        setScreen("result");
        LinearLayout r=column();
        titleBar(r,"النتيجة","Result");
        addGap(r,10);

        LinearLayout meta=new LinearLayout(this);
        meta.setGravity(Gravity.CENTER_VERTICAL);
        meta.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        PlatformIconView pi=new PlatformIconView(this,sel);
        meta.addView(pi,new LinearLayout.LayoutParams(dp(34),dp(34)));
        TextView tool=text(sel,14,WHITE,true);
        tool.setGravity(Gravity.CENTER);
        meta.addView(tool,new LinearLayout.LayoutParams(0,dp(42),1));
        TextView taskP=pill(taskName());
        meta.addView(taskP,new LinearLayout.LayoutParams(-2,dp(34)));
        r.addView(meta,new LinearLayout.LayoutParams(-1,dp(42)));

        addGap(r,10);
        TextView out=text(last,15,WHITE,false);
        out.setTextIsSelectable(true);
        out.setGravity(ar()?Gravity.RIGHT|Gravity.TOP:Gravity.LEFT|Gravity.TOP);
        out.setPadding(dp(16),dp(16),dp(16),dp(16));
        out.setBackground(rounded(PANEL,BORDER2,25));
        r.addView(out,new LinearLayout.LayoutParams(-1,dp(420)));

        addGap(r,12);
        TextView copy=button(tr("نسخ البرومبت","Copy prompt"),true);
        copy.setOnClickListener(v->copy(last));
        r.addView(copy,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(r,8);

        LinearLayout acts=new LinearLayout(this);
        acts.setGravity(Gravity.CENTER);
        acts.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        TextView save=button(tr("★  حفظ","★  Save"),false);
        save.setOnClickListener(v->{s.add("saved",last);toast(tr("تم حفظ البرومبت","Prompt saved"));});
        TextView share=button(tr("مشاركة","Share"),false);
        share.setOnClickListener(v->{
            Intent i=new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,last);
            startActivity(Intent.createChooser(i,tr("مشاركة البرومبت","Share prompt")));
        });
        acts.addView(save,new LinearLayout.LayoutParams(0,dp(56),1));
        Space g=new Space(this);acts.addView(g,new LinearLayout.LayoutParams(dp(8),1));
        acts.addView(share,new LinearLayout.LayoutParams(0,dp(56),1));
        r.addView(acts,new LinearLayout.LayoutParams(-1,dp(56)));

        addGap(r,12);
        TextView again=button(tr("إنشاء برومبت جديد","Create another prompt"),false);
        again.setOnClickListener(v->create());
        r.addView(again,new LinearLayout.LayoutParams(-1,dp(54)));

        showRoot(scroll(r));
    }

    private void improve(){
        setScreen("improve");
        LinearLayout r=column();
        titleBar(r,"تحسين برومبت","Improve Prompt");
        addGap(r,10);

        FrameLayout tip=panel(22);
        PFIconView ic=new PFIconView(this,"spark",PURPLE);
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(28),dp(28));
        ip.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        ip.rightMargin=ar()?dp(14):0;ip.leftMargin=ar()?0:dp(14);
        tip.addView(ic,ip);
        TextView tx=text(tr("الصق أي برومبت لديك، وسنرتبه ونوضحه مع الحفاظ على هدفه.","Paste any prompt. We improve structure and clarity while preserving its intent."),12,MUTED,false);
        tx.setGravity(ar()?Gravity.RIGHT|Gravity.CENTER_VERTICAL:Gravity.LEFT|Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,-1);
        if(ar())tp.rightMargin=dp(54);else tp.leftMargin=dp(54);
        tip.addView(tx,tp);
        r.addView(tip,new LinearLayout.LayoutParams(-1,dp(76)));

        addGap(r,12);
        EditText input=editor("ألصق البرومبت هنا...","Paste your prompt here...",9);
        r.addView(input,new LinearLayout.LayoutParams(-1,dp(235)));
        addGap(r,12);

        TextView b=button(tr("✦  تحسين البرومبت","✦  Improve Prompt"),true);
        b.setOnClickListener(v->{
            String source=input.getText().toString();
            if(source.trim().length()<3){toast(tr("ألصق البرومبت أولاً","Paste a prompt first"));return;}
            b.setEnabled(false);
            last=PromptEngine.improve(source,ar());
            s.add("history",last);
            b.setEnabled(true);
            result();
        });
        r.addView(b,new LinearLayout.LayoutParams(-1,dp(62)));

        showRoot(scroll(r));
    }

    private void platforms(){
        setScreen("platforms");
        LinearLayout r=column();
        titleBar(r,"كل المنصات والأدوات","All AI Platforms");
        addGap(r,8);

        EditText q=editor("ابحث عن أداة أو فئة…","Search a tool or category…",1);
        q.setSingleLine(true);
        q.setGravity(ar()?Gravity.CENTER_VERTICAL|Gravity.RIGHT:Gravity.CENTER_VERTICAL|Gravity.LEFT);
        r.addView(q,new LinearLayout.LayoutParams(-1,dp(54)));
        addGap(r,10);

        LinearLayout list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        Runnable fill=()->{
            list.removeAllViews();
            String query=q.getText().toString().trim().toLowerCase(Locale.ROOT);
            for(Platform p:ps){
                String n=p.name.toLowerCase(Locale.ROOT),c=p.category.toLowerCase(Locale.ROOT);
                if(query.isEmpty()||n.contains(query)||c.contains(query)){
                    FrameLayout item=platformListRow(p);
                    list.addView(item,new LinearLayout.LayoutParams(-1,dp(62)));
                    addGap(list,7);
                }
            }
            if(list.getChildCount()==0){
                TextView empty=text(tr("لا توجد نتائج مطابقة.","No matching tools found."),14,MUTED,false);
                empty.setGravity(Gravity.CENTER);
                list.addView(empty,new LinearLayout.LayoutParams(-1,dp(80)));
            }
        };
        q.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence a,int b,int c,int d){}
            public void onTextChanged(CharSequence a,int b,int c,int d){fill.run();}
            public void afterTextChanged(Editable e){}
        });
        fill.run();
        r.addView(list);
        showRoot(scroll(r));
    }

    private FrameLayout platformListRow(Platform p){
        FrameLayout box=panel(20);
        box.setClickable(true);box.setFocusable(true);
        box.setOnClickListener(v->platformChoice(p));

        PlatformIconView ic=new PlatformIconView(this,p.name);
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(40),dp(40));
        ip.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        ip.rightMargin=ar()?dp(12):0;ip.leftMargin=ar()?0:dp(12);
        box.addView(ic,ip);

        TextView name=text(p.name,14,WHITE,true);
        name.setGravity(ar()?Gravity.RIGHT|Gravity.CENTER_VERTICAL:Gravity.LEFT|Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams np=new FrameLayout.LayoutParams(-1,dp(26));
        np.gravity=Gravity.CENTER_VERTICAL;
        if(ar())np.rightMargin=dp(62);else np.leftMargin=dp(62);
        box.addView(name,np);

        TextView cat=text(p.category,10,MUTED2,false);
        cat.setGravity(ar()?Gravity.RIGHT|Gravity.CENTER_VERTICAL:Gravity.LEFT|Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,dp(22));
        cp.gravity=Gravity.BOTTOM;
        cp.bottomMargin=dp(8);
        if(ar())cp.rightMargin=dp(62);else cp.leftMargin=dp(62);
        box.addView(cat,cp);

        PFIconView arrow=new PFIconView(this,ar()?"arrowLeft":"arrowRight",MUTED);
        FrameLayout.LayoutParams ap=new FrameLayout.LayoutParams(dp(20),dp(20));
        ap.gravity=(ar()?Gravity.LEFT:Gravity.RIGHT)|Gravity.CENTER_VERTICAL;
        ap.rightMargin=ar()?0:dp(12);ap.leftMargin=ar()?dp(12):0;
        box.addView(arrow,ap);
        return box;
    }

    private void platformChoice(Platform p){
        final Dialog d=designDialog();
        LinearLayout box=dialogBox();

        LinearLayout head=new LinearLayout(this);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        PlatformIconView ic=new PlatformIconView(this,p.name);
        head.addView(ic,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout tx=new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.addView(text(p.name,18,WHITE,true),new LinearLayout.LayoutParams(-1,dp(28)));
        tx.addView(text(p.category,11,MUTED,false),new LinearLayout.LayoutParams(-1,dp(22)));
        head.addView(tx,new LinearLayout.LayoutParams(0,dp(48),1));
        box.addView(head,new LinearLayout.LayoutParams(-1,dp(52)));

        addGap(box,14);
        TextView select=button(tr("استخدام هذه الأداة","Use this tool"),true);
        select.setOnClickListener(v->{sel=p.name;d.dismiss();create();});
        box.addView(select,new LinearLayout.LayoutParams(-1,dp(56)));
        addGap(box,8);

        TextView open=button(tr("فتح الموقع","Open website"),false);
        open.setOnClickListener(v->open(p.url));
        box.addView(open,new LinearLayout.LayoutParams(-1,dp(52)));
        addGap(box,8);

        TextView cancel=button(tr("إلغاء","Cancel"),false);
        cancel.setOnClickListener(v->d.dismiss());
        box.addView(cancel,new LinearLayout.LayoutParams(-1,dp(52)));

        d.setContentView(box);
        sizeDialog(d,0.88f);d.show();sizeDialog(d,0.88f);
    }

    private Platform find(String name){
        for(Platform p:ps)if(p.name.equals(name))return p;
        return ps.get(0);
    }

    private void listScreen(String kind){
        setScreen("list");
        LinearLayout r=column();
        boolean saved="saved".equals(kind);
        titleBar(r,saved?"المحفوظات":"المحادثات",saved?"Saved":"Recent prompts");
        addGap(r,8);

        List<String> items=s.list(kind);
        if(items.isEmpty()){
            r.addView(emptyState(),new LinearLayout.LayoutParams(-1,dp(92)));
        }else{
            for(int i=0;i<items.size();i++){
                final String value=items.get(i);
                FrameLayout box=recentRow(value,i);
                box.setOnClickListener(v->{last=value;result();});
                r.addView(box,new LinearLayout.LayoutParams(-1,dp(82)));
                addGap(r,8);
                if(i>=49)break;
            }
        }
        showRoot(scroll(r));
    }

    private void account(){
        setScreen("account");
        LinearLayout r=column();
        titleBar(r,"الحساب","Account");
        addGap(r,10);

        if(!s.accountUser().isEmpty()){
            FrameLayout profile=panel(26);
            PFIconView user=new PFIconView(this,"user",WHITE);
            FrameLayout avatar=new FrameLayout(this);
            avatar.setBackground(gradient(30));
            avatar.addView(user,new FrameLayout.LayoutParams(dp(32),dp(32),Gravity.CENTER));
            FrameLayout.LayoutParams ap=new FrameLayout.LayoutParams(dp(58),dp(58));
            ap.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
            ap.rightMargin=ar()?dp(16):0;ap.leftMargin=ar()?0:dp(16);
            profile.addView(avatar,ap);

            TextView name=text(s.accountUser(),17,WHITE,true);
            FrameLayout.LayoutParams np=new FrameLayout.LayoutParams(-1,dp(30));
            np.gravity=Gravity.TOP;
            np.topMargin=dp(13);
            if(ar())np.rightMargin=dp(88);else np.leftMargin=dp(88);
            profile.addView(name,np);

            TextView status=text(tr("حساب فعّال","Active account"),11,SUCCESS,true);
            FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,dp(24));
            sp.gravity=Gravity.BOTTOM;
            sp.bottomMargin=dp(12);
            if(ar())sp.rightMargin=dp(88);else sp.leftMargin=dp(88);
            profile.addView(status,sp);
            r.addView(profile,new LinearLayout.LayoutParams(-1,dp(88)));

            addGap(r,12);
            TextView change=button(tr("تغيير كلمة المرور","Change password"),true);
            change.setOnClickListener(v->changePassword(change));
            r.addView(change,new LinearLayout.LayoutParams(-1,dp(58)));
            addGap(r,8);

            TextView out=button(tr("تسجيل الخروج","Log out"),false);
            out.setOnClickListener(v->logoutConfirm());
            r.addView(out,new LinearLayout.LayoutParams(-1,dp(58)));
            showRoot(scroll(r));return;
        }

        TextView note=text(tr("سجّل الدخول إلى حسابك للوصول إلى مساحة العمل.","Sign in to access your workspace."),15,MUTED,false);
        note.setGravity(Gravity.CENTER);
        r.addView(note,new LinearLayout.LayoutParams(-1,dp(80)));
        TextView login=button(tr("تسجيل الدخول","Sign in"),true);
        login.setOnClickListener(v->loginScreen());
        r.addView(login,new LinearLayout.LayoutParams(-1,dp(58)));
        showRoot(scroll(r));
    }

    private void changePassword(TextView source){
        final Dialog d=designDialog();
        LinearLayout box=dialogBox();
        TextView title=text(tr("تغيير كلمة المرور","Change password"),19,WHITE,true);title.setGravity(Gravity.CENTER);
        box.addView(title,new LinearLayout.LayoutParams(-1,dp(40)));addGap(box,8);
        EditText pass=editor("كلمة المرور الجديدة (8 أحرف على الأقل)","New password (8+ characters)",1);
        pass.setSingleLine(true);pass.setInputType(0x00000081);
        box.addView(pass,new LinearLayout.LayoutParams(-1,dp(56)));addGap(box,10);

        TextView save=button(tr("حفظ التغيير","Save change"),true);
        save.setOnClickListener(v->{
            String p=pass.getText().toString();
            if(p.length()<8){toast(tr("كلمة المرور يجب أن تكون 8 أحرف على الأقل","Password must be at least 8 characters"));return;}
            save.setEnabled(false);
            new RemotePromptClient().updatePassword(BASE_URL,s.accountToken(),p,(ok,val)->runOnUiThread(()->{
                save.setEnabled(true);
                if(!ok){toast(tr("تعذر تعديل كلمة المرور","Could not update password"));return;}
                try{
                    org.json.JSONObject j=new org.json.JSONObject(val);
                    s.account(j.optString("username",s.accountUser()),j.optString("token",s.accountToken()),s.isAdmin());
                }catch(Exception ignored){}
                d.dismiss();toast(tr("تم تعديل كلمة المرور","Password updated"));
            }));
        });
        box.addView(save,new LinearLayout.LayoutParams(-1,dp(56)));addGap(box,8);
        TextView cancel=button(tr("إلغاء","Cancel"),false);cancel.setOnClickListener(v->d.dismiss());
        box.addView(cancel,new LinearLayout.LayoutParams(-1,dp(52)));
        d.setContentView(box);sizeDialog(d,0.90f);d.show();sizeDialog(d,0.90f);
    }

    private void logoutConfirm(){
        final Dialog d=designDialog();
        LinearLayout box=dialogBox();
        PromptForgeLogoView logo=new PromptForgeLogoView(this);
        LinearLayout holder=new LinearLayout(this);holder.setGravity(Gravity.CENTER);holder.addView(logo,new LinearLayout.LayoutParams(dp(54),dp(54)));
        box.addView(holder,new LinearLayout.LayoutParams(-1,dp(54)));addGap(box,10);
        TextView title=text(tr("تسجيل الخروج","Log out"),19,WHITE,true);title.setGravity(Gravity.CENTER);box.addView(title,new LinearLayout.LayoutParams(-1,dp(36)));
        TextView msg=text(tr("هل تريد تسجيل الخروج من الحساب؟","Do you want to sign out?"),13,MUTED,false);msg.setGravity(Gravity.CENTER);box.addView(msg,new LinearLayout.LayoutParams(-1,dp(40)));
        addGap(box,8);
        LinearLayout acts=new LinearLayout(this);acts.setOrientation(LinearLayout.HORIZONTAL);acts.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView no=button(tr("إلغاء","Cancel"),false);no.setOnClickListener(v->d.dismiss());
        TextView yes=button(tr("خروج","Log out"),true);yes.setOnClickListener(v->{d.dismiss();s.logout();loginScreen();});
        acts.addView(no,new LinearLayout.LayoutParams(0,dp(54),1));Space g=new Space(this);acts.addView(g,new LinearLayout.LayoutParams(dp(8),1));acts.addView(yes,new LinearLayout.LayoutParams(0,dp(54),1));
        box.addView(acts,new LinearLayout.LayoutParams(-1,dp(54)));
        d.setContentView(box);sizeDialog(d,0.88f);d.show();sizeDialog(d,0.88f);
    }

    private void settings(){
        setScreen("settings");
        LinearLayout r=column();
        titleBar(r,"الإعدادات","Settings");
        addGap(r,8);

        if(s.isAdmin()){
            TextView admin=button(tr("🛡  لوحة تحكم الأدمن","🛡  Admin Control Panel"),true);
            admin.setOnClickListener(v->{
                Intent i=new Intent(this,AdminActivity.class);
                i.putExtra("admin_token",s.accountToken());
                startActivity(i);
            });
            r.addView(admin,new LinearLayout.LayoutParams(-1,dp(58)));
            addGap(r,10);
        }

        TextView langBtn=button(tr("لغة التطبيق: العربية","App language: English"),false);
        langBtn.setOnClickListener(v->languageDialog());
        r.addView(langBtn,new LinearLayout.LayoutParams(-1,dp(56)));
        addGap(r,12);

        FrameLayout appCard=panel(25);
        PFIconView icon=new PFIconView(this,"spark",BLUE);
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(34),dp(34));
        ip.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.TOP;
        ip.topMargin=dp(16);
        ip.rightMargin=ar()?dp(16):0;
        ip.leftMargin=ar()?0:dp(16);
        appCard.addView(icon,ip);

        TextView h=text(tr("PromptForge AI","PromptForge AI"),16,WHITE,true);
        TextView d=text(tr(
            "هندسة برومبتات مخصصة لأدوات الذكاء الاصطناعي المختلفة، مع الحفاظ على هدفك وصياغة مخرجات جاهزة للاستخدام.",
            "Purpose-built prompt engineering for different AI tools, preserving your intent and shaping ready-to-use outputs."
        ),11,MUTED,false);
        TextView count=text(tr(ps.size()+" أداة ومنصة متاحة",""+ps.size()+" tools and platforms available"),10,MUTED2,false);

        LinearLayout tx=new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setGravity(Gravity.CENTER_VERTICAL);
        tx.addView(h,new LinearLayout.LayoutParams(-1,dp(25)));
        tx.addView(d,new LinearLayout.LayoutParams(-1,dp(48)));
        tx.addView(count,new LinearLayout.LayoutParams(-1,dp(20)));

        FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,dp(96));
        tp.gravity=Gravity.CENTER_VERTICAL;
        if(ar())tp.rightMargin=dp(64);else tp.leftMargin=dp(64);
        appCard.addView(tx,tp);

        r.addView(appCard,new LinearLayout.LayoutParams(-1,dp(126)));
        addGap(r,10);

        TextView translationInfo=text(tr(
            "الترجمة العربية → الإنجليزية تعمل على الجهاز عبر Google ML Kit، بدون مفتاح API أو خدمة مدفوعة. قد يُطلب تنزيل نموذج ترجمة عند أول استخدام.",
            "Arabic → English translation runs on-device with Google ML Kit, with no API key or paid service. A translation model may download on first use."
        ),10,MUTED2,false);
        translationInfo.setGravity(ar()?Gravity.RIGHT:Gravity.LEFT);
        translationInfo.setPadding(dp(4),dp(4),dp(4),dp(4));
        r.addView(translationInfo,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(r,6);

        TextView accountBtn=button(tr("الحساب","Account"),false);
        accountBtn.setOnClickListener(v->account());
        r.addView(accountBtn,new LinearLayout.LayoutParams(-1,dp(54)));

        showRoot(scroll(r));
    }

    private void confirmExit(){
        final Dialog d=designDialog();
        LinearLayout box=dialogBox();
        PromptForgeLogoView logo=new PromptForgeLogoView(this);
        LinearLayout holder=new LinearLayout(this);holder.setGravity(Gravity.CENTER);
        holder.addView(logo,new LinearLayout.LayoutParams(dp(58),dp(58)));
        box.addView(holder,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(box,10);

        TextView title=text(tr("مغادرة التطبيق","Exit app"),21,WHITE,true);
        title.setGravity(Gravity.CENTER);
        box.addView(title,new LinearLayout.LayoutParams(-1,dp(38)));

        TextView msg=text(tr("هل تريد مغادرة التطبيق؟","Do you want to exit the app?"),14,MUTED,false);
        msg.setGravity(Gravity.CENTER);
        box.addView(msg,new LinearLayout.LayoutParams(-1,dp(38)));
        addGap(box,8);

        LinearLayout acts=new LinearLayout(this);
        acts.setOrientation(LinearLayout.HORIZONTAL);
        acts.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView no=button(tr("لا","No"),false);
        no.setOnClickListener(v->d.dismiss());
        TextView yes=button(tr("نعم","Yes"),true);
        yes.setOnClickListener(v->{d.dismiss();finishAffinity();});
        acts.addView(no,new LinearLayout.LayoutParams(0,dp(54),1));
        Space g=new Space(this);acts.addView(g,new LinearLayout.LayoutParams(dp(8),1));
        acts.addView(yes,new LinearLayout.LayoutParams(0,dp(54),1));
        box.addView(acts,new LinearLayout.LayoutParams(-1,dp(54)));

        d.setContentView(box);sizeDialog(d,0.88f);d.show();sizeDialog(d,0.88f);
    }


    private void setScreen(String screen){currentScreen=screen;}

    private void open(String url){
        try{startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));}
        catch(Exception e){toast(tr("تعذر فتح الرابط","Cannot open URL"));}
    }

    private void copy(String value){
        android.content.ClipboardManager cm=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("PromptForge",value));
        toast(tr("تم النسخ","Copied"));
    }

    private void toast(String value){Toast.makeText(this,value,Toast.LENGTH_SHORT).show();}

    private class AmbientBackgroundView extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        AmbientBackgroundView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float w=getWidth(),h=getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0,0,w,h,BG,Color.rgb(5,12,29),Shader.TileMode.CLAMP));
            c.drawRect(0,0,w,h,p);
            p.setShader(new RadialGradient(w*0.84f,h*0.18f,dp(250),new int[]{Color.argb(70,24,150,255),Color.argb(0,24,150,255)},new float[]{0f,1f},Shader.TileMode.CLAMP));
            c.drawCircle(w*0.84f,h*0.18f,dp(250),p);
            p.setShader(new RadialGradient(w*0.08f,h*0.82f,dp(260),new int[]{Color.argb(48,122,67,255),Color.argb(0,122,67,255)},new float[]{0f,1f},Shader.TileMode.CLAMP));
            c.drawCircle(w*0.08f,h*0.82f,dp(260),p);
            p.setShader(null);
            p.setColor(Color.argb(34,110,160,255));
            for(int i=0;i<65;i++){
                float x=(float)((i*97)%Math.max(1,(int)w))+((i%3)*9);
                float y=(float)((i*151)%Math.max(1,(int)h));
                float rr=(i%7==0)?dp(1.4f):dp(0.7f);
                c.drawCircle(x,y,rr,p);
            }
        }
    }

    private GradientDrawable gradientPanel(int radius){
        GradientDrawable d=new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(18,34,68),Color.rgb(12,20,45),Color.rgb(29,18,65)});
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1),Color.rgb(42,67,112));
        return d;
    }

    private class GradientLine extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        GradientLine(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            p.setShader(new LinearGradient(0,0,getWidth(),0,BLUE,PURPLE,Shader.TileMode.CLAMP));
            c.drawRoundRect(0,0,getWidth(),getHeight(),dp(5),dp(5),p);
            p.setShader(null);
        }
    }

    private class PromptForgeLogoView extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path leftBrain=new Path();
        private final Path rightBrain=new Path();
        PromptForgeLogoView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);

            float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f;
            float size=Math.min(w,h)*0.80f;
            RectF outer=new RectF(cx-size/2f,cy-size/2f,cx+size/2f,cy+size/2f);

            // 3D / glass chassis: luminous edge + dark recessed face.
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(
                outer.left,outer.top,outer.right,outer.bottom,
                new int[]{CYAN,VIOLET,PURPLE},
                null,Shader.TileMode.CLAMP));
            p.setShadowLayer(dp(16),0,dp(4),Color.argb(100,30,150,255));
            c.drawRoundRect(outer,dp(25),dp(25),p);
            p.clearShadowLayer();
            p.setShader(null);

            RectF face=new RectF(outer.left+dp(4),outer.top+dp(4),outer.right-dp(4),outer.bottom-dp(4));
            p.setColor(Color.rgb(5,12,28));
            c.drawRoundRect(face,dp(21),dp(21),p);

            // Beveled rim: bright upper-left, deep lower-right.
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1.4f));
            p.setColor(Color.argb(170,130,222,255));
            c.drawRoundRect(new RectF(face.left+dp(1),face.top+dp(1),face.right-dp(1),face.bottom-dp(1)),dp(20),dp(20),p);
            p.setColor(Color.argb(120,60,34,130));
            c.drawRoundRect(new RectF(face.left+dp(2),face.top+dp(2),face.right-dp(2),face.bottom-dp(2)),dp(19),dp(19),p);

            buildBrains(cx,cy);

            // Soft 3D extrusion beneath both halves.
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeWidth(dp(5.6f));
            p.setColor(Color.argb(110,0,71,120));
            c.save();
            c.translate(dp(3),dp(4));
            c.drawPath(leftBrain,p);
            p.setColor(Color.argb(105,74,28,145));
            c.drawPath(rightBrain,p);
            c.restore();

            // Main neon outlines with a vertical cyan→violet gradient.
            p.setStrokeWidth(dp(3.4f));
            p.setShader(new LinearGradient(cx-dp(40),0,cx,0,CYAN,BLUE,Shader.TileMode.CLAMP));
            p.setColor(WHITE);
            p.setShadowLayer(dp(7),0,0,Color.argb(120,35,180,255));
            c.drawPath(leftBrain,p);
            p.clearShadowLayer();
            p.setShader(new LinearGradient(cx,0,cx+dp(40),0,PURPLE,VIOLET,Shader.TileMode.CLAMP));
            p.setShadowLayer(dp(7),0,0,Color.argb(115,145,80,255));
            c.drawPath(rightBrain,p);
            p.clearShadowLayer();
            p.setShader(null);

            // Fine white/cyan highlight line = glass-like edge.
            p.setStrokeWidth(dp(1.15f));
            p.setColor(Color.argb(200,220,250,255));
            c.drawPath(leftBrain,p);
            p.setColor(Color.argb(175,245,220,255));
            c.drawPath(rightBrain,p);

            drawCircuits(c,cx,cy);

            // Central spine: makes the two halves read as one coherent brain.
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2.2f));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(Color.argb(235,WHITE>>16&255,WHITE>>8&255,WHITE&255));
            Path spine=new Path();
            spine.moveTo(cx,cy-dp(31));
            spine.cubicTo(cx-dp(2),cy-dp(17),cx+dp(2),cy-dp(4),cx,cy+dp(12));
            spine.cubicTo(cx-dp(2),cy+dp(21),cx+dp(2),cy+dp(27),cx,cy+dp(31));
            c.drawPath(spine,p);
        }

        private void buildBrains(float cx,float cy){
            leftBrain.reset();
            leftBrain.moveTo(cx,cy-dp(31));
            leftBrain.cubicTo(cx-dp(12),cy-dp(45),cx-dp(28),cy-dp(43),cx-dp(35),cy-dp(32));
            leftBrain.cubicTo(cx-dp(44),cy-dp(25),cx-dp(43),cy-dp(15),cx-dp(38),cy-dp(8));
            leftBrain.cubicTo(cx-dp(47),cy+dp(2),cx-dp(41),cy+dp(14),cx-dp(31),cy+dp(18));
            leftBrain.cubicTo(cx-dp(30),cy+dp(29),cx-dp(19),cy+dp(35),cx-dp(10),cy+dp(27));
            leftBrain.cubicTo(cx-dp(5),cy+dp(36),cx-dp(2),cy+dp(33),cx,cy+dp(22));

            rightBrain.reset();
            rightBrain.moveTo(cx,cy-dp(31));
            rightBrain.cubicTo(cx+dp(12),cy-dp(45),cx+dp(28),cy-dp(43),cx+dp(35),cy-dp(32));
            rightBrain.cubicTo(cx+dp(44),cy-dp(25),cx+dp(43),cy-dp(15),cx+dp(38),cy-dp(8));
            rightBrain.cubicTo(cx+dp(47),cy+dp(2),cx+dp(41),cy+dp(14),cx+dp(31),cy+dp(18));
            rightBrain.cubicTo(cx+dp(30),cy+dp(29),cx+dp(19),cy+dp(35),cx+dp(10),cy+dp(27));
            rightBrain.cubicTo(cx+dp(5),cy+dp(36),cx+dp(2),cy+dp(33),cx,cy+dp(22));
        }

        private void drawCircuits(Canvas c,float cx,float cy){
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeWidth(dp(1.7f));

            // Left/cyan circuits.
            p.setColor(Color.argb(235,CYAN>>16&255,CYAN>>8&255,CYAN&255));
            Path[] lefts=new Path[]{
                line(cx-dp(8),cy-dp(14),cx-dp(22),cy-dp(8)),
                line(cx-dp(7),cy+dp(1),cx-dp(24),cy+dp(10)),
                line(cx-dp(8),cy+dp(15),cx-dp(20),cy+dp(22))
            };
            for(Path q:lefts)c.drawPath(q,p);
            c.drawCircle(cx-dp(25),cy-dp(8),dp(2.6f),p);
            c.drawCircle(cx-dp(27),cy+dp(10),dp(2.6f),p);
            c.drawCircle(cx-dp(23),cy+dp(22),dp(2.6f),p);

            // Right/violet circuits.
            p.setColor(Color.argb(235,PURPLE>>16&255,PURPLE>>8&255,PURPLE&255));
            Path[] rights=new Path[]{
                line(cx+dp(8),cy-dp(14),cx+dp(22),cy-dp(22)),
                line(cx+dp(8),cy, cx+dp(25),cy-dp(8)),
                line(cx+dp(8),cy+dp(13),cx+dp(22),cy+dp(5))
            };
            for(Path q:rights)c.drawPath(q,p);
            c.drawCircle(cx+dp(22),cy-dp(22),dp(2.6f),p);
            c.drawCircle(cx+dp(27),cy-dp(8),dp(2.6f),p);
            c.drawCircle(cx+dp(24),cy+dp(5),dp(2.6f),p);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(220,WHITE>>16&255,WHITE>>8&255,WHITE&255));
            c.drawCircle(cx-dp(8),cy-dp(14),dp(2.1f),p);
            c.drawCircle(cx+dp(8),cy-dp(14),dp(2.1f),p);
        }

        private Path line(float x1,float y1,float x2,float y2){
            Path q=new Path();
            q.moveTo(x1,y1);
            q.lineTo(x2,y2);
            return q;
        }
    }

    private class PFIconView extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final String kind; private final int tint;
        PFIconView(Context c,String kind,int tint){super(c);this.kind=kind;this.tint=tint;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f,s=Math.min(w,h);
            p.setColor(tint);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(dp(1.6f),s*0.075f));
            p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);

            if(kind.equals("menu")){
                for(int i=-1;i<=1;i++)c.drawLine(cx-s*.30f,cy+i*s*.20f,cx+s*.30f,cy+i*s*.20f,p);return;
            }
            if(kind.equals("home")){
                Path x=new Path();x.moveTo(cx-s*.34f,cy);x.lineTo(cx,cy-s*.30f);x.lineTo(cx+s*.34f,cy);x.moveTo(cx-s*.25f,cy-s*.02f);x.lineTo(cx-s*.25f,cy+s*.30f);x.lineTo(cx+s*.25f,cy+s*.30f);x.lineTo(cx+s*.25f,cy-s*.02f);c.drawPath(x,p);return;
            }
            if(kind.equals("grid")){
                for(int i=0;i<2;i++)for(int j=0;j<2;j++)c.drawRoundRect(cx-s*.30f+j*s*.30f,cy-s*.30f+i*s*.30f,cx-s*.02f+j*s*.30f,cy-s*.02f+i*s*.30f,s*.05f,s*.05f,p);return;
            }
            if(kind.equals("chat")){
                RectF r=new RectF(cx-s*.32f,cy-s*.24f,cx+s*.32f,cy+s*.20f);c.drawRoundRect(r,s*.10f,s*.10f,p);Path q=new Path();q.moveTo(cx-s*.08f,cy+s*.20f);q.lineTo(cx-s*.18f,cy+s*.35f);q.lineTo(cx-s*.18f,cy+s*.12f);c.drawPath(q,p);return;
            }
            if(kind.equals("user")){
                c.drawCircle(cx,cy-s*.17f,s*.12f,p);c.drawArc(new RectF(cx-s*.27f,cy, cx+s*.27f,cy+s*.30f),200,140,false,p);return;
            }
            if(kind.equals("lock")){
                RectF r=new RectF(cx-s*.23f,cy-s*.02f,cx+s*.23f,cy+s*.30f);c.drawRoundRect(r,s*.07f,s*.07f,p);c.drawArc(new RectF(cx-s*.16f,cy-s*.28f,cx+s*.16f,cy+s*.05f),180,-180,false,p);return;
            }
            if(kind.equals("settings")){
                c.drawCircle(cx,cy,s*.22f,p);for(int i=0;i<8;i++){double a=i*Math.PI/4;float x1=cx+(float)Math.cos(a)*s*.25f,y1=cy+(float)Math.sin(a)*s*.25f;float x2=cx+(float)Math.cos(a)*s*.37f,y2=cy+(float)Math.sin(a)*s*.37f;c.drawLine(x1,y1,x2,y2,p);}c.drawCircle(cx,cy,s*.08f,p);return;
            }
            if(kind.equals("spark")||kind.equals("star")){
                Path st=new Path();for(int i=0;i<8;i++){double a=-Math.PI/2+i*Math.PI/4;float rr=(i%2==0)?s*.35f:s*.10f;float x=cx+(float)Math.cos(a)*rr,y=cy+(float)Math.sin(a)*rr;if(i==0)st.moveTo(x,y);else st.lineTo(x,y);}st.close();p.setStyle(Paint.Style.FILL);c.drawPath(st,p);return;
            }
            if(kind.equals("edit")){
                c.drawLine(cx-s*.25f,cy+s*.25f,cx+s*.25f,cy-s*.25f,p);c.drawLine(cx-s*.31f,cy+s*.31f,cx-s*.12f,cy+s*.27f,p);return;
            }
            if(kind.equals("arrowRight")||kind.equals("arrowLeft")){
                boolean right=kind.equals("arrowRight");Path a=new Path();float dir=right?1:-1;a.moveTo(cx-dir*s*.13f,cy-s*.20f);a.lineTo(cx+dir*s*.18f,cy);a.lineTo(cx-dir*s*.13f,cy+s*.20f);c.drawPath(a,p);return;
            }
            if(kind.equals("copy")){
                c.drawRoundRect(new RectF(cx-s*.28f,cy-s*.20f,cx+s*.08f,cy+s*.28f),s*.05f,s*.05f,p);c.drawRoundRect(new RectF(cx-s*.10f,cy-s*.30f,cx+s*.27f,cy+s*.18f),s*.05f,s*.05f,p);return;
            }
            if(kind.equals("share")){
                p.setStyle(Paint.Style.FILL);c.drawCircle(cx-s*.25f,cy,s*.09f,p);c.drawCircle(cx+s*.23f,cy-s*.22f,s*.09f,p);c.drawCircle(cx+s*.23f,cy+s*.22f,s*.09f,p);p.setStyle(Paint.Style.STROKE);c.drawLine(cx-s*.17f,cy-s*.05f,cx+s*.15f,cy-s*.17f,p);c.drawLine(cx-s*.17f,cy+s*.05f,cx+s*.15f,cy+s*.17f,p);return;
            }
            p.setStyle(Paint.Style.FILL);c.drawCircle(cx,cy,s*.28f,p);
        }
    }

    private class PlatformIconView extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path=new Path();
        private final String name;
        PlatformIconView(Context c,String name){super(c);this.name=name==null?"":name;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}

        @Override protected void onDraw(Canvas c){
            float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f,s=Math.min(w,h);
            String x=name.toLowerCase(Locale.ROOT);

            // Soft icon plate.
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(55,255,255,255));
            c.drawCircle(cx,cy,s*.38f,p);

            if(x.contains("chatgpt")){drawChatGPT(c,cx,cy,s);return;}
            if(x.contains("claude")){drawClaude(c,cx,cy,s);return;}
            if(x.contains("gemini")){drawGemini(c,cx,cy,s);return;}
            if(x.contains("midjourney")){drawMidjourney(c,cx,cy,s);return;}
            if(x.contains("perplexity")){drawPerplexity(c,cx,cy,s);return;}
            if(x.contains("stable diffusion")){drawStable(c,cx,cy,s);return;}
            if(x.contains("elevenlabs")){drawEleven(c,cx,cy,s);return;}
            if(x.contains("runway")){drawRunway(c,cx,cy,s);return;}

            drawGeneric(c,cx,cy,s);
        }

        private void stroke(int color,float width){
            p.setStyle(Paint.Style.STROKE);p.setColor(color);p.setStrokeWidth(dp(width));
            p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
            p.setShader(null);
        }

        private void drawChatGPT(Canvas c,float cx,float cy,float s){
            stroke(Color.WHITE,2.3f);
            for(int i=0;i<6;i++){
                c.save();c.rotate(i*60,cx,cy);
                RectF r=new RectF(cx-s*.23f,cy-s*.10f,cx+s*.23f,cy+s*.10f);
                c.drawRoundRect(r,s*.10f,s*.10f,p);c.restore();
            }
            p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);c.drawCircle(cx,cy,s*.065f,p);
        }

        private void drawClaude(Canvas c,float cx,float cy,float s){
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(240,103,55));
            Path st=new Path();
            for(int i=0;i<10;i++){
                double a=-Math.PI/2+i*Math.PI/5;
                float rr=(i%2==0)?s*.28f:s*.11f;
                float xx=cx+(float)Math.cos(a)*rr,yy=cy+(float)Math.sin(a)*rr;
                if(i==0)st.moveTo(xx,yy);else st.lineTo(xx,yy);
            }st.close();c.drawPath(st,p);
        }

        private void drawGemini(Canvas c,float cx,float cy,float s){
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(79,126,255));
            path.reset();
            path.moveTo(cx,cy-s*.32f);path.lineTo(cx+s*.13f,cy-s*.13f);path.lineTo(cx+s*.31f,cy);
            path.lineTo(cx+s*.13f,cy+s*.13f);path.lineTo(cx,cy+s*.32f);
            path.lineTo(cx-s*.13f,cy+s*.13f);path.lineTo(cx-s*.31f,cy);
            path.lineTo(cx-s*.13f,cy-s*.13f);path.close();c.drawPath(path,p);
        }

        private void drawMidjourney(Canvas c,float cx,float cy,float s){
            stroke(Color.WHITE,1.7f);
            path.reset();
            path.moveTo(cx-s*.28f,cy+s*.25f);path.lineTo(cx-s*.10f,cy-s*.24f);path.lineTo(cx+s*.10f,cy+s*.25f);path.lineTo(cx+s*.27f,cy-s*.19f);
            c.drawPath(path,p);
            path.reset();
            path.moveTo(cx-s*.20f,cy+s*.25f);path.lineTo(cx,cy-s*.12f);path.lineTo(cx+s*.19f,cy+s*.25f);
            c.drawPath(path,p);
            c.drawLine(cx-s*.31f,cy+s*.27f,cx+s*.31f,cy+s*.27f,p);
        }

        private void drawPerplexity(Canvas c,float cx,float cy,float s){
            stroke(Color.rgb(0,218,225),1.8f);
            for(int i=0;i<6;i++){
                double a=i*Math.PI/3;
                float x1=cx+(float)Math.cos(a)*s*.07f,y1=cy+(float)Math.sin(a)*s*.07f;
                float x2=cx+(float)Math.cos(a)*s*.29f,y2=cy+(float)Math.sin(a)*s*.29f;
                c.drawLine(x1,y1,x2,y2,p);
            }
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(0,218,225));
            c.drawCircle(cx,cy,s*.07f,p);
        }

        private void drawStable(Canvas c,float cx,float cy,float s){
            stroke(Color.rgb(156,79,255),2.2f);
            c.drawArc(new RectF(cx-s*.25f,cy-s*.25f,cx+s*.25f,cy+s*.25f),55,285,false,p);
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(156,79,255));
            c.drawCircle(cx-s*.10f,cy-s*.08f,s*.035f,p);
            c.drawCircle(cx+s*.10f,cy+s*.08f,s*.035f,p);
        }

        private void drawEleven(Canvas c,float cx,float cy,float s){
            p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);c.drawCircle(cx,cy,s*.29f,p);
            p.setColor(Color.rgb(20,28,45));c.drawRoundRect(new RectF(cx-s*.10f,cy-s*.15f,cx-s*.02f,cy+s*.15f),s*.02f,s*.02f,p);
            c.drawRoundRect(new RectF(cx+s*.02f,cy-s*.15f,cx+s*.10f,cy+s*.15f),s*.02f,s*.02f,p);
        }

        private void drawRunway(Canvas c,float cx,float cy,float s){
            stroke(Color.WHITE,2.2f);
            path.reset();
            path.moveTo(cx-s*.21f,cy+s*.26f);path.lineTo(cx-s*.21f,cy-s*.28f);path.lineTo(cx+s*.14f,cy-s*.28f);
            path.cubicTo(cx+s*.34f,cy-s*.28f,cx+s*.34f,cy-s*.04f,cx+s*.14f,cy-s*.02f);
            path.lineTo(cx-s*.21f,cy+s*.02f);
            c.drawPath(path,p);
            c.drawLine(cx-s*.02f,cy+s*.03f,cx+s*.24f,cy+s*.27f,p);
        }

        private void drawGeneric(Canvas c,float cx,float cy,float s){
            int[] cs=genericColors(name);
            p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,0,getWidth(),getHeight(),cs[0],cs[1],Shader.TileMode.CLAMP));
            c.drawCircle(cx,cy,s*.38f,p);p.setShader(null);
            String ab=abbr(name);
            p.setColor(WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(s*(ab.length()>2?.26f:.32f));
            p.setTypeface(Typeface.create("sans",Typeface.BOLD));
            Paint.FontMetrics fm=p.getFontMetrics();
            c.drawText(ab,cx,cy-(fm.ascent+fm.descent)/2,p);
        }

        private int[] genericColors(String n){
            if(n.contains("voice"))return new int[]{Color.rgb(245,100,160),Color.rgb(120,70,255)};
            if(n.contains("video"))return new int[]{Color.rgb(35,130,255),Color.rgb(75,75,255)};
            if(n.contains("music"))return new int[]{Color.rgb(255,95,125),Color.rgb(130,65,255)};
            return new int[]{BLUE,VIOLET};
        }

        private String abbr(String n){
            String x=n.trim();
            if(x.toLowerCase(Locale.ROOT).contains("adobe firefly"))return "F";
            if(x.toLowerCase(Locale.ROOT).contains("leonardo"))return "L";
            if(x.toLowerCase(Locale.ROOT).contains("freepik"))return "FP";
            if(x.toLowerCase(Locale.ROOT).contains("google veo"))return "V";
            if(x.toLowerCase(Locale.ROOT).contains("kling"))return "K";
            if(x.toLowerCase(Locale.ROOT).contains("pika"))return "P";
            if(x.toLowerCase(Locale.ROOT).contains("suno"))return "S";
            if(x.toLowerCase(Locale.ROOT).contains("udio"))return "U";
            String[] parts=x.split("\\s+");
            if(parts.length>=2)return ""+Character.toUpperCase(parts[0].charAt(0))+Character.toUpperCase(parts[1].charAt(0));
            return x.isEmpty()?"AI":""+Character.toUpperCase(x.charAt(0));
        }
    }

    private class UKFlagView extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        UKFlagView(Context c){super(c);}
        @Override protected void onDraw(Canvas c){
            float w=getWidth(),h=getHeight();
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(1,33,105));c.drawRoundRect(0,0,w,h,dp(4),dp(4),p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.SQUARE);p.setStrokeWidth(Math.max(1,dp(6)));p.setColor(Color.WHITE);
            c.drawLine(0,0,w,h,p);c.drawLine(w,0,0,h,p);
            p.setStrokeWidth(Math.max(1,dp(2.5f)));p.setColor(Color.rgb(200,16,46));c.drawLine(0,0,w,h,p);c.drawLine(w,0,0,h,p);
            p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);c.drawRect(w*.42f,0,w*.58f,h,p);c.drawRect(0,h*.35f,w,h*.65f,p);
            p.setColor(Color.rgb(200,16,46));c.drawRect(w*.46f,0,w*.54f,h,p);c.drawRect(0,h*.43f,w,h*.57f,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(Color.argb(100,255,255,255));c.drawRoundRect(0,0,w,h,dp(4),dp(4),p);
        }
    }

    private class SyrianFlagView extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path star=new Path();
        SyrianFlagView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            float w=getWidth(),h=getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(0,122,61));c.drawRect(0,0,w,h/3f,p);
            p.setColor(Color.WHITE);c.drawRect(0,h/3f,w,2*h/3f,p);
            p.setColor(Color.BLACK);c.drawRect(0,2*h/3f,w,h,p);
            for(int i=0;i<3;i++)drawStar(c,w*(.25f+.25f*i),h*.5f,Math.min(w,h)*.18f);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(Color.argb(90,255,255,255));c.drawRoundRect(0,0,w,h,dp(4),dp(4),p);
        }
        private void drawStar(Canvas c,float cx,float cy,float r){
            star.reset();
            for(int i=0;i<10;i++){
                double a=-Math.PI/2+i*Math.PI/5;float rr=(i%2==0)?r:r*.42f;float x=cx+(float)Math.cos(a)*rr,y=cy+(float)Math.sin(a)*rr;
                if(i==0)star.moveTo(x,y);else star.lineTo(x,y);
            }
            star.close();p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(206,17,38));c.drawPath(star,p);
        }
    }
}
