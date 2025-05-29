package com.aiassistant.ai.features.movement;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Movement Analysis Manager
 * Simplified interface for using the movement analysis feature
 */
public class MovementAnalysisManager implements MovementAnalysisFeature.MovementAnalysisListener {
    private static final String TAG = "MovementAnalysisManager";
    
    private final Context context;
    private final MovementAnalysisFeature movementAnalysisFeature;
    private final List<MovementRecommendationListener> recommendationListeners;
    private final List<MovementStatisticsListener> statisticsListeners;
    private final List<MapAreaListener> mapAreaListeners;
    
    /**
     * Constructor
     * @param context Application context
     * @param movementAnalysisFeature Movement analysis feature
     */
    public MovementAnalysisManager(Context context, 
                                 MovementAnalysisFeature movementAnalysisFeature) {
        this.context = context;
        this.movementAnalysisFeature = movementAnalysisFeature;
        this.recommendationListeners = new ArrayList<>();
        this.statisticsListeners = new ArrayList<>();
        this.mapAreaListeners = new ArrayList<>();
        
        // Register as a listener
        movementAnalysisFeature.addListener(this);
    }
    
    /**
     * Update player position
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param mode Movement mode
     */
    public void updatePosition(float x, float y, float z, 
                              MovementAnalysisFeature.MovementMode mode) {
        if (movementAnalysisFeature.isEnabled()) {
            movementAnalysisFeature.recordPosition(x, y, z, mode);
        }
    }
    
    /**
     * Update player position (2D)
     * @param x X coordinate
     * @param y Y coordinate
     * @param mode Movement mode
     */
    public void updatePosition(float x, float y, MovementAnalysisFeature.MovementMode mode) {
        if (movementAnalysisFeature.isEnabled()) {
            movementAnalysisFeature.recordPosition(x, y, mode);
        }
    }
    
    /**
     * Record obstacle
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param type Obstacle type
     */
    public void recordObstacle(float x, float y, float z, String type) {
        if (movementAnalysisFeature.isEnabled()) {
            movementAnalysisFeature.recordObstacle(x, y, z, type);
        }
    }
    
    /**
     * Signal that movement has stopped
     */
    public void stopMovement() {
        if (movementAnalysisFeature.isEnabled()) {
            movementAnalysisFeature.stopMovement();
        }
    }
    
    /**
     * Get current speed
     * @return Current speed
     */
    public float getCurrentSpeed() {
        if (movementAnalysisFeature.isEnabled()) {
            return movementAnalysisFeature.getCurrentSpeed();
        }
        return 0.0f;
    }
    
    /**
     * Get average speed
     * @return Average speed
     */
    public float getAverageSpeed() {
        if (movementAnalysisFeature.isEnabled()) {
            return movementAnalysisFeature.getAverageSpeed();
        }
        return 0.0f;
    }
    
    /**
     * Get movement efficiency
     * @return Efficiency rating (0-1)
     */
    public float getMovementEfficiency() {
        if (movementAnalysisFeature.isEnabled()) {
            return movementAnalysisFeature.getMovementEfficiency();
        }
        return 0.0f;
    }
    
    /**
     * Get recommended movement mode
     * @return Recommended mode
     */
    public MovementAnalysisFeature.MovementMode getRecommendedMode() {
        if (movementAnalysisFeature.isEnabled()) {
            return movementAnalysisFeature.getRecommendedMovementMode();
        }
        return MovementAnalysisFeature.MovementMode.WALKING;
    }
    
    /**
     * Check if player is currently moving
     * @return true if moving
     */
    public boolean isMoving() {
        return movementAnalysisFeature.isEnabled() && 
               movementAnalysisFeature.isMoving();
    }
    
    /**
     * Add a movement recommendation listener
     * @param listener Listener to add
     */
    public void addRecommendationListener(MovementRecommendationListener listener) {
        if (listener != null && !recommendationListeners.contains(listener)) {
            recommendationListeners.add(listener);
        }
    }
    
