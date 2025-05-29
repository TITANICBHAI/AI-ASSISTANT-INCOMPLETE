package com.aiassistant.core.memory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Long-term memory implementation
 * Persistent memory that's stored in SharedPreferences
 */
public class LongTermMemory {
    private static final String TAG = "LongTermMemory";
    
    // SharedPreferences file name
    private static final String PREFS_NAME = "ai_long_term_memory";
    
    // Memory keys
    private static final String KEY_MEMORY = "memory_data";
    private static final String KEY_METADATA = "memory_metadata";
    
    // Context for accessing SharedPreferences
    private final Context context;
    
    // Gson for serialization
    private final Gson gson;
    
    // Background thread executor
    private final Executor executor;
    
    // Memory storage (key -> memory item)
    private Map<String, MemoryItem> memoryMap;
    
    /**
     * Constructor
     * @param context Application context
     */
    public LongTermMemory(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
        
        // Load memory from SharedPreferences
        loadMemory();
        
        Log.d(TAG, "Long-term memory initialized with " + memoryMap.size() + " items");
    }
    
    /**
     * Store information in long-term memory
     * @param key Memory key
     * @param value Memory value
     * @param importance Importance (0.0-1.0)
     */
    public void store(String key, String value, float importance) {
        if (key == null || key.isEmpty()) {
            Log.w(TAG, "Cannot store with null or empty key");
            return;
        }
        
        // Clamp importance
        importance = Math.max(0.0f, Math.min(1.0f, importance));
        
        // Create memory item
        MemoryItem item = new MemoryItem(key, value, importance);
        
        // Store in memory
        memoryMap.put(key, item);
        
        // Save to SharedPreferences
        saveMemory();
        
        Log.d(TAG, "Stored in long-term memory: " + key + " (importance: " + importance + ")");
    }
    
    /**
     * Retrieve information from long-term memory
     * @param key Memory key
     * @return Memory value or null if not found
     */
    public String retrieve(String key) {
        if (key == null || key.isEmpty()) {
            Log.w(TAG, "Cannot retrieve with null or empty key");
            return null;
        }
        
        // Get memory item
        MemoryItem item = memoryMap.get(key);
        
        if (item != null) {
            // Update access count and last access time
            item.incrementAccessCount();
            
            // Save to SharedPreferences (in background)
            saveMemory();
            
            Log.d(TAG, "Retrieved from long-term memory: " + key);
            return item.getValue();
        }
        
        Log.d(TAG, "Failed to retrieve from long-term memory: " + key);
        return null;
    }
    
    /**
     * Check if memory contains key
     * @param key Memory key
     * @return True if memory contains key
     */
    public boolean contains(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        
        return memoryMap.containsKey(key);
    }
    
    /**
     * Remove information from long-term memory
     * @param key Memory key
     */
    public void remove(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        
        if (memoryMap.remove(key) != null) {
            // Save to SharedPreferences
            saveMemory();
            Log.d(TAG, "Removed from long-term memory: " + key);
        }
    }
    
