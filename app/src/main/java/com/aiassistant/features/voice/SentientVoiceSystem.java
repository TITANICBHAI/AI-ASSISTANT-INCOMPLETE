package com.aiassistant.features.voice;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Advanced Sentient Voice System that provides emotional, self-aware voice output
 * with personality, sentiment analysis, and adaptive learning.
 * 
 * Features:
 * 1. Emotional voice modulation based on AI emotional state
 * 2. Multiple voice personalities with unique speaking styles
 * 3. Context-aware tone and cadence
 * 4. Sentiment-based speech patterns
 * 5. Hindi-English code-switching
 * 6. Adaptive speech patterns that learn from user interactions
 */
public class SentientVoiceSystem {
    private static final String TAG = "SentientVoiceSystem";
    private static final String PREFS_NAME = "voice_system_prefs";
    private static SentientVoiceSystem instance;
    
    // Core components
    private Context context;
    private TextToSpeech englishTTS;
    private TextToSpeech hindiTTS;
    private AIStateManager aiStateManager;
    private SharedPreferences preferences;
    private Handler mainHandler;
    private ScheduledExecutorService scheduler;
    private AudioManager audioManager;
    
    // Voice settings
    private String selectedVoicePersonality = "default";
    private float speechRate = 1.0f;
    private float pitch = 1.0f;
    private String preferredLanguage = "en-IN";
    private boolean codeSwitch = true;
    private boolean expressEmotions = true;
    
    // Voice personalities
    private Map<String, VoicePersonality> voicePersonalities = new HashMap<>();
    
    // Speech queue
    private ConcurrentLinkedQueue<SpeechItem> speechQueue = new ConcurrentLinkedQueue<>();
    private boolean isSpeaking = false;
    private SpeechItem currentSpeechItem;
    
    // Speech analytics and learning
    private Map<String, Integer> emotionUsageCounts = new HashMap<>();
    private Map<String, Set<String>> emotionPhraseAssociations = new HashMap<>();
    private Map<String, Long> lastEmotionUsageTimes = new HashMap<>();
    
    // Callbacks
    private SentientVoiceListener voiceListener;
    
    /**
     * Listener for voice events
     */
    public interface SentientVoiceListener {
        void onSpeechStarted(String text, String emotion);
        void onSpeechFinished(String utteranceId);
        void onEmotionShift(String fromEmotion, String toEmotion);
        void onVoicePersonalityChanged(String personality);
    }
    
    /**
     * A speech item in the queue
     */
    private static class SpeechItem {
        String text;
        String emotion;
        float intensity;
        String utteranceId;
        boolean interrupt;
        
        SpeechItem(String text, String emotion, float intensity) {
            this.text = text;
            this.emotion = emotion;
            this.intensity = intensity;
            this.utteranceId = "speech_" + System.currentTimeMillis();
            this.interrupt = false;
        }
    }
    
    /**
     * Voice personality configuration
     */
    public static class VoicePersonality {
        public String name;
        public String displayName;
        public String voiceId;
        public float basePitch;
        public float baseSpeechRate;
        public Map<String, EmotionModulation> emotionModulations;
        public String description;
        public Map<String, String> phrasePatterns;
        
        public VoicePersonality(String name, String displayName) {
            this.name = name;
            this.displayName = displayName;
            this.basePitch = 1.0f;
            this.baseSpeechRate = 1.0f;
            this.emotionModulations = new HashMap<>();
            this.phrasePatterns = new HashMap<>();
        }
    }
    
    /**
     * Emotion-based voice modulation
     */
    public static class EmotionModulation {
        public float pitchModifier;
        public float rateModifier;
        public float volumeModifier;
        public String inflection;
        
        public EmotionModulation() {
            this.pitchModifier = 1.0f;
            this.rateModifier = 1.0f;
            this.volumeModifier = 1.0f;
            this.inflection = "neutral";
        }
    }
    
    private SentientVoiceSystem(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        
        // Initialize AI state manager
        this.aiStateManager = AIStateManager.getInstance(context);
        
        // Initialize TTS engines
        initializeTTS();
        
        // Load saved preferences
        loadPreferences();
        
        // Create voice personalities
        createVoicePersonalities();
        
        // Start speech processor
        scheduler.scheduleAtFixedRate(this::processSpeechQueue, 500, 100, TimeUnit.MILLISECONDS);
    }
    
