package com.aiassistant.core.ai.neural;

import android.content.Context;

import java.io.Closeable;

/**
 * Base interface for all TensorFlow Lite models in the application.
 * Defines the common operations for model initialization, inference, and resource management.
 */
public interface TFLiteModel extends Closeable {
    
    /**
     * Initialize the model with required resources
     * @param context Application context for accessing resources
     * @return true if initialization was successful
     */
    boolean initialize(Context context);
    
    /**
     * Get the name of the model
     * @return Model name
     */
    String getModelName();
    
    /**
     * Get the version of the model
     * @return Model version
     */
    String getModelVersion();
    
    /**
     * Check if the model is ready for inference
     * @return true if model is ready
     */
    boolean isReady();
    
    /**
     * Get a description of the model's purpose and capabilities
     * @return Model description
     */
    String getModelDescription();
    
    /**
     * Release all resources associated with the model
     */
    @Override
    void close();
}
