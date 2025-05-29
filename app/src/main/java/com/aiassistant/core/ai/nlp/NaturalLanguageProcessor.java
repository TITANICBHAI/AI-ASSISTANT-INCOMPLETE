package com.aiassistant.core.ai.nlp;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Core NLP processing system for understanding user commands
 * and context in text.
 */
public class NaturalLanguageProcessor {
    private static final String TAG = "NLProcessor";
    
    private Context context;
    private IntentHandler intentHandler;
    private SpeechRecognitionHandler speechRecognition;
    private TextToSpeechManager textToSpeech;
    
    // Intent patterns for basic command recognition
    private Map<String, Pattern> intentPatterns;
    
    // History of recent utterances
    private List<String> recentUtterances;
    private static final int MAX_HISTORY = 10;
    
    public NaturalLanguageProcessor(Context context) {
        this.context = context;
        
        // Initialize components
        intentHandler = new IntentHandler(context);
        speechRecognition = new SpeechRecognitionHandler(context);
        textToSpeech = new TextToSpeechManager(context);
        
        // Initialize history
        recentUtterances = new ArrayList<>();
        
        // Initialize patterns
        initializeIntentPatterns();
        
        Log.i(TAG, "Natural Language Processor initialized");
    }
    
    /**
     * Set up regex patterns for intent matching
     */
    private void initializeIntentPatterns() {
        intentPatterns = new HashMap<>();
        
        // Control intents
        intentPatterns.put("START", Pattern.compile("(?i)(start|begin|activate|launch|run)\\s+(assistant|ai|app)"));
        intentPatterns.put("STOP", Pattern.compile("(?i)(stop|end|deactivate|quit|exit|close)\\s+(assistant|ai|app)"));
        
        // Mode intents
        intentPatterns.put("AUTO_MODE", Pattern.compile("(?i)(auto|automatic|autonomous)\\s+(mode|control)"));
        intentPatterns.put("COPILOT_MODE", Pattern.compile("(?i)(copilot|assist|help)\\s+(mode|me)"));
        
        // Action intents
        intentPatterns.put("CLICK", Pattern.compile("(?i)(click|tap|press|touch)\\s+(on\\s+)?(the\\s+)?(.+)"));
        intentPatterns.put("SWIPE", Pattern.compile("(?i)(swipe|scroll)\\s+(up|down|left|right|to\\s+.+)"));
        
        // Information intents
        intentPatterns.put("WHAT_IS", Pattern.compile("(?i)(what\\s+is|explain|tell\\s+me\\s+about)\\s+(.+)"));
        intentPatterns.put("HOW_TO", Pattern.compile("(?i)(how\\s+to|how\\s+do\\s+I)\\s+(.+)"));
        
        Log.d(TAG, "Initialized " + intentPatterns.size() + " intent patterns");
    }
    
    /**
     * Process text input to extract intent and entities
     */
    public void processText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        
        // Add to history
        addToHistory(text);
        
        // Extract intent
        Map.Entry<String, String[]> intent = extractIntent(text);
        
