package com.aiassistant.data.models;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import android.graphics.drawable.Drawable;

/**
 * Model for storing information about an installed application.
 */
public class AppInfo {
    private String packageName;
    private String appName;
    private Drawable appIcon;
    private boolean isSelected;
    
    public AppInfo(String packageName, String appName, Drawable appIcon) {
        this.packageName = packageName;
        this.appName = appName;
        this.appIcon = appIcon;
        this.isSelected = false;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public Drawable getAppIcon() {
        return appIcon;
    }
    
    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }
    
    public boolean isSelected() {
        return isSelected;
    }
    
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        AppInfo appInfo = (AppInfo) o;
        
        return packageName.equals(appInfo.packageName);
    }
    
    @Override
    public int hashCode() {
        return packageName.hashCode();
    }
}