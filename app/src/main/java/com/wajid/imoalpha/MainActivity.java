package com.wajid.imoalpha;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView webView;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String raw = uri.toString();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme();
                if (raw.startsWith("intent://")) {
                    try {
                        Intent intent = Intent.parseUri(raw, Intent.URI_INTENT_SCHEME);
                        if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
                        else Toast.makeText(MainActivity.this, "IMO অ্যাপ ইনস্টল আছে কি না দেখো।", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "IMO খোলা যায়নি।", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                if (!scheme.equals("file") && !scheme.equals("https") && !scheme.equals("http")) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                    catch (Exception e) { Toast.makeText(MainActivity.this, "এই লিংক খোলা যায়নি", Toast.LENGTH_SHORT).show(); }
                    return true;
                }
                return false;
            }
        });
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
