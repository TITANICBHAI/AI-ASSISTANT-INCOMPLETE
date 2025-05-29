package com.aiassistant.ai.nlp;

import android.content.Context;
import android.util.Log;

import com.aiassistant.utils.Constants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Word embedding model for semantic understanding.
 * Provides vector representations of words for semantic similarity calculations.
 */
public class WordEmbeddingModel {
    private static final String TAG = Constants.TAG_PREFIX + "WordEmbedding";
    
    // Vocabulary and embeddings
    private final Map<String, float[]> wordVectors;
    
    // Word tokenizer
    private final SimpleTokenizer tokenizer;
    
    // Intent embeddings cache
    private final Map<String, float[]> intentEmbeddings;
    
    /**
     * Constructor
     */
    public WordEmbeddingModel() {
        wordVectors = new HashMap<>();
        tokenizer = new SimpleTokenizer();
        intentEmbeddings = new HashMap<>();
    }
    
    /**
     * Initialize the word embedding model
     * @param context Application context
     */
    public void initialize(Context context) {
        // Load pre-trained embeddings
        try {
            loadEmbeddings(context);
            Log.i(TAG, "Word embedding model initialized with " + wordVectors.size() + " words");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing word embedding model", e);
        }
    }
    
    /**
     * Load word embeddings from assets
     * @param context Application context
     */
    private void loadEmbeddings(Context context) {
        // In a real implementation, we would load pre-trained embeddings
        // from a file in assets. For this example, we'll use a small set of words.
        
        // Common gaming terms
        addWordVector("tap", new float[]{0.2f, 0.8f, 0.1f, 0.3f, 0.1f});
        addWordVector("click", new float[]{0.2f, 0.8f, 0.1f, 0.3f, 0.1f});
        addWordVector("swipe", new float[]{0.3f, 0.7f, 0.2f, 0.4f, 0.1f});
        addWordVector("scroll", new float[]{0.3f, 0.7f, 0.2f, 0.4f, 0.1f});
        addWordVector("type", new float[]{0.4f, 0.2f, 0.7f, 0.1f, 0.1f});
        addWordVector("input", new float[]{0.4f, 0.2f, 0.7f, 0.1f, 0.1f});
        addWordVector("back", new float[]{0.7f, 0.2f, 0.1f, 0.5f, 0.3f});
        addWordVector("home", new float[]{0.7f, 0.1f, 0.1f, 0.6f, 0.3f});
        addWordVector("activate", new float[]{0.2f, 0.2f, 0.1f, 0.8f, 0.5f});
        addWordVector("deactivate", new float[]{0.2f, 0.2f, 0.1f, 0.8f, -0.5f});
        addWordVector("learn", new float[]{0.1f, 0.1f, 0.2f, 0.3f, 0.9f});
        addWordVector("train", new float[]{0.1f, 0.1f, 0.2f, 0.3f, 0.9f});
        addWordVector("analyze", new float[]{0.5f, 0.1f, 0.7f, 0.3f, 0.5f});
        
        // Directional terms
        addWordVector("up", new float[]{0.1f, 0.1f, 0.8f, 0.1f, 0.1f});
        addWordVector("down", new float[]{0.1f, 0.1f, -0.8f, 0.1f, 0.1f});
        addWordVector("left", new float[]{-0.8f, 0.1f, 0.1f, 0.1f, 0.1f});
        addWordVector("right", new float[]{0.8f, 0.1f, 0.1f, 0.1f, 0.1f});
        
        // UI element terms
        addWordVector("button", new float[]{0.3f, 0.8f, 0.1f, 0.1f, 0.2f});
        addWordVector("text", new float[]{0.1f, 0.2f, 0.8f, 0.1f, 0.3f});
        addWordVector("image", new float[]{0.1f, 0.1f, 0.2f, 0.8f, 0.3f});
        addWordVector("field", new float[]{0.2f, 0.3f, 0.7f, 0.1f, 0.2f});
        
        // Mode terms
        addWordVector("autonomous", new float[]{0.7f, 0.2f, 0.1f, 0.1f, 0.8f});
        addWordVector("copilot", new float[]{0.3f, 0.6f, 0.1f, 0.1f, 0.7f});
    }
    
