package com.promptforge.ai;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;

public class AdminActivity extends Activity {
    private static final String DEFAULT_BACKEND_URL = "https://promptforgeai-backend.onrender.com";
    private static final String DEFAULT_ADMIN_URL = DEFAULT_BACKEND_URL + "/admin";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView w = new WebView(this);
        w.setWebViewClient(new WebViewClient());
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        String token=getIntent().getStringExtra("admin_token");
        String configured=getSharedPreferences("pf", MODE_PRIVATE).getString("backend_url", DEFAULT_BACKEND_URL);
        String base=configured == null || configured.trim().isEmpty() ? DEFAULT_BACKEND_URL : configured.trim();
        String url=base.replaceAll("/$", "") + "/admin" + (token==null||token.isEmpty()?"":"?token=" + Uri.encode(token));
        w.loadUrl(url);
        setContentView(w);
    }
}
