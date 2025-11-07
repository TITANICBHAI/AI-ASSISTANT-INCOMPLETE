package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.VoiceSampleEntity;

import java.util.List;

@Dao
public interface VoiceSampleDao {
    
    @Insert
    long insert(VoiceSampleEntity voiceSample);
    
    @Insert
    List<Long> insertAll(List<VoiceSampleEntity> voiceSamples);
    
    @Update
    void update(VoiceSampleEntity voiceSample);
    
    @Delete
    void delete(VoiceSampleEntity voiceSample);
    
    @Query("SELECT * FROM voice_samples ORDER BY timestamp DESC")
    List<VoiceSampleEntity> getAll();
    
    @Query("SELECT * FROM voice_samples ORDER BY timestamp DESC")
    LiveData<List<VoiceSampleEntity>> getAllLive();
    
    @Query("SELECT * FROM voice_samples WHERE voiceSampleId = :id")
    VoiceSampleEntity getById(String id);
    
    @Query("SELECT * FROM voice_samples WHERE label = :label ORDER BY timestamp DESC")
    List<VoiceSampleEntity> getByLabel(String label);
    
    @Query("SELECT * FROM voice_samples WHERE label = :label ORDER BY timestamp DESC")
    LiveData<List<VoiceSampleEntity>> getByLabelLive(String label);
    
    @Query("SELECT * FROM voice_samples WHERE confidence >= :minConfidence ORDER BY confidence DESC")
    List<VoiceSampleEntity> getByMinConfidence(float minConfidence);
    
    @Query("SELECT * FROM voice_samples WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    List<VoiceSampleEntity> getByTimeRange(long startTime, long endTime);
    
    @Query("SELECT COUNT(*) FROM voice_samples WHERE label = :label")
    int countByLabel(String label);
    
    @Query("SELECT COUNT(*) FROM voice_samples")
    int count();
    
    @Query("DELETE FROM voice_samples WHERE label = :label")
    int deleteByLabel(String label);
    
    @Query("DELETE FROM voice_samples WHERE timestamp < :timestamp")
    int deleteOldSamples(long timestamp);
    
    @Query("DELETE FROM voice_samples")
    void deleteAll();
}
