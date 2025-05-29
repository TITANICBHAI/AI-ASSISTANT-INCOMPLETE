package com.aiassistant.core.ai.model;

/**
 * Configuration class for AI models.
 * Contains metadata and settings for TensorFlow Lite models used by the AI Gaming Assistant.
 */
public class AIModelConfig {
    // Model identifiers
    public static final String UI_DETECTOR = "ui_elements_detector";
    public static final String COMBAT_DETECTOR = "combat_effects_detector";
    public static final String ENVIRONMENT_DETECTOR = "environment_detector";
    public static final String ITEM_DETECTOR = "item_detector";
    public static final String ENEMY_DETECTOR = "enemy_detector";
    public static final String TERRAIN_ANALYZER = "terrain_analyzer";
    public static final String GAME_STATE_CLASSIFIER = "game_state_classifier";
    public static final String INTERACTION_DETECTOR = "interaction_detector";
    public static final String GAME_IDENTIFIER = "game_identifier";
    
    // Model file paths (relative to assets directory)
    public static final String UI_DETECTOR_PATH = "ml_models/ui_elements_detector.tflite";
    public static final String COMBAT_DETECTOR_PATH = "ml_models/combat_effects_detector.tflite";
    public static final String ENVIRONMENT_DETECTOR_PATH = "ml_models/environment_detector.tflite";
    public static final String ITEM_DETECTOR_PATH = "ml_models/item_detector.tflite";
    public static final String ENEMY_DETECTOR_PATH = "ml_models/enemy_detector.tflite";
    public static final String TERRAIN_ANALYZER_PATH = "ml_models/terrain_analyzer.tflite";
    public static final String GAME_STATE_CLASSIFIER_PATH = "ml_models/game_state_classifier.tflite";
    public static final String INTERACTION_DETECTOR_PATH = "ml_models/interaction_detector.tflite";
    public static final String GAME_IDENTIFIER_PATH = "ml_models/game_identifier.tflite";
    
    // Label file paths (relative to assets directory)
    public static final String UI_DETECTOR_LABELS = "labels/ui_elements_extended.txt";
    public static final String COMBAT_DETECTOR_LABELS = "labels/combat_effects_extended.txt";
    public static final String ENVIRONMENT_DETECTOR_LABELS = "labels/environment_types_extended.txt";
    public static final String ITEM_DETECTOR_LABELS = "labels/item_types_extended.txt";
    public static final String ENEMY_DETECTOR_LABELS = "labels/general_game_labels.txt";
    public static final String TERRAIN_ANALYZER_LABELS = "labels/environment_types_extended.txt";
    public static final String GAME_IDENTIFIER_LABELS = "labels/game_identifier_labels.txt";
    
    // Game-specific label files
    public static final String PUBG_LABELS = "labels/pubg_mobile_specific.txt";
    public static final String FREE_FIRE_LABELS = "labels/free_fire_specific.txt";
    public static final String COD_MOBILE_LABELS = "labels/cod_mobile_specific.txt";
    
    // Advanced detection label files
    public static final String ADVANCED_AI_DETECTION_LABELS = "labels/advanced_ai_detection.txt";
    public static final String ADAPTIVE_BEHAVIOR_LABELS = "labels/adaptive_behavior_detection.txt";
    
    // Default model input size
    public static final int DEFAULT_INPUT_SIZE = 224;
    
    // Confidence thresholds
    public static final float DEFAULT_DETECTION_THRESHOLD = 0.6f;
    public static final float HIGH_CONFIDENCE_THRESHOLD = 0.8f;
    public static final float LOW_CONFIDENCE_THRESHOLD = 0.4f;
    
    // Input normalization parameters
    public static final float[] NORMALIZATION_MEAN = new float[]{127.5f, 127.5f, 127.5f};
    public static final float[] NORMALIZATION_STD = new float[]{127.5f, 127.5f, 127.5f};
    
    // Game identifiers
    public static final String GAME_PUBG_MOBILE = "pubg_mobile";
    public static final String GAME_FREE_FIRE = "free_fire";
    public static final String GAME_COD_MOBILE = "call_of_duty_mobile";
    public static final String GAME_UNKNOWN = "unknown_game";
    
    /**
     * Get the label file path for a specific game
     * 
     * @param gameId The game identifier
     * @return The path to the appropriate label file
     */
    public static String getLabelFileForGame(String gameId) {
        switch (gameId) {
            case GAME_PUBG_MOBILE:
                return PUBG_LABELS;
            case GAME_FREE_FIRE:
                return FREE_FIRE_LABELS;
            case GAME_COD_MOBILE:
                return COD_MOBILE_LABELS;
            default:
                return UI_DETECTOR_LABELS;
        }
    }
    
    /**
     * Get the minimum confidence threshold for a specific model
     * 
     * @param modelId The model identifier
     * @return The appropriate confidence threshold
     */
    public static float getConfidenceThresholdForModel(String modelId) {
        switch (modelId) {
            case ENEMY_DETECTOR:
            case GAME_IDENTIFIER:
                return HIGH_CONFIDENCE_THRESHOLD;
            case TERRAIN_ANALYZER:
            case ITEM_DETECTOR:
                return LOW_CONFIDENCE_THRESHOLD;
            default:
                return DEFAULT_DETECTION_THRESHOLD;
        }
    }
}
