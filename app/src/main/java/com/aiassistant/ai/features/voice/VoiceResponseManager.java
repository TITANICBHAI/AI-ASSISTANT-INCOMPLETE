package com.aiassistant.ai.features.voice;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Voice Response Manager
 * Simplified interface for using the voice response feature
 */
public class VoiceResponseManager implements VoiceResponseFeature.VoiceResponseListener {
    private static final String TAG = "VoiceResponseManager";
    
    private final Context context;
    private final VoiceResponseFeature voiceResponseFeature;
    private final List<VoiceOutputListener> outputListeners;
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    // Current speech state
    private boolean isSpeaking;
    private String currentText;
    
    // Characteristics
    private final List<String> voiceCharacteristics;
    
    /**
     * Constructor
     * @param context Application context
     * @param voiceResponseFeature Voice response feature
     */
    public VoiceResponseManager(Context context, VoiceResponseFeature voiceResponseFeature) {
        this.context = context;
        this.voiceResponseFeature = voiceResponseFeature;
        this.outputListeners = new ArrayList<>();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.isSpeaking = false;
        this.currentText = "";
        this.voiceCharacteristics = new ArrayList<>();
        
        // Register as listener
        voiceResponseFeature.addResponseListener(this);
        
        // Default characteristics
        voiceCharacteristics.add("clear");
        voiceCharacteristics.add("concise");
    }
    
    /**
     * Speak text
     * @param text Text to speak
     */
    public void speak(String text) {
        speak(text, false);
    }
    
    /**
     * Speak text
     * @param text Text to speak
     * @param interrupt true to interrupt current speech
     */
    public void speak(String text, boolean interrupt) {
        if (voiceResponseFeature.isEnabled()) {
            currentText = text;
            voiceResponseFeature.speak(text, interrupt);
        }
    }
    
    /**
     * Speak text asynchronously
     * @param text Text to speak
     */
    public void speakAsync(String text) {
        executor.execute(() -> speak(text));
    }
    
    /**
     * Stop speaking
     */
    public void stopSpeaking() {
        if (voiceResponseFeature.isEnabled()) {
            voiceResponseFeature.stopSpeaking();
        }
    }
    
    /**
     * Check if currently speaking
     * @return true if speaking
     */
    public boolean isSpeaking() {
        return isSpeaking;
    }
    
    /**
     * Set speech language
     * @param languageCode Language code (e.g., "en-US")
     * @return true if language was set
     */
    public boolean setLanguage(String languageCode) {
        if (voiceResponseFeature.isEnabled()) {
            return voiceResponseFeature.setLanguage(languageCode);
        }
        return false;
    }
    
    /**
     * Get current language code
     * @return Language code
     */
    public String getLanguageCode() {
        Locale locale = voiceResponseFeature.getLanguage();
        if (locale != null) {
            if (locale.getCountry().isEmpty()) {
                return locale.getLanguage();
            } else {
                return locale.getLanguage() + "-" + locale.getCountry();
            }
        }
        return "en-US"; // Default
    }
    
    /**
     * Set speech voice
     * @param voiceName Voice name
     * @return true if voice was set
     */
    public boolean setVoice(String voiceName) {
        if (voiceResponseFeature.isEnabled()) {
            return voiceResponseFeature.setVoice(voiceName);
        }
        return false;
    }
    
    /**
     * Get available voices
     * @return List of available voice names
     */
    public List<String> getAvailableVoices() {
        if (voiceResponseFeature.isEnabled()) {
            return voiceResponseFeature.getAvailableVoices();
        }
        return new ArrayList<>();
    }
    
    /**
     * Get current voice name
     * @return Voice name or null if no voice set
     */
    public String getCurrentVoiceName() {
        Voice voice = voiceResponseFeature.getCurrentVoice();
        return voice != null ? voice.getName() : null;
    }
    
    /**
     * Set speech mode
     * @param mode Speech mode
     */
    public void setSpeechMode(VoiceResponseFeature.SpeechMode mode) {
        if (voiceResponseFeature.isEnabled()) {
            voiceResponseFeature.setSpeechMode(mode);
        }
    }
    
    /**
     * Set speech mode from string
     * @param modeName Mode name (normal, whisper, assertive, brief, detailed)
     * @return true if mode was set
     */
    public boolean setSpeechMode(String modeName) {
        if (!voiceResponseFeature.isEnabled()) {
            return false;
        }
        
        try {
            VoiceResponseFeature.SpeechMode mode = 
                VoiceResponseFeature.SpeechMode.valueOf(modeName.toUpperCase());
            voiceResponseFeature.setSpeechMode(mode);
            return true;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid speech mode: " + modeName);
            return false;
        }
    }
    
    /**
     * Get current speech mode
     * @return Speech mode
     */
    public VoiceResponseFeature.SpeechMode getSpeechMode() {
        return voiceResponseFeature.getSpeechMode();
    }
    
    /**
     * Set speech rate
     * @param rate Speech rate (0.1 - 2.0)
     */
    public void setSpeechRate(float rate) {
        if (voiceResponseFeature.isEnabled()) {
            voiceResponseFeature.setSpeechRate(rate);
        }
    }
    
