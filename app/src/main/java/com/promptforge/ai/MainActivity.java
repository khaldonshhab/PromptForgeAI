package com.promptforge.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PromptForge V2 clean UI.
 * All AI generation is remote; this screen deliberately has no translation/local-template fallback.
 */
public final class MainActivity extends Activity {
    private static final String BASE_URL = "https://promptforge-backend-2p4q.onrender.com";
    private static final int BG = Color.rgb(10, 12, 18);
    private static final int PANEL = Color.rgb(22, 26, 35);
    private static final int BORDER = Color.rgb(47, 55, 70);
    private static final int WHITE = Color.rgb(245, 247, 252);
    private static final int MUTED = Color.rgb(164, 174, 192);
    private static final int ACCENT = Color.rgb(93, 148, 255);
    private static final int GREEN = Color.rgb(86, 205, 157);

    private Storage storage;
    private RemotePromptClient remote;
    private LinearLayout root;
    private String token = "";
    private String screen = "login";
    private String language = "English";
    private final List<String> backStack = new ArrayList<>();
    private List<Platform> platforms = new ArrayList<>();
    private Spinner platformSpinner;
    private Spinner taskSpinner;
    private EditText ideaInput;
    private TextView generatedOutput;
    private EditText randomName;
    private Spinner randomType;
    private TextView randomOutput;
    private boolean busy = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        storage = new Storage(this);
        remote = new RemotePromptClient();
        language = "Arabic".equals(storage.lang()) ? "Arabic" : "English";
        token = storage.accountToken();
        platforms = PlatformRepository.all();
        // The login screen is always the first screen on launch, as requested.
        showLogin();
        remote.health(BASE_URL, (ok, value) -> { });
    }

    private boolean ar() { return "Arabic".equals(language); }
    private String tr(String arabic, String english) { return ar() ? arabic : english; }

    private void showLogin() {
        screen = "login";
        backStack.clear();
        root = newRoot();
        LinearLayout page = column(22);
        root.addView(page, matchWrap());
        LinearLayout top = row();
        TextView brand = text("PromptForge AI", 24, WHITE, true);
        top.addView(brand, new LinearLayout.LayoutParams(0, dp(48), 1));
        Spinner languageSpinner = new Spinner(this);
        ArrayAdapter<String> langs = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"English", "Arabic"});
        langs.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(langs);
        languageSpinner.setSelection(ar() ? 1 : 0);
        top.addView(languageSpinner, new LinearLayout.LayoutParams(dp(112), dp(48)));
        languageSpinner.setOnItemSelectedListener(new SimpleSelection(position -> {
            String next = position == 1 ? "Arabic" : "English";
            if (!next.equals(language)) {
                language = next;
                storage.lang(language);
                showLogin();
            }
        }));
        page.addView(top);
        addSpace(page, 56);
        TextView mark = text("✦", 46, ACCENT, true);
        mark.setGravity(Gravity.CENTER);
        page.addView(mark, matchHeight(62));
        TextView title = text(tr("أنشئ برومبتات احترافية","Create professional prompts"), 26, WHITE, true);
        title.setGravity(Gravity.CENTER);
        page.addView(title, matchHeight(58));
        TextView sub = text(tr("سجّل الدخول للمتابعة","Sign in to continue"), 15, MUTED, false);
        sub.setGravity(Gravity.CENTER);
        page.addView(sub, matchHeight(36));
        addSpace(page, 24);
        EditText username = input(tr("اسم المستخدم","Username"));
        EditText password = input(tr("كلمة المرور","Password"));
        password.setInputType(0x00000081);
        page.addView(username, matchHeight(56));
        addSpace(page, 12);
        page.addView(password, matchHeight(56));
        addSpace(page, 18);
        Button signIn = button(tr("تسجيل الدخول","Sign in"), true);
        page.addView(signIn, matchHeight(58));
        signIn.setOnClickListener(v -> {
            String u = username.getText().toString().trim();
            String p = password.getText().toString();
            if (u.length() < 3 || p.isEmpty()) {
                toast(tr("أدخل اسم المستخدم وكلمة المرور","Enter your username and password"));
                return;
            }
            signIn.setEnabled(false);
            signIn.setText(tr("جارٍ التحقق...","Signing in..."));
            remote.login(BASE_URL, u, p, storage.deviceId(), (ok, value) -> runOnUiThread(() -> {
                signIn.setEnabled(true);
                signIn.setText(tr("تسجيل الدخول","Sign in"));
                if (!ok) {
                    toast(loginError(value));
                    return;
                }
                try {
                    JSONObject response = new JSONObject(value);
                    token = response.optString("token", "");
                    boolean admin = response.optBoolean("admin", false);
                    if (token.isEmpty()) {
                        toast(tr("استجابة تسجيل الدخول غير صالحة","Invalid login response"));
                        return;
                    }
                    storage.account(u, token, admin);
                    if (admin) openAdmin();
                    else showHome();
                } catch (Exception e) {
                    toast(tr("تعذر قراءة استجابة تسجيل الدخول","Could not read login response"));
                }
            }));
        });
        addSpace(page, 18);
        TextView note = text(tr("لا يوجد اشتراك مدفوع أو إعلانات داخل التطبيق.","No paid subscription or ads in the app."), 12, MUTED, false);
        note.setGravity(Gravity.CENTER);
        page.addView(note, matchHeight(40));
        showRoot();
    }

    private String loginError(String raw) {
        if (raw == null) return tr("تعذر الاتصال بالسيرفر","Could not connect to server");
        if (raw.contains("http_401")) return tr("اسم المستخدم أو كلمة المرور غير صحيحة","Incorrect username or password");
        if (raw.contains("http_409")) return tr("هذا الحساب مرتبط بجهاز آخر","This account is bound to another device");
        if (raw.contains("http_503")) return tr("إعدادات تسجيل الدخول غير مكتملة على السيرفر","Server authentication is not configured");
        return tr("فشل تسجيل الدخول. تحقق من الإنترنت وحاول مجدداً.","Sign-in failed. Check your connection and try again.");
    }

    private void showHome() {
        screen = "home";
        root = newRoot();
        LinearLayout page = column(18);
        root.addView(page, matchWrap());
        addHeader(page, tr("مساحة العمل","Workspace"));
        TextView greeting = text(tr("أهلاً، ","Welcome, ") + storage.accountUser(), 18, WHITE, true);
        page.addView(greeting, matchHeight(42));
        TextView description = text(tr("حوّل فكرتك إلى برومبت واضح ومخصص للأداة التي تختارها.","Turn your idea into a clear prompt tailored to your selected AI tool."), 14, MUTED, false);
        page.addView(description, matchWrap());
        addSpace(page, 14);
        addActionCard(page, tr("إنشاء برومبت","Create a prompt"), tr("اكتب فكرتك واختر الأداة المناسبة","Describe your idea and choose a target tool"), "✦", () -> showGenerate());
        addActionCard(page, tr("برومبت عشوائي","Random prompt"), tr("صورة أو قصيدة أو نكتة أو أغنية مع اسم تختاره","Image, poem, joke, or song with a name"), "✧", () -> showRandom());
        addActionCard(page, tr("سجل البرومبتات","Prompt history"), tr("راجع آخر النتائج التي أنشأتها على هذا الجهاز","Review recent results saved on this device"), "◷", () -> showHistory());
        addSpace(page, 14);
        if (storage.isAdmin()) {
            Button admin = button(tr("لوحة تحكم الأدمن","Admin dashboard"), false);
            page.addView(admin, matchHeight(54));
            admin.setOnClickListener(v -> openAdmin());
            addSpace(page, 10);
        }
        Button logout = button(tr("تسجيل الخروج","Sign out"), false);
        page.addView(logout, matchHeight(52));
        logout.setOnClickListener(v -> {
            storage.logout();
            token = "";
            showLogin();
        });
        showRoot();
    }

    private void showGenerate() {
        push("generate");
        screen = "generate";
        root = newRoot();
        LinearLayout page = column(18);
        root.addView(page, matchWrap());
        addHeader(page, tr("إنشاء برومبت","Create prompt"));
        TextView toolLabel = text(tr("الأداة المستهدفة","Target tool"), 14, MUTED, true);
        page.addView(toolLabel, matchHeight(30));
        platformSpinner = new Spinner(this);
        List<String> names = new ArrayList<>();
        for (Platform p : platforms) names.add(p.name);
        ArrayAdapter<String> platformAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        platformAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        platformSpinner.setAdapter(platformAdapter);
        page.addView(platformSpinner, matchHeight(52));
        addSpace(page, 12);
        page.addView(text(tr("نوع المهمة","Task type"), 14, MUTED, true), matchHeight(30));
        taskSpinner = new Spinner(this);
        String[] tasks = {"General", "Image prompt", "Video prompt", "Voice / TTS", "Music", "Coding", "Research", "Education", "Marketing", "Business", "Writing"};
        ArrayAdapter<String> taskAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tasks);
        taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskSpinner.setAdapter(taskAdapter);
        page.addView(taskSpinner, matchHeight(52));
        addSpace(page, 12);
        page.addView(text(tr("اشرح فكرتك بلغتك الطبيعية","Describe your idea in your own language"), 14, MUTED, true), matchHeight(30));
        ideaInput = input(tr("مثال: صورة سينمائية لمسرح مهجور بعد نهاية العالم...","Example: a cinematic image of an abandoned theater after the apocalypse..."));
        ideaInput.setGravity(Gravity.TOP | Gravity.START);
        ideaInput.setSingleLine(false);
        ideaInput.setMinLines(5);
        ideaInput.setPadding(dp(14), dp(14), dp(14), dp(14));
        page.addView(ideaInput, matchHeight(142));
        addSpace(page, 16);
        Button generate = button(tr("✦  أنشئ البرومبت","✦  Generate prompt"), true);
        page.addView(generate, matchHeight(58));
        generate.setOnClickListener(v -> generatePrompt(generate));
        addSpace(page, 18);
        generatedOutput = text("", 15, WHITE, false);
        generatedOutput.setTextIsSelectable(true);
        generatedOutput.setPadding(dp(14), dp(14), dp(14), dp(14));
        generatedOutput.setBackground(panelBackground());
        page.addView(generatedOutput, matchWrap());
        Button copy = button(tr("نسخ النتيجة","Copy result"), false);
        page.addView(copy, matchHeight(50));
        copy.setOnClickListener(v -> {
            String result = generatedOutput.getText().toString().trim();
            if (result.isEmpty()) toast(tr("لا توجد نتيجة لنسخها","There is no result to copy"));
            else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("PromptForge prompt", result));
                toast(tr("تم نسخ البرومبت","Prompt copied"));
            }
        });
        showRoot();
    }

    private void generatePrompt(Button generate) {
        String idea = ideaInput.getText().toString().trim();
        if (idea.length() < 3) {
            toast(tr("اكتب فكرتك أولاً","Enter your idea first"));
            return;
        }
        if (busy) return;
        busy = true;
        generate.setEnabled(false);
        generate.setText(tr("جارٍ إنشاء برومبت احترافي...","Generating professional prompt..."));
        generatedOutput.setText(tr("يتم تحليل الفكرة وإنشاء النتيجة...","Analyzing the idea and generating the result..."));
        String platform = String.valueOf(platformSpinner.getSelectedItem());
        String task = String.valueOf(taskSpinner.getSelectedItem());
        remote.generate(BASE_URL, idea, platform, task, "English", token, (ok, value) -> runOnUiThread(() -> {
            busy = false;
            generate.setEnabled(true);
            generate.setText(tr("✦  أنشئ البرومبت","✦  Generate prompt"));
            if (!ok) {
                generatedOutput.setText("");
                if (value != null && value.contains("http_502")) {
                    toast(tr("المحرك رفض نتيجة ضعيفة. جرّب مرة أخرى بعد قليل.","The engine rejected a weak result. Please retry shortly."));
                } else if (value != null && value.contains("http_401")) {
                    toast(tr("انتهت الجلسة. سجّل الدخول مجدداً.","Session expired. Please sign in again."));
                    storage.logout();
                    token = "";
                    showLogin();
                } else if (value != null && value.contains("http_429")) {
                    toast(tr("وصلت إلى حد الطلبات المؤقت. انتظر قليلاً.","Temporary request limit reached. Please wait."));
                } else {
                    toast(tr("تعذر إنشاء البرومبت. تحقق من الاتصال وحاول مجدداً.","Prompt generation failed. Check your connection and retry."));
                }
                return;
            }
            String result = value == null ? "" : value.trim();
            if (result.length() < 20) {
                toast(tr("وصلت نتيجة غير مكتملة؛ لم يتم حفظها.","An incomplete result was received and was not saved."));
                generatedOutput.setText("");
                return;
            }
            generatedOutput.setText(result);
            storage.add("history", result);
            storage.add("history_meta", platform + " • " + task);
            toast(tr("تم إنشاء البرومبت","Prompt generated"));
        }));
    }

    private void showRandom() {
        push("random");
        screen = "random";
        root = newRoot();
        LinearLayout page = column(18);
        root.addView(page, matchWrap());
        addHeader(page, tr("برومبت عشوائي","Random prompt"));
        page.addView(text(tr("اختر نوعاً وأضف اسماً إن أردت تخصيص النتيجة.","Choose a type and optionally personalize it with a name."), 14, MUTED, false), matchWrap());
        randomType = new Spinner(this);
        String[] types = {tr("صورة","Image"), tr("قصيدة","Poem"), tr("نكتة","Joke"), tr("أغنية","Song")};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        randomType.setAdapter(typeAdapter);
        page.addView(randomType, matchHeight(54));
        addSpace(page, 12);
        randomName = input(tr("اسمك أو اسم أي شخص (اختياري)","Your name or anyone's name (optional)"));
        page.addView(randomName, matchHeight(54));
        addSpace(page, 14);
        Button make = button(tr("✧  توليد برومبت عشوائي","✧  Generate random prompt"), true);
        page.addView(make, matchHeight(58));
        randomOutput = text("", 15, WHITE, false);
        randomOutput.setTextIsSelectable(true);
        randomOutput.setPadding(dp(14), dp(14), dp(14), dp(14));
        randomOutput.setBackground(panelBackground());
        page.addView(randomOutput, matchWrap());
        addSpace(page, 10);
        Button copy = button(tr("نسخ","Copy"), false);
        page.addView(copy, matchHeight(48));
        make.setOnClickListener(v -> randomOutput.setText(randomPromptText(randomType.getSelectedItemPosition(), randomName.getText().toString().trim())));
        copy.setOnClickListener(v -> {
            String result = randomOutput.getText().toString().trim();
            if (result.isEmpty()) return;
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("PromptForge random prompt", result));
            toast(tr("تم النسخ","Copied"));
        });
        showRoot();
    }

    private String randomPromptText(int type, String name) {
        String who = name.isEmpty() ? "a fictional character" : name;
        String[] prompts;
        if (type == 0) {
            prompts = new String[]{
                "Create a cinematic, photorealistic portrait of " + who + " in a rain-soaked city at midnight. Use dramatic rim lighting, rich reflections, realistic skin and fabric textures, a carefully composed frame, and a mysterious atmosphere.",
                "Create an imaginative editorial image featuring " + who + " in a surreal world where floating theater stages drift among the clouds. Use refined composition, atmospheric depth, elegant color contrast, and high visual detail.",
                "Create a striking fantasy image of " + who + " standing before an ancient monumental doorway in a forgotten desert city at golden hour. Emphasize scale, tactile materials, cinematic light, and a sense of discovery."
            };
        } else if (type == 1) {
            prompts = new String[]{
                "Write an original English poem dedicated to " + who + " about hope surviving in a broken world. Use vivid imagery, musical language, a clear emotional arc, and an ending that feels earned rather than sentimental.",
                "Write a lyrical free-verse poem about " + who + " standing alone in an abandoned theater after the final curtain. Explore memory, silence, and the persistence of art through precise imagery and restrained emotion.",
                "Write an original poem for " + who + " about a journey from fear to courage. Use fresh metaphors, natural rhythm, and a memorable final line. Avoid clichés."
            };
        } else if (type == 2) {
            prompts = new String[]{
                "Write a clever, friendly, original English joke featuring " + who + ". Keep it concise, easy to understand, and based on a surprising but harmless punchline.",
                "Create a short comic dialogue in English between " + who + " and an overly dramatic robot. Build toward an unexpected punchline without insulting or humiliating anyone.",
                "Write three short, witty English one-liners about " + who + " trying to solve an absurd everyday problem. Keep the humor playful and suitable for a general audience."
            };
        } else {
            prompts = new String[]{
                "Write original English song lyrics dedicated to " + who + " in an uplifting cinematic pop style. Include a memorable chorus, two concise verses, a bridge, and a hopeful final chorus. Do not imitate any existing artist.",
                "Create an original English acoustic ballad for " + who + " about friendship across distance. Use intimate imagery, a singable chorus, natural phrasing, and a gentle emotional build. Avoid clichés.",
                "Write an original theatrical anthem for " + who + " about finding light after darkness. Include verses, a powerful chorus, and a final refrain suitable for a live stage performance. Do not imitate copyrighted lyrics."
            };
        }
        return prompts[new Random().nextInt(prompts.length)];
    }

    private void showHistory() {
        push("history");
        screen = "history";
        root = newRoot();
        LinearLayout page = column(18);
        root.addView(page, matchWrap());
        addHeader(page, tr("سجل البرومبتات","Prompt history"));
        List<String> history = storage.list("history");
        if (history.isEmpty()) {
            TextView empty = text(tr("لا يوجد سجل بعد. أنشئ أول برومبت ليظهر هنا.","No history yet. Create your first prompt to see it here."), 14, MUTED, false);
            page.addView(empty, matchWrap());
        } else {
            for (int i = 0; i < history.size(); i++) {
                final String item = history.get(i);
                LinearLayout card = column(14);
                card.setBackground(panelBackground());
                TextView body = text(item, 14, WHITE, false);
                body.setMaxLines(7);
                card.addView(body, matchWrap());
                Button copy = button(tr("نسخ","Copy"), false);
                card.addView(copy, matchHeight(44));
                copy.setOnClickListener(v -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("PromptForge prompt", item));
                    toast(tr("تم النسخ","Copied"));
                });
                page.addView(card, matchWrap());
                addSpace(page, 10);
            }
            Button clear = button(tr("مسح السجل المحلي","Clear local history"), false);
            page.addView(clear, matchHeight(50));
            clear.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle(tr("مسح السجل","Clear history"))
                    .setMessage(tr("سيتم حذف سجل البرومبتات من هذا الجهاز فقط.","This removes prompt history from this device only."))
                    .setNegativeButton(tr("إلغاء","Cancel"), null)
                    .setPositiveButton(tr("مسح","Clear"), (d, w) -> {
                        storage.put("history", "[]");
                        showHistory();
                    }).show());
        }
        showRoot();
    }

    private void addHeader(LinearLayout page, String title) {
        LinearLayout header = row();
        Button back = button("‹", false);
        header.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        back.setOnClickListener(v -> goBack());
        TextView heading = text(title, 23, WHITE, true);
        heading.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        header.addView(heading, new LinearLayout.LayoutParams(0, dp(48), 1));
        TextView languageButton = text(ar() ? "ع" : "EN", 14, ACCENT, true);
        languageButton.setGravity(Gravity.CENTER);
        header.addView(languageButton, new LinearLayout.LayoutParams(dp(42), dp(48)));
        languageButton.setOnClickListener(v -> {
            language = ar() ? "English" : "Arabic";
            storage.lang(language);
            renderCurrent();
        });
        page.addView(header);
        addSpace(page, 18);
    }

    private void addActionCard(LinearLayout page, String title, String subtitle, String icon, Runnable action) {
        LinearLayout card = column(16);
        card.setBackground(panelBackground());
        LinearLayout line = row();
        TextView glyph = text(icon, 26, ACCENT, true);
        glyph.setGravity(Gravity.CENTER);
        line.addView(glyph, new LinearLayout.LayoutParams(dp(46), dp(48)));
        LinearLayout texts = column(2);
        texts.addView(text(title, 17, WHITE, true), matchWrap());
        texts.addView(text(subtitle, 13, MUTED, false), matchWrap());
        line.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(line, matchWrap());
        page.addView(card, matchWrap());
        card.setOnClickListener(v -> action.run());
        addSpace(page, 10);
    }

    private void openAdmin() {
        if (token == null || token.isEmpty()) {
            toast(tr("جلسة الأدمن غير صالحة. سجّل الدخول مجدداً.","Admin session is invalid. Sign in again."));
            showLogin();
            return;
        }
        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra("admin_token", token);
        startActivity(intent);
    }

    private void push(String next) { if (screen != null && !screen.equals(next)) backStack.add(screen); }
    private void goBack() {
        if (backStack.isEmpty()) { showHome(); return; }
        String previous = backStack.remove(backStack.size() - 1);
        if ("home".equals(previous)) showHome();
        else if ("generate".equals(previous)) showGenerateWithoutPush();
        else if ("random".equals(previous)) showRandomWithoutPush();
        else if ("history".equals(previous)) showHistoryWithoutPush();
        else showHome();
    }
    private void renderCurrent() {
        if ("login".equals(screen)) showLogin();
        else if ("generate".equals(screen)) showGenerateWithoutPush();
        else if ("random".equals(screen)) showRandomWithoutPush();
        else if ("history".equals(screen)) showHistoryWithoutPush();
        else showHome();
    }
    private void showGenerateWithoutPush() { if (!backStack.isEmpty()) backStack.remove(backStack.size()-1); showGenerate(); }
    private void showRandomWithoutPush() { if (!backStack.isEmpty()) backStack.remove(backStack.size()-1); showRandom(); }
    private void showHistoryWithoutPush() { if (!backStack.isEmpty()) backStack.remove(backStack.size()-1); showHistory(); }

    @Override public void onBackPressed() {
        if ("login".equals(screen)) super.onBackPressed();
        else goBack();
    }

    private LinearLayout newRoot() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout container = column(0);
        container.setPadding(dp(18), dp(10), dp(18), dp(26));
        scroll.addView(container, new ScrollView.LayoutParams(-1, -2));
        root = container;
        setContentView(scroll);
        return container;
    }
    private void showRoot() { if (root != null) root.setLayoutDirection(ar() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR); }
    private LinearLayout column(int pad) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(pad), dp(pad), dp(pad), dp(pad));
        return l;
    }
    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }
    private TextView text(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT, bold ? Typeface.BOLD : Typeface.NORMAL);
        t.setGravity(ar() ? Gravity.RIGHT : Gravity.LEFT);
        return t;
    }
    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(WHITE);
        e.setHintTextColor(MUTED);
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setPadding(dp(14), dp(8), dp(14), dp(8));
        e.setBackground(panelBackground());
        e.setGravity((ar() ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        return e;
    }
    private Button button(String label, boolean primary) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(WHITE);
        b.setAllCaps(false);
        b.setBackground(roundBackground(primary ? ACCENT : PANEL, BORDER, 16));
        return b;
    }
    private GradientDrawable panelBackground() { return roundBackground(PANEL, BORDER, 16); }
    private GradientDrawable roundBackground(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), stroke);
        return d;
    }
    private LinearLayout.LayoutParams matchHeight(int height) { return new LinearLayout.LayoutParams(-1, dp(height)); }
    private LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(-1, -2); }
    private void addSpace(LinearLayout parent, int height) {
        View v = new View(this);
        parent.addView(v, new LinearLayout.LayoutParams(1, dp(height)));
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private void toast(String value) { Toast.makeText(this, value, Toast.LENGTH_LONG).show(); }

    private interface SelectionCallback { void selected(int position); }
    private final class SimpleSelection implements android.widget.AdapterView.OnItemSelectedListener {
        private final SelectionCallback callback;
        private boolean first = true;
        SimpleSelection(SelectionCallback callback) { this.callback = callback; }
        @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            if (first) { first = false; return; }
            callback.selected(position);
        }
        @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
    }
}
