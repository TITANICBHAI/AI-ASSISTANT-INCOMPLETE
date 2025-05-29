package com.aiassistant.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aiassistant.core.memory.EmotionalMemory;
import com.aiassistant.core.memory.LongTermMemory;
import com.aiassistant.core.memory.MemoryManager;
import com.aiassistant.core.memory.ShortTermMemory;

import java.util.Map;

/**
 * Service for accessing AI assistant memory subsystem
 */
public class MemoryService extends Service {
    private static final String TAG = "MemoryService";
    
    // Memory manager
    private MemoryManager memoryManager;
    
    // Binder for client communication
    private final IBinder binder = new MemoryBinder();
    
    /**
     * Binder class for client communication
     */
    public class MemoryBinder extends Binder {
        /**
         * Get service instance
         * @return MemoryService instance
         */
        public MemoryService getService() {
            return MemoryService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize memory manager
        memoryManager = MemoryManager.getInstance(this);
        
        Log.d(TAG, "Memory service created");
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Memory service bound");
        return binder;
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Memory service unbound");
        return super.onUnbind(intent);
    }
    
    /**
     * Get memory manager
     * @return Memory manager
     */
    public MemoryManager getMemoryManager() {
        return memoryManager;
    }
    
    /**
     * Get short-term memory
     * @return Short-term memory
     */
    public ShortTermMemory getShortTermMemory() {
        return memoryManager.getShortTermMemory();
    }
    
    /**
     * Get long-term memory
     * @return Long-term memory
     */
    public LongTermMemory getLongTermMemory() {
        return memoryManager.getLongTermMemory();
    }
    
    /**
     * Get emotional memory
     * @return Emotional memory
     */
    public EmotionalMemory getEmotionalMemory() {
        return memoryManager.getEmotionalMemory();
    }
    
    /**
     * Remember information in short-term memory
     * @param key Memory key
     * @param value Memory value
     */
    public void rememberShortTerm(String key, String value) {
        memoryManager.rememberShortTerm(key, value);
    }
    
    /**
     * Remember information in long-term memory
     * @param key Memory key
     * @param value Memory value
     * @param importance Importance (0.0-1.0)
     */
    public void rememberLongTerm(String key, String value, float importance) {
        memoryManager.rememberLongTerm(key, value, importance);
    }
    
    /**
     * Recall information from memory
     * @param key Memory key
     * @return Memory value or null if not found
     */
    public String recall(String key) {
        return memoryManager.recall(key);
    }
    
    /**
     * Check if memory contains key
     * @param key Memory key
     * @return True if memory contains key
     */
    public boolean contains(String key) {
        return memoryManager.contains(key);
    }
    
    /**
     * Forget information from all memory
     * @param key Memory key
     */
    public void forget(String key) {
        memoryManager.forget(key);
    }
    
    /**
     * Record an emotional state
     * @param emotion Emotion name
     * @param intensity Intensity (0.0-1.0)
     * @param context Optional context information
     */
    public void recordEmotion(String emotion, float intensity, String context) {
        memoryManager.recordEmotion(emotion, intensity, context);
    }
    
    /**
     * Get dominant emotion from history
     * @return Dominant emotion name
     */
    public String getDominantEmotion() {
        return memoryManager.getDominantEmotion();
    }
    
    /**
     * Set user preference
     * @param preference Preference name
     * @param value Preference value
     */
    public void setUserPreference(String preference, String value) {
        memoryManager.setUserPreference(preference, value);
    }
    
    /**
     * Get user preference
     * @param preference Preference name
     * @return Preference value or null if not found
     */
    public String getUserPreference(String preference) {
        return memoryManager.getUserPreference(preference);
    }
    
    /**
     * Set conversation context
     * @param context Context name
     * @param value Context value
     */
    public void setConversationContext(String context, String value) {
        memoryManager.setConversationContext(context, value);
    }
    
    /**
     * Get conversation context
     * @param context Context name
     * @return Context value or null if not found
     */
    public String getConversationContext(String context) {
        return memoryManager.getConversationContext(context);
    }
    
    /**
     * Get all conversation contexts
     * @return Map of context names to values
     */
    public Map<String, String> getAllConversationContexts() {
        return memoryManager.getAllConversationContexts();
    }
    
    /**
     * Clear conversation context
     */
    public void clearConversationContext() {
        memoryManager.clearConversationContext();
    }
    
    /**
     * Search memory by key pattern
     * @param keyPattern Key pattern to search for
     * @return Map of matching key-value pairs
     */
    public Map<String, String> searchByKey(String keyPattern) {
        return memoryManager.searchByKey(keyPattern);
    }
    
    /**
     * Search memory by value pattern
     * @param valuePattern Value pattern to search for
     * @return Map of matching key-value pairs
     */
    public Map<String, String> searchByValue(String valuePattern) {
        return memoryManager.searchByValue(valuePattern);
    }
    
    /**
     * Get memory summary
     * @return Memory usage summary
     */
    public String getMemorySummary() {
        return memoryManager.getMemorySummary();
    }
    
    /**
     * Clear all memory
     */
    public void clearAllMemory() {
        memoryManager.clearAllMemory();
    }
}