    /**
     * Add a word vector
     * @param word Word
     * @param vector Vector
     */
    private void addWordVector(String word, float[] vector) {
        wordVectors.put(word.toLowerCase(), vector);
    }
    
    /**
     * Get a word vector
     * @param word Word
     * @return Vector or null
     */
    public float[] getWordVector(String word) {
        return wordVectors.get(word.toLowerCase());
    }
    
    /**
     * Find the intent closest to the text
     * @param text Input text
     * @param intents Available intents
     * @return Intent result
     */
    public Map<String, Object> findClosestIntent(String text, Set<String> intents) {
        if (text == null || text.isEmpty() || intents == null || intents.isEmpty()) {
            return createUnknownResult(text);
        }
        
        // Tokenize text
        List<String> tokens = tokenizer.tokenize(text);
        
        // Compute text embedding
        float[] textEmbedding = computeTextEmbedding(tokens);
        
        // Find closest intent
        String bestIntent = null;
        float bestSimilarity = -1.0f;
        
        for (String intent : intents) {
            // Get or compute intent embedding
            float[] intentEmbedding = getIntentEmbedding(intent);
            
            if (intentEmbedding != null) {
                float similarity = cosineSimilarity(textEmbedding, intentEmbedding);
                
                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                    bestIntent = intent;
                }
            }
        }
        
        // Create result
        Map<String, Object> result = new HashMap<>();
        
        if (bestIntent != null && bestSimilarity > 0.5f) {
            result.put("intent", bestIntent);
            result.put("confidence", bestSimilarity);
            result.put("original_text", text);
            
            // Extract parameters based on intent
            extractParametersForIntent(result, bestIntent, text, tokens);
        } else {
            result = createUnknownResult(text);
        }
        
