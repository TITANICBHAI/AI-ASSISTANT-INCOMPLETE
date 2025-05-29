package com.aiassistant.test;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.analysis.scene.SceneGraphAnalyzer;
import com.aiassistant.core.analysis.scene.SceneGraphAnalyzer.SceneNode;
import com.aiassistant.core.analysis.spatial.reasoning.AdvancedSpatialReasoning;
import com.aiassistant.core.analysis.spatial.reasoning.AdvancedSpatialReasoning.SpatialRelationship;
import com.aiassistant.core.analysis.spatial.protection.SecurityProtectionSystem;
import com.aiassistant.core.analysis.spatial.protection.SecurityProtectionSystem.SecurityThreat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for the Advanced Spatial Reasoning component
 */
public class SpatialReasoningTest {
    private static final String TAG = "SpatialReasoningTest";
    
    // Component instances
    private final SceneGraphAnalyzer sceneAnalyzer;
    private final AdvancedSpatialReasoning spatialReasoning;
    private final SecurityProtectionSystem securitySystem;
    
    public SpatialReasoningTest(Context context) {
        // Get component instances
        sceneAnalyzer = SceneGraphAnalyzer.getInstance();
        spatialReasoning = AdvancedSpatialReasoning.getInstance();
        securitySystem = SecurityProtectionSystem.getInstance();
        
        Log.d(TAG, "Spatial Reasoning Test initialized");
    }
    
    /**
     * Run a basic test of spatial reasoning capabilities
     */
    public void runTest() {
        Log.d(TAG, "Running spatial reasoning test");
        
        // Create test scene
        createTestScene();
        
        // Test spatial relationships
        testSpatialRelationships();
        
        // Test occlusion analysis
        testOcclusions();
        
        // Test security analysis
        testSecurityAnalysis();
        
        // Test threat response
        testThreatResponse();
        
        Log.d(TAG, "Spatial reasoning test completed");
    }
    
    /**
     * Create a test scene with various objects
     */
    private void createTestScene() {
        Log.d(TAG, "Creating test scene");
        
        // Create scene root
        SceneNode root = new SceneNode("scene_root", "Root", SceneNode.NodeType.ROOT);
        root.coordinates.put("x", 0.0f);
        root.coordinates.put("y", 0.0f);
        root.coordinates.put("z", 0.0f);
        root.dimensions.put("width", 100.0f);
        root.dimensions.put("height", 10.0f);
        root.dimensions.put("depth", 100.0f);
        root.properties.put("type", "room");
        
        // Add root node
        sceneAnalyzer.addNode(root);
        
        // Create player node
        SceneNode player = new SceneNode("player", "Player", SceneNode.NodeType.PLAYER);
        player.coordinates.put("x", 0.0f);
        player.coordinates.put("y", 1.7f);
        player.coordinates.put("z", 0.0f);
        player.dimensions.put("width", 0.5f);
        player.dimensions.put("height", 1.7f);
        player.dimensions.put("depth", 0.5f);
        player.importance = 1.0f;
        
        // Add player node
        sceneAnalyzer.addNode(player);
        
        // Create some walls
        createWall("wall_north", 0.0f, 2.0f, -50.0f, 100.0f, 4.0f, 1.0f);
        createWall("wall_south", 0.0f, 2.0f, 50.0f, 100.0f, 4.0f, 1.0f);
        createWall("wall_east", 50.0f, 2.0f, 0.0f, 1.0f, 4.0f, 100.0f);
        createWall("wall_west", -50.0f, 2.0f, 0.0f, 1.0f, 4.0f, 100.0f);
        
        // Create some objects
        createObject("table", "Table", 3.0f, 0.8f, 5.0f, 2.0f, 0.8f, 1.5f, SceneNode.NodeType.STRUCTURE);
        createObject("chair", "Chair", 4.5f, 0.5f, 5.5f, 0.6f, 1.0f, 0.6f, SceneNode.NodeType.STRUCTURE);
        createObject("box", "Box", 2.0f, 1.5f, 5.0f, 0.5f, 0.5f, 0.5f, SceneNode.NodeType.ITEM);
        createObject("book", "Book", 3.0f, 1.3f, 5.0f, 0.3f, 0.05f, 0.2f, SceneNode.NodeType.ITEM);
        
        // Create an enemy
        SceneNode enemy = new SceneNode("enemy", "Enemy", SceneNode.NodeType.NPC);
        enemy.coordinates.put("x", 10.0f);
        enemy.coordinates.put("y", 1.7f);
        enemy.coordinates.put("z", -10.0f);
        enemy.dimensions.put("width", 0.5f);
        enemy.dimensions.put("height", 1.7f);
        enemy.dimensions.put("depth", 0.5f);
        enemy.properties.put("hostile", true);
        enemy.properties.put("enemy", true);
        enemy.importance = 0.9f;
        
        // Add enemy node
        sceneAnalyzer.addNode(enemy);
        
        // Create a cover object
        SceneNode cover = new SceneNode("cover", "Cover", SceneNode.NodeType.STRUCTURE);
        cover.coordinates.put("x", 5.0f);
        cover.coordinates.put("y", 1.0f);
        cover.coordinates.put("z", -5.0f);
        cover.dimensions.put("width", 3.0f);
        cover.dimensions.put("height", 2.0f);
        cover.dimensions.put("depth", 0.5f);
        cover.properties.put("cover", true);
        
        // Add cover node
        sceneAnalyzer.addNode(cover);
        
        // Add relationships
        sceneAnalyzer.addRelationship("player_in_room", "INSIDE", "player", "scene_root");
        sceneAnalyzer.addRelationship("enemy_in_room", "INSIDE", "enemy", "scene_root");
        sceneAnalyzer.addRelationship("table_in_room", "INSIDE", "table", "scene_root");
        sceneAnalyzer.addRelationship("box_on_table", "ON", "box", "table");
        sceneAnalyzer.addRelationship("book_on_table", "ON", "book", "table");
        
        Log.d(TAG, "Test scene created with " + sceneAnalyzer.getNodeRegistry().size() + " nodes");
    }
    
