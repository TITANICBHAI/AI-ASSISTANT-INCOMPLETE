package com.aiassistant.ai.features.profile;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Game Profile
 * Represents a single game configuration profile
 */
public class GameProfile implements Parcelable {
    private static final String TAG = "GameProfile";
    
    // Game identification
    private final String packageName;
    private String displayName;
    private String gameVersion;
    
    // Profile data
    private String iconPath;
    private long createdTime;
    private long lastUsedTime;
    private int sessionCount;
    
    // Profile settings
    private final Map<String, Object> settings;
    
    // Feature states
    private final Map<String, Boolean> featureStates;
    
    /**
     * Constructor
     * @param packageName Game package name
     * @param displayName Display name for the game
     */
    public GameProfile(String packageName, String displayName) {
        this.packageName = packageName;
        this.displayName = displayName;
        this.gameVersion = "";
        this.iconPath = "";
        this.createdTime = System.currentTimeMillis();
        this.lastUsedTime = this.createdTime;
        this.sessionCount = 0;
        this.settings = new ConcurrentHashMap<>();
        this.featureStates = new ConcurrentHashMap<>();
    }
    
    /**
     * Get game package name
     * @return Package name
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * Get display name
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Set display name
     * @param displayName New display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Get game version
     * @return Game version
     */
    public String getGameVersion() {
        return gameVersion;
    }
    
