package com.aiassistant.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIAssistantApplication;
import com.aiassistant.ui.MainActivity;
import com.aiassistant.utils.Constants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Foreground service that handles continuous AI processing
 * This service ensures the AI assistant can continue running
 * even when the app is in the background.
 */
public class AIProcessingService extends Service {
    private static final String TAG = "AIProcessingService";
    private static final int NOTIFICATION_ID = 1;
    private ScheduledExecutorService executor;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AI Processing Service created");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AI Processing Service started");
        
        // Start as a foreground service with notification
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Initialize processing threads
        if (!isRunning) {
            startProcessing();
        }
        
        // Return sticky so it restarts if killed
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "AI Processing Service destroyed");
        stopProcessing();
        super.onDestroy();
    }

    /**
     * Start the background processing threads
     */
    private void startProcessing() {
        isRunning = true;
        
        // Create a scheduled executor for periodic tasks
        executor = Executors.newScheduledThreadPool(2);
        
        // Schedule AI processing tasks
        executor.scheduleAtFixedRate(() -> {
            try {
                if (AIAssistantApplication.getInstance() != null && 
                    AIAssistantApplication.getInstance().getAIStateManager() != null) {
                    AIAssistantApplication.getInstance().getAIStateManager().processFrame();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in AI processing", e);
            }
        }, 1000, Constants.DETECTION_INTERVAL_MS, TimeUnit.MILLISECONDS);
        
        Log.d(TAG, "AI processing started with interval: " + Constants.DETECTION_INTERVAL_MS + "ms");
    }

    /**
     * Stop all background processing
     */
    private void stopProcessing() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        isRunning = false;
        Log.d(TAG, "AI processing stopped");
    }

    /**
     * Create the notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    "AI Assistant Service",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Allows the AI Assistant to run in the background");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Create the persistent notification for the foreground service
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("AI Gaming Assistant")
                .setContentText("Running in the background")
                .setSmallIcon(R.drawable.ic_ai_assistant)
                .setContentIntent(pendingIntent)
                .build();
    }
}
