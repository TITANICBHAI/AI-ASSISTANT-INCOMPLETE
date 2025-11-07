package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.GestureSampleEntity;

import java.util.List;

@Dao
public interface GestureSampleDao {
    
    @Insert
    long insert(GestureSampleEntity gestureSample);
    
    @Insert
    List<Long> insertAll(List<GestureSampleEntity> gestureSamples);
    
    @Update
    void update(GestureSampleEntity gestureSample);
    
    @Delete
    void delete(GestureSampleEntity gestureSample);
    
    @Query("SELECT * FROM gesture_samples ORDER BY timestamp DESC")
    List<GestureSampleEntity> getAll();
    
    @Query("SELECT * FROM gesture_samples ORDER BY timestamp DESC")
    LiveData<List<GestureSampleEntity>> getAllLive();
    
    @Query("SELECT * FROM gesture_samples WHERE gestureId = :id")
    GestureSampleEntity getById(String id);
    
    @Query("SELECT * FROM gesture_samples WHERE gestureType = :gestureType ORDER BY timestamp DESC")
    List<GestureSampleEntity> getByGestureType(String gestureType);
    
    @Query("SELECT * FROM gesture_samples WHERE gestureType = :gestureType ORDER BY timestamp DESC")
    LiveData<List<GestureSampleEntity>> getByGestureTypeLive(String gestureType);
    
    @Query("SELECT * FROM gesture_samples WHERE label = :label ORDER BY timestamp DESC")
    List<GestureSampleEntity> getByLabel(String label);
    
    @Query("SELECT * FROM gesture_samples WHERE label = :label ORDER BY timestamp DESC")
    LiveData<List<GestureSampleEntity>> getByLabelLive(String label);
    
    @Query("SELECT * FROM gesture_samples WHERE confidence >= :minConfidence ORDER BY confidence DESC")
    List<GestureSampleEntity> getByMinConfidence(float minConfidence);
    
    @Query("SELECT * FROM gesture_samples WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    List<GestureSampleEntity> getByTimeRange(long startTime, long endTime);
    
    @Query("SELECT COUNT(*) FROM gesture_samples WHERE label = :label")
    int countByLabel(String label);
    
    @Query("SELECT COUNT(*) FROM gesture_samples WHERE gestureType = :gestureType")
    int countByGestureType(String gestureType);
    
    @Query("SELECT COUNT(*) FROM gesture_samples")
    int count();
    
    @Query("DELETE FROM gesture_samples WHERE label = :label")
    int deleteByLabel(String label);
    
    @Query("DELETE FROM gesture_samples WHERE gestureType = :gestureType")
    int deleteByGestureType(String gestureType);
    
    @Query("DELETE FROM gesture_samples WHERE timestamp < :timestamp")
    int deleteOldSamples(long timestamp);
    
    @Query("DELETE FROM gesture_samples")
    void deleteAll();
}
