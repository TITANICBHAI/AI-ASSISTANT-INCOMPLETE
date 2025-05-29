package com.aiassistant.core.ai.neural;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Neural network model for tactical situation analysis in games.
 * This model provides tactical insights and advantage calculations.
 */
public class TacticalAnalysisModel extends BaseTFLiteModel {
    private static final String TAG = "TacticalAnalysisModel";
    private static final String VERSION = "1.0";
    private static final String DESCRIPTION = "Model for tactical situation analysis in game environments";
    
    // Model configuration
    private static final int MAX_ENTITIES = 16;      // Maximum number of entities to analyze
    private static final int ENTITY_FEATURES = 12;   // Features per entity
    private static final int SITUATION_FEATURES = 32; // Situation features
    
    /**
     * Constructor
     * @param modelName Model name
     */
    public TacticalAnalysisModel(String modelName) {
        super(modelName);
        this.modelPath = "models/tactical_analysis.tflite";
    }
    
    @Override
    public String getModelVersion() {
        return VERSION;
    }
    
    @Override
    public String getModelDescription() {
        return DESCRIPTION;
    }
    
    /**
     * Analyze tactical situation based on entities and environment
     * @param entities Array of game entities (player, enemies, objects)
     * @param environment Current environment data
     * @return Tactical analysis result
     */
    public TacticalAnalysisResult analyzeTacticalSituation(Entity[] entities, Environment environment) {
        if (!isReady()) {
            Log.e(TAG, "Model not initialized");
            return new TacticalAnalysisResult();
        }
        
        if (entities == null || entities.length == 0) {
            Log.e(TAG, "No entities provided for tactical analysis");
            return new TacticalAnalysisResult();
        }
        
        try {
            // Extract features from entities and environment
            float[] features = extractTacticalFeatures(entities, environment);
            
            // Load data into input buffer
            inputBuffer.rewind();
            for (float value : features) {
                inputBuffer.putFloat(value);
            }
            
            // Prepare output buffer for tactical insights
            // Output format depends on the specific tactical metrics needed
            float[][] output = new float[1][64];
            
            // Run inference
            interpreter.run(inputBuffer, output);
            
            // Process output into tactical insights
            return processTacticalOutput(output[0], entities, environment);
            
        } catch (Exception e) {
            Log.e(TAG, "Error during tactical analysis: " + e.getMessage());
            return new TacticalAnalysisResult();
        }
    }
    
