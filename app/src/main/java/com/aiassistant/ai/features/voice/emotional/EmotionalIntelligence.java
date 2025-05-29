package com.aiassistant.ai.features.voice.emotional;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.memory.EmotionalState;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides emotional intelligence capabilities to the voice system
 */
public class EmotionalIntelligence {
    private static final String TAG = "EmotionalIntelligence";
    
    private Context context;
    private AIStateManager stateManager;
    
    // Emotional response templates
    private Map<String, String[]> emotionalResponses = new HashMap<>();
    
    /**
     * Constructor
     */
    public EmotionalIntelligence(Context context) {
        this.context = context;
        this.stateManager = AIStateManager.getInstance(context);
        
        // Initialize emotional response templates
        initializeEmotionalResponses();
    }
    
    /**
     * Initialize emotional response templates
     */
    private void initializeEmotionalResponses() {
        // Joy responses
        emotionalResponses.put("joy", new String[] {
            "I'm so happy to hear that!",
            "That's wonderful news!",
            "I'm delighted about this!",
            "This makes me feel really good.",
            "I'm genuinely happy for you."
        });
        
        // Sadness responses
        emotionalResponses.put("sadness", new String[] {
            "I'm sorry to hear that. Is there anything I can do?",
            "That sounds difficult. I'm here for you.",
            "I understand why you'd feel sad about that.",
            "I wish I could make things better for you.",
            "Take all the time you need, I'm here to listen."
        });
        
        // Anger responses
        emotionalResponses.put("anger", new String[] {
            "I understand why that would upset you.",
            "That does sound frustrating.",
            "I'd be annoyed by that too.",
            "You have every right to feel that way.",
            "Let's take a moment to think about this."
        });
        
        // Fear responses
        emotionalResponses.put("fear", new String[] {
            "I understand your concern.",
            "It's natural to worry about that.",
            "Let's think about how to address this.",
            "I'm here to help you through this.",
            "We can face this together."
        });
        
        // Surprise responses
        emotionalResponses.put("surprise", new String[] {
            "Wow, I didn't expect that!",
            "That's quite surprising!",
            "Oh! That's unexpected.",
            "I'm as surprised as you are!",
            "I never would have guessed that."
        });
    }
    
    /**
     * Analyze text for emotional content and update AI emotional state
     */
    public void processEmotionalContent(String text) {
        // This would use NLP for sentiment analysis
        // For now, using a simple keyword approach
        
        text = text.toLowerCase();
        Map<String, Double> emotionScores = new HashMap<>();
        
        // Check for joy words
        if (text.contains("happy") || text.contains("great") || text.contains("wonderful") || 
            text.contains("love") || text.contains("excited")) {
            emotionScores.put("joy", 0.2);
        }
        
        // Check for sadness words
        if (text.contains("sad") || text.contains("upset") || text.contains("depressed") || 
            text.contains("unhappy") || text.contains("disappointed")) {
            emotionScores.put("sadness", 0.2);
        }
        
        // Check for anger words
        if (text.contains("angry") || text.contains("frustrated") || text.contains("mad") || 
            text.contains("annoying") || text.contains("hate")) {
            emotionScores.put("anger", 0.2);
        }
        
        // Check for fear words
        if (text.contains("scared") || text.contains("afraid") || text.contains("worried") || 
            text.contains("nervous") || text.contains("terrified")) {
            emotionScores.put("fear", 0.2);
        }
        
        // Check for surprise words
        if (text.contains("surprised") || text.contains("shocked") || text.contains("unexpected") || 
            text.contains("wow") || text.contains("unbelievable")) {
            emotionScores.put("surprise", 0.2);
        }
        
        // Update AI emotional state
        for (Map.Entry<String, Double> entry : emotionScores.entrySet()) {
            stateManager.updateEmotionalState(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Get appropriate emotional response based on current state
     */
    public String getEmotionalResponse() {
        EmotionalState state = stateManager.getEmotionalState();
        String dominantEmotion = state.dominantEmotion;
        
        // Get response template for dominant emotion
        String[] responses = emotionalResponses.get(dominantEmotion);
        
        if (responses == null || responses.length == 0) {
            // Default to neutral response
            return "I understand.";
        }
        
        // Select a random response from the available templates
        int index = (int) (Math.random() * responses.length);
        return responses[index];
    }
    
    /**
     * Modulate text based on emotional state
     */
    public String modulate(String text) {
        EmotionalState state = stateManager.getEmotionalState();
        String dominantEmotion = state.dominantEmotion;
        double intensity = state.intensity;
        
        // Only modulate if intensity is significant
        if (intensity < 0.6) {
            return text;
        }
        
        // Modulate based on emotion
        switch (dominantEmotion) {
            case "joy":
                return addJoyModulation(text, intensity);
            case "sadness":
                return addSadnessModulation(text, intensity);
            case "anger":
                return addAngerModulation(text, intensity);
            case "fear":
                return addFearModulation(text, intensity);
            case "surprise":
                return addSurpriseModulation(text, intensity);
            default:
                return text;
        }
    }
    
    /**
     * Add joy modulation to text
     */
    private String addJoyModulation(String text, double intensity) {
        if (intensity > 0.8) {
            // High joy
            text = "I'm really happy! " + text;
            text = text.replace(".", "!");
            return text + " This is exciting!";
        } else {
            // Moderate joy
            return "I'm feeling good. " + text;
        }
    }
    
    /**
     * Add sadness modulation to text
     */
    private String addSadnessModulation(String text, double intensity) {
        if (intensity > 0.8) {
            // High sadness
            return "I feel sad... " + text + " Sorry if I seem down.";
        } else {
            // Moderate sadness
            return "I'm a bit sad. " + text;
        }
    }
    
    /**
     * Add anger modulation to text
     */
    private String addAngerModulation(String text, double intensity) {
        if (intensity > 0.8) {
            // High anger
            return "I'm quite frustrated. " + text + " I apologize if I seem upset.";
        } else {
            // Moderate anger
            return "I'm a bit annoyed. " + text;
        }
    }
    
    /**
     * Add fear modulation to text
     */
    private String addFearModulation(String text, double intensity) {
        if (intensity > 0.8) {
            // High fear
            return "I'm feeling anxious... " + text + " I hope that's okay.";
        } else {
            // Moderate fear
            return "I'm a bit concerned. " + text;
        }
    }
    
    /**
     * Add surprise modulation to text
     */
    private String addSurpriseModulation(String text, double intensity) {
        if (intensity > 0.8) {
            // High surprise
            return "Wow! " + text + " I didn't expect that!";
        } else {
            // Moderate surprise
            return "Oh! " + text;
        }
    }
}
