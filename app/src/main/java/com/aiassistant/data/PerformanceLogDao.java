package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.PerformanceLog;

import java.util.List;

@Dao
public interface PerformanceLogDao {
    
    @Insert
    long insert(PerformanceLog log);
    
    @Update
    void update(PerformanceLog log);
    
    @Delete
    void delete(PerformanceLog log);
    
    @Query("SELECT * FROM performance_logs ORDER BY timestamp DESC")
    List<PerformanceLog> getAll();
    
    @Query("SELECT * FROM performance_logs ORDER BY timestamp DESC")
    LiveData<List<PerformanceLog>> getAllLive();
    
    @Query("SELECT * FROM performance_logs WHERE id = :id LIMIT 1")
    PerformanceLog getById(String id);
    
    @Query("SELECT * FROM performance_logs WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<PerformanceLog> getByGameId(String gameId);
    
    @Query("SELECT * FROM performance_logs WHERE metricName = :metricName ORDER BY timestamp DESC")
    List<PerformanceLog> getByMetricName(String metricName);
    
    @Query("SELECT * FROM performance_logs WHERE gameId = :gameId AND metricName = :metricName ORDER BY timestamp DESC")
    List<PerformanceLog> getByGameIdAndMetricName(String gameId, String metricName);
    
    @Query("SELECT AVG(value) FROM performance_logs WHERE metricName = :metricName")
    Float getAverageValueForMetric(String metricName);
    
    @Query("SELECT * FROM performance_logs ORDER BY timestamp DESC LIMIT :limit")
    List<PerformanceLog> getRecentLogs(int limit);
    
    @Query("DELETE FROM performance_logs")
    void deleteAll();
    
    @Query("DELETE FROM performance_logs WHERE id = :id")
    void deleteById(String id);
    
    @Query("DELETE FROM performance_logs WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
}
