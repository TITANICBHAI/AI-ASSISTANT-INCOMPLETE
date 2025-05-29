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
 * Game configuration entity
 */
@Entity(
    tableName = "game_configs",
    foreignKeys = @ForeignKey(
        entity = Game.class,
        parentColumns = "id",
        childColumns = "gameId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("gameId")}
)
@TypeConverters(Converters.class)
public class GameConfig {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String gameId;
    private String name;
    private String description;
    private boolean active;
    private Date creationDate;
    private Date lastModifiedDate;
    private Map<String, String> settings;
    private String aiModeSettings;
    private String inputConfiguration;
    private String screenZones;
    private String gameSpecificSettings;
    
    /**
     * Default constructor
     */
    public GameConfig() {
        this.active = false;
        this.creationDate = new Date();
        this.lastModifiedDate = new Date();
        this.settings = new HashMap<>();
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
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    
    public Map<String, String> getSettings() {
        return settings;
    }
    
    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
        this.lastModifiedDate = new Date();
    }
    
    public void addSetting(String key, String value) {
        this.settings.put(key, value);
        this.lastModifiedDate = new Date();
    }
    
    public String getSetting(String key) {
        return this.settings.get(key);
    }
    
    public String getAiModeSettings() {
        return aiModeSettings;
    }
    
    public void setAiModeSettings(String aiModeSettings) {
        this.aiModeSettings = aiModeSettings;
        this.lastModifiedDate = new Date();
    }
    
    public String getInputConfiguration() {
        return inputConfiguration;
    }
    
    public void setInputConfiguration(String inputConfiguration) {
        this.inputConfiguration = inputConfiguration;
        this.lastModifiedDate = new Date();
    }
    
    public String getScreenZones() {
        return screenZones;
    }
    
    public void setScreenZones(String screenZones) {
        this.screenZones = screenZones;
        this.lastModifiedDate = new Date();
    }
    
    public String getGameSpecificSettings() {
        return gameSpecificSettings;
    }
    
    public void setGameSpecificSettings(String gameSpecificSettings) {
        this.gameSpecificSettings = gameSpecificSettings;
        this.lastModifiedDate = new Date();
    }
}