    /**
     * Extract features from entities and environment for model input
     * @param entities Array of game entities
     * @param environment Current environment data
     * @return Feature array for model input
     */
    private float[] extractTacticalFeatures(Entity[] entities, Environment environment) {
        // Calculate total feature size
        int numEntities = Math.min(entities.length, MAX_ENTITIES);
        int totalFeatureSize = (numEntities * ENTITY_FEATURES) + SITUATION_FEATURES;
        float[] features = new float[totalFeatureSize];
        
        // Default to zeros
        Arrays.fill(features, 0.0f);
        
        // Extract environment features first
        if (environment != null) {
            int offset = 0;
            
            // Physical environment features (8 features)
            features[offset++] = normalizeValue(environment.visibility, 0, 100);
            features[offset++] = normalizeValue(environment.cover, 0, 100);
            features[offset++] = normalizeValue(environment.elevation, -100, 100);
            features[offset++] = normalizeValue(environment.terrain, 0, TerrainType.values().length - 1);
            features[offset++] = normalizeValue(environment.obstacles, 0, 100);
            features[offset++] = normalizeValue(environment.size, 0, 1000);
            features[offset++] = normalizeValue(environment.complexity, 0, 100);
            features[offset++] = normalizeValue(environment.timeOfDay, 0, 24);
            
            // Resource features (8 features)
            features[offset++] = normalizeValue(environment.resourceDensity, 0, 100);
            features[offset++] = normalizeValue(environment.defensivePositions, 0, 100);
            features[offset++] = normalizeValue(environment.chokepoints, 0, 10);
            features[offset++] = normalizeValue(environment.exitPoints, 0, 10);
            features[offset++] = normalizeValue(environment.highGround, 0, 100);
            features[offset++] = normalizeValue(environment.dangerZones, 0, 100);
            features[offset++] = normalizeValue(environment.noise, 0, 100);
            features[offset++] = normalizeValue(environment.weather, 0, WeatherType.values().length - 1);
            
            // Tactical situation features (16 features)
            features[offset++] = normalizeValue(environment.combatIntensity, 0, 100);
            features[offset++] = normalizeValue(environment.timeConstraint, 0, 100);
            features[offset++] = normalizeValue(environment.enemyAwareness, 0, 100);
            features[offset++] = normalizeValue(environment.playerDetected, 0, 1);
            features[offset++] = normalizeValue(environment.reinforcementChance, 0, 100);
            features[offset++] = normalizeValue(environment.tacticalDifficulty, 0, 100);
            features[offset++] = normalizeValue(environment.ambushRisk, 0, 100);
            features[offset++] = normalizeValue(environment.escapeRoutes, 0, 10);
            
            // Additional tactical features
            features[offset++] = normalizeValue(environment.territoryControl, -100, 100);
            features[offset++] = normalizeValue(environment.flankingOpportunities, 0, 100);
            features[offset++] = normalizeValue(environment.supplyLines, 0, 100);
            features[offset++] = normalizeValue(environment.informationVisibility, 0, 100);
            features[offset++] = normalizeValue(environment.environmentHazards, 0, 100);
            features[offset++] = normalizeValue(environment.distanceToObjective, 0, 1000);
            features[offset++] = normalizeValue(environment.objectiveDefense, 0, 100);
            features[offset++] = normalizeValue(environment.objectiveValue, 0, 100);
        }
        
        // Extract features for each entity
        for (int i = 0; i < numEntities; i++) {
            Entity entity = entities[i];
            int baseOffset = SITUATION_FEATURES + (i * ENTITY_FEATURES);
            
            // Entity type (one-hot encoding across first 4 features)
            int typeOrdinal = Math.min(entity.type.ordinal(), 3);
            for (int j = 0; j < 4; j++) {
                features[baseOffset + j] = (j == typeOrdinal) ? 1.0f : 0.0f;
            }
            
            // Entity position (normalized to 0-1 based on environment size)
            float normalizedX = 0.5f;
            float normalizedY = 0.5f;
            float normalizedZ = 0.5f;
            
            if (environment != null && environment.size > 0) {
                normalizedX = (entity.positionX + (environment.size / 2)) / environment.size;
                normalizedY = (entity.positionY + (environment.size / 2)) / environment.size;
                normalizedZ = (entity.positionZ + (environment.size / 2)) / environment.size;
            }
            
            features[baseOffset + 4] = normalizedX;
            features[baseOffset + 5] = normalizedY;
            features[baseOffset + 6] = normalizedZ;
            
            // Entity attributes
            features[baseOffset + 7] = normalizeValue(entity.health, 0, 100);
            features[baseOffset + 8] = normalizeValue(entity.attackPower, 0, 100);
            features[baseOffset + 9] = normalizeValue(entity.defense, 0, 100);
            features[baseOffset + 10] = normalizeValue(entity.mobility, 0, 100);
            features[baseOffset + 11] = normalizeValue(entity.awareness, 0, 100);
        }
        
        return features;
    }
    
    /**
     * Normalize a value to 0-1 range
     */
    private float normalizeValue(float value, float min, float max) {
        if (max == min) return 0.5f;
        return Math.max(0.0f, Math.min(1.0f, (value - min) / (max - min)));
    }
    