        return result;
    }
    
    /**
     * Extract parameters for an intent
     * @param result Result map
     * @param intent Intent
     * @param text Original text
     * @param tokens Tokenized text
     */
    private void extractParametersForIntent(Map<String, Object> result, String intent, String text, List<String> tokens) {
        switch (intent) {
            case "tap":
                extractTapParameters(result, text, tokens);
                break;
                
            case "swipe":
                extractSwipeParameters(result, text, tokens);
                break;
                
            case "input_text":
                extractTextInputParameters(result, text, tokens);
                break;
                
            case "activate":
                extractActivateParameters(result, text, tokens);
                break;
                
            case "learn":
                extractLearnParameters(result, text, tokens);
                break;
                
            case "analyze":
                extractAnalyzeParameters(result, text, tokens);
                break;
        }
    }
    
    /**
     * Extract tap parameters
     * @param result Result map
     * @param text Original text
     * @param tokens Tokenized text
     */
    private void extractTapParameters(Map<String, Object> result, String text, List<String> tokens) {
        // Check for coordinates in parentheses
        if (text.contains("(") && text.contains(")")) {
            String coords = text.substring(text.indexOf("(") + 1, text.indexOf(")"));
            String[] parts = coords.split(",");
            if (parts.length == 2) {
                try {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].trim());
                    result.put("x", x);
                    result.put("y", y);
                    return;
                } catch (NumberFormatException e) {
                    // Ignore and try other methods
                }
            }
        }
        
        // Look for "text" or "button" keywords
        int textIndex = tokens.indexOf("text");
        int buttonIndex = tokens.indexOf("button");
        
        if (textIndex >= 0 && textIndex < tokens.size() - 1) {
            // Assume next token is the text
            result.put("element_text", tokens.get(textIndex + 1));
        } else if (buttonIndex >= 0 && buttonIndex < tokens.size() - 1) {
            // Assume next token is the button text
            result.put("element_text", tokens.get(buttonIndex + 1));
        }
    }
    
    /**
     * Extract swipe parameters
     * @param result Result map
     * @param text Original text
     * @param tokens Tokenized text
     */
    private void extractSwipeParameters(Map<String, Object> result, String text, List<String> tokens) {
        // Check for direction words
        for (String token : tokens) {
            if (token.equals("up") || token.equals("down") || 
                token.equals("left") || token.equals("right")) {
                result.put("direction", token);
                return;
            }
        }
    }
    
    /**
     * Extract text input parameters
     * @param result Result map
     * @param text Original text
     * @param tokens Tokenized text
     */
    private void extractTextInputParameters(Map<String, Object> result, String text, List<String> tokens) {
        // Look for content in quotes
        if (text.contains("\"")) {
            int startQuote = text.indexOf("\"");
            int endQuote = text.indexOf("\"", startQuote + 1);
            if (endQuote > startQuote) {
                String quoted = text.substring(startQuote + 1, endQuote);
                result.put("text", quoted);
                return;
            }
        }
        
        // Otherwise try to find text after "type" or "input"
        int typeIndex = tokens.indexOf("type");
        int inputIndex = tokens.indexOf("input");
        
        int startIndex = Math.max(typeIndex, inputIndex);
        if (startIndex >= 0 && startIndex < tokens.size() - 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = startIndex + 1; i < tokens.size(); i++) {
                if (tokens.get(i).equals("in") || tokens.get(i).equals("into")) {
                    break;
                }
                sb.append(tokens.get(i)).append(" ");
            }
            result.put("text", sb.toString().trim());
        }
    }
    
    /**
     * Extract activate parameters
     * @param result Result map
     * @param text Original text
     * @param tokens Tokenized text
     */
    private void extractActivateParameters(Map<String, Object> result, String text, List<String> tokens) {
        // Look for mode
        if (tokens.contains("autonomous")) {
            result.put("mode", "autonomous");
        } else if (tokens.contains("copilot")) {
            result.put("mode", "copilot");
        } else {
            result.put("mode", "autonomous"); // Default
        }
    }
    
    /**
     * Extract learn parameters
     * @param result Result map
     * @param text Original text
     * @param tokens Tokenized text
     */
    private void extractLearnParameters(Map<String, Object> result, String text, List<String> tokens) {
        // Look for algorithm
        if (tokens.contains("dqn")) {
            result.put("algorithm", "dqn");
        } else if (tokens.contains("sarsa")) {
            result.put("algorithm", "sarsa");
        } else if (tokens.contains("qlearning")) {
            result.put("algorithm", "qlearning");
        } else if (tokens.contains("ppo")) {
            result.put("algorithm", "ppo");
        } else {
            result.put("algorithm", "dqn"); // Default
        }
        
        // Look for game name (assuming it's the last few tokens)
        int learnIndex = tokens.indexOf("learn");
        int trainIndex = tokens.indexOf("train");
        int aboutIndex = tokens.indexOf("about");
        
        int startIndex = Math.max(learnIndex, trainIndex);
        if (startIndex < 0) {
            return;
        }
        
        if (aboutIndex > startIndex) {
            startIndex = aboutIndex;
        }
        
        if (startIndex >= 0 && startIndex < tokens.size() - 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = startIndex + 1; i < tokens.size(); i++) {
                if (tokens.get(i).equals("using")) {
                    break;
                }
                sb.append(tokens.get(i)).append(" ");
            }
            result.put("game_name", sb.toString().trim());
        }
    }
    
    /**
     * Extract analyze parameters
     * @param result Result map
     * @param text Original text
     * @param tokens Tokenized text
     */
    private void extractAnalyzeParameters(Map<String, Object> result, String text, List<String> tokens) {
        if (tokens.contains("game")) {
            result.put("target", "game");
        } else {
            result.put("target", "state");
        }
    }
    
    /**
     * Create an unknown result
     * @param text Original text
     * @return Unknown result
     */
    private Map<String, Object> createUnknownResult(String text) {
        Map<String, Object> result = new HashMap<>();
        result.put("intent", "unknown");
        result.put("confidence", 0.0f);
        result.put("original_text", text);
        return result;
    }
    
    /**
     * Compute a text embedding from tokens
     * @param tokens Tokenized text
     * @return Text embedding
     */
    private float[] computeTextEmbedding(List<String> tokens) {
        if (tokens.isEmpty()) {
            return new float[]{0, 0, 0, 0, 0}; // Default zero vector
        }
        
        // Average of word vectors
        float[] embedding = new float[5]; // Using 5-dimensional vectors in our example
        int count = 0;
        
        for (String token : tokens) {
            float[] vector = wordVectors.get(token.toLowerCase());
            if (vector != null) {
                for (int i = 0; i < vector.length; i++) {
                    embedding[i] += vector[i];
                }
                count++;
            }
        }
        
        // Normalize
        if (count > 0) {
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] /= count;
            }
        }
        
        return embedding;
    }
    
    /**
     * Get or compute an intent embedding
     * @param intent Intent name
     * @return Intent embedding
     */
    private float[] getIntentEmbedding(String intent) {
        // Check cache
        if (intentEmbeddings.containsKey(intent)) {
            return intentEmbeddings.get(intent);
        }
        
        // Create embedding based on intent
        float[] embedding = null;
        
        switch (intent) {
            case "tap":
                embedding = averageVectors(Arrays.asList("tap", "click", "button"));
                break;
                
            case "swipe":
                embedding = averageVectors(Arrays.asList("swipe", "scroll", "move"));
                break;
                
            case "input_text":
                embedding = averageVectors(Arrays.asList("type", "input", "text", "enter"));
                break;
                
            case "back":
                embedding = averageVectors(Arrays.asList("back", "previous", "return"));
                break;
                
            case "home":
                embedding = averageVectors(Arrays.asList("home", "main", "screen"));
                break;
                
            case "activate":
                embedding = averageVectors(Arrays.asList("activate", "start", "enable", "turn", "on"));
                break;
                
            case "deactivate":
                embedding = averageVectors(Arrays.asList("deactivate", "stop", "disable", "turn", "off"));
                break;
                
            case "learn":
                embedding = averageVectors(Arrays.asList("learn", "train", "study", "practice"));
                break;
                
            case "stop_learning":
                embedding = averageVectors(Arrays.asList("stop", "end", "finish", "learning", "training"));
                break;
                
            case "analyze":
                embedding = averageVectors(Arrays.asList("analyze", "examine", "inspect", "understand"));
                break;
        }
        
        // Cache result
        if (embedding != null) {
            intentEmbeddings.put(intent, embedding);
        }
        
        return embedding;
    }
    
    /**
     * Average multiple word vectors
     * @param words List of words
     * @return Average vector
     */
    private float[] averageVectors(List<String> words) {
        if (words.isEmpty()) {
            return null;
        }
        
        float[] result = new float[5]; // Using 5-dimensional vectors
        int count = 0;
        
        for (String word : words) {
            float[] vector = wordVectors.get(word.toLowerCase());
            if (vector != null) {
                for (int i = 0; i < vector.length; i++) {
                    result[i] += vector[i];
                }
                count++;
            }
        }
        
        if (count > 0) {
            for (int i = 0; i < result.length; i++) {
                result[i] /= count;
            }
            return result;
        }
        
        return null;
    }
    
    /**
     * Calculate cosine similarity between vectors
     * @param v1 First vector
     * @param v2 Second vector
     * @return Similarity (1 = identical, 0 = orthogonal, -1 = opposite)
     */
    private float cosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) {
            return 0.0f;
        }
        
        float dotProduct = 0.0f;
        float normV1 = 0.0f;
        float normV2 = 0.0f;
        
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normV1 += v1[i] * v1[i];
            normV2 += v2[i] * v2[i];
        }
        
        if (normV1 <= 0.0f || normV2 <= 0.0f) {
            return 0.0f;
        }
        
        return dotProduct / (float) (Math.sqrt(normV1) * Math.sqrt(normV2));
    }
    
    /**
     * Simple word tokenizer class
     */
    private static class SimpleTokenizer {
        /**
         * Tokenize text into words
         * @param text Input text
         * @return List of tokens
         */
        public List<String> tokenize(String text) {
            if (text == null || text.isEmpty()) {
                return new ArrayList<>();
            }
            
            // Split on whitespace and punctuation
            String[] tokens = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", " ")
                .trim()
                .split("\\s+");
            
            // Remove stop words
            List<String> result = new ArrayList<>();
            for (String token : tokens) {
                if (!isStopWord(token)) {
                    result.add(token);
                }
            }
            
            return result;
        }
        
        /**
         * Check if a word is a stop word
         * @param word Word to check
         * @return True if stop word
         */
        private boolean isStopWord(String word) {
            String[] stopWords = {
                "a", "an", "the", "and", "but", "or", "for", "nor", "on", "at", "to", "from", "by",
                "is", "are", "was", "were", "be", "being", "been", "have", "has", "had", "do", "does", "did",
                "will", "would", "shall", "should", "may", "might", "must", "can", "could"
            };
            
            for (String stopWord : stopWords) {
                if (stopWord.equals(word)) {
                    return true;
                }
            }
            
            return false;
        }
    }
}