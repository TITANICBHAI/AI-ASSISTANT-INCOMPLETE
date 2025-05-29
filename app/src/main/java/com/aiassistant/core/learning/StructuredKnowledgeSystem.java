package com.aiassistant.core.learning;

import android.content.Context;
import android.util.Log;

import com.aiassistant.security.AccessControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Structured Knowledge Acquisition System
 * 
 * This system manages the acquisition, organization, and storage of knowledge
 * gathered from various sources including user interactions, document analysis,
 * and potentially external AI systems.
 */
public class StructuredKnowledgeSystem {
    private static final String TAG = "StructuredKnowledge";
    
    // Knowledge domains
    public enum KnowledgeDomain {
        GAMING,              // Gaming-related knowledge
        ACADEMIC,            // Academic subjects
        TECHNICAL,           // Technical and programming knowledge
        GENERAL,             // General knowledge
        SPECIALIZED,         // Specialized domain-specific knowledge
        USER_SPECIFIC        // Knowledge specific to the user
    }
    
    // Knowledge source types
    public enum KnowledgeSource {
        USER_INTERACTION,    // Learned from user interactions
        DOCUMENT_ANALYSIS,   // Extracted from documents (PDFs, etc.)
        WEB_CONTENT,         // Gathered from web browsing
        EXTERNAL_AI,         // Obtained from external AI systems
        SYSTEM_OBSERVATION,  // Observed from system interactions
        INFERENCE            // Inferred from existing knowledge
    }
    
    // Knowledge reliability levels
    public enum ReliabilityLevel {
        UNVERIFIED,          // Not yet verified
        LOW,                 // Low reliability
        MEDIUM,              // Medium reliability
        HIGH,                // High reliability
        VERIFIED             // Verified through multiple sources
    }
    
    // Knowledge entry class
    public static class KnowledgeEntry {
        private final String id;
        private final KnowledgeDomain domain;
        private final KnowledgeSource source;
        private final long timestamp;
        private final Map<String, Object> attributes;
        private String content;
        private ReliabilityLevel reliability;
        private final List<String> relatedEntryIds;
        private int usageCount;
        private long lastAccessTime;
        
        public KnowledgeEntry(String content, KnowledgeDomain domain, KnowledgeSource source) {
            this.id = UUID.randomUUID().toString();
            this.content = content;
            this.domain = domain;
            this.source = source;
            this.timestamp = System.currentTimeMillis();
            this.reliability = ReliabilityLevel.UNVERIFIED;
            this.attributes = new HashMap<>();
            this.relatedEntryIds = new ArrayList<>();
            this.usageCount = 0;
            this.lastAccessTime = this.timestamp;
        }
        
        // Getters
        public String getId() { return id; }
        public String getContent() { return content; }
        public KnowledgeDomain getDomain() { return domain; }
        public KnowledgeSource getSource() { return source; }
        public long getTimestamp() { return timestamp; }
        public ReliabilityLevel getReliability() { return reliability; }
        public int getUsageCount() { return usageCount; }
        public long getLastAccessTime() { return lastAccessTime; }
        
        // Setters
        public void setContent(String content) { this.content = content; }
        public void setReliability(ReliabilityLevel reliability) { this.reliability = reliability; }
        
        // Attribute management
        public void setAttribute(String key, Object value) { attributes.put(key, value); }
        public Object getAttribute(String key) { return attributes.get(key); }
        public boolean hasAttribute(String key) { return attributes.containsKey(key); }
        public Map<String, Object> getAllAttributes() { return new HashMap<>(attributes); }
        
        // Related entries management
        public void addRelatedEntry(String entryId) { 
            if (!relatedEntryIds.contains(entryId)) {
                relatedEntryIds.add(entryId);
            }
        }
        public List<String> getRelatedEntryIds() { return new ArrayList<>(relatedEntryIds); }
        
        // Usage tracking
        public void incrementUsage() {
            usageCount++;
            lastAccessTime = System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return "KnowledgeEntry{" +
                    "id='" + id + '\'' +
                    ", domain=" + domain +
                    ", source=" + source +
                    ", reliability=" + reliability +
                    ", usageCount=" + usageCount +
                    '}';
        }
    }
    
