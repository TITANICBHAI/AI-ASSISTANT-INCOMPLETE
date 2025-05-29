package com.aiassistant.data.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.FeedbackRecord;

import java.util.List;

/**
 * DAO for FeedbackRecord
 */
@Dao
public interface FeedbackRecordDao {
    
    /**
     * Get all feedback records
     * 
     * @return The feedback records
     */
    @Query("SELECT * FROM feedback_records ORDER BY timestamp DESC")
    List<FeedbackRecord> getAll();
    
    /**
     * Get feedback record by ID
     * 
     * @param id The ID
     * @return The feedback record
     */
    @Query("SELECT * FROM feedback_records WHERE id = :id")
    FeedbackRecord getById(long id);
    
    /**
     * Get feedback records by game ID
     * 
     * @param gameId The game ID
     * @return The feedback records
     */
    @Query("SELECT * FROM feedback_records WHERE gameId = :gameId ORDER BY timestamp DESC")
    List<FeedbackRecord> getByGameId(String gameId);
    
    /**
     * Get feedback records by algorithm ID
     * 
     * @param algorithmId The algorithm ID
     * @return The feedback records
     */
    @Query("SELECT * FROM feedback_records WHERE algorithmId = :algorithmId ORDER BY timestamp DESC")
    List<FeedbackRecord> getByAlgorithmId(int algorithmId);
    
    /**
     * Insert a feedback record
     * 
     * @param feedbackRecord The feedback record
     * @return The inserted ID
     */
    @Insert
    long insert(FeedbackRecord feedbackRecord);
    
    /**
     * Insert multiple feedback records
     * 
     * @param feedbackRecords The feedback records
     * @return The inserted IDs
     */
    @Insert
    List<Long> insertAll(List<FeedbackRecord> feedbackRecords);
    
    /**
     * Update a feedback record
     * 
     * @param feedbackRecord The feedback record
     */
    @Update
    void update(FeedbackRecord feedbackRecord);
    
    /**
     * Delete a feedback record
     * 
     * @param feedbackRecord The feedback record
     */
    @Delete
    void delete(FeedbackRecord feedbackRecord);
    
    /**
     * Delete feedback records by game ID
     * 
     * @param gameId The game ID
     */
    @Query("DELETE FROM feedback_records WHERE gameId = :gameId")
    void deleteByGameId(String gameId);
    
    /**
     * Delete old feedback records
     * 
     * @param timestamp The cutoff timestamp
     */
    @Query("DELETE FROM feedback_records WHERE timestamp < :timestamp")
    void deleteOld(long timestamp);
}
