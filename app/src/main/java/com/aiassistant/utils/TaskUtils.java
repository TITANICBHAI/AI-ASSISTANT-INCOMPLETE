package com.aiassistant.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.aiassistant.data.AppDatabase;
import com.aiassistant.data.models.ScheduledTask;
import com.aiassistant.receivers.TaskAlarmReceiver;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for task scheduling and management
 */
public class TaskUtils {
    private static final String TAG = "TaskUtils";
    
    // Task type constants
    public static final String TASK_TYPE_GAME_MONITORING = "game_monitoring";
    public static final String TASK_TYPE_AI_TRAINING = "ai_training";
    public static final String TASK_TYPE_GAME_INTERACTION = "game_interaction";
    public static final String TASK_TYPE_DATA_CLEANUP = "data_cleanup";
    public static final String TASK_TYPE_PATTERN_ANALYSIS = "pattern_analysis";
    
    // Task status constants
    public static final int TASK_STATUS_PENDING = 0;
    public static final int TASK_STATUS_IN_PROGRESS = 1;
    public static final int TASK_STATUS_COMPLETED = 2;
    public static final int TASK_STATUS_FAILED = 3;
    public static final int TASK_STATUS_CANCELLED = 4;
    
    /**
     * Schedule a task
     * 
     * @param context The context
     * @param task The task
     */
    public static void scheduleTask(Context context, ScheduledTask task) {
        if (context == null || task == null) {
            Log.e(TAG, "Invalid context or task");
            return;
        }
        
        try {
            // Get alarm manager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager not available");
                return;
            }
            
            // Create intent
            Intent intent = new Intent(context, TaskAlarmReceiver.class);
            intent.setAction("com.aiassistant.ACTION_EXECUTE_TASK");
            intent.putExtra("task_id", task.getId());
            intent.putExtra("task_type", task.getTaskType());
            
            // Create pending intent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.getId().hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT |
                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));
            
            // Schedule alarm
            long scheduledTime = task.getScheduledTime();
            if (scheduledTime <= System.currentTimeMillis()) {
                // If scheduled time is in the past, run immediately
                scheduledTime = System.currentTimeMillis() + 1000;
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent);
            }
            
            Log.d(TAG, "Scheduled task " + task.getId() + " at " + new Date(scheduledTime));
            
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling task: " + e.getMessage(), e);
        }
    }
    
    /**
     * Schedule all pending tasks
     * 
     * @param context The context
     */
    public static void scheduleAllPendingTasks(Context context) {
        if (context == null) {
            Log.e(TAG, "Invalid context");
            return;
        }
        
        try {
            // Get database
            AppDatabase database = AppDatabase.getInstance(context);
            
            // Get pending tasks
            List<ScheduledTask> pendingTasks = database.taskDao().getPendingTasks();
            
            Log.d(TAG, "Found " + pendingTasks.size() + " pending tasks");
            
            // Schedule each task
            for (ScheduledTask task : pendingTasks) {
                scheduleTask(context, task);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling pending tasks: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cancel a task
     * 
     * @param context The context
     * @param taskId The task ID
     */
    public static void cancelTask(Context context, String taskId) {
        if (context == null || taskId == null) {
            Log.e(TAG, "Invalid context or task ID");
            return;
        }
        
        try {
            // Get alarm manager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager not available");
                return;
            }
            
            // Create intent
            Intent intent = new Intent(context, TaskAlarmReceiver.class);
            intent.setAction("com.aiassistant.ACTION_EXECUTE_TASK");
            
            // Create pending intent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskId.hashCode(),
                    intent,
                    PendingIntent.FLAG_NO_CREATE |
                            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));
            
            // Cancel alarm
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                Log.d(TAG, "Cancelled task " + taskId);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling task: " + e.getMessage(), e);
        }
    }
    
    /**
     * Reschedule a task
     * 
     * @param context The context
     * @param task The task
     */
    public static void rescheduleTask(Context context, ScheduledTask task) {
        if (context == null || task == null) {
            Log.e(TAG, "Invalid context or task");
            return;
        }
        
        try {
            // Cancel existing task
            cancelTask(context, task.getId());
            
            // Schedule task
            scheduleTask(context, task);
            
        } catch (Exception e) {
            Log.e(TAG, "Error rescheduling task: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a daily task that runs at a specific time
     * 
     * @param taskType The task type
     * @param params The task parameters
     * @param hour The hour (0-23)
     * @param minute The minute (0-59)
     * @return The task
     */
    public static ScheduledTask createDailyTask(
            String taskType,
            String params,
            int hour,
            int minute) {
        
        // Create task
        ScheduledTask task = new ScheduledTask();
        task.setTaskType(taskType);
        task.setParams(params);
        task.setPriority(1);
        
        // Calculate scheduled time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        // If time is in the past, schedule for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        task.setScheduledTime(calendar.getTimeInMillis());
        
        return task;
    }
    
    /**
     * Create a task that runs after a delay
     * 
     * @param taskType The task type
     * @param params The task parameters
     * @param delayMinutes The delay in minutes
     * @return The task
     */
    public static ScheduledTask createDelayedTask(
            String taskType,
            String params,
            int delayMinutes) {
        
        // Create task
        ScheduledTask task = new ScheduledTask();
        task.setTaskType(taskType);
        task.setParams(params);
        task.setPriority(1);
        
        // Calculate scheduled time
        long scheduledTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(delayMinutes);
        task.setScheduledTime(scheduledTime);
        
        return task;
    }
    
    /**
     * Create a training task
     * 
     * @param gameId The game ID
     * @param steps The training steps
     * @param delayMinutes The delay in minutes
     * @return The task
     */
    public static ScheduledTask createTrainingTask(
            String gameId,
            int steps,
            int delayMinutes) {
        
        // Create params
        String params = "{\"gameId\":\"" + gameId + "\",\"steps\":" + steps + "}";
        
        return createDelayedTask(TASK_TYPE_AI_TRAINING, params, delayMinutes);
    }
}
