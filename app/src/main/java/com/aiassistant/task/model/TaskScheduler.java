package com.aiassistant.task.model;

import android.content.Context;

import com.aiassistant.data.AppDatabase;
import com.aiassistant.data.models.ScheduledTask;

import java.util.List;

/**
 * Task scheduler to manage scheduled tasks
 */
public class TaskScheduler {
    
    private final Context context;
    private final AppDatabase database;
    
    /**
     * Constructor
     * 
     * @param context The application context
     */
    public TaskScheduler(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
    }
    
    /**
     * Schedule a task
     * 
     * @param task The task to schedule
     * @return The task ID
     */
    public long scheduleTask(ScheduledTask task) {
        if (task == null) {
            return -1;
        }
        
        return database.scheduledTaskDao().insert(task);
    }
    
    /**
     * Get all scheduled tasks
     * 
     * @return The tasks
     */
    public List<ScheduledTask> getAllTasks() {
        return database.scheduledTaskDao().getAllTasks();
    }
    
    /**
     * Get active tasks
     * 
     * @return The tasks
     */
    public List<ScheduledTask> getActiveTasks() {
        return database.scheduledTaskDao().getActiveTasks();
    }
    
    /**
     * Get task by ID
     * 
     * @param taskId The task ID
     * @return The task
     */
    public ScheduledTask getTaskById(long taskId) {
        return database.scheduledTaskDao().getById(taskId);
    }
    
    /**
     * Delete a task
     * 
     * @param taskId The task ID
     */
    public void deleteTask(long taskId) {
        ScheduledTask task = getTaskById(taskId);
        if (task != null) {
            database.scheduledTaskDao().delete(task);
        }
    }
    
    /**
     * Mark a task as completed
     * 
     * @param taskId The task ID
     */
    public void completeTask(long taskId) {
        ScheduledTask task = getTaskById(taskId);
        if (task != null) {
            task.setCompleted(true);
            database.scheduledTaskDao().update(task);
        }
    }
}