    private final Context context;
    private final AccessControl accessControl;
    private final ConcurrentHashMap<String, KnowledgeEntry> knowledgeBase;
    private final Map<KnowledgeDomain, List<String>> domainIndex;
    private final Map<KnowledgeSource, List<String>> sourceIndex;
    private final Map<ReliabilityLevel, List<String>> reliabilityIndex;
    private final Map<String, List<String>> keywordIndex;
    
    private final List<KnowledgeListener> listeners;
    
    /**
     * Constructor
     * @param context Application context
     * @param accessControl Access control system
     */
    public StructuredKnowledgeSystem(Context context, AccessControl accessControl) {
        this.context = context;
        this.accessControl = accessControl;
        this.knowledgeBase = new ConcurrentHashMap<>();
        this.domainIndex = new HashMap<>();
        this.sourceIndex = new HashMap<>();
        this.reliabilityIndex = new HashMap<>();
        this.keywordIndex = new HashMap<>();
        this.listeners = new ArrayList<>();
        
        // Initialize indices
        for (KnowledgeDomain domain : KnowledgeDomain.values()) {
            domainIndex.put(domain, new ArrayList<>());
        }
        
        for (KnowledgeSource source : KnowledgeSource.values()) {
            sourceIndex.put(source, new ArrayList<>());
        }
        
        for (ReliabilityLevel reliability : ReliabilityLevel.values()) {
            reliabilityIndex.put(reliability, new ArrayList<>());
        }
        
        Log.d(TAG, "Structured Knowledge System initialized");
    }
    
