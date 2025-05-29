package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.Game;

import java.util.List;

/**
 * Data access object for Game entity
 */
@Dao
public interface GameDao {
    
    /**
     * Insert a game
     * 
     * @param game The game
     */
    @Insert
    void insert(Game game);
    
    /**
     * Update a game
     * 
     * @param game The game
     */
    @Update
    void update(Game game);
    
    /**
     * Delete a game
     * 
     * @param game The game
     */
    @Delete
    void delete(Game game);
    
    /**
     * Get a game by ID
     * 
     * @param id The game ID
     * @return The game
     */
    @Query("SELECT * FROM games WHERE id = :id")
    Game getById(String id);
    
    /**
     * Get a game by package name
     * 
     * @param packageName The package name
     * @return The game
     */
    @Query("SELECT * FROM games WHERE packageName = :packageName")
    Game getByPackageName(String packageName);
    
    /**
     * Get all games
     * 
     * @return The games
     */
    @Query("SELECT * FROM games")
    List<Game> getAll();
    
    /**
     * Get games by type
     * 
     * @param gameType The game type
     * @return The games
     */
    @Query("SELECT * FROM games WHERE gameType = :gameType")
    List<Game> getByType(String gameType);
    
    /**
     * Get recently played games
     * 
     * @param limit The maximum number of games
     * @return The games
     */
    @Query("SELECT * FROM games ORDER BY lastPlayedDate DESC LIMIT :limit")
    List<Game> getRecentlyPlayed(int limit);
}
