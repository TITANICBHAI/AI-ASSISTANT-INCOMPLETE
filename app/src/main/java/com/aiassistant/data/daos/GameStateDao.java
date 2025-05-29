package com.aiassistant.data.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.aiassistant.data.models.GameState;

import java.util.List;

/**
 * DAO for GameState
 */
@Dao
public interface GameStateDao {
    
    /**
     * Get all game states
     * 
     * @return The game states
     */
    @Query("SELECT * FROM game_states ORDER BY timestamp DESC")
    List<GameState> getAll();
    
    /**
     * Get game state by ID
     * 
     * @param id The ID
     * @return The game state
     */
    @Query("SELECT * FROM game_states WHERE id = :id")
    GameState getById(long id);
    
    /**
     * Get game states by game ID
     * 
     * @param gameId The game ID
     * @return The game states
     */
    @Query("SELECT * FROM game_states WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<GameState> getByGameId(String gameId);
    
    /**
     * Get latest game state by game ID
     * 
     * @param gameId The game ID
     * @return The game state
     */
    @Query("SELECT * FROM game_states WHERE gameId = :gameId ORDER BY timestamp DESC LIMIT 1")
    GameState getLatestByGameId(String gameId);
    
    /**
     * Get game states by game ID and time range
     * 
     * @param gameId The game ID
     * @param startTime The start time
     * @param endTime The end time
     * @return The game states
     */
    @Query("SELECT * FROM game_states WHERE gameId = :gameId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    List<GameState> getByGameIdAndTimeRange(String gameId, long startTime, long endTime);
    
    /**
     * Insert a game state
     * 
     * @param gameState The game state
     * @return The inserted ID
     */
    @Insert
    long insert(GameState gameState);
    
    /**
     * Insert multiple game states
     * 
     * @param gameStates The game states
     * @return The inserted IDs
     */
    @Insert
    List<Long> insertAll(List<GameState> gameStates);
    
    /**
     * Update a game state
     * 
     * @param gameState The game state
     */
    @Update
    void update(GameState gameState);
    
    /**
     * Delete a game state
     * 
     * @param gameState The game state
     */
    @Delete
    void delete(GameState gameState);
    
    /**
     * Delete game states by game ID
     * 
     * @param gameId The game ID
     */
    @Query("DELETE FROM game_states WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
    
    /**
     * Delete old game states
     * 
     * @param timestamp The cutoff timestamp
     */
    @Query("DELETE FROM game_states WHERE timestamp < :timestamp")
    void deleteOld(long timestamp);
    
    /**
     * Count game states by game ID
     * 
     * @param gameId The game ID
     * @return The count
     */
    @Query("SELECT COUNT(*) FROM game_states WHERE gameId = :gameId")
    int countByGameId(String gameId);
}
