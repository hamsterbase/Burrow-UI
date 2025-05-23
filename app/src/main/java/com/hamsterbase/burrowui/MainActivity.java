package com.hamsterbase.burrowui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hamsterbase.burrowui.service.AppInfo;
import com.hamsterbase.burrowui.service.AppManagementService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView timeTextView;
    private TextView dateTextView;
    private TextView amPmTextView;
    private LinearLayout appLinearLayout;
    private List<AppInfo> selectedApps;
    private SettingsManager settingsManager;
    private AppManagementService appManagementService;
    private Handler handler;
    private Runnable updateTimeRunnable;
    private BroadcastReceiver batteryReceiver;
    private String batteryText = "";

    private float touchStartY;
    private static final float SWIPE_THRESHOLD = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        timeTextView = findViewById(R.id.timeTextView);
        dateTextView = findViewById(R.id.dateTextView);
        amPmTextView = findViewById(R.id.amPmTextView);
        appLinearLayout = findViewById(R.id.appLinearLayout);

        ScrollView appList = findViewById(R.id.appScrollList);
        appList.setOverScrollMode(View.OVER_SCROLL_NEVER);
        appList.setVerticalScrollBarEnabled(false);

        TextView debugTextView = findViewById(R.id.debugTextView);
        if (BuildConfig.DEBUG) {
            debugTextView.setVisibility(View.VISIBLE);
            String debugInfo = "Debug: " + Build.MODEL + " - " + Build.VERSION.RELEASE;
            debugTextView.setText(debugInfo);
        } else {
            debugTextView.setVisibility(View.GONE);
        }

        settingsManager = new SettingsManager(this);
        appManagementService = AppManagementService.getInstance(this);

        handler = new Handler(Looper.getMainLooper());
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000); // Update every second
            }
        };

        loadApps();
        displaySelectedApps();

        View rootView = findViewById(android.R.id.content);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            private boolean isLongPress = false;
            private Handler longPressHandler = new Handler();
            private static final long LONG_PRESS_TIMEOUT = 600;
            private boolean isPullDownEnabled = false;

            private Runnable longPressRunnable = new Runnable() {
                @Override
                public void run() {
                    isLongPress = true;
                    openSettingsActivity();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchStartY = event.getY();
                        isLongPress = false;
                        isPullDownEnabled = touchStartY > 50 && settingsManager.isEnablePullDownSearch();
                        longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (isPullDownEnabled && Math.abs(event.getY() - touchStartY) > SWIPE_THRESHOLD) {
                            longPressHandler.removeCallbacks(longPressRunnable);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        longPressHandler.removeCallbacks(longPressRunnable);
                        if (isLongPress) {
                            return true;
                        }
                        float touchEndY = event.getY();
                        float deltaY = touchEndY - touchStartY;
                        if (isPullDownEnabled && deltaY > SWIPE_THRESHOLD) {
                            openSearchActivity();
                            return true;
                        }
                        return false;

                    case MotionEvent.ACTION_CANCEL:
                        longPressHandler.removeCallbacks(longPressRunnable);
                        return false;
                }
                return false;
            }
        });

        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateBatteryStatus(intent);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApps();
        displaySelectedApps();
        handler.post(updateTimeRunnable);

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateTimeRunnable);
        unregisterReceiver(batteryReceiver);
    }

    private void updateTime() {
        if (settingsManager.isUse24HourFormat()) {
            // 24-hour format
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            timeTextView.setText(currentTime);
            amPmTextView.setVisibility(View.GONE);
        } else {
            // 12-hour format
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
            String currentTime = sdf.format(new Date());
            timeTextView.setText(currentTime);
            
            // Get AM/PM separately using English locale to ensure "AM"/"PM" instead of "上午"/"下午"
            SimpleDateFormat amPmSdf = new SimpleDateFormat("a", Locale.ENGLISH);
            String amPm = amPmSdf.format(new Date());
            amPmTextView.setText(amPm);
            amPmTextView.setVisibility(View.VISIBLE);
        }
        
        String dateFormat = settingsManager.getDateFormat();
        SimpleDateFormat dateSdf = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        String currentDate = dateSdf.format(new Date()).concat(batteryText);
        dateTextView.setText(currentDate);
    }

    private void updateBatteryStatus(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level * 100 / (float) scale;
        batteryText = String.format(Locale.getDefault(), " %.0f%%", batteryPct);
    }

    private void loadApps() {
        List<AppInfo> allApps = appManagementService.listApps();
        selectedApps = new ArrayList<>();
        List<SettingsManager.SelectedItem> selectedItems = settingsManager.getSelectedItems();
        for (SettingsManager.SelectedItem item : selectedItems) {
            if (item.getType().equals("application")) {
                for (AppInfo app : allApps) {
                    if (appManagementService.isSelectItemEqualWith(app, item)) {
                        selectedApps.add(app);
                    }
                }
            }
        }
    }

    private void displaySelectedApps() {
        appLinearLayout.removeAllViews();
        for (AppInfo app : selectedApps) {
            addAppToLayout(app);
        }

        if (settingsManager.isShowSettingsIcon()) {
            addSettingsAppToLayout();
        }
    }

    private void addAppToLayout(AppInfo app) {
        View appView = getLayoutInflater().inflate(R.layout.app_item, null);
        ImageView iconView = appView.findViewById(R.id.appIcon);
        TextView nameView = appView.findViewById(R.id.appName);

        iconView.setImageDrawable(appManagementService.getIcon(app.getPackageName(), app.getUserId()));
        nameView.setText(app.getLabel());
        appView.setOnClickListener(v -> appManagementService.launchApp(app));
        appLinearLayout.addView(appView);
    }

    private void addSettingsAppToLayout() {
        View settingsAppView = getLayoutInflater().inflate(R.layout.app_item, null);
        ImageView iconView = settingsAppView.findViewById(R.id.appIcon);
        TextView nameView = settingsAppView.findViewById(R.id.appName);

        iconView.setImageResource(R.drawable.ic_settings);
        nameView.setText(R.string.launcher_settings);
        settingsAppView.setOnClickListener(v -> openSettingsActivity());
        appLinearLayout.addView(settingsAppView);
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
}
