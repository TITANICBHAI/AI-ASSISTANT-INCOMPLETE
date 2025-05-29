package com.aiassistant.data.models;

/**
 * Model class for game metadata
 */
public class GameMetadata {
    
    private String packageName;
    private String displayName;
    private String description;
    private boolean isSupported;
    private int supportLevel;
    private long installDate;
    private long lastPlayedDate;
    
    /**
     * Default constructor
     */
    public GameMetadata() {
    }
    
    /**
     * Constructor with package name
     * 
     * @param packageName The package name
     */
    public GameMetadata(String packageName) {
        this.packageName = packageName;
    }
    
    /**
     * Get package name
     * 
     * @return The package name
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * Set package name
     * 
     * @param packageName The package name
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    /**
     * Get display name
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Set display name
     * 
     * @param displayName The display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Get description
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set description
     * 
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Check if the game is supported
     * 
     * @return Whether the game is supported
     */
    public boolean isSupported() {
        return isSupported;
    }
    
    /**
     * Set whether the game is supported
     * 
     * @param supported Whether the game is supported
     */
    public void setSupported(boolean supported) {
        isSupported = supported;
    }
    
    /**
     * Get support level (0-100)
     * 
     * @return The support level
     */
    public int getSupportLevel() {
        return supportLevel;
    }
    
    /**
     * Set support level (0-100)
     * 
     * @param supportLevel The support level
     */
    public void setSupportLevel(int supportLevel) {
        this.supportLevel = supportLevel;
    }
    
    /**
     * Get install date
     * 
     * @return The install date
     */
    public long getInstallDate() {
        return installDate;
    }
    
    /**
     * Set install date
     * 
     * @param installDate The install date
     */
    public void setInstallDate(long installDate) {
        this.installDate = installDate;
    }
    
    /**
     * Get last played date
     * 
     * @return The last played date
     */
    public long getLastPlayedDate() {
        return lastPlayedDate;
    }
    
    /**
     * Set last played date
     * 
     * @param lastPlayedDate The last played date
     */
    public void setLastPlayedDate(long lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
    }
}
