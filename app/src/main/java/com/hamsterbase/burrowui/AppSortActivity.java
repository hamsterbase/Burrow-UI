package com.hamsterbase.burrowui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hamsterbase.burrowui.service.AppInfo;
import com.hamsterbase.burrowui.service.AppManagementService;

import java.util.ArrayList;
import java.util.List;

public class AppSortActivity extends Activity implements NavigationBar.OnBackClickListener {

    private List<SortableItem> sortableItemList;
    private AppAdapter appAdapter;
    private SettingsManager settingsManager;
    private AppManagementService appManagementService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_app_sort);

        ListView appListView = findViewById(R.id.appSortListView);
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
        List<AppInfo> allApps = appManagementService.listApps();
        sortableItemList = new ArrayList<>();
        List<SettingsManager.SelectedItem> selectedItems = settingsManager.getSelectedItems();
        for (int i = 0; i < selectedItems.size(); i++) {
            SettingsManager.SelectedItem item = selectedItems.get(i);
            if (item.getType().equals("application")) {
                for (AppInfo app : allApps) {
                    if (appManagementService.isSelectItemEqualWith(app, item)) {
                        sortableItemList.add(new SortableItem(app.getLabel(), appManagementService.getIcon(app.getPackageName(), app.getUserId()), i));
                        break;
                    }
                }
            }
        }
        if (appAdapter != null) {
            appAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackClick() {
        finish();
    }

    private class AppAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        AppAdapter() {
            inflater = LayoutInflater.from(AppSortActivity.this);
        }

        @Override
        public int getCount() {
            return sortableItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return sortableItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.sort_app_item, parent, false);
                holder = new ViewHolder();
                holder.appIcon = convertView.findViewById(R.id.appIcon);
                holder.appName = convertView.findViewById(R.id.appName);
                holder.upButton = convertView.findViewById(R.id.upButton);
                holder.downButton = convertView.findViewById(R.id.downButton);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            SortableItem sortableApp = sortableItemList.get(position);
            holder.appName.setText(sortableApp.getLabel());
            holder.appIcon.setImageDrawable(sortableApp.getIcon());

            holder.upButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position > 0) {
                        swapApps(position, position - 1);
                        notifyDataSetChanged();
                    }
                }
            });

            holder.downButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position < sortableItemList.size() - 1) {
                        swapApps(position, position + 1);
                        notifyDataSetChanged();
                    }
                }
            });

            if (position == 0) {
                holder.upButton.setAlpha(0);
            }
            if (position == sortableItemList.size() - 1) {
                holder.downButton.setAlpha(0);
            }
            return convertView;
        }

        private void swapApps(int fromPosition, int toPosition) {
            SortableItem fromItem = sortableItemList.get(fromPosition);
            SortableItem toItem = sortableItemList.get(toPosition);
            settingsManager.swapSelectedItems(fromItem.getOriginalPos(), toItem.getOriginalPos());
            sortableItemList.set(fromPosition, toItem);
            sortableItemList.set(toPosition, fromItem);


            int fromOriginalPos = fromItem.getOriginalPos();
            fromItem.setOriginalPos(toItem.getOriginalPos());
            toItem.setOriginalPos(fromOriginalPos);
        }
    }

    private static class ViewHolder {
        ImageView appIcon;
        TextView appName;
        ImageButton upButton;
        ImageButton downButton;
    }


    private static class SortableItem {
        private String label;
        private int originalPos;
        private Drawable icon;


        SortableItem(String label, Drawable icon, int originalPos) {
            this.label = label;
            this.originalPos = originalPos;
            this.icon = icon;
        }

        public String getLabel() {
            return this.label;
        }

        public Drawable getIcon() {
            return this.icon;
        }

        public int getOriginalPos() {
            return originalPos;
        }

        public void setOriginalPos(int originalPos) {
            this.originalPos = originalPos;
        }
    }
}
