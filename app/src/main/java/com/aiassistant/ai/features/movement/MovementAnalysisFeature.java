package com.aiassistant.ai.features.movement;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import com.aiassistant.ai.features.BaseFeature;
import com.aiassistant.security.SecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Advanced Movement Analysis Feature
 * - Analyzes player movement patterns
 * - Optimizes pathing and navigation
 * - Identifies efficient movement strategies
 * - Provides real-time movement recommendations
 */
public class MovementAnalysisFeature extends BaseFeature {
    private static final String TAG = "MovementAnalysis";
    private static final String FEATURE_NAME = "advanced_movement_analysis";
    
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = 100;
    
    // Maximum positions to track
    private static final int MAX_POSITION_HISTORY = 1000;
    
    // Last update timestamp
    private long lastUpdateTime;
    
    // Current movement session
    private MovementSession currentSession;
    
    // Historical movement data
    private final List<MovementSession> movementHistory;
    
    // Known map areas
    private final Map<String, MapArea> knownAreas;
    
    // Movement patterns library
    private final Map<String, MovementPattern> patternsLibrary;
    
    // Listeners for movement events
    private final List<MovementAnalysisListener> listeners;
    
    // Movement state
    private boolean isMoving;
    private MovementMode currentMode;
    
    /**
     * Movement mode enumeration
     */
    public enum MovementMode {
        WALKING,
        RUNNING,
        CROUCHING,
        JUMPING,
        CLIMBING,
        SWIMMING,
        FLYING,
        CUSTOM
    }
    
    /**
     * Constructor
     * @param context Application context
     */
    public MovementAnalysisFeature(Context context) {
        super(context, FEATURE_NAME);
        this.lastUpdateTime = 0;
        this.currentSession = null;
        this.movementHistory = new ArrayList<>();
        this.knownAreas = new HashMap<>();
        this.patternsLibrary = new HashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.isMoving = false;
        this.currentMode = MovementMode.WALKING;
    }
    
    @Override
    public boolean initialize() {
        boolean success = super.initialize();
        if (success) {
            try {
                // Load predefined movement patterns
                loadMovementPatterns();
                
                // Load known map areas
                loadMapAreas();
                
                // Start a new session
                startNewSession();
                
                Log.d(TAG, "Movement analysis system initialized with " +
                      patternsLibrary.size() + " patterns and " +
                      knownAreas.size() + " map areas");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize movement analysis", e);
                return false;
            }
        }
        return false;
    }
    
