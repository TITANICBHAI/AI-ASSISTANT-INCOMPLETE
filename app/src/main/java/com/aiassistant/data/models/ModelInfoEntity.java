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

@Entity(tableName = "model_info",
        foreignKeys = @ForeignKey(
            entity = LabelDefinitionEntity.class,
            parentColumns = "labelId",
            childColumns = "labelId",
            onDelete = ForeignKey.SET_NULL
        ),
        indices = {@Index("labelId")})
@TypeConverters(DateConverter.class)
public class ModelInfoEntity implements Serializable {
    
    @PrimaryKey
    @NonNull
    private String modelId;
    
    private String modelName;
    private String modelPath;
    private String labelId;
    private float accuracy;
    private String version;
    private Date createdAt;
    private String status;
    
    public ModelInfoEntity() {
        this.modelId = UUID.randomUUID().toString();
        this.createdAt = new Date();
        this.accuracy = 0.0f;
        this.version = "1.0";
        this.status = "UNTRAINED";
    }
    
    public ModelInfoEntity(@NonNull String modelId, String modelName, String modelPath,
                          String labelId, float accuracy, String version, Date createdAt, String status) {
        this.modelId = modelId;
        this.modelName = modelName;
        this.modelPath = modelPath;
        this.labelId = labelId;
        this.accuracy = accuracy;
        this.version = version;
        this.createdAt = createdAt;
        this.status = status;
    }
    
    @NonNull
    public String getModelId() {
        return modelId;
    }
    
    public void setModelId(@NonNull String modelId) {
        this.modelId = modelId;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public String getModelPath() {
        return modelPath;
    }
    
    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }
    
    public String getLabelId() {
        return labelId;
    }
    
    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }
    
    public float getAccuracy() {
        return accuracy;
    }
    
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
