package com.hamsterbase.burrowui.service;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.os.UserManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppManagementService {

    private static AppManagementService instance;
    private final Context context;
    private final Map<String, Drawable> iconCache;
    private final LauncherApps launcherApps;
    private final UserManager userManager;

    private AppManagementService(Context context) {
        this.context = context.getApplicationContext();
        this.iconCache = new HashMap<>();
        this.launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
    }

    public static synchronized AppManagementService getInstance(Context context) {
        if (instance == null) {
            instance = new AppManagementService(context);
        }
        return instance;
    }

    public List<AppInfo> listApps() {
        List<AppInfo> appInfoList = new ArrayList<>();
        List<UserHandle> users = userManager.getUserProfiles();

        UserHandle currentUser = android.os.Process.myUserHandle();
        int currentUserId = currentUser.hashCode();

        for (UserHandle user : users) {
            List<LauncherActivityInfo> activities = launcherApps.getActivityList(null, user);
            for (LauncherActivityInfo activityInfo : activities) {
                String userId = user.hashCode() == currentUserId ? null : String.valueOf(user.hashCode());
                appInfoList.add(new AppInfo(
                        activityInfo.getLabel().toString(),
                        activityInfo.getApplicationInfo().packageName,
                        userId
                ));
            }
        }

        return appInfoList;
    }

    public Drawable getIcon(String packageName, String userId) {
        String cacheKey = packageName + (userId != null ? userId : "");

        // Check cache first
        Drawable cachedIcon = iconCache.get(cacheKey);
        if (cachedIcon != null) {
            return cachedIcon;
        }

        // Load icon
        Drawable icon = loadIconFromLauncherApps(packageName, userId);
        iconCache.put(cacheKey, icon);

        return icon;
    }

    private Drawable loadIconFromLauncherApps(String packageName, String userId) {
        UserHandle userHandle = null;
        if (userId != null) {
            for (UserHandle user : userManager.getUserProfiles()) {
                if (String.valueOf(user.hashCode()).equals(userId)) {
                    userHandle = user;
                    break;
                }
            }
        }

        if (userHandle == null) {
            userHandle = android.os.Process.myUserHandle();
        }

        List<LauncherActivityInfo> activities = launcherApps.getActivityList(packageName, userHandle);
        if (!activities.isEmpty()) {
            return activities.get(0).getIcon(0);
        }

        // Return a default icon if the package is not found
        return context.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
    }


}
