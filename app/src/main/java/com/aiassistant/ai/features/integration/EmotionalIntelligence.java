package com.aiassistant.ai.features.integration;

/**
 * Emotional intelligence system for AI
 */
public class EmotionalIntelligence {
    
    /**
     * Sentiment level enum
     */
    public enum SentimentLevel {
        VERY_NEGATIVE(-1.0f, -0.7f),
        NEGATIVE(-0.7f, -0.3f),
        NEUTRAL(-0.3f, 0.3f),
        POSITIVE(0.3f, 0.7f),
        VERY_POSITIVE(0.7f, 1.0f);
        
        private final float minValue;
        private final float maxValue;
        
        SentimentLevel(float minValue, float maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
        }
        
        public float getMinValue() {
            return minValue;
        }
        
        public float getMaxValue() {
            return maxValue;
        }
        
        public static SentimentLevel fromValue(float value) {
            for (SentimentLevel level : values()) {
                if (value >= level.minValue && value <= level.maxValue) {
                    return level;
                }
            }
            return NEUTRAL;
        }
    }
    
    /**
     * Analyze sentiment of text
     * @param text Text to analyze
     * @return Sentiment score (-1.0 to 1.0)
     */
    public float analyzeSentiment(String text) {
        // This is a stub implementation
        // In a real implementation, this would use NLP to analyze sentiment
        
        if (text == null || text.isEmpty()) {
            return 0.0f;
        }
        
        // Simple keyword-based sentiment analysis
        String lowerText = text.toLowerCase();
        
        // Positive keywords
        int positiveScore = countKeywords(lowerText, new String[] {
            "good", "great", "excellent", "happy", "love", "like", "wonderful",
            "amazing", "fantastic", "awesome", "joy", "smile", "best", "beautiful",
            "perfect", "pleased", "thank", "thanks", "appreciate", "excited"
        });
        
        // Negative keywords
        int negativeScore = countKeywords(lowerText, new String[] {
            "bad", "terrible", "awful", "sad", "hate", "dislike", "horrible",
            "annoying", "worst", "poor", "angry", "mad", "upset", "disappointed",
            "frustrate", "sorry", "worry", "regret", "fault", "fail"
        });
        
        // Calculate sentiment
        float totalWords = Math.max(text.split("\\s+").length, 1);
        float sentiment = (positiveScore - negativeScore) / totalWords;
        
        // Clamp to range
        return Math.max(-1.0f, Math.min(1.0f, sentiment * 3)); // Scaling factor
    }
    
    /**
     * Get sentiment level from text
     * @param text Text to analyze
     * @return Sentiment level
     */
    public SentimentLevel getSentimentLevel(String text) {
        float sentiment = analyzeSentiment(text);
        return SentimentLevel.fromValue(sentiment);
    }
    
    /**
     * Count occurrences of keywords in text
     */
    private int countKeywords(String text, String[] keywords) {
        int count = 0;
        for (String keyword : keywords) {
            int index = -1;
            while ((index = text.indexOf(keyword, index + 1)) >= 0) {
                count++;
            }
        }
        return count;
    }
}
