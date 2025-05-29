package com.aiassistant.security;

/**
 * Threat level enum
 */
public enum ThreatLevel {
    NONE(0, "No threats detected"),
    LOW(1, "Low level threats detected"),
    MEDIUM(2, "Medium level threats detected"),
    HIGH(3, "High level threats detected"),
    CRITICAL(4, "Critical threats detected"),
    UNKNOWN(-1, "Threat level unknown");
    
    private final int level;
    private final String description;
    
    /**
     * Constructor
     */
    ThreatLevel(int level, String description) {
        this.level = level;
        this.description = description;
    }
    
    /**
     * Get level value
     * @return Level value
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Get description
     * @return Description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if level is at least the specified level
     * @param other Other level
     * @return True if this level is at least the other level
     */
    public boolean isAtLeast(ThreatLevel other) {
        return this.level >= other.level;
    }
    
    /**
     * Check if level is higher than the specified level
     * @param other Other level
     * @return True if this level is higher than the other level
     */
    public boolean isHigherThan(ThreatLevel other) {
        return this.level > other.level;
    }
    
    /**
     * Get threat level by value
     * @param value Level value
     * @return Threat level or UNKNOWN if not found
     */
    public static ThreatLevel getByValue(int value) {
        for (ThreatLevel level : values()) {
            if (level.level == value) {
                return level;
            }
        }
        return UNKNOWN;
    }
}
