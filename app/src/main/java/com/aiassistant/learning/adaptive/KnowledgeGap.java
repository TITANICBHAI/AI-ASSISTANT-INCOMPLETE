package com.aiassistant.learning.adaptive;

import java.io.Serializable;

/**
 * Represents an identified gap in the AI's knowledge
 */
public class KnowledgeGap implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * Types of knowledge gaps
     */
    public enum GapType {
        INSUFFICIENT_COVERAGE,  // Not enough knowledge in a domain
        OUTDATED_KNOWLEDGE,     // Knowledge that needs updating
        MISSING_CONNECTIONS,    // Lack of connections between domains
        CONFLICTING_INFORMATION, // Contradictory knowledge
        INCOMPLETE_CONTEXT      // Missing contextual understanding
    }
    
    private final String domainId;
    private final String description;
    private final GapType gapType;
    private final long identificationTime;
    
    private boolean addressed = false;
    private long addressedTime = 0;
    
    /**
     * Constructor
     * @param domainId Domain ID
     * @param description Gap description
     * @param gapType Type of gap
     * @param identificationTime Time when gap was identified
     */
    public KnowledgeGap(String domainId, String description, GapType gapType, long identificationTime) {
        this.domainId = domainId;
        this.description = description;
        this.gapType = gapType;
        this.identificationTime = identificationTime;
    }

    /**
     * Get domain ID
     * @return Domain ID
     */
    public String getDomainId() {
        return domainId;
    }

    /**
     * Get gap description
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get gap type
     * @return Gap type
     */
    public GapType getGapType() {
        return gapType;
    }

    /**
     * Get identification time
     * @return Time in milliseconds
     */
    public long getIdentificationTime() {
        return identificationTime;
    }

    /**
     * Check if gap is addressed
     * @return True if addressed
     */
    public boolean isAddressed() {
        return addressed;
    }

    /**
     * Mark gap as addressed
     */
    public void markAddressed() {
        this.addressed = true;
        this.addressedTime = System.currentTimeMillis();
    }

    /**
     * Get addressed time
     * @return Time in milliseconds
     */
    public long getAddressedTime() {
        return addressedTime;
    }
    
    /**
     * Get age of gap in milliseconds
     * @return Age in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - identificationTime;
    }
}
