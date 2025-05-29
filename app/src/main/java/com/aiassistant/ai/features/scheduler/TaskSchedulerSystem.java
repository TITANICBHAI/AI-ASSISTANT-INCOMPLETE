package com.aiassistant.ai.features.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The TaskSchedulerSystem provides scheduling capabilities for AI tasks,
 * reminders, study sessions, and other time-based activities.
 */
public class TaskSchedulerSystem {
    private static final String TAG = "TaskSchedulerSystem";
    
    private Context context;
    private ExecutorService schedulerExecutor;
    private Handler mainHandler;
    private AlarmManager alarmManager;
    private Map<String, ScheduledTask> tasks;
    private List<TaskSchedulerListener> listeners;
    private boolean isRunning;
    
    /**
     * Constructor
     * @param context Android context
     */
    public TaskSchedulerSystem(Context context) {
        this.context = context;
        this.schedulerExecutor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.tasks = new HashMap<>();
        this.listeners = new ArrayList<>();
        this.isRunning = false;
        
        Log.i(TAG, "TaskSchedulerSystem initialized");
    }
    
    /**
     * Start the Task Scheduler system
     * @return Success status
     */
    public boolean start() {
        if (isRunning) {
            return true;
        }
        
        this.isRunning = true;
        
        // Start scheduler thread
        schedulerExecutor.submit(this::schedulerLoop);
        
        Log.i(TAG, "TaskSchedulerSystem started");
        return true;
    }
    
    /**
     * Stop the Task Scheduler system
     * @return Success status
     */
    public boolean stop() {
        this.isRunning = false;
        Log.i(TAG, "TaskSchedulerSystem stopped");
        return true;
    }
    
    /**
     * Schedule a new task
     * @param task Task to schedule
     * @return Task ID or null if failed
     */
    public String scheduleTask(ScheduledTask task) {
        if (task == null || task.getScheduledTime() == null) {
            Log.e(TAG, "Cannot schedule null task or task with null time");
            return null;
        }
        
        try {
            // Generate ID if not provided
            if (task.getId() == null || task.getId().isEmpty()) {
                task.setId(generateTaskId(task));
            }
            
            // Store task
            tasks.put(task.getId(), task);
            
            // Schedule system alarm if needed
            if (task.isUseSystemAlarm()) {
                scheduleSystemAlarm(task);
            }
            
            Log.i(TAG, "Task scheduled: " + task.getTitle() + " at " + task.getScheduledTime().getTime());
            
            // Notify listeners
            for (TaskSchedulerListener listener : listeners) {
                listener.onTaskScheduled(task);
            }
            
            return task.getId();
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling task", e);
            return null;
        }
    }
    
    /**
     * Cancel a scheduled task
     * @param taskId Task ID
     * @return Success status
     */
    public boolean cancelTask(String taskId) {
        if (taskId == null || !tasks.containsKey(taskId)) {
            return false;
        }
        
        try {
            ScheduledTask task = tasks.get(taskId);
            
            // Cancel system alarm if used
            if (task.isUseSystemAlarm()) {
                cancelSystemAlarm(task);
            }
            
            // Remove task
            tasks.remove(taskId);
            
            Log.i(TAG, "Task canceled: " + task.getTitle());
            
            // Notify listeners
            for (TaskSchedulerListener listener : listeners) {
                listener.onTaskCanceled(task);
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error canceling task", e);
            return false;
        }
    }
    
    /**
     * Get all scheduled tasks
     * @return List of tasks
     */
    public List<ScheduledTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }
    
    /**
     * Get tasks scheduled for today
     * @return List of today's tasks
     */
    public List<ScheduledTask> getTodayTasks() {
        List<ScheduledTask> todayTasks = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        
        for (ScheduledTask task : tasks.values()) {
            Calendar taskTime = task.getScheduledTime();
            if (taskTime.after(today) && taskTime.before(tomorrow)) {
                todayTasks.add(task);
            }
        }
        
        // Sort by time
        Collections.sort(todayTasks, (a, b) -> a.getScheduledTime().compareTo(b.getScheduledTime()));
        
        return todayTasks;
    }
    
    /**
     * Get upcoming tasks (next 7 days)
     * @return List of upcoming tasks
     */
    public List<ScheduledTask> getUpcomingTasks() {
        List<ScheduledTask> upcomingTasks = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        
        Calendar weekLater = (Calendar) now.clone();
        weekLater.add(Calendar.DAY_OF_MONTH, 7);
        
        for (ScheduledTask task : tasks.values()) {
            Calendar taskTime = task.getScheduledTime();
            if (taskTime.after(now) && taskTime.before(weekLater)) {
                upcomingTasks.add(task);
            }
        }
        
        // Sort by time
        Collections.sort(upcomingTasks, (a, b) -> a.getScheduledTime().compareTo(b.getScheduledTime()));
        
        return upcomingTasks;
    }
    
