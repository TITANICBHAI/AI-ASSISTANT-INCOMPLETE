package com.aiassistant.data.dao;

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
     * Insert a new caller profile
     * @param callerProfile Caller profile to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CallerProfile callerProfile);
    
    /**
     * Update an existing caller profile
     * @param callerProfile Caller profile to update
     */
    @Update
    void update(CallerProfile callerProfile);
    
    /**
     * Delete a caller profile
     * @param callerProfile Caller profile to delete
     */
    @Delete
    void delete(CallerProfile callerProfile);
    
    /**
     * Get a caller profile by phone number
     * @param phoneNumber Phone number to look up
     * @return Caller profile
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
     * Get all caller profiles sorted by last call timestamp (most recent first)
     * @return List of caller profiles
     */
    @Query("SELECT * FROM caller_profiles ORDER BY lastCallTimestamp DESC")
    List<CallerProfile> getAllSortedByLastCall();
    
    /**
     * Get frequent callers (call count >= minimum)
     * @param minCallCount Minimum call count
     * @return List of frequent caller profiles
     */
    @Query("SELECT * FROM caller_profiles WHERE callCount >= :minCallCount ORDER BY callCount DESC")
    List<CallerProfile> getFrequentCallers(int minCallCount);
    
    /**
     * Get caller profiles by relationship type
     * @param relationshipType Relationship type
     * @return List of caller profiles
     */
    @Query("SELECT * FROM caller_profiles WHERE relationshipType = :relationshipType")
    List<CallerProfile> getByRelationshipType(String relationshipType);
    
    /**
     * Search caller profiles by display name
     * @param query Search query
     * @return List of matching caller profiles
     */
    @Query("SELECT * FROM caller_profiles WHERE displayName LIKE '%' || :query || '%'")
    List<CallerProfile> searchByName(String query);
    
    /**
     * Count all caller profiles
     * @return Total count of caller profiles
     */
    @Query("SELECT COUNT(*) FROM caller_profiles")
    int count();
    
    /**
     * Delete all caller profiles
     */
    @Query("DELETE FROM caller_profiles")
    void deleteAll();
}
