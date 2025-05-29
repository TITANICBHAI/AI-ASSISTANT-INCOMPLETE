package com.aiassistant.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Detects hooking frameworks and method hooking attempts.
 * This class can identify common hooking frameworks like Xposed, Frida,
 * Substrate, and other runtime manipulation tools.
 */
public class HookDetection {
    private static final String TAG = "HookDetection";
    private static final boolean DEBUG = false;
    
    // Known hooking frameworks
    private static final String[] XPOSED_PACKAGES = {
        "de.robv.android.xposed.installer",
        "org.meowcat.edxposed.manager",
        "org.lsposed.manager",
        "com.solohsu.android.edxp.manager",
        "com.topjohnwu.magisk"
    };
    
    private static final String[] XPOSED_FILES = {
        "/system/framework/XposedBridge.jar",
        "/system/lib/libxposed_art.so",
        "/system/lib64/libxposed_art.so",
        "/system/xposed.prop",
        "/system/lib/modules/modules.dep",
        "/proc/mounts"  // For checking xposed mounted paths
    };
    
    private static final String[] FRIDA_PACKAGES = {
        "com.frida.fridagadget",
        "com.saurik.substrate"
    };
    
    private static final String[] FRIDA_FILES = {
        "/data/local/tmp/frida-server",
        "/data/local/tmp/re.frida.server",
        "/system/lib/libfrida-gadget.so",
        "/system/lib64/libfrida-gadget.so",
        "/system/bin/frida-server"
    };
    
    private static final String[] SUBSTRATE_FILES = {
        "/system/lib/libsubstrate.so",
        "/system/lib64/libsubstrate.so",
        "/data/app/com.saurik.substrate"
    };
    
    // Native method declarations
    private native boolean nativeDetectHooks();
    private native boolean nativeDetectXposed();
    private native boolean nativeDetectFrida();
    private native boolean nativeDetectSubstrate();
    private native boolean nativeCheckMethodHooked(String className, String methodName);

    private final Context mContext;
    private final Set<String> mDetectedFrameworks;

    /**
     * Constructor initializes the hook detection
     * @param context Application context
     */
    public HookDetection(Context context) {
        mContext = context;
        mDetectedFrameworks = new HashSet<>();
    }

