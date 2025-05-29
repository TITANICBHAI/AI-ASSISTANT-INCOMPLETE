package com.aiassistant.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.aiassistant.MainActivity;
import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;

public class BackgroundMonitoringService extends Service {
    private static final String TAG = "BackgroundService";
    private static final String CHANNEL_ID = "AIAssistantChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private AIStateManager aiStateManager;
    private boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BackgroundMonitoringService created");
        
        // Create notification channel for Android O and above
        createNotificationChannel();
        
        // Initialize AI state manager
        aiStateManager = AIStateManager.getInstance(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BackgroundMonitoringService started");
        
        // Start as foreground service with notification
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Perform initialization
        initialize();
        
        // Return sticky to automatically restart service if killed
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        isRunning = false;
        Log.d(TAG, "BackgroundMonitoringService destroyed");
        super.onDestroy();
    }
    
    /**
     * Initialize service operations
     */
    private void initialize() {
        isRunning = true;
        
        // Initialize AI state
        if (aiStateManager != null) {
            aiStateManager.initialize();
        }
        
        Log.d(TAG, "BackgroundMonitoringService initialized");
    }
    
    /**
     * Create notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "AI Assistant Service",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Keeps the AI Assistant running in the background");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    /**
     * Create notification for foreground service
     * @return Notification object
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("AI Assistant")
                .setContentText("AI Assistant is running in the background")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        
        return builder.build();
    }
}
