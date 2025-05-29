package com.aiassistant.core.ai.memory;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Emotional memory component for storing memories with emotional context
 * Includes valence (positive/negative) and arousal (intensity) dimensions
 */
public class EmotionalMemory {
    private static final String TAG = "EmotionalMemory";
    
    // Memory storage with emotional metadata
    private final Map<String, EmotionalMemoryEntry> memories;
    
    /**
     * Constructor
     */
    public EmotionalMemory() {
        this.memories = new HashMap<>();
        
        Log.d(TAG, "EmotionalMemory initialized");
    }
    
    /**
     * Store memory with emotional context
     * @param key Memory key
     * @param value Memory value
     * @param valence Emotional valence (-1.0 to 1.0)
     * @param arousal Emotional arousal (0.0 to 1.0)
     */
    public synchronized void storeMemory(String key, String value, float valence, float arousal) {
        // Clamp values to valid ranges
        valence = Math.max(-1.0f, Math.min(1.0f, valence));
        arousal = Math.max(0.0f, Math.min(1.0f, arousal));
        
        // Create entry
        EmotionalMemoryEntry entry = new EmotionalMemoryEntry(value, valence, arousal);
        
        // Store memory
        memories.put(key, entry);
        
        Log.d(TAG, "Stored emotional memory: " + key + " (V:" + valence + ", A:" + arousal + ")");
    }
    
    /**
     * Get memory entry
     * @param key Memory key
     * @return Memory entry or null if not found
     */
    public synchronized EmotionalMemoryEntry getMemoryEntry(String key) {
        return memories.get(key);
    }
    
    /**
     * Get memory value
     * @param key Memory key
     * @return Memory value or null if not found
     */
    public synchronized String getMemory(String key) {
        EmotionalMemoryEntry entry = memories.get(key);
        return entry != null ? entry.getValue() : null;
    }
    
    /**
     * Get valence for memory
     * @param key Memory key
     * @return Valence or 0.0 if not found
     */
    public synchronized float getValence(String key) {
        EmotionalMemoryEntry entry = memories.get(key);
        return entry != null ? entry.getValence() : 0.0f;
    }
    
    /**
     * Get arousal for memory
     * @param key Memory key
     * @return Arousal or 0.0 if not found
     */
    public synchronized float getArousal(String key) {
        EmotionalMemoryEntry entry = memories.get(key);
        return entry != null ? entry.getArousal() : 0.0f;
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
        
        Log.d(TAG, "Removed emotional memory: " + key);
    }
    
    /**
     * Get all memories
     * @return Map of all memories
     */
    public synchronized Map<String, EmotionalMemoryEntry> getAllMemories() {
        return new HashMap<>(memories);
    }
    
    /**
     * Get positive memories
     * @param threshold Valence threshold (0.0 to 1.0)
     * @return Map of positive memories
     */
    public synchronized Map<String, EmotionalMemoryEntry> getPositiveMemories(float threshold) {
        Map<String, EmotionalMemoryEntry> result = new HashMap<>();
        
        for (Map.Entry<String, EmotionalMemoryEntry> entry : memories.entrySet()) {
            if (entry.getValue().getValence() >= threshold) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Get negative memories
     * @param threshold Negative valence threshold (-1.0 to 0.0)
     * @return Map of negative memories
     */
    public synchronized Map<String, EmotionalMemoryEntry> getNegativeMemories(float threshold) {
        Map<String, EmotionalMemoryEntry> result = new HashMap<>();
        
        for (Map.Entry<String, EmotionalMemoryEntry> entry : memories.entrySet()) {
            if (entry.getValue().getValence() <= threshold) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Get high arousal memories
     * @param threshold Arousal threshold (0.0 to 1.0)
     * @return Map of high arousal memories
     */
    public synchronized Map<String, EmotionalMemoryEntry> getHighArousalMemories(float threshold) {
        Map<String, EmotionalMemoryEntry> result = new HashMap<>();
        
        for (Map.Entry<String, EmotionalMemoryEntry> entry : memories.entrySet()) {
            if (entry.getValue().getArousal() >= threshold) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Clear all memories
     */
    public synchronized void clear() {
        memories.clear();
        
        Log.d(TAG, "Cleared all emotional memories");
    }
    
    /**
     * Get number of memories
     * @return Memory count
     */
    public synchronized int size() {
        return memories.size();
    }
    
    /**
     * Entry class for emotional memory
     */
    public static class EmotionalMemoryEntry {
        private final String value;
        private final float valence;
        private final float arousal;
        private final long timestamp;
        
        /**
         * Constructor
         * @param value Memory value
         * @param valence Emotional valence
         * @param arousal Emotional arousal
         */
        public EmotionalMemoryEntry(String value, float valence, float arousal) {
            this.value = value;
            this.valence = valence;
            this.arousal = arousal;
            this.timestamp = System.currentTimeMillis();
        }
        
        /**
         * Get memory value
         * @return Memory value
         */
        public String getValue() {
            return value;
        }
        
        /**
         * Get valence
         * @return Valence value
         */
        public float getValence() {
            return valence;
        }
        
        /**
         * Get arousal
         * @return Arousal value
         */
        public float getArousal() {
            return arousal;
        }
        
        /**
         * Get timestamp
         * @return Timestamp in milliseconds
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public String toString() {
            return "EmotionalMemoryEntry{" +
                    "value='" + value + '\'' +
                    ", valence=" + valence +
                    ", arousal=" + arousal +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
