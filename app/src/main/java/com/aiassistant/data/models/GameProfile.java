package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.Converters;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Game profile entity
 */
@Entity(
    tableName = "game_profiles",
    foreignKeys = @ForeignKey(
        entity = Game.class,
        parentColumns = "id",
        childColumns = "gameId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("gameId")}
)
@TypeConverters(Converters.class)
public class GameProfile {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String gameId;
    private String name;
    private String description;
    private Date creationDate;
    private Date lastUpdatedDate;
    private int playCount;
    private long totalPlayTimeMs;
    private float userRating;
    private String profileType;
    private Map<String, String> settings;
    private String detectedGameFeatures;
    private String detectedGameControlScheme;
    private String detectedGameGenre;
    private String detectedGameDifficulty;
    private String preferredStrategies;
    private String userPreferences;
    private boolean active;
    
    /**
     * Default constructor
     */
    public GameProfile() {
        this.creationDate = new Date();
        this.lastUpdatedDate = new Date();
        this.playCount = 0;
        this.totalPlayTimeMs = 0;
        this.userRating = 0.0f;
        this.settings = new HashMap<>();
        this.active = true;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
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
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }
    
    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
    
    public int getPlayCount() {
        return playCount;
    }
    
    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }
    
    public void incrementPlayCount() {
        this.playCount++;
        this.lastUpdatedDate = new Date();
    }
    
    public long getTotalPlayTimeMs() {
        return totalPlayTimeMs;
    }
    
    public void setTotalPlayTimeMs(long totalPlayTimeMs) {
        this.totalPlayTimeMs = totalPlayTimeMs;
    }
    
    public void addPlayTimeMs(long playTimeMs) {
        this.totalPlayTimeMs += playTimeMs;
        this.lastUpdatedDate = new Date();
    }
    
    public float getUserRating() {
        return userRating;
    }
    
    public void setUserRating(float userRating) {
        this.userRating = userRating;
        this.lastUpdatedDate = new Date();
    }
    
    public String getProfileType() {
        return profileType;
    }
    
    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }
    
    public Map<String, String> getSettings() {
        return settings;
    }
    
    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
        this.lastUpdatedDate = new Date();
    }
    
    public void addSetting(String key, String value) {
        this.settings.put(key, value);
        this.lastUpdatedDate = new Date();
    }
    
    public String getSetting(String key) {
        return this.settings.get(key);
    }
    
    public String getDetectedGameFeatures() {
        return detectedGameFeatures;
    }
    
    public void setDetectedGameFeatures(String detectedGameFeatures) {
        this.detectedGameFeatures = detectedGameFeatures;
    }
    
    public String getDetectedGameControlScheme() {
        return detectedGameControlScheme;
    }
    
    public void setDetectedGameControlScheme(String detectedGameControlScheme) {
        this.detectedGameControlScheme = detectedGameControlScheme;
    }
    
    public String getDetectedGameGenre() {
        return detectedGameGenre;
    }
    
    public void setDetectedGameGenre(String detectedGameGenre) {
        this.detectedGameGenre = detectedGameGenre;
    }
    
    public String getDetectedGameDifficulty() {
        return detectedGameDifficulty;
    }
    
    public void setDetectedGameDifficulty(String detectedGameDifficulty) {
        this.detectedGameDifficulty = detectedGameDifficulty;
    }
    
    public String getPreferredStrategies() {
        return preferredStrategies;
    }
    
    public void setPreferredStrategies(String preferredStrategies) {
        this.preferredStrategies = preferredStrategies;
    }
    
    public String getUserPreferences() {
        return userPreferences;
    }
    
    public void setUserPreferences(String userPreferences) {
        this.userPreferences = userPreferences;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
