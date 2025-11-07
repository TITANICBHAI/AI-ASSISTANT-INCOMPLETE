package com.aiassistant.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.CallerProfile;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.GestureSampleEntity;
import com.aiassistant.data.models.ImageSampleEntity;
import com.aiassistant.data.models.LabelDefinitionEntity;
import com.aiassistant.data.models.ModelInfoEntity;
import com.aiassistant.data.models.ScheduledTask;
import com.aiassistant.data.models.ScreenActionEntity;
import com.aiassistant.data.models.Task;
import com.aiassistant.data.models.TouchPath;
import com.aiassistant.data.models.UIElement;
import com.aiassistant.data.models.VoiceSampleEntity;

/**
 * Main database class for the application
 */
@Database(
    entities = {
        AIAction.class,
        CallerProfile.class,
        GameState.class,
        GestureSampleEntity.class,
        ImageSampleEntity.class,
        LabelDefinitionEntity.class,
        ModelInfoEntity.class,
        ScheduledTask.class,
        ScreenActionEntity.class,
        Task.class,
        TouchPath.class,
        UIElement.class,
        VoiceSampleEntity.class
    },
    version = 4,
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

    /**
     * Abstract method to access the TaskDao
     * @return TaskDao
     */
    public abstract TaskDao taskDao();

    /**
     * Abstract method to access the ScheduledTaskDao
     * @return ScheduledTaskDao
     */
    public abstract ScheduledTaskDao scheduledTaskDao();

    /**
     * Abstract method to access the VoiceSampleDao
     * @return VoiceSampleDao
     */
    public abstract VoiceSampleDao voiceSampleDao();

    /**
     * Abstract method to access the GestureSampleDao
     * @return GestureSampleDao
     */
    public abstract GestureSampleDao gestureSampleDao();

    /**
     * Abstract method to access the ImageSampleDao
     * @return ImageSampleDao
     */
    public abstract ImageSampleDao imageSampleDao();

    /**
     * Abstract method to access the LabelDefinitionDao
     * @return LabelDefinitionDao
     */
    public abstract LabelDefinitionDao labelDefinitionDao();

    /**
     * Abstract method to access the ModelInfoDao
     * @return ModelInfoDao
     */
    public abstract ModelInfoDao modelInfoDao();
}
