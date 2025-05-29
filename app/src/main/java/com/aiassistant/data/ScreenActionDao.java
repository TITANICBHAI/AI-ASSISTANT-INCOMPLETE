package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ScreenActionEntity;

import java.util.List;

/**
 * Data Access Object for ScreenActionEntity entities
 */
@Dao
public interface ScreenActionDao {
    /**
     * Insert screen action
     * @param screenAction Screen action to insert
     * @return Inserted ID
     */
    @Insert
    long insert(ScreenActionEntity screenAction);

    /**
     * Update screen action
     * @param screenAction Screen action to update
     */
    @Update
    void update(ScreenActionEntity screenAction);

    /**
     * Delete screen action
     * @param screenAction Screen action to delete
     */
    @Delete
    void delete(ScreenActionEntity screenAction);

    /**
     * Get all screen actions
     * @return All screen actions
     */
    @Query("SELECT * FROM screen_actions ORDER BY timestamp DESC")
    List<ScreenActionEntity> getAllScreenActions();

    /**
     * Get all screen actions as LiveData
     * @return All screen actions as LiveData
     */
    @Query("SELECT * FROM screen_actions ORDER BY timestamp DESC")
    LiveData<List<ScreenActionEntity>> getAllScreenActionsLive();

    /**
     * Get screen actions for screen
     * @param screenId Screen ID
     * @return Screen actions for specified screen
     */
    @Query("SELECT * FROM screen_actions WHERE screenId = :screenId ORDER BY timestamp DESC")
    List<ScreenActionEntity> getActionsForScreen(String screenId);

    /**
     * Get screen actions by type
     * @param actionType Action type
     * @return Screen actions with specified type
     */
    @Query("SELECT * FROM screen_actions WHERE actionType = :actionType ORDER BY timestamp DESC")
    List<ScreenActionEntity> getActionsByType(String actionType);

    /**
     * Get recent screen actions
     * @param limit Maximum number of actions to return
     * @return Recent screen actions
     */
    @Query("SELECT * FROM screen_actions ORDER BY timestamp DESC LIMIT :limit")
    List<ScreenActionEntity> getRecentActions(int limit);

    /**
     * Delete old screen actions
     * @param timestamp Timestamp threshold
     * @return Number of deleted rows
     */
    @Query("DELETE FROM screen_actions WHERE timestamp < :timestamp")
    int deleteOldActions(long timestamp);
}
