package com.aiassistant.security;

import android.os.Debug;
import android.os.Process;
import android.system.Os;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Prevents debugging of the application by detecting and responding to debugging attempts.
 * This class implements various anti-debugging techniques to make reverse engineering
 * of the application more difficult.
 */
public class DebuggingPrevention {
    private static final String TAG = "DebuggingPrevention";
    private static final boolean DEBUG = false;
    
    // Timing constants
    private static final long CHECK_INTERVAL_MS = 500;
    
    // Debug detection files
    private static final String[] DEBUG_INDICATOR_FILES = {
        "/proc/self/status",
        "/proc/self/stat",
        "/proc/self/maps"
    };
    
    // Native method declarations
    private native boolean nativeIsBeingDebugged();
    private native void nativePreventDebugging();
    private native void nativeSetUnhandledExceptionHandler();

    private final Timer mTimer;
    private boolean mIsActive;
    private int mSecurityLevel;
    private boolean mLastDebugState;

    /**
     * Constructor initializes the debugging prevention
     */
    public DebuggingPrevention() {
        mTimer = new Timer("DebuggingPreventionTimer", true);
        mIsActive = false;
        mSecurityLevel = 0;
        mLastDebugState = false;
        
        // Try to set native exception handler
        try {
            nativeSetUnhandledExceptionHandler();
        } catch (UnsatisfiedLinkError e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to set native exception handler", e);
            }
        }
    }

    /**
     * Starts debugging prevention with the specified security level
     * @param securityLevel Current security level (0-3)
     */
    public void start(int securityLevel) {
        if (mIsActive) {
            // Already running, just update security level
            mSecurityLevel = securityLevel;
            return;
        }
        
        mIsActive = true;
        mSecurityLevel = securityLevel;
        
        try {
            // Apply initial prevention
            applyDebugPrevention();
            
            // Schedule periodic checks if security level is high enough
            if (securityLevel > 0) {
                mTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            checkForDebuggers();
                        } catch (Exception e) {
                            if (DEBUG) {
                                Log.e(TAG, "Error checking for debuggers", e);
                            }
                        }
                    }
                }, CHECK_INTERVAL_MS, CHECK_INTERVAL_MS);
            }
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error starting debugging prevention", e);
            }
        }
    }

    /**
     * Stops debugging prevention
     */
    public void stop() {
        if (!mIsActive) {
            return;
        }
        
        mIsActive = false;
        
        try {
            // Cancel timer
            mTimer.cancel();
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error stopping debugging prevention", e);
            }
        }
    }

    /**
     * Applies various debugging prevention techniques
     */
    private void applyDebugPrevention() {
        try {
            // Try native prevention first
            try {
                nativePreventDebugging();
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native debugging prevention unavailable");
                }
            }
            
            // Set Java thread as non-debuggable where possible
            try {
                // Find setDebuggerRequestedFlag using reflection
                Class<?> threadClass = Class.forName("java.lang.Thread");
                Method setDebugMethod = threadClass.getDeclaredMethod(
                    "setDebuggerRequestedFlag", boolean.class);
                setDebugMethod.setAccessible(true);
                setDebugMethod.invoke(Thread.currentThread(), false);
            } catch (Exception e) {
                // This is expected on most Android versions
                // as this method isn't publicly accessible
            }
            
            // Detect initial debug state
            mLastDebugState = isBeingDebugged();
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error applying debug prevention", e);
            }
        }
    }

    /**
     * Checks if the app is currently being debugged
     * @return True if a debugger is detected
     */
    public boolean isBeingDebugged() {
        try {
            // Try native detection first
            try {
                return nativeIsBeingDebugged();
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native debugging detection unavailable");
                }
            }
            
            // Check using Android APIs
            if (Debug.isDebuggerConnected()) {
                return true;
            }
            
            // Check common indicators
            if (checkDebugIndicators()) {
                return true;
            }
            
            // Check for tracers
            if (isBeingTraced()) {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking for debugger", e);
            }
            return false;
        }
    }

    /**
     * Checks debug indicator files for signs of debugging
     * @return True if debugging is indicated
     */
    private boolean checkDebugIndicators() {
        try {
            // Check /proc/self/status for TracerPid
            try {
                File statusFile = new File("/proc/self/status");
                if (statusFile.exists() && statusFile.canRead()) {
                    BufferedReader reader = new BufferedReader(new FileReader(statusFile));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("TracerPid:")) {
                            String tracerPid = line.substring("TracerPid:".length()).trim();
                            if (!"0".equals(tracerPid)) {
                                reader.close();
                                return true;
                            }
                        }
                    }
                    reader.close();
                }
            } catch (Exception e) {
                // Ignore file read errors
            }
            
            // Check for other indicators
            for (String filePath : DEBUG_INDICATOR_FILES) {
                File file = new File(filePath);
                if (file.exists() && file.canRead()) {
                    try {
                        // Read the file contents
                        StringBuilder content = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line);
                        }
                        reader.close();
                        
                        // Look for specific indicators
                        String fileContent = content.toString();
                        if (filePath.endsWith("/maps")) {
                            // Check for debugging tools like gdb, lldb, etc.
                            if (fileContent.contains("gdb") || fileContent.contains("lldb") ||
                                fileContent.contains("frida") || fileContent.contains("xposed")) {
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        // Ignore file read errors
                    }
                }
            }
            
            return false;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error checking debug indicators", e);
            }
            return false;
        }
    }

    /**
     * Checks if the process is being traced (e.g., by strace, ptrace)
     * @return True if being traced
     */
    private boolean isBeingTraced() {
        try {
            Class<?> osClass = Os.class;
            Method gettidMethod = osClass.getDeclaredMethod("gettid");
            gettidMethod.setAccessible(true);
            int tid = (int) gettidMethod.invoke(null);
            
            // Check /proc/self/task/<tid>/status for TracerPid
            File tracerFile = new File("/proc/self/task/" + tid + "/status");
            if (tracerFile.exists() && tracerFile.canRead()) {
                BufferedReader reader = new BufferedReader(new FileReader(tracerFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("TracerPid:")) {
                        String tracerPid = line.substring("TracerPid:".length()).trim();
                        if (!"0".equals(tracerPid)) {
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
                Log.e(TAG, "Error checking for tracer", e);
            }
            return false;
        }
    }

    /**
     * Periodically checks for debuggers and responds accordingly
     */
    private void checkForDebuggers() {
        boolean isDebugged = isBeingDebugged();
        
        // If debug state has changed, or it's continuously being debugged
        if (isDebugged || mLastDebugState != isDebugged) {
            handleDebuggerDetected(isDebugged);
        }
        
        mLastDebugState = isDebugged;
    }

    /**
     * Handles detection of a debugger based on security level
     * @param isDebugged Current debugging state
     */
    private void handleDebuggerDetected(boolean isDebugged) {
        if (!isDebugged) {
            return;
        }
        
        try {
            if (DEBUG) {
                Log.w(TAG, "Debugger detected! Security level: " + mSecurityLevel);
            }
            
            switch (mSecurityLevel) {
                case 0:
                    // Low security - just log it
                    break;
                    
                case 1:
                    // Medium security - make debugging difficult
                    obfuscateExecution();
                    break;
                    
                case 2:
                    // High security - try to detach debugger
                    obfuscateExecution();
                    try {
                        // Attempt to fork and continue in child process
                        forkAndContinue();
                    } catch (Exception e) {
                        // Fork failed, not critical
                    }
                    break;
                    
                case 3:
                    // Maximum security - terminate to prevent analysis
                    obfuscateExecution();
                    try {
                        Thread.sleep(100 + mRandom.nextInt(900));
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                    Process.killProcess(Process.myPid());
                    System.exit(0);
                    break;
            }
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error handling debugger detection", e);
            }
        }
    }

    // Random generator for timing obfuscation
    private final java.util.Random mRandom = new java.util.Random();

    /**
     * Obfuscates execution to make debugging more difficult
     */
    private void obfuscateExecution() {
        try {
            // Make execution timing unpredictable
            for (int i = 0; i < 3 + mRandom.nextInt(5); i++) {
                Thread.sleep(mRandom.nextInt(20));
                
                // CPU busy-wait
                long endTime = System.currentTimeMillis() + mRandom.nextInt(10);
                while (System.currentTimeMillis() < endTime) {
                    // Busy wait
                    for (int j = 0; j < 1000; j++) {
                        Math.sqrt(mRandom.nextDouble() * 10000);
                    }
                }
            }
            
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Attempts to fork and continue in the child process
     * This is a technique to make debugging more difficult
     * Note: This is a simplistic implementation and may not work on all Android versions
     */
    private void forkAndContinue() {
        try {
            // This is a mock implementation since actual forking requires
            // native code on Android
            if (DEBUG) {
                Log.d(TAG, "Fork and continue would be attempted here");
            }
            
            // In a real implementation, this would use a native method
            // to fork the process and then appropriately handle the parent/child
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error in fork and continue", e);
            }
        }
    }
}
