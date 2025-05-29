package com.aiassistant.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.DateConverter;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Entity representing a performance log entry
 */
@Entity(tableName = "performance_logs")
@TypeConverters(DateConverter.class)
public class PerformanceLog implements Serializable {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String gameId;
    private String metricName;
    private float value;
    private Date timestamp;
    private String userId;
    private String deviceInfo;
    private String additionalInfo;
    
    /**
     * Default constructor
     */
    public PerformanceLog() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date();
    }
    
    /**
     * Constructor with metric name and value
     * 
     * @param metricName The metric name
     * @param value The value
     */
    public PerformanceLog(String metricName, float value) {
        this();
        this.metricName = metricName;
        this.value = value;
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
     * Get the game ID
     * 
     * @return The game ID
     */
    public String getGameId() {
        return gameId;
    }
    
    /**
     * Set the game ID
     * 
     * @param gameId The game ID
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    /**
     * Get the metric name
     * 
     * @return The metric name
     */
    public String getMetricName() {
        return metricName;
    }
    
    /**
     * Set the metric name
     * 
     * @param metricName The metric name
     */
    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }
    
    /**
     * Get the value
     * 
     * @return The value
     */
    public float getValue() {
        return value;
    }
    
    /**
     * Set the value
     * 
     * @param value The value
     */
    public void setValue(float value) {
        this.value = value;
    }
    
    /**
     * Get the timestamp
     * 
     * @return The timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set the timestamp
     * 
     * @param timestamp The timestamp
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Get the user ID
     * 
     * @return The user ID
     */
    public String getUserId() {
        return userId;
    }
    
    /**
     * Set the user ID
     * 
     * @param userId The user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    /**
     * Get the device info
     * 
     * @return The device info
     */
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    /**
     * Set the device info
     * 
     * @param deviceInfo The device info
     */
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    /**
     * Get the additional info
     * 
     * @return The additional info
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }
    
    /**
     * Set the additional info
     * 
     * @param additionalInfo The additional info
     */
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
