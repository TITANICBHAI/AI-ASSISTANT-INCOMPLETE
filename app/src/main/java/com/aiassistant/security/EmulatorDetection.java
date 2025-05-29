package com.aiassistant.security;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Detects if the application is running in an emulator environment.
 * This class implements various techniques to identify common emulators
 * such as Android Emulator, Genymotion, NOX, BlueStacks, etc.
 */
public class EmulatorDetection {
    private static final String TAG = "EmulatorDetection";
    private static final boolean DEBUG = false;
    
    // Emulator indicators
    private static final String[] KNOWN_EMULATOR_NUMBERS = {
        "15555215554", "15555215556", "15555215558", "15555215560",
        "15555215562", "15555215564", "15555215566", "15555215568",
        "15555215570", "15555215572", "15555215574", "15555215576",
        "15555215578", "15555215580", "15555215582", "15555215584"
    };
    
    private static final String[] KNOWN_DEVICE_IDS = {
        "000000000000000",
        "e21833235b6eef10",
        "012345678912345"
    };
    
    private static final String[] KNOWN_IMSI_IDS = {
        "310260000000000"
    };
    
    private static final String[] KNOWN_PIPES = {
        "/dev/socket/qemud",
        "/dev/qemu_pipe"
    };
    
    private static final String[] KNOWN_FILES = {
        "/system/lib/libc_malloc_debug_qemu.so",
        "/sys/qemu_trace",
        "/system/bin/qemu-props"
    };
    
    private static final String[] KNOWN_GENY_FILES = {
        "/dev/socket/genyd",
        "/dev/socket/baseband_genyd"
    };
    
    private static final String[] KNOWN_QEMU_DRIVERS = {
        "goldfish"
    };
    
    // Native method declarations
    private native boolean nativeDetectEmulator();
    private native void nativeInitEmulatorDetection();

    private final Context mContext;

