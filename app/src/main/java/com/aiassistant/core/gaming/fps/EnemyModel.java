package com.aiassistant.core.gaming.fps;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for tracking enemy information in FPS games.
 * Stores position history, movement patterns, and state information for a specific enemy.
 */
public class EnemyModel {
    // Enemy identification
    private final String id;
    
    // Current state
    private Rect currentBounds;
    private Point center;
    private Point aimPoint;
    private long lastUpdateTime;
    
    // Movement tracking
    private final List<Point> positionHistory = new ArrayList<>();
    private final List<Long> timeHistory = new ArrayList<>();
    private String movementPattern = "UNKNOWN";
    private float speed = 0.0f;
    private float threatLevel = 0.5f; // 0.0 to 1.0, default medium
    
    // Health/damage tracking
    private int estimatedHealth = 100;
    private boolean isVisible = true;
    
    // Position prediction
    private float velocityX = 0.0f;
    private float velocityY = 0.0f;
    private float accelerationX = 0.0f;
    private float accelerationY = 0.0f;
    
    // History limits
    private static final int MAX_HISTORY_ITEMS = 20;
    private static final long POSITION_HISTORY_MAX_AGE_MS = 5000; // 5 seconds
    
    /**
     * Constructor
     * @param id Unique identifier for this enemy
     * @param bounds Initial bounding rectangle
     */
    public EnemyModel(String id, Rect bounds) {
        this.id = id;
        this.currentBounds = new Rect(bounds);
        this.lastUpdateTime = System.currentTimeMillis();
        
        // Calculate center point
        this.center = new Point(
                bounds.left + bounds.width() / 2,
                bounds.top + bounds.height() / 2
        );
        
        // Initialize aim point (typically at the head, which is in the upper portion)
        this.aimPoint = new Point(
                center.x,
                bounds.top + Math.round(bounds.height() * 0.2f) // Aim at head (20% from top)
        );
        
        // Initialize position history
        addPositionToHistory(center, lastUpdateTime);
    }
    
    /**
     * Update the enemy model with new position data
     * @param bounds New bounding rectangle
     */
    public void update(Rect bounds) {
        // Update time
        long currentTime = System.currentTimeMillis();
        long timeDelta = currentTime - lastUpdateTime;
        
        // Store previous values for velocity calculation
        Point previousCenter = this.center;
        long previousTime = lastUpdateTime;
        
        // Update bounding rect
        this.currentBounds = new Rect(bounds);
        
        // Update center
        this.center = new Point(
                bounds.left + bounds.width() / 2,
                bounds.top + bounds.height() / 2
        );
        
        // Update aim point (maintaining the same relative position)
        this.aimPoint = new Point(
                center.x,
                bounds.top + Math.round(bounds.height() * 0.2f) // Aim at head
        );
        
        // Add to position history
        addPositionToHistory(center, currentTime);
        
        // Calculate velocity if enough time has passed
        if (timeDelta > 0) {
            // Calculate instantaneous velocity
            float newVelocityX = (center.x - previousCenter.x) / (timeDelta / 1000.0f);
            float newVelocityY = (center.y - previousCenter.y) / (timeDelta / 1000.0f);
            
            // Calculate acceleration
            accelerationX = (newVelocityX - velocityX) / (timeDelta / 1000.0f);
            accelerationY = (newVelocityY - velocityY) / (timeDelta / 1000.0f);
            
            // Update velocity with smoothing
            velocityX = newVelocityX * 0.7f + velocityX * 0.3f;
            velocityY = newVelocityY * 0.7f + velocityY * 0.3f;
            
            // Calculate current speed
            speed = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        }
        
        // Update last update time
        this.lastUpdateTime = currentTime;
        
        // Mark as visible
        this.isVisible = true;
        
        // Clean up old history entries
        cleanupHistory();
    }
    
    /**
     * Mark enemy as hit (reduce estimated health)
     * @param damage Amount of damage
     */
    public void hit(int damage) {
        estimatedHealth = Math.max(0, estimatedHealth - damage);
    }
    
    /**
     * Mark enemy as no longer visible
     */
    public void markNotVisible() {
        this.isVisible = false;
    }
    
    /**
     * Get the unique identifier
     * @return Enemy ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the current center position
     * @return Center point
     */
    public Point getCenter() {
        return center;
    }
    
