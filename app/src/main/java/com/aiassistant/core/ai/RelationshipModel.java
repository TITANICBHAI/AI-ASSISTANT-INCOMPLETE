package com.aiassistant.core.ai;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Models the AI's understanding of a relationship with a person
 * Tracks sentiment, interaction history, and contextual information
 */
public class RelationshipModel {
    // Unique identifier for the relationship
    public String id;
    
    // Person identifiers
    public String personId;
    public String name;
    
    // Relationship metadata
    public long created;
    public long lastInteraction;
    public int interactionCount;
    
    // Sentiment tracking
    public double overallSentiment; // Range from -1.0 (negative) to 1.0 (positive)
    public List<InteractionRecord> recentInteractions;
    
    // Context information
    public Map<String, String> contextualInfo;
    public Map<String, Double> topics; // Topics of interest with relevance scores
    
    // Maximum number of recent interactions to store
    private static final int MAX_RECENT_INTERACTIONS = 10;
    
    /**
     * Record of a single interaction
     */
    public static class InteractionRecord {
        public long timestamp;
        public double sentiment;
        public String context;
        
        public InteractionRecord(long timestamp, double sentiment, String context) {
            this.timestamp = timestamp;
            this.sentiment = sentiment;
            this.context = context;
        }
        
        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("timestamp", timestamp);
            json.put("sentiment", sentiment);
            json.put("context", context);
            return json;
        }
        
