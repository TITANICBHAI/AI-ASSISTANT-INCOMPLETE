package com.aiassistant.core.ai;

import android.content.Context;
import android.util.Log;

import com.aiassistant.services.GroqApiService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Hybrid AI Learning System
 * 
 * This system enables the AI to learn from Groq API responses while building
 * its own independent capabilities over time.
 * 
 * Key Features:
 * - Uses local AI models when confidence is high
 * - Falls back to Groq when uncertain
 * - Learns from Groq responses to improve local models
 * - Tracks learning progress toward independence
 */
public class HybridAILearningSystem {
    private static final String TAG = "HybridAILearning";
    
    private static HybridAILearningSystem instance;
    private final Context context;
    private final GroqApiService groqApiService;
    private final AIStateManager aiStateManager;
    
    // Learning parameters
    private static final float CONFIDENCE_THRESHOLD = 0.75f;
    private static final int MAX_LEARNING_SAMPLES = 1000;
    
    // Learning cache
    private final Map<String, LearnedResponse> learningCache;
    private int totalInteractions = 0;
    private int localSuccesses = 0;
    
    private HybridAILearningSystem(Context context) {
        this.context = context.getApplicationContext();
        this.groqApiService = GroqApiService.getInstance(context);
        this.aiStateManager = AIStateManager.getInstance(context);
        this.learningCache = new HashMap<>();
        
        Log.d(TAG, "Hybrid AI Learning System initialized");
    }
    
    public static synchronized HybridAILearningSystem getInstance(Context context) {
        if (instance == null) {
            instance = new HybridAILearningSystem(context);
        }
        return instance;
    }
    
    /**
     * Process a query using hybrid approach
     * 
     * @param query The user query
     * @param localPrediction Local AI prediction (can be null)
     * @param confidence Confidence score of local prediction (0.0-1.0)
     * @param callback Response callback
     */
    public void processQuery(String query, String localPrediction, float confidence, ResponseCallback callback) {
        totalInteractions++;
        
        // If local confidence is high enough, use it
        if (localPrediction != null && confidence >= CONFIDENCE_THRESHOLD) {
            Log.d(TAG, "Using local prediction (confidence: " + confidence + ")");
            localSuccesses++;
            callback.onResponse(localPrediction, "local");
            return;
        }
        
        // Check learning cache
        LearnedResponse cached = learningCache.get(query.toLowerCase().trim());
        if (cached != null && cached.useCount < 5) {
            Log.d(TAG, "Using cached learned response");
            cached.useCount++;
            callback.onResponse(cached.response, "cached");
            
            // Still query Groq to refine
            queryGroqAndLearn(query, localPrediction, null);
            return;
        }
        
        // Fall back to Groq
        Log.d(TAG, "Falling back to Groq (local confidence too low: " + confidence + ")");
        queryGroqAndLearn(query, localPrediction, callback);
    }
    
