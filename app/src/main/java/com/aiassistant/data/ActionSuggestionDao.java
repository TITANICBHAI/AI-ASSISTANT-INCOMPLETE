package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ActionSuggestion;

import java.util.List;

@Dao
public interface ActionSuggestionDao {
    
    @Insert
    long insert(ActionSuggestion suggestion);
    
    @Update
    void update(ActionSuggestion suggestion);
    
    @Delete
    void delete(ActionSuggestion suggestion);
    
    @Query("SELECT * FROM action_suggestions ORDER BY timestamp DESC")
    List<ActionSuggestion> getAll();
    
    @Query("SELECT * FROM action_suggestions ORDER BY timestamp DESC")
    LiveData<List<ActionSuggestion>> getAllLive();
    
    @Query("SELECT * FROM action_suggestions WHERE id = :id LIMIT 1")
    ActionSuggestion getById(long id);
    
    @Query("SELECT * FROM action_suggestions WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<ActionSuggestion> getByGameId(String gameId);
    
    @Query("SELECT * FROM action_suggestions WHERE gameStateId = :gameStateId ORDER BY confidence DESC")
    List<ActionSuggestion> getByGameStateId(long gameStateId);
    
    @Query("SELECT * FROM action_suggestions WHERE isAccepted = 1 ORDER BY timestamp DESC")
    List<ActionSuggestion> getAcceptedSuggestions();
    
    @Query("SELECT * FROM action_suggestions WHERE confidence >= :minConfidence ORDER BY confidence DESC")
    List<ActionSuggestion> getHighConfidenceSuggestions(float minConfidence);
    
    @Query("DELETE FROM action_suggestions")
    void deleteAll();
    
    @Query("DELETE FROM action_suggestions WHERE id = :id")
    void deleteById(long id);
    
    @Query("DELETE FROM action_suggestions WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
}