    /**
     * Process model output into tactical analysis result
     * @param output Raw model output
     * @param entities Original entities array
     * @param environment Original environment data
     * @return Structured tactical analysis result
     */
    private TacticalAnalysisResult processTacticalOutput(float[] output, 
                                                        Entity[] entities, 
                                                        Environment environment) {
        TacticalAnalysisResult result = new TacticalAnalysisResult();
        
        // Overall tactical metrics (first 8 outputs)
        result.overallAdvantage = (output[0] * 2) - 1;           // Convert 0-1 to -1 to 1
        result.positionalAdvantage = (output[1] * 2) - 1;        // Convert 0-1 to -1 to 1
        result.resourceAdvantage = (output[2] * 2) - 1;          // Convert 0-1 to -1 to 1
        result.combatReadiness = output[3];
        result.detectionRisk = output[4];
        result.environmentalThreat = output[5];
        result.timeAdvantage = (output[6] * 2) - 1;              // Convert 0-1 to -1 to 1
        result.surprisePotential = output[7];
        
        // Tactical insights (next 8 outputs)
        result.optimalRangeEngagement = output[8] * 100;         // Convert to 0-100 scale
        result.escapeViability = output[9];
        result.counterAttackPotential = output[10];
        result.defensivePositionQuality = output[11];
        result.offensiveOpportunity = output[12];
        result.ambushVulnerability = output[13];
        result.territorialControl = (output[14] * 2) - 1;        // Convert 0-1 to -1 to 1
        result.reinforcementImpact = output[15];
        
        // Entity-specific tactical ratings (next MAX_ENTITIES outputs)
        int numEntities = Math.min(entities.length, MAX_ENTITIES);
        result.entityTacticalRatings = new float[numEntities];
        for (int i = 0; i < numEntities; i++) {
            result.entityTacticalRatings[i] = output[16 + i];
        }
        
        // Key position analysis (next 3 outputs are coordinates of key positions)
        result.keyPositions = new ArrayList<>(3);
        
        int posOffset = 16 + MAX_ENTITIES;
        for (int i = 0; i < 3; i++) {
            float x = (output[posOffset + (i * 3)] - 0.5f) * environment.size;
            float y = (output[posOffset + (i * 3) + 1] - 0.5f) * environment.size;
            float z = (output[posOffset + (i * 3) + 2] - 0.5f) * environment.size;
            
            KeyPosition keyPos = new KeyPosition(x, y, z);
            
            // Determine position type based on values and position
            // These are simplified heuristics - in a real model the type would be predicted
            if (i == 0) {
                keyPos.type = KeyPositionType.DEFENSIVE;
            } else if (i == 1) {
                keyPos.type = KeyPositionType.OFFENSIVE;
            } else {
                keyPos.type = KeyPositionType.STRATEGIC;
            }
            
            // Set importance based on the i value (first position most important)
            keyPos.importance = 1.0f - (i * 0.25f);
            
            result.keyPositions.add(keyPos);
        }
        
        // Identify threats and opportunities
        result.identifiedThreats = new ArrayList<>();
        result.identifiedOpportunities = new ArrayList<>();
        
        for (int i = 0; i < numEntities; i++) {
            Entity entity = entities[i];
            
            if (entity.type == EntityType.ENEMY) {
                // Check if this enemy is a significant threat
                if (result.entityTacticalRatings[i] > 0.7f) {
                    result.identifiedThreats.add(new TacticalInsight(
                            InsightType.HIGH_THREAT,
                            "High-threat enemy at (" + 
                                    Math.round(entity.positionX) + ", " + 
                                    Math.round(entity.positionY) + ", " + 
                                    Math.round(entity.positionZ) + ")",
                            entity,
                            result.entityTacticalRatings[i]
                    ));
                } else if (entity.health < 30 && entity.attackPower > 70) {
                    result.identifiedOpportunities.add(new TacticalInsight(
                            InsightType.VULNERABLE_TARGET,
                            "Vulnerable high-damage enemy",
                            entity,
                            0.8f
                    ));
                }
            } else if (entity.type == EntityType.RESOURCE) {
                if (result.entityTacticalRatings[i] > 0.6f) {
                    result.identifiedOpportunities.add(new TacticalInsight(
                            InsightType.CRITICAL_RESOURCE,
                            "High-value resource available",
                            entity,
                            result.entityTacticalRatings[i]
                    ));
                }
            }
        }
        
        // Generate tactical recommendations based on analysis
        result.tacticalRecommendations = generateRecommendations(result, entities, environment);
        
        return result;
    }
    
