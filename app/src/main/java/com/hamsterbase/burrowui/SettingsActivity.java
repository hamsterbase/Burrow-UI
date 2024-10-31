package com.hamsterbase.burrowui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.hamsterbase.burrowui.components.SettingsItem;
import com.hamsterbase.burrowui.components.SwitchSettingsItem;

public class SettingsActivity extends Activity implements NavigationBar.OnBackClickListener {

    private SettingsManager settingsManager;
    private LinearLayout settingsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_settings);
        settingsContainer = findViewById(R.id.settingsContainer);
        settingsManager = new SettingsManager(this);

        addSection("Donate", "Support the development of this app", R.drawable.ic_link,
                v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/hamsterbase"))));

        addLine();


        addSection("Select Apps", "Choose which apps to display in the launcher", R.drawable.ic_right,
                v -> startActivity(new Intent(SettingsActivity.this, AppSelectionActivity.class)));

        addLine();
        
        settingsContainer.addView(new SwitchSettingsItem(
                this,
                "Settings Icon",
                "Show or hide the settings icon in the homepage",
                settingsManager.isShowSettingsIcon(),
                isChecked -> {
                    settingsManager.setShowSettingsIcon(isChecked);
                }
        ));

        addLine();

        addSection("About", "View app information", R.drawable.ic_right,
                v -> startActivity(new Intent(SettingsActivity.this, AboutActivity.class)));

        NavigationBar navigationBar = findViewById(R.id.navigation_bar);
        navigationBar.setOnBackClickListener(this);
    }

    private void addSection(String title, String description, int iconResId, View.OnClickListener listener) {
        SettingsItem section = new SettingsItem(this);
        section.setTitle(title);
        section.setDescription(description);
        section.setIcon(iconResId);
        section.setOnClickListener(listener);
        settingsContainer.addView(section);
    }


    private void addLine() {
        View dividerView = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        int marginInPixels = (int) (24 * getResources().getDisplayMetrics().density);
        params.setMargins(marginInPixels, 0, marginInPixels, 0);
        dividerView.setLayoutParams(params);
        dividerView.setBackgroundColor(Color.BLACK);
        settingsContainer.addView(dividerView);
    }

    @Override
    public void onBackClick() {
        finish();
    }
}
