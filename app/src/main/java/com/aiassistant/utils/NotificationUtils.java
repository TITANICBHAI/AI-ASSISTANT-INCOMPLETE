package com.aiassistant.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.aiassistant.R;
import com.aiassistant.ui.MainActivity;

/**
 * Utility class for creating and managing notifications
 */
public class NotificationUtils {
    // Channel IDs
    public static final String CHANNEL_ID_SERVICE = "ai_assistant_service";
    public static final String CHANNEL_ID_ALERTS = "ai_assistant_alerts";
    public static final String CHANNEL_ID_TASKS = "ai_assistant_tasks";
    
    // Channel Names
    private static final String CHANNEL_NAME_SERVICE = "AI Assistant Service";
    private static final String CHANNEL_NAME_ALERTS = "AI Assistant Alerts";
    private static final String CHANNEL_NAME_TASKS = "Scheduled Tasks";
    
    /**
     * Create notification channels for Android O and above
     * @param context Application context
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager != null) {
                // Service channel (low priority)
                NotificationChannel serviceChannel = new NotificationChannel(
                        CHANNEL_ID_SERVICE,
                        CHANNEL_NAME_SERVICE,
                        NotificationManager.IMPORTANCE_LOW
                );
                serviceChannel.setDescription("Background service notifications");
                serviceChannel.setShowBadge(false);
                notificationManager.createNotificationChannel(serviceChannel);
                
                // Alerts channel (high priority)
                NotificationChannel alertsChannel = new NotificationChannel(
                        CHANNEL_ID_ALERTS,
                        CHANNEL_NAME_ALERTS,
                        NotificationManager.IMPORTANCE_HIGH
                );
                alertsChannel.setDescription("Important AI assistant alerts");
                alertsChannel.setShowBadge(true);
                notificationManager.createNotificationChannel(alertsChannel);
                
                // Tasks channel (default priority)
                NotificationChannel tasksChannel = new NotificationChannel(
                        CHANNEL_ID_TASKS,
                        CHANNEL_NAME_TASKS,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                tasksChannel.setDescription("Scheduled task notifications");
                tasksChannel.setShowBadge(true);
                notificationManager.createNotificationChannel(tasksChannel);
            }
        }
    }
    
    /**
     * Create a notification for foreground services
     * @param context Application context
     * @param title Notification title
     * @param content Notification content
     * @param priority Notification priority
     * @return A notification object
     */
    public static Notification createForegroundServiceNotification(
            Context context, String title, String content, int priority) {
        
        // Create pending intent for when the notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | 
                    (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_SERVICE)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(priority)
                .setOngoing(true);
        
        return builder.build();
    }
    
    /**
     * Create and show an alert notification
     * @param context Application context
     * @param title Notification title
     * @param content Notification content
     * @param notificationId Unique notification ID
     */
    public static void showAlertNotification(
            Context context, String title, String content, int notificationId) {
        
        // Create pending intent for when the notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                notificationId, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | 
                    (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        
        // Show the notification
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
    
    /**
     * Create and show a task notification
     * @param context Application context
     * @param title Notification title
     * @param content Notification content
     * @param notificationId Unique notification ID
     */
    public static void showTaskNotification(
            Context context, String title, String content, int notificationId) {
        
        // Create pending intent for when the notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                notificationId, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | 
                    (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        
        // Show the notification
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
}