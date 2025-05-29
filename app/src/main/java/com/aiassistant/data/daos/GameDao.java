package com.aiassistant.data.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.Game;

import java.util.List;

/**
 * DAO for Game
 */
@Dao
public interface GameDao {
    
    /**
     * Get all games
     * 
     * @return The games
     */
    @Query("SELECT * FROM games ORDER BY lastPlayedTimestamp DESC")
    List<Game> getAll();
    
    /**
     * Get game by ID
     * 
     * @param id The ID
     * @return The game
     */
    @Query("SELECT * FROM games WHERE id = :id")
    Game getById(String id);
    
    /**
     * Get game by package name
     * 
     * @param packageName The package name
     * @return The game
     */
    @Query("SELECT * FROM games WHERE packageName = :packageName")
    Game getByPackageName(String packageName);
    
    /**
     * Insert a game
     * 
     * @param game The game
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Game game);
    
    /**
     * Insert multiple games
     * 
     * @param games The games
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Game> games);
    
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
     * Delete all games
     */
    @Query("DELETE FROM games")
    void deleteAll();
}
