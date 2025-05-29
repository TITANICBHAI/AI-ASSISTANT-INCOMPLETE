package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.Converters;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Game entity
 */
@Entity(tableName = "games")
@TypeConverters(Converters.class)
public class Game {
    @PrimaryKey
    private String id;
    
    private String name;
    private String packageName;
    private String gameType;
    private Date firstDetectedDate;
    private Date lastPlayedDate;
    private int playCount;
    private long totalPlayTimeMs;
    private float userRating;
    private Map<String, String> gameSettings;
    private String detectedFeatures;
    private String supportedActions;
    private boolean isProfileCreated;
    private String iconPath;
    
    /**
     * Default constructor
     */
    public Game() {
        this.id = java.util.UUID.randomUUID().toString();
        this.gameSettings = new HashMap<>();
        this.playCount = 0;
        this.totalPlayTimeMs = 0;
        this.userRating = 0.0f;
        this.isProfileCreated = false;
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getGameType() {
        return gameType;
    }
    
    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
    
    public Date getFirstDetectedDate() {
        return firstDetectedDate;
    }
    
    public void setFirstDetectedDate(Date firstDetectedDate) {
        this.firstDetectedDate = firstDetectedDate;
    }
    
    public Date getLastPlayedDate() {
        return lastPlayedDate;
    }
    
    public void setLastPlayedDate(Date lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
    }
    
    public int getPlayCount() {
        return playCount;
    }
    
    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }
    
    public void incrementPlayCount() {
        this.playCount++;
    }
    
    public long getTotalPlayTimeMs() {
        return totalPlayTimeMs;
    }
    
    public void setTotalPlayTimeMs(long totalPlayTimeMs) {
        this.totalPlayTimeMs = totalPlayTimeMs;
    }
    
    public void addPlayTimeMs(long playTimeMs) {
        this.totalPlayTimeMs += playTimeMs;
    }
    
    public float getUserRating() {
        return userRating;
    }
    
    public void setUserRating(float userRating) {
        this.userRating = userRating;
    }
    
    public Map<String, String> getGameSettings() {
        return gameSettings;
    }
    
    public void setGameSettings(Map<String, String> gameSettings) {
        this.gameSettings = gameSettings;
    }
    
    public void addGameSetting(String key, String value) {
        this.gameSettings.put(key, value);
    }
    
    public String getGameSetting(String key) {
        return this.gameSettings.get(key);
    }
    
    public String getDetectedFeatures() {
        return detectedFeatures;
    }
    
    public void setDetectedFeatures(String detectedFeatures) {
        this.detectedFeatures = detectedFeatures;
    }
    
    public String getSupportedActions() {
        return supportedActions;
    }
    
    public void setSupportedActions(String supportedActions) {
        this.supportedActions = supportedActions;
    }
    
    public boolean isProfileCreated() {
        return isProfileCreated;
    }
    
    public void setProfileCreated(boolean profileCreated) {
        isProfileCreated = profileCreated;
    }
    
    public String getIconPath() {
        return iconPath;
    }
    
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}
