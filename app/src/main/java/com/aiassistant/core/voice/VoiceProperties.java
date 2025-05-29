package com.aiassistant.core.voice;

/**
 * Voice properties
 * Used to customize speech output
 */
public class VoiceProperties {
    // Default values
    private static final float DEFAULT_PITCH = 1.0f;
    private static final float DEFAULT_SPEECH_RATE = 1.0f;
    
    // Voice properties
    private float pitch;
    private float speechRate;
    private String emotion;
    private float emotionIntensity;
    
    /**
     * Default constructor
     */
    public VoiceProperties() {
        this.pitch = DEFAULT_PITCH;
        this.speechRate = DEFAULT_SPEECH_RATE;
        this.emotion = null;
        this.emotionIntensity = 0.0f;
    }
    
    /**
     * Constructor with pitch and speech rate
     * @param pitch Voice pitch (0.5 to 2.0, where 1.0 is normal)
     * @param speechRate Speech rate (0.5 to 2.0, where 1.0 is normal)
     */
    public VoiceProperties(float pitch, float speechRate) {
        this.pitch = clamp(pitch, 0.5f, 2.0f);
        this.speechRate = clamp(speechRate, 0.5f, 2.0f);
        this.emotion = null;
        this.emotionIntensity = 0.0f;
    }
    
    /**
     * Constructor with all properties
     * @param pitch Voice pitch (0.5 to 2.0, where 1.0 is normal)
     * @param speechRate Speech rate (0.5 to 2.0, where 1.0 is normal)
     * @param emotion Emotion name
     * @param emotionIntensity Emotion intensity (0.0 to 1.0)
     */
    public VoiceProperties(float pitch, float speechRate, String emotion, float emotionIntensity) {
        this.pitch = clamp(pitch, 0.5f, 2.0f);
        this.speechRate = clamp(speechRate, 0.5f, 2.0f);
        this.emotion = emotion;
        this.emotionIntensity = clamp(emotionIntensity, 0.0f, 1.0f);
    }
    
    /**
     * Get voice pitch
     * @return Voice pitch
     */
    public float getPitch() {
        return pitch;
    }
    
    /**
     * Set voice pitch
     * @param pitch Voice pitch (0.5 to 2.0, where 1.0 is normal)
     * @return This instance for chaining
     */
    public VoiceProperties setPitch(float pitch) {
        this.pitch = clamp(pitch, 0.5f, 2.0f);
        return this;
    }
    
    /**
     * Get speech rate
     * @return Speech rate
     */
    public float getSpeechRate() {
        return speechRate;
    }
    
    /**
     * Set speech rate
     * @param speechRate Speech rate (0.5 to 2.0, where 1.0 is normal)
     * @return This instance for chaining
     */
    public VoiceProperties setSpeechRate(float speechRate) {
        this.speechRate = clamp(speechRate, 0.5f, 2.0f);
        return this;
    }
    
    /**
     * Get emotion
     * @return Emotion name
     */
    public String getEmotion() {
        return emotion;
    }
    
    /**
     * Set emotion
     * @param emotion Emotion name
     * @return This instance for chaining
     */
    public VoiceProperties setEmotion(String emotion) {
        this.emotion = emotion;
        return this;
    }
    
    /**
     * Get emotion intensity
     * @return Emotion intensity
     */
    public float getEmotionIntensity() {
        return emotionIntensity;
    }
    
    /**
     * Set emotion intensity
     * @param emotionIntensity Emotion intensity (0.0 to 1.0)
     * @return This instance for chaining
     */
    public VoiceProperties setEmotionIntensity(float emotionIntensity) {
        this.emotionIntensity = clamp(emotionIntensity, 0.0f, 1.0f);
        return this;
    }
    
    /**
     * Create voice properties for specific emotion
     * @param emotion Emotion name
     * @param intensity Emotion intensity (0.0 to 1.0)
     * @return Voice properties
     */
    public static VoiceProperties forEmotion(String emotion, float intensity) {
        VoiceProperties properties = new VoiceProperties();
        properties.setEmotion(emotion);
        properties.setEmotionIntensity(intensity);
        
        // Adjust speech parameters based on emotion
        switch (emotion.toLowerCase()) {
            case "happy":
                properties.setPitch(1.1f);
                properties.setSpeechRate(1.1f);
                break;
            case "sad":
                properties.setPitch(0.9f);
                properties.setSpeechRate(0.9f);
                break;
            case "angry":
                properties.setPitch(1.2f);
                properties.setSpeechRate(1.2f);
                break;
            case "calm":
                properties.setPitch(0.95f);
                properties.setSpeechRate(0.9f);
                break;
            case "excited":
                properties.setPitch(1.2f);
                properties.setSpeechRate(1.3f);
                break;
            case "fear":
                properties.setPitch(1.1f);
                properties.setSpeechRate(1.3f);
                break;
            case "surprise":
                properties.setPitch(1.3f);
                properties.setSpeechRate(1.1f);
                break;
            case "neutral":
            default:
                properties.setPitch(1.0f);
                properties.setSpeechRate(1.0f);
                break;
        }
        
        // Adjust based on intensity
        float intensityFactor = intensity * 0.5f; // Scale by 0.5 to avoid extreme values
        if (properties.getPitch() > 1.0f) {
            properties.setPitch(1.0f + (properties.getPitch() - 1.0f) * intensityFactor);
        } else if (properties.getPitch() < 1.0f) {
            properties.setPitch(1.0f - (1.0f - properties.getPitch()) * intensityFactor);
        }
        
        if (properties.getSpeechRate() > 1.0f) {
            properties.setSpeechRate(1.0f + (properties.getSpeechRate() - 1.0f) * intensityFactor);
        } else if (properties.getSpeechRate() < 1.0f) {
            properties.setSpeechRate(1.0f - (1.0f - properties.getSpeechRate()) * intensityFactor);
        }
        
        return properties;
    }
    
    /**
     * Clamp value between min and max
     * @param value Value to clamp
     * @param min Minimum value
     * @param max Maximum value
     * @return Clamped value
     */
    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
