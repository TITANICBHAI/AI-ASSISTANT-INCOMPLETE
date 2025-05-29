package com.aiassistant.ai.features.voice.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context tracking for maintaining conversational state
 */
public class Context {
    
    /**
     * Domain types for contextual understanding
     */
    public enum Domain {
        GAMING,
        EDUCATION,
        BUSINESS,
        PERSONAL,
        TECHNICAL,
        GENERAL
    }
    
    // Current active domain
    private Domain activeDomain = Domain.GENERAL;
    
    // Context entities by domain
    private final Map<Domain, List<ContextEntity>> domainEntities = new HashMap<>();
    
    // Recent conversation history
    private final List<ConversationEntry> conversationHistory = new ArrayList<>();
    
    // Maximum conversation history size
    private static final int MAX_HISTORY_SIZE = 10;
    
    /**
     * Constructor
     */
    public Context() {
        // Initialize entity lists for each domain
        for (Domain domain : Domain.values()) {
            domainEntities.put(domain, new ArrayList<>());
        }
    }
    
    /**
     * Add entity to domain context
     */
    public void addEntity(Domain domain, ContextEntity entity) {
        List<ContextEntity> entities = domainEntities.get(domain);
        if (entities != null) {
            entities.add(entity);
        }
    }
    
    /**
     * Add conversation entry to history
     */
    public void addConversationEntry(String speaker, String text) {
        conversationHistory.add(new ConversationEntry(speaker, text));
        
        // Trim history if needed
        if (conversationHistory.size() > MAX_HISTORY_SIZE) {
            conversationHistory.remove(0);
        }
    }
    
    /**
     * Set active domain
     */
    public void setActiveDomain(Domain domain) {
        this.activeDomain = domain;
    }
    
    /**
     * Get active domain
     */
    public Domain getActiveDomain() {
        return activeDomain;
    }
    
    /**
     * Get entities for domain
     */
    public List<ContextEntity> getEntitiesForDomain(Domain domain) {
        return new ArrayList<>(domainEntities.get(domain));
    }
    
    /**
     * Get conversation history
     */
    public List<ConversationEntry> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }
    
    /**
     * Clear context for domain
     */
    public void clearDomainContext(Domain domain) {
        domainEntities.get(domain).clear();
    }
    
    /**
     * Clear all context
     */
    public void clearAllContext() {
        for (Domain domain : Domain.values()) {
            domainEntities.get(domain).clear();
        }
        conversationHistory.clear();
        activeDomain = Domain.GENERAL;
    }
    
    /**
     * Context entity class
     */
    public static class ContextEntity {
        private final String type;
        private final String value;
        private final Map<String, String> attributes = new HashMap<>();
        
        public ContextEntity(String type, String value) {
            this.type = type;
            this.value = value;
        }
        
        public void addAttribute(String key, String value) {
            attributes.put(key, value);
        }
        
        public String getType() {
            return type;
        }
        
        public String getValue() {
            return value;
        }
        
        public Map<String, String> getAttributes() {
            return new HashMap<>(attributes);
        }
    }
    
    /**
     * Conversation entry class
     */
    public static class ConversationEntry {
        private final String speaker;
        private final String text;
        private final long timestamp;
        
        public ConversationEntry(String speaker, String text) {
            this.speaker = speaker;
            this.text = text;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getSpeaker() {
            return speaker;
        }
        
        public String getText() {
            return text;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
}
