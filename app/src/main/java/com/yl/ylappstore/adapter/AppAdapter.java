package com.yl.ylappstore.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.yl.basemvp.AppInfo;
import com.yl.ylappstore.DownloadManager;
import com.yl.ylappstore.R;

import java.io.File;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private List<AppInfo> appList;
    private Context context;
    private DownloadManager downloadManager;

    public AppAdapter(List<AppInfo> appList, Context context) {
        this.appList = appList;
        this.context = context;
        this.downloadManager = new DownloadManager(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        holder.appName.setText(app.getName());
        holder.appIcon.setImageResource(app.getLocalIcon());

        boolean isInstalled = isAppInstalled(app.getPackageName());
        boolean isSystem = app.isSystemApp();

        updateButtonState(holder.btnAction, isInstalled, isSystem);

        holder.btnAction.setOnClickListener(v -> handleAppAction(app, holder.progressBar));
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    private void updateButtonState(Button btn, boolean installed, boolean isSystem) {
        if (isSystem) {
            btn.setText("系统应用");
            btn.setEnabled(false);
            btn.setBackgroundColor(Color.GRAY);
        } else if (installed) {
            btn.setText("打开");
            btn.setEnabled(true);
            btn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            btn.setText("安装");
            btn.setEnabled(true);
            btn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
        }
    }

    // 添加列表更新方法
    public void updateList(List<AppInfo> newList) {
        appList.clear();
        appList.addAll(newList);
        notifyDataSetChanged();
    }

    // 添加项目点击处理
    public void performItemClick(int position, ViewHolder holder) {
        AppInfo app = appList.get(position);
        if (holder != null) {
            handleAppAction(app, holder.progressBar);
        }
    }

    private void handleAppAction(AppInfo app, ProgressBar progressBar) {
        if (app.isSystemApp()) {
            showSystemAppDialog();
        } else if (isAppInstalled(app.getPackageName())) {
            openApp(app.getPackageName());
        } else {
            downloadManager.startDownload(
                    app.getDownloadUrl(),
                    app.getPackageName(),
                    new DownloadManager.DownloadCallback() {
                        @Override
                        public void onProgress(int progress) {
                            Log.d("TAG", "onProgress: " + progress);
                            progressBar.setVisibility(View.VISIBLE);
                            progressBar.setProgress(progress);
                        }

                        @Override
                        public void onComplete(File file) {
                            installApk(file);
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(context, "下载失败: " + error, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
            );
        }
    }

    private void showSystemAppDialog() {
        Log.e("TAG", "showSystemAppDialog: ");
    }

    private void openApp(String packageName) {
        Log.e("TAG", "openApp: " + packageName);
    }

    private boolean isAppInstalled(String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void installApk(File apkFile) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        Uri apkUri = FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".provider",
                apkFile);
        //判读版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= 24) {
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            install.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        context.startActivity(install);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        Button btnAction;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.iv_app_icon);
            appName = itemView.findViewById(R.id.tv_app_name);
            btnAction = itemView.findViewById(R.id.btn_action);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
}
