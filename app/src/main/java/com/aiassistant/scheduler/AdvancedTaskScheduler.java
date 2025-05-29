package com.aiassistant.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import com.aiassistant.data.models.ScheduledTask;
import com.aiassistant.receivers.TaskAlarmReceiver;
import com.aiassistant.utils.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Advanced task scheduler for managing recurring and one-time tasks
 */
public class AdvancedTaskScheduler {
    
    private static final String TAG = "AdvancedTaskScheduler";
    
    // Singleton instance
    private static AdvancedTaskScheduler instance;
    
    // Context
    private final Context context;
    
    // Alarm manager
    private final AlarmManager alarmManager;
    
    // Database helper
    private final TaskDatabaseHelper dbHelper;
    
    /**
     * Recurrence types
     */
    public enum RecurrenceType {
        NONE,
        DAILY,
        WEEKLY,
        MONTHLY,
        CUSTOM
    }
    
    /**
     * Get the singleton instance
     * 
     * @param context The context
     * @return The instance
     */
    public static synchronized AdvancedTaskScheduler getInstance(Context context) {
        if (instance == null) {
            instance = new AdvancedTaskScheduler(context);
        }
        return instance;
    }
    
    /**
     * Private constructor
     * 
     * @param context The context
     */
    private AdvancedTaskScheduler(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.dbHelper = new TaskDatabaseHelper(context);
    }
    
    /**
     * Schedule a one-time task
     * 
     * @param taskType The task type
     * @param params The parameters
     * @param delayMinutes The delay in minutes
     * @param priority The priority
     * @return The task
     */
    public ScheduledTask scheduleTask(String taskType, String params, int delayMinutes, int priority) {
        // Create task
        ScheduledTask task = new ScheduledTask();
        task.setTaskType(taskType);
        task.setParams(params);
        task.setPriority(priority);
        
        // Calculate scheduled time
        long scheduledTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(delayMinutes);
        task.setScheduledTime(scheduledTime);
        
        // Insert into database and schedule alarm
        insertTaskInDb(task);
        scheduleAlarm(task);
        
        return task;
    }
    
    /**
     * Schedule a recurring task
     * 
     * @param taskType The task type
     * @param params The parameters
     * @param recurrenceType The recurrence type
     * @param recurrenceInterval The recurrence interval
     * @param startTime The start time (or null for immediate)
     * @param priority The priority
     * @return The task
     */
    public ScheduledTask scheduleRecurringTask(
            String taskType,
            String params,
            RecurrenceType recurrenceType,
            int recurrenceInterval,
            Calendar startTime,
            int priority) {
        
        // Create task
        ScheduledTask task = new ScheduledTask();
        task.setTaskType(taskType);
        task.setParams(params);
        task.setPriority(priority);
        
        // Apply recurrence parameters using a custom field or serialized JSON
        // In this implementation, we just use a simple approach
        
        // Calculate first scheduled time
        long scheduledTime;
        if (startTime != null) {
            scheduledTime = startTime.getTimeInMillis();
        } else {
            scheduledTime = calculateNextOccurrence(recurrenceType, recurrenceInterval);
        }
        
        task.setScheduledTime(scheduledTime);
        
        // Insert into database and schedule alarm
        insertTaskInDb(task);
        scheduleAlarm(task);
        
        return task;
    }
    
    /**
     * Calculate the next occurrence based on recurrence type and interval
     * 
     * @param recurrenceType The recurrence type
     * @param interval The interval
     * @return The next occurrence time
     */
    private long calculateNextOccurrence(RecurrenceType recurrenceType, int interval) {
        Calendar calendar = Calendar.getInstance();
        
        switch (recurrenceType) {
            case DAILY:
                calendar.add(Calendar.DAY_OF_YEAR, interval);
                break;
            case WEEKLY:
                calendar.add(Calendar.WEEK_OF_YEAR, interval);
                break;
            case MONTHLY:
                calendar.add(Calendar.MONTH, interval);
                break;
            default:
                // For no recurrence or custom, just schedule for now
                break;
        }
        
        return calendar.getTimeInMillis();
    }
    
