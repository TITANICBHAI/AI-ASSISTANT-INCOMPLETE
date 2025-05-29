package com.aiassistant.ai.features.voice.synthesis;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import com.aiassistant.ai.features.voice.personality.PersonalityModel;
import com.aiassistant.debug.DebugLogger;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Advanced voice synthesizer that adapts voice characteristics
 * based on personality and emotional context.
 */
public class AdvancedVoiceSynthesizer {
    private static final String TAG = "AdvancedVoiceSynth";
    
    // TTS engine
    private TextToSpeech tts;
    private boolean ttsReady;
    
    // Voice parameters
    private float speechRate;
    private float pitch;
    private Locale currentLocale;
    private Voice selectedVoice;
    
    // Personality model
    private PersonalityModel personalityModel;
    
    // Context
    private Context context;
    
    // Voice adaptation level (0.0-1.0)
    private float voiceAdaptationLevel;
    
    // Current emotional context for voice modulation
    private Map<String, Float> emotionalContext;
    
    // Voice completion listener
    private OnVoiceCompletionListener completionListener;
    
    // Interface for voice completion events
    public interface OnVoiceCompletionListener {
        void onVoiceCompleted(String utteranceId);
        void onVoiceError(String utteranceId, int errorCode);
    }
    
    /**
     * Constructor
     * @param context Android context
     * @param personalityModel Personality model for voice characteristics
     */
    public AdvancedVoiceSynthesizer(Context context, PersonalityModel personalityModel) {
        this.context = context;
        this.personalityModel = personalityModel;
        this.voiceAdaptationLevel = 0.5f; // Start with moderate adaptation
        this.emotionalContext = new HashMap<>();
        
        // Initialize TTS engine
        initializeTTS();
        
        DebugLogger.i(TAG, "AdvancedVoiceSynthesizer initialized with personality type: " + 
                    personalityModel.getPersonaType());
    }
    
    /**
     * Initialize the TTS engine
     */
    private void initializeTTS() {
        ttsReady = false;
        
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // TTS engine initialized successfully
                ttsReady = true;
                
                // Set default language
                currentLocale = Locale.US;
                int result = tts.setLanguage(currentLocale);
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    DebugLogger.e(TAG, "Language not supported: " + currentLocale);
                }
                
                // Initialize voice parameters based on personality
                updateVoiceParameters();
                
