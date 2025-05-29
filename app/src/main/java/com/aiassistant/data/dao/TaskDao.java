package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ScheduledTask;

import java.util.List;

/**
 * Data access object for ScheduledTask entity
 */
@Dao
public interface TaskDao {
    
    /**
     * Insert a task
     * 
     * @param task The task
     */
    @Insert
    void insert(ScheduledTask task);
    
    /**
     * Update a task
     * 
     * @param task The task
     */
    @Update
    void update(ScheduledTask task);
    
    /**
     * Delete a task
     * 
     * @param task The task
     */
    @Delete
    void delete(ScheduledTask task);
    
    /**
     * Get a task by ID
     * 
     * @param id The task ID
     * @return The task
     */
    @Query("SELECT * FROM scheduled_tasks WHERE id = :id")
    ScheduledTask getById(String id);
    
    /**
     * Get all tasks
     * 
     * @return The tasks
     */
    @Query("SELECT * FROM scheduled_tasks")
    List<ScheduledTask> getAll();
    
    /**
     * Get pending tasks
     * 
     * @return The pending tasks
     */
    @Query("SELECT * FROM scheduled_tasks WHERE status = 0 ORDER BY scheduledTime ASC")
    List<ScheduledTask> getPendingTasks();
    
    /**
     * Get tasks by type
     * 
     * @param taskType The task type
     * @return The tasks
     */
    @Query("SELECT * FROM scheduled_tasks WHERE taskType = :taskType")
    List<ScheduledTask> getByType(String taskType);
    
    /**
     * Delete completed tasks
     * 
     * @return The number of deleted tasks
     */
    @Query("DELETE FROM scheduled_tasks WHERE status = 2 OR status = 3")
    int deleteCompletedTasks();
}
