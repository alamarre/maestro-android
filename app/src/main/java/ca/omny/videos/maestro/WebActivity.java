package ca.omny.videos.maestro;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ca.omny.videos.maestro.models.PlayResult;
import ca.omny.videos.maestro.models.PlayVideo;

public class WebActivity extends Activity {

    private WebView mWebview;
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 7000;
    private final int MY_PERMISSIONS_POWER = 7001;
    private final int MY_INSTALL_ID = 9001;
    private Overlay mOverlay;
    private Gson gson = new Gson();

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;

        switch (keyCode) {
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
                if (mWebview.canGoBack()) {
                    mWebview.goBack();

                    handled = true;
                }
                break;
        }
        return handled || super.onKeyDown(keyCode, event);
    }

    private void setupOverlay() {
        WindowManager windowManager = (WindowManager) getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        mOverlay = new Overlay(getBaseContext());
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.FIRST_SUB_WINDOW);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        layoutParams.width = metrics.widthPixels;
        layoutParams.height = metrics.heightPixels;

        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags =
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        //layoutParams.token = getWindow().getDecorView().getRootView().getWindowToken();

        //Feel free to inflate here

        //mTestView.setBackgroundColor(Color.RED);

        //Must wire up back button, otherwise it's not sent to our activity
        mOverlay.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    onBackPressed();
                }
                return true;
            }
        });
        windowManager.addView(mOverlay, layoutParams);
    }

    public void askPermission(Activity context){
        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(context);
        } else {
            permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (permission) {
            //do your code
            Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 1);
        }  else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivityForResult(intent, 1000);
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_SETTINGS}, 1000);
            }
        }
    }

    // from https://stackoverflow.com/questions/18752202/check-if-application-is-installed-android (Robin Kanters)
    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isVlcInstalled() {
        return isPackageInstalled("org.videolan.vlc", this.getPackageManager());
    }

    @JavascriptInterface
    public void showVideo(String json) {
        PlayVideo videoToPlay = gson.fromJson(json, PlayVideo.class);
        new StartVlcVideo(this, videoToPlay).execute();
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

        //askPermission(this);
        //Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 1);
        setContentView(R.layout.activity_web);
        mOverlay = new Overlay(this);

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
        if(isVlcInstalled()) {
            mWebview.addJavascriptInterface(this, "MaestroNative");
        }
        if(! BuildConfig.MAESTRO_URL.equals(mWebview.getUrl())) {
            mWebview.loadUrl(BuildConfig.MAESTRO_URL);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WAKE_LOCK},
                    MY_PERMISSIONS_POWER);

        } else {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"maestro:lock");
            lock.acquire();
        }

        //setContentView(mWebview );
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWebview.loadUrl(BuildConfig.MAESTRO_URL);
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
            case MY_PERMISSIONS_POWER: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock lock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,"maestro:lock");
                    lock.acquire();
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
        } else if (requestCode == 42) {
            try {
                if(data == null) {
                    return;
                }
                long position = (long) data.getExtras().get("extra_position");
                long duration = (long) data.getExtras().get("extra_duration");
                System.out.println(position);
                System.out.println(duration);
                PlayResult playResult = new PlayResult();
                playResult.duration = duration;
                playResult.progress = position;
                String jsonPlayResult = gson.toJson(playResult);
                mWebview.evaluateJavascript(
                        "var event = new CustomEvent('stopped-playing', {detail: " + jsonPlayResult + "} );" +
                        "document.dispatchEvent(event);", null);
            } catch(Exception e) {
                e.printStackTrace();
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
