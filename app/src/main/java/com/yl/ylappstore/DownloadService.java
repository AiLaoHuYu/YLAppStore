package com.yl.ylappstore;

import static com.yl.ylappstore.DownloadManager.DOWNLOAD_DIR;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends Service {
    private static final int NOTIFICATION_ID = 1;
    private DownloadBinder binder = new DownloadBinder();
    private DownloadProgress progressCallback;

    public interface DownloadProgress {
        void onProgress(int progress);

        void onComplete(File file);

        void onError(String error);
    }

    public class DownloadBinder extends Binder {
        public void setCallback(DownloadProgress callback) {
            progressCallback = callback;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("download_url");
        String packageName = intent.getStringExtra("package_name");
        Log.e("TAG", "onStartCommand: " + url + ":: " + packageName);
        new DownloadTask().execute(url, packageName);
        return START_STICKY;
    }

    private class DownloadTask extends AsyncTask<String, Integer, File> {
        @Override
        protected File doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                File outputDir = new File(getExternalFilesDir(null), DOWNLOAD_DIR);
                if (!outputDir.exists()) outputDir.mkdirs();
                File outputFile = new File(outputDir, params[1] + ".apk");

                try (InputStream input = connection.getInputStream();
                     FileOutputStream output = new FileOutputStream(outputFile)) {

                    byte[] buffer = new byte[4096];
                    int totalBytes = connection.getContentLength();
                    int downloadedBytes = 0;
                    int bytesRead;

                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                        downloadedBytes += bytesRead;
                        publishProgress((downloadedBytes * 100) / totalBytes);
                    }
                }
                return outputFile;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (progressCallback != null) {
                progressCallback.onProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(File file) {
            if (file != null) {
                progressCallback.onComplete(file);
            } else {
                progressCallback.onError("下载失败");
            }
            stopSelf();
        }
    }

    private boolean verifyApk(File apkFile, String expectedPackage) {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkFile.getPath(), 0);
            return info != null && info.packageName.equals(expectedPackage);
        } catch (Exception e) {
            return false;
        }
    }

}
