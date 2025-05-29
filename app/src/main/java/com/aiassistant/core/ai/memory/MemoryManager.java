package com.aiassistant.core.ai.memory;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages AI memory system including short-term, long-term, and emotional memory
 * Coordinates memory storage, retrieval, and persistence
 */
public class MemoryManager {
    private static final String TAG = "MemoryManager";
    
    // Singleton instance
    private static volatile MemoryManager instance;
    
    // Memory components
    private final ShortTermMemory shortTermMemory;
    private final LongTermMemory longTermMemory;
    private final EmotionalMemory emotionalMemory;
    
    // Application context
    private final Context context;
    
    // Memory tags for organizing memories
    private final Map<String, List<String>> memoryTags;
    
    // Background thread executor
    private final ExecutorService executor;
    
    /**
     * Private constructor for singleton
     * @param context Application context
     */
    private MemoryManager(Context context) {
        this.context = context.getApplicationContext();
        this.shortTermMemory = new ShortTermMemory();
        this.longTermMemory = new LongTermMemory(context);
        this.emotionalMemory = new EmotionalMemory();
        this.memoryTags = new HashMap<>();
        this.executor = Executors.newSingleThreadExecutor();
        
        // Start memory service
        startMemoryService();
        
        Log.d(TAG, "MemoryManager initialized");
    }
    