    /**
     * Remove a movement recommendation listener
     * @param listener Listener to remove
     */
    public void removeRecommendationListener(MovementRecommendationListener listener) {
        recommendationListeners.remove(listener);
    }
    
    /**
     * Add a movement statistics listener
     * @param listener Listener to add
     */
    public void addStatisticsListener(MovementStatisticsListener listener) {
        if (listener != null && !statisticsListeners.contains(listener)) {
            statisticsListeners.add(listener);
        }
    }
    
    /**
     * Remove a movement statistics listener
     * @param listener Listener to remove
     */
    public void removeStatisticsListener(MovementStatisticsListener listener) {
        statisticsListeners.remove(listener);
    }
    
    /**
     * Add a map area listener
     * @param listener Listener to add
     */
    public void addMapAreaListener(MapAreaListener listener) {
        if (listener != null && !mapAreaListeners.contains(listener)) {
            mapAreaListeners.add(listener);
        }
    }
    
    /**
     * Remove a map area listener
     * @param listener Listener to remove
     */
    public void removeMapAreaListener(MapAreaListener listener) {
        mapAreaListeners.remove(listener);
    }
    
    @Override
    public void onSessionStarted(MovementAnalysisFeature.MovementSession session) {
        // Notify statistics listeners
        for (MovementStatisticsListener listener : statisticsListeners) {
            listener.onMovementSessionStarted();
        }
    }
    
    @Override
    public void onSessionEnded(MovementAnalysisFeature.MovementSession session) {
        // Notify statistics listeners
        MovementStatistics stats = new MovementStatistics(
            session.getEfficiencyRating(),
            session.getAverageSpeed(),
            session.getTotalDistance(),
            session.getDuration() / 1000.0f
        );
        
        for (MovementStatisticsListener listener : statisticsListeners) {
            listener.onMovementSessionEnded(stats);
        }
    }
    
    @Override
    public void onMovementStarted(MovementAnalysisFeature.Position position) {
        // Notify statistics listeners
        for (MovementStatisticsListener listener : statisticsListeners) {
            listener.onMovementStarted();
        }
    }
    
    @Override
    public void onMovementStopped(MovementAnalysisFeature.Position position) {
        // Notify statistics listeners
        for (MovementStatisticsListener listener : statisticsListeners) {
            listener.onMovementStopped();
        }
    }
    
    @Override
    public void onPositionUpdated(MovementAnalysisFeature.Position position) {
        // Update statistics listeners
        float currentSpeed = getCurrentSpeed();
        float efficiency = getMovementEfficiency();
        
        for (MovementStatisticsListener listener : statisticsListeners) {
            listener.onStatsUpdated(currentSpeed, efficiency);
        }
    }
    
    @Override
    public void onObstacleEncountered(MovementAnalysisFeature.Obstacle obstacle) {
        // Notify recommendation listeners
        MovementRecommendation recommendation = new MovementRecommendation(
            "avoid_obstacle",
            "Avoid " + obstacle.getType() + " obstacle",
            1
        );
        
        for (MovementRecommendationListener listener : recommendationListeners) {
            listener.onRecommendationGenerated(recommendation);
        }
    }
    
    @Override
    public void onPatternDetected(MovementAnalysisFeature.MovementPattern pattern) {
        // Notify recommendation listeners
        MovementRecommendation recommendation = new MovementRecommendation(
            pattern.getId(),
            pattern.getDescription(),
            2
        );
        
        for (MovementRecommendationListener listener : recommendationListeners) {
            listener.onRecommendationGenerated(recommendation);
        }
    }
    
    @Override
    public void onAreaEntered(MovementAnalysisFeature.MapArea area) {
        // Notify map area listeners
        for (MapAreaListener listener : mapAreaListeners) {
            listener.onAreaEntered(area.getName(), area.getId());
        }
    }
    
