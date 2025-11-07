package com.aiassistant.services;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroqIntegrationHelper {
    private static final String TAG = "GroqIntegrationHelper";
    
    private final Context context;
    private final AIStateManager aiStateManager;
    private final GroqApiService groqApiService;
    
    public GroqIntegrationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.aiStateManager = AIStateManager.getInstance(context);
        this.groqApiService = GroqApiService.getInstance(context);
    }
    
    public void processVoiceCommandWithGroq(String voiceCommand, ResponseCallback callback) {
        Log.d(TAG, "Processing voice command: " + voiceCommand);
        
        aiStateManager.processVoiceCommand(voiceCommand, new GroqApiService.ChatCompletionCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Voice command processed: " + response);
                callback.onSuccess(response);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error processing voice command: " + error);
                callback.onError(error);
            }
        });
    }
    
    public void handleCallConversation(String callerName, String userInput, 
                                      List<GroqApiService.ChatMessage> history, 
                                      ResponseCallback callback) {
        Log.d(TAG, "Handling call conversation with " + callerName);
        
        aiStateManager.generateCallResponse(callerName, history, userInput, 
            new GroqApiService.ChatCompletionCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d(TAG, "Call response generated: " + response);
                    callback.onSuccess(response);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error generating call response: " + error);
                    callback.onError(error);
                }
            });
    }
    
    public void solveJEEProblem(String problemText, ResponseCallback callback) {
        Log.d(TAG, "Solving JEE problem");
        
        aiStateManager.solveJEEProblem(problemText, new GroqApiService.ChatCompletionCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "JEE problem solved: " + response);
                callback.onSuccess(response);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error solving JEE problem: " + error);
                callback.onError(error);
            }
        });
    }
    
    public void getDecisionAdvice(String situation, String[] options, ResponseCallback callback) {
        Log.d(TAG, "Getting decision advice for situation: " + situation);
        
        List<String> optionsList = Arrays.asList(options);
        
        aiStateManager.getDecisionMakingAdvice(situation, optionsList, 
            new GroqApiService.ChatCompletionCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d(TAG, "Decision advice generated: " + response);
                    callback.onSuccess(response);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error generating decision advice: " + error);
                    callback.onError(error);
                }
            });
    }
    
    public void streamResponse(String prompt, StreamCallback callback) {
        Log.d(TAG, "Streaming response for prompt: " + prompt);
        
        aiStateManager.generateStreamingResponse(prompt, new GroqApiService.StreamingCallback() {
            @Override
            public void onChunk(String chunk) {
                callback.onChunk(chunk);
            }
            
            @Override
            public void onComplete() {
                Log.d(TAG, "Streaming complete");
                callback.onComplete();
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Streaming error: " + error);
                callback.onError(error);
            }
        });
    }
    
    public interface ResponseCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public interface StreamCallback {
        void onChunk(String chunk);
        void onComplete();
        void onError(String error);
    }
}
