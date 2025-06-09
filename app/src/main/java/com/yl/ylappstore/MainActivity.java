package com.yl.ylappstore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.yl.basemvp.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    public List<AppInfo> systemApps = new ArrayList<>();
    private NavigationView navigationView;
    private int lastPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化系统应用数据
        initializeSystemApps();

        // 初始化视图组件
        setupViews();

        // 配置ViewPager和底部导航的联动
        setupViewPagerWithNav();

        // 检查系统应用状态
        verifySystemApps();
    }

    private void initializeSystemApps() {
        // 从预设仓库加载系统应用数据
        systemApps.addAll(SystemAppRepository.getSystemApps());
    }

    private void setupViews() {
        viewPager = findViewById(R.id.view_pager);
        navigationView = findViewById(R.id.navigation);
        // 禁用用户滑动切换（车机版常用设置）
        viewPager.setUserInputEnabled(false);
    }

    private void setupViewPagerWithNav() {
        // 创建Fragment列表
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(AppListFragment.newInstance("navigation"));
        fragments.add(AppListFragment.newInstance("music"));
        fragments.add(AppListFragment.newInstance("other"));

        // 配置ViewPager适配器
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        });
        ColorStateList csl=(ColorStateList)getResources().getColorStateList(R.drawable.navigation_menu_item_color);
        navigationView.setItemTextColor(csl);

        // 底部导航点击监听
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_navigation) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.nav_music) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.nav_other) {
                viewPager.setCurrentItem(2);
                return true;
            }
            return false;
        });

        // ViewPager页面切换回调
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (lastPosition != -1) {
                    navigationView.getMenu().getItem(lastPosition).setChecked(false);
                }
                navigationView.getMenu().getItem(position).setChecked(true);
                lastPosition = position;
            }
        });
    }

    private void verifySystemApps() {
        // 验证系统应用是否真实存在
        new Thread(() -> {
            for (AppInfo app : systemApps) {
                try {
                    getPackageManager().getPackageInfo(app.getPackageName(), 0);
                    Log.d("SystemAppCheck", app.getName() + " 验证通过");
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w("SystemAppCheck", app.getName() + " 未找到");
                    app.setSystemApp(false);
                }
            }
        }).start();
    }

    private static final int REQUEST_CODE = 1001;
    private static final String[] REQUIRED_PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
    }

    private void checkPermissions() {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    missingPermissions.toArray(new String[0]),
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(code, permissions, results);
        if (code == REQUEST_CODE) {
            for (int i = 0; i < results.length; i++) {
                if (results[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "需要存储权限才能下载应用", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
        }
    }

    // 车机硬件按键处理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                handleHardwareLeft();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                handleHardwareRight();
                return true;
            case KeyEvent.KEYCODE_ENTER:
                handleHardwareConfirm();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void handleHardwareLeft() {
        int current = viewPager.getCurrentItem();
        if (current > 0) {
            viewPager.setCurrentItem(current - 1);
        }
    }

    private void handleHardwareRight() {
        int current = viewPager.getCurrentItem();
        if (current < viewPager.getAdapter().getItemCount() - 1) {
            viewPager.setCurrentItem(current + 1);
        }
    }

    private void handleHardwareConfirm() {
        // 获取当前Fragment执行确认操作
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentFragment instanceof AppListFragment) {
            ((AppListFragment) currentFragment).handleHardwareSelect();
        }
    }

    // 刷新所有Fragment的数据
    public void refreshAllFragments() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof AppListFragment) {
                ((AppListFragment) fragment).refreshAppList();
            }
        }
    }

}