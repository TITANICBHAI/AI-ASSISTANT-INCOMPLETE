package com.aiassistant.core.analysis.spatial.reasoning;

import android.util.Log;

import com.aiassistant.core.analysis.scene.SceneGraphAnalyzer;
import com.aiassistant.core.analysis.scene.SceneGraphAnalyzer.SceneNode;
import com.aiassistant.core.analysis.scene.SceneGraphAnalyzer.SceneRelationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Advanced spatial reasoning component that provides sophisticated understanding of
 * complex 3D environments including spatial relationships, object relationships, 
 * and advanced reasoning capabilities.
 * 
 * This component integrates with the scene graph analyzer to provide higher-level
 * understanding of spatial concepts beyond simple positional data.
 */
public class AdvancedSpatialReasoning {
    private static final String TAG = "AdvancedSpatialReasoning";
    
    // Singleton instance
    private static AdvancedSpatialReasoning instance;
    
    // Scene graph analyzer instance
    private final SceneGraphAnalyzer sceneGraphAnalyzer;
    
    // Cached complex relationship analysis
    private final Map<String, List<SpatialRelationship>> cachedRelationships = new HashMap<>();
    private final Map<String, Obstacle> cachedObstacles = new HashMap<>();
    private final Map<String, List<SpatialContext>> contextMap = new HashMap<>();
    
    // Reasoning history for pattern analysis
    private final List<ReasoningRecord> reasoningHistory = new ArrayList<>();
    
    /**
     * Detailed spatial relationship between objects
     */
    public static class SpatialRelationship {
        public final String id;
        public final RelationType type;
        public final SceneNode objectA;
        public final SceneNode objectB;
        public final Map<String, Float> metrics = new HashMap<>();
        public final Map<String, Object> attributes = new HashMap<>();
        
        public enum RelationType {
            ABOVE,
            BELOW,
            LEFT_OF,
            RIGHT_OF,
            IN_FRONT_OF,
            BEHIND,
            INSIDE,
            OUTSIDE,
            TOUCHING,
            ALIGNED_WITH,
            BETWEEN,
            SURROUNDED_BY,
            PARTIALLY_OCCLUDED_BY,
            COMPLETELY_OCCLUDED_BY
        }
        
        public SpatialRelationship(String id, RelationType type, SceneNode objectA, SceneNode objectB) {
            this.id = id;
            this.type = type;
            this.objectA = objectA;
            this.objectB = objectB;
        }
    }
    
    /**
     * Represents a spatial context (area of influence or zone)
     */
    public static class SpatialContext {
        public final String id;
        public final ContextType type;
        public final SceneNode primaryNode;
        public final List<SceneNode> containedNodes = new ArrayList<>();
        public final Map<String, Float> boundingBox = new HashMap<>();
        public final Map<String, Object> attributes = new HashMap<>();
        public float importance;
        
        public enum ContextType {
            INTERACTION_ZONE,
            DANGER_ZONE,
            OBJECTIVE_AREA,
            RESOURCE_REGION,
            STRATEGIC_LOCATION,
            COVER_AREA,
            LINE_OF_SIGHT,
            PATROL_ROUTE,
            CHOKEPOINT,
            OPEN_AREA
        }
        
        public SpatialContext(String id, ContextType type, SceneNode primaryNode) {
            this.id = id;
            this.type = type;
            this.primaryNode = primaryNode;
            this.importance = 0.5f;
            
            // Initialize bounding box with primary node dimensions
            initializeBoundingBox();
        }
        
        private void initializeBoundingBox() {
            if (primaryNode != null) {
                float x = primaryNode.coordinates.get("x");
                float y = primaryNode.coordinates.get("y");
                float z = primaryNode.coordinates.get("z");
                float width = primaryNode.dimensions.get("width");
                float height = primaryNode.dimensions.get("height");
                float depth = primaryNode.dimensions.get("depth");
                
                boundingBox.put("minX", x - width/2);
                boundingBox.put("maxX", x + width/2);
                boundingBox.put("minY", y - height/2);
                boundingBox.put("maxY", y + height/2);
                boundingBox.put("minZ", z - depth/2);
                boundingBox.put("maxZ", z + depth/2);
            }
        }
    }
    
    /**
     * Represents an obstacle or barrier in the environment
     */
    public static class Obstacle {
        public final String id;
        public final ObstacleType type;
        public final SceneNode node;
        public final Map<String, Float> boundingBox = new HashMap<>();
        public float severity;
        public float crossability;
        
        public enum ObstacleType {
            PHYSICAL_BARRIER,
            TERRAIN_ELEVATION,
            GAP,
            HAZARD,
            RESTRICTED_ZONE,
            MOVING_OBSTACLE,
            DETECTION_ZONE
        }
        
        public Obstacle(String id, ObstacleType type, SceneNode node) {
            this.id = id;
            this.type = type;
            this.node = node;
            this.severity = 0.5f;
            this.crossability = 0.5f;
            
            // Initialize bounding box
            initializeBoundingBox();
        }
        
        private void initializeBoundingBox() {
            if (node != null) {
                float x = node.coordinates.get("x");
                float y = node.coordinates.get("y");
                float z = node.coordinates.get("z");
                float width = node.dimensions.get("width");
                float height = node.dimensions.get("height");
                float depth = node.dimensions.get("depth");
                
                boundingBox.put("minX", x - width/2);
                boundingBox.put("maxX", x + width/2);
                boundingBox.put("minY", y - height/2);
                boundingBox.put("maxY", y + height/2);
                boundingBox.put("minZ", z - depth/2);
                boundingBox.put("maxZ", z + depth/2);
            }
        }
    }
    
    /**
     * Record of spatial reasoning for pattern analysis
     */
    private static class ReasoningRecord {
        public final String id;
        public final long timestamp;
        public final String operationType;
        public final Map<String, Object> parameters = new HashMap<>();
        public final Map<String, Object> results = new HashMap<>();
        
        public ReasoningRecord(String id, String operationType) {
            this.id = id;
            this.timestamp = System.currentTimeMillis();
            this.operationType = operationType;
        }
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized AdvancedSpatialReasoning getInstance() {
        if (instance == null) {
            instance = new AdvancedSpatialReasoning();
        }
        return instance;
    }
    
    /**
     * Private constructor
     */
    private AdvancedSpatialReasoning() {
        sceneGraphAnalyzer = SceneGraphAnalyzer.getInstance();
        Log.d(TAG, "Advanced Spatial Reasoning system initialized");
    }
    
