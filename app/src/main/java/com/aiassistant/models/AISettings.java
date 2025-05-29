package com.aiassistant.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.Converters;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * AI settings entity
 */
@Entity(tableName = "ai_settings")
@TypeConverters(Converters.class)
public class AISettings {
    @PrimaryKey
    private String id;
    
    private boolean enableVoiceCommands;
    private boolean enableVoiceResponses;
    private boolean enableGameAnalysis;
    private boolean enableAutonomousMode;
    private boolean enableLearning;
    private boolean enableProfiling;
    private boolean enableSecurityProtection;
    private boolean enableDataCollection;
    private int maxMemoryUsageMb;
    private int cpuUsagePercentage;
    private int performanceLevel;
    private int responseSpeed;
    private Map<String, String> advancedSettings;
    private Date lastUpdated;
    
    /**
     * Default constructor
     */
    public AISettings() {
        this.id = "default";
        this.enableVoiceCommands = true;
        this.enableVoiceResponses = true;
        this.enableGameAnalysis = true;
        this.enableAutonomousMode = false;
        this.enableLearning = true;
        this.enableProfiling = true;
        this.enableSecurityProtection = true;
        this.enableDataCollection = false;
        this.maxMemoryUsageMb = 512;
        this.cpuUsagePercentage = 50;
        this.performanceLevel = 2; // Medium (range: 1-3)
        this.responseSpeed = 2; // Medium (range: 1-3)
        this.advancedSettings = new HashMap<>();
        this.lastUpdated = new Date();
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public boolean isEnableVoiceCommands() {
        return enableVoiceCommands;
    }
    
    public void setEnableVoiceCommands(boolean enableVoiceCommands) {
        this.enableVoiceCommands = enableVoiceCommands;
        this.lastUpdated = new Date();
    }
    
    public boolean isEnableVoiceResponses() {
        return enableVoiceResponses;
    }
    
    public void setEnableVoiceResponses(boolean enableVoiceResponses) {
        this.enableVoiceResponses = enableVoiceResponses;
        this.lastUpdated = new Date();
    }
    
    public boolean isEnableGameAnalysis() {
        return enableGameAnalysis;
    }
    
    public void setEnableGameAnalysis(boolean enableGameAnalysis) {
        this.enableGameAnalysis = enableGameAnalysis;
        this.lastUpdated = new Date();
    }
    
    public boolean isEnableAutonomousMode() {
        return enableAutonomousMode;
    }
    
    public void setEnableAutonomousMode(boolean enableAutonomousMode) {
        this.enableAutonomousMode = enableAutonomousMode;
        this.lastUpdated = new Date();
    }
    
    public boolean isEnableLearning() {
        return enableLearning;
    }
    
    public void setEnableLearning(boolean enableLearning) {
        this.enableLearning = enableLearning;
        this.lastUpdated = new Date();
    }
    
    public boolean isEnableProfiling() {
        return enableProfiling;
    }
    
    public void setEnableProfiling(boolean enableProfiling) {
        this.enableProfiling = enableProfiling;
        this.lastUpdated = new Date();
    }
    
    public boolean isEnableSecurityProtection() {
        return enableSecurityProtection;
    }
    
    public void setEnableSecurityProtection(boolean enableSecurityProtection) {
        this.enableSecurityProtection = enableSecurityProtection;
        this.lastUpdated = new Date();
    }
    
    public boolean isEnableDataCollection() {
        return enableDataCollection;
    }
    
    public void setEnableDataCollection(boolean enableDataCollection) {
        this.enableDataCollection = enableDataCollection;
        this.lastUpdated = new Date();
    }
    
    public int getMaxMemoryUsageMb() {
        return maxMemoryUsageMb;
    }
    
    public void setMaxMemoryUsageMb(int maxMemoryUsageMb) {
        this.maxMemoryUsageMb = maxMemoryUsageMb;
        this.lastUpdated = new Date();
    }
    
    public int getCpuUsagePercentage() {
        return cpuUsagePercentage;
    }
    
    public void setCpuUsagePercentage(int cpuUsagePercentage) {
        this.cpuUsagePercentage = cpuUsagePercentage;
        this.lastUpdated = new Date();
    }
    
    public int getPerformanceLevel() {
        return performanceLevel;
    }
    
    public void setPerformanceLevel(int performanceLevel) {
        this.performanceLevel = performanceLevel;
        this.lastUpdated = new Date();
    }
    
    public int getResponseSpeed() {
        return responseSpeed;
    }
    
    public void setResponseSpeed(int responseSpeed) {
        this.responseSpeed = responseSpeed;
        this.lastUpdated = new Date();
    }
    
    public Map<String, String> getAdvancedSettings() {
        return advancedSettings;
    }
    
    public void setAdvancedSettings(Map<String, String> advancedSettings) {
        this.advancedSettings = advancedSettings;
        this.lastUpdated = new Date();
    }
    
    public void addAdvancedSetting(String key, String value) {
        this.advancedSettings.put(key, value);
        this.lastUpdated = new Date();
    }
    
    public String getAdvancedSetting(String key) {
        return this.advancedSettings.get(key);
    }
    
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
