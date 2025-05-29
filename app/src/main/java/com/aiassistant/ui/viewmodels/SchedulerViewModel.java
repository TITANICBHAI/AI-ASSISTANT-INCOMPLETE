package com.aiassistant.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aiassistant.data.models.ScheduledTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for the Task Scheduler screen which manages scheduled AI tasks.
 */
public class SchedulerViewModel extends AndroidViewModel {
    
    private final MutableLiveData<List<ScheduledTask>> scheduledTasks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ScheduledTask>> pendingTasks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ScheduledTask>> completedTasks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isSchedulerActive = new MutableLiveData<>(true);
    private final MutableLiveData<String> schedulerStatusMessage = new MutableLiveData<>("");
    
    /**
     * Constructor
     */
    public SchedulerViewModel(@NonNull Application application) {
        super(application);
        
        // Initialize with sample tasks for demonstration
        initializeSampleTasks();
        
        updateStatusMessage();
    }
    
    /**
     * Get observable for all scheduled tasks
     */
    public LiveData<List<ScheduledTask>> getScheduledTasks() {
        return scheduledTasks;
    }
    
    /**
     * Get observable for pending tasks
     */
    public LiveData<List<ScheduledTask>> getPendingTasks() {
        return pendingTasks;
    }
    
    /**
     * Get observable for completed tasks
     */
    public LiveData<List<ScheduledTask>> getCompletedTasks() {
        return completedTasks;
    }
    
    /**
     * Get observable for scheduler active state
     */
    public LiveData<Boolean> getIsSchedulerActive() {
        return isSchedulerActive;
    }
    
    /**
     * Get observable for scheduler status message
     */
    public LiveData<String> getSchedulerStatusMessage() {
        return schedulerStatusMessage;
    }
    
    /**
     * Initialize sample tasks for demonstration
     */
    private void initializeSampleTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        
        // Create a few sample future tasks
        Calendar calendar = Calendar.getInstance();
        
        // Task 1: One hour from now
        calendar.add(Calendar.HOUR, 1);
        tasks.add(new ScheduledTask(
                "Daily game check-in",
                "Automatically check in to collect daily rewards",
                "com.example.game",
                "daily_checkin",
                calendar.getTime(),
                true,
                24 * 60 // Daily recurrence (24 hours)
        ));
        
        // Task 2: 30 minutes from now
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 30);
        tasks.add(new ScheduledTask(
                "Send message",
                "Send automated reminder message",
                "com.android.messaging",
                "send_reminder",
                calendar.getTime(),
                false,
                0 // No recurrence
        ));
        
        // Task 3: Tomorrow
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        tasks.add(new ScheduledTask(
                "Morning routine",
                "Check weather, calendar, and news",
                "multiple_apps",
                "morning_check",
                calendar.getTime(),
                true,
                24 * 60 // Daily recurrence
        ));
        
        // Add a completed task
        calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -2);
        ScheduledTask completedTask = new ScheduledTask(
                "System maintenance",
                "Clear cache and check for updates",
                "system",
                "maintenance",
                calendar.getTime(),
                true,
                7 * 24 * 60 // Weekly recurrence
        );
        completedTask.setLastExecutedTime(new Date());
        tasks.add(completedTask);
        
        scheduledTasks.setValue(tasks);
        updateTaskLists();
    }
    
    /**
     * Update pending and completed task lists
     */
    private void updateTaskLists() {
        List<ScheduledTask> tasks = scheduledTasks.getValue();
        if (tasks == null) return;
        
        List<ScheduledTask> pending = new ArrayList<>();
        List<ScheduledTask> completed = new ArrayList<>();
        
        Date now = new Date();
        
        for (ScheduledTask task : tasks) {
            if (task.getLastExecutedTime() != null) {
                if (task.isRecurring() && task.isDue(now)) {
                    pending.add(task);
                } else if (!task.isRecurring()) {
                    completed.add(task);
                } else {
                    // Recurring task that will run again in the future
                    pending.add(task);
                }
            } else {
                // Never executed
                pending.add(task);
            }
        }
        
        pendingTasks.setValue(pending);
        completedTasks.setValue(completed);
    }
    
    /**
     * Add a new scheduled task
     */
    public void addTask(ScheduledTask task) {
        List<ScheduledTask> tasks = scheduledTasks.getValue();
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        
        tasks.add(task);
        scheduledTasks.setValue(tasks);
        
        updateTaskLists();
        updateStatusMessage();
    }
    
    /**
     * Delete a scheduled task
     */
    public void deleteTask(ScheduledTask task) {
        List<ScheduledTask> tasks = scheduledTasks.getValue();
        if (tasks == null) return;
        
        if (tasks.remove(task)) {
            scheduledTasks.setValue(tasks);
            updateTaskLists();
            updateStatusMessage();
        }
    }
    
    /**
     * Toggle task enabled state
     */
    public void toggleTaskEnabled(ScheduledTask task, boolean enabled) {
        task.setEnabled(enabled);
        
        // Force update
        List<ScheduledTask> tasks = scheduledTasks.getValue();
        if (tasks != null) {
            scheduledTasks.setValue(new ArrayList<>(tasks));
        }
        
        updateTaskLists();
        updateStatusMessage();
    }
    
    /**
     * Run a task immediately
     */
    public void runTaskNow(ScheduledTask task) {
        // Mark as executed now
        task.setLastExecutedTime(new Date());
        
        // Force update
        List<ScheduledTask> tasks = scheduledTasks.getValue();
        if (tasks != null) {
            scheduledTasks.setValue(new ArrayList<>(tasks));
        }
        
        updateTaskLists();
        updateStatusMessage();
    }
    
    /**
     * Toggle scheduler active state
     */
    public void setSchedulerActive(boolean active) {
        isSchedulerActive.setValue(active);
        updateStatusMessage();
    }
    
    /**
     * Update status message based on current state
     */
    private void updateStatusMessage() {
        String message;
        
        if (!isSchedulerActive.getValue()) {
            message = "Task scheduler is currently inactive. Enable to run scheduled tasks.";
        } else {
            List<ScheduledTask> pending = pendingTasks.getValue();
            int pendingCount = pending != null ? pending.size() : 0;
            
            List<ScheduledTask> completed = completedTasks.getValue();
            int completedCount = completed != null ? completed.size() : 0;
            
            message = "Scheduler active: " + pendingCount + " pending tasks, " + 
                     completedCount + " completed tasks";
            
            // Find next scheduled task
            if (pending != null && !pending.isEmpty()) {
                ScheduledTask nextTask = findNextDueTask(pending);
                if (nextTask != null) {
                    message += "\nNext task: " + nextTask.getTaskName() + " at " + 
                             nextTask.getScheduledTime();
                }
            }
        }
        
        schedulerStatusMessage.setValue(message);
    }
    
    /**
     * Find the next due task from a list
     */
    private ScheduledTask findNextDueTask(List<ScheduledTask> tasks) {
        if (tasks == null || tasks.isEmpty()) return null;
        
        ScheduledTask nextTask = null;
        Date earliestTime = null;
        Date now = new Date();
        
        for (ScheduledTask task : tasks) {
            if (!task.isEnabled()) continue;
            
            Date taskTime = task.getNextExecutionTime();
            if (taskTime.after(now) && (earliestTime == null || taskTime.before(earliestTime))) {
                earliestTime = taskTime;
                nextTask = task;
            }
        }
        
        return nextTask;
    }
}