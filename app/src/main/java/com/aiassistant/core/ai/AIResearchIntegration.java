package com.aiassistant.core.ai;

import android.content.Context;
import android.util.Log;

import com.aiassistant.research.ResearchManager;
import com.aiassistant.research.InformationVerifier;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Collections;
import java.util.Comparator;

/**
 * AIResearchIntegration integrates the AI's research capabilities with
 * its knowledge management system, allowing the AI to augment its knowledge
 * with information gathered from online sources.
 */
public class AIResearchIntegration {
    private static final String TAG = "AIResearchIntegration";
    
    // Core components
    private Context context;
    private AIStateManager aiStateManager;
    private ResearchManager researchManager;
    private InformationVerifier informationVerifier;
    
    // Settings
    private boolean autoUpdateKnowledge = true;
    private int maxCachedResearch = 100;
    private long researchCacheExpirationMs = 24 * 60 * 60 * 1000; // 24 hours
    
    // Research cache and tracking
    private Map<String, ResearchEntry> researchCache = new HashMap<>();
    
    /**
     * Research entry for caching and knowledge integration
     */
    public static class ResearchEntry {
        public String id;
        public String query;
        public String summary;
        public List<String> sources = new ArrayList<>();
        public Map<String, Double> factConfidence = new HashMap<>();
        public Set<String> integratedKnowledgeIds = new HashSet<>();
        public long timestamp;
        public boolean verified;
        
        public ResearchEntry(String query) {
            this.id = UUID.randomUUID().toString();
            this.query = query;
            this.timestamp = System.currentTimeMillis();
            this.verified = false;
        }
    }
    
    /**
     * Constructor
     */
    public AIResearchIntegration(Context context, AIStateManager aiStateManager) {
        this.context = context.getApplicationContext();
        this.aiStateManager = aiStateManager;
        this.researchManager = ResearchManager.getInstance(context);
        this.informationVerifier = new InformationVerifier(context);
    }
    
    /**
     * Research a topic and integrate findings into the AI's knowledge
     */
    public void researchAndIntegrate(String query, final ResearchCallback callback) {
        // Check for internet connectivity
        if (!researchManager.isInternetAvailable()) {
            if (callback != null) {
                callback.onResearchCompleted(query, null, "No internet connection available");
            }
            return;
        }
        
        // Check if we already have recent research on this topic
        ResearchEntry cachedEntry = getCachedResearch(query);
        if (cachedEntry != null) {
            Log.d(TAG, "Using cached research for: " + query);
            if (callback != null) {
                callback.onResearchCompleted(query, cachedEntry, null);
            }
            return;
        }
        
        // Perform new research
        Log.d(TAG, "Performing new research for: " + query);
        researchManager.researchTopic(query, new ResearchManager.ResearchCallback() {
            @Override
            public void onResearchCompleted(ResearchManager.ResearchResult result) {
                // Convert to our internal format
                ResearchEntry entry = convertResearchResult(result);
                
                // Cache the result
                cacheResearch(entry);
                
                // Integrate with knowledge base if enabled
                if (autoUpdateKnowledge) {
                    integrateResearchIntoKnowledge(entry);
                }
                
                // Store as memory
                recordResearchMemory(entry);
                
                if (callback != null) {
                    callback.onResearchCompleted(query, entry, null);
                }
            }
            
            @Override
            public void onResearchError(String query, String errorMessage) {
                Log.e(TAG, "Research error: " + errorMessage);
                if (callback != null) {
                    callback.onResearchCompleted(query, null, errorMessage);
                }
            }
        });
    }
    
    /**
     * Callback for research operations
     */
    public interface ResearchCallback {
        void onResearchCompleted(String query, ResearchEntry result, String errorMessage);
    }
    
    /**
     * Convert ResearchManager.ResearchResult to our internal ResearchEntry
     */
    private ResearchEntry convertResearchResult(ResearchManager.ResearchResult result) {
        ResearchEntry entry = new ResearchEntry(result.query);
        entry.summary = result.summary;
        
        // Copy sources
        for (ResearchManager.SourceInfo source : result.sources) {
            entry.sources.add(source.url);
        }
        
        // Copy fact confidence
        entry.factConfidence.putAll(result.factConfidence);
        
        // Copy verification status
        entry.verified = result.isVerified;
        
        return entry;
    }
    
    /**
     * Cache research result
     */
    private void cacheResearch(ResearchEntry entry) {
        // Store in cache
        researchCache.put(entry.id, entry);
        
        // Enforce cache size limits
        if (researchCache.size() > maxCachedResearch) {
            pruneResearchCache();
        }
    }
    
