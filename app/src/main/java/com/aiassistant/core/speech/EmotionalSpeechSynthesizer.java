package com.aiassistant.core.speech;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Advanced speech synthesizer with emotional and prosodic customization capabilities.
 * Enables dynamic control of speech parameters to convey different emotional states
 * and speaking styles.
 */
public class EmotionalSpeechSynthesizer implements TextToSpeech.OnInitListener {
    private static final String TAG = "EmotionalSpeechSynth";
    
    // TextToSpeech engine
    private TextToSpeech textToSpeech;
    private Context context;
    private boolean isInitialized = false;
    
    // Speech parameters
    private float speechRate = 1.0f;
    private float pitch = 1.0f;
    private float volume = 1.0f;
    private Locale currentLocale = Locale.US;
    private String selectedVoice = null;
    
    // Emotional presets
    private Map<EmotionType, SpeechParams> emotionPresets = new HashMap<>();
    
    // Callback interfaces
    private SpeechSynthesisListener listener;
    
    /**
     * Constructor
     */
    public EmotionalSpeechSynthesizer(Context context) {
        this.context = context;
        initializeTextToSpeech();
        initializeEmotionPresets();
    }
    
    /**
     * Initialize the TTS engine
     */
    private void initializeTextToSpeech() {
        try {
            textToSpeech = new TextToSpeech(context, this);
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    if (listener != null) {
                        listener.onSpeechStarted(utteranceId);
                    }
                }
                
                @Override
                public void onDone(String utteranceId) {
                    if (listener != null) {
                        listener.onSpeechFinished(utteranceId);
                    }
                }
                
                @Override
                public void onError(String utteranceId) {
                    if (listener != null) {
                        listener.onSpeechError(utteranceId, "Speech synthesis error");
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing TextToSpeech: " + e.getMessage());
        }
    }
    
    /**
     * Initialize emotion presets with default values
     */
    private void initializeEmotionPresets() {
        // Neutral speech
        emotionPresets.put(EmotionType.NEUTRAL, 
            new SpeechParams(1.0f, 1.0f, 1.0f, 0));
        
        // Happy speech - faster, higher pitch
        emotionPresets.put(EmotionType.HAPPY, 
            new SpeechParams(1.1f, 1.2f, 1.1f, 5));
        
        // Sad speech - slower, lower pitch
        emotionPresets.put(EmotionType.SAD, 
            new SpeechParams(0.9f, 0.8f, 0.9f, -10));
            
        // Angry speech - faster, higher intensity
        emotionPresets.put(EmotionType.ANGRY, 
            new SpeechParams(1.2f, 1.0f, 1.3f, 15));
            
        // Calm speech - slower, smoother
        emotionPresets.put(EmotionType.CALM, 
            new SpeechParams(0.85f, 0.95f, 0.8f, -5));
            
        // Excited speech - faster, more dynamic
        emotionPresets.put(EmotionType.EXCITED, 
            new SpeechParams(1.25f, 1.3f, 1.2f, 20));
            
        // Serious speech - measured pace, lower pitch
        emotionPresets.put(EmotionType.SERIOUS, 
            new SpeechParams(0.9f, 0.85f, 1.0f, 0));
            
        // Urgent speech - faster, intense
        emotionPresets.put(EmotionType.URGENT, 
            new SpeechParams(1.3f, 1.1f, 1.3f, 10));
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(currentLocale);
            
            if (result == TextToSpeech.LANG_MISSING_DATA || 
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported: " + currentLocale);
            } else {
                isInitialized = true;
                textToSpeech.setPitch(pitch);
                textToSpeech.setSpeechRate(speechRate);
                
                // Apply default voice if available
                selectBestVoice();
                
                if (listener != null) {
                    listener.onEngineReady();
                }
            }
        } else {
            Log.e(TAG, "TextToSpeech initialization failed");
            if (listener != null) {
                listener.onEngineError("TextToSpeech initialization failed with status: " + status);
            }
        }
    }
    
    /**
     * Select the best available voice based on quality and gender
     */
    private void selectBestVoice() {
        if (!isInitialized) return;
        
        try {
            Set<Voice> voices = textToSpeech.getVoices();
            Voice bestVoice = null;
            int bestQuality = -1;
            
            for (Voice voice : voices) {
                if (voice.getLocale().equals(currentLocale) && 
                    !voice.isNetworkConnectionRequired() &&
                    voice.getQuality() > bestQuality) {
                    bestVoice = voice;
                    bestQuality = voice.getQuality();
                }
            }
            
            if (bestVoice != null) {
                textToSpeech.setVoice(bestVoice);
                selectedVoice = bestVoice.getName();
                Log.d(TAG, "Selected voice: " + selectedVoice);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error selecting voice: " + e.getMessage());
        }
    }
    
    /**
     * Speak text with default parameters
     */
    public void speak(String text) {
        speak(text, EmotionType.NEUTRAL, "default");
    }
    
    /**
     * Speak text with a specific emotion
     */
    public void speak(String text, EmotionType emotion, String utteranceId) {
        if (!isInitialized) {
            Log.e(TAG, "TextToSpeech not initialized");
            if (listener != null) {
                listener.onSpeechError(utteranceId, "TextToSpeech not initialized");
            }
            return;
        }
        
        try {
            // Apply emotion preset
            SpeechParams params = emotionPresets.get(emotion);
            if (params != null) {
                textToSpeech.setPitch(params.pitch);
                textToSpeech.setSpeechRate(params.rate);
                
                // Prepare utterance parameters
                HashMap<String, String> ttsParams = new HashMap<>();
                ttsParams.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(params.volume));
                
                // Add prosody effects if supported
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    // Use SSML for advanced prosody control
                    String ssmlText = addEmotionalProsody(text, params);
                    textToSpeech.speak(ssmlText, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                } else {
                    // Fallback for older Android versions
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, ttsParams, utteranceId);
                }
            } else {
                // Use default settings if emotion preset not found
                textToSpeech.setPitch(pitch);
                textToSpeech.setSpeechRate(speechRate);
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during speech synthesis: " + e.getMessage());
            if (listener != null) {
                listener.onSpeechError(utteranceId, "Error during speech synthesis: " + e.getMessage());
            }
        }
    }
    
