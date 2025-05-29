package com.aiassistant.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.aiassistant.data.models.CallerProfile;

import java.util.List;

/**
 * Data Access Object for caller profiles
 */
@Dao
public interface CallerProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(CallerProfile callerProfile);
    
    @Delete
    void delete(CallerProfile callerProfile);
    
    @Query("SELECT * FROM caller_profiles WHERE phoneNumber = :phoneNumber")
    CallerProfile getCallerByPhoneNumber(String phoneNumber);
    
    @Query("SELECT * FROM caller_profiles ORDER BY lastCallTimestamp DESC")
    List<CallerProfile> getAllCallers();
    
    @Query("SELECT COUNT(*) FROM caller_profiles")
    int getCallerCount();
    
    @Query("SELECT * FROM caller_profiles WHERE relationshipType = :relationshipType")
    List<CallerProfile> getCallersByRelationshipType(String relationshipType);
    
    @Query("SELECT * FROM caller_profiles ORDER BY callCount DESC LIMIT :limit")
    List<CallerProfile> getTopCallers(int limit);
    
    @Query("SELECT * FROM caller_profiles WHERE lastCallTimestamp > :timestamp")
    List<CallerProfile> getRecentCallers(long timestamp);
}
