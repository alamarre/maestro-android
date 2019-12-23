package ca.omny.videos.maestro;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class UpdateCheck extends AsyncTask<Void, Void, Intent> {

    private static final String MAESTRO_UPDATE_URL = "https://alamarre.s3.amazonaws.com/fire-tv-maestro.apk";
    private static final String MAESTRO_VERSION_HEADER = "x-amz-meta-maestro-version";
    private static final String ACTION_INSTALL_COMPLETE = "ca.omny.videos.maestro.INSTALL_COMPLETE";
    private Activity activity;
    int id;


    public UpdateCheck(Activity activity, int id) {
        this.activity = activity;
        this.id = id;
    }

    @Override
    protected Intent doInBackground(Void... voids) {
        try {
            String versionName = BuildConfig.VERSION_NAME;

            URL url = new URL(MAESTRO_UPDATE_URL);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("HEAD");
            c.connect();
            Map<String, List<String>> map = c.getHeaderFields();
            String newVersion = map.get(MAESTRO_VERSION_HEADER).get(0);
            if(newVersion.equals(versionName)) {
                return null;
            }

            Intent myIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE, Uri.parse(MAESTRO_UPDATE_URL));
            if(myIntent != null) {
                //return myIntent;
            }
            url = new URL(MAESTRO_UPDATE_URL);
            c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.connect();

            /*File outputDir = activity.getCacheDir(); // context being the Activity pointer
            File outputFile = File.createTempFile("prefix", "apk", outputDir);*/

            String PATH = Environment.getExternalStorageDirectory() + "/download/";
            File file = new File(PATH);
            file.mkdirs();
            File outputFile = new File(file, "app.apk");
            //activity.openFile
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            //installPackage(activity, is, BuildConfig.APPLICATION_ID);
            //return null;

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();//till here, it works fine - .apk is download to my sdcard in download file
            //Uri.fromFile(outputFile)
            Uri uri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", outputFile);
            //uri = Uri.fromFile(outputFile);
            Intent promptInstall = new Intent(Intent.ACTION_INSTALL_PACKAGE)
                    .setDataAndType(uri, "application/vnd.android.package-archive");
            //promptInstall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            promptInstall.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //installation is not working

            return promptInstall;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean installPackage(Context context, InputStream in, String packageName)
            throws IOException {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);
        // set params
        int sessionId = packageInstaller.createSession(params);
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);
        OutputStream out = session.openWrite("COSU", 0, -1);
        byte[] buffer = new byte[65536];
        int c;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
        session.fsync(out);
        in.close();
        out.close();

        session.commit(createIntentSender(context, sessionId));
        return true;
    }



    private static IntentSender createIntentSender(Context context, int sessionId) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(ACTION_INSTALL_COMPLETE),
                0);
        return pendingIntent.getIntentSender();
    }

    @Override
    protected void onPostExecute(Intent promptInstall) {
        if(promptInstall != null) {
            try {
                //promptInstall.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                promptInstall.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                //activity.startActivity(promptInstall);
                activity.startActivityForResult(promptInstall, this.id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
