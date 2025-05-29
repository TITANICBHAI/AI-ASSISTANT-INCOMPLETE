package com.aiassistant.core.ai.models;

// Use fully qualified import to avoid conflict
import com.aiassistant.data.models.GameState.GameStateType;

import java.util.List;
import java.util.Map;

/**
 * Represents the current state of a game for AI analysis
 */
public class GameState {
    private String gameId;
    private Map<String, Object> environmentData;
    private List<Map<String, Object>> entities;
    private Map<String, Object> playerState;
    private long timestamp;
    private String gameStateType;

    /**
     * Default constructor for GameState
     */
    public GameState() {
        // Default constructor
    }

    /**
     * Full constructor for GameState
     * @param gameId Game ID
     * @param environmentData Environment data
     * @param entities Game entities
     * @param playerState Player state
     * @param timestamp Timestamp
     * @param gameStateType Game state type
     */
    public GameState(String gameId, Map<String, Object> environmentData, 
                    List<Map<String, Object>> entities, Map<String, Object> playerState, 
                    long timestamp, String gameStateType) {
        this.gameId = gameId;
        this.environmentData = environmentData;
        this.entities = entities;
        this.playerState = playerState;
        this.timestamp = timestamp;
        this.gameStateType = gameStateType;
    }

    /**
     * Get game ID
     * @return Game ID
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Set game ID
     * @param gameId New game ID
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    /**
     * Get environment data
     * @return Environment data
     */
    public Map<String, Object> getEnvironmentData() {
        return environmentData;
    }

    /**
     * Set environment data
     * @param environmentData New environment data
     */
    public void setEnvironmentData(Map<String, Object> environmentData) {
        this.environmentData = environmentData;
    }

    /**
     * Get entities
     * @return Entities
     */
    public List<Map<String, Object>> getEntities() {
        return entities;
    }

    /**
     * Set entities
     * @param entities New entities
     */
    public void setEntities(List<Map<String, Object>> entities) {
        this.entities = entities;
    }

    /**
     * Get player state
     * @return Player state
     */
    public Map<String, Object> getPlayerState() {
        return playerState;
    }

    /**
     * Set player state
     * @param playerState New player state
     */
    public void setPlayerState(Map<String, Object> playerState) {
        this.playerState = playerState;
    }

    /**
     * Get timestamp
     * @return Timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set timestamp
     * @param timestamp New timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get game state type
     * @return Game state type
     */
    public String getGameStateType() {
        return gameStateType;
    }

    /**
     * Set game state type
     * @param gameStateType New game state type
     */
    public void setGameStateType(String gameStateType) {
        this.gameStateType = gameStateType;
    }
}
