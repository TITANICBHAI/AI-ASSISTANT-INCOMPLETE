package com.aiassistant.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;
import android.util.Log;

import com.aiassistant.ai.features.call.CallHandlingSystem;
import com.aiassistant.ai.features.call.ResearchEnabledCallHandler;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.features.voice.ContextAwareVoiceCommand;
import com.aiassistant.features.voice.SentientVoiceSystem;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.RequiresApi;

/**
 * AI-powered call service that can answer and manage active calls,
 * allowing the AI assistant to speak and respond on behalf of the user.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class AICallService extends InCallService {
    private static final String TAG = "AICallService";
    
    private CallHandlingSystem callHandlingSystem;
    private AIStateManager aiStateManager;
    private SentientVoiceSystem voiceSystem;
    private ContextAwareVoiceCommand voiceCommand;
    private ResearchEnabledCallHandler researchHandler;
    
    private Map<String, Call> activeCalls = new HashMap<>();
    private Map<String, CallState> callStates = new HashMap<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    
    // Call state tracking
    private enum CallState {
        NEW,
        RINGING,
        DIALING,
        ACTIVE,
        DISCONNECTED
    }
    
    // Auto-answer settings
    private boolean autoAnswerEnabled = false;
    private int autoAnswerDelayMs = 3000;
    
    // AI call handling settings
    private boolean aiCallHandlingEnabled = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize components
        callHandlingSystem = CallHandlingSystem.getInstance(this);
        aiStateManager = AIStateManager.getInstance(this);
        voiceSystem = SentientVoiceSystem.getInstance(this);
        voiceCommand = ContextAwareVoiceCommand.getInstance(this);
        researchHandler = ResearchEnabledCallHandler.getInstance(this);
        
        // Load user preferences
        loadPreferences();
        
        Log.d(TAG, "AI Call Service created");
    }
    
    @Override
    public void onCallAdded(Call call) {
        Log.d(TAG, "Call added: " + call.getDetails().getHandle());
        
        // Store call
        String callId = call.getDetails().getTelecomCallId();
        activeCalls.put(callId, call);
        
        // Register callback
        call.registerCallback(new AICallCallback());
        
        // Check call state
        int state = call.getState();
        updateCallState(call, state);
    }
    
    @Override
    public void onCallRemoved(Call call) {
        Log.d(TAG, "Call removed: " + call.getDetails().getHandle());
        
        // Handle call ending
        if (call.getDetails().getHandle() != null) {
            String phoneNumber = call.getDetails().getHandle().getSchemeSpecificPart();
            callHandlingSystem.handleCallEnded(phoneNumber, calculateCallDuration(call));
        }
        
        // Remove from active calls
        String callId = call.getDetails().getTelecomCallId();
        activeCalls.remove(callId);
        callStates.remove(callId);
    }
    
    /**
     * Calculate call duration in seconds
     */
    private int calculateCallDuration(Call call) {
        // In a real implementation, this would calculate the actual duration
        // For now, return a placeholder value
        return 60;
    }
    
    /**
     * Update the state of a call
     */
    private void updateCallState(Call call, int state) {
        String callId = call.getDetails().getTelecomCallId();
        String phoneNumber = "";
        
        if (call.getDetails().getHandle() != null) {
            phoneNumber = call.getDetails().getHandle().getSchemeSpecificPart();
        }
        
        // Track call state
        switch (state) {
            case Call.STATE_RINGING:
                callStates.put(callId, CallState.RINGING);
                
                // If auto-answer is enabled, answer after delay
                if (autoAnswerEnabled && shouldAutoAnswer(phoneNumber)) {
                    handler.postDelayed(() -> {
                        if (activeCalls.containsKey(callId) && 
                            callStates.get(callId) == CallState.RINGING) {
                            answerCall(call);
                        }
                    }, autoAnswerDelayMs);
                }
                break;
                
            case Call.STATE_DIALING:
                callStates.put(callId, CallState.DIALING);
                break;
                
            case Call.STATE_ACTIVE:
                callStates.put(callId, CallState.ACTIVE);
                
                if (phoneNumber.length() > 0) {
                    // Notify the call handling system
                    callHandlingSystem.handleCallAnswered(phoneNumber);
                    
                    // If AI call handling is enabled, activate it
                    if (aiCallHandlingEnabled) {
                        activateAICallHandler(call);
                    }
                }
                break;
                
            case Call.STATE_DISCONNECTED:
                callStates.put(callId, CallState.DISCONNECTED);
                break;
                
            default:
                // Other states
                break;
        }
    }
    
    /**
     * Answer a call
     */
    private void answerCall(Call call) {
        try {
            Log.d(TAG, "Answering call automatically");
            call.answer(VideoProfile.STATE_AUDIO_ONLY);
            
            // Speak notification that AI answered the call
            voiceSystem.speak("I've answered the call for you.", "neutral", 0.7f);
        } catch (Exception e) {
            Log.e(TAG, "Error answering call: " + e.getMessage());
        }
    }
    
    /**
     * Activate AI call handler to speak on user's behalf
     */
    private void activateAICallHandler(Call call) {
        String phoneNumber = "";
        String callerName = "Unknown Caller";
        
        if (call.getDetails().getHandle() != null) {
            phoneNumber = call.getDetails().getHandle().getSchemeSpecificPart();
            // In a real implementation, get caller name from contacts
        }
        
        Log.d(TAG, "Activating AI call handler for call with " + callerName);
        
        // Send greeting message through TTS
        final String finalCallerName = callerName;
        researchHandler.handleCallMessageWithResearch(callerName, "greeting", 
            new ResearchEnabledCallHandler.ResearchResponseCallback() {
                @Override
                public void onResponseGenerated(String query, String response) {
                    // Speak the AI's response
                    voiceSystem.speak(response, "friendly", 0.8f);
                    
                    // Start listening for the caller's response
                    startListeningForCallerResponse(call, finalCallerName);
                }
            });
    }
    
    /**
     * Start speech recognition to listen for caller's response
     */
    private void startListeningForCallerResponse(Call call, String callerName) {
        // Configure voice command for call context
        voiceCommand.setContext("call_conversation");
        
        // Start listening
        voiceCommand.setCommandListener(new ContextAwareVoiceCommand.VoiceCommandListener() {
            @Override
            public void onCommandRecognized(String command, Map<String, String> parameters, double confidence) {
                // This is recognizing what the other person is saying
                handleCallerResponse(call, callerName, command);
            }
            
            @Override
            public void onPartialCommandRecognized(String partialCommand) {
                // Partial recognition, do nothing
            }
            
            @Override
            public void onCommandError(int errorCode) {
                Log.e(TAG, "Error recognizing caller speech: " + errorCode);
                
                // If there's an error, just ask a general question
                researchHandler.handleCallMessageWithResearch(callerName, "How can I help you today?", 
                    new ResearchEnabledCallHandler.ResearchResponseCallback() {
                        @Override
                        public void onResponseGenerated(String query, String response) {
                            voiceSystem.speak(response, "friendly", 0.8f);
                            
                            // Restart listening
                            startListeningForCallerResponse(call, callerName);
                        }
                    });
            }
        });
        
        voiceCommand.startListening();
    }
    
    /**
     * Handle caller's response and generate AI reply
     */
    private void handleCallerResponse(Call call, String callerName, String callerSpeech) {
        Log.d(TAG, "Caller said: " + callerSpeech);
        
        // Process caller's speech through research-enabled call handler
        researchHandler.handleCallMessageWithResearch(callerName, callerSpeech, 
            new ResearchEnabledCallHandler.ResearchResponseCallback() {
                @Override
                public void onResponseGenerated(String query, String response) {
                    // Speak the AI's response
                    voiceSystem.speak(response, "friendly", 0.8f);
                    
                    // Resume listening for next response
                    startListeningForCallerResponse(call, callerName);
                }
            });
    }
    
    /**
     * Check if a call should be auto-answered
     */
    private boolean shouldAutoAnswer(String phoneNumber) {
        // Check user preferences
        String whitelistPref = aiStateManager.getUserPreference("call_auto_answer_whitelist", "");
        String[] whitelist = whitelistPref.split(",");
        
        for (String whitelistedNumber : whitelist) {
            if (phoneNumber.contains(whitelistedNumber) || whitelistedNumber.contains(phoneNumber)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Load user preferences
     */
    private void loadPreferences() {
        // Load auto-answer preference
        String autoAnswerPref = aiStateManager.getUserPreference("call_auto_answer", "false");
        autoAnswerEnabled = Boolean.parseBoolean(autoAnswerPref);
        
        // Load AI call handling preference
        String aiHandlingPref = aiStateManager.getUserPreference("ai_call_handling", "false");
        aiCallHandlingEnabled = Boolean.parseBoolean(aiHandlingPref);
        
        // Load auto-answer delay
        String delayPref = aiStateManager.getUserPreference("auto_answer_delay_ms", "3000");
        try {
            autoAnswerDelayMs = Integer.parseInt(delayPref);
        } catch (NumberFormatException e) {
            autoAnswerDelayMs = 3000;
        }
    }
    
    /**
     * Enable/disable auto-answer
     */
    public void setAutoAnswerEnabled(boolean enabled) {
        this.autoAnswerEnabled = enabled;
        aiStateManager.setUserPreference("call_auto_answer", String.valueOf(enabled));
    }
    
    /**
     * Enable/disable AI call handling
     */
    public void setAICallHandlingEnabled(boolean enabled) {
        this.aiCallHandlingEnabled = enabled;
        aiStateManager.setUserPreference("ai_call_handling", String.valueOf(enabled));
    }
    
    /**
     * Call callback to track call state changes
     */
    private class AICallCallback extends Call.Callback {
        @Override
        public void onStateChanged(Call call, int state) {
            updateCallState(call, state);
        }
    }
    
    // Helper class for video profile (required for answering calls)
    private static class VideoProfile {
        public static final int STATE_AUDIO_ONLY = 0;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AI Call Service destroyed");
        
        // Clean up
        for (Call call : activeCalls.values()) {
            call.unregisterCallback(new AICallCallback());
        }
        
        activeCalls.clear();
        callStates.clear();
    }
}