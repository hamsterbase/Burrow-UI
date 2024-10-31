package com.hamsterbase.burrowui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class AboutActivity extends Activity implements NavigationBar.OnBackClickListener {

    private TextView versionTextView;
    private View licenseSection;
    private View sourceCodeSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_about);

        versionTextView = findViewById(R.id.versionTextView);
        licenseSection = findViewById(R.id.licenseSection);
        sourceCodeSection = findViewById(R.id.sourceCodeSection);

        setupVersionInfo();
        setupLicenseInfo();
        setupSourceCodeInfo();

        NavigationBar navigationBar = findViewById(R.id.navigation_bar);
        navigationBar.setOnBackClickListener(this);
    }

    private void setupVersionInfo() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            versionTextView.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            versionTextView.setText(R.string.unknown);
        }
    }

    private void setupLicenseInfo() {
        licenseSection.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hamsterbase/Burrow-UI/blob/main/License"));
            startActivity(intent);
        });
    }

    private void setupSourceCodeInfo() {
        sourceCodeSection.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hamsterbase/Burrow-UI"));
            startActivity(intent);
        });
    }

    @Override
    public void onBackClick() {
        finish();
    }
}
