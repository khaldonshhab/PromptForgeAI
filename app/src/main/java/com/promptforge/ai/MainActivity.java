package com.promptforge.ai;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private Storage s;
    private List<Platform> ps;
    private String lang="", sel="ChatGPT", task="General", last="";
    private String currentScreen="login";

    private final int BG=Color.rgb(5,8,23), PANEL=Color.rgb(13,24,49), PANEL2=Color.rgb(9,18,40);
    private final int BORDER=Color.rgb(43,61,105), BLUE=Color.rgb(25,191,255), PURPLE=Color.rgb(123,77,255);
    private final int WHITE=Color.rgb(246,248,255), MUTED=Color.rgb(171,183,211);

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(true);
        s=new Storage(this);
        ps=PlatformRepository.all();
        lang=s.lang();
        if(lang.isEmpty()) { lang="ar"; s.lang(lang); }
        showSplash();
    }

    @Override public void onBackPressed(){
        if("home".equals(currentScreen) || "login".equals(currentScreen)){
            confirmExit();
        }else if("splash".equals(currentScreen)){
            confirmExit();
        }else{
            home();
        }
    }

    private void confirmExit(){
        new AlertDialog.Builder(this)
            .setTitle(tr("مغادرة التطبيق","Exit app"))
            .setMessage(tr("هل تريد مغادرة التطبيق؟","Do you want to exit the app?"))
            .setPositiveButton(tr("نعم","Yes"),(d,w)->finishAffinity())
            .setNegativeButton(tr("لا","No"),null)
            .show();
    }

    private void setScreen(String screen){ currentScreen=screen; }

    private boolean ar(){return "ar".equals(lang);}
    private String tr(String a,String e){return ar()?a:e;}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}

    private void showSplash(){
        setScreen("splash");
        FrameLayout root=new FrameLayout(this);
        root.setBackgroundColor(BG);

        LinearLayout center=new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER_HORIZONTAL);

        PromptForgeLogoView logo=new PromptForgeLogoView(this);
        center.addView(logo,new LinearLayout.LayoutParams(dp(150),dp(150)));

        addGap(center,18);
        TextView title=text("PromptForge AI",30,WHITE,true);
        title.setGravity(Gravity.CENTER);
        title.setTextDirection(View.TEXT_DIRECTION_LTR);
        center.addView(title,new LinearLayout.LayoutParams(-1,dp(48)));

        TextView sub=text(tr("هندسة برومبتات دقيقة","Precision Prompt Engineering"),14,MUTED,false);
        sub.setGravity(Gravity.CENTER);
        center.addView(sub,new LinearLayout.LayoutParams(-1,dp(34)));

        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(-1,-2);
        cp.gravity=Gravity.CENTER;
        cp.leftMargin=dp(28); cp.rightMargin=dp(28);
        root.addView(center,cp);

        showRoot(root);
        new Handler(Looper.getMainLooper()).postDelayed(this::loginScreen,900);
    }

    private String bidi(String x){
        if(x==null || !ar()) return x;
        StringBuilder out=new StringBuilder();
        boolean latin=false;
        for(int i=0;i<x.length();i++){
            char ch=x.charAt(i);
            boolean now=(ch<128 && (Character.isLetterOrDigit(ch) || ".:/_-@+#%()".indexOf(ch)>=0));
            if(now&&!latin) out.append('\u2066');
            if(!now&&latin) out.append('\u2069');
            out.append(ch);
            latin=now;
        }
        if(latin) out.append('\u2069');
        return out.toString();
    }

    private LinearLayout column(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(18),dp(18),dp(18),dp(24));
        root.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        return root;
    }

    private ScrollView scroll(View child){
        ScrollView sc=new ScrollView(this);
        sc.setFillViewport(true);
        sc.setClipToPadding(false);
        sc.addView(child);
        return sc;
    }

    private void showRoot(View v){
        setContentView(v);
        v.setOnApplyWindowInsetsListener((view,insets)->{
            int l=getResources().getDisplayMetrics().densityDpi;
            if(Build.VERSION.SDK_INT>=30){
                android.graphics.Insets z=insets.getInsets(WindowInsets.Type.systemBars());
                view.setPadding(view.getPaddingLeft(),dp(18)+z.top,view.getPaddingRight(),dp(24)+z.bottom);
            }else{
                view.setPadding(view.getPaddingLeft(),dp(18)+insets.getSystemWindowInsetTop(),view.getPaddingRight(),dp(24)+insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        v.requestApplyInsets();
    }

    private TextView text(String value,float size,int color,boolean bold){
        TextView t=new TextView(this);
        t.setText(bidi(value));
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL);
        t.setIncludeFontPadding(true);
        t.setTextDirection(ar()?View.TEXT_DIRECTION_FIRST_STRONG:View.TEXT_DIRECTION_LTR);
        t.setGravity(ar()?Gravity.RIGHT:Gravity.LEFT);
        t.setMaxLines(20);
        return t;
    }

    private TextView button(String value,boolean primary){
        TextView b=new TextView(this);
        b.setText(bidi(value));
        b.setTextSize(16);
        b.setTextColor(WHITE);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setIncludeFontPadding(true);
        b.setClickable(true);
        b.setFocusable(true);
        b.setPadding(dp(12),0,dp(12),0);
        if(primary){
            GradientDrawable g=new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{BLUE,PURPLE});
            g.setCornerRadius(dp(22));
            b.setBackground(g);
        }else{
            b.setBackground(rounded(PANEL2,BORDER,22));
        }
        return b;
    }

    private GradientDrawable rounded(int fill,int stroke,int radius){
        GradientDrawable d=new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radius));
        if(stroke!=0)d.setStroke(dp(1),stroke);
        return d;
    }

    private TextView smallBack(){
        TextView b=button(tr("‹ رجوع","‹ Back"),false);
        b.setTextSize(14);
        b.setOnClickListener(v->home());
        return b;
    }

    private void titleBar(LinearLayout root,String arTitle,String enTitle){
        FrameLayout bar=new FrameLayout(this);
        TextView title=text(tr(arTitle,enTitle),25,WHITE,true);
        title.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,dp(58));
        bar.addView(title,tp);
        View divider=new View(this);
        divider.setBackgroundColor(Color.rgb(27,39,70));
        FrameLayout.LayoutParams dpv=new FrameLayout.LayoutParams(-1,dp(1));
        dpv.gravity=Gravity.BOTTOM;
        bar.addView(divider,dpv);
        root.addView(bar,new LinearLayout.LayoutParams(-1,dp(60)));
    }

    private void addGap(LinearLayout r,int h){Space sp=new Space(this);r.addView(sp,new LinearLayout.LayoutParams(1,dp(h)));}

    private FrameLayout card(String icon,String arTitle,String enTitle,String arDesc,String enDesc,View.OnClickListener click){
        FrameLayout box=new FrameLayout(this);
        box.setBackground(rounded(PANEL,BORDER,26));
        box.setClickable(true);
        box.setFocusable(true);
        box.setOnClickListener(click);
        box.setPadding(dp(16),dp(8),dp(16),dp(8));

        LinearLayout texts=new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setGravity(Gravity.CENTER_VERTICAL);
        texts.setLayoutDirection(ar()?View.LAYOUT_DIRECTION_RTL:View.LAYOUT_DIRECTION_LTR);
        TextView h=text(tr(arTitle,enTitle),18,WHITE,true);
        TextView d=text(tr(arDesc,enDesc),13,MUTED,false);
        texts.addView(h,new LinearLayout.LayoutParams(-1,dp(30)));
        texts.addView(d,new LinearLayout.LayoutParams(-1,dp(28)));

        FrameLayout.LayoutParams tx=new FrameLayout.LayoutParams(-1,-1);
        tx.gravity=Gravity.CENTER;
        int end=dp(72);
        if(ar()) tx.rightMargin=end; else tx.leftMargin=end;
        box.addView(texts,tx);

        TextView ic=text(icon,28,BLUE,true);
        ic.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(dp(56),dp(60));
        ip.gravity=(ar()?Gravity.RIGHT:Gravity.LEFT)|Gravity.CENTER_VERTICAL;
        box.addView(ic,ip);
        return box;
    }

    private void chooseLanguage(){
        LinearLayout r=column();
        r.setGravity(Gravity.CENTER_HORIZONTAL);

        addGap(r,12);
        TextView logo=text("PROMPTFORGE",30,WHITE,true);
        logo.setGravity(Gravity.CENTER);
        logo.setTextDirection(View.TEXT_DIRECTION_LTR);
        r.addView(logo,new LinearLayout.LayoutParams(-1,dp(46)));
        TextView ai=text("AI",22,BLUE,true);
        ai.setGravity(Gravity.CENTER);
        ai.setTextDirection(View.TEXT_DIRECTION_LTR);
        r.addView(ai,new LinearLayout.LayoutParams(-1,dp(34)));
        addGap(r,20);

        TextView head=text(tr("اختر لغة التطبيق","Choose app language"),19,WHITE,true);
        head.setGravity(Gravity.CENTER);
        r.addView(head,new LinearLayout.LayoutParams(-1,dp(44)));

        addGap(r,14);
        LinearLayout arRow=new LinearLayout(this);
        arRow.setGravity(Gravity.CENTER_VERTICAL);
        arRow.setPadding(dp(14),0,dp(14),0);
        arRow.setBackground(rounded(PANEL,BORDER,18));
        SyrianFlagView flag=new SyrianFlagView(this);
        arRow.addView(flag,new LinearLayout.LayoutParams(dp(34),dp(22)));
        TextView at=text("العربية",18,WHITE,true);
        at.setGravity(Gravity.CENTER);
        arRow.addView(at,new LinearLayout.LayoutParams(0,dp(56),1));
        arRow.setOnClickListener(v->{lang="ar";s.lang(lang);loginScreen();});
        r.addView(arRow,new LinearLayout.LayoutParams(-1,dp(62)));

        addGap(r,12);
        LinearLayout enRow=new LinearLayout(this);
        enRow.setGravity(Gravity.CENTER_VERTICAL);
        enRow.setPadding(dp(14),0,dp(14),0);
        enRow.setBackground(rounded(PANEL,BORDER,18));
        TextView enIcon=text("EN",16,BLUE,true);
        enIcon.setGravity(Gravity.CENTER);
        enRow.addView(enIcon,new LinearLayout.LayoutParams(dp(40),dp(40)));
        TextView et=text("English",18,WHITE,true);
        et.setGravity(Gravity.CENTER);
        et.setTextDirection(View.TEXT_DIRECTION_LTR);
        enRow.addView(et,new LinearLayout.LayoutParams(0,dp(56),1));
        enRow.setOnClickListener(v->{lang="en";s.lang(lang);loginScreen();});
        r.addView(enRow,new LinearLayout.LayoutParams(-1,dp(62)));

        addGap(r,20);
        TextView note=text(tr("يمكن تغيير اللغة من الإعدادات لاحقاً","You can change the language later from Settings"),13,MUTED,false);
        note.setGravity(Gravity.CENTER);
        r.addView(note,new LinearLayout.LayoutParams(-1,dp(36)));

        showRoot(r);
    }

    private View brandHeader(int height,boolean withLanguage){
        FrameLayout top=new FrameLayout(this);
        top.setPadding(0,0,0,dp(4));

        LinearLayout brand=new LinearLayout(this);
        brand.setOrientation(LinearLayout.HORIZONTAL);
        brand.setGravity(Gravity.CENTER_VERTICAL);

        PromptForgeLogoView mini=new PromptForgeLogoView(this);
        brand.addView(mini,new LinearLayout.LayoutParams(dp(34),dp(34)));

        TextView name=text("PromptForge AI",18,WHITE,true);
        name.setTextDirection(View.TEXT_DIRECTION_LTR);
        name.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
        LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-2,dp(40));
        np.leftMargin=dp(8);
        brand.addView(name,np);

        FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-2,dp(height));
        bp.gravity=Gravity.LEFT|Gravity.CENTER_VERTICAL;
        top.addView(brand,bp);

        if(withLanguage){
            View lang=languageControl();
            FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(40),dp(40));
            lp.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
            top.addView(lang,lp);
        }

        View line=new View(this);
        line.setBackgroundColor(Color.rgb(27,39,70));
        FrameLayout.LayoutParams lp2=new FrameLayout.LayoutParams(-1,dp(1));
        lp2.gravity=Gravity.BOTTOM;
        top.addView(line,lp2);

        return top;
    }

    private View languageControl(){
        FrameLayout wrap=new FrameLayout(this);
        wrap.setBackground(rounded(PANEL2,BORDER,12));
        wrap.setClickable(true);
        wrap.setFocusable(true);
        wrap.setPadding(dp(6),dp(6),dp(6),dp(6));

        View flag=ar()?new SyrianFlagView(this):new UKFlagView(this);
        FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(dp(28),dp(18));
        fp.gravity=Gravity.CENTER;
        wrap.addView(flag,fp);

        wrap.setContentDescription(ar()?"تغيير لغة التطبيق":"Change app language");
        wrap.setOnClickListener(v->languageDialog());
        return wrap;
    }

    private void languageDialog(){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18),dp(8),dp(18),dp(8));

        LinearLayout arRow=languageRow(new SyrianFlagView(this),"العربية");
        arRow.setOnClickListener(v->{lang="ar";s.lang(lang);loginScreen();});
        box.addView(arRow,new LinearLayout.LayoutParams(-1,dp(58)));

        addGap(box,8);
        LinearLayout enRow=languageRow(new UKFlagView(this),"English");
        enRow.setOnClickListener(v->{lang="en";s.lang(lang);loginScreen();});
        box.addView(enRow,new LinearLayout.LayoutParams(-1,dp(58)));

        new AlertDialog.Builder(this)
            .setTitle(tr("لغة التطبيق","App language"))
            .setView(box)
            .setNegativeButton(tr("إلغاء","Cancel"),null)
            .show();
    }

    private LinearLayout languageRow(View flag,String label){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12),0,dp(12),0);
        row.setBackground(rounded(PANEL2,BORDER,14));

        row.addView(flag,new LinearLayout.LayoutParams(dp(30),dp(20)));
        TextView t=text(label,16,WHITE,true);
        t.setGravity(Gravity.CENTER);
        t.setTextDirection(label.equals("English")?View.TEXT_DIRECTION_LTR:View.TEXT_DIRECTION_RTL);
        row.addView(t,new LinearLayout.LayoutParams(0,dp(52),1));
        return row;
    }

    private void home(){
        setScreen("home");
        LinearLayout r=column();

        FrameLayout top=new FrameLayout(this);
        TextView brand=text("PromptForge AI",22,WHITE,true);
        brand.setGravity(Gravity.CENTER);
        brand.setTextDirection(View.TEXT_DIRECTION_LTR);
        FrameLayout.LayoutParams brp=new FrameLayout.LayoutParams(-1,dp(54));
brp.leftMargin=dp(102); brp.rightMargin=dp(102);
        top.addView(brand,brp);

        View langBtn=languageControl();
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(118),dp(44));
        lp.gravity=Gravity.CENTER_VERTICAL|Gravity.END;
        top.addView(langBtn,lp);
        r.addView(top,new LinearLayout.LayoutParams(-1,dp(58)));

        addGap(r,4);
        TextView hero=text(tr("حوّل فكرتك إلى برومبت احترافي","Turn your idea into a professional prompt"),22,WHITE,true);
        hero.setGravity(ar()?Gravity.RIGHT:Gravity.LEFT);
        r.addView(hero,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView sub=text(tr("محرك محلي سريع مع دعم عشرات أدوات الذكاء الاصطناعي وقابلية التوسعة","Fast local engine with dozens of AI tools and an extensible catalog"),14,MUTED,false);
        r.addView(sub,new LinearLayout.LayoutParams(-1,dp(48)));

        addGap(r,12);
        addGap(r,10);
        r.addView(card("✦","إنشاء برومبت","Create Prompt","حوّل الفكرة إلى برومبت جاهز","Turn an idea into a ready prompt",v->create()),new LinearLayout.LayoutParams(-1,dp(86)));
        addGap(r,10);
        r.addView(card("✎","تحسين برومبت","Improve Prompt","حسّن أي برومبت موجود","Upgrade any existing prompt",v->improve()),new LinearLayout.LayoutParams(-1,dp(86)));
        addGap(r,10);
        r.addView(card("◎","كل المنصات","All AI Platforms",ar()?ps.size()+" منصة وأداة":ps.size()+" tools and platforms",ar()?ps.size()+" منصة وأداة":ps.size()+" tools and platforms",v->platforms()),new LinearLayout.LayoutParams(-1,dp(86)));
        addGap(r,10);
        r.addView(card("★","المحفوظات","Saved","البرومبتات التي حفظتها","Your saved prompts",v->listScreen("saved")),new LinearLayout.LayoutParams(-1,dp(86)));
        addGap(r,10);
        r.addView(card("◷","السجل","History","آخر البرومبتات","Latest generated prompts",v->listScreen("history")),new LinearLayout.LayoutParams(-1,dp(86)));

        addGap(r,12);

        TextView settings=button(tr("⚙  الإعدادات","⚙  Settings"),false);
        settings.setOnClickListener(v->settings());
        r.addView(settings,new LinearLayout.LayoutParams(-1,dp(58)));

        showRoot(scroll(r));
    }

    private EditText editor(String arHint,String enHint,int minLines){
        EditText e=new EditText(this);
        e.setTextColor(WHITE);
        e.setHintTextColor(MUTED);
        e.setHint(bidi(tr(arHint,enHint)));
        e.setTextSize(16);
        e.setGravity(ar()?Gravity.TOP|Gravity.RIGHT:Gravity.TOP|Gravity.LEFT);
        e.setTextDirection(ar()?View.TEXT_DIRECTION_FIRST_STRONG:View.TEXT_DIRECTION_LTR);
        e.setPadding(dp(16),dp(16),dp(16),dp(16));
        e.setMinLines(minLines);
        e.setBackground(rounded(PANEL2,BORDER,22));
        return e;
    }

    private void create(){
        setScreen("create");
        LinearLayout r=column();
        titleBar(r,"إنشاء Prompt","Create Prompt");
        EditText idea=editor("اكتب فكرتك هنا...","Describe your idea...",6);
        r.addView(idea,new LinearLayout.LayoutParams(-1,dp(180)));
        addGap(r,10);

        TextView platform=button(tr("المنصة: ","Platform: ")+bidi(sel),true);
        platform.setOnClickListener(v->platforms());
        r.addView(platform,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(r,8);

        TextView taskBtn=button(taskLabel(),false);
        taskBtn.setOnClickListener(v->taskDialog(taskBtn));
        r.addView(taskBtn,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(r,10);

        TextView gen=button(tr("✦  توليد البرومبت","✦  Generate Prompt"),true);
        gen.setOnClickListener(v->generate(idea.getText().toString()));
        r.addView(gen,new LinearLayout.LayoutParams(-1,dp(62)));

        addGap(r,8);
        TextView remain=text(tr("المتاح اليوم: ","Today remaining: ")+s.remaining(),13,MUTED,false);
        r.addView(remain,new LinearLayout.LayoutParams(-1,dp(32)));

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

    private String taskLabel(){return tr("نوع المهمة: ","Task: ")+bidi(taskName());}

    private void taskDialog(TextView target){
        String[] items=ar()
            ?new String[]{"عام","تسويق","برومبت صورة","برومبت فيديو","صوت / TTS","برمجة","بحث","تعليم","سوشيال ميديا","أعمال"}
            :new String[]{"General","Marketing","Image prompt","Video prompt","Voice / TTS","Coding","Research","Education","Social media","Business"};
        new AlertDialog.Builder(this).setTitle(tr("اختر نوع المهمة","Choose task")).setItems(items,(d,w)->{
            String[] canonical={"General","Marketing","Image prompt","Video prompt","Voice / TTS","Coding","Research","Education","Social media","Business"};
            task=canonical[w];
            target.setText(bidi(taskLabel()));
        }).show();
    }

    private void generate(String idea){
        String base="https://promptforge-backend-2p4q.onrender.com";
        if(base.isEmpty()){
            local(idea);
        }else{
            toast(tr("جار التوليد...","Generating..."));
            new RemotePromptClient().generate(base,idea,sel,task,lang,s.accountToken(),(ok,val)->runOnUiThread(()->{
                if(ok){last=val;s.add("history",last);result();}
                else{local(idea);}
            }));
        }
    }

    private void local(String idea){
        last=PromptEngine.generate(idea,find(sel),task,ar());
        s.add("history",last);
        result();
    }

    private void result(){
        setScreen("result");
        LinearLayout r=column();
        titleBar(r,"النتيجة","Result");
        TextView out=text(last,16,WHITE,false);
        out.setBackground(rounded(PANEL,BORDER,20));
        out.setPadding(dp(16),dp(16),dp(16),dp(16));
        out.setTextIsSelectable(true);
        r.addView(out,new LinearLayout.LayoutParams(-1,dp(400)));
        addGap(r,10);

        TextView copy=button("نسخ".equals("English")? "Copy":tr("نسخ","Copy"),true);
        copy.setOnClickListener(v->copy(last));
        r.addView(copy,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(r,8);

        TextView save=button("★  "+tr("حفظ","Save"),false);
        save.setOnClickListener(v->{s.add("saved",last);toast(tr("تم الحفظ","Saved"));});
        r.addView(save,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(r,8);

        TextView share=button(tr("مشاركة","Share"),false);
        share.setOnClickListener(v->{
            Intent i=new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT,last);
            startActivity(Intent.createChooser(i,tr("مشاركة","Share")));
        });
        r.addView(share,new LinearLayout.LayoutParams(-1,dp(58)));

        showRoot(scroll(r));
    }

    private void improve(){
        setScreen("improve");
        LinearLayout r=column();
        titleBar(r,"تحسين Prompt","Improve Prompt");
        EditText input=editor("ألصق البرومبت هنا...","Paste your prompt...",8);
        r.addView(input,new LinearLayout.LayoutParams(-1,dp(210)));
        addGap(r,10);
        TextView b=button(tr("✦  تحسين البرومبت","✦  Improve Prompt"),true);
        b.setOnClickListener(v->{last=PromptEngine.improve(input.getText().toString(),ar());s.add("history",last);result();});
        r.addView(b,new LinearLayout.LayoutParams(-1,dp(62)));
        showRoot(scroll(r));
    }

    private void platforms(){
        setScreen("platforms");
        LinearLayout r=column();
        titleBar(r,"منصات الذكاء الاصطناعي","AI Platforms");
        EditText q=editor("بحث عن منصة...","Search platforms...",1);
        q.setSingleLine(true);
        q.setGravity(ar()?Gravity.CENTER_VERTICAL|Gravity.RIGHT:Gravity.CENTER_VERTICAL|Gravity.LEFT);
        r.addView(q,new LinearLayout.LayoutParams(-1,dp(56)));
        addGap(r,10);

        LinearLayout list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        Runnable fill=()->{
            list.removeAllViews();
            String query=q.getText().toString().trim().toLowerCase(Locale.ROOT);
            for(Platform p:ps){
                String n=p.name.toLowerCase(Locale.ROOT), c=p.category.toLowerCase(Locale.ROOT);
                if(query.isEmpty()||n.contains(query)||c.contains(query)){
                    TextView item=button(bidi(p.name)+"  •  "+bidi(p.category),false);
                    item.setGravity(ar()?Gravity.RIGHT|Gravity.CENTER_VERTICAL:Gravity.LEFT|Gravity.CENTER_VERTICAL);
                    item.setPadding(dp(18),0,dp(18),0);
                    item.setOnClickListener(v->platformChoice(p));
                    list.addView(item,new LinearLayout.LayoutParams(-1,dp(54)));
                    addGap(list,7);
                }
            }
            if(list.getChildCount()==0){
                TextView empty=text(tr("لا توجد نتائج","No results"),15,MUTED,false);
                empty.setGravity(Gravity.CENTER);
                list.addView(empty,new LinearLayout.LayoutParams(-1,dp(60)));
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

    private void platformChoice(Platform p){
        new AlertDialog.Builder(this)
            .setTitle(bidi(p.name))
            .setMessage(bidi(p.category))
            .setPositiveButton(tr("اختيار","Select"),(d,w)->{sel=p.name;create();})
            .setNegativeButton(tr("فتح المنصة","Open"),(d,w)->open(p.url))
            .show();
    }

    private Platform find(String name){
        for(Platform p:ps)if(p.name.equals(name))return p;
        return ps.get(0);
    }

    private void listScreen(String kind){
        setScreen("list");
        LinearLayout r=column();
        titleBar(r,kind.equals("saved")?"المحفوظات":"السجل",kind.equals("saved")?"Saved":"History");
        List<String> items=s.list(kind);
        if(items.isEmpty()){
            TextView e=text(tr("لا يوجد شيء بعد","Nothing here yet"),16,MUTED,false);
            e.setGravity(Gravity.CENTER);
            r.addView(e,new LinearLayout.LayoutParams(-1,dp(100)));
        }else{
            for(String item:items){
                TextView row=text(item,14,WHITE,false);
                row.setBackground(rounded(PANEL,BORDER,18));
                row.setPadding(dp(14),dp(14),dp(14),dp(14));
                row.setTextIsSelectable(true);
                r.addView(row,new LinearLayout.LayoutParams(-1,dp(180)));
                addGap(r,8);
            }
        }
        showRoot(scroll(r));
    }

    private void loginScreen(){
        setScreen("login");
        LinearLayout r=column();
        r.setGravity(Gravity.CENTER_HORIZONTAL);
        addGap(r,6);
        r.addView(brandHeader(54,false),new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(r,8);
        PromptForgeLogoView loginLogo=new PromptForgeLogoView(this);
        LinearLayout logoHolder=new LinearLayout(this);
        logoHolder.setGravity(Gravity.CENTER);
        logoHolder.addView(loginLogo,new LinearLayout.LayoutParams(dp(92),dp(92)));
        r.addView(logoHolder,new LinearLayout.LayoutParams(-1,dp(98)));
        addGap(r,14);
        TextView head=text(tr("تسجيل الدخول","Sign in"),28,WHITE,true);
        head.setGravity(Gravity.CENTER);
        r.addView(head,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView sub=text(tr("أدخل بيانات حسابك للمتابعة","Enter your account details to continue"),14,MUTED,false);
        sub.setGravity(Gravity.CENTER);
        r.addView(sub,new LinearLayout.LayoutParams(-1,dp(40)));
        addGap(r,18);
        LinearLayout form=new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(18),dp(18),dp(18),dp(18));
        form.setBackground(rounded(PANEL,BORDER,28));

        EditText user=editor("اسم المستخدم","Username",1);
        user.setSingleLine(true);
        form.addView(user,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(form,10);

        EditText pass=editor("كلمة المرور","Password",1);
        pass.setSingleLine(true);
        pass.setInputType(0x00000081);
        form.addView(pass,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(form,16);

        TextView login=button(tr("دخول","Sign in"),true);
        login.setOnClickListener(v->{
            String base="https://promptforge-backend-2p4q.onrender.com";
            String u=user.getText().toString().trim(), p=pass.getText().toString();
            if(u.isEmpty()||p.isEmpty()){toast(tr("أدخل اسم المستخدم وكلمة المرور","Enter username and password"));return;}
            login.setEnabled(false);
            new RemotePromptClient().login(base,u,p,(ok,val)->runOnUiThread(()->{
                login.setEnabled(true);
                if(!ok){
                    String err=val==null?"":val.trim();
                    if(err.contains("auth_not_configured")) toast(tr("الخادم غير مهيأ للمصادقة: راجع PF_AUTH_SECRET","Server authentication is not configured: check PF_AUTH_SECRET"));
                    else if(err.contains("invalid_credentials")) toast(tr("اسم المستخدم أو كلمة المرور غير صحيحين","Invalid username or password"));
                    else if(err.isEmpty()) toast(tr("تعذر الاتصال بالخادم","Could not reach the server"));
                    else toast(tr("خطأ من الخادم: "+err,"Server error: "+err));
                    return;
                }
                try{
                    org.json.JSONObject j=new org.json.JSONObject(val);
                    boolean admin=j.optBoolean("admin",false);
                    s.account(j.optString("username",""),j.optString("token",""),admin);
                    s.accountPremium(false);
                    toast(tr("تم تسجيل الدخول","Signed in"));
                    home();
                }catch(Exception e){toast(tr("تعذر قراءة استجابة الخادم","Invalid server response"));}
            }));
        });
        form.addView(login,new LinearLayout.LayoutParams(-1,dp(62)));
        r.addView(form,new LinearLayout.LayoutParams(-1,-2));
        addGap(r,10);
        showRoot(r);
    }

    private void account(){
        setScreen("account");
        LinearLayout r=column();
        titleBar(r,tr("حساب المستخدم","User Account"),"User Account");
        if(!s.accountUser().isEmpty()){
            r.addView(text(tr("مسجل الدخول باسم: ","Signed in as: ")+s.accountUser(),16,WHITE,true),new LinearLayout.LayoutParams(-1,dp(50)));
            addGap(r,8);
            TextView change=button(tr("تغيير كلمة المرور","Change password"),true);
            change.setOnClickListener(v->{
                EditText np=editor("كلمة المرور الجديدة (8 أحرف على الأقل)","New password (8+ characters)",1); np.setSingleLine(true); np.setInputType(0x00000081);
                new AlertDialog.Builder(this).setTitle(tr("تعديل الحساب","Edit account")).setView(np).setPositiveButton(tr("حفظ","Save"),(d,w)->{
                    String pass=np.getText().toString(); if(pass.length()<8){toast(tr("كلمة المرور يجب أن تكون 8 أحرف على الأقل","Password must be at least 8 characters"));return;}
                    String base="https://promptforge-backend-2p4q.onrender.com"; change.setEnabled(false);
                    new RemotePromptClient().updatePassword(base,s.accountToken(),pass,(ok,val)->runOnUiThread(()->{change.setEnabled(true);if(!ok){toast(tr("تعذر تعديل الحساب","Could not update account"));return;}try{org.json.JSONObject j=new org.json.JSONObject(val);s.account(j.optString("username",s.accountUser()),j.optString("token",s.accountToken()));toast(tr("تم تعديل كلمة المرور","Password updated"));}catch(Exception e){toast(tr("تم التعديل","Updated"));}}));
                }).setNegativeButton(tr("إلغاء","Cancel"),null).show();
            });
            r.addView(change,new LinearLayout.LayoutParams(-1,dp(58)));
            addGap(r,8);
            TextView out=button(tr("تسجيل الخروج","Log out"),false);
            out.setOnClickListener(v->{s.logout();toast(tr("تم تسجيل الخروج","Logged out"));loginScreen();});
            r.addView(out,new LinearLayout.LayoutParams(-1,dp(58)));
            showRoot(scroll(r)); return;
        }
        r.addView(text(tr("سجّل الدخول بحسابك.","Sign in with your account."),14,MUTED,false),new LinearLayout.LayoutParams(-1,dp(70)));
        addGap(r,8);
        EditText user=editor("اسم المستخدم","Username",1); user.setSingleLine(true); r.addView(user,new LinearLayout.LayoutParams(-1,dp(56)));
        addGap(r,8);
        EditText pass=editor("كلمة المرور","Password",1); pass.setSingleLine(true); pass.setInputType(0x00000081); r.addView(pass,new LinearLayout.LayoutParams(-1,dp(56)));
        addGap(r,10);
        TextView login=button(tr("تسجيل الدخول","Sign in"),true);
        login.setOnClickListener(v->{String base="https://promptforge-backend-2p4q.onrender.com";if(base.isEmpty()){toast(tr("ضع رابط الخادم أولاً من الإعدادات.","Set the backend URL in Settings first."));return;}login.setEnabled(false);new RemotePromptClient().login(base,user.getText().toString().trim(),pass.getText().toString(),(ok,val)->runOnUiThread(()->{login.setEnabled(true);if(!ok){toast(tr("اسم المستخدم أو كلمة المرور غير صحيحين.","Invalid username or password."));return;}try{org.json.JSONObject j=new org.json.JSONObject(val);boolean admin=j.optBoolean("admin",false);s.account(j.optString("username",""),j.optString("token",""),admin);s.accountPremium(false);toast(tr("تم تسجيل الدخول وتفعيل الحساب.","Signed in and account activated."));home();}catch(Exception e){toast(tr("تعذر قراءة استجابة الخادم.","Invalid server response."));}}));});
        r.addView(login,new LinearLayout.LayoutParams(-1,dp(58)));
        showRoot(scroll(r));
    }

    private void settings(){
        setScreen("settings");
        LinearLayout r=column();
        titleBar(r,"الإعدادات","Settings");

        if(s.isAdmin()){
            TextView admin=button(tr("🛡  لوحة تحكم الأدمن","🛡  Admin Control Panel"),true);
            admin.setOnClickListener(v->{Intent i=new Intent(this,AdminActivity.class);i.putExtra("admin_token",s.accountToken());startActivity(i);});
            r.addView(admin,new LinearLayout.LayoutParams(-1,dp(58)));
            addGap(r,12);
        }

        TextView l=button(tr("لغة التطبيق: العربية","App language: 🇬🇧 English"),false);
        l.setOnClickListener(v->languageDialog());
        r.addView(l,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(r,12);

        LinearLayout backendCard=new LinearLayout(this);
        backendCard.setOrientation(LinearLayout.VERTICAL);
        backendCard.setPadding(dp(16),dp(16),dp(16),dp(16));
        backendCard.setBackground(rounded(PANEL,BORDER,28));

        TextView label=text(tr("رابط الخادم","Backend URL"),15,WHITE,true);
        backendCard.addView(label,new LinearLayout.LayoutParams(-1,dp(32)));
        TextView disclosure=text(tr(
            "عند تفعيل الخادم، يُرسل النص الذي تدخله فقط عند طلب التوليد إلى الخادم عبر اتصال HTTPS. راجع سياسة الخصوصية لمعرفة طريقة المعالجة.",
            "When a backend is enabled, the text you submit is sent to that server only when you request generation over HTTPS. See the Privacy Policy for data handling details."
        ),12,MUTED,false);
        disclosure.setPadding(0,0,0,dp(8));
        backendCard.addView(disclosure,new LinearLayout.LayoutParams(-1,dp(64)));

        EditText url=editor("https://...","https://...",1);
        url.setSingleLine(true);
        url.setText(s.get("backend_url",""));
        url.setGravity(ar()?Gravity.CENTER_VERTICAL|Gravity.RIGHT:Gravity.CENTER_VERTICAL|Gravity.LEFT);
        backendCard.addView(url,new LinearLayout.LayoutParams(-1,dp(56)));
        addGap(backendCard,10);

        TextView save=button(tr("حفظ","Save"),true);
        save.setOnClickListener(v->{s.put("backend_url",url.getText().toString().trim());toast(tr("تم الحفظ","Saved"));});
        backendCard.addView(save,new LinearLayout.LayoutParams(-1,dp(58)));
        addGap(backendCard,8);

        TextView test=button(tr("اختبار الاتصال","Test connection"),false);
        test.setOnClickListener(v->new RemotePromptClient().health(url.getText().toString().trim(),(ok,msg)->runOnUiThread(()->toast(ok?tr("الخادم يعمل ✅","Backend healthy ✅"):tr("فشل الاتصال","Connection failed")))));
        backendCard.addView(test,new LinearLayout.LayoutParams(-1,dp(58)));

        r.addView(backendCard,new LinearLayout.LayoutParams(-1,-2));
        addGap(r,12);

        TextView privacy=button(tr("سياسة الخصوصية","Privacy Policy"),false);
        privacy.setOnClickListener(v->open("https://github.com/khaldonshhab/PromptForgeAI/blob/main/PRIVACY.md"));
        r.addView(privacy,new LinearLayout.LayoutParams(-1,dp(58)));

        showRoot(scroll(r));
    }

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

    private class PromptForgeLogoView extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path brain=new Path();
        PromptForgeLogoView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float w=getWidth(), h=getHeight();
            float cx=w/2f, cy=h/2f, box=Math.min(w,h)*0.78f;
            RectF r=new RectF(cx-box/2f,cy-box/2f,cx+box/2f,cy+box/2f);
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0,0,w,h,BLUE,PURPLE,Shader.TileMode.CLAMP));
            c.drawRoundRect(r,dp(28),dp(28),p);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(235,8,15,35));
            c.drawRoundRect(new RectF(r.left+dp(4),r.top+dp(4),r.right-dp(4),r.bottom-dp(4)),dp(25),dp(25),p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(4));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(BLUE);
            brain.reset();
            brain.moveTo(cx,cy-dp(34)); brain.cubicTo(cx-dp(22),cy-dp(46),cx-dp(39),cy-dp(24),cx-dp(30),cy-dp(8));
            brain.cubicTo(cx-dp(43),cy+dp(2),cx-dp(32),cy+dp(28),cx-dp(15),cy+dp(28));
            brain.cubicTo(cx-dp(9),cy+dp(42),cx-dp(2),cy+dp(33),cx,cy+dp(27));
            c.drawPath(brain,p);
            p.setColor(PURPLE);
            brain.reset();
            brain.moveTo(cx,cy-dp(34)); brain.cubicTo(cx+dp(22),cy-dp(46),cx+dp(39),cy-dp(24),cx+dp(30),cy-dp(8));
            brain.cubicTo(cx+dp(43),cy+dp(2),cx+dp(32),cy+dp(28),cx+dp(15),cy+dp(28));
            brain.cubicTo(cx+dp(9),cy+dp(42),cx+dp(2),cy+dp(33),cx,cy+dp(27));
            c.drawPath(brain,p);

            p.setColor(WHITE); p.setStrokeWidth(dp(2));
            for(int i=0;i<3;i++){
                float y=cy-dp(16)+i*dp(16);
                c.drawLine(cx-dp(10),y,cx-dp(24),y,p);
                c.drawCircle(cx-dp(27),y,dp(3),p);
                c.drawLine(cx+dp(10),y,cx+dp(24),y,p);
                c.drawCircle(cx+dp(27),y,dp(3),p);
            }
        }
    }

    private class UKFlagView extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        UKFlagView(Context c){super(c);}
        @Override protected void onDraw(Canvas c){
            float w=getWidth(),h=getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(1,33,105)); c.drawRect(0,0,w,h,p);
            p.setStrokeCap(Paint.Cap.SQUARE);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1,dp(7)));
            p.setColor(Color.WHITE);
            c.drawLine(0,0,w,h,p); c.drawLine(w,0,0,h,p);
            p.setStrokeWidth(Math.max(1,dp(3)));
            p.setColor(Color.rgb(200,16,46));
            c.drawLine(0,0,w,h,p); c.drawLine(w,0,0,h,p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            c.drawRect(w*0.42f,0,w*0.58f,h,p);
            c.drawRect(0,h*0.35f,w,h*0.65f,p);
            p.setColor(Color.rgb(200,16,46));
            c.drawRect(w*0.46f,0,w*0.54f,h,p);
            c.drawRect(0,h*0.43f,w,h*0.57f,p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(100,255,255,255));
            c.drawRect(0,0,w,h,p);
        }
    }

    private class SyrianFlagView extends View{
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path star=new Path();
        SyrianFlagView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float w=getWidth(), h=getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(0,122,61)); c.drawRect(0,0,w,h/3f,p);
            p.setColor(Color.WHITE); c.drawRect(0,h/3f,w,2*h/3f,p);
            p.setColor(Color.BLACK); c.drawRect(0,2*h/3f,w,h,p);
            for(int i=0;i<3;i++) drawStar(c,w*(0.25f+0.25f*i),h*0.5f,Math.min(w,h)*0.18f);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(Color.argb(90,255,255,255));c.drawRect(0,0,w,h,p);
        }
        private void drawStar(Canvas c,float cx,float cy,float r){
            star.reset();
            for(int i=0;i<10;i++){
                double a=-Math.PI/2 + i*Math.PI/5;
                float rr=(i%2==0)?r:r*0.42f;
                float x=cx+(float)Math.cos(a)*rr, y=cy+(float)Math.sin(a)*rr;
                if(i==0)star.moveTo(x,y);else star.lineTo(x,y);
            }
            star.close();
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(206,17,38));c.drawPath(star,p);
        }
    }
}
