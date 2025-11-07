package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.FeedbackRecord;

import java.util.List;

@Dao
public interface FeedbackRecordDao {
    
    @Insert
    long insert(FeedbackRecord record);
    
    @Update
    void update(FeedbackRecord record);
    
    @Delete
    void delete(FeedbackRecord record);
    
    @Query("SELECT * FROM feedback_records ORDER BY timestamp DESC")
    List<FeedbackRecord> getAll();
    
    @Query("SELECT * FROM feedback_records ORDER BY timestamp DESC")
    LiveData<List<FeedbackRecord>> getAllLive();
    
    @Query("SELECT * FROM feedback_records WHERE id = :id LIMIT 1")
    FeedbackRecord getById(long id);
    
    @Query("SELECT * FROM feedback_records WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<FeedbackRecord> getByGameId(String gameId);
    
    @Query("SELECT * FROM feedback_records WHERE actionId = :actionId ORDER BY timestamp DESC")
    List<FeedbackRecord> getByActionId(long actionId);
    
    @Query("SELECT * FROM feedback_records WHERE algorithmId = :algorithmId ORDER BY timestamp DESC")
    List<FeedbackRecord> getByAlgorithmId(int algorithmId);
    
    @Query("SELECT * FROM feedback_records WHERE isExplicitFeedback = 1 ORDER BY timestamp DESC")
    List<FeedbackRecord> getExplicitFeedback();
    
    @Query("SELECT AVG(rating) FROM feedback_records WHERE gameId = :gameId")
    Float getAverageRatingForGame(String gameId);
    
    @Query("DELETE FROM feedback_records")
    void deleteAll();
    
    @Query("DELETE FROM feedback_records WHERE id = :id")
    void deleteById(long id);
    
    @Query("DELETE FROM feedback_records WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
}
