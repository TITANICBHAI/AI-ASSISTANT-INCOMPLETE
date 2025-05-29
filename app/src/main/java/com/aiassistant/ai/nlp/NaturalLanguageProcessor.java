// This file is now deprecated. Use the consolidated NaturalLanguageProcessor in core.ai.nlp package instead.
// See: com.aiassistant.core.ai.nlp.NaturalLanguageProcessor

package com.aiassistant.ai.nlp;

import android.content.Context;
import android.util.Log;

import com.aiassistant.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @deprecated This class is kept for backward compatibility only.
 * Please use {@link com.aiassistant.core.ai.nlp.NaturalLanguageProcessor} instead.
 */
@Deprecated
public class NaturalLanguageProcessor {
    private static final String TAG = Constants.TAG_PREFIX + "NLP";
    
    // Context for resource access
    private Context context;
    
    // Intent patterns for command recognition
    private final Map<String, List<Pattern>> intentPatterns;
    
    // Semantic model for understanding meanings
    private WordEmbeddingModel wordEmbeddings;
    
    // Language context for tracking conversation
    private final List<Map<String, Object>> conversationHistory;
    
    // Context entities
    private final Map<String, Object> contextEntities;
    
    /**
     * Constructor
     */
    public NaturalLanguageProcessor() {
        intentPatterns = new HashMap<>();
        conversationHistory = new ArrayList<>();
        contextEntities = new HashMap<>();
        initializeIntentPatterns();
    }
    
    /**
     * Initialize the NLP processor
     * @param context Application context
     */
    public void initialize(Context context) {
        this.context = context;
        
        // Initialize word embeddings
        wordEmbeddings = new WordEmbeddingModel();
        wordEmbeddings.initialize(context);
        
        Log.i(TAG, "Natural Language Processor initialized");
    }
    