                // Set utterance progress listener
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        DebugLogger.d(TAG, "TTS started: " + utteranceId);
                    }
                    
                    @Override
                    public void onDone(String utteranceId) {
                        DebugLogger.d(TAG, "TTS completed: " + utteranceId);
                        if (completionListener != null) {
                            completionListener.onVoiceCompleted(utteranceId);
                        }
                    }
                    
                    @Override
                    public void onError(String utteranceId) {
                        DebugLogger.e(TAG, "TTS error: " + utteranceId);
                        if (completionListener != null) {
                            completionListener.onVoiceError(utteranceId, -1);
                        }
                    }
                });
                
                DebugLogger.i(TAG, "TTS engine initialized successfully");
            } else {
                DebugLogger.e(TAG, "TTS initialization failed with status: " + status);
            }
        });
    }
    
    /**
     * Update voice parameters based on personality model
     */
    private void updateVoiceParameters() {
        if (tts == null || !ttsReady) return;
        
        // Get personality-based voice characteristics
        float personalitySpeechRate = personalityModel.getSpeechRate();
        float personalityPitchVariation = personalityModel.getPitchVariation();
        
        // Apply voice adaptation level to determine how much personality affects voice
        speechRate = 0.8f + (personalitySpeechRate * 0.4f); // Range: 0.8-1.2
        pitch = 1.0f + ((personalityPitchVariation - 0.5f) * 0.4f); // Range: 0.8-1.2
        
        // Apply to TTS engine
        tts.setSpeechRate(speechRate);
        tts.setPitch(pitch);
        
        // Try to find a suitable voice based on personality
        selectVoiceByPersonality();
        
        DebugLogger.d(TAG, "Voice parameters updated: rate=" + speechRate + ", pitch=" + pitch);
    }
    
    /**
     * Try to select a voice that matches personality characteristics
     */
    private void selectVoiceByPersonality() {
        if (tts == null || !ttsReady) return;
        
        // Get available voices
        Set<Voice> voices = tts.getVoices();
        if (voices == null || voices.isEmpty()) {
            DebugLogger.w(TAG, "No voices available for selection");
            return;
        }
        
        // Get personality traits
        Map<String, Float> traits = personalityModel.getPersonalityParameters();
        float extraversion = traits.getOrDefault("extraversion", 0.5f);
        float formality = traits.getOrDefault("formality", 0.5f);
        
        // Simple voice selection heuristic based on personality
        Voice bestMatch = null;
        float bestMatchScore = -1f;
        
        for (Voice voice : voices) {
            // Skip voices that don't match our locale
            if (!voice.getLocale().equals(currentLocale)) continue;
            
            // Calculate a match score based on voice and personality
            float matchScore = calculateVoiceMatchScore(voice, extraversion, formality);
            
            if (matchScore > bestMatchScore) {
                bestMatchScore = matchScore;
                bestMatch = voice;
            }
        }
        
        // Apply the best matching voice if found
        if (bestMatch != null) {
            tts.setVoice(bestMatch);
            selectedVoice = bestMatch;
            DebugLogger.d(TAG, "Selected voice: " + bestMatch.getName());
        }
    }
    
    /**
     * Calculate how well a voice matches the personality
     * @param voice Voice to evaluate
     * @param extraversion Extraversion trait
     * @param formality Formality trait
     * @return Match score (higher is better)
     */
    private float calculateVoiceMatchScore(Voice voice, float extraversion, float formality) {
        // This is a simplified heuristic
        // In a real implementation, this would analyze voice characteristics
        
        float score = 0.5f; // Base score
        
        // Voice quality preferences based on personality
        Set<String> qualities = voice.getQuality();
        
        // Prefer high quality for formal personalities
        if (formality > 0.7f && qualities.contains(Voice.QUALITY_VERY_HIGH)) {
            score += 0.2f;
        }
        
        // Voice gender preferences based on personality (simplified example)
        // In a real app, voice selection would be based on user preference, not personality
        if (extraversion > 0.7f && voice.getName().toLowerCase().contains("female")) {
            score += 0.1f;
        } else if (extraversion < 0.3f && voice.getName().toLowerCase().contains("male")) {
            score += 0.1f;
        }
        
        return score;
    }
    
    /**
     * Speak text with emotional context
     * @param text Text to speak
     * @param emotionalContext Emotional context for voice modulation
     * @param utteranceId ID for tracking this utterance
     * @return True if speech successfully started
     */
    public boolean speak(String text, Map<String, Float> emotionalContext, String utteranceId) {
        if (tts == null || !ttsReady) {
            DebugLogger.e(TAG, "TTS not ready for speaking");
            return false;
        }
        
        // Update emotional context
        this.emotionalContext = emotionalContext;
        
        // Apply emotional modulation
        applyEmotionalModulation();
        
        // Prepare speech parameters
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        
        // Apply prosody markup for more expressive speech if supported
        String processedText = applyProsodyMarkup(text);
        
        // Speak the text
        int result = tts.speak(processedText, TextToSpeech.QUEUE_FLUSH, params);
        
        return (result == TextToSpeech.SUCCESS);
    }
    
    /**
     * Apply emotional modulation to voice parameters
     */
    private void applyEmotionalModulation() {
        if (tts == null || !ttsReady || emotionalContext == null) return;
        
        // Get base parameters from personality
        float baseSpeechRate = 0.8f + (personalityModel.getSpeechRate() * 0.4f);
        float basePitch = 1.0f + ((personalityModel.getPitchVariation() - 0.5f) * 0.4f);
        
        // Extract emotional intensities
        float joy = emotionalContext.getOrDefault("joy", 0.0f);
        float sadness = emotionalContext.getOrDefault("sadness", 0.0f);
        float anger = emotionalContext.getOrDefault("anger", 0.0f);
        float fear = emotionalContext.getOrDefault("fear", 0.0f);
        float surprise = emotionalContext.getOrDefault("surprise", 0.0f);
        
        // Apply emotional effects to speech rate
        float rateModifier = 1.0f;
        rateModifier += joy * 0.2f;       // Joy increases rate slightly
        rateModifier -= sadness * 0.3f;   // Sadness decreases rate
        rateModifier += anger * 0.3f;     // Anger increases rate
        rateModifier -= fear * 0.1f;      // Fear slightly decreases rate
        rateModifier += surprise * 0.2f;  // Surprise increases rate
        
        // Apply emotional effects to pitch
        float pitchModifier = 1.0f;
        pitchModifier += joy * 0.2f;      // Joy raises pitch
        pitchModifier -= sadness * 0.2f;  // Sadness lowers pitch
        pitchModifier += anger * 0.1f;    // Anger raises pitch slightly
        pitchModifier += fear * 0.3f;     // Fear raises pitch more
        pitchModifier += surprise * 0.3f; // Surprise raises pitch
        
        // Calculate final parameters with emotion and adaptation level
        speechRate = baseSpeechRate * (1.0f + ((rateModifier - 1.0f) * voiceAdaptationLevel));
        pitch = basePitch * (1.0f + ((pitchModifier - 1.0f) * voiceAdaptationLevel));
        
        // Ensure values are within reasonable ranges
        speechRate = Math.max(0.5f, Math.min(2.0f, speechRate));
        pitch = Math.max(0.5f, Math.min(2.0f, pitch));
        
        // Apply to TTS engine
        tts.setSpeechRate(speechRate);
        tts.setPitch(pitch);
        
        DebugLogger.d(TAG, "Applied emotional modulation: rate=" + speechRate + ", pitch=" + pitch);
    }
    
    /**
     * Apply prosody markup for more expressive speech
     * @param text Text to enhance
     * @return Enhanced text with prosody markup if supported
     */
    private String applyProsodyMarkup(String text) {
        // Check if SSML is supported by the TTS engine
        boolean supportsSSML = tts.getFeatures(currentLocale).containsKey(TextToSpeech.Engine.KEY_FEATURE_EMBEDDED_SYNTHESIS);
        
        if (!supportsSSML) {
            return text; // Return original text if SSML not supported
        }
        
        // Extract personality and emotional factors
        float expressiveness = personalityModel.getExpressiveness();
        
        // Only apply markup if expressiveness is high enough
        if (expressiveness < 0.4f) {
            return text;
        }
        
        // Simple SSML prosody markup for emphasis and pauses
        StringBuilder ssmlText = new StringBuilder();
        ssmlText.append("<speak>");
        
        // Process text to add emphasis and pauses
        String[] sentences = text.split("(?<=[.!?])\\s+");
        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i].trim();
            
            // Skip empty sentences
            if (sentence.isEmpty()) continue;
            
            // Add emphasis to important words (simplified approach)
            if (sentence.contains("important") || sentence.contains("critical") || 
                sentence.contains("essential") || sentence.contains("significant")) {
                
                ssmlText.append("<emphasis level=\"strong\">")
                       .append(sentence)
                       .append("</emphasis>");
            } else {
                ssmlText.append(sentence);
            }
            
            // Add appropriate pauses between sentences
            if (i < sentences.length - 1) {
                ssmlText.append(" <break time=\"500ms\"/> ");
            }
        }
        
        ssmlText.append("</speak>");
        return ssmlText.toString();
    }
    
    /**
     * Set language for speech
     * @param locale Language locale
     * @return True if language was set successfully
     */
    public boolean setLanguage(Locale locale) {
        if (tts == null || !ttsReady) return false;
        
        int result = tts.setLanguage(locale);
        if (result == TextToSpeech.LANG_MISSING_DATA || 
            result == TextToSpeech.LANG_NOT_SUPPORTED) {
            DebugLogger.e(TAG, "Language not supported: " + locale);
            return false;
        }
        
        currentLocale = locale;
        
        // Update voice selection for new locale
        selectVoiceByPersonality();
        
        DebugLogger.i(TAG, "Language set to: " + locale);
        return true;
    }
    
    /**
     * Set voice adaptation level
     * @param level Adaptation level (0.0-1.0)
     */
    public void setVoiceAdaptationLevel(float level) {
        this.voiceAdaptationLevel = Math.max(0.0f, Math.min(1.0f, level));
        DebugLogger.d(TAG, "Voice adaptation level set to: " + voiceAdaptationLevel);
        
        // Update voice parameters with new adaptation level
        updateVoiceParameters();
    }
    
    /**
     * Set completion listener
     * @param listener Completion listener
     */
    public void setOnVoiceCompletionListener(OnVoiceCompletionListener listener) {
        this.completionListener = listener;
    }
    
    /**
     * Shutdown the TTS engine
     */
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            ttsReady = false;
        }
    }
    
    /**
     * Check if TTS is ready
     * @return True if TTS is ready
     */
    public boolean isTTSReady() {
        return ttsReady;
    }
    
    /**
     * Get current voice parameters
     * @return Map of voice parameters
     */
    public Map<String, Object> getVoiceParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("speechRate", speechRate);
        params.put("pitch", pitch);
        params.put("locale", currentLocale.toString());
        params.put("voice", selectedVoice != null ? selectedVoice.getName() : "default");
        params.put("adaptationLevel", voiceAdaptationLevel);
        
        return params;
    }
}
