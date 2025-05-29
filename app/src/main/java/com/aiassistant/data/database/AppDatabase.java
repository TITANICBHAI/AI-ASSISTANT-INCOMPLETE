package com.aiassistant.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.aiassistant.data.dao.CallerProfileDao;
import com.aiassistant.data.models.CallerProfile;

/**
 * Room database for the application
 */
@Database(entities = {CallerProfile.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "ai_assistant_db";
    
    // Singleton instance
    private static volatile AppDatabase instance;
    
    /**
     * Get singleton database instance
     * @param context Application context
     * @return The AppDatabase instance
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration() // For simplicity in development
                    .build();
        }
        return instance;
    }
    
    /**
     * Get the CallerProfileDao
     * @return CallerProfileDao
     */
    public abstract CallerProfileDao callerProfileDao();
}
