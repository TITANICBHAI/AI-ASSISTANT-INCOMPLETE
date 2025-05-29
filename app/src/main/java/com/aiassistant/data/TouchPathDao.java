package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.TouchPath;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for TouchPath entities
 */
@Dao
public interface TouchPathDao {
    /**
     * Insert touch path
     * @param touchPath Touch path to insert
     * @return Inserted ID
     */
    @Insert
    long insert(TouchPath touchPath);

    /**
     * Update touch path
     * @param touchPath Touch path to update
     */
    @Update
    void update(TouchPath touchPath);

    /**
     * Delete touch path
     * @param touchPath Touch path to delete
     */
    @Delete
    void delete(TouchPath touchPath);

    /**
     * Get all touch paths
     * @return All touch paths
     */
    @Query("SELECT * FROM touch_paths ORDER BY timestamp DESC")
    List<TouchPath> getAllTouchPaths();

    /**
     * Get all touch paths as LiveData
     * @return All touch paths as LiveData
     */
    @Query("SELECT * FROM touch_paths ORDER BY timestamp DESC")
    LiveData<List<TouchPath>> getAllTouchPathsLive();

    /**
     * Get touch paths for game
     * @param gameId Game ID
     * @return Touch paths for specified game
     */
    @Query("SELECT * FROM touch_paths WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<TouchPath> getTouchPathsForGame(String gameId);

    /**
     * Get touch paths for screen
     * @param screenId Screen ID
     * @return Touch paths for specified screen
     */
    @Query("SELECT * FROM touch_paths WHERE screenId = :screenId ORDER BY timestamp DESC")
    List<TouchPath> getTouchPathsForScreen(String screenId);

    /**
     * Get touch paths by type
     * @param pathType Path type
     * @return Touch paths with specified type
     */
    @Query("SELECT * FROM touch_paths WHERE pathType = :pathType ORDER BY timestamp DESC")
    List<TouchPath> getTouchPathsByType(String pathType);

    /**
     * Get touch paths in time range
     * @param startTime Start time
     * @param endTime End time
     * @return Touch paths in specified time range
     */
    @Query("SELECT * FROM touch_paths WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    List<TouchPath> getTouchPathsInTimeRange(Date startTime, Date endTime);

    /**
     * Delete old touch paths
     * @param timestamp Timestamp threshold
     * @return Number of deleted rows
     */
    @Query("DELETE FROM touch_paths WHERE timestamp < :timestamp")
    int deleteOldTouchPaths(Date timestamp);
}
