package com.aiassistant.core.ai.memory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Long-term memory component for persistent storage of information
 * Provides durable storage with serialization to disk
 */
public class LongTermMemory {
    private static final String TAG = "LongTermMemory";
    
    // Memory storage
    private final Map<String, String> memories;
    
    // Context for file access
    private final Context context;
    
    // Configuration
    private static final String MEMORY_FILE = "long_term_memory.json";
    private static final String PREFS_NAME = "memory_preferences";
    private static final String MEMORY_INDEX_KEY = "memory_index";
    
    /**
     * Constructor
     * @param context Application context
     */
    public LongTermMemory(Context context) {
        this.context = context.getApplicationContext();
        this.memories = new HashMap<>();
        
        // Load from storage
        loadFromStorage();
        
        Log.d(TAG, "LongTermMemory initialized with " + memories.size() + " memories");
    }
    
    /**
     * Store memory
     * @param key Memory key
     * @param value Memory value
     */
    public void storeMemory(String key, String value) {
        memories.put(key, value);
        updateMemoryIndex(key);
        Log.d(TAG, "Stored memory: " + key);
    }
    
    /**
     * Get memory
     * @param key Memory key
     * @return Memory value or null if not found
     */
    public String getMemory(String key) {
        String value = memories.get(key);
        if (value != null) {
            Log.d(TAG, "Retrieved memory: " + key);
        }
        return value;
    }
    
    /**
     * Check if memory exists
     * @param key Memory key
     * @return True if memory exists
     */
    public boolean hasMemory(String key) {
        return memories.containsKey(key);
    }
    
    /**
     * Remove memory
     * @param key Memory key
     */
    public void removeMemory(String key) {
        memories.remove(key);
        removeFromMemoryIndex(key);
        Log.d(TAG, "Removed memory: " + key);
    }
    
    /**
     * Get all memories
     * @return Map of all memories
     */
    public Map<String, String> getAllMemories() {
        return new HashMap<>(memories);
    }
    
    /**
     * Clear all memories
     */
    public void clear() {
        memories.clear();
        clearMemoryIndex();
        Log.d(TAG, "Cleared all memories");
    }
    
    /**
     * Get number of memories
     * @return Memory count
     */
    public int size() {
        return memories.size();
    }
    
    /**
     * Load memories from storage
     */
    public void loadFromStorage() {
        // Load memory file
        File file = new File(context.getFilesDir(), MEMORY_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder jsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                
                JSONObject json = new JSONObject(jsonString.toString());
                Iterator<String> keys = json.keys();
                
                while (keys.hasNext()) {
                    String key = keys.next();
                    memories.put(key, json.getString(key));
                }
                
                Log.d(TAG, "Loaded " + memories.size() + " memories from storage");
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error loading memories from storage: " + e.getMessage());
            }
        }
        
        // Load memory index
        loadMemoryIndex();
    }
    
    /**
     * Save memories to storage
     */
    public void saveToStorage() {
        // Save memory file
        File file = new File(context.getFilesDir(), MEMORY_FILE);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            JSONObject json = new JSONObject();
            
            for (Map.Entry<String, String> entry : memories.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
            
            writer.write(json.toString());
            Log.d(TAG, "Saved " + memories.size() + " memories to storage");
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error saving memories to storage: " + e.getMessage());
        }
        
        // Save memory index
        saveMemoryIndex();
    }
    
    /**
     * Load memory index from preferences
     */
    private void loadMemoryIndex() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String indexJson = prefs.getString(MEMORY_INDEX_KEY, "{}");
        
        try {
            JSONObject json = new JSONObject(indexJson);
            Iterator<String> keys = json.keys();
            
            while (keys.hasNext()) {
                String key = keys.next();
                
                // Only add to index if memory exists
                if (memories.containsKey(key)) {
                    updateMemoryIndex(key);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error loading memory index: " + e.getMessage());
        }
    }
    
    /**
     * Save memory index to preferences
     */
    private void saveMemoryIndex() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        try {
            JSONObject json = new JSONObject();
            
            for (String key : memories.keySet()) {
                json.put(key, true);
            }
            
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(MEMORY_INDEX_KEY, json.toString());
            editor.apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error saving memory index: " + e.getMessage());
        }
    }
    
    /**
     * Update memory index
     * @param key Memory key
     */
    private void updateMemoryIndex(String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String indexJson = prefs.getString(MEMORY_INDEX_KEY, "{}");
        
        try {
            JSONObject json = new JSONObject(indexJson);
            json.put(key, true);
            
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(MEMORY_INDEX_KEY, json.toString());
            editor.apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error updating memory index: " + e.getMessage());
        }
    }
    
    /**
     * Remove key from memory index
     * @param key Memory key
     */
    private void removeFromMemoryIndex(String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String indexJson = prefs.getString(MEMORY_INDEX_KEY, "{}");
        
        try {
            JSONObject json = new JSONObject(indexJson);
            json.remove(key);
            
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(MEMORY_INDEX_KEY, json.toString());
            editor.apply();
        } catch (JSONException e) {
            Log.e(TAG, "Error removing from memory index: " + e.getMessage());
        }
    }
    
    /**
     * Clear memory index
     */
    private void clearMemoryIndex() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MEMORY_INDEX_KEY, "{}");
        editor.apply();
    }
}
