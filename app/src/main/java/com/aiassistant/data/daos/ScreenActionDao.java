package com.aiassistant.data.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ScreenActionEntity;

import java.util.List;

/**
 * DAO for ScreenActionEntity
 */
@Dao
public interface ScreenActionDao {
    
    /**
     * Get all screen actions
     * 
     * @return The screen actions
     */
    @Query("SELECT * FROM screen_actions ORDER BY timestamp DESC")
    List<ScreenActionEntity> getAll();
    
    /**
     * Get screen action by ID
     * 
     * @param id The ID
     * @return The screen action
     */
    @Query("SELECT * FROM screen_actions WHERE id = :id")
    ScreenActionEntity getById(long id);
    
    /**
     * Get screen actions by game state ID
     * 
     * @param gameStateId The game state ID
     * @return The screen actions
     */
    @Query("SELECT * FROM screen_actions WHERE gameStateId = :gameStateId ORDER BY timestamp ASC")
    List<ScreenActionEntity> getByGameStateId(long gameStateId);
    
    /**
     * Get screen actions by time range
     * 
     * @param startTime The start time
     * @param endTime The end time
     * @return The screen actions
     */
    @Query("SELECT * FROM screen_actions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    List<ScreenActionEntity> getByTimeRange(long startTime, long endTime);
    
    /**
     * Insert a screen action
     * 
     * @param screenAction The screen action
     * @return The inserted ID
     */
    @Insert
    long insert(ScreenActionEntity screenAction);
    
    /**
     * Insert multiple screen actions
     * 
     * @param screenActions The screen actions
     * @return The inserted IDs
     */
    @Insert
    List<Long> insertAll(List<ScreenActionEntity> screenActions);
    
    /**
     * Update a screen action
     * 
     * @param screenAction The screen action
     */
    @Update
    void update(ScreenActionEntity screenAction);
    
    /**
     * Delete a screen action
     * 
     * @param screenAction The screen action
     */
    @Delete
    void delete(ScreenActionEntity screenAction);
    
    /**
     * Delete screen actions by game state ID
     * 
     * @param gameStateId The game state ID
     */
    @Query("DELETE FROM screen_actions WHERE gameStateId = :gameStateId")
    void deleteByGameStateId(long gameStateId);
    
    /**
     * Delete old screen actions
     * 
     * @param timestamp The cutoff timestamp
     */
    @Query("DELETE FROM screen_actions WHERE timestamp < :timestamp")
    void deleteOld(long timestamp);
}
