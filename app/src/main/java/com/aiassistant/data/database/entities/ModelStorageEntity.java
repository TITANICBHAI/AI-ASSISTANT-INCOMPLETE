package com.aiassistant.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.database.converters.Converters;

/**
 * Entity for storing model data in the database
 */
@Entity(tableName = "model_storage")
@TypeConverters(Converters.class)
public class ModelStorageEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String modelId;
    private String gameId;
    private int modelType;
    private long lastUpdated;
    private float[] weights;
    private String metadataJson;
    
    /**
     * Default constructor
     */
    public ModelStorageEntity() {
    }
    
    /**
     * Constructor with parameters
     * 
     * @param modelId The model ID
     * @param gameId The game ID
     * @param modelType The model type
     * @param weights The model weights
     * @param metadataJson The metadata JSON
     */
    public ModelStorageEntity(String modelId, String gameId, int modelType, float[] weights, String metadataJson) {
        this.modelId = modelId;
        this.gameId = gameId;
        this.modelType = modelType;
        this.lastUpdated = System.currentTimeMillis();
        this.weights = weights;
        this.metadataJson = metadataJson;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getModelId() {
        return modelId;
    }
    
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    public int getModelType() {
        return modelType;
    }
    
    public void setModelType(int modelType) {
        this.modelType = modelType;
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public float[] getWeights() {
        return weights;
    }
    
    public void setWeights(float[] weights) {
        this.weights = weights;
    }
    
    public String getMetadataJson() {
        return metadataJson;
    }
    
    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }
}
