package com.aiassistant.core.input;

import android.graphics.PointF;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a touch path (gesture) for input simulation
 */
public class TouchPath {
    
    /**
     * A point with time information
     */
    public static class Point {
        public float x;
        public float y;
        public long time;
        
        /**
         * Constructor
         * 
         * @param x X coordinate
         * @param y Y coordinate
         * @param time Time in milliseconds
         */
        public Point(float x, float y, long time) {
            this.x = x;
            this.y = y;
            this.time = time;
        }
        
        /**
         * Create a point with the current time
         * 
         * @param x X coordinate
         * @param y Y coordinate
         * @return The created point
         */
        public static Point now(float x, float y) {
            return new Point(x, y, SystemClock.uptimeMillis());
        }
        
        @Override
        public String toString() {
            return String.format("(%.1f, %.1f) @ %d", x, y, time);
        }
    }
    
    private List<Point> points;
    private long startTime;
    private long endTime;
    private boolean isComplete;
    
    /**
     * Constructor
     */
    public TouchPath() {
        this.points = new ArrayList<>();
        this.startTime = 0;
        this.endTime = 0;
        this.isComplete = false;
    }
    
    /**
     * Add a point to the path
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param time Time in milliseconds
     */
    public void addPoint(float x, float y, long time) {
        Point point = new Point(x, y, time);
        if (points.isEmpty()) {
            startTime = time;
        }
        endTime = time;
        points.add(point);
    }
    
    /**
     * Create a path from start to end points with a specified duration
     * 
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param endX End X coordinate
     * @param endY End Y coordinate
     * @param durationMs Duration in milliseconds
     * @param pointCount Number of points to generate
     * @return The created path
     */
    public static TouchPath createPath(float startX, float startY, float endX, float endY,
                                      long durationMs, int pointCount) {
        TouchPath path = new TouchPath();
        long currentTime = SystemClock.uptimeMillis();
        
        for (int i = 0; i < pointCount; i++) {
            float progress = (float) i / (pointCount - 1);
            float x = startX + (endX - startX) * progress;
            float y = startY + (endY - startY) * progress;
            long time = currentTime + (long) (durationMs * progress);
            path.addPoint(x, y, time);
        }
        
        path.setComplete(true);
        return path;
    }
    
    /**
     * Get the points in the path
     * 
     * @return The points
     */
    public List<Point> getPoints() {
        return points;
    }
    
    /**
     * Get the start time
     * 
     * @return The start time
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Get the end time
     * 
     * @return The end time
     */
    public long getEndTime() {
        return endTime;
    }
    
    /**
     * Get the duration
     * 
     * @return The duration in milliseconds
     */
    public long getDuration() {
        if (points.isEmpty()) {
            return 0;
        }
        return endTime - startTime;
    }
    
    /**
     * Check if the path is complete
     * 
     * @return Whether the path is complete
     */
    public boolean isComplete() {
        return isComplete;
    }
    
    /**
     * Set whether the path is complete
     * 
     * @param complete Whether the path is complete
     */
    public void setComplete(boolean complete) {
        isComplete = complete;
    }
    
    /**
     * Get the distance of the path
     * 
     * @return The distance in pixels
     */
    public float getDistance() {
        if (points.size() < 2) {
            return 0;
        }
        
        float distance = 0;
        Point prevPoint = points.get(0);
        
        for (int i = 1; i < points.size(); i++) {
            Point point = points.get(i);
            float dx = point.x - prevPoint.x;
            float dy = point.y - prevPoint.y;
            distance += Math.sqrt(dx * dx + dy * dy);
            prevPoint = point;
        }
        
        return distance;
    }
    
    /**
     * Get the velocity of the path
     * 
     * @return The velocity in pixels per second
     */
    public float getVelocity() {
        long duration = getDuration();
        if (duration == 0) {
            return 0;
        }
        return getDistance() * 1000 / duration;
    }
    
    /**
     * Clear the path
     */
    public void clear() {
        points.clear();
        startTime = 0;
        endTime = 0;
        isComplete = false;
    }
}
