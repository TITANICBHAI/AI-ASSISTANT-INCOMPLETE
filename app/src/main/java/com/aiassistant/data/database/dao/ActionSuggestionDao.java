package com.aiassistant.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ActionSuggestion;

import java.util.List;

/**
 * DAO for action suggestion entities
 */
@Dao
public interface ActionSuggestionDao {
    
    /**
     * Insert a suggestion
     * 
     * @param suggestion The suggestion
     * @return The new row ID
     */
    @Insert
    long insert(ActionSuggestion suggestion);
    
    /**
     * Update a suggestion
     * 
     * @param suggestion The suggestion
     */
    @Update
    void update(ActionSuggestion suggestion);
    
    /**
     * Delete a suggestion
     * 
     * @param suggestion The suggestion
     */
    @Delete
    void delete(ActionSuggestion suggestion);
    
    /**
     * Get a suggestion by ID
     * 
     * @param id The ID
     * @return The suggestion
     */
    @Query("SELECT * FROM action_suggestions WHERE id = :id")
    ActionSuggestion getById(long id);
    
    /**
     * Get suggestions for a game
     * 
     * @param gameId The game ID
     * @return The suggestions
     */
    @Query("SELECT * FROM action_suggestions WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<ActionSuggestion> getForGame(String gameId);
    
    /**
     * Get suggestions for a game state
     * 
     * @param gameStateId The game state ID
     * @return The suggestions
     */
    @Query("SELECT * FROM action_suggestions WHERE gameStateId = :gameStateId")
    List<ActionSuggestion> getForGameState(long gameStateId);
    
    /**
     * Get accepted suggestions
     * 
     * @param limit The maximum number of results
     * @return The suggestions
     */
    @Query("SELECT * FROM action_suggestions WHERE isAccepted = 1 ORDER BY timestamp DESC LIMIT :limit")
    List<ActionSuggestion> getAcceptedSuggestions(int limit);
    
    /**
     * Delete old suggestions
     * 
     * @param timestamp The cutoff timestamp
     * @return The number of rows deleted
     */
    @Query("DELETE FROM action_suggestions WHERE timestamp < :timestamp")
    int deleteOldSuggestions(long timestamp);
    
    /**
     * Count suggestions
     * 
     * @return The count
     */
    @Query("SELECT COUNT(*) FROM action_suggestions")
    int count();
}
