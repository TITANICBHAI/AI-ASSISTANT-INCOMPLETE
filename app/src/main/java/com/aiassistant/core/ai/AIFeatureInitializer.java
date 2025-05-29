package com.aiassistant.core.ai;

import android.content.Context;
import android.util.Log;

import com.aiassistant.ai.features.FeatureManager;
import com.aiassistant.ai.features.patterns.PatternRecognitionFeature;
import com.aiassistant.ai.features.security.EnhancedSecurityFeature;
import com.aiassistant.ai.features.learning.MetaLearningFeature;

/**
 * Initializes and configures all AI features
 */
public class AIFeatureInitializer {
    private static final String TAG = "AIFeatureInitializer";
    
    private final Context context;
    private final FeatureManager featureManager;
    
    /**
     * Constructor
     * @param context Application context
     */
    public AIFeatureInitializer(Context context) {
        this.context = context.getApplicationContext();
        this.featureManager = FeatureManager.getInstance(context);
    }
    
    /**
     * Initialize all AI features
     */
    public void initializeFeatures() {
        Log.d(TAG, "Initializing AI features");
        
        try {
            // Register features with the manager
            registerFeatures();
            
            // Initialize all registered features
            featureManager.initializeFeatures();
            
            Log.d(TAG, "All AI features initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing AI features", e);
        }
    }
    
    /**
     * Register all features with the feature manager
     */
    private void registerFeatures() {
        // Pattern Recognition
        PatternRecognitionFeature patternRecognition = new PatternRecognitionFeature(context);
        featureManager.registerFeature(patternRecognition);
        
        // Enhanced Security
        EnhancedSecurityFeature enhancedSecurity = new EnhancedSecurityFeature(context);
        featureManager.registerFeature(enhancedSecurity);
        
        // Meta-Learning System
        MetaLearningFeature metaLearning = new MetaLearningFeature(context);
        featureManager.registerFeature(metaLearning);
        
        // Additional features would be registered here
        
        Log.d(TAG, "All features registered successfully");
    }
    
    /**
     * Enable all features
     */
    public void enableAllFeatures() {
        featureManager.getAllFeatures().forEach(feature -> feature.setEnabled(true));
    }
    
    /**
     * Disable all features
     */
    public void disableAllFeatures() {
        featureManager.getAllFeatures().forEach(feature -> feature.setEnabled(false));
    }
    
    /**
     * Get the feature manager
     * @return Feature manager instance
     */
    public FeatureManager getFeatureManager() {
        return featureManager;
    }
}
