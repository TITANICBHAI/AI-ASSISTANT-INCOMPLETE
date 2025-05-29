package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.models.AISettings;

import java.util.List;

/**
 * Data Access Object (DAO) for AISettings.
 * This interface defines the database operations for AISettings entities.
 */
@Dao
public interface AISettingsDao {
    
    /**
     * Insert AI settings
     * @param aiSettings Settings to insert
     * @return Inserted row ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AISettings aiSettings);
    
    /**
     * Update AI settings
     * @param aiSettings Settings to update
     */
    @Update
    void update(AISettings aiSettings);
    
    /**
     * Delete AI settings
     * @param aiSettings Settings to delete
     */
    @Delete
    void delete(AISettings aiSettings);
    
    /**
     * Get all AI settings
     * @return List of all settings
     */
    @Query("SELECT * FROM ai_settings")
    List<AISettings> getAllSettings();
    
    /**
     * Get default AI settings
     * @return Default settings or null
     */
    @Query("SELECT * FROM ai_settings LIMIT 1")
    AISettings getDefaultSettings();
    
    /**
     * Delete all AI settings
     */
    @Query("DELETE FROM ai_settings")
    void deleteAllSettings();
}