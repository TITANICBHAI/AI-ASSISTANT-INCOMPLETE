package com.aiassistant.core.feedback;

import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;

/**
 * Manages automatic AI feedback
 */
public class AutoAIFeedbackManager {
    private static final String TAG = "AutoAIFeedbackManager";
    
    private final AIStateManager stateManager;
    private boolean isRunning = false;
    
    /**
     * Constructor
     * @param stateManager State manager
     */
    public AutoAIFeedbackManager(AIStateManager stateManager) {
        this.stateManager = stateManager;
    }
    
    /**
     * Start manager
     */
    public void start() {
        if (isRunning) {
            return;
        }
        
        isRunning = true;
        Log.d(TAG, "Starting AutoAIFeedbackManager");
    }
    
    /**
     * Stop manager
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        Log.d(TAG, "Stopping AutoAIFeedbackManager");
    }
}
