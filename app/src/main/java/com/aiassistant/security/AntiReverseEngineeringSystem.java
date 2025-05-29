package com.aiassistant.security;

import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements advanced anti-reverse engineering techniques while preserving
 * legitimate functionality.
 */
public class AntiReverseEngineeringSystem {
    private static final String TAG = "AntiReverseEngineering";
    
    // Protection techniques
    private enum ProtectionTechnique {
        CODE_OBFUSCATION,
        ANTI_DEBUGGING,
        EMULATOR_DETECTION,
        HOOKING_DETECTION,
        INSTRUMENTATION_DETECTION,
        DEBUGGER_DETECTION,
        FRIDA_DETECTION
    }
    
    private final Context context;
    private final Handler mainHandler;
    
    // Configuration
    private int protectionLevel = 3; // 1-5 scale
    private boolean isRunning = false;
    
    // Active techniques
    private final Map<ProtectionTechnique, Boolean> activeTechniques;
    
    // Critical code regions
    private final Set<String> criticalRegions;
    
    // State
    private final AtomicBoolean attackDetected = new AtomicBoolean(false);
    private String lastDetectionReason = null;
    
    // Timer for periodic checks
    private Timer protectionTimer;
    
    /**
     * Constructor
     * @param context Application context
     */
    public AntiReverseEngineeringSystem(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.activeTechniques = new HashMap<>();
        this.criticalRegions = new HashSet<>();
        
        // Initialize protection techniques
        for (ProtectionTechnique technique : ProtectionTechnique.values()) {
            activeTechniques.put(technique, false);
        }
        
        // Define critical code regions
        criticalRegions.add("AIStateManager");
        criticalRegions.add("AIState");
        criticalRegions.add("AntiDetectionManager");
        criticalRegions.add("SecurityProtectionSystem");
        criticalRegions.add("AccessControl");
    }
    
    /**
     * Start protection
     */
    public void start() {
        if (isRunning) {
            return;
        }
        
        Log.d(TAG, "Starting anti-reverse engineering system with level " + protectionLevel);
        
        // Configure active techniques
        applyProtectionLevel();
        
        // Initialize protection
        initializeProtection();
        
        // Schedule periodic protection checks
        scheduleProtectionChecks();
        
        isRunning = true;
    }
    
    /**
     * Stop protection
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        Log.d(TAG, "Stopping anti-reverse engineering system");
        
        // Cancel timer
        if (protectionTimer != null) {
            protectionTimer.cancel();
            protectionTimer = null;
        }
        
        isRunning = false;
    }
    
    /**
     * Set protection level
     * @param level Protection level (1-5)
     */
    public void setProtectionLevel(int level) {
        if (level < 1 || level > 5) {
            Log.e(TAG, "Invalid protection level: " + level);
            return;
        }
        
        if (this.protectionLevel == level) {
            return;
        }
        
        Log.d(TAG, "Setting protection level to " + level);
        this.protectionLevel = level;
        
        // Apply new protection level
        applyProtectionLevel();
        
        // If running, restart protection
        if (isRunning) {
            stop();
            start();
        }
    }
    
