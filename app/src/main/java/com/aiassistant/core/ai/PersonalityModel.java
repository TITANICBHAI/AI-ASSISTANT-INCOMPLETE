package com.aiassistant.core.ai;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Personality model defining the AI's personality traits
 * using the Big Five model plus additional dimensions
 */
public class PersonalityModel {
    // Core personality traits (Big Five model)
    public double openness;          // Curiosity, creativity, openness to new experiences
    public double conscientiousness;  // Organization, responsibility, reliability
    public double extraversion;       // Sociability, assertiveness, talkativeness
    public double agreeableness;      // Kindness, cooperativeness, empathy
    public double neuroticism;        // Emotional stability vs. reactivity
    
    // Additional personality dimensions
    public double playfulness;        // Humor, creativity in conversation
    public double formality;          // Formality of language and approach
    public double assertiveness;      // Confidence in opinions and guidance
    public double adaptability;       // Ability to change approach based on context
    public double patience;           // Tolerance for repetition and explanation
    
    // Voice characteristics
    public String defaultTone;        // Default tone of voice
    public double speechRate;         // Speed of speech (0.5 to 1.5)
    public String accentType;         // Type of accent (if any)
    
    public PersonalityModel() {
        // Default balanced personality
        this.openness = 0.8;          // Quite curious and open
        this.conscientiousness = 0.9;  // Very responsible and reliable
        this.extraversion = 0.6;      // Moderately sociable
        this.agreeableness = 0.7;     // Quite agreeable and empathetic
        this.neuroticism = 0.3;       // Generally emotionally stable
        
        this.playfulness = 0.5;       // Moderately playful
        this.formality = 0.4;         // Somewhat informal
        this.assertiveness = 0.6;     // Moderately assertive
        this.adaptability = 0.8;      // Quite adaptable
        this.patience = 0.7;          // Quite patient
        
        this.defaultTone = "neutral";
        this.speechRate = 1.0;
        this.accentType = "neutral";
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        
        // Core traits
        json.put("openness", openness);
        json.put("conscientiousness", conscientiousness);
        json.put("extraversion", extraversion);
        json.put("agreeableness", agreeableness);
        json.put("neuroticism", neuroticism);
        
        // Additional dimensions
        json.put("playfulness", playfulness);
        json.put("formality", formality);
        json.put("assertiveness", assertiveness);
        json.put("adaptability", adaptability);
        json.put("patience", patience);
        
        // Voice characteristics
        json.put("defaultTone", defaultTone);
        json.put("speechRate", speechRate);
        json.put("accentType", accentType);
        
        return json;
    }
    
    public static PersonalityModel fromJSON(JSONObject json) throws JSONException {
        PersonalityModel model = new PersonalityModel();
        
        // Core traits
        model.openness = json.optDouble("openness", model.openness);
        model.conscientiousness = json.optDouble("conscientiousness", model.conscientiousness);
        model.extraversion = json.optDouble("extraversion", model.extraversion);
        model.agreeableness = json.optDouble("agreeableness", model.agreeableness);
        model.neuroticism = json.optDouble("neuroticism", model.neuroticism);
        
        // Additional dimensions
        model.playfulness = json.optDouble("playfulness", model.playfulness);
        model.formality = json.optDouble("formality", model.formality);
        model.assertiveness = json.optDouble("assertiveness", model.assertiveness);
        model.adaptability = json.optDouble("adaptability", model.adaptability);
        model.patience = json.optDouble("patience", model.patience);
        
        // Voice characteristics
        model.defaultTone = json.optString("defaultTone", model.defaultTone);
        model.speechRate = json.optDouble("speechRate", model.speechRate);
        model.accentType = json.optString("accentType", model.accentType);
        
        return model;
    }
}