    @Override
    public void onPointOfInterestNearby(MovementAnalysisFeature.PointOfInterest poi) {
        // Notify map area listeners
        for (MapAreaListener listener : mapAreaListeners) {
            listener.onPointOfInterestNearby(poi.getName(), poi.getType());
        }
    }
    
    /**
     * Movement Recommendation class
     * Simplified recommendation for movement
     */
    public static class MovementRecommendation {
        private final String id;
        private final String description;
        private final int priority;
        
        /**
         * Constructor
         * @param id Recommendation ID
         * @param description Description
         * @param priority Priority (lower = higher priority)
         */
        public MovementRecommendation(String id, String description, int priority) {
            this.id = id;
            this.description = description;
            this.priority = priority;
        }
        
        /**
         * Get recommendation ID
         * @return Recommendation ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get description
         * @return Description
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * Get priority
         * @return Priority
         */
        public int getPriority() {
            return priority;
        }
    }
    
    /**
     * Movement Statistics class
     * Movement performance metrics
     */
    public static class MovementStatistics {
        private final float efficiency;
        private final float averageSpeed;
        private final float totalDistance;
        private final float duration;
        
        /**
         * Constructor
         * @param efficiency Efficiency rating (0-1)
         * @param averageSpeed Average speed
         * @param totalDistance Total distance traveled
         * @param duration Duration in seconds
         */
        public MovementStatistics(float efficiency, float averageSpeed, 
                                float totalDistance, float duration) {
            this.efficiency = efficiency;
            this.averageSpeed = averageSpeed;
            this.totalDistance = totalDistance;
            this.duration = duration;
        }
        
        /**
         * Get efficiency rating
         * @return Efficiency (0-1)
         */
        public float getEfficiency() {
            return efficiency;
        }
        
        /**
         * Get average speed
         * @return Average speed
         */
        public float getAverageSpeed() {
            return averageSpeed;
        }
        
        /**
         * Get total distance
         * @return Total distance
         */
        public float getTotalDistance() {
            return totalDistance;
        }
        
        /**
         * Get duration
         * @return Duration in seconds
         */
        public float getDuration() {
            return duration;
        }
        
        /**
         * Get efficiency grade (A-F)
         * @return Letter grade
         */
        public String getEfficiencyGrade() {
            if (efficiency >= 0.9f) return "A";
            if (efficiency >= 0.7f) return "B";
            if (efficiency >= 0.5f) return "C";
            if (efficiency >= 0.3f) return "D";
            return "F";
        }
    }
    
    /**
     * Movement Recommendation Listener interface
     * For receiving movement recommendations
     */
    public interface MovementRecommendationListener {
        /**
         * Called when a recommendation is generated
         * @param recommendation The recommendation
         */
        void onRecommendationGenerated(MovementRecommendation recommendation);
    }
    
    /**
     * Movement Statistics Listener interface
     * For receiving movement statistics
     */
    public interface MovementStatisticsListener {
        /**
         * Called when a movement session starts
         */
        void onMovementSessionStarted();
        
        /**
         * Called when a movement session ends
         * @param statistics Session statistics
         */
        void onMovementSessionEnded(MovementStatistics statistics);
        
        /**
         * Called when movement starts
         */
        void onMovementStarted();
        
        /**
         * Called when movement stops
         */
        void onMovementStopped();
        
        /**
         * Called when movement statistics are updated
         * @param currentSpeed Current speed
         * @param efficiency Efficiency rating
         */
        void onStatsUpdated(float currentSpeed, float efficiency);
    }
    
    /**
     * Map Area Listener interface
     * For receiving map area events
     */
    public interface MapAreaListener {
        /**
         * Called when entering a map area
         * @param areaName Area name
         * @param areaId Area ID
         */
        void onAreaEntered(String areaName, String areaId);
        
        /**
         * Called when near a point of interest
         * @param poiName Point name
         * @param poiType Point type
         */
        void onPointOfInterestNearby(String poiName, String poiType);
    }
}
