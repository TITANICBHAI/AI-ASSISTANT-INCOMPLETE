package com.aiassistant.core.gaming.fps;

/**
 * Class representing enemy information for the FPS game state.
 * Contains position, size, and other relevant data for gameplay assistance.
 */
public class EnemyInfo {
    // Position
    public final int centerX;
    public final int centerY;
    
    // Size
    public final int width;
    public final int height;
    
    // Metrics
    public final double distanceFromCenter;
    public final float relativeSize;
    
    // Time tracking
    public final long detectionTime;
    
    /**
     * Constructor
     * @param centerX X coordinate of enemy center
     * @param centerY Y coordinate of enemy center
     * @param width Width of enemy bounding box
     * @param height Height of enemy bounding box
     * @param distanceFromCenter Distance from screen center
     * @param relativeSize Size relative to screen
     */
    public EnemyInfo(int centerX, int centerY, int width, int height, 
                     double distanceFromCenter, float relativeSize) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.distanceFromCenter = distanceFromCenter;
        this.relativeSize = relativeSize;
        this.detectionTime = System.currentTimeMillis();
    }
}
