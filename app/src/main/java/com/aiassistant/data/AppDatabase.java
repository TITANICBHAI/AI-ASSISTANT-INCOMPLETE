package com.aiassistant.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.Converters;
import com.aiassistant.data.models.ActionSequence;
import com.aiassistant.data.models.ActionSuggestion;
import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.AIActionReward;
import com.aiassistant.data.models.AISettings;
import com.aiassistant.data.models.CallerProfile;
import com.aiassistant.data.models.ContactEntity;
import com.aiassistant.data.models.FeedbackRecord;
import com.aiassistant.data.models.Game;
import com.aiassistant.data.models.GameAction;
import com.aiassistant.data.models.GameConfig;
import com.aiassistant.data.models.GameProfile;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.GestureSampleEntity;
import com.aiassistant.data.models.ImageSampleEntity;
import com.aiassistant.data.models.LabelDefinitionEntity;
import com.aiassistant.data.models.ModelInfo;
import com.aiassistant.data.models.ModelInfoEntity;
import com.aiassistant.data.models.PerformanceLog;
import com.aiassistant.data.models.PerformanceMetric;
import com.aiassistant.data.models.ScheduledTask;
import com.aiassistant.data.models.ScreenActionEntity;
import com.aiassistant.data.models.Settings;
import com.aiassistant.data.models.Strategy;
import com.aiassistant.data.models.Task;
import com.aiassistant.data.models.TouchPath;
import com.aiassistant.data.models.TrainingData;
import com.aiassistant.data.models.UIElement;
import com.aiassistant.data.models.UserFeedback;
import com.aiassistant.data.models.UserProfile;
import com.aiassistant.data.models.VoiceSampleEntity;

/**
 * Main database class for the application
 * Version 5 - Added all missing entities and TypeConverter registration
 */
@Database(
    entities = {
        ActionSequence.class,
        ActionSuggestion.class,
        AIAction.class,
        AIActionReward.class,
        AISettings.class,
        CallerProfile.class,
        ContactEntity.class,
        FeedbackRecord.class,
        Game.class,
        GameAction.class,
        GameConfig.class,
        GameProfile.class,
        GameState.class,
        GestureSampleEntity.class,
        ImageSampleEntity.class,
        LabelDefinitionEntity.class,
        ModelInfo.class,
        ModelInfoEntity.class,
        PerformanceLog.class,
        PerformanceMetric.class,
        ScheduledTask.class,
        ScreenActionEntity.class,
        Settings.class,
        Strategy.class,
        Task.class,
        TouchPath.class,
        TrainingData.class,
        UIElement.class,
        UserFeedback.class,
        UserProfile.class,
        VoiceSampleEntity.class
    },
    version = 5,
    exportSchema = false
)
@TypeConverters({Converters.class})
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

    // DAO methods for database entities
    public abstract AIActionDao aiActionDao();
    public abstract ActionSequenceDao actionSequenceDao();
    public abstract ActionSuggestionDao actionSuggestionDao();
    public abstract AIActionRewardDao aiActionRewardDao();
    public abstract AISettingsDao aiSettingsDao();
    public abstract CallerProfileDao callerProfileDao();
    public abstract ContactDao contactDao();
    public abstract FeedbackRecordDao feedbackRecordDao();
    public abstract GameDao gameDao();
    public abstract GameActionDao gameActionDao();
    public abstract GameConfigDao gameConfigDao();
    public abstract GameProfileDao gameProfileDao();
    public abstract GameStateDao gameStateDao();
    public abstract GestureSampleDao gestureSampleDao();
    public abstract ImageSampleDao imageSampleDao();
    public abstract LabelDefinitionDao labelDefinitionDao();
    public abstract ModelInfoDao modelInfoDao();
    public abstract PerformanceLogDao performanceLogDao();
    public abstract PerformanceMetricDao performanceMetricDao();
    public abstract ScheduledTaskDao scheduledTaskDao();
    public abstract ScreenActionDao screenActionDao();
    public abstract SettingsDao settingsDao();
    public abstract StrategyDao strategyDao();
    public abstract TaskDao taskDao();
    public abstract TouchPathDao touchPathDao();
    public abstract TrainingDataDao trainingDataDao();
    public abstract UIElementDao uiElementDao();
    public abstract UserFeedbackDao userFeedbackDao();
    public abstract UserProfileDao userProfileDao();
    public abstract VoiceSampleDao voiceSampleDao();
}
