package com.hamsterbase.burrowui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hamsterbase.burrowui.service.AppInfo;
import com.hamsterbase.burrowui.service.AppManagementService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends Activity {

    private static final String TAG = "SearchActivity";

    private EditText searchInput;
    private Button exitButton;
    private ImageButton clearButton;
    private ListView appListView;
    private List<AppInfo> allApps;
    private List<AppInfo> filteredApps;
    private AppAdapter adapter;
    private AppManagementService appManagementService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        searchInput = findViewById(R.id.search_input);
        searchInput.requestFocus();
        exitButton = findViewById(R.id.exit_button);
        clearButton = findViewById(R.id.clear_button);
        appListView = findViewById(R.id.app_list);
        appListView.setDivider(null);
        appListView.setVerticalScrollBarEnabled(false);
        appListView.setDividerHeight(0);
        appListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        appManagementService = ((BurrowUIApplication) getApplication()).getAppManagementService();

        allApps = appManagementService.listApps();
        filteredApps = new ArrayList<>();
        adapter = new AppAdapter();
        appListView.setAdapter(adapter);


        // 设置 IME 选项为 "Done"
        searchInput.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // 设置 Editor Action Listener
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // 在这里处理 "Done" 按钮的点击事件
                    // 例如，可以隐藏软键盘
                    // InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    // imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    // 返回 true 表示我们已经处理了这个事件
                    return true;
                }
                // 如果不是 "Done" 动作，让系统继续处理
                return false;
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
                updateClearButtonVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInput.setText("");
            }
        });

        appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppInfo app = filteredApps.get(position);
                appManagementService.launchApp(app);
            }
        });

        updateClearButtonVisibility();
    }


    private void filterApps(String query) {
        filteredApps.clear();
        for (AppInfo app : allApps) {
            if (app.getLabel().toLowerCase().contains(query.toLowerCase())) {
                filteredApps.add(app);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateClearButtonVisibility() {
        if (searchInput.getText().length() > 0) {
            clearButton.setVisibility(View.VISIBLE);
        } else {
            clearButton.setVisibility(View.GONE);
        }
    }

    private class AppAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        AppAdapter() {
            inflater = LayoutInflater.from(SearchActivity.this);
        }

        @Override
        public int getCount() {
            return filteredApps.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredApps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.app_item, parent, false);
                holder = new ViewHolder();
                holder.appIcon = convertView.findViewById(R.id.appIcon);
                holder.appName = convertView.findViewById(R.id.appName);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            AppInfo app = filteredApps.get(position);
            holder.appName.setText(app.getLabel());
            loadAppIcon(holder.appIcon, app);

            return convertView;
        }
    }

    private static class ViewHolder {
        ImageView appIcon;
        TextView appName;
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
}
