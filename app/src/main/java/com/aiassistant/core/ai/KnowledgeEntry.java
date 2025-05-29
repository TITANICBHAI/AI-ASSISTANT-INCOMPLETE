package com.aiassistant.core.ai;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.data.converters.Converters;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Knowledge entry entity for AI learning
 */
@Entity(tableName = "knowledge_entries")
@TypeConverters(Converters.class)
public class KnowledgeEntry {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String domain;
    private String concept;
    private String content;
    private float confidence;
    private Date creationDate;
    private Date lastAccessDate;
    private int accessCount;
    private String source;
    private Map<String, String> metadata;
    
    /**
     * Knowledge source types
     */
    public static final String SOURCE_USER_INTERACTION = "USER_INTERACTION";
    public static final String SOURCE_VOICE_COMMAND = "VOICE_COMMAND";
    public static final String SOURCE_DOCUMENT_ANALYSIS = "DOCUMENT_ANALYSIS";
    public static final String SOURCE_WEB_CONTENT = "WEB_CONTENT";
    public static final String SOURCE_SYSTEM_OBSERVATION = "SYSTEM_OBSERVATION";
    public static final String SOURCE_EXTERNAL_AI = "EXTERNAL_AI";
    
    /**
     * Default constructor
     */
    public KnowledgeEntry() {
        this.metadata = new HashMap<>();
        this.creationDate = new Date();
        this.lastAccessDate = new Date();
        this.accessCount = 0;
        this.confidence = 0.5f;
    }
    
    /**
     * Constructor with basic fields
     * 
     * @param domain Domain
     * @param concept Concept
     * @param content Content
     * @param confidence Confidence
     */
    public KnowledgeEntry(String domain, String concept, String content, float confidence) {
        this();
        this.domain = domain;
        this.concept = concept;
        this.content = content;
        this.confidence = confidence;
    }
    
    /**
     * Constructor with source
     * 
     * @param domain Domain
     * @param concept Concept
     * @param content Content
     * @param confidence Confidence
     * @param source Source
     */
    public KnowledgeEntry(String domain, String concept, String content, float confidence, String source) {
        this(domain, concept, content, confidence);
        this.source = source;
    }
    
    /**
     * Access the entry (update last access time and increment counter)
     */
    public void access() {
        this.lastAccessDate = new Date();
        this.accessCount++;
    }
    
    // Getters and setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public String getConcept() {
        return concept;
    }
    
    public void setConcept(String concept) {
        this.concept = concept;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public float getConfidence() {
        return confidence;
    }
    
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public Date getLastAccessDate() {
        return lastAccessDate;
    }
    
    public void setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }
    
    public int getAccessCount() {
        return accessCount;
    }
    
    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
    
    public String getMetadata(String key) {
        return this.metadata.get(key);
    }
}