    /**
     * Add a new knowledge entry
     * @param content Content of the knowledge
     * @param domain Knowledge domain
     * @param source Knowledge source
     * @return ID of the new entry or null if failed
     */
    public String addKnowledgeEntry(String content, KnowledgeDomain domain, KnowledgeSource source) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_WRITE)) {
            Log.w(TAG, "Permission denied for adding knowledge entry");
            return null;
        }
        
        // Validate input
        if (content == null || content.trim().isEmpty()) {
            Log.w(TAG, "Cannot add empty knowledge entry");
            return null;
        }
        
        // Create new entry
        KnowledgeEntry entry = new KnowledgeEntry(content, domain, source);
        
        // Add to knowledge base
        knowledgeBase.put(entry.getId(), entry);
        
        // Update indices
        domainIndex.get(domain).add(entry.getId());
        sourceIndex.get(source).add(entry.getId());
        reliabilityIndex.get(entry.getReliability()).add(entry.getId());
        
        // Update keyword index
        indexKeywords(entry);
        
        // Notify listeners
        notifyKnowledgeAdded(entry);
        
        Log.d(TAG, "Added knowledge entry: " + entry.getId() + " in domain " + domain);
        
        return entry.getId();
    }
    
    /**
     * Index keywords from entry
     * @param entry Entry to index
     */
    private void indexKeywords(KnowledgeEntry entry) {
        // Simple keyword extraction - in a real implementation, 
        // would use more sophisticated NLP techniques
        String[] words = entry.getContent().toLowerCase().split("\\s+");
        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z0-9]", "").trim();
            if (word.length() < 3) continue; // Skip short words
            
            if (!keywordIndex.containsKey(word)) {
                keywordIndex.put(word, new ArrayList<>());
            }
            
            if (!keywordIndex.get(word).contains(entry.getId())) {
                keywordIndex.get(word).add(entry.getId());
            }
        }
    }
    
    /**
     * Update reliability of a knowledge entry
     * @param entryId Entry ID
     * @param reliability New reliability level
     * @return True if updated
     */
    public boolean updateReliability(String entryId, ReliabilityLevel reliability) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_WRITE)) {
            Log.w(TAG, "Permission denied for updating knowledge reliability");
            return false;
        }
        
        KnowledgeEntry entry = knowledgeBase.get(entryId);
        if (entry == null) {
            Log.w(TAG, "Cannot update reliability of non-existent entry: " + entryId);
            return false;
        }
        
        // Update indices
        reliabilityIndex.get(entry.getReliability()).remove(entryId);
        reliabilityIndex.get(reliability).add(entryId);
        
        // Update entry
        entry.setReliability(reliability);
        
        // Notify listeners
        notifyKnowledgeUpdated(entry);
        
        Log.d(TAG, "Updated reliability of entry " + entryId + " to " + reliability);
        
        return true;
    }
    
    /**
     * Establish a relationship between two knowledge entries
     * @param sourceEntryId Source entry ID
     * @param targetEntryId Target entry ID
     * @return True if relationship established
     */
    public boolean establishRelationship(String sourceEntryId, String targetEntryId) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_WRITE)) {
            Log.w(TAG, "Permission denied for establishing knowledge relationship");
            return false;
        }
        
        KnowledgeEntry sourceEntry = knowledgeBase.get(sourceEntryId);
        KnowledgeEntry targetEntry = knowledgeBase.get(targetEntryId);
        
        if (sourceEntry == null || targetEntry == null) {
            Log.w(TAG, "Cannot establish relationship between non-existent entries");
            return false;
        }
        
        // Add relationship in both directions
        sourceEntry.addRelatedEntry(targetEntryId);
        targetEntry.addRelatedEntry(sourceEntryId);
        
        Log.d(TAG, "Established relationship between " + sourceEntryId + " and " + targetEntryId);
        
        return true;
    }
    
    /**
     * Search for knowledge by keyword
     * @param keyword Keyword to search for
     * @return List of matching entry IDs
     */
    public List<String> searchByKeyword(String keyword) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for knowledge search");
            return new ArrayList<>();
        }
        
        keyword = keyword.toLowerCase().trim();
        
        // Return matches if keyword exists in index
        if (keywordIndex.containsKey(keyword)) {
            List<String> results = new ArrayList<>(keywordIndex.get(keyword));
            // Update usage statistics for returned entries
            for (String entryId : results) {
                KnowledgeEntry entry = knowledgeBase.get(entryId);
                if (entry != null) {
                    entry.incrementUsage();
                }
            }
            return results;
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Get entries by domain
     * @param domain Knowledge domain
     * @return List of entry IDs in that domain
     */
    public List<String> getEntriesByDomain(KnowledgeDomain domain) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for retrieving knowledge by domain");
            return new ArrayList<>();
        }
        
        return new ArrayList<>(domainIndex.get(domain));
    }
    
    /**
     * Get entries by source
     * @param source Knowledge source
     * @return List of entry IDs from that source
     */
    public List<String> getEntriesBySource(KnowledgeSource source) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for retrieving knowledge by source");
            return new ArrayList<>();
        }
        
        return new ArrayList<>(sourceIndex.get(source));
    }
    
    /**
     * Get entries by reliability level
     * @param reliability Reliability level
     * @return List of entry IDs with that reliability
     */
    public List<String> getEntriesByReliability(ReliabilityLevel reliability) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for retrieving knowledge by reliability");
            return new ArrayList<>();
        }
        
        return new ArrayList<>(reliabilityIndex.get(reliability));
    }
    
    /**
     * Get a specific knowledge entry
     * @param entryId Entry ID
     * @return Knowledge entry or null if not found
     */
    public KnowledgeEntry getEntry(String entryId) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for retrieving knowledge entry");
            return null;
        }
        
        KnowledgeEntry entry = knowledgeBase.get(entryId);
        
        if (entry != null) {
            entry.incrementUsage();
        }
        
        return entry;
    }
    
    /**
     * Get related entries for a knowledge entry
     * @param entryId Entry ID
     * @return List of related knowledge entries
     */
    public List<KnowledgeEntry> getRelatedEntries(String entryId) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for retrieving related knowledge entries");
            return new ArrayList<>();
        }
        
        KnowledgeEntry entry = knowledgeBase.get(entryId);
        if (entry == null) {
            return new ArrayList<>();
        }
        
        List<KnowledgeEntry> relatedEntries = new ArrayList<>();
        for (String relatedId : entry.getRelatedEntryIds()) {
            KnowledgeEntry relatedEntry = knowledgeBase.get(relatedId);
            if (relatedEntry != null) {
                relatedEntries.add(relatedEntry);
                relatedEntry.incrementUsage();
            }
        }
        
        return relatedEntries;
    }
    
    /**
     * Get knowledge entry count
     * @return Total number of entries
     */
    public int getEntryCount() {
        return knowledgeBase.size();
    }
    
    /**
     * Get domain entry count
     * @param domain Knowledge domain
     * @return Number of entries in that domain
     */
    public int getDomainEntryCount(KnowledgeDomain domain) {
        return domainIndex.get(domain).size();
    }
    
    /**
     * Get most used entries
     * @param limit Maximum number of entries to return
     * @return List of most frequently used entries
     */
    public List<KnowledgeEntry> getMostUsedEntries(int limit) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for retrieving most used knowledge");
            return new ArrayList<>();
        }
        
        // Convert to list and sort by usage count
        List<KnowledgeEntry> entries = new ArrayList<>(knowledgeBase.values());
        entries.sort((e1, e2) -> Integer.compare(e2.getUsageCount(), e1.getUsageCount()));
        
        // Return top entries (up to limit)
        return entries.subList(0, Math.min(limit, entries.size()));
    }
    
    /**
     * Get recently added entries
     * @param limit Maximum number of entries to return
     * @return List of most recently added entries
     */
    public List<KnowledgeEntry> getRecentEntries(int limit) {
        // Verify access permission
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.READ_ONLY)) {
            Log.w(TAG, "Permission denied for retrieving recent knowledge");
            return new ArrayList<>();
        }
        
        // Convert to list and sort by timestamp
        List<KnowledgeEntry> entries = new ArrayList<>(knowledgeBase.values());
        entries.sort((e1, e2) -> Long.compare(e2.getTimestamp(), e1.getTimestamp()));
        
        // Return latest entries (up to limit)
        return entries.subList(0, Math.min(limit, entries.size()));
    }
    
    /**
     * Add a knowledge listener
     * @param listener Listener to add
     */
    public void addKnowledgeListener(KnowledgeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a knowledge listener
     * @param listener Listener to remove
     */
    public void removeKnowledgeListener(KnowledgeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify listeners of new knowledge
     * @param entry New knowledge entry
     */
    private void notifyKnowledgeAdded(KnowledgeEntry entry) {
        for (KnowledgeListener listener : listeners) {
            listener.onKnowledgeAdded(entry);
        }
    }
    
    /**
     * Notify listeners of updated knowledge
     * @param entry Updated knowledge entry
     */
    private void notifyKnowledgeUpdated(KnowledgeEntry entry) {
        for (KnowledgeListener listener : listeners) {
            listener.onKnowledgeUpdated(entry);
        }
    }
    
    /**
     * Clear all knowledge
     * Only available to admin
     */
    public void clearAllKnowledge() {
        // Verify access permission - requires admin
        if (!accessControl.checkPermission(AccessControl.SecurityZone.AI_STATE, 
                AccessControl.PermissionLevel.ADMIN)) {
            Log.w(TAG, "Permission denied for clearing knowledge base");
            return;
        }
        
        // Clear everything
        knowledgeBase.clear();
        
        for (KnowledgeDomain domain : KnowledgeDomain.values()) {
            domainIndex.get(domain).clear();
        }
        
        for (KnowledgeSource source : KnowledgeSource.values()) {
            sourceIndex.get(source).clear();
        }
        
        for (ReliabilityLevel reliability : ReliabilityLevel.values()) {
            reliabilityIndex.get(reliability).clear();
        }
        
        keywordIndex.clear();
        
        Log.d(TAG, "Knowledge base cleared");
    }
    
    /**
     * Knowledge listener interface
     */
    public interface KnowledgeListener {
        /**
         * Called when new knowledge is added
         * @param entry New knowledge entry
         */
        void onKnowledgeAdded(KnowledgeEntry entry);
        
        /**
         * Called when knowledge is updated
         * @param entry Updated knowledge entry
         */
        void onKnowledgeUpdated(KnowledgeEntry entry);
    }
}
