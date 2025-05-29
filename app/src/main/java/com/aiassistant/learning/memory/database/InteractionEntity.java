package com.aiassistant.learning.memory.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity for storing knowledge and interactions in memory database
 */
@Entity(tableName = "interactions")
public class InteractionEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    @NonNull
    private String domain;
    
    @NonNull
    private String key;
    
    private String value;
    
    private long timestamp;
    
    /**
     * Constructor
     */
    public InteractionEntity(@NonNull String domain, @NonNull String key, String value, long timestamp) {
        this.domain = domain;
        this.key = key;
        this.value = value;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    @NonNull
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(@NonNull String domain) {
        this.domain = domain;
    }
    
    @NonNull
    public String getKey() {
        return key;
    }
    
    public void setKey(@NonNull String key) {
        this.key = key;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
