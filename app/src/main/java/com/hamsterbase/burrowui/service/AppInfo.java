package com.hamsterbase.burrowui.service;

public class AppInfo {
    private String label;
    private String packageName;
    private String userId;
    private String componentName;


    public AppInfo(String label, String packageName, String userId, String componentName) {
        this.label = label;
        this.packageName = packageName;
        this.userId = userId;
        this.componentName = componentName;
    }

    // Getters and setters
    public String getLabel() {
        return label;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getUserId() {
        return userId;
    }

    public String getComponentName() {
        return componentName;
    }
}