    /**
     * Get task by ID
     * @param taskId Task ID
     * @return Task or null if not found
     */
    public ScheduledTask getTask(String taskId) {
        return tasks.get(taskId);
    }
    
    /**
     * Add a listener for Task Scheduler events
     * @param listener Listener to add
     */
    public void addListener(TaskSchedulerListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a listener
     * @param listener Listener to remove
     */
    public void removeListener(TaskSchedulerListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Main scheduler loop
     */
    private void schedulerLoop() {
        Log.i(TAG, "Starting scheduler loop");
        
        while (isRunning) {
            try {
                // Check for due tasks
                checkDueTasks();
                
                // Sleep for a while
                Thread.sleep(60 * 1000); // Check every minute
            } catch (InterruptedException e) {
                Log.e(TAG, "Scheduler loop interrupted", e);
                break;
            }
        }
        
        Log.i(TAG, "Scheduler loop ended");
    }
    
    /**
     * Check for tasks that are due
     */
    private void checkDueTasks() {
        Calendar now = Calendar.getInstance();
        List<String> dueTasks = new ArrayList<>();
        
        // Find due tasks
        for (Map.Entry<String, ScheduledTask> entry : tasks.entrySet()) {
            ScheduledTask task = entry.getValue();
            Calendar taskTime = task.getScheduledTime();
            
            // Task is due if scheduled time is before now and not already executed
            if (taskTime.before(now) && !task.isExecuted()) {
                dueTasks.add(entry.getKey());
            }
        }
        
        // Execute due tasks
        for (String taskId : dueTasks) {
            executeTask(taskId);
        }
    }
    
    /**
     * Execute a task
     * @param taskId Task ID
     */
    private void executeTask(String taskId) {
        final ScheduledTask task = tasks.get(taskId);
        if (task == null) {
            return;
        }
        
        // Mark as executed
        task.setExecuted(true);
        
        // Post to main thread
        mainHandler.post(() -> {
            try {
                Log.i(TAG, "Executing task: " + task.getTitle());
                
                // Execute based on task type
                switch (task.getType()) {
                    case STUDY_REMINDER:
                        executeStudyReminder(task);
                        break;
                    case GAME_REMINDER:
                        executeGameReminder(task);
                        break;
                    case VOICE_REMINDER:
                        executeVoiceReminder(task);
                        break;
                    case GENERAL_REMINDER:
                    default:
                        executeGeneralReminder(task);
                        break;
                }
                
                // Notify listeners
                for (TaskSchedulerListener listener : listeners) {
                    listener.onTaskExecuted(task);
                }
                
                // Handle recurring tasks
                handleRecurringTask(task);
            } catch (Exception e) {
                Log.e(TAG, "Error executing task: " + task.getTitle(), e);
            }
        });
    }
    
    /**
     * Execute a study reminder task
     * @param task Task to execute
     */
    private void executeStudyReminder(ScheduledTask task) {
        // In a real implementation, this would show a notification, etc.
        Log.i(TAG, "Study reminder: " + task.getTitle() + " - " + task.getDescription());
    }
    
    /**
     * Execute a game reminder task
     * @param task Task to execute
     */
    private void executeGameReminder(ScheduledTask task) {
        // In a real implementation, this would show a notification, etc.
        Log.i(TAG, "Game reminder: " + task.getTitle() + " - " + task.getDescription());
    }
    
    /**
     * Execute a voice reminder task
     * @param task Task to execute
     */
    private void executeVoiceReminder(ScheduledTask task) {
        // In a real implementation, this would trigger voice output, etc.
        Log.i(TAG, "Voice reminder: " + task.getTitle() + " - " + task.getDescription());
    }
    
    /**
     * Execute a general reminder task
     * @param task Task to execute
     */
    private void executeGeneralReminder(ScheduledTask task) {
        // In a real implementation, this would show a notification, etc.
        Log.i(TAG, "General reminder: " + task.getTitle() + " - " + task.getDescription());
    }
    
    /**
     * Handle recurring task after execution
     * @param task Executed task
     */
    private void handleRecurringTask(ScheduledTask task) {
        if (!task.isRecurring()) {
            // Non-recurring tasks can be removed
            tasks.remove(task.getId());
            return;
        }
        
        // Calculate next occurrence
        Calendar nextTime = (Calendar) task.getScheduledTime().clone();
        
        switch (task.getRecurrencePattern()) {
            case DAILY:
                nextTime.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEKLY:
                nextTime.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case MONTHLY:
                nextTime.add(Calendar.MONTH, 1);
                break;
            case YEARLY:
                nextTime.add(Calendar.YEAR, 1);
                break;
        }
        
        // Create new task instance
        ScheduledTask nextTask = new ScheduledTask(
            task.getType(),
            task.getTitle(),
            task.getDescription(),
            nextTime
        );
        
        nextTask.setRecurring(true);
        nextTask.setRecurrencePattern(task.getRecurrencePattern());
        nextTask.setUseSystemAlarm(task.isUseSystemAlarm());
        
        // Remove old task and schedule new one
        tasks.remove(task.getId());
        scheduleTask(nextTask);
    }
    
    /**
     * Schedule a system alarm for a task
     * @param task Task to schedule
     */
    private void scheduleSystemAlarm(ScheduledTask task) {
        // In a real implementation, this would use AlarmManager
        // This is a simplified version
        
        /*
        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.putExtra("task_id", task.getId());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            task.getId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            task.getScheduledTime().getTimeInMillis(),
            pendingIntent
        );
        */
        
        Log.i(TAG, "System alarm scheduled for: " + task.getTitle());
    }
    
    /**
     * Cancel a system alarm for a task
     * @param task Task to cancel
     */
    private void cancelSystemAlarm(ScheduledTask task) {
        // In a real implementation, this would use AlarmManager
        // This is a simplified version
        
        /*
        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.putExtra("task_id", task.getId());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            task.getId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.cancel(pendingIntent);
        */
        
        Log.i(TAG, "System alarm canceled for: " + task.getTitle());
    }
    
    /**
     * Generate a unique task ID
     * @param task Task to generate ID for
     * @return Unique ID
     */
    private String generateTaskId(ScheduledTask task) {
        return "task_" + System.currentTimeMillis() + "_" + 
               task.getTitle().hashCode();
    }
    
    /**
     * Schedule a recurring study session
     * @param title Session title
     * @param daysOfWeek Days of week (1-7, where 1 is Sunday)
     * @param hour Hour of day (0-23)
     * @param minute Minute of hour (0-59)
     * @return List of scheduled task IDs
     */
    public List<String> scheduleRecurringStudySession(String title, int[] daysOfWeek, 
                                                     int hour, int minute) {
        List<String> taskIds = new ArrayList<>();
        
        for (int dayOfWeek : daysOfWeek) {
            // Calculate next occurrence of this day
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            // Adjust day of week
            int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (currentDayOfWeek > dayOfWeek) {
                // Move to next week
                cal.add(Calendar.DAY_OF_MONTH, 7 - (currentDayOfWeek - dayOfWeek));
            } else {
                cal.add(Calendar.DAY_OF_MONTH, dayOfWeek - currentDayOfWeek);
            }
            
            // Create and schedule task
            ScheduledTask task = new ScheduledTask(
                ScheduledTask.TaskType.STUDY_REMINDER,
                title,
                "Time for your scheduled study session: " + title,
                cal
            );
            
            task.setRecurring(true);
            task.setRecurrencePattern(ScheduledTask.RecurrencePattern.WEEKLY);
            task.setUseSystemAlarm(true);
            
            String taskId = scheduleTask(task);
            if (taskId != null) {
                taskIds.add(taskId);
            }
        }
        
        return taskIds;
    }
    
    /**
     * Schedule a one-time study session
     * @param title Session title
     * @param description Session description
     * @param scheduledTime Scheduled time
     * @return Task ID or null if failed
     */
    public String scheduleStudySession(String title, String description, Calendar scheduledTime) {
        ScheduledTask task = new ScheduledTask(
            ScheduledTask.TaskType.STUDY_REMINDER,
            title,
            description,
            scheduledTime
        );
        
        task.setUseSystemAlarm(true);
        
        return scheduleTask(task);
    }
    
    /**
     * Schedule a game session
     * @param gameTitle Game title
     * @param scheduledTime Scheduled time
     * @return Task ID or null if failed
     */
    public String scheduleGameSession(String gameTitle, Calendar scheduledTime) {
        ScheduledTask task = new ScheduledTask(
            ScheduledTask.TaskType.GAME_REMINDER,
            "Time to play " + gameTitle,
            "Your scheduled gaming session for " + gameTitle + " is starting now.",
            scheduledTime
        );
        
        task.setUseSystemAlarm(true);
        
        return scheduleTask(task);
    }
    
    /**
     * Schedule a voice reminder
     * @param message Reminder message to speak
     * @param scheduledTime Scheduled time
     * @return Task ID or null if failed
     */
    public String scheduleVoiceReminder(String message, Calendar scheduledTime) {
        ScheduledTask task = new ScheduledTask(
            ScheduledTask.TaskType.VOICE_REMINDER,
            "Voice Reminder",
            message,
            scheduledTime
        );
        
        task.setUseSystemAlarm(true);
        
        return scheduleTask(task);
    }
    
    /**
     * Interface for Task Scheduler event listeners
     */
    public interface TaskSchedulerListener {
        void onTaskScheduled(ScheduledTask task);
        void onTaskExecuted(ScheduledTask task);
        void onTaskCanceled(ScheduledTask task);
    }
    
    /**
     * Shutdown and cleanup
     */
    public void shutdown() {
        stop();
        schedulerExecutor.shutdown();
    }
}
