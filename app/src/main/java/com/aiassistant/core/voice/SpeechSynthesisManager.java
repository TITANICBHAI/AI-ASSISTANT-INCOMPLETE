package com.aiassistant.core.voice;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import com.aiassistant.security.SecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manager for advanced speech synthesis capabilities
 * Provides human-like speech with emotional expression and conversational fillers
 */
public class SpeechSynthesisManager implements TextToSpeech.OnInitListener {
    private static final String TAG = "SpeechSynthesisManager";
    
    // Core components
    private final Context context;
    private TextToSpeech textToSpeech;
    private final Handler mainHandler;
    private final ExecutorService executorService;
    private final Random random = new Random();
    
    // Speech settings
    private Locale speechLocale = Locale.US;
    private float speechRate = 1.0f;
    private float speechPitch = 1.0f;
    private Voice selectedVoice = null;
    private EmotionType currentEmotion = EmotionType.NEUTRAL;
    
    // State tracking
    private boolean isInitialized = false;
    private boolean isSpeaking = false;
    private final Map<String, String> pendingUtterances = new ConcurrentHashMap<>();
    private final Map<String, SpeechCompletionListener> completionListeners = new ConcurrentHashMap<>();
    
    // Conversational fillers
    private final List<String> pauseFillers = new ArrayList<>(); // "um", "uh", etc.
    private final List<String> startFillers = new ArrayList<>(); // "well", "so", etc.
    private final List<String> endFillers = new ArrayList<>(); // "you know", "right", etc.
    
    // Probability settings for fillers
    private double pauseFillerProbability = 0.15;
    private double startFillerProbability = 0.2;
    private double endFillerProbability = 0.1;
    private boolean useConversationalFillers = true;
    
    /**
     * Constructor
     */
    public SpeechSynthesisManager(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newCachedThreadPool();
        
        // Initialize Text-to-Speech
        textToSpeech = new TextToSpeech(context, this);
        
        // Initialize conversational fillers
        initializeFillers();
        
        Log.d(TAG, "SpeechSynthesisManager created");
    }
    
    /**
     * Initialize conversational fillers
     */
    private void initializeFillers() {
        // Pause fillers (used mid-sentence)
        pauseFillers.add("um");
        pauseFillers.add("uh");
        pauseFillers.add("hmm");
        pauseFillers.add("er");
        pauseFillers.add("like");
        
        // Start fillers (used at the beginning of sentences)
        startFillers.add("well");
        startFillers.add("so");
        startFillers.add("okay");
        startFillers.add("now");
        startFillers.add("alright");
        startFillers.add("basically");
        
        // End fillers (used at the end of sentences)
        endFillers.add("you know");
        endFillers.add("right");
        endFillers.add("okay");
        endFillers.add("see");
        endFillers.add("got it");
        
        Log.d(TAG, "Conversational fillers initialized");
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language
            int result = textToSpeech.setLanguage(speechLocale);
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported: " + speechLocale);
                textToSpeech.setLanguage(Locale.US); // Fallback to US English
            }
            
            // Set initial speech parameters
            textToSpeech.setSpeechRate(speechRate);
            textToSpeech.setPitch(speechPitch);
            
            // Find and set the best available voice
            selectBestVoice();
            
