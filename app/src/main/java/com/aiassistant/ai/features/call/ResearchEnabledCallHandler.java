package com.aiassistant.ai.features.call;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.features.voice.SentientVoiceSystem;
import com.aiassistant.research.InformationVerifier;
import com.aiassistant.research.ResearchManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ResearchEnabledCallHandler extends call handling functionality with the ability
 * to research information online when appropriate, enabling the AI to provide
 * more informative responses during calls.
 */
public class ResearchEnabledCallHandler {
    private static final String TAG = "ResearchEnabledCallHandler";
    private static ResearchEnabledCallHandler instance;
    
    // Core components
    private Context context;
    private CallHandlingSystem callSystem;
    private AIStateManager aiStateManager;
    private SentientVoiceSystem voiceSystem;
    private ResearchManager researchManager;
    private InformationVerifier informationVerifier;
    private ExecutorService executor;
    
    // Settings
    private boolean autoResearchEnabled = true;
    private boolean researchSportsEnabled = true;
    private boolean researchNewsEnabled = true;
    
    // Callback interface
    public interface ResearchResponseCallback {
        void onResponseGenerated(String query, String response);
    }
    
    private ResearchEnabledCallHandler(Context context) {
        this.context = context.getApplicationContext();
        this.callSystem = CallHandlingSystem.getInstance(context);
        this.aiStateManager = AIStateManager.getInstance(context);
        this.voiceSystem = SentientVoiceSystem.getInstance(context);
        this.researchManager = ResearchManager.getInstance(context);
        this.informationVerifier = new InformationVerifier(context);
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public static synchronized ResearchEnabledCallHandler getInstance(Context context) {
        if (instance == null) {
            instance = new ResearchEnabledCallHandler(context);
        }
        return instance;
    }
    
    /**
     * Handle a call message that might require research
     */
    public void handleCallMessageWithResearch(String callerName, String message, ResearchResponseCallback callback) {
        // Check if research is enabled and internet is available
        if (!autoResearchEnabled || !researchManager.isInternetAvailable()) {
            // Use standard non-researched response
            generateStandardResponse(callerName, message, callback);
            return;
        }
        
        // Check if this is a message that could benefit from research
        if (shouldResearchMessage(message)) {
            Log.d(TAG, "Message requires research: " + message);
            
            // For sports predictions specifically, handle differently
            if (isSportsPredictionQuery(message)) {
                handleSportsPredictionQuery(callerName, message, callback);
            } else {
                // Research general informational query
                researchAndRespond(callerName, message, callback);
            }
        } else {
            // Use standard non-researched response
            generateStandardResponse(callerName, message, callback);
        }
    }
    
    /**
     * Determine if a message should be researched
     */
    private boolean shouldResearchMessage(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        
        // Check for question indicators
        boolean isQuestion = message.contains("?") || 
                message.toLowerCase().startsWith("who") ||
                message.toLowerCase().startsWith("what") ||
                message.toLowerCase().startsWith("when") ||
                message.toLowerCase().startsWith("where") ||
                message.toLowerCase().startsWith("why") ||
                message.toLowerCase().startsWith("how");
        
        if (!isQuestion) {
            return false;
        }
        
        // Check for sports-related queries
        if (researchSportsEnabled && 
                (message.toLowerCase().contains("cricket") ||
                 message.toLowerCase().contains("football") ||
                 message.toLowerCase().contains("basketball") ||
                 message.toLowerCase().contains("tennis") ||
                 message.toLowerCase().contains("game") ||
                 message.toLowerCase().contains("match") ||
                 message.toLowerCase().contains("team") ||
                 message.toLowerCase().contains("player") ||
                 message.toLowerCase().contains("score") ||
                 message.toLowerCase().contains("win"))) {
            return true;
        }
        
        // Check for news-related queries
        if (researchNewsEnabled &&
                (message.toLowerCase().contains("news") ||
                 message.toLowerCase().contains("recent") ||
                 message.toLowerCase().contains("latest") ||
                 message.toLowerCase().contains("update") ||
                 message.toLowerCase().contains("happened"))) {
            return true;
        }
        
        // By default, don't research
        return false;
    }
    
    /**
     * Check if the query is a sports prediction
     */
    private boolean isSportsPredictionQuery(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        
        // Check for prediction patterns
        boolean isPredictionQuery = 
                message.toLowerCase().contains("who will win") ||
                message.toLowerCase().contains("who is going to win") ||
                message.toLowerCase().contains("which team will win") ||
                message.toLowerCase().contains("predict the winner") ||
                message.toLowerCase().contains("who do you think will win");
        
        // Check for sports terms
        boolean isSportsRelated =
                message.toLowerCase().contains("cricket") ||
                message.toLowerCase().contains("football") ||
                message.toLowerCase().contains("basketball") ||
                message.toLowerCase().contains("match") ||
                message.toLowerCase().contains("game") ||
                message.toLowerCase().contains("tournament");
        
        return isPredictionQuery && isSportsRelated;
    }
    
    /**
     * Handle sports prediction query
     */
    private void handleSportsPredictionQuery(String callerName, String message, ResearchResponseCallback callback) {
        // For sports predictions, we'll use a special response that explains we don't make predictions
        executor.submit(() -> {
            // First, let the user know we're researching
            if (callback != null) {
                callback.onResponseGenerated(message, 
                        "I'm researching information about this sports question. One moment please...");
            }
            
            // However, we can provide information about the teams/players
            researchManager.researchTopic(message, new ResearchManager.ResearchCallback() {
                @Override
                public void onResearchCompleted(ResearchManager.ResearchResult result) {
                    // Generate a special response for prediction queries
                    String response = formatSportsPredictionResponse(callerName, message, result);
                    
                    if (callback != null) {
                        callback.onResponseGenerated(message, response);
                    }
                }
                
                @Override
                public void onResearchError(String query, String errorMessage) {
                    // Fall back to standard non-prediction response
                    String response = "Hi " + callerName + ", I'm the AI assistant helping the person you're calling. " +
                            "I don't make sports predictions as they involve many variables and uncertainty. " +
                            "I'd be happy to take a message or have them call you back to discuss the match.";
                    
                    if (callback != null) {
                        callback.onResponseGenerated(message, response);
                    }
                }
            });
        });
    }
    
    /**
     * Format a response for sports prediction queries
     */
    private String formatSportsPredictionResponse(String callerName, String message, ResearchManager.ResearchResult result) {
        StringBuilder response = new StringBuilder();
        
        // Identify which teams/players are being asked about
        String teamMention = "";
        if (message.toLowerCase().contains("india") && message.toLowerCase().contains("australia")) {
            teamMention = "India and Australia";
        } else if (message.toLowerCase().contains("india")) {
            teamMention = "India";
        } else if (message.toLowerCase().contains("australia")) {
            teamMention = "Australia";
        }
        
        // Start with a friendly greeting that identifies this as an AI
        response.append("Hi ").append(callerName).append(", ");
        response.append("I'm the AI assistant helping the person you're calling. ");
        
        // Explain that we don't make predictions
        response.append("While I don't make predictions about who will win ");
        if (!teamMention.isEmpty()) {
            response.append("between ").append(teamMention).append(" ");
        }
        response.append("as sports outcomes are highly unpredictable, ");
        
        // Offer factual information instead
        response.append("I can share some relevant facts from my research:\n\n");
        
        // Add a few key facts from research
        boolean factsAdded = false;
        for (Map.Entry<String, Double> entry : result.factConfidence.entrySet()) {
            if (entry.getValue() >= 0.7) {
                response.append("- ").append(entry.getKey()).append("\n");
                factsAdded = true;
                // Limit to 3 facts
                if (response.toString().split("\n").length > 5) {
                    break;
                }
            }
        }
        
        if (!factsAdded) {
            // No high-confidence facts found
            response.append("I don't have enough reliable information about this match at the moment. ");
        }
        
        // Offer to take a message
        response.append("\nWould you like me to take a message or have them call you back to discuss the match?");
        
        return response.toString();
    }
    
    /**
     * Research a query and generate a response
     */
    private void researchAndRespond(String callerName, String message, ResearchResponseCallback callback) {
        executor.submit(() -> {
            // First, let the user know we're researching
            if (callback != null) {
                callback.onResponseGenerated(message, 
                        "I'm researching information about this question. One moment please...");
            }
            
            // Perform research
            researchManager.generateResearchedResponse(message, new ResearchManager.ResponseCallback() {
                @Override
                public void onResponseGenerated(String query, String researchResponse) {
                    // Format the response for a call context
                    String formattedResponse = formatResearchResponseForCall(callerName, researchResponse);
                    
                    if (callback != null) {
                        callback.onResponseGenerated(query, formattedResponse);
                    }
                }
            });
        });
    }
    
    /**
     * Format a researched response for call context
     */
    private String formatResearchResponseForCall(String callerName, String researchResponse) {
        StringBuilder response = new StringBuilder();
        
        // Start with a friendly greeting that identifies this as an AI
        response.append("Hi ").append(callerName).append(", ");
        response.append("I'm the AI assistant helping the person you're calling. ");
        
        // Add researched information, but keep it concise for a call
        String[] lines = researchResponse.split("\n");
        if (lines.length > 10) {
            // Summarize long responses
            response.append("Based on my research, here's a brief summary: \n\n");
            
            // Add introduction line if available
            if (lines.length > 0) {
                response.append(lines[0]).append("\n\n");
            }
            
            // Add key points (identified by bullet points)
            int pointsAdded = 0;
            for (String line : lines) {
                if (line.trim().startsWith("-") || line.trim().startsWith("•")) {
                    response.append(line).append("\n");
                    pointsAdded++;
                    
                    if (pointsAdded >= 5) {
                        break; // Limit to 5 key points
                    }
                }
            }
        } else {
            // For shorter responses, include the full research
            response.append(researchResponse);
        }
        
        // Offer to take a message
        response.append("\n\nWould you like me to take a message or have them call you back to discuss this further?");
        
        return response.toString();
    }
    
    /**
     * Generate standard response without research
     */
    private void generateStandardResponse(String callerName, String message, ResearchResponseCallback callback) {
        // Standard response for when research is not appropriate or available
        String response = "Hi " + callerName + ", I'm the AI assistant helping the person you're calling. " +
                "I can't answer that specific question for you, but I'd be happy to take a message or " +
                "have them call you back when they're available. How can I help?";
        
        if (callback != null) {
            callback.onResponseGenerated(message, response);
        }
    }
    
    /**
     * Set whether automatic research is enabled
     */
    public void setAutoResearchEnabled(boolean enabled) {
        this.autoResearchEnabled = enabled;
    }
    
    /**
     * Check if automatic research is enabled
     */
    public boolean isAutoResearchEnabled() {
        return autoResearchEnabled;
    }
    
    /**
     * Set whether sports research is enabled
     */
    public void setResearchSportsEnabled(boolean enabled) {
        this.researchSportsEnabled = enabled;
    }
    
    /**
     * Set whether news research is enabled
     */
    public void setResearchNewsEnabled(boolean enabled) {
        this.researchNewsEnabled = enabled;
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
