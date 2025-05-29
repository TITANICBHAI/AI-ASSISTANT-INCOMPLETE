package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.GameProfile;

import java.util.List;

/**
 * Data access object for GameProfile entity
 */
@Dao
public interface GameProfileDao {
    
    /**
     * Insert a game profile
     * 
     * @param gameProfile The game profile
     */
    @Insert
    void insert(GameProfile gameProfile);
    
    /**
     * Update a game profile
     * 
     * @param gameProfile The game profile
     */
    @Update
    void update(GameProfile gameProfile);
    
    /**
     * Delete a game profile
     * 
     * @param gameProfile The game profile
     */
    @Delete
    void delete(GameProfile gameProfile);
    
    /**
     * Get a game profile by package name
     * 
     * @param packageName The package name
     * @return The game profile or null
     */
    @Query("SELECT * FROM game_profiles WHERE packageName = :packageName")
    GameProfile getByPackageName(String packageName);
    
    /**
     * Get all game profiles
     * 
     * @return The game profiles
     */
    @Query("SELECT * FROM game_profiles")
    List<GameProfile> getAll();
    
    /**
     * Get game profiles by type
     * 
     * @param gameType The game type
     * @return The game profiles
     */
    @Query("SELECT * FROM game_profiles WHERE gameType = :gameType")
    List<GameProfile> getByType(String gameType);
    
    /**
     * Get recently played game profiles
     * 
     * @param limit The maximum number of profiles
     * @return The game profiles
     */
    @Query("SELECT * FROM game_profiles ORDER BY lastPlayedAt DESC LIMIT :limit")
    List<GameProfile> getRecentlyPlayed(int limit);
    
    /**
     * Get most played game profiles
     * 
     * @param limit The maximum number of profiles
     * @return The game profiles
     */
    @Query("SELECT * FROM game_profiles ORDER BY playCount DESC LIMIT :limit")
    List<GameProfile> getMostPlayed(int limit);
}
