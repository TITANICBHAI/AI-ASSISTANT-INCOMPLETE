package com.aiassistant.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.DateConverter;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity(tableName = "gesture_samples")
@TypeConverters(DateConverter.class)
public class GestureSampleEntity implements Serializable {
    
    @PrimaryKey
    @NonNull
    private String gestureId;
    
    private String gestureType;
    private String coordinatesJson;
    private Date timestamp;
    private String label;
    private float confidence;
    
    public GestureSampleEntity() {
        this.gestureId = UUID.randomUUID().toString();
        this.timestamp = new Date();
        this.confidence = 0.0f;
    }
    
    public GestureSampleEntity(@NonNull String gestureId, String gestureType, String coordinatesJson,
                              Date timestamp, String label, float confidence) {
        this.gestureId = gestureId;
        this.gestureType = gestureType;
        this.coordinatesJson = coordinatesJson;
        this.timestamp = timestamp;
        this.label = label;
        this.confidence = confidence;
    }
    
    @NonNull
    public String getGestureId() {
        return gestureId;
    }
    
    public void setGestureId(@NonNull String gestureId) {
        this.gestureId = gestureId;
    }
    
    public String getGestureType() {
        return gestureType;
    }
    
    public void setGestureType(String gestureType) {
        this.gestureType = gestureType;
    }
    
    public String getCoordinatesJson() {
        return coordinatesJson;
    }
    
    public void setCoordinatesJson(String coordinatesJson) {
        this.coordinatesJson = coordinatesJson;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}
