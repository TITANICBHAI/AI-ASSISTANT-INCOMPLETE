package com.aiassistant.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.database.entities.ModelStorageEntity;

import java.util.List;

/**
 * DAO for model storage entities
 */
@Dao
public interface ModelStorageDao {
    
    /**
     * Insert a model
     * 
     * @param model The model
     * @return The new row ID
     */
    @Insert
    long insert(ModelStorageEntity model);
    
    /**
     * Update a model
     * 
     * @param model The model
     */
    @Update
    void update(ModelStorageEntity model);
    
    /**
     * Delete a model
     * 
     * @param model The model
     */
    @Delete
    void delete(ModelStorageEntity model);
    
    /**
     * Get a model by ID
     * 
     * @param id The ID
     * @return The model
     */
    @Query("SELECT * FROM model_storage WHERE id = :id")
    ModelStorageEntity getById(long id);
    
    /**
     * Get a model by model ID
     * 
     * @param modelId The model ID
     * @return The model
     */
    @Query("SELECT * FROM model_storage WHERE modelId = :modelId")
    ModelStorageEntity getByModelId(String modelId);
    
    /**
     * Get models for a game
     * 
     * @param gameId The game ID
     * @return The models
     */
    @Query("SELECT * FROM model_storage WHERE gameId = :gameId")
    List<ModelStorageEntity> getForGame(String gameId);
    
    /**
     * Get models of a specific type
     * 
     * @param modelType The model type
     * @return The models
     */
    @Query("SELECT * FROM model_storage WHERE modelType = :modelType")
    List<ModelStorageEntity> getByType(int modelType);
    
    /**
     * Get all models
     * 
     * @return The models
     */
    @Query("SELECT * FROM model_storage")
    List<ModelStorageEntity> getAll();
    
    /**
     * Delete models for a game
     * 
     * @param gameId The game ID
     * @return The number of rows deleted
     */
    @Query("DELETE FROM model_storage WHERE gameId = :gameId")
    int deleteForGame(String gameId);
    
    /**
     * Count models
     * 
     * @return The count
     */
    @Query("SELECT COUNT(*) FROM model_storage")
    int count();
}
