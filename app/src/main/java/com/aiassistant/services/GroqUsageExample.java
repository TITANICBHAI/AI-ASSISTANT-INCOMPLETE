package com.aiassistant.services;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class GroqUsageExample {
    private static final String TAG = "GroqUsageExample";
    
    public static void exampleBasicChatCompletion(Context context) {
        GroqApiService groqService = GroqApiService.getInstance(context);
        
        groqService.chatCompletion("What is the capital of France?", 
            new GroqApiService.ChatCompletionCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d(TAG, "Response: " + response);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error: " + error);
                }
            });
    }
    
    public static void exampleConversationalChat(Context context) {
        GroqApiService groqService = GroqApiService.getInstance(context);
        
        List<GroqApiService.ChatMessage> messages = new ArrayList<>();
        messages.add(new GroqApiService.ChatMessage("system", "You are a helpful assistant."));
        messages.add(new GroqApiService.ChatMessage("user", "Hello, who are you?"));
        messages.add(new GroqApiService.ChatMessage("assistant", "I am an AI assistant here to help you."));
        messages.add(new GroqApiService.ChatMessage("user", "Can you solve math problems?"));
        
        groqService.chatCompletion(messages, "llama-3.3-70b-versatile", 
            new GroqApiService.ChatCompletionCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d(TAG, "Response: " + response);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error: " + error);
                }
            });
    }
    
    public static void exampleStreamingResponse(Context context) {
        GroqApiService groqService = GroqApiService.getInstance(context);
        
        groqService.chatCompletionStreaming("Write a short story about a robot.", 
            new GroqApiService.StreamingCallback() {
                @Override
                public void onChunk(String chunk) {
                    Log.d(TAG, "Chunk: " + chunk);
                }
                
                @Override
                public void onComplete() {
                    Log.d(TAG, "Streaming complete");
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error: " + error);
                }
            });
    }
    
    public static void exampleVoiceCommand(Context context) {
        GroqIntegrationHelper helper = new GroqIntegrationHelper(context);
        
        helper.processVoiceCommandWithGroq("Turn on the lights", 
            new GroqIntegrationHelper.ResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d(TAG, "Voice command response: " + response);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Voice command error: " + error);
                }
            });
    }
    
    public static void exampleCallHandling(Context context) {
        GroqIntegrationHelper helper = new GroqIntegrationHelper(context);
        
        List<GroqApiService.ChatMessage> history = new ArrayList<>();
        history.add(new GroqApiService.ChatMessage("user", "Hello, is this the support line?"));
        history.add(new GroqApiService.ChatMessage("assistant", "Yes, this is support. How can I help you today?"));
        
        helper.handleCallConversation("John", "I need help with my account", history, 
            new GroqIntegrationHelper.ResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d(TAG, "Call response: " + response);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Call handling error: " + error);
                }
            });
    }
    
    public static void exampleJEESolver(Context context) {
        GroqIntegrationHelper helper = new GroqIntegrationHelper(context);
        
        String problem = "A ball is thrown vertically upward with an initial velocity of 20 m/s. " +
                        "Calculate the maximum height reached by the ball. (g = 10 m/s²)";
        
        helper.solveJEEProblem(problem, new GroqIntegrationHelper.ResponseCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "JEE Solution: " + response);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "JEE solver error: " + error);
            }
        });
    }
    
    public static void exampleDecisionMaking(Context context) {
        GroqIntegrationHelper helper = new GroqIntegrationHelper(context);
        
        String situation = "I need to choose a university for my engineering degree.";
        String[] options = {
            "Top-ranked university with high fees",
            "Medium-ranked university with scholarship",
            "Local university close to home"
        };
        
        helper.getDecisionAdvice(situation, options, 
            new GroqIntegrationHelper.ResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d(TAG, "Decision advice: " + response);
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Decision making error: " + error);
                }
            });
    }
    
    public static void exampleApiKeyManagement(Context context) {
        GroqApiKeyManager keyManager = GroqApiKeyManager.getInstance(context);
        
        keyManager.setApiKey("your_groq_api_key_here");
        Log.d(TAG, "API key saved");
        
        boolean hasKey = keyManager.hasApiKey();
        Log.d(TAG, "Has API key: " + hasKey);
        
        String apiKey = keyManager.getApiKey();
        Log.d(TAG, "Retrieved API key: " + (apiKey != null ? "***" : "null"));
        
        boolean encryptionAvailable = keyManager.isEncryptionAvailable();
        Log.d(TAG, "Encryption available: " + encryptionAvailable);
    }
}
