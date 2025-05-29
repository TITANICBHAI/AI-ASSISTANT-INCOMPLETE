package com.aiassistant.core.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.aiassistant.core.data.converter.MapConverter;
import com.aiassistant.core.data.dao.CallerProfileDao;
import com.aiassistant.core.data.model.CallerProfile;

/**
 * Room database for the application
 */
@Database(entities = {CallerProfile.class}, version = 1, exportSchema = false)
@TypeConverters({MapConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "ai_assistant_db";
    
    // Singleton instance
    private static volatile AppDatabase INSTANCE;
    
    /**
     * Get database instance
     * @param context Application context
     * @return Database instance
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
    
    /**
     * Close database
     */
    public static void closeDatabase() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
    
    /**
     * Get caller profile DAO
     * @return Caller profile DAO
     */
    public abstract CallerProfileDao callerProfileDao();
}
