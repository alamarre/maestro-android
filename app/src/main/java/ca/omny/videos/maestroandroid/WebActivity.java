package ca.omny.videos.maestroandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebActivity extends Activity {

    private WebView mWebview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mWebview = findViewById(R.id.webview1);
        if(mWebview != null) {

            final Activity activity = this;

            mWebview.setWebViewClient(new WebViewClient() {
                @SuppressWarnings("deprecation")
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onPageFinished(WebView view, String url) {
                    mWebview.evaluateJavascript("if(window.location.hash == '#/login') {" +
                            "function setCookie(cname, cvalue, exdays) { " +
                            "var d = new Date(); d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));" +
                            "var expires = \"expires=\" + d.toGMTString();" +
                            "document.cookie = cname + \"=\" + cvalue + \"; \" + expires;" +
                            "}" +
                            "" +
                            "setCookie('access_token', '" + BuildConfig.MAESTRO_TOKEN + "', 365);" +
                            "setCookie('user_profile', '" + BuildConfig.MAESTRO_PROFILE+ "', 365);" +
                            "setCookie('myClientName', '" + BuildConfig.MAESTRO_DEVICE_NAME +"', 365);" +
                            "setCookie('remoteControl', 'true', 365);" +
                            "window.location='/';" +
                            "} else if (window.location.hash.indexOf('#/view?') == 0) {" +
                            //"document.getElementsByTagName(\"video\")[0].play();" +
                            "}" , null);
                }
                @TargetApi(android.os.Build.VERSION_CODES.M)
                @Override
                public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                    // Redirect to deprecated method, so you can use it in all SDK versions
                    onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
                }
            });
        }
        mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript


        mWebview.setWebContentsDebuggingEnabled(true);
        mWebview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        mWebview.setFocusable(true);
        mWebview.setFocusableInTouchMode(true);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setBlockNetworkImage(false);
        mWebview.getSettings().setAllowContentAccess(true);
        mWebview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebview.getSettings().setDomStorageEnabled(true);
        mWebview.getSettings().setDatabaseEnabled(true);
        mWebview.getSettings().setAppCacheEnabled(true);
        mWebview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        if(! "http://192.168.86.152:8081".equals(mWebview.getUrl())) {
            mWebview.loadUrl("http://192.168.86.152:8081");
        }


        //setContentView(mWebview );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWebview != null) {
            mWebview.destroy();
        }
    }
}
