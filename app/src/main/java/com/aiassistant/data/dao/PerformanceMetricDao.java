package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.aiassistant.data.models.PerformanceMetric;

import java.util.List;

@Dao
public interface PerformanceMetricDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PerformanceMetric item);
    
    @Update
    void update(PerformanceMetric item);
}
