package com.aiassistant.core.ai.memory;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Short-term memory component for temporary storage of recent information
 * Provides fast access with limited capacity
 */
public class ShortTermMemory {
    private static final String TAG = "ShortTermMemory";
    
    // Memory storage
    private final Map<String, String> memories;
    
    // Access tracking
    private final Map<String, Integer> accessCounts;
    private final Map<String, Long> lastAccessTimes;
    
    // Configuration
    private static final int MAX_MEMORIES = 100;
    
    /**
     * Constructor
     */
    public ShortTermMemory() {
        this.memories = new HashMap<>();
        this.accessCounts = new HashMap<>();
        this.lastAccessTimes = new HashMap<>();
        
        Log.d(TAG, "ShortTermMemory initialized");
    }
    
    /**
     * Store memory
     * @param key Memory key
     * @param value Memory value
     */
    public synchronized void storeMemory(String key, String value) {
        // Ensure capacity
        ensureCapacity();
        
        // Store memory
        memories.put(key, value);
        
        // Initialize access tracking
        accessCounts.put(key, 0);
        lastAccessTimes.put(key, System.currentTimeMillis());
        
        Log.d(TAG, "Stored memory: " + key);
    }
    
    /**
     * Get memory
     * @param key Memory key
     * @return Memory value or null if not found
     */
    public synchronized String getMemory(String key) {
        String value = memories.get(key);
        
        if (value != null) {
            // Update access tracking
            int count = accessCounts.getOrDefault(key, 0);
            accessCounts.put(key, count + 1);
            lastAccessTimes.put(key, System.currentTimeMillis());
            
            Log.d(TAG, "Retrieved memory: " + key);
        }
        
        return value;
    }
    
    /**
     * Check if memory exists
     * @param key Memory key
     * @return True if memory exists
     */
    public synchronized boolean hasMemory(String key) {
        return memories.containsKey(key);
    }
    
    /**
     * Remove memory
     * @param key Memory key
     */
    public synchronized void removeMemory(String key) {
        memories.remove(key);
        accessCounts.remove(key);
        lastAccessTimes.remove(key);
        
        Log.d(TAG, "Removed memory: " + key);
    }
    
    /**
     * Get all memories
     * @return Map of all memories
     */
    public synchronized Map<String, String> getAllMemories() {
        return new HashMap<>(memories);
    }
    
    /**
     * Get access counts
     * @return Map of access counts
     */
    public synchronized Map<String, Integer> getAccessCounts() {
        return new HashMap<>(accessCounts);
    }
    
    /**
     * Get last access times
     * @return Map of last access times
     */
    public synchronized Map<String, Long> getLastAccessTimes() {
        return new HashMap<>(lastAccessTimes);
    }
    
    /**
     * Clear all memories
     */
    public synchronized void clear() {
        memories.clear();
        accessCounts.clear();
        lastAccessTimes.clear();
        
        Log.d(TAG, "Cleared all memories");
    }
    
    /**
     * Get number of memories
     * @return Memory count
     */
    public synchronized int size() {
        return memories.size();
    }
    
    /**
     * Ensure memory capacity
     * Removes least recently accessed memories if over capacity
     */
    private synchronized void ensureCapacity() {
        if (memories.size() >= MAX_MEMORIES) {
            // Find least recently accessed memory
            String oldestKey = null;
            long oldestTime = Long.MAX_VALUE;
            
            for (Map.Entry<String, Long> entry : lastAccessTimes.entrySet()) {
                if (entry.getValue() < oldestTime) {
                    oldestTime = entry.getValue();
                    oldestKey = entry.getKey();
                }
            }
            
            // Remove oldest memory
            if (oldestKey != null) {
                removeMemory(oldestKey);
                Log.d(TAG, "Removed oldest memory to ensure capacity: " + oldestKey);
            }
        }
    }
}
