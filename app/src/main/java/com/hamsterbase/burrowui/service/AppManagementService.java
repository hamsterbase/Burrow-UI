package com.hamsterbase.burrowui.service;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
    private final Map<String, UserHandle> userCache;
    private final LauncherApps launcherApps;
    private final UserManager userManager;
    private final PackageManager packageManager;

    private AppManagementService(Context context) {
        this.context = context.getApplicationContext();
        this.iconCache = new HashMap<>();
        this.userCache = new HashMap<>();
        this.launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        this.packageManager = context.getPackageManager();
        initUserCache();
    }

    public static synchronized AppManagementService getInstance(Context context) {
        if (instance == null) {
            instance = new AppManagementService(context);
        }
        return instance;
    }

    private void initUserCache() {
        List<UserHandle> users = userManager.getUserProfiles();
        for (UserHandle user : users) {
            userCache.put(String.valueOf(user.hashCode()), user);
        }
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
        String cacheKey = packageName + (userId != null ? ":" + userId : "");

        Drawable cachedIcon = iconCache.get(cacheKey);
        if (cachedIcon != null) {
            return cachedIcon;
        }

        return loadIconFromLauncherApps(packageName, userId);
    }

    private Drawable loadIconFromLauncherApps(String packageName, String userId) {
        if (userId == null) {
            try {
                return packageManager.getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }
        UserHandle userHandle = userCache.get(userId);
        if (userHandle == null) {
            return null;
        }
        try {
            if (iconCache.get(packageName) == null) {
                iconCache.put(packageName, copyIcon(packageManager.getApplicationIcon(packageName)));
            }
        } catch (PackageManager.NameNotFoundException e) {
            //
        }
        List<LauncherActivityInfo> activities = launcherApps.getActivityList(packageName, userHandle);
        if (!activities.isEmpty()) {
            String cacheKey = packageName + ":" + userId;
            Drawable icon = activities.get(0).getIcon(0);
            iconCache.put(cacheKey, icon);
            return icon;
        }
        return null;
    }

    public Drawable copyIcon(Drawable icon) {
        if (icon instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
            return new BitmapDrawable(context.getResources(), bitmap.copy(bitmap.getConfig(), true));
        }
        return icon.getConstantState().newDrawable().mutate();
    }
}
