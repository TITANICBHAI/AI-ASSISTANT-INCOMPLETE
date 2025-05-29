package com.aiassistant.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.Converters;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Task execution result entity
 */
@Entity(
    tableName = "task_execution_results",
    foreignKeys = @ForeignKey(
        entity = ScheduledTask.class,
        parentColumns = "id",
        childColumns = "taskId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("taskId")}
)
@TypeConverters(Converters.class)
public class TaskExecutionResult {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long taskId;
    private Date executionTime;
    private Date completionTime;
    private boolean successful;
    private String resultCode;
    private String resultMessage;
    private Map<String, String> resultData;
    private long executionDurationMs;
    private String executorId;
    
    /**
     * Default constructor
     */
    public TaskExecutionResult() {
        this.resultData = new HashMap<>();
        this.successful = false;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(long taskId) {
        this.taskId = taskId;
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
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
    public String getResultCode() {
        return resultCode;
    }
    
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
    
    public String getResultMessage() {
        return resultMessage;
    }
    
    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
    
    public Map<String, String> getResultData() {
        return resultData;
    }
    
    public void setResultData(Map<String, String> resultData) {
        this.resultData = resultData;
    }
    
    public void addResultData(String key, String value) {
        this.resultData.put(key, value);
    }
    
    public String getResultData(String key) {
        return this.resultData.get(key);
    }
    
    public long getExecutionDurationMs() {
        return executionDurationMs;
    }
    
    public void setExecutionDurationMs(long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }
    
    public String getExecutorId() {
        return executorId;
    }
    
    public void setExecutorId(String executorId) {
        this.executorId = executorId;
    }
}
