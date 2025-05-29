package com.aiassistant.data.database.entities;

import android.graphics.Rect;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.database.converters.Converters;
import com.aiassistant.data.models.GameState;

import java.util.Map;

/**
 * Entity for storing game states in the database
 */
@Entity(tableName = "game_states")
@TypeConverters(Converters.class)
public class GameStateEntity {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String gameId;
    private long timestamp;
    private int screenWidth;
    private int screenHeight;
    private boolean inCombat;
    private float playerHealth;
    private float playerAmmo;
    private String locationName;
    private String gamePhase;
    
    // Serialized data
    private String uiElementsJson;
    private String enemiesJson;
    private String gameParametersJson;
    private float[] featureVector;
    
    /**
     * Default constructor
     */
    public GameStateEntity() {
    }
    
    /**
     * Convert from GameState
     * 
     * @param gameState The game state
     * @return The entity
     */
    public static GameStateEntity fromGameState(GameState gameState) {
        if (gameState == null) {
            return null;
        }
        
        GameStateEntity entity = new GameStateEntity();
        entity.setGameId(gameState.getGameId());
        entity.setTimestamp(gameState.getTimestamp());
        entity.setScreenWidth(gameState.getScreenWidth());
        entity.setScreenHeight(gameState.getScreenHeight());
        entity.setInCombat(gameState.isInCombat());
        entity.setPlayerHealth(gameState.getPlayerHealth());
        entity.setPlayerAmmo(gameState.getPlayerAmmo());
        entity.setLocationName(gameState.getLocationName());
        entity.setGamePhase(gameState.getGamePhase());
        
        // Get feature vector
        entity.setFeatureVector(gameState.toFeatureVector());
        
        return entity;
    }
    
    /**
     * Convert to GameState
     * 
     * @return The game state
     */
    public GameState toGameState() {
        GameState gameState = new GameState();
        gameState.setGameId(gameId);
        gameState.setTimestamp(timestamp);
        gameState.setScreenWidth(screenWidth);
        gameState.setScreenHeight(screenHeight);
        gameState.setInCombat(inCombat);
        gameState.setPlayerHealth(playerHealth);
        gameState.setPlayerAmmo(playerAmmo);
        gameState.setLocationName(locationName);
        gameState.setGamePhase(gamePhase);
        
        return gameState;
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
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
    
    public float getPlayerAmmo() {
        return playerAmmo;
    }
    
    public void setPlayerAmmo(float playerAmmo) {
        this.playerAmmo = playerAmmo;
    }
    
    public String getLocationName() {
        return locationName;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    public String getGamePhase() {
        return gamePhase;
    }
    
    public void setGamePhase(String gamePhase) {
        this.gamePhase = gamePhase;
    }
    
    public String getUiElementsJson() {
        return uiElementsJson;
    }
    
    public void setUiElementsJson(String uiElementsJson) {
        this.uiElementsJson = uiElementsJson;
    }
    
    public String getEnemiesJson() {
        return enemiesJson;
    }
    
    public void setEnemiesJson(String enemiesJson) {
        this.enemiesJson = enemiesJson;
    }
    
    public String getGameParametersJson() {
        return gameParametersJson;
    }
    
    public void setGameParametersJson(String gameParametersJson) {
        this.gameParametersJson = gameParametersJson;
    }
    
    public float[] getFeatureVector() {
        return featureVector;
    }
    
    public void setFeatureVector(float[] featureVector) {
        this.featureVector = featureVector;
    }
}