        public static InteractionRecord fromJSON(JSONObject json) throws JSONException {
            return new InteractionRecord(
                json.getLong("timestamp"),
                json.getDouble("sentiment"),
                json.getString("context")
            );
        }
    }
    
    /**
     * Constructor for new relationship
     */
    public RelationshipModel(String personId, String name) {
        this.id = UUID.randomUUID().toString();
        this.personId = personId;
        this.name = name;
        this.created = System.currentTimeMillis();
        this.lastInteraction = this.created;
        this.interactionCount = 0;
        this.overallSentiment = 0.0;
        this.recentInteractions = new ArrayList<>();
        this.contextualInfo = new HashMap<>();
        this.topics = new HashMap<>();
    }
    
    /**
     * Add a new interaction with sentiment score
     */
    public void addInteraction(double sentiment) {
        addInteraction(sentiment, null);
    }
    
    /**
     * Add a new interaction with sentiment score and context
     */
    public void addInteraction(double sentiment, String context) {
        long now = System.currentTimeMillis();
        
        // Create record
        InteractionRecord record = new InteractionRecord(
            now,
            sentiment,
            context != null ? context : ""
        );
        
        // Add to recent interactions
        recentInteractions.add(0, record);
        
        // Trim if needed
        if (recentInteractions.size() > MAX_RECENT_INTERACTIONS) {
            recentInteractions.remove(recentInteractions.size() - 1);
        }
        
        // Update overall sentiment with weighted average
        // More recent interactions have higher weight
        if (interactionCount == 0) {
            overallSentiment = sentiment;
        } else {
            // 20% weight on new interaction, 80% on previous sentiment
            overallSentiment = (overallSentiment * 0.8) + (sentiment * 0.2);
        }
        
        // Update metadata
        lastInteraction = now;
        interactionCount++;
    }
    
    /**
     * Add contextual information about this relationship
     */
    public void addContextualInfo(String key, String value) {
        contextualInfo.put(key, value);
    }
    
    /**
     * Add or update topic relevance
     */
    public void updateTopicRelevance(String topic, double relevance) {
        Double currentRelevance = topics.get(topic);
        
        if (currentRelevance == null) {
            topics.put(topic, relevance);
        } else {
            // Weighted average, favoring new relevance slightly
            topics.put(topic, currentRelevance * 0.7 + relevance * 0.3);
        }
    }
    
    /**
     * Get the sentiment trend (positive, negative, neutral, or improving/declining)
     */
    public String getSentimentTrend() {
        if (recentInteractions.size() < 3) {
            // Not enough data for trend
            return overallSentiment > 0.3 ? "positive" : 
                   overallSentiment < -0.3 ? "negative" : "neutral";
        }
        
        // Calculate average sentiment for first and second half of interactions
        int midpoint = recentInteractions.size() / 2;
        double recentAvg = 0;
        double olderAvg = 0;
        
        for (int i = 0; i < midpoint; i++) {
            recentAvg += recentInteractions.get(i).sentiment;
        }
        
        for (int i = midpoint; i < recentInteractions.size(); i++) {
            olderAvg += recentInteractions.get(i).sentiment;
        }
        
        recentAvg /= midpoint;
        olderAvg /= (recentInteractions.size() - midpoint);
        
        double diff = recentAvg - olderAvg;
        
        if (Math.abs(diff) < 0.1) {
            return overallSentiment > 0.3 ? "positive" : 
                   overallSentiment < -0.3 ? "negative" : "neutral";
        } else {
            return diff > 0 ? "improving" : "declining";
        }
    }
    
    /**
     * Get most relevant topics (with relevance above threshold)
     */
    public List<String> getRelevantTopics(double threshold) {
        List<String> relevant = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : topics.entrySet()) {
            if (entry.getValue() >= threshold) {
                relevant.add(entry.getKey());
            }
        }
        
        return relevant;
    }
    
    /**
     * Convert to JSON for persistence
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        
        json.put("id", id);
        json.put("personId", personId);
        json.put("name", name);
        json.put("created", created);
        json.put("lastInteraction", lastInteraction);
        json.put("interactionCount", interactionCount);
        json.put("overallSentiment", overallSentiment);
        
        // Recent interactions
        JSONArray interactionsArray = new JSONArray();
        for (InteractionRecord record : recentInteractions) {
            interactionsArray.put(record.toJSON());
        }
        json.put("recentInteractions", interactionsArray);
        
        // Contextual info
        JSONObject contextObj = new JSONObject();
        for (Map.Entry<String, String> entry : contextualInfo.entrySet()) {
            contextObj.put(entry.getKey(), entry.getValue());
        }
        json.put("contextualInfo", contextObj);
        
        // Topics
        JSONObject topicsObj = new JSONObject();
        for (Map.Entry<String, Double> entry : topics.entrySet()) {
            topicsObj.put(entry.getKey(), entry.getValue());
        }
        json.put("topics", topicsObj);
        
        return json;
    }
    
    /**
     * Create from JSON persistence
     */
    public static RelationshipModel fromJSON(JSONObject json) throws JSONException {
        String personId = json.getString("personId");
        String name = json.getString("name");
        
        RelationshipModel model = new RelationshipModel(personId, name);
        
        model.id = json.getString("id");
        model.created = json.getLong("created");
        model.lastInteraction = json.getLong("lastInteraction");
        model.interactionCount = json.getInt("interactionCount");
        model.overallSentiment = json.getDouble("overallSentiment");
        
        // Load recent interactions
        model.recentInteractions.clear();
        JSONArray interactionsArray = json.getJSONArray("recentInteractions");
        for (int i = 0; i < interactionsArray.length(); i++) {
            InteractionRecord record = InteractionRecord.fromJSON(interactionsArray.getJSONObject(i));
            model.recentInteractions.add(record);
        }
        
        // Load contextual info
        model.contextualInfo.clear();
        JSONObject contextObj = json.getJSONObject("contextualInfo");
        if (contextObj != null && contextObj.length() > 0) {
            String[] keys = JSONObject.getNames(contextObj);
            if (keys != null) {
                for (String key : keys) {
                    model.contextualInfo.put(key, contextObj.getString(key));
                }
            }
        }
        
        // Load topics
        model.topics.clear();
        JSONObject topicsObj = json.getJSONObject("topics");
        if (topicsObj != null && topicsObj.length() > 0) {
            String[] keys = JSONObject.getNames(topicsObj);
            if (keys != null) {
                for (String key : keys) {
                    model.topics.put(key, topicsObj.getDouble(key));
                }
            }
        }
        
        return model;
    }
}
