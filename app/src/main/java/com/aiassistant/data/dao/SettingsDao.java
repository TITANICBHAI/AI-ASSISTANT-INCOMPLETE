package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.Settings;

import java.util.List;

/**
 * Data access object for Settings entity
 */
@Dao
public interface SettingsDao {
    
    /**
     * Insert a setting
     * 
     * @param setting The setting
     */
    @Insert
    void insert(Settings setting);
    
    /**
     * Update a setting
     * 
     * @param setting The setting
     */
    @Update
    void update(Settings setting);
    
    /**
     * Delete a setting
     * 
     * @param setting The setting
     */
    @Delete
    void delete(Settings setting);
    
    /**
     * Get a setting by key
     * 
     * @param key The key
     * @return The setting
     */
    @Query("SELECT * FROM settings WHERE `key` = :key")
    Settings getByKey(String key);
    
    /**
     * Get all settings
     * 
     * @return The settings
     */
    @Query("SELECT * FROM settings")
    List<Settings> getAll();
    
    /**
     * Get settings by value type
     * 
     * @param valueType The value type
     * @return The settings
     */
    @Query("SELECT * FROM settings WHERE valueType = :valueType")
    List<Settings> getByValueType(String valueType);
    
    /**
     * Get encrypted settings
     * 
     * @return The settings
     */
    @Query("SELECT * FROM settings WHERE isEncrypted = 1")
    List<Settings> getEncryptedSettings();
    
    /**
     * Delete a setting by key
     * 
     * @param key The key
     * @return The number of deleted settings
     */
    @Query("DELETE FROM settings WHERE `key` = :key")
    int deleteByKey(String key);
}
