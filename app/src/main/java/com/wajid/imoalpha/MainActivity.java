package com.wajid.imoalpha;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String IMO_PACKAGE = "com.imo.android.imoim";
    private WebView webView;

    private final class AlphaBridge {
        @JavascriptInterface
        public void openImo() {
            runOnUiThread(() -> {
                try {
                    Intent launch = getPackageManager().getLaunchIntentForPackage(IMO_PACKAGE);
                    if (launch != null) startActivity(launch);
                    else startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + IMO_PACKAGE)));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "IMO খোলা যায়নি।", Toast.LENGTH_LONG).show();
                }
            });
        }

        @JavascriptInterface
        public void openImoSettings() {
            runOnUiThread(() -> {
                try {
                    Intent settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + IMO_PACKAGE));
                    startActivity(settings);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "IMO Settings খোলা যায়নি।", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(false);
        webView.addJavascriptInterface(new AlphaBridge(), "AlphaAndroid");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme();
                if (scheme.equals("file")) return false;
                if (scheme.equals("https") || scheme.equals("http")) return true;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                catch (Exception e) {
                    Toast.makeText(MainActivity.this, "এই লিংক খোলা যায়নি।", Toast.LENGTH_SHORT).show();
                }
                return true;
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