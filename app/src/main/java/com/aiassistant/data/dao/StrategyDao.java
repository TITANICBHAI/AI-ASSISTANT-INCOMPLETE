package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.aiassistant.data.models.Strategy;

import java.util.List;

@Dao
public interface StrategyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Strategy item);
    
    @Update
    void update(Strategy item);
}
