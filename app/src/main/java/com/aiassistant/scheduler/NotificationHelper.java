package com.aiassistant.scheduler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.aiassistant.MainActivity;
import com.aiassistant.R;
import com.aiassistant.data.models.GameType;
import com.aiassistant.data.models.Task;

/**
 * Helper class for creating and managing notifications
 */
public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    
    // Notification channel IDs
    private static final String CHANNEL_ID_ALERTS = "ai_assistant_alerts";
    private static final String CHANNEL_ID_TASKS = "ai_assistant_tasks";
    private static final String CHANNEL_ID_UPDATES = "ai_assistant_updates";
    
    // Notification IDs
    private static final int NOTIFICATION_ID_AI_STATUS = 1001;
    private static final int NOTIFICATION_ID_TASK_REMINDER = 2001;
    private static final int NOTIFICATION_ID_GAME_ALERT = 3001;
    
    // Notification manager
    private final NotificationManager notificationManager;
    private final Context context;
    
    /**
     * Constructor
     * 
     * @param context Application context
     */
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels();
    }
    
    /**
     * Create notification channels (for Android O and above)
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Alerts channel (high importance)
            NotificationChannel alertsChannel = new NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    "AI Alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            alertsChannel.setDescription("Critical AI assistant alerts");
            alertsChannel.enableLights(true);
            alertsChannel.setLightColor(Color.RED);
            alertsChannel.enableVibration(true);
            notificationManager.createNotificationChannel(alertsChannel);
            
            // Tasks channel (default importance)
            NotificationChannel tasksChannel = new NotificationChannel(
                    CHANNEL_ID_TASKS,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT);
            tasksChannel.setDescription("Reminders for scheduled tasks");
            tasksChannel.enableLights(true);
            tasksChannel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(tasksChannel);
            
            // Updates channel (low importance)
            NotificationChannel updatesChannel = new NotificationChannel(
                    CHANNEL_ID_UPDATES,
                    "AI Updates",
                    NotificationManager.IMPORTANCE_LOW);
            updatesChannel.setDescription("General updates from AI assistant");
            notificationManager.createNotificationChannel(updatesChannel);
        }
    }
    
    /**
     * Show AI status notification
     * 
     * @param title Notification title
     * @param message Notification message
     * @param aiMode Current AI mode
     */
    public void showAIStatusNotification(String title, String message, String aiMode) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID_AI_STATUS,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Add extra style based on AI mode
        if ("AUTO".equals(aiMode)) {
            builder.setColor(Color.GREEN);
        } else if ("LEARNING".equals(aiMode)) {
            builder.setColor(Color.BLUE);
        }
        
        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID_AI_STATUS, notification);
    }
    
    /**
     * Show task reminder notification
     * 
     * @param task The task to remind about
     */
    public void showTaskReminderNotification(Task task) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID_TASK_REMINDER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Task Reminder: " + task.getTitle())
                .setContentText(task.getDescription())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Add priority based on task priority
        if (task.getPriority() >= 2) { // High or Critical
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        
        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID_TASK_REMINDER + task.getId(), notification);
    }
    
    /**
     * Show game alert notification
     * 
     * @param gameType The game type
     * @param title Notification title
     * @param message Notification message
     */
    public void showGameAlertNotification(GameType gameType, String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID_GAME_ALERT,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        
        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID_GAME_ALERT, notification);
    }
    
    /**
     * Cancel a specific notification
     * 
     * @param notificationId The notification ID to cancel
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
    
    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
    
    /**
     * Create a foreground service notification
     * 
     * @param title Notification title
     * @param message Notification message
     * @return Notification object for the service
     */
    public Notification createForegroundServiceNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID_AI_STATUS,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }
}