    /**
     * Get singleton instance
     * @param context Application context
     * @return MemoryManager instance
     */
    public static synchronized MemoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new MemoryManager(context);
        }
        return instance;
    }
    
    /**
     * Start memory service for background persistence
     */
    private void startMemoryService() {
        try {
            Intent intent = new Intent(context, MemoryService.class);
            context.startService(intent);
            Log.d(TAG, "Memory service started");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start memory service: " + e.getMessage());
        }
    }
    
    /**
     * Store memory in short-term memory
     * @param key Memory key
     * @param value Memory value
     */
    public void storeShortTermMemory(String key, String value) {
        shortTermMemory.storeMemory(key, value);
        Log.d(TAG, "Stored in short-term memory: " + key);
    }
    
    /**
     * Store memory in long-term memory
     * @param key Memory key
     * @param value Memory value
     */
    public void storeLongTermMemory(String key, String value) {
        executor.execute(() -> {
            longTermMemory.storeMemory(key, value);
            Log.d(TAG, "Stored in long-term memory: " + key);
        });
    }
    
    /**
     * Store emotional memory
     * @param key Memory key
     * @param value Memory value
     * @param valence Emotional valence (-1.0 to 1.0)
     * @param arousal Emotional arousal (0.0 to 1.0)
     */
    public void storeEmotionalMemory(String key, String value, float valence, float arousal) {
        emotionalMemory.storeMemory(key, value, valence, arousal);
        Log.d(TAG, "Stored in emotional memory: " + key);
    }
    
    /**
     * Tag memory for organization
     * @param key Memory key
     * @param tag Tag to associate with memory
     */
    public void tagMemory(String key, String tag) {
        if (key == null || tag == null) {
            return;
        }
        
        // Get or create tag list
        List<String> keys = memoryTags.getOrDefault(tag, new ArrayList<>());
        
        // Add key if not already present
        if (!keys.contains(key)) {
            keys.add(key);
        }
        
        // Update tag map
        memoryTags.put(tag, keys);
        
        Log.d(TAG, "Tagged memory " + key + " with " + tag);
    }
    
    /**
     * Get memory from short-term memory
     * @param key Memory key
     * @return Memory value or null if not found
     */
    public String getShortTermMemory(String key) {
        return shortTermMemory.getMemory(key);
    }
    
    /**
     * Get memory from long-term memory
     * @param key Memory key
     * @return Memory value or null if not found
     */
    public String getLongTermMemory(String key) {
        return longTermMemory.getMemory(key);
    }
    
    /**
     * Get emotional memory
     * @param key Memory key
     * @return Memory entry or null if not found
     */
    public EmotionalMemory.EmotionalMemoryEntry getEmotionalMemory(String key) {
        return emotionalMemory.getMemoryEntry(key);
    }
    
    /**
     * Get all short-term memories
     * @return Map of all short-term memories
     */
    public Map<String, String> getAllShortTermMemories() {
        return shortTermMemory.getAllMemories();
    }
    
    /**
     * Get all long-term memories
     * @return Map of all long-term memories
     */
    public Map<String, String> getAllLongTermMemories() {
        return longTermMemory.getAllMemories();
    }
    
    /**
     * Get all emotional memories
     * @return Map of all emotional memories
     */
    public Map<String, EmotionalMemory.EmotionalMemoryEntry> getAllEmotionalMemories() {
        return emotionalMemory.getAllMemories();
    }
    
    /**
     * Transfer memory from short-term to long-term
     * @param key Memory key
     */
    public void transferToLongTerm(String key) {
        String value = shortTermMemory.getMemory(key);
        if (value != null) {
            storeLongTermMemory(key, value);
            Log.d(TAG, "Transferred memory to long-term: " + key);
        }
    }
    
    /**
     * Transfer all short-term memories to long-term
     */
    public void transferAllToLongTerm() {
        Map<String, String> memories = shortTermMemory.getAllMemories();
        for (Map.Entry<String, String> entry : memories.entrySet()) {
            storeLongTermMemory(entry.getKey(), entry.getValue());
        }
        Log.d(TAG, "Transferred all memories to long-term");
    }
    
    /**
     * Clear all short-term memories
     */
    public void clearShortTermMemories() {
        shortTermMemory.clear();
        Log.d(TAG, "Cleared all short-term memories");
    }
    
    /**
     * Get memories by tag
     * @param tag Memory tag
     * @return List of memory values
     */
    public List<String> getMemoriesByTag(String tag) {
        List<String> result = new ArrayList<>();
        
        // Get keys for tag
        List<String> keys = memoryTags.getOrDefault(tag, new ArrayList<>());
        
        // Get memory values
        for (String key : keys) {
            // Try short-term first
            String value = shortTermMemory.getMemory(key);
            
            // Try long-term if not in short-term
            if (value == null) {
                value = longTermMemory.getMemory(key);
            }
            
            // Add to result if found
            if (value != null) {
                result.add(value);
            }
        }
        
        return result;
    }
    
    /**
     * Load memories by tag
     * Loads from long-term to short-term memory
     * @param tag Memory tag
     */
    public void loadMemoryByTag(String tag) {
        // Get keys for tag
        List<String> keys = memoryTags.getOrDefault(tag, new ArrayList<>());
        
        // Load memories
        for (String key : keys) {
            String value = longTermMemory.getMemory(key);
            if (value != null) {
                shortTermMemory.storeMemory(key, value);
            }
        }
        
        Log.d(TAG, "Loaded memories with tag: " + tag);
    }
    
    /**
     * Search memories by content
     * @param query Search query
     * @return List of matching memory values
     */
    public List<String> searchMemories(String query) {
        List<String> results = new ArrayList<>();
        
        // Search short-term memory
        for (Map.Entry<String, String> entry : shortTermMemory.getAllMemories().entrySet()) {
            if (entry.getValue().toLowerCase().contains(query.toLowerCase())) {
                results.add(entry.getValue());
            }
        }
        
        // Search long-term memory
        for (Map.Entry<String, String> entry : longTermMemory.getAllMemories().entrySet()) {
            if (entry.getValue().toLowerCase().contains(query.toLowerCase())) {
                results.add(entry.getValue());
            }
        }
        
        Log.d(TAG, "Memory search for '" + query + "' found " + results.size() + " results");
        return results;
    }
    
    /**
     * Persist all memories
     * Ensures all memories are saved to storage
     */
    public void persistAllMemories() {
        executor.execute(() -> {
            // Persist long-term memory
            longTermMemory.saveToStorage();
            
            // Transfer important short-term memories to long-term
            transferImportantMemoriesToLongTerm();
            
            Log.d(TAG, "All memories persisted");
        });
    }
    
    /**
     * Transfer important short-term memories to long-term
     * Based on recency and access frequency
     */
    private void transferImportantMemoriesToLongTerm() {
        Map<String, Integer> accessCounts = shortTermMemory.getAccessCounts();
        
        for (Map.Entry<String, Integer> entry : accessCounts.entrySet()) {
            // If accessed more than twice, consider important
            if (entry.getValue() > 2) {
                String key = entry.getKey();
                String value = shortTermMemory.getMemory(key);
                if (value != null) {
                    storeLongTermMemory(key, value);
                }
            }
        }
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        // Persist all memories
        persistAllMemories();
        
        // Shutdown executor
        executor.shutdown();
        
        Log.d(TAG, "MemoryManager shutdown");
    }
}
