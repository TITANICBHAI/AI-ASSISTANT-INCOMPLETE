package com.aiassistant.core.ai.nlp;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

/**
 * Manager for text-to-speech functionality
 */
public class TextToSpeechManager {
    private static final String TAG = "TextToSpeechManager";
    
    private Context context;
    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;
    private float speechRate = 1.0f;
    private float pitchRate = 1.0f;
    
    /**
     * Constructor
     */
    public TextToSpeechManager(Context context) {
        this.context = context;
        
        // Initialize TTS engine
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.getDefault());
                
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported or missing data");
                } else {
                    isInitialized = true;
                    textToSpeech.setPitch(pitchRate);
                    textToSpeech.setSpeechRate(speechRate);
                    Log.i(TAG, "Text-to-speech initialized successfully");
                }
            } else {
                Log.e(TAG, "Failed to initialize text-to-speech engine");
            }
        });
        
        // Set up utterance progress listener
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.d(TAG, "Started speaking: " + utteranceId);
            }
            
            @Override
            public void onDone(String utteranceId) {
                Log.d(TAG, "Finished speaking: " + utteranceId);
            }
            
            @Override
            public void onError(String utteranceId) {
                Log.e(TAG, "Error speaking: " + utteranceId);
            }
        });
    }
    
    /**
     * Speak the given text
     */
    public void speak(String text) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot speak - text-to-speech not initialized");
            return;
        }
        
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        
        String utteranceId = UUID.randomUUID().toString();
        
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        
        int result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params);
        
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "Error speaking text");
        }
    }
    
    /**
     * Set speech rate
     */
    public void setSpeechRate(float rate) {
        if (rate < 0.1f) rate = 0.1f;
        if (rate > 2.0f) rate = 2.0f;
        
        this.speechRate = rate;
        
        if (isInitialized) {
            textToSpeech.setSpeechRate(rate);
        }
    }
    
    /**
     * Set pitch rate
     */
    public void setPitchRate(float rate) {
        if (rate < 0.1f) rate = 0.1f;
        if (rate > 2.0f) rate = 2.0f;
        
        this.pitchRate = rate;
        
        if (isInitialized) {
            textToSpeech.setPitch(rate);
        }
    }
    
    /**
     * Check if currently speaking
     */
    public boolean isSpeaking() {
        return isInitialized && textToSpeech.isSpeaking();
    }
    
    /**
     * Stop speaking
     */
    public void stop() {
        if (isInitialized) {
            textToSpeech.stop();
        }
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            isInitialized = false;
        }
    }
}
