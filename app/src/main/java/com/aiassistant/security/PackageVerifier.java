package com.aiassistant.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Verifies packages installed on the device and identifies potentially
 * dangerous applications that could be used for detection or monitoring.
 */
public class PackageVerifier {
    private static final String TAG = "PackageVerifier";
    private static final boolean DEBUG = false;

    // Lists of package prefixes and names to watch for
    private static final String[] DANGEROUS_PACKAGE_PREFIXES = {
            "com.noshufou.android.su",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser",
            "com.topjohnwu.magisk",
            "de.robv.android.xposed",
            "com.saurik.substrate",
            "com.android.vending.billing.InAppBillingService",
            "com.chelpus",
            "com.xmodgame",
            "org.meowcat.edxposed",
            "com.android.game.hack",
            "com.cih.game_cih",
            "com.dimonvideo",
            "com.cheatdroid",
            "cn.lm.sq",
            "co.kubo.injector",
            "org.sb.sbhack"
    };

    private final Context mContext;
    private final Set<String> mDangerousPackages;
    private final Set<String> mMonitoringPackages;
    
    // Native method declarations
    private native boolean nativeVerifyPackageIntegrity(String packageName);
    private native boolean nativeCheckForDangerousPackages();
    private native void nativeInitPackageVerifier();

    /**
     * Constructor initializes the package verifier
     * @param context Application context
     */
    public PackageVerifier(Context context) {
        mContext = context;
        mDangerousPackages = new HashSet<>();
        mMonitoringPackages = new HashSet<>();
        
        // Initialize the native component
        try {
            nativeInitPackageVerifier();
        } catch (UnsatisfiedLinkError e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to initialize native component", e);
            }
        }
    }

    /**
     * Scans installed packages to identify potentially dangerous applications
     * that could be used for detection or monitoring
     * @return List of potentially dangerous package names
     */
    public List<String> scanForDangerousPackages() {
        List<String> detectedPackages = new ArrayList<>();
        
        try {
            // First attempt native check for better obfuscation
            boolean nativeDetection = false;
            try {
                nativeDetection = nativeCheckForDangerousPackages();
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native package checking unavailable");
                }
            }
            
            // Fall back to Java implementation if native fails or isn't available
            if (!nativeDetection) {
                PackageManager pm = mContext.getPackageManager();
                List<ApplicationInfo> installedApps = pm.getInstalledApplications(0);
                
                for (ApplicationInfo appInfo : installedApps) {
                    String packageName = appInfo.packageName;
                    
                    // Check if the package is in our dangerous list
                    for (String prefix : DANGEROUS_PACKAGE_PREFIXES) {
                        if (packageName.startsWith(prefix)) {
                            detectedPackages.add(packageName);
                            mDangerousPackages.add(packageName);
                            break;
                        }
                    }
                    
                    // Check for other attributes that might indicate monitoring tools
                    if (hasMonitoringCapabilities(appInfo)) {
                        mMonitoringPackages.add(packageName);
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error scanning for dangerous packages", e);
            }
        }
        
        return detectedPackages;
    }
    
    /**
     * Checks if the given application has monitoring capabilities
     * @param appInfo Application information
     * @return True if the application has monitoring capabilities
     */
    private boolean hasMonitoringCapabilities(ApplicationInfo appInfo) {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pkgInfo = pm.getPackageInfo(appInfo.packageName, 
                                                  PackageManager.GET_PERMISSIONS);
            
            if (pkgInfo.requestedPermissions != null) {
                List<String> permissions = Arrays.asList(pkgInfo.requestedPermissions);
                
                // Check for permissions that might indicate monitoring capabilities
                if (permissions.contains("android.permission.SYSTEM_ALERT_WINDOW") &&
                    permissions.contains("android.permission.GET_TASKS") &&
                    permissions.contains("android.permission.READ_LOGS")) {
                    return true;
                }
                
                // Additional checks for specific monitoring capabilities
                if (appInfo.packageName.contains("monitor") || 
                    appInfo.packageName.contains("security") ||
                    appInfo.packageName.contains("firewall")) {
                    
                    if (permissions.contains("android.permission.INTERNET") &&
                        permissions.contains("android.permission.ACCESS_NETWORK_STATE")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking monitoring capabilities", e);
            }
        }
        
        return false;
    }
    
    /**
     * Verifies if the specified package is installed and validates its integrity
     * @param packageName Package name to verify
     * @return True if the package is verified and safe
     */
    public boolean verifyPackage(String packageName) {
        try {
            // First attempt native verification for better security
            try {
                return nativeVerifyPackageIntegrity(packageName);
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native package verification unavailable");
                }
            }
            
            // Fall back to Java implementation
            PackageManager pm = mContext.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 
                                                     PackageManager.GET_SIGNATURES);
            
            // Check if it's a dangerous package
            if (mDangerousPackages.contains(packageName)) {
                return false;
            }
            
            // Check if it's a monitoring package
            if (mMonitoringPackages.contains(packageName)) {
                return false;
            }
            
            // Additional verification could be done here
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error verifying package", e);
            }
            return false;
        }
    }
    
    /**
     * Gets the set of detected dangerous packages
     * @return Set of dangerous package names
     */
    public Set<String> getDangerousPackages() {
        return new HashSet<>(mDangerousPackages);
    }
    
    /**
     * Gets the set of detected monitoring packages
     * @return Set of monitoring package names
     */
    public Set<String> getMonitoringPackages() {
        return new HashSet<>(mMonitoringPackages);
    }
    
    /**
     * Checks if the current environment is secure from monitoring tools
     * @return True if the environment is considered secure
     */
    public boolean isEnvironmentSecure() {
        // Do a full scan
        List<String> detectedPackages = scanForDangerousPackages();
        
        // Environment is considered secure if no dangerous packages are detected
        return detectedPackages.isEmpty() && mMonitoringPackages.isEmpty();
    }
}
