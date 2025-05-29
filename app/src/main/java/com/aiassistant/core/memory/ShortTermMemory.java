package com.aiassistant.core.memory;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Short-term memory implementation
 * Volatile memory that's cleared when the app is closed
 */
public class ShortTermMemory {
    private static final String TAG = "ShortTermMemory";
    
    // Default expiration time (30 minutes)
    private static final long DEFAULT_EXPIRATION_MS = TimeUnit.MINUTES.toMillis(30);
    
    // Maximum memory items
    private static final int MAX_MEMORY_ITEMS = 1000;
    
    // Memory storage (key -> memory item)
    private final ConcurrentHashMap<String, MemoryItem> memoryMap;
    
    /**
     * Constructor
     */
    public ShortTermMemory() {
        this.memoryMap = new ConcurrentHashMap<>();
        Log.d(TAG, "Short-term memory initialized");
    }
    
    /**
     * Store information in memory
     * @param key Memory key
     * @param value Memory value
     */
    public void remember(String key, String value) {
        remember(key, value, null);
    }
    
    /**
     * Store information in memory with context
     * @param key Memory key
     * @param value Memory value
     * @param context Context tag (optional)
     */
    public void remember(String key, String value, String context) {
        remember(key, value, context, DEFAULT_EXPIRATION_MS);
    }
    
    /**
     * Store information in memory with expiration
     * @param key Memory key
     * @param value Memory value
     * @param context Context tag (optional)
     * @param expirationMs Expiration time in milliseconds
     */
    public void remember(String key, String value, String context, long expirationMs) {
        if (key == null || key.isEmpty()) {
            Log.w(TAG, "Cannot remember with null or empty key");
            return;
        }
        
        // Create memory item
        MemoryItem item = new MemoryItem(key, value, context, expirationMs);
        
        // Store in memory
        memoryMap.put(key, item);
        
        // Check if we need to clear old items
        if (memoryMap.size() > MAX_MEMORY_ITEMS) {
            clearOldestItems(MAX_MEMORY_ITEMS / 5);
        }
        
        Log.d(TAG, "Remembered: " + key);
    }
    
    /**
     * Retrieve information from memory
     * @param key Memory key
     * @return Memory value or null if not found or expired
     */
    public String recall(String key) {
        if (key == null || key.isEmpty()) {
            Log.w(TAG, "Cannot recall with null or empty key");
            return null;
        }
        
        // Get memory item
        MemoryItem item = memoryMap.get(key);
        
        if (item != null) {
            // Check if expired
            if (item.isExpired()) {
                // Remove expired item
                memoryMap.remove(key);
                Log.d(TAG, "Removed expired memory: " + key);
                return null;
            }
            
            // Update last access time
            item.updateLastAccess();
            
            Log.d(TAG, "Recalled: " + key);
            return item.getValue();
        }
        
        Log.d(TAG, "Failed to recall: " + key);
        return null;
    }
    
