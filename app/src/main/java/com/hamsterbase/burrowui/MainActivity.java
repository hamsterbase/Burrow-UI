package com.hamsterbase.burrowui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    private TextView timeTextView;
    private TextView dateTextView;
    private TextView debugTextView;
    private LinearLayout appLinearLayout;
    private List<ResolveInfo> allApps;
    private List<ResolveInfo> selectedApps = new ArrayList<>();
    private SettingsManager settingsManager;
    private Handler handler;
    private Runnable updateTimeRunnable;
    private BroadcastReceiver batteryReceiver;
    private String batteryText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        timeTextView = findViewById(R.id.timeTextView);
        dateTextView = findViewById(R.id.dateTextView);
        appLinearLayout = findViewById(R.id.appLinearLayout);


        debugTextView = findViewById(R.id.debugTextView);
        if (BuildConfig.DEBUG) {
            debugTextView.setVisibility(View.VISIBLE);
            String debugInfo = "Debug: " + Build.MODEL + " - " + Build.VERSION.RELEASE;
            debugTextView.setText(debugInfo);
        } else {
            debugTextView.setVisibility(View.GONE);
        }

        settingsManager = new SettingsManager(this);

        handler = new Handler(Looper.getMainLooper());
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1 * 1000); // Update every minute
            }
        };

        loadApps();
        displaySelectedApps();

        View rootView = findViewById(android.R.id.content);
        rootView.setOnLongClickListener(v -> {
            openSettingsActivity();
            return true;
        });

        // Initialize battery receiver
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

        // Register battery receiver
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateTimeRunnable);

        // Unregister battery receiver
        unregisterReceiver(batteryReceiver);
    }

    private void updateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        timeTextView.setText(currentTime);
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
        selectedApps.clear();
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        allApps = pm.queryIntentActivities(mainIntent, 0);

        Set<String> savedApps = settingsManager.getSelectedApps();
        for (ResolveInfo app : allApps) {
            if (savedApps.contains(app.activityInfo.packageName)) {
                selectedApps.add(app);
            }
        }
    }

    private void displaySelectedApps() {
        appLinearLayout.removeAllViews();
        for (ResolveInfo app : selectedApps) {
            addAppToLayout(app);
        }

        if (settingsManager.isShowSettingsIcon()) {
            addSettingsAppToLayout();
        }
    }

    private void addAppToLayout(ResolveInfo app) {
        View appView = getLayoutInflater().inflate(R.layout.app_item, null);
        ImageView iconView = appView.findViewById(R.id.appIcon);
        TextView nameView = appView.findViewById(R.id.appName);

        iconView.setImageDrawable(app.loadIcon(getPackageManager()));
        nameView.setText(app.loadLabel(getPackageManager()));
        appView.setOnClickListener(v -> launchApp(app));
        appLinearLayout.addView(appView);
    }

    private void addSettingsAppToLayout() {
        View settingsAppView = getLayoutInflater().inflate(R.layout.app_item, null);
        ImageView iconView = settingsAppView.findViewById(R.id.appIcon);
        TextView nameView = settingsAppView.findViewById(R.id.appName);

        iconView.setImageResource(R.drawable.ic_settings); // Make sure to add this icon to your drawable resources
        nameView.setText("Launcher Settings");
        settingsAppView.setOnClickListener(v -> openSettingsActivity());
        appLinearLayout.addView(settingsAppView);
    }

    private void launchApp(ResolveInfo app) {
        String packageName = app.activityInfo.packageName;
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            startActivity(launchIntent);
        }
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