    /**
     * Search memory by key pattern
     * @param keyPattern Key pattern to search for
     * @return Map of matching key-value pairs
     */
    public Map<String, String> searchByKey(String keyPattern) {
        Map<String, String> result = new HashMap<>();
        
        if (keyPattern == null || keyPattern.isEmpty()) {
            return result;
        }
        
        // Check all keys
        for (Map.Entry<String, MemoryItem> entry : memoryMap.entrySet()) {
            if (entry.getKey().contains(keyPattern)) {
                result.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Search memory by value pattern
     * @param valuePattern Value pattern to search for
     * @return Map of matching key-value pairs
     */
    public Map<String, String> searchByValue(String valuePattern) {
        Map<String, String> result = new HashMap<>();
        
        if (valuePattern == null || valuePattern.isEmpty()) {
            return result;
        }
        
        // Check all values
        for (Map.Entry<String, MemoryItem> entry : memoryMap.entrySet()) {
            if (entry.getValue().getValue().contains(valuePattern)) {
                result.put(entry.getKey(), entry.getValue().getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Get memory item for key
     * @param key Memory key
     * @return Memory item or null if not found
     */
    public MemoryItem getMemoryItem(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        
        return memoryMap.get(key);
    }
    
    /**
     * Get all memory items
     * @return Map of memory items
     */
    public Map<String, MemoryItem> getAllMemories() {
        return new HashMap<>(memoryMap);
    }
    
    /**
     * Get most important memories
     * @param count Maximum number of memories to return
     * @return List of most important memory items
     */
    public List<MemoryItem> getMostImportantMemories(int count) {
        // Sort by importance
        List<MemoryItem> items = new ArrayList<>(memoryMap.values());
        Collections.sort(items, (a, b) -> Float.compare(b.getImportance(), a.getImportance()));
        
        // Limit result size
        if (items.size() > count) {
            items = items.subList(0, count);
        }
        
        return items;
    }
    
    /**
     * Get most frequently accessed memories
     * @param count Maximum number of memories to return
     * @return List of most frequently accessed memory items
     */
    public List<MemoryItem> getMostAccessedMemories(int count) {
        // Sort by access count
        List<MemoryItem> items = new ArrayList<>(memoryMap.values());
        Collections.sort(items, (a, b) -> Integer.compare(b.getAccessCount(), a.getAccessCount()));
        
        // Limit result size
        if (items.size() > count) {
            items = items.subList(0, count);
        }
        
        return items;
    }
    
    /**
     * Get most recently accessed memories
     * @param count Maximum number of memories to return
     * @return List of most recently accessed memory items
     */
    public List<MemoryItem> getMostRecentMemories(int count) {
        // Sort by last access time
        List<MemoryItem> items = new ArrayList<>(memoryMap.values());
        Collections.sort(items, (a, b) -> Long.compare(b.getLastAccessTime(), a.getLastAccessTime()));
        
        // Limit result size
        if (items.size() > count) {
            items = items.subList(0, count);
        }
        
        return items;
    }
    
    /**
     * Update importance of memory item
     * @param key Memory key
     * @param importance New importance value (0.0-1.0)
     */
    public void updateImportance(String key, float importance) {
        if (key == null || key.isEmpty()) {
            return;
        }
        
        // Clamp importance
        importance = Math.max(0.0f, Math.min(1.0f, importance));
        
        // Get memory item
        MemoryItem item = memoryMap.get(key);
        
        if (item != null) {
            // Update importance
            item.setImportance(importance);
            
            // Save to SharedPreferences
            saveMemory();
            
            Log.d(TAG, "Updated importance of " + key + " to " + importance);
        }
    }
    
    /**
     * Get memory size
     * @return Number of memory items
     */
    public int size() {
        return memoryMap.size();
    }
    
    /**
     * Clear all memory
     */
    public void clear() {
        memoryMap.clear();
        saveMemory();
        Log.d(TAG, "Cleared all long-term memory");
    }
    
    /**
     * Load memory from SharedPreferences
     */
    private void loadMemory() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Get memory data
        String memoryJson = prefs.getString(KEY_MEMORY, "{}");
        
        // Parse JSON
        Type memoryType = new TypeToken<HashMap<String, MemoryItem>>(){}.getType();
        
        try {
            memoryMap = gson.fromJson(memoryJson, memoryType);
            
            // Initialize if null
            if (memoryMap == null) {
                memoryMap = new HashMap<>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading memory: " + e.getMessage());
            memoryMap = new HashMap<>();
        }
    }
    
    /**
     * Save memory to SharedPreferences
     */
    private void saveMemory() {
        // Save on background thread
        executor.execute(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                
                // Convert to JSON
                String memoryJson = gson.toJson(memoryMap);
                
                // Save to SharedPreferences
                editor.putString(KEY_MEMORY, memoryJson);
                editor.apply();
                
                Log.d(TAG, "Saved memory to SharedPreferences");
            } catch (Exception e) {
                Log.e(TAG, "Error saving memory: " + e.getMessage());
            }
        });
    }
    
    /**
     * Memory item class
     */
    public static class MemoryItem {
        private final String key;
        private final String value;
        private float importance;
        private final long creationTime;
        private long lastAccessTime;
        private int accessCount;
        
        /**
         * Constructor
         * @param key Memory key
         * @param value Memory value
         * @param importance Importance (0.0-1.0)
         */
        public MemoryItem(String key, String value, float importance) {
            this.key = key;
            this.value = value;
            this.importance = Math.max(0.0f, Math.min(1.0f, importance));
            this.creationTime = System.currentTimeMillis();
            this.lastAccessTime = this.creationTime;
            this.accessCount = 0;
        }
        
        /**
         * Get memory key
         * @return Memory key
         */
        public String getKey() {
            return key;
        }
        
        /**
         * Get memory value
         * @return Memory value
         */
        public String getValue() {
            return value;
        }
        
        /**
         * Get importance
         * @return Importance (0.0-1.0)
         */
        public float getImportance() {
            return importance;
        }
        
        /**
         * Set importance
         * @param importance Importance (0.0-1.0)
         */
        public void setImportance(float importance) {
            this.importance = Math.max(0.0f, Math.min(1.0f, importance));
        }
        
        /**
         * Get creation time
         * @return Creation time in milliseconds
         */
        public long getCreationTime() {
            return creationTime;
        }
        
        /**
         * Get last access time
         * @return Last access time in milliseconds
         */
        public long getLastAccessTime() {
            return lastAccessTime;
        }
        
        /**
         * Get access count
         * @return Number of times this memory has been accessed
         */
        public int getAccessCount() {
            return accessCount;
        }
        
        /**
         * Increment access count and update last access time
         */
        public void incrementAccessCount() {
            this.accessCount++;
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        /**
         * Get age of memory
         * @return Age in milliseconds
         */
        public long getAge() {
            return System.currentTimeMillis() - creationTime;
        }
        
        /**
         * Get time since last access
         * @return Time since last access in milliseconds
         */
        public long getTimeSinceLastAccess() {
            return System.currentTimeMillis() - lastAccessTime;
        }
    }
}
