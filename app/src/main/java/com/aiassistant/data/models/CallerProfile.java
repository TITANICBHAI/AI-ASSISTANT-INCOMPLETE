package com.aiassistant.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity class representing a caller profile
 */
@Entity(tableName = "caller_profiles")
public class CallerProfile {
    @PrimaryKey
    @NonNull
    private String phoneNumber;
    
    private String displayName;
    
    private String relationshipType; // family, friend, colleague, etc.
    
    private int callCount;
    
    private long lastCallTimestamp;
    
    private float relationshipFamiliarity; // 0.0 to 1.0
    
    private String emotionalHistory; // JSON string of emotion map
    
    private String contextualHistory; // JSON string of relevant contexts
    
    /**
     * Default constructor for Room
     */
    public CallerProfile() {
        this.phoneNumber = "";
        this.displayName = "";
        this.callCount = 0;
        this.lastCallTimestamp = System.currentTimeMillis();
        this.relationshipFamiliarity = 0.0f;
        this.emotionalHistory = "{}";
        this.contextualHistory = "{}";
    }
    
    /**
     * Constructor
     * @param phoneNumber Phone number
     * @param displayName Display name
     */
    public CallerProfile(@NonNull String phoneNumber, String displayName) {
        this.phoneNumber = phoneNumber;
        this.displayName = displayName;
        this.callCount = 0;
        this.lastCallTimestamp = System.currentTimeMillis();
        this.relationshipFamiliarity = 0.0f;
        this.emotionalHistory = "{}";
        this.contextualHistory = "{}";
    }
    
    /**
     * Get phone number
     * @return Phone number
     */
    @NonNull
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    /**
     * Set phone number
     * @param phoneNumber Phone number
     */
    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    /**
     * Get display name
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Set display name
     * @param displayName Display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Get relationship type
     * @return Relationship type
     */
    public String getRelationshipType() {
        return relationshipType;
    }
    
    /**
     * Set relationship type
     * @param relationshipType Relationship type
     */
    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
        
