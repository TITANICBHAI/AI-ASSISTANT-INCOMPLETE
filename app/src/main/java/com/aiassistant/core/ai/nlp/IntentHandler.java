package com.aiassistant.core.ai.nlp;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.ai.HybridAILearningSystem;

/**
 * Handler for processing extracted intents and taking appropriate actions
 */
public class IntentHandler {
    private static final String TAG = "IntentHandler";
    
    private Context context;
    private AIStateManager aiStateManager;
    private HybridAILearningSystem hybridAI;
    private Handler mainHandler;
    
    /**
     * Constructor
     */
    public IntentHandler(Context context) {
        this.context = context;
        this.aiStateManager = AIStateManager.getInstance();
        this.hybridAI = HybridAILearningSystem.getInstance(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Handle an extracted intent with its entities
     */
    public void handleIntent(String intentName, String[] entities, String originalText) {
        if (intentName == null) {
            Log.w(TAG, "Cannot handle null intent");
            return;
        }
        
        Log.d(TAG, "Handling intent: " + intentName);
        
        switch (intentName) {
            case "START":
                handleStartIntent();
                break;
                
            case "STOP":
                handleStopIntent();
                break;
                
            case "AUTO_MODE":
                handleAutoModeIntent();
                break;
                
            case "COPILOT_MODE":
                handleCopilotModeIntent();
                break;
                
            case "CLICK":
                handleClickIntent(entities, originalText);
                break;
                
            case "SWIPE":
                handleSwipeIntent(entities, originalText);
                break;
                
            case "WHAT_IS":
                handleWhatIsIntent(entities, originalText);
                break;
                
            case "HOW_TO":
                handleHowToIntent(entities, originalText);
                break;
                
            default:
                Log.w(TAG, "Unknown intent: " + intentName);
                break;
        }
    }
    
    /**
     * Handle START intent
     */
    private void handleStartIntent() {
        Log.i(TAG, "Handling START intent");
        
        // Activate the AI assistant
        aiStateManager.setActive(true);
    }
    
    /**
     * Handle STOP intent
     */
    private void handleStopIntent() {
        Log.i(TAG, "Handling STOP intent");
        
        // Deactivate the AI assistant
        aiStateManager.setActive(false);
    }
    
    /**
     * Handle AUTO_MODE intent
     */
    private void handleAutoModeIntent() {
        Log.i(TAG, "Handling AUTO_MODE intent");
        
        // Set to auto mode
        aiStateManager.setOperationMode(AIStateManager.OperationMode.AUTO);
    }
    
    /**
     * Handle COPILOT_MODE intent
     */
    private void handleCopilotModeIntent() {
        Log.i(TAG, "Handling COPILOT_MODE intent");
        
        // Set to copilot mode
        aiStateManager.setOperationMode(AIStateManager.OperationMode.COPILOT);
    }
    
    /**
     * Handle CLICK intent
     */
    private void handleClickIntent(String[] entities, String originalText) {
        if (entities == null || entities.length < 1) {
            Log.w(TAG, "Missing entities for CLICK intent");
            return;
        }
        
        String target = entities[entities.length - 1];
        Log.i(TAG, "Handling CLICK intent on target: " + target);
        
        String query = "Identify the UI element to click based on: '" + originalText + 
                      "'. Provide the element description and location strategy (e.g., text match, content description, resource ID).";
        
        hybridAI.processQuery(query, null, 0.0f, new HybridAILearningSystem.ResponseCallback() {
            @Override
            public void onResponse(String response, String source) {
                Log.i(TAG, "Click element identified (" + source + "): " + response);
                mainHandler.post(() -> {
                    Toast.makeText(context, "Click action: " + response, Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error identifying click element: " + error);
            }
        });
    }
    
    /**
     * Handle SWIPE intent
     */
    private void handleSwipeIntent(String[] entities, String originalText) {
        if (entities == null || entities.length < 1) {
            Log.w(TAG, "Missing entities for SWIPE intent");
            return;
        }
        
        String direction = entities[0];
        Log.i(TAG, "Handling SWIPE intent in direction: " + direction);
        
        String query = "For the swipe command: '" + originalText + 
                      "', determine the exact swipe direction (up/down/left/right), start coordinates as percentage of screen (0-100%), " +
                      "end coordinates, and duration in milliseconds. Format: direction|startX,startY|endX,endY|duration";
        
        hybridAI.processQuery(query, null, 0.0f, new HybridAILearningSystem.ResponseCallback() {
            @Override
            public void onResponse(String response, String source) {
                Log.i(TAG, "Swipe parameters identified (" + source + "): " + response);
                mainHandler.post(() -> {
                    Toast.makeText(context, "Swipe action: " + response, Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error identifying swipe parameters: " + error);
            }
        });
    }
    
    /**
     * Handle WHAT_IS intent
     */
    private void handleWhatIsIntent(String[] entities, String originalText) {
        if (entities == null || entities.length < 1) {
            Log.w(TAG, "Missing entities for WHAT_IS intent");
            return;
        }
        
        String subject = entities[entities.length - 1];
        Log.i(TAG, "Handling WHAT_IS intent for subject: " + subject);
        
        String query = "What is " + subject + "? Provide a clear and concise explanation.";
        
        hybridAI.processQuery(query, null, 0.0f, new HybridAILearningSystem.ResponseCallback() {
            @Override
            public void onResponse(String response, String source) {
                Log.i(TAG, "Information retrieved (" + source + "): " + response);
                mainHandler.post(() -> {
                    Toast.makeText(context, response.length() > 100 ? 
                        response.substring(0, 97) + "..." : response, Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error retrieving information: " + error);
                mainHandler.post(() -> {
                    Toast.makeText(context, "Could not retrieve information: " + error, 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * Handle HOW_TO intent
     */
    private void handleHowToIntent(String[] entities, String originalText) {
        if (entities == null || entities.length < 1) {
            Log.w(TAG, "Missing entities for HOW_TO intent");
            return;
        }
        
        String task = entities[0];
        Log.i(TAG, "Handling HOW_TO intent for task: " + task);
        
        String query = "How to " + task + "? Provide step-by-step instructions.";
        
        hybridAI.processQuery(query, null, 0.0f, new HybridAILearningSystem.ResponseCallback() {
            @Override
            public void onResponse(String response, String source) {
                Log.i(TAG, "Instructions retrieved (" + source + "): " + response);
                mainHandler.post(() -> {
                    Toast.makeText(context, "Instructions: " + 
                        (response.length() > 100 ? response.substring(0, 97) + "..." : response), 
                        Toast.LENGTH_LONG).show();
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error retrieving instructions: " + error);
                mainHandler.post(() -> {
                    Toast.makeText(context, "Could not retrieve instructions: " + error, 
                        Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
