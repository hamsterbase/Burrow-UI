package com.hamsterbase.burrowui;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SettingsManager {
    private static final String PREFS_NAME = "Burrow UI";
    private static final String SHOW_SETTINGS_ICON_KEY = "ShowSettingsIcon";
    private static final String PREF_DATE_FORMAT = "date_format";
    private static final String DEFAULT_DATE_FORMAT = "EEE,MMM d";
    private static final String SELECTED_ITEMS_KEY = "SelectedItems";

    private SharedPreferences sharedPreferences;

    public SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getDateFormat() {
        return sharedPreferences.getString(PREF_DATE_FORMAT, DEFAULT_DATE_FORMAT);
    }

    public boolean isShowSettingsIcon() {
        return sharedPreferences.getBoolean(SHOW_SETTINGS_ICON_KEY, true);
    }

    public void setShowSettingsIcon(boolean show) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHOW_SETTINGS_ICON_KEY, show);
        editor.apply();
    }

    public List<SelectedItem> getSelectedItems() {
        String jsonString = sharedPreferences.getString(SELECTED_ITEMS_KEY, "[]");
        List<SelectedItem> selectedItems = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String type = jsonObject.getString("type");
                JSONObject metaJson = jsonObject.getJSONObject("meta");
                Map<String, String> meta = new HashMap<>();
                for (Iterator<String> it = metaJson.keys(); it.hasNext(); ) {
                    String key = it.next();
                    meta.put(key, metaJson.getString(key));
                }
                selectedItems.add(new SelectedItem(type, meta));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return selectedItems;
    }

    private void saveSelectedItems(List<SelectedItem> selectedItems) {
        JSONArray jsonArray = new JSONArray();

        for (SelectedItem item : selectedItems) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", item.getType());

                JSONObject metaJson = new JSONObject();
                for (Map.Entry<String, String> entry : item.getMeta().entrySet()) {
                    metaJson.put(entry.getKey(), entry.getValue());
                }

                jsonObject.put("meta", metaJson);
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SELECTED_ITEMS_KEY, jsonArray.toString());
        editor.apply();
    }

    public void deleteSelectedItem(int index) {
        List<SelectedItem> items = getSelectedItems();
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            saveSelectedItems(items);
        }
    }

    public void pushSelectedItem(SelectedItem item) {
        List<SelectedItem> items = getSelectedItems();
        items.add(item);
        saveSelectedItems(items);
    }

    public void swapSelectedItems(int index1, int index2) {
        List<SelectedItem> items = getSelectedItems();
        if (index1 >= 0 && index1 < items.size() && index2 >= 0 && index2 < items.size()) {
            SelectedItem temp = items.get(index1);
            items.set(index1, items.get(index2));
            items.set(index2, temp);
            saveSelectedItems(items);
        }
    }


    public static class SelectedItem {
        private String type;
        private Map<String, String> meta;

        public SelectedItem(String type, Map<String, String> meta) {
            this.type = type;
            this.meta = meta;
        }

        public String getType() {
            return type;
        }

        public Map<String, String> getMeta() {
            return meta;
        }
    }
}
