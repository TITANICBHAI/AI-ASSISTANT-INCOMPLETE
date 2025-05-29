package com.aiassistant.security;

import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Security context that manages feature permissions and access control
 * Used to ensure that features operate within their intended security boundaries
 */
public class SecurityContext {
    private static final String TAG = "SecurityContext";
    
    // Singleton instance
    private static SecurityContext instance;
    
    // Thread local storage for current feature
    private final ThreadLocal<String> currentFeature = new ThreadLocal<>();
    
    // Feature permissions map (what each feature can access)
    private final Map<String, Set<String>> featurePermissions = new HashMap<>();
    
    // Lock for thread-safe updates
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    // Feature activation tracking
    private final Map<String, Long> featureActivationTimes = new HashMap<>();
    private final Map<String, Integer> featureUsageCounts = new HashMap<>();
    
    /**
     * Private constructor
     */
    private SecurityContext() {
        // Initialize default permissions
        initializeDefaultPermissions();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized SecurityContext getInstance() {
        if (instance == null) {
            instance = new SecurityContext();
        }
        return instance;
    }
    
    /**
     * Initialize default feature permissions
     */
    private void initializeDefaultPermissions() {
        // Game interaction permissions
        Set<String> gameInteractionPermissions = new HashSet<>();
        gameInteractionPermissions.add("accessibility_service");
        gameInteractionPermissions.add("screen_capture");
        gameInteractionPermissions.add("input_gestures");
        featurePermissions.put("game_interaction", gameInteractionPermissions);
        
        // Game analysis permissions
        Set<String> gameAnalysisPermissions = new HashSet<>();
        gameAnalysisPermissions.add("screen_capture");
        gameAnalysisPermissions.add("ml_inference");
        gameAnalysisPermissions.add("data_collection");
        featurePermissions.put("game_analysis", gameAnalysisPermissions);
        
        // Voice command permissions
        Set<String> voiceCommandPermissions = new HashSet<>();
        voiceCommandPermissions.add("microphone");
        voiceCommandPermissions.add("speech_recognition");
        voiceCommandPermissions.add("ml_inference");
        featurePermissions.put("voice_command", voiceCommandPermissions);
        
        // Speech synthesis permissions
        Set<String> speechSynthesisPermissions = new HashSet<>();
        speechSynthesisPermissions.add("audio_output");
        speechSynthesisPermissions.add("ml_inference");
        featurePermissions.put("speech_synthesis", speechSynthesisPermissions);
        
        // PDF learning permissions
        Set<String> pdfLearningPermissions = new HashSet<>();
        pdfLearningPermissions.add("file_access");
        pdfLearningPermissions.add("ml_inference");
        pdfLearningPermissions.add("data_collection");
        featurePermissions.put("pdf_learning", pdfLearningPermissions);
        
        // Business negotiation permissions
        Set<String> businessNegotiationPermissions = new HashSet<>();
        businessNegotiationPermissions.add("audio_output");
        businessNegotiationPermissions.add("microphone");
        businessNegotiationPermissions.add("speech_recognition");
        businessNegotiationPermissions.add("ml_inference");
        businessNegotiationPermissions.add("network_access");
        featurePermissions.put("business_negotiation", businessNegotiationPermissions);
        
        // Telephony permissions
        Set<String> telephonyPermissions = new HashSet<>();
        telephonyPermissions.add("phone_call");
        telephonyPermissions.add("audio_output");
        telephonyPermissions.add("microphone");
        telephonyPermissions.add("speech_recognition");
        telephonyPermissions.add("ml_inference");
        featurePermissions.put("telephony", telephonyPermissions);
    }
    
    /**
     * Set the current active feature for this thread
     * This should be called at the start of a feature's operation
     * @param featureName Name of the feature
     */
    public void setCurrentFeatureActive(String featureName) {
        if (featureName == null) {
            currentFeature.remove();
            return;
        }
        
        // Track feature usage
        lock.writeLock().lock();
        try {
            featureActivationTimes.put(featureName, System.currentTimeMillis());
            featureUsageCounts.put(featureName, 
                    featureUsageCounts.getOrDefault(featureName, 0) + 1);
            Log.d(TAG, "Feature activated: " + featureName + ", usage count: " + 
                    featureUsageCounts.get(featureName));
        } finally {
            lock.writeLock().unlock();
        }
        
        // Set current feature
        currentFeature.set(featureName);
    }
    
    /**
     * Clear the current active feature for this thread
     * This should be called at the end of a feature's operation
     */
    public void clearCurrentFeatureActive() {
        currentFeature.remove();
    }
    
    /**
     * Get the current active feature for this thread
     * @return Feature name or null if none is active
     */
    public String getCurrentFeature() {
        return currentFeature.get();
    }
    
    /**
     * Check if a feature has a specific permission
     * @param featureName Name of the feature
     * @param permission Permission to check
     * @return True if the feature has the permission
     */
    public boolean hasPermission(String featureName, String permission) {
        if (featureName == null || permission == null) {
            return false;
        }
        
        lock.readLock().lock();
        try {
            Set<String> permissions = featurePermissions.get(featureName);
            return permissions != null && permissions.contains(permission);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Check if the current active feature has a specific permission
     * @param permission Permission to check
     * @return True if the current feature has the permission
     */
    public boolean currentFeatureHasPermission(String permission) {
        String feature = currentFeature.get();
        return feature != null && hasPermission(feature, permission);
    }
    
    /**
     * Add a permission to a feature
     * @param featureName Name of the feature
     * @param permission Permission to add
     */
    public void addPermission(String featureName, String permission) {
        if (featureName == null || permission == null) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            Set<String> permissions = featurePermissions.computeIfAbsent(
                    featureName, k -> new HashSet<>());
            permissions.add(permission);
            Log.d(TAG, "Added permission: " + permission + " to feature: " + featureName);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Remove a permission from a feature
     * @param featureName Name of the feature
     * @param permission Permission to remove
     */
    public void removePermission(String featureName, String permission) {
        if (featureName == null || permission == null) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            Set<String> permissions = featurePermissions.get(featureName);
            if (permissions != null) {
                permissions.remove(permission);
                Log.d(TAG, "Removed permission: " + permission + " from feature: " + featureName);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Get all permissions for a feature
     * @param featureName Name of the feature
     * @return Set of permissions or empty set if none
     */
    public Set<String> getFeaturePermissions(String featureName) {
        if (featureName == null) {
            return new HashSet<>();
        }
        
        lock.readLock().lock();
        try {
            Set<String> permissions = featurePermissions.get(featureName);
            return permissions != null ? new HashSet<>(permissions) : new HashSet<>();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get the time when a feature was last activated
     * @param featureName Name of the feature
     * @return Timestamp of last activation or 0 if never activated
     */
    public long getFeatureLastActivationTime(String featureName) {
        if (featureName == null) {
            return 0;
        }
        
        lock.readLock().lock();
        try {
            return featureActivationTimes.getOrDefault(featureName, 0L);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Get the number of times a feature has been used
     * @param featureName Name of the feature
     * @return Usage count or 0 if never used
     */
    public int getFeatureUsageCount(String featureName) {
        if (featureName == null) {
            return 0;
        }
        
        lock.readLock().lock();
        try {
            return featureUsageCounts.getOrDefault(featureName, 0);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Reset usage statistics for all features
     */
    public void resetUsageStats() {
        lock.writeLock().lock();
        try {
            featureActivationTimes.clear();
            featureUsageCounts.clear();
            Log.d(TAG, "Reset all feature usage statistics");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