    /**
     * Enhanced emotion-based speech using advanced SSML formatting
     */
    private String addEmotionalProsody(String text, SpeechParams params) {
        StringBuilder ssml = new StringBuilder();
        ssml.append("<speak>");
        
        // Apply prosody adjustments
        ssml.append("<prosody rate=\"").append(params.rate * 100).append("%\" ");
        ssml.append("pitch=\"").append(params.contour).append("st\" ");
        ssml.append("volume=\"").append(params.volume * 100).append("%\">");
        
        // Add breathing and pauses for more natural speech
        String processed = addEmotionalBreathingPatterns(text, params.emotionIntensity);
        ssml.append(processed);
        
        ssml.append("</prosody>");
        ssml.append("</speak>");
        
        return ssml.toString();
    }
    
    /**
     * Add appropriate breathing patterns and pauses based on emotion intensity
     */
    private String addEmotionalBreathingPatterns(String text, int emotionIntensity) {
        // Absolute value of emotion intensity determines breathing frequency
        int intensity = Math.abs(emotionIntensity);
        
        // Add breathing for high intensity emotions
        if (intensity > 10) {
            // Add breath marks at natural pause points
            text = text.replace(". ", ". <break time=\"300ms\"/> ");
            text = text.replace("! ", "! <break time=\"200ms\"/> ");
            text = text.replace("? ", "? <break time=\"250ms\"/> ");
            
            // Add emphasis to important words based on emotion intensity
            if (intensity > 15) {
                text = addEmphasisToImportantWords(text);
            }
        }
        
        return text;
    }
    
    /**
     * Add emphasis to likely important words in the text
     */
    private String addEmphasisToImportantWords(String text) {
        // Simplified approach - emphasize words that might be important
        String[] emphasizableWords = {"critical", "urgent", "danger", "careful", 
                                    "important", "immediately", "serious", "warning",
                                    "alert", "emergency", "crucial", "essential"};
        
        for (String word : emphasizableWords) {
            // Case insensitive replacement with emphasis
            String pattern = "(?i)\\b" + word + "\\b";
            text = text.replaceAll(pattern, "<emphasis level=\"strong\">" + word + "</emphasis>");
        }
        
        return text;
    }
    
    /**
     * Get available voices for current locale
     */
    public Map<String, Voice> getAvailableVoices() {
        if (!isInitialized) return new HashMap<>();
        
        Map<String, Voice> availableVoices = new HashMap<>();
        Set<Voice> voices = textToSpeech.getVoices();
        
        for (Voice voice : voices) {
            if (voice.getLocale().equals(currentLocale)) {
                availableVoices.put(voice.getName(), voice);
            }
        }
        
        return availableVoices;
    }
    
    /**
     * Set a specific voice by name
     */
    public boolean setVoice(String voiceName) {
        if (!isInitialized) return false;
        
        try {
            Map<String, Voice> availableVoices = getAvailableVoices();
            Voice voice = availableVoices.get(voiceName);
            
            if (voice != null) {
                textToSpeech.setVoice(voice);
                selectedVoice = voiceName;
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting voice: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Set language for speech
     */
    public boolean setLanguage(Locale locale) {
        if (!isInitialized) return false;
        
        try {
            int result = textToSpeech.setLanguage(locale);
            
            if (result == TextToSpeech.LANG_MISSING_DATA || 
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported: " + locale);
                return false;
            }
            
            currentLocale = locale;
            selectBestVoice();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error setting language: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create a custom emotional preset
     */
    public void createCustomEmotionPreset(String presetName, float rate, float pitch, 
                                         float volume, int contour) {
        EmotionType customEmotion = EmotionType.valueOf(presetName.toUpperCase());
        SpeechParams params = new SpeechParams(rate, pitch, volume, contour);
        emotionPresets.put(customEmotion, params);
    }
    
    /**
     * Stop speaking immediately
     */
    public void stop() {
        if (isInitialized && textToSpeech != null) {
            textToSpeech.stop();
        }
    }
    
    /**
     * Release resources
     */
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        isInitialized = false;
    }
    
    /**
     * Set speech listener
     */
    public void setSpeechSynthesisListener(SpeechSynthesisListener listener) {
        this.listener = listener;
    }
    
    /**
     * Speech parameters for emotional presets
     */
    private static class SpeechParams {
        float rate;
        float pitch;
        float volume;
        int emotionIntensity;
        int contour;
        
        SpeechParams(float rate, float pitch, float volume, int contour) {
            this.rate = rate;
            this.pitch = pitch;
            this.volume = volume;
            this.contour = contour;
            this.emotionIntensity = contour;
        }
    }
    
    /**
     * Emotion types for speech synthesis
     */
    public enum EmotionType {
        NEUTRAL,
        HAPPY,
        SAD,
        ANGRY,
        CALM,
        EXCITED,
        SERIOUS,
        URGENT,
        CUSTOM
    }
    
    /**
     * Listener for speech synthesis events
     */
    public interface SpeechSynthesisListener {
        void onEngineReady();
        void onEngineError(String errorMessage);
        void onSpeechStarted(String utteranceId);
        void onSpeechFinished(String utteranceId);
        void onSpeechError(String utteranceId, String errorMessage);
    }
}
