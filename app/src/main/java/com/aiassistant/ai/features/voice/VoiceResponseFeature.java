package com.aiassistant.ai.features.voice;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import com.aiassistant.ai.features.BaseFeature;
import com.aiassistant.security.SecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Voice Response Feature
 * - Enables AI to respond with voice output
 * - Supports multiple languages and voices
 * - Customizable speech parameters
 * - Queue management for speech outputs
 */
public class VoiceResponseFeature extends BaseFeature implements TextToSpeech.OnInitListener {
    private static final String TAG = "VoiceResponse";
    private static final String FEATURE_NAME = "voice_response";
    
    // Text-to-Speech engine
    private TextToSpeech textToSpeech;
    
    // TTS initialization state
    private boolean ttsInitialized;
    
    // Speech parameters
    private float speechRate;
    private float speechPitch;
    private int speechVolume;
    
    // Current voice
    private Voice currentVoice;
    
    // Current locale
    private Locale currentLocale;
    
    // Available voices map
    private final Map<String, Voice> availableVoices;
    
    // Speech queue
    private final List<SpeechItem> speechQueue;
    
    // Speaking state
    private boolean isSpeaking;
    
    // Listeners for speech events
    private final List<VoiceResponseListener> listeners;
    
    // Speech mode
    private SpeechMode speechMode;
    
    /**
     * Speech mode enumeration
     */
    public enum SpeechMode {
        NORMAL,     // Regular speech
        WHISPER,    // Quiet, whispering voice
        ASSERTIVE,  // Loud, confident voice
        BRIEF,      // Short, concise speech
        DETAILED    // Verbose, detailed speech
    }
    
    /**
     * Constructor
     * @param context Application context
     */
    public VoiceResponseFeature(Context context) {
        super(context, FEATURE_NAME);
        this.ttsInitialized = false;
        this.speechRate = 1.0f;
        this.speechPitch = 1.0f;
        this.speechVolume = 100;
        this.currentVoice = null;
        this.currentLocale = Locale.US;
        this.availableVoices = new HashMap<>();
        this.speechQueue = new ArrayList<>();
        this.isSpeaking = false;
        this.listeners = new CopyOnWriteArrayList<>();
        this.speechMode = SpeechMode.NORMAL;
    }
    
    @Override
    public boolean initialize() {
        boolean success = super.initialize();
        if (success) {
            try {
                // Initialize TTS engine
                initializeTextToSpeech();
                
                Log.d(TAG, "Voice response system initializing...");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize voice response", e);
                return false;
            }
        }
        return false;
    }
    
