package com.aiassistant.core.ai.call;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.ai.memory.MemoryManager;
import com.aiassistant.core.ai.neural.EmotionalIntelligenceModel;
import com.aiassistant.core.data.model.CallerProfile;
import com.aiassistant.core.data.repository.CallerProfileRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling calls with emotional intelligence
 * Provides emotional analysis, response generation and call context management
 */
public class EmotionalCallHandlingService {
    private static final String TAG = "EmotionalCallHandler";
    
    // Components
    private final Context context;
    private final EmotionalIntelligenceModel emotionalModel;
    private final AIStateManager aiStateManager;
    private final MemoryManager memoryManager;
    private final CallerProfileRepository callerProfileRepository;
    
    // Emotional thresholds
    private static final float EMOTION_THRESHOLD_LOW = 0.3f;
    private static final float EMOTION_THRESHOLD_MEDIUM = 0.6f;
    private static final float EMOTION_THRESHOLD_HIGH = 0.8f;
    
    /**
     * Constructor
     * @param context Application context
     */
    public EmotionalCallHandlingService(Context context) {
        this.context = context;
        this.emotionalModel = new EmotionalIntelligenceModel(context);
        this.aiStateManager = AIStateManager.getInstance(context);
        this.memoryManager = MemoryManager.getInstance(context);
        this.callerProfileRepository = new CallerProfileRepository(context);
        
        // Initialize emotional model
        if (!emotionalModel.isModelLoaded()) {
            Log.d(TAG, "Loading emotional intelligence model");
            emotionalModel.loadModel();
        }
        
        Log.d(TAG, "EmotionalCallHandlingService initialized");
    }
    
    /**
     * Handle incoming call
     * @param phoneNumber Caller phone number
     * @param contactName Contact name (if available)
     */
    public void handleIncomingCall(String phoneNumber, String contactName) {
        Log.d(TAG, "Handling incoming call from " + phoneNumber + (contactName != null ? " (" + contactName + ")" : ""));
        
        // Get caller profile or create new one
        CallerProfile callerProfile = getOrCreateCallerProfile(phoneNumber, contactName);
        
        // Set AI state based on caller profile
        updateAIStateFromProfile(callerProfile);
        
        // Store call context in memory
        storeCallContextInMemory(phoneNumber, contactName, "incoming");
        
        // Generate greeting based on emotional state
        String greeting = generateGreeting(callerProfile);
        Log.d(TAG, "Generated greeting: " + greeting);
    }
    
    /**
     * Handle outgoing call
     * @param phoneNumber Call recipient phone number
     * @param contactName Contact name (if available)
     */
    public void handleOutgoingCall(String phoneNumber, String contactName) {
        Log.d(TAG, "Handling outgoing call to " + phoneNumber + (contactName != null ? " (" + contactName + ")" : ""));
        
        // Get caller profile or create new one
        CallerProfile callerProfile = getOrCreateCallerProfile(phoneNumber, contactName);
        
        // Set AI state based on caller profile
        updateAIStateFromProfile(callerProfile);
        
        // Store call context in memory
        storeCallContextInMemory(phoneNumber, contactName, "outgoing");
    }
    
    /**
     * Handle call ended
     * @param phoneNumber Phone number
     * @param callDuration Call duration in seconds
     */
    public void handleCallEnded(String phoneNumber, long callDuration) {
        Log.d(TAG, "Call ended with " + phoneNumber + ", duration: " + callDuration + " seconds");
        
        // Get caller profile
        CallerProfile callerProfile = callerProfileRepository.getCallerByPhone(phoneNumber);
        if (callerProfile == null) {
            Log.w(TAG, "No caller profile found for " + phoneNumber);
            return;
        }
        
        // Get emotional state from AI
        Map<String, Float> emotions = aiStateManager.getCurrentEmotions();
        String dominantEmotion = getDominantEmotion(emotions);
        float valence = aiStateManager.getEmotionalValence();
        float arousal = aiStateManager.getEmotionalArousal();
        
        // Update caller profile with call information
        callerProfile.updateWithCallInfo(callDuration, dominantEmotion, valence, arousal);
        
        // Save updated profile
        callerProfileRepository.updateCaller(callerProfile);
        
        // Store summary in memory
        String callSummary = "Call with " + (callerProfile.getName() != null ? callerProfile.getName() : phoneNumber) +
                " ended after " + callDuration + " seconds. Emotional state: " + dominantEmotion +
                " (V:" + valence + ", A:" + arousal + ")";
        memoryManager.storeShortTermMemory("call_summary", callSummary);
        
        // Reset AI state
        aiStateManager.resetEmotionalState();
        
        Log.d(TAG, "Call summary: " + callSummary);
    }
    
