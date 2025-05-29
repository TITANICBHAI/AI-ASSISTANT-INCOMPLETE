
package com.aiassistant.core.gaming.vision;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;

import com.aiassistant.core.gaming.GameEntity;
import com.aiassistant.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Predictive vision model that uses historical data to predict future positions
 * and movements of entities in games
 */
public class PredictiveVisionModel {
    private static final String TAG = "PredictiveVisionModel";
    
    // Movement tracking
    private Map<String, List<PositionHistory>> entityPositionHistory = new HashMap<>();
    private static final int MAX_HISTORY_ENTRIES = 120; // 4 seconds at 30fps
    private static final long MAX_HISTORY_AGE = 10000; // 10 seconds
    
    // Movement patterns
    private Map<String, MovementPattern> detectedPatterns = new HashMap<>();
    
    /**
     * Class to store position history
     */
    private static class PositionHistory {
        public final PointF position;
        public final PointF velocity;
        public final long timestamp;
        public final float confidence;
        
        public PositionHistory(PointF position, PointF velocity, long timestamp, float confidence) {
            this.position = position;
            this.velocity = velocity;
            this.timestamp = timestamp;
            this.confidence = confidence;
        }
    }
    
    /**
     * Class to represent a movement pattern
     */
    private static class MovementPattern {
        public final String type; // LINEAR, CIRCULAR, ERRATIC, etc.
        public final float confidence;
        public final long lastUpdated;
        public final Map<String, Object> parameters; // Pattern-specific parameters
        
        public MovementPattern(String type, float confidence, Map<String, Object> parameters) {
            this.type = type;
            this.confidence = confidence;
            this.lastUpdated = System.currentTimeMillis();
            this.parameters = parameters;
        }
    }
    
    /**
     * Track entity position
     * @param entityId Entity ID
     * @param position Current position
     * @param timestamp Timestamp
     * @param confidence Detection confidence
     */
    public void trackEntityPosition(String entityId, PointF position, long timestamp, float confidence) {
        // Get or create history list
        List<PositionHistory> history = entityPositionHistory.getOrDefault(entityId, new ArrayList<>());
        
        // Calculate velocity if we have previous positions
        PointF velocity = new PointF(0, 0);
        
        if (!history.isEmpty()) {
            PositionHistory prev = history.get(history.size() - 1);
            long timeDelta = timestamp - prev.timestamp;
            
            if (timeDelta > 0) {
                float vx = (position.x - prev.position.x) / timeDelta * 1000; // px/s
                float vy = (position.y - prev.position.y) / timeDelta * 1000; // px/s
                velocity = new PointF(vx, vy);
            }
        }
        
        // Add to history
        history.add(new PositionHistory(position, velocity, timestamp, confidence));
        
        // Limit history size
        while (history.size() > MAX_HISTORY_ENTRIES) {
            history.remove(0);
        }
        
        // Clean up old entries
        long oldestAllowed = timestamp - MAX_HISTORY_AGE;
        history.removeIf(entry -> entry.timestamp < oldestAllowed);
        
        // Update history map
        entityPositionHistory.put(entityId, history);
        
        // Update movement pattern
        updateMovementPattern(entityId, history);
    }
    
    /**
     * Clear tracking for an entity
     * @param entityId Entity ID
     */
    public void clearEntityTracking(String entityId) {
        entityPositionHistory.remove(entityId);
        detectedPatterns.remove(entityId);
    }
    
    /**
     * Predict future position of entity
     * @param entityId Entity ID
     * @param timeAheadMs Time ahead to predict (ms)
     * @return Predicted position or null if prediction not possible
     */
    public PointF predictPosition(String entityId, long timeAheadMs) {
        List<PositionHistory> history = entityPositionHistory.get(entityId);
        
        if (history == null || history.size() < 3) {
            return null; // Not enough history for prediction
        }
        
        // Get latest position
        PositionHistory latest = history.get(history.size() - 1);
        
        // Check if we have a pattern for this entity
        MovementPattern pattern = detectedPatterns.get(entityId);
        
        if (pattern != null && pattern.confidence > 0.6) {
            // Use pattern-based prediction
            return predictUsingPattern(entityId, timeAheadMs, pattern, latest);
        } else {
            // Use velocity-based prediction (linear)
            return predictLinear(latest, timeAheadMs);
        }
    }
    
    /**
     * Predict using detected movement pattern
     * @param entityId Entity ID
     * @param timeAheadMs Time ahead to predict (ms)
     * @param pattern Detected movement pattern
     * @param latest Latest position history
     * @return Predicted position
     */
    private PointF predictUsingPattern(String entityId, long timeAheadMs, MovementPattern pattern, PositionHistory latest) {
        switch (pattern.type) {
            case "LINEAR":
                return predictLinear(latest, timeAheadMs);
                
            case "CIRCULAR":
                return predictCircular(pattern, latest, timeAheadMs);
                
            case "ZIGZAG":
                return predictZigzag(pattern, latest, timeAheadMs);
                
            case "STOPPING":
                return predictStopping(pattern, latest, timeAheadMs);
                
            default:
                // Fallback to linear prediction
                return predictLinear(latest, timeAheadMs);
        }
    }
    
    /**
     * Linear prediction based on current velocity
     * @param latest Latest position history
     * @param timeAheadMs Time ahead to predict (ms)
     * @return Predicted position
     */
    private PointF predictLinear(PositionHistory latest, long timeAheadMs) {
        // Simple linear prediction
        float timeFactor = timeAheadMs / 1000f; // Convert to seconds
        
        float predictedX = latest.position.x + (latest.velocity.x * timeFactor);
        float predictedY = latest.position.y + (latest.velocity.y * timeFactor);
        
        return new PointF(predictedX, predictedY);
    }
    
    /**
     * Circular movement prediction
     * @param pattern Movement pattern
     * @param latest Latest position history
     * @param timeAheadMs Time ahead to predict (ms)
     * @return Predicted position
     */
    private PointF predictCircular(MovementPattern pattern, PositionHistory latest, long timeAheadMs) {
        // Extract pattern parameters
        PointF center = (PointF) pattern.parameters.getOrDefault("center", latest.position);
        float radius = (float) pattern.parameters.getOrDefault("radius", 100f);
        float angularVelocity = (float) pattern.parameters.getOrDefault("angularVelocity", 1f);
        float currentAngle = (float) pattern.parameters.getOrDefault("currentAngle", 0f);
        
        // Calculate time in seconds
        float timeInSeconds = timeAheadMs / 1000f;
        
        // Calculate new angle
        float newAngle = currentAngle + (angularVelocity * timeInSeconds);
        
        // Calculate new position
        float predictedX = center.x + radius * (float) Math.cos(newAngle);
        float predictedY = center.y + radius * (float) Math.sin(newAngle);
        
        return new PointF(predictedX, predictedY);
    }
    
    /**
     * Zigzag movement prediction
     * @param pattern Movement pattern
     * @param latest Latest position history
     * @param timeAheadMs Time ahead to predict (ms)
     * @return Predicted position
     */
    private PointF predictZigzag(MovementPattern pattern, PositionHistory latest, long timeAheadMs) {
        // Extract pattern parameters
        float baseVelocityX = (float) pattern.parameters.getOrDefault("baseVelocityX", latest.velocity.x);
        float baseVelocityY = (float) pattern.parameters.getOrDefault("baseVelocityY", latest.velocity.y);
        float amplitude = (float) pattern.parameters.getOrDefault("amplitude", 50f);
        float frequency = (float) pattern.parameters.getOrDefault("frequency", 0.5f);
        float phase = (float) pattern.parameters.getOrDefault("phase", 0f);
        
        // Calculate time in seconds
        float timeInSeconds = timeAheadMs / 1000f;
        
        // Calculate new position
        float baseX = latest.position.x + (baseVelocityX * timeInSeconds);
        float baseY = latest.position.y + (baseVelocityY * timeInSeconds);
        
        // Add zigzag offset (perpendicular to movement direction)
        float moveAngle = (float) Math.atan2(baseVelocityY, baseVelocityX);
        float perpAngle = moveAngle + (float) (Math.PI / 2);
        
        float newPhase = phase + (frequency * timeInSeconds);
        float offset = amplitude * (float) Math.sin(newPhase);
        
        float offsetX = offset * (float) Math.cos(perpAngle);
        float offsetY = offset * (float) Math.sin(perpAngle);
        
        return new PointF(baseX + offsetX, baseY + offsetY);
    }
    
    /**
     * Stopping movement prediction (decelerating)
     * @param pattern Movement pattern
     * @param latest Latest position history
     * @param timeAheadMs Time ahead to predict (ms)
     * @return Predicted position
     */
    private PointF predictStopping(MovementPattern pattern, PositionHistory latest, long timeAheadMs) {
        // Extract pattern parameters
        float deceleration = (float) pattern.parameters.getOrDefault("deceleration", 500f); // px/s²
        
        // Calculate time in seconds
        float timeInSeconds = timeAheadMs / 1000f;
        
        // Calculate initial velocity magnitude
        float velocityMagnitude = (float) Math.sqrt(
            latest.velocity.x * latest.velocity.x + 
            latest.velocity.y * latest.velocity.y
        );
        
        // Calculate time to stop
        float timeToStop = velocityMagnitude / deceleration;
        
        // Calculate normalized velocity components
        float vxNorm = 0f;
        float vyNorm = 0f;
        
        if (velocityMagnitude > 0) {
            vxNorm = latest.velocity.x / velocityMagnitude;
            vyNorm = latest.velocity.y / velocityMagnitude;
        }
        
        // Calculate predicted position
        float predictedX, predictedY;
        
        if (timeInSeconds >= timeToStop) {
            // Entity will have stopped
            float distanceX = 0.5f * latest.velocity.x * timeToStop;
            float distanceY = 0.5f * latest.velocity.y * timeToStop;
            
            predictedX = latest.position.x + distanceX;
            predictedY = latest.position.y + distanceY;
        } else {
            // Entity is still decelerating
            float newVelocityMagnitude = velocityMagnitude - (deceleration * timeInSeconds);
            float avgVelocityMagnitude = (velocityMagnitude + newVelocityMagnitude) / 2;
            
            float distanceX = avgVelocityMagnitude * vxNorm * timeInSeconds;
            float distanceY = avgVelocityMagnitude * vyNorm * timeInSeconds;
            
            predictedX = latest.position.x + distanceX;
            predictedY = latest.position.y + distanceY;
        }
        
        return new PointF(predictedX, predictedY);
    }
    
    /**
     * Update movement pattern for entity
     * @param entityId Entity ID
     * @param history Position history
     */
    private void updateMovementPattern(String entityId, List<PositionHistory> history) {
        if (history.size() < 10) {
            return; // Not enough history to detect patterns
        }
        
        // Get last 10 entries
        List<PositionHistory> recentHistory = new ArrayList<>(history.subList(
            Math.max(0, history.size() - 10), history.size()));
        
        // Check for pattern types
        Pair<String, Float> linearConfidence = checkLinearMovement(recentHistory);
        Pair<String, Float> circularConfidence = checkCircularMovement(recentHistory);
        Pair<String, Float> zigzagConfidence = checkZigzagMovement(recentHistory);
        Pair<String, Float> stoppingConfidence = checkStoppingMovement(recentHistory);
        
        // Find highest confidence pattern
        String patternType = null;
        float highestConfidence = 0f;
        
        if (linearConfidence.second > highestConfidence) {
            patternType = "LINEAR";
            highestConfidence = linearConfidence.second;
        }
        
        if (circularConfidence.second > highestConfidence) {
            patternType = "CIRCULAR";
            highestConfidence = circularConfidence.second;
        }
        
        if (zigzagConfidence.second > highestConfidence) {
            patternType = "ZIGZAG";
            highestConfidence = zigzagConfidence.second;
        }
        
        if (stoppingConfidence.second > highestConfidence) {
            patternType = "STOPPING";
            highestConfidence = stoppingConfidence.second;
        }
        
        // If no strong pattern, default to linear
        if (patternType == null || highestConfidence < 0.4) {
            patternType = "LINEAR";
            highestConfidence = 0.5f;
        }
        
        // Create pattern parameters
        Map<String, Object> parameters = new HashMap<>();
        
        switch (patternType) {
            case "LINEAR":
                // No special parameters needed
                break;
                
            case "CIRCULAR":
                parameters = calculateCircularParameters(recentHistory);
                break;
                
            case "ZIGZAG":
                parameters = calculateZigzagParameters(recentHistory);
                break;
                
            case "STOPPING":
                parameters = calculateStoppingParameters(recentHistory);
                break;
        }
        
        // Update pattern
        detectedPatterns.put(entityId, new MovementPattern(patternType, highestConfidence, parameters));
    }
    
    /**
     * Check for linear movement pattern
     * @param history Recent position history
     * @return Pattern type and confidence
     */
    private Pair<String, Float> checkLinearMovement(List<PositionHistory> history) {
        // Calculate average velocity and its variance
        PointF avgVelocity = new PointF(0, 0);
        
        for (PositionHistory entry : history) {
            avgVelocity.x += entry.velocity.x;
            avgVelocity.y += entry.velocity.y;
        }
        
        avgVelocity.x /= history.size();
        avgVelocity.y /= history.size();
        
        // Calculate variance
        float velocityVariance = 0;
        
        for (PositionHistory entry : history) {
            float dx = entry.velocity.x - avgVelocity.x;
            float dy = entry.velocity.y - avgVelocity.y;
            velocityVariance += (dx * dx + dy * dy);
        }
        
        velocityVariance /= history.size();
        
        // Calculate velocity magnitude
        float velocityMagnitude = (float) Math.sqrt(
            avgVelocity.x * avgVelocity.x + avgVelocity.y * avgVelocity.y);
        
        // If velocity magnitude is very low, not moving linearly
        if (velocityMagnitude < 50) {
            return new Pair<>("LINEAR", 0.2f);
        }
        
        // Calculate normalized variance
        float normalizedVariance = velocityVariance / (velocityMagnitude * velocityMagnitude);
        
        // Convert variance to confidence (lower variance = higher confidence)
        float confidence = 1.0f - Math.min(1.0f, normalizedVariance / 0.5f);
        
        return new Pair<>("LINEAR", confidence);
    }
    
    /**
     * Check for circular movement pattern
     * @param history Recent position history
     * @return Pattern type and confidence
     */
    private Pair<String, Float> checkCircularMovement(List<PositionHistory> history) {
        if (history.size() < 6) {
            return new Pair<>("CIRCULAR", 0.0f);
        }
        
        // Try to fit a circle to the points
        Map<String, Object> circleParams = fitCircle(history);
        PointF center = (PointF) circleParams.get("center");
        float radius = (float) circleParams.get("radius");
        float error = (float) circleParams.get("error");
        
        // Calculate angular changes
        float totalAngleChange = 0;
        PointF prevPoint = null;
        PointF prevVector = null;
        
        for (PositionHistory entry : history) {
            PointF point = entry.position;
            
            // Calculate vector from center to point
            PointF vector = new PointF(point.x - center.x, point.y - center.y);
            
            if (prevPoint != null && prevVector != null) {
                // Calculate angle between vectors
                float angle = angleBetweenVectors(prevVector, vector);
                totalAngleChange += angle;
            }
            
            prevPoint = point;
            prevVector = vector;
        }
        
        // For a full circle, total angle change would be close to 2π
        // We want to see if there's consistent rotation
        float absAngleChange = Math.abs(totalAngleChange);
        
        // Calculate confidence based on error and angle change
        float confidence = 0.0f;
        
        // If points roughly follow a circle
        if (error < 0.3) {
            // Higher confidence if there's significant angle change
            if (absAngleChange > Math.PI / 2) {
                confidence = 0.7f - error;
            } else {
                confidence = 0.4f - error;
            }
        }
        
        return new Pair<>("CIRCULAR", Math.max(0, confidence));
    }
    
    /**
     * Check for zigzag movement pattern
     * @param history Recent position history
     * @return Pattern type and confidence
     */
    private Pair<String, Float> checkZigzagMovement(List<PositionHistory> history) {
        if (history.size() < 6) {
            return new Pair<>("ZIGZAG", 0.0f);
        }
        
        // Calculate average movement direction
        PointF avgDirection = new PointF(0, 0);
        
        for (PositionHistory entry : history) {
            float velMagnitude = (float) Math.sqrt(
                entry.velocity.x * entry.velocity.x + entry.velocity.y * entry.velocity.y);
            
            if (velMagnitude > 0) {
                avgDirection.x += entry.velocity.x / velMagnitude;
                avgDirection.y += entry.velocity.y / velMagnitude;
            }
        }
        
        avgDirection.x /= history.size();
        avgDirection.y /= history.size();
        
        // Normalize
        float dirMagnitude = (float) Math.sqrt(
            avgDirection.x * avgDirection.x + avgDirection.y * avgDirection.y);
        
        if (dirMagnitude > 0) {
            avgDirection.x /= dirMagnitude;
            avgDirection.y /= dirMagnitude;
        }
        
        // Calculate perpendicular direction
        PointF perpDirection = new PointF(-avgDirection.y, avgDirection.x);
        
        // Check for oscillation along perpendicular direction
        float[] projections = new float[history.size()];
        
        for (int i = 0; i < history.size(); i++) {
            PointF pos = history.get(i).position;
            PointF origin = history.get(0).position;
            
            float dx = pos.x - origin.x;
            float dy = pos.y - origin.y;
            
            // Project onto perpendicular direction
            projections[i] = dx * perpDirection.x + dy * perpDirection.y;
        }
        
        // Count direction changes
        int directionChanges = 0;
        float prevDiff = 0;
        
        for (int i = 1; i < projections.length; i++) {
            float diff = projections[i] - projections[i - 1];
            
            if (prevDiff != 0 && diff * prevDiff < 0) {
                directionChanges++;
            }
            
            prevDiff = diff;
        }
        
        // Calculate confidence based on number of direction changes
        float confidence = 0.0f;
        
        if (directionChanges >= 2) {
            confidence = Math.min(0.8f, 0.3f + (directionChanges - 2) * 0.15f);
        }
        
        return new Pair<>("ZIGZAG", confidence);
    }
    
    /**
     * Check for stopping movement pattern
     * @param history Recent position history
     * @return Pattern type and confidence
     */
    private Pair<String, Float> checkStoppingMovement(List<PositionHistory> history) {
        if (history.size() < 5) {
            return new Pair<>("STOPPING", 0.0f);
        }
        
        // Calculate velocity magnitudes
        float[] velocityMagnitudes = new float[history.size()];
        
        for (int i = 0; i < history.size(); i++) {
            PointF velocity = history.get(i).velocity;
            velocityMagnitudes[i] = (float) Math.sqrt(
                velocity.x * velocity.x + velocity.y * velocity.y);
        }
        
        // Check if velocities are consistently decreasing
        boolean isDecreasing = true;
        int decreaseCount = 0;
        
        for (int i = 1; i < velocityMagnitudes.length; i++) {
            if (velocityMagnitudes[i] > velocityMagnitudes[i - 1]) {
                isDecreasing = false;
            } else if (velocityMagnitudes[i] < velocityMagnitudes[i - 1]) {
                decreaseCount++;
            }
        }
        
        // Calculate deceleration
        float initialVelocity = velocityMagnitudes[0];
        float finalVelocity = velocityMagnitudes[velocityMagnitudes.length - 1];
        
        // Check if entity is significantly slowing down
        boolean isSlowingDown = finalVelocity < initialVelocity * 0.7;
        
        // Calculate confidence
        float confidence = 0.0f;
        
        if (isDecreasing && isSlowingDown) {
            confidence = 0.8f;
        } else if (decreaseCount >= velocityMagnitudes.length * 0.7) {
            confidence = 0.6f;
        } else if (isSlowingDown) {
            confidence = 0.4f;
        }
        
        return new Pair<>("STOPPING", confidence);
    }
    
    /**
     * Calculate parameters for circular movement
     * @param history Recent position history
     * @return Parameters map
     */
    private Map<String, Object> calculateCircularParameters(List<PositionHistory> history) {
        Map<String, Object> params = fitCircle(history);
        
        // Calculate current angle and angular velocity
        PointF center = (PointF) params.get("center");
        PointF latestPos = history.get(history.size() - 1).position;
        
        // Calculate current angle
        float dx = latestPos.x - center.x;
        float dy = latestPos.y - center.y;
        float currentAngle = (float) Math.atan2(dy, dx);
        
        // Calculate angular velocity from last few points
        float angularVelocity = 0;
        
        if (history.size() >= 3) {
            PointF prevPos = history.get(history.size() - 3).position;
            float prevDx = prevPos.x - center.x;
            float prevDy = prevPos.y - center.y;
            float prevAngle = (float) Math.atan2(prevDy, prevDx);
            
            float angleDiff = normalizeAngle(currentAngle - prevAngle);
            long timeDiff = history.get(history.size() - 1).timestamp - history.get(history.size() - 3).timestamp;
            
            if (timeDiff > 0) {
                angularVelocity = angleDiff / (timeDiff / 1000f);
            }
        }
        
        params.put("currentAngle", currentAngle);
        params.put("angularVelocity", angularVelocity);
        
        return params;
    }
    
    /**
     * Calculate parameters for zigzag movement
     * @param history Recent position history
     * @return Parameters map
     */
    private Map<String, Object> calculateZigzagParameters(List<PositionHistory> history) {
        Map<String, Object> params = new HashMap<>();
        
        // Calculate average velocity
        PointF avgVelocity = new PointF(0, 0);
        
        for (PositionHistory entry : history) {
            avgVelocity.x += entry.velocity.x;
            avgVelocity.y += entry.velocity.y;
        }
        
        avgVelocity.x /= history.size();
        avgVelocity.y /= history.size();
        
        // Calculate perpendicular direction
        float velMagnitude = (float) Math.sqrt(
            avgVelocity.x * avgVelocity.x + avgVelocity.y * avgVelocity.y);
        
        PointF normalizedVel = new PointF(0, 0);
        
        if (velMagnitude > 0) {
            normalizedVel.x = avgVelocity.x / velMagnitude;
            normalizedVel.y = avgVelocity.y / velMagnitude;
        }
        
        PointF perpDirection = new PointF(-normalizedVel.y, normalizedVel.x);
        
        // Project positions onto perpendicular direction
        float[] projections = new float[history.size()];
        
        for (int i = 0; i < history.size(); i++) {
            PointF pos = history.get(i).position;
            PointF origin = history.get(0).position;
            
            float dx = pos.x - origin.x;
            float dy = pos.y - origin.y;
            
            projections[i] = dx * perpDirection.x + dy * perpDirection.y;
        }
        
        // Find max amplitude
        float maxProj = Float.MIN_VALUE;
        float minProj = Float.MAX_VALUE;
        
        for (float proj : projections) {
            maxProj = Math.max(maxProj, proj);
            minProj = Math.min(minProj, proj);
        }
        
        float amplitude = (maxProj - minProj) / 2;
        
        // Estimate frequency
        int cycles = 0;
        boolean increasing = false;
        
        for (int i = 1; i < projections.length; i++) {
            if (projections[i] > projections[i - 1] && !increasing) {
                increasing = true;
                cycles++;
            } else if (projections[i] < projections[i - 1] && increasing) {
                increasing = false;
                cycles++;
            }
        }
        
        float timeSpan = (history.get(history.size() - 1).timestamp - history.get(0).timestamp) / 1000f;
        float frequency = (cycles / 2) / timeSpan; // cycles per second
        
        // Estimate phase
        float phase = 0;
        
        if (projections.length > 0) {
            phase = (float) (projections[0] > 0 ? 0 : Math.PI);
        }
        
        // Set parameters
        params.put("baseVelocityX", avgVelocity.x);
        params.put("baseVelocityY", avgVelocity.y);
        params.put("amplitude", amplitude);
        params.put("frequency", frequency);
        params.put("phase", phase);
        
        return params;
    }
    
    /**
     * Calculate parameters for stopping movement
     * @param history Recent position history
     * @return Parameters map
     */
    private Map<String, Object> calculateStoppingParameters(List<PositionHistory> history) {
        Map<String, Object> params = new HashMap<>();
        
        // Calculate velocity magnitudes
        float[] velocityMagnitudes = new float[history.size()];
        long[] timestamps = new long[history.size()];
        
        for (int i = 0; i < history.size(); i++) {
            PointF velocity = history.get(i).velocity;
            velocityMagnitudes[i] = (float) Math.sqrt(
                velocity.x * velocity.x + velocity.y * velocity.y);
            timestamps[i] = history.get(i).timestamp;
        }
        
        // Calculate deceleration
        float initialVelocity = velocityMagnitudes[0];
        float finalVelocity = velocityMagnitudes[velocityMagnitudes.length - 1];
        float timeSpan = (timestamps[timestamps.length - 1] - timestamps[0]) / 1000f;
        
        float deceleration = 0;
        
        if (timeSpan > 0) {
            deceleration = (initialVelocity - finalVelocity) / timeSpan;
        }
        
        deceleration = Math.max(deceleration, 100f); // Minimum deceleration
        
        params.put("deceleration", deceleration);
        
        return params;
    }
    
    /**
     * Fit a circle to a set of points
     * @param history Position history
     * @return Circle parameters (center, radius, error)
     */
    private Map<String, Object> fitCircle(List<PositionHistory> history) {
        Map<String, Object> result = new HashMap<>();
        
        // Extract points
        float[] x = new float[history.size()];
        float[] y = new float[history.size()];
        
        for (int i = 0; i < history.size(); i++) {
            x[i] = history.get(i).position.x;
            y[i] = history.get(i).position.y;
        }
        
        // Calculate mean of x and y
        float meanX = 0, meanY = 0;
        
        for (int i = 0; i < x.length; i++) {
            meanX += x[i];
            meanY += y[i];
        }
        
        meanX /= x.length;
        meanY /= y.length;
        
        // Compute u = x - meanX, v = y - meanY
        float[] u = new float[x.length];
        float[] v = new float[y.length];
        
        for (int i = 0; i < x.length; i++) {
            u[i] = x[i] - meanX;
            v[i] = y[i] - meanY;
        }
        
        // Compute summations
        float sumUU = 0, sumVV = 0, sumUV = 0, sumUUU = 0, sumVVV = 0, sumUVV = 0, sumVUU = 0;
        
        for (int i = 0; i < x.length; i++) {
            sumUU += u[i] * u[i];
            sumVV += v[i] * v[i];
            sumUV += u[i] * v[i];
            sumUUU += u[i] * u[i] * u[i];
            sumVVV += v[i] * v[i] * v[i];
            sumUVV += u[i] * v[i] * v[i];
            sumVUU += v[i] * u[i] * u[i];
        }
        
        // Calculate circle parameters
        float uc = 0, vc = 0;
        
        // Simple approximation if not enough points or summations are too small
        if (x.length < 5 || Math.abs(sumUU) < 1e-3 || Math.abs(sumVV) < 1e-3) {
            // Use centroid as center
            uc = 0;
            vc = 0;
        } else {
            // Calculate center
            float a = (sumUUU + sumUVV) / 2;
            float b = (sumVVV + sumVUU) / 2;
            
            uc = (a * sumVV - b * sumUV) / (sumUU * sumVV - sumUV * sumUV);
            vc = (b * sumUU - a * sumUV) / (sumUU * sumVV - sumUV * sumUV);
        }
        
        // Calculate radius
        float radius = 0;
        
        for (int i = 0; i < x.length; i++) {
            float dx = u[i] - uc;
            float dy = v[i] - vc;
            radius += (float) Math.sqrt(dx * dx + dy * dy);
        }
        
        radius /= x.length;
        
        // Calculate error
        float error = 0;
        
        for (int i = 0; i < x.length; i++) {
            float dx = u[i] - uc;
            float dy = v[i] - vc;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            error += Math.abs(distance - radius) / radius;
        }
        
        error /= x.length;
        
        // Set result
        result.put("center", new PointF(meanX + uc, meanY + vc));
        result.put("radius", radius);
        result.put("error", error);
        
        return result;
    }
    
    /**
     * Calculate angle between two vectors
     * @param v1 First vector
     * @param v2 Second vector
     * @return Angle in radians
     */
    private float angleBetweenVectors(PointF v1, PointF v2) {
        float dot = v1.x * v2.x + v1.y * v2.y;
        float v1Mag = (float) Math.sqrt(v1.x * v1.x + v1.y * v1.y);
        float v2Mag = (float) Math.sqrt(v2.x * v2.x + v2.y * v2.y);
        
        if (v1Mag < 1e-5 || v2Mag < 1e-5) {
            return 0;
        }
        
        float cosAngle = dot / (v1Mag * v2Mag);
        cosAngle = Math.max(-1, Math.min(1, cosAngle)); // Clamp to [-1, 1]
        
        float angle = (float) Math.acos(cosAngle);
        
        // Determine direction (clockwise or counter-clockwise)
        float cross = v1.x * v2.y - v1.y * v2.x;
        
        if (cross < 0) {
            angle = -angle;
        }
        
        return angle;
    }
    
    /**
     * Normalize angle to [-π, π]
     * @param angle Angle in radians
     * @return Normalized angle
     */
    private float normalizeAngle(float angle) {
        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        
        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }
        
        return angle;
    }
}
