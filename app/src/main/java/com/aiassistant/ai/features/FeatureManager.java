package com.aiassistant.ai.features;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all AI features in the application
 * Handles initialization, updates, and shutdown of features
 */
public class FeatureManager {
    private static final String TAG = "FeatureManager";
    private static FeatureManager instance;
    
    private final Map<String, AIFeature> features;
    private final Context context;
    
    private FeatureManager(Context context) {
        this.context = context.getApplicationContext();
        this.features = new HashMap<>();
    }
    
    /**
     * Get the singleton instance of FeatureManager
     * @param context Application context
     * @return FeatureManager instance
     */
    public static synchronized FeatureManager getInstance(Context context) {
        if (instance == null) {
            instance = new FeatureManager(context);
        }
        return instance;
    }
    
    /**
     * Register a feature with the manager
     * @param feature The feature to register
     */
    public void registerFeature(AIFeature feature) {
        if (feature != null) {
            features.put(feature.getName(), feature);
            Log.d(TAG, "Registered feature: " + feature.getName());
        }
    }
    
    /**
     * Initialize all registered features
     */
    public void initializeFeatures() {
        for (AIFeature feature : features.values()) {
            try {
                boolean success = feature.initialize();
                Log.d(TAG, "Feature " + feature.getName() + 
                      " initialization " + (success ? "successful" : "failed"));
            } catch (Exception e) {
                Log.e(TAG, "Error initializing feature " + feature.getName(), e);
            }
        }
    }
    
    /**
     * Update all enabled features
     */
    public void updateFeatures() {
        for (AIFeature feature : features.values()) {
            if (feature.isEnabled()) {
                try {
                    feature.update();
                } catch (Exception e) {
                    Log.e(TAG, "Error updating feature " + feature.getName(), e);
                }
            }
        }
    }
    
    /**
     * Shutdown all features
     */
    public void shutdownFeatures() {
        for (AIFeature feature : features.values()) {
            try {
                feature.shutdown();
                Log.d(TAG, "Feature " + feature.getName() + " shutdown");
            } catch (Exception e) {
                Log.e(TAG, "Error shutting down feature " + feature.getName(), e);
            }
        }
        features.clear();
    }
    
    /**
     * Get a specific feature by name
     * @param featureName Name of the feature
     * @return The feature, or null if not found
     */
    public AIFeature getFeature(String featureName) {
        return features.get(featureName);
    }
    
    /**
     * Get all registered features
     * @return List of all features
     */
    public List<AIFeature> getAllFeatures() {
        return new ArrayList<>(features.values());
    }
    
    /**
     * Get all enabled features
     * @return List of enabled features
     */
    public List<AIFeature> getEnabledFeatures() {
        List<AIFeature> enabledFeatures = new ArrayList<>();
        for (AIFeature feature : features.values()) {
            if (feature.isEnabled()) {
                enabledFeatures.add(feature);
            }
        }
        return enabledFeatures;
    }
    
    /**
     * Get application context
     * @return Application context
     */
    public Context getContext() {
        return context;
    }
}
