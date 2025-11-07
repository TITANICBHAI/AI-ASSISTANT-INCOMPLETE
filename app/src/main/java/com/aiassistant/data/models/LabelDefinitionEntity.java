package com.aiassistant.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.DateConverter;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity(tableName = "label_definitions")
@TypeConverters(DateConverter.class)
public class LabelDefinitionEntity implements Serializable {
    
    @PrimaryKey
    @NonNull
    private String labelId;
    
    private String name;
    private String purpose;
    private String category;
    private Date createdAt;
    private int usageCount;
    
    public LabelDefinitionEntity() {
        this.labelId = UUID.randomUUID().toString();
        this.createdAt = new Date();
        this.usageCount = 0;
    }
    
    public LabelDefinitionEntity(@NonNull String labelId, String name, String purpose,
                                String category, Date createdAt, int usageCount) {
        this.labelId = labelId;
        this.name = name;
        this.purpose = purpose;
        this.category = category;
        this.createdAt = createdAt;
        this.usageCount = usageCount;
    }
    
    @NonNull
    public String getLabelId() {
        return labelId;
    }
    
    public void setLabelId(@NonNull String labelId) {
        this.labelId = labelId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public int getUsageCount() {
        return usageCount;
    }
    
    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }
    
    public void incrementUsageCount() {
        this.usageCount++;
    }
}
