package com.aiassistant.core.memory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a memory in the AI's memory system
 */
public class Memory {
    public String id;
    public String content;
    public long timestamp;
    public List<String> topics;
    public double importance;
    public Map<String, Double> emotionalContext;
    public int accessCount;
    public long lastAccessTime;
    
    public Memory(String content) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.topics = new ArrayList<>();
        this.importance = 0.5;
        this.emotionalContext = new HashMap<>();
        this.accessCount = 0;
        this.lastAccessTime = this.timestamp;
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("content", content);
        json.put("timestamp", timestamp);
        
        JSONArray topicsArray = new JSONArray();
        for (String topic : topics) {
            topicsArray.put(topic);
        }
        json.put("topics", topicsArray);
        
        json.put("importance", importance);
        
        JSONObject emotionsJson = new JSONObject();
        for (Map.Entry<String, Double> entry : emotionalContext.entrySet()) {
            emotionsJson.put(entry.getKey(), entry.getValue());
        }
        json.put("emotionalContext", emotionsJson);
        
        json.put("accessCount", accessCount);
        json.put("lastAccessTime", lastAccessTime);
        
        return json;
    }
    
    public static Memory fromJSON(JSONObject json) throws JSONException {
        String content = json.getString("content");
        Memory memory = new Memory(content);
        
        memory.id = json.getString("id");
        memory.timestamp = json.getLong("timestamp");
        
        JSONArray topicsArray = json.getJSONArray("topics");
        for (int i = 0; i < topicsArray.length(); i++) {
            memory.topics.add(topicsArray.getString(i));
        }
        
        memory.importance = json.optDouble("importance", memory.importance);
        
        if (json.has("emotionalContext")) {
            JSONObject emotionsJson = json.getJSONObject("emotionalContext");
            Iterator<String> keys = emotionsJson.keys();
            
            while (keys.hasNext()) {
                String key = keys.next();
                memory.emotionalContext.put(key, emotionsJson.getDouble(key));
            }
        }
        
        memory.accessCount = json.optInt("accessCount", memory.accessCount);
        memory.lastAccessTime = json.optLong("lastAccessTime", memory.lastAccessTime);
        
        return memory;
    }
}
