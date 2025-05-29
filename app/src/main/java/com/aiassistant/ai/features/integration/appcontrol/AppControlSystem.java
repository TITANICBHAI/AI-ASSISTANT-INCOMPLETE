package com.aiassistant.ai.features.integration.appcontrol;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * App Control System for interacting with and controlling external applications
 * This system allows the AI Assistant to launch, interact with, and control other apps.
 */
public class AppControlSystem {
    private static final String TAG = "AppControlSystem";
    
    // Common apps with their package names
    private static final Map<String, String> COMMON_APPS = new HashMap<>();
    static {
        // Social media
        COMMON_APPS.put("facebook", "com.facebook.katana");
        COMMON_APPS.put("instagram", "com.instagram.android");
        COMMON_APPS.put("twitter", "com.twitter.android");
        COMMON_APPS.put("linkedin", "com.linkedin.android");
        
        // Productivity
        COMMON_APPS.put("gmail", "com.google.android.gm");
        COMMON_APPS.put("outlook", "com.microsoft.office.outlook");
        COMMON_APPS.put("chrome", "com.android.chrome");
        COMMON_APPS.put("firefox", "org.mozilla.firefox");
        COMMON_APPS.put("drive", "com.google.android.apps.docs");
        COMMON_APPS.put("docs", "com.google.android.apps.docs.editors.docs");
        COMMON_APPS.put("sheets", "com.google.android.apps.docs.editors.sheets");
        
        // Entertainment
        COMMON_APPS.put("youtube", "com.google.android.youtube");
        COMMON_APPS.put("spotify", "com.spotify.music");
        COMMON_APPS.put("netflix", "com.netflix.mediaclient");
        COMMON_APPS.put("amazon", "com.amazon.mShop.android.shopping");
        
        // Maps and navigation
        COMMON_APPS.put("maps", "com.google.android.apps.maps");
        COMMON_APPS.put("waze", "com.waze");
        COMMON_APPS.put("uber", "com.ubercab");
        
        // Other
        COMMON_APPS.put("calendar", "com.google.android.calendar");
        COMMON_APPS.put("camera", "com.android.camera");
        COMMON_APPS.put("settings", "com.android.settings");
    }
    
    private Context context;
    private Map<String, String> installedApps;
    
    /**
     * Constructor
     * @param context Android context
     */
    public AppControlSystem(Context context) {
        this.context = context;
        this.installedApps = new HashMap<>();
        
        // Initialize installed apps
        scanInstalledApps();
        
        Log.i(TAG, "AppControlSystem initialized with " + installedApps.size() + " apps");
    }
    
    /**
     * Scan for installed apps on the device
     */
    private void scanInstalledApps() {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        
        for (ApplicationInfo app : apps) {
            String appName = packageManager.getApplicationLabel(app).toString().toLowerCase();
            installedApps.put(appName, app.packageName);
        }
        
        // Add common app aliases
        for (Map.Entry<String, String> entry : COMMON_APPS.entrySet()) {
            if (isAppInstalled(entry.getValue())) {
                installedApps.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Launch an app by name
     * @param appName Name of the app (case-insensitive)
     * @return Success status
     */
    public boolean launchApp(String appName) {
        appName = appName.toLowerCase();
        
        // Try to find the app
        String packageName = installedApps.get(appName);
        
        // If not found by name, try to find by partial match
        if (packageName == null) {
            for (Map.Entry<String, String> entry : installedApps.entrySet()) {
                if (entry.getKey().contains(appName)) {
                    packageName = entry.getValue();
                    break;
                }
            }
        }
        
        // Launch the app if found
        if (packageName != null) {
            try {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    Log.i(TAG, "Launched app: " + appName + " (" + packageName + ")");
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error launching app: " + appName, e);
            }
        } else {
            Log.w(TAG, "App not found: " + appName);
        }
        
        return false;
    }
    
    /**
     * Open system settings
     * @param settingType Type of settings (e.g., "wifi", "bluetooth")
     * @return Success status
     */
    public boolean openSettings(String settingType) {
        Intent intent = null;
        
        try {
            switch (settingType.toLowerCase()) {
                case "wifi":
                    intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    break;
                case "bluetooth":
                    intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                    break;
                case "location":
                    intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    break;
                case "display":
                    intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                    break;
                case "sound":
                    intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
                    break;
                case "storage":
                    intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
                    break;
                case "battery":
                    intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                    break;
                case "apps":
                    intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                    break;
                case "data":
                    intent = new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
                    break;
                default:
                    // Open main settings
                    intent = new Intent(Settings.ACTION_SETTINGS);
                    break;
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.i(TAG, "Opened settings: " + settingType);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error opening settings: " + settingType, e);
            return false;
        }
    }
    
    /**
     * Open a URL in the default browser
     * @param url The URL to open
     * @return Success status
     */
    public boolean openUrl(String url) {
        try {
            // Make sure URL starts with http:// or https://
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.i(TAG, "Opened URL: " + url);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error opening URL: " + url, e);
            return false;
        }
    }
    
    /**
     * Make a phone call
     * @param phoneNumber Phone number to call
     * @return Success status
     */
    public boolean makePhoneCall(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.i(TAG, "Initiated phone call to: " + phoneNumber);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error making phone call: " + phoneNumber, e);
            return false;
        }
    }
    
    /**
     * Share text content using the Android share sheet
     * @param text Text to share
     * @param title Title for share sheet
     * @return Success status
     */
    public boolean shareText(String text, String title) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            Intent chooser = Intent.createChooser(intent, title);
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            context.startActivity(chooser);
            Log.i(TAG, "Shared text via share sheet");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error sharing text", e);
            return false;
        }
    }
    
    /**
     * Get a list of installed app names
     * @return List of app names
     */
    public List<String> getInstalledAppNames() {
        return new ArrayList<>(installedApps.keySet());
    }
    
    /**
     * Check if an app is installed by package name
     * @param packageName Package name to check
     * @return True if installed
     */
    public boolean isAppInstalled(String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if an app is installed by name
     * @param appName App name to check
     * @return True if installed
     */
    public boolean isAppInstalledByName(String appName) {
        appName = appName.toLowerCase();
        return installedApps.containsKey(appName);
    }
}
