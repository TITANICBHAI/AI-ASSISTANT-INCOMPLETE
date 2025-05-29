package com.aiassistant.core.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.memory.MemoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Voice manager
 * Handles speech recognition and text-to-speech
 */
public class VoiceManager {
    private static final String TAG = "VoiceManager";
    
    // Singleton instance
    private static volatile VoiceManager instance;
    
    // Context
    private final Context context;
    
    // Speech recognizer
    private SpeechRecognizer speechRecognizer;
    
    // Text-to-speech
    private TextToSpeech textToSpeech;
    
    // AI state manager
    private final AIStateManager aiStateManager;
    
    // Memory manager
    private final MemoryManager memoryManager;
    
    // Voice input listeners
    private final CopyOnWriteArrayList<VoiceInputListener> voiceInputListeners;
    
    // Voice output listeners
    private final CopyOnWriteArrayList<VoiceOutputListener> voiceOutputListeners;
    
    // Current voice recognition context
    private String recognitionContext;
    
    // Is speech recognition active
    private boolean isRecognizing;
    
    // Is text-to-speech active
    private boolean isSpeaking;
    
    // Voice properties
    private VoiceProperties voiceProperties;
    
    /**
     * Get singleton instance
     * @param context Application context
     * @return VoiceManager instance
     */
    public static synchronized VoiceManager getInstance(Context context) {
        if (instance == null) {
            instance = new VoiceManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Private constructor
     * @param context Application context
     */
    private VoiceManager(Context context) {
        this.context = context;
        this.voiceInputListeners = new CopyOnWriteArrayList<>();
        this.voiceOutputListeners = new CopyOnWriteArrayList<>();
        this.aiStateManager = AIStateManager.getInstance(context);
        this.memoryManager = MemoryManager.getInstance(context);
        this.voiceProperties = new VoiceProperties();
        
        initializeSpeechRecognizer();
        initializeTextToSpeech();
        
        Log.d(TAG, "Voice manager initialized");
    }
    
    /**
     * Initialize speech recognizer
     */
    private void initializeSpeechRecognizer() {
        try {
            // Check if speech recognition is available
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.e(TAG, "Speech recognition is not available on this device");
                return;
            }
            
            // Create speech recognizer
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
            
            Log.d(TAG, "Speech recognizer initialized");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing speech recognizer: " + e.getMessage());
        }
    }
    
    /**
     * Initialize text-to-speech
     */
    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Set language
                int result = textToSpeech.setLanguage(Locale.US);
                
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Text-to-speech language not supported");
                } else {
                    Log.d(TAG, "Text-to-speech initialized");
                    
                    // Set utterance progress listener
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            isSpeaking = true;
                            notifyOutputStarted(utteranceId);
                        }
                        
                        @Override
                        public void onDone(String utteranceId) {
                            isSpeaking = false;
                            notifyOutputCompleted(utteranceId);
                        }
                        
