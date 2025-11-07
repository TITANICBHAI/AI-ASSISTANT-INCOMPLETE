package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.GameConfig;

import java.util.List;

@Dao
public interface GameConfigDao {
    
    @Insert
    long insert(GameConfig config);
    
    @Update
    void update(GameConfig config);
    
    @Delete
    void delete(GameConfig config);
    
    @Query("SELECT * FROM game_configs ORDER BY lastModifiedDate DESC")
    List<GameConfig> getAll();
    
    @Query("SELECT * FROM game_configs ORDER BY lastModifiedDate DESC")
    LiveData<List<GameConfig>> getAllLive();
    
    @Query("SELECT * FROM game_configs WHERE id = :id LIMIT 1")
    GameConfig getById(long id);
    
    @Query("SELECT * FROM game_configs WHERE gameId = :gameId ORDER BY lastModifiedDate DESC")
    List<GameConfig> getByGameId(String gameId);
    
    @Query("SELECT * FROM game_configs WHERE gameId = :gameId AND active = 1 LIMIT 1")
    GameConfig getActiveConfigForGame(String gameId);
    
    @Query("SELECT * FROM game_configs WHERE active = 1 ORDER BY lastModifiedDate DESC")
    List<GameConfig> getActiveConfigs();
    
    @Query("SELECT * FROM game_configs WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    List<GameConfig> searchByName(String searchQuery);
    
    @Query("UPDATE game_configs SET active = 0 WHERE gameId = :gameId")
    void deactivateAllConfigsForGame(String gameId);
    
    @Query("DELETE FROM game_configs")
    void deleteAll();
    
    @Query("DELETE FROM game_configs WHERE id = :id")
    void deleteById(long id);
    
    @Query("DELETE FROM game_configs WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
}
