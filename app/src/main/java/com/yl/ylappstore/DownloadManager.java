package com.yl.ylappstore;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

public class DownloadManager {
    private Context context;
    public static final String DOWNLOAD_DIR = "AutoStoreDownloads";
    private DownloadCallback currentCallback;

    public interface DownloadCallback {
        void onProgress(int progress);
        void onComplete(File file);
        void onError(String error);
    }

    public DownloadManager(Context context) {
        this.context = context;
    }

    // 添加服务连接管理
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) service;
            binder.setCallback(new DownloadService.DownloadProgress() {
                @Override
                public void onProgress(int progress) {
                    currentCallback.onProgress(progress);
                }

                @Override
                public void onComplete(File file) {
                    currentCallback.onComplete(file);
                    context.unbindService(connection);
                }

                @Override
                public void onError(String error) {
                    currentCallback.onError(error);
                    context.unbindService(connection);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    public void startDownload(String url, String packageName, DownloadCallback callback) {
        Log.e("TAG", "startDownload: " + url);
        this.currentCallback = callback;
        Intent serviceIntent = new Intent(context, DownloadService.class);
        serviceIntent.putExtra("download_url", url);
        serviceIntent.putExtra("package_name", packageName);

        // 启动并绑定服务
        context.startService(serviceIntent);
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }
}