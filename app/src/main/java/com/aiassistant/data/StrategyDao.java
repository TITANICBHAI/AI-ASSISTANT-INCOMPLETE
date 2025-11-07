package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.Strategy;

import java.util.List;

@Dao
public interface StrategyDao {
    
    @Insert
    long insert(Strategy strategy);
    
    @Update
    void update(Strategy strategy);
    
    @Delete
    void delete(Strategy strategy);
    
    @Query("SELECT * FROM strategies ORDER BY updatedAt DESC")
    List<Strategy> getAll();
    
    @Query("SELECT * FROM strategies ORDER BY updatedAt DESC")
    LiveData<List<Strategy>> getAllLive();
    
    @Query("SELECT * FROM strategies WHERE strategyId = :strategyId LIMIT 1")
    Strategy getById(String strategyId);
    
    @Query("SELECT * FROM strategies WHERE gameId = :gameId ORDER BY updatedAt DESC")
    List<Strategy> getByGameId(String gameId);
    
    @Query("SELECT * FROM strategies WHERE isActive = 1 ORDER BY updatedAt DESC")
    List<Strategy> getActiveStrategies();
    
    @Query("SELECT * FROM strategies WHERE gameId = :gameId AND isActive = 1 ORDER BY updatedAt DESC")
    List<Strategy> getActiveStrategiesByGameId(String gameId);
    
    @Query("SELECT * FROM strategies WHERE targetScenario = :scenario ORDER BY updatedAt DESC")
    List<Strategy> getByTargetScenario(String scenario);
    
    @Query("SELECT * FROM strategies WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    List<Strategy> searchByName(String searchQuery);
    
    @Query("DELETE FROM strategies")
    void deleteAll();
    
    @Query("DELETE FROM strategies WHERE strategyId = :strategyId")
    void deleteById(String strategyId);
    
    @Query("DELETE FROM strategies WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
}
