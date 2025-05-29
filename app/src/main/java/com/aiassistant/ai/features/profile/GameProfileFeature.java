package com.aiassistant.ai.features.profile;

import android.content.Context;
import android.util.Log;

import com.aiassistant.ai.features.BaseFeature;
import com.aiassistant.security.SecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-Game Profile System
 * - Manages game-specific profiles and configurations
 * - Automatic game detection and profile switching
 * - Customizable settings for each game
 * - Profile import/export capabilities
 */
public class GameProfileFeature extends BaseFeature {
    private static final String TAG = "GameProfile";
    private static final String FEATURE_NAME = "multi_game_profile_system";
    
    // Current active profile
    private GameProfile activeProfile;
    
    // All registered profiles
    private final Map<String, GameProfile> profiles;
    
    // Default profile settings
    private final Map<String, Object> defaultSettings;
    
    // Recent profile history
    private final List<String> recentProfiles;
    private static final int MAX_RECENT_PROFILES = 5;
    
    // Profile change listeners
    private final List<ProfileChangeListener> listeners;
    
    /**
     * Constructor
     * @param context Application context
     */
    public GameProfileFeature(Context context) {
        super(context, FEATURE_NAME);
        this.profiles = new ConcurrentHashMap<>();
        this.defaultSettings = new HashMap<>();
        this.recentProfiles = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.activeProfile = null;
    }
    
    @Override
    public boolean initialize() {
        boolean success = super.initialize();
        if (success) {
            try {
                // Set up default settings
                setupDefaultSettings();
                
                // Load saved profiles
                loadProfiles();
                
                Log.d(TAG, "Game profile system initialized with " + 
                      profiles.size() + " profiles");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize game profile system", e);
                return false;
            }
        }
        return false;
    }
    
