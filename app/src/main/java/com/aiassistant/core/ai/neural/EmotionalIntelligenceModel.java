package com.aiassistant.core.ai.neural;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * TensorFlow Lite model for emotional intelligence
 * Analyzes text for emotional content and sentiment
 */
public class EmotionalIntelligenceModel extends BaseTFLiteModel {
    private static final String TAG = "EmotionalModel";
    
    // Model configuration
    private static final String MODEL_PATH = "models/emotional_intelligence.tflite";
    private static final int INPUT_SIZE = 128; // Max sequence length
    private static final boolean QUANTIZED = true;
    
    // Vocabulary for text encoding
    private final Map<String, Integer> vocabulary;
    private final int vocabularySize;
    
    // Emotion labels
    private static final String[] EMOTIONS = {
            "neutral", "happy", "sad", "angry", "surprised", "fear", "disgust"
    };
    
    /**
     * Constructor
     * @param context Application context
     */
    public EmotionalIntelligenceModel(Context context) {
        super(context, MODEL_PATH, INPUT_SIZE, QUANTIZED);
        
        // Initialize vocabulary (simplified for demo)
        this.vocabulary = buildSimpleVocabulary();
        this.vocabularySize = vocabulary.size();
        
        Log.d(TAG, "EmotionalIntelligenceModel initialized with vocabulary size: " + vocabularySize);
    }
    
    /**
     * Analyze text for emotions
     * @param text Input text
     * @return Map of emotions to confidence scores
     */
    public Map<String, Float> analyzeEmotion(String text) {
        if (!modelLoaded) {
            Log.w(TAG, "Model not loaded, attempting to load now");
            if (!loadModel()) {
                Log.e(TAG, "Failed to load model for emotion analysis");
                return getDefaultEmotions();
            }
        }
        
        try {
            // Prepare input
            ByteBuffer inputBuffer = preprocessText(text);
            
            // Prepare output
            float[][] output = new float[1][EMOTIONS.length];
            
            // Run inference
            interpreter.run(inputBuffer, output);
            
            // Process results
            return processResults(output[0]);
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing emotion: " + e.getMessage());
            return getDefaultEmotions();
        }
    }
    
    /**
     * Preprocess text for model input
     * @param text Input text
     * @return ByteBuffer containing encoded text
     */
    private ByteBuffer preprocessText(String text) {
        // Create input buffer
        ByteBuffer buffer = ByteBuffer.allocateDirect(INPUT_SIZE * 4); // 4 bytes per float
        buffer.order(ByteOrder.nativeOrder());
        
        // Tokenize and encode text
        String[] tokens = text.toLowerCase().split("\\s+");
        
        // Fill buffer with token IDs
        int i = 0;
        for (; i < Math.min(tokens.length, INPUT_SIZE); i++) {
            String token = tokens[i];
            int id = vocabulary.getOrDefault(token, 1); // 1 is <UNK> token
            buffer.putFloat(id);
        }
        
        // Pad remaining space
        for (; i < INPUT_SIZE; i++) {
            buffer.putFloat(0); // 0 is <PAD> token
        }
        
        // Rewind buffer to beginning
        buffer.rewind();
        
        return buffer;
    }
    
    /**
     * Process model output
     * @param output Raw model output
     * @return Map of emotions to confidence scores
     */
    private Map<String, Float> processResults(float[] output) {
        Map<String, Float> emotions = new HashMap<>();
        
        // Map output values to emotions
        for (int i = 0; i < EMOTIONS.length; i++) {
            emotions.put(EMOTIONS[i], output[i]);
        }
        
        return emotions;
    }
    