    /**
     * Generate tactical recommendations based on the analysis
     */
    private List<TacticalRecommendation> generateRecommendations(
            TacticalAnalysisResult analysis, 
            Entity[] entities,
            Environment environment) {
        
        List<TacticalRecommendation> recommendations = new ArrayList<>();
        
        // Find player entity
        Entity player = null;
        for (Entity entity : entities) {
            if (entity.type == EntityType.PLAYER) {
                player = entity;
                break;
            }
        }
        
        if (player == null) {
            return recommendations;
        }
        
        // Based on overall advantage
        if (analysis.overallAdvantage < -0.3f) {
            // At a significant disadvantage
            recommendations.add(new TacticalRecommendation(
                    RecommendationType.RETREAT,
                    "Significant tactical disadvantage. Consider retreat or repositioning.",
                    0.8f
            ));
        } else if (analysis.overallAdvantage > 0.3f) {
            // At a significant advantage
            recommendations.add(new TacticalRecommendation(
                    RecommendationType.PRESS_ADVANTAGE,
                    "Strong tactical advantage. Press forward and engage.",
                    0.8f
            ));
        }
        
        // Based on positional analysis
        if (analysis.positionalAdvantage < -0.2f) {
            // Poor position
            if (analysis.keyPositions.size() > 0) {
                KeyPosition bestPos = analysis.keyPositions.get(0);
                recommendations.add(new TacticalRecommendation(
                        RecommendationType.REPOSITION,
                        "Current position is vulnerable. Move to coordinates: (" + 
                                Math.round(bestPos.x) + ", " + 
                                Math.round(bestPos.y) + ", " + 
                                Math.round(bestPos.z) + ")",
                        0.7f
                ));
            }
        }
        
        // Based on detection risk
        if (analysis.detectionRisk > 0.7f) {
            recommendations.add(new TacticalRecommendation(
                    RecommendationType.STEALTH,
                    "High detection risk. Reduce movement and use cover.",
                    0.9f
            ));
        }
        
        // Based on resource status
        if (analysis.resourceAdvantage < -0.3f) {
            // Resource disadvantage
            for (Entity entity : entities) {
                if (entity.type == EntityType.RESOURCE) {
                    recommendations.add(new TacticalRecommendation(
                            RecommendationType.GATHER,
                            "Resource disadvantage. Prioritize gathering nearby resources.",
                            0.7f
                    ));
                    break;
                }
            }
        }
        
        // Based on combat readiness
        if (analysis.combatReadiness < 0.4f && analysis.detectionRisk < 0.5f) {
            recommendations.add(new TacticalRecommendation(
                    RecommendationType.PREPARE,
                    "Low combat readiness. Take time to prepare before engaging.",
                    0.8f
            ));
        }
        
        // Based on opportunity assessment
        if (analysis.offensiveOpportunity > 0.7f) {
            recommendations.add(new TacticalRecommendation(
                    RecommendationType.ATTACK,
                    "Strong offensive opportunity. Consider attacking now.",
                    0.8f
            ));
        }
        
        return recommendations;
    }
    
    /**
     * Types of game entities
     */
    public enum EntityType {
        PLAYER,
        ALLY,
        ENEMY,
        RESOURCE,
        OBJECT,
        OTHER
    }
    
    /**
     * Types of terrain
     */
    public enum TerrainType {
        FLAT,
        HILLS,
        MOUNTAINS,
        FOREST,
        URBAN,
        WATER,
        DESERT,
        UNDERGROUND
    }
    
    /**
     * Types of weather
     */
    public enum WeatherType {
        CLEAR,
        RAIN,
        FOG,
        SNOW,
        STORM
    }
    
    /**
     * Types of key positions
     */
    public enum KeyPositionType {
        DEFENSIVE,
        OFFENSIVE,
        STRATEGIC,
        RESOURCE,
        ESCAPE
    }
    
    /**
     * Types of tactical insights
     */
    public enum InsightType {
        HIGH_THREAT,
        VULNERABLE_TARGET,
        CRITICAL_RESOURCE,
        ENVIRONMENTAL_HAZARD,
        CHOKEPOINT,
        AMBUSH_VULNERABILITY
    }
    
    /**
     * Types of tactical recommendations
     */
    public enum RecommendationType {
        ATTACK,
        DEFEND,
        RETREAT,
        FLANK,
        REPOSITION,
        GATHER,
        STEALTH,
        PREPARE,
        PRESS_ADVANTAGE
    }
    
    /**
     * Game entity information
     */
    public static class Entity {
        public EntityType type;
        public float positionX, positionY, positionZ;
        public float health;
        public float attackPower;
        public float defense;
        public float mobility;
        public float awareness;
        
        public Entity(EntityType type, float x, float y, float z) {
            this.type = type;
            this.positionX = x;
            this.positionY = y;
            this.positionZ = z;
            this.health = 100;
            this.attackPower = 50;
            this.defense = 50;
            this.mobility = 50;
            this.awareness = 50;
        }
    }
    