            // Set up utterance progress listener
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    isSpeaking = true;
                    Log.d(TAG, "Speech started: " + utteranceId);
                }
                
                @Override
                public void onDone(String utteranceId) {
                    mainHandler.post(() -> {
                        isSpeaking = false;
                        
                        // Remove from pending utterances
                        pendingUtterances.remove(utteranceId);
                        
                        // Notify completion listener if any
                        SpeechCompletionListener listener = completionListeners.remove(utteranceId);
                        if (listener != null) {
                            listener.onSpeechCompleted(utteranceId);
                        }
                        
                        Log.d(TAG, "Speech completed: " + utteranceId);
                        
                        // Speak next queued item if any
                        speakNextQueued();
                    });
                }
                
                @Override
                public void onError(String utteranceId) {
                    mainHandler.post(() -> {
                        isSpeaking = false;
                        
                        // Remove from pending utterances
                        pendingUtterances.remove(utteranceId);
                        
                        // Notify completion listener if any
                        SpeechCompletionListener listener = completionListeners.remove(utteranceId);
                        if (listener != null) {
                            listener.onSpeechError(utteranceId, "Speech synthesis error");
                        }
                        
                        Log.e(TAG, "Speech error: " + utteranceId);
                        
                        // Speak next queued item if any
                        speakNextQueued();
                    });
                }
                
                // Required for API level 21+
                @Override
                public void onError(String utteranceId, int errorCode) {
                    onError(utteranceId);
                }
            });
            
            isInitialized = true;
            Log.d(TAG, "Text-to-speech engine initialized successfully");
        } else {
            Log.e(TAG, "Failed to initialize text-to-speech engine");
        }
    }
    
    /**
     * Select the best available voice
     */
    private void selectBestVoice() {
        if (textToSpeech == null) {
            return;
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Set<Voice> voices = textToSpeech.getVoices();
                if (voices != null && !voices.isEmpty()) {
                    // Find a high-quality voice that matches our locale
                    for (Voice voice : voices) {
                        Set<String> features = voice.getFeatures();
                        Locale voiceLocale = voice.getLocale();
                        
                        if (voiceLocale != null && voiceLocale.getLanguage().equals(speechLocale.getLanguage()) &&
                                features != null && !voice.isNetworkConnectionRequired() &&
                                features.contains(TextToSpeech.Engine.KEY_FEATURE_EMBEDDED)) {
                            selectedVoice = voice;
                            textToSpeech.setVoice(voice);
                            Log.d(TAG, "Selected voice: " + voice.getName());
                            break;
                        }
                    }
                    
                    // If no matching voice found, use any available voice
                    if (selectedVoice == null && !voices.isEmpty()) {
                        for (Voice voice : voices) {
                            if (!voice.isNetworkConnectionRequired()) {
                                selectedVoice = voice;
                                textToSpeech.setVoice(voice);
                                Log.d(TAG, "Selected default voice: " + voice.getName());
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error selecting voice: " + e.getMessage());
            }
        }
    }
    
    /**
     * Speak text
     */
    public void speak(String text) {
        speak(text, false);
    }
    
    /**
     * Speak text with option to queue
     */
    public void speak(String text, boolean queue) {
        speak(text, queue, null);
    }
    
    /**
     * Speak text with completion listener
     */
    public void speak(String text, boolean queue, SpeechCompletionListener listener) {
        SecurityContext.getInstance().setCurrentFeatureActive("speech_synthesis");
        
        try {
            if (text == null || text.isEmpty()) {
                return;
            }
            
            if (!isInitialized) {
                Log.e(TAG, "Text-to-speech engine not initialized");
                if (listener != null) {
                    listener.onSpeechError("tts_not_initialized", "Text-to-speech engine not initialized");
                }
                return;
            }
            
            // Generate a unique utterance ID
            final String utteranceId = UUID.randomUUID().toString();
            
            // Store in pending utterances
            pendingUtterances.put(utteranceId, text);
            
            // Store completion listener if provided
            if (listener != null) {
                completionListeners.put(utteranceId, listener);
            }
            
            if (isSpeaking && queue) {
                // Will be spoken when current speech finishes
                Log.d(TAG, "Speech queued: " + utteranceId);
            } else {
                // Stop any current speech if not queueing
                if (!queue) {
                    stopSpeaking();
                }
                
                // Speak the text
                speakUtterance(utteranceId, text);
            }
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Speak with conversational fillers
     */
    public void speakWithFillers(String text, boolean queue) {
        speakWithFillers(text, queue, null);
    }
    
    /**
     * Speak with conversational fillers and completion listener
     */
    public void speakWithFillers(String text, boolean queue, SpeechCompletionListener listener) {
        SecurityContext.getInstance().setCurrentFeatureActive("speech_synthesis");
        
        try {
            if (text == null || text.isEmpty() || !useConversationalFillers) {
                // If fillers are disabled, just use regular speech
                speak(text, queue, listener);
                return;
            }
            
            // Process text to add conversational fillers
            String processedText = addConversationalFillers(text);
            
            // Speak the processed text
            speak(processedText, queue, listener);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Add conversational fillers to text
     */
    private String addConversationalFillers(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        
        // Split text into sentences
        String[] sentences = text.split("(?<=[.!?])\\s+");
        
        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i].trim();
            if (sentence.isEmpty()) {
                continue;
            }
            
            // Add a start filler to some sentences
            if (random.nextDouble() < startFillerProbability) {
                String filler = startFillers.get(random.nextInt(startFillers.size()));
                result.append(filler).append(", ");
            }
            
            // Split sentence into chunks (around commas, etc.)
            String[] chunks = sentence.split("(?<=[,;:])\\s+");
            
            for (int j = 0; j < chunks.length; j++) {
                String chunk = chunks[j].trim();
                if (chunk.isEmpty()) {
                    continue;
                }
                
                // Add the chunk
                result.append(chunk);
                
                // Add a space if not end of chunk
                if (j < chunks.length - 1) {
                    result.append(" ");
                }
                
                // Add a pause filler after some chunks (but not at the end of the sentence)
                if (j < chunks.length - 1 && random.nextDouble() < pauseFillerProbability) {
                    String filler = pauseFillers.get(random.nextInt(pauseFillers.size()));
                    result.append(", ").append(filler).append(", ");
                }
            }
            
            // Add an end filler to some sentences
            if (random.nextDouble() < endFillerProbability) {
                String filler = endFillers.get(random.nextInt(endFillers.size()));
                result.append(", ").append(filler);
            }
            
            // Add a space between sentences
            if (i < sentences.length - 1) {
                result.append(" ");
            }
        }
        
        return result.toString();
    }
    
    /**
     * Speak the next queued utterance
     */
    private void speakNextQueued() {
        if (pendingUtterances.isEmpty() || isSpeaking) {
            return;
        }
        
        // Get the first pending utterance
        String utteranceId = pendingUtterances.keySet().iterator().next();
        String text = pendingUtterances.get(utteranceId);
        
        // Speak it
        speakUtterance(utteranceId, text);
    }
    
    /**
     * Speak a specific utterance
     */
    private void speakUtterance(String utteranceId, String text) {
        if (textToSpeech == null || text == null) {
            return;
        }
        
        // Configure speech parameters based on current emotion
        configureSpeechForEmotion();
        
        // Set up speech params
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        
        // Set audio attributes for calls
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            params.putParcelable(TextToSpeech.Engine.KEY_PARAM_AUDIO_ATTRIBUTES, attributes);
        }
        
        // Speak the text
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
            } else {
                HashMap<String, String> legacyParams = new HashMap<>();
                legacyParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, legacyParams);
            }
            
            Log.d(TAG, "Speaking: " + text + " (ID: " + utteranceId + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error speaking text: " + e.getMessage());
        }
    }
    
    /**
     * Stop speaking
     */
    public void stopSpeaking() {
        SecurityContext.getInstance().setCurrentFeatureActive("speech_synthesis");
        
        try {
            if (textToSpeech != null) {
                textToSpeech.stop();
                
                // Clear any pending utterances
                pendingUtterances.clear();
                completionListeners.clear();
                
                isSpeaking = false;
                
                Log.d(TAG, "Speech stopped");
            }
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Configure speech parameters based on current emotion
     */
    private void configureSpeechForEmotion() {
        if (textToSpeech == null) {
            return;
        }
        
        float pitch = speechPitch;
        float rate = speechRate;
        
        // Adjust pitch and rate based on emotion
        switch (currentEmotion) {
            case HAPPY:
                pitch *= 1.1f;
                rate *= 1.1f;
                break;
                
            case SAD:
                pitch *= 0.9f;
                rate *= 0.9f;
                break;
                
            case ANGRY:
                pitch *= 1.2f;
                rate *= 1.2f;
                break;
                
            case AFRAID:
                pitch *= 1.15f;
                rate *= 1.25f;
                break;
                
            case NEUTRAL:
            default:
                // No adjustment
                break;
        }
        
        // Apply the settings
        textToSpeech.setPitch(pitch);
        textToSpeech.setSpeechRate(rate);
    }
    
    /**
     * Set the speech emotion
     */
    public void setSpeechEmotion(EmotionType emotion) {
        SecurityContext.getInstance().setCurrentFeatureActive("speech_synthesis");
        
        try {
            this.currentEmotion = emotion != null ? emotion : EmotionType.NEUTRAL;
            Log.d(TAG, "Speech emotion set to: " + currentEmotion);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Set the speech locale
     */
    public void setSpeechLocale(Locale locale) {
        SecurityContext.getInstance().setCurrentFeatureActive("speech_synthesis");
        
        try {
            if (locale == null) {
                return;
            }
            
            this.speechLocale = locale;
            
            if (textToSpeech != null) {
                int result = textToSpeech.setLanguage(locale);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported: " + locale);
                    textToSpeech.setLanguage(Locale.US); // Fallback to US English
                } else {
                    // Re-select best voice for this locale
                    selectBestVoice();
                }
            }
            
            Log.d(TAG, "Speech locale set to: " + locale);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Set the speech rate
     */
    public void setSpeechRate(float rate) {
        SecurityContext.getInstance().setCurrentFeatureActive("speech_synthesis");
        
        try {
            this.speechRate = Math.max(0.1f, Math.min(2.0f, rate));
            
            if (textToSpeech != null) {
                textToSpeech.setSpeechRate(speechRate);
            }
            
            Log.d(TAG, "Speech rate set to: " + speechRate);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Set the speech pitch
     */
    public void setSpeechPitch(float pitch) {
        SecurityContext.getInstance().setCurrentFeatureActive("speech_synthesis");
        
        try {
            this.speechPitch = Math.max(0.1f, Math.min(2.0f, pitch));
            
            if (textToSpeech != null) {
                textToSpeech.setPitch(speechPitch);
            }
            
            Log.d(TAG, "Speech pitch set to: " + speechPitch);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Set whether to use conversational fillers
     */
    public void setUseConversationalFillers(boolean useFillers) {
        SecurityContext.getInstance().setCurrentFeatureActive("speech_synthesis");
        
        try {
            this.useConversationalFillers = useFillers;
            Log.d(TAG, "Conversational fillers " + (useFillers ? "enabled" : "disabled"));
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Set probabilities for different types of fillers
     */
    public void setFillerProbabilities(double pauseProb, double startProb, double endProb) {
        SecurityContext.getInstance().setCurrentFeatureActive("speech_synthesis");
        
        try {
            this.pauseFillerProbability = Math.max(0.0, Math.min(1.0, pauseProb));
            this.startFillerProbability = Math.max(0.0, Math.min(1.0, startProb));
            this.endFillerProbability = Math.max(0.0, Math.min(1.0, endProb));
            
            Log.d(TAG, String.format("Filler probabilities set - pause: %.2f, start: %.2f, end: %.2f",
                    pauseFillerProbability, startFillerProbability, endFillerProbability));
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Get the current speech locale
     */
    public Locale getSpeechLocale() {
        return speechLocale;
    }
    
    /**
     * Get the current speech rate
     */
    public float getSpeechRate() {
        return speechRate;
    }
    
    /**
     * Get the current speech pitch
     */
    public float getSpeechPitch() {
        return speechPitch;
    }
    
    /**
     * Get the current emotion
     */
    public EmotionType getCurrentEmotion() {
        return currentEmotion;
    }
    
    /**
     * Check if speech is currently active
     */
    public boolean isSpeaking() {
        return isSpeaking;
    }
    
    /**
     * Check if the manager is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * Shutdown the manager
     */
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        
        executorService.shutdown();
        isInitialized = false;
        
        Log.d(TAG, "SpeechSynthesisManager shutdown");
    }
    
    /**
     * Enum for different emotional speaking styles
     */
    public enum EmotionType {
        NEUTRAL,
        HAPPY,
        SAD,
        ANGRY,
        AFRAID
    }
    
    /**
     * Interface for speech completion events
     */
    public interface SpeechCompletionListener {
        void onSpeechCompleted(String utteranceId);
        void onSpeechError(String utteranceId, String errorMessage);
    }
}
