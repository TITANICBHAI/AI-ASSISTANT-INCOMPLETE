package com.aiassistant.voice;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Speech synthesis manager for text-to-speech functionality
 */
public class SpeechSynthesisManager {
    private static final String TAG = "SpeechSynthesis";
    
    private Context context;
    private TextToSpeech textToSpeech;
    private boolean initialized;
    private Map<String, OnSpeechCompletedListener> listeners;
    private Locale currentLocale;
    private float speechRate;
    private float pitch;
    private int emotionMode;
    
    /**
     * Emotion modes
     */
    public static final int EMOTION_NEUTRAL = 0;
    public static final int EMOTION_HAPPY = 1;
    public static final int EMOTION_SAD = 2;
    public static final int EMOTION_ANGRY = 3;
    public static final int EMOTION_SURPRISED = 4;
    
    /**
     * Constructor
     */
    public SpeechSynthesisManager(Context context) {
        this.context = context;
        this.initialized = false;
        this.listeners = new HashMap<>();
        this.currentLocale = Locale.US;
        this.speechRate = 1.0f;
        this.pitch = 1.0f;
        this.emotionMode = EMOTION_NEUTRAL;
    }
    
    /**
     * Initialize the manager
     * @return True if initialization successful
     */
    public boolean initialize() {
        Log.d(TAG, "Initializing speech synthesis manager");
        
        try {
            textToSpeech = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(currentLocale);
                    
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "Language not supported: " + currentLocale);
                        initialized = false;
                    } else {
                        textToSpeech.setPitch(pitch);
                        textToSpeech.setSpeechRate(speechRate);
                        
                        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {
                                Log.d(TAG, "Speech started: " + utteranceId);
                            }
                            
                            @Override
                            public void onDone(String utteranceId) {
                                Log.d(TAG, "Speech completed: " + utteranceId);
                                notifySpeechCompleted(utteranceId);
                            }
                            
                            @Override
                            public void onError(String utteranceId) {
                                Log.e(TAG, "Speech error: " + utteranceId);
                                notifySpeechError(utteranceId);
                            }
                        });
                        
                        initialized = true;
                        Log.d(TAG, "Speech synthesis initialized successfully");
                    }
                } else {
                    Log.e(TAG, "Failed to initialize TextToSpeech");
                    initialized = false;
                }
            });
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing speech synthesis: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Speak text
     * @param text Text to speak
     * @return Utterance ID
     */
    public String speak(String text) {
        return speak(text, null);
    }
    
    /**
     * Speak text with completion listener
     * @param text Text to speak
     * @param listener Completion listener
     * @return Utterance ID
     */
    public String speak(String text, OnSpeechCompletedListener listener) {
        if (!initialized) {
            Log.w(TAG, "Manager not initialized");
            return null;
        }
        
        if (text == null || text.isEmpty()) {
            Log.w(TAG, "Empty text provided");
            return null;
        }
        
        try {
            String utteranceId = UUID.randomUUID().toString();
            
            if (listener != null) {
                listeners.put(utteranceId, listener);
            }
            
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            
            // Apply emotion-specific parameters
            applyEmotionParameters(params);
            
            int result = textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, params);
            
            if (result == TextToSpeech.ERROR) {
                Log.e(TAG, "Error speaking text");
                return null;
            }
            
            Log.d(TAG, "Speaking text: " + text);
            return utteranceId;
            
        } catch (Exception e) {
            Log.e(TAG, "Error speaking text: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Apply emotion-specific parameters
     * @param params Parameters map
     */
    private void applyEmotionParameters(Map<String, String> params) {
        switch (emotionMode) {
            case EMOTION_HAPPY:
                textToSpeech.setPitch(pitch * 1.1f);
                textToSpeech.setSpeechRate(speechRate * 1.1f);
                break;
            case EMOTION_SAD:
                textToSpeech.setPitch(pitch * 0.9f);
                textToSpeech.setSpeechRate(speechRate * 0.8f);
                break;
            case EMOTION_ANGRY:
                textToSpeech.setPitch(pitch * 0.8f);
                textToSpeech.setSpeechRate(speechRate * 1.2f);
                break;
            case EMOTION_SURPRISED:
                textToSpeech.setPitch(pitch * 1.2f);
                textToSpeech.setSpeechRate(speechRate * 1.0f);
                break;
            case EMOTION_NEUTRAL:
            default:
                textToSpeech.setPitch(pitch);
                textToSpeech.setSpeechRate(speechRate);
                break;
        }
    }
    
    /**
     * Stop speaking
     */
    public void stop() {
        if (initialized && textToSpeech != null) {
            textToSpeech.stop();
            Log.d(TAG, "Stopped speaking");
        }
    }
    
    /**
     * Set speech rate
     * @param rate Speech rate (0.0-2.0)
     */
    public void setSpeechRate(float rate) {
        if (rate < 0.1f) rate = 0.1f;
        if (rate > 2.0f) rate = 2.0f;
        
        this.speechRate = rate;
        
        if (initialized && textToSpeech != null) {
            textToSpeech.setSpeechRate(rate);
        }
    }
    
    /**
     * Get speech rate
     * @return Speech rate
     */
    public float getSpeechRate() {
        return speechRate;
    }
    
    /**
     * Set pitch
     * @param pitch Pitch (0.0-2.0)
     */
    public void setPitch(float pitch) {
        if (pitch < 0.1f) pitch = 0.1f;
        if (pitch > 2.0f) pitch = 2.0f;
        
        this.pitch = pitch;
        
        if (initialized && textToSpeech != null) {
            textToSpeech.setPitch(pitch);
        }
    }
    
    /**
     * Get pitch
     * @return Pitch
     */
    public float getPitch() {
        return pitch;
    }
    
    /**
     * Set language
     * @param locale Language locale
     * @return True if language set successfully
     */
    public boolean setLanguage(Locale locale) {
        if (!initialized || textToSpeech == null) {
            this.currentLocale = locale;
            return false;
        }
        
        int result = textToSpeech.setLanguage(locale);
        
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e(TAG, "Language not supported: " + locale);
            return false;
        }
        
        this.currentLocale = locale;
        return true;
    }
    
    /**
     * Get current language
     * @return Language locale
     */
    public Locale getLanguage() {
        return currentLocale;
    }
    
    /**
     * Set emotion mode
     * @param emotionMode Emotion mode
     */
    public void setEmotionMode(int emotionMode) {
        this.emotionMode = emotionMode;
        
        // Update parameters for next speech
        HashMap<String, String> params = new HashMap<>();
        applyEmotionParameters(params);
    }
    
    /**
     * Get emotion mode
     * @return Emotion mode
     */
    public int getEmotionMode() {
        return emotionMode;
    }
    
    /**
     * Check if manager is initialized
     * @return True if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Check if currently speaking
     * @return True if speaking
     */
    public boolean isSpeaking() {
        return initialized && textToSpeech != null && textToSpeech.isSpeaking();
    }
    
    /**
     * Shutdown the manager
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }
        
        Log.d(TAG, "Shutting down speech synthesis manager");
        
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        
        listeners.clear();
        initialized = false;
    }
    
    /**
     * Notify speech completed
     * @param utteranceId Utterance ID
     */
    private void notifySpeechCompleted(String utteranceId) {
        OnSpeechCompletedListener listener = listeners.remove(utteranceId);
        if (listener != null) {
            listener.onSpeechCompleted(utteranceId);
        }
    }
    
    /**
     * Notify speech error
     * @param utteranceId Utterance ID
     */
    private void notifySpeechError(String utteranceId) {
        OnSpeechCompletedListener listener = listeners.remove(utteranceId);
        if (listener != null) {
            listener.onSpeechError(utteranceId, "Speech synthesis error");
        }
    }
    
    /**
     * Speech completed listener interface
     */
    public interface OnSpeechCompletedListener {
        void onSpeechCompleted(String utteranceId);
        void onSpeechError(String utteranceId, String errorMessage);
    }
}
