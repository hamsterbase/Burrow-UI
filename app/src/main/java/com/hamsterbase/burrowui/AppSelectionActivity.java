package com.hamsterbase.burrowui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
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

import java.lang.ref.WeakReference;
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
    private SparseArray<Boolean> selectedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_app_selection);
        appListView = findViewById(R.id.appListView);
        appListView.setDivider(null);
        appListView.setDividerHeight(0);
        appListView.setVerticalScrollBarEnabled(false);
        appListView.setOverScrollMode(View.OVER_SCROLL_NEVER);

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
        selectedState = new SparseArray<>();
        for (int i = 0; i < allApps.size(); i++) {
            selectedState.put(i, isAppSelected(allApps.get(i)));
        }
        if (appAdapter != null) {
            appAdapter.notifyDataSetChanged();
        }
    }

    public void onBackClick() {
        finish();
    }

    private class AppAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        AppAdapter() {
            inflater = LayoutInflater.from(AppSelectionActivity.this);
        }

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.settings_app_item, parent, false);
                holder = new ViewHolder();
                holder.appIcon = convertView.findViewById(R.id.appIcon);
                holder.appName = convertView.findViewById(R.id.appName);
                holder.appCheckImage = convertView.findViewById(R.id.appCheckImage);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppInfo app = allApps.get(position);
            holder.appName.setText(app.getLabel());

            boolean isSelected = selectedState.get(position);
            updateCheckImage(holder.appCheckImage, isSelected);

            loadAppIcon(holder.appIcon, app);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean newState = !selectedState.get(position);
                    selectedState.put(position, newState);
                    if (newState) {
                        addSelectedApp(app);
                    } else {
                        removeSelectedApp(app);
                    }
                    updateCheckImage(holder.appCheckImage, newState);
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }

        private void updateCheckImage(ImageView imageView, boolean isSelected) {
            imageView.setImageResource(isSelected ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        }
    }

    private static class ViewHolder {
        ImageView appIcon;
        TextView appName;
        ImageView appCheckImage;
    }

    private void loadAppIcon(ImageView imageView, AppInfo app) {
        new LoadIconTask(imageView).execute(app);
    }

    private class LoadIconTask extends AsyncTask<AppInfo, Void, Drawable> {
        private final WeakReference<ImageView> imageViewReference;

        LoadIconTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Drawable doInBackground(AppInfo... params) {
            AppInfo app = params[0];
            return appManagementService.getIcon(app.getPackageName(), app.getUserId());
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (isCancelled()) {
                drawable = null;
            }

            ImageView imageView = imageViewReference.get();
            if (imageView != null && drawable != null) {
                imageView.setImageDrawable(drawable);
            }
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
        meta.put("userId", app.getUserId());
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
