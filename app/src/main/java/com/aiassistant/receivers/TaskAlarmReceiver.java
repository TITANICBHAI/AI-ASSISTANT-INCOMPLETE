package com.aiassistant.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.data.AppDatabase;
import com.aiassistant.data.models.ScheduledTask;
import com.aiassistant.services.TaskExecutorService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Receiver for task alarms
 */
public class TaskAlarmReceiver extends BroadcastReceiver {
    
    private static final String TAG = "TaskAlarmReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received task alarm");
        
        if (intent == null || intent.getAction() == null) {
            Log.e(TAG, "Invalid intent or action");
            return;
        }
        
        if ("com.aiassistant.ACTION_EXECUTE_TASK".equals(intent.getAction())) {
            String taskId = intent.getStringExtra("task_id");
            String taskType = intent.getStringExtra("task_type");
            
            if (taskId == null || taskType == null) {
                Log.e(TAG, "Invalid task ID or type");
                return;
            }
            
            // Execute task in background
            Executor executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            
            executor.execute(() -> {
                try {
                    // Get database
                    AppDatabase database = AppDatabase.getInstance(context);
                    
                    // Get task
                    ScheduledTask task = database.taskDao().getById(taskId);
                    
                    if (task == null) {
                        Log.e(TAG, "Task not found: " + taskId);
                        return;
                    }
                    
                    // Mark task as in progress
                    task.markAsInProgress();
                    database.taskDao().update(task);
                    
                    // Execute task based on type
                    String result;
                    
                    switch (taskType) {
                        case "ai_training":
                            result = executeTrainingTask(context, task);
                            break;
                        case "data_cleanup":
                            result = executeCleanupTask(context, task);
                            break;
                        default:
                            // Start service for other tasks
                            Intent serviceIntent = new Intent(context, TaskExecutorService.class);
                            serviceIntent.putExtra("task_id", taskId);
                            
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent);
                            } else {
                                context.startService(serviceIntent);
                            }
                            
                            return;
                    }
                    
                    // Mark task as completed
                    task.markAsCompleted(result);
                    database.taskDao().update(task);
                    
                    Log.d(TAG, "Task completed: " + taskId + ", result: " + result);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error executing task: " + e.getMessage(), e);
                }
            });
        }
    }
    
    /**
     * Execute a training task
     * 
     * @param context The context
     * @param task The task
     * @return The result
     */
    private String executeTrainingTask(Context context, ScheduledTask task) {
        try {
            // Parse parameters
            JSONObject params = new JSONObject(task.getParams());
            String gameId = params.optString("gameId", "");
            int steps = params.optInt("steps", 1000);
            
            // Execute training
            AIStateManager aiManager = AIStateManager.getInstance(context);
            aiManager.trainModels(gameId, steps);
            
            return "Trained model for game " + gameId + " with " + steps + " steps";
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing task parameters: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Execute a cleanup task
     * 
     * @param context The context
     * @param task The task
     * @return The result
     */
    private String executeCleanupTask(Context context, ScheduledTask task) {
        try {
            // Get database
            AppDatabase database = AppDatabase.getInstance(context);
            
            // Delete completed and failed tasks
            int count = database.taskDao().deleteCompletedTasks();
            
            return "Cleaned up " + count + " completed tasks";
            
        } catch (Exception e) {
            Log.e(TAG, "Error executing cleanup task: " + e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
}
