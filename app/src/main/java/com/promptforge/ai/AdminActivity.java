package com.promptforge.ai;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;\nimport android.net.Uri;

public class AdminActivity extends Activity {
    private static final String ADMIN_URL = "https://promptforge-backend-2p4q.onrender.com/admin";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView w = new WebView(this);
        w.setWebViewClient(new WebViewClient());
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        String token=getIntent().getStringExtra("admin_token");\n        String url=ADMIN_URL+(token==null||token.isEmpty()?"":"?token="+Uri.encode(token));\n        w.loadUrl(url);
        setContentView(w);
    }
}