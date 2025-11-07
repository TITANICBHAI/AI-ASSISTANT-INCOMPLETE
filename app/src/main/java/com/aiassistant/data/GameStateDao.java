package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.GameState;

import java.util.List;

/**
 * Data Access Object for GameState entities
 * Consolidated from multiple DAO implementations
 */
@Dao
public interface GameStateDao {
    
    /**
     * Insert game state
     * @param gameState Game state to insert
     * @return Inserted ID
     */
    @Insert
    long insert(GameState gameState);
    
    /**
     * Insert multiple game states
     * @param gameStates The game states
     * @return The inserted IDs
     */
    @Insert
    List<Long> insertAll(List<GameState> gameStates);
    
    /**
     * Update game state
     * @param gameState Game state to update
     */
    @Update
    void update(GameState gameState);
    
    /**
     * Delete game state
     * @param gameState Game state to delete
     */
    @Delete
    void delete(GameState gameState);
    
    /**
     * Get all game states
     * @return All game states ordered by timestamp DESC
     */
    @Query("SELECT * FROM game_states ORDER BY timestamp DESC")
    List<GameState> getAllGameStates();
    
    /**
     * Get all game states as LiveData
     * @return All game states as LiveData
     */
    @Query("SELECT * FROM game_states ORDER BY timestamp DESC")
    LiveData<List<GameState>> getAllGameStatesLive();
    
    /**
     * Get game state by ID
     * @param id The ID
     * @return The game state
     */
    @Query("SELECT * FROM game_states WHERE id = :id")
    GameState getById(long id);
    
    /**
     * Get game states for game
     * @param gameId Game ID
     * @return Game states for specified game
     */
    @Query("SELECT * FROM game_states WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<GameState> getGameStatesForGame(String gameId);
    
    /**
     * Get latest game state by game ID
     * @param gameId The game ID
     * @return The game state
     */
    @Query("SELECT * FROM game_states WHERE gameId = :gameId ORDER BY timestamp DESC LIMIT 1")
    GameState getLatestByGameId(String gameId);
    
    /**
     * Get game states by type
     * @param gameStateType Game state type
     * @return Game states with specified type
     */
    @Query("SELECT * FROM game_states WHERE gameStateType = :gameStateType ORDER BY timestamp DESC")
    List<GameState> getGameStatesByType(String gameStateType);
    
    /**
     * Get game states by game ID and time range
     * @param gameId The game ID
     * @param startTime The start time
     * @param endTime The end time
     * @return The game states
     */
    @Query("SELECT * FROM game_states WHERE gameId = :gameId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    List<GameState> getByGameIdAndTimeRange(String gameId, long startTime, long endTime);
    
    /**
     * Count game states by game ID
     * @param gameId The game ID
     * @return The count
     */
    @Query("SELECT COUNT(*) FROM game_states WHERE gameId = :gameId")
    int countByGameId(String gameId);
    
    /**
     * Delete game states by game ID
     * @param gameId The game ID
     */
    @Query("DELETE FROM game_states WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
    
    /**
     * Delete old game states
     * @param timestamp Timestamp threshold
     * @return Number of deleted rows
     */
    @Query("DELETE FROM game_states WHERE timestamp < :timestamp")
    int deleteOldGameStates(long timestamp);
}
