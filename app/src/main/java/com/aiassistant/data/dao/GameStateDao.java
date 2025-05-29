package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.GameState;

import java.util.List;

/**
 * Data access object for GameState entity
 */
@Dao
public interface GameStateDao {
    
    /**
     * Insert a game state
     * 
     * @param gameState The game state
     */
    @Insert
    void insert(GameState gameState);
    
    /**
     * Update a game state
     * 
     * @param gameState The game state
     */
    @Update
    void update(GameState gameState);
    
    /**
     * Delete a game state
     * 
     * @param gameState The game state
     */
    @Delete
    void delete(GameState gameState);
    
    /**
     * Get a game state by ID
     * 
     * @param id The game state ID
     * @return The game state
     */
    @Query("SELECT * FROM game_states WHERE id = :id")
    GameState getById(String id);
    
    /**
     * Get game states for a game
     * 
     * @param gameId The game ID
     * @return The game states
     */
    @Query("SELECT * FROM game_states WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<GameState> getByGameId(String gameId);
    
    /**
     * Get recent game states
     * 
     * @param limit The maximum number of states
     * @return The game states
     */
    @Query("SELECT * FROM game_states ORDER BY timestamp DESC LIMIT :limit")
    List<GameState> getRecent(int limit);
    
    /**
     * Get game states in combat
     * 
     * @param gameId The game ID
     * @return The game states
     */
    @Query("SELECT * FROM game_states WHERE gameId = :gameId AND inCombat = 1 ORDER BY timestamp DESC")
    List<GameState> getCombatStates(String gameId);
    
    /**
     * Delete old game states
     * 
     * @param timestamp Older than this timestamp
     * @return The number of deleted states
     */
    @Query("DELETE FROM game_states WHERE timestamp < :timestamp")
    int deleteOldStates(long timestamp);
}
