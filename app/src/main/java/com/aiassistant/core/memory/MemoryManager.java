package com.aiassistant.core.memory;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Integrated memory manager
 * Combines different memory types and provides a unified interface
 */
public class MemoryManager {
    private static final String TAG = "MemoryManager";
    
    // Singleton instance
    private static volatile MemoryManager instance;
    
    // Memory components
    private final ShortTermMemory shortTermMemory;
    private final LongTermMemory longTermMemory;
    private final EmotionalMemory emotionalMemory;
    
    // Memory keys for tracking important information
    private static final String KEY_USER_PREFERENCES = "user_preferences";
    private static final String KEY_CONVERSATION_CONTEXT = "conversation_context";
    private static final String KEY_SYSTEM_SETTINGS = "system_settings";
    
    /**
     * Get singleton instance
     * @param context Application context
     * @return The MemoryManager instance
     */
    public static synchronized MemoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new MemoryManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Private constructor
     * @param context Application context
     */
    private MemoryManager(Context context) {
        // Initialize memory components
        shortTermMemory = new ShortTermMemory();
        longTermMemory = new LongTermMemory(context);
        emotionalMemory = new EmotionalMemory(context);
        
        Log.d(TAG, "Memory manager initialized");
    }
    
    /**
     * Get short-term memory
     * @return Short-term memory
     */
    public ShortTermMemory getShortTermMemory() {
        return shortTermMemory;
    }
    
    /**
     * Get long-term memory
     * @return Long-term memory
     */
    public LongTermMemory getLongTermMemory() {
        return longTermMemory;
    }
    
    /**
     * Get emotional memory
     * @return Emotional memory
     */
    public EmotionalMemory getEmotionalMemory() {
        return emotionalMemory;
    }
    
    /**
     * Store information in memory
     * @param key Memory key
     * @param value Memory value
     * @param context Optional context tag
     * @param isPersistent Whether to store in long-term memory
     * @param importance Importance (0.0-1.0) for long-term memory
     */
    public void remember(String key, String value, String context, boolean isPersistent, float importance) {
        // Store in short-term memory
        shortTermMemory.remember(key, value, context);
        
        // If persistent, store in long-term memory
        if (isPersistent) {
            longTermMemory.store(key, value, importance);
        }
        
        Log.d(TAG, "Remembered: " + key + (isPersistent ? " (persistent)" : ""));
    }
    
    /**
     * Store information in short-term memory
     * @param key Memory key
     * @param value Memory value
     */
    public void rememberShortTerm(String key, String value) {
        remember(key, value, null, false, 0.0f);
    }
    
    /**
     * Store information in long-term memory
     * @param key Memory key
     * @param value Memory value
     * @param importance Importance (0.0-1.0)
     */
    public void rememberLongTerm(String key, String value, float importance) {
        remember(key, value, null, true, importance);
    }
    
    /**
     * Retrieve information from memory
     * First checks short-term memory, then long-term if not found
     * @param key Memory key
     * @return Memory value or null if not found
     */
    public String recall(String key) {
        // Check short-term memory first
        String value = shortTermMemory.recall(key);
        
        // If not found, check long-term memory
        if (value == null) {
            value = longTermMemory.retrieve(key);
            
            if (value != null) {
                // Cache in short-term memory for faster access
                shortTermMemory.remember(key, value);
            }
        }
        
        // Log result
        if (value != null) {
            Log.d(TAG, "Recalled: " + key);
        } else {
            Log.d(TAG, "Failed to recall: " + key);
        }
        
        return value;
    }
    
    /**
     * Check if memory contains key
     * @param key Memory key
     * @return True if memory contains key
     */
    public boolean contains(String key) {
        return shortTermMemory.contains(key) || longTermMemory.contains(key);
    }
    
    /**
     * Forget information from all memory
     * @param key Memory key
     */
    public void forget(String key) {
        shortTermMemory.forget(key);
        longTermMemory.remove(key);
        Log.d(TAG, "Forgot: " + key);
    }
    
    /**
     * Record an emotional state
     * @param emotion Emotion name
     * @param intensity Intensity (0.0-1.0)
     * @param context Optional context information
     */
    public void recordEmotion(String emotion, float intensity, String context) {
        emotionalMemory.recordEmotion(emotion, intensity, context);
    }
    
    /**
     * Get dominant emotion from history
     * @return Dominant emotion name
     */
    public String getDominantEmotion() {
        return emotionalMemory.getDominantEmotion();
    }
    
    /**
     * Store user preference
     * @param preference Preference name
     * @param value Preference value
     */
    public void setUserPreference(String preference, String value) {
        // Get existing preferences
        String preferencesJson = recall(KEY_USER_PREFERENCES);
        Map<String, String> preferences = parseJsonMap(preferencesJson);
        
        // Update preference
        preferences.put(preference, value);
        
        // Store updated preferences
        String updatedJson = mapToJsonString(preferences);
        rememberLongTerm(KEY_USER_PREFERENCES, updatedJson, 0.9f);
        
        Log.d(TAG, "Set user preference: " + preference + " = " + value);
    }
    
    /**
     * Get user preference
     * @param preference Preference name
     * @return Preference value or null if not found
     */
    public String getUserPreference(String preference) {
        // Get preferences
        String preferencesJson = recall(KEY_USER_PREFERENCES);
        Map<String, String> preferences = parseJsonMap(preferencesJson);
        
        // Get preference
        return preferences.get(preference);
    }
    