    /**
     * Build a simple vocabulary for text encoding
     * In a real implementation, this would be loaded from a file
     * @return Map of tokens to token IDs
     */
    private Map<String, Integer> buildSimpleVocabulary() {
        Map<String, Integer> vocab = new HashMap<>();
        
        // Special tokens
        vocab.put("<PAD>", 0);
        vocab.put("<UNK>", 1);
        vocab.put("<START>", 2);
        vocab.put("<END>", 3);
        
        // Common emotion words
        String[] commonWords = {
            "i", "you", "he", "she", "it", "we", "they",
            "am", "is", "are", "was", "were",
            "happy", "sad", "angry", "upset", "frustrated", "annoyed",
            "excited", "thrilled", "delighted", "pleased", "content",
            "depressed", "disappointed", "unhappy", "miserable",
            "afraid", "scared", "terrified", "anxious", "worried",
            "surprised", "shocked", "amazed", "astonished",
            "disgusted", "appalled", "revolted",
            "confused", "puzzled", "perplexed",
            "love", "hate", "like", "dislike",
            "good", "bad", "great", "terrible", "awful", "wonderful",
            "feel", "feeling", "felt", "emotion", "emotional",
            "very", "really", "extremely", "quite", "somewhat",
            "not", "don't", "doesn't", "didn't", "no", "never",
            "yes", "always", "sometimes", "often",
            "help", "please", "thank", "thanks", "sorry",
            "hello", "hi", "hey", "goodbye", "bye",
            "today", "yesterday", "tomorrow", "now", "later",
            "what", "why", "how", "when", "where", "who",
            "can", "could", "would", "should", "will", "shall",
            "need", "want", "have", "had", "has",
            "do", "did", "does", "doing", "done",
            "call", "calling", "called", "speak", "speaking", "spoke",
            "talk", "talking", "talked", "say", "saying", "said",
            "hear", "hearing", "heard", "listen", "listening", "listened"
        };
        
        // Add common words
        for (int i = 0; i < commonWords.length; i++) {
            vocab.put(commonWords[i], i + 4); // Start after special tokens
        }
        
        return vocab;
    }
    
    /**
     * Get default emotions when model fails
     * @return Map with default neutral emotion
     */
    private Map<String, Float> getDefaultEmotions() {
        Map<String, Float> emotions = new HashMap<>();
        emotions.put("neutral", 1.0f);
        
        for (String emotion : EMOTIONS) {
            if (!emotion.equals("neutral")) {
                emotions.put(emotion, 0.0f);
            }
        }
        
        return emotions;
    }
    
    /**
     * Analyze emotional valence (positive/negative sentiment)
     * @param text Input text
     * @return Valence score (-1.0 to 1.0)
     */
    public float analyzeValence(String text) {
        Map<String, Float> emotions = analyzeEmotion(text);
        
        // Calculate valence from emotions
        float valence = 0.0f;
        
        // Positive emotions increase valence
        valence += emotions.getOrDefault("happy", 0.0f) * 0.8f;
        valence += emotions.getOrDefault("surprised", 0.0f) * 0.3f;
        
        // Negative emotions decrease valence
        valence -= emotions.getOrDefault("sad", 0.0f) * 0.7f;
        valence -= emotions.getOrDefault("angry", 0.0f) * 0.8f;
        valence -= emotions.getOrDefault("fear", 0.0f) * 0.6f;
        valence -= emotions.getOrDefault("disgust", 0.0f) * 0.7f;
        
        // Neutral contributes to middle
        float neutral = emotions.getOrDefault("neutral", 0.0f);
        valence = valence * (1.0f - neutral * 0.5f);
        
        // Ensure range -1.0 to 1.0
        return Math.max(-1.0f, Math.min(1.0f, valence));
    }
    
    /**
     * Analyze emotional arousal (intensity)
     * @param text Input text
     * @return Arousal score (0.0 to 1.0)
     */
    public float analyzeArousal(String text) {
        Map<String, Float> emotions = analyzeEmotion(text);
        
        // Calculate arousal from emotions
        float arousal = 0.0f;
        
        // High arousal emotions
        arousal += emotions.getOrDefault("angry", 0.0f) * 0.9f;
        arousal += emotions.getOrDefault("surprised", 0.0f) * 0.8f;
        arousal += emotions.getOrDefault("fear", 0.0f) * 0.8f;
        
        // Medium arousal emotions
        arousal += emotions.getOrDefault("happy", 0.0f) * 0.6f;
        arousal += emotions.getOrDefault("disgust", 0.0f) * 0.6f;
        
        // Low arousal emotions
        arousal += emotions.getOrDefault("sad", 0.0f) * 0.3f;
        
        // Neutral reduces arousal
        arousal -= emotions.getOrDefault("neutral", 0.0f) * 0.5f;
        
        // Ensure range 0.0 to 1.0
        return Math.max(0.0f, Math.min(1.0f, arousal));
    }
}
