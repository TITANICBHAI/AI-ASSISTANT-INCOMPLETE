package com.aiassistant.core.ai;

import android.content.Context;
import com.aiassistant.core.learning.AdaptiveLearningInitializer;
import com.aiassistant.core.learning.PersonalityType;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the current state of the AI system
 */
public class AIState {
    private static final String TAG = "AIState";
    
    /**
     * AI operating modes
     */
    public enum Mode {
        GAMING,
        PRODUCTIVITY,
        LEARNING,
        SOCIAL
    }
    
    // Current AI mode
    private Mode currentMode;
    
    // Is the AI active
    private boolean active;
    
    // Is the AI processing a command
    private boolean processing;
    
    // The AI's personality type
    private PersonalityType personalityType;
    
    // Security protection level (1-5)
    private int securityProtectionLevel;
    
    // Battery preservation level (1-5)
    private int batteryPreservationLevel;
    
    // Processing power use (1-5)
    private int processingPowerUse;
    
    // Last mode change timestamp
    private long lastModeChange;
    
    // Active time in current session (ms)
    private long activeTime;
    
    // Session start time
    private long sessionStartTime;
    
    // Number of commands processed in current session
    private AtomicInteger commandsProcessed;
    
    // Number of security incidents detected
    private AtomicInteger securityIncidentsDetected;
    
    /**
     * Constructor
     */
    public AIState() {
        this.currentMode = Mode.GAMING; // Default to gaming mode
        this.active = false;
        this.processing = false;
        this.personalityType = PersonalityType.PROFESSIONAL; // Default personality
        this.securityProtectionLevel = 3; // Default medium security
        this.batteryPreservationLevel = 2; // Default low battery preservation
        this.processingPowerUse = 3; // Default medium processing
        this.lastModeChange = 0;
        this.activeTime = 0;
        this.sessionStartTime = 0;
        this.commandsProcessed = new AtomicInteger(0);
        this.securityIncidentsDetected = new AtomicInteger(0);
    }
    
    /**
     * Get current AI mode
     * @return Current mode
     */
    public Mode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Set current AI mode
     * @param mode New mode
     */
    public void setCurrentMode(Mode mode) {
        if (this.currentMode != mode) {
            this.lastModeChange = System.currentTimeMillis();
            this.currentMode = mode;
        }
    }
    
    /**
     * Is the AI active
     * @return True if active
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Set AI active state
     * @param active New active state
     */
    public void setActive(boolean active) {
        // If activating and was previously inactive, record session start time
        if (active && !this.active) {
            this.sessionStartTime = System.currentTimeMillis();
        } 
        // If deactivating and was previously active, update active time
        else if (!active && this.active) {
            this.activeTime += (System.currentTimeMillis() - sessionStartTime);
        }
        
        this.active = active;
    }
    
    /**
     * Is the AI processing a command
     * @return True if processing
     */
    public boolean isProcessing() {
        return processing;
    }
    
    /**
     * Set AI processing state
     * @param processing New processing state
     */
    public void setProcessing(boolean processing) {
        this.processing = processing;
        
        // If finished processing, increment commands processed
        if (!processing) {
            commandsProcessed.incrementAndGet();
        }
    }
    
    /**
     * Get personality type
     * @return Personality type
     */
    public PersonalityType getPersonalityType() {
        return personalityType;
    }
    
    /**
     * Set personality type
     * @param personalityType New personality type
     */
    public void setPersonalityType(PersonalityType personalityType) {
        this.personalityType = personalityType;
    }
    
    /**
     * Get security protection level (1-5)
     * @return Security level
     */
    public int getSecurityProtectionLevel() {
        return securityProtectionLevel;
    }
    
    /**
     * Set security protection level (1-5)
     * @param level New security level
     */
    public void setSecurityProtectionLevel(int level) {
        if (level < 1) level = 1;
        if (level > 5) level = 5;
        this.securityProtectionLevel = level;
    }
    
    /**
     * Get battery preservation level (1-5)
     * @return Battery preservation level
     */
    public int getBatteryPreservationLevel() {
        return batteryPreservationLevel;
    }
    
    /**
     * Set battery preservation level (1-5)
     * @param level New battery preservation level
     */
    public void setBatteryPreservationLevel(int level) {
        if (level < 1) level = 1;
        if (level > 5) level = 5;
        this.batteryPreservationLevel = level;
    }
    
    /**
     * Get processing power use level (1-5)
     * @return Processing power use level
     */
    public int getProcessingPowerUse() {
        return processingPowerUse;
    }
    
    /**
     * Set processing power use level (1-5)
     * @param level New processing power use level
     */
    public void setProcessingPowerUse(int level) {
        if (level < 1) level = 1;
        if (level > 5) level = 5;
        this.processingPowerUse = level;
    }
    
    /**
     * Get last mode change timestamp
     * @return Last mode change timestamp
     */
    public long getLastModeChange() {
        return lastModeChange;
    }
    
    /**
     * Get active time in current session (ms)
     * @return Active time
     */
    public long getActiveTime() {
        // If currently active, add current session time
        if (active) {
            return activeTime + (System.currentTimeMillis() - sessionStartTime);
        }
        return activeTime;
    }
    
    /**
     * Get session start time
     * @return Session start time
     */
    public long getSessionStartTime() {
        return sessionStartTime;
    }
    
    /**
     * Get number of commands processed in current session
     * @return Commands processed
     */
    public int getCommandsProcessed() {
        return commandsProcessed.get();
    }
    
    /**
     * Record a security incident
     */
    public void recordSecurityIncident() {
        securityIncidentsDetected.incrementAndGet();
    }
    
    /**
     * Get number of security incidents detected
     * @return Security incidents detected
     */
    public int getSecurityIncidentsDetected() {
        return securityIncidentsDetected.get();
    }
    
    /**
     * Reset session statistics
     */
    public void resetSessionStats() {
        activeTime = 0;
        sessionStartTime = System.currentTimeMillis();
        commandsProcessed.set(0);
    }
}