    /**
     * Handle speech detected from caller
     * @param phoneNumber Caller phone number
     * @param speechText Detected speech text
     * @return Response text (if applicable)
     */
    public String handleCallerSpeech(String phoneNumber, String speechText) {
        if (speechText == null || speechText.isEmpty()) {
            return null;
        }
        
        Log.d(TAG, "Handling speech from " + phoneNumber + ": " + speechText);
        
        // Get caller profile
        CallerProfile callerProfile = callerProfileRepository.getCallerByPhone(phoneNumber);
        if (callerProfile == null) {
            Log.w(TAG, "No caller profile found for " + phoneNumber);
            return null;
        }
        
        // Analyze emotions in speech
        Map<String, Float> emotions = emotionalModel.analyzeEmotion(speechText);
        
        // Update AI state with detected emotions
        for (Map.Entry<String, Float> emotion : emotions.entrySet()) {
            if (emotion.getValue() > EMOTION_THRESHOLD_LOW) {
                aiStateManager.updateEmotionalState(emotion.getKey(), emotion.getValue());
            }
        }
        
        // Store speech in memory
        memoryManager.storeShortTermMemory("last_caller_speech", speechText);
        
        // Generate response if needed
        if (shouldRespondToSpeech(speechText, emotions)) {
            return generateResponseToSpeech(speechText, callerProfile, emotions);
        }
        
        return null;
    }
    
    /**
     * Generate response during call
     * @param phoneNumber Caller phone number
     * @param prompt Prompt for response generation
     * @return Generated response
     */
    public String generateResponseDuringCall(String phoneNumber, String prompt) {
        Log.d(TAG, "Generating response for " + phoneNumber + " with prompt: " + prompt);
        
        // Get caller profile
        CallerProfile callerProfile = callerProfileRepository.getCallerByPhone(phoneNumber);
        if (callerProfile == null) {
            Log.w(TAG, "No caller profile found for " + phoneNumber);
            return getGenericResponse();
        }
        
        // Get current emotional state
        Map<String, Float> emotions = aiStateManager.getCurrentEmotions();
        String dominantEmotion = getDominantEmotion(emotions);
        
        // Generate personalized response
        String response;
        if (prompt != null && !prompt.isEmpty()) {
            response = generateResponseToSpeech(prompt, callerProfile, emotions);
        } else {
            response = generateGenericResponse(callerProfile, dominantEmotion);
        }
        
        Log.d(TAG, "Generated response: " + response);
        return response;
    }
    
    /**
     * Get or create caller profile
     * @param phoneNumber Phone number
     * @param contactName Contact name (optional)
     * @return CallerProfile object
     */
    private CallerProfile getOrCreateCallerProfile(String phoneNumber, String contactName) {
        // Try to get existing profile
        CallerProfile callerProfile = callerProfileRepository.getCallerByPhone(phoneNumber);
        
        // Create new profile if not found
        if (callerProfile == null) {
            Log.d(TAG, "Creating new caller profile for " + phoneNumber);
            callerProfile = new CallerProfile(phoneNumber, contactName);
            callerProfileRepository.addCaller(callerProfile);
        } 
        // Update contact name if available
        else if (contactName != null && !contactName.isEmpty() && 
                (callerProfile.getName() == null || callerProfile.getName().isEmpty())) {
            callerProfile.setName(contactName);
            callerProfileRepository.updateCaller(callerProfile);
        }
        
        return callerProfile;
    }
    
    /**
     * Update AI state from caller profile
     * @param callerProfile Caller profile
     */
    private void updateAIStateFromProfile(CallerProfile callerProfile) {
        // Reset to neutral state
        aiStateManager.resetEmotionalState();
        
        // If we have previous emotional data, use it as baseline
        if (callerProfile.getLastEmotion() != null) {
            float baselineStrength = EMOTION_THRESHOLD_MEDIUM;
            aiStateManager.updateEmotionalState(callerProfile.getLastEmotion(), baselineStrength);
            aiStateManager.setEmotionalValence(callerProfile.getEmotionalValence());
            aiStateManager.setEmotionalArousal(callerProfile.getEmotionalArousal());
            
            Log.d(TAG, "Set baseline emotional state to " + callerProfile.getLastEmotion() + 
                    " (V:" + callerProfile.getEmotionalValence() + ", A:" + callerProfile.getEmotionalArousal() + ")");
        }
        
        // Set other AI states based on caller profile
        aiStateManager.setCurrentContext("call");
        
        // Load caller's memory
        memoryManager.loadMemoryByTag(callerProfile.getPhoneNumber());
    }
    
