package com.aiassistant.core.memory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a piece of knowledge in the AI's knowledge base
 */
public class KnowledgeEntry {
    public String id;
    public String concept;
    public String content;
    public double confidence;
    public boolean verified;
    public String source;
    public long createdTime;
    public long lastAccessTime;
    public List<String> relatedConcepts;
    
    public KnowledgeEntry(String concept, String content) {
        this.id = UUID.randomUUID().toString();
        this.concept = concept;
        this.content = content;
        this.confidence = 0.5;
        this.verified = false;
        this.source = "user";
        this.createdTime = System.currentTimeMillis();
        this.lastAccessTime = this.createdTime;
        this.relatedConcepts = new ArrayList<>();
    }
    
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("concept", concept);
        json.put("content", content);
        json.put("confidence", confidence);
        json.put("verified", verified);
        json.put("source", source);
        json.put("createdTime", createdTime);
        json.put("lastAccessTime", lastAccessTime);
        
        JSONArray relatedArray = new JSONArray();
        for (String related : relatedConcepts) {
            relatedArray.put(related);
        }
        json.put("relatedConcepts", relatedArray);
        
        return json;
    }
    
    public static KnowledgeEntry fromJSON(JSONObject json) throws JSONException {
        String concept = json.getString("concept");
        String content = json.getString("content");
        KnowledgeEntry entry = new KnowledgeEntry(concept, content);
        
        entry.id = json.getString("id");
        entry.confidence = json.optDouble("confidence", entry.confidence);
        entry.verified = json.optBoolean("verified", entry.verified);
        entry.source = json.optString("source", entry.source);
        entry.createdTime = json.optLong("createdTime", entry.createdTime);
        entry.lastAccessTime = json.optLong("lastAccessTime", entry.lastAccessTime);
        
        if (json.has("relatedConcepts")) {
            JSONArray relatedArray = json.getJSONArray("relatedConcepts");
            for (int i = 0; i < relatedArray.length(); i++) {
                entry.relatedConcepts.add(relatedArray.getString(i));
            }
        }
        
        return entry;
    }
}
