package com.aiassistant.data.converters;

import androidx.room.TypeConverter;

import com.aiassistant.data.models.GameState;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Room TypeConverter for GameState objects
 */
public class GameStateConverter {
    
    private static final Gson gson = new Gson();
    
    /**
     * Convert GameState to JSON string
     * 
     * @param gameState The game state
     * @return The JSON string
     */
    @TypeConverter
    public static String fromGameState(GameState gameState) {
        if (gameState == null) {
            return null;
        }
        
        // Don't serialize the bitmap to avoid memory issues
        // Store only a reference or a placeholder
        GameState clone = gameState.copy();
        clone.setScreenImage(null);
        
        return gson.toJson(clone);
    }
    
    /**
     * Convert JSON string to GameState
     * 
     * @param gameStateString The JSON string
     * @return The game state
     */
    @TypeConverter
    public static GameState toGameState(String gameStateString) {
        if (gameStateString == null) {
            return null;
        }
        
        Type type = new TypeToken<GameState>() {}.getType();
        return gson.fromJson(gameStateString, type);
    }
}
