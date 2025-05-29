package com.aiassistant.core.speech;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.os.Handler;
import android.os.Looper;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import com.aiassistant.core.ai.models.EmotionState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Advanced speech synthesis manager with emotional expression and human-like qualities
 */
public class SpeechSynthesisManager {
    private static final String TAG = "SpeechSynthesis";
    
    private final Context context;
    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;
    private Locale currentLocale = Locale.US;
    private VoiceType currentVoiceType = VoiceType.NEUTRAL;
    private float speechRate = 1.0f;
    private float pitchMultiplier = 1.0f;
    private EmotionState currentEmotionState = EmotionState.NEUTRAL;
    private ExecutorService executor;
    private Random random;
    
    // Speech fillers (like "um", "uh", etc.) for more natural sounding speech
    private String[] speechFillers = {"um", "uh", "hmm", "like", "you know", "well", "so"};
    
    // Pauses in milliseconds
    private static final int SHORT_PAUSE = 300;
    private static final int MEDIUM_PAUSE = 500;
    private static final int LONG_PAUSE = 800;
    
    // Voice types
    public enum VoiceType {
        MALE, FEMALE, NEUTRAL, AUTHORITATIVE, FRIENDLY, PROFESSIONAL
    }
    
    public SpeechSynthesisManager(Context context) {
        this.context = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
        this.random = new Random();
        initializeTTS();
    }
    
