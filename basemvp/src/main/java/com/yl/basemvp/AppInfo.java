package com.yl.basemvp;

// 数据模型
public class AppInfo {
    private String name;
    private String packageName;
    private String category;
    private String downloadUrl;
    private int localIcon;
    private boolean isSystemApp;

    public AppInfo(String name, String packageName, String category,
                   String downloadUrl, int localIcon, boolean isSystemApp) {
        this.name = name;
        this.packageName = packageName;
        this.category = category;
        this.downloadUrl = downloadUrl;
        this.localIcon = localIcon;
        this.isSystemApp = isSystemApp;
    }

    // Getters
    public String getDownloadUrl() { return downloadUrl; }
    public boolean isSystemApp() { return isSystemApp; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public int getLocalIcon() {
        return localIcon;
    }

    public void setLocalIcon(int localIcon) {
        this.localIcon = localIcon;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }
}
