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
 * Task action entity
 */
@Entity(
    tableName = "task_actions",
    foreignKeys = @ForeignKey(
        entity = ScheduledTask.class,
        parentColumns = "id",
        childColumns = "taskId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("taskId")}
)
@TypeConverters(Converters.class)
public class TaskAction {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long taskId;
    private String actionType;
    private Map<String, String> parameters;
    private Date executionTime;
    private Date completionTime;
    private String result;
    private boolean successful;
    
    /**
     * Default constructor
     */
    public TaskAction() {
        this.parameters = new HashMap<>();
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
    
    public String getActionType() {
        return actionType;
    }
    
    public void setActionType(String actionType) {
        this.actionType = actionType;
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
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
