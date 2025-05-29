package com.aiassistant.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity for storing performance metrics
 */
@Entity(tableName = "performance_metrics")
public class PerformanceMetric {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String gameId;
    private String sessionId;
    private long timestamp;
    private String metricType;
    private double value;
    private String description;
    
    public PerformanceMetric() {
    }
    
    public PerformanceMetric(String gameId, String sessionId, long timestamp, String metricType, double value) {
        this.gameId = gameId;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.metricType = metricType;
        this.value = value;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getMetricType() {
        return metricType;
    }
    
    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }
    
    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        this.value = value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
