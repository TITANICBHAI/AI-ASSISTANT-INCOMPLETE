package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ImageSampleEntity;

import java.util.List;

@Dao
public interface ImageSampleDao {
    
    @Insert
    long insert(ImageSampleEntity imageSample);
    
    @Insert
    List<Long> insertAll(List<ImageSampleEntity> imageSamples);
    
    @Update
    void update(ImageSampleEntity imageSample);
    
    @Delete
    void delete(ImageSampleEntity imageSample);
    
    @Query("SELECT * FROM image_samples ORDER BY timestamp DESC")
    List<ImageSampleEntity> getAll();
    
    @Query("SELECT * FROM image_samples ORDER BY timestamp DESC")
    LiveData<List<ImageSampleEntity>> getAllLive();
    
    @Query("SELECT * FROM image_samples WHERE imageId = :id")
    ImageSampleEntity getById(String id);
    
    @Query("SELECT * FROM image_samples WHERE labelId = :labelId ORDER BY timestamp DESC")
    List<ImageSampleEntity> getByLabelId(String labelId);
    
    @Query("SELECT * FROM image_samples WHERE labelId = :labelId ORDER BY timestamp DESC")
    LiveData<List<ImageSampleEntity>> getByLabelIdLive(String labelId);
    
    @Query("SELECT * FROM image_samples WHERE confidence >= :minConfidence ORDER BY confidence DESC")
    List<ImageSampleEntity> getByMinConfidence(float minConfidence);
    
    @Query("SELECT * FROM image_samples WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    List<ImageSampleEntity> getByTimeRange(long startTime, long endTime);
    
    @Query("SELECT COUNT(*) FROM image_samples WHERE labelId = :labelId")
    int countByLabelId(String labelId);
    
    @Query("SELECT COUNT(*) FROM image_samples")
    int count();
    
    @Query("DELETE FROM image_samples WHERE labelId = :labelId")
    int deleteByLabelId(String labelId);
    
    @Query("DELETE FROM image_samples WHERE timestamp < :timestamp")
    int deleteOldSamples(long timestamp);
    
    @Query("DELETE FROM image_samples")
    void deleteAll();
}