    @Override
    public void update() {
        if (!isEnabled() || !ttsInitialized) return;
        
        // Apply security context for this operation
        SecurityContext.getInstance().setCurrentFeatureActive(FEATURE_NAME);
        
        try {
            // Process speech queue if not speaking
            if (!isSpeaking && !speechQueue.isEmpty()) {
                processSpeechQueue();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating voice response", e);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    @Override
    public void shutdown() {
        // Stop any ongoing speech
        if (ttsInitialized && textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        
        // Clear queue
        speechQueue.clear();
        
        // Clear state
        ttsInitialized = false;
        isSpeaking = false;
        
        // Clear listeners
        listeners.clear();
        
        super.shutdown();
    }
    
    /**
     * Speak text
     * @param text Text to speak
     * @return true if text was queued for speaking
     */
    public boolean speak(String text) {
        return speak(text, false);
    }
    
    /**
     * Speak text
     * @param text Text to speak
     * @param interruptCurrent true to interrupt current speech
     * @return true if text was queued for speaking
     */
    public boolean speak(String text, boolean interruptCurrent) {
        if (!isEnabled() || !ttsInitialized || text == null || text.isEmpty()) {
            return false;
        }
        
        // Check if we should interrupt current speech
        if (interruptCurrent && isSpeaking) {
            textToSpeech.stop();
            isSpeaking = false;
        }
        
        // Create speech item
        String utteranceId = UUID.randomUUID().toString();
        SpeechItem item = new SpeechItem(text, utteranceId);
        
        // Add to queue
        speechQueue.add(item);
        
        Log.d(TAG, "Text queued for speech: " + text);
        
        // Notify listeners
        for (VoiceResponseListener listener : listeners) {
            listener.onSpeechQueued(text);
        }
        
        return true;
    }
    
    /**
     * Stop speaking
     */
    public void stopSpeaking() {
        if (!isEnabled() || !ttsInitialized) {
            return;
        }
        
        // Stop current speech
        textToSpeech.stop();
        
        // Clear queue
        speechQueue.clear();
        
        // Update state
        isSpeaking = false;
        
        Log.d(TAG, "Speech stopped");
        
        // Notify listeners
        for (VoiceResponseListener listener : listeners) {
            listener.onSpeechStopped();
        }
    }
    
    /**
     * Check if speaking
     * @return true if speaking
     */
    public boolean isSpeaking() {
        return isSpeaking;
    }
    
    /**
     * Set speech rate
     * @param rate Speech rate (0.1 - 2.0)
     */
    public void setSpeechRate(float rate) {
        this.speechRate = Math.max(0.1f, Math.min(2.0f, rate));
        
        if (ttsInitialized) {
            textToSpeech.setSpeechRate(speechRate);
        }
        
        Log.d(TAG, "Speech rate set to " + speechRate);
    }
    
    /**
     * Get speech rate
     * @return Speech rate
     */
    public float getSpeechRate() {
        return speechRate;
    }
    
    /**
     * Set speech pitch
     * @param pitch Speech pitch (0.1 - 2.0)
     */
    public void setSpeechPitch(float pitch) {
        this.speechPitch = Math.max(0.1f, Math.min(2.0f, pitch));
        
        if (ttsInitialized) {
            textToSpeech.setPitch(speechPitch);
        }
        
        Log.d(TAG, "Speech pitch set to " + speechPitch);
    }
    
    /**
     * Get speech pitch
     * @return Speech pitch
     */
    public float getSpeechPitch() {
        return speechPitch;
    }
    
    /**
     * Set speech volume
     * @param volume Speech volume (0 - 100)
     */
    public void setSpeechVolume(int volume) {
        this.speechVolume = Math.max(0, Math.min(100, volume));
        Log.d(TAG, "Speech volume set to " + speechVolume);
    }
    
    /**
     * Get speech volume
     * @return Speech volume
     */
    public int getSpeechVolume() {
        return speechVolume;
    }
    
    /**
     * Set speech language
     * @param localeString Locale string (e.g., "en-US")
     * @return true if language was set
     */
    public boolean setLanguage(String localeString) {
        if (!ttsInitialized) {
            return false;
        }
        
        try {
            Locale locale = parseLocaleString(localeString);
            int result = textToSpeech.setLanguage(locale);
            
            if (result == TextToSpeech.LANG_MISSING_DATA || 
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported: " + localeString);
                return false;
            }
            
            this.currentLocale = locale;
            
            // Update available voices for this locale
            updateAvailableVoices();
            
            Log.d(TAG, "Speech language set to " + localeString);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error setting language: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get current language
     * @return Current language locale
     */
    public Locale getLanguage() {
        return currentLocale;
    }
    
    /**
     * Set voice
     * @param voiceName Voice name
     * @return true if voice was set
     */
    public boolean setVoice(String voiceName) {
        if (!ttsInitialized || !availableVoices.containsKey(voiceName)) {
            return false;
        }
        
        Voice voice = availableVoices.get(voiceName);
        int result = textToSpeech.setVoice(voice);
        
        if (result == TextToSpeech.SUCCESS) {
            this.currentVoice = voice;
            Log.d(TAG, "Speech voice set to " + voiceName);
            return true;
        } else {
            Log.e(TAG, "Failed to set voice: " + voiceName);
            return false;
        }
    }
    
    /**
     * Get current voice
     * @return Current voice
     */
    public Voice getCurrentVoice() {
        return currentVoice;
    }
    
    /**
     * Get available voices
     * @return List of available voice names
     */
    public List<String> getAvailableVoices() {
        return new ArrayList<>(availableVoices.keySet());
    }
    
    /**
     * Set speech mode
     * @param mode Speech mode
     */
    public void setSpeechMode(SpeechMode mode) {
        this.speechMode = mode;
        
        // Apply mode settings
        switch (mode) {
            case WHISPER:
                setSpeechPitch(1.2f);
                setSpeechRate(0.8f);
                setSpeechVolume(50);
                break;
                
            case ASSERTIVE:
                setSpeechPitch(0.9f);
                setSpeechRate(1.1f);
                setSpeechVolume(100);
                break;
                
            case BRIEF:
                setSpeechPitch(1.0f);
                setSpeechRate(1.2f);
                setSpeechVolume(80);
                break;
                
            case DETAILED:
                setSpeechPitch(1.0f);
                setSpeechRate(0.9f);
                setSpeechVolume(80);
                break;
                
            case NORMAL:
            default:
                setSpeechPitch(1.0f);
                setSpeechRate(1.0f);
                setSpeechVolume(80);
                break;
        }
        
        Log.d(TAG, "Speech mode set to " + mode);
    }
    
    /**
     * Get speech mode
     * @return Current speech mode
     */
    public SpeechMode getSpeechMode() {
        return speechMode;
    }
    
    /**
     * Add a voice response listener
     * @param listener Listener to add
     */
    public void addResponseListener(VoiceResponseListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a voice response listener
     * @param listener Listener to remove
     */
    public void removeResponseListener(VoiceResponseListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Format text for the current speech mode
     * @param text Original text
     * @return Formatted text
     */
    public String formatTextForMode(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        switch (speechMode) {
            case WHISPER:
                // Make text sound like a whisper
                return text.toLowerCase();
                
            case ASSERTIVE:
                // Make text more assertive
                return text.trim();
                
            case BRIEF:
                // Abbreviate text
                return abbreviateText(text);
                
            case DETAILED:
                // Leave detailed text as is
                return text;
                
            case NORMAL:
            default:
                // No special formatting
                return text;
        }
    }
    
    /**
     * Initialize Text-to-Speech engine
     */
    private void initializeTextToSpeech() {
        // Create TTS instance
        textToSpeech = new TextToSpeech(getContext(), this);
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language
            int langResult = textToSpeech.setLanguage(currentLocale);
            
            if (langResult == TextToSpeech.LANG_MISSING_DATA || 
                langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported: " + currentLocale);
            } else {
                // Set parameters
                textToSpeech.setSpeechRate(speechRate);
                textToSpeech.setPitch(speechPitch);
                
                // Set audio attributes for Android Lollipop and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build());
                }
                
                // Set utterance progress listener
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        isSpeaking = true;
                        
                        // Notify listeners
                        for (VoiceResponseListener listener : listeners) {
                            listener.onSpeechStarted();
                        }
                    }
                    
                    @Override
                    public void onDone(String utteranceId) {
                        isSpeaking = false;
                        
                        // Notify listeners
                        for (VoiceResponseListener listener : listeners) {
                            listener.onSpeechFinished();
                        }
                        
                        // Process next item in queue
                        if (!speechQueue.isEmpty()) {
                            processSpeechQueue();
                        }
                    }
                    
                    @Override
                    public void onError(String utteranceId) {
                        isSpeaking = false;
                        
                        // Notify listeners
                        for (VoiceResponseListener listener : listeners) {
                            listener.onSpeechError("Error speaking text");
                        }
                        
                        // Process next item in queue
                        if (!speechQueue.isEmpty()) {
                            processSpeechQueue();
                        }
                    }
                });
                
                // Update available voices
                updateAvailableVoices();
                
                ttsInitialized = true;
                Log.d(TAG, "Voice response system initialized successfully");
                
                // Notify listeners
                for (VoiceResponseListener listener : listeners) {
                    listener.onEngineInitialized();
                }
            }
        } else {
            Log.e(TAG, "Failed to initialize TextToSpeech");
            
            // Notify listeners of error
            for (VoiceResponseListener listener : listeners) {
                listener.onEngineError("Failed to initialize text-to-speech engine");
            }
        }
    }
    
    /**
     * Process the speech queue
     */
    private void processSpeechQueue() {
        if (speechQueue.isEmpty() || isSpeaking || !ttsInitialized) {
            return;
        }
        
        // Get next item
        SpeechItem item = speechQueue.remove(0);
        
        // Format text based on speech mode
        String formattedText = formatTextForMode(item.getText());
        
        // Create speech params
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, item.getUtteranceId());
        params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(speechVolume / 100.0f));
        
        // Speak the text
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(formattedText, TextToSpeech.QUEUE_FLUSH, params, item.getUtteranceId());
        } else {
            textToSpeech.speak(formattedText, TextToSpeech.QUEUE_FLUSH, params);
        }
        
        Log.d(TAG, "Speaking: " + formattedText);
    }
    
