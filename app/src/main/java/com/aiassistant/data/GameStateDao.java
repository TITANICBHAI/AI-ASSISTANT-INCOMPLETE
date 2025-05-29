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
     * @return All game states
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
     * Get game states for game
     * @param gameId Game ID
     * @return Game states for specified game
     */
    @Query("SELECT * FROM game_states WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<GameState> getGameStatesForGame(String gameId);

    /**
     * Get game states by type
     * @param gameStateType Game state type
     * @return Game states with specified type
     */
    @Query("SELECT * FROM game_states WHERE gameStateType = :gameStateType ORDER BY timestamp DESC")
    List<GameState> getGameStatesByType(String gameStateType);

    /**
     * Delete old game states
     * @param timestamp Timestamp threshold
     * @return Number of deleted rows
     */
    @Query("DELETE FROM game_states WHERE timestamp < :timestamp")
    int deleteOldGameStates(long timestamp);
}
