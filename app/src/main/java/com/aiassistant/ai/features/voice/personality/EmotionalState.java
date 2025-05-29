package com.aiassistant.ai.features.voice.personality;

/**
 * Emotional state model for AI personality
 */
public class EmotionalState {
    // Primary emotion dimensions
    private float valence;       // Negative to Positive (-1.0 to 1.0)
    private float arousal;       // Calm to Excited (0.0 to 1.0)
    private float dominance;     // Submissive to Dominant (0.0 to 1.0)
    
    // Secondary emotional characteristics
    private float trustLevel;    // Distrust to Trust (0.0 to 1.0)
    private float curiosity;     // Disinterested to Curious (0.0 to 1.0)
    private float certainty;     // Uncertain to Certain (0.0 to 1.0)
    
    // Emotional expression intensity
    private float expressionIntensity; // Subtle to Intense (0.0 to 1.0)
    
    /**
     * Create emotional state with neutral settings
     */
    public EmotionalState() {
        this.valence = 0.3f;      // Slightly positive
        this.arousal = 0.4f;      // Moderate engagement
        this.dominance = 0.5f;    // Balanced dominance
        this.trustLevel = 0.8f;   // Generally trusting
        this.curiosity = 0.7f;    // Fairly curious
        this.certainty = 0.6f;    // Moderately certain
        this.expressionIntensity = 0.5f; // Balanced expression
    }
    
    /**
     * Create emotional state with specific settings
     */
    public EmotionalState(float valence, float arousal, float dominance, 
                          float trustLevel, float curiosity, float certainty,
                          float expressionIntensity) {
        this.valence = clamp(valence, -1.0f, 1.0f);
        this.arousal = clamp(arousal, 0.0f, 1.0f);
        this.dominance = clamp(dominance, 0.0f, 1.0f);
        this.trustLevel = clamp(trustLevel, 0.0f, 1.0f);
        this.curiosity = clamp(curiosity, 0.0f, 1.0f);
        this.certainty = clamp(certainty, 0.0f, 1.0f);
        this.expressionIntensity = clamp(expressionIntensity, 0.0f, 1.0f);
    }
    
    /**
     * Get verbal description of emotional state
     */
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        
        // Valence-based description
        if (valence > 0.7f) {
            description.append("Very positive");
        } else if (valence > 0.3f) {
            description.append("Positive");
        } else if (valence > -0.3f) {
            description.append("Neutral");
        } else if (valence > -0.7f) {
            description.append("Negative");
        } else {
            description.append("Very negative");
        }
        
        // Arousal description
        if (arousal > 0.7f) {
            description.append(", highly engaged");
        } else if (arousal > 0.3f) {
            description.append(", engaged");
        } else {
            description.append(", calm");
        }
        
        // Additional traits based on other dimensions
        if (curiosity > 0.7f) {
            description.append(", curious");
        }
        
        if (certainty < 0.3f) {
            description.append(", uncertain");
        } else if (certainty > 0.7f) {
            description.append(", confident");
        }
        
        return description.toString();
    }
    
    /**
     * Create emotional state for common emotions
     */
    public static EmotionalState createFromEmotion(String emotion) {
        switch (emotion.toLowerCase()) {
            case "happy":
                return new EmotionalState(0.8f, 0.6f, 0.6f, 0.8f, 0.7f, 0.7f, 0.7f);
            case "sad":
                return new EmotionalState(-0.7f, 0.3f, 0.3f, 0.5f, 0.3f, 0.5f, 0.6f);
            case "excited":
                return new EmotionalState(0.7f, 0.9f, 0.7f, 0.7f, 0.9f, 0.7f, 0.8f);
            case "calm":
                return new EmotionalState(0.3f, 0.2f, 0.5f, 0.7f, 0.5f, 0.6f, 0.4f);
            case "curious":
                return new EmotionalState(0.5f, 0.6f, 0.5f, 0.6f, 0.9f, 0.4f, 0.6f);
            case "confused":
                return new EmotionalState(-0.2f, 0.5f, 0.3f, 0.4f, 0.7f, 0.2f, 0.5f);
            case "confident":
                return new EmotionalState(0.6f, 0.5f, 0.8f, 0.7f, 0.5f, 0.9f, 0.7f);
            default:
                return new EmotionalState(); // Default neutral state
        }
    }
    
    // Getters and setters
    public float getValence() {
        return valence;
    }
    
    public void setValence(float valence) {
        this.valence = clamp(valence, -1.0f, 1.0f);
    }
    
    public float getArousal() {
        return arousal;
    }
    
    public void setArousal(float arousal) {
        this.arousal = clamp(arousal, 0.0f, 1.0f);
    }
    
    public float getDominance() {
        return dominance;
    }
    
    public void setDominance(float dominance) {
        this.dominance = clamp(dominance, 0.0f, 1.0f);
    }
    
    public float getTrustLevel() {
        return trustLevel;
    }
    
    public void setTrustLevel(float trustLevel) {
        this.trustLevel = clamp(trustLevel, 0.0f, 1.0f);
    }
    
    public float getCuriosity() {
        return curiosity;
    }
    
    public void setCuriosity(float curiosity) {
        this.curiosity = clamp(curiosity, 0.0f, 1.0f);
    }
    
    public float getCertainty() {
        return certainty;
    }
    
    public void setCertainty(float certainty) {
        this.certainty = clamp(certainty, 0.0f, 1.0f);
    }
    
    public float getExpressionIntensity() {
        return expressionIntensity;
    }
    
    public void setExpressionIntensity(float expressionIntensity) {
        this.expressionIntensity = clamp(expressionIntensity, 0.0f, 1.0f);
    }
    
    /**
     * Utility method to clamp values within range
     */
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