    /**
     * Set speech pitch
     * @param pitch Speech pitch (0.1 - 2.0)
     */
    public void setSpeechPitch(float pitch) {
        if (voiceResponseFeature.isEnabled()) {
            voiceResponseFeature.setSpeechPitch(pitch);
        }
    }
    
    /**
     * Set speech volume
     * @param volume Speech volume (0 - 100)
     */
    public void setSpeechVolume(int volume) {
        if (voiceResponseFeature.isEnabled()) {
            voiceResponseFeature.setSpeechVolume(volume);
        }
    }
    
    /**
     * Add voice characteristic
     * @param characteristic Characteristic (e.g., "clear", "friendly")
     */
    public void addVoiceCharacteristic(String characteristic) {
        if (characteristic != null && !characteristic.isEmpty() && 
            !voiceCharacteristics.contains(characteristic)) {
            voiceCharacteristics.add(characteristic);
        }
    }
    
    /**
     * Remove voice characteristic
     * @param characteristic Characteristic to remove
     */
    public void removeVoiceCharacteristic(String characteristic) {
        voiceCharacteristics.remove(characteristic);
    }
    
    /**
     * Get voice characteristics
     * @return List of characteristics
     */
    public List<String> getVoiceCharacteristics() {
        return new ArrayList<>(voiceCharacteristics);
    }
    
    /**
     * Format text for speaking
     * @param text Original text
     * @return Formatted text
     */
    public String formatTextForSpeaking(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Remove special characters
        String formatted = text
            .replace("*", "")     // Remove asterisks
            .replace("#", "")     // Remove hash signs
            .replace("|", "")     // Remove vertical bars
            .replace("_", " ")    // Replace underscores with spaces
            .replace("\n", " ")   // Replace newlines with spaces
            .replace("  ", " ");  // Collapse multiple spaces
        
        // Apply speech mode formatting
        return voiceResponseFeature.formatTextForMode(formatted);
    }
    
    /**
     * Add a voice output listener
     * @param listener Listener to add
     */
    public void addOutputListener(VoiceOutputListener listener) {
        if (listener != null && !outputListeners.contains(listener)) {
            outputListeners.add(listener);
        }
    }
    
    /**
     * Remove a voice output listener
     * @param listener Listener to remove
     */
    public void removeOutputListener(VoiceOutputListener listener) {
        outputListeners.remove(listener);
    }
    
    // VoiceResponseListener implementation
    
    @Override
    public void onEngineInitialized() {
        // Notify listeners on main thread
        mainHandler.post(() -> {
            for (VoiceOutputListener listener : outputListeners) {
                listener.onVoiceReady();
            }
        });
    }
    
    @Override
    public void onEngineError(String errorMessage) {
        // Notify listeners on main thread
        mainHandler.post(() -> {
            for (VoiceOutputListener listener : outputListeners) {
                listener.onVoiceError(errorMessage);
            }
        });
    }
    
    @Override
    public void onSpeechQueued(String text) {
        // Update current text
        this.currentText = text;
        
        // Notify listeners on main thread
        mainHandler.post(() -> {
            for (VoiceOutputListener listener : outputListeners) {
                listener.onTextQueued(text);
            }
        });
    }
    
    @Override
    public void onSpeechStarted() {
        // Update state
        this.isSpeaking = true;
        
        // Notify listeners on main thread
        mainHandler.post(() -> {
            for (VoiceOutputListener listener : outputListeners) {
                listener.onSpeakingStarted(currentText);
            }
        });
    }
    
    @Override
    public void onSpeechFinished() {
        // Update state
        this.isSpeaking = false;
        
        // Notify listeners on main thread
        mainHandler.post(() -> {
            for (VoiceOutputListener listener : outputListeners) {
                listener.onSpeakingFinished();
            }
        });
    }
    
    @Override
    public void onSpeechStopped() {
        // Update state
        this.isSpeaking = false;
        
        // Notify listeners on main thread
        mainHandler.post(() -> {
            for (VoiceOutputListener listener : outputListeners) {
                listener.onSpeakingStopped();
            }
        });
    }
    
    @Override
    public void onSpeechError(String errorMessage) {
        // Update state
        this.isSpeaking = false;
        
        // Notify listeners on main thread
        mainHandler.post(() -> {
            for (VoiceOutputListener listener : outputListeners) {
                listener.onVoiceError(errorMessage);
            }
        });
    }
    
    /**
     * Voice Output Listener interface
     * For receiving voice output events
     */
    public interface VoiceOutputListener {
        /**
         * Called when voice system is ready
         */
        void onVoiceReady();
        
        /**
         * Called when text is queued for speaking
         * @param text Queued text
         */
        void onTextQueued(String text);
        
        /**
         * Called when speaking starts
         * @param text Text being spoken
         */
        void onSpeakingStarted(String text);
        
        /**
         * Called when speaking finishes
         */
        void onSpeakingFinished();
        
        /**
         * Called when speaking is stopped
         */
        void onSpeakingStopped();
        
        /**
         * Called when there's a voice error
         * @param errorMessage Error message
         */
        void onVoiceError(String errorMessage);
    }
}
