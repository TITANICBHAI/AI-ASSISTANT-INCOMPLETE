package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.GameAction;

import java.util.List;

@Dao
public interface GameActionDao {
    
    @Insert
    long insert(GameAction action);
    
    @Update
    void update(GameAction action);
    
    @Delete
    void delete(GameAction action);
    
    @Query("SELECT * FROM game_actions ORDER BY timestamp DESC")
    List<GameAction> getAll();
    
    @Query("SELECT * FROM game_actions ORDER BY timestamp DESC")
    LiveData<List<GameAction>> getAllLive();
    
    @Query("SELECT * FROM game_actions WHERE id = :id LIMIT 1")
    GameAction getById(long id);
    
    @Query("SELECT * FROM game_actions WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<GameAction> getByGameId(String gameId);
    
    @Query("SELECT * FROM game_actions WHERE actionType = :actionType ORDER BY timestamp DESC")
    List<GameAction> getByActionType(int actionType);
    
    @Query("SELECT * FROM game_actions WHERE gameId = :gameId AND actionType = :actionType ORDER BY timestamp DESC")
    List<GameAction> getByGameIdAndActionType(String gameId, int actionType);
    
    @Query("SELECT * FROM game_actions ORDER BY timestamp DESC LIMIT :limit")
    List<GameAction> getRecentActions(int limit);
    
    @Query("SELECT * FROM game_actions WHERE gameId = :gameId ORDER BY timestamp DESC LIMIT :limit")
    List<GameAction> getRecentActionsByGameId(String gameId, int limit);
    
    @Query("DELETE FROM game_actions")
    void deleteAll();
    
    @Query("DELETE FROM game_actions WHERE id = :id")
    void deleteById(long id);
    
    @Query("DELETE FROM game_actions WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
}
