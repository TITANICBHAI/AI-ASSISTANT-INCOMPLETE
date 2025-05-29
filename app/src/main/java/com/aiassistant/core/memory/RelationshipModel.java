package com.aiassistant.core.memory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Model representing the AI's relationship with a person or entity
 */
public class RelationshipModel {
    // Core attributes
    public String entityId;
    public String entityName;
    public String entityType; // person, organization, etc.
    
    // Relationship attributes
    public double trustLevel;       // 0.0 to 1.0
    public double familiarityLevel; // 0.0 to 1.0
    public Map<String, Double> emotionalAssociations;
    
    // Last interaction
    public long lastInteractionTime;
    
    /**
     * Create a new relationship model
     */
    public RelationshipModel(String entityId, String entityName, String entityType) {
        this.entityId = entityId;
        this.entityName = entityName;
        this.entityType = entityType;
        this.trustLevel = 0.5; // neutral starting point
        this.familiarityLevel = 0.1; // low familiarity initially
        this.emotionalAssociations = new HashMap<>();
        this.lastInteractionTime = System.currentTimeMillis();
    }
    
    /**
     * Update the relationship based on an interaction
     */
    public void updateFromInteraction(double trustImpact, double familiarityImpact, 
                                      String emotionType, double emotionIntensity) {
        // Update trust
        trustLevel = Math.max(0.0, Math.min(1.0, trustLevel + trustImpact));
        
        // Update familiarity
        familiarityLevel = Math.max(0.0, Math.min(1.0, familiarityLevel + familiarityImpact));
        
        // Update emotional association
        if (emotionType != null && !emotionType.isEmpty()) {
            Double currentIntensity = emotionalAssociations.getOrDefault(emotionType, 0.0);
            // Average with new intensity, weighted by recency
            double newIntensity = (currentIntensity * 0.7) + (emotionIntensity * 0.3);
            emotionalAssociations.put(emotionType, newIntensity);
        }
        
        // Update last interaction time
        lastInteractionTime = System.currentTimeMillis();
    }
    
    /**
     * Calculate relationship strength (overall closeness)
     */
    public double calculateRelationshipStrength() {
        return (trustLevel * 0.5) + (familiarityLevel * 0.5);
    }
    
    /**
     * Convert to JSON for storage
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("entityId", entityId);
        json.put("entityName", entityName);
        json.put("entityType", entityType);
        json.put("trustLevel", trustLevel);
        json.put("familiarityLevel", familiarityLevel);
        json.put("lastInteractionTime", lastInteractionTime);
        
        // Convert emotional associations
        JSONObject emotionsJson = new JSONObject();
        for (Map.Entry<String, Double> entry : emotionalAssociations.entrySet()) {
            emotionsJson.put(entry.getKey(), entry.getValue());
        }
        json.put("emotionalAssociations", emotionsJson);
        
        return json;
    }
    
    /**
     * Create from JSON
     */
    public static RelationshipModel fromJSON(JSONObject json) throws JSONException {
        String entityId = json.getString("entityId");
        String entityName = json.getString("entityName");
        String entityType = json.getString("entityType");
        
        RelationshipModel model = new RelationshipModel(entityId, entityName, entityType);
        model.trustLevel = json.getDouble("trustLevel");
        model.familiarityLevel = json.getDouble("familiarityLevel");
        model.lastInteractionTime = json.getLong("lastInteractionTime");
        
        // Load emotional associations
        JSONObject emotionsJson = json.getJSONObject("emotionalAssociations");
        for (String key : JSONObject.getNames(emotionsJson)) {
            model.emotionalAssociations.put(key, emotionsJson.getDouble(key));
        }
        
        return model;
    }
}
