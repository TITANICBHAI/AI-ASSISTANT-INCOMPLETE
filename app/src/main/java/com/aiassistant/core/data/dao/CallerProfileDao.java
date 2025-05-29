package com.aiassistant.core.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.core.data.model.CallerProfile;

import java.util.List;

/**
 * Data Access Object (DAO) for CallerProfile
 */
@Dao
public interface CallerProfileDao {
    /**
     * Insert new caller profile
     * @param callerProfile Caller profile to insert
     * @return Row ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CallerProfile callerProfile);
    
    /**
     * Update existing caller profile
     * @param callerProfile Caller profile to update
     */
    @Update
    void update(CallerProfile callerProfile);
    
    /**
     * Delete caller profile
     * @param callerProfile Caller profile to delete
     */
    @Delete
    void delete(CallerProfile callerProfile);
    
    /**
     * Get caller profile by ID
     * @param id Caller profile ID
     * @return Caller profile or null if not found
     */
    @Query("SELECT * FROM caller_profiles WHERE id = :id")
    CallerProfile getById(long id);
    
    /**
     * Get caller profile by phone number
     * @param phoneNumber Phone number
     * @return Caller profile or null if not found
     */
    @Query("SELECT * FROM caller_profiles WHERE phoneNumber = :phoneNumber")
    CallerProfile getByPhoneNumber(String phoneNumber);
    
    /**
     * Get all caller profiles
     * @return List of all caller profiles
     */
    @Query("SELECT * FROM caller_profiles")
    List<CallerProfile> getAll();
    
    /**
     * Get most frequent callers
     * @param limit Maximum number of callers to return
     * @return List of most frequent callers
     */
    @Query("SELECT * FROM caller_profiles ORDER BY callCount DESC LIMIT :limit")
    List<CallerProfile> getMostFrequentCallers(int limit);
    
    /**
     * Get most recent callers
     * @param limit Maximum number of callers to return
     * @return List of most recent callers
     */
    @Query("SELECT * FROM caller_profiles ORDER BY lastCallTime DESC LIMIT :limit")
    List<CallerProfile> getMostRecentCallers(int limit);
    
    /**
     * Get caller profiles by emotion
     * @param emotion Emotion name
     * @return List of caller profiles with matching emotion
     */
    @Query("SELECT * FROM caller_profiles WHERE lastEmotion = :emotion")
    List<CallerProfile> getByEmotion(String emotion);
    
    /**
     * Get caller profiles by name pattern
     * @param namePattern Name pattern
     * @return List of caller profiles with matching name
     */
    @Query("SELECT * FROM caller_profiles WHERE name LIKE '%' || :namePattern || '%'")
    List<CallerProfile> searchByName(String namePattern);
    
    /**
     * Get count of caller profiles
     * @return Number of caller profiles
     */
    @Query("SELECT COUNT(*) FROM caller_profiles")
    int getCount();
    
    /**
     * Delete all caller profiles
     */
    @Query("DELETE FROM caller_profiles")
    void deleteAll();
}
