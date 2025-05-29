package com.aiassistant.database.converters;

import android.graphics.Rect;

import androidx.room.TypeConverter;

import com.aiassistant.models.GameState;
import com.aiassistant.models.GameType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Type converter for Room database to handle GameState objects.
 * Converts between GameState and String for storage in the database.
 */
public class GameStateConverter {
    
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Rect.class, new RectTypeAdapter())
            .create();
    
    /**
     * Converts a GameState to a JSON string
     * @param gameState The game state to convert
     * @return JSON string representation of the game state
     */
    @TypeConverter
    public static String fromGameState(GameState gameState) {
        if (gameState == null) {
            return null;
        }
        
        return gson.toJson(gameState);
    }
    
    /**
     * Converts a JSON string to a GameState
     * @param gameStateString JSON string representation of a game state
     * @return GameState object
     */
    @TypeConverter
    public static GameState toGameState(String gameStateString) {
        if (gameStateString == null) {
            return null;
        }
        
        return gson.fromJson(gameStateString, GameState.class);
    }
    
    /**
     * Converts a GameType enum to a string
     * @param gameType The game type enum
     * @return String representation of the game type
     */
    @TypeConverter
    public static String fromGameType(GameType gameType) {
        if (gameType == null) {
            return null;
        }
        
        return gameType.name();
    }
    
    /**
     * Converts a string to a GameType enum
     * @param gameTypeString String representation of a game type
     * @return GameType enum
     */
    @TypeConverter
    public static GameType toGameType(String gameTypeString) {
        if (gameTypeString == null) {
            return null;
        }
        
        return GameType.valueOf(gameTypeString);
    }
    
    /**
     * Custom type adapter for Rect objects to properly serialize and deserialize them
     */
    private static class RectTypeAdapter implements com.google.gson.JsonSerializer<Rect>, com.google.gson.JsonDeserializer<Rect> {
        
        @Override
        public com.google.gson.JsonElement serialize(Rect src, java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
            com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
            jsonObject.addProperty("left", src.left);
            jsonObject.addProperty("top", src.top);
            jsonObject.addProperty("right", src.right);
            jsonObject.addProperty("bottom", src.bottom);
            return jsonObject;
        }
        
        @Override
        public Rect deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
            com.google.gson.JsonObject jsonObject = json.getAsJsonObject();
            int left = jsonObject.get("left").getAsInt();
            int top = jsonObject.get("top").getAsInt();
            int right = jsonObject.get("right").getAsInt();
            int bottom = jsonObject.get("bottom").getAsInt();
            return new Rect(left, top, right, bottom);
        }
    }
}