    /**
     * Initialize intent patterns
     */
    private void initializeIntentPatterns() {
        // Tap patterns
        List<Pattern> tapPatterns = new ArrayList<>();
        tapPatterns.add(Pattern.compile("(?i)tap(\\s+on)?\\s+(?:the\\s+)?(?:button|element|icon|link)?(?:\\s+with\\s+text)?\\s+['\"]?([\\w\\s]+)['\"]?", Pattern.CASE_INSENSITIVE));
        tapPatterns.add(Pattern.compile("(?i)tap(\\s+at)?\\s+(?:position\\s+)?\\(?(\\d+)\\s*,\\s*(\\d+)\\)?", Pattern.CASE_INSENSITIVE));
        tapPatterns.add(Pattern.compile("(?i)click(\\s+on)?\\s+(?:the\\s+)?(?:button|element|icon|link)?(?:\\s+with\\s+text)?\\s+['\"]?([\\w\\s]+)['\"]?", Pattern.CASE_INSENSITIVE));
        intentPatterns.put("tap", tapPatterns);
        
        // Swipe patterns
        List<Pattern> swipePatterns = new ArrayList<>();
        swipePatterns.add(Pattern.compile("(?i)swipe\\s+(up|down|left|right)", Pattern.CASE_INSENSITIVE));
        swipePatterns.add(Pattern.compile("(?i)swipe\\s+from\\s+\\(?(\\d+)\\s*,\\s*(\\d+)\\)?\\s+to\\s+\\(?(\\d+)\\s*,\\s*(\\d+)\\)?", Pattern.CASE_INSENSITIVE));
        swipePatterns.add(Pattern.compile("(?i)scroll\\s+(up|down|left|right)", Pattern.CASE_INSENSITIVE));
        intentPatterns.put("swipe", swipePatterns);
        
        // Text input patterns
        List<Pattern> textInputPatterns = new ArrayList<>();
        textInputPatterns.add(Pattern.compile("(?i)(?:type|input|enter|write)\\s+(?:the\\s+)?(?:text|string)?\\s+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE));
        textInputPatterns.add(Pattern.compile("(?i)(?:type|input|enter|write)\\s+([\\w\\s]+)\\s+(?:in(?:to)?\\s+(?:the\\s+)?(?:textbox|input|field))?", Pattern.CASE_INSENSITIVE));
        intentPatterns.put("input_text", textInputPatterns);
        
        // Navigation patterns
        List<Pattern> backPatterns = new ArrayList<>();
        backPatterns.add(Pattern.compile("(?i)(?:press|go|navigate)\\s+back", Pattern.CASE_INSENSITIVE));
        intentPatterns.put("back", backPatterns);
        
        List<Pattern> homePatterns = new ArrayList<>();
        homePatterns.add(Pattern.compile("(?i)(?:press|go|navigate|return)(?:\\s+to)?\\s+home", Pattern.CASE_INSENSITIVE));
        intentPatterns.put("home", homePatterns);
        
        // AI control patterns
        List<Pattern> activatePatterns = new ArrayList<>();
        activatePatterns.add(Pattern.compile("(?i)activate\\s+(?:the\\s+)?ai(?:\\s+in\\s+(autonomous|copilot)\\s+mode)?", Pattern.CASE_INSENSITIVE));
        activatePatterns.add(Pattern.compile("(?i)turn\\s+on\\s+(?:the\\s+)?ai(?:\\s+in\\s+(autonomous|copilot)\\s+mode)?", Pattern.CASE_INSENSITIVE));
        activatePatterns.add(Pattern.compile("(?i)start\\s+(?:the\\s+)?ai(?:\\s+in\\s+(autonomous|copilot)\\s+mode)?", Pattern.CASE_INSENSITIVE));
        intentPatterns.put("activate", activatePatterns);
        
        List<Pattern> deactivatePatterns = new ArrayList<>();
        deactivatePatterns.add(Pattern.compile("(?i)deactivate\\s+(?:the\\s+)?ai", Pattern.CASE_INSENSITIVE));
        deactivatePatterns.add(Pattern.compile("(?i)turn\\s+off\\s+(?:the\\s+)?ai", Pattern.CASE_INSENSITIVE));
        deactivatePatterns.add(Pattern.compile("(?i)stop\\s+(?:the\\s+)?ai", Pattern.CASE_INSENSITIVE));
        intentPatterns.put("deactivate", deactivatePatterns);
        
        // Learning patterns
        List<Pattern> learnPatterns = new ArrayList<>();
        learnPatterns.add(Pattern.compile("(?i)learn\\s+(?:about\\s+)?(?:the\\s+)?(?:game\\s+)?([\\w\\s]+)(?:\\s+using\\s+(dqn|sarsa|qlearning|ppo))?", Pattern.CASE_INSENSITIVE));
        learnPatterns.add(Pattern.compile("(?i)start\\s+learning\\s+(?:about\\s+)?(?:the\\s+)?(?:game\\s+)?([\\w\\s]+)(?:\\s+using\\s+(dqn|sarsa|qlearning|ppo))?", Pattern.CASE_INSENSITIVE));
        learnPatterns.add(Pattern.compile("(?i)train\\s+(?:on|for)\\s+(?:the\\s+)?(?:game\\s+)?([\\w\\s]+)(?:\\s+using\\s+(dqn|sarsa|qlearning|ppo))?", Pattern.CASE_INSENSITIVE));
        intentPatterns.put("learn", learnPatterns);
        
        List<Pattern> stopLearningPatterns = new ArrayList<>();
        stopLearningPatterns.add(Pattern.compile("(?i)stop\\s+learning", Pattern.CASE_INSENSITIVE));
        stopLearningPatterns.add(Pattern.compile("(?i)end\\s+(?:the\\s+)?learning\\s+session", Pattern.CASE_INSENSITIVE));
        stopLearningPatterns.add(Pattern.compile("(?i)finish\\s+(?:the\\s+)?learning", Pattern.CASE_INSENSITIVE));
        intentPatterns.put("stop_learning", stopLearningPatterns);
        
        // Analysis patterns
        List<Pattern> analyzePatterns = new ArrayList<>();
        analyzePatterns.add(Pattern.compile("(?i)analyze\\s+(?:the\\s+)?(current\\s+)?(?:game|state)", Pattern.CASE_INSENSITIVE));
        analyzePatterns.add(Pattern.compile("(?i)what\\s+(?:do\\s+you\\s+)?(?:see|understand)(?:\\s+about\\s+(?:the\\s+)?(current\\s+)?(?:game|state))?", Pattern.CASE_INSENSITIVE));
        analyzePatterns.add(Pattern.compile("(?i)describe\\s+(?:the\\s+)?(current\\s+)?(?:game|state)", Pattern.CASE_INSENSITIVE));
        intentPatterns.put("analyze", analyzePatterns);
    }
    
    /**
     * Process text to extract meaning and intent
     * @param text Input text
     * @return Extracted meaning and intent
     */
    public Map<String, Object> processText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return createErrorResult("Empty input");
        }
        
        // Normalize text
        String normalizedText = normalizeText(text);
        
        // Extract intent
        Map<String, Object> result = extractIntent(normalizedText);
        
        // If no intent matched, try to understand semantically
        if ("unknown".equals(result.get("intent"))) {
            result = wordEmbeddings.findClosestIntent(normalizedText, intentPatterns.keySet());
        }
        
        // Add to conversation history
        addToConversationHistory(text, result);
        
        Log.d(TAG, "Processed text: " + text + " -> " + result);
        
        return result;
    }
    
