package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents parameters for a task action
 */
@Entity(tableName = "action_parameters",
        foreignKeys = @ForeignKey(
                entity = Task.class,
                parentColumns = "id",
                childColumns = "taskId",
                onDelete = ForeignKey.CASCADE
        ))
public class ActionParameters implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Parameter data types
    public static final int TYPE_STRING = 0;
    public static final int TYPE_INTEGER = 1;
    public static final int TYPE_FLOAT = 2;
    public static final int TYPE_BOOLEAN = 3;
    public static final int TYPE_JSON = 4;
    
    @PrimaryKey
    private String id;
    
    // Association with a Task
    private String taskId;
    
    // Parameter information
    private String paramName;
    private String paramValue;
    private int paramType;
    private boolean required;
    private String defaultValue;
    
    /**
     * Default constructor
     */
    public ActionParameters() {
        this.id = UUID.randomUUID().toString();
        this.paramType = TYPE_STRING;
        this.required = false;
    }
    
    /**
     * Constructor with basic details
     * 
     * @param taskId The associated task ID
     * @param paramName The parameter name
     * @param paramValue The parameter value
     * @param paramType The parameter type (use TYPE_* constants)
     */
    public ActionParameters(String taskId, String paramName, String paramValue, int paramType) {
        this();
        this.taskId = taskId;
        this.paramName = paramName;
        this.paramValue = paramValue;
        this.paramType = paramType;
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getParamName() {
        return paramName;
    }
    
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
    
    public String getParamValue() {
        return paramValue;
    }
    
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }
    
    public int getParamType() {
        return paramType;
    }
    
    public void setParamType(int paramType) {
        this.paramType = paramType;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    /**
     * Get the parameter value as an Integer
     * 
     * @return Integer value or null if conversion fails
     */
    public Integer getIntValue() {
        if (paramType != TYPE_INTEGER || paramValue == null) {
            return null;
        }
        
        try {
            return Integer.parseInt(paramValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Get the parameter value as a Float
     * 
     * @return Float value or null if conversion fails
     */
    public Float getFloatValue() {
        if (paramType != TYPE_FLOAT || paramValue == null) {
            return null;
        }
        
        try {
            return Float.parseFloat(paramValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Get the parameter value as a Boolean
     * 
     * @return Boolean value or null if conversion fails
     */
    public Boolean getBooleanValue() {
        if (paramType != TYPE_BOOLEAN || paramValue == null) {
            return null;
        }
        
        return Boolean.parseBoolean(paramValue);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ActionParameters that = (ActionParameters) o;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return "ActionParameters{" +
               "id='" + id + '\'' +
               ", taskId='" + taskId + '\'' +
               ", name='" + paramName + '\'' +
               ", value='" + paramValue + '\'' +
               ", type=" + paramType +
               '}';
    }
}
