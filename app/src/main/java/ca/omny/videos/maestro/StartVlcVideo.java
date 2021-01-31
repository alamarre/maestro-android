package ca.omny.videos.maestro;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ca.omny.videos.maestro.models.PlayVideo;

public class StartVlcVideo extends AsyncTask<Void, Void, Void> {
    private Activity activity;
    private PlayVideo videoToPlay;

    public StartVlcVideo(Activity activity, PlayVideo videoToPlay) {
        this.activity = activity;
        this.videoToPlay = videoToPlay;
    }

    private String DownloadFileAndGetUri(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.connect();

            /*File outputDir = activity.getCacheDir(); // context being the Activity pointer
            File outputFile = File.createTempFile("prefix", "apk", outputDir);*/

        String PATH = Environment.getExternalStorageDirectory() + "/download/";
        File file = new File(PATH);
        file.mkdirs();
        File outputFile = new File(file, "subtitle-temp.vtt");
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
        return outputFile.getAbsolutePath();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        int vlcRequestCode = 42;

        Uri uri = Uri.parse(videoToPlay.getSources()[0]);
        Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
        vlcIntent.setPackage("org.videolan.vlc");
        vlcIntent.setDataAndTypeAndNormalize(uri, "video/*");
        vlcIntent.putExtra("title", videoToPlay.getTitle());
        vlcIntent.putExtra("disable_hardware", true);
        if(videoToPlay.getStartTime() <= 0) {
            vlcIntent.putExtra("from_start", true);
        } else {
            vlcIntent.putExtra("position", videoToPlay.getStartTime() * 1000);
        }
        if(videoToPlay.getSubtitleSources() != null && videoToPlay.getSubtitleSources().length > 0) {
            try {
                String subtitleuri = DownloadFileAndGetUri(videoToPlay.getSubtitleSources()[0]);
                vlcIntent.putExtra("subtitles_location", subtitleuri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // vlcIntent.setComponent(new ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.tv.audioplayer.AudioPlayerActivity"));
        //vlcIntent.setComponent(new ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.video.VideoPlayerActivity"));
        activity.startActivityForResult(vlcIntent, vlcRequestCode);
        return null;
    }
}
