package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ModelInfo;

import java.util.List;

/**
 * Data access object for ModelInfo entity
 */
@Dao
public interface ModelDao {
    
    /**
     * Insert a model
     * 
     * @param model The model
     */
    @Insert
    void insert(ModelInfo model);
    
    /**
     * Update a model
     * 
     * @param model The model
     */
    @Update
    void update(ModelInfo model);
    
    /**
     * Delete a model
     * 
     * @param model The model
     */
    @Delete
    void delete(ModelInfo model);
    
    /**
     * Get a model by ID
     * 
     * @param id The model ID
     * @return The model
     */
    @Query("SELECT * FROM models WHERE id = :id")
    ModelInfo getById(String id);
    
    /**
     * Get models for a game
     * 
     * @param gameId The game ID
     * @return The models
     */
    @Query("SELECT * FROM models WHERE gameId = :gameId")
    List<ModelInfo> getByGameId(String gameId);
    
    /**
     * Get active models for a game
     * 
     * @param gameId The game ID
     * @return The models
     */
    @Query("SELECT * FROM models WHERE gameId = :gameId AND isActive = 1")
    List<ModelInfo> getActiveByGameId(String gameId);
    
    /**
     * Get models by algorithm type
     * 
     * @param algorithmType The algorithm type
     * @return The models
     */
    @Query("SELECT * FROM models WHERE algorithmType = :algorithmType")
    List<ModelInfo> getByAlgorithmType(String algorithmType);
    
    /**
     * Get all models
     * 
     * @return The models
     */
    @Query("SELECT * FROM models")
    List<ModelInfo> getAll();
    
    /**
     * Deactivate all models for a game
     * 
     * @param gameId The game ID
     */
    @Query("UPDATE models SET isActive = 0 WHERE gameId = :gameId")
    void deactivateAllForGame(String gameId);
    
    /**
     * Activate a model
     * 
     * @param modelId The model ID
     */
    @Query("UPDATE models SET isActive = 1 WHERE id = :modelId")
    void activateModel(String modelId);
}