    /**
     * Check if memory contains key
     * @param key Memory key
     * @return True if memory contains unexpired key
     */
    public boolean contains(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        
        // Get memory item
        MemoryItem item = memoryMap.get(key);
        
        if (item != null) {
            // Check if expired
            if (item.isExpired()) {
                // Remove expired item
                memoryMap.remove(key);
                return false;
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Forget information from memory
     * @param key Memory key
     */
    public void forget(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }
        
        memoryMap.remove(key);
        Log.d(TAG, "Forgot: " + key);
    }
    
    /**
     * Search memory by key pattern
     * @param keyPattern Key pattern to search for
     * @return List of matching memory items
     */
    public List<MemoryItem> searchByKey(String keyPattern) {
        List<MemoryItem> result = new ArrayList<>();
        
        if (keyPattern == null || keyPattern.isEmpty()) {
            return result;
        }
        
        // Check all keys
        for (Map.Entry<String, MemoryItem> entry : memoryMap.entrySet()) {
            if (entry.getKey().contains(keyPattern)) {
                MemoryItem item = entry.getValue();
                
                // Skip expired items
                if (item.isExpired()) {
                    continue;
                }
                
                result.add(item);
            }
        }
        
        return result;
    }
    
    /**
     * Search memory by value pattern
     * @param valuePattern Value pattern to search for
     * @return List of matching memory items
     */
    public List<MemoryItem> searchByValue(String valuePattern) {
        List<MemoryItem> result = new ArrayList<>();
        
        if (valuePattern == null || valuePattern.isEmpty()) {
            return result;
        }
        
        // Check all values
        for (MemoryItem item : memoryMap.values()) {
            // Skip expired items
            if (item.isExpired()) {
                continue;
            }
            
            if (item.getValue().contains(valuePattern)) {
                result.add(item);
            }
        }
        
        return result;
    }
    
    /**
     * Get memory item for key
     * @param key Memory key
     * @return Memory item or null if not found or expired
     */
    public MemoryItem getMemoryItem(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        
        // Get memory item
        MemoryItem item = memoryMap.get(key);
        
        if (item != null) {
            // Check if expired
            if (item.isExpired()) {
                // Remove expired item
                memoryMap.remove(key);
                return null;
            }
            
            return item;
        }
        
        return null;
    }
    
    /**
     * Get all memory items
     * @return Map of memory items
     */
    public Map<String, MemoryItem> getAllMemories() {
        Map<String, MemoryItem> result = new HashMap<>();
        
        // Remove expired items while copying
        for (Map.Entry<String, MemoryItem> entry : memoryMap.entrySet()) {
            MemoryItem item = entry.getValue();
            
            // Skip expired items
            if (item.isExpired()) {
                memoryMap.remove(entry.getKey());
                continue;
            }
            
            result.put(entry.getKey(), item);
        }
        
        return result;
    }
    
    /**
     * Get memory items by context
     * @param context Context tag
     * @return List of memory items with matching context
     */
    public List<MemoryItem> getMemoriesByContext(String context) {
        List<MemoryItem> result = new ArrayList<>();
        
        if (context == null || context.isEmpty()) {
            return result;
        }
        
        // Check all items
        for (MemoryItem item : memoryMap.values()) {
            // Skip expired items
            if (item.isExpired()) {
                continue;
            }
            
            if (context.equals(item.getContext())) {
                result.add(item);
            }
        }
        
        return result;
    }
    
    /**
     * Clear expired memory items
     * @return Number of cleared items
     */
    public int clearExpiredItems() {
        int count = 0;
        
        // Check all items
        for (Map.Entry<String, MemoryItem> entry : memoryMap.entrySet()) {
            MemoryItem item = entry.getValue();
            
            // Remove expired items
            if (item.isExpired()) {
                memoryMap.remove(entry.getKey());
                count++;
            }
        }
        
        if (count > 0) {
            Log.d(TAG, "Cleared " + count + " expired items");
        }
        
        return count;
    }
    
    /**
     * Clear oldest memory items
     * @param count Number of items to clear
     * @return Number of cleared items
     */
    public int clearOldestItems(int count) {
        if (count <= 0 || memoryMap.isEmpty()) {
            return 0;
        }
        
        // Sort by last access time
        List<Map.Entry<String, MemoryItem>> entries = new ArrayList<>(memoryMap.entrySet());
        entries.sort((a, b) -> Long.compare(a.getValue().getLastAccessTime(), b.getValue().getLastAccessTime()));
        
        // Clear oldest items
        int cleared = 0;
        for (int i = 0; i < count && i < entries.size(); i++) {
            memoryMap.remove(entries.get(i).getKey());
            cleared++;
        }
        
        if (cleared > 0) {
            Log.d(TAG, "Cleared " + cleared + " oldest items");
        }
        
        return cleared;
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
        Log.d(TAG, "Cleared all short-term memory");
    }
    
    /**
     * Memory item class
     */
    public static class MemoryItem {
        private final String key;
        private final String value;
        private final String context;
        private final long creationTime;
        private final long expirationTime;
        private long lastAccessTime;
        
        /**
         * Constructor
         * @param key Memory key
         * @param value Memory value
         * @param context Context tag (optional)
         * @param expirationMs Expiration time in milliseconds
         */
        public MemoryItem(String key, String value, String context, long expirationMs) {
            this.key = key;
            this.value = value;
            this.context = context;
            this.creationTime = System.currentTimeMillis();
            this.lastAccessTime = this.creationTime;
            this.expirationTime = this.creationTime + expirationMs;
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
         * Get context tag
         * @return Context tag
         */
        public String getContext() {
            return context;
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
         * Get expiration time
         * @return Expiration time in milliseconds
         */
        public long getExpirationTime() {
            return expirationTime;
        }
        
        /**
         * Check if memory item is expired
         * @return True if expired
         */
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
        
        /**
         * Update last access time
         */
        public void updateLastAccess() {
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        /**
         * Get time to expiration
         * @return Time to expiration in milliseconds
         */
        public long getTimeToExpiration() {
            long current = System.currentTimeMillis();
            
            if (current > expirationTime) {
                return 0;
            }
            
            return expirationTime - current;
        }
    }
}
