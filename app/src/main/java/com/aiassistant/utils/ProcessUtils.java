package com.aiassistant.utils;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for process-related operations, particularly for
 * anti-detection and security measures.
 */
public class ProcessUtils {
    private static final String TAG = "ProcessUtils";
    
    // Known suspicious process names that might be monitoring for cheats
    private static final Set<String> SUSPICIOUS_PROCESS_NAMES = new HashSet<>(Arrays.asList(
        "cheatdetector", "gameguardian", "magisk", "frida", "xposed", "substrate",
        "antifraud", "anticheat", "securitymonitor", "gameprotection"
    ));
    
    /**
     * Attempt to hide the current process from the process list
     */
    public static boolean hideProcessFromList(Context context) {
        try {
            // This requires root or system privileges in production
            // For simulation purposes, we'll just log the action
            Log.d(TAG, "Simulating hiding process from list");
            
            // On a real implementation, this would involve native code
            // to modify process visibility in the /proc filesystem
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to hide process", e);
            return false;
        }
    }
    
    /**
     * Check for processes that might be monitoring our app
     */
    public static List<String> checkForSuspiciousProcesses() {
        List<String> detected = new ArrayList<>();
        
        try {
            // Check running processes
            File procDir = new File("/proc");
            if (procDir.exists() && procDir.isDirectory()) {
                for (File file : procDir.listFiles()) {
                    // Only check directories with numeric names (PIDs)
                    if (file.isDirectory() && file.getName().matches("\\d+")) {
                        String processName = getProcessName(file.getName());
                        if (processName != null && isSuspiciousProcess(processName)) {
                            detected.add(processName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking for suspicious processes", e);
        }
        
        return detected;
    }
    
    /**
     * Get the process name from its PID
     */
    private static String getProcessName(String pid) {
        BufferedReader reader = null;
        try {
            File cmdline = new File("/proc/" + pid + "/cmdline");
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(cmdline)));
            String processName = reader.readLine();
            if (processName != null) {
                processName = processName.replace('\0', ' ').trim();
                return processName;
            }
        } catch (IOException e) {
            // Process may have terminated, ignore
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return null;
    }
    
    /**
     * Check if a process name indicates a suspicious monitoring tool
     */
    private static boolean isSuspiciousProcess(String processName) {
        if (processName == null || processName.isEmpty()) {
            return false;
        }
        
        processName = processName.toLowerCase();
        
        // Check against known suspicious names
        for (String suspicious : SUSPICIOUS_PROCESS_NAMES) {
            if (processName.contains(suspicious)) {
                return true;
            }
        }
        
        // Check for other indicators
        return processName.contains("monitor") && processName.contains("game") ||
               processName.contains("hack") || processName.contains("inject") ||
               processName.contains("mod_") || processName.contains("cheat");
    }
    
    /**
     * Attempt to rename the current process to appear more innocuous
     */
    public static boolean renameProcess(String newName) {
        try {
            // This is a simplified implementation; actual implementation would use JNI
            Log.d(TAG, "Simulating process rename to: " + newName);
            
            // In a real implementation with native code, we would modify 
            // the process name in native layer
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to rename process", e);
            return false;
        }
    }
    
    /**
     * Check if any debuggers are attached to this process
     */
    public static boolean isBeingDebugged() {
        try {
            // Check android.os.Debug for debugger detection
            Class<?> debugClass = Class.forName("android.os.Debug");
            Method isDebugMethod = debugClass.getMethod("isDebuggerConnected");
            return (boolean) isDebugMethod.invoke(null);
        } catch (Exception e) {
            Log.e(TAG, "Error checking debug status", e);
            return false;
        }
    }
    
    /**
     * Get the current process ID
     */
    public static int getMyPid() {
        return Process.myPid();
    }
    
    /**
     * Check if a specific process is running
     */
    public static boolean isProcessRunning(String processName) {
        List<String> runningProcesses = getRunningProcesses();
        for (String process : runningProcesses) {
            if (process.equalsIgnoreCase(processName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get a list of running processes
     */
    public static List<String> getRunningProcesses() {
        List<String> processes = new ArrayList<>();
        
        try {
            // Check running processes in /proc
            File procDir = new File("/proc");
            if (procDir.exists() && procDir.isDirectory()) {
                for (File file : procDir.listFiles()) {
                    // Only check directories with numeric names (PIDs)
                    if (file.isDirectory() && file.getName().matches("\\d+")) {
                        String processName = getProcessName(file.getName());
                        if (processName != null && !processName.isEmpty()) {
                            processes.add(processName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting running processes", e);
        }
        
        return processes;
    }
}
