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
 * Entity representing training data for reinforcement learning
 */
@Entity(tableName = "training_data")
@TypeConverters(DateConverter.class)
public class TrainingData implements Serializable {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String gameId;
    private String modelId;
    private String stateData;
    private String actionData;
    private float reward;
    private String nextStateData;
    private boolean terminal;
    private Date timestamp;
    private int trainingEpisode;
    private int trainingStep;
    private boolean usedForTraining;
    
    /**
     * Default constructor
     */
    public TrainingData() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date();
    }
    
    /**
     * Constructor with game ID and model ID
     * 
     * @param gameId The game ID
     * @param modelId The model ID
     */
    public TrainingData(String gameId, String modelId) {
        this();
        this.gameId = gameId;
        this.modelId = modelId;
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
     * Get the model ID
     * 
     * @return The model ID
     */
    public String getModelId() {
        return modelId;
    }
    
    /**
     * Set the model ID
     * 
     * @param modelId The model ID
     */
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
    
    /**
     * Get the state data
     * 
     * @return The state data
     */
    public String getStateData() {
        return stateData;
    }
    
    /**
     * Set the state data
     * 
     * @param stateData The state data
     */
    public void setStateData(String stateData) {
        this.stateData = stateData;
    }
    
    /**
     * Get the action data
     * 
     * @return The action data
     */
    public String getActionData() {
        return actionData;
    }
    
    /**
     * Set the action data
     * 
     * @param actionData The action data
     */
    public void setActionData(String actionData) {
        this.actionData = actionData;
    }
    
    /**
     * Get the reward
     * 
     * @return The reward
     */
    public float getReward() {
        return reward;
    }
    
    /**
     * Set the reward
     * 
     * @param reward The reward
     */
    public void setReward(float reward) {
        this.reward = reward;
    }
    
    /**
     * Get the next state data
     * 
     * @return The next state data
     */
    public String getNextStateData() {
        return nextStateData;
    }
    
    /**
     * Set the next state data
     * 
     * @param nextStateData The next state data
     */
    public void setNextStateData(String nextStateData) {
        this.nextStateData = nextStateData;
    }
    
    /**
     * Check if the state is terminal
     * 
     * @return Whether the state is terminal
     */
    public boolean isTerminal() {
        return terminal;
    }
    
    /**
     * Set whether the state is terminal
     * 
     * @param terminal Whether the state is terminal
     */
    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
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
     * Get the training episode
     * 
     * @return The training episode
     */
    public int getTrainingEpisode() {
        return trainingEpisode;
    }
    
    /**
     * Set the training episode
     * 
     * @param trainingEpisode The training episode
     */
    public void setTrainingEpisode(int trainingEpisode) {
        this.trainingEpisode = trainingEpisode;
    }
    
    /**
     * Get the training step
     * 
     * @return The training step
     */
    public int getTrainingStep() {
        return trainingStep;
    }
    
    /**
     * Set the training step
     * 
     * @param trainingStep The training step
     */
    public void setTrainingStep(int trainingStep) {
        this.trainingStep = trainingStep;
    }
    
    /**
     * Check if the data has been used for training
     * 
     * @return Whether the data has been used for training
     */
    public boolean isUsedForTraining() {
        return usedForTraining;
    }
    
    /**
     * Set whether the data has been used for training
     * 
     * @param usedForTraining Whether the data has been used for training
     */
    public void setUsedForTraining(boolean usedForTraining) {
        this.usedForTraining = usedForTraining;
    }
}
