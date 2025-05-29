package com.aiassistant.learning.memory;

import java.io.Serializable;

/**
 * Represents a discrete memory item in the AI's knowledge base
 */
public class MemoryItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private final String content;
    private final String domainId;
    private final long timestamp;
    private final String source;
    
    private int retrievalCount = 0;
    private long lastRetrievalTime = 0;
    
    /**
     * Constructor
     * @param id Unique identifier
     * @param content Memory content
     * @param domainId Knowledge domain
     * @param timestamp Creation timestamp
     * @param source Source of the memory
     */
    public MemoryItem(String id, String content, String domainId, long timestamp, String source) {
        this.id = id;
        this.content = content;
        this.domainId = domainId;
        this.timestamp = timestamp;
        this.source = source;
    }
    
    /**
     * Record memory retrieval
     * Updates retrieval statistics
     */
    public void recordRetrieval() {
        retrievalCount++;
        lastRetrievalTime = System.currentTimeMillis();
    }

    /**
     * Get memory ID
     * @return Memory ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get memory content
     * @return Content string
     */
    public String getContent() {
        return content;
    }

    /**
     * Get domain ID
     * @return Domain ID
     */
    public String getDomainId() {
        return domainId;
    }

    /**
     * Get creation timestamp
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get memory source
     * @return Source description
     */
    public String getSource() {
        return source;
    }

    /**
     * Get retrieval count
     * @return Number of times this memory was retrieved
     */
    public int getRetrievalCount() {
        return retrievalCount;
    }

    /**
     * Get last retrieval time
     * @return Timestamp of last retrieval in milliseconds
     */
    public long getLastRetrievalTime() {
        return lastRetrievalTime;
    }
    
    /**
     * Get age of memory in milliseconds
     * @return Age in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemoryItem that = (MemoryItem) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