    /**
     * Normalize text for processing
     * @param text Input text
     * @return Normalized text
     */
    private String normalizeText(String text) {
        // Convert to lowercase
        String normalized = text.toLowerCase();
        
        // Remove extra whitespace
        normalized = normalized.trim().replaceAll("\\s+", " ");
        
        // Remove punctuation (except those needed for parsing)
        normalized = normalized.replaceAll("[.!?]", "");
        
        return normalized;
    }
    
    /**
     * Extract intent from text using patterns
     * @param text Input text
     * @return Intent and parameters
     */
    private Map<String, Object> extractIntent(String text) {
        for (Map.Entry<String, List<Pattern>> entry : intentPatterns.entrySet()) {
            String intent = entry.getKey();
            List<Pattern> patterns = entry.getValue();
            
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    return createIntentResult(intent, matcher, text);
                }
            }
        }
        
        // No intent matched
        return createUnknownIntent(text);
    }
    
    /**
     * Create an intent result from a matcher
     * @param intent Intent name
     * @param matcher Pattern matcher
     * @param originalText Original text
     * @return Intent result
     */
    private Map<String, Object> createIntentResult(String intent, Matcher matcher, String originalText) {
        Map<String, Object> result = new HashMap<>();
        result.put("intent", intent);
        result.put("confidence", 0.9f); // High confidence for pattern matches
        result.put("original_text", originalText);
        
        switch (intent) {
            case "tap":
                handleTapIntent(result, matcher);
                break;
                
            case "swipe":
                handleSwipeIntent(result, matcher);
                break;
                
            case "input_text":
                if (matcher.groupCount() >= 1) {
                    result.put("text", matcher.group(1));
                }
                break;
                
            case "activate":
                if (matcher.groupCount() >= 1 && matcher.group(1) != null) {
                    result.put("mode", matcher.group(1).toLowerCase());
                } else {
                    result.put("mode", "autonomous"); // Default mode
                }
                break;
                
            case "learn":
                if (matcher.groupCount() >= 1) {
                    result.put("game_name", matcher.group(1));
                    
                    if (matcher.groupCount() >= 2 && matcher.group(2) != null) {
                        result.put("algorithm", matcher.group(2).toLowerCase());
                    } else {
                        result.put("algorithm", "dqn"); // Default algorithm
                    }
                }
                break;
                
            case "analyze":
                if (originalText.contains("game")) {
                    result.put("target", "game");
                } else {
                    result.put("target", "state");
                }
                break;
        }
        
        return result;
    }
    
    /**
     * Handle tap intent extraction
     * @param result Result map
     * @param matcher Pattern matcher
     */
    private void handleTapIntent(Map<String, Object> result, Matcher matcher) {
        if (matcher.groupCount() >= 3 && matcher.group(2) != null && matcher.group(3) != null) {
            // Coordinate-based tap
            try {
                int x = Integer.parseInt(matcher.group(2));
                int y = Integer.parseInt(matcher.group(3));
                
                result.put("x", x);
                result.put("y", y);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing tap coordinates", e);
            }
        } else if (matcher.groupCount() >= 1 && matcher.group(1) != null) {
            // Text-based tap
            result.put("element_text", matcher.group(1));
        }
    }
    
    /**
     * Handle swipe intent extraction
     * @param result Result map
     * @param matcher Pattern matcher
     */
    private void handleSwipeIntent(Map<String, Object> result, Matcher matcher) {
        if (matcher.groupCount() >= 4 && 
            matcher.group(1) != null && matcher.group(2) != null && 
            matcher.group(3) != null && matcher.group(4) != null) {
            // Coordinate-based swipe
            try {
                int startX = Integer.parseInt(matcher.group(1));
                int startY = Integer.parseInt(matcher.group(2));
                int endX = Integer.parseInt(matcher.group(3));
                int endY = Integer.parseInt(matcher.group(4));
                
                result.put("start_x", startX);
                result.put("start_y", startY);
                result.put("end_x", endX);
                result.put("end_y", endY);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing swipe coordinates", e);
            }
        } else if (matcher.groupCount() >= 1 && matcher.group(1) != null) {
            // Direction-based swipe
            result.put("direction", matcher.group(1).toLowerCase());
        }
    }
    
    /**
     * Create an unknown intent result
     * @param text Input text
     * @return Unknown intent result
     */
    private Map<String, Object> createUnknownIntent(String text) {
        Map<String, Object> result = new HashMap<>();
        result.put("intent", "unknown");
        result.put("confidence", 0.0f);
        result.put("original_text", text);
        return result;
    }
    
    /**
     * Create an error result
     * @param error Error message
     * @return Error result
     */
    private Map<String, Object> createErrorResult(String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("intent", "error");
        result.put("error", error);
        result.put("confidence", 0.0f);
        return result;
    }
    
    /**
     * Add a conversation entry to history
     * @param userText User input text
     * @param processingResult Processing result
     */
    private void addToConversationHistory(String userText, Map<String, Object> processingResult) {
        Map<String, Object> entry = new HashMap<>();
        entry.put("user_text", userText);
        entry.put("processing_result", new HashMap<>(processingResult));
        entry.put("timestamp", System.currentTimeMillis());
        
        conversationHistory.add(entry);
        
        // Limit history size
        if (conversationHistory.size() > 20) {
            conversationHistory.remove(0);
        }
    }
    
    /**
     * Get the conversation history
     * @return Conversation history
     */
    public List<Map<String, Object>> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }
    
    /**
     * Get the most recent user request
     * @return Most recent user request or null
     */
    public String getMostRecentUserRequest() {
        if (conversationHistory.isEmpty()) {
            return null;
        }
        
        Map<String, Object> lastEntry = conversationHistory.get(conversationHistory.size() - 1);
        return (String) lastEntry.get("user_text");
    }
    
    /**
     * Add a context entity
     * @param key Entity key
     * @param value Entity value
     */
    public void addContextEntity(String key, Object value) {
        contextEntities.put(key, value);
    }
    
    /**
     * Get a context entity
     * @param key Entity key
     * @return Entity value or null
     */
    public Object getContextEntity(String key) {
        return contextEntities.get(key);
    }
    
    /**
     * Clear all context entities
     */
    public void clearContextEntities() {
        contextEntities.clear();
    }
    
    /**
     * Clear the conversation history
     */
    public void clearConversationHistory() {
        conversationHistory.clear();
    }
}