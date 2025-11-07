package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.Game;

import java.util.List;

@Dao
public interface GameDao {
    
    @Insert
    long insert(Game game);
    
    @Update
    void update(Game game);
    
    @Delete
    void delete(Game game);
    
    @Query("SELECT * FROM games ORDER BY lastPlayedDate DESC")
    List<Game> getAll();
    
    @Query("SELECT * FROM games ORDER BY lastPlayedDate DESC")
    LiveData<List<Game>> getAllLive();
    
    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    Game getById(String id);
    
    @Query("SELECT * FROM games WHERE packageName = :packageName LIMIT 1")
    Game getByPackageName(String packageName);
    
    @Query("SELECT * FROM games WHERE gameType = :gameType ORDER BY lastPlayedDate DESC")
    List<Game> getByGameType(String gameType);
    
    @Query("SELECT * FROM games WHERE isProfileCreated = 1 ORDER BY lastPlayedDate DESC")
    List<Game> getGamesWithProfiles();
    
    @Query("SELECT * FROM games ORDER BY playCount DESC LIMIT :limit")
    List<Game> getMostPlayedGames(int limit);
    
    @Query("SELECT * FROM games ORDER BY userRating DESC LIMIT :limit")
    List<Game> getTopRatedGames(int limit);
    
    @Query("SELECT * FROM games WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    List<Game> searchByName(String searchQuery);
    
    @Query("DELETE FROM games")
    void deleteAll();
    
    @Query("DELETE FROM games WHERE id = :id")
    void deleteById(String id);
}