    @Override
    public void update() {
        if (!isEnabled()) return;
        
        // Apply security context for this operation
        SecurityContext.getInstance().setCurrentFeatureActive(FEATURE_NAME);
        
        try {
            // Check for any needed profile updates
            if (activeProfile != null) {
                activeProfile.updateLastUsed();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating game profile", e);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    @Override
    public void shutdown() {
        // Save profiles before shutting down
        saveProfiles();
        
        // Clear listeners
        listeners.clear();
        
        super.shutdown();
    }
    
    /**
     * Create a new game profile
     * @param packageName Game package name
     * @param displayName Display name for the game
     * @return The created profile
     */
    public GameProfile createProfile(String packageName, String displayName) {
        // Check if profile already exists
        if (profiles.containsKey(packageName)) {
            Log.w(TAG, "Profile already exists for " + packageName);
            return profiles.get(packageName);
        }
        
        // Create new profile with default settings
        GameProfile profile = new GameProfile(packageName, displayName);
        
        // Apply default settings
        for (Map.Entry<String, Object> entry : defaultSettings.entrySet()) {
            profile.setSetting(entry.getKey(), entry.getValue());
        }
        
        // Add to profiles map
        profiles.put(packageName, profile);
        
        Log.d(TAG, "Created new profile for " + displayName + " (" + packageName + ")");
        
        return profile;
    }
    
    /**
     * Get a profile by package name
     * @param packageName Game package name
     * @return The profile or null if not found
     */
    public GameProfile getProfile(String packageName) {
        return profiles.get(packageName);
    }
    
    /**
     * Set the active profile
     * @param packageName Game package name
     * @return true if profile was found and activated, false otherwise
     */
    public boolean setActiveProfile(String packageName) {
        GameProfile profile = profiles.get(packageName);
        if (profile != null) {
            GameProfile previousProfile = activeProfile;
            activeProfile = profile;
            
            // Update last used time
            profile.updateLastUsed();
            
            // Add to recent profiles
            addToRecentProfiles(packageName);
            
            Log.d(TAG, "Activated profile for " + profile.getDisplayName());
            
            // Notify listeners
            for (ProfileChangeListener listener : listeners) {
                listener.onProfileChanged(previousProfile, activeProfile);
            }
            
            return true;
        }
        
        Log.w(TAG, "Profile not found for " + packageName);
        return false;
    }
    
    /**
     * Get the active profile
     * @return Currently active profile or null if none
     */
    public GameProfile getActiveProfile() {
        return activeProfile;
    }
    
    /**
     * Delete a profile
     * @param packageName Game package name
     * @return true if profile was found and deleted, false otherwise
     */
    public boolean deleteProfile(String packageName) {
        GameProfile profile = profiles.remove(packageName);
        if (profile != null) {
            // If this was the active profile, clear it
            if (activeProfile != null && activeProfile.getPackageName().equals(packageName)) {
                activeProfile = null;
                
                // Notify listeners
                for (ProfileChangeListener listener : listeners) {
                    listener.onProfileChanged(profile, null);
                }
            }
            
            // Remove from recent profiles
            recentProfiles.remove(packageName);
            
            Log.d(TAG, "Deleted profile for " + profile.getDisplayName());
            return true;
        }
        
        return false;
    }
    
    /**
     * Get all profiles
     * @return Map of all profiles
     */
    public Map<String, GameProfile> getAllProfiles() {
        return new HashMap<>(profiles);
    }
    
    /**
     * Get a list of all profiles
     * @return List of all profiles
     */
    public List<GameProfile> getProfilesList() {
        return new ArrayList<>(profiles.values());
    }
    
    /**
     * Get recent profiles
     * @return List of recent profile package names
     */
    public List<String> getRecentProfiles() {
        return new ArrayList<>(recentProfiles);
    }
    
    /**
     * Get recent profiles as GameProfile objects
     * @return List of recent profiles
     */
    public List<GameProfile> getRecentProfilesList() {
        List<GameProfile> result = new ArrayList<>();
        for (String packageName : recentProfiles) {
            GameProfile profile = profiles.get(packageName);
            if (profile != null) {
                result.add(profile);
            }
        }
        return result;
    }
    
    /**
     * Add a profile change listener
     * @param listener Listener to add
     */
    public void addProfileChangeListener(ProfileChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a profile change listener
     * @param listener Listener to remove
     */
    public void removeProfileChangeListener(ProfileChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Import a profile from JSON
     * @param json JSON string representation of profile
     * @return The imported profile or null if import failed
     */
    public GameProfile importProfile(String json) {
        try {
            GameProfile profile = GameProfile.fromJson(json);
            if (profile != null) {
                profiles.put(profile.getPackageName(), profile);
                Log.d(TAG, "Imported profile for " + profile.getDisplayName());
                return profile;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to import profile", e);
        }
        return null;
    }
    
    /**
     * Export a profile to JSON
     * @param packageName Game package name
     * @return JSON string representation of profile or null if export failed
     */
    public String exportProfile(String packageName) {
        GameProfile profile = profiles.get(packageName);
        if (profile != null) {
            try {
                return profile.toJson();
            } catch (Exception e) {
                Log.e(TAG, "Failed to export profile", e);
            }
        }
        return null;
    }
    
    /**
     * Get a setting from the active profile
     * @param key Setting key
     * @param defaultValue Default value if setting not found
     * @return Setting value or default value if not found
     */
    public Object getSetting(String key, Object defaultValue) {
        if (activeProfile != null) {
            return activeProfile.getSetting(key, defaultValue);
        }
        return defaultValue;
    }
    
    /**
     * Set a setting in the active profile
     * @param key Setting key
     * @param value Setting value
     * @return true if setting was set, false if no active profile
     */
    public boolean setSetting(String key, Object value) {
        if (activeProfile != null) {
            activeProfile.setSetting(key, value);
            return true;
        }
        return false;
    }
    
    /**
     * Set up default settings
     */
    private void setupDefaultSettings() {
        // Default settings for all profiles
        defaultSettings.put("ai_assistance_level", 2); // Medium assistance
        defaultSettings.put("notification_enabled", true);
        defaultSettings.put("tactical_overlay_enabled", true);
        defaultSettings.put("combat_analysis_enabled", true);
        defaultSettings.put("security_level", 2); // Medium security
        defaultSettings.put("auto_detect_game_state", true);
        defaultSettings.put("performance_mode", "balanced");
    }
    
    /**
     * Load profiles from storage
     */
    private void loadProfiles() {
        // This would load from storage or database
        // For now, just create some sample profiles
        createSampleProfiles();
    }
    
    /**
     * Save profiles to storage
     */
    private void saveProfiles() {
        // This would save to storage or database
        Log.d(TAG, "Saving " + profiles.size() + " profiles");
    }
    
    /**
     * Create sample profiles for testing
     */
    private void createSampleProfiles() {
        // Sample FPS game profile
        GameProfile fpsProfile = createProfile(
            "com.example.fps", "Example FPS Game");
        fpsProfile.setSetting("aim_assistance", true);
        fpsProfile.setSetting("recoil_control", 0.7f);
        fpsProfile.setSetting("tactical_overlay_mode", "combat");
        
        // Sample RPG game profile
        GameProfile rpgProfile = createProfile(
            "com.example.rpg", "Example RPG Game");
        rpgProfile.setSetting("combat_optimization", true);
        rpgProfile.setSetting("resource_tracking", true);
        rpgProfile.setSetting("tactical_overlay_mode", "exploration");
        
        // Sample strategy game profile
        GameProfile strategyProfile = createProfile(
            "com.example.strategy", "Example Strategy Game");
        strategyProfile.setSetting("ai_assistance_level", 3); // High assistance
        strategyProfile.setSetting("resource_optimization", true);
        strategyProfile.setSetting("tactical_overlay_mode", "detailed");
    }
    
    /**
     * Add a profile to recent profiles list
     * @param packageName Game package name
     */
    private void addToRecentProfiles(String packageName) {
        // Remove if already in list
        recentProfiles.remove(packageName);
        
        // Add to front of list
        recentProfiles.add(0, packageName);
        
        // Trim list if too long
        while (recentProfiles.size() > MAX_RECENT_PROFILES) {
            recentProfiles.remove(recentProfiles.size() - 1);
        }
    }
    
    /**
     * Profile Change Listener interface
     * For receiving profile change events
     */
    public interface ProfileChangeListener {
        /**
         * Called when the active profile changes
         * @param oldProfile Previous profile (may be null)
         * @param newProfile New profile (may be null)
         */
        void onProfileChanged(GameProfile oldProfile, GameProfile newProfile);
    }
}
