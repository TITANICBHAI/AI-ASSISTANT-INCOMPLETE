package com.aiassistant.core.ai.nlp;

import android.content.Context;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyzes sentiment in text to understand user emotions.
 * Used to adapt AI behavior based on user's emotional state.
 */
public class SentimentAnalyzer {
    private static final String TAG = "SentimentAnalyzer";
    
    // Context
    private final Context context;
    
    // Sentiment dictionaries
    private final Map<String, Float> positiveWords = new HashMap<>();
    private final Map<String, Float> negativeWords = new HashMap<>();
    
    // Sentiment thresholds
    private static final float VERY_POSITIVE_THRESHOLD = 1.5f;
    private static final float POSITIVE_THRESHOLD = 0.5f;
    private static final float NEGATIVE_THRESHOLD = -0.5f;
    private static final float VERY_NEGATIVE_THRESHOLD = -1.5f;
    
    /**
     * Constructor
     * @param context Application context
     */
    public SentimentAnalyzer(Context context) {
        this.context = context;
        initializeSentimentDictionaries();
        Log.d(TAG, "Sentiment Analyzer initialized");
    }
    
    /**
     * Initialize sentiment dictionaries
     */
    private void initializeSentimentDictionaries() {
        // Initialize positive words with sentiment scores
        positiveWords.put("good", 1.0f);
        positiveWords.put("great", 1.5f);
        positiveWords.put("excellent", 2.0f);
        positiveWords.put("amazing", 2.0f);
        positiveWords.put("awesome", 1.5f);
        positiveWords.put("nice", 1.0f);
        positiveWords.put("happy", 1.5f);
        positiveWords.put("love", 2.0f);
        positiveWords.put("enjoy", 1.0f);
        positiveWords.put("like", 0.8f);
        positiveWords.put("helpful", 1.0f);
        positiveWords.put("impressive", 1.2f);
        positiveWords.put("perfect", 1.8f);
        positiveWords.put("smart", 1.0f);
        positiveWords.put("clever", 1.2f);
        
        // Initialize negative words with sentiment scores
        negativeWords.put("bad", -1.0f);
        negativeWords.put("terrible", -1.5f);
        negativeWords.put("awful", -1.5f);
        negativeWords.put("horrible", -2.0f);
        negativeWords.put("poor", -1.0f);
        negativeWords.put("worst", -2.0f);
        negativeWords.put("annoying", -1.2f);
        negativeWords.put("hate", -2.0f);
        negativeWords.put("dislike", -1.0f);
        negativeWords.put("slow", -0.8f);
        negativeWords.put("stupid", -1.5f);
        negativeWords.put("useless", -1.5f);
        negativeWords.put("frustrating", -1.3f);
        negativeWords.put("disappointing", -1.2f);
        negativeWords.put("wrong", -1.0f);
        
        Log.d(TAG, "Initialized sentiment dictionaries with " + 
              positiveWords.size() + " positive and " + 
              negativeWords.size() + " negative words");
    }
    
    /**
     * Analyze the sentiment of text
     * @param text The text to analyze
     * @return Sentiment score (positive > 0, negative < 0)
     */
    public float analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0f;
        }
        
        // Normalize text
        String normalizedText = text.toLowerCase().trim();
        
        // Split into words
        String[] words = normalizedText.split("\\s+");
        
        // Calculate sentiment score
        float sentimentScore = 0.0f;
        int sentimentWordCount = 0;
        
        for (String word : words) {
            // Check positive words
            if (positiveWords.containsKey(word)) {
                sentimentScore += positiveWords.get(word);
                sentimentWordCount++;
            }
            
            // Check negative words
            if (negativeWords.containsKey(word)) {
                sentimentScore += negativeWords.get(word);
                sentimentWordCount++;
            }
        }
        
        // Normalize score based on number of sentiment words found
        if (sentimentWordCount > 0) {
            sentimentScore = sentimentScore / sentimentWordCount;
        }
        
        Log.d(TAG, "Sentiment analysis for '" + text + "': " + sentimentScore);
        return sentimentScore;
    }
    
    /**
     * Get sentiment category from score
     * @param sentimentScore The sentiment score
     * @return Sentiment category
     */
    public String getSentimentCategory(float sentimentScore) {
        if (sentimentScore >= VERY_POSITIVE_THRESHOLD) {
            return "very_positive";
        } else if (sentimentScore >= POSITIVE_THRESHOLD) {
            return "positive";
        } else if (sentimentScore > NEGATIVE_THRESHOLD) {
            return "neutral";
        } else if (sentimentScore > VERY_NEGATIVE_THRESHOLD) {
            return "negative";
        } else {
            return "very_negative";
        }
    }
    
    /**
     * Check if text contains negation words
     * @param text The text to check
     * @return True if contains negation
     */
    public boolean containsNegation(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        // List of negation words
        List<String> negationWords = Arrays.asList(
                "not", "no", "never", "don't", "doesn't", "didn't", 
                "isn't", "aren't", "wasn't", "weren't", "won't", 
                "can't", "cannot", "couldn't", "shouldn't", "wouldn't"
        );
        
        // Normalize text
        String normalizedText = text.toLowerCase().trim();
        
        // Check for negation words
        for (String negation : negationWords) {
            if (normalizedText.contains(negation)) {
                Log.d(TAG, "Negation detected: " + negation + " in '" + text + "'");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Find the most emotional word in text
     * @param text The text to analyze
     * @return The most emotional word and its score
     */
    public Map.Entry<String, Float> getMostEmotionalWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // Normalize text
        String normalizedText = text.toLowerCase().trim();
        
        // Split into words
        String[] words = normalizedText.split("\\s+");
        
        String mostEmotionalWord = null;
        float highestScore = 0.0f;
        
        for (String word : words) {
            // Check positive words
            if (positiveWords.containsKey(word)) {
                float score = Math.abs(positiveWords.get(word));
                if (score > highestScore) {
                    highestScore = score;
                    mostEmotionalWord = word;
                }
            }
            
            // Check negative words
            if (negativeWords.containsKey(word)) {
                float score = Math.abs(negativeWords.get(word));
                if (score > highestScore) {
                    highestScore = score;
                    mostEmotionalWord = word;
                }
            }
        }
        
        if (mostEmotionalWord != null) {
            // Get actual score (may be negative)
            float actualScore = positiveWords.containsKey(mostEmotionalWord) ? 
                    positiveWords.get(mostEmotionalWord) : 
                    negativeWords.get(mostEmotionalWord);
            
            Log.d(TAG, "Most emotional word in '" + text + "': " + 
                  mostEmotionalWord + " (score: " + actualScore + ")");
            
            final String emotionalWord = mostEmotionalWord;
            final float emotionalScore = actualScore;
            
            return new Map.Entry<String, Float>() {
                @Override
                public String getKey() {
                    return emotionalWord;
                }
                
                @Override
                public Float getValue() {
                    return emotionalScore;
                }
                
                @Override
                public Float setValue(Float value) {
                    throw new UnsupportedOperationException();
                }
            };
        }
        
        return null;
    }
    
    /**
     * Add a word to the sentiment dictionary
     * @param word The word to add
     * @param score The sentiment score (positive > 0, negative < 0)
     */
    public void addWordToSentimentDictionary(String word, float score) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        
        word = word.toLowerCase().trim();
        
        if (score > 0) {
            positiveWords.put(word, score);
        } else if (score < 0) {
            negativeWords.put(word, score);
        }
        
        Log.d(TAG, "Added word to sentiment dictionary: " + word + " (score: " + score + ")");
    }
}
