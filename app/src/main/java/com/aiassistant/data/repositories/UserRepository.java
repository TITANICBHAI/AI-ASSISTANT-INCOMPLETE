package com.aiassistant.data.repositories;

import android.content.Context;

import androidx.room.Room;

import com.aiassistant.data.database.AppDatabase;
import com.aiassistant.data.database.UserProfileDao;
import com.aiassistant.data.models.UserProfile;

/**
 * Repository for user profile data
 */
public class UserRepository {
    
    private final UserProfileDao userProfileDao;
    
    /**
     * Constructor
     * 
     * @param context Application context
     */
    public UserRepository(Context context) {
        AppDatabase database = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "ai_assistant_db")
                .fallbackToDestructiveMigration()
                .build();
        
        userProfileDao = database.userProfileDao();
    }
    
    /**
     * Get the user profile
     * 
     * @return The user profile, or null if not found
     */
    public UserProfile getUserProfile() {
        return userProfileDao.getUserProfile();
    }
    
    /**
     * Save the user profile
     * 
     * @param profile The profile to save
     */
    public void saveUserProfile(UserProfile profile) {
        UserProfile existingProfile = userProfileDao.getUserProfile();
        if (existingProfile == null) {
            userProfileDao.insertUserProfile(profile);
        } else {
            userProfileDao.updateUserProfile(profile);
        }
    }
}
