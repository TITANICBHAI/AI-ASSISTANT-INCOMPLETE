package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.UserFeedback;

import java.util.List;

@Dao
public interface UserFeedbackDao {
    
    @Insert
    long insert(UserFeedback feedback);
    
    @Update
    void update(UserFeedback feedback);
    
    @Delete
    void delete(UserFeedback feedback);
    
    @Query("SELECT * FROM user_feedback ORDER BY timestamp DESC")
    List<UserFeedback> getAll();
    
    @Query("SELECT * FROM user_feedback ORDER BY timestamp DESC")
    LiveData<List<UserFeedback>> getAllLive();
    
    @Query("SELECT * FROM user_feedback WHERE id = :id LIMIT 1")
    UserFeedback getById(long id);
    
    @Query("SELECT * FROM user_feedback WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<UserFeedback> getByGameId(String gameId);
    
    @Query("SELECT * FROM user_feedback WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    List<UserFeedback> getBySessionId(String sessionId);
    
    @Query("SELECT * FROM user_feedback WHERE actionId = :actionId ORDER BY timestamp DESC")
    List<UserFeedback> getByActionId(String actionId);
    
    @Query("SELECT AVG(rating) FROM user_feedback WHERE gameId = :gameId")
    Double getAverageRatingForGame(String gameId);
    
    @Query("SELECT * FROM user_feedback WHERE rating >= :minRating ORDER BY timestamp DESC")
    List<UserFeedback> getPositiveFeedback(int minRating);
    
    @Query("SELECT * FROM user_feedback WHERE comment IS NOT NULL AND comment != '' ORDER BY timestamp DESC")
    List<UserFeedback> getFeedbackWithComments();
    
    @Query("DELETE FROM user_feedback")
    void deleteAll();
    
    @Query("DELETE FROM user_feedback WHERE id = :id")
    void deleteById(long id);
    
    @Query("DELETE FROM user_feedback WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
}
