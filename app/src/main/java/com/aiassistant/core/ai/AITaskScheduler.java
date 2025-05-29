package com.aiassistant.core.ai;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aiassistant.core.AIAssistantApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Schedules and manages AI tasks.
 * Handles task priority, scheduling, and execution to ensure
 * the AI system runs efficiently and responds appropriately to events.
 */
public class AITaskScheduler {
    private static final String TAG = "AITaskScheduler";
    
    // Task types
    public static final int TASK_TYPE_IMMEDIATE = 0;
    public static final int TASK_TYPE_HIGH_PRIORITY = 1;
    public static final int TASK_TYPE_NORMAL = 2;
    public static final int TASK_TYPE_BACKGROUND = 3;
    public static final int TASK_TYPE_PERIODIC = 4;
    
    // Executors
    private final ScheduledExecutorService scheduledExecutor;
    private final Executor backgroundExecutor;
    private final Handler mainHandler;
    
    // Task queues
    private final PriorityQueue<ScheduledTask> taskQueue;
    private final Map<String, ScheduledFuture<?>> periodicTasks;
    private final List<ScheduledTask> activeTasks;
    
    // State flags
    private boolean isPaused = false;
    
    /**
     * Constructor for the AI Task Scheduler
     */
    public AITaskScheduler() {
        // Initialize executors
        scheduledExecutor = new ScheduledThreadPoolExecutor(2);
        backgroundExecutor = AIAssistantApplication.getInstance().getDiskExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize collections
        taskQueue = new PriorityQueue<>();
        periodicTasks = new HashMap<>();
        activeTasks = Collections.synchronizedList(new ArrayList<>());
        
        Log.d(TAG, "AI Task Scheduler initialized");
    }
    
    /**
     * Schedule a task to run as soon as possible
     * @param taskId Unique task identifier
     * @param task The runnable task to execute
     * @param taskType The type and priority of the task
     * @return True if task was scheduled successfully
     */
    public boolean scheduleTask(@NonNull String taskId, @NonNull Runnable task, int taskType) {
        return scheduleTask(taskId, task, taskType, 0, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Schedule a task to run after a delay
     * @param taskId Unique task identifier
     * @param task The runnable task to execute
     * @param taskType The type and priority of the task
     * @param delay The time to wait before execution
     * @param timeUnit The time unit of the delay
     * @return True if task was scheduled successfully
     */
    public boolean scheduleTask(@NonNull String taskId, @NonNull Runnable task, 
                               int taskType, long delay, @NonNull TimeUnit timeUnit) {
        if (isPaused && taskType != TASK_TYPE_IMMEDIATE) {
            Log.d(TAG, "Task scheduler is paused, not scheduling task: " + taskId);
            return false;
        }
        
        ScheduledTask scheduledTask = new ScheduledTask(taskId, task, taskType, 
                System.currentTimeMillis() + timeUnit.toMillis(delay));
        
        synchronized (taskQueue) {
            // Check if task with same ID exists
            for (ScheduledTask existingTask : taskQueue) {
                if (existingTask.taskId.equals(taskId)) {
                    Log.d(TAG, "Task with ID " + taskId + " already exists, replacing");
                    taskQueue.remove(existingTask);
                    break;
                }
            }
            
            // Add to queue
            taskQueue.add(scheduledTask);
        }
        
        // For immediate and high priority tasks, execute right away
        if (taskType == TASK_TYPE_IMMEDIATE) {
            executeTask(scheduledTask);
        } else if (delay > 0) {
            scheduledExecutor.schedule(() -> executeTask(scheduledTask), delay, timeUnit);
        } else {
            processNextTask();
        }
        
        Log.d(TAG, "Scheduled task: " + taskId + " with type: " + taskType);
        return true;
    }
    
    /**
     * Schedule a task to run periodically
     * @param taskId Unique task identifier
     * @param task The runnable task to execute
     * @param initialDelay Initial delay before first execution
     * @param period Period between successive executions
     * @param timeUnit The time unit of the period
     * @return True if task was scheduled successfully
     */
    public boolean schedulePeriodicTask(@NonNull String taskId, @NonNull Runnable task,
                                      long initialDelay, long period, @NonNull TimeUnit timeUnit) {
        if (isPaused) {
            Log.d(TAG, "Task scheduler is paused, not scheduling periodic task: " + taskId);
            return false;
        }
        
        // Cancel existing task with same ID
        cancelPeriodicTask(taskId);
        
        // Schedule the periodic task
        ScheduledFuture<?> future = scheduledExecutor.scheduleAtFixedRate(
                () -> executePeriodicTask(taskId, task),
                initialDelay, period, timeUnit);
        
        // Store the future for cancellation
        periodicTasks.put(taskId, future);
        
        Log.d(TAG, "Scheduled periodic task: " + taskId + 
                " with period: " + period + " " + timeUnit.name());
        return true;
    }
    
    /**
     * Cancel a scheduled task by ID
     * @param taskId The ID of the task to cancel
     * @return True if task was cancelled
     */
    public boolean cancelTask(@NonNull String taskId) {
        boolean removed = false;
        
        synchronized (taskQueue) {
            // Remove from queue if present
            for (ScheduledTask task : taskQueue) {
                if (task.taskId.equals(taskId)) {
                    taskQueue.remove(task);
                    removed = true;
                    break;
                }
            }
        }
        
        // Check if task is active and interrupt if possible
        synchronized (activeTasks) {
            for (int i = 0; i < activeTasks.size(); i++) {
                ScheduledTask task = activeTasks.get(i);
                if (task.taskId.equals(taskId)) {
                    // Can't really interrupt, but mark as completed
                    activeTasks.remove(i);
                    removed = true;
                    break;
                }
            }
        }
        
        Log.d(TAG, "Cancelled task: " + taskId + " - " + (removed ? "removed" : "not found"));
        return removed;
    }
    
    /**
     * Cancel a periodic task by ID
     * @param taskId The ID of the periodic task to cancel
     * @return True if task was cancelled
     */
    public boolean cancelPeriodicTask(@NonNull String taskId) {
        ScheduledFuture<?> future = periodicTasks.remove(taskId);
        if (future != null) {
            future.cancel(false);
            Log.d(TAG, "Cancelled periodic task: " + taskId);
            return true;
        }
        return false;
    }
    
    /**
     * Process the next task in the queue
     */
    private void processNextTask() {
        if (isPaused) {
            return;
        }
        
        synchronized (taskQueue) {
            if (!taskQueue.isEmpty()) {
                ScheduledTask nextTask = taskQueue.poll();
                if (nextTask != null) {
                    long now = System.currentTimeMillis();
                    if (now >= nextTask.scheduledTime) {
                        executeTask(nextTask);
                    } else {
                        // Task is not ready yet, put it back and schedule
                        taskQueue.add(nextTask);
                        long delay = nextTask.scheduledTime - now;
                        scheduledExecutor.schedule(this::processNextTask, delay, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }
    }
    
    /**
     * Execute a scheduled task based on its type
     * @param task The task to execute
     */
    private void executeTask(ScheduledTask task) {
        if (isPaused && task.taskType != TASK_TYPE_IMMEDIATE) {
            // Don't execute paused tasks except immediate ones
            synchronized (taskQueue) {
                taskQueue.add(task);
            }
            return;
        }
        
        synchronized (activeTasks) {
            activeTasks.add(task);
        }
        
        try {
            switch (task.taskType) {
                case TASK_TYPE_IMMEDIATE:
                    // Run on calling thread
                    task.runnable.run();
                    break;
                    
                case TASK_TYPE_HIGH_PRIORITY:
                    // Run on main thread
                    mainHandler.post(task.runnable);
                    break;
                    
                case TASK_TYPE_NORMAL:
                    // Run on scheduled executor
                    scheduledExecutor.execute(task.runnable);
                    break;
                    
                case TASK_TYPE_BACKGROUND:
                    // Run on background executor
                    backgroundExecutor.execute(task.runnable);
                    break;
                    
                default:
                    Log.w(TAG, "Unknown task type: " + task.taskType);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error executing task: " + task.taskId, e);
        } finally {
            synchronized (activeTasks) {
                activeTasks.remove(task);
            }
            
            // Process next task if available
            processNextTask();
        }
    }
    
    /**
     * Execute a periodic task
     * @param taskId The ID of the task
     * @param task The task to execute
     */
    private void executePeriodicTask(String taskId, Runnable task) {
        if (isPaused) {
            return;
        }
        
        ScheduledTask scheduledTask = new ScheduledTask(taskId, task, TASK_TYPE_PERIODIC, 0);
        
        synchronized (activeTasks) {
            activeTasks.add(scheduledTask);
        }
        
        try {
            task.run();
        } catch (Exception e) {
            Log.e(TAG, "Error executing periodic task: " + taskId, e);
        } finally {
            synchronized (activeTasks) {
                activeTasks.remove(scheduledTask);
            }
        }
    }
    
    /**
     * Pause the task scheduler
     * Tasks in the queue will not be executed until resumed
     * Immediate tasks will still be executed
     */
    public void pause() {
        isPaused = true;
        Log.d(TAG, "Task scheduler paused");
    }
    
    /**
     * Resume the task scheduler
     * Tasks in the queue will be executed
     */
    public void resume() {
        isPaused = false;
        processNextTask();
        Log.d(TAG, "Task scheduler resumed");
    }
    
    /**
     * Check if the task scheduler is paused
     * @return True if paused
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Get the count of pending tasks
     * @return Number of tasks in the queue
     */
    public int getPendingTaskCount() {
        synchronized (taskQueue) {
            return taskQueue.size();
        }
    }
    
    /**
     * Get the count of active tasks
     * @return Number of tasks currently executing
     */
    public int getActiveTaskCount() {
        synchronized (activeTasks) {
            return activeTasks.size();
        }
    }
    
    /**
     * Get the count of periodic tasks
     * @return Number of periodic tasks
     */
    public int getPeriodicTaskCount() {
        return periodicTasks.size();
    }
    
    /**
     * Shutdown the task scheduler
     * All pending tasks will be cancelled
     */
    public void shutdown() {
        isPaused = true;
        
        // Cancel all periodic tasks
        for (ScheduledFuture<?> future : periodicTasks.values()) {
            future.cancel(false);
        }
        periodicTasks.clear();
        
        // Clear task queue
        synchronized (taskQueue) {
            taskQueue.clear();
        }
        
        // Wait for active tasks to complete
        Log.d(TAG, getActiveTaskCount() + " tasks still active during shutdown");
        
        // Shutdown executor
        scheduledExecutor.shutdown();
        
        Log.d(TAG, "Task scheduler shut down");
    }
    
    /**
     * Scheduled task class for the queue
     */
    private static class ScheduledTask implements Comparable<ScheduledTask> {
        final String taskId;
        final Runnable runnable;
        final int taskType;
        final long scheduledTime;
        
        ScheduledTask(String taskId, Runnable runnable, int taskType, long scheduledTime) {
            this.taskId = taskId;
            this.runnable = runnable;
            this.taskType = taskType;
            this.scheduledTime = scheduledTime;
        }
        
        @Override
        public int compareTo(ScheduledTask other) {
            // First compare by task type (priority)
            int typeDiff = taskType - other.taskType;
            if (typeDiff != 0) {
                return typeDiff;
            }
            
            // Then compare by scheduled time
            return Long.compare(scheduledTime, other.scheduledTime);
        }
    }
}
