package com.aiassistant.ai.features;

/**
 * Base interface for all AI features
 * All feature implementations should implement this interface
 */
public interface AIFeature {
    /**
     * Initialize the feature
     * @return true if initialization was successful
     */
    boolean initialize();
    
    /**
     * Update the feature with latest data
     */
    void update();
    
    /**
     * Clean up resources used by this feature
     */
    void shutdown();
    
    /**
     * Get the name of this feature
     * @return feature name
     */
    String getName();
    
    /**
     * Get the current status of this feature
     * @return true if feature is enabled
     */
    boolean isEnabled();
    
    /**
     * Enable or disable this feature
     * @param enabled true to enable, false to disable
     */
    void setEnabled(boolean enabled);
}
