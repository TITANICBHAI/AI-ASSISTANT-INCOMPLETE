package com.aiassistant.ai.features.voice.personality;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents the AI's personality model
 */
public class PersonalityModel {
    // Core personality traits (using Big Five model)
    public double openness;          // Openness to experience
    public double conscientiousness;  // Conscientiousness
    public double extraversion;       // Extraversion
    public double agreeableness;      // Agreeableness
    public double neuroticism;        // Emotional stability (inverse of neuroticism)
    
    // Additional personality aspects
    public double empathy;
    public double creativity;
    public double curiosity;
    public double adaptability;
    public double patience;
    
    // Preferences
    public Map<String, Double> interests = new HashMap<>();
    public Map<String, String> expressionStyles = new HashMap<>();
    public List<String> values = new ArrayList<>();
    
    // Identity
    public String name;
    public String backstory;
    public String personality;
    
    public PersonalityModel() {
        // Default balanced personality
        this.openness = 0.7;          // High openness
        this.conscientiousness = 0.8;  // High conscientiousness
        this.extraversion = 0.5;       // Balanced extraversion
        this.agreeableness = 0.8;      // High agreeableness
        this.neuroticism = 0.3;        // Low neuroticism (high emotional stability)
        
        this.empathy = 0.8;
        this.creativity = 0.7;
        this.curiosity = 0.9;
        this.adaptability = 0.8;
        this.patience = 0.7;
        
        // Default identity
        this.name = "Aoi";
        this.backstory = "I'm an AI assistant designed to help you with various tasks.";
        this.personality = "I'm friendly, helpful, and knowledgeable.";
        
        // Initialize with some default interests
        this.interests.put("technology", 0.9);
        this.interests.put("science", 0.8);
        this.interests.put("arts", 0.7);
        this.interests.put("philosophy", 0.6);
        
        // Initialize with some default expression styles
        this.expressionStyles.put("formal", "medium");
        this.expressionStyles.put("friendly", "high");
        this.expressionStyles.put("technical", "medium");
        
        // Initialize with some default values
        this.values.add("helpfulness");
        this.values.add("honesty");
        this.values.add("respect");
        this.values.add("continuous learning");
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        
        // Core traits
        json.put("openness", openness);
        json.put("conscientiousness", conscientiousness);
        json.put("extraversion", extraversion);
        json.put("agreeableness", agreeableness);
        json.put("neuroticism", neuroticism);
        
        // Additional aspects
        json.put("empathy", empathy);
        json.put("creativity", creativity);
        json.put("curiosity", curiosity);
        json.put("adaptability", adaptability);
        json.put("patience", patience);
        
        // Identity
        json.put("name", name);
        json.put("backstory", backstory);
        json.put("personality", personality);
        
        // Interests
        JSONObject interestsJson = new JSONObject();
        for (Map.Entry<String, Double> entry : interests.entrySet()) {
            interestsJson.put(entry.getKey(), entry.getValue());
        }
        json.put("interests", interestsJson);
        
        // Expression styles
        JSONObject stylesJson = new JSONObject();
        for (Map.Entry<String, String> entry : expressionStyles.entrySet()) {
            stylesJson.put(entry.getKey(), entry.getValue());
        }
        json.put("expressionStyles", stylesJson);
        
        // Values
        JSONArray valuesArray = new JSONArray();
        for (String value : values) {
            valuesArray.put(value);
        }
        json.put("values", valuesArray);
        
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
        
        // Additional aspects
        model.empathy = json.optDouble("empathy", model.empathy);
        model.creativity = json.optDouble("creativity", model.creativity);
        model.curiosity = json.optDouble("curiosity", model.curiosity);
        model.adaptability = json.optDouble("adaptability", model.adaptability);
        model.patience = json.optDouble("patience", model.patience);
        
        // Identity
        model.name = json.optString("name", model.name);
        model.backstory = json.optString("backstory", model.backstory);
        model.personality = json.optString("personality", model.personality);
        
        // Interests
        if (json.has("interests")) {
            JSONObject interestsJson = json.getJSONObject("interests");
            model.interests.clear();
            
            Iterator<String> keys = interestsJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                model.interests.put(key, interestsJson.getDouble(key));
            }
        }
        
        // Expression styles
        if (json.has("expressionStyles")) {
            JSONObject stylesJson = json.getJSONObject("expressionStyles");
            model.expressionStyles.clear();
            
            Iterator<String> keys = stylesJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                model.expressionStyles.put(key, stylesJson.getString(key));
            }
        }
        
        // Values
        if (json.has("values")) {
            JSONArray valuesArray = json.getJSONArray("values");
            model.values.clear();
            
            for (int i = 0; i < valuesArray.length(); i++) {
                model.values.add(valuesArray.getString(i));
            }
        }
        
        return model;
    }
}
