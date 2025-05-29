package com.aiassistant.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Access control system for securing key components
 */
public class AccessControl {
    private static final String TAG = "AccessControl";
    
    // Access permission levels
    public enum PermissionLevel {
        READ_ONLY,
        READ_WRITE,
        FULL_ACCESS,
        ADMIN
    }
    
    // Security zones
    public enum SecurityZone {
        SYSTEM_CORE,
        AI_STATE,
        ANALYTICS,
        EXTERNAL_COMMUNICATION,
        SETTINGS
    }

    private final Context context;
    private final Set<String> authorizedPackages;
    private final Set<Integer> authorizedCallerUids;
    private final Set<String> adminPackages;
    private boolean strictMode = true;
    
    /**
     * Constructor
     * @param context Application context
     */
    public AccessControl(Context context) {
        this.context = context;
        this.authorizedPackages = new HashSet<>();
        this.authorizedCallerUids = new HashSet<>();
        this.adminPackages = new HashSet<>();
        
        // Add this app's package to authorized list
        authorizedPackages.add(context.getPackageName());
        
        // Add this process's UID to authorized list
        authorizedCallerUids.add(Process.myUid());
        
        // Self is admin
        adminPackages.add(context.getPackageName());
        
        // Initialize the access control system
        initialize();
    }
    
    /**
     * Initialize the access control system
     */
    private void initialize() {
        Log.d(TAG, "Initializing access control system");
        
        // You can add specific system packages or UIDs that need access
        // For example, allowing specific system services
        // authorizedPackages.add("com.android.systemui");
    }
    
    /**
     * Add an authorized package
     * @param packageName Package name to authorize
     * @return True if added
     */
    public boolean addAuthorizedPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        
        // Verify package exists
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            authorizedPackages.add(packageName);
            authorizedCallerUids.add(info.uid);
            Log.d(TAG, "Authorized package added: " + packageName);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Attempted to authorize non-existent package: " + packageName);
            return false;
        }
    }
    
    /**
     * Add admin package
     * @param packageName Package name to add as admin
     * @return True if added
     */
    public boolean addAdminPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        
        // Verify caller has admin rights
        if (!isCallerAdmin()) {
            Log.w(TAG, "Non-admin attempted to add admin package: " + packageName);
            return false;
        }
        
        // Verify package exists
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            adminPackages.add(packageName);
            // Admin packages are also authorized
            addAuthorizedPackage(packageName);
            Log.d(TAG, "Admin package added: " + packageName);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Attempted to add non-existent admin package: " + packageName);
            return false;
        }
    }
    
    /**
     * Set strict mode
     * @param strict Whether to use strict mode
     */
    public void setStrictMode(boolean strict) {
        // Only admin can change strict mode
        if (!isCallerAdmin()) {
            Log.w(TAG, "Non-admin attempted to change strict mode");
            return;
        }
        
        this.strictMode = strict;
        Log.d(TAG, "Strict mode set to: " + strict);
    }
    
    /**
     * Check if caller is authorized
     * @return True if caller is authorized
     */
    public boolean isCallerAuthorized() {
        int callingUid = getCallingUid();
        String callingPackage = getCallingPackage();
        
        // Check UID
        if (authorizedCallerUids.contains(callingUid)) {
            return true;
        }
        
        // Check package
        return callingPackage != null && authorizedPackages.contains(callingPackage);
    }
    
    /**
     * Check if caller is admin
     * @return True if caller is admin
     */
    public boolean isCallerAdmin() {
        String callingPackage = getCallingPackage();
        return callingPackage != null && adminPackages.contains(callingPackage);
    }
    
    /**
     * Check if caller has required permission for zone
     * @param zone Security zone
     * @param requiredLevel Required permission level
     * @return True if permitted
     */
    public boolean checkPermission(SecurityZone zone, PermissionLevel requiredLevel) {
        // First check if caller is authorized at all
        if (!isCallerAuthorized()) {
            Log.w(TAG, "Unauthorized access attempt to zone: " + zone);
            return false;
        }
        
        // Admin has all permissions
        if (isCallerAdmin()) {
            return true;
        }
        
        // For now simple permissions - can be expanded to more fine-grained control
        switch (zone) {
            case SYSTEM_CORE:
                // Only admin can access system core in strict mode
                return !strictMode || requiredLevel == PermissionLevel.READ_ONLY;
                
            case AI_STATE:
                // Most components need to read AI state but write access is more limited
                return requiredLevel == PermissionLevel.READ_ONLY || 
                       requiredLevel == PermissionLevel.READ_WRITE;
                
            case ANALYTICS:
                // Analytics can be accessed by most components
                return true;
                
            case EXTERNAL_COMMUNICATION:
                // External communication needs to be controlled
                return requiredLevel != PermissionLevel.ADMIN;
                
            case SETTINGS:
                // Settings changes need proper authorization
                return requiredLevel == PermissionLevel.READ_ONLY || isCallerAdmin();
                
            default:
                // Unknown zone
                Log.w(TAG, "Unknown security zone: " + zone);
                return false;
        }
    }
    
    /**
     * Get calling UID
     * @return Calling UID
     */
    private int getCallingUid() {
        return Process.myUid(); // In production would use Binder.getCallingUid()
    }
    
    /**
     * Get calling package
     * @return Calling package name
     */
    private String getCallingPackage() {
        // In production, would use a method to determine actual calling package
        // This is a simplified version for demonstration
        return context.getPackageName();
    }
    
    /**
     * Log access check
     * @param zone Security zone
     * @param level Permission level
     * @param granted Whether access was granted
     */
    public void logAccessCheck(SecurityZone zone, PermissionLevel level, boolean granted) {
        if (granted) {
            Log.d(TAG, "Access granted to zone: " + zone + " at level: " + level);
        } else {
            Log.w(TAG, "Access denied to zone: " + zone + " at level: " + level + 
                  " for package: " + getCallingPackage());
        }
    }
}