    /**
     * Apply protection level
     */
    private void applyProtectionLevel() {
        // Configure which techniques are active based on protection level
        switch (protectionLevel) {
            case 1: // Minimal
                activeTechniques.put(ProtectionTechnique.CODE_OBFUSCATION, true);
                activeTechniques.put(ProtectionTechnique.ANTI_DEBUGGING, false);
                activeTechniques.put(ProtectionTechnique.EMULATOR_DETECTION, false);
                activeTechniques.put(ProtectionTechnique.HOOKING_DETECTION, false);
                activeTechniques.put(ProtectionTechnique.INSTRUMENTATION_DETECTION, false);
                activeTechniques.put(ProtectionTechnique.DEBUGGER_DETECTION, true);
                activeTechniques.put(ProtectionTechnique.FRIDA_DETECTION, false);
                break;
                
            case 2: // Low
                activeTechniques.put(ProtectionTechnique.CODE_OBFUSCATION, true);
                activeTechniques.put(ProtectionTechnique.ANTI_DEBUGGING, true);
                activeTechniques.put(ProtectionTechnique.EMULATOR_DETECTION, true);
                activeTechniques.put(ProtectionTechnique.HOOKING_DETECTION, false);
                activeTechniques.put(ProtectionTechnique.INSTRUMENTATION_DETECTION, false);
                activeTechniques.put(ProtectionTechnique.DEBUGGER_DETECTION, true);
                activeTechniques.put(ProtectionTechnique.FRIDA_DETECTION, false);
                break;
                
            case 3: // Medium (Default)
                activeTechniques.put(ProtectionTechnique.CODE_OBFUSCATION, true);
                activeTechniques.put(ProtectionTechnique.ANTI_DEBUGGING, true);
                activeTechniques.put(ProtectionTechnique.EMULATOR_DETECTION, true);
                activeTechniques.put(ProtectionTechnique.HOOKING_DETECTION, true);
                activeTechniques.put(ProtectionTechnique.INSTRUMENTATION_DETECTION, false);
                activeTechniques.put(ProtectionTechnique.DEBUGGER_DETECTION, true);
                activeTechniques.put(ProtectionTechnique.FRIDA_DETECTION, true);
                break;
                
            case 4: // High
                activeTechniques.put(ProtectionTechnique.CODE_OBFUSCATION, true);
                activeTechniques.put(ProtectionTechnique.ANTI_DEBUGGING, true);
                activeTechniques.put(ProtectionTechnique.EMULATOR_DETECTION, true);
                activeTechniques.put(ProtectionTechnique.HOOKING_DETECTION, true);
                activeTechniques.put(ProtectionTechnique.INSTRUMENTATION_DETECTION, true);
                activeTechniques.put(ProtectionTechnique.DEBUGGER_DETECTION, true);
                activeTechniques.put(ProtectionTechnique.FRIDA_DETECTION, true);
                break;
                
            case 5: // Maximum
                // Enable all protection techniques
                for (ProtectionTechnique technique : ProtectionTechnique.values()) {
                    activeTechniques.put(technique, true);
                }
                break;
        }
        
        Log.d(TAG, "Applied protection configuration for level " + protectionLevel);
    }
    
    /**
     * Initialize protection techniques
     */
    private void initializeProtection() {
        // Apply code obfuscation if enabled
        if (activeTechniques.get(ProtectionTechnique.CODE_OBFUSCATION)) {
            applyCodeObfuscation();
        }
        
        // Apply anti-debugging if enabled
        if (activeTechniques.get(ProtectionTechnique.ANTI_DEBUGGING)) {
            applyAntiDebugging();
        }
        
        // Check for debugger right away
        if (activeTechniques.get(ProtectionTechnique.DEBUGGER_DETECTION)) {
            if (Debug.isDebuggerConnected()) {
                onAttackDetected("Debugger connected", ProtectionTechnique.DEBUGGER_DETECTION);
            }
        }
        
        // Check for emulator
        if (activeTechniques.get(ProtectionTechnique.EMULATOR_DETECTION)) {
            if (isEmulator()) {
                onAttackDetected("Running in emulator", ProtectionTechnique.EMULATOR_DETECTION);
            }
        }
    }
    