    /**
     * Update available voices list
     */
    private void updateAvailableVoices() {
        if (!ttsInitialized) {
            return;
        }
        
        availableVoices.clear();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Get all available voices
            Set<Voice> voices = textToSpeech.getVoices();
            
            if (voices != null) {
                for (Voice voice : voices) {
                    // Check if voice matches current locale
                    if (voice.getLocale().getLanguage().equals(currentLocale.getLanguage())) {
                        availableVoices.put(voice.getName(), voice);
                    }
                }
            }
        }
        
        Log.d(TAG, "Found " + availableVoices.size() + " voices for locale " + currentLocale);
    }
    
    /**
     * Parse locale string to Locale object
     * @param localeString Locale string (e.g., "en-US")
     * @return Locale object
     */
    private Locale parseLocaleString(String localeString) {
        String[] parts = localeString.split("-");
        
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        } else if (parts.length == 3) {
            return new Locale(parts[0], parts[1], parts[2]);
        } else {
            return Locale.US; // Default
        }
    }
    
    /**
     * Abbreviate text for brief mode
     * @param text Text to abbreviate
     * @return Abbreviated text
     */
    private String abbreviateText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Split into sentences
        String[] sentences = text.split("[.!?]+");
        
        if (sentences.length <= 1) {
            return text;
        }
        
        // Return first sentence
        return sentences[0].trim() + ".";
    }
    
    /**
     * Speech Item class
     * Represents an item in the speech queue
     */
    private static class SpeechItem {
        private final String text;
        private final String utteranceId;
        
        /**
         * Constructor
         * @param text Text to speak
         * @param utteranceId Utterance ID
         */
        public SpeechItem(String text, String utteranceId) {
            this.text = text;
            this.utteranceId = utteranceId;
        }
        
        /**
         * Get text
         * @return Text to speak
         */
        public String getText() {
            return text;
        }
        
        /**
         * Get utterance ID
         * @return Utterance ID
         */
        public String getUtteranceId() {
            return utteranceId;
        }
    }
    
    /**
     * Voice Response Listener interface
     * For receiving voice response events
     */
    public interface VoiceResponseListener {
        /**
         * Called when the TTS engine is initialized
         */
        void onEngineInitialized();
        
        /**
         * Called when there's an engine error
         * @param errorMessage Error message
         */
        void onEngineError(String errorMessage);
        
        /**
         * Called when text is queued for speech
         * @param text Text queued
         */
        void onSpeechQueued(String text);
        
        /**
         * Called when speech starts
         */
        void onSpeechStarted();
        
        /**
         * Called when speech finishes
         */
        void onSpeechFinished();
        
        /**
         * Called when speech is stopped
         */
        void onSpeechStopped();
        
        /**
         * Called when there's a speech error
         * @param errorMessage Error message
         */
        void onSpeechError(String errorMessage);
    }
}
