package com.aiassistant.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ScheduledTask;

import java.util.List;

/**
 * DAO for scheduled task entities
 */
@Dao
public interface ScheduledTaskDao {
    
    /**
     * Insert a task
     * 
     * @param task The task
     * @return The new row ID
     */
    @Insert
    long insert(ScheduledTask task);
    
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
     * @param id The ID
     * @return The task
     */
    @Query("SELECT * FROM scheduled_tasks WHERE id = :id")
    ScheduledTask getById(long id);
    
    /**
     * Get all tasks
     * 
     * @return The tasks
     */
    @Query("SELECT * FROM scheduled_tasks ORDER BY scheduledTime ASC")
    List<ScheduledTask> getAllTasks();
    
    /**
     * Get active tasks
     * 
     * @return The tasks
     */
    @Query("SELECT * FROM scheduled_tasks WHERE isActive = 1 AND isCompleted = 0 ORDER BY scheduledTime ASC")
    List<ScheduledTask> getActiveTasks();
    
    /**
     * Get tasks due before a time
     * 
     * @param timestamp The cutoff timestamp
     * @return The tasks
     */
    @Query("SELECT * FROM scheduled_tasks WHERE isActive = 1 AND isCompleted = 0 AND scheduledTime <= :timestamp ORDER BY scheduledTime ASC")
    List<ScheduledTask> getTasksDueBefore(long timestamp);
    
    /**
     * Get tasks of a type
     * 
     * @param taskType The task type
     * @return The tasks
     */
    @Query("SELECT * FROM scheduled_tasks WHERE taskType = :taskType ORDER BY scheduledTime ASC")
    List<ScheduledTask> getTasksByType(int taskType);
    
    /**
     * Count active tasks
     * 
     * @return The count
     */
    @Query("SELECT COUNT(*) FROM scheduled_tasks WHERE isActive = 1 AND isCompleted = 0")
    int countActiveTasks();
    
    /**
     * Delete old tasks
     * 
     * @param timestamp The cutoff timestamp
     * @return The number of rows deleted
     */
    @Query("DELETE FROM scheduled_tasks WHERE isCompleted = 1 AND scheduledTime < :timestamp")
    int deleteOldTasks(long timestamp);
}
