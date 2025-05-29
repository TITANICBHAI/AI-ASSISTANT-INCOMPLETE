package com.aiassistant.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.aiassistant.data.TaskRepository;
import com.aiassistant.data.models.Task;
import com.aiassistant.scheduler.NotificationHelper;
import com.aiassistant.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Broadcast receiver for task alarms
 */
public class TaskAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "TaskAlarmReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received task alarm");
        
        // Get task details from intent
        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e(TAG, "No extras found in intent");
            return;
        }
        
        int taskId = extras.getInt("task_id", -1);
        String taskData = extras.getString("task_data", null);
        
        if (taskId == -1 || taskData == null) {
            Log.e(TAG, "Invalid task data in intent");
            return;
        }
        
        // Get task from JSON data
        try {
            JSONObject taskJson = new JSONObject(taskData);
            Task task = parseTaskFromJson(taskJson);
            
            if (task != null) {
                // Show notification for the task
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.showTaskReminderNotification(task);
                
                // Update task in repository (e.g., update reminder status)
                TaskRepository repository = TaskRepository.getInstance(context);
                repository.getTaskById(taskId, loadedTask -> {
                    if (loadedTask != null) {
                        loadedTask.setAdditionalData(JSONUtils.getString(taskJson, "updated_data", loadedTask.getAdditionalData()));
                        repository.updateTask(loadedTask);
                    }
                });
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing task JSON: " + e.getMessage());
        }
    }
    
    /**
     * Parse a task from JSON data
     * 
     * @param json JSON object with task data
     * @return Task object or null if invalid
     */
    private Task parseTaskFromJson(JSONObject json) {
        try {
            Task task = new Task();
            task.setId(json.getInt("id"));
            task.setTitle(json.getString("title"));
            task.setDescription(json.optString("description", ""));
            task.setPriority(json.optInt("priority", 1));
            
            if (json.has("game_id")) {
                task.setGameId(json.getString("game_id"));
            }
            
            if (json.has("additional_data")) {
                task.setAdditionalData(json.getString("additional_data"));
            }
            
            return task;
        } catch (JSONException e) {
            Log.e(TAG, "Error creating task from JSON: " + e.getMessage());
            return null;
        }
    }
}
