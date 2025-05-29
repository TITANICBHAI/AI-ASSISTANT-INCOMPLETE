package com.aiassistant.data.repository;

import android.content.Context;
import android.util.Log;

import com.aiassistant.data.dao.CallerProfileDao;
import com.aiassistant.data.database.AppDatabase;
import com.aiassistant.data.models.CallerProfile;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository for managing CallerProfile data
 * Provides a clean API for data access to the rest of the application
 */
public class CallerProfileRepository {
    private static final String TAG = "CallerProfileRepo";
    
    // Data Access Objects
    private final CallerProfileDao callerProfileDao;
    
    // Executor for background tasks
    private final Executor executor;
    
    /**
     * Constructor
     * @param context Application context
     */
    public CallerProfileRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        callerProfileDao = database.callerProfileDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Insert a new caller profile
     * @param callerProfile Caller profile to insert
     */
    public void insertCallerProfile(CallerProfile callerProfile) {
        executor.execute(() -> {
            try {
                callerProfileDao.insert(callerProfile);
                Log.d(TAG, "Inserted caller profile: " + callerProfile.getPhoneNumber());
            } catch (Exception e) {
                Log.e(TAG, "Error inserting caller profile: " + e.getMessage());
            }
        });
    }
    
    /**
     * Update an existing caller profile
     * @param callerProfile Caller profile to update
     */
    public void updateCallerProfile(CallerProfile callerProfile) {
        executor.execute(() -> {
            try {
                callerProfileDao.update(callerProfile);
                Log.d(TAG, "Updated caller profile: " + callerProfile.getPhoneNumber());
            } catch (Exception e) {
                Log.e(TAG, "Error updating caller profile: " + e.getMessage());
            }
        });
    }
    
    /**
     * Delete a caller profile
     * @param callerProfile Caller profile to delete
     */
    public void deleteCallerProfile(CallerProfile callerProfile) {
        executor.execute(() -> {
            try {
                callerProfileDao.delete(callerProfile);
                Log.d(TAG, "Deleted caller profile: " + callerProfile.getPhoneNumber());
            } catch (Exception e) {
                Log.e(TAG, "Error deleting caller profile: " + e.getMessage());
            }
        });
    }
    
    /**
     * Get a caller profile by phone number
     * This is a blocking operation and should not be called on the main thread
     * @param phoneNumber Phone number to look up
     * @return Caller profile, or null if not found
     */
    public CallerProfile getCallerProfile(String phoneNumber) {
        try {
            return callerProfileDao.getByPhoneNumber(phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "Error getting caller profile: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all caller profiles
     * This is a blocking operation and should not be called on the main thread
     * @return List of all caller profiles
     */
    public List<CallerProfile> getAllCallerProfiles() {
        try {
            return callerProfileDao.getAll();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all caller profiles: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all caller profiles sorted by last call timestamp
     * This is a blocking operation and should not be called on the main thread
     * @return List of caller profiles
     */
    public List<CallerProfile> getRecentCallers() {
        try {
            return callerProfileDao.getAllSortedByLastCall();
        } catch (Exception e) {
            Log.e(TAG, "Error getting recent callers: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get frequent callers
     * This is a blocking operation and should not be called on the main thread
     * @param minCallCount Minimum call count
     * @return List of frequent caller profiles
     */
    public List<CallerProfile> getFrequentCallers(int minCallCount) {
        try {
            return callerProfileDao.getFrequentCallers(minCallCount);
        } catch (Exception e) {
            Log.e(TAG, "Error getting frequent callers: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get caller profiles by relationship type
     * This is a blocking operation and should not be called on the main thread
     * @param relationshipType Relationship type
     * @return List of caller profiles
     */
    public List<CallerProfile> getCallersByRelationship(String relationshipType) {
        try {
            return callerProfileDao.getByRelationshipType(relationshipType);
        } catch (Exception e) {
            Log.e(TAG, "Error getting callers by relationship: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Search caller profiles by name
     * This is a blocking operation and should not be called on the main thread
     * @param query Search query
     * @return List of matching caller profiles
     */
    public List<CallerProfile> searchCallersByName(String query) {
        try {
            return callerProfileDao.searchByName(query);
        } catch (Exception e) {
            Log.e(TAG, "Error searching callers by name: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get count of caller profiles
     * This is a blocking operation and should not be called on the main thread
     * @return Count of caller profiles
     */
    public int getCallerCount() {
        try {
            return callerProfileDao.count();
        } catch (Exception e) {
            Log.e(TAG, "Error getting caller count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Delete all caller profiles
     */
    public void deleteAllCallerProfiles() {
        executor.execute(() -> {
            try {
                callerProfileDao.deleteAll();
                Log.d(TAG, "Deleted all caller profiles");
            } catch (Exception e) {
                Log.e(TAG, "Error deleting all caller profiles: " + e.getMessage());
            }
        });
    }
}
