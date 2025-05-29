package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.AIAction;

import java.util.List;

/**
 * Data Access Object for AIAction entities
 */
@Dao
public interface AIActionDao {
    /**
     * Insert action
     * @param action Action to insert
     * @return Inserted ID
     */
    @Insert
    long insert(AIAction action);

    /**
     * Update action
     * @param action Action to update
     */
    @Update
    void update(AIAction action);

    /**
     * Delete action
     * @param action Action to delete
     */
    @Delete
    void delete(AIAction action);

    /**
     * Get all actions
     * @return All actions
     */
    @Query("SELECT * FROM ai_actions ORDER BY timestamp DESC")
    List<AIAction> getAllActions();

    /**
     * Get all actions as LiveData
     * @return All actions as LiveData
     */
    @Query("SELECT * FROM ai_actions ORDER BY timestamp DESC")
    LiveData<List<AIAction>> getAllActionsLive();

    /**
     * Get actions by type
     * @param actionType Action type
     * @return Actions with specified type
     */
    @Query("SELECT * FROM ai_actions WHERE actionType = :actionType ORDER BY timestamp DESC")
    List<AIAction> getActionsByType(String actionType);

    /**
     * Get recent actions
     * @param limit Maximum number of actions to return
     * @return Recent actions
     */
    @Query("SELECT * FROM ai_actions ORDER BY timestamp DESC LIMIT :limit")
    List<AIAction> getRecentActions(int limit);

    /**
     * Delete old actions
     * @param timestamp Timestamp threshold
     * @return Number of deleted rows
     */
    @Query("DELETE FROM ai_actions WHERE timestamp < :timestamp")
    int deleteOldActions(long timestamp);
}