    /**
     * Get the optimal aim point (typically head)
     * @return Aim point
     */
    public Point getAimPoint() {
        return aimPoint;
    }
    
    /**
     * Get the current bounding rectangle
     * @return Bounding rectangle
     */
    public Rect getBounds() {
        return currentBounds;
    }
    
    /**
     * Check if the enemy is still visible
     * @return True if visible, false otherwise
     */
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * Get the time since the enemy was last updated
     * @return Time in milliseconds
     */
    public long getTimeSinceLastSeen() {
        return System.currentTimeMillis() - lastUpdateTime;
    }
    
    /**
     * Get the enemy's current speed in pixels per second
     * @return Speed
     */
    public float getSpeed() {
        return speed;
    }
    
    /**
     * Get the identified movement pattern
     * @return Movement pattern string
     */
    public String getMovementPattern() {
        return movementPattern;
    }
    
    /**
     * Set the movement pattern (from pattern recognizer)
     * @param pattern Movement pattern string
     */
    public void setMovementPattern(String pattern) {
        this.movementPattern = pattern;
    }
    
    /**
     * Get the current threat level (0.0 to 1.0)
     * @return Threat level
     */
    public float getThreatLevel() {
        return threatLevel;
    }
    
    /**
     * Set the threat level (from threat analysis)
     * @param level Threat level (0.0 to 1.0)
     */
    public void setThreatLevel(float level) {
        this.threatLevel = Math.max(0.0f, Math.min(1.0f, level));
    }
    
    /**
     * Get estimated remaining health
     * @return Health value
     */
    public int getEstimatedHealth() {
        return estimatedHealth;
    }
    
    /**
     * Predict the enemy's position at a future time
     * @param timeMs Time in the future (milliseconds)
     * @return Predicted position
     */
    public Point predictPosition(long timeMs) {
        float seconds = timeMs / 1000.0f;
        
        // Basic physics prediction
        int predictedX = Math.round(center.x + velocityX * seconds + 0.5f * accelerationX * seconds * seconds);
        int predictedY = Math.round(center.y + velocityY * seconds + 0.5f * accelerationY * seconds * seconds);
        
        // Apply pattern-specific adjustments
        switch (movementPattern) {
            case "STRAFING":
                // For strafing enemies, limit vertical prediction
                predictedY = center.y;
                break;
                
            case "VERTICAL":
                // For vertical movement, limit horizontal prediction
                predictedX = center.x;
                break;
                
            case "STATIONARY":
                // For stationary enemies, don't predict movement
                return new Point(center.x, center.y);
                
            case "ERRATIC":
                // For erratic movement, reduce prediction confidence
                predictedX = center.x + Math.round((predictedX - center.x) * 0.5f);
                predictedY = center.y + Math.round((predictedY - center.y) * 0.5f);
                break;
        }
        
        return new Point(predictedX, predictedY);
    }
    
    /**
     * Add a position to the history
     */
    private void addPositionToHistory(Point position, long timestamp) {
        positionHistory.add(new Point(position));
        timeHistory.add(timestamp);
        
        // Keep history size limited
        if (positionHistory.size() > MAX_HISTORY_ITEMS) {
            positionHistory.remove(0);
            timeHistory.remove(0);
        }
    }
    
    /**
     * Clean up old history entries
     */
    private void cleanupHistory() {
        long currentTime = System.currentTimeMillis();
        while (!timeHistory.isEmpty() && 
               (currentTime - timeHistory.get(0)) > POSITION_HISTORY_MAX_AGE_MS) {
            positionHistory.remove(0);
            timeHistory.remove(0);
        }
    }
    
    /**
     * Get the enemy's velocity vector
     * @return Velocity as a Point (x, y)
     */
    public Point getVelocity() {
        return new Point(Math.round(velocityX), Math.round(velocityY));
    }
    
    /**
     * Get the enemy's position history
     * @return List of historical positions
     */
    public List<Point> getPositionHistory() {
        return new ArrayList<>(positionHistory);
    }
    
    /**
     * Get the time history corresponding to positions
     * @return List of timestamps
     */
    public List<Long> getTimeHistory() {
        return new ArrayList<>(timeHistory);
    }
}
