package com.aiassistant.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.aiassistant.models.ScheduledTask;
import com.aiassistant.scheduler.AdvancedTaskScheduler;
import com.aiassistant.scheduler.NotificationHelper;
import com.aiassistant.scheduler.TaskExecutionReceiver;
import com.aiassistant.services.AIBackgroundService;

/**
 * Receiver for scheduled task alarms
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received for scheduled task");
        
        // Get the task ID from the intent
        long taskId = intent.getLongExtra("taskId", -1);
        if (taskId == -1) {
            Log.e(TAG, "Invalid task ID received");
            return;
        }
        
        // Get the task details
        AdvancedTaskScheduler scheduler = new AdvancedTaskScheduler(context);
        ScheduledTask task = scheduler.getTaskById(taskId);
        
        if (task == null) {
            Log.e(TAG, "Task not found for ID: " + taskId);
            return;
        }
        
        Log.i(TAG, "Executing scheduled task: " + task.getName() + " (ID: " + taskId + ")");
        
        // Update task status to in progress
        scheduler.updateTaskStatus(taskId, ScheduledTask.STATUS_IN_PROGRESS);
        
        // Create a notification for the task
        NotificationHelper notificationHelper = new NotificationHelper(context);
        int notificationId = (int) taskId;
        notificationHelper.showTaskExecutionNotification(task, notificationId);
        
        // Start the execution based on task type
        executeTask(context, task);
    }
    
    /**
     * Execute the task based on its type
     */
    private void executeTask(Context context, ScheduledTask task) {
        // Create an intent for the TaskExecutionReceiver
        Intent executionIntent = new Intent(context, TaskExecutionReceiver.class);
        executionIntent.putExtra("taskId", task.getId());
        
        // Based on task type, handle differently
        switch (task.getTaskType()) {
            case ScheduledTask.TYPE_APP_AUTOMATION:
                // Start the AI assistant service to handle automation
                startAIServiceForTask(context, task);
                break;
                
            case ScheduledTask.TYPE_MESSAGE_SEND:
                // Prepare to send messages
                executionIntent.setAction(TaskExecutionReceiver.ACTION_SEND_MESSAGE);
                context.sendBroadcast(executionIntent);
                break;
                
            case ScheduledTask.TYPE_GAME_PLAY:
                // Start game play automation
                startAIServiceForTask(context, task);
                break;
                
            case ScheduledTask.TYPE_CUSTOM_ACTION:
                // Execute custom action
                executionIntent.setAction(TaskExecutionReceiver.ACTION_CUSTOM);
                context.sendBroadcast(executionIntent);
                break;
                
            default:
                // Unknown task type
                Log.e(TAG, "Unknown task type: " + task.getTaskType());
                AdvancedTaskScheduler scheduler = new AdvancedTaskScheduler(context);
                scheduler.updateTaskStatus(task.getId(), ScheduledTask.STATUS_FAILED);
                break;
        }
    }
    
    /**
     * Start the AI background service to handle the task
     */
    private void startAIServiceForTask(Context context, ScheduledTask task) {
        Intent serviceIntent = new Intent(context, AIBackgroundService.class);
        serviceIntent.putExtra("taskId", task.getId());
        serviceIntent.putExtra("isScheduledTask", true);
        
        if (task.getTaskType() == ScheduledTask.TYPE_APP_AUTOMATION) {
            serviceIntent.putExtra("operation", AIBackgroundService.OPERATION_APP_AUTOMATION);
        } else if (task.getTaskType() == ScheduledTask.TYPE_GAME_PLAY) {
            serviceIntent.putExtra("operation", AIBackgroundService.OPERATION_GAME_PLAY);
        }
        
        if (task.getTargetPackage() != null) {
            serviceIntent.putExtra("targetPackage", task.getTargetPackage());
        }
        
        // Start the service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
        
        Log.d(TAG, "Started AI service for task: " + task.getName());
    }
}