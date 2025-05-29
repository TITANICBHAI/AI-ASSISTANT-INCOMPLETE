package com.aiassistant.ai.features.profile;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Game Detection Service
 * Monitors foreground apps to automatically detect and switch to game profiles
 */
public class GameDetectionService {
    private static final String TAG = "GameDetectionService";
    
    private final Context context;
    private final ProfileManager profileManager;
    private final GameProfileFeature profileFeature;
    
    private final ScheduledExecutorService scheduler;
    private final Handler mainHandler;
    
    private boolean isRunning;
    private String lastDetectedPackage;
    
    // Detection interval in milliseconds
    private static final long DETECTION_INTERVAL = 5000; // 5 seconds
    
    /**
     * Constructor
     * @param context Application context
     * @param profileManager Profile manager
     * @param profileFeature Game profile feature
     */
    public GameDetectionService(Context context, ProfileManager profileManager,
                               GameProfileFeature profileFeature) {
        this.context = context;
        this.profileManager = profileManager;
        this.profileFeature = profileFeature;
        
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        this.isRunning = false;
        this.lastDetectedPackage = null;
    }
    
    /**
     * Start game detection
     */
    public void start() {
        if (isRunning) return;
        
        isRunning = true;
        
        // Schedule periodic detection
        scheduler.scheduleAtFixedRate(
            this::detectForegroundGame,
            0,
            DETECTION_INTERVAL,
            TimeUnit.MILLISECONDS
        );
        
        Log.d(TAG, "Game detection service started");
    }
    
    /**
     * Stop game detection
     */
    public void stop() {
        if (!isRunning) return;
        
        isRunning = false;
        
        // Stop scheduler
        scheduler.shutdown();
        
        Log.d(TAG, "Game detection service stopped");
    }
    
    /**
     * Check if service is running
     * @return true if running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Detect foreground game
     */
    private void detectForegroundGame() {
        try {
            ActivityManager activityManager = 
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            
            if (activityManager == null) return;
            
            // Get current foreground app
            String foregroundApp = getForegroundPackage(activityManager);
            
            if (foregroundApp != null && !foregroundApp.equals(lastDetectedPackage)) {
                // Check if this is a known game
                GameProfile profile = profileFeature.getProfile(foregroundApp);
                
                if (profile != null) {
                    // This is a known game, switch to its profile
                    Log.d(TAG, "Detected game: " + foregroundApp);
                    
                    // Switch on main thread
                    mainHandler.post(() -> {
                        profileManager.switchToProfile(foregroundApp);
                    });
                    
                    lastDetectedPackage = foregroundApp;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error detecting foreground game", e);
        }
    }
    
    /**
     * Get foreground package name
     * @param activityManager Activity manager
     * @return Foreground package name or null if not available
     */
    private String getForegroundPackage(ActivityManager activityManager) {
        // Get running tasks
        List<ActivityManager.RunningAppProcessInfo> processes = 
            activityManager.getRunningAppProcesses();
        
        if (processes == null || processes.isEmpty()) return null;
        
        // Look for foreground process
        for (ActivityManager.RunningAppProcessInfo process : processes) {
            if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (process.pkgList.length > 0) {
                    return process.pkgList[0];
                }
            }
        }
        
        return null;
    }
}