    /**
     * Query Groq and learn from response
     */
    private void queryGroqAndLearn(String query, String localPrediction, ResponseCallback callback) {
        // Build enhanced prompt that includes local prediction for learning
        String enhancedPrompt = query;
        if (localPrediction != null) {
            enhancedPrompt = "User query: " + query + "\n\n" +
                           "Local AI prediction: " + localPrediction + "\n\n" +
                           "Please provide the best response and explain if the local prediction was correct.";
        }
        
        groqApiService.chatCompletion(enhancedPrompt, new GroqApiService.ChatCompletionCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Groq response received");
                
                // Store in learning cache
                LearnedResponse learned = new LearnedResponse(response, query);
                learningCache.put(query.toLowerCase().trim(), learned);
                
                // Trim cache if too large
                if (learningCache.size() > MAX_LEARNING_SAMPLES) {
                    trimLearningCache();
                }
                
                // Extract learning signal
                if (localPrediction != null) {
                    extractLearningSignal(query, localPrediction, response);
                }
                
                if (callback != null) {
                    callback.onResponse(response, "groq");
                }
                
                Log.d(TAG, "Learning progress: " + getIndependenceScore() + "% independent");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Groq error: " + error);
                
                // Fall back to local prediction even if low confidence
                if (callback != null) {
                    if (localPrediction != null) {
                        callback.onResponse(localPrediction, "local_fallback");
                    } else {
                        callback.onError("Unable to process query: " + error);
                    }
                }
            }
        });
    }
    
    /**
     * Process streaming query with hybrid approach
     */
    public void processQueryStreaming(String query, String localPrediction, float confidence, 
                                     StreamingResponseCallback callback) {
        totalInteractions++;
        
        // If local confidence is high, use it
        if (localPrediction != null && confidence >= CONFIDENCE_THRESHOLD) {
            Log.d(TAG, "Using local prediction (streaming mode)");
            localSuccesses++;
            
            // Simulate streaming for consistency
            String[] words = localPrediction.split(" ");
            for (String word : words) {
                callback.onChunk(word + " ");
            }
            callback.onComplete("local");
            return;
        }
        
        // Use Groq streaming
        Log.d(TAG, "Using Groq streaming (local confidence: " + confidence + ")");
        
        StringBuilder fullResponse = new StringBuilder();
        groqApiService.chatCompletionStreaming(query, new GroqApiService.StreamingCallback() {
            @Override
            public void onChunk(String chunk) {
                fullResponse.append(chunk);
                callback.onChunk(chunk);
            }
            
            @Override
            public void onComplete() {
                // Learn from complete response
                LearnedResponse learned = new LearnedResponse(fullResponse.toString(), query);
                learningCache.put(query.toLowerCase().trim(), learned);
                
                callback.onComplete("groq");
                
                Log.d(TAG, "Streaming complete. Learning progress: " + getIndependenceScore() + "%");
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Extract learning signal from Groq response
     */
    private void extractLearningSignal(String query, String localPrediction, String groqResponse) {
        // This would update local models based on the comparison
        // For now, just log it
        Log.d(TAG, "Learning from query: " + query.substring(0, Math.min(50, query.length())) + "...");
    }
    
    /**
     * Trim learning cache to prevent memory issues
     */
    private void trimLearningCache() {
        // Remove least used entries
        String leastUsedKey = null;
        int minUseCount = Integer.MAX_VALUE;
        
        for (Map.Entry<String, LearnedResponse> entry : learningCache.entrySet()) {
            if (entry.getValue().useCount < minUseCount) {
                minUseCount = entry.getValue().useCount;
                leastUsedKey = entry.getKey();
            }
        }
        
        if (leastUsedKey != null) {
            learningCache.remove(leastUsedKey);
        }
    }
    
    /**
     * Get independence score (percentage of queries handled locally)
     */
    public float getIndependenceScore() {
        if (totalInteractions == 0) {
            return 0f;
        }
        return (localSuccesses * 100f) / totalInteractions;
    }
    
    /**
     * Get learning statistics
     */
    public LearningStats getStats() {
        return new LearningStats(
            totalInteractions,
            localSuccesses,
            learningCache.size(),
            getIndependenceScore()
        );
    }
    
    /**
     * Learned response cache entry
     */
    private static class LearnedResponse {
        String response;
        String originalQuery;
        int useCount;
        long timestamp;
        
        LearnedResponse(String response, String query) {
            this.response = response;
            this.originalQuery = query;
            this.useCount = 0;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Learning statistics
     */
    public static class LearningStats {
        public final int totalInteractions;
        public final int localSuccesses;
        public final int cacheSize;
        public final float independenceScore;
        
        LearningStats(int total, int local, int cache, float score) {
            this.totalInteractions = total;
            this.localSuccesses = local;
            this.cacheSize = cache;
            this.independenceScore = score;
        }
    }
    
    /**
     * Response callback
     */
    public interface ResponseCallback {
        void onResponse(String response, String source);
        void onError(String error);
    }
    
    /**
     * Streaming response callback
     */
    public interface StreamingResponseCallback {
        void onChunk(String chunk);
        void onComplete(String source);
        void onError(String error);
    }
}
