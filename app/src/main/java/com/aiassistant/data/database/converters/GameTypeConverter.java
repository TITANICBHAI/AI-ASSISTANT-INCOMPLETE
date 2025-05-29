package com.aiassistant.data.database.converters;

import androidx.room.TypeConverter;

import com.aiassistant.data.models.GameType;

/**
 * Type converter for GameType enum in Room database
 */
public class GameTypeConverter {
    
    @TypeConverter
    public static String fromGameType(GameType gameType) {
        return gameType == null ? null : gameType.name();
    }

    @TypeConverter
    public static GameType toGameType(String value) {
        try {
            return value == null ? GameType.UNKNOWN : GameType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return GameType.UNKNOWN;
        }
    }
}
