package com.yl.ylappstore;

import com.yl.basemvp.AppInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemAppRepository {
    private static final Map<String, AppInfo> SYSTEM_APPS = new HashMap<>();

    static {
        // 导航类
        putApp(new AppInfo(
                "高德地图",
                "com.autonavi.amapauto",
                "navigation",
                "https://mapdownload.autonavi.com/apps/auto/manual/V810/Auto_8.1.0.600185_release_signed.apk?u=e9df77bc9122e3a8699b8fe13987c015&",
                R.drawable.ic_amap,
                true
        ));

        // 音乐类
        putApp(new AppInfo(
                "酷狗音乐车机版",
                "com.kugou.android.auto",
                "music",
                "https://download.kugou.com/dl/kugou_auto",
                R.drawable.ic_kugou,
                true
        ));

        putApp(new AppInfo(
                "QQ音乐车机版",
                "com.tencent.qqmusic.auto",
                "music",
                "https://your-cdn.com/qqmusic_auto.apk",
                R.drawable.ic_qqmusic,
                true
        ));
    }

    private static void putApp(AppInfo app) {
        SYSTEM_APPS.put(app.getPackageName(), app);
    }

    public static List<AppInfo> getSystemApps() {
        return new ArrayList<>(SYSTEM_APPS.values());
    }

    public static boolean isSystemApp(String packageName) {
        return SYSTEM_APPS.containsKey(packageName);
    }
}