package com.aiassistant.data.repositories;

import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.aiassistant.data.database.AppDatabase;
import com.aiassistant.data.database.CallerProfileDao;
import com.aiassistant.data.models.CallerProfile;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for caller profile data
 * Provides methods to access and manipulate caller profiles
 */
public class CallerProfileRepository {
    private static final String TAG = "CallerProfileRepo";
    
    private final CallerProfileDao callerProfileDao;
    private final ExecutorService executorService;
    
    /**
     * Constructor
     * @param context Application context
     */
    public CallerProfileRepository(Context context) {
        AppDatabase database = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "app_database")
                .build();
                
        callerProfileDao = database.callerProfileDao();
        executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Get caller profile by phone number
     * @param phoneNumber Phone number
     * @return CallerProfile or null if not found
     */
    public CallerProfile getCallerProfile(String phoneNumber) {
        try {
            return callerProfileDao.getCallerByPhoneNumber(phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "Error getting caller profile: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Save caller profile
     * @param callerProfile Caller profile to save
     */
    public void saveCallerProfile(CallerProfile callerProfile) {
        executorService.execute(() -> {
            try {
                callerProfileDao.insertOrUpdate(callerProfile);
                Log.d(TAG, "Saved caller profile: " + callerProfile.getPhoneNumber());
            } catch (Exception e) {
                Log.e(TAG, "Error saving caller profile: " + e.getMessage());
            }
        });
    }
    
    /**
     * Delete caller profile
     * @param callerProfile Caller profile to delete
     */
    public void deleteCallerProfile(CallerProfile callerProfile) {
        executorService.execute(() -> {
            try {
                callerProfileDao.delete(callerProfile);
                Log.d(TAG, "Deleted caller profile: " + callerProfile.getPhoneNumber());
            } catch (Exception e) {
                Log.e(TAG, "Error deleting caller profile: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get all caller profiles
     * @return List of all caller profiles
     */
    public List<CallerProfile> getAllCallerProfiles() {
        try {
            return callerProfileDao.getAllCallers();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all caller profiles: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get number of profiles
     * @return Number of profiles
     */
    public int getProfileCount() {
        try {
            return callerProfileDao.getCallerCount();
        } catch (Exception e) {
            Log.e(TAG, "Error getting profile count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Update caller relationship type
     * @param phoneNumber Phone number
     * @param relationshipType Relationship type
     */
    public void updateRelationshipType(String phoneNumber, String relationshipType) {
        executorService.execute(() -> {
            try {
                CallerProfile profile = callerProfileDao.getCallerByPhoneNumber(phoneNumber);
                if (profile != null) {
                    profile.setRelationshipType(relationshipType);
                    callerProfileDao.insertOrUpdate(profile);
                    Log.d(TAG, "Updated relationship type for " + phoneNumber + " to " + relationshipType);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating relationship type: " + e.getMessage());
            }
        });
    }
    
    /**
     * Update caller display name
     * @param phoneNumber Phone number
     * @param displayName Display name
     */
    public void updateDisplayName(String phoneNumber, String displayName) {
        executorService.execute(() -> {
            try {
                CallerProfile profile = callerProfileDao.getCallerByPhoneNumber(phoneNumber);
                if (profile != null) {
                    profile.setDisplayName(displayName);
                    callerProfileDao.insertOrUpdate(profile);
                    Log.d(TAG, "Updated display name for " + phoneNumber + " to " + displayName);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating display name: " + e.getMessage());
            }
        });
    }
}
