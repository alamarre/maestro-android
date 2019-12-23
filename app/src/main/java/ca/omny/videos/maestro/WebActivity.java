package ca.omny.videos.maestro;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class WebActivity extends Activity {

    private WebView mWebview;
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 7000;
    private final int MY_INSTALL_ID = 9001;

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

                    //handled = true;
                }
                break;
        }
        return handled || super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        } else {
            new UpdateCheck(this, MY_INSTALL_ID).execute();
        }

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
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new UpdateCheck(this, MY_INSTALL_ID).execute();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == MY_INSTALL_ID) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                System.out.println("excellent");
            }
        }
    }

            @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWebview != null) {
            mWebview.destroy();
        }
    }
}
