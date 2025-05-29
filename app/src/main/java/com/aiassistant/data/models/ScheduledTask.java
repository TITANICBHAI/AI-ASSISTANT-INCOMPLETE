package com.aiassistant.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.UUID;

/**
 * Entity representing a scheduled task
 */
@Entity(tableName = "scheduled_tasks")
public class ScheduledTask implements Serializable {
    
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_IN_PROGRESS = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_FAILED = 3;
    public static final int STATUS_CANCELLED = 4;
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String taskType;
    private String params;
    private long scheduledTime;
    private long executionTime;
    private int status;
    private String result;
    private int priority;
    private int retryCount;
    private long lastRetryTime;
    
    /**
     * Default constructor
     */
    public ScheduledTask() {
        this.id = UUID.randomUUID().toString();
        this.status = STATUS_PENDING;
        this.retryCount = 0;
    }
    
    /**
     * Get the ID
     * 
     * @return The ID
     */
    @NonNull
    public String getId() {
        return id;
    }
    
    /**
     * Set the ID
     * 
     * @param id The ID
     */
    public void setId(@NonNull String id) {
        this.id = id;
    }
    
    /**
     * Get the task type
     * 
     * @return The task type
     */
    public String getTaskType() {
        return taskType;
    }
    
    /**
     * Set the task type
     * 
     * @param taskType The task type
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    
    /**
     * Get the parameters
     * 
     * @return The parameters
     */
    public String getParams() {
        return params;
    }
    
    /**
     * Set the parameters
     * 
     * @param params The parameters
     */
    public void setParams(String params) {
        this.params = params;
    }
    
    /**
     * Get the scheduled time
     * 
     * @return The scheduled time
     */
    public long getScheduledTime() {
        return scheduledTime;
    }
    
    /**
     * Set the scheduled time
     * 
     * @param scheduledTime The scheduled time
     */
    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }
    
    /**
     * Get the execution time
     * 
     * @return The execution time
     */
    public long getExecutionTime() {
        return executionTime;
    }
    
    /**
     * Set the execution time
     * 
     * @param executionTime The execution time
     */
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
    
    /**
     * Get the status
     * 
     * @return The status
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * Set the status
     * 
     * @param status The status
     */
    public void setStatus(int status) {
        this.status = status;
    }
    
    /**
     * Get the result
     * 
     * @return The result
     */
    public String getResult() {
        return result;
    }
    
    /**
     * Set the result
     * 
     * @param result The result
     */
    public void setResult(String result) {
        this.result = result;
    }
    
    /**
     * Get the priority
     * 
     * @return The priority
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Set the priority
     * 
     * @param priority The priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    /**
     * Get the retry count
     * 
     * @return The retry count
     */
    public int getRetryCount() {
        return retryCount;
    }
    
    /**
     * Set the retry count
     * 
     * @param retryCount The retry count
     */
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
    /**
     * Get the last retry time
     * 
     * @return The last retry time
     */
    public long getLastRetryTime() {
        return lastRetryTime;
    }
    
    /**
     * Set the last retry time
     * 
     * @param lastRetryTime The last retry time
     */
    public void setLastRetryTime(long lastRetryTime) {
        this.lastRetryTime = lastRetryTime;
    }
    
    /**
     * Mark the task as in progress
     */
    public void markAsInProgress() {
        this.status = STATUS_IN_PROGRESS;
        this.executionTime = System.currentTimeMillis();
    }
    
    /**
     * Mark the task as completed
     * 
     * @param result The result
     */
    public void markAsCompleted(String result) {
        this.status = STATUS_COMPLETED;
        this.result = result;
    }
    
    /**
     * Mark the task as failed
     * 
     * @param result The result
     */
    public void markAsFailed(String result) {
        this.status = STATUS_FAILED;
        this.result = result;
        this.retryCount++;
        this.lastRetryTime = System.currentTimeMillis();
    }
    
    /**
     * Mark the task as cancelled
     */
    public void markAsCancelled() {
        this.status = STATUS_CANCELLED;
    }
    
    /**
     * Check if the task is pending
     * 
     * @return Whether the task is pending
     */
    public boolean isPending() {
        return status == STATUS_PENDING;
    }
    
    /**
     * Check if the task is in progress
     * 
     * @return Whether the task is in progress
     */
    public boolean isInProgress() {
        return status == STATUS_IN_PROGRESS;
    }
    
    /**
     * Check if the task is completed
     * 
     * @return Whether the task is completed
     */
    public boolean isCompleted() {
        return status == STATUS_COMPLETED;
    }
    
    /**
     * Check if the task is failed
     * 
     * @return Whether the task is failed
     */
    public boolean isFailed() {
        return status == STATUS_FAILED;
    }
    
    /**
     * Check if the task is cancelled
     * 
     * @return Whether the task is cancelled
     */
    public boolean isCancelled() {
        return status == STATUS_CANCELLED;
    }
}
