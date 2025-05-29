package com.aiassistant.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

/**
 * System responsible for collecting, processing, and responding to feedback
 * from both user actions and AI performance metrics
 */
public class FeedbackSystem {
    private static final String TAG = "FeedbackSystem";
    
    // Context for accessing resources and preferences
    private Context context;
    
    // Handler for posting UI updates
    private Handler mainHandler;
    
    // Feedback metrics
    private int successfulInteractions = 0;
    private int failedInteractions = 0;
    private long totalInteractionTime = 0;
    private long startTimestamp = 0;
    
    // System state
    private boolean isRunning = false;
    
    /**
     * Constructor for the feedback system
     */
    public FeedbackSystem() {
        mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Initialize the feedback system with application context
     * @param context The application context
     */
    public void initialize(Context context) {
        this.context = context;
        
        // Additional initialization if needed
        
        Log.i(TAG, "FeedbackSystem initialized");
    }
    
    /**
     * Start the feedback system
     */
    public void start() {
        if (isRunning) {
            Log.w(TAG, "FeedbackSystem already running");
            return;
        }
        
        startTimestamp = System.currentTimeMillis();
        isRunning = true;
        
        Log.i(TAG, "FeedbackSystem started");
    }
    
    /**
     * Stop the feedback system
     */
    public void stop() {
        if (!isRunning) {
            Log.w(TAG, "FeedbackSystem not running");
            return;
        }
        
        // Record final metrics
        totalInteractionTime += (System.currentTimeMillis() - startTimestamp);
        
        isRunning = false;
        Log.i(TAG, "FeedbackSystem stopped");
    }
    
    /**
     * Record a successful interaction
     * @param interactionType The type of interaction that succeeded
     * @param details Additional details about the interaction
     */
    public void recordSuccess(String interactionType, String details) {
        if (!isRunning) {
            Log.w(TAG, "Cannot record: FeedbackSystem not running");
            return;
        }
        
        successfulInteractions++;
        
        // Store data for later analysis
        Log.d(TAG, "Successful interaction: " + interactionType + " - " + details);
    }
    
    /**
     * Record a failed interaction
     * @param interactionType The type of interaction that failed
     * @param reason The reason for the failure
     * @param details Additional details about the interaction
     */
    public void recordFailure(String interactionType, String reason, String details) {
        if (!isRunning) {
            Log.w(TAG, "Cannot record: FeedbackSystem not running");
            return;
        }
        
        failedInteractions++;
        
        // Store data for later analysis
        Log.w(TAG, "Failed interaction: " + interactionType + " - " + reason + " - " + details);
    }
    
    /**
     * Show a feedback message to the user
     * @param message The message to display
     */
    public void showUserFeedback(final String message) {
        mainHandler.post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * Process all feedback data and generate insights
     * @return A summary of feedback insights
     */
    public String generateFeedbackInsights() {
        if (successfulInteractions + failedInteractions == 0) {
            return "No interactions recorded yet";
        }
        
        float successRate = (float) successfulInteractions / 
                            (successfulInteractions + failedInteractions) * 100;
        
        return String.format(
            "Success rate: %.1f%% (%d/%d)\nTotal interaction time: %d seconds",
            successRate,
            successfulInteractions,
            (successfulInteractions + failedInteractions),
            totalInteractionTime / 1000
        );
    }
    
    /**
     * Get the number of successful interactions
     * @return Number of successful interactions
     */
    public int getSuccessfulInteractions() {
        return successfulInteractions;
    }
    
    /**
     * Get the number of failed interactions
     * @return Number of failed interactions
     */
    public int getFailedInteractions() {
        return failedInteractions;
    }
}