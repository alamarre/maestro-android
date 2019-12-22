package ca.omny.videos.maestro;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
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
    public boolean onKeyDown(int keyCode, KeyEvent event){
        boolean handled = false;

        switch (keyCode){
            case KeyEvent.KEYCODE_MENU:
                // ... handle left action
                mWebview.evaluateJavascript("var event = document.createEvent('Event');" +
                                "event.initEvent('keydown', true, true);" +
                                "event.keyCode = 77;" +
                                "document.dispatchEvent(event);", null);
                handled = true;
                break;
            case KeyEvent.KEYCODE_BACK:
                // ... handle right action
                if(mWebview.canGoBack()) {
                    mWebview.goBack();

                    handled = true;
                }
                break;
        }
        return handled || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mWebview = findViewById(R.id.webview1);
        if(mWebview != null) {

            final Activity activity = this;

            mWebview.setWebViewClient(new WebViewClient() {
                /*@SuppressWarnings("deprecation")
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    //Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
                }*/
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
        if(! BuildConfig.MAESTRO_URL.equals(mWebview.getUrl())) {
            mWebview.loadUrl(BuildConfig.MAESTRO_URL);
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
