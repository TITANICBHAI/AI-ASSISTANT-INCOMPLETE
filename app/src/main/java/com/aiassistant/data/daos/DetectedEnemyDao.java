package com.aiassistant.data.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.DetectedEnemy;

import java.util.List;

/**
 * DAO for DetectedEnemy
 */
@Dao
public interface DetectedEnemyDao {
    
    /**
     * Get all detected enemies
     * 
     * @return The detected enemies
     */
    @Query("SELECT * FROM detected_enemies")
    List<DetectedEnemy> getAll();
    
    /**
     * Get detected enemy by ID
     * 
     * @param id The ID
     * @return The detected enemy
     */
    @Query("SELECT * FROM detected_enemies WHERE id = :id")
    DetectedEnemy getById(long id);
    
    /**
     * Get detected enemies by game state ID
     * 
     * @param gameStateId The game state ID
     * @return The detected enemies
     */
    @Query("SELECT * FROM detected_enemies WHERE gameStateId = :gameStateId")
    List<DetectedEnemy> getByGameStateId(long gameStateId);
    
    /**
     * Insert a detected enemy
     * 
     * @param detectedEnemy The detected enemy
     * @return The inserted ID
     */
    @Insert
    long insert(DetectedEnemy detectedEnemy);
    
    /**
     * Insert multiple detected enemies
     * 
     * @param detectedEnemies The detected enemies
     * @return The inserted IDs
     */
    @Insert
    List<Long> insertAll(List<DetectedEnemy> detectedEnemies);
    
    /**
     * Update a detected enemy
     * 
     * @param detectedEnemy The detected enemy
     */
    @Update
    void update(DetectedEnemy detectedEnemy);
    
    /**
     * Delete a detected enemy
     * 
     * @param detectedEnemy The detected enemy
     */
    @Delete
    void delete(DetectedEnemy detectedEnemy);
    
    /**
     * Delete detected enemies by game state ID
     * 
     * @param gameStateId The game state ID
     */
    @Query("DELETE FROM detected_enemies WHERE gameStateId = :gameStateId")
    void deleteByGameStateId(long gameStateId);
}
