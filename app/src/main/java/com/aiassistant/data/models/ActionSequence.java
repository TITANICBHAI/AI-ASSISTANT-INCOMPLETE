package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.core.ai.AIAction;
import com.aiassistant.data.converters.DateConverter;
import com.aiassistant.data.converters.ListConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity representing a sequence of AI actions that can be executed together.
 * Used for storing successful interaction patterns for later replay.
 */
@Entity(tableName = "action_sequences")
@TypeConverters({DateConverter.class, ListConverter.class})
public class ActionSequence {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    /**
     * Descriptive name of this sequence
     */
    private String name;
    
    /**
     * Package name of the app this sequence applies to
     */
    private String targetAppPackage;
    
    /**
     * Activity name within the app (optional)
     */
    private String targetAppActivity;
    
    /**
     * List of actions in this sequence
     */
    private List<AIAction> actions = new ArrayList<>();
    
    /**
     * Whether this is a complete sequence that accomplishes a specific goal
     */
    private boolean isCompleteSequence;
    
    /**
     * Tags for categorizing sequences
     */
    private List<String> tags = new ArrayList<>();
    
    /**
     * User-provided description
     */
    private String description;
    
    /**
     * Learning algorithm that generated this sequence
     */
    private String generatedByAlgorithm;
    
    /**
     * Screen state hash when this sequence starts (for matching)
     */
    private String startScreenStateHash;
    
    /**
     * Expected screen state hash after sequence completes (for validation)
     */
    private String expectedEndScreenStateHash;
    
    /**
     * Creation timestamp
     */
    private Date createdAt;
    
    /**
     * Last used timestamp
     */
    private Date lastUsedAt;
    
    /**
     * Sequence execution count
     */
    private int executionCount = 0;
    
    /**
     * Success rate (0.0 - 1.0)
     */
    private float successRate = 0.0f;
    
    /**
     * Average reward gained from executing this sequence
     */
    private float averageReward = 0.0f;
    
    /**
     * Total rewards gained from executing this sequence
     */
    private float totalRewards = 0.0f;
    
    /**
     * Average execution time in milliseconds
     */
    private long averageExecutionTimeMs = 0;
    
    /**
     * Default constructor
     */
    public ActionSequence() {
    }
    
    /**
     * Constructor with basic parameters
     * @param name Sequence name
     * @param targetAppPackage Target app package name
     */
    public ActionSequence(String name, String targetAppPackage) {
        this.name = name;
        this.targetAppPackage = targetAppPackage;
        this.createdAt = new Date();
        this.lastUsedAt = new Date();
    }

    // Getters and Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetAppPackage() {
        return targetAppPackage;
    }

    public void setTargetAppPackage(String targetAppPackage) {
        this.targetAppPackage = targetAppPackage;
    }

    public String getTargetAppActivity() {
        return targetAppActivity;
    }

    public void setTargetAppActivity(String targetAppActivity) {
        this.targetAppActivity = targetAppActivity;
    }

    public List<AIAction> getActions() {
        return actions;
    }

    public void setActions(List<AIAction> actions) {
        this.actions = actions;
    }
    
    public void addAction(AIAction action) {
        if (this.actions == null) {
            this.actions = new ArrayList<>();
        }
        this.actions.add(action);
    }

    public boolean isCompleteSequence() {
        return isCompleteSequence;
    }

    public void setCompleteSequence(boolean completeSequence) {
        isCompleteSequence = completeSequence;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGeneratedByAlgorithm() {
        return generatedByAlgorithm;
    }

    public void setGeneratedByAlgorithm(String generatedByAlgorithm) {
        this.generatedByAlgorithm = generatedByAlgorithm;
    }

    public String getStartScreenStateHash() {
        return startScreenStateHash;
    }

    public void setStartScreenStateHash(String startScreenStateHash) {
        this.startScreenStateHash = startScreenStateHash;
    }

    public String getExpectedEndScreenStateHash() {
        return expectedEndScreenStateHash;
    }

    public void setExpectedEndScreenStateHash(String expectedEndScreenStateHash) {
        this.expectedEndScreenStateHash = expectedEndScreenStateHash;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Date lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }
    
    public void incrementExecutionCount() {
        this.executionCount++;
    }

    public float getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(float successRate) {
        this.successRate = successRate;
    }

    public float getAverageReward() {
        return averageReward;
    }

    public void setAverageReward(float averageReward) {
        this.averageReward = averageReward;
    }

    public float getTotalRewards() {
        return totalRewards;
    }

    public void setTotalRewards(float totalRewards) {
        this.totalRewards = totalRewards;
    }
    
    public void addReward(float reward) {
        this.totalRewards += reward;
        if (executionCount > 0) {
            this.averageReward = this.totalRewards / executionCount;
        }
    }

    public long getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public void setAverageExecutionTimeMs(long averageExecutionTimeMs) {
        this.averageExecutionTimeMs = averageExecutionTimeMs;
    }
    
    public void updateAverageExecutionTime(long executionTimeMs) {
        if (executionCount <= 1) {
            this.averageExecutionTimeMs = executionTimeMs;
        } else {
            // Moving average
            this.averageExecutionTimeMs = 
                (this.averageExecutionTimeMs * (executionCount - 1) + executionTimeMs) / executionCount;
        }
    }
}