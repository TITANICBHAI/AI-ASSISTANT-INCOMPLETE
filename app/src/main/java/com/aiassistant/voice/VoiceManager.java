package com.aiassistant.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

/**
 * Manages voice input and output
 * Handles speech recognition and text-to-speech
 */
public class VoiceManager {
    private static final String TAG = "VoiceManager";
    
    // Context and handlers
    private final Context context;
    private final Handler mainHandler;
    
    // Speech recognition
    private SpeechRecognizer speechRecognizer;
    private boolean isListening;
    private VoiceRecognitionCallback recognitionCallback;
    
    // Text-to-speech
    private TextToSpeech textToSpeech;
    private boolean isSpeaking;
    private VoiceSpeakingCallback speakingCallback;
    
    // Speech language
    private Locale speechLocale;
    
    /**
     * Constructor
     * @param context Application context
     */
    public VoiceManager(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.isListening = false;
        this.isSpeaking = false;
        this.speechLocale = Locale.US;
        
        // Initialize components
        initializeSpeechRecognizer();
        initializeTextToSpeech();
        
        Log.d(TAG, "VoiceManager initialized");
    }
    
    /**
     * Initialize speech recognizer
     */
    private void initializeSpeechRecognizer() {
        // Check if device supports speech recognition
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Speech recognition not available on this device");
            return;
        }
        
        // Create recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(createRecognitionListener());
        