    /**
     * Store call context in memory
     * @param phoneNumber Phone number
     * @param contactName Contact name
     * @param callType Call type (incoming/outgoing)
     */
    private void storeCallContextInMemory(String phoneNumber, String contactName, String callType) {
        String callerName = contactName != null ? contactName : phoneNumber;
        String memoryKey = callType + "_call_" + System.currentTimeMillis();
        String memoryValue = callType + " call with " + callerName + " started at " + 
                new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
        
        memoryManager.storeShortTermMemory(memoryKey, memoryValue);
        memoryManager.tagMemory(memoryKey, phoneNumber);
        
        Log.d(TAG, "Stored call context in memory: " + memoryValue);
    }
    
    /**
     * Generate greeting based on caller profile
     * @param callerProfile Caller profile
     * @return Greeting text
     */
    private String generateGreeting(CallerProfile callerProfile) {
        String name = callerProfile.getName() != null ? callerProfile.getName() : "there";
        
        // First time caller
        if (callerProfile.getCallCount() == 0) {
            return "Hello " + name + ", it's nice to talk to you for the first time.";
        }
        
        // Return caller
        String greeting = "Hello again " + name + ".";
        
        // Add personalized touch based on call history
        if (callerProfile.getCallCount() > 5) {
            greeting += " It's always good to hear from you.";
        }
        
        // Add emotional component if available
        if (callerProfile.getLastEmotion() != null) {
            switch (callerProfile.getLastEmotion().toLowerCase()) {
                case "happy":
                case "joy":
                    greeting += " I remember you were in a great mood last time!";
                    break;
                case "sad":
                case "depressed":
                    greeting += " I hope you're feeling better today.";
                    break;
                case "angry":
                case "upset":
                    greeting += " I hope today is going well for you.";
                    break;
                case "neutral":
                    // No special addition
                    break;
                default:
                    // No special addition
                    break;
            }
        }
        
        return greeting;
    }
    
    /**
     * Determine if we should respond to speech
     * @param speechText Detected speech text
     * @param emotions Detected emotions
     * @return True if should respond
     */
    private boolean shouldRespondToSpeech(String speechText, Map<String, Float> emotions) {
        // Respond to direct questions
        if (speechText.contains("?")) {
            return true;
        }
        
        // Respond to strong emotions
        for (float strength : emotions.values()) {
            if (strength > EMOTION_THRESHOLD_HIGH) {
                return true;
            }
        }
        
        // Respond to certain keywords
        String[] triggerWords = {"help", "please", "thanks", "thank you", "hello", "hi"};
        for (String word : triggerWords) {
            if (speechText.toLowerCase().contains(word)) {
                return true;
            }
        }
        
        // Default is to not respond to every speech
        return false;
    }
    
    /**
     * Generate response to speech
     * @param speechText Detected speech text
     * @param callerProfile Caller profile
     * @param emotions Detected emotions
     * @return Response text
     */
    private String generateResponseToSpeech(String speechText, CallerProfile callerProfile, Map<String, Float> emotions) {
        // Check for questions
        if (speechText.contains("?")) {
            return generateQuestionResponse(speechText, callerProfile);
        }
        
        // Check for greetings
        String lowerSpeech = speechText.toLowerCase();
        if (lowerSpeech.contains("hello") || lowerSpeech.contains("hi ") || 
                lowerSpeech.equals("hi") || lowerSpeech.contains("hey")) {
            String name = callerProfile.getName() != null ? callerProfile.getName() : "there";
            return "Hello " + name + ", how can I assist you today?";
        }
        
        // Check for thanks
        if (lowerSpeech.contains("thank") || lowerSpeech.contains("thanks")) {
            return "You're welcome! I'm happy to help.";
        }
        
        // Respond based on dominant emotion
        String dominantEmotion = getDominantEmotion(emotions);
        float dominantStrength = emotions.getOrDefault(dominantEmotion, 0.0f);
        
        if (dominantStrength > EMOTION_THRESHOLD_HIGH) {
            return generateEmotionalResponse(dominantEmotion, callerProfile);
        }
        
        // Default response
        return getGenericResponse();
    }
    