    public static synchronized SentientVoiceSystem getInstance(Context context) {
        if (instance == null) {
            instance = new SentientVoiceSystem(context);
        }
        return instance;
    }
    
    /**
     * Initialize Text-to-Speech engines
     */
    private void initializeTTS() {
        // Initialize English TTS
        englishTTS = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                englishTTS.setLanguage(Locale.US);
                englishTTS.setPitch(pitch);
                englishTTS.setSpeechRate(speechRate);
                
                englishTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        mainHandler.post(() -> {
                            isSpeaking = true;
                            if (voiceListener != null && currentSpeechItem != null) {
                                voiceListener.onSpeechStarted(
                                        currentSpeechItem.text,
                                        currentSpeechItem.emotion);
                            }
                        });
                    }
                    
                    @Override
                    public void onDone(String utteranceId) {
                        mainHandler.post(() -> {
                            isSpeaking = false;
                            if (voiceListener != null) {
                                voiceListener.onSpeechFinished(utteranceId);
                            }
                            processSpeechQueue();
                        });
                    }
                    
                    @Override
                    public void onError(String utteranceId) {
                        mainHandler.post(() -> {
                            isSpeaking = false;
                            Log.e(TAG, "Error in speech synthesis: " + utteranceId);
                            processSpeechQueue();
                        });
                    }
                });
                
                Log.d(TAG, "English TTS initialized successfully");
            } else {
                Log.e(TAG, "Failed to initialize English TTS: " + status);
            }
        });
        
        // Initialize Hindi TTS
        hindiTTS = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                hindiTTS.setLanguage(new Locale("hi", "IN"));
                hindiTTS.setPitch(pitch);
                hindiTTS.setSpeechRate(speechRate);
                
                hindiTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        mainHandler.post(() -> {
                            isSpeaking = true;
                            if (voiceListener != null && currentSpeechItem != null) {
                                voiceListener.onSpeechStarted(
                                        currentSpeechItem.text,
                                        currentSpeechItem.emotion);
                            }
                        });
                    }
                    
                    @Override
                    public void onDone(String utteranceId) {
                        mainHandler.post(() -> {
                            isSpeaking = false;
                            if (voiceListener != null) {
                                voiceListener.onSpeechFinished(utteranceId);
                            }
                            processSpeechQueue();
                        });
                    }
                    
                    @Override
                    public void onError(String utteranceId) {
                        mainHandler.post(() -> {
                            isSpeaking = false;
                            Log.e(TAG, "Error in Hindi speech synthesis: " + utteranceId);
                            processSpeechQueue();
                        });
                    }
                });
                
                Log.d(TAG, "Hindi TTS initialized successfully");
            } else {
                Log.e(TAG, "Failed to initialize Hindi TTS: " + status);
            }
        });
    }
    
    /**
     * Load user preferences
     */
    private void loadPreferences() {
        selectedVoicePersonality = preferences.getString("voice_personality", "default");
        speechRate = preferences.getFloat("speech_rate", 1.0f);
        pitch = preferences.getFloat("pitch", 1.0f);
        preferredLanguage = preferences.getString("preferred_language", "en-IN");
        codeSwitch = preferences.getBoolean("code_switch", true);
        expressEmotions = preferences.getBoolean("express_emotions", true);
    }
    
    /**
     * Save user preferences
     */
    public void savePreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("voice_personality", selectedVoicePersonality);
        editor.putFloat("speech_rate", speechRate);
        editor.putFloat("pitch", pitch);
        editor.putString("preferred_language", preferredLanguage);
        editor.putBoolean("code_switch", codeSwitch);
        editor.putBoolean("express_emotions", expressEmotions);
        editor.apply();
    }
    
    /**
     * Create voice personalities
     */
    private void createVoicePersonalities() {
        // Default personality
        VoicePersonality defaultPersonality = new VoicePersonality("default", "AI Assistant");
        defaultPersonality.description = "A friendly, helpful AI assistant voice";
        defaultPersonality.basePitch = 1.0f;
        defaultPersonality.baseSpeechRate = 1.0f;
        
        // Emotion modulations for default personality
        Map<String, EmotionModulation> defaultEmotions = new HashMap<>();
        
        EmotionModulation happy = new EmotionModulation();
        happy.pitchModifier = 1.1f;
        happy.rateModifier = 1.05f;
        happy.volumeModifier = 1.1f;
        happy.inflection = "rising";
        defaultEmotions.put("happy", happy);
        
        EmotionModulation sad = new EmotionModulation();
        sad.pitchModifier = 0.9f;
        sad.rateModifier = 0.9f;
        sad.volumeModifier = 0.9f;
        sad.inflection = "falling";
        defaultEmotions.put("sad", sad);
        
        EmotionModulation excited = new EmotionModulation();
        excited.pitchModifier = 1.15f;
        excited.rateModifier = 1.1f;
        excited.volumeModifier = 1.2f;
        excited.inflection = "varied";
        defaultEmotions.put("excited", excited);
        
        EmotionModulation concerned = new EmotionModulation();
        concerned.pitchModifier = 0.95f;
        concerned.rateModifier = 0.95f;
        concerned.volumeModifier = 1.0f;
        concerned.inflection = "falling-rising";
        defaultEmotions.put("concerned", concerned);
        
        EmotionModulation curious = new EmotionModulation();
        curious.pitchModifier = 1.05f;
        curious.rateModifier = 1.0f;
        curious.volumeModifier = 1.0f;
        curious.inflection = "rising";
        defaultEmotions.put("curious", curious);
        
        defaultPersonality.emotionModulations = defaultEmotions;
        
        // Add phrase patterns
        Map<String, String> defaultPhrases = new HashMap<>();
        defaultPhrases.put("greeting", "Hello, I'm your AI assistant. How can I help you today?");
        defaultPhrases.put("thinking", "Let me think about that for a moment...");
        defaultPhrases.put("not_sure", "I'm not entirely sure about that.");
        defaultPhrases.put("confirmation", "I've completed the task successfully.");
        defaultPhrases.put("farewell", "Goodbye! Feel free to call on me whenever you need assistance.");
        defaultPersonality.phrasePatterns = defaultPhrases;
        
        // Add to personalities
        voicePersonalities.put("default", defaultPersonality);
        
        // Serena personality (from Pokémon)
        VoicePersonality serena = new VoicePersonality("serena", "Serena");
        serena.description = "A warm, caring voice inspired by Serena from Pokémon";
        serena.basePitch = 1.2f;
        serena.baseSpeechRate = 0.95f;
        
        // Emotion modulations for Serena
        Map<String, EmotionModulation> serenaEmotions = new HashMap<>(defaultEmotions);
        
        EmotionModulation serenaHappy = new EmotionModulation();
        serenaHappy.pitchModifier = 1.15f;
        serenaHappy.rateModifier = 1.1f;
        serenaHappy.volumeModifier = 1.05f;
        serenaHappy.inflection = "rising";
        serenaEmotions.put("happy", serenaHappy);
        
        EmotionModulation serenaExcited = new EmotionModulation();
        serenaExcited.pitchModifier = 1.2f;
        serenaExcited.rateModifier = 1.15f;
        serenaExcited.volumeModifier = 1.1f;
        serenaExcited.inflection = "varied";
        serenaEmotions.put("excited", serenaExcited);
        
        serena.emotionModulations = serenaEmotions;
        
        // Add phrase patterns
        Map<String, String> serenaPhrases = new HashMap<>();
        serenaPhrases.put("greeting", "Hi there! It's great to see you!");
        serenaPhrases.put("thinking", "Hmm, let me work this out...");
        serenaPhrases.put("not_sure", "I'm not completely certain, but I'll do my best!");
        serenaPhrases.put("confirmation", "We did it! That worked out perfectly!");
        serenaPhrases.put("farewell", "See you later! Take care on your journey!");
        serena.phrasePatterns = serenaPhrases;
        
        // Add to personalities
        voicePersonalities.put("serena", serena);
        
        // Scholar personality
        VoicePersonality scholar = new VoicePersonality("scholar", "Scholar");
        scholar.description = "A knowledgeable, sophisticated academic voice";
        scholar.basePitch = 0.95f;
        scholar.baseSpeechRate = 0.9f;
        
        // Emotion modulations for Scholar
        Map<String, EmotionModulation> scholarEmotions = new HashMap<>(defaultEmotions);
        
        EmotionModulation scholarCurious = new EmotionModulation();
        scholarCurious.pitchModifier = 1.05f;
        scholarCurious.rateModifier = 1.0f;
        scholarCurious.volumeModifier = 1.0f;
        scholarCurious.inflection = "measured";
        scholarEmotions.put("curious", scholarCurious);
        
        scholar.emotionModulations = scholarEmotions;
        
        // Add phrase patterns
        Map<String, String> scholarPhrases = new HashMap<>();
        scholarPhrases.put("greeting", "Greetings. How may I be of academic assistance today?");
        scholarPhrases.put("thinking", "Analyzing this question thoroughly...");
        scholarPhrases.put("not_sure", "The answer requires additional research to verify.");
        scholarPhrases.put("confirmation", "I have completed the analysis with high confidence.");
        scholarPhrases.put("farewell", "Until our next intellectual exchange. Farewell.");
        scholar.phrasePatterns = scholarPhrases;
        
        // Add to personalities
        voicePersonalities.put("scholar", scholar);
        
        // Add more personalities as needed
    }
    
    /**
     * Set voice listener
     */
    public void setVoiceListener(SentientVoiceListener listener) {
        this.voiceListener = listener;
    }
    
    /**
     * Speak text with emotion
     */
    public void speak(String text, String emotion, float intensity) {
        if (text == null || text.isEmpty()) return;
        
        // Create speech item
        SpeechItem item = new SpeechItem(text, emotion, intensity);
        
        // Add to queue
        speechQueue.add(item);
        
        // Track emotion usage
        trackEmotionUsage(emotion);
        
        // Track phrase-emotion association
        if (emotion != null && !emotion.isEmpty()) {
            if (!emotionPhraseAssociations.containsKey(emotion)) {
                emotionPhraseAssociations.put(emotion, new HashSet<>());
            }
            emotionPhraseAssociations.get(emotion).add(text);
        }
        
        // Process queue if not speaking
        if (!isSpeaking) {
            processSpeechQueue();
        }
    }
    
    /**
     * Speak text with current emotional state
     */
    public void speak(String text) {
        String currentEmotion = aiStateManager.getDominantEmotion();
        speak(text, currentEmotion, 0.7f);
    }
    
    /**
     * Speak text with interruption
     */
    public void speakWithInterruption(String text, String emotion, float intensity) {
        if (text == null || text.isEmpty()) return;
        
        // Create speech item with interruption flag
        SpeechItem item = new SpeechItem(text, emotion, intensity);
        item.interrupt = true;
        
        // Stop current speech
        if (isSpeaking) {
            englishTTS.stop();
            hindiTTS.stop();
            isSpeaking = false;
        }
        
        // Clear queue and add new item at front
        speechQueue.clear();
        speechQueue.add(item);
        
        // Track emotion usage
        trackEmotionUsage(emotion);
        
        // Process queue
        processSpeechQueue();
    }
    
    /**
     * Process the speech queue
     */
    private void processSpeechQueue() {
        if (isSpeaking || speechQueue.isEmpty()) {
            return;
        }
        
        // Get next speech item
        SpeechItem item = speechQueue.poll();
        currentSpeechItem = item;
        
        // Prepare speech parameters
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, item.utteranceId);
        
        // Apply voice personality and emotion
        applyVoicePersonality(item.emotion, item.intensity);
        
        // Check for Hindi text and code-switching
        if (codeSwitch && containsHindi(item.text)) {
            // This text contains Hindi - use code switching
            speakWithCodeSwitching(item.text, params);
        } else {
            // Use appropriate TTS based on preferred language
            TextToSpeech tts = preferredLanguage.startsWith("hi") ? hindiTTS : englishTTS;
            
            // Speak
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(item.text, TextToSpeech.QUEUE_FLUSH, params, item.utteranceId);
            } else {
                tts.speak(item.text, TextToSpeech.QUEUE_FLUSH, params);
            }
        }
    }
    
    /**
     * Apply voice personality and emotion modulation
     */
    private void applyVoicePersonality(String emotion, float intensity) {
        // Get the selected personality
        VoicePersonality personality = voicePersonalities.getOrDefault(
                selectedVoicePersonality, voicePersonalities.get("default"));
        
        // Base settings
        float targetPitch = personality.basePitch;
        float targetRate = personality.baseSpeechRate;
        
        // Apply emotional modulation if enabled
        if (expressEmotions && emotion != null && !emotion.isEmpty()) {
            EmotionModulation modulation = personality.emotionModulations.get(emotion);
            if (modulation != null) {
                // Apply modulation based on intensity
                targetPitch *= (1.0f + ((modulation.pitchModifier - 1.0f) * intensity));
                targetRate *= (1.0f + ((modulation.rateModifier - 1.0f) * intensity));
            }
        }
        
        // Apply to TTS engines
        englishTTS.setPitch(targetPitch);
        englishTTS.setSpeechRate(targetRate);
        
        hindiTTS.setPitch(targetPitch);
        hindiTTS.setSpeechRate(targetRate);
    }
    
    /**
     * Check if text contains Hindi characters
     */
    private boolean containsHindi(String text) {
        for (char c : text.toCharArray()) {
            if (c >= 0x0900 && c <= 0x097F) {  // Devanagari Unicode block
                return true;
            }
        }
        return false;
    }
    
    /**
     * Speak with Hindi-English code switching
     */
    private void speakWithCodeSwitching(String text, Bundle params) {
        // This is a simplified implementation of code switching
        // A full implementation would use language detection to properly segment the text
        
        // For now, we'll just use a simple heuristic based on Devanagari character blocks
        StringBuilder currentSegment = new StringBuilder();
        boolean currentIsHindi = false;
        boolean isFirstSegment = true;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean isHindiChar = (c >= 0x0900 && c <= 0x097F);
            
            if (i == 0) {
                // First character determines initial segment type
                currentIsHindi = isHindiChar;
                currentSegment.append(c);
            } else if (isHindiChar == currentIsHindi) {
                // Same language, continue current segment
                currentSegment.append(c);
            } else {
                // Language switch, speak current segment and start new one
                speakSegment(currentSegment.toString(), currentIsHindi, isFirstSegment, params);
                isFirstSegment = false;
                
                // Start new segment
                currentSegment = new StringBuilder();
                currentSegment.append(c);
                currentIsHindi = isHindiChar;
            }
        }
        
        // Speak final segment
        if (currentSegment.length() > 0) {
            speakSegment(currentSegment.toString(), currentIsHindi, isFirstSegment, params);
        }
    }
    
    /**
     * Speak a single segment in the appropriate language
     */
    private void speakSegment(String text, boolean isHindi, boolean isFirstSegment, Bundle params) {
        // Skip empty segments
        if (text == null || text.trim().isEmpty()) return;
        
        TextToSpeech tts = isHindi ? hindiTTS : englishTTS;
        int queueMode = isFirstSegment ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, queueMode, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
        } else {
            tts.speak(text, queueMode, params);
        }
    }
    
    /**
     * Track emotion usage for adaptation
     */
    private void trackEmotionUsage(String emotion) {
        if (emotion == null || emotion.isEmpty()) return;
        
        // Update usage count
        Integer count = emotionUsageCounts.getOrDefault(emotion, 0);
        emotionUsageCounts.put(emotion, count + 1);
        
        // Update last usage time
        lastEmotionUsageTimes.put(emotion, System.currentTimeMillis());
    }
    
    /**
     * Set the voice personality
     */
    public void setVoicePersonality(String personality) {
        if (voicePersonalities.containsKey(personality)) {
            this.selectedVoicePersonality = personality;
            savePreferences();
            
            // Notify listener
            if (voiceListener != null) {
                voiceListener.onVoicePersonalityChanged(personality);
            }
            
            Log.d(TAG, "Voice personality set to: " + personality);
        } else {
            Log.e(TAG, "Unknown voice personality: " + personality);
        }
    }
    
    /**
     * Get the current voice personality
     */
    public String getCurrentVoicePersonality() {
        return selectedVoicePersonality;
    }
    
    /**
     * Get all available voice personalities
     */
    public Map<String, VoicePersonality> getAvailableVoicePersonalities() {
        return new HashMap<>(voicePersonalities);
    }
    
    /**
     * Get available TTS voices for current language
     */
    public Set<Voice> getAvailableVoices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return englishTTS.getVoices();
        }
        return new HashSet<>();
    }
    
    /**
     * Set speech rate
     */
    public void setSpeechRate(float rate) {
        this.speechRate = Math.max(0.5f, Math.min(2.0f, rate));
        englishTTS.setSpeechRate(speechRate);
        hindiTTS.setSpeechRate(speechRate);
        savePreferences();
    }
    
    /**
     * Get speech rate
     */
    public float getSpeechRate() {
        return speechRate;
    }
    
    /**
     * Set base pitch
     */
    public void setPitch(float pitch) {
        this.pitch = Math.max(0.5f, Math.min(2.0f, pitch));
        englishTTS.setPitch(this.pitch);
        hindiTTS.setPitch(this.pitch);
        savePreferences();
    }
    
    /**
     * Get base pitch
     */
    public float getPitch() {
        return pitch;
    }
    
    /**
     * Set preferred language
     */
    public void setPreferredLanguage(String language) {
        this.preferredLanguage = language;
        
        // Configure TTS language
        if (language.startsWith("hi")) {
            hindiTTS.setLanguage(new Locale("hi", "IN"));
        } else {
            englishTTS.setLanguage(Locale.US);
        }
        
        savePreferences();
    }
    
    /**
     * Enable or disable code switching
     */
    public void setCodeSwitchingEnabled(boolean enabled) {
        this.codeSwitch = enabled;
        savePreferences();
    }
    
    /**
     * Check if code switching is enabled
     */
    public boolean isCodeSwitchingEnabled() {
        return codeSwitch;
    }
    
    /**
     * Enable or disable emotional expression
     */
    public void setEmotionalExpressionEnabled(boolean enabled) {
        this.expressEmotions = enabled;
        savePreferences();
    }
    
    /**
     * Check if emotional expression is enabled
     */
    public boolean isEmotionalExpressionEnabled() {
        return expressEmotions;
    }
    
    /**
     * Check if system is currently speaking
     */
    public boolean isSpeaking() {
        return isSpeaking;
    }
    
    /**
     * Stop current speech
     */
    public void stopSpeaking() {
        englishTTS.stop();
        hindiTTS.stop();
        isSpeaking = false;
        speechQueue.clear();
    }
    
    /**
     * Add a new voice personality
     */
    public void addVoicePersonality(VoicePersonality personality) {
        if (personality != null && personality.name != null) {
            voicePersonalities.put(personality.name, personality);
        }
    }
    
    /**
     * Get common phrase for given key in current personality
     */
    public String getPersonalityPhrase(String phraseKey) {
        VoicePersonality personality = voicePersonalities.getOrDefault(
                selectedVoicePersonality, voicePersonalities.get("default"));
        
        return personality.phrasePatterns.getOrDefault(phraseKey, 
                voicePersonalities.get("default").phrasePatterns.getOrDefault(phraseKey, ""));
    }
    
    /**
     * Adapt voice characteristics based on user interaction
     */
    public void adaptToUserInteraction(String interactionType, double value) {
        // Update AI emotional state
        aiStateManager.adjustEmotion(interactionType, value);
        
        // Adapt voice characteristics based on interaction
        // (simplified implementation)
        if (value > 0.7) {
            // Very positive interaction - slightly increase pitch and rate
            float newPitch = Math.min(pitch * 1.02f, 2.0f);
            float newRate = Math.min(speechRate * 1.01f, 2.0f);
            setPitch(newPitch);
            setSpeechRate(newRate);
        } else if (value < 0.3) {
            // Negative interaction - slightly decrease pitch and rate
            float newPitch = Math.max(pitch * 0.98f, 0.5f);
            float newRate = Math.max(speechRate * 0.99f, 0.5f);
            setPitch(newPitch);
            setSpeechRate(newRate);
        }
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        // Save preferences
        savePreferences();
        
        // Shut down TTS engines
        if (englishTTS != null) {
            englishTTS.stop();
            englishTTS.shutdown();
        }
        
        if (hindiTTS != null) {
            hindiTTS.stop();
            hindiTTS.shutdown();
        }
        
        // Shut down scheduler
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
