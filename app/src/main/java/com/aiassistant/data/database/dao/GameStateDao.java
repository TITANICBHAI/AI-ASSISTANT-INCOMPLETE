package com.aiassistant.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.database.entities.GameStateEntity;

import java.util.List;

/**
 * DAO for game state entities
 */
@Dao
public interface GameStateDao {
    
    /**
     * Insert a game state
     * 
     * @param gameState The game state
     * @return The new row ID
     */
    @Insert
    long insert(GameStateEntity gameState);
    
    /**
     * Update a game state
     * 
     * @param gameState The game state
     */
    @Update
    void update(GameStateEntity gameState);
    
    /**
     * Delete a game state
     * 
     * @param gameState The game state
     */
    @Delete
    void delete(GameStateEntity gameState);
    
    /**
     * Get a game state by ID
     * 
     * @param id The ID
     * @return The game state
     */
    @Query("SELECT * FROM game_states WHERE id = :id")
    GameStateEntity getById(long id);
    
    /**
     * Get game states for a game
     * 
     * @param gameId The game ID
     * @return The game states
     */
    @Query("SELECT * FROM game_states WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<GameStateEntity> getForGame(String gameId);
    
    /**
     * Get the most recent game state
     * 
     * @return The game state
     */
    @Query("SELECT * FROM game_states ORDER BY timestamp DESC LIMIT 1")
    GameStateEntity getMostRecent();
    
    /**
     * Get game states with combat
     * 
     * @param limit The maximum number of results
     * @return The game states
     */
    @Query("SELECT * FROM game_states WHERE inCombat = 1 ORDER BY timestamp DESC LIMIT :limit")
    List<GameStateEntity> getCombatStates(int limit);
    
    /**
     * Delete old game states
     * 
     * @param timestamp The cutoff timestamp
     * @return The number of rows deleted
     */
    @Query("DELETE FROM game_states WHERE timestamp < :timestamp")
    int deleteOldStates(long timestamp);
    
    /**
     * Count game states
     * 
     * @return The count
     */
    @Query("SELECT COUNT(*) FROM game_states")
    int count();
}
