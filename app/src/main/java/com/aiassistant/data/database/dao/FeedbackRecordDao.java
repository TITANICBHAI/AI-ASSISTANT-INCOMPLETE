package com.aiassistant.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.FeedbackRecord;

import java.util.List;

/**
 * DAO for feedback record entities
 */
@Dao
public interface FeedbackRecordDao {
    
    /**
     * Insert a feedback record
     * 
     * @param record The feedback record
     * @return The new row ID
     */
    @Insert
    long insert(FeedbackRecord record);
    
    /**
     * Update a feedback record
     * 
     * @param record The feedback record
     */
    @Update
    void update(FeedbackRecord record);
    
    /**
     * Delete a feedback record
     * 
     * @param record The feedback record
     */
    @Delete
    void delete(FeedbackRecord record);
    
    /**
     * Get a feedback record by ID
     * 
     * @param id The ID
     * @return The feedback record
     */
    @Query("SELECT * FROM feedback_records WHERE id = :id")
    FeedbackRecord getById(long id);
    
    /**
     * Get feedback records for a game
     * 
     * @param gameId The game ID
     * @return The feedback records
     */
    @Query("SELECT * FROM feedback_records WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<FeedbackRecord> getForGame(String gameId);
    
    /**
     * Get feedback records for an action
     * 
     * @param actionId The action ID
     * @return The feedback records
     */
    @Query("SELECT * FROM feedback_records WHERE actionId = :actionId")
    List<FeedbackRecord> getForAction(long actionId);
    
    /**
     * Get negative feedback records
     * 
     * @param limit The maximum number of results
     * @return The feedback records
     */
    @Query("SELECT * FROM feedback_records WHERE rating < 3 ORDER BY timestamp DESC LIMIT :limit")
    List<FeedbackRecord> getNegativeFeedback(int limit);
    
    /**
     * Get positive feedback records
     * 
     * @param limit The maximum number of results
     * @return The feedback records
     */
    @Query("SELECT * FROM feedback_records WHERE rating >= 3 ORDER BY timestamp DESC LIMIT :limit")
    List<FeedbackRecord> getPositiveFeedback(int limit);
    
    /**
     * Delete old feedback records
     * 
     * @param timestamp The cutoff timestamp
     * @return The number of rows deleted
     */
    @Query("DELETE FROM feedback_records WHERE timestamp < :timestamp")
    int deleteOldRecords(long timestamp);
    
    /**
     * Count feedback records
     * 
     * @return The count
     */
    @Query("SELECT COUNT(*) FROM feedback_records")
    int count();
}