    /**
     * Environment and situation data
     */
    public static class Environment {
        // Physical environment
        public float visibility;
        public float cover;
        public float elevation;
        public int terrain;
        public float obstacles;
        public float size;
        public float complexity;
        public float timeOfDay;
        
        // Resource aspects
        public float resourceDensity;
        public float defensivePositions;
        public float chokepoints;
        public float exitPoints;
        public float highGround;
        public float dangerZones;
        public float noise;
        public int weather;
        
        // Tactical situation
        public float combatIntensity;
        public float timeConstraint;
        public float enemyAwareness;
        public float playerDetected;
        public float reinforcementChance;
        public float tacticalDifficulty;
        public float ambushRisk;
        public float escapeRoutes;
        
        // Additional tactical features
        public float territoryControl;
        public float flankingOpportunities;
        public float supplyLines;
        public float informationVisibility;
        public float environmentHazards;
        public float distanceToObjective;
        public float objectiveDefense;
        public float objectiveValue;
        
        public Environment() {
            // Default values
            visibility = 80;
            cover = 50;
            elevation = 0;
            terrain = TerrainType.FLAT.ordinal();
            obstacles = 20;
            size = 500;
            complexity = 50;
            timeOfDay = 12;
            
            resourceDensity = 50;
            defensivePositions = 50;
            chokepoints = 2;
            exitPoints = 4;
            highGround = 30;
            dangerZones = 20;
            noise = 20;
            weather = WeatherType.CLEAR.ordinal();
            
            combatIntensity = 0;
            timeConstraint = 30;
            enemyAwareness = 50;
            playerDetected = 0;
            reinforcementChance = 20;
            tacticalDifficulty = 50;
            ambushRisk = 30;
            escapeRoutes = 3;
            
            territoryControl = 0;
            flankingOpportunities = 50;
            supplyLines = 80;
            informationVisibility = 70;
            environmentHazards = 10;
            distanceToObjective = 200;
            objectiveDefense = 50;
            objectiveValue = 70;
        }
    }
    
    /**
     * Key position in tactical analysis
     */
    public static class KeyPosition {
        public float x, y, z;
        public KeyPositionType type;
        public float importance;
        
        public KeyPosition(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = KeyPositionType.STRATEGIC;
            this.importance = 0.5f;
        }
        
        @Override
        public String toString() {
            return type + " position at (" + 
                   Math.round(x) + ", " + 
                   Math.round(y) + ", " + 
                   Math.round(z) + "), " +
                   "Importance: " + Math.round(importance * 100) + "%";
        }
    }
    
    /**
     * Tactical insight about specific entities or situations
     */
    public static class TacticalInsight {
        public InsightType type;
        public String description;
        public Entity relatedEntity;
        public float importance;
        
        public TacticalInsight(InsightType type, String description, Entity relatedEntity, float importance) {
            this.type = type;
            this.description = description;
            this.relatedEntity = relatedEntity;
            this.importance = importance;
        }
        
        @Override
        public String toString() {
            return type + ": " + description + 
                   " (Importance: " + Math.round(importance * 100) + "%)";
        }
    }
    
    /**
     * Tactical recommendation for player action
     */
    public static class TacticalRecommendation {
        public RecommendationType type;
        public String description;
        public float confidence;
        
        public TacticalRecommendation(RecommendationType type, String description, float confidence) {
            this.type = type;
            this.description = description;
            this.confidence = confidence;
        }
        
        @Override
        public String toString() {
            return type + ": " + description + 
                   " (Confidence: " + Math.round(confidence * 100) + "%)";
        }
    }
    
    /**
     * Result of tactical analysis
     */
    public static class TacticalAnalysisResult {
        // Overall tactical assessment
        public float overallAdvantage;        // -1 to 1 (disadvantage to advantage)
        public float positionalAdvantage;     // -1 to 1 (disadvantage to advantage)
        public float resourceAdvantage;       // -1 to 1 (disadvantage to advantage)
        public float combatReadiness;         // 0 to 1 scale
        public float detectionRisk;           // 0 to 1 scale
        public float environmentalThreat;     // 0 to 1 scale
        public float timeAdvantage;           // -1 to 1 (disadvantage to advantage)
        public float surprisePotential;       // 0 to 1 scale
        
