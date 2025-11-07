package com.aiassistant.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.CallerProfile;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.ScreenActionEntity;
import com.aiassistant.data.models.TouchPath;
import com.aiassistant.data.models.UIElement;

/**
 * Main database class for the application
 */
@Database(
    entities = {
        AIAction.class,
        CallerProfile.class,
        GameState.class,
        ScreenActionEntity.class,
        TouchPath.class,
        UIElement.class
    },
    version = 2,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "ai_assistant_db";
    private static volatile AppDatabase INSTANCE;

    /**
     * Get the database instance
     * @param context Application context
     * @return Database instance
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Abstract method to access the AIActionDao
     * @return AIActionDao
     */
    public abstract AIActionDao aiActionDao();

    /**
     * Abstract method to access the GameStateDao
     * @return GameStateDao
     */
    public abstract GameStateDao gameStateDao();

    /**
     * Abstract method to access the ScreenActionDao
     * @return ScreenActionDao
     */
    public abstract ScreenActionDao screenActionDao();

    /**
     * Abstract method to access the TouchPathDao
     * @return TouchPathDao
     */
    public abstract TouchPathDao touchPathDao();

    /**
     * Abstract method to access the UIElementDao
     * @return UIElementDao
     */
    public abstract UIElementDao uiElementDao();

    /**
     * Abstract method to access the CallerProfileDao
     * @return CallerProfileDao
     */
    public abstract CallerProfileDao callerProfileDao();
}
