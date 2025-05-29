package com.aiassistant.security;

import java.util.ArrayList;
import java.util.List;

/**
 * Threat detection result
 */
public class ThreatDetectionResult {
    private boolean success;
    private ThreatLevel threatLevel;
    private String message;
    private List<String> detectedThreats;
    private long timestamp;
    
    /**
     * Constructor
     */
    public ThreatDetectionResult(boolean success, ThreatLevel threatLevel, String message) {
        this.success = success;
        this.threatLevel = threatLevel;
        this.message = message;
        this.detectedThreats = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Check if detection was successful
     * @return True if successful
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Get threat level
     * @return Threat level
     */
    public ThreatLevel getThreatLevel() {
        return threatLevel;
    }
    
    /**
     * Get message
     * @return Message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Get detected threats
     * @return List of detected threats
     */
    public List<String> getDetectedThreats() {
        return new ArrayList<>(detectedThreats);
    }
    
    /**
     * Add detected threat
     * @param threat Threat description
     */
    public void addDetectedThreat(String threat) {
        detectedThreats.add(threat);
    }
    
    /**
     * Get timestamp
     * @return Timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Check if threats were detected
     * @return True if threats detected
     */
    public boolean hasThreats() {
        return threatLevel != ThreatLevel.NONE && threatLevel != ThreatLevel.UNKNOWN;
    }
    
    /**
     * Get number of detected threats
     * @return Number of threats
     */
    public int getThreatCount() {
        return detectedThreats.size();
    }
}
