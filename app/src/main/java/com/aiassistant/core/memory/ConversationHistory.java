package com.aiassistant.core.memory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages conversation history with emotional context
 */
public class ConversationHistory {
    private static final String TAG = "ConversationHistory";
    
    // SharedPreferences names
    private static final String PREFS_NAME = "conversation_history";
    private static final String CONVERSATIONS_KEY = "conversations";
    
    // Maximum entries per caller
    private static final int MAX_ENTRIES = 50;
    
    private final Context context;
    private final SharedPreferences preferences;
    private final Gson gson;
    
    // Cache of conversation histories
    private Map<String, List<ConversationEntry>> conversations;
    
    /**
     * Constructor
     * @param context Application context
     */
    public ConversationHistory(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        
        // Load conversation histories from storage
        loadConversations();
    }
    
    /**
     * Inner class for conversation entries
     */
    public static class ConversationEntry {
        public String text;
        public Map<String, Float> emotions;
        public long timestamp;
        
        public ConversationEntry(String text, Map<String, Float> emotions) {
            this.text = text;
            this.emotions = emotions;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Load conversations from SharedPreferences
     */
    private void loadConversations() {
        String json = preferences.getString(CONVERSATIONS_KEY, null);
        
        if (json != null) {
            try {
                Type type = new TypeToken<Map<String, List<ConversationEntry>>>() {}.getType();
                conversations = gson.fromJson(json, type);
                Log.d(TAG, "Loaded conversations for " + conversations.size() + " callers");
            } catch (Exception e) {
                Log.e(TAG, "Error loading conversations: " + e.getMessage());
                conversations = new HashMap<>();
            }
        } else {
            conversations = new HashMap<>();
            Log.d(TAG, "No conversations found, creating new map");
        }
    }
    
    /**
     * Save conversations to SharedPreferences
     */
    private void saveConversations() {
        try {
            String json = gson.toJson(conversations);
            preferences.edit().putString(CONVERSATIONS_KEY, json).apply();
            Log.d(TAG, "Saved conversations for " + conversations.size() + " callers");
        } catch (Exception e) {
            Log.e(TAG, "Error saving conversations: " + e.getMessage());
        }
    }
    
    /**
     * Add an entry to the conversation history
     * @param phoneNumber Caller's phone number
     * @param text The conversation text
     * @param emotions The emotional context for this text
     */
    public void addEntry(String phoneNumber, String text, Map<String, Float> emotions) {
        // Get existing entries or create new list
        List<ConversationEntry> entries = conversations.get(phoneNumber);
        if (entries == null) {
            entries = new ArrayList<>();
            conversations.put(phoneNumber, entries);
        }
        
        // Add new entry
        entries.add(new ConversationEntry(text, emotions));
        
        // Trim if exceeding maximum size
        if (entries.size() > MAX_ENTRIES) {
            entries.remove(0); // Remove oldest entry
        }
        
        // Save updated conversations
        saveConversations();
    }
    
    /**
     * Get conversation history for a caller
     * @param phoneNumber Caller's phone number
     * @return List of conversation entries, or empty list if no history
     */
    public List<ConversationEntry> getConversationHistory(String phoneNumber) {
        List<ConversationEntry> entries = conversations.get(phoneNumber);
        if (entries == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(entries);
    }
    
    /**
     * Get conversation history for a caller, limited to the most recent entries
     * @param phoneNumber Caller's phone number
     * @param limit Maximum number of entries to return
     * @return List of recent conversation entries
     */
    public List<ConversationEntry> getRecentConversationHistory(String phoneNumber, int limit) {
        List<ConversationEntry> allEntries = getConversationHistory(phoneNumber);
        
        if (allEntries.size() <= limit) {
            return allEntries;
        }
        
        return allEntries.subList(allEntries.size() - limit, allEntries.size());
    }
    
    /**
     * Clear conversation history for a caller
     * @param phoneNumber Caller's phone number
     */
    public void clearConversationHistory(String phoneNumber) {
        if (conversations.containsKey(phoneNumber)) {
            conversations.remove(phoneNumber);
            saveConversations();
            Log.d(TAG, "Cleared conversation history for caller: " + phoneNumber);
        }
    }
    
    /**
     * Get all conversation histories
     * @return Map of phone numbers to conversation histories
     */
    public Map<String, List<ConversationEntry>> getAllConversations() {
        return new HashMap<>(conversations);
    }
    
    /**
     * Get the emotional trend for a caller
     * @param phoneNumber Caller's phone number
     * @return Map of emotions to their trend values (-1.0 to 1.0, where positive means increasing)
     */
    public Map<String, Float> getEmotionalTrend(String phoneNumber) {
        List<ConversationEntry> entries = getConversationHistory(phoneNumber);
        Map<String, Float> trends = new HashMap<>();
        
        if (entries.size() < 3) {
            return trends; // Not enough data for trend analysis
        }
        
        // Get the last few entries for trend analysis
        int numEntries = Math.min(entries.size(), 5);
        List<ConversationEntry> recentEntries = entries.subList(entries.size() - numEntries, entries.size());
        
        // Track emotions through the entries
        Map<String, List<Float>> emotionValues = new HashMap<>();
        
        for (ConversationEntry entry : recentEntries) {
            for (Map.Entry<String, Float> emotion : entry.emotions.entrySet()) {
                String emotionType = emotion.getKey();
                float intensity = emotion.getValue();
                
                if (!emotionValues.containsKey(emotionType)) {
                    emotionValues.put(emotionType, new ArrayList<>());
                }
                
                emotionValues.get(emotionType).add(intensity);
            }
        }
        
        // Calculate trends
        for (Map.Entry<String, List<Float>> entry : emotionValues.entrySet()) {
            String emotionType = entry.getKey();
            List<Float> values = entry.getValue();
            
            if (values.size() < 2) {
                continue;
            }
            
            // Calculate simple linear trend
            float first = values.get(0);
            float last = values.get(values.size() - 1);
            float trend = (last - first) / Math.max(0.1f, first);
            
            // Normalize to -1.0 to 1.0 range
            trend = Math.max(-1.0f, Math.min(1.0f, trend));
            
            trends.put(emotionType, trend);
        }
        
        return trends;
    }
}
