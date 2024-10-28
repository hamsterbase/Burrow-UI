package com.hamsterbase.burrowui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hamsterbase.burrowui.service.AppInfo;
import com.hamsterbase.burrowui.service.AppManagementService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppSelectionActivity extends Activity implements NavigationBar.OnBackClickListener {
    private List<AppInfo> allApps;
    private List<SettingsManager.SelectedItem> selectedItems;
    private ListView appListView;
    private AppAdapter appAdapter;
    private SettingsManager settingsManager;
    private AppManagementService appManagementService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_app_selection);
        appListView = findViewById(R.id.appListView);

        settingsManager = new SettingsManager(this);
        appManagementService = ((BurrowUIApplication) getApplication()).getAppManagementService();
        loadApps();
        appAdapter = new AppAdapter();
        appListView.setAdapter(appAdapter);

        NavigationBar navigationBar = findViewById(R.id.navigation_bar);
        navigationBar.setListView(appListView);
        navigationBar.setOnBackClickListener(this);
    }

    private void loadApps() {
        allApps = appManagementService.listApps();
        selectedItems = settingsManager.getSelectedItems();
    }

    public void onBackClick() {
        finish();
    }

    private class AppAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return allApps.size();
        }

        @Override
        public Object getItem(int position) {
            return allApps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(AppSelectionActivity.this).inflate(R.layout.settings_app_item, parent, false);
                holder = new ViewHolder();
                holder.appIcon = convertView.findViewById(R.id.appIcon);
                holder.appName = convertView.findViewById(R.id.appName);
                holder.appCheckImage = convertView.findViewById(R.id.appCheckImage);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppInfo app = allApps.get(position);
            holder.appIcon.setImageDrawable(appManagementService.getIcon(app.getPackageName(), app.getUserId()));
            holder.appName.setText(app.getLabel());

            boolean isSelected = isAppSelected(app);
            holder.appCheckImage.setImageResource(isSelected ? R.drawable.ic_checked : R.drawable.ic_unchecked);

            convertView.setOnClickListener(v -> {
                boolean newState = !isAppSelected(app);
                if (newState) {
                    addSelectedApp(app);
                } else {
                    removeSelectedApp(app);
                }
                holder.appCheckImage.setImageResource(newState ? R.drawable.ic_checked : R.drawable.ic_unchecked);
            });

            return convertView;
        }

        private class ViewHolder {
            ImageView appIcon;
            TextView appName;
            ImageView appCheckImage;
        }
    }

    private boolean isAppSelected(AppInfo app) {
        for (SettingsManager.SelectedItem item : selectedItems) {
            if (item.getType().equals("application") &&
                    item.getMeta().get("packageName").equals(app.getPackageName()) &&
                    (app.getUserId() == null || app.getUserId().equals(item.getMeta().get("userId")))) {
                return true;
            }
        }
        return false;
    }

    private void addSelectedApp(AppInfo app) {
        Map<String, String> meta = new HashMap<>();
        meta.put("packageName", app.getPackageName());
        if (app.getUserId() != null) {
            meta.put("userId", app.getUserId());
        }
        SettingsManager.SelectedItem newItem = new SettingsManager.SelectedItem("application", meta);
        settingsManager.pushSelectedItem(newItem);
        selectedItems.add(newItem);
    }

    private void removeSelectedApp(AppInfo app) {
        for (int i = 0; i < selectedItems.size(); i++) {
            SettingsManager.SelectedItem item = selectedItems.get(i);
            if (item.getType().equals("application") &&
                    item.getMeta().get("packageName").equals(app.getPackageName()) &&
                    (app.getUserId() == null || app.getUserId().equals(item.getMeta().get("userId")))) {
                settingsManager.deleteSelectedItem(i);
                selectedItems.remove(i);
                break;
            }
        }
    }
}
