package com.aiassistant.ai.features.scheduler;

import java.util.Calendar;

/**
 * Represents a scheduled task in the system
 */
public class ScheduledTask {
    
    /**
     * Types of tasks
     */
    public enum TaskType {
        GENERAL_REMINDER,
        STUDY_REMINDER,
        GAME_REMINDER,
        VOICE_REMINDER
    }
    
    /**
     * Recurrence patterns
     */
    public enum RecurrencePattern {
        NONE,
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }
    
    private String id;
    private TaskType type;
    private String title;
    private String description;
    private Calendar scheduledTime;
    private boolean recurring;
    private RecurrencePattern recurrencePattern;
    private boolean useSystemAlarm;
    private boolean executed;
    
    /**
     * Constructor
     * @param type Task type
     * @param title Task title
     * @param description Task description
     * @param scheduledTime Scheduled time
     */
    public ScheduledTask(TaskType type, String title, String description, Calendar scheduledTime) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.scheduledTime = scheduledTime;
        this.recurring = false;
        this.recurrencePattern = RecurrencePattern.NONE;
        this.useSystemAlarm = false;
        this.executed = false;
    }
    
    /**
     * Get the task ID
     * @return Task ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Set the task ID
     * @param id Task ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Get the task type
     * @return Task type
     */
    public TaskType getType() {
        return type;
    }
    
    /**
     * Get the task title
     * @return Task title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Get the task description
     * @return Task description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the scheduled time
     * @return Scheduled time
     */
    public Calendar getScheduledTime() {
        return scheduledTime;
    }
    
    /**
     * Check if the task is recurring
     * @return True if recurring
     */
    public boolean isRecurring() {
        return recurring;
    }
    
    /**
     * Set whether the task is recurring
     * @param recurring True if recurring
     */
    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }
    
    /**
     * Get the recurrence pattern
     * @return Recurrence pattern
     */
    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }
    
    /**
     * Set the recurrence pattern
     * @param recurrencePattern Recurrence pattern
     */
    public void setRecurrencePattern(RecurrencePattern recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }
    
    /**
     * Check if the task uses system alarm
     * @return True if using system alarm
     */
    public boolean isUseSystemAlarm() {
        return useSystemAlarm;
    }
    
    /**
     * Set whether the task uses system alarm
     * @param useSystemAlarm True if using system alarm
     */
    public void setUseSystemAlarm(boolean useSystemAlarm) {
        this.useSystemAlarm = useSystemAlarm;
    }
    
    /**
     * Check if the task has been executed
     * @return True if executed
     */
    public boolean isExecuted() {
        return executed;
    }
    
    /**
     * Set whether the task has been executed
     * @param executed True if executed
     */
    public void setExecuted(boolean executed) {
        this.executed = executed;
    }
}
