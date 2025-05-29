package com.aiassistant.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.aiassistant.utils.PreferenceManager;

/**
 * Utility for detecting user inactivity
 */
public class InactivityDetector {
    private static final String TAG = "InactivityDetector";
    
    // Default timeout values
    private static final long DEFAULT_INACTIVITY_TIMEOUT = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final long DEFAULT_ALARM_INTERVAL = 30 * 60 * 1000; // 30 minutes in milliseconds
    
    private final Context context;
    private final Handler handler;
    private final Runnable inactivityRunnable;
    private long lastActivityTime;
    private long inactivityTimeout;
    private boolean inactivityDetectionActive;
    private AlarmManager alarmManager;
    private PendingIntent inactivityAlarmIntent;
    
    /**
     * Constructor
     * 
     * @param context Application context
     */
    public InactivityDetector(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
        this.inactivityRunnable = this::handleInactivityTimeout;
        this.lastActivityTime = SystemClock.elapsedRealtime();
        this.inactivityTimeout = DEFAULT_INACTIVITY_TIMEOUT;
        this.inactivityDetectionActive = false;
        
        // Load settings from preferences
        PreferenceManager prefManager = PreferenceManager.getInstance(context);
        // This would use a stored preference for the inactivity timeout
        
        // Initialize alarm manager
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Create pending intent for alarm
        Intent alarmIntent = new Intent(context, InactivityAlarmReceiver.class);
        this.inactivityAlarmIntent = PendingIntent.getBroadcast(
                context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    
    /**
     * Start inactivity detection
     */
    public void startInactivityDetection() {
        if (inactivityDetectionActive) {
            return;
        }
        
        inactivityDetectionActive = true;
        lastActivityTime = SystemClock.elapsedRealtime();
        scheduleInactivityCheck();
        Log.d(TAG, "Inactivity detection started");
        
        // Schedule alarm for periodic checks
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + DEFAULT_ALARM_INTERVAL,
                    DEFAULT_ALARM_INTERVAL,
                    inactivityAlarmIntent);
        }
    }
    
    /**
     * Stop inactivity detection
     */
    public void stopInactivityDetection() {
        inactivityDetectionActive = false;
        handler.removeCallbacks(inactivityRunnable);
        
        // Cancel alarm
        if (alarmManager != null && inactivityAlarmIntent != null) {
            alarmManager.cancel(inactivityAlarmIntent);
        }
        
        Log.d(TAG, "Inactivity detection stopped");
    }
    
    /**
     * Set the inactivity timeout
     * 
     * @param timeoutMillis Timeout in milliseconds
     */
    public void setInactivityTimeout(long timeoutMillis) {
        this.inactivityTimeout = timeoutMillis;
        
        // If detection is active, reschedule with new timeout
        if (inactivityDetectionActive) {
            handler.removeCallbacks(inactivityRunnable);
            scheduleInactivityCheck();
        }
    }
    
    /**
     * Report user activity to reset the inactivity timer
     */
    public void onUserActivity() {
        lastActivityTime = SystemClock.elapsedRealtime();
        
        // Reschedule the inactivity check
        if (inactivityDetectionActive) {
            handler.removeCallbacks(inactivityRunnable);
            scheduleInactivityCheck();
        }
    }
    
    /**
     * Schedule the inactivity check
     */
    private void scheduleInactivityCheck() {
        handler.postDelayed(inactivityRunnable, inactivityTimeout);
    }
    
    /**
     * Handle inactivity timeout
     */
    private void handleInactivityTimeout() {
        if (!inactivityDetectionActive) {
            return;
        }
        
        long currentTime = SystemClock.elapsedRealtime();
        long inactiveTime = currentTime - lastActivityTime;
        
        if (inactiveTime >= inactivityTimeout) {
            // User has been inactive for the timeout period
            onInactivityDetected();
        } else {
            // Reschedule for the remaining time
            long remainingTime = inactivityTimeout - inactiveTime;
            handler.postDelayed(inactivityRunnable, remainingTime);
        }
    }
    
    /**
     * Handle detected inactivity
     */
    private void onInactivityDetected() {
        Log.d(TAG, "User inactivity detected");
        
        // Notify listeners or take appropriate action
        // This could include disabling AI auto mode, showing a notification, etc.
        
        // Example: Switch to passive mode if in auto mode
        PreferenceManager prefManager = PreferenceManager.getInstance(context);
        String currentMode = prefManager.getAIMode();
        
        if ("AUTO".equals(currentMode)) {
            prefManager.setAIMode("PASSIVE");
            Log.d(TAG, "Switched to passive mode due to inactivity");
            
            // Could also send a notification
            // NotificationHelper notificationHelper = new NotificationHelper(context);
            // notificationHelper.showAIStatusNotification("AI Mode Changed", 
            //                                           "Switched to passive mode due to inactivity", 
            //                                           "PASSIVE");
        }
        
        // If we want to continue monitoring, schedule the next check
        if (inactivityDetectionActive) {
            lastActivityTime = SystemClock.elapsedRealtime();
            scheduleInactivityCheck();
        }
    }
    
    /**
     * Inner broadcast receiver for inactivity alarms
     */
    public static class InactivityAlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // This would be called by the alarm manager
            // It could check if the service is running and trigger inactivity checks
            
            Log.d(TAG, "Inactivity alarm received");
            
            // In a real implementation, this would interact with the service
            // For now, just log the event
        }
    }
}