        if (intent != null) {
            String intentName = intent.getKey();
            String[] entities = intent.getValue();
            
            Log.d(TAG, "Extracted intent: " + intentName + " with " + (entities != null ? entities.length : 0) + " entities");
            
            // Process the intent
            intentHandler.handleIntent(intentName, entities, text);
        } else {
            Log.d(TAG, "No intent matched for: " + text);
        }
    }
    
    /**
     * Match text against intent patterns to extract intent and entities
     */
    private Map.Entry<String, String[]> extractIntent(String text) {
        for (Map.Entry<String, Pattern> entry : intentPatterns.entrySet()) {
            Matcher matcher = entry.getValue().matcher(text);
            if (matcher.find()) {
                String intentName = entry.getKey();
                
                // Extract entities from matcher groups
                List<String> entities = new ArrayList<>();
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    String entity = matcher.group(i);
                    if (entity != null && !entity.isEmpty()) {
                        entities.add(entity);
                    }
                }
                
                return new HashMap.SimpleEntry<>(intentName, entities.toArray(new String[0]));
            }
        }
        
        return null;
    }
    
    /**
     * Add text to history
     */
    private void addToHistory(String text) {
        recentUtterances.add(0, text); // Add to front
        if (recentUtterances.size() > MAX_HISTORY) {
            recentUtterances.remove(recentUtterances.size() - 1); // Remove oldest
        }
    }
    
    /**
     * Get the speech recognition handler
     */
    public SpeechRecognitionHandler getSpeechRecognition() {
        return speechRecognition;
    }
    
    /**
     * Get the text-to-speech manager
     */
    public TextToSpeechManager getTextToSpeech() {
        return textToSpeech;
    }
    
    /**
     * Speak a response to the user
     */
    public void speakResponse(String response) {
        if (textToSpeech != null) {
            textToSpeech.speak(response);
        }
    }
    
    /**
     * Start listening for speech input
     */
    public void startListening() {
        if (speechRecognition != null) {
            speechRecognition.startListening(result -> {
                if (result != null && !result.isEmpty()) {
                    processText(result);
                }
            });
        }
    }
    
    /**
     * Stop listening for speech input
     */
    public void stopListening() {
        if (speechRecognition != null) {
            speechRecognition.stopListening();
        }
    }
    
    /**
     * Extract keywords from text
     * @param text The text to analyze
     * @return List of extracted keywords
     */
    public List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return keywords;
        }
        
        // Split text into words
        String[] words = text.toLowerCase().split("\\s+");
        
        // Filter out common stop words and short words
        for (String word : words) {
            // Clean the word
            word = word.replaceAll("[^a-zA-Z0-9]", "").trim();
            
            // Skip stop words, short words, numbers
            if (word.length() <= 2 || isStopWord(word) || word.matches("\\d+")) {
                continue;
            }
            
            // Add to keywords if not already present
            if (!keywords.contains(word)) {
                keywords.add(word);
            }
        }
        
        return keywords;
    }
    
    /**
     * Extract semantic meaning from text
     * @param text The text to analyze
     * @return Map of semantic concepts with confidence scores
     */
    public Map<String, Float> extractSemanticMeaning(String text) {
        Map<String, Float> concepts = new HashMap<>();
        
        if (text == null || text.isEmpty()) {
            return concepts;
        }
        
        // Extract keywords first
        List<String> keywords = extractKeywords(text);
        
        // Assign importance scores based on position, frequency, and domain relevance
        for (String keyword : keywords) {
            // Calculate a confidence score for this concept
            float score = calculateConceptScore(keyword, text);
            concepts.put(keyword, score);
        }
        
        // Add any domain-specific concepts
        addDomainConcepts(text, concepts);
        
        return concepts;
    }
    
    /**
     * Calculate a confidence score for a keyword in context
     * @param keyword The keyword
     * @param text The full text
     * @return Confidence score between 0.0 and 1.0
     */
    private float calculateConceptScore(String keyword, String text) {
        // Simple scoring based on frequency
        float score = 0.3f; // Base score
        
        // Count occurrences (case insensitive)
        String lowerText = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        
        int count = 0;
        int index = lowerText.indexOf(lowerKeyword);
        while (index != -1) {
            count++;
            index = lowerText.indexOf(lowerKeyword, index + 1);
        }
        
        // Increase score based on frequency
        score += Math.min(0.4f, count * 0.1f);
        
        // Check if it appears early in the text (more important)
        if (lowerText.indexOf(lowerKeyword) < lowerText.length() / 3) {
            score += 0.1f;
        }
        
        return Math.min(1.0f, score);
    }
    
    /**
     * Add domain-specific concepts to the semantic mapping
     * @param text Original text
     * @param concepts Concepts map to add to
     */
    private void addDomainConcepts(String text, Map<String, Float> concepts) {
        String lowerText = text.toLowerCase();
        
        // Gaming concepts
        if (lowerText.contains("game") || lowerText.contains("play")) {
            concepts.put("gaming", 0.8f);
        }
        
        // AI control concepts
        if (lowerText.contains("assist") || lowerText.contains("help") || 
            lowerText.contains("guide") || lowerText.contains("copilot")) {
            concepts.put("assistance", 0.9f);
        }
        
        if (lowerText.contains("auto") || lowerText.contains("autonomous") ||
            lowerText.contains("control") || lowerText.contains("take over")) {
            concepts.put("automation", 0.9f);
        }
        
        // Learning concepts
        if (lowerText.contains("learn") || lowerText.contains("train") ||
            lowerText.contains("understand") || lowerText.contains("adapt")) {
            concepts.put("learning", 0.8f);
        }
    }
    
    /**
     * Check if a word is a stop word (common word with little meaning)
     * @param word The word to check
     * @return True if it's a stop word
     */
    private boolean isStopWord(String word) {
        // Common English stop words
        String[] stopWords = {
            "the", "and", "that", "have", "for", "not", "with", "you", "this", 
            "but", "his", "from", "they", "she", "will", "would", "there", "their",
            "what", "about", "which", "when", "make", "like", "time", "just", "him",
            "know", "take", "into", "year", "your", "good", "some", "could", "them",
            "than", "then", "now", "over", "also", "back", "after", "use", "two",
            "how", "our", "well", "way", "even", "new", "want", "because", "any",
            "these", "give", "day", "most", "cant", "cant"
        };
        
        for (String stopWord : stopWords) {
            if (stopWord.equals(word)) {
                return true;
            }
        }
        
        return false;
    }
}
