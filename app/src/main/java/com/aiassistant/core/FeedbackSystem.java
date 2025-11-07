package com.aiassistant.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    // Enhanced metrics for adaptive scheduling
    private final Map<String, ComponentQualityMetrics> componentMetrics;
    private final List<PerformanceRecord> performanceHistory;
    
    // System state
    private boolean isRunning = false;
    
    /**
     * Constructor for the feedback system
     */
    public FeedbackSystem() {
        mainHandler = new Handler(Looper.getMainLooper());
        componentMetrics = new HashMap<>();
        performanceHistory = new ArrayList<>();
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
    
    public void recordComponentExecution(String componentId, long latencyMs, 
                                        float confidenceScore, boolean success) {
        ComponentQualityMetrics metrics = componentMetrics.computeIfAbsent(
            componentId,
            k -> new ComponentQualityMetrics(componentId)
        );
        
        metrics.recordExecution(latencyMs, confidenceScore, success);
        
        PerformanceRecord record = new PerformanceRecord(
            componentId,
            latencyMs,
            confidenceScore,
            success,
            System.currentTimeMillis()
        );
        performanceHistory.add(record);
        
        if (performanceHistory.size() > 1000) {
            performanceHistory.remove(0);
        }
        
        Log.d(TAG, "Component " + componentId + " execution recorded: " + 
              (success ? "success" : "failure") + 
              ", latency: " + latencyMs + "ms, confidence: " + confidenceScore);
    }
    
    public ComponentQualityMetrics getComponentMetrics(String componentId) {
        return componentMetrics.get(componentId);
    }
    
    public Map<String, ComponentQualityMetrics> getAllComponentMetrics() {
        return new HashMap<>(componentMetrics);
    }
    
    public float getComponentQualityScore(String componentId) {
        ComponentQualityMetrics metrics = componentMetrics.get(componentId);
        if (metrics == null) {
            return 0.5f;
        }
        return metrics.getQualityScore();
    }
    
    public boolean shouldDeprioritize(String componentId) {
        float qualityScore = getComponentQualityScore(componentId);
        return qualityScore < 0.4f;
    }
    
    public static class ComponentQualityMetrics {
        public final String componentId;
        public int totalExecutions;
        public int successfulExecutions;
        public long totalLatency;
        public float averageLatency;
        public float averageConfidence;
        public float successRate;
        
        private long minLatency = Long.MAX_VALUE;
        private long maxLatency = 0;
        private float totalConfidence = 0;
        
        public ComponentQualityMetrics(String componentId) {
            this.componentId = componentId;
        }
        
        public void recordExecution(long latencyMs, float confidenceScore, boolean success) {
            totalExecutions++;
            if (success) {
                successfulExecutions++;
            }
            
            totalLatency += latencyMs;
            minLatency = Math.min(minLatency, latencyMs);
            maxLatency = Math.max(maxLatency, latencyMs);
            averageLatency = (float) totalLatency / totalExecutions;
            
            totalConfidence += confidenceScore;
            averageConfidence = totalConfidence / totalExecutions;
            
            successRate = (float) successfulExecutions / totalExecutions;
        }
        
        public float getQualityScore() {
            float latencyScore = calculateLatencyScore();
            float confidenceScore = averageConfidence;
            
            return (successRate * 0.5f) + (confidenceScore * 0.3f) + (latencyScore * 0.2f);
        }
        
        private float calculateLatencyScore() {
            if (averageLatency < 100) return 1.0f;
            if (averageLatency < 500) return 0.8f;
            if (averageLatency < 1000) return 0.6f;
            if (averageLatency < 2000) return 0.4f;
            return 0.2f;
        }
    }
    
    public static class PerformanceRecord {
        public final String componentId;
        public final long latencyMs;
        public final float confidenceScore;
        public final boolean success;
        public final long timestamp;
        
        public PerformanceRecord(String componentId, long latencyMs, 
                                float confidenceScore, boolean success, long timestamp) {
            this.componentId = componentId;
            this.latencyMs = latencyMs;
            this.confidenceScore = confidenceScore;
            this.success = success;
            this.timestamp = timestamp;
        }
    }
}