    /**
     * Initialize Text-to-Speech engine
     */
    private void initializeTTS() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(currentLocale);
                
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported: " + currentLocale);
                } else {
                    // Set initial parameters
                    textToSpeech.setSpeechRate(speechRate);
                    textToSpeech.setPitch(pitchMultiplier);
                    
                    // Select voice based on current type
                    selectVoiceByType(currentVoiceType);
                    
                    isInitialized = true;
                    Log.d(TAG, "Text-to-Speech engine initialized successfully");
                }
            } else {
                Log.e(TAG, "Failed to initialize Text-to-Speech engine");
            }
        });
        
        // Set progress listener
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.d(TAG, "Speech started: " + utteranceId);
            }
            
            @Override
            public void onDone(String utteranceId) {
                Log.d(TAG, "Speech completed: " + utteranceId);
            }
            
            @Override
            public void onError(String utteranceId) {
                Log.e(TAG, "Speech error: " + utteranceId);
            }
        });
    }
    
    /**
     * Speak the given text
     * 
     * @param text The text to speak
     * @param queueMode Whether to queue the speech or interrupt current speech
     * @param applyEmotions Whether to apply emotional expression
     */
    public void speak(String text, boolean queueMode, boolean applyEmotions) {
        if (!isInitialized) {
            Log.e(TAG, "TTS not initialized, cannot speak");
            return;
        }
        
        // Apply emotional expression if enabled
        if (applyEmotions) {
            text = applyEmotionalExpression(text);
        }
        
        // Create speech parameters
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speech_" + System.currentTimeMillis());
        
        // Set queue mode
        int mode = queueMode ? TextToSpeech.QUEUE_ADD : TextToSpeech.QUEUE_FLUSH;
        
        // Speak
        textToSpeech.speak(text, mode, params);
    }
    
    /**
     * Speak with conversational fillers for more natural sounding speech
     * 
     * @param text The text to speak
     * @param isInitialResponse Whether this is the initial response (more fillers)
     */
    public void speakWithFillers(String text, boolean isInitialResponse) {
        if (!isInitialized) {
            Log.e(TAG, "TTS not initialized, cannot speak with fillers");
            return;
        }
        
        executor.execute(() -> {
            // Add initial filler if this is the beginning of conversation
            if (isInitialResponse) {
                String initialFiller = getRandomFiller();
                speakInternal(initialFiller, TextToSpeech.QUEUE_FLUSH, SHORT_PAUSE);
            }
            
            // Split into sentences
            String[] sentences = text.split("(?<=[.!?])\\s+");
            
            for (int i = 0; i < sentences.length; i++) {
                String sentence = sentences[i].trim();
                if (sentence.isEmpty()) continue;
                
                // Determine if we should add a filler
                boolean addFiller = shouldAddFiller(i, sentences.length, isInitialResponse);
                
                // Add filler before sentence sometimes
                if (addFiller && i > 0) {
                    String filler = getRandomFiller();
                    speakInternal(filler, TextToSpeech.QUEUE_ADD, SHORT_PAUSE);
                }
                
                // Speak the sentence
                speakInternal(sentence, TextToSpeech.QUEUE_ADD, 
                             i < sentences.length - 1 ? MEDIUM_PAUSE : LONG_PAUSE);
            }
        });
    }
    
    /**
     * Internal speak method with pause
     */
    private void speakInternal(String text, int queueMode, int pauseAfter) {
        HashMap<String, String> params = new HashMap<>();
        String utteranceId = "speech_" + System.currentTimeMillis();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        
        textToSpeech.speak(text, queueMode, params);
        
        // Add a pause
        try {
            Thread.sleep(calculateSpeechDuration(text) + pauseAfter);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Apply emotional expression to the text
     */
    private String applyEmotionalExpression(String text) {
        switch (currentEmotionState) {
            case HAPPY:
                // Add enthusiastic markers
                text = text.replace(".", "!");
                text = "😊 " + text;
                break;
                
            case SAD:
                // Add hesitation and softer tone
                text = text.replace("!", ".");
                text = "😔 " + text;
                break;
                
            case ANGRY:
                // Add emphasis
                text = text.toUpperCase();
                text = "😠 " + text;
                break;
                
            case SURPRISED:
                // Add surprise markers
                text = text.replace(".", "!");
                text = "😲 " + text;
                break;
                
            case NEUTRAL:
            default:
                // No changes
                break;
        }
        
        return text;
    }
    
    /**
     * Determine if we should add a filler at this point
     */
    private boolean shouldAddFiller(int sentenceIndex, int totalSentences, boolean isInitialResponse) {
        // More fillers in initial response to sound more human
        float fillerChance = isInitialResponse ? 0.4f : 0.2f;
        
        // Higher chance at beginning and end of response
        if (sentenceIndex == 0 || sentenceIndex == totalSentences - 1) {
            fillerChance += 0.1f;
        }
        
        return random.nextFloat() < fillerChance;
    }
    
    /**
     * Get a random filler word/phrase
     */
    private String getRandomFiller() {
        return speechFillers[random.nextInt(speechFillers.length)];
    }
    
    /**
     * Roughly calculate how long it will take to speak the text
     */
    private long calculateSpeechDuration(String text) {
        // Average speaking rate is about 150 words per minute
        // Adjust for the current speech rate
        int wordsPerMinute = (int)(150 * speechRate);
        
        // Count words
        String[] words = text.split("\\s+");
        int wordCount = words.length;
        
        // Calculate duration in milliseconds
        return (long)((wordCount * 60000.0) / wordsPerMinute);
    }
    
    /**
     * Set the emotion state
     */
    public void setEmotionState(EmotionState emotionState) {
        this.currentEmotionState = emotionState;
        
        // Adjust speech parameters based on emotion
        switch (emotionState) {
            case HAPPY:
                speechRate = 1.1f;
                pitchMultiplier = 1.1f;
                break;
                
            case SAD:
                speechRate = 0.9f;
                pitchMultiplier = 0.9f;
                break;
                
            case ANGRY:
                speechRate = 1.2f;
                pitchMultiplier = 1.2f;
                break;
                
            case SURPRISED:
                speechRate = 1.3f;
                pitchMultiplier = 1.2f;
                break;
                
            case NEUTRAL:
            default:
                speechRate = 1.0f;
                pitchMultiplier = 1.0f;
                break;
        }
        
        if (isInitialized) {
            textToSpeech.setSpeechRate(speechRate);
            textToSpeech.setPitch(pitchMultiplier);
        }
    }
    
    /**
     * Select voice by type
     */
    private void selectVoiceByType(VoiceType voiceType) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return; // Voice selection not supported
        }
        
        Set<Voice> voices = textToSpeech.getVoices();
        if (voices == null || voices.isEmpty()) {
            Log.d(TAG, "No voices available");
            return;
        }
        
        // Create a list of available voices
        List<Voice> availableVoices = new ArrayList<>();
        for (Voice voice : voices) {
            if (voice.getLocale().equals(currentLocale)) {
                availableVoices.add(voice);
            }
        }
        
        if (availableVoices.isEmpty()) {
            Log.d(TAG, "No voices available for locale: " + currentLocale);
            return;
        }
        
        // Select voice based on type
        for (Voice voice : availableVoices) {
            boolean matches = false;
            
            switch (voiceType) {
                case MALE:
                    matches = voice.getName().toLowerCase().contains("male");
                    break;
                    
                case FEMALE:
                    matches = voice.getName().toLowerCase().contains("female");
                    break;
                    
                case NEUTRAL:
                    matches = true; // Any voice can be neutral
                    break;
                    
                case AUTHORITATIVE:
                    matches = voice.getName().toLowerCase().contains("male");
                    break;
                    
                case FRIENDLY:
                    matches = voice.getName().toLowerCase().contains("female");
                    break;
                    
                case PROFESSIONAL:
                    matches = true; // Any voice can be professional
                    break;
            }
            
            if (matches) {
                textToSpeech.setVoice(voice);
                Log.d(TAG, "Selected voice: " + voice.getName());
                return;
            }
        }
        
        // If no match, use the first available voice
        if (!availableVoices.isEmpty()) {
            textToSpeech.setVoice(availableVoices.get(0));
            Log.d(TAG, "Selected default voice: " + availableVoices.get(0).getName());
        }
    }
    
    /**
     * Set the voice type
     */
    public void setVoiceType(VoiceType voiceType) {
        this.currentVoiceType = voiceType;
        selectVoiceByType(voiceType);
    }
    
    /**
     * Set the locale
     */
    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        
        if (isInitialized) {
            int result = textToSpeech.setLanguage(locale);
            
            if (result == TextToSpeech.LANG_MISSING_DATA || 
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported: " + locale);
            } else {
                // Re-select voice for the new locale
                selectVoiceByType(currentVoiceType);
            }
        }
    }
    
    /**
     * Stop speaking immediately
     */
    public void stop() {
        if (isInitialized) {
            textToSpeech.stop();
        }
    }
    
    /**
     * Is the system currently speaking
     */
    public boolean isSpeaking() {
        return isInitialized && textToSpeech.isSpeaking();
    }
    
    /**
     * Get the current locale
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    
    /**
     * Get the current voice type
     */
    public VoiceType getCurrentVoiceType() {
        return currentVoiceType;
    }
    
    /**
     * Shutdown the TTS engine
     */
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            isInitialized = false;
        }
        
        executor.shutdown();
    }
}
    /**
     * Interface for speech completion callbacks
     */
    public interface OnSpeechCompletedListener {
        void onSpeechCompleted();
    }
    
    /**
     * Speak text with optional natural speech features and completion callback
     */
    public void speak(String text, boolean useEmotions, boolean useFillers, 
            OnSpeechCompletedListener listener) {
        // Add natural speech features if requested
        String processedText = text;
        if (useFillers) {
            processedText = addConversationalFillers(processedText);
        }
        if (useEmotions) {
            processedText = addEmotionalMarkers(processedText);
        }
        
        speak(processedText);
        
        // Simulate speech completion after delay
        // In a real app, use TextToSpeech.OnUtteranceCompletedListener
        if (listener != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listener.onSpeechCompleted();
                }
            }, calculateSpeechDuration(processedText));
        }
    }
    
    /**
     * Calculate approximate speech duration in milliseconds
     */
    private long calculateSpeechDuration(String text) {
        // Average speaking rate is about 150 words per minute
        // So each word takes about 400ms on average
        String[] words = text.split("\\s+");
        return words.length * 400L;
    }