        // Tactical insights
        public float optimalRangeEngagement;  // Optimal engagement range (in units)
        public float escapeViability;         // 0 to 1 scale
        public float counterAttackPotential;  // 0 to 1 scale
        public float defensivePositionQuality; // 0 to 1 scale
        public float offensiveOpportunity;    // 0 to 1 scale
        public float ambushVulnerability;     // 0 to 1 scale
        public float territorialControl;      // -1 to 1 scale
        public float reinforcementImpact;     // 0 to 1 scale
        
        // Entity-specific tactical ratings
        public float[] entityTacticalRatings; // 0 to 1 scale per entity
        
        // Key positions identified
        public List<KeyPosition> keyPositions;
        
        // Identified threats and opportunities
        public List<TacticalInsight> identifiedThreats;
        public List<TacticalInsight> identifiedOpportunities;
        
        // Tactical recommendations
        public List<TacticalRecommendation> tacticalRecommendations;
        
        public TacticalAnalysisResult() {
            overallAdvantage = 0;
            positionalAdvantage = 0;
            resourceAdvantage = 0;
            combatReadiness = 0.5f;
            detectionRisk = 0.5f;
            environmentalThreat = 0.2f;
            timeAdvantage = 0;
            surprisePotential = 0.5f;
            
            optimalRangeEngagement = 50;
            escapeViability = 0.5f;
            counterAttackPotential = 0.5f;
            defensivePositionQuality = 0.5f;
            offensiveOpportunity = 0.5f;
            ambushVulnerability = 0.5f;
            territorialControl = 0;
            reinforcementImpact = 0.5f;
            
            entityTacticalRatings = new float[0];
            keyPositions = new ArrayList<>();
            identifiedThreats = new ArrayList<>();
            identifiedOpportunities = new ArrayList<>();
            tacticalRecommendations = new ArrayList<>();
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Tactical Analysis Results:\n");
            
            // Overall assessment
            sb.append("Overall Advantage: ").append(formatAdvantage(overallAdvantage)).append("\n");
            sb.append("Positional Advantage: ").append(formatAdvantage(positionalAdvantage)).append("\n");
            sb.append("Resource Advantage: ").append(formatAdvantage(resourceAdvantage)).append("\n");
            sb.append("Combat Readiness: ").append(Math.round(combatReadiness * 100)).append("%\n");
            sb.append("Detection Risk: ").append(Math.round(detectionRisk * 100)).append("%\n");
            
            // Key positions
            if (!keyPositions.isEmpty()) {
                sb.append("\nKey Positions:\n");
                for (int i = 0; i < keyPositions.size(); i++) {
                    sb.append("  ").append(i+1).append(". ").append(keyPositions.get(i)).append("\n");
                }
            }
            
            // Threats
            if (!identifiedThreats.isEmpty()) {
                sb.append("\nIdentified Threats:\n");
                for (int i = 0; i < identifiedThreats.size(); i++) {
                    sb.append("  ").append(i+1).append(". ").append(identifiedThreats.get(i)).append("\n");
                }
            }
            
            // Opportunities
            if (!identifiedOpportunities.isEmpty()) {
                sb.append("\nIdentified Opportunities:\n");
                for (int i = 0; i < identifiedOpportunities.size(); i++) {
                    sb.append("  ").append(i+1).append(". ").append(identifiedOpportunities.get(i)).append("\n");
                }
            }
            
            // Recommendations
            if (!tacticalRecommendations.isEmpty()) {
                sb.append("\nTactical Recommendations:\n");
                for (int i = 0; i < tacticalRecommendations.size(); i++) {
                    sb.append("  ").append(i+1).append(". ").append(tacticalRecommendations.get(i)).append("\n");
                }
            }
            
            return sb.toString();
        }
        
        /**
         * Format advantage value for display
         */
        private String formatAdvantage(float advantage) {
            String percentage = Math.round(Math.abs(advantage) * 100) + "%";
            if (advantage > 0.05f) {
                return "+" + percentage + " (Advantage)";
            } else if (advantage < -0.05f) {
                return "-" + percentage + " (Disadvantage)";
            } else {
                return "Neutral";
            }
        }
    }
}
