package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.Settings;

import java.util.List;

@Dao
public interface SettingsDao {
    
    @Insert
    long insert(Settings settings);
    
    @Update
    void update(Settings settings);
    
    @Delete
    void delete(Settings settings);
    
    @Query("SELECT * FROM settings ORDER BY lastUpdated DESC")
    List<Settings> getAll();
    
    @Query("SELECT * FROM settings ORDER BY lastUpdated DESC")
    LiveData<List<Settings>> getAllLive();
    
    @Query("SELECT * FROM settings WHERE key = :key LIMIT 1")
    Settings getByKey(String key);
    
    @Query("SELECT * FROM settings WHERE key = :key LIMIT 1")
    LiveData<Settings> getByKeyLive(String key);
    
    @Query("SELECT value FROM settings WHERE key = :key LIMIT 1")
    String getValue(String key);
    
    @Query("SELECT * FROM settings WHERE valueType = :valueType ORDER BY key ASC")
    List<Settings> getByValueType(String valueType);
    
    @Query("SELECT * FROM settings WHERE isEncrypted = 1 ORDER BY key ASC")
    List<Settings> getEncryptedSettings();
    
    @Query("UPDATE settings SET value = :value, lastUpdated = :timestamp WHERE key = :key")
    void updateValue(String key, String value, long timestamp);
    
    @Query("DELETE FROM settings")
    void deleteAll();
    
    @Query("DELETE FROM settings WHERE key = :key")
    void deleteByKey(String key);
}