    /**
     * Cancel a task
     * 
     * @param taskId The task ID
     */
    public void cancelTask(String taskId) {
        if (taskId == null) {
            Log.e(TAG, "Invalid task ID");
            return;
        }
        
        // Get the task from database
        ScheduledTask task = getTaskById(taskId);
        
        if (task == null) {
            Log.e(TAG, "Task not found: " + taskId);
            return;
        }
        
        // Cancel alarm
        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.setAction(Constants.ACTION_EXECUTE_TASK);
        intent.putExtra("task_id", taskId);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE |
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));
        
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        
        // Mark as cancelled in database
        task.markAsCancelled();
        updateTaskInDb(task);
        
        Log.d(TAG, "Cancelled task: " + taskId);
    }
    
    /**
     * Cancel all tasks
     */
    public void cancelAllTasks() {
        // Get all scheduled tasks
        List<ScheduledTask> tasks = getScheduledTasks();
        
        // Cancel each task
        for (ScheduledTask task : tasks) {
            cancelTask(task.getId());
        }
    }
    
    /**
     * Handle task completion
     * 
     * @param taskId The task ID
     * @param result The result
     */
    public void handleTaskCompletion(String taskId, String result) {
        // Get the task from database
        ScheduledTask task = getTaskById(taskId);
        
        if (task == null) {
            Log.e(TAG, "Task not found: " + taskId);
            return;
        }
        
        // Mark as completed
        task.markAsCompleted(result);
        
        // If this is a recurring task, schedule the next occurrence
        if (isRecurringTask(task)) {
            scheduleNextOccurrence(task);
        } else {
            // Update in database
            updateTaskInDb(task);
        }
    }
    
    /**
     * Check if a task is recurring
     * 
     * @param task The task
     * @return Whether the task is recurring
     */
    private boolean isRecurringTask(ScheduledTask task) {
        // In a real implementation, this would check the recurrence parameters
        return false;
    }
    
    /**
     * Schedule the next occurrence of a recurring task
     * 
     * @param completedTask The completed task
     */
    private void scheduleNextOccurrence(ScheduledTask completedTask) {
        // In a real implementation, this would calculate the next occurrence and create a new task
    }
    
    /**
     * Get a task by ID
     * 
     * @param taskId The task ID
     * @return The task
     */
    public ScheduledTask getTaskById(String taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = TaskDatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {taskId};
        
        try (Cursor cursor = db.query(
                TaskDatabaseHelper.TABLE_TASKS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null)) {
            
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToTask(cursor);
            }
        }
        
        return null;
    }
    
    /**
     * Get all tasks
     * 
     * @return The tasks
     */
    public List<ScheduledTask> getAllTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        try (Cursor cursor = db.query(
                TaskDatabaseHelper.TABLE_TASKS,
                null,
                null,
                null,
                null,
                null,
                TaskDatabaseHelper.COLUMN_SCHEDULED_TIME + " ASC")) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    tasks.add(cursorToTask(cursor));
                } while (cursor.moveToNext());
            }
        }
        
        return tasks;
    }
    
    /**
     * Get scheduled tasks
     * 
     * @return The tasks
     */
    public List<ScheduledTask> getScheduledTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = TaskDatabaseHelper.COLUMN_STATUS + " = ?";
        String[] selectionArgs = {String.valueOf(ScheduledTask.STATUS_PENDING)};
        
        try (Cursor cursor = db.query(
                TaskDatabaseHelper.TABLE_TASKS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                TaskDatabaseHelper.COLUMN_SCHEDULED_TIME + " ASC")) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    tasks.add(cursorToTask(cursor));
                } while (cursor.moveToNext());
            }
        }
        
        return tasks;
    }
    
    /**
     * Get upcoming tasks
     * 
     * @return The tasks
     */
    public List<ScheduledTask> getUpcomingTasks() {
        List<ScheduledTask> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        long now = System.currentTimeMillis();
        
        String selection = TaskDatabaseHelper.COLUMN_STATUS + " = ? AND " +
                TaskDatabaseHelper.COLUMN_SCHEDULED_TIME + " > ?";
        String[] selectionArgs = {
                String.valueOf(ScheduledTask.STATUS_PENDING),
                String.valueOf(now)
        };
        
        try (Cursor cursor = db.query(
                TaskDatabaseHelper.TABLE_TASKS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                TaskDatabaseHelper.COLUMN_SCHEDULED_TIME + " ASC")) {
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    tasks.add(cursorToTask(cursor));
                } while (cursor.moveToNext());
            }
        }
        
        return tasks;
    }
    
    /**
     * Reschedule all tasks
     */
    public void rescheduleAllTasks() {
        // Get all scheduled tasks
        List<ScheduledTask> tasks = getScheduledTasks();
        
        // Reschedule each task
        for (ScheduledTask task : tasks) {
            scheduleAlarm(task);
        }
    }
    
    /**
     * Clean up completed and failed tasks
     * 
     * @param olderThan The age in milliseconds
     * @return The number of deleted tasks
     */
    public int cleanupTasks(long olderThan) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        long timestamp = System.currentTimeMillis() - olderThan;
        
        String whereClause = "(" + TaskDatabaseHelper.COLUMN_STATUS + " = ? OR " +
                TaskDatabaseHelper.COLUMN_STATUS + " = ?) AND " +
                TaskDatabaseHelper.COLUMN_EXECUTION_TIME + " < ?";
        String[] whereArgs = {
                String.valueOf(ScheduledTask.STATUS_COMPLETED),
                String.valueOf(ScheduledTask.STATUS_FAILED),
                String.valueOf(timestamp)
        };
        
        return db.delete(TaskDatabaseHelper.TABLE_TASKS, whereClause, whereArgs);
    }
    
    /**
     * Schedule an alarm for a task
     * 
     * @param task The task
     */
    private void scheduleAlarm(ScheduledTask task) {
        if (task == null || task.getId() == null) {
            Log.e(TAG, "Invalid task");
            return;
        }
        
        // Create intent
        Intent intent = new Intent(context, TaskAlarmReceiver.class);
        intent.setAction(Constants.ACTION_EXECUTE_TASK);
        intent.putExtra("task_id", task.getId());
        intent.putExtra("task_type", task.getTaskType());
        
        // Create pending intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));
        
        // Get scheduled time
        long scheduledTime = task.getScheduledTime();
        
        // If time is in the past, schedule for now + 1 second
        long now = System.currentTimeMillis();
        if (scheduledTime < now) {
            scheduledTime = now + 1000;
            
            // Update task
            task.setScheduledTime(scheduledTime);
            updateTaskInDb(task);
        }
        
        // Schedule alarm based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent);
        }
        
        Log.d(TAG, "Scheduled task " + task.getId() + " at " + scheduledTime);
    }
    
    /**
     * Insert a task into the database
     * 
     * @param task The task
     */
    private void insertTaskInDb(ScheduledTask task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = taskToContentValues(task);
        
        db.insert(TaskDatabaseHelper.TABLE_TASKS, null, values);
    }
    
    /**
     * Update a task in the database
     * 
     * @param task The task
     */
    private void updateTaskInDb(ScheduledTask task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = taskToContentValues(task);
        
        String whereClause = TaskDatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {task.getId()};
        
        db.update(TaskDatabaseHelper.TABLE_TASKS, values, whereClause, whereArgs);
    }
    
    /**
     * Convert a task to content values
     * 
     * @param task The task
     * @return The content values
     */
    private ContentValues taskToContentValues(ScheduledTask task) {
        ContentValues values = new ContentValues();
        values.put(TaskDatabaseHelper.COLUMN_ID, task.getId());
        values.put(TaskDatabaseHelper.COLUMN_TASK_TYPE, task.getTaskType());
        values.put(TaskDatabaseHelper.COLUMN_PARAMS, task.getParams());
        values.put(TaskDatabaseHelper.COLUMN_SCHEDULED_TIME, task.getScheduledTime());
        values.put(TaskDatabaseHelper.COLUMN_EXECUTION_TIME, task.getExecutionTime());
        values.put(TaskDatabaseHelper.COLUMN_STATUS, task.getStatus());
        values.put(TaskDatabaseHelper.COLUMN_RESULT, task.getResult());
        values.put(TaskDatabaseHelper.COLUMN_PRIORITY, task.getPriority());
        values.put(TaskDatabaseHelper.COLUMN_RETRY_COUNT, task.getRetryCount());
        values.put(TaskDatabaseHelper.COLUMN_LAST_RETRY_TIME, task.getLastRetryTime());
        
        return values;
    }
    
    /**
     * Convert a cursor to a task
     * 
     * @param cursor The cursor
     * @return The task
     */
    private ScheduledTask cursorToTask(Cursor cursor) {
        ScheduledTask task = new ScheduledTask();
        
        task.setId(cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_ID)));
        task.setTaskType(cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_TASK_TYPE)));
        task.setParams(cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_PARAMS)));
        task.setScheduledTime(cursor.getLong(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_SCHEDULED_TIME)));
        task.setExecutionTime(cursor.getLong(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_EXECUTION_TIME)));
        task.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_STATUS)));
        task.setResult(cursor.getString(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_RESULT)));
        task.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_PRIORITY)));
        task.setRetryCount(cursor.getInt(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_RETRY_COUNT)));
        task.setLastRetryTime(cursor.getLong(cursor.getColumnIndexOrThrow(TaskDatabaseHelper.COLUMN_LAST_RETRY_TIME)));
        
        return task;
    }
    
    /**
     * Database helper for task storage
     */
    private static class TaskDatabaseHelper extends android.database.sqlite.SQLiteOpenHelper {
        
        private static final String DATABASE_NAME = "tasks.db";
        private static final int DATABASE_VERSION = 1;
        
        private static final String TABLE_TASKS = "tasks";
        
        private static final String COLUMN_ID = "id";
        private static final String COLUMN_TASK_TYPE = "task_type";
        private static final String COLUMN_PARAMS = "params";
        private static final String COLUMN_SCHEDULED_TIME = "scheduled_time";
        private static final String COLUMN_EXECUTION_TIME = "execution_time";
        private static final String COLUMN_STATUS = "status";
        private static final String COLUMN_RESULT = "result";
        private static final String COLUMN_PRIORITY = "priority";
        private static final String COLUMN_RETRY_COUNT = "retry_count";
        private static final String COLUMN_LAST_RETRY_TIME = "last_retry_time";
        
        private static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_TASKS + " (" +
                        COLUMN_ID + " TEXT PRIMARY KEY," +
                        COLUMN_TASK_TYPE + " TEXT," +
                        COLUMN_PARAMS + " TEXT," +
                        COLUMN_SCHEDULED_TIME + " INTEGER," +
                        COLUMN_EXECUTION_TIME + " INTEGER," +
                        COLUMN_STATUS + " INTEGER," +
                        COLUMN_RESULT + " TEXT," +
                        COLUMN_PRIORITY + " INTEGER," +
                        COLUMN_RETRY_COUNT + " INTEGER," +
                        COLUMN_LAST_RETRY_TIME + " INTEGER" +
                        ")";
        
        private static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_TASKS;
        
        /**
         * Constructor
         * 
         * @param context The context
         */
        public TaskDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(android.database.sqlite.SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE);
        }
        
        @Override
        public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_TABLE);
            onCreate(db);
        }
    }
}
