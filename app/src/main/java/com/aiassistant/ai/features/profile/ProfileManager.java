package com.aiassistant.ai.features.profile;

import android.content.Context;
import android.util.Log;

import com.aiassistant.ai.features.AIFeature;
import com.aiassistant.ai.features.FeatureManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Profile Manager
 * Provides simplified interface for profile management
 */
public class ProfileManager implements GameProfileFeature.ProfileChangeListener {
    private static final String TAG = "ProfileManager";
    
    private final Context context;
    private final GameProfileFeature profileFeature;
    private final FeatureManager featureManager;
    private final List<ProfileEventListener> listeners;
    
    /**
     * Constructor
     * @param context Application context
     * @param profileFeature Game profile feature
     * @param featureManager Feature manager
     */
    public ProfileManager(Context context, GameProfileFeature profileFeature, 
                         FeatureManager featureManager) {
        this.context = context;
        this.profileFeature = profileFeature;
        this.featureManager = featureManager;
        this.listeners = new ArrayList<>();
        
        // Register as profile change listener
        profileFeature.addProfileChangeListener(this);
    }
    
    /**
     * Create a new game profile
     * @param packageName Game package name
     * @param displayName Display name
     * @return Created profile
     */
    public GameProfile createProfile(String packageName, String displayName) {
        GameProfile profile = profileFeature.createProfile(packageName, displayName);
        
        // Notify listeners
        for (ProfileEventListener listener : listeners) {
            listener.onProfileCreated(profile);
        }
        
        return profile;
    }
    
    /**
     * Switch to a profile
     * @param packageName Game package name
     * @return true if switch successful
     */
    public boolean switchToProfile(String packageName) {
        boolean success = profileFeature.setActiveProfile(packageName);
        
        if (success) {
            // Apply profile settings to features
            applyProfileSettings();
            
            Log.d(TAG, "Switched to profile: " + packageName);
        }
        
        return success;
    }
    
    /**
     * Get active profile
     * @return Active profile or null if none
     */
    public GameProfile getActiveProfile() {
        return profileFeature.getActiveProfile();
    }
    
    /**
     * Get all profiles
     * @return List of all profiles
     */
    public List<GameProfile> getAllProfiles() {
        return profileFeature.getProfilesList();
    }
    
    /**
     * Get recent profiles
     * @return List of recent profiles
     */
    public List<GameProfile> getRecentProfiles() {
        return profileFeature.getRecentProfilesList();
    }
    
    /**
     * Delete a profile
     * @param packageName Game package name
     * @return true if deletion successful
     */
    public boolean deleteProfile(String packageName) {
        boolean success = profileFeature.deleteProfile(packageName);
        
        if (success) {
            // Notify listeners
            for (ProfileEventListener listener : listeners) {
                listener.onProfileDeleted(packageName);
            }
        }
        
        return success;
    }
    
    /**
     * Update profile settings
     * @param profile Profile to update
     * @param settings Map of settings to update
     */
    public void updateProfileSettings(GameProfile profile, 
                                     java.util.Map<String, Object> settings) {
        if (profile == null) return;
        
        // Update settings
        for (java.util.Map.Entry<String, Object> entry : settings.entrySet()) {
            profile.setSetting(entry.getKey(), entry.getValue());
        }
        
        // If this is the active profile, apply settings
        if (profile.equals(profileFeature.getActiveProfile())) {
            applyProfileSettings();
        }
        
        // Notify listeners
        for (ProfileEventListener listener : listeners) {
            listener.onProfileUpdated(profile);
        }
    }
    
    /**
     * Enable or disable a feature for a profile
     * @param profile Profile to update
     * @param featureId Feature ID
     * @param enabled true to enable, false to disable
     */
    public void setFeatureEnabled(GameProfile profile, String featureId, boolean enabled) {
        if (profile == null) return;
        
        // Update feature state in profile
        profile.setFeatureEnabled(featureId, enabled);
        
        // If this is the active profile, apply the change
        if (profile.equals(profileFeature.getActiveProfile())) {
            AIFeature feature = featureManager.getFeature(featureId);
            if (feature != null) {
                feature.setEnabled(enabled);
                Log.d(TAG, "Set feature " + featureId + " to " + 
                      (enabled ? "enabled" : "disabled"));
            }
        }
    }
    
    /**
     * Apply active profile settings to all features
     */
    private void applyProfileSettings() {
        GameProfile activeProfile = profileFeature.getActiveProfile();
        if (activeProfile == null) return;
        
        // Get all features
        List<AIFeature> allFeatures = featureManager.getAllFeatures();
        
        // Apply feature states from profile
        for (AIFeature feature : allFeatures) {
            String featureId = feature.getName();
            boolean enabled = activeProfile.isFeatureEnabled(featureId, feature.isEnabled());
            feature.setEnabled(enabled);
            
            Log.d(TAG, "Applied profile setting: " + featureId + " = " + 
                  (enabled ? "enabled" : "disabled"));
        }
        
        // Notify listeners
        for (ProfileEventListener listener : listeners) {
            listener.onProfileSettingsApplied(activeProfile);
        }
    }
    
    /**
     * Add a profile event listener
     * @param listener Listener to add
     */
    public void addProfileEventListener(ProfileEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a profile event listener
     * @param listener Listener to remove
     */
    public void removeProfileEventListener(ProfileEventListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void onProfileChanged(GameProfile oldProfile, GameProfile newProfile) {
        // Profile has been changed by the feature, apply settings
        applyProfileSettings();
        
        // Notify listeners
        for (ProfileEventListener listener : listeners) {
            listener.onActiveProfileChanged(oldProfile, newProfile);
        }
    }
    
    /**
     * Profile Event Listener interface
     * For receiving profile events
     */
    public interface ProfileEventListener {
        /**
         * Called when a profile is created
         * @param profile Created profile
         */
        void onProfileCreated(GameProfile profile);
        
        /**
         * Called when a profile is deleted
         * @param packageName Package name of deleted profile
         */
        void onProfileDeleted(String packageName);
        
        /**
         * Called when a profile is updated
         * @param profile Updated profile
         */
        void onProfileUpdated(GameProfile profile);
        
        /**
         * Called when the active profile changes
         * @param oldProfile Previous profile (may be null)
         * @param newProfile New profile (may be null)
         */
        void onActiveProfileChanged(GameProfile oldProfile, GameProfile newProfile);
        
        /**
         * Called when profile settings are applied to features
         * @param profile Profile whose settings were applied
         */
        void onProfileSettingsApplied(GameProfile profile);
    }
}
