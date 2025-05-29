package com.aiassistant.data.models;

import android.graphics.Bitmap;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.core.ai.detection.DetectedEnemy;
import com.aiassistant.data.converters.Converters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Game state entity
 */
@Entity(tableName = "game_states")
@TypeConverters(Converters.class)
public class GameState {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String gameId;
    private Date timestamp;
    private float[] features;
    private Bitmap screenBitmap;
    private String screenText;
    private String detectedEntities;
    private float complexityScore;
    private String environmentType;
    private String playerState;
    private int screenWidth;
    private int screenHeight;
    private boolean inCombat;
    private float playerHealth;
    
    // Transient fields not stored in the database
    @Ignore
    private transient List<UIElement> uiElements;
    
    @Ignore
    private transient List<DetectedEnemy> enemies;
    
    /**
     * Default constructor
     */
    public GameState() {
        this.timestamp = new Date();
        this.uiElements = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.inCombat = false;
        this.playerHealth = 1.0f;
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
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public float[] getFeatures() {
        return features;
    }
    
    public void setFeatures(float[] features) {
        this.features = features;
    }
    
    public Bitmap getScreenBitmap() {
        return screenBitmap;
    }
    
    public void setScreenBitmap(Bitmap screenBitmap) {
        this.screenBitmap = screenBitmap;
        if (screenBitmap != null) {
            this.screenWidth = screenBitmap.getWidth();
            this.screenHeight = screenBitmap.getHeight();
        }
    }
    
    public String getScreenText() {
        return screenText;
    }
    
    public void setScreenText(String screenText) {
        this.screenText = screenText;
    }
    
    public String getDetectedEntities() {
        return detectedEntities;
    }
    
    public void setDetectedEntities(String detectedEntities) {
        this.detectedEntities = detectedEntities;
    }
    
    public float getComplexityScore() {
        return complexityScore;
    }
    
    public void setComplexityScore(float complexityScore) {
        this.complexityScore = complexityScore;
    }
    
    public String getEnvironmentType() {
        return environmentType;
    }
    
    public void setEnvironmentType(String environmentType) {
        this.environmentType = environmentType;
    }
    
    public String getPlayerState() {
        return playerState;
    }
    
    public void setPlayerState(String playerState) {
        this.playerState = playerState;
    }
    
    public int getScreenWidth() {
        return screenWidth;
    }
    
    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }
    
    public int getScreenHeight() {
        return screenHeight;
    }
    
    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }
    
    public boolean isInCombat() {
        return inCombat;
    }
    
    public void setInCombat(boolean inCombat) {
        this.inCombat = inCombat;
    }
    
    public float getPlayerHealth() {
        return playerHealth;
    }
    
    public void setPlayerHealth(float playerHealth) {
        this.playerHealth = playerHealth;
    }
    
    /**
     * Get UI elements
     * @return UI elements
     */
    public List<UIElement> getUiElements() {
        return uiElements;
    }
    
    /**
     * Set UI elements
     * @param uiElements UI elements
     */
    public void setUiElements(List<UIElement> uiElements) {
        this.uiElements = uiElements;
    }
    
    /**
     * Add UI element
     * @param element UI element
     */
    public void addUiElement(UIElement element) {
        if (uiElements == null) {
            uiElements = new ArrayList<>();
        }
        uiElements.add(element);
    }
    
    /**
     * Get detected enemies
     * @return List of detected enemies
     */
    public List<DetectedEnemy> getEnemies() {
        return enemies;
    }
    
    /**
     * Set detected enemies
     * @param enemies List of detected enemies
     */
    public void setEnemies(List<DetectedEnemy> enemies) {
        this.enemies = enemies;
    }
    
    /**
     * Add detected enemy
     * @param enemy Detected enemy
     */
    public void addEnemy(DetectedEnemy enemy) {
        if (enemies == null) {
            enemies = new ArrayList<>();
        }
        enemies.add(enemy);
    }
    
    /**
     * Convert to feature vector
     * @return Feature vector
     */
    public float[] toFeatureVector() {
        return getFeatures();
    }
}