        // Update familiarity based on relationship type
        if (relationshipType != null) {
            switch (relationshipType) {
                case "family":
                    setRelationshipFamiliarity(Math.max(0.7f, getRelationshipFamiliarity()));
                    break;
                case "friend":
                    setRelationshipFamiliarity(Math.max(0.5f, getRelationshipFamiliarity()));
                    break;
                case "colleague":
                    setRelationshipFamiliarity(Math.max(0.3f, getRelationshipFamiliarity()));
                    break;
            }
        }
    }
    
    /**
     * Get call count
     * @return Call count
     */
    public int getCallCount() {
        return callCount;
    }
    
    /**
     * Set call count
     * @param callCount Call count
     */
    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }
    
    /**
     * Get last call timestamp
     * @return Last call timestamp
     */
    public long getLastCallTimestamp() {
        return lastCallTimestamp;
    }
    
    /**
     * Set last call timestamp
     * @param lastCallTimestamp Last call timestamp
     */
    public void setLastCallTimestamp(long lastCallTimestamp) {
        this.lastCallTimestamp = lastCallTimestamp;
    }
    
    /**
     * Get relationship familiarity
     * @return Relationship familiarity
     */
    public float getRelationshipFamiliarity() {
        return relationshipFamiliarity;
    }
    
    /**
     * Set relationship familiarity
     * @param relationshipFamiliarity Relationship familiarity
     */
    public void setRelationshipFamiliarity(float relationshipFamiliarity) {
        this.relationshipFamiliarity = relationshipFamiliarity;
    }
    
    /**
     * Get emotional history as string
     * @return Emotional history JSON string
     */
    public String getEmotionalHistory() {
        return emotionalHistory;
    }
    
    /**
     * Set emotional history from string
     * @param emotionalHistory Emotional history JSON string
     */
    public void setEmotionalHistory(String emotionalHistory) {
        this.emotionalHistory = emotionalHistory;
    }
    
    /**
     * Get contextual history as string
     * @return Contextual history JSON string
     */
    public String getContextualHistory() {
        return contextualHistory;
    }
    
    /**
     * Set contextual history from string
     * @param contextualHistory Contextual history JSON string
     */
    public void setContextualHistory(String contextualHistory) {
        this.contextualHistory = contextualHistory;
    }
    
    /**
     * Get emotional history as map
     * @return Map of emotion to frequency
     */
    public Map<String, Integer> getEmotionalHistoryMap() {
        Gson gson = new Gson();
        Map<String, Integer> map = gson.fromJson(
                emotionalHistory,
                new TypeToken<HashMap<String, Integer>>(){}.getType());
        
        if (map == null) {
            map = new HashMap<>();
        }
        
        return map;
    }
    
    /**
     * Set emotional history from map
     * @param emotionalHistoryMap Map of emotion to frequency
     */
    public void setEmotionalHistoryMap(Map<String, Integer> emotionalHistoryMap) {
        Gson gson = new Gson();
        this.emotionalHistory = gson.toJson(emotionalHistoryMap);
    }
    
    /**
     * Get contextual history as map
     * @return Map of context to frequency
     */
    public Map<String, Integer> getContextualHistoryMap() {
        Gson gson = new Gson();
        Map<String, Integer> map = gson.fromJson(
                contextualHistory,
                new TypeToken<HashMap<String, Integer>>(){}.getType());
        
        if (map == null) {
            map = new HashMap<>();
        }
        
        return map;
    }
    
    /**
     * Set contextual history from map
     * @param contextualHistoryMap Map of context to frequency
     */
    public void setContextualHistoryMap(Map<String, Integer> contextualHistoryMap) {
        Gson gson = new Gson();
        this.contextualHistory = gson.toJson(contextualHistoryMap);
    }
    
    /**
     * Update profile after a call
     */
    public void updateAfterCall() {
        this.callCount++;
        this.lastCallTimestamp = System.currentTimeMillis();
        
        // Increase familiarity based on call count
        float familiarityIncrease = 0.05f; // 5% increase per call
        this.relationshipFamiliarity = Math.min(1.0f, this.relationshipFamiliarity + familiarityIncrease);
    }
    
    /**
     * Record an emotion for this caller
     * @param emotion Emotion name
     */
    public void recordEmotion(String emotion) {
        Map<String, Integer> emotions = getEmotionalHistoryMap();
        emotions.put(emotion, emotions.getOrDefault(emotion, 0) + 1);
        setEmotionalHistoryMap(emotions);
    }
    
    /**
     * Record a context for this caller
     * @param context Context name
     */
    public void recordContext(String context) {
        Map<String, Integer> contexts = getContextualHistoryMap();
        contexts.put(context, contexts.getOrDefault(context, 0) + 1);
        setContextualHistoryMap(contexts);
    }
    
    /**
     * Get dominant emotion
     * @return The most frequently recorded emotion
     */
    public String getDominantEmotion() {
        Map<String, Integer> emotions = getEmotionalHistoryMap();
        if (emotions.isEmpty()) {
            return "neutral";
        }
        
        String dominantEmotion = "neutral";
        int maxCount = 0;
        
        for (Map.Entry<String, Integer> entry : emotions.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantEmotion = entry.getKey();
            }
        }
        
        return dominantEmotion;
    }
    
    /**
     * Get dominant context
     * @return The most frequently recorded context
     */
    public String getDominantContext() {
        Map<String, Integer> contexts = getContextualHistoryMap();
        if (contexts.isEmpty()) {
            return "general";
        }
        
        String dominantContext = "general";
        int maxCount = 0;
        
        for (Map.Entry<String, Integer> entry : contexts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantContext = entry.getKey();
            }
        }
        
        return dominantContext;
    }
    
    /**
     * Get last call date as string
     * @return Last call date string
     */
    public String getLastCallDateString() {
        if (lastCallTimestamp == 0) {
            return "Never";
        }
        return new Date(lastCallTimestamp).toString();
    }
}