    /**
     * Performs a complete scan for hooking frameworks
     * @return True if any hooking framework is detected
     */
    public boolean detectHooks() {
        try {
            // First try native detection for better obfuscation
            try {
                return nativeDetectHooks();
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native hook detection unavailable");
                }
            }
            
            // Clear previous detections
            mDetectedFrameworks.clear();
            
            // Check for various hooking frameworks
            boolean xposedDetected = detectXposed();
            boolean fridaDetected = detectFrida();
            boolean substrateDetected = detectSubstrate();
            boolean otherHooksDetected = detectOtherHooks();
            
            return xposedDetected || fridaDetected || substrateDetected || otherHooksDetected;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error detecting hooks", e);
            }
            return false;
        }
    }

    /**
     * Gets a list of detected hooking frameworks
     * @return Set of detected framework names
     */
    public Set<String> getDetectedFrameworks() {
        return new HashSet<>(mDetectedFrameworks);
    }

    /**
     * Detects Xposed framework
     * @return True if Xposed is detected
     */
    public boolean detectXposed() {
        try {
            // First try native detection
            try {
                return nativeDetectXposed();
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native Xposed detection unavailable");
                }
            }
            
            // Check for Xposed packages
            PackageManager pm = mContext.getPackageManager();
            for (String packageName : XPOSED_PACKAGES) {
                try {
                    ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                    if (info != null) {
                        mDetectedFrameworks.add("Xposed (Package: " + packageName + ")");
                        return true;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // Package not found, continue checking
                }
            }
            
            // Check for Xposed files
            for (String filePath : XPOSED_FILES) {
                File file = new File(filePath);
                if (file.exists()) {
                    if (filePath.equals("/proc/mounts")) {
                        // Special check for mounts
                        if (checkMountsForXposed(file)) {
                            mDetectedFrameworks.add("Xposed (Mount: /proc/mounts)");
                            return true;
                        }
                    } else {
                        mDetectedFrameworks.add("Xposed (File: " + filePath + ")");
                        return true;
                    }
                }
            }
            
            // Check for XposedBridge class
            try {
                Class.forName("de.robv.android.xposed.XposedBridge", false, 
                               ClassLoader.getSystemClassLoader());
                mDetectedFrameworks.add("Xposed (Class: XposedBridge)");
                return true;
            } catch (ClassNotFoundException e) {
                // Class not found, not detected
            }
            
            // Check for Xposed stack trace
            try {
                throw new Exception("Xposed stack trace check");
            } catch (Exception e) {
                StackTraceElement[] stackTrace = e.getStackTrace();
                for (StackTraceElement element : stackTrace) {
                    if (element.getClassName().contains("xposed") || 
                        element.getClassName().contains("XposedBridge")) {
                        mDetectedFrameworks.add("Xposed (StackTrace: " + 
                                               element.getClassName() + ")");
                        return true;
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error detecting Xposed", e);
            }
            return false;
        }
    }

    /**
     * Checks mount points for Xposed-related paths
     * @param mountsFile /proc/mounts file
     * @return True if Xposed mounts are detected
     */
    private boolean checkMountsForXposed(File mountsFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mountsFile));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("xposed") || line.contains("Xposed") || 
                    line.contains("magisk") || line.contains("Magisk")) {
                    reader.close();
                    return true;
                }
            }
            reader.close();
            return false;
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking mounts", e);
            }
            return false;
        }
    }

    /**
     * Detects Frida framework
     * @return True if Frida is detected
     */
    public boolean detectFrida() {
        try {
            // First try native detection
            try {
                return nativeDetectFrida();
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native Frida detection unavailable");
                }
            }
            
            // Check for Frida packages
            PackageManager pm = mContext.getPackageManager();
            for (String packageName : FRIDA_PACKAGES) {
                try {
                    ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                    if (info != null) {
                        mDetectedFrameworks.add("Frida (Package: " + packageName + ")");
                        return true;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // Package not found, continue checking
                }
            }
            
            // Check for Frida files
            for (String filePath : FRIDA_FILES) {
                File file = new File(filePath);
                if (file.exists()) {
                    mDetectedFrameworks.add("Frida (File: " + filePath + ")");
                    return true;
                }
            }
            
            // Check for Frida ports
            if (checkFridaPorts()) {
                mDetectedFrameworks.add("Frida (Open ports)");
                return true;
            }
            
            // Check for suspicious libraries
            if (checkLoadedLibraries()) {
                mDetectedFrameworks.add("Frida (Suspicious libraries)");
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error detecting Frida", e);
            }
            return false;
        }
    }

    /**
     * Checks if known Frida ports are open
     * @return True if Frida ports are detected
     */
    private boolean checkFridaPorts() {
        try {
            // Common Frida ports
            int[] fridaPorts = {27042, 27043};
            
            // Check /proc/net/tcp for open ports
            File tcpFile = new File("/proc/net/tcp");
            if (tcpFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(tcpFile));
                String line;
                // Skip header
                reader.readLine();
                
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length > 2) {
                        String localAddr = parts[1];
                        // Extract port from address (last 4 hex digits)
                        if (localAddr.length() > 4) {
                            try {
                                String portHex = localAddr.substring(localAddr.length() - 4);
                                int port = Integer.parseInt(portHex, 16);
                                
                                for (int fridaPort : fridaPorts) {
                                    if (port == fridaPort) {
                                        reader.close();
                                        return true;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                // Ignore parse errors
                            }
                        }
                    }
                }
                reader.close();
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking Frida ports", e);
            }
            return false;
        }
    }

    /**
     * Checks loaded libraries for suspicious names
     * @return True if suspicious libraries are detected
     */
    private boolean checkLoadedLibraries() {
        try {
            // Keywords to look for in loaded libraries
            String[] suspiciousLibs = {"frida", "gum", "gadget", "inject"};
            
            // Attempt to use Java's Runtime to get loaded libraries
            Method getLoadedLibraries = null;
            try {
                getLoadedLibraries = Runtime.class.getDeclaredMethod("getLoadedLibraries");
                getLoadedLibraries.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // Method not available, try a different approach
            }
            
            if (getLoadedLibraries != null) {
                try {
                    List<String> libraries = (List<String>) getLoadedLibraries.invoke(Runtime.getRuntime());
                    
                    for (String lib : libraries) {
                        for (String suspiciousLib : suspiciousLibs) {
                            if (lib.toLowerCase().contains(suspiciousLib)) {
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore invocation errors
                }
            }
            
            // If the above method fails, try checking /proc/self/maps
            File mapsFile = new File("/proc/self/maps");
            if (mapsFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(mapsFile));
                String line;
                
                while ((line = reader.readLine()) != null) {
                    for (String suspiciousLib : suspiciousLibs) {
                        if (line.toLowerCase().contains(suspiciousLib)) {
                            reader.close();
                            return true;
                        }
                    }
                }
                reader.close();
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking loaded libraries", e);
            }
            return false;
        }
    }

    /**
     * Detects Substrate framework
     * @return True if Substrate is detected
     */
    public boolean detectSubstrate() {
        try {
            // First try native detection
            try {
                return nativeDetectSubstrate();
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native Substrate detection unavailable");
                }
            }
            
            // Check for Substrate packages
            PackageManager pm = mContext.getPackageManager();
            try {
                ApplicationInfo info = pm.getApplicationInfo("com.saurik.substrate", 0);
                if (info != null) {
                    mDetectedFrameworks.add("Substrate (Package)");
                    return true;
                }
            } catch (PackageManager.NameNotFoundException e) {
                // Package not found, continue checking
            }
            
            // Check for Substrate files
            for (String filePath : SUBSTRATE_FILES) {
                File file = new File(filePath);
                if (file.exists()) {
                    mDetectedFrameworks.add("Substrate (File: " + filePath + ")");
                    return true;
                }
            }
            
            // Check for Substrate class
            try {
                Class.forName("com.saurik.substrate.MS", false, 
                               ClassLoader.getSystemClassLoader());
                mDetectedFrameworks.add("Substrate (Class)");
                return true;
            } catch (ClassNotFoundException e) {
                // Class not found, not detected
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error detecting Substrate", e);
            }
            return false;
        }
    }

    /**
     * Detects other hooking methods not covered by specific framework checks
     * @return True if other hooks are detected
     */
    public boolean detectOtherHooks() {
        try {
            // Check for changes to system properties
            if (detectSystemPropertyHooks()) {
                mDetectedFrameworks.add("System property hooks");
                return true;
            }
            
            // Check for runtime hooking
            if (detectRuntimeHooks()) {
                mDetectedFrameworks.add("Runtime hooks");
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error detecting other hooks", e);
            }
            return false;
        }
    }

    /**
     * Detects hooks in system properties
     * @return True if system property hooks are detected
     */
    private boolean detectSystemPropertyHooks() {
        try {
            // Check if ro.debuggable is set to 1
            String debuggable = getProp("ro.debuggable");
            if ("1".equals(debuggable)) {
                return true;
            }
            
            // Check if ro.secure is set to 0
            String secure = getProp("ro.secure");
            if ("0".equals(secure)) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error detecting system property hooks", e);
            }
            return false;
        }
    }

    /**
     * Gets a system property value
     * @param propName Property name to get
     * @return Property value, or empty string if not found
     */
    private String getProp(String propName) {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method getProp = systemProperties.getMethod("get", String.class);
            return (String) getProp.invoke(null, propName);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Detects hooks in runtime environment
     * @return True if runtime hooks are detected
     */
    private boolean detectRuntimeHooks() {
        try {
            // Check if the Runtime class has been tampered with
            try {
                // Create a test exception to get a stack trace
                Exception exception = new Exception("Hook detection");
                StackTraceElement[] stackTrace = exception.getStackTrace();
                
                // Check for unusual elements in the stack trace
                for (StackTraceElement element : stackTrace) {
                    String className = element.getClassName();
                    
                    if (className.contains("reflect") && className.contains("Method")) {
                        // This is not a definitive indication, but unusual in normal operation
                        if (DEBUG) {
                            Log.d(TAG, "Reflection method in stack trace: " + className);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
            
            // Check for particular methods being hooked
            if (isMethodHooked("java.lang.Runtime", "exec")) {
                return true;
            }
            
            if (isMethodHooked("android.app.ActivityManager", "getRunningServices")) {
                return true;
            }
            
            if (isMethodHooked("android.app.ActivityManager", "getRunningAppProcesses")) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error detecting runtime hooks", e);
            }
            return false;
        }
    }

    /**
     * Checks if a specific method has been hooked
     * @param className Class name containing the method
     * @param methodName Method name to check
     * @return True if the method appears to be hooked
     */
    public boolean isMethodHooked(String className, String methodName) {
        try {
            // First try native detection
            try {
                return nativeCheckMethodHooked(className, methodName);
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native method hook detection unavailable");
                }
            }
            
            // Java detection is less reliable but can try some heuristics
            try {
                Class<?> clazz = Class.forName(className);
                Method[] methods = clazz.getDeclaredMethods();
                
                for (Method method : methods) {
                    if (method.getName().equals(methodName)) {
                        // We found the method, now check for signs of hooking
                        
                        // Basic check: look at the declaring class
                        if (!method.getDeclaringClass().getName().equals(className)) {
                            // Method's declaring class doesn't match expected class
                            return true;
                        }
                        
                        return false;
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking method hooks", e);
            }
            return false;
        }
    }
    
    /**
     * Gets a detailed report of hook detection results
     * @return String with detailed hooking information
     */
    public String getHookingReport() {
        try {
            StringBuilder report = new StringBuilder();
            
            // Run detection
            boolean hooksDetected = detectHooks();
            
            report.append("Hook Detection Report\n");
            report.append("====================\n\n");
            
            report.append("Hooks Detected: ").append(hooksDetected ? "YES" : "NO").append("\n\n");
            
            if (hooksDetected) {
                report.append("Detected Frameworks:\n");
                for (String framework : mDetectedFrameworks) {
                    report.append("- ").append(framework).append("\n");
                }
                report.append("\n");
            }
            
            // Add specific framework checks
            report.append("Framework Checks:\n");
            report.append("- Xposed: ").append(detectXposed() ? "DETECTED" : "Not found").append("\n");
            report.append("- Frida: ").append(detectFrida() ? "DETECTED" : "Not found").append("\n");
            report.append("- Substrate: ").append(detectSubstrate() ? "DETECTED" : "Not found").append("\n");
            report.append("- Other Hooks: ").append(detectOtherHooks() ? "DETECTED" : "Not found").append("\n\n");
            
            // Common method hook checks
            report.append("Method Hook Checks:\n");
            report.append("- Runtime.exec: ").
                append(isMethodHooked("java.lang.Runtime", "exec") ? "HOOKED" : "Clean").append("\n");
            report.append("- ActivityManager.getRunningServices: ").
                append(isMethodHooked("android.app.ActivityManager", "getRunningServices") ? 
                      "HOOKED" : "Clean").append("\n");
            report.append("- PackageManager.getInstalledApplications: ").
                append(isMethodHooked("android.content.pm.PackageManager", "getInstalledApplications") ? 
                      "HOOKED" : "Clean").append("\n");
            
            return report.toString();
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error generating hooking report", e);
            }
            return "Error generating hooking report";
        }
    }
}
