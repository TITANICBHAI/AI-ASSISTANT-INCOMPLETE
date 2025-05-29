package com.aiassistant.learning.model;

import java.io.Serializable;

/**
 * Represents a distinct knowledge domain in the AI's learning system
 */
public class KnowledgeDomain implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private final String name;
    private final String description;
    
    private int knowledgeCount = 0;
    private long lastUpdated = 0;
    private int retrievalCount = 0;
    
    /**
     * Constructor
     * @param id Domain ID
     * @param name Domain name
     * @param description Domain description
     */
    public KnowledgeDomain(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Get domain ID
     * @return Domain ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get domain name
     * @return Domain name
     */
    public String getName() {
        return name;
    }

    /**
     * Get domain description
     * @return Domain description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get knowledge count
     * @return Number of knowledge items in this domain
     */
    public int getKnowledgeCount() {
        return knowledgeCount;
    }

    /**
     * Set knowledge count
     * @param knowledgeCount New count
     */
    public void setKnowledgeCount(int knowledgeCount) {
        this.knowledgeCount = knowledgeCount;
    }

    /**
     * Increment knowledge count
     */
    public void incrementKnowledgeCount() {
        knowledgeCount++;
    }

    /**
     * Get last updated timestamp
     * @return Last update time in milliseconds
     */
    public long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Set last updated timestamp
     * @param lastUpdated Last update time
     */
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Get retrieval count
     * @return Number of times this domain was retrieved
     */
    public int getRetrievalCount() {
        return retrievalCount;
    }

    /**
     * Increment retrieval count
     */
    public void incrementRetrievalCount() {
        retrievalCount++;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeDomain that = (KnowledgeDomain) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
