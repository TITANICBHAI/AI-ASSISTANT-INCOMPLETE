package com.aiassistant.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity for storing game strategies
 */
@Entity(tableName = "strategies")
public class Strategy {
    @PrimaryKey
    @NonNull
    private String strategyId;
    
    private String gameId;
    private String name;
    private String description;
    private String rules;
    private String targetScenario;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;
    
    public Strategy() {
    }
    
    public Strategy(@NonNull String strategyId, String gameId, String name, String rules) {
        this.strategyId = strategyId;
        this.gameId = gameId;
        this.name = name;
        this.rules = rules;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
    }
    
    @NonNull
    public String getStrategyId() {
        return strategyId;
    }
    
    public void setStrategyId(@NonNull String strategyId) {
        this.strategyId = strategyId;
    }
    
    public String getGameId() {
        return gameId;
    }
    
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getRules() {
        return rules;
    }
    
    public void setRules(String rules) {
        this.rules = rules;
    }
    
    public String getTargetScenario() {
        return targetScenario;
    }
    
    public void setTargetScenario(String targetScenario) {
        this.targetScenario = targetScenario;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
