package com.aiassistant.system.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.aiassistant.ai.features.call.CallHandlingSystem;
import com.aiassistant.core.ai.AIStateManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Advanced Adaptive Notification Manager that intelligently adjusts
 * notification behavior based on importance, context, and user preferences.
 * 
 * Features:
 * 1. Context-aware notification importance
 * 2. Gradual volume escalation for important alerts
 * 3. Custom vibration patterns based on urgency
 * 4. Adaptive notification timing
 * 5. Smart interruption management
 * 6. Persistent important notifications
 */
public class AdaptiveNotificationManager {
    private static final String TAG = "AdaptiveNotifManager";
    private static final String PREFS_NAME = "adaptive_notif_prefs";
    private static AdaptiveNotificationManager instance;
    
    // Core components
    private Context context;
    private AIStateManager aiStateManager;
    private CallHandlingSystem callHandlingSystem;
    private NotificationManagerCompat notificationManager;
    private AudioManager audioManager;
    private Vibrator vibrator;
    private SharedPreferences preferences;
    private ScheduledExecutorService scheduler;
    private Handler mainHandler;
    private Random random = new Random();
    
    // Notification settings
    private boolean adaptiveVolumeEnabled = true;
    private boolean adaptiveVibrationEnabled = true;
    private boolean smartTimingEnabled = true;
    private boolean doNotDisturbRespected = true;
    private Map<String, NotificationChannel> customChannels = new HashMap<>();
    
    // Notification history and context
    private List<NotificationRecord> notificationHistory = new ArrayList<>();
    private Map<String, Integer> notificationFrequency = new HashMap<>();
    private Map<Integer, Long> activeNotifications = new HashMap<>();
    
    // Channel IDs
    private static final String CHANNEL_URGENT = "urgent_notifications";
    private static final String CHANNEL_HIGH = "high_priority_notifications";
    private static final String CHANNEL_NORMAL = "normal_notifications";
    private static final String CHANNEL_LOW = "low_priority_notifications";
    
    /**
     * Notification importance levels
     */
    public enum NotificationImportance {
        URGENT(3),
        HIGH(2),
        NORMAL(1),
        LOW(0);
        
        private final int value;
        
        NotificationImportance(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }
    
    /**
     * Record of a notification
     */
    public static class NotificationRecord {
        public int id;
        public String title;
        public String message;
        public NotificationImportance importance;
        public long timestamp;
        public String category;
        public String sender;
        public boolean userInteracted;
        
        public NotificationRecord(int id, String title, String message, NotificationImportance importance) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.importance = importance;
            this.timestamp = System.currentTimeMillis();
            this.userInteracted = false;
        }
    }
    
    private AdaptiveNotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.notificationManager = NotificationManagerCompat.from(context);
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize AI and call system
        this.aiStateManager = AIStateManager.getInstance(context);
        this.callHandlingSystem = CallHandlingSystem.getInstance(context);
        
        // Create notification channels
        createNotificationChannels();
        
        // Load saved preferences
        loadPreferences();
    }
    
    public static synchronized AdaptiveNotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdaptiveNotificationManager(context);
        }
        return instance;
    }
    
    /**
     * Create notification channels for Android O and above
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Urgent channel
            NotificationChannel urgentChannel = new NotificationChannel(
                    CHANNEL_URGENT,
                    "Urgent Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            urgentChannel.setDescription("Critical notifications that require immediate attention");
            urgentChannel.enableVibration(true);
            urgentChannel.enableLights(true);
            urgentChannel.setLightColor(Color.RED);
            urgentChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
            urgentChannel.setVibrationPattern(new long[]{0, 400, 200, 400, 200, 400});
            urgentChannel.setBypassDnd(true);
            
            // High priority channel
            NotificationChannel highChannel = new NotificationChannel(
                    CHANNEL_HIGH,
                    "Important Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            highChannel.setDescription("Important notifications that should not be missed");
            highChannel.enableVibration(true);
            highChannel.enableLights(true);
            highChannel.setLightColor(Color.YELLOW);
            highChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());
            
            // Normal channel
            NotificationChannel normalChannel = new NotificationChannel(
                    CHANNEL_NORMAL,
                    "Regular Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            normalChannel.setDescription("Regular notifications and updates");
            normalChannel.enableVibration(true);
            normalChannel.setVibrationPattern(new long[]{0, 250, 250, 250});
            
            // Low priority channel
            NotificationChannel lowChannel = new NotificationChannel(
                    CHANNEL_LOW,
                    "Low Priority Notifications",
                    NotificationManager.IMPORTANCE_LOW);
            lowChannel.setDescription("Non-critical information and background updates");
            lowChannel.enableVibration(false);
            lowChannel.setSound(null, null);
            
            // Register channels
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(urgentChannel);
                manager.createNotificationChannel(highChannel);
                manager.createNotificationChannel(normalChannel);
                manager.createNotificationChannel(lowChannel);
                
                // Store for later use
                customChannels.put(CHANNEL_URGENT, urgentChannel);
                customChannels.put(CHANNEL_HIGH, highChannel);
                customChannels.put(CHANNEL_NORMAL, normalChannel);
                customChannels.put(CHANNEL_LOW, lowChannel);
            }
        }
    }
    
    /**
     * Load saved preferences
     */
    private void loadPreferences() {
        adaptiveVolumeEnabled = preferences.getBoolean("adaptive_volume_enabled", true);
        adaptiveVibrationEnabled = preferences.getBoolean("adaptive_vibration_enabled", true);
        smartTimingEnabled = preferences.getBoolean("smart_timing_enabled", true);
        doNotDisturbRespected = preferences.getBoolean("do_not_disturb_respected", true);
    }
    
    /**
     * Save current preferences
     */
    public void savePreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("adaptive_volume_enabled", adaptiveVolumeEnabled);
        editor.putBoolean("adaptive_vibration_enabled", adaptiveVibrationEnabled);
        editor.putBoolean("smart_timing_enabled", smartTimingEnabled);
        editor.putBoolean("do_not_disturb_respected", doNotDisturbRespected);
        editor.apply();
    }
    
    /**
     * Show a notification with adaptive behavior
     */
    public void showNotification(int notificationId, String title, String message, 
            NotificationImportance importance, Intent contentIntent) {
        
        // Create notification record
        NotificationRecord record = new NotificationRecord(notificationId, title, message, importance);
        notificationHistory.add(0, record); // Add to beginning of list
        
        // Update notification frequency
        String key = title + "|" + importance.name();
        Integer count = notificationFrequency.getOrDefault(key, 0);
        notificationFrequency.put(key, count + 1);
        
        // Check if the notification should be shown now or delayed
        if (smartTimingEnabled && importance != NotificationImportance.URGENT && shouldDelayNotification()) {
            scheduleDelayedNotification(record, contentIntent);
            return;
        }
        
        // Create and show notification
        showNotificationInternal(record, contentIntent);
    }
    
    /**
     * Show notification internal implementation
     */
    private void showNotificationInternal(NotificationRecord record, Intent contentIntent) {
        // Track active notification
        activeNotifications.put(record.id, System.currentTimeMillis());
        
        // Create pending intent for notification tap
        PendingIntent pendingIntent = null;
        if (contentIntent != null) {
            pendingIntent = PendingIntent.getActivity(context, record.id, contentIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        
        // Select notification channel based on importance
        String channelId;
        switch (record.importance) {
            case URGENT:
                channelId = CHANNEL_URGENT;
                break;
            case HIGH:
                channelId = CHANNEL_HIGH;
                break;
            case LOW:
                channelId = CHANNEL_LOW;
                break;
            case NORMAL:
            default:
                channelId = CHANNEL_NORMAL;
                break;
        }
        
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder icon
                .setContentTitle(record.title)
                .setContentText(record.message)
                .setPriority(getPriorityFromImportance(record.importance))
                .setAutoCancel(true);
        
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        
        // Add adaptive behavior based on importance
        applyAdaptiveBehavior(builder, record);
        
        // Show the notification
        try {
            notificationManager.notify(record.id, builder.build());
            
            // For important notifications, consider escalation
            if (record.importance == NotificationImportance.URGENT || 
                    record.importance == NotificationImportance.HIGH) {
                scheduleNotificationEscalation(record);
            }
            
            Log.d(TAG, "Showed notification: " + record.title + " with importance " + record.importance);
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage());
        }
    }
    
    /**
     * Apply adaptive behavior based on notification importance
     */
    private void applyAdaptiveBehavior(NotificationCompat.Builder builder, NotificationRecord record) {
        // Set category
        switch (record.importance) {
            case URGENT:
                builder.setCategory(NotificationCompat.CATEGORY_ALARM);
                break;
            case HIGH:
                builder.setCategory(NotificationCompat.CATEGORY_CALL);
                break;
            case NORMAL:
                builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
                break;
            case LOW:
                builder.setCategory(NotificationCompat.CATEGORY_STATUS);
                break;
        }
        
        // Set visibility on lock screen
        switch (record.importance) {
            case URGENT:
            case HIGH:
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                break;
            case NORMAL:
                builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
                break;
            case LOW:
                builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
                break;
        }
        
        // Set sound
        if (adaptiveVolumeEnabled) {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            
            Uri sound = null;
            
            switch (record.importance) {
                case URGENT:
                    sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    // Set to max volume for urgent notifications
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxVolume, 0);
                    break;
                case HIGH:
                    sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    // Set to 75% volume for high importance
                    audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 
                            (int)(maxVolume * 0.75), 0);
                    break;
                case NORMAL:
                    sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    // Keep current volume
                    break;
                case LOW:
                    // No sound for low importance
                    break;
            }
            
            if (sound != null) {
                builder.setSound(sound);
            }
            
            // Restore original volume after a delay for URGENT and HIGH
            if (record.importance == NotificationImportance.URGENT || 
                    record.importance == NotificationImportance.HIGH) {
                final int originalVolume = currentVolume;
                mainHandler.postDelayed(() -> 
                        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, originalVolume, 0), 
                        5000);
            }
        }
        
        // Set vibration pattern
        if (adaptiveVibrationEnabled) {
            long[] pattern;
            
            switch (record.importance) {
                case URGENT:
                    // Strong repeated pattern for urgent
                    pattern = new long[]{0, 400, 200, 400, 200, 400, 200, 400};
                    break;
                case HIGH:
                    // Distinctive pattern for high
                    pattern = new long[]{0, 300, 150, 300, 150, 300};
                    break;
                case NORMAL:
                    // Standard pattern
                    pattern = new long[]{0, 250, 250, 250};
                    break;
                case LOW:
                default:
                    // Very subtle
                    pattern = new long[]{0, 100, 100};
                    break;
            }
            
            builder.setVibrate(pattern);
            
            // Also trigger vibration immediately for urgent notifications
            if (record.importance == NotificationImportance.URGENT) {
                triggerVibration(pattern);
            }
        }
        
        // Set lights
        switch (record.importance) {
            case URGENT:
                builder.setLights(Color.RED, 500, 500);
                break;
            case HIGH:
                builder.setLights(Color.YELLOW, 500, 1000);
                break;
            case NORMAL:
                builder.setLights(Color.GREEN, 500, 2000);
                break;
            case LOW:
                // No lights
                break;
        }
        
        // For urgent notifications, make them persistent
        if (record.importance == NotificationImportance.URGENT) {
            builder.setOngoing(true);
        }
    }
    
    /**
     * Trigger a vibration with the specified pattern
     */
    private void triggerVibration(long[] pattern) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
            } else {
                vibrator.vibrate(pattern, -1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error triggering vibration: " + e.getMessage());
        }
    }
    
    /**
     * Schedule delayed notification based on context
     */
    private void scheduleDelayedNotification(NotificationRecord record, Intent contentIntent) {
        // Determine appropriate delay based on importance and context
        long delayMillis;
        
        switch (record.importance) {
            case HIGH:
                // Short delay for high importance
                delayMillis = getSmartDelayForHighImportance();
                break;
            case NORMAL:
                // Medium delay for normal importance
                delayMillis = getSmartDelayForNormalImportance();
                break;
            case LOW:
                // Longer delay for low importance
                delayMillis = getSmartDelayForLowImportance();
                break;
            case URGENT:
            default:
                // No delay for urgent (shouldn't happen due to check in main method)
                delayMillis = 0;
                break;
        }
        
        if (delayMillis > 0) {
            Log.d(TAG, "Delaying notification: " + record.title + " for " + delayMillis + "ms");
            
            // Schedule notification
            final NotificationRecord finalRecord = record;
            final Intent finalIntent = contentIntent;
            scheduler.schedule(() -> 
                    mainHandler.post(() -> showNotificationInternal(finalRecord, finalIntent)), 
                    delayMillis, TimeUnit.MILLISECONDS);
        } else {
            // Show immediately
            showNotificationInternal(record, contentIntent);
        }
    }
    
    /**
     * Schedule notification escalation for important notifications
     */
    private void scheduleNotificationEscalation(NotificationRecord record) {
        // Only escalate urgent and high importance notifications
        if (record.importance != NotificationImportance.URGENT && 
                record.importance != NotificationImportance.HIGH) {
            return;
        }
        
        // Schedule escalation sequence
        long initialDelay = record.importance == NotificationImportance.URGENT ? 30000 : 60000;
        
        scheduler.schedule(() -> {
            // Check if notification is still active and not interacted with
            if (activeNotifications.containsKey(record.id) && !record.userInteracted) {
                escalateNotification(record);
            }
        }, initialDelay, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Escalate an unacknowledged important notification
     */
    private void escalateNotification(NotificationRecord record) {
        Log.d(TAG, "Escalating notification: " + record.title);
        
        // Increment volume
        if (adaptiveVolumeEnabled) {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            
            // Increase by 20% up to max
            int newVolume = Math.min(maxVolume, (int)(currentVolume * 1.2));
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, newVolume, 0);
        }
        
        // Stronger vibration
        if (adaptiveVibrationEnabled) {
            long[] strongPattern = {0, 500, 200, 500, 200, 500};
            triggerVibration(strongPattern);
        }
        
        // Schedule next escalation if still urgent
        if (record.importance == NotificationImportance.URGENT) {
            scheduler.schedule(() -> {
                if (activeNotifications.containsKey(record.id) && !record.userInteracted) {
                    escalateNotification(record);
                }
            }, 30000, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Get notification priority from importance
     */
    private int getPriorityFromImportance(NotificationImportance importance) {
        switch (importance) {
            case URGENT:
                return NotificationCompat.PRIORITY_MAX;
            case HIGH:
                return NotificationCompat.PRIORITY_HIGH;
            case NORMAL:
                return NotificationCompat.PRIORITY_DEFAULT;
            case LOW:
                return NotificationCompat.PRIORITY_LOW;
            default:
                return NotificationCompat.PRIORITY_DEFAULT;
        }
    }
    
    /**
     * Check if a notification should be delayed based on context
     */
    private boolean shouldDelayNotification() {
        // Check if user is in a call
        if (callHandlingSystem.getCurrentCallState() == CallHandlingSystem.CallState.ACTIVE || 
                callHandlingSystem.getCurrentCallState() == CallHandlingSystem.CallState.ANSWERED) {
            return true;
        }
        
        // Check do not disturb mode
        if (doNotDisturbRespected && 
                audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            return true;
        }
        
        // Check time of day (avoid notifications late at night)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 23 || hour < 7) {
            return true;
        }
        
        // Add additional context checks as needed
        
        return false;
    }
    
    /**
     * Get smart delay duration for high importance notifications
     */
    private long getSmartDelayForHighImportance() {
        // Check if in call
        if (callHandlingSystem.getCurrentCallState() == CallHandlingSystem.CallState.ACTIVE || 
                callHandlingSystem.getCurrentCallState() == CallHandlingSystem.CallState.ANSWERED) {
            // Delay until likely end of short call
            return 60000; // 1 minute
        }
        
        // Check time of day
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 22 || hour < 7) {
            // Late night/early morning - longer delay
            return 600000; // 10 minutes
        }
        
        // Normal conditions - short delay
        return 5000; // 5 seconds
    }
    
    /**
     * Get smart delay duration for normal importance notifications
     */
    private long getSmartDelayForNormalImportance() {
        // Check if in call
        if (callHandlingSystem.getCurrentCallState() == CallHandlingSystem.CallState.ACTIVE || 
                callHandlingSystem.getCurrentCallState() == CallHandlingSystem.CallState.ANSWERED) {
            // Delay until well after call likely ends
            return 300000; // 5 minutes
        }
        
        // Check time of day
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 22 || hour < 7) {
            // Late night/early morning - long delay
            return 7200000; // 2 hours
        }
        
        // Normal conditions - medium delay
        return 30000; // 30 seconds
    }
    
    /**
     * Get smart delay duration for low importance notifications
     */
    private long getSmartDelayForLowImportance() {
        // Check if in call
        if (callHandlingSystem.getCurrentCallState() == CallHandlingSystem.CallState.ACTIVE || 
                callHandlingSystem.getCurrentCallState() == CallHandlingSystem.CallState.ANSWERED) {
            // Long delay
            return 1800000; // 30 minutes
        }
        
        // Check time of day
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 22 || hour < 7) {
            // Late night/early morning - very long delay
            return 28800000; // 8 hours
        }
        
        // Check if multiple notifications recently
        int recentNotificationCount = 0;
        long recentThreshold = System.currentTimeMillis() - 3600000; // Last hour
        for (NotificationRecord record : notificationHistory) {
            if (record.timestamp > recentThreshold) {
                recentNotificationCount++;
            }
        }
        
        if (recentNotificationCount > 5) {
            // Many recent notifications, batch these up
            return 1800000; // 30 minutes
        }
        
        // Normal conditions
        return 180000; // 3 minutes
    }
    
    /**
     * Mark a notification as interacted with
     */
    public void markNotificationInteracted(int notificationId) {
        // Update record
        for (NotificationRecord record : notificationHistory) {
            if (record.id == notificationId) {
                record.userInteracted = true;
                break;
            }
        }
        
        // Remove from active notifications
        activeNotifications.remove(notificationId);
    }
    
    /**
     * Cancel a notification
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
        activeNotifications.remove(notificationId);
    }
    
    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
        activeNotifications.clear();
    }
    
    /**
     * Check if adaptive volume adjustment is enabled
     */
    public boolean isAdaptiveVolumeEnabled() {
        return adaptiveVolumeEnabled;
    }
    
    /**
     * Set adaptive volume adjustment
     */
    public void setAdaptiveVolumeEnabled(boolean enabled) {
        adaptiveVolumeEnabled = enabled;
        savePreferences();
    }
    
    /**
     * Check if adaptive vibration is enabled
     */
    public boolean isAdaptiveVibrationEnabled() {
        return adaptiveVibrationEnabled;
    }
    
    /**
     * Set adaptive vibration
     */
    public void setAdaptiveVibrationEnabled(boolean enabled) {
        adaptiveVibrationEnabled = enabled;
        savePreferences();
    }
    
    /**
     * Check if smart timing is enabled
     */
    public boolean isSmartTimingEnabled() {
        return smartTimingEnabled;
    }
    
    /**
     * Set smart timing
     */
    public void setSmartTimingEnabled(boolean enabled) {
        smartTimingEnabled = enabled;
        savePreferences();
    }
    
    /**
     * Check if do not disturb is respected
     */
    public boolean isDoNotDisturbRespected() {
        return doNotDisturbRespected;
    }
    
    /**
     * Set do not disturb respect
     */
    public void setDoNotDisturbRespected(boolean respected) {
        doNotDisturbRespected = respected;
        savePreferences();
    }
    
    /**
     * Get recent notification history
     */
    public List<NotificationRecord> getRecentNotifications(int limit) {
        int count = Math.min(limit, notificationHistory.size());
        return new ArrayList<>(notificationHistory.subList(0, count));
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        // Save preferences
        savePreferences();
        
        // Shut down scheduler
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
