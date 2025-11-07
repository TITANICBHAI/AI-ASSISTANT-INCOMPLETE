package com.aiassistant.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.DateConverter;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity(tableName = "image_samples",
        foreignKeys = @ForeignKey(
            entity = LabelDefinitionEntity.class,
            parentColumns = "labelId",
            childColumns = "labelId",
            onDelete = ForeignKey.SET_NULL
        ),
        indices = {@Index("labelId")})
@TypeConverters(DateConverter.class)
public class ImageSampleEntity implements Serializable {
    
    @PrimaryKey
    @NonNull
    private String imageId;
    
    private String imagePath;
    private Date timestamp;
    private String labelId;
    private float confidence;
    
    public ImageSampleEntity() {
        this.imageId = UUID.randomUUID().toString();
        this.timestamp = new Date();
        this.confidence = 0.0f;
    }
    
    public ImageSampleEntity(@NonNull String imageId, String imagePath, Date timestamp,
                            String labelId, float confidence) {
        this.imageId = imageId;
        this.imagePath = imagePath;
        this.timestamp = timestamp;
        this.labelId = labelId;
        this.confidence = confidence;
    }
    
    @NonNull
    public String getImageId() {
        return imageId;
    }
    
    public void setImageId(@NonNull String imageId) {
        this.imageId = imageId;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getLabelId() {
        return labelId;
    }
    
    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}
