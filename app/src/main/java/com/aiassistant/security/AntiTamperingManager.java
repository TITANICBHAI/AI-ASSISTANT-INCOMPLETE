package com.aiassistant.security;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implements advanced anti-tampering techniques to protect the application
 * against modification, repackaging and code injection.
 */
public class AntiTamperingManager {
    private static final String TAG = "AntiTamperingManager";
    
    // Tampering detection methods
    public enum TamperingCheckMethod {
        APP_SIGNATURE,
        INSTALLER_VERIFICATION,
        DEX_INTEGRITY,
        NATIVE_LIBRARY_INTEGRITY,
        RESOURCES_INTEGRITY,
        DEBUG_FLAGS
    }
    
    private final Context context;
    private final Handler mainHandler;
    private final ScheduledExecutorService scheduler;
    
    // Configuration
    private int checkIntensity = 3; // 1-5 scale
    private boolean isRunning = false;
    
    // Active check methods
    private final Map<TamperingCheckMethod, Boolean> activeChecks;
    
    // Integrity checksums (would be populated during build process)
    private final Map<String, String> expectedChecksums;
    
    // Registered listeners
    private final List<TamperingListener> listeners;
    
    // Integrity status
    private boolean integrityVerified = false;
    private String lastFailureReason = null;
    
    /**
     * Constructor
     * @param context Application context
     */
    public AntiTamperingManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.activeChecks = new HashMap<>();
        this.expectedChecksums = new HashMap<>();
        this.listeners = new ArrayList<>();
        
        // Initialize check methods
        for (TamperingCheckMethod method : TamperingCheckMethod.values()) {
            activeChecks.put(method, false);
        }
        
