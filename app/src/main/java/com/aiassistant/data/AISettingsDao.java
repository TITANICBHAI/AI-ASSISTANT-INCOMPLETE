package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.AISettings;

import java.util.List;

@Dao
public interface AISettingsDao {
    
    @Insert
    long insert(AISettings settings);
    
    @Update
    void update(AISettings settings);
    
    @Delete
    void delete(AISettings settings);
    
    @Query("SELECT * FROM ai_settings ORDER BY lastModified DESC")
    List<AISettings> getAll();
    
    @Query("SELECT * FROM ai_settings ORDER BY lastModified DESC")
    LiveData<List<AISettings>> getAllLive();
    
    @Query("SELECT * FROM ai_settings WHERE id = :id LIMIT 1")
    AISettings getById(long id);
    
    @Query("SELECT * FROM ai_settings LIMIT 1")
    AISettings getCurrentSettings();
    
    @Query("SELECT * FROM ai_settings LIMIT 1")
    LiveData<AISettings> getCurrentSettingsLive();
    
    @Query("SELECT aiMode FROM ai_settings LIMIT 1")
    String getAIMode();
    
    @Query("UPDATE ai_settings SET aiMode = :mode, lastModified = :timestamp WHERE id = :id")
    void updateAIMode(long id, String mode, long timestamp);
    
    @Query("UPDATE ai_settings SET learningRate = :rate, lastModified = :timestamp WHERE id = :id")
    void updateLearningRate(long id, float rate, long timestamp);
    
    @Query("DELETE FROM ai_settings")
    void deleteAll();
}
