package com.aiassistant.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.Task;

import java.util.Date;
import java.util.List;

/**
 * Data Access Object for Task entities
 */
@Dao
public interface TaskDao {
    /**
     * Insert a task
     * @param task Task to insert
     * @return Inserted task ID
     */
    @Insert
    long insertTask(Task task);

    /**
     * Update a task
     * @param task Task to update
     */
    @Update
    void updateTask(Task task);

    /**
     * Delete a task
     * @param task Task to delete
     */
    @Delete
    void deleteTask(Task task);

    /**
     * Get task by ID
     * @param taskId Task ID
     * @return Task or null
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    Task getTaskById(int taskId);

    /**
     * Get all tasks
     * @return List of all tasks
     */
    @Query("SELECT * FROM tasks ORDER BY createdTime DESC")
    List<Task> getAllTasks();

    /**
     * Get pending tasks
     * @return List of pending tasks
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isActive = 1 ORDER BY scheduledTime ASC")
    List<Task> getPendingTasks();

    /**
     * Get completed tasks
     * @return List of completed tasks
     */
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedTime DESC")
    List<Task> getCompletedTasks();

    /**
     * Get tasks due by date
     * @param date Due date
     * @return List of tasks
     */
    @Query("SELECT * FROM tasks WHERE scheduledTime <= :date AND isCompleted = 0 ORDER BY scheduledTime ASC")
    List<Task> getTasksDueByDate(Date date);

    /**
     * Get tasks for a specific game
     * @param gameId Game ID
     * @return List of tasks
     */
    @Query("SELECT * FROM tasks WHERE gameId = :gameId ORDER BY scheduledTime DESC")
    List<Task> getTasksForGame(String gameId);

    /**
     * Get tasks by priority
     * @param priority Priority level
     * @return List of tasks
     */
    @Query("SELECT * FROM tasks WHERE taskType = :priority AND isCompleted = 0 ORDER BY scheduledTime ASC")
    List<Task> getTasksByPriority(int priority);

    /**
     * Get tasks created by AI
     * @return List of AI-created tasks
     */
    @Query("SELECT * FROM tasks WHERE taskData LIKE '%ai_created%' ORDER BY createdTime DESC")
    List<Task> getTasksCreatedByAI();

    /**
     * Delete all tasks
     */
    @Query("DELETE FROM tasks")
    void deleteAllTasks();
}
