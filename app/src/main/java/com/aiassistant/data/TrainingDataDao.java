package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.TrainingData;

import java.util.List;

@Dao
public interface TrainingDataDao {
    
    @Insert
    long insert(TrainingData data);
    
    @Update
    void update(TrainingData data);
    
    @Delete
    void delete(TrainingData data);
    
    @Query("SELECT * FROM training_data ORDER BY timestamp DESC")
    List<TrainingData> getAll();
    
    @Query("SELECT * FROM training_data ORDER BY timestamp DESC")
    LiveData<List<TrainingData>> getAllLive();
    
    @Query("SELECT * FROM training_data WHERE id = :id LIMIT 1")
    TrainingData getById(String id);
    
    @Query("SELECT * FROM training_data WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<TrainingData> getByGameId(String gameId);
    
    @Query("SELECT * FROM training_data WHERE modelId = :modelId ORDER BY timestamp DESC")
    List<TrainingData> getByModelId(String modelId);
    
    @Query("SELECT * FROM training_data WHERE gameId = :gameId AND modelId = :modelId ORDER BY timestamp DESC")
    List<TrainingData> getByGameIdAndModelId(String gameId, String modelId);
    
    @Query("SELECT * FROM training_data WHERE trainingEpisode = :episode ORDER BY trainingStep ASC")
    List<TrainingData> getByEpisode(int episode);
    
    @Query("SELECT * FROM training_data WHERE usedForTraining = 0 ORDER BY timestamp ASC LIMIT :limit")
    List<TrainingData> getUnusedTrainingData(int limit);
    
    @Query("SELECT * FROM training_data WHERE terminal = 1 ORDER BY timestamp DESC")
    List<TrainingData> getTerminalStates();
    
    @Query("UPDATE training_data SET usedForTraining = 1 WHERE id = :id")
    void markAsUsed(String id);
    
    @Query("DELETE FROM training_data")
    void deleteAll();
    
    @Query("DELETE FROM training_data WHERE id = :id")
    void deleteById(String id);
    
    @Query("DELETE FROM training_data WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
}