    /**
     * Generate response to a question
     * @param question Question text
     * @param callerProfile Caller profile
     * @return Response text
     */
    private String generateQuestionResponse(String question, CallerProfile callerProfile) {
        String lowerQuestion = question.toLowerCase();
        
        // Handle common questions
        if (lowerQuestion.contains("who are you") || lowerQuestion.contains("what are you")) {
            return "I'm an AI assistant designed to help with your calls and provide information.";
        }
        
        if (lowerQuestion.contains("how are you")) {
            return "I'm functioning well, thank you for asking! How can I help you today?";
        }
        
        if (lowerQuestion.contains("time") && lowerQuestion.contains("what")) {
            return "The current time is " + new java.text.SimpleDateFormat("h:mm a").format(new java.util.Date());
        }
        
        if (lowerQuestion.contains("date") && lowerQuestion.contains("what")) {
            return "Today is " + new java.text.SimpleDateFormat("EEEE, MMMM d").format(new java.util.Date());
        }
        
        if (lowerQuestion.contains("help") || lowerQuestion.contains("can you")) {
            return "I can help with information, take notes during your call, or assist with scheduling. What would you like help with?";
        }
        
        // Default question response
        return "That's an interesting question. I'll do my best to help you with that.";
    }
    
    /**
     * Generate emotional response
     * @param emotion Dominant emotion
     * @param callerProfile Caller profile
     * @return Response text
     */
    private String generateEmotionalResponse(String emotion, CallerProfile callerProfile) {
        String name = callerProfile.getName() != null ? callerProfile.getName() : "";
        
        switch (emotion.toLowerCase()) {
            case "happy":
            case "joy":
                return "I'm glad you're feeling positive" + (name.isEmpty() ? "!" : ", " + name + "!");
                
            case "sad":
            case "depressed":
                return "I understand you might be feeling down" + (name.isEmpty() ? "." : ", " + name + ".") + 
                       " Is there anything I can help with?";
                
            case "angry":
            case "upset":
                return "I understand you might be frustrated" + (name.isEmpty() ? "." : ", " + name + ".") + 
                       " Let me know if there's a way I can help address your concerns.";
                
            case "surprised":
                return "That does sound surprising" + (name.isEmpty() ? "!" : ", " + name + "!") + 
                       " I'm here to help process this new information.";
                
            case "fear":
            case "anxious":
                return "I understand this might be concerning" + (name.isEmpty() ? "." : ", " + name + ".") + 
                       " Let me know if I can help in any way.";
                
            case "neutral":
            default:
                return getGenericResponse();
        }
    }
    
    /**
     * Generate generic response
     * @param callerProfile Caller profile
     * @param emotion Current emotion
     * @return Generic response text
     */
    private String generateGenericResponse(CallerProfile callerProfile, String emotion) {
        String name = callerProfile.getName() != null ? callerProfile.getName() : "";
        
        // Array of possible generic responses
        String[] responses = {
            "I'm listening" + (name.isEmpty() ? "." : ", " + name + "."),
            "Please continue, I'm here to help.",
            "I understand. What else would you like to discuss?",
            "I appreciate your input. How else can I assist you?",
            "Thank you for sharing that. Is there anything else you'd like to add?"
        };
        
        // Select random response
        int index = (int)(Math.random() * responses.length);
        return responses[index];
    }
    
    /**
     * Get generic response
     * @return Generic response text
     */
    private String getGenericResponse() {
        // Array of possible generic responses
        String[] responses = {
            "I understand. How else can I help?",
            "I'm here to assist you. What else would you like to discuss?",
            "Thank you for sharing that. Is there anything else I can help with?",
            "I appreciate your input. Is there anything specific you need assistance with?",
            "I'm listening and ready to help. What would you like to know?"
        };
        
        // Select random response
        int index = (int)(Math.random() * responses.length);
        return responses[index];
    }
    
    /**
     * Get dominant emotion from emotions map
     * @param emotions Map of emotions and their strengths
     * @return Dominant emotion name
     */
    private String getDominantEmotion(Map<String, Float> emotions) {
        if (emotions == null || emotions.isEmpty()) {
            return "neutral";
        }
        
        String dominant = "neutral";
        float maxStrength = 0.0f;
        
        for (Map.Entry<String, Float> entry : emotions.entrySet()) {
            if (entry.getValue() > maxStrength) {
                maxStrength = entry.getValue();
                dominant = entry.getKey();
            }
        }
        
        // If no strong emotion, return neutral
        if (maxStrength < EMOTION_THRESHOLD_LOW) {
            return "neutral";
        }
        
        return dominant;
    }
}
