package com.aiassistant.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.GameConfig;

import java.util.List;

@Dao
public interface GameConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GameConfig config);
    
    @Update
    void update(GameConfig config);
    
    @Query("DELETE FROM game_configs WHERE gameId = :gameId")
    void deleteById(String gameId);
    
    @Query("SELECT * FROM game_configs WHERE gameId = :gameId")
    GameConfig getById(String gameId);
    
    @Query("SELECT * FROM game_configs ORDER BY lastUpdated DESC")
    LiveData<List<GameConfig>> getAllConfigs();
    
    @Query("SELECT * FROM game_configs WHERE gamePackage = :packageName LIMIT 1")
    GameConfig getByPackageName(String packageName);
}