    @Override
    public void update() {
        if (!isEnabled()) return;
        
        // Check if update is needed
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        
        // Apply security context for this operation
        SecurityContext.getInstance().setCurrentFeatureActive(FEATURE_NAME);
        
        try {
            // Update movement state
            if (currentSession != null) {
                updateMovementState();
                
                // If moving, analyze current movement
                if (isMoving) {
                    analyzeCurrentMovement();
                }
            }
            
            // Update timestamp
            lastUpdateTime = currentTime;
        } catch (Exception e) {
            Log.e(TAG, "Error updating movement analysis", e);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    @Override
    public void shutdown() {
        // End current session if one is active
        if (currentSession != null) {
            endSession();
        }
        
        // Clear listeners
        listeners.clear();
        
        super.shutdown();
    }
    
    /**
     * Start a new movement session
     * @return The new movement session
     */
    public MovementSession startNewSession() {
        // End current session if one exists
        if (currentSession != null) {
            endSession();
        }
        
        // Create new session
        currentSession = new MovementSession();
        
        Log.d(TAG, "Started new movement session");
        
        // Notify listeners
        for (MovementAnalysisListener listener : listeners) {
            listener.onSessionStarted(currentSession);
        }
        
        return currentSession;
    }
    
    /**
     * End current movement session
     * @return The completed movement session
     */
    public MovementSession endSession() {
        if (currentSession == null) {
            Log.w(TAG, "No active movement session to end");
            return null;
        }
        
        // Finalize session
        currentSession.setEndTime(System.currentTimeMillis());
        
        // Add to history
        movementHistory.add(currentSession);
        
        // Trim history if needed
        while (movementHistory.size() > 10) {
            movementHistory.remove(0);
        }
        
        // Analyze completed session
        analyzeMovementSession(currentSession);
        
        Log.d(TAG, "Ended movement session, duration: " + 
              currentSession.getDuration() + "ms, distance: " + 
              currentSession.getTotalDistance());
        
        // Notify listeners
        for (MovementAnalysisListener listener : listeners) {
            listener.onSessionEnded(currentSession);
        }
        
        // Store reference to completed session
        MovementSession completedSession = currentSession;
        
        // Reset current session
        currentSession = null;
        
        return completedSession;
    }
    
    /**
     * Record player position
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate (optional, can be 0 for 2D)
     * @param movementMode Current movement mode
     */
    public void recordPosition(float x, float y, float z, MovementMode movementMode) {
        if (currentSession == null) {
            startNewSession();
        }
        
        // Create position
        Position position = new Position(x, y, z, System.currentTimeMillis());
        
        // Add to current session
        currentSession.addPosition(position);
        
        // Update movement mode
        this.currentMode = movementMode;
        
        // Set moving state
        if (!isMoving) {
            isMoving = true;
            
            // Notify listeners
            for (MovementAnalysisListener listener : listeners) {
                listener.onMovementStarted(position);
            }
        }
        
        // Check for obstacles or points of interest
        checkForPointsOfInterest(position);
        
        // Notify listeners
        for (MovementAnalysisListener listener : listeners) {
            listener.onPositionUpdated(position);
        }
    }
    
    /**
     * Record player position (2D)
     * @param x X coordinate
     * @param y Y coordinate
     * @param movementMode Current movement mode
     */
    public void recordPosition(float x, float y, MovementMode movementMode) {
        recordPosition(x, y, 0, movementMode);
    }
    
    /**
     * Record collision with obstacle
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param obstacleType Type of obstacle
     */
    public void recordObstacle(float x, float y, float z, String obstacleType) {
        if (currentSession == null) {
            return;
        }
        
        // Create obstacle
        Obstacle obstacle = new Obstacle(x, y, z, obstacleType, System.currentTimeMillis());
        
        // Add to current session
        currentSession.addObstacle(obstacle);
        
        Log.d(TAG, "Recorded obstacle: " + obstacleType + " at (" + x + "," + y + "," + z + ")");
        
        // Notify listeners
        for (MovementAnalysisListener listener : listeners) {
            listener.onObstacleEncountered(obstacle);
        }
    }
    
    /**
     * Signal that movement has stopped
     */
    public void stopMovement() {
        if (isMoving) {
            isMoving = false;
            
            // Notify listeners
            for (MovementAnalysisListener listener : listeners) {
                listener.onMovementStopped(currentSession.getLastPosition());
            }
        }
    }
    
    /**
     * Add a movement analysis listener
     * @param listener Listener to add
     */
    public void addListener(MovementAnalysisListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a movement analysis listener
     * @param listener Listener to remove
     */
    public void removeListener(MovementAnalysisListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Add a map area
     * @param area Map area to add
     */
    public void addMapArea(MapArea area) {
        if (area != null) {
            knownAreas.put(area.getId(), area);
            Log.d(TAG, "Added map area: " + area.getName());
        }
    }
    
    /**
     * Get a map area by ID
     * @param areaId Area ID
     * @return Map area or null if not found
     */
    public MapArea getMapArea(String areaId) {
        return knownAreas.get(areaId);
    }
    
    /**
     * Find current map area based on position
     * @param position Current position
     * @return Current map area or null if not in any known area
     */
    public MapArea findCurrentArea(Position position) {
        for (MapArea area : knownAreas.values()) {
            if (area.containsPosition(position)) {
                return area;
            }
        }
        return null;
    }
    
    /**
     * Get current movement session
     * @return Current session
     */
    public MovementSession getCurrentSession() {
        return currentSession;
    }
    
    /**
     * Get current movement mode
     * @return Current movement mode
     */
    public MovementMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Get all movement sessions
     * @return List of all recorded sessions
     */
    public List<MovementSession> getAllSessions() {
        return new ArrayList<>(movementHistory);
    }
    
    /**
     * Check if player is currently moving
     * @return true if moving
     */
    public boolean isMoving() {
        return isMoving;
    }
    
    /**
     * Get current speed in units per second
     * @return Current speed
     */
    public float getCurrentSpeed() {
        if (currentSession == null || !isMoving) {
            return 0.0f;
        }
        
        return currentSession.getCurrentSpeed();
    }
    
    /**
     * Get average speed for current session
     * @return Average speed
     */
    public float getAverageSpeed() {
        if (currentSession == null) {
            return 0.0f;
        }
        
        return currentSession.getAverageSpeed();
    }
    
    /**
     * Get optimal path between two points
     * @param start Start position
     * @param end End position
     * @return List of positions forming the path
     */
    public List<Position> getOptimalPath(Position start, Position end) {
        // Simple implementation for now
        // In a real implementation, this would use pathfinding algorithms
        List<Position> path = new ArrayList<>();
        path.add(start);
        path.add(end);
        return path;
    }
    
    /**
     * Get movement efficiency rating (0-1)
     * @return Efficiency rating
     */
    public float getMovementEfficiency() {
        if (currentSession == null) {
            return 0.0f;
        }
        
        return currentSession.getEfficiencyRating();
    }
    
    /**
     * Get recommended movement mode for current situation
     * @return Recommended movement mode
     */
    public MovementMode getRecommendedMovementMode() {
        // This would analyze current context and recommend best movement mode
        // For now, return current mode as recommendation
        return currentMode;
    }
    
    /**
     * Load predefined movement patterns
     */
    private void loadMovementPatterns() {
        // This would load from storage or predefined data
        // For now, add some sample patterns
        
        // Zigzag pattern
        MovementPattern zigzag = new MovementPattern("zigzag");
        zigzag.setDescription("Zigzag movement pattern for avoiding projectiles");
        patternsLibrary.put(zigzag.getId(), zigzag);
        
        // Circle strafe pattern
        MovementPattern circleStrafe = new MovementPattern("circle_strafe");
        circleStrafe.setDescription("Circular movement around a target while facing it");
        patternsLibrary.put(circleStrafe.getId(), circleStrafe);
        
        // Bunny hop pattern
        MovementPattern bunnyHop = new MovementPattern("bunny_hop");
        bunnyHop.setDescription("Timed jumps to maintain momentum and speed");
        patternsLibrary.put(bunnyHop.getId(), bunnyHop);
    }
    
    /**
     * Load known map areas
     */
    private void loadMapAreas() {
        // This would load from storage or predefined data
        // For now, leave empty
    }
    
    /**
     * Update movement state
     */
    private void updateMovementState() {
        if (currentSession == null) {
            return;
        }
        
        // Check if enough positions to analyze
        if (currentSession.getPositions().size() < 2) {
            return;
        }
        
        // Calculate current speed
        float speed = currentSession.getCurrentSpeed();
        
        // If speed is very low, consider not moving
        if (speed < 0.1f && isMoving) {
            stopMovement();
        }
    }
    
    /**
     * Analyze current movement
     */
    private void analyzeCurrentMovement() {
        if (currentSession == null) {
            return;
        }
        
        // Calculate metrics
        currentSession.updateMetrics();
        
        // Detect movement patterns
        MovementPattern detectedPattern = detectMovementPattern();
        
        if (detectedPattern != null) {
            // Notify listeners of detected pattern
            for (MovementAnalysisListener listener : listeners) {
                listener.onPatternDetected(detectedPattern);
            }
        }
        
        // Check if current area has changed
        MapArea currentArea = findCurrentArea(currentSession.getLastPosition());
        if (currentArea != null && !currentArea.equals(currentSession.getCurrentArea())) {
            currentSession.setCurrentArea(currentArea);
            
            // Notify listeners of area change
            for (MovementAnalysisListener listener : listeners) {
                listener.onAreaEntered(currentArea);
            }
        }
    }
    
    /**
     * Analyze a completed movement session
     * @param session The session to analyze
     */
    private void analyzeMovementSession(MovementSession session) {
        if (session == null) {
            return;
        }
        
        // Calculate final metrics
        session.calculateFinalMetrics();
        
        // Look for patterns that could improve movement
        // This would analyze the session for inefficiencies
        
        Log.d(TAG, "Movement session analysis complete. Efficiency: " + 
              session.getEfficiencyRating() + ", Average speed: " + 
              session.getAverageSpeed());
    }
    
    /**
     * Detect movement pattern in current session
     * @return Detected pattern or null if none detected
     */
    private MovementPattern detectMovementPattern() {
        // This would analyze recent positions to detect patterns
        // For now, return null (no pattern detected)
        return null;
    }
    
    /**
     * Check for points of interest near position
     * @param position Current position
     */
    private void checkForPointsOfInterest(Position position) {
        // Check all known areas for points of interest
        for (MapArea area : knownAreas.values()) {
            if (area.containsPosition(position)) {
                for (PointOfInterest poi : area.getPointsOfInterest()) {
                    if (poi.isNearby(position)) {
                        // Notify listeners
                        for (MovementAnalysisListener listener : listeners) {
                            listener.onPointOfInterestNearby(poi);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Position class
     * Represents a 3D position with timestamp
     */
    public static class Position {
        private final float x;
        private final float y;
        private final float z;
        private final long timestamp;
        
        /**
         * Constructor
         * @param x X coordinate
         * @param y Y coordinate
         * @param z Z coordinate
         * @param timestamp Time when position was recorded
         */
        public Position(float x, float y, float z, long timestamp) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestamp = timestamp;
        }
        
        /**
         * Get X coordinate
         * @return X coordinate
         */
        public float getX() {
            return x;
        }
        
        /**
         * Get Y coordinate
         * @return Y coordinate
         */
        public float getY() {
            return y;
        }
        
        /**
         * Get Z coordinate
         * @return Z coordinate
         */
        public float getZ() {
            return z;
        }
        
        /**
         * Get timestamp
         * @return Timestamp in milliseconds
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        /**
         * Calculate distance to another position
         * @param other Other position
         * @return Distance
         */
        public float distanceTo(Position other) {
            float dx = other.x - this.x;
            float dy = other.y - this.y;
            float dz = other.z - this.z;
            return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
        
        /**
         * Calculate 2D distance (ignoring Z)
         * @param other Other position
         * @return 2D distance
         */
        public float distance2D(Position other) {
            float dx = other.x - this.x;
            float dy = other.y - this.y;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ", " + z + ")";
        }
    }
    
    /**
     * Obstacle class
     * Represents an obstacle encountered during movement
     */
    public static class Obstacle {
        private final float x;
        private final float y;
        private final float z;
        private final String type;
        private final long timestamp;
        
        /**
         * Constructor
         * @param x X coordinate
         * @param y Y coordinate
         * @param z Z coordinate
         * @param type Obstacle type
         * @param timestamp Time when obstacle was encountered
         */
        public Obstacle(float x, float y, float z, String type, long timestamp) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = type;
            this.timestamp = timestamp;
        }
        
        /**
         * Get X coordinate
         * @return X coordinate
         */
        public float getX() {
            return x;
        }
        
        /**
         * Get Y coordinate
         * @return Y coordinate
         */
        public float getY() {
            return y;
        }
        
        /**
         * Get Z coordinate
         * @return Z coordinate
         */
        public float getZ() {
            return z;
        }
        
        /**
         * Get obstacle type
         * @return Obstacle type
         */
        public String getType() {
            return type;
        }
        
        /**
         * Get timestamp
         * @return Timestamp in milliseconds
         */
        public long getTimestamp() {
            return timestamp;
        }
        
        /**
         * Get position of obstacle
         * @return Position
         */
        public Position getPosition() {
            return new Position(x, y, z, timestamp);
        }
    }
    
    /**
     * Movement Session class
     * Represents a continuous movement session
     */
    public static class MovementSession {
        private final long startTime;
        private long endTime;
        private final List<Position> positions;
        private final List<Obstacle> obstacles;
        private MapArea currentArea;
        private float totalDistance;
        private float averageSpeed;
        private float efficiencyRating;
        private final Map<String, Float> metrics;
        
        /**
         * Constructor
         */
        public MovementSession() {
            this.startTime = System.currentTimeMillis();
            this.endTime = 0;
            this.positions = new ArrayList<>();
            this.obstacles = new ArrayList<>();
            this.currentArea = null;
            this.totalDistance = 0.0f;
            this.averageSpeed = 0.0f;
            this.efficiencyRating = 0.0f;
            this.metrics = new HashMap<>();
        }
        
        /**
         * Add a position to the session
         * @param position Position to add
         */
        public void addPosition(Position position) {
            // Calculate distance from previous position
            if (!positions.isEmpty()) {
                Position prev = positions.get(positions.size() - 1);
                float distance = prev.distanceTo(position);
                totalDistance += distance;
            }
            
            // Add to positions list
            positions.add(position);
            
            // Trim list if too long
            while (positions.size() > MAX_POSITION_HISTORY) {
                positions.remove(0);
            }
        }
        
        /**
         * Add an obstacle to the session
         * @param obstacle Obstacle to add
         */
        public void addObstacle(Obstacle obstacle) {
            obstacles.add(obstacle);
        }
        
        /**
         * Set current map area
         * @param area Current area
         */
        public void setCurrentArea(MapArea area) {
            this.currentArea = area;
        }
        
        /**
         * Get current map area
         * @return Current area
         */
        public MapArea getCurrentArea() {
            return currentArea;
        }
        
        /**
         * Set end time
         * @param endTime End time in milliseconds
         */
        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }
        
        /**
         * Get all positions
         * @return List of positions
         */
        public List<Position> getPositions() {
            return new ArrayList<>(positions);
        }
        
        /**
         * Get all obstacles
         * @return List of obstacles
         */
        public List<Obstacle> getObstacles() {
            return new ArrayList<>(obstacles);
        }
        
        /**
         * Get last recorded position
         * @return Last position or null if none
         */
        public Position getLastPosition() {
            if (positions.isEmpty()) {
                return null;
            }
            return positions.get(positions.size() - 1);
        }
        
        /**
         * Get total distance traveled
         * @return Total distance
         */
        public float getTotalDistance() {
            return totalDistance;
        }
        
        /**
         * Get average speed
         * @return Average speed in units per second
         */
        public float getAverageSpeed() {
            return averageSpeed;
        }
        
        /**
         * Get current speed
         * @return Current speed in units per second
         */
        public float getCurrentSpeed() {
            // Need at least 2 positions to calculate speed
            if (positions.size() < 2) {
                return 0.0f;
            }
            
            // Get last two positions
            Position current = positions.get(positions.size() - 1);
            Position previous = positions.get(positions.size() - 2);
            
            // Calculate time difference in seconds
            float timeDiff = (current.getTimestamp() - previous.getTimestamp()) / 1000.0f;
            if (timeDiff <= 0) {
                return 0.0f;
            }
            
            // Calculate distance
            float distance = previous.distanceTo(current);
            
            // Calculate speed
            return distance / timeDiff;
        }
        
        /**
         * Get session duration
         * @return Duration in milliseconds
         */
        public long getDuration() {
            if (endTime > 0) {
                return endTime - startTime;
            } else {
                return System.currentTimeMillis() - startTime;
            }
        }
        
        /**
         * Get efficiency rating
         * @return Efficiency rating (0-1)
         */
        public float getEfficiencyRating() {
            return efficiencyRating;
        }
        
        /**
         * Update metrics for ongoing session
         */
        public void updateMetrics() {
            // Calculate current metrics
            
            // Current speed
            float currentSpeed = getCurrentSpeed();
            metrics.put("current_speed", currentSpeed);
            
            // Current acceleration (if enough positions)
            if (positions.size() >= 3) {
                Position p1 = positions.get(positions.size() - 3);
                Position p2 = positions.get(positions.size() - 2);
                Position p3 = positions.get(positions.size() - 1);
                
                float t1 = (p2.getTimestamp() - p1.getTimestamp()) / 1000.0f;
                float t2 = (p3.getTimestamp() - p2.getTimestamp()) / 1000.0f;
                
                float s1 = p1.distanceTo(p2) / t1;
                float s2 = p2.distanceTo(p3) / t2;
                
                float acceleration = (s2 - s1) / ((t1 + t2) / 2);
                metrics.put("current_acceleration", acceleration);
            }
            
            // Update efficiency rating (simplified)
            if (positions.size() >= 2) {
                // Calculate direct distance from first to last position
                Position first = positions.get(0);
                Position last = positions.get(positions.size() - 1);
                float directDistance = first.distanceTo(last);
                
                // Calculate efficiency as ratio of direct distance to total distance
                // Higher ratio means more efficient (straighter path)
                if (totalDistance > 0) {
                    efficiencyRating = Math.min(1.0f, directDistance / totalDistance);
                    metrics.put("efficiency_rating", efficiencyRating);
                }
            }
        }
        
        /**
         * Calculate final metrics for completed session
         */
        public void calculateFinalMetrics() {
            if (positions.isEmpty()) {
                return;
            }
            
            // Calculate duration in seconds
            float durationSeconds = getDuration() / 1000.0f;
            
            // Calculate average speed
            if (durationSeconds > 0) {
                averageSpeed = totalDistance / durationSeconds;
            }
            
            // Calculate efficiency
            if (positions.size() >= 2) {
                Position first = positions.get(0);
                Position last = positions.get(positions.size() - 1);
                float directDistance = first.distanceTo(last);
                
                if (totalDistance > 0) {
                    efficiencyRating = Math.min(1.0f, directDistance / totalDistance);
                }
            }
            
            // Store metrics
            metrics.put("total_distance", totalDistance);
            metrics.put("average_speed", averageSpeed);
            metrics.put("duration_seconds", durationSeconds);
            metrics.put("efficiency_rating", efficiencyRating);
            metrics.put("position_count", (float) positions.size());
            metrics.put("obstacle_count", (float) obstacles.size());
        }
        
        /**
         * Get a specific metric
         * @param name Metric name
         * @param defaultValue Default value if metric not found
         * @return Metric value
         */
        public float getMetric(String name, float defaultValue) {
            return metrics.getOrDefault(name, defaultValue);
        }
        
        /**
         * Get all metrics
         * @return Map of all metrics
         */
        public Map<String, Float> getAllMetrics() {
            return new HashMap<>(metrics);
        }
    }
    
    /**
     * Movement Pattern class
     * Represents a recognizable movement pattern
     */
    public static class MovementPattern {
        private final String id;
        private String description;
        private Map<String, Object> attributes;
        
        /**
         * Constructor
         * @param id Pattern ID
         */
        public MovementPattern(String id) {
            this.id = id;
            this.description = "";
            this.attributes = new HashMap<>();
        }
        
        /**
         * Get pattern ID
         * @return Pattern ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get pattern description
         * @return Description
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * Set pattern description
         * @param description Description
         */
        public void setDescription(String description) {
            this.description = description;
        }
        
        /**
         * Set an attribute
         * @param key Attribute key
         * @param value Attribute value
         */
        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }
        
        /**
         * Get an attribute
         * @param key Attribute key
         * @return Attribute value or null if not found
         */
        public Object getAttribute(String key) {
            return attributes.get(key);
        }
        
        /**
         * Get all attributes
         * @return Map of all attributes
         */
        public Map<String, Object> getAllAttributes() {
            return new HashMap<>(attributes);
        }
    }
    
    /**
     * Map Area class
     * Represents a defined area on the map
     */
    public static class MapArea {
        private final String id;
        private String name;
        private final List<PointOfInterest> pointsOfInterest;
        private float minX, minY, minZ;
        private float maxX, maxY, maxZ;
        private Map<String, Object> properties;
        
        /**
         * Constructor
         * @param id Area ID
         * @param name Area name
         */
        public MapArea(String id, String name) {
            this.id = id;
            this.name = name;
            this.pointsOfInterest = new ArrayList<>();
            this.properties = new HashMap<>();
        }
        
        /**
         * Set area bounds
         * @param minX Minimum X
         * @param minY Minimum Y
         * @param minZ Minimum Z
         * @param maxX Maximum X
         * @param maxY Maximum Y
         * @param maxZ Maximum Z
         */
        public void setBounds(float minX, float minY, float minZ, 
                              float maxX, float maxY, float maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }
        
        /**
         * Add a point of interest
         * @param poi Point of interest
         */
        public void addPointOfInterest(PointOfInterest poi) {
            pointsOfInterest.add(poi);
        }
        
        /**
         * Check if position is inside this area
         * @param position Position to check
         * @return true if inside
         */
        public boolean containsPosition(Position position) {
            return position.getX() >= minX && position.getX() <= maxX &&
                   position.getY() >= minY && position.getY() <= maxY &&
                   position.getZ() >= minZ && position.getZ() <= maxZ;
        }
        
        /**
         * Get area ID
         * @return Area ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get area name
         * @return Area name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set area name
         * @param name Area name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get all points of interest
         * @return List of points of interest
         */
        public List<PointOfInterest> getPointsOfInterest() {
            return new ArrayList<>(pointsOfInterest);
        }
        
        /**
         * Set a property
         * @param key Property key
         * @param value Property value
         */
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
        
        /**
         * Get a property
         * @param key Property key
         * @return Property value or null if not found
         */
        public Object getProperty(String key) {
            return properties.get(key);
        }
        
        /**
         * Get all properties
         * @return Map of all properties
         */
        public Map<String, Object> getAllProperties() {
            return new HashMap<>(properties);
        }
    }
    
    /**
     * Point of Interest class
     * Represents a notable location on the map
     */
    public static class PointOfInterest {
        private final String id;
        private String name;
        private String type;
        private final float x;
        private final float y;
        private final float z;
        private float radius;
        private Map<String, Object> properties;
        
        /**
         * Constructor
         * @param id Point ID
         * @param name Point name
         * @param type Point type
         * @param x X coordinate
         * @param y Y coordinate
         * @param z Z coordinate
         * @param radius Detection radius
         */
        public PointOfInterest(String id, String name, String type, 
                              float x, float y, float z, float radius) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
            this.properties = new HashMap<>();
        }
        
        /**
         * Check if position is near this point
         * @param position Position to check
         * @return true if within radius
         */
        public boolean isNearby(Position position) {
            float dx = position.getX() - x;
            float dy = position.getY() - y;
            float dz = position.getZ() - z;
            float distanceSquared = dx * dx + dy * dy + dz * dz;
            return distanceSquared <= radius * radius;
        }
        
        /**
         * Get point ID
         * @return Point ID
         */
        public String getId() {
            return id;
        }
        
        /**
         * Get point name
         * @return Point name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Set point name
         * @param name Point name
         */
        public void setName(String name) {
            this.name = name;
        }
        
        /**
         * Get point type
         * @return Point type
         */
        public String getType() {
            return type;
        }
        
        /**
         * Set point type
         * @param type Point type
         */
        public void setType(String type) {
            this.type = type;
        }
        
        /**
         * Get X coordinate
         * @return X coordinate
         */
        public float getX() {
            return x;
        }
        
        /**
         * Get Y coordinate
         * @return Y coordinate
         */
        public float getY() {
            return y;
        }
        
        /**
         * Get Z coordinate
         * @return Z coordinate
         */
        public float getZ() {
            return z;
        }
        
        /**
         * Get position
         * @return Position
         */
        public Position getPosition() {
            return new Position(x, y, z, 0);
        }
        
        /**
         * Get detection radius
         * @return Radius
         */
        public float getRadius() {
            return radius;
        }
        
        /**
         * Set detection radius
         * @param radius Radius
         */
        public void setRadius(float radius) {
            this.radius = radius;
        }
        
        /**
         * Set a property
         * @param key Property key
         * @param value Property value
         */
        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
        
        /**
         * Get a property
         * @param key Property key
         * @return Property value or null if not found
         */
        public Object getProperty(String key) {
            return properties.get(key);
        }
        
        /**
         * Get all properties
         * @return Map of all properties
         */
        public Map<String, Object> getAllProperties() {
            return new HashMap<>(properties);
        }
    }
    
    /**
     * Movement Analysis Listener interface
     * For receiving movement events
     */
    public interface MovementAnalysisListener {
        /**
         * Called when a movement session starts
         * @param session New movement session
         */
        void onSessionStarted(MovementSession session);
        
        /**
         * Called when a movement session ends
         * @param session Completed movement session
         */
        void onSessionEnded(MovementSession session);
        
        /**
         * Called when movement starts
         * @param position Starting position
         */
        void onMovementStarted(Position position);
        
        /**
         * Called when movement stops
         * @param position Stopping position
         */
        void onMovementStopped(Position position);
        
        /**
         * Called when position is updated
         * @param position New position
         */
        void onPositionUpdated(Position position);
        
        /**
         * Called when an obstacle is encountered
         * @param obstacle Encountered obstacle
         */
        void onObstacleEncountered(Obstacle obstacle);
        
        /**
         * Called when a movement pattern is detected
         * @param pattern Detected pattern
         */
        void onPatternDetected(MovementPattern pattern);
        
        /**
         * Called when entering a new map area
         * @param area Entered area
         */
        void onAreaEntered(MapArea area);
        
        /**
         * Called when near a point of interest
         * @param poi Nearby point of interest
         */
        void onPointOfInterestNearby(PointOfInterest poi);
    }
}