    /**
     * Constructor initializes the emulator detection
     * @param context Application context
     */
    public EmulatorDetection(Context context) {
        mContext = context;
        
        // Initialize native components
        try {
            nativeInitEmulatorDetection();
        } catch (UnsatisfiedLinkError e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to initialize native emulator detection", e);
            }
        }
    }

    /**
     * Checks if the application is running in an emulator
     * @return True if emulator is detected
     */
    public boolean isEmulator() {
        try {
            // First try native detection for better obfuscation
            try {
                return nativeDetectEmulator();
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native emulator detection unavailable");
                }
            }
            
            // Fall back to Java implementation
            // Perform multiple checks
            int score = 0;
            
            // Check Build properties
            if (checkBuildProperties()) {
                score += 3;
            }
            
            // Check telephony
            if (checkTelephony()) {
                score += 2;
            }
            
            // Check system files
            if (checkFiles()) {
                score += 2;
            }
            
            // Check hardware
            if (checkHardware()) {
                score += 2;
            }
            
            // More specific checks
            if (checkQEMU()) {
                score += 3;
            }
            
            if (checkGenymotion()) {
                score += 3;
            }
            
            // Consider it an emulator if score is high enough
            return score >= 4;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error in emulator detection", e);
            }
            return false;
        }
    }

    /**
     * Gets a list of detected emulator indicators
     * @return List of detected indicators
     */
    public List<String> getEmulatorIndicators() {
        List<String> indicators = new ArrayList<>();
        
        try {
            // Check Build properties
            if (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.contains("vbox") ||
                Build.FINGERPRINT.contains("sdk_gphone")) {
                indicators.add("Suspicious build fingerprint: " + Build.FINGERPRINT);
            }
            
            if (Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK")) {
                indicators.add("Suspicious build model: " + Build.MODEL);
            }
            
            if (Build.MANUFACTURER.contains("Genymotion") ||
                "unknown".equalsIgnoreCase(Build.MANUFACTURER)) {
                indicators.add("Suspicious manufacturer: " + Build.MANUFACTURER);
            }
            
            if (Build.BRAND.contains("generic") || Build.BRAND.contains("android")) {
                indicators.add("Suspicious brand: " + Build.BRAND);
            }
            
            if (Build.DEVICE.contains("generic") || Build.DEVICE.contains("vbox")) {
                indicators.add("Suspicious device: " + Build.DEVICE);
            }
            
            if (Build.PRODUCT.contains("sdk") || 
                Build.PRODUCT.contains("vbox") ||
                Build.PRODUCT.contains("emulator")) {
                indicators.add("Suspicious product: " + Build.PRODUCT);
            }
            
            if (Build.HARDWARE.contains("goldfish") || 
                Build.HARDWARE.contains("ranchu") ||
                Build.HARDWARE.contains("vbox")) {
                indicators.add("Suspicious hardware: " + Build.HARDWARE);
            }
            
            // Check telephony
            TelephonyManager tm = 
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            
            if (tm != null) {
                String phoneNumber = tm.getLine1Number();
                for (String number : KNOWN_EMULATOR_NUMBERS) {
                    if (number.equals(phoneNumber)) {
                        indicators.add("Emulator phone number detected: " + phoneNumber);
                        break;
                    }
                }
                
                String deviceId = "";
                try {
                    if (mContext.checkCallingOrSelfPermission(
                            android.Manifest.permission.READ_PHONE_STATE) 
                            == PackageManager.PERMISSION_GRANTED) {
                        deviceId = tm.getDeviceId();
                    }
                } catch (Exception e) {
                    // Ignore permission issues
                }
                
                if (deviceId != null) {
                    for (String id : KNOWN_DEVICE_IDS) {
                        if (id.equals(deviceId)) {
                            indicators.add("Emulator device ID detected: " + deviceId);
                            break;
                        }
                    }
                }
                
                String subscriberId = "";
                try {
                    if (mContext.checkCallingOrSelfPermission(
                            android.Manifest.permission.READ_PHONE_STATE) 
                            == PackageManager.PERMISSION_GRANTED) {
                        subscriberId = tm.getSubscriberId();
                    }
                } catch (Exception e) {
                    // Ignore permission issues
                }
                
                if (subscriberId != null) {
                    for (String id : KNOWN_IMSI_IDS) {
                        if (id.equals(subscriberId)) {
                            indicators.add("Emulator IMSI detected: " + subscriberId);
                            break;
                        }
                    }
                }
            }
            
            // Check files
            for (String pipe : KNOWN_PIPES) {
                if (new File(pipe).exists()) {
                    indicators.add("Emulator pipe detected: " + pipe);
                }
            }
            
            for (String file : KNOWN_FILES) {
                if (new File(file).exists()) {
                    indicators.add("Emulator file detected: " + file);
                }
            }
            
            for (String file : KNOWN_GENY_FILES) {
                if (new File(file).exists()) {
                    indicators.add("Genymotion file detected: " + file);
                }
            }
            
            // Check hardware through /proc/cpuinfo
            File cpuInfo = new File("/proc/cpuinfo");
            if (cpuInfo.exists()) {
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(cpuInfo));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        for (String driver : KNOWN_QEMU_DRIVERS) {
                            if (line.contains(driver)) {
                                indicators.add("QEMU driver detected: " + line);
                                break;
                            }
                        }
                    }
                    reader.close();
                } catch (Exception e) {
                    // Ignore file read errors
                }
            }
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error getting emulator indicators", e);
            }
        }
        
        return indicators;
    }

    /**
     * Checks build properties for emulator indicators
     * @return True if emulator is detected
     */
    private boolean checkBuildProperties() {
        try {
            return (Build.FINGERPRINT.startsWith("generic") ||
                    Build.FINGERPRINT.contains("vbox") ||
                    Build.FINGERPRINT.contains("sdk_gphone") ||
                    Build.MODEL.contains("google_sdk") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.MODEL.contains("Android SDK") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    "unknown".equalsIgnoreCase(Build.MANUFACTURER) ||
                    Build.BRAND.contains("generic") ||
                    Build.BRAND.contains("android") ||
                    Build.DEVICE.contains("generic") ||
                    Build.DEVICE.contains("vbox") ||
                    Build.PRODUCT.contains("sdk") ||
                    Build.PRODUCT.contains("vbox") ||
                    Build.PRODUCT.contains("emulator") ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu") ||
                    Build.HARDWARE.contains("vbox"));
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking build properties", e);
            }
            return false;
        }
    }

    /**
     * Checks telephony information for emulator indicators
     * @return True if emulator is detected
     */
    private boolean checkTelephony() {
        try {
            TelephonyManager tm = 
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            
            if (tm == null) {
                return false;
            }
            
            String phoneNumber = tm.getLine1Number();
            
            if (phoneNumber != null) {
                for (String number : KNOWN_EMULATOR_NUMBERS) {
                    if (number.equals(phoneNumber)) {
                        return true;
                    }
                }
            }
            
            // Check device ID
            String deviceId = "";
            try {
                if (mContext.checkCallingOrSelfPermission(
                        android.Manifest.permission.READ_PHONE_STATE) 
                        == PackageManager.PERMISSION_GRANTED) {
                    deviceId = tm.getDeviceId();
                }
            } catch (Exception e) {
                // Ignore permission issues
            }
            
            if (deviceId != null) {
                for (String id : KNOWN_DEVICE_IDS) {
                    if (id.equals(deviceId)) {
                        return true;
                    }
                }
            }
            
            // Check IMSI
            String subscriberId = "";
            try {
                if (mContext.checkCallingOrSelfPermission(
                        android.Manifest.permission.READ_PHONE_STATE) 
                        == PackageManager.PERMISSION_GRANTED) {
                    subscriberId = tm.getSubscriberId();
                }
            } catch (Exception e) {
                // Ignore permission issues
            }
            
            if (subscriberId != null) {
                for (String id : KNOWN_IMSI_IDS) {
                    if (id.equals(subscriberId)) {
                        return true;
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking telephony", e);
            }
            return false;
        }
    }

    /**
     * Checks for emulator-specific files
     * @return True if emulator is detected
     */
    private boolean checkFiles() {
        try {
            // Check for known pipes
            for (String pipe : KNOWN_PIPES) {
                if (new File(pipe).exists()) {
                    return true;
                }
            }
            
            // Check for known files
            for (String file : KNOWN_FILES) {
                if (new File(file).exists()) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking files", e);
            }
            return false;
        }
    }

    /**
     * Checks hardware information for emulator indicators
     * @return True if emulator is detected
     */
    private boolean checkHardware() {
        try {
            File cpuInfo = new File("/proc/cpuinfo");
            if (cpuInfo.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(cpuInfo));
                String line;
                while ((line = reader.readLine()) != null) {
                    for (String driver : KNOWN_QEMU_DRIVERS) {
                        if (line.contains(driver)) {
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
                Log.e(TAG, "Error checking hardware", e);
            }
            return false;
        }
    }

    /**
     * Specific check for QEMU emulator
     * @return True if QEMU is detected
     */
    private boolean checkQEMU() {
        try {
            // Check qemu properties file
            File qemuProps = new File("/system/build.prop");
            if (qemuProps.exists()) {
                Properties properties = new Properties();
                InputStream is = new FileInputStream(qemuProps);
                properties.load(is);
                is.close();
                
                if (properties.containsKey("ro.kernel.qemu") && 
                    "1".equals(properties.getProperty("ro.kernel.qemu"))) {
                    return true;
                }
            }
            
            // Check for QEMU-specific environment variables
            if (System.getenv("ANDROID_SDK_HOME") != null ||
                System.getenv("ANDROID_ROOT") != null ||
                System.getenv("ANDROID_DATA") != null) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking QEMU", e);
            }
            return false;
        }
    }

    /**
     * Specific check for Genymotion emulator
     * @return True if Genymotion is detected
     */
    private boolean checkGenymotion() {
        try {
            // Check for Genymotion-specific files
            for (String file : KNOWN_GENY_FILES) {
                if (new File(file).exists()) {
                    return true;
                }
            }
            
            // Check for Genymotion in manufacturer
            if (Build.MANUFACTURER.contains("Genymotion")) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking Genymotion", e);
            }
            return false;
        }
    }
    
    /**
     * Gets detailed emulator information if detected
     * @return String with detailed information, or empty if not an emulator
     */
    public String getEmulatorDetails() {
        if (!isEmulator()) {
            return "No emulator detected";
        }
        
        try {
            StringBuilder details = new StringBuilder("Emulator detected:\n");
            
            details.append("Build.FINGERPRINT: ").append(Build.FINGERPRINT).append("\n");
            details.append("Build.MODEL: ").append(Build.MODEL).append("\n");
            details.append("Build.MANUFACTURER: ").append(Build.MANUFACTURER).append("\n");
            details.append("Build.BRAND: ").append(Build.BRAND).append("\n");
            details.append("Build.DEVICE: ").append(Build.DEVICE).append("\n");
            details.append("Build.PRODUCT: ").append(Build.PRODUCT).append("\n");
            details.append("Build.HARDWARE: ").append(Build.HARDWARE).append("\n");
            
            // Add specific indicators
            List<String> indicators = getEmulatorIndicators();
            details.append("\nDetected Indicators:\n");
            for (String indicator : indicators) {
                details.append("- ").append(indicator).append("\n");
            }
            
            return details.toString();
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error getting emulator details", e);
            }
            return "Error getting emulator details";
        }
    }
}
