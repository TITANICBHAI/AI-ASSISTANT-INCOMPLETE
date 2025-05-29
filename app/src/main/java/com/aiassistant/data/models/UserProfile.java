package com.aiassistant.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Represents the user profile and statistics
 */
@Entity(tableName = "user_profile")
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @PrimaryKey
    private int id;
    
    private String username;
    private int experiencePoints;
    private int currentLevel;
    private int sessionCount;
    private float averageAccuracy;
    private long totalPlayTime;
    private int tasksCompleted;
    private int gamesAssisted;
    
    // AI preferences
    private boolean autoStartEnabled;
    private boolean notificationsEnabled;
    private int defaultAIMode;
    private float volumeFeedback;
    private float gestureRecognitionSensitivity;
    
    /**
     * Default constructor
     */
    public UserProfile() {
        this.id = 1; // Single user profile, so use constant ID
        this.username = "Player";
        this.experiencePoints = 0;
        this.currentLevel = 1;
        this.sessionCount = 0;
        this.averageAccuracy = 0.0f;
        this.totalPlayTime = 0;
        this.tasksCompleted = 0;
        this.gamesAssisted = 0;
        this.autoStartEnabled = false;
        this.notificationsEnabled = true;
        this.defaultAIMode = 1; // Co-pilot mode as default
        this.volumeFeedback = 0.7f;
        this.gestureRecognitionSensitivity = 0.5f;
    }
    
    // Getters and setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public int getExperiencePoints() {
        return experiencePoints;
    }
    
    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
        updateLevel();
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }
    
    public int getSessionCount() {
        return sessionCount;
    }
    
    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }
    
    public float getAverageAccuracy() {
        return averageAccuracy;
    }
    
    public void setAverageAccuracy(float averageAccuracy) {
        this.averageAccuracy = averageAccuracy;
    }
    
    public long getTotalPlayTime() {
        return totalPlayTime;
    }
    
    public void setTotalPlayTime(long totalPlayTime) {
        this.totalPlayTime = totalPlayTime;
    }
    
    public int getTasksCompleted() {
        return tasksCompleted;
    }
    
    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }
    
    public int getGamesAssisted() {
        return gamesAssisted;
    }
    
    public void setGamesAssisted(int gamesAssisted) {
        this.gamesAssisted = gamesAssisted;
    }
    
    public boolean isAutoStartEnabled() {
        return autoStartEnabled;
    }
    
    public void setAutoStartEnabled(boolean autoStartEnabled) {
        this.autoStartEnabled = autoStartEnabled;
    }
    
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    
    public int getDefaultAIMode() {
        return defaultAIMode;
    }
    
    public void setDefaultAIMode(int defaultAIMode) {
        this.defaultAIMode = defaultAIMode;
    }
    
    public float getVolumeFeedback() {
        return volumeFeedback;
    }
    
    public void setVolumeFeedback(float volumeFeedback) {
        this.volumeFeedback = volumeFeedback;
    }
    
    public float getGestureRecognitionSensitivity() {
        return gestureRecognitionSensitivity;
    }
    
    public void setGestureRecognitionSensitivity(float gestureRecognitionSensitivity) {
        this.gestureRecognitionSensitivity = gestureRecognitionSensitivity;
    }
    
    /**
     * Add experience points to the profile
     * 
     * @param xp The experience points to add
     * @return The new level if a level up occurred, otherwise 0
     */
    public int addExperience(int xp) {
        int oldLevel = currentLevel;
        experiencePoints += xp;
        updateLevel();
        return (currentLevel > oldLevel) ? currentLevel : 0;
    }
    
    /**
     * Record a completed task
     * 
     * @param accuracy The accuracy of the task completion (0.0 - 1.0)
     */
    public void recordTaskCompletion(float accuracy) {
        tasksCompleted++;
        
        // Update average accuracy
        averageAccuracy = ((averageAccuracy * (tasksCompleted - 1)) + accuracy) / tasksCompleted;
        
        // Add experience based on accuracy
        int xpGained = (int)(10 * (0.5 + accuracy * 0.5)); // 5-10 XP based on accuracy
        addExperience(xpGained);
    }
    
    /**
     * Record a new game session
     * 
     * @param gamePackage The game package name
     * @param durationMinutes The duration of the session in minutes
     */
    public void recordGameSession(String gamePackage, int durationMinutes) {
        sessionCount++;
        totalPlayTime += durationMinutes * 60 * 1000; // Convert to milliseconds
        
        // Add experience
        int xpGained = Math.min(100, durationMinutes * 2); // 2 XP per minute, max 100
        addExperience(xpGained);
    }
    
    /**
     * Update the level based on current experience points
     */
    private void updateLevel() {
        // Level formula: each level requires 100 * level XP
        int xpRequired = 0;
        int newLevel = 1;
        
        while (true) {
            int nextLevelXp = 100 * newLevel;
            if (experiencePoints >= xpRequired + nextLevelXp) {
                xpRequired += nextLevelXp;
                newLevel++;
            } else {
                break;
            }
        }
        
        this.currentLevel = newLevel;
    }
    
    /**
     * Get the experience required for the next level
     * 
     * @return XP required for next level
     */
    public int getExperienceToNextLevel() {
        return 100 * currentLevel;
    }
    
    /**
     * Get the current level progress (XP in current level)
     * 
     * @return XP in current level
     */
    public int getLevelProgress() {
        int totalXpForCurrentLevel = 0;
        for (int i = 1; i < currentLevel; i++) {
            totalXpForCurrentLevel += 100 * i;
        }
        return experiencePoints - totalXpForCurrentLevel;
    }
    
    /**
     * Reset all statistics to default values
     */
    public void resetStats() {
        this.experiencePoints = 0;
        this.currentLevel = 1;
        this.sessionCount = 0;
        this.averageAccuracy = 0.0f;
        this.totalPlayTime = 0;
        this.tasksCompleted = 0;
        this.gamesAssisted = 0;
    }
}