        Log.d(TAG, "Speech recognizer initialized");
    }
    
    /**
     * Initialize text-to-speech
     */
    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Set language
                int result = textToSpeech.setLanguage(speechLocale);
                
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported: " + speechLocale);
                } else {
                    Log.d(TAG, "Text-to-speech initialized successfully");
                }
            } else {
                Log.e(TAG, "Failed to initialize text-to-speech");
            }
        });
        
        // Set progress listener
        textToSpeech.setOnUtteranceProgressListener(createUtteranceProgressListener());
    }
    
    /**
     * Start listening for speech
     * @param callback Callback for recognition results
     */
    public void startListening(VoiceRecognitionCallback callback) {
        if (isListening) {
            stopListening();
        }
        
        if (speechRecognizer == null) {
            Log.e(TAG, "Speech recognizer not available");
            if (callback != null) {
                callback.onError("Speech recognizer not available");
            }
            return;
        }
        
        // Set callback
        this.recognitionCallback = callback;
        
        // Create intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, speechLocale);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        
        try {
            speechRecognizer.startListening(intent);
            isListening = true;
            Log.d(TAG, "Started listening for speech");
            
            if (callback != null) {
                callback.onListeningStarted();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition: " + e.getMessage());
            isListening = false;
            
            if (callback != null) {
                callback.onError("Error starting speech recognition: " + e.getMessage());
            }
        }
    }
    
    /**
     * Stop listening for speech
     */
    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            Log.d(TAG, "Stopped listening for speech");
            
            if (recognitionCallback != null) {
                recognitionCallback.onListeningStopped();
            }
        }
    }
    
    /**
     * Speak text
     * @param text Text to speak
     */
    public void speak(String text) {
        speak(text, null);
    }
    
    /**
     * Speak text with callback
     * @param text Text to speak
     * @param callback Callback for speaking events
     */
    public void speak(String text, VoiceSpeakingCallback callback) {
        if (textToSpeech == null) {
            Log.e(TAG, "Text-to-speech not initialized");
            if (callback != null) {
                callback.onError("Text-to-speech not initialized");
            }
            return;
        }
        
        // Stop if already speaking
        if (isSpeaking) {
            stopSpeaking();
        }
        
        // Set callback
        this.speakingCallback = callback;
        
        // Generate utterance ID
        String utteranceId = UUID.randomUUID().toString();
        
        // Create params
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        
        // Speak
        int result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
        
        if (result == TextToSpeech.SUCCESS) {
            isSpeaking = true;
            Log.d(TAG, "Speaking text: " + text);
            
            if (callback != null) {
                callback.onSpeakingStarted();
            }
        } else {
            Log.e(TAG, "Error speaking text: " + result);
            
            if (callback != null) {
                callback.onError("Error speaking text: " + result);
            }
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
            
            if (speakingCallback != null) {
                speakingCallback.onSpeakingStopped();
            }
        }
    }
    
    /**
     * Set speech language
     * @param locale Language locale
     * @return True if language was set successfully
     */
    public boolean setSpeechLanguage(Locale locale) {
        if (textToSpeech == null) {
            Log.e(TAG, "Text-to-speech not initialized");
            return false;
        }
        
        int result = textToSpeech.setLanguage(locale);
        
        if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e(TAG, "Language not supported: " + locale);
            return false;
        }
        
        speechLocale = locale;
        Log.d(TAG, "Speech language set to: " + locale);
        return true;
    }
    
    /**
     * Get current speech language
     * @return Language locale
     */
    public Locale getSpeechLanguage() {
        return speechLocale;
    }
    
    /**
     * Check if speech recognizer is listening
     * @return True if listening
     */
    public boolean isListening() {
        return isListening;
    }
    
    /**
     * Check if text-to-speech is speaking
     * @return True if speaking
     */
    public boolean isSpeaking() {
        return isSpeaking;
    }
    
    /**
     * Release resources
     */
    public void release() {
        // Release speech recognizer
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        
        // Release text-to-speech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        
        Log.d(TAG, "VoiceManager released");
    }
    
    /**
     * Create recognition listener
     * @return RecognitionListener instance
     */
    private RecognitionListener createRecognitionListener() {
        return new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
                isListening = true;
                
                if (recognitionCallback != null) {
                    mainHandler.post(() -> recognitionCallback.onListeningStarted());
                }
            }
            
            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Beginning of speech");
                
                if (recognitionCallback != null) {
                    mainHandler.post(() -> recognitionCallback.onSpeechDetected());
                }
            }
            
            @Override
            public void onRmsChanged(float rmsdB) {
                // Update audio level
                if (recognitionCallback != null) {
                    mainHandler.post(() -> recognitionCallback.onAudioLevelChanged(rmsdB));
                }
            }
            
            @Override
            public void onBufferReceived(byte[] buffer) {
                // Buffer received, no action needed
            }
            
            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "End of speech");
                
                if (recognitionCallback != null) {
                    mainHandler.post(() -> recognitionCallback.onSpeechEnded());
                }
            }
            
            @Override
            public void onError(int error) {
                String errorMessage = getErrorMessage(error);
                Log.e(TAG, "Speech recognition error: " + errorMessage);
                isListening = false;
                
                if (recognitionCallback != null) {
                    mainHandler.post(() -> {
                        recognitionCallback.onError(errorMessage);
                        recognitionCallback.onListeningStopped();
                    });
                }
            }
            
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.d(TAG, "Speech recognition results: " + matches);
                isListening = false;
                
                if (recognitionCallback != null && matches != null && !matches.isEmpty()) {
                    mainHandler.post(() -> {
                        recognitionCallback.onResults(matches);
                        recognitionCallback.onListeningStopped();
                    });
                }
            }
            
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                
                if (recognitionCallback != null && matches != null && !matches.isEmpty()) {
                    mainHandler.post(() -> recognitionCallback.onPartialResults(matches));
                }
            }
            
            @Override
            public void onEvent(int eventType, Bundle params) {
                // Event received, no action needed
            }
        };
    }
    
    /**
     * Create utterance progress listener
     * @return UtteranceProgressListener instance
     */
    private UtteranceProgressListener createUtteranceProgressListener() {
        return new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.d(TAG, "Speaking started: " + utteranceId);
                isSpeaking = true;
                
                if (speakingCallback != null) {
                    mainHandler.post(() -> speakingCallback.onSpeakingStarted());
                }
            }
            
            @Override
            public void onDone(String utteranceId) {
                Log.d(TAG, "Speaking done: " + utteranceId);
                isSpeaking = false;
                
                if (speakingCallback != null) {
                    mainHandler.post(() -> {
                        speakingCallback.onSpeakingFinished();
                        speakingCallback.onSpeakingStopped();
                    });
                }
            }
            
            @Override
            public void onError(String utteranceId) {
                Log.e(TAG, "Speaking error: " + utteranceId);
                isSpeaking = false;
                
                if (speakingCallback != null) {
                    mainHandler.post(() -> {
                        speakingCallback.onError("Speaking error: " + utteranceId);
                        speakingCallback.onSpeakingStopped();
                    });
                }
            }
        };
    }
    
    /**
     * Get error message for speech recognition error code
     * @param errorCode Error code
     * @return Error message
     */
    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No recognition matches";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error: " + errorCode;
        }
    }
    
    /**
     * Callback interface for speech recognition
     */
    public interface VoiceRecognitionCallback {
        /**
         * Called when listening starts
         */
        void onListeningStarted();
        
        /**
         * Called when speech is detected
         */
        void onSpeechDetected();
        
        /**
         * Called when speech ends
         */
        void onSpeechEnded();
        
        /**
         * Called when listening stops
         */
        void onListeningStopped();
        
        /**
         * Called when partial results are available
         * @param results List of partial results
         */
        void onPartialResults(ArrayList<String> results);
        
        /**
         * Called when final results are available
         * @param results List of final results
         */
        void onResults(ArrayList<String> results);
        
        /**
         * Called when audio level changes
         * @param rmsdB Audio level in dB
         */
        void onAudioLevelChanged(float rmsdB);
        
        /**
         * Called when an error occurs
         * @param error Error message
         */
        void onError(String error);
    }
    
    /**
     * Callback interface for text-to-speech
     */
    public interface VoiceSpeakingCallback {
        /**
         * Called when speaking starts
         */
        void onSpeakingStarted();
        
        /**
         * Called when speaking finishes
         */
        void onSpeakingFinished();
        
        /**
         * Called when speaking stops (due to finish or cancel)
         */
        void onSpeakingStopped();
        
        /**
         * Called when an error occurs
         * @param error Error message
         */
        void onError(String error);
    }
}
