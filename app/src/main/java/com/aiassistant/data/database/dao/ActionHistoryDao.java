package com.aiassistant.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.database.entities.ActionHistory;

import java.util.List;

/**
 * DAO for action history entities
 */
@Dao
public interface ActionHistoryDao {
    
    /**
     * Insert an action history
     * 
     * @param action The action history
     * @return The new row ID
     */
    @Insert
    long insert(ActionHistory action);
    
    /**
     * Update an action history
     * 
     * @param action The action history
     */
    @Update
    void update(ActionHistory action);
    
    /**
     * Delete an action history
     * 
     * @param action The action history
     */
    @Delete
    void delete(ActionHistory action);
    
    /**
     * Get an action history by ID
     * 
     * @param id The ID
     * @return The action history
     */
    @Query("SELECT * FROM action_history WHERE id = :id")
    ActionHistory getById(long id);
    
    /**
     * Get action history for a game
     * 
     * @param gameId The game ID
     * @return The action history
     */
    @Query("SELECT * FROM action_history WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<ActionHistory> getForGame(String gameId);
    
    /**
     * Get action history for a game state
     * 
     * @param gameStateId The game state ID
     * @return The action history
     */
    @Query("SELECT * FROM action_history WHERE gameStateId = :gameStateId")
    List<ActionHistory> getForGameState(long gameStateId);
    
    /**
     * Get AI-generated action history
     * 
     * @param limit The maximum number of results
     * @return The action history
     */
    @Query("SELECT * FROM action_history WHERE isAIGenerated = 1 ORDER BY timestamp DESC LIMIT :limit")
    List<ActionHistory> getAIGeneratedActions(int limit);
    
    /**
     * Get user-generated action history
     * 
     * @param limit The maximum number of results
     * @return The action history
     */
    @Query("SELECT * FROM action_history WHERE isAIGenerated = 0 ORDER BY timestamp DESC LIMIT :limit")
    List<ActionHistory> getUserGeneratedActions(int limit);
    
    /**
     * Delete old action history
     * 
     * @param timestamp The cutoff timestamp
     * @return The number of rows deleted
     */
    @Query("DELETE FROM action_history WHERE timestamp < :timestamp")
    int deleteOldActions(long timestamp);
    
    /**
     * Count action history
     * 
     * @return The count
     */
    @Query("SELECT COUNT(*) FROM action_history")
    int count();
}
