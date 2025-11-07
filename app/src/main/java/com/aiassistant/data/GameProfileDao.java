package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.GameProfile;

import java.util.List;

@Dao
public interface GameProfileDao {
    
    @Insert
    long insert(GameProfile profile);
    
    @Update
    void update(GameProfile profile);
    
    @Delete
    void delete(GameProfile profile);
    
    @Query("SELECT * FROM game_profiles ORDER BY lastUpdatedDate DESC")
    List<GameProfile> getAll();
    
    @Query("SELECT * FROM game_profiles ORDER BY lastUpdatedDate DESC")
    LiveData<List<GameProfile>> getAllLive();
    
    @Query("SELECT * FROM game_profiles WHERE id = :id LIMIT 1")
    GameProfile getById(long id);
    
    @Query("SELECT * FROM game_profiles WHERE gameId = :gameId ORDER BY lastUpdatedDate DESC")
    List<GameProfile> getByGameId(String gameId);
    
    @Query("SELECT * FROM game_profiles WHERE gameId = :gameId AND active = 1 LIMIT 1")
    GameProfile getActiveProfileForGame(String gameId);
    
    @Query("SELECT * FROM game_profiles WHERE active = 1 ORDER BY lastUpdatedDate DESC")
    List<GameProfile> getActiveProfiles();
    
    @Query("SELECT * FROM game_profiles WHERE profileType = :profileType ORDER BY lastUpdatedDate DESC")
    List<GameProfile> getByProfileType(String profileType);
    
    @Query("SELECT * FROM game_profiles ORDER BY userRating DESC LIMIT :limit")
    List<GameProfile> getTopRatedProfiles(int limit);
    
    @Query("UPDATE game_profiles SET active = 0 WHERE gameId = :gameId")
    void deactivateAllProfilesForGame(String gameId);
    
    @Query("DELETE FROM game_profiles")
    void deleteAll();
    
    @Query("DELETE FROM game_profiles WHERE id = :id")
    void deleteById(long id);
    
    @Query("DELETE FROM game_profiles WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
}