    /**
     * Set game version
     * @param gameVersion New game version
     */
    public void setGameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
    }
    
    /**
     * Get icon path
     * @return Icon path
     */
    public String getIconPath() {
        return iconPath;
    }
    
    /**
     * Set icon path
     * @param iconPath New icon path
     */
    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
    
    /**
     * Get created time
     * @return Created time in milliseconds
     */
    public long getCreatedTime() {
        return createdTime;
    }
    
    /**
     * Get last used time
     * @return Last used time in milliseconds
     */
    public long getLastUsedTime() {
        return lastUsedTime;
    }
    
    /**
     * Update last used time to current time
     * @return Updated timestamp
     */
    public long updateLastUsed() {
        lastUsedTime = System.currentTimeMillis();
        return lastUsedTime;
    }
    
    /**
     * Get session count
     * @return Number of sessions
     */
    public int getSessionCount() {
        return sessionCount;
    }
    
    /**
     * Increment session count
     * @return New session count
     */
    public int incrementSessionCount() {
        return ++sessionCount;
    }
    
    /**
     * Set a profile setting
     * @param key Setting key
     * @param value Setting value
     */
    public void setSetting(String key, Object value) {
        settings.put(key, value);
    }
    
    /**
     * Get a profile setting
     * @param key Setting key
     * @param defaultValue Default value if setting not found
     * @return Setting value or default value if not found
     */
    public Object getSetting(String key, Object defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }
    
    /**
     * Get a boolean setting
     * @param key Setting key
     * @param defaultValue Default value if setting not found
     * @return Setting value or default value if not found
     */
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        Object value = settings.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
    
    /**
     * Get an integer setting
     * @param key Setting key
     * @param defaultValue Default value if setting not found
     * @return Setting value or default value if not found
     */
    public int getIntSetting(String key, int defaultValue) {
        Object value = settings.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    /**
     * Get a float setting
     * @param key Setting key
     * @param defaultValue Default value if setting not found
     * @return Setting value or default value if not found
     */
    public float getFloatSetting(String key, float defaultValue) {
        Object value = settings.get(key);
        if (value instanceof Float) {
            return (Float) value;
        } else if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return defaultValue;
    }
    
    /**
     * Get a string setting
     * @param key Setting key
     * @param defaultValue Default value if setting not found
     * @return Setting value or default value if not found
     */
    public String getStringSetting(String key, String defaultValue) {
        Object value = settings.get(key);
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return value.toString();
        }
        return defaultValue;
    }
    
    /**
     * Get all settings
     * @return Map of all settings
     */
    public Map<String, Object> getAllSettings() {
        return new HashMap<>(settings);
    }
    
    /**
     * Set feature state
     * @param featureId Feature ID
     * @param enabled true to enable, false to disable
     */
    public void setFeatureEnabled(String featureId, boolean enabled) {
        featureStates.put(featureId, enabled);
    }
    
    /**
     * Check if a feature is enabled
     * @param featureId Feature ID
     * @param defaultValue Default value if not specified
     * @return true if enabled, false otherwise
     */
    public boolean isFeatureEnabled(String featureId, boolean defaultValue) {
        return featureStates.getOrDefault(featureId, defaultValue);
    }
    
    /**
     * Get all feature states
     * @return Map of feature states
     */
    public Map<String, Boolean> getAllFeatureStates() {
        return new HashMap<>(featureStates);
    }
    
    /**
     * Convert profile to JSON
     * @return JSON string representation
     */
    public String toJson() {
        try {
            JSONObject json = new JSONObject();
            
            // Basic profile data
            json.put("packageName", packageName);
            json.put("displayName", displayName);
            json.put("gameVersion", gameVersion);
            json.put("iconPath", iconPath);
            json.put("createdTime", createdTime);
            json.put("lastUsedTime", lastUsedTime);
            json.put("sessionCount", sessionCount);
            
            // Settings
            JSONObject settingsJson = new JSONObject();
            for (Map.Entry<String, Object> entry : settings.entrySet()) {
                settingsJson.put(entry.getKey(), entry.getValue());
            }
            json.put("settings", settingsJson);
            
            // Feature states
            JSONObject featuresJson = new JSONObject();
            for (Map.Entry<String, Boolean> entry : featureStates.entrySet()) {
                featuresJson.put(entry.getKey(), entry.getValue());
            }
            json.put("features", featuresJson);
            
            return json.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error converting profile to JSON", e);
            return null;
        }
    }
    
    /**
     * Create profile from JSON
     * @param json JSON string representation
     * @return Created profile or null if parsing failed
     */
    public static GameProfile fromJson(String json) {
        try {
            JSONObject jsonObj = new JSONObject(json);
            
            // Get basic profile data
            String packageName = jsonObj.getString("packageName");
            String displayName = jsonObj.getString("displayName");
            
            // Create profile
            GameProfile profile = new GameProfile(packageName, displayName);
            
            // Set optional fields
            if (jsonObj.has("gameVersion")) {
                profile.setGameVersion(jsonObj.getString("gameVersion"));
            }
            if (jsonObj.has("iconPath")) {
                profile.setIconPath(jsonObj.getString("iconPath"));
            }
            if (jsonObj.has("createdTime")) {
                profile.createdTime = jsonObj.getLong("createdTime");
            }
            if (jsonObj.has("lastUsedTime")) {
                profile.lastUsedTime = jsonObj.getLong("lastUsedTime");
            }
            if (jsonObj.has("sessionCount")) {
                profile.sessionCount = jsonObj.getInt("sessionCount");
            }
            
            // Load settings
            if (jsonObj.has("settings")) {
                JSONObject settingsJson = jsonObj.getJSONObject("settings");
                Iterator<String> keys = settingsJson.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    profile.settings.put(key, settingsJson.get(key));
                }
            }
            
            // Load feature states
            if (jsonObj.has("features")) {
                JSONObject featuresJson = jsonObj.getJSONObject("features");
                Iterator<String> keys = featuresJson.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    profile.featureStates.put(key, featuresJson.getBoolean(key));
                }
            }
            
            return profile;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing profile from JSON", e);
            return null;
        }
    }
    
    // Parcelable implementation
    
    protected GameProfile(Parcel in) {
        packageName = in.readString();
        displayName = in.readString();
        gameVersion = in.readString();
        iconPath = in.readString();
        createdTime = in.readLong();
        lastUsedTime = in.readLong();
        sessionCount = in.readInt();
        
        // Read settings
        settings = new ConcurrentHashMap<>();
        int settingsSize = in.readInt();
        for (int i = 0; i < settingsSize; i++) {
            String key = in.readString();
            Object value = in.readValue(getClass().getClassLoader());
            settings.put(key, value);
        }
        
        // Read feature states
        featureStates = new ConcurrentHashMap<>();
        int featuresSize = in.readInt();
        for (int i = 0; i < featuresSize; i++) {
            String key = in.readString();
            boolean value = in.readInt() == 1;
            featureStates.put(key, value);
        }
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeString(displayName);
        dest.writeString(gameVersion);
        dest.writeString(iconPath);
        dest.writeLong(createdTime);
        dest.writeLong(lastUsedTime);
        dest.writeInt(sessionCount);
        
        // Write settings
        dest.writeInt(settings.size());
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeValue(entry.getValue());
        }
        
        // Write feature states
        dest.writeInt(featureStates.size());
        for (Map.Entry<String, Boolean> entry : featureStates.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeInt(entry.getValue() ? 1 : 0);
        }
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<GameProfile> CREATOR = new Creator<GameProfile>() {
        @Override
        public GameProfile createFromParcel(Parcel in) {
            return new GameProfile(in);
        }
        
        @Override
        public GameProfile[] newArray(int size) {
            return new GameProfile[size];
        }
    };
}