        // Initialize expected checksums (would come from a secure source in production)
        initializeExpectedChecksums();
    }
    
    /**
     * Initialize expected checksums
     */
    private void initializeExpectedChecksums() {
        // In a real implementation, these would be generated during build
        // and stored in an obfuscated way
        expectedChecksums.put("classes.dex", "0123456789abcdef0123456789abcdef");
        expectedChecksums.put("lib/arm64-v8a/libnative.so", "fedcba9876543210fedcba9876543210");
        
        // Initialize app signature
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            if (info.signatures != null && info.signatures.length > 0) {
                String signatureHash = calculateSignatureHash(info.signatures[0].toByteArray());
                expectedChecksums.put("app_signature", signatureHash);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize signature checksum", e);
        }
    }
    
    /**
     * Start integrity monitoring
     */
    public void start() {
        if (isRunning) {
            return;
        }
        
        Log.d(TAG, "Starting anti-tampering system with intensity " + checkIntensity);
        
        // Apply configuration based on intensity
        applyIntensityConfiguration();
        
        // Perform initial integrity check
        performIntegrityCheck();
        
        // Schedule periodic checks
        Random random = new Random();
        int baseInterval = 30 + random.nextInt(30); // 30-60 seconds
        scheduler.scheduleAtFixedRate(
                this::performIntegrityCheck,
                baseInterval, 
                baseInterval,
                TimeUnit.SECONDS);
        
        isRunning = true;
    }
    
    /**
     * Stop integrity monitoring
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        Log.d(TAG, "Stopping anti-tampering system");
        
        // Shutdown scheduler
        scheduler.shutdown();
        
        isRunning = false;
    }
    
    /**
     * Set check intensity
     * @param level Intensity level (1-5)
     */
    public void setCheckIntensity(int level) {
        if (level < 1 || level > 5) {
            Log.e(TAG, "Invalid intensity level: " + level);
            return;
        }
        
        if (this.checkIntensity == level) {
            return;
        }
        
        Log.d(TAG, "Setting anti-tampering intensity to " + level);
        this.checkIntensity = level;
        
        // Apply new configuration
        applyIntensityConfiguration();
        
        // If running, perform immediate check
        if (isRunning) {
            performIntegrityCheck();
        }
    }
    
    /**
     * Apply intensity configuration
     */
    private void applyIntensityConfiguration() {
        // Apply different check methods based on intensity level
        switch (checkIntensity) {
            case 1: // Minimal
                activeChecks.put(TamperingCheckMethod.APP_SIGNATURE, true);
                activeChecks.put(TamperingCheckMethod.INSTALLER_VERIFICATION, false);
                activeChecks.put(TamperingCheckMethod.DEX_INTEGRITY, false);
                activeChecks.put(TamperingCheckMethod.NATIVE_LIBRARY_INTEGRITY, false);
                activeChecks.put(TamperingCheckMethod.RESOURCES_INTEGRITY, false);
                activeChecks.put(TamperingCheckMethod.DEBUG_FLAGS, true);
                break;
                
            case 2: // Low
                activeChecks.put(TamperingCheckMethod.APP_SIGNATURE, true);
                activeChecks.put(TamperingCheckMethod.INSTALLER_VERIFICATION, true);
                activeChecks.put(TamperingCheckMethod.DEX_INTEGRITY, false);
                activeChecks.put(TamperingCheckMethod.NATIVE_LIBRARY_INTEGRITY, false);
                activeChecks.put(TamperingCheckMethod.RESOURCES_INTEGRITY, false);
                activeChecks.put(TamperingCheckMethod.DEBUG_FLAGS, true);
                break;
                
            case 3: // Medium (Default)
                activeChecks.put(TamperingCheckMethod.APP_SIGNATURE, true);
                activeChecks.put(TamperingCheckMethod.INSTALLER_VERIFICATION, true);
                activeChecks.put(TamperingCheckMethod.DEX_INTEGRITY, true);
                activeChecks.put(TamperingCheckMethod.NATIVE_LIBRARY_INTEGRITY, false);
                activeChecks.put(TamperingCheckMethod.RESOURCES_INTEGRITY, false);
                activeChecks.put(TamperingCheckMethod.DEBUG_FLAGS, true);
                break;
                
            case 4: // High
                activeChecks.put(TamperingCheckMethod.APP_SIGNATURE, true);
                activeChecks.put(TamperingCheckMethod.INSTALLER_VERIFICATION, true);
                activeChecks.put(TamperingCheckMethod.DEX_INTEGRITY, true);
                activeChecks.put(TamperingCheckMethod.NATIVE_LIBRARY_INTEGRITY, true);
                activeChecks.put(TamperingCheckMethod.RESOURCES_INTEGRITY, false);
                activeChecks.put(TamperingCheckMethod.DEBUG_FLAGS, true);
                break;
                
            case 5: // Maximum
                activeChecks.put(TamperingCheckMethod.APP_SIGNATURE, true);
                activeChecks.put(TamperingCheckMethod.INSTALLER_VERIFICATION, true);
                activeChecks.put(TamperingCheckMethod.DEX_INTEGRITY, true);
                activeChecks.put(TamperingCheckMethod.NATIVE_LIBRARY_INTEGRITY, true);
                activeChecks.put(TamperingCheckMethod.RESOURCES_INTEGRITY, true);
                activeChecks.put(TamperingCheckMethod.DEBUG_FLAGS, true);
                break;
        }
        
        Log.d(TAG, "Applied anti-tampering configuration for intensity level " + checkIntensity);
    }
    
    /**
     * Add tampering listener
     * @param listener Listener to add
     */
    public void addTamperingListener(TamperingListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove tampering listener
     * @param listener Listener to remove
     */
    public void removeTamperingListener(TamperingListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Perform integrity check
     */
    public void performIntegrityCheck() {
        Log.d(TAG, "Performing integrity check");
        
        // Reset status
        boolean overallIntegrity = true;
        String failureReason = null;
        
        // Check app signature if enabled
        if (activeChecks.get(TamperingCheckMethod.APP_SIGNATURE)) {
            boolean signatureValid = checkAppSignature();
            if (!signatureValid) {
                overallIntegrity = false;
                failureReason = "App signature verification failed";
            }
        }
        
        // Check installer verification if enabled
        if (activeChecks.get(TamperingCheckMethod.INSTALLER_VERIFICATION)) {
            boolean installerValid = checkInstallerPackage();
            if (!installerValid) {
                // This is a warning rather than a failure because legitimate sideloading exists
                Log.w(TAG, "App not installed from official store");
            }
        }
        
        // Check DEX integrity if enabled
        if (activeChecks.get(TamperingCheckMethod.DEX_INTEGRITY)) {
            boolean dexValid = checkDexIntegrity();
            if (!dexValid) {
                overallIntegrity = false;
                failureReason = "DEX integrity check failed";
            }
        }
        
        // Check native library integrity if enabled
        if (activeChecks.get(TamperingCheckMethod.NATIVE_LIBRARY_INTEGRITY)) {
            boolean libsValid = checkNativeLibraryIntegrity();
            if (!libsValid) {
                overallIntegrity = false;
                failureReason = "Native library integrity check failed";
            }
        }
        
        // Check resources integrity if enabled
        if (activeChecks.get(TamperingCheckMethod.RESOURCES_INTEGRITY)) {
            boolean resourcesValid = checkResourcesIntegrity();
            if (!resourcesValid) {
                overallIntegrity = false;
                failureReason = "Resources integrity check failed";
            }
        }
        
        // Check debug flags if enabled
        if (activeChecks.get(TamperingCheckMethod.DEBUG_FLAGS)) {
            boolean debugFlagsValid = checkDebugFlags();
            if (!debugFlagsValid) {
                // This is more of a warning since debugging could be legitimate
                Log.w(TAG, "Debug flags detected");
            }
        }
        
        // Update integrity status
        final boolean finalIntegrity = overallIntegrity;
        final String finalReason = failureReason;
        
        // Notify on main thread
        mainHandler.post(() -> {
            boolean statusChanged = integrityVerified != finalIntegrity || 
                    (finalReason == null ? lastFailureReason != null : !finalReason.equals(lastFailureReason));
            
            integrityVerified = finalIntegrity;
            lastFailureReason = finalReason;
            
            if (statusChanged) {
                notifyListeners(finalIntegrity, finalReason);
            }
        });
    }
    
    /**
     * Check app signature
     * @return True if signature valid
     */
    private boolean checkAppSignature() {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            
            if (info.signatures != null && info.signatures.length > 0) {
                String currentSignature = calculateSignatureHash(info.signatures[0].toByteArray());
                String expectedSignature = expectedChecksums.get("app_signature");
                
                if (expectedSignature != null && expectedSignature.equals(currentSignature)) {
                    return true;
                } else {
                    Log.w(TAG, "App signature mismatch");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking app signature", e);
        }
        
        return false;
    }
    
    /**
     * Check installer package
     * @return True if installed from official source
     */
    private boolean checkInstallerPackage() {
        String installer = null;
        
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                installer = context.getPackageManager().getInstallSourceInfo(
                        context.getPackageName()).getInstallingPackageName();
            } else {
                installer = context.getPackageManager().getInstallerPackageName(
                        context.getPackageName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking installer package", e);
        }
        
        // Check if installed from a known source
        return installer != null && (
                installer.startsWith("com.android.vending") || // Google Play
                installer.startsWith("com.amazon.venezia") || // Amazon Appstore
                installer.startsWith("com.sec.android.app.samsungapps") // Samsung Galaxy Store
        );
    }
    
    /**
     * Check DEX integrity
     * @return True if DEX checksums match
     */
    private boolean checkDexIntegrity() {
        // In a real implementation, would check actual DEX file checksums
        // This is simplified for demonstration
        
        // Pretend we're checking DEX files
        String expectedDexChecksum = expectedChecksums.get("classes.dex");
        if (expectedDexChecksum == null) {
            Log.w(TAG, "No expected DEX checksum available");
            return true; // Can't verify
        }
        
        // Simulate verification by returning success for demo
        return true;
    }
    
    /**
     * Check native library integrity
     * @return True if native libraries checksums match
     */
    private boolean checkNativeLibraryIntegrity() {
        // In a real implementation, would calculate checksums of native libraries
        // and compare with expected values
        
        // Pretend we're checking native libraries
        String expectedLibChecksum = expectedChecksums.get("lib/arm64-v8a/libnative.so");
        if (expectedLibChecksum == null) {
            Log.w(TAG, "No expected native library checksum available");
            return true; // Can't verify
        }
        
        // Simulate verification by returning success for demo
        return true;
    }
    
    /**
     * Check resources integrity
     * @return True if resources checksums match
     */
    private boolean checkResourcesIntegrity() {
        // In a real implementation, would check integrity of important resources
        
        // Simulate verification by returning success for demo
        return true;
    }
    
    /**
     * Check debug flags
     * @return True if no debug flags detected
     */
    private boolean checkDebugFlags() {
        try {
            // Check if app is debuggable
            ApplicationInfo ai = context.getApplicationInfo();
            boolean isDebuggable = (ai.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            
            if (isDebuggable) {
                Log.w(TAG, "Application is debuggable");
                return false;
            }
            
            // Additional debug checks could be added here
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking debug flags", e);
        }
        
        return true;
    }
    
    /**
     * Calculate SHA-256 hash of signature bytes
     * @param signatureBytes Signature bytes
     * @return Hash as hex string
     */
    private String calculateSignatureHash(byte[] signatureBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(signatureBytes);
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error calculating signature hash", e);
            return "";
        }
    }
    
    /**
     * Calculate checksum of a file
     * @param filePath File path
     * @return Checksum as hex string
     */
    private String calculateFileChecksum(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            File file = new File(filePath);
            
            try (DigestInputStream dis = new DigestInputStream(new FileInputStream(file), md)) {
                byte[] buffer = new byte[8192];
                while (dis.read(buffer) != -1) {
                    // Read file completely
                }
                
                byte[] digest = md.digest();
                return bytesToHex(digest);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            Log.e(TAG, "Error calculating file checksum: " + filePath, e);
            return "";
        }
    }
    
    /**
     * Convert bytes to hex string
     * @param bytes Bytes to convert
     * @return Hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * Notify listeners of integrity status change
     * @param integrity Current integrity status
     * @param reason Failure reason (if any)
     */
    private void notifyListeners(boolean integrity, String reason) {
        Log.d(TAG, "Notifying listeners of integrity status: " + integrity + 
              (reason != null ? " (" + reason + ")" : ""));
        
        for (TamperingListener listener : listeners) {
            listener.onIntegrityStatusChanged(integrity, reason);
        }
    }
    
    /**
     * Check if integrity is verified
     * @return True if verified
     */
    public boolean isIntegrityVerified() {
        return integrityVerified;
    }
    
    /**
     * Get last failure reason
     * @return Failure reason or null if none
     */
    public String getLastFailureReason() {
        return lastFailureReason;
    }
    
    /**
     * Interface for tampering event listeners
     */
    public interface TamperingListener {
        /**
         * Called when integrity status changes
         * @param integrityVerified True if integrity verified
         * @param failureReason Reason for failure (if any)
         */
        void onIntegrityStatusChanged(boolean integrityVerified, String failureReason);
    }
}