                        @Override
                        public void onError(String utteranceId) {
                            isSpeaking = false;
                            notifyOutputError(utteranceId, "Error in speech output");
                        }
                    });
                }
            } else {
                Log.e(TAG, "Text-to-speech initialization failed");
            }
        });
    }
    
    /**
     * Set recognition context
     * @param context Recognition context
     */
    public void setRecognitionContext(String context) {
        this.recognitionContext = context;
        Log.d(TAG, "Recognition context set to: " + context);
    }
    
    /**
     * Start speech recognition
     */
    public void startListening() {
        if (speechRecognizer == null) {
            Log.e(TAG, "Speech recognizer not initialized");
            return;
        }
        
        if (isRecognizing) {
            stopListening();
        }
        
        try {
            // Create recognition intent
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            
            // Start listening
            speechRecognizer.startListening(intent);
            isRecognizing = true;
            
            // Notify listeners
            notifyListeningStarted();
            
            Log.d(TAG, "Started listening for speech input");
        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition: " + e.getMessage());
            notifyInputError("Failed to start speech recognition");
        }
    }
    
    /**
     * Stop speech recognition
     */
    public void stopListening() {
        if (speechRecognizer != null && isRecognizing) {
            try {
                speechRecognizer.stopListening();
                isRecognizing = false;
                
                // Notify listeners
                notifyListeningStopped();
                
                Log.d(TAG, "Stopped listening for speech input");
            } catch (Exception e) {
                Log.e(TAG, "Error stopping speech recognition: " + e.getMessage());
            }
        }
    }
    
    /**
     * Speak text using text-to-speech
     * @param text Text to speak
     * @return Utterance ID
     */
    public String speak(String text) {
        return speak(text, null);
    }
    
    /**
     * Speak text using text-to-speech with custom properties
     * @param text Text to speak
     * @param properties Voice properties (null for default)
     * @return Utterance ID
     */
    public String speak(String text, VoiceProperties properties) {
        if (textToSpeech == null) {
            Log.e(TAG, "Text-to-speech not initialized");
            return null;
        }
        
        if (text == null || text.isEmpty()) {
            Log.w(TAG, "Empty text provided for speech");
            return null;
        }
        
        // Generate utterance ID
        String utteranceId = UUID.randomUUID().toString();
        
        try {
            // Apply voice properties
            VoiceProperties propsToUse = properties != null ? properties : voiceProperties;
            
            // Set speech parameters
            textToSpeech.setPitch(propsToUse.getPitch());
            textToSpeech.setSpeechRate(propsToUse.getSpeechRate());
            
            // Create params
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            
            // Speak
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
            
            // Log and notify
            Log.d(TAG, "Speaking text: " + text);
            notifyOutputStarting(utteranceId, text);
            
            // Record in emotional memory if speaking with emotion
            if (propsToUse.getEmotion() != null) {
                memoryManager.recordEmotion(propsToUse.getEmotion(), propsToUse.getEmotionIntensity(), "speech: " + text);
            }
            
            return utteranceId;
        } catch (Exception e) {
            Log.e(TAG, "Error in text-to-speech: " + e.getMessage());
            notifyOutputError(utteranceId, "Error in speech output");
            return null;
        }
    }
    
    /**
     * Stop speaking
     */
    public void stopSpeaking() {
        if (textToSpeech != null && isSpeaking) {
            textToSpeech.stop();
            isSpeaking = false;
            Log.d(TAG, "Stopped speaking");
        }
    }
    
    /**
     * Set voice properties
     * @param properties Voice properties
     */
    public void setVoiceProperties(VoiceProperties properties) {
        this.voiceProperties = properties;
        Log.d(TAG, "Voice properties updated");
    }
    
    /**
     * Get voice properties
     * @return Voice properties
     */
    public VoiceProperties getVoiceProperties() {
        return voiceProperties;
    }
    
    /**
     * Add voice input listener
     * @param listener Voice input listener
     */
    public void addVoiceInputListener(VoiceInputListener listener) {
        if (listener != null && !voiceInputListeners.contains(listener)) {
            voiceInputListeners.add(listener);
        }
    }
    
    /**
     * Remove voice input listener
     * @param listener Voice input listener
     */
    public void removeVoiceInputListener(VoiceInputListener listener) {
        if (listener != null) {
            voiceInputListeners.remove(listener);
        }
    }
    
    /**
     * Add voice output listener
     * @param listener Voice output listener
     */
    public void addVoiceOutputListener(VoiceOutputListener listener) {
        if (listener != null && !voiceOutputListeners.contains(listener)) {
            voiceOutputListeners.add(listener);
        }
    }
    
    /**
     * Remove voice output listener
     * @param listener Voice output listener
     */
    public void removeVoiceOutputListener(VoiceOutputListener listener) {
        if (listener != null) {
            voiceOutputListeners.remove(listener);
        }
    }
    
    /**
     * Speech recognition listener
     */
    private class SpeechRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "Ready for speech");
        }
        
        @Override
        public void onBeginningOfSpeech() {
            Log.d(TAG, "Beginning of speech");
        }
        
        @Override
        public void onRmsChanged(float rmsdB) {
            // Volume level changed, not logging to avoid spam
        }
        
        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "Buffer received");
        }
        
        @Override
        public void onEndOfSpeech() {
            Log.d(TAG, "End of speech");
            isRecognizing = false;
        }
        
        @Override
        public void onError(int error) {
            isRecognizing = false;
            
            String errorMessage;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    errorMessage = "Audio recording error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    errorMessage = "Client side error";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    errorMessage = "Insufficient permissions";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    errorMessage = "Network error";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errorMessage = "Network timeout";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorMessage = "No recognition matches";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errorMessage = "Recognition service busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    errorMessage = "Server error";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    errorMessage = "No speech input";
                    break;
                default:
                    errorMessage = "Unknown error";
                    break;
            }
            
            Log.e(TAG, "Speech recognition error: " + errorMessage);
            notifyInputError(errorMessage);
        }
        
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            
            if (matches != null && !matches.isEmpty()) {
                String text = matches.get(0);
                Log.d(TAG, "Speech recognition result: " + text);
                
                // Notify listeners
                notifyInputRecognized(text);
                
                // Record in memory
                if (recognitionContext != null) {
                    memoryManager.setConversationContext(recognitionContext, text);
                }
                
                // Update AI state
                if (aiStateManager != null) {
                    aiStateManager.processVoiceInput(text, recognitionContext);
                }
            } else {
                Log.w(TAG, "No speech recognition results");
                notifyInputError("No speech recognized");
            }
        }
        
        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            
            if (matches != null && !matches.isEmpty()) {
                String text = matches.get(0);
                Log.d(TAG, "Partial speech recognition: " + text);
                
                // Notify listeners
                notifyPartialInputRecognized(text);
            }
        }
        
        @Override
        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "Speech recognition event: " + eventType);
        }
    }
    
    /**
     * Notify listeners that listening started
     */
    private void notifyListeningStarted() {
        for (VoiceInputListener listener : voiceInputListeners) {
            try {
                listener.onListeningStarted();
            } catch (Exception e) {
                Log.e(TAG, "Error in listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners that listening stopped
     */
    private void notifyListeningStopped() {
        for (VoiceInputListener listener : voiceInputListeners) {
            try {
                listener.onListeningStopped();
            } catch (Exception e) {
                Log.e(TAG, "Error in listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners that input was recognized
     * @param text Recognized text
     */
    private void notifyInputRecognized(String text) {
        for (VoiceInputListener listener : voiceInputListeners) {
            try {
                listener.onSpeechRecognized(text);
            } catch (Exception e) {
                Log.e(TAG, "Error in listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners that partial input was recognized
     * @param text Recognized text
     */
    private void notifyPartialInputRecognized(String text) {
        for (VoiceInputListener listener : voiceInputListeners) {
            try {
                listener.onPartialSpeechRecognized(text);
            } catch (Exception e) {
                Log.e(TAG, "Error in listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners of input error
     * @param errorMessage Error message
     */
    private void notifyInputError(String errorMessage) {
        for (VoiceInputListener listener : voiceInputListeners) {
            try {
                listener.onSpeechError(errorMessage);
            } catch (Exception e) {
                Log.e(TAG, "Error in listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners that output is starting
     * @param utteranceId Utterance ID
     * @param text Text being spoken
     */
    private void notifyOutputStarting(String utteranceId, String text) {
        for (VoiceOutputListener listener : voiceOutputListeners) {
            try {
                listener.onSpeechStarting(utteranceId, text);
            } catch (Exception e) {
                Log.e(TAG, "Error in listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners that output has started
     * @param utteranceId Utterance ID
     */
    private void notifyOutputStarted(String utteranceId) {
        for (VoiceOutputListener listener : voiceOutputListeners) {
            try {
                listener.onSpeechStarted(utteranceId);
            } catch (Exception e) {
                Log.e(TAG, "Error in listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners that output has completed
     * @param utteranceId Utterance ID
     */
    private void notifyOutputCompleted(String utteranceId) {
        for (VoiceOutputListener listener : voiceOutputListeners) {
            try {
                listener.onSpeechCompleted(utteranceId);
            } catch (Exception e) {
                Log.e(TAG, "Error in listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notify listeners of output error
     * @param utteranceId Utterance ID
     * @param errorMessage Error message
     */
    private void notifyOutputError(String utteranceId, String errorMessage) {
        for (VoiceOutputListener listener : voiceOutputListeners) {
            try {
                listener.onSpeechError(utteranceId, errorMessage);
            } catch (Exception e) {
                Log.e(TAG, "Error in listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Release resources
     */
    public void close() {
        // Stop speech recognition
        if (speechRecognizer != null) {
            try {
                speechRecognizer.destroy();
            } catch (Exception e) {
                Log.e(TAG, "Error destroying speech recognizer: " + e.getMessage());
            }
            speechRecognizer = null;
        }
        
        // Stop text-to-speech
        if (textToSpeech != null) {
            try {
                textToSpeech.stop();
                textToSpeech.shutdown();
            } catch (Exception e) {
                Log.e(TAG, "Error shutting down text-to-speech: " + e.getMessage());
            }
            textToSpeech = null;
        }
        
        // Clear listeners
        voiceInputListeners.clear();
        voiceOutputListeners.clear();
        
        // Clear instance
        instance = null;
        
        Log.d(TAG, "Voice manager closed");
    }
    
    /**
     * Voice input listener interface
     */
    public interface VoiceInputListener {
        /**
         * Called when listening starts
         */
        void onListeningStarted();
        
        /**
         * Called when listening stops
         */
        void onListeningStopped();
        
        /**
         * Called when speech is recognized
         * @param text Recognized text
         */
        void onSpeechRecognized(String text);
        
        /**
         * Called when partial speech is recognized
         * @param text Partially recognized text
         */
        void onPartialSpeechRecognized(String text);
        
        /**
         * Called when speech recognition error occurs
         * @param errorMessage Error message
         */
        void onSpeechError(String errorMessage);
    }
    
    /**
     * Voice output listener interface
     */
    public interface VoiceOutputListener {
        /**
         * Called when speech is about to start
         * @param utteranceId Utterance ID
         * @param text Text being spoken
         */
        void onSpeechStarting(String utteranceId, String text);
        
        /**
         * Called when speech has started
         * @param utteranceId Utterance ID
         */
        void onSpeechStarted(String utteranceId);
        
        /**
         * Called when speech has completed
         * @param utteranceId Utterance ID
         */
        void onSpeechCompleted(String utteranceId);
        
        /**
         * Called when speech error occurs
         * @param utteranceId Utterance ID
         * @param errorMessage Error message
         */
        void onSpeechError(String utteranceId, String errorMessage);
    }
}
