package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.PerformanceMetric;

import java.util.List;

@Dao
public interface PerformanceMetricDao {
    
    @Insert
    long insert(PerformanceMetric metric);
    
    @Update
    void update(PerformanceMetric metric);
    
    @Delete
    void delete(PerformanceMetric metric);
    
    @Query("SELECT * FROM performance_metrics ORDER BY timestamp DESC")
    List<PerformanceMetric> getAll();
    
    @Query("SELECT * FROM performance_metrics ORDER BY timestamp DESC")
    LiveData<List<PerformanceMetric>> getAllLive();
    
    @Query("SELECT * FROM performance_metrics WHERE id = :id LIMIT 1")
    PerformanceMetric getById(long id);
    
    @Query("SELECT * FROM performance_metrics WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<PerformanceMetric> getByGameId(String gameId);
    
    @Query("SELECT * FROM performance_metrics WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    List<PerformanceMetric> getBySessionId(String sessionId);
    
    @Query("SELECT * FROM performance_metrics WHERE metricType = :metricType ORDER BY timestamp DESC")
    List<PerformanceMetric> getByMetricType(String metricType);
    
    @Query("SELECT * FROM performance_metrics WHERE gameId = :gameId AND metricType = :metricType ORDER BY timestamp DESC")
    List<PerformanceMetric> getByGameIdAndMetricType(String gameId, String metricType);
    
    @Query("SELECT AVG(value) FROM performance_metrics WHERE metricType = :metricType")
    Double getAverageValueForMetricType(String metricType);
    
    @Query("DELETE FROM performance_metrics")
    void deleteAll();
    
    @Query("DELETE FROM performance_metrics WHERE id = :id")
    void deleteById(long id);
    
    @Query("DELETE FROM performance_metrics WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
    
    @Query("DELETE FROM performance_metrics WHERE sessionId = :sessionId")
    void deleteBySessionId(String sessionId);
}
