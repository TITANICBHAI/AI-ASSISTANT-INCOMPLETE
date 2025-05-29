package com.aiassistant.core.data.repository;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.data.AppDatabase;
import com.aiassistant.core.data.dao.CallerProfileDao;
import com.aiassistant.core.data.model.CallerProfile;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Repository for CallerProfile
 * Provides access to caller profiles data
 */
public class CallerProfileRepository {
    private static final String TAG = "CallerProfileRepo";
    
    // Database and DAO
    private final AppDatabase database;
    private final CallerProfileDao callerProfileDao;
    
    // Background thread executor
    private final Executor executor;
    
    /**
     * Constructor
     * @param context Application context
     */
    public CallerProfileRepository(Context context) {
        database = AppDatabase.getInstance(context);
        callerProfileDao = database.callerProfileDao();
        executor = Executors.newSingleThreadExecutor();
        
        Log.d(TAG, "CallerProfileRepository initialized");
    }
    
    /**
     * Add new caller profile
     * @param callerProfile Caller profile to add
     */
    public void addCaller(CallerProfile callerProfile) {
        executor.execute(() -> {
            long id = callerProfileDao.insert(callerProfile);
            Log.d(TAG, "Added caller profile: ID=" + id + ", Phone=" + callerProfile.getPhoneNumber());
        });
    }
    
    /**
     * Update existing caller profile
     * @param callerProfile Caller profile to update
     */
    public void updateCaller(CallerProfile callerProfile) {
        executor.execute(() -> {
            callerProfileDao.update(callerProfile);
            Log.d(TAG, "Updated caller profile: ID=" + callerProfile.getId() + 
                  ", Phone=" + callerProfile.getPhoneNumber());
        });
    }
    
    /**
     * Delete caller profile
     * @param callerProfile Caller profile to delete
     */
    public void deleteCaller(CallerProfile callerProfile) {
        executor.execute(() -> {
            callerProfileDao.delete(callerProfile);
            Log.d(TAG, "Deleted caller profile: ID=" + callerProfile.getId() + 
                  ", Phone=" + callerProfile.getPhoneNumber());
        });
    }
    
    /**
     * Get caller profile by ID
     * @param id Caller profile ID
     * @return Caller profile or null if not found
     */
    public CallerProfile getCallerById(long id) {
        try {
            return callerProfileDao.getById(id);
        } catch (Exception e) {
            Log.e(TAG, "Error getting caller by ID: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get caller profile by phone number
     * @param phoneNumber Phone number
     * @return Caller profile or null if not found
     */
    public CallerProfile getCallerByPhone(String phoneNumber) {
        try {
            return callerProfileDao.getByPhoneNumber(phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "Error getting caller by phone: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all caller profiles
     * @return List of all caller profiles
     */
    public List<CallerProfile> getAllCallers() {
        try {
            return callerProfileDao.getAll();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all callers: " + e.getMessage());
            return List.of(); // Empty list
        }
    }
    
    /**
     * Get most frequent callers
     * @param limit Maximum number of callers to return
     * @return List of most frequent callers
     */
    public List<CallerProfile> getMostFrequentCallers(int limit) {
        try {
            return callerProfileDao.getMostFrequentCallers(limit);
        } catch (Exception e) {
            Log.e(TAG, "Error getting most frequent callers: " + e.getMessage());
            return List.of(); // Empty list
        }
    }
    
    /**
     * Get most recent callers
     * @param limit Maximum number of callers to return
     * @return List of most recent callers
     */
    public List<CallerProfile> getMostRecentCallers(int limit) {
        try {
            return callerProfileDao.getMostRecentCallers(limit);
        } catch (Exception e) {
            Log.e(TAG, "Error getting most recent callers: " + e.getMessage());
            return List.of(); // Empty list
        }
    }
    
    /**
     * Get caller profiles by emotion
     * @param emotion Emotion name
     * @return List of caller profiles with matching emotion
     */
    public List<CallerProfile> getCallersByEmotion(String emotion) {
        try {
            return callerProfileDao.getByEmotion(emotion);
        } catch (Exception e) {
            Log.e(TAG, "Error getting callers by emotion: " + e.getMessage());
            return List.of(); // Empty list
        }
    }
    
    /**
     * Search caller profiles by name
     * @param namePattern Name pattern
     * @return List of caller profiles with matching name
     */
    public List<CallerProfile> searchCallersByName(String namePattern) {
        try {
            return callerProfileDao.searchByName(namePattern);
        } catch (Exception e) {
            Log.e(TAG, "Error searching callers by name: " + e.getMessage());
            return List.of(); // Empty list
        }
    }
    
    /**
     * Get count of caller profiles
     * @return Number of caller profiles
     */
    public int getCallerCount() {
        try {
            return callerProfileDao.getCount();
        } catch (Exception e) {
            Log.e(TAG, "Error getting caller count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Delete all caller profiles
     */
    public void deleteAllCallers() {
        executor.execute(() -> {
            try {
                callerProfileDao.deleteAll();
                Log.d(TAG, "Deleted all caller profiles");
            } catch (Exception e) {
                Log.e(TAG, "Error deleting all callers: " + e.getMessage());
            }
        });
    }
}
