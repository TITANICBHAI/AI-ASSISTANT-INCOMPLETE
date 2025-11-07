package com.aiassistant.ai.features.call;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.ai.HybridAILearningSystem;
import com.aiassistant.features.voice.ContextAwareVoiceCommand;
import com.aiassistant.features.voice.SentientVoiceSystem;
import com.aiassistant.services.AICallInitiationService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles voice commands related to making and receiving calls,
 * providing a hands-free calling experience with AI assistance.
 */
public class VoiceCommandCallHandler {
    private static final String TAG = "VoiceCommandCallHandler";
    private static VoiceCommandCallHandler instance;
    
    // Core components
    private Context context;
    private AIStateManager aiStateManager;
    private SentientVoiceSystem voiceSystem;
    private ContextAwareVoiceCommand voiceCommand;
    private CallHandlingSystem callHandlingSystem;
    private AICallInitiationService callInitiationService;
    private ResearchEnabledCallHandler researchHandler;
    private HybridAILearningSystem hybridAI;
    
    // Voice command patterns for call commands
    private static final Pattern CALL_COMMAND_PATTERN = 
        Pattern.compile("(?:make a call to|call)(?: to)? (.+?)(?: for me)?", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern CALL_AND_SPEAK_COMMAND_PATTERN = 
        Pattern.compile("call (.+?) and (speak|talk) (for|on behalf of|with) me", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern ANSWER_CALL_COMMAND_PATTERN = 
        Pattern.compile("(answer|pick up|take) (?:the|this)? call", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern ANSWER_AND_SPEAK_COMMAND_PATTERN = 
        Pattern.compile("(answer|pick up|take) (?:the|this)? call and (speak|talk) (for|on behalf of) me", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern HANG_UP_COMMAND_PATTERN = 
        Pattern.compile("(hang up|end|terminate) (?:the|this)? call", Pattern.CASE_INSENSITIVE);
    
    private VoiceCommandCallHandler(Context context) {
        this.context = context.getApplicationContext();
        this.aiStateManager = AIStateManager.getInstance(context);
        this.voiceSystem = SentientVoiceSystem.getInstance(context);
        this.voiceCommand = ContextAwareVoiceCommand.getInstance(context);
        this.callHandlingSystem = CallHandlingSystem.getInstance(context);
        this.callInitiationService = AICallInitiationService.getInstance(context);
        this.researchHandler = ResearchEnabledCallHandler.getInstance(context);
        this.hybridAI = HybridAILearningSystem.getInstance(context);
        
        // Register command patterns
        initializeCommandPatterns();
    }
    
    public static synchronized VoiceCommandCallHandler getInstance(Context context) {
        if (instance == null) {
            instance = new VoiceCommandCallHandler(context);
        }
        return instance;
    }
    
    /**
     * Initialize command patterns for voice recognition
     */
    private void initializeCommandPatterns() {
        // Register with voice command system
        voiceCommand.registerCommandPattern("make_call", "make a call to (.*)", true);
        voiceCommand.registerCommandPattern("make_call", "call (.*)", true);
        voiceCommand.registerCommandPattern("make_call_speak", "call (.*) and speak for me", true);
        voiceCommand.registerCommandPattern("make_call_speak", "call (.*) and talk on my behalf", true);
        voiceCommand.registerCommandPattern("answer_call", "answer (the|this) call", true);
        voiceCommand.registerCommandPattern("answer_call", "pick up (the|this) call", true);
        voiceCommand.registerCommandPattern("answer_call_speak", "answer (the|this) call and speak for me", true);
        voiceCommand.registerCommandPattern("hang_up", "hang up (the|this) call", true);
        voiceCommand.registerCommandPattern("hang_up", "end (the|this) call", true);
    }
    
    /**
     * Start listening for call-related voice commands
     */
    public void startListeningForCallCommands() {
        // Set voice command listener
        voiceCommand.setCommandListener(new ContextAwareVoiceCommand.VoiceCommandListener() {
            @Override
            public void onCommandRecognized(String command, Map<String, String> parameters, double confidence) {
                handleVoiceCommand(command, parameters);
            }
            
            @Override
            public void onPartialCommandRecognized(String partialCommand) {
                // Not needed for call handling
            }
            
            @Override
            public void onCommandError(int errorCode) {
                Log.e(TAG, "Error recognizing voice command: " + errorCode);
            }
        });
        
        // Set listening context to calls
        voiceCommand.setContext("calls");
        
        // Start listening
        voiceCommand.startListening();
    }
    
    /**
     * Process recognized voice commands
     */
    private void handleVoiceCommand(String command, Map<String, String> parameters) {
        Log.d(TAG, "Call command recognized: " + command);
        
        if (command.equals("make_call") && parameters.containsKey("1")) {
            String recipient = parameters.get("1");
            makeCall(recipient, false);
        }
        else if (command.equals("make_call_speak") && parameters.containsKey("1")) {
            String recipient = parameters.get("1");
            makeCall(recipient, true);
        }
        else if (command.equals("answer_call")) {
            answerCall(false);
        }
        else if (command.equals("answer_call_speak")) {
            answerCall(true);
        }
        else if (command.equals("hang_up")) {
            hangUpCall();
        }
    }
    
    /**
     * Process general voice commands that might be call-related
     */
    public boolean processGeneralVoiceCommand(String userCommand) {
        // Check for call commands
        Matcher callMatcher = CALL_COMMAND_PATTERN.matcher(userCommand);
        if (callMatcher.find()) {
            String recipient = callMatcher.group(1);
            makeCall(recipient, false);
            return true;
        }
        
        // Check for call and speak commands
        Matcher callSpeakMatcher = CALL_AND_SPEAK_COMMAND_PATTERN.matcher(userCommand);
        if (callSpeakMatcher.find()) {
            String recipient = callSpeakMatcher.group(1);
            makeCall(recipient, true);
            return true;
        }
        
        // Check for answer commands
        Matcher answerMatcher = ANSWER_CALL_COMMAND_PATTERN.matcher(userCommand);
        if (answerMatcher.find()) {
            answerCall(false);
            return true;
        }
        
        // Check for answer and speak commands
        Matcher answerSpeakMatcher = ANSWER_AND_SPEAK_COMMAND_PATTERN.matcher(userCommand);
        if (answerSpeakMatcher.find()) {
            answerCall(true);
            return true;
        }
        
        // Check for hang up commands
        Matcher hangUpMatcher = HANG_UP_COMMAND_PATTERN.matcher(userCommand);
        if (hangUpMatcher.find()) {
            hangUpCall();
            return true;
        }
        
        // Fallback: Use HybridAI to parse the command
        Log.d(TAG, "No pattern matched, using HybridAI to parse command");
        
        String prompt = "Parse this call command: " + userCommand + 
                       ". Extract: action (call/answer/hangup), recipient name (if any), and whether AI should speak. " +
                       "Return JSON format: {\"action\": \"call|answer|hangup\", \"recipient\": \"name or null\", \"aiSpeaking\": true|false}";
        
        final boolean[] handled = {false};
        final Object lock = new Object();
        
        hybridAI.processQuery(prompt, null, 0.0f, new HybridAILearningSystem.ResponseCallback() {
            @Override
            public void onResponse(String response, String source) {
                try {
                    // Parse JSON response
                    if (response.contains("{") && response.contains("}")) {
                        String jsonPart = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
                        
                        // Simple JSON parsing
                        boolean aiSpeaking = jsonPart.contains("\"aiSpeaking\": true") || jsonPart.contains("\"aiSpeaking\":true");
                        String action = null;
                        String recipient = null;
                        
                        if (jsonPart.contains("\"action\": \"call\"") || jsonPart.contains("\"action\":\"call\"")) {
                            action = "call";
                        } else if (jsonPart.contains("\"action\": \"answer\"") || jsonPart.contains("\"action\":\"answer\"")) {
                            action = "answer";
                        } else if (jsonPart.contains("\"action\": \"hangup\"") || jsonPart.contains("\"action\":\"hangup\"")) {
                            action = "hangup";
                        }
                        
                        // Extract recipient
                        int recipientStart = jsonPart.indexOf("\"recipient\": \"");
                        if (recipientStart > 0) {
                            recipientStart += "\"recipient\": \"".length();
                            int recipientEnd = jsonPart.indexOf("\"", recipientStart);
                            if (recipientEnd > recipientStart) {
                                recipient = jsonPart.substring(recipientStart, recipientEnd);
                                if (recipient.equals("null")) recipient = null;
                            }
                        }
                        
                        // Execute action
                        if ("call".equals(action) && recipient != null) {
                            makeCall(recipient, aiSpeaking);
                            handled[0] = true;
                        } else if ("answer".equals(action)) {
                            answerCall(aiSpeaking);
                            handled[0] = true;
                        } else if ("hangup".equals(action)) {
                            hangUpCall();
                            handled[0] = true;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing HybridAI response", e);
                }
                
                synchronized (lock) {
                    lock.notify();
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "HybridAI error: " + error);
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        
        // Wait for response with timeout
        synchronized (lock) {
            try {
                lock.wait(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while waiting for HybridAI", e);
            }
        }
        
        return handled[0];
    }
    
    /**
     * Make a call to the specified recipient
     */
    private void makeCall(String recipient, boolean aiSpeaking) {
        Log.d(TAG, "Making call to: " + recipient + " (AI speaking: " + aiSpeaking + ")");
        
        // First try to find the recipient as a contact
        String phoneNumber = callInitiationService.findContactByName(recipient);
        
        if (phoneNumber == null) {
            // Not a saved contact, check if the recipient might be a phone number
            if (recipient.replaceAll("[^0-9]", "").length() >= 10) {
                // Looks like a phone number, use it directly
                phoneNumber = recipient.replaceAll("[^0-9+]", "");
            } else {
                // Cannot determine phone number
                voiceSystem.speak("I'm sorry, I couldn't find a contact named " + recipient + 
                                 ". Would you like to save this contact?", "concerned", 0.7f);
                return;
            }
        }
        
        // Confirm the call is being made
        if (aiSpeaking) {
            voiceSystem.speak("I'll call " + recipient + " and speak on your behalf.", "helpful", 0.8f);
        } else {
            voiceSystem.speak("Calling " + recipient + " now.", "helpful", 0.8f);
        }
        
        // Update the call handling system
        callHandlingSystem.initiateCall(phoneNumber, aiSpeaking);
        
        // Make the actual call
        callInitiationService.makeCall(phoneNumber, recipient, aiSpeaking);
    }
    
    /**
     * Answer an incoming call
     */
    private void answerCall(boolean aiSpeaking) {
        Log.d(TAG, "Answering call (AI speaking: " + aiSpeaking + ")");
        
        // Enable AI call handling if requested
        if (aiSpeaking) {
            aiStateManager.setUserPreference("ai_call_handling", "true");
            voiceSystem.speak("I'll answer and speak on your behalf.", "helpful", 0.8f);
        } else {
            aiStateManager.setUserPreference("ai_call_handling", "false");
            voiceSystem.speak("Answering call now.", "helpful", 0.8f);
        }
        
        // The actual call answering is handled by AICallService
        // This just sets the preferences that control the behavior
    }
    
    /**
     * Hang up the current call
     */
    private void hangUpCall() {
        Log.d(TAG, "Hanging up call");
        
        // In a real implementation, this would use the telecom framework
        // to end the active call
        voiceSystem.speak("Ending call now.", "neutral", 0.7f);
        
        // The actual call disconnection is handled by AICallService
    }
}