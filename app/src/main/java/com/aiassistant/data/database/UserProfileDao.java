package com.aiassistant.data.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.aiassistant.data.models.UserProfile;

/**
 * Data Access Object for UserProfile entity
 */
@Dao
public interface UserProfileDao {
    
    /**
     * Get the user profile
     * 
     * @return The user profile, or null if not found
     */
    @Query("SELECT * FROM user_profile LIMIT 1")
    UserProfile getUserProfile();
    
    /**
     * Insert a user profile
     * 
     * @param profile The profile to insert
     * @return Row ID of the inserted profile
     */
    @Insert
    long insertUserProfile(UserProfile profile);
    
    /**
     * Update a user profile
     * 
     * @param profile The profile to update
     */
    @Update
    void updateUserProfile(UserProfile profile);
}