    /**
     * Set conversation context
     * @param context Context name
     * @param value Context value
     */
    public void setConversationContext(String context, String value) {
        // Get existing context
        String contextJson = recall(KEY_CONVERSATION_CONTEXT);
        Map<String, String> contextMap = parseJsonMap(contextJson);
        
        // Update context
        contextMap.put(context, value);
        
        // Store updated context
        String updatedJson = mapToJsonString(contextMap);
        rememberShortTerm(KEY_CONVERSATION_CONTEXT, updatedJson);
        
        Log.d(TAG, "Set conversation context: " + context + " = " + value);
    }
    
    /**
     * Get conversation context
     * @param context Context name
     * @return Context value or null if not found
     */
    public String getConversationContext(String context) {
        // Get context
        String contextJson = recall(KEY_CONVERSATION_CONTEXT);
        Map<String, String> contextMap = parseJsonMap(contextJson);
        
        // Get value
        return contextMap.get(context);
    }
    
    /**
     * Get all conversation contexts
     * @return Map of context names to values
     */
    public Map<String, String> getAllConversationContexts() {
        String contextJson = recall(KEY_CONVERSATION_CONTEXT);
        return parseJsonMap(contextJson);
    }
    
    /**
     * Clear conversation context
     */
    public void clearConversationContext() {
        rememberShortTerm(KEY_CONVERSATION_CONTEXT, "{}");
        Log.d(TAG, "Cleared conversation context");
    }
    
    /**
     * Search memory by key pattern
     * @param keyPattern Key pattern to search for
     * @return Map of matching key-value pairs
     */
    public Map<String, String> searchByKey(String keyPattern) {
        Map<String, String> result = new HashMap<>();
        
        // Search short-term memory
        List<ShortTermMemory.MemoryItem> shortTermResults = shortTermMemory.searchByKey(keyPattern);
        for (ShortTermMemory.MemoryItem item : shortTermResults) {
            result.put(item.getKey(), item.getValue());
        }
        
        // Search long-term memory
        Map<String, String> longTermResults = longTermMemory.searchByKey(keyPattern);
        result.putAll(longTermResults);
        
        return result;
    }
    
    /**
     * Search memory by value pattern
     * @param valuePattern Value pattern to search for
     * @return Map of matching key-value pairs
     */
    public Map<String, String> searchByValue(String valuePattern) {
        Map<String, String> result = new HashMap<>();
        
        // Search short-term memory
        List<ShortTermMemory.MemoryItem> shortTermResults = shortTermMemory.searchByValue(valuePattern);
        for (ShortTermMemory.MemoryItem item : shortTermResults) {
            result.put(item.getKey(), item.getValue());
        }
        
        // Search long-term memory
        Map<String, String> longTermResults = longTermMemory.searchByValue(valuePattern);
        result.putAll(longTermResults);
        
        return result;
    }
    
    /**
     * Get memory summary
     * @return Memory usage summary
     */
    public String getMemorySummary() {
        int shortTermCount = shortTermMemory.size();
        int longTermCount = longTermMemory.getAllMemories().size();
        int emotionalCount = emotionalMemory.getAllHistory().size();
        
        return String.format("Memory summary: %d short-term, %d long-term, %d emotional",
                shortTermCount, longTermCount, emotionalCount);
    }
    
    /**
     * Clear all memory
     */
    public void clearAllMemory() {
        shortTermMemory.clear();
        longTermMemory.clear();
        emotionalMemory.clear();
        Log.d(TAG, "Cleared all memory");
    }
    
    /**
     * Parse JSON string to map
     * @param json JSON string
     * @return Map of string to string
     */
    private Map<String, String> parseJsonMap(String json) {
        Map<String, String> result = new HashMap<>();
        
        if (json == null || json.isEmpty() || "{}".equals(json)) {
            return result;
        }
        
        // Simple JSON parsing (for basic maps only)
        try {
            // Remove braces
            json = json.substring(1, json.length() - 1).trim();
            
            if (json.isEmpty()) {
                return result;
            }
            
            // Split by commas (not inside quotes)
            List<String> pairs = splitJson(json);
            
            for (String pair : pairs) {
                // Split by colon
                int colonIndex = pair.indexOf(':');
                if (colonIndex > 0) {
                    String key = pair.substring(0, colonIndex).trim();
                    String value = pair.substring(colonIndex + 1).trim();
                    
                    // Remove quotes
                    key = key.substring(1, key.length() - 1);
                    
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    result.put(key, value);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Split JSON string by commas (ignoring commas inside quotes)
     * @param json JSON string
     * @return List of key-value pair strings
     */
    private List<String> splitJson(String json) {
        List<String> result = new ArrayList<>();
        
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char[] chars = json.toCharArray();
        
        for (char c : chars) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            }
            
            if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }
        
        return result;
    }
    
    /**
     * Convert map to JSON string
     * @param map Map of string to string
     * @return JSON string
     */
    private String mapToJsonString(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            
            json.append("\"").append(entry.getKey()).append("\":\"")
                .append(entry.getValue().replace("\"", "\\\"")).append("\"");
            
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
}
