package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.Converters;

import java.util.Date;

/**
 * Model class for scheduled tasks
 */
@Entity(tableName = "tasks",
        indices = @Index("gameId"))
@TypeConverters(Converters.class)
public class Task {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String taskName;
    private int taskType;
    private String gameId;
    private String targetPackage;
    private Date scheduledTime;
    private boolean isRepeating;
    private long repeatInterval;
    private boolean isActive;
    private boolean isCompleted;
    private Date completedTime;
    private String taskData;
    private Date createdTime;
    private Date lastModifiedTime;
    
    /**
     * Enum for task types
     */
    public enum TaskType {
        GAME_AUTOMATION(0),
        NOTIFICATION(1),
        MESSAGE(2),
        SYSTEM_ACTION(3),
        CUSTOM(4);
        
        private final int value;
        
        TaskType(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static TaskType fromValue(int value) {
            for (TaskType type : values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return GAME_AUTOMATION;
        }
    }
    
    public Task() {
        createdTime = new Date();
        lastModifiedTime = new Date();
        isActive = true;
        isCompleted = false;
    }
    
    @Ignore
    public Task(String taskName, int taskType, String gameId, Date scheduledTime) {
        this.taskName = taskName;
        this.taskType = taskType;
        this.gameId = gameId;
        this.scheduledTime = scheduledTime;
        this.isRepeating = false;
        this.isActive = true;
        this.isCompleted = false;
        this.createdTime = new Date();
        this.lastModifiedTime = new Date();
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public int getTaskType() {
        return taskType;
    }
    
    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    public String getTargetPackage() {
        return targetPackage;
    }
    
    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }
    
    public Date getScheduledTime() {
        return scheduledTime;
    }
    
    public void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    
    public boolean isRepeating() {
        return isRepeating;
    }
    
    public void setRepeating(boolean repeating) {
        isRepeating = repeating;
    }
    
    public long getRepeatInterval() {
        return repeatInterval;
    }
    
    public void setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
        if (completed) {
            completedTime = new Date();
        }
    }
    
    public Date getCompletedTime() {
        return completedTime;
    }
    
    public void setCompletedTime(Date completedTime) {
        this.completedTime = completedTime;
    }
    
    public String getTaskData() {
        return taskData;
    }
    
    public void setTaskData(String taskData) {
        this.taskData = taskData;
    }
    
    public Date getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    
    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }
    
    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }
    
    /**
     * Update the last modified time
     */
    public void touch() {
        this.lastModifiedTime = new Date();
    }
    
    /**
     * Get the next scheduled time (considering repeating tasks)
     * 
     * @return The next scheduled time
     */
    public Date getNextScheduledTime() {
        if (isCompleted && !isRepeating) {
            return null;
        }
        
        if (isRepeating && isCompleted && completedTime != null) {
            // Calculate the next time based on completed time and repeat interval
            return new Date(completedTime.getTime() + repeatInterval);
        }
        
        return scheduledTime;
    }
    
    /**
     * Check if the task is due
     * 
     * @return True if the task is due
     */
    public boolean isDue() {
        if (!isActive || isCompleted) {
            return false;
        }
        
        Date now = new Date();
        return scheduledTime != null && scheduledTime.before(now);
    }
    
    /**
     * Get a string representation of the repeat interval
     * 
     * @return Repeat interval string
     */
    public String getRepeatIntervalString() {
        if (!isRepeating) {
            return "One-time";
        }
        
        long minutes = repeatInterval / 60000;
        long hours = minutes / 60;
        minutes %= 60;
        
        if (hours > 0 && minutes > 0) {
            return hours + " hours, " + minutes + " minutes";
        } else if (hours > 0) {
            return hours + " hours";
        } else {
            return minutes + " minutes";
        }
    }
}
