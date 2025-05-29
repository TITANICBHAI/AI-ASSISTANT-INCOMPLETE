package com.aiassistant.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.DateConverter;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity representing application settings
 */
@Entity(tableName = "settings")
@TypeConverters(DateConverter.class)
public class Settings implements Serializable {
    
    @PrimaryKey
    @NonNull
    private String key;
    
    private String value;
    private String valueType;
    private Date lastUpdated;
    private boolean isEncrypted;
    private String description;
    
    /**
     * Default constructor
     */
    public Settings() {
        this.key = "";
        this.lastUpdated = new Date();
    }
    
    /**
     * Constructor with key and value
     * 
     * @param key The key
     * @param value The value
     */
    public Settings(@NonNull String key, String value) {
        this.key = key;
        this.value = value;
        this.lastUpdated = new Date();
    }
    
    /**
     * Get the key
     * 
     * @return The key
     */
    @NonNull
    public String getKey() {
        return key;
    }
    
    /**
     * Set the key
     * 
     * @param key The key
     */
    public void setKey(@NonNull String key) {
        this.key = key;
    }
    
    /**
     * Get the value
     * 
     * @return The value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Set the value
     * 
     * @param value The value
     */
    public void setValue(String value) {
        this.value = value;
        this.lastUpdated = new Date();
    }
    
    /**
     * Get the value type
     * 
     * @return The value type
     */
    public String getValueType() {
        return valueType;
    }
    
    /**
     * Set the value type
     * 
     * @param valueType The value type
     */
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
    
    /**
     * Get the last updated date
     * 
     * @return The last updated date
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    /**
     * Set the last updated date
     * 
     * @param lastUpdated The last updated date
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Check if the value is encrypted
     * 
     * @return Whether the value is encrypted
     */
    public boolean isEncrypted() {
        return isEncrypted;
    }
    
    /**
     * Set whether the value is encrypted
     * 
     * @param encrypted Whether the value is encrypted
     */
    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }
    
    /**
     * Get the description
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the description
     * 
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the value as a boolean
     * 
     * @return The boolean value
     */
    public boolean getBooleanValue() {
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Get the value as an integer
     * 
     * @return The integer value
     */
    public int getIntValue() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Get the value as a float
     * 
     * @return The float value
     */
    public float getFloatValue() {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return 0f;
        }
    }
    
    /**
     * Get the value as a long
     * 
     * @return The long value
     */
    public long getLongValue() {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