    /**
     * Create a wall object
     */
    private void createWall(String id, float x, float y, float z, float width, float height, float depth) {
        SceneNode wall = new SceneNode(id, id, SceneNode.NodeType.STRUCTURE);
        wall.coordinates.put("x", x);
        wall.coordinates.put("y", y);
        wall.coordinates.put("z", z);
        wall.dimensions.put("width", width);
        wall.dimensions.put("height", height);
        wall.dimensions.put("depth", depth);
        wall.properties.put("solid", true);
        
        sceneAnalyzer.addNode(wall);
        sceneAnalyzer.addRelationship(id + "_in_room", "INSIDE", id, "scene_root");
    }
    
    /**
     * Create a generic object
     */
    private void createObject(String id, String name, float x, float y, float z, 
                             float width, float height, float depth, SceneNode.NodeType type) {
        SceneNode obj = new SceneNode(id, name, type);
        obj.coordinates.put("x", x);
        obj.coordinates.put("y", y);
        obj.coordinates.put("z", z);
        obj.dimensions.put("width", width);
        obj.dimensions.put("height", height);
        obj.dimensions.put("depth", depth);
        
        sceneAnalyzer.addNode(obj);
    }
    
    /**
     * Test spatial relationship analysis
     */
    private void testSpatialRelationships() {
        Log.d(TAG, "Testing spatial relationships");
        
        // Get nodes
        SceneNode table = sceneAnalyzer.getNode("table");
        SceneNode box = sceneAnalyzer.getNode("box");
        SceneNode book = sceneAnalyzer.getNode("book");
        
        if (table == null || box == null || book == null) {
            Log.e(TAG, "Missing test nodes");
            return;
        }
        
        // Analyze relationships between box and table
        List<SpatialRelationship> boxTableRelations = spatialReasoning.analyzeSpatialRelationships(box, table);
        Log.d(TAG, "Found " + boxTableRelations.size() + " relationships between box and table");
        
        for (SpatialRelationship rel : boxTableRelations) {
            Log.d(TAG, "  Relationship: " + rel.id + " - Type: " + rel.type);
        }
        
        // Analyze relationships between book and box
        List<SpatialRelationship> bookBoxRelations = spatialReasoning.analyzeSpatialRelationships(book, box);
        Log.d(TAG, "Found " + bookBoxRelations.size() + " relationships between book and box");
        
        for (SpatialRelationship rel : bookBoxRelations) {
            Log.d(TAG, "  Relationship: " + rel.id + " - Type: " + rel.type);
        }
    }
    
