package com.promptforge.ai;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;

public class AdminActivity extends Activity {
    private static final String DEFAULT_BACKEND_URL = "https://promptforge-backend-2p4q.onrender.com";
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
        String trimmed=configured == null ? "" : configured.trim();
        String base=trimmed.isEmpty() || trimmed.equals("https://promptforgeai-backend.onrender.com") ? DEFAULT_BACKEND_URL : trimmed;
        String url=base.replaceAll("/$", "") + "/admin" + (token==null||token.isEmpty()?"":"?token=" + Uri.encode(token));
        w.loadUrl(url);
        setContentView(w);
    }
}
