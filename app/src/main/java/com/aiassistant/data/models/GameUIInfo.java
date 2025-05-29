package com.aiassistant.data.models;

import java.util.Arrays;

/**
 * Information about game UI elements
 */
public class GameUIInfo {
    
    // Health information
    private int healthPercent;
    private boolean hasHealthBar;
    
    // Ammo information
    private int ammoCount;
    private int maxAmmo;
    private boolean hasAmmoIndicator;
    
    // Score information
    private int score;
    private boolean hasScoreDisplay;
    
    // Mini-map
    private boolean hasMiniMap;
    
    // Status effects
    private String[] statusEffects;
    
    /**
     * Default constructor
     */
    public GameUIInfo() {
        this.healthPercent = 100;
        this.hasHealthBar = false;
        this.ammoCount = 0;
        this.maxAmmo = 0;
        this.hasAmmoIndicator = false;
        this.score = 0;
        this.hasScoreDisplay = false;
        this.hasMiniMap = false;
        this.statusEffects = new String[0];
    }

    /**
     * Get health percentage
     * @return Health percentage (0-100)
     */
    public int getHealthPercent() {
        return healthPercent;
    }

    /**
     * Set health percentage
     * @param healthPercent Health percentage (0-100)
     */
    public void setHealthPercent(int healthPercent) {
        this.healthPercent = Math.min(100, Math.max(0, healthPercent));
    }

    /**
     * Check if health bar is present
     * @return True if health bar is detected
     */
    public boolean hasHealthBar() {
        return hasHealthBar;
    }

    /**
     * Set whether health bar is present
     * @param hasHealthBar True if health bar is detected
     */
    public void setHasHealthBar(boolean hasHealthBar) {
        this.hasHealthBar = hasHealthBar;
    }

    /**
     * Get current ammo count
     * @return Current ammo count
     */
    public int getAmmoCount() {
        return ammoCount;
    }

    /**
     * Set current ammo count
     * @param ammoCount Current ammo count
     */
    public void setAmmoCount(int ammoCount) {
        this.ammoCount = Math.max(0, ammoCount);
    }

    /**
     * Get maximum ammo
     * @return Maximum ammo
     */
    public int getMaxAmmo() {
        return maxAmmo;
    }

    /**
     * Set maximum ammo
     * @param maxAmmo Maximum ammo
     */
    public void setMaxAmmo(int maxAmmo) {
        this.maxAmmo = Math.max(0, maxAmmo);
    }

    /**
     * Check if ammo indicator is present
     * @return True if ammo indicator is detected
     */
    public boolean hasAmmoIndicator() {
        return hasAmmoIndicator;
    }

    /**
     * Set whether ammo indicator is present
     * @param hasAmmoIndicator True if ammo indicator is detected
     */
    public void setHasAmmoIndicator(boolean hasAmmoIndicator) {
        this.hasAmmoIndicator = hasAmmoIndicator;
    }

    /**
     * Get current score
     * @return Current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Set current score
     * @param score Current score
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Check if score display is present
     * @return True if score display is detected
     */
    public boolean hasScoreDisplay() {
        return hasScoreDisplay;
    }

    /**
     * Set whether score display is present
     * @param hasScoreDisplay True if score display is detected
     */
    public void setHasScoreDisplay(boolean hasScoreDisplay) {
        this.hasScoreDisplay = hasScoreDisplay;
    }

    /**
     * Check if mini-map is present
     * @return True if mini-map is detected
     */
    public boolean hasMiniMap() {
        return hasMiniMap;
    }

    /**
     * Set whether mini-map is present
     * @param hasMiniMap True if mini-map is detected
     */
    public void setHasMiniMap(boolean hasMiniMap) {
        this.hasMiniMap = hasMiniMap;
    }

    /**
     * Get status effects
     * @return Array of status effect strings
     */
    public String[] getStatusEffects() {
        return Arrays.copyOf(statusEffects, statusEffects.length);
    }

    /**
     * Set status effects
     * @param statusEffects Array of status effect strings
     */
    public void setStatusEffects(String[] statusEffects) {
        this.statusEffects = Arrays.copyOf(statusEffects, statusEffects.length);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GameUIInfo{");
        
        if (hasHealthBar) {
            sb.append("health=").append(healthPercent).append("%, ");
        }
        
        if (hasAmmoIndicator) {
            sb.append("ammo=").append(ammoCount).append("/").append(maxAmmo).append(", ");
        }
        
        if (hasScoreDisplay) {
            sb.append("score=").append(score).append(", ");
        }
        
        if (hasMiniMap) {
            sb.append("has mini-map, ");
        }
        
        if (statusEffects.length > 0) {
            sb.append("status=").append(Arrays.toString(statusEffects));
        }
        
        sb.append("}");
        return sb.toString();
    }
}
