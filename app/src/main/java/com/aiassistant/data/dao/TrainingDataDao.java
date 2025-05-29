package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.TrainingData;

import java.util.List;

/**
 * Data access object for TrainingData entity
 */
@Dao
public interface TrainingDataDao {
    
    /**
     * Insert training data
     * 
     * @param trainingData The training data
     */
    @Insert
    void insert(TrainingData trainingData);
    
    /**
     * Insert multiple training data
     * 
     * @param trainingDataList The training data list
     */
    @Insert
    void insertAll(List<TrainingData> trainingDataList);
    
    /**
     * Update training data
     * 
     * @param trainingData The training data
     */
    @Update
    void update(TrainingData trainingData);
    
    /**
     * Delete training data
     * 
     * @param trainingData The training data
     */
    @Delete
    void delete(TrainingData trainingData);
    
    /**
     * Get training data by ID
     * 
     * @param id The ID
     * @return The training data
     */
    @Query("SELECT * FROM training_data WHERE id = :id")
    TrainingData getById(String id);
    
    /**
     * Get training data by game ID
     * 
     * @param gameId The game ID
     * @return The training data
     */
    @Query("SELECT * FROM training_data WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<TrainingData> getByGameId(String gameId);
    
    /**
     * Get training data by model ID
     * 
     * @param modelId The model ID
     * @return The training data
     */
    @Query("SELECT * FROM training_data WHERE modelId = :modelId ORDER BY timestamp DESC")
    List<TrainingData> getByModelId(String modelId);
    
    /**
     * Get training data by game ID and model ID
     * 
     * @param gameId The game ID
     * @param modelId The model ID
     * @return The training data
     */
    @Query("SELECT * FROM training_data WHERE gameId = :gameId AND modelId = :modelId ORDER BY timestamp DESC")
    List<TrainingData> getByGameIdAndModelId(String gameId, String modelId);
    
    /**
     * Get unused training data by model ID
     * 
     * @param modelId The model ID
     * @param limit The maximum number of records
     * @return The training data
     */
    @Query("SELECT * FROM training_data WHERE modelId = :modelId AND usedForTraining = 0 ORDER BY timestamp ASC LIMIT :limit")
    List<TrainingData> getUnusedByModelId(String modelId, int limit);
    
    /**
     * Mark training data as used
     * 
     * @param id The ID
     */
    @Query("UPDATE training_data SET usedForTraining = 1 WHERE id = :id")
    void markAsUsed(String id);
    
    /**
     * Mark all training data for a model as used
     * 
     * @param modelId The model ID
     */
    @Query("UPDATE training_data SET usedForTraining = 1 WHERE modelId = :modelId")
    void markAllAsUsed(String modelId);
    
    /**
     * Get the count of training data by model ID
     * 
     * @param modelId The model ID
     * @return The count
     */
    @Query("SELECT COUNT(*) FROM training_data WHERE modelId = :modelId")
    int getCountByModelId(String modelId);
    
    /**
     * Get the count of unused training data by model ID
     * 
     * @param modelId The model ID
     * @return The count
     */
    @Query("SELECT COUNT(*) FROM training_data WHERE modelId = :modelId AND usedForTraining = 0")
    int getUnusedCountByModelId(String modelId);
    
    /**
     * Delete old training data
     * 
     * @param timestamp Older than this timestamp
     * @return The number of deleted records
     */
    @Query("DELETE FROM training_data WHERE timestamp < :timestamp")
    int deleteOldData(long timestamp);
}
