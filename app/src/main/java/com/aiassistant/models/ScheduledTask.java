package com.aiassistant.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.Converters;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Scheduled task entity
 */
@Entity(tableName = "scheduled_tasks")
@TypeConverters(Converters.class)
public class ScheduledTask {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private String description;
    private String taskType;
    private String taskAction;
    private Map<String, String> parameters;
    private Date scheduledTime;
    private Date executionTime;
    private Date completionTime;
    private TaskStatus status;
    private String result;
    private int priority;
    private boolean recurring;
    private long recurringIntervalMs;
    
    /**
     * Task status enum
     */
    public enum TaskStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED
    }
    
    public ScheduledTask() {
        this.parameters = new HashMap<>();
        this.status = TaskStatus.PENDING;
        this.priority = 5; // Default priority (1-10)
        this.recurring = false;
        this.recurringIntervalMs = 0;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTaskType() {
        return taskType;
    }
    
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    
    public String getTaskAction() {
        return taskAction;
    }
    
    public void setTaskAction(String taskAction) {
        this.taskAction = taskAction;
    }
    
    public Map<String, String> getParameters() {
        return parameters;
    }
    
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
    
    public void addParameter(String key, String value) {
        this.parameters.put(key, value);
    }
    
    public String getParameter(String key) {
        return this.parameters.get(key);
    }
    
    public Date getScheduledTime() {
        return scheduledTime;
    }
    
    public void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    
    public Date getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }
    
    public Date getCompletionTime() {
        return completionTime;
    }
    
    public void setCompletionTime(Date completionTime) {
        this.completionTime = completionTime;
    }
    
    public TaskStatus getStatus() {
        return status;
    }
    
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public boolean isRecurring() {
        return recurring;
    }
    
    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }
    
    public long getRecurringIntervalMs() {
        return recurringIntervalMs;
    }
    
    public void setRecurringIntervalMs(long recurringIntervalMs) {
        this.recurringIntervalMs = recurringIntervalMs;
    }
}
