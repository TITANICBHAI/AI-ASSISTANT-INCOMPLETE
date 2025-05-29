package com.aiassistant.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.data.AppDatabase;
import com.aiassistant.data.models.ScheduledTask;
import com.aiassistant.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for executing scheduled tasks
 */
public class TaskExecutorService extends Service {
    
    private static final String TAG = "TaskExecutorService";
    
    private PowerManager.WakeLock wakeLock;
    private ExecutorService executor;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize executor
        executor = Executors.newCachedThreadPool();
        
        // Create notification channel
        createNotificationChannel();
        
        // Create wake lock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AIAssistant:TaskExecutorWakeLock");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get task ID from intent
        String taskId = intent != null ? intent.getStringExtra("task_id") : null;
        
        if (taskId == null) {
            Log.e(TAG, "Task ID is null");
            stopSelf();
            return START_NOT_STICKY;
        }
        
        // Start foreground
        startForeground(Constants.NOTIFICATION_ID_FOREGROUND, createNotification("Executing task..."));
        
        // Acquire wake lock
        wakeLock.acquire(10 * 60 * 1000L); // 10 minutes max
        
        // Execute task
        executeTask(taskId, () -> {
            // Release wake lock
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
            
            // Stop service
            stopSelf();
        });
        
        return START_REDELIVER_INTENT;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        // Shutdown executor
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    /**
     * Create the notification channel
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    Constants.NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    /**
     * Create a notification
     * 
     * @param text The notification text
     * @return The notification
     */
    private Notification createNotification(String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("AI Assistant")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        
        return builder.build();
    }
    
    /**
     * Update the notification
     * 
     * @param text The notification text
     */
    private void updateNotification(String text) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Constants.NOTIFICATION_ID_FOREGROUND, createNotification(text));
    }
    
    /**
     * Execute a task
     * 
     * @param taskId The task ID
     * @param callback The callback to run when done
     */
    private void executeTask(String taskId, Runnable callback) {
        executor.execute(() -> {
            try {
                // Get database
                AppDatabase database = AppDatabase.getInstance(this);
                
                // Get task
                ScheduledTask task = database.taskDao().getById(taskId);
                
                if (task == null) {
                    Log.e(TAG, "Task not found: " + taskId);
                    callback.run();
                    return;
                }
                
                // Mark task as in progress
                task.markAsInProgress();
                database.taskDao().update(task);
                
                // Update notification
                updateNotification("Executing task: " + task.getTaskType());
                
                // Execute task based on type
                String result;
                
                switch (task.getTaskType()) {
                    case "game_monitoring":
                        result = executeGameMonitoringTask(task);
                        break;
                    case "ai_training":
                        result = executeTrainingTask(task);
                        break;
                    case "game_interaction":
                        result = executeGameInteractionTask(task);
                        break;
                    case "data_cleanup":
                        result = executeCleanupTask(task);
                        break;
                    case "pattern_analysis":
                        result = executePatternAnalysisTask(task);
                        break;
                    default:
                        result = "Unknown task type: " + task.getTaskType();
                        break;
                }
                
                // Mark task as completed
                task.markAsCompleted(result);
                database.taskDao().update(task);
                
                Log.d(TAG, "Task completed: " + taskId + ", result: " + result);
                
                // Update notification
                updateNotification("Task completed: " + task.getTaskType());
                
            } catch (Exception e) {
                Log.e(TAG, "Error executing task: " + e.getMessage(), e);
                
                try {
                    // Mark task as failed
                    AppDatabase database = AppDatabase.getInstance(this);
                    ScheduledTask task = database.taskDao().getById(taskId);
                    
                    if (task != null) {
                        task.markAsFailed("Error: " + e.getMessage());
                        database.taskDao().update(task);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error updating task status: " + ex.getMessage(), ex);
                }
                
                // Update notification
                updateNotification("Task failed: " + e.getMessage());
                
            } finally {
                // Run callback
                callback.run();
            }
        });
    }
    
    /**
     * Execute a game monitoring task
     * 
     * @param task The task
     * @return The result
     */
    private String executeGameMonitoringTask(ScheduledTask task) {
        try {
            // Parse parameters
            JSONObject params = new JSONObject(task.getParams());
            String gameId = params.optString("gameId", "");
            
            // Monitor game
            // In a real implementation, this would collect statistics, check for issues, etc.
            
            return "Monitored game " + gameId;
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing task parameters: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Execute a training task
     * 
     * @param task The task
     * @return The result
     */
    private String executeTrainingTask(ScheduledTask task) {
        try {
            // Parse parameters
            JSONObject params = new JSONObject(task.getParams());
            String gameId = params.optString("gameId", "");
            int steps = params.optInt("steps", 1000);
            
            // Execute training
            AIStateManager aiManager = AIStateManager.getInstance(this);
            aiManager.trainModels(gameId, steps);
            
            return "Trained model for game " + gameId + " with " + steps + " steps";
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing task parameters: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Execute a game interaction task
     * 
     * @param task The task
     * @return The result
     */
    private String executeGameInteractionTask(ScheduledTask task) {
        try {
            // Parse parameters
            JSONObject params = new JSONObject(task.getParams());
            String gameId = params.optString("gameId", "");
            String interactionType = params.optString("type", "");
            
            // Execute interaction
            // In a real implementation, this would interact with the game
            
            return "Executed interaction " + interactionType + " for game " + gameId;
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing task parameters: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Execute a cleanup task
     * 
     * @param task The task
     * @return The result
     */
    private String executeCleanupTask(ScheduledTask task) {
        try {
            // Get database
            AppDatabase database = AppDatabase.getInstance(this);
            
            // Delete completed and failed tasks
            int taskCount = database.taskDao().deleteCompletedTasks();
            
            // Delete old log entries
            // In a real implementation, this would also clean up other data
            
            return "Cleaned up " + taskCount + " completed tasks";
            
        } catch (Exception e) {
            Log.e(TAG, "Error executing cleanup task: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Execute a pattern analysis task
     * 
     * @param task The task
     * @return The result
     */
    private String executePatternAnalysisTask(ScheduledTask task) {
        try {
            // Parse parameters
            JSONObject params = new JSONObject(task.getParams());
            String gameId = params.optString("gameId", "");
            
            // Execute pattern analysis
            // In a real implementation, this would analyze patterns in game data
            
            return "Analyzed patterns for game " + gameId;
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing task parameters: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
}
