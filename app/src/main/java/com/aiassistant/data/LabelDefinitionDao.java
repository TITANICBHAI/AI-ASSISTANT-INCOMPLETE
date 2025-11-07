package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.LabelDefinitionEntity;

import java.util.List;

@Dao
public interface LabelDefinitionDao {
    
    @Insert
    long insert(LabelDefinitionEntity labelDefinition);
    
    @Insert
    List<Long> insertAll(List<LabelDefinitionEntity> labelDefinitions);
    
    @Update
    void update(LabelDefinitionEntity labelDefinition);
    
    @Delete
    void delete(LabelDefinitionEntity labelDefinition);
    
    @Query("SELECT * FROM label_definitions ORDER BY name ASC")
    List<LabelDefinitionEntity> getAll();
    
    @Query("SELECT * FROM label_definitions ORDER BY name ASC")
    LiveData<List<LabelDefinitionEntity>> getAllLive();
    
    @Query("SELECT * FROM label_definitions WHERE labelId = :id")
    LabelDefinitionEntity getById(String id);
    
    @Query("SELECT * FROM label_definitions WHERE name = :name LIMIT 1")
    LabelDefinitionEntity getByName(String name);
    
    @Query("SELECT * FROM label_definitions WHERE category = :category ORDER BY name ASC")
    List<LabelDefinitionEntity> getByCategory(String category);
    
    @Query("SELECT * FROM label_definitions WHERE category = :category ORDER BY name ASC")
    LiveData<List<LabelDefinitionEntity>> getByCategoryLive(String category);
    
    @Query("SELECT * FROM label_definitions ORDER BY usageCount DESC LIMIT :limit")
    List<LabelDefinitionEntity> getMostUsed(int limit);
    
    @Query("SELECT DISTINCT category FROM label_definitions ORDER BY category ASC")
    List<String> getAllCategories();
    
    @Query("SELECT COUNT(*) FROM label_definitions")
    int count();
    
    @Query("SELECT COUNT(*) FROM label_definitions WHERE category = :category")
    int countByCategory(String category);
    
    @Query("UPDATE label_definitions SET usageCount = usageCount + 1 WHERE labelId = :labelId")
    void incrementUsageCount(String labelId);
    
    @Query("DELETE FROM label_definitions WHERE labelId = :labelId")
    int deleteById(String labelId);
    
    @Query("DELETE FROM label_definitions")
    void deleteAll();
}
