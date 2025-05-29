package com.aiassistant.ui.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aiassistant.data.models.AISettings;
import com.aiassistant.learning.ReinforcementLearner;
import com.aiassistant.utils.AccessibilityUtils;

/**
 * ViewModel for the Settings screen which manages AI configuration.
 */
public class SettingsViewModel extends AndroidViewModel {
    
    private static final String PREFS_NAME = "ai_assistant_prefs";
    
    private final MutableLiveData<AISettings> settings = new MutableLiveData<>();
    private final MutableLiveData<Boolean> accessibilityServiceEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<String> settingsStatusMessage = new MutableLiveData<>("");
    private final MutableLiveData<ReinforcementLearner.Algorithm> selectedAlgorithm = new MutableLiveData<>();
    
    /**
     * Constructor
     */
    public SettingsViewModel(@NonNull Application application) {
        super(application);
        
        // Load settings
        loadSettings();
        
        // Check accessibility service status
        updateAccessibilityStatus();
        
        updateStatusMessage();
    }
    
    /**
     * Get observable for AI settings
     */
    public LiveData<AISettings> getSettings() {
        return settings;
    }
    
    /**
     * Get observable for accessibility service status
     */
    public LiveData<Boolean> getAccessibilityServiceEnabled() {
        return accessibilityServiceEnabled;
    }
    
    /**
     * Get observable for settings status message
     */
    public LiveData<String> getSettingsStatusMessage() {
        return settingsStatusMessage;
    }
    
    /**
     * Get observable for selected algorithm
     */
    public LiveData<ReinforcementLearner.Algorithm> getSelectedAlgorithm() {
        return selectedAlgorithm;
    }
    
    /**
     * Load settings from persistent storage
     */
    private void loadSettings() {
        // In a real app, this would load from database or preferences
        
        // For now, create default settings
        AISettings aiSettings = new AISettings();
        
        // Set default algorithm to match the ViewModel state
        selectedAlgorithm.setValue(ReinforcementLearner.Algorithm.PPO);
        aiSettings.setPreferredAlgorithm("PPO");
        
        settings.setValue(aiSettings);
    }
    
    /**
     * Save settings to persistent storage
     */
    public void saveSettings(AISettings updatedSettings) {
        // In a real app, this would save to database or preferences
        settings.setValue(updatedSettings);
        
        // Also update algorithm if it changed
        if (updatedSettings != null) {
            try {
                ReinforcementLearner.Algorithm algorithm = 
                        ReinforcementLearner.Algorithm.valueOf(updatedSettings.getPreferredAlgorithm());
                selectedAlgorithm.setValue(algorithm);
            } catch (IllegalArgumentException e) {
                // Invalid algorithm name, keep current
            }
        }
        
        updateStatusMessage();
    }
    
    /**
     * Update accessibility service status
     */
    public void updateAccessibilityStatus() {
        // Check if accessibility service is enabled
        boolean isEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(
                getApplication().getApplicationContext(),
                null); // Would use actual service class in real implementation
        
        accessibilityServiceEnabled.setValue(isEnabled);
        updateStatusMessage();
    }
    
    /**
     * Set learning algorithm
     */
    public void setLearningAlgorithm(ReinforcementLearner.Algorithm algorithm) {
        selectedAlgorithm.setValue(algorithm);
        
        // Update settings
        AISettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.setPreferredAlgorithm(algorithm.name());
            settings.setValue(currentSettings);
        }
        
        updateStatusMessage();
    }
    
    /**
     * Save a setting value
     */
    public void saveSetting(String key, String value) {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }
    
    /**
     * Save a setting value (boolean)
     */
    public void saveSetting(String key, boolean value) {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    
    /**
     * Save a setting value (int)
     */
    public void saveSetting(String key, int value) {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.apply();
    }
    
    /**
     * Save a setting value (float)
     */
    public void saveSetting(String key, float value) {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(key, value);
        editor.apply();
    }
    
    /**
     * Update status message based on current state
     */
    private void updateStatusMessage() {
        StringBuilder message = new StringBuilder();
        
        // Check accessibility service
        if (!accessibilityServiceEnabled.getValue()) {
            message.append("⚠️ Accessibility service is disabled. Enable it in Android settings for full functionality.\n\n");
        }
        
        // Show current settings summary
        AISettings aiSettings = settings.getValue();
        if (aiSettings != null) {
            message.append("Current settings:\n");
            
            message.append("• Auto mode: ").append(aiSettings.isAutoModeEnabled() ? "Enabled" : "Disabled").append("\n");
            
            if (aiSettings.isAutoModeEnabled()) {
                message.append("• Auto activation after: ").append(aiSettings.getInactivityThresholdSeconds()).append(" seconds\n");
            }
            
            message.append("• Learning algorithm: ").append(aiSettings.getPreferredAlgorithm()).append("\n");
            message.append("• Learning rate: ").append(aiSettings.getLearningRate()).append("\n");
            message.append("• Bypass security: ").append(aiSettings.isBypassSecurityEnabled() ? "Enabled" : "Disabled").append("\n");
            message.append("• Permission level: ").append(aiSettings.getAccessPermissionLevel());
        }
        
        settingsStatusMessage.setValue(message.toString());
    }
    
    /**
     * Reset settings to defaults (maximum permissions and capabilities)
     */
    public void resetToDefaults() {
        AISettings defaultSettings = new AISettings();
        settings.setValue(defaultSettings);
        selectedAlgorithm.setValue(ReinforcementLearner.Algorithm.valueOf(defaultSettings.getPreferredAlgorithm()));
        updateStatusMessage();
    }
    
    /**
     * Update inactivity threshold setting
     */
    public void setInactivityThreshold(int seconds) {
        AISettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.setInactivityThresholdSeconds(seconds);
            settings.setValue(currentSettings);
            updateStatusMessage();
        }
    }
    
    /**
     * Update auto mode enabled setting
     */
    public void setAutoModeEnabled(boolean enabled) {
        AISettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.setAutoModeEnabled(enabled);
            settings.setValue(currentSettings);
            updateStatusMessage();
        }
    }
    
    /**
     * Update bypass security setting
     */
    public void setBypassSecurityEnabled(boolean enabled) {
        AISettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.setBypassSecurityEnabled(enabled);
            settings.setValue(currentSettings);
            updateStatusMessage();
        }
    }
    
    /**
     * Set permission level
     */
    public void setPermissionLevel(String level) {
        AISettings currentSettings = settings.getValue();
        if (currentSettings != null) {
            currentSettings.setAccessPermissionLevel(level);
            settings.setValue(currentSettings);
            updateStatusMessage();
        }
    }
}