    /**
     * Analyze spatial relationships between two objects
     */
    public List<SpatialRelationship> analyzeSpatialRelationships(SceneNode objectA, SceneNode objectB) {
        if (objectA == null || objectB == null) {
            Log.e(TAG, "Cannot analyze null objects");
            return new ArrayList<>();
        }
        
        // Check cache first
        String cacheKey = objectA.id + "_" + objectB.id;
        if (cachedRelationships.containsKey(cacheKey)) {
            return cachedRelationships.get(cacheKey);
        }
        
        // Start new relationship analysis
        List<SpatialRelationship> relationships = new ArrayList<>();
        
        // Get positional data
        float ax = objectA.coordinates.get("x");
        float ay = objectA.coordinates.get("y");
        float az = objectA.coordinates.get("z");
        float aw = objectA.dimensions.get("width");
        float ah = objectA.dimensions.get("height");
        float ad = objectA.dimensions.get("depth");
        
        float bx = objectB.coordinates.get("x");
        float by = objectB.coordinates.get("y");
        float bz = objectB.coordinates.get("z");
        float bw = objectB.dimensions.get("width");
        float bh = objectB.dimensions.get("height");
        float bd = objectB.dimensions.get("depth");
        
        // Analyze vertical relationship
        if (ay + ah/2 < by - bh/2) {
            // A is below B
            SpatialRelationship belowRel = new SpatialRelationship(
                "below_" + objectA.id + "_" + objectB.id,
                SpatialRelationship.RelationType.BELOW,
                objectA,
                objectB
            );
            belowRel.metrics.put("vertical_distance", (by - bh/2) - (ay + ah/2));
            relationships.add(belowRel);
        } else if (by + bh/2 < ay - ah/2) {
            // A is above B
            SpatialRelationship aboveRel = new SpatialRelationship(
                "above_" + objectA.id + "_" + objectB.id,
                SpatialRelationship.RelationType.ABOVE,
                objectA,
                objectB
            );
            aboveRel.metrics.put("vertical_distance", (ay - ah/2) - (by + bh/2));
            relationships.add(aboveRel);
        }
        
        // Analyze horizontal relationships
        if (ax + aw/2 < bx - bw/2) {
            // A is left of B
            SpatialRelationship leftRel = new SpatialRelationship(
                "left_of_" + objectA.id + "_" + objectB.id,
                SpatialRelationship.RelationType.LEFT_OF,
                objectA,
                objectB
            );
            leftRel.metrics.put("horizontal_distance", (bx - bw/2) - (ax + aw/2));
            relationships.add(leftRel);
        } else if (bx + bw/2 < ax - aw/2) {
            // A is right of B
            SpatialRelationship rightRel = new SpatialRelationship(
                "right_of_" + objectA.id + "_" + objectB.id,
                SpatialRelationship.RelationType.RIGHT_OF,
                objectA,
                objectB
            );
            rightRel.metrics.put("horizontal_distance", (ax - aw/2) - (bx + bw/2));
            relationships.add(rightRel);
        }
        
        // Analyze depth relationships (front/back)
        if (az + ad/2 < bz - bd/2) {
            // A is in front of B
            SpatialRelationship frontRel = new SpatialRelationship(
                "in_front_of_" + objectA.id + "_" + objectB.id,
                SpatialRelationship.RelationType.IN_FRONT_OF,
                objectA,
                objectB
            );
            frontRel.metrics.put("depth_distance", (bz - bd/2) - (az + ad/2));
            relationships.add(frontRel);
        } else if (bz + bd/2 < az - ad/2) {
            // A is behind B
            SpatialRelationship behindRel = new SpatialRelationship(
                "behind_" + objectA.id + "_" + objectB.id,
                SpatialRelationship.RelationType.BEHIND,
                objectA,
                objectB
            );
            behindRel.metrics.put("depth_distance", (az - ad/2) - (bz + bd/2));
            relationships.add(behindRel);
        }
        
        // Check for touching/intersection
        boolean intersectX = !(ax + aw/2 < bx - bw/2 || bx + bw/2 < ax - aw/2);
        boolean intersectY = !(ay + ah/2 < by - bh/2 || by + bh/2 < ay - ah/2);
        boolean intersectZ = !(az + ad/2 < bz - bd/2 || bz + bd/2 < az - ad/2);
        
        if (intersectX && intersectY && intersectZ) {
            // Objects are touching or intersecting
            SpatialRelationship touchRel = new SpatialRelationship(
                "touching_" + objectA.id + "_" + objectB.id,
                SpatialRelationship.RelationType.TOUCHING,
                objectA,
                objectB
            );
            relationships.add(touchRel);
        }
        
        // Check for containment
        boolean aContainsB = (ax - aw/2 <= bx - bw/2) && (ax + aw/2 >= bx + bw/2) &&
                           (ay - ah/2 <= by - bh/2) && (ay + ah/2 >= by + bh/2) &&
                           (az - ad/2 <= bz - bd/2) && (az + ad/2 >= bz + bd/2);
        
        boolean bContainsA = (bx - bw/2 <= ax - aw/2) && (bx + bw/2 >= ax + aw/2) &&
                           (by - bh/2 <= ay - ah/2) && (by + bh/2 >= ay + ah/2) &&
                           (bz - bd/2 <= az - ad/2) && (bz + bd/2 >= az + ad/2);
        
        if (aContainsB) {
            // A contains B (B is inside A)
            SpatialRelationship insideRel = new SpatialRelationship(
                "inside_" + objectB.id + "_" + objectA.id,
                SpatialRelationship.RelationType.INSIDE,
                objectB,
                objectA
            );
            relationships.add(insideRel);
        } else if (bContainsA) {
            // B contains A (A is inside B)
            SpatialRelationship insideRel = new SpatialRelationship(
                "inside_" + objectA.id + "_" + objectB.id,
                SpatialRelationship.RelationType.INSIDE,
                objectA,
                objectB
            );
            relationships.add(insideRel);
        }
        
        // Record this analysis in cache
        cachedRelationships.put(cacheKey, relationships);
        
        // Record reasoning operation
        ReasoningRecord record = new ReasoningRecord(
            "spatial_analysis_" + System.currentTimeMillis(),
            "analyze_spatial_relationships"
        );
        record.parameters.put("objectA", objectA.id);
        record.parameters.put("objectB", objectB.id);
        record.results.put("relationshipCount", relationships.size());
        reasoningHistory.add(record);
        
        return relationships;
    }
    
    /**
     * Analyze occlusion relationships (what blocks view)
     */
    public List<SpatialRelationship> analyzeOcclusions(SceneNode viewpoint, SceneNode target) {
        if (viewpoint == null || target == null) {
            Log.e(TAG, "Cannot analyze occlusions with null objects");
            return new ArrayList<>();
        }
        
        List<SpatialRelationship> occlusions = new ArrayList<>();
        
        // Get potential blockers between viewpoint and target
        List<SceneNode> potentialBlockers = sceneGraphAnalyzer.getPotentialBlockers(viewpoint, target);
        
        for (SceneNode blocker : potentialBlockers) {
            // Calculate occlusion metrics
            float occlusionStrength = calculateOcclusionStrength(viewpoint, blocker, target);
            
            // Create appropriate occlusion relationship
            SpatialRelationship occlusionRel;
            
            if (occlusionStrength > 0.8f) {
                // Complete occlusion
                occlusionRel = new SpatialRelationship(
                    "completely_occluded_" + target.id + "_by_" + blocker.id,
                    SpatialRelationship.RelationType.COMPLETELY_OCCLUDED_BY,
                    target,
                    blocker
                );
            } else if (occlusionStrength > 0.1f) {
                // Partial occlusion
                occlusionRel = new SpatialRelationship(
                    "partially_occluded_" + target.id + "_by_" + blocker.id,
                    SpatialRelationship.RelationType.PARTIALLY_OCCLUDED_BY,
                    target,
                    blocker
                );
            } else {
                // No significant occlusion
                continue;
            }
            
            occlusionRel.metrics.put("occlusion_strength", occlusionStrength);
            occlusionRel.attributes.put("viewpoint", viewpoint.id);
            
            occlusions.add(occlusionRel);
        }
        
        return occlusions;
    }
    
    /**
     * Calculate occlusion strength (similar to blockingStrength but specialized)
     */
    private float calculateOcclusionStrength(SceneNode viewpoint, SceneNode blocker, SceneNode target) {
        // In a full implementation, this would use ray casting
        // For this example, we'll do a simplified calculation
        
        float vx = viewpoint.coordinates.get("x");
        float vy = viewpoint.coordinates.get("y");
        float vz = viewpoint.coordinates.get("z");
        
        float bx = blocker.coordinates.get("x");
        float by = blocker.coordinates.get("y");
        float bz = blocker.coordinates.get("z");
        float bw = blocker.dimensions.get("width");
        float bh = blocker.dimensions.get("height");
        
        float tx = target.coordinates.get("x");
        float ty = target.coordinates.get("y");
        float tz = target.coordinates.get("z");
        float tw = target.dimensions.get("width");
        float th = target.dimensions.get("height");
        
        // Calculate distances
        float distToBlocker = calculateDistance(vx, vy, vz, bx, by, bz);
        float distToTarget = calculateDistance(vx, vy, vz, tx, ty, tz);
        
        // If blocker is behind target, no occlusion
        if (distToBlocker > distToTarget) {
            return 0.0f;
        }
        
        // Calculate angular size of blocker and target from viewpoint
        float blockerAngularWidth = (float) Math.atan2(bw, distToBlocker);
        float blockerAngularHeight = (float) Math.atan2(bh, distToBlocker);
        float targetAngularWidth = (float) Math.atan2(tw, distToTarget);
        float targetAngularHeight = (float) Math.atan2(th, distToTarget);
        
        // Calculate angular displacement
        float dx = bx - tx;
        float dy = by - ty;
        float angularDisplacementX = (float) Math.atan2(dx, distToTarget);
        float angularDisplacementY = (float) Math.atan2(dy, distToTarget);
        
        // Calculate overlap
        float overlapX = Math.max(0, 
            Math.min(blockerAngularWidth/2 + angularDisplacementX, targetAngularWidth/2) - 
            Math.max(-blockerAngularWidth/2 + angularDisplacementX, -targetAngularWidth/2));
        
        float overlapY = Math.max(0, 
            Math.min(blockerAngularHeight/2 + angularDisplacementY, targetAngularHeight/2) - 
            Math.max(-blockerAngularHeight/2 + angularDisplacementY, -targetAngularHeight/2));
        
        // Calculate occlusion area and strength
        float targetAngularArea = targetAngularWidth * targetAngularHeight;
        float overlapArea = overlapX * overlapY;
        
        if (targetAngularArea == 0) return 0;
        
        return Math.min(1.0f, overlapArea / targetAngularArea);
    }
    
    /**
     * Helper function to calculate distance between points
     */
    private float calculateDistance(float x1, float y1, float z1, float x2, float y2, float z2) {
        return (float) Math.sqrt(
            Math.pow(x2 - x1, 2) + 
            Math.pow(y2 - y1, 2) + 
            Math.pow(z2 - z1, 2)
        );
    }
    
    /**
     * Find objects that are between two other objects
     */
    public List<SceneNode> findObjectsBetween(SceneNode objectA, SceneNode objectB) {
        if (objectA == null || objectB == null) {
            Log.e(TAG, "Cannot find objects between null objects");
            return new ArrayList<>();
        }
        
        List<SceneNode> betweenObjects = new ArrayList<>();
        
        // Get vector from A to B
        float ax = objectA.coordinates.get("x");
        float ay = objectA.coordinates.get("y");
        float az = objectA.coordinates.get("z");
        
        float bx = objectB.coordinates.get("x");
        float by = objectB.coordinates.get("y");
        float bz = objectB.coordinates.get("z");
        
        float dx = bx - ax;
        float dy = by - ay;
        float dz = bz - az;
        
        float distAB = calculateDistance(ax, ay, az, bx, by, bz);
        
        // Check all nodes
        for (String nodeId : sceneGraphAnalyzer.getNodeRegistry().keySet()) {
            SceneNode node = sceneGraphAnalyzer.getNode(nodeId);
            if (node == objectA || node == objectB) continue;
            
            float nx = node.coordinates.get("x");
            float ny = node.coordinates.get("y");
            float nz = node.coordinates.get("z");
            
            // Calculate distance from node to line between A and B
            float t = ((nx - ax) * dx + (ny - ay) * dy + (nz - az) * dz) / 
                      (dx * dx + dy * dy + dz * dz);
            
            // Check if point is between A and B
            if (t > 0 && t < 1) {
                // Calculate closest point on line
                float closestX = ax + t * dx;
                float closestY = ay + t * dy;
                float closestZ = az + t * dz;
                
                // Calculate distance from node to closest point
                float distance = calculateDistance(nx, ny, nz, closestX, closestY, closestZ);
                
                // Get maximum radius of node
                float nodeRadius = Math.max(
                    node.dimensions.get("width"), 
                    Math.max(node.dimensions.get("height"), node.dimensions.get("depth"))
                ) / 2;
                
                // If node is close enough to line, it's between A and B
                if (distance <= nodeRadius + 1.0f) {  // Adding 1.0 for tolerance
                    betweenObjects.add(node);
                    
                    // Create BETWEEN relationship if it doesn't exist
                    String relationshipId = "between_" + node.id + "_" + objectA.id + "_" + objectB.id;
                    boolean exists = false;
                    
                    for (SpatialRelationship rel : this.getSpatialRelationships()) {
                        if (rel.id.equals(relationshipId)) {
                            exists = true;
                            break;
                        }
                    }
                    
                    if (!exists) {
                        SpatialRelationship betweenRel = new SpatialRelationship(
                            relationshipId,
                            SpatialRelationship.RelationType.BETWEEN,
                            node,
                            objectA  // 'between' relations use objectB for the pair of objects
                        );
                        betweenRel.attributes.put("secondObject", objectB.id);
                        betweenRel.metrics.put("distance_along_path", t * distAB);
                        betweenRel.metrics.put("lateral_distance", distance);
                        
                        this.recordSpatialRelationship(betweenRel);
                    }
                }
            }
        }
        
        return betweenObjects;
    }
    
    /**
     * Get all spatial relationships
     */
    public List<SpatialRelationship> getSpatialRelationships() {
        List<SpatialRelationship> allRelationships = new ArrayList<>();
        
        for (List<SpatialRelationship> relationships : cachedRelationships.values()) {
            allRelationships.addAll(relationships);
        }
        
        return allRelationships;
    }
    
    /**
     * Record a new spatial relationship
     */
    public void recordSpatialRelationship(SpatialRelationship relationship) {
        if (relationship == null) return;
        
        String cacheKey = relationship.objectA.id + "_" + relationship.objectB.id;
        
        if (!cachedRelationships.containsKey(cacheKey)) {
            cachedRelationships.put(cacheKey, new ArrayList<>());
        }
        
        cachedRelationships.get(cacheKey).add(relationship);
    }
    
    /**
     * Identify and classify obstacles in the environment
     */
    public List<Obstacle> identifyObstacles(SceneNode viewpoint) {
        List<Obstacle> obstacles = new ArrayList<>();
        
        // Get all nodes
        Map<String, SceneNode> nodes = sceneGraphAnalyzer.getNodeRegistry();
        
        for (SceneNode node : nodes.values()) {
            if (node == viewpoint) continue;
            
            // Skip nodes that are too small to be obstacles
            float size = Math.max(
                node.dimensions.get("width"), 
                Math.max(node.dimensions.get("height"), node.dimensions.get("depth"))
            );
            
            if (size < 1.0f) continue;
            
            // Check if node is already classified as an obstacle
            if (cachedObstacles.containsKey(node.id)) {
                obstacles.add(cachedObstacles.get(node.id));
                continue;
            }
            
            // Determine obstacle type based on properties
            Obstacle.ObstacleType obstacleType = classifyObstacleType(node);
            
            if (obstacleType != null) {
                Obstacle obstacle = new Obstacle("obstacle_" + node.id, obstacleType, node);
                
                // Calculate obstacle properties
                obstacle.severity = calculateObstacleSeverity(node, viewpoint);
                obstacle.crossability = calculateObstacleCrossability(node);
                
                // Cache and add to result
                cachedObstacles.put(node.id, obstacle);
                obstacles.add(obstacle);
            }
        }
        
        return obstacles;
    }
    
    /**
     * Classify obstacle type based on node properties
     */
    private Obstacle.ObstacleType classifyObstacleType(SceneNode node) {
        // Check node type
        switch (node.type) {
            case BOUNDARY:
            case STRUCTURE:
                return Obstacle.ObstacleType.PHYSICAL_BARRIER;
                
            case TERRAIN:
                // Check if it's elevated
                if (node.coordinates.get("y") > 1.0f) {
                    return Obstacle.ObstacleType.TERRAIN_ELEVATION;
                }
                return null; // Flat terrain isn't an obstacle
                
            case TRIGGER:
                // Check if it's a detection zone
                if (node.properties.containsKey("detection") || 
                    node.properties.containsKey("alarm") ||
                    node.properties.containsKey("security")) {
                    return Obstacle.ObstacleType.DETECTION_ZONE;
                }
                return null;
                
            default:
                // Check properties
                if (node.properties.containsKey("hazard") || 
                    node.properties.containsKey("danger") ||
                    node.properties.containsKey("damage")) {
                    return Obstacle.ObstacleType.HAZARD;
                }
                
                // Check if node is moving
                if (node.dynamicState.containsKey("velocity") || 
                    node.dynamicState.containsKey("speed") ||
                    node.dynamicState.containsKey("moving")) {
                    return Obstacle.ObstacleType.MOVING_OBSTACLE;
                }
                
                // If it's large and in the way, it's a physical barrier
                float size = Math.max(
                    node.dimensions.get("width"), 
                    Math.max(node.dimensions.get("height"), node.dimensions.get("depth"))
                );
                
                if (size > 2.0f) {
                    return Obstacle.ObstacleType.PHYSICAL_BARRIER;
                }
                
                return null; // Not an obstacle
        }
    }
    
    /**
     * Calculate obstacle severity
     */
    private float calculateObstacleSeverity(SceneNode node, SceneNode viewpoint) {
        float severity = 0.5f; // Default medium severity
        
        // Adjust based on obstacle size
        float size = Math.max(
            node.dimensions.get("width"), 
            Math.max(node.dimensions.get("height"), node.dimensions.get("depth"))
        );
        severity += (size / 10.0f); // Larger obstacles are more severe
        
        // Adjust based on distance from viewpoint
        if (viewpoint != null) {
            float distance = calculateDistance(
                node.coordinates.get("x"), node.coordinates.get("y"), node.coordinates.get("z"),
                viewpoint.coordinates.get("x"), viewpoint.coordinates.get("y"), viewpoint.coordinates.get("z")
            );
            
            // Closer obstacles are more severe
            if (distance < 5.0f) {
                severity += 0.3f;
            } else if (distance < 10.0f) {
                severity += 0.1f;
            }
        }
        
        // Check for hazard properties
        if (node.properties.containsKey("hazard") || node.properties.containsKey("danger")) {
            severity += 0.3f;
        }
        
        // Check for movement
        if (node.dynamicState.containsKey("velocity") || node.dynamicState.containsKey("moving")) {
            severity += 0.2f;
        }
        
        // Clamp to 0-1 range
        return Math.min(1.0f, Math.max(0.0f, severity));
    }
    
    /**
     * Calculate obstacle crossability
     */
    private float calculateObstacleCrossability(SceneNode node) {
        float crossability = 0.5f; // Default medium crossability
        
        // Check node type
        switch (node.type) {
            case BOUNDARY:
                return 0.0f; // Cannot cross boundaries
                
            case STRUCTURE:
                if (node.properties.containsKey("door") || node.properties.containsKey("entrance")) {
                    return 0.8f; // Doors are crossable
                }
                return 0.1f; // Structures are generally hard to cross
                
            case TERRAIN:
                // Check slope
                if (node.properties.containsKey("slope")) {
                    Object slopeValue = node.properties.get("slope");
                    if (slopeValue instanceof Float) {
                        float slope = (Float) slopeValue;
                        return Math.max(0.0f, 1.0f - (slope / 45.0f)); // Steeper slopes are harder to cross
                    }
                }
                return 0.7f; // Most terrain is crossable
                
            case TRIGGER:
                return 1.0f; // Triggers don't physically block
                
            default:
                // Check properties
                if (node.properties.containsKey("solid") || node.properties.containsKey("blocking")) {
                    return 0.1f; // Solid objects are hard to cross
                }
                
                if (node.properties.containsKey("liquid")) {
                    return 0.3f; // Liquids are somewhat crossable
                }
                
                // Check height
                float height = node.dimensions.get("height");
                if (height < 0.5f) {
                    return 0.9f; // Low objects are easy to cross
                } else if (height < 1.5f) {
                    return 0.4f; // Medium height objects are somewhat crossable
                } else {
                    return 0.2f; // Tall objects are hard to cross
                }
        }
    }
    
    /**
     * Identify and create spatial contexts
     */
    public List<SpatialContext> identifySpatialContexts() {
        List<SpatialContext> contexts = new ArrayList<>();
        
        // Clear existing contexts
        contextMap.clear();
        
        // Get all nodes
        Map<String, SceneNode> nodes = sceneGraphAnalyzer.getNodeRegistry();
        
        // Generate contexts for interesting objects
        for (SceneNode node : nodes.values()) {
            // Skip small or unimportant objects
            if (node.importance < 0.4f) continue;
            
            // Determine context type based on node properties
            SpatialContext.ContextType contextType = classifyContextType(node);
            
            if (contextType != null) {
                // Create context
                SpatialContext context = new SpatialContext(
                    "context_" + node.id,
                    contextType,
                    node
                );
                
                // Set importance based on node
                context.importance = node.importance;
                
                // Expand context bounds beyond the node itself
                expandContextBounds(context, contextType);
                
                // Find nodes contained in this context
                findContainedNodes(context);
                
                // Add to results
                contexts.add(context);
                contextMap.put(context.id, new ArrayList<>());
            }
        }
        
        // Check for special relationships that create contexts
        for (SpatialRelationship rel : getSpatialRelationships()) {
            if (rel.type == SpatialRelationship.RelationType.LINE_OF_SIGHT) {
                // Create line of sight context
                SpatialContext losContext = new SpatialContext(
                    "los_context_" + rel.id,
                    SpatialContext.ContextType.LINE_OF_SIGHT,
                    rel.objectA
                );
                
                // Set bounding box to cover both objects plus a corridor between them
                createCorridorBounds(losContext, rel.objectA, rel.objectB);
                
                // Find nodes in this corridor
                findContainedNodes(losContext);
                
                contexts.add(losContext);
                contextMap.put(losContext.id, new ArrayList<>());
            }
        }
        
        return contexts;
    }
    
    /**
     * Classify context type based on node properties
     */
    private SpatialContext.ContextType classifyContextType(SceneNode node) {
        // Check node type
        switch (node.type) {
            case TRIGGER:
                if (node.properties.containsKey("interaction") || node.properties.containsKey("usable")) {
                    return SpatialContext.ContextType.INTERACTION_ZONE;
                }
                return null;
                
            case STRUCTURE:
                if (node.properties.containsKey("cover") || node.properties.containsKey("protection")) {
                    return SpatialContext.ContextType.COVER_AREA;
                }
                if (node.properties.containsKey("chokepoint") || node.properties.containsKey("narrow")) {
                    return SpatialContext.ContextType.CHOKEPOINT;
                }
                return null;
                
            case ITEM:
                if (node.properties.containsKey("resource") || node.properties.containsKey("supply")) {
                    return SpatialContext.ContextType.RESOURCE_REGION;
                }
                return null;
                
            case TERRAIN:
                if (node.properties.containsKey("open") || node.properties.containsKey("clearing")) {
                    return SpatialContext.ContextType.OPEN_AREA;
                }
                return null;
                
            case NPC:
                if (node.properties.containsKey("enemy") || node.properties.containsKey("hostile")) {
                    return SpatialContext.ContextType.DANGER_ZONE;
                }
                if (node.properties.containsKey("patrol") || node.properties.containsKey("guard")) {
                    return SpatialContext.ContextType.PATROL_ROUTE;
                }
                return null;
                
            default:
                if (node.properties.containsKey("objective") || node.properties.containsKey("goal")) {
                    return SpatialContext.ContextType.OBJECTIVE_AREA;
                }
                if (node.properties.containsKey("strategic") || node.properties.containsKey("important")) {
                    return SpatialContext.ContextType.STRATEGIC_LOCATION;
                }
                return null;
        }
    }
    
    /**
     * Expand context bounds based on type
     */
    private void expandContextBounds(SpatialContext context, SpatialContext.ContextType type) {
        float expandAmount = 0;
        
        switch (type) {
            case INTERACTION_ZONE:
                expandAmount = 2.0f;
                break;
                
            case DANGER_ZONE:
                expandAmount = 5.0f;
                break;
                
            case OBJECTIVE_AREA:
                expandAmount = 3.0f;
                break;
                
            case RESOURCE_REGION:
                expandAmount = 2.5f;
                break;
                
            case STRATEGIC_LOCATION:
                expandAmount = 4.0f;
                break;
                
            case COVER_AREA:
                expandAmount = 1.5f;
                break;
                
            case PATROL_ROUTE:
                expandAmount = 3.0f;
                break;
                
            case CHOKEPOINT:
                expandAmount = 2.0f;
                break;
                
            case OPEN_AREA:
                expandAmount = 5.0f;
                break;
                
            default:
                expandAmount = 2.0f;
                break;
        }
        
        // Expand bounds
        context.boundingBox.put("minX", context.boundingBox.get("minX") - expandAmount);
        context.boundingBox.put("maxX", context.boundingBox.get("maxX") + expandAmount);
        context.boundingBox.put("minY", context.boundingBox.get("minY") - expandAmount);
        context.boundingBox.put("maxY", context.boundingBox.get("maxY") + expandAmount);
        context.boundingBox.put("minZ", context.boundingBox.get("minZ") - expandAmount);
        context.boundingBox.put("maxZ", context.boundingBox.get("maxZ") + expandAmount);
    }
    
    /**
     * Create corridor-shaped bounds between two nodes
     */
    private void createCorridorBounds(SpatialContext context, SceneNode nodeA, SceneNode nodeB) {
        float ax = nodeA.coordinates.get("x");
        float ay = nodeA.coordinates.get("y");
        float az = nodeA.coordinates.get("z");
        
        float bx = nodeB.coordinates.get("x");
        float by = nodeB.coordinates.get("y");
        float bz = nodeB.coordinates.get("z");
        
        // Calculate corridor direction
        float dx = bx - ax;
        float dy = by - ay;
        float dz = bz - az;
        
        // Corridor width
        float width = 2.0f;
        
        // Set bounding box to include both nodes plus a corridor between them
        context.boundingBox.put("minX", Math.min(ax, bx) - width);
        context.boundingBox.put("maxX", Math.max(ax, bx) + width);
        context.boundingBox.put("minY", Math.min(ay, by) - width);
        context.boundingBox.put("maxY", Math.max(ay, by) + width);
        context.boundingBox.put("minZ", Math.min(az, bz) - width);
        context.boundingBox.put("maxZ", Math.max(az, bz) + width);
    }
    
    /**
     * Find nodes contained within a context's bounds
     */
    private void findContainedNodes(SpatialContext context) {
        // Get bounding box
        float minX = context.boundingBox.get("minX");
        float maxX = context.boundingBox.get("maxX");
        float minY = context.boundingBox.get("minY");
        float maxY = context.boundingBox.get("maxY");
        float minZ = context.boundingBox.get("minZ");
        float maxZ = context.boundingBox.get("maxZ");
        
        // Get all nodes
        Map<String, SceneNode> nodes = sceneGraphAnalyzer.getNodeRegistry();
        
        for (SceneNode node : nodes.values()) {
            if (node == context.primaryNode) {
                context.containedNodes.add(node);
                continue;
            }
            
            // Get node position
            float nx = node.coordinates.get("x");
            float ny = node.coordinates.get("y");
            float nz = node.coordinates.get("z");
            
            // Check if node is within bounds
            if (nx >= minX && nx <= maxX && 
                ny >= minY && ny <= maxY && 
                nz >= minZ && nz <= maxZ) {
                
                context.containedNodes.add(node);
            }
        }
    }
    
    /**
     * Create security-focused contextual analysis
     * This identifies potential security threats/vulnerabilities in the spatial arrangement
     */
    public Map<String, Object> analyzeSecurityContext(SceneNode viewpoint) {
        Map<String, Object> securityAnalysis = new HashMap<>();
        List<Map<String, Object>> vulnerabilities = new ArrayList<>();
        
        // If no viewpoint, can't do much
        if (viewpoint == null) {
            securityAnalysis.put("status", "error");
            securityAnalysis.put("message", "No viewpoint provided");
            return securityAnalysis;
        }
        
        // Get visible nodes
        List<SceneNode> visibleNodes = sceneGraphAnalyzer.getVisibleNodes(viewpoint);
        securityAnalysis.put("visibleNodeCount", visibleNodes.size());
        
        // Find obstacles
        List<Obstacle> obstacles = identifyObstacles(viewpoint);
        securityAnalysis.put("obstacleCount", obstacles.size());
        
        // Analyze line of sight vulnerabilities
        for (SceneNode node : visibleNodes) {
            if (isSecurityThreat(node)) {
                Map<String, Object> vulnerability = new HashMap<>();
                vulnerability.put("type", "line_of_sight");
                vulnerability.put("severity", 0.8f);
                vulnerability.put("sourceId", node.id);
                vulnerability.put("sourceName", node.name);
                vulnerability.put("distance", calculateDistance(
                    viewpoint.coordinates.get("x"), viewpoint.coordinates.get("y"), viewpoint.coordinates.get("z"),
                    node.coordinates.get("x"), node.coordinates.get("y"), node.coordinates.get("z")
                ));
                
                // Find any partial cover
                List<SceneNode> partialCover = findPartialCover(viewpoint, node);
                vulnerability.put("partialCoverAvailable", !partialCover.isEmpty());
                
                vulnerabilities.add(vulnerability);
            }
        }
        
        // Check for exposed areas
        List<SpatialContext> contexts = identifySpatialContexts();
        for (SpatialContext context : contexts) {
            if (context.type == SpatialContext.ContextType.OPEN_AREA && 
                isNodeInContext(viewpoint, context)) {
                
                Map<String, Object> vulnerability = new HashMap<>();
                vulnerability.put("type", "exposed_position");
                vulnerability.put("severity", 0.7f);
                vulnerability.put("contextId", context.id);
                vulnerability.put("nearestCover", findNearestCover(viewpoint));
                
                vulnerabilities.add(vulnerability);
            }
        }
        
        // Check for high ground disadvantage
        for (SceneNode node : visibleNodes) {
            if (isHigherThan(node, viewpoint) && isSecurityThreat(node)) {
                Map<String, Object> vulnerability = new HashMap<>();
                vulnerability.put("type", "height_disadvantage");
                vulnerability.put("severity", 0.9f);
                vulnerability.put("sourceId", node.id);
                vulnerability.put("sourceName", node.name);
                vulnerability.put("heightDifference", node.coordinates.get("y") - viewpoint.coordinates.get("y"));
                
                vulnerabilities.add(vulnerability);
            }
        }
        
        // Add to result
        securityAnalysis.put("vulnerabilities", vulnerabilities);
        securityAnalysis.put("overallThreatLevel", calculateOverallThreatLevel(vulnerabilities));
        securityAnalysis.put("recommendedActions", generateSecurityRecommendations(vulnerabilities, viewpoint));
        
        return securityAnalysis;
    }
    
    /**
     * Check if a node represents a security threat
     */
    private boolean isSecurityThreat(SceneNode node) {
        // Check node type
        if (node.type == SceneNode.NodeType.NPC) {
            // Check for enemy properties
            return node.properties.containsKey("enemy") || 
                   node.properties.containsKey("hostile") ||
                   node.properties.containsKey("threat");
        }
        
        // Check for threat properties
        return node.properties.containsKey("threat") || 
               node.properties.containsKey("danger") ||
               node.properties.containsKey("hazard");
    }
    
    /**
     * Find partial cover between two nodes
     */
    private List<SceneNode> findPartialCover(SceneNode from, SceneNode to) {
        List<SceneNode> cover = new ArrayList<>();
        
        // Get nodes between the two points
        List<SceneNode> between = findObjectsBetween(from, to);
        
        for (SceneNode node : between) {
            // Check if node could provide cover
            if (node.properties.containsKey("cover") || 
                node.type == SceneNode.NodeType.STRUCTURE ||
                node.dimensions.get("height") >= 1.0f) {
                
                cover.add(node);
            }
        }
        
        return cover;
    }
    
    /**
     * Find nearest cover to a node
     */
    private Map<String, Object> findNearestCover(SceneNode node) {
        Map<String, Object> result = new HashMap<>();
        
        // Default values
        result.put("coverAvailable", false);
        result.put("distance", Float.MAX_VALUE);
        
        // Get all nodes
        Map<String, SceneNode> nodes = sceneGraphAnalyzer.getNodeRegistry();
        
        // Find closest cover
        float closestDistance = Float.MAX_VALUE;
        SceneNode closestCover = null;
        
        for (SceneNode potential : nodes.values()) {
            if (potential == node) continue;
            
            // Check if node could provide cover
            if (potential.properties.containsKey("cover") || 
                potential.type == SceneNode.NodeType.STRUCTURE ||
                potential.dimensions.get("height") >= 1.0f) {
                
                float distance = calculateDistance(
                    node.coordinates.get("x"), node.coordinates.get("y"), node.coordinates.get("z"),
                    potential.coordinates.get("x"), potential.coordinates.get("y"), potential.coordinates.get("z")
                );
                
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestCover = potential;
                }
            }
        }
        
        if (closestCover != null) {
            result.put("coverAvailable", true);
            result.put("distance", closestDistance);
            result.put("coverId", closestCover.id);
            result.put("coverName", closestCover.name);
        }
        
        return result;
    }
    
    /**
     * Check if one node is higher than another
     */
    private boolean isHigherThan(SceneNode a, SceneNode b) {
        return a.coordinates.get("y") > b.coordinates.get("y") + 1.0f; // At least 1 unit higher
    }
    
    /**
     * Check if a node is within a spatial context
     */
    private boolean isNodeInContext(SceneNode node, SpatialContext context) {
        // Get node position
        float nx = node.coordinates.get("x");
        float ny = node.coordinates.get("y");
        float nz = node.coordinates.get("z");
        
        // Get context bounds
        float minX = context.boundingBox.get("minX");
        float maxX = context.boundingBox.get("maxX");
        float minY = context.boundingBox.get("minY");
        float maxY = context.boundingBox.get("maxY");
        float minZ = context.boundingBox.get("minZ");
        float maxZ = context.boundingBox.get("maxZ");
        
        // Check if node is within bounds
        return nx >= minX && nx <= maxX && 
               ny >= minY && ny <= maxY && 
               nz >= minZ && nz <= maxZ;
    }
    
    /**
     * Calculate overall threat level
     */
    private float calculateOverallThreatLevel(List<Map<String, Object>> vulnerabilities) {
        if (vulnerabilities.isEmpty()) return 0.0f;
        
        float maxSeverity = 0.0f;
        float sumSeverity = 0.0f;
        
        for (Map<String, Object> vuln : vulnerabilities) {
            float severity = (Float) vuln.get("severity");
            maxSeverity = Math.max(maxSeverity, severity);
            sumSeverity += severity;
        }
        
        // Weight maximum severity more heavily than average
        return (maxSeverity * 0.7f) + ((sumSeverity / vulnerabilities.size()) * 0.3f);
    }
    
    /**
     * Generate security recommendations
     */
    private List<String> generateSecurityRecommendations(List<Map<String, Object>> vulnerabilities, SceneNode viewpoint) {
        List<String> recommendations = new ArrayList<>();
        
        if (vulnerabilities.isEmpty()) {
            recommendations.add("Current position appears secure.");
            return recommendations;
        }
        
        // Check for line of sight vulnerabilities
        boolean hasLosVulnerability = false;
        for (Map<String, Object> vuln : vulnerabilities) {
            if ("line_of_sight".equals(vuln.get("type"))) {
                hasLosVulnerability = true;
                
                boolean partialCoverAvailable = (Boolean) vuln.get("partialCoverAvailable");
                if (partialCoverAvailable) {
                    recommendations.add("Move to cover to break line of sight with " + vuln.get("sourceName"));
                } else {
                    recommendations.add("Move to a position where " + vuln.get("sourceName") + " cannot see you");
                }
            }
        }
        
        // Check for exposed position
        boolean hasExposedVulnerability = false;
        for (Map<String, Object> vuln : vulnerabilities) {
            if ("exposed_position".equals(vuln.get("type"))) {
                hasExposedVulnerability = true;
                
                Map<String, Object> nearestCover = (Map<String, Object>) vuln.get("nearestCover");
                if ((Boolean) nearestCover.get("coverAvailable")) {
                    recommendations.add("Move to nearby cover at " + nearestCover.get("coverName"));
                } else {
                    recommendations.add("Current area is exposed. Move to a more sheltered position");
                }
                
                break; // Only need one of these recommendations
            }
        }
        
        // Check for height disadvantage
        boolean hasHeightVulnerability = false;
        for (Map<String, Object> vuln : vulnerabilities) {
            if ("height_disadvantage".equals(vuln.get("type"))) {
                hasHeightVulnerability = true;
                recommendations.add("You are at a height disadvantage to " + vuln.get("sourceName") + 
                                    ". Move to higher ground or seek cover.");
                break;
            }
        }
        
        // General recommendation based on threat level
        float threatLevel = calculateOverallThreatLevel(vulnerabilities);
        if (threatLevel > 0.8f) {
            recommendations.add("High security threat detected. Immediate action recommended.");
        } else if (threatLevel > 0.5f) {
            recommendations.add("Moderate security threat detected. Caution advised.");
        } else {
            recommendations.add("Low security threat detected. Remain vigilant.");
        }
        
        return recommendations;
    }
    
    /**
     * Reset component
     */
    public void reset() {
        Log.d(TAG, "Resetting Advanced Spatial Reasoning");
        
        // Clear cached data
        cachedRelationships.clear();
        cachedObstacles.clear();
        contextMap.clear();
        reasoningHistory.clear();
        
        Log.d(TAG, "Advanced Spatial Reasoning reset completed");
    }

    /**
     * Optimize spatial processing based on available resources 
     * @param resourceLevel Resource level (0-5)
     */
    public void optimizeSpatialProcessing(int resourceLevel) {
        Log.d(TAG, "Optimizing spatial processing for resource level: " + resourceLevel);
        
        // Configure processing based on resource availability
        switch (resourceLevel) {
            case 0: // Minimal resources
                // Disable complex features, use basic approximations
                disableComplexFeatures();
                break;
                
            case 1: // Low resources
                // Use simplified calculations with reduced precision
                useSimplifiedCalculations();
                break;
                
            case 2: // Medium resources
                // Balance between accuracy and performance
                balancedProcessingMode();
                break;
                
            case 3: // Standard resources
                // Standard processing with some optimizations
                standardProcessingMode();
                break;
                
            case 4: // High resources
                // Full feature set with minor optimizations
                highQualityProcessingMode();
                break;
                
            case 5: // Maximum resources
                // No optimizations, maximum quality
                maximumQualityProcessingMode();
                break;
        }
        
        // Record optimization in reasoning history
        ReasoningRecord record = new ReasoningRecord(
            "optimization_" + System.currentTimeMillis(),
            "optimize_spatial_processing"
        );
        record.parameters.put("resourceLevel", resourceLevel);
        reasoningHistory.add(record);
    }
    
    /**
     * Configure minimal processing mode (resource level 0)
     */
    private void disableComplexFeatures() {
        // Disable pattern recognition
        // Use simple bounding box calculations only
        // Limit maximum object count
        // Use aggressive caching
        
        // Record configuration
        Log.d(TAG, "Configured for minimal resources - basic functionality only");
    }
    
    /**
     * Configure low resource processing mode (resource level 1)
     */
    private void useSimplifiedCalculations() {
        // Use simplified math approximations
        // Reduce precision of calculations
        // Limit update frequency
        // Enable aggressive timeout for long operations
        
        // Record configuration
        Log.d(TAG, "Configured for low resources - simplified calculations");
    }
    
    /**
     * Configure balanced processing mode (resource level 2)
     */
    private void balancedProcessingMode() {
        // Use balanced settings for precision vs. performance
        // Enable moderate object tracking
        // Use adaptive processing frequency
        
        // Record configuration
        Log.d(TAG, "Configured for medium resources - balanced processing");
    }
    
    /**
     * Configure standard processing mode (resource level 3)
     */
    private void standardProcessingMode() {
        // Standard processing with optimizations for most common cases
        // Enable comprehensive object tracking
        // Allow complex calculations with timeout limits
        
        // Record configuration
        Log.d(TAG, "Configured for standard resources - full feature set with optimizations");
    }
    
    /**
     * Configure high quality processing mode (resource level 4)
     */
    private void highQualityProcessingMode() {
        // High quality processing with minimal optimization
        // Enable complete feature set
        // Use precise calculations
        // Allow complex pattern recognition
        
        // Record configuration
        Log.d(TAG, "Configured for high resources - high quality processing");
    }
    
    /**
     * Configure maximum quality processing mode (resource level 5)
     */
    private void maximumQualityProcessingMode() {
        // Maximum quality, no optimizations
        // Use highest precision calculations
        // No timeout limits on operations
        // Enable all advanced features
        
        // Record configuration
        Log.d(TAG, "Configured for maximum resources - no optimization, maximum quality");
    }
    
    /**
     * Dynamic memory allocation system for optimizing resource usage
     */
    public static class SpatialResourceManager {
        private final int maxMemoryMB;
        private int allocatedMemoryMB;
        private final Map<String, Integer> componentAllocations = new HashMap<>();
        
        public SpatialResourceManager(int maxMemoryMB) {
            this.maxMemoryMB = maxMemoryMB;
            this.allocatedMemoryMB = 0;
        }
        
        /**
         * Allocate memory to a component
         * @param componentId Component identifier
         * @param memoryMB Memory in MB to allocate
         * @return Whether allocation was successful
         */
        public boolean allocateMemory(String componentId, int memoryMB) {
            // Check if we have enough memory
            if (allocatedMemoryMB + memoryMB > maxMemoryMB) {
                return false;
            }
            
            // Add to existing allocation or create new
            if (componentAllocations.containsKey(componentId)) {
                int current = componentAllocations.get(componentId);
                componentAllocations.put(componentId, current + memoryMB);
            } else {
                componentAllocations.put(componentId, memoryMB);
            }
            
            // Update total
            allocatedMemoryMB += memoryMB;
            return true;
        }
        
        /**
         * Release memory from a component
         * @param componentId Component identifier
         * @param memoryMB Memory in MB to release
         */
        public void releaseMemory(String componentId, int memoryMB) {
            if (!componentAllocations.containsKey(componentId)) {
                return;
            }
            
            int current = componentAllocations.get(componentId);
            int toRelease = Math.min(memoryMB, current);
            
            componentAllocations.put(componentId, current - toRelease);
            allocatedMemoryMB -= toRelease;
            
            // Remove component if allocation is zero
            if (componentAllocations.get(componentId) == 0) {
                componentAllocations.remove(componentId);
            }
        }
        
        /**
         * Get total available memory
         * @return Available memory in MB
         */
        public int getAvailableMemory() {
            return maxMemoryMB - allocatedMemoryMB;
        }
        
        /**
         * Get component allocation
         * @param componentId Component identifier
         * @return Allocated memory in MB
         */
        public int getComponentAllocation(String componentId) {
            return componentAllocations.getOrDefault(componentId, 0);
        }
    }
    
    /**
     * Spatial pattern recognition system
     */
    public static class SpatialPatternRecognition {
        private final List<SpatialPattern> knownPatterns = new ArrayList<>();
        private final Map<String, Float> patternConfidence = new HashMap<>();
        
        /**
         * Recognize spatial patterns in the current scene
         * @param nodes List of scene nodes to analyze
         * @return List of recognized patterns with confidence scores
         */
        public List<RecognizedPattern> recognizePatterns(List<SceneNode> nodes) {
            List<RecognizedPattern> recognizedPatterns = new ArrayList<>();
            
            for (SpatialPattern pattern : knownPatterns) {
                float confidence = pattern.matchConfidence(nodes);
                if (confidence > 0.4f) {
                    recognizedPatterns.add(new RecognizedPattern(pattern.name, confidence));
                }
            }
            
            return recognizedPatterns;
        }
        
        /**
         * Add a new pattern to the recognition system
         * @param pattern Pattern to add
         */
        public void addPattern(SpatialPattern pattern) {
            if (!containsPattern(pattern.name)) {
                knownPatterns.add(pattern);
                patternConfidence.put(pattern.name, 0.5f);
            }
        }
        
        /**
         * Update pattern confidence based on feedback
         * @param patternName Pattern name
         * @param wasCorrect Whether the recognition was correct
         */
        public void updatePatternConfidence(String patternName, boolean wasCorrect) {
            if (patternConfidence.containsKey(patternName)) {
                float currentConfidence = patternConfidence.get(patternName);
                float adjustment = wasCorrect ? 0.05f : -0.05f;
                patternConfidence.put(patternName, 
                        Math.max(0.1f, Math.min(0.95f, currentConfidence + adjustment)));
            }
        }
        
        /**
         * Check if pattern exists in the system
         * @param patternName Pattern name
         * @return True if the pattern exists
         */
        public boolean containsPattern(String patternName) {
            for (SpatialPattern pattern : knownPatterns) {
                if (pattern.name.equals(patternName)) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Spatial pattern definition
         */
        public static class SpatialPattern {
            public final String name;
            public final List<PatternNode> nodes = new ArrayList<>();
            public final List<PatternRelationship> relationships = new ArrayList<>();
            
            public SpatialPattern(String name) {
                this.name = name;
            }
            
            /**
             * Add a node to the pattern
             * @param id Node identifier
             * @param type Node type
             * @return This pattern for chaining
             */
            public SpatialPattern addNode(String id, String type) {
                nodes.add(new PatternNode(id, type));
                return this;
            }
            
            /**
             * Add a relationship to the pattern
             * @param nodeA First node identifier
             * @param nodeB Second node identifier
             * @param relationType Relationship type
             * @return This pattern for chaining
             */
            public SpatialPattern addRelationship(String nodeA, String nodeB, String relationType) {
                relationships.add(new PatternRelationship(nodeA, nodeB, relationType));
                return this;
            }
            
            /**
             * Calculate match confidence for a set of nodes
             * @param sceneNodes Scene nodes to match against
             * @return Confidence score (0-1)
             */
            public float matchConfidence(List<SceneNode> sceneNodes) {
                // This would contain sophisticated pattern matching logic
                // For now, return a placeholder value
                return 0.5f;
            }
        }
        
        /**
         * Node in a pattern definition
         */
        public static class PatternNode {
            public final String id;
            public final String type;
            
            public PatternNode(String id, String type) {
                this.id = id;
                this.type = type;
            }
        }
        
        /**
         * Relationship in a pattern definition
         */
        public static class PatternRelationship {
            public final String nodeAId;
            public final String nodeBId;
            public final String relationType;
            
            public PatternRelationship(String nodeAId, String nodeBId, String relationType) {
                this.nodeAId = nodeAId;
                this.nodeBId = nodeBId;
                this.relationType = relationType;
            }
        }
        
        /**
         * Recognized pattern with confidence score
         */
        public static class RecognizedPattern {
            public final String patternName;
            public final float confidence;
            
            public RecognizedPattern(String patternName, float confidence) {
                this.patternName = patternName;
                this.confidence = confidence;
            }
        }
    }
}
