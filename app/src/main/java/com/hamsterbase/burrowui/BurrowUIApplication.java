package com.hamsterbase.burrowui;

import android.app.Application;

import com.hamsterbase.burrowui.service.AppManagementService;

public class BurrowUIApplication extends Application {


    private AppManagementService appManagementService;

    @Override
    public void onCreate() {
        super.onCreate();
        appManagementService = AppManagementService.getInstance(this);
    }

    public AppManagementService getAppManagementService() {
        return appManagementService;
    }
}
