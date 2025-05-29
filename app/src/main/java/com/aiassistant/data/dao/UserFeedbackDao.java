package com.aiassistant.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.lifecycle.LiveData;

import com.aiassistant.data.models.UserFeedback;

import java.util.List;

@Dao
public interface UserFeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserFeedback item);
    
    @Update
    void update(UserFeedback item);
}
