package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.ModelInfoEntity;

import java.util.List;

@Dao
public interface ModelInfoDao {
    
    @Insert
    long insert(ModelInfoEntity modelInfo);
    
    @Insert
    List<Long> insertAll(List<ModelInfoEntity> modelInfos);
    
    @Update
    void update(ModelInfoEntity modelInfo);
    
    @Delete
    void delete(ModelInfoEntity modelInfo);
    
    @Query("SELECT * FROM model_info ORDER BY createdAt DESC")
    List<ModelInfoEntity> getAll();
    
    @Query("SELECT * FROM model_info ORDER BY createdAt DESC")
    LiveData<List<ModelInfoEntity>> getAllLive();
    
    @Query("SELECT * FROM model_info WHERE modelId = :id")
    ModelInfoEntity getById(String id);
    
    @Query("SELECT * FROM model_info WHERE labelId = :labelId ORDER BY createdAt DESC")
    List<ModelInfoEntity> getByLabelId(String labelId);
    
    @Query("SELECT * FROM model_info WHERE labelId = :labelId ORDER BY createdAt DESC")
    LiveData<List<ModelInfoEntity>> getByLabelIdLive(String labelId);
    
    @Query("SELECT * FROM model_info WHERE status = :status ORDER BY createdAt DESC")
    List<ModelInfoEntity> getByStatus(String status);
    
    @Query("SELECT * FROM model_info WHERE status = :status ORDER BY createdAt DESC")
    LiveData<List<ModelInfoEntity>> getByStatusLive(String status);
    
    @Query("SELECT * FROM model_info WHERE accuracy >= :minAccuracy ORDER BY accuracy DESC")
    List<ModelInfoEntity> getByMinAccuracy(float minAccuracy);
    
    @Query("SELECT * FROM model_info WHERE labelId = :labelId ORDER BY accuracy DESC LIMIT 1")
    ModelInfoEntity getBestModelForLabel(String labelId);
    
    @Query("SELECT * FROM model_info WHERE labelId = :labelId ORDER BY createdAt DESC LIMIT 1")
    ModelInfoEntity getLatestModelForLabel(String labelId);
    
    @Query("SELECT COUNT(*) FROM model_info WHERE labelId = :labelId")
    int countByLabelId(String labelId);
    
    @Query("SELECT COUNT(*) FROM model_info WHERE status = :status")
    int countByStatus(String status);
    
    @Query("SELECT COUNT(*) FROM model_info")
    int count();
    
    @Query("UPDATE model_info SET status = :status WHERE modelId = :modelId")
    void updateStatus(String modelId, String status);
    
    @Query("UPDATE model_info SET accuracy = :accuracy WHERE modelId = :modelId")
    void updateAccuracy(String modelId, float accuracy);
    
    @Query("DELETE FROM model_info WHERE labelId = :labelId")
    int deleteByLabelId(String labelId);
    
    @Query("DELETE FROM model_info WHERE status = :status")
    int deleteByStatus(String status);
    
    @Query("DELETE FROM model_info")
    void deleteAll();
}