    /**
     * Schedule periodic protection checks
     */
    private void scheduleProtectionChecks() {
        // Create a new timer
        protectionTimer = new Timer("ProtectionTimer");
        
        // Schedule checks with varying intervals to prevent pattern detection
        Random random = new Random();
        
        // Base checking frequency varies by protection level
        long baseInterval = 5000; // 5 seconds base for lowest level
        switch (protectionLevel) {
            case 1: baseInterval = 60000; break; // 1 minute
            case 2: baseInterval = 30000; break; // 30 seconds
            case 3: baseInterval = 15000; break; // 15 seconds
            case 4: baseInterval = 10000; break; // 10 seconds
            case 5: baseInterval = 5000; break;  // 5 seconds
        }
        
        // Schedule initial check
        protectionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                performProtectionChecks();
                
                // Schedule next check with random variation
                long nextInterval = baseInterval + random.nextInt((int)(baseInterval / 2));
                protectionTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        performProtectionChecks();
                    }
                }, nextInterval);
            }
        }, random.nextInt(3000) + 1000); // Initial delay 1-4 seconds
    }
    
    /**
     * Perform protection checks
     */
    private void performProtectionChecks() {
        // Only proceed if running
        if (!isRunning) {
            return;
        }
        
        // Add random delays to make timing analysis more difficult
        try {
            Thread.sleep(new Random().nextInt(50));
        } catch (InterruptedException e) {
            // Ignore
        }
        
        // Check for debugger
        if (activeTechniques.get(ProtectionTechnique.DEBUGGER_DETECTION)) {
            checkForDebugger();
        }
        
        // Check for hooks
        if (activeTechniques.get(ProtectionTechnique.HOOKING_DETECTION)) {
            checkForHooks();
        }
        
        // Check for instrumentation
        if (activeTechniques.get(ProtectionTechnique.INSTRUMENTATION_DETECTION)) {
            checkForInstrumentation();
        }
        
        // Check for Frida
        if (activeTechniques.get(ProtectionTechnique.FRIDA_DETECTION)) {
            checkForFrida();
        }
    }
    
    /**
     * Apply code obfuscation techniques
     * These are primarily build-time techniques, but we can add some runtime aspects
     */
    private void applyCodeObfuscation() {
        Log.d(TAG, "Applying code obfuscation");
        
        // Runtime string obfuscation helper is initialized
        // In a real implementation, would include:
        // - String encryption/decryption at runtime
        // - Control flow obfuscation
        // - Dynamic method resolution
        
        // For this demo, we'll just have a placeholder
    }
    
    /**
     * Apply anti-debugging techniques
     */
    private void applyAntiDebugging() {
        Log.d(TAG, "Applying anti-debugging protection");
        
        // Check debug flags
        try {
            boolean isDebuggable = (context.getApplicationInfo().flags & 
                                    android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            if (isDebuggable) {
                Log.w(TAG, "Application is debuggable");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking debug flags", e);
        }
        
        // In a real implementation, would apply various anti-debug techniques
    }
    
    /**
     * Check for debugger
     */
    private void checkForDebugger() {
        if (Debug.isDebuggerConnected()) {
            onAttackDetected("Debugger connected", ProtectionTechnique.DEBUGGER_DETECTION);
        }
    }
    
    /**
     * Check for hook frameworks
     */
    private void checkForHooks() {
        // Check for known hooking frameworks
        boolean hooksDetected = false;
        String reason = null;
        
        // Check for Xposed
        try {
            ClassLoader.getSystemClassLoader().loadClass("de.robv.android.xposed.XposedBridge");
            hooksDetected = true;
            reason = "Xposed framework detected";
        } catch (ClassNotFoundException ignored) {
            // Not found, which is good
        }
        
        // Check for Substrate
        if (!hooksDetected) {
            try {
                ClassLoader.getSystemClassLoader().loadClass("com.saurik.substrate.MS");
                hooksDetected = true;
                reason = "Substrate framework detected";
            } catch (ClassNotFoundException ignored) {
                // Not found, which is good
            }
        }
        
        // Check for suspicious files
        if (!hooksDetected) {
            String[] suspiciousFiles = {
                    "/system/lib/libsubstrate.so",
                    "/system/lib/libsubstrate-dvm.so",
                    "/system/lib/libXposedBridge.so",
                    "/data/data/de.robv.android.xposed.installer"
            };
            
            for (String file : suspiciousFiles) {
                if (new File(file).exists()) {
                    hooksDetected = true;
                    reason = "Hook-related file detected: " + file;
                    break;
                }
            }
        }
        
        if (hooksDetected) {
            onAttackDetected(reason, ProtectionTechnique.HOOKING_DETECTION);
        }
    }
    
    /**
     * Check for instrumentation
     */
    private void checkForInstrumentation() {
        // In a real implementation, would check for:
        // - Method call timing anomalies
        // - Unexpected stack trace patterns
        // - Modified method behavior
        
        // This is a simplified placeholder
    }
    
    /**
     * Check for Frida (dynamic instrumentation tool)
     */
    private void checkForFrida() {
        boolean fridaDetected = false;
        String reason = null;
        
        // Check for Frida server process
        try {
            Process process = Runtime.getRuntime().exec("ps");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("frida") || line.contains("gum-js-loop")) {
                    fridaDetected = true;
                    reason = "Frida process detected";
                    break;
                }
            }
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "Error checking for Frida", e);
        }
        
        // Check for Frida libraries
        if (!fridaDetected) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader("/proc/self/maps"));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("frida") || line.contains("gum")) {
                        fridaDetected = true;
                        reason = "Frida library detected";
                        break;
                    }
                }
                reader.close();
            } catch (Exception e) {
                // Ignore - this is expected on some devices
            }
        }
        
        // Check for Frida ports
        if (!fridaDetected) {
            try {
                Process process = Runtime.getRuntime().exec("netstat -na");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Check for default Frida ports
                    if (line.contains("27042") || line.contains("27043")) {
                        fridaDetected = true;
                        reason = "Frida network port detected";
                        break;
                    }
                }
                reader.close();
            } catch (Exception e) {
                Log.e(TAG, "Error checking for Frida ports", e);
            }
        }
        
        if (fridaDetected) {
            onAttackDetected(reason, ProtectionTechnique.FRIDA_DETECTION);
        }
    }
    
    /**
     * Check if running in an emulator
     * @return True if likely in emulator
     */
    private boolean isEmulator() {
        // Check various emulator indicators
        return (Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk".equals(Build.PRODUCT) ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu") ||
                Build.HARDWARE.contains("vbox86"));
    }
    
    /**
     * Handle detected reverse engineering attack
     * @param reason Reason for detection
     * @param technique Technique that detected the attack
     */
    private void onAttackDetected(String reason, ProtectionTechnique technique) {
        if (attackDetected.compareAndSet(false, true)) {
            Log.w(TAG, "Reverse engineering attempt detected: " + reason + " via " + technique);
            lastDetectionReason = reason;
            
            // Send an obfuscated and delayed notification to the main thread
            // to avoid immediate detection of our response
            final long delayMs = new Random().nextInt(500) + 100; // 100-600ms
            mainHandler.postDelayed(() -> {
                notifySecuritySystem(reason, technique);
            }, delayMs);
        }
    }
    
    /**
     * Notify security system of attack
     * @param reason Detection reason
     * @param technique Detection technique
     */
    private void notifySecuritySystem(String reason, ProtectionTechnique technique) {
        // In a real implementation, would notify the central security system
        // and trigger appropriate countermeasures
        
        // For this demo, we just log the event
        Log.w(TAG, "Security system notified of reverse engineering attempt: " + 
              reason + " (detected by " + technique + ")");
    }
    
    /**
     * Reset attack detection (for testing)
     */
    public void reset() {
        attackDetected.set(false);
        lastDetectionReason = null;
    }
    
    /**
     * Check if an attack was detected
     * @return True if attack detected
     */
    public boolean wasAttackDetected() {
        return attackDetected.get();
    }
    
    /**
     * Get reason for last attack detection
     * @return Reason or null if no attack detected
     */
    public String getDetectionReason() {
        return lastDetectionReason;
    }
    
    /**
     * Get protection level
     * @return Current protection level (1-5)
     */
    public int getProtectionLevel() {
        return protectionLevel;
    }
    
    /**
     * Is the system running
     * @return True if running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Enter a critical code region
     * This can apply additional protection during execution of sensitive code
     * @param regionName Name of critical region
     */
    public void enterCriticalRegion(String regionName) {
        if (!criticalRegions.contains(regionName)) {
            return; // Not a registered critical region
        }
        
        // Apply enhanced protection while in critical region
        // In a real implementation, would increase security checks
        // and apply additional obfuscation techniques
    }
    
    /**
     * Exit a critical code region
     * @param regionName Name of critical region
     */
    public void exitCriticalRegion(String regionName) {
        if (!criticalRegions.contains(regionName)) {
            return; // Not a registered critical region
        }
        
        // Return to normal protection level
    }
}
