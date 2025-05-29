package com.aiassistant.ai.features.integration;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class for managing permissions required for integration features
 */
public class PermissionsHelper {
    private static final String TAG = "PermissionsHelper";
    
    // Permission request codes
    public static final int REQUEST_MESSAGING_PERMISSIONS = 100;
    public static final int REQUEST_CALL_PERMISSIONS = 101;
    public static final int REQUEST_APP_PERMISSIONS = 102;
    
    // Required permissions
    private static final String[] MESSAGING_PERMISSIONS = {
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS
    };
    
    private static final String[] CALL_PERMISSIONS = {
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS
    };
    
    private static final String[] APP_PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    /**
     * Check if all messaging permissions are granted
     * @param context Android context
     * @return True if all permissions granted
     */
    public static boolean hasMessagingPermissions(Context context) {
        return hasPermissions(context, MESSAGING_PERMISSIONS);
    }
    
    /**
     * Check if all call permissions are granted
     * @param context Android context
     * @return True if all permissions granted
     */
    public static boolean hasCallPermissions(Context context) {
        return hasPermissions(context, CALL_PERMISSIONS);
    }
    
    /**
     * Check if all app control permissions are granted
     * @param context Android context
     * @return True if all permissions granted
     */
    public static boolean hasAppPermissions(Context context) {
        return hasPermissions(context, APP_PERMISSIONS);
    }
    
    /**
     * Check if specific permissions are granted
     * @param context Android context
     * @param permissions Array of permission strings
     * @return True if all permissions granted
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Request messaging permissions
     * @param activity Activity to request permissions from
     */
    public static void requestMessagingPermissions(Activity activity) {
        requestPermissions(activity, REQUEST_MESSAGING_PERMISSIONS, MESSAGING_PERMISSIONS);
    }
    
    /**
     * Request call permissions
     * @param activity Activity to request permissions from
     */
    public static void requestCallPermissions(Activity activity) {
        requestPermissions(activity, REQUEST_CALL_PERMISSIONS, CALL_PERMISSIONS);
    }
    
    /**
     * Request app control permissions
     * @param activity Activity to request permissions from
     */
    public static void requestAppPermissions(Activity activity) {
        requestPermissions(activity, REQUEST_APP_PERMISSIONS, APP_PERMISSIONS);
    }
    
    /**
     * Request specific permissions
     * @param activity Activity to request permissions from
     * @param requestCode Request code for callback
     * @param permissions Array of permission strings
     */
    public static void requestPermissions(Activity activity, int requestCode, String... permissions) {
        List<String> permissionsToRequest = new ArrayList<>();
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toArray(new String[0]),
                requestCode
            );
            Log.i(TAG, "Requesting permissions: " + permissionsToRequest);
        }
    }
    
    /**
     * Handle permission request results
     * @param requestCode Request code from onRequestPermissionsResult
     * @param permissions Permission strings from onRequestPermissionsResult
     * @param grantResults Grant results from onRequestPermissionsResult
     * @return True if all requested permissions were granted
     */
    public static boolean handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        Log.i(TAG, "Permission request " + requestCode + " result: " + 
              (allGranted ? "All granted" : "Some denied") +
              " - " + Arrays.toString(permissions));
        
        return allGranted;
    }
}
