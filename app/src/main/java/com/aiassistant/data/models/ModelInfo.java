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
 * Entity representing information about a trained model
 */
@Entity(tableName = "models")
@TypeConverters(DateConverter.class)
public class ModelInfo implements Serializable {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String name;
    private String gameId;
    private String algorithmType;
    private String version;
    private String filePath;
    private long fileSize;
    private Date createdAt;
    private Date lastUpdatedAt;
    private int trainingEpisodes;
    private float accuracy;
    private boolean isActive;
    private String parameters;
    
    /**
     * Default constructor
     */
    public ModelInfo() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = new Date();
        this.lastUpdatedAt = new Date();
    }
    
    /**
     * Constructor with name and game ID
     * 
     * @param name The name
     * @param gameId The game ID
     */
    public ModelInfo(String name, String gameId) {
        this();
        this.name = name;
        this.gameId = gameId;
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
     * Get the name
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the name
     * 
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
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
     * Get the algorithm type
     * 
     * @return The algorithm type
     */
    public String getAlgorithmType() {
        return algorithmType;
    }
    
    /**
     * Set the algorithm type
     * 
     * @param algorithmType The algorithm type
     */
    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }
    
    /**
     * Get the version
     * 
     * @return The version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Set the version
     * 
     * @param version The version
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * Get the file path
     * 
     * @return The file path
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Set the file path
     * 
     * @param filePath The file path
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Get the file size
     * 
     * @return The file size
     */
    public long getFileSize() {
        return fileSize;
    }
    
    /**
     * Set the file size
     * 
     * @param fileSize The file size
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    /**
     * Get the created at date
     * 
     * @return The created at date
     */
    public Date getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Set the created at date
     * 
     * @param createdAt The created at date
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Get the last updated at date
     * 
     * @return The last updated at date
     */
    public Date getLastUpdatedAt() {
        return lastUpdatedAt;
    }
    
    /**
     * Set the last updated at date
     * 
     * @param lastUpdatedAt The last updated at date
     */
    public void setLastUpdatedAt(Date lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
    
    /**
     * Get the training episodes
     * 
     * @return The training episodes
     */
    public int getTrainingEpisodes() {
        return trainingEpisodes;
    }
    
    /**
     * Set the training episodes
     * 
     * @param trainingEpisodes The training episodes
     */
    public void setTrainingEpisodes(int trainingEpisodes) {
        this.trainingEpisodes = trainingEpisodes;
    }
    
    /**
     * Get the accuracy
     * 
     * @return The accuracy
     */
    public float getAccuracy() {
        return accuracy;
    }
    
    /**
     * Set the accuracy
     * 
     * @param accuracy The accuracy
     */
    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }
    
    /**
     * Check if the model is active
     * 
     * @return Whether the model is active
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Set whether the model is active
     * 
     * @param active Whether the model is active
     */
    public void setActive(boolean active) {
        isActive = active;
    }
    
    /**
     * Get the parameters
     * 
     * @return The parameters
     */
    public String getParameters() {
        return parameters;
    }
    
    /**
     * Set the parameters
     * 
     * @param parameters The parameters
     */
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
    /**
     * Update the model info
     */
    public void updateLastUpdated() {
        this.lastUpdatedAt = new Date();
    }
}