    /**
     * Test occlusion analysis
     */
    private void testOcclusions() {
        Log.d(TAG, "Testing occlusion analysis");
        
        // Get nodes
        SceneNode player = sceneAnalyzer.getNode("player");
        SceneNode enemy = sceneAnalyzer.getNode("enemy");
        
        if (player == null || enemy == null) {
            Log.e(TAG, "Missing player or enemy node");
            return;
        }
        
        // Analyze occlusions between player and enemy
        List<SpatialRelationship> occlusions = spatialReasoning.analyzeOcclusions(player, enemy);
        Log.d(TAG, "Found " + occlusions.size() + " occlusions between player and enemy");
        
        for (SpatialRelationship rel : occlusions) {
            Log.d(TAG, "  Occlusion: " + rel.id + " - Type: " + rel.type);
            Log.d(TAG, "  Occlusion strength: " + rel.metrics.get("occlusion_strength"));
        }
        
        // Find objects between player and enemy
        List<SceneNode> betweenObjects = spatialReasoning.findObjectsBetween(player, enemy);
        Log.d(TAG, "Found " + betweenObjects.size() + " objects between player and enemy");
        
        for (SceneNode node : betweenObjects) {
            Log.d(TAG, "  Between object: " + node.id + " - " + node.name);
        }
    }
    
    /**
     * Test security analysis
     */
    private void testSecurityAnalysis() {
        Log.d(TAG, "Testing security analysis");
        
        // Get player node for viewpoint
        SceneNode player = sceneAnalyzer.getNode("player");
        
        if (player == null) {
            Log.e(TAG, "Missing player node");
            return;
        }
        
        // Perform security analysis from player viewpoint
        Map<String, Object> securityAnalysis = spatialReasoning.analyzeSecurityContext(player);
        
        // Log key results
        Log.d(TAG, "Security analysis results:");
        Log.d(TAG, "  Visible nodes: " + securityAnalysis.get("visibleNodeCount"));
        Log.d(TAG, "  Obstacle count: " + securityAnalysis.get("obstacleCount"));
        Log.d(TAG, "  Overall threat level: " + securityAnalysis.get("overallThreatLevel"));
        
        // Log vulnerabilities
        List<Map<String, Object>> vulnerabilities = 
            (List<Map<String, Object>>) securityAnalysis.get("vulnerabilities");
        
        if (vulnerabilities != null) {
            Log.d(TAG, "  Vulnerabilities: " + vulnerabilities.size());
            
            for (Map<String, Object> vuln : vulnerabilities) {
                Log.d(TAG, "    Type: " + vuln.get("type") + " - Severity: " + vuln.get("severity"));
            }
        }
        
        // Log recommendations
        List<String> recommendations = (List<String>) securityAnalysis.get("recommendedActions");
        
        if (recommendations != null) {
            Log.d(TAG, "  Recommendations:");
            
            for (String rec : recommendations) {
                Log.d(TAG, "    " + rec);
            }
        }
    }
    
    /**
     * Test threat response
     */
    private void testThreatResponse() {
        Log.d(TAG, "Testing threat response");
        
        // Create a simulated attack
        Map<String, Object> attackInfo = new HashMap<>();
        attackInfo.put("type", "memory_scan");
        attackInfo.put("severity", 0.8f);
        attackInfo.put("source", "external");
        attackInfo.put("target", "memory_region_critical");
        
        // Log current security level
        Log.d(TAG, "Current security level: " + securitySystem.getCurrentSecurityLevel());
        
        // Respond to the attack
        securitySystem.respondToAttack(attackInfo);
        
        // Log new security level
        Log.d(TAG, "New security level: " + securitySystem.getCurrentSecurityLevel());
        
        // Get active threats
        List<SecurityThreat> activeThreats = securitySystem.getActiveThreats();
        Log.d(TAG, "Active threats: " + activeThreats.size());
        
        for (SecurityThreat threat : activeThreats) {
            Log.d(TAG, "  Threat: " + threat.id + " - Type: " + threat.type);
            Log.d(TAG, "  Severity: " + threat.severity + " - Mitigated: " + threat.mitigated);
            
            if (threat.mitigated) {
                Log.d(TAG, "  Mitigation method: " + threat.mitigationMethod);
            }
        }
    }
}
