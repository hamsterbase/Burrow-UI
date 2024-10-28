package com.hamsterbase.burrowui.service;

public class AppInfo {
    private String label;
    private String packageName;
    private String userId;

    public AppInfo(String label, String packageName, String userId) {
        this.label = label;
        this.packageName = packageName;
        this.userId = userId;
    }

    // Getters and setters
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
