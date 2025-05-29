package com.aiassistant.core.ai.combat;

/**
 * Combat pattern class for enemy behavior analysis
 */
public class CombatPattern {
    // Pattern types
    public static final int PATTERN_UNKNOWN = 0;
    public static final int PATTERN_CIRCULAR = 1;
    public static final int PATTERN_LINEAR = 2;
    public static final int PATTERN_ZIGZAG = 3;
    public static final int PATTERN_ATTACK = 4;
    public static final int PATTERN_DEFENSIVE = 5;
    public static final int PATTERN_CHARGING = 6;
    public static final int PATTERN_RETREATING = 7;
    
    private int enemyId;
    private int patternType;
    private float confidence;
    private long detectionTime;
    private long durationMs;
    
    /**
     * Constructor
     * @param enemyId Enemy ID
     */
    public CombatPattern(int enemyId) {
        this.enemyId = enemyId;
        this.patternType = PATTERN_UNKNOWN;
        this.confidence = 0.0f;
        this.detectionTime = System.currentTimeMillis();
        this.durationMs = 0;
    }
    
    /**
     * Get enemy ID
     * @return Enemy ID
     */
    public int getEnemyId() {
        return enemyId;
    }
    
    /**
     * Get pattern type
     * @return Pattern type
     */
    public int getPatternType() {
        return patternType;
    }
    
    /**
     * Set pattern type
     * @param patternType Pattern type
     */
    public void setPatternType(int patternType) {
        this.patternType = patternType;
    }
    
    /**
     * Get confidence
     * @return Confidence value (0.0-1.0)
     */
    public float getConfidence() {
        return confidence;
    }
    
    /**
     * Set confidence
     * @param confidence Confidence value
     */
    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
    
    /**
     * Get detection time
     * @return Detection time
     */
    public long getDetectionTime() {
        return detectionTime;
    }
    
    /**
     * Set detection time
     * @param detectionTime Detection time
     */
    public void setDetectionTime(long detectionTime) {
        this.detectionTime = detectionTime;
    }
    
    /**
     * Get duration
     * @return Duration in milliseconds
     */
    public long getDurationMs() {
        return durationMs;
    }
    
    /**
     * Set duration
     * @param durationMs Duration in milliseconds
     */
    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
    
    /**
     * Get pattern type name
     * @return Pattern type name
     */
    public String getPatternTypeName() {
        switch (patternType) {
            case PATTERN_CIRCULAR:
                return "Circular";
            case PATTERN_LINEAR:
                return "Linear";
            case PATTERN_ZIGZAG:
                return "Zigzag";
            case PATTERN_ATTACK:
                return "Attack";
            case PATTERN_DEFENSIVE:
                return "Defensive";
            case PATTERN_CHARGING:
                return "Charging";
            case PATTERN_RETREATING:
                return "Retreating";
            default:
                return "Unknown";
        }
    }
}
