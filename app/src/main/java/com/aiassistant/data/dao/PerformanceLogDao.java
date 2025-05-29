package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.PerformanceLog;

import java.util.List;

/**
 * Data access object for PerformanceLog entity
 */
@Dao
public interface PerformanceLogDao {
    
    /**
     * Insert a performance log
     * 
     * @param log The performance log
     */
    @Insert
    void insert(PerformanceLog log);
    
    /**
     * Update a performance log
     * 
     * @param log The performance log
     */
    @Update
    void update(PerformanceLog log);
    
    /**
     * Delete a performance log
     * 
     * @param log The performance log
     */
    @Delete
    void delete(PerformanceLog log);
    
    /**
     * Get a performance log by ID
     * 
     * @param id The performance log ID
     * @return The performance log
     */
    @Query("SELECT * FROM performance_logs WHERE id = :id")
    PerformanceLog getById(String id);
    
    /**
     * Get performance logs by game ID
     * 
     * @param gameId The game ID
     * @return The performance logs
     */
    @Query("SELECT * FROM performance_logs WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<PerformanceLog> getByGameId(String gameId);
    
    /**
     * Get performance logs by metric name
     * 
     * @param metricName The metric name
     * @return The performance logs
     */
    @Query("SELECT * FROM performance_logs WHERE metricName = :metricName ORDER BY timestamp DESC")
    List<PerformanceLog> getByMetricName(String metricName);
    
    /**
     * Get recent performance logs
     * 
     * @param limit The maximum number of logs
     * @return The performance logs
     */
    @Query("SELECT * FROM performance_logs ORDER BY timestamp DESC LIMIT :limit")
    List<PerformanceLog> getRecent(int limit);
    
    /**
     * Get average value for a metric
     * 
     * @param metricName The metric name
     * @return The average value
     */
    @Query("SELECT AVG(value) FROM performance_logs WHERE metricName = :metricName")
    float getAverageForMetric(String metricName);
    
    /**
     * Get average value for a metric by game
     * 
     * @param metricName The metric name
     * @param gameId The game ID
     * @return The average value
     */
    @Query("SELECT AVG(value) FROM performance_logs WHERE metricName = :metricName AND gameId = :gameId")
    float getAverageForMetricByGame(String metricName, String gameId);
    
    /**
     * Delete old performance logs
     * 
     * @param timestamp Older than this timestamp
     * @return The number of deleted logs
     */
    @Query("DELETE FROM performance_logs WHERE timestamp < :timestamp")
    int deleteOldLogs(long timestamp);
}
