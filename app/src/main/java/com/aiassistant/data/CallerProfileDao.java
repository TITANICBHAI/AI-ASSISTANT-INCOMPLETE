package com.aiassistant.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.CallerProfile;

import java.util.List;

/**
 * Data Access Object for CallerProfile entities
 */
@Dao
public interface CallerProfileDao {
    /**
     * Insert or update caller profile
     * @param callerProfile Caller profile to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CallerProfile callerProfile);

    /**
     * Update caller profile
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
     * Get caller profile by phone number
     * @param phoneNumber Phone number
     * @return Caller profile or null
     */
    @Query("SELECT * FROM caller_profiles WHERE phoneNumber = :phoneNumber LIMIT 1")
    CallerProfile getByPhoneNumber(String phoneNumber);

    /**
     * Get all caller profiles
     * @return List of all caller profiles
     */
    @Query("SELECT * FROM caller_profiles")
    List<CallerProfile> getAll();

    /**
     * Get all caller profiles as LiveData
     * @return LiveData list of all caller profiles
     */
    @Query("SELECT * FROM caller_profiles ORDER BY lastCallTimestamp DESC")
    LiveData<List<CallerProfile>> getAllLive();

    /**
     * Get all caller profiles sorted by last call timestamp
     * @return List of caller profiles
     */
    @Query("SELECT * FROM caller_profiles ORDER BY lastCallTimestamp DESC")
    List<CallerProfile> getAllSortedByLastCall();

    /**
     * Get frequent callers
     * @param minCallCount Minimum call count
     * @return List of frequent callers
     */
    @Query("SELECT * FROM caller_profiles WHERE callCount >= :minCallCount ORDER BY callCount DESC")
    List<CallerProfile> getFrequentCallers(int minCallCount);

    /**
     * Get callers by relationship type
     * @param relationshipType Relationship type
     * @return List of callers
     */
    @Query("SELECT * FROM caller_profiles WHERE relationshipType = :relationshipType")
    List<CallerProfile> getByRelationshipType(String relationshipType);

    /**
     * Search callers by name
     * @param query Search query
     * @return List of matching callers
     */
    @Query("SELECT * FROM caller_profiles WHERE displayName LIKE '%' || :query || '%'")
    List<CallerProfile> searchByName(String query);

    /**
     * Get total count of caller profiles
     * @return Count
     */
    @Query("SELECT COUNT(*) FROM caller_profiles")
    int count();

    /**
     * Delete all caller profiles
     */
    @Query("DELETE FROM caller_profiles")
    void deleteAll();
}
