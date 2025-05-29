package com.aiassistant.core.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.aiassistant.core.data.converter.MapConverter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a caller profile
 * Used to store information about callers and their interactions
 */
@Entity(tableName = "caller_profiles")
public class CallerProfile {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String phoneNumber;
    private String name;
    private int callCount;
    private long lastCallTime;
    private long totalTalkTime;
    private String lastEmotion;
    private float emotionalValence;  // Positive-negative scale
    private float emotionalArousal;  // Calm-excited scale
    
    @TypeConverters(MapConverter.class)
    private Map<String, Float> emotionHistory;
    
    @TypeConverters(MapConverter.class)
    private Map<String, String> metadata;
    
    /**
     * Default constructor
     */
    public CallerProfile() {
        this.callCount = 0;
        this.totalTalkTime = 0;
        this.emotionalValence = 0.0f;
        this.emotionalArousal = 0.0f;
        this.emotionHistory = new HashMap<>();
        this.metadata = new HashMap<>();
    }
    
    /**
     * Constructor with phone number and name
     * @param phoneNumber Caller phone number
     * @param name Caller name
     */
    public CallerProfile(String phoneNumber, String name) {
        this();
        this.phoneNumber = phoneNumber;
        this.name = name;
    }
    
    /**
     * Get ID
     * @return Profile ID
     */
    public long getId() {
        return id;
    }
    
    /**
     * Set ID
     * @param id Profile ID
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * Get phone number
     * @return Phone number
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    /**
     * Set phone number
     * @param phoneNumber Phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    /**
     * Get name
     * @return Caller name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set name
     * @param name Caller name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get call count
     * @return Number of calls
     */
    public int getCallCount() {
        return callCount;
    }
    
    /**
     * Set call count
     * @param callCount Number of calls
     */
    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }
    
    /**
     * Increment call count
     */
    public void incrementCallCount() {
        this.callCount++;
    }
    
    /**
     * Get last call time
     * @return Last call timestamp in milliseconds
     */
    public long getLastCallTime() {
        return lastCallTime;
    }
    
    /**
     * Set last call time
     * @param lastCallTime Last call timestamp in milliseconds
     */
    public void setLastCallTime(long lastCallTime) {
        this.lastCallTime = lastCallTime;
    }
    
    /**
     * Update last call time to current time
     */
    public void updateLastCallTime() {
        this.lastCallTime = System.currentTimeMillis();
    }
    
    /**
     * Get total talk time
     * @return Total talk time in seconds
     */
    public long getTotalTalkTime() {
        return totalTalkTime;
    }
    
    /**
     * Set total talk time
     * @param totalTalkTime Total talk time in seconds
     */
    public void setTotalTalkTime(long totalTalkTime) {
        this.totalTalkTime = totalTalkTime;
    }
    
    /**
     * Add talk time
     * @param talkTime Talk time to add in seconds
     */
    public void addTalkTime(long talkTime) {
        this.totalTalkTime += talkTime;
    }
    
    /**
     * Get last emotion
     * @return Last emotion
     */
    public String getLastEmotion() {
        return lastEmotion;
    }
    
    /**
     * Set last emotion
     * @param lastEmotion Last emotion
     */
    public void setLastEmotion(String lastEmotion) {
        this.lastEmotion = lastEmotion;
    }
    
    /**
     * Get emotional valence
     * @return Emotional valence value
     */
    public float getEmotionalValence() {
        return emotionalValence;
    }
    
    /**
     * Set emotional valence
     * @param emotionalValence Emotional valence value
     */
    public void setEmotionalValence(float emotionalValence) {
        this.emotionalValence = emotionalValence;
    }
    
    /**
     * Get emotional arousal
     * @return Emotional arousal value
     */
    public float getEmotionalArousal() {
        return emotionalArousal;
    }
    
    /**
     * Set emotional arousal
     * @param emotionalArousal Emotional arousal value
     */
    public void setEmotionalArousal(float emotionalArousal) {
        this.emotionalArousal = emotionalArousal;
    }
    
    /**
     * Get emotion history
     * @return Map of emotions and their strength values
     */
    public Map<String, Float> getEmotionHistory() {
        return emotionHistory;
    }
    
    /**
     * Set emotion history
     * @param emotionHistory Map of emotions and their strength values
     */
    public void setEmotionHistory(Map<String, Float> emotionHistory) {
        this.emotionHistory = emotionHistory;
    }
    
    /**
     * Add emotion to history
     * @param emotion Emotion name
     * @param value Emotion strength value
     */
    public void addEmotionToHistory(String emotion, float value) {
        if (this.emotionHistory == null) {
            this.emotionHistory = new HashMap<>();
        }
        this.emotionHistory.put(emotion, value);
    }
    
    /**
     * Get metadata
     * @return Map of metadata key-value pairs
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /**
     * Set metadata
     * @param metadata Map of metadata key-value pairs
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Add metadata
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, String value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
    
    /**
     * Get metadata value
     * @param key Metadata key
     * @return Metadata value or null if not found
     */
    public String getMetadataValue(String key) {
        if (this.metadata == null) {
            return null;
        }
        return this.metadata.get(key);
    }
    
    /**
     * Update profile with call information
     * @param callDuration Call duration in seconds
     * @param emotion Detected emotion
     * @param valence Emotional valence value
     * @param arousal Emotional arousal value
     */
    public void updateWithCallInfo(long callDuration, String emotion, float valence, float arousal) {
        incrementCallCount();
        updateLastCallTime();
        addTalkTime(callDuration);
        setLastEmotion(emotion);
        setEmotionalValence(valence);
        setEmotionalArousal(arousal);
        addEmotionToHistory(emotion, valence);
    }
    
    @Override
    public String toString() {
        return "CallerProfile{" +
                "id=" + id +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", name='" + name + '\'' +
                ", callCount=" + callCount +
                ", lastCallTime=" + new Date(lastCallTime) +
                ", totalTalkTime=" + totalTalkTime +
                ", lastEmotion='" + lastEmotion + '\'' +
                '}';
    }
}