    /**
     * Get cached research if available and not expired
     */
    private ResearchEntry getCachedResearch(String query) {
        // Check for exact matches first
        for (ResearchEntry entry : researchCache.values()) {
            if (normalizeQuery(entry.query).equals(normalizeQuery(query))) {
                // Check if expired
                if (System.currentTimeMillis() - entry.timestamp > researchCacheExpirationMs) {
                    return null; // Expired
                }
                return entry;
            }
        }
        
        // Check for similar queries
        ResearchEntry bestMatch = null;
        double bestSimilarity = 0.6; // Minimum threshold for similarity
        
        for (ResearchEntry entry : researchCache.values()) {
            // Skip if expired
            if (System.currentTimeMillis() - entry.timestamp > researchCacheExpirationMs) {
                continue;
            }
            
            double similarity = calculateQuerySimilarity(query, entry.query);
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestMatch = entry;
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Calculate similarity between two queries
     */
    private double calculateQuerySimilarity(String query1, String query2) {
        // Normalize queries
        String norm1 = normalizeQuery(query1);
        String norm2 = normalizeQuery(query2);
        
        // Extract words (skip short words)
        Set<String> words1 = new HashSet<>();
        Set<String> words2 = new HashSet<>();
        
        for (String word : norm1.split("\\s+")) {
            if (word.length() > 3) words1.add(word);
        }
        
        for (String word : norm2.split("\\s+")) {
            if (word.length() > 3) words2.add(word);
        }
        
        if (words1.isEmpty() || words2.isEmpty()) {
            return 0.0;
        }
        
        // Count common words
        int commonWords = 0;
        for (String word : words1) {
            if (words2.contains(word)) {
                commonWords++;
            }
        }
        
        // Calculate Jaccard similarity
        double unionSize = words1.size() + words2.size() - commonWords;
        return (double) commonWords / unionSize;
    }
    
    /**
     * Normalize query for comparison
     */
    private String normalizeQuery(String query) {
        if (query == null) return "";
        
        // Convert to lowercase
        String normalized = query.toLowerCase();
        
        // Remove punctuation
        normalized = normalized.replaceAll("[^a-z0-9\\s]", " ");
        
        // Normalize whitespace
        normalized = normalized.replaceAll("\\s+", " ").trim();
        
        return normalized;
    }
    
    /**
     * Prune research cache to stay within limits
     */
    private void pruneResearchCache() {
        // First remove expired entries
        List<String> expiredIds = new ArrayList<>();
        
        long expirationTime = System.currentTimeMillis() - researchCacheExpirationMs;
        for (Map.Entry<String, ResearchEntry> entry : researchCache.entrySet()) {
            if (entry.getValue().timestamp < expirationTime) {
                expiredIds.add(entry.getKey());
            }
        }
        
        for (String id : expiredIds) {
            researchCache.remove(id);
        }
        
        // If still over capacity, remove oldest entries
        if (researchCache.size() > maxCachedResearch) {
            List<ResearchEntry> sortedEntries = new ArrayList<>(researchCache.values());
            
            // Sort by timestamp (oldest first)
            Collections.sort(sortedEntries, new Comparator<ResearchEntry>() {
                @Override
                public int compare(ResearchEntry e1, ResearchEntry e2) {
                    return Long.compare(e1.timestamp, e2.timestamp);
                }
            });
            
            // Remove oldest entries until within capacity
            int entriesToRemove = sortedEntries.size() - maxCachedResearch;
            for (int i = 0; i < entriesToRemove; i++) {
                researchCache.remove(sortedEntries.get(i).id);
            }
        }
    }
    
    /**
     * Integrate research findings into AI's knowledge base
     */
    private void integrateResearchIntoKnowledge(ResearchEntry research) {
        // Add high-confidence facts to knowledge base
        for (Map.Entry<String, Double> fact : research.factConfidence.entrySet()) {
            // Only add facts with high confidence
            if (fact.getValue() >= 0.7) {
                // Create knowledge entry
                String knowledgeId = aiStateManager.storeKnowledge(research.query, fact.getKey()).id;
                
                // Track this knowledge entry
                research.integratedKnowledgeIds.add(knowledgeId);
                
                // Add appropriate categories
                categorizeKnowledge(knowledgeId, research.query);
            }
        }
    }
    
    /**
     * Add appropriate categories to knowledge entries
     */
    private void categorizeKnowledge(String knowledgeId, String query) {
        // Check for common categories
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("cricket") || 
                lowerQuery.contains("football") || 
                lowerQuery.contains("tennis") || 
                lowerQuery.contains("basketball") || 
                lowerQuery.contains("team") || 
                lowerQuery.contains("player") || 
                lowerQuery.contains("match") || 
                lowerQuery.contains("game")) {
            aiStateManager.addKnowledgeCategory(knowledgeId, "Sports");
            
            // Add specific sport if identified
            if (lowerQuery.contains("cricket")) {
                aiStateManager.addKnowledgeCategory(knowledgeId, "Cricket");
            } else if (lowerQuery.contains("football")) {
                aiStateManager.addKnowledgeCategory(knowledgeId, "Football");
            } else if (lowerQuery.contains("tennis")) {
                aiStateManager.addKnowledgeCategory(knowledgeId, "Tennis");
            } else if (lowerQuery.contains("basketball")) {
                aiStateManager.addKnowledgeCategory(knowledgeId, "Basketball");
            }
        }
        
        if (lowerQuery.contains("physics") || 
                lowerQuery.contains("chemistry") || 
                lowerQuery.contains("biology") || 
                lowerQuery.contains("science") || 
                lowerQuery.contains("theory") || 
                lowerQuery.contains("equation")) {
            aiStateManager.addKnowledgeCategory(knowledgeId, "Science");
            
            // Add specific science if identified
            if (lowerQuery.contains("physics")) {
                aiStateManager.addKnowledgeCategory(knowledgeId, "Physics");
            } else if (lowerQuery.contains("chemistry")) {
                aiStateManager.addKnowledgeCategory(knowledgeId, "Chemistry");
            } else if (lowerQuery.contains("biology")) {
                aiStateManager.addKnowledgeCategory(knowledgeId, "Biology");
            }
        }
        
        if (lowerQuery.contains("jee") || 
                lowerQuery.contains("exam") || 
                lowerQuery.contains("test") || 
                lowerQuery.contains("question") || 
                lowerQuery.contains("solve") || 
                lowerQuery.contains("problem")) {
            aiStateManager.addKnowledgeCategory(knowledgeId, "Education");
            
            // Add specific exam if identified
            if (lowerQuery.contains("jee")) {
                aiStateManager.addKnowledgeCategory(knowledgeId, "JEE");
            }
        }
        
        // Default category if none matched
        aiStateManager.addKnowledgeCategory(knowledgeId, "Research");
    }
    
    /**
     * Record research activity as memory
     */
    private void recordResearchMemory(ResearchEntry research) {
        // Store memory
        String memoryContent = "Researched information about: " + research.query;
        
        // Create memory entry
        AIStateManager.MemoryEntry memoryEntry = aiStateManager.storeMemory(memoryContent);
        
        // Add tags
        aiStateManager.tagMemory(memoryEntry.id, "research");
        
        // Add more specific tags based on the query
        String lowerQuery = research.query.toLowerCase();
        if (lowerQuery.contains("cricket") || 
                lowerQuery.contains("sport") || 
                lowerQuery.contains("game")) {
            aiStateManager.tagMemory(memoryEntry.id, "sports");
        }
        
        if (lowerQuery.contains("science") || 
                lowerQuery.contains("physics") || 
                lowerQuery.contains("chemistry")) {
            aiStateManager.tagMemory(memoryEntry.id, "science");
        }
        
        if (lowerQuery.contains("jee") || 
                lowerQuery.contains("education") || 
                lowerQuery.contains("exam")) {
            aiStateManager.tagMemory(memoryEntry.id, "education");
        }
        
        // Set importance based on how much knowledge was integrated
        if (!research.integratedKnowledgeIds.isEmpty()) {
            double importance = Math.min(1.0, 0.5 + (research.integratedKnowledgeIds.size() * 0.05));
            aiStateManager.setMemoryImportance(memoryEntry.id, importance);
        }
    }
    
    /**
     * Get all cached research entries
     */
    public List<ResearchEntry> getAllCachedResearch() {
        return new ArrayList<>(researchCache.values());
    }
    
    /**
     * Get research entry by ID
     */
    public ResearchEntry getResearchById(String id) {
        return researchCache.get(id);
    }
    
    /**
     * Set whether to automatically update knowledge base
     */
    public void setAutoUpdateKnowledge(boolean enabled) {
        this.autoUpdateKnowledge = enabled;
    }
    
    /**
     * Check if auto-update knowledge is enabled
     */
    public boolean isAutoUpdateKnowledgeEnabled() {
        return autoUpdateKnowledge;
    }
}
