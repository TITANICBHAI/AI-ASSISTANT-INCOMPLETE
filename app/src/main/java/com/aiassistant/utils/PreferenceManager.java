package com.aiassistant.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.aiassistant.data.models.GameProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager for shared preferences
 */
public class PreferenceManager {
    
    private static final String PREF_NAME = "ai_assistant_prefs";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_CURRENT_GAME = "current_game";
    private static final String KEY_AI_ENABLED = "ai_enabled";
    private static final String KEY_AI_MODE = "ai_mode";
    private static final String KEY_GAME_PROFILES = "game_profiles";
    
    private final SharedPreferences preferences;
    private final Gson gson;
    
    /**
     * Constructor
     * 
     * @param context The context
     */
    public PreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    /**
     * Check if this is the first run
     * 
     * @return Whether this is the first run
     */
    public boolean isFirstRun() {
        return preferences.getBoolean(KEY_FIRST_RUN, true);
    }
    
    /**
     * Set whether this is the first run
     * 
     * @param firstRun Whether this is the first run
     */
    public void setFirstRun(boolean firstRun) {
        preferences.edit().putBoolean(KEY_FIRST_RUN, firstRun).apply();
    }
    
    /**
     * Get the current game ID
     * 
     * @return The current game ID
     */
    public String getCurrentGame() {
        return preferences.getString(KEY_CURRENT_GAME, null);
    }
    
    /**
     * Set the current game ID
     * 
     * @param gameId The current game ID
     */
    public void setCurrentGame(String gameId) {
        preferences.edit().putString(KEY_CURRENT_GAME, gameId).apply();
    }
    
    /**
     * Check if AI is enabled
     * 
     * @return Whether AI is enabled
     */
    public boolean isAIEnabled() {
        return preferences.getBoolean(KEY_AI_ENABLED, false);
    }
    
    /**
     * Set whether AI is enabled
     * 
     * @param enabled Whether AI is enabled
     */
    public void setAIEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_AI_ENABLED, enabled).apply();
    }
    
    /**
     * Get the AI mode
     * 
     * @return The AI mode
     */
    public int getAIMode() {
        return preferences.getInt(KEY_AI_MODE, 0);
    }
    
    /**
     * Set the AI mode
     * 
     * @param mode The AI mode
     */
    public void setAIMode(int mode) {
        preferences.edit().putInt(KEY_AI_MODE, mode).apply();
    }
    
    /**
     * Get the game profiles
     * 
     * @return The game profiles
     */
    public Map<String, GameProfile> getGameProfiles() {
        String json = preferences.getString(KEY_GAME_PROFILES, null);
        if (json == null) {
            return new HashMap<>();
        }
        
        Type type = new TypeToken<Map<String, GameProfile>>() {}.getType();
        Map<String, GameProfile> profiles = gson.fromJson(json, type);
        
        return profiles != null ? profiles : new HashMap<>();
    }
    
    /**
     * Set the game profiles
     * 
     * @param profiles The game profiles
     */
    public void setGameProfiles(Map<String, GameProfile> profiles) {
        String json = gson.toJson(profiles);
        preferences.edit().putString(KEY_GAME_PROFILES, json).apply();
    }
    
    /**
     * Get a game profile
     * 
     * @param packageName The package name
     * @return The game profile or null
     */
    public GameProfile getGameProfile(String packageName) {
        Map<String, GameProfile> profiles = getGameProfiles();
        return profiles.get(packageName);
    }
    
    /**
     * Save a game profile
     * 
     * @param profile The game profile
     */
    public void saveGameProfile(GameProfile profile) {
        Map<String, GameProfile> profiles = getGameProfiles();
        profiles.put(profile.getPackageName(), profile);
        setGameProfiles(profiles);
    }
    
    /**
     * Clear all preferences
     */
    public void clear() {
        preferences.edit().clear().apply();
    }
}
