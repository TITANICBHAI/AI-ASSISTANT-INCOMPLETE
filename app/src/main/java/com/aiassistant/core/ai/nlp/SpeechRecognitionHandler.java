package com.aiassistant.core.ai.nlp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Handler for speech recognition functionality
 */
public class SpeechRecognitionHandler implements RecognitionListener {
    private static final String TAG = "SpeechRecognition";
    
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private boolean isListening = false;
    private ResultCallback callback;
    
    /**
     * Callback interface for speech recognition results
     */
    public interface ResultCallback {
        void onResult(String result);
    }
    
    /**
     * Constructor
     */
    public SpeechRecognitionHandler(Context context) {
        this.context = context;
        
        // Initialize speech recognizer if available
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(this);
            Log.i(TAG, "Speech recognition initialized");
        } else {
            Log.w(TAG, "Speech recognition not available on this device");
        }
    }
    
    /**
     * Start listening for speech input
     */
    public void startListening(ResultCallback callback) {
        if (speechRecognizer == null) {
            Log.e(TAG, "Cannot start listening - speech recognition not available");
            return;
        }
        
        this.callback = callback;
        
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        
        try {
            speechRecognizer.startListening(recognizerIntent);
            isListening = true;
            Log.d(TAG, "Started listening for speech");
        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition", e);
        }
    }
    
    /**
     * Stop listening for speech input
     */
    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            Log.d(TAG, "Stopped listening for speech");
        }
    }
    
    /**
     * Check if currently listening
     */
    public boolean isListening() {
        return isListening;
    }
    
    /**
     * Clean up resources
     */
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }
    
    // RecognitionListener implementation
    
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
        // Log.v(TAG, "RMS changed: " + rmsdB);
    }
    
    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d(TAG, "Buffer received");
    }
    
    @Override
    public void onEndOfSpeech() {
        Log.d(TAG, "End of speech");
        isListening = false;
    }
    
    @Override
    public void onError(int error) {
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
                errorMessage = "No match found";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                errorMessage = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                errorMessage = "Server error";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                errorMessage = "No speech input";
                break;
            default:
                errorMessage = "Unknown error: " + error;
                break;
        }
        
        Log.e(TAG, "Error during speech recognition: " + errorMessage);
        isListening = false;
    }
    
    @Override
    public void onResults(Bundle results) {
        Log.d(TAG, "Speech recognition results received");
        isListening = false;
        
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty() && callback != null) {
            String result = matches.get(0);
            Log.d(TAG, "Speech recognized: " + result);
            callback.onResult(result);
        }
    }
    
    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String result = matches.get(0);
            Log.d(TAG, "Partial result: " + result);
        }
    }
    
    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.d(TAG, "Speech event: " + eventType);
    }
}
