package com.aiassistant.features.voice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.aiassistant.core.ai.AIStateManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced Context-Aware Voice Command System that understands commands in context,
 * adapts to user speech patterns, and handles complex multi-turn conversations.
 * 
 * Features:
 * 1. Context-sensitive command recognition
 * 2. Natural language understanding with parameter extraction
 * 3. Command learning from user interactions
 * 4. Support for Hindi-English code-switching
 * 5. Adaptive recognition of user speech patterns
 * 6. Continuous background listening with hotword detection
 * 7. Multi-turn conversation support
 */
public class ContextAwareVoiceCommand {
    private static final String TAG = "ContextAwareVoiceCmd";
    private static final String PREFS_NAME = "voice_command_prefs";
    private static ContextAwareVoiceCommand instance;
    
    // Core components
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private AIStateManager aiStateManager;
    private AudioManager audioManager;
    private SharedPreferences preferences;
    private Handler mainHandler;
    private ScheduledExecutorService scheduler;
    
    // Voice command settings
    private boolean isListening = false;
    private boolean isContinuousListening = false;
    private boolean isHotwordEnabled = true;
    private Set<String> hotwords = new HashSet<>();
    private String currentContext = "default";
    private String preferredLanguage = "en-IN";
    private boolean adaptiveEnabled = true;
    
    // Voice command registry
    private Map<String, Map<String, CommandMatcher>> contextualCommands = new ConcurrentHashMap<>();
    private Map<String, List<String>> commandExamples = new HashMap<>();
    
    // Learning and adaptation
    private Map<String, Integer> commandUsageCounts = new HashMap<>();
    private Map<String, List<String>> userPhraseVariations = new HashMap<>();
    private Map<String, Map<String, String>> userParameterValues = new HashMap<>();
    
    // Conversation state
    private List<String> conversationHistory = new ArrayList<>();
    private Map<String, Object> conversationContext = new HashMap<>();
    private int consecutiveErrors = 0;
    
    // Callbacks
    private VoiceCommandListener commandListener;
    
    /**
     * Listener for command recognition events
     */
    public interface VoiceCommandListener {
        void onCommandRecognized(String command, Map<String, String> parameters, double confidence);
        void onPartialCommandRecognized(String partialCommand);
        void onCommandError(int errorCode);
    }
    
    /**
     * Command matcher for recognizing commands with parameters
     */
    public static class CommandMatcher {
        public String command;
        public List<Pattern> patterns;
        public List<String> parameterNames;
        public boolean isRegex;
        public double priorityWeight;
        
        public CommandMatcher(String command, double priorityWeight) {
            this.command = command;
            this.patterns = new ArrayList<>();
            this.parameterNames = new ArrayList<>();
            this.isRegex = false;
            this.priorityWeight = priorityWeight;
        }
    }
    
    private ContextAwareVoiceCommand(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        
        // Initialize AI state manager
        this.aiStateManager = AIStateManager.getInstance(context);
        
        // Initialize speech recognizer and intent
        initializeSpeechRecognizer();
        
        // Load saved preferences
        loadPreferences();
        
        // Register built-in commands
        registerBuiltInCommands();
        
        // Load learned command variants
        loadLearnedCommands();
    }
    
    public static synchronized ContextAwareVoiceCommand getInstance(Context context) {
        if (instance == null) {
            instance = new ContextAwareVoiceCommand(context);
        }
        return instance;
    }
    
    /**
     * Initialize speech recognizer
     */
    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(createRecognitionListener());
            
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN"); // Default language
            
            Log.d(TAG, "Speech recognizer initialized");
        } else {
            Log.e(TAG, "Speech recognition not available on this device");
        }
    }
    
    /**
     * Create the recognition listener
     */
    private RecognitionListener createRecognitionListener() {
        return new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
            }
            
            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Speech started");
            }
            
            @Override
            public void onRmsChanged(float rmsdB) {
                // Voice level changed
            }
            
            @Override
            public void onBufferReceived(byte[] buffer) {
                // Buffer received
            }
            
            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "Speech ended");
                
                // If we're in continuous listening mode, restart listening after a delay
                if (isContinuousListening) {
                    mainHandler.postDelayed(() -> {
                        if (isContinuousListening) {
                            startListening();
                        }
                    }, 300);
                } else {
                    isListening = false;
                }
            }
            
            @Override
            public void onError(int error) {
                String errorMessage = getErrorText(error);
                Log.e(TAG, "Speech recognition error: " + errorMessage);
                
                consecutiveErrors++;
                
                // Notify listener
                if (commandListener != null) {
                    commandListener.onCommandError(error);
                }
                
                // If we're in continuous listening mode, restart after error
                // (but use exponential backoff if errors persist)
                if (isContinuousListening) {
                    long delay = Math.min(300 * (long)Math.pow(2, consecutiveErrors - 1), 5000);
                    mainHandler.postDelayed(() -> {
                        if (isContinuousListening) {
                            startListening();
                        }
                    }, delay);
                } else {
                    isListening = false;
                }
            }
            
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String bestMatch = matches.get(0);
                    float[] confidenceScores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
                    float confidence = (confidenceScores != null && confidenceScores.length > 0) ? 
                            confidenceScores[0] : 0.5f;
                    
                    Log.d(TAG, "Speech recognized: " + bestMatch + " (confidence: " + confidence + ")");
                    
                    // Process recognition result
                    processRecognitionResult(bestMatch, confidence, matches);
                    
                    // Reset error counter on success
                    consecutiveErrors = 0;
                }
            }
            
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String partialText = matches.get(0);
                    
                    // Check for hotwords if enabled
                    if (isHotwordEnabled && !currentContext.equals("listening")) {
                        checkHotwords(partialText);
                    }
                    
                    // Notify listener of partial result
                    if (commandListener != null) {
                        commandListener.onPartialCommandRecognized(partialText);
                    }
                }
            }
            
            @Override
            public void onEvent(int eventType, Bundle params) {
                // Speech recognition event
            }
        };
    }
    
    /**
     * Get error text for error code
     */
    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No recognition match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error";
        }
    }
    
    /**
     * Load saved preferences
     */
    private void loadPreferences() {
        isHotwordEnabled = preferences.getBoolean("hotword_enabled", true);
        Set<String> savedHotwords = preferences.getStringSet("hotwords", null);
        if (savedHotwords != null) {
            hotwords.addAll(savedHotwords);
        } else {
            // Default hotwords
            hotwords.add("hey assistant");
            hotwords.add("okay assistant");
            hotwords.add("hello assistant");
            hotwords.add("सहायक");  // Hindi for "assistant"
        }
        
        preferredLanguage = preferences.getString("preferred_language", "en-IN");
        adaptiveEnabled = preferences.getBoolean("adaptive_enabled", true);
        
        // Update speech recognizer intent with preferred language
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, preferredLanguage);
    }
    
    /**
     * Save preferences
     */
    public void savePreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hotword_enabled", isHotwordEnabled);
        editor.putStringSet("hotwords", new HashSet<>(hotwords));
        editor.putString("preferred_language", preferredLanguage);
        editor.putBoolean("adaptive_enabled", adaptiveEnabled);
        editor.apply();
    }
    
    /**
     * Register built-in commands
     */
    private void registerBuiltInCommands() {
        // Default context commands
        registerCommand("default", "help", "show me help|help|what can you do|show commands|available commands");
        registerCommand("default", "stop listening", "stop listening|stop|cancel|exit|quit");
        registerCommand("default", "reset", "reset|start over|clear|clear context");
        
        // System commands
        registerCommand("system", "volume up", "volume up|increase volume|louder");
        registerCommand("system", "volume down", "volume down|decrease volume|quieter");
        registerCommand("system", "mute", "mute|silence|quiet");
        registerCommand("system", "unmute", "unmute|restore volume");
        
        // App context
        registerCommand("app", "open app", "open (?<app>\\w+)|launch (?<app>\\w+)", true);
        registerCommand("app", "close app", "close (?<app>\\w+)|exit (?<app>\\w+)", true);
        registerCommand("app", "go back", "go back|back|previous screen");
        
        // Call context
        registerCommand("call", "end call", "end call|hang up|terminate call");
        registerCommand("call", "mute call", "mute call|mute microphone");
        registerCommand("call", "unmute call", "unmute call|unmute microphone");
        registerCommand("call", "speaker on", "speaker on|speakerphone on|turn on speaker");
        registerCommand("call", "speaker off", "speaker off|speakerphone off|turn off speaker");
        
        // Education context
        registerCommand("education", "solve problem", "solve (?<problem>.+)|calculate (?<problem>.+)", true);
        registerCommand("education", "explain concept", "explain (?<concept>.+)|what is (?<concept>.+)", true);
        registerCommand("education", "learn from pdf", "learn from pdf|process pdf|analyze document");
        
        // More contexts can be added as needed
    }
    
    /**
     * Load learned command variants from preferences
     */
    private void loadLearnedCommands() {
        try {
            // Load command usage counts
            Map<String, ?> usageCountsMap = preferences.getAll();
            for (Map.Entry<String, ?> entry : usageCountsMap.entrySet()) {
                if (entry.getKey().startsWith("cmd_count_") && entry.getValue() instanceof Integer) {
                    String command = entry.getKey().substring(10); // Remove "cmd_count_" prefix
                    commandUsageCounts.put(command, (Integer) entry.getValue());
                }
            }
            
            // Load phrase variations
            for (Map.Entry<String, ?> entry : usageCountsMap.entrySet()) {
                if (entry.getKey().startsWith("phrases_") && entry.getValue() instanceof String) {
                    String command = entry.getKey().substring(8); // Remove "phrases_" prefix
                    String phrasesStr = (String) entry.getValue();
                    String[] phrases = phrasesStr.split("\\|");
                    userPhraseVariations.put(command, new ArrayList<>(Arrays.asList(phrases)));
                    
                    // Also register these learned variations
                    String context = "default"; // Assume default context for learned phrases
                    if (command.contains(":")) {
                        String[] parts = command.split(":");
                        context = parts[0];
                        command = parts[1];
                    }
                    
                    registerCommand(context, command, phrasesStr);
                }
            }
            
            // Load parameter values
            for (Map.Entry<String, ?> entry : usageCountsMap.entrySet()) {
                if (entry.getKey().startsWith("params_") && entry.getValue() instanceof String) {
                    String command = entry.getKey().substring(7); // Remove "params_" prefix
                    String paramsStr = (String) entry.getValue();
                    Map<String, String> params = new HashMap<>();
                    
                    String[] paramPairs = paramsStr.split("\\|");
                    for (String pair : paramPairs) {
                        String[] keyValue = pair.split("=", 2);
                        if (keyValue.length == 2) {
                            params.put(keyValue[0], keyValue[1]);
                        }
                    }
                    
                    userParameterValues.put(command, params);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading learned commands: " + e.getMessage());
        }
    }
    
    /**
     * Save learned command variants to preferences
     */
    private void saveLearnedCommands() {
        try {
            SharedPreferences.Editor editor = preferences.edit();
            
            // Save command usage counts
            for (Map.Entry<String, Integer> entry : commandUsageCounts.entrySet()) {
                editor.putInt("cmd_count_" + entry.getKey(), entry.getValue());
            }
            
            // Save phrase variations
            for (Map.Entry<String, List<String>> entry : userPhraseVariations.entrySet()) {
                StringBuilder sb = new StringBuilder();
                for (String phrase : entry.getValue()) {
                    if (sb.length() > 0) sb.append("|");
                    sb.append(phrase);
                }
                editor.putString("phrases_" + entry.getKey(), sb.toString());
            }
            
            // Save parameter values
            for (Map.Entry<String, Map<String, String>> entry : userParameterValues.entrySet()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> param : entry.getValue().entrySet()) {
                    if (sb.length() > 0) sb.append("|");
                    sb.append(param.getKey()).append("=").append(param.getValue());
                }
                editor.putString("params_" + entry.getKey(), sb.toString());
            }
            
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving learned commands: " + e.getMessage());
        }
    }
    
    /**
     * Register a new command pattern
     */
    public void registerCommand(String context, String command, String patternStr) {
        registerCommand(context, command, patternStr, false);
    }
    
    /**
     * Register a new command pattern
     */
    public void registerCommand(String context, String command, String patternStr, boolean isRegex) {
        // Ensure context exists
        if (!contextualCommands.containsKey(context)) {
            contextualCommands.put(context, new ConcurrentHashMap<>());
        }
        
        // Create or get command matcher
        CommandMatcher matcher = contextualCommands.get(context).getOrDefault(command, 
                new CommandMatcher(command, 1.0));
        matcher.isRegex = isRegex;
        
        // Split pattern string by pipe for multiple patterns
        String[] patterns = patternStr.split("\\|");
        for (String pattern : patterns) {
            // For regex patterns, extract parameter names and compile pattern
            if (isRegex) {
                // Find all named capture groups in pattern
                List<String> paramNames = new ArrayList<>();
                Pattern groupPattern = Pattern.compile("\\(\\?<([a-zA-Z0-9]+)>.*?\\)");
                Matcher groupMatcher = groupPattern.matcher(pattern);
                while (groupMatcher.find()) {
                    paramNames.add(groupMatcher.group(1));
                }
                
                // Compile the pattern
                try {
                    matcher.patterns.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
                    matcher.parameterNames.addAll(paramNames);
                } catch (Exception e) {
                    Log.e(TAG, "Error compiling pattern '" + pattern + "': " + e.getMessage());
                }
            } else {
                // For literal patterns, escape and compile
                String escapedPattern = Pattern.quote(pattern.toLowerCase());
                matcher.patterns.add(Pattern.compile(escapedPattern, Pattern.CASE_INSENSITIVE));
            }
        }
        
        // Store command matcher
        contextualCommands.get(context).put(command, matcher);
        
        // Store example for help
        if (!commandExamples.containsKey(context)) {
            commandExamples.put(context, new ArrayList<>());
        }
        commandExamples.get(context).add(command + ": " + patterns[0]);
    }
    
    /**
     * Set command listener
     */
    public void setCommandListener(VoiceCommandListener listener) {
        this.commandListener = listener;
    }
    
    /**
     * Start listening for commands
     */
    public boolean startListening() {
        if (speechRecognizer == null) {
            Log.e(TAG, "Speech recognizer not initialized");
            return false;
        }
        
        try {
            // Release recognizer to ensure clean state
            speechRecognizer.cancel();
            
            // Start listening
            speechRecognizer.startListening(speechRecognizerIntent);
            isListening = true;
            
            Log.d(TAG, "Started listening for commands in context: " + currentContext);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error starting speech recognition: " + e.getMessage());
            isListening = false;
            return false;
        }
    }
    
    /**
     * Stop listening for commands
     */
    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
        }
        isListening = false;
        isContinuousListening = false;
        
        Log.d(TAG, "Stopped listening for commands");
    }
    
    /**
     * Start continuous listening
     */
    public boolean startContinuousListening() {
        isContinuousListening = true;
        return startListening();
    }
    
    /**
     * Check for hotwords in partial results
     */
    private void checkHotwords(String text) {
        String lowerText = text.toLowerCase();
        for (String hotword : hotwords) {
            if (lowerText.contains(hotword.toLowerCase())) {
                Log.d(TAG, "Hotword detected: " + hotword);
                
                // Switch to listening context
                setContext("listening");
                
                // Notify AI state
                aiStateManager.storeMemory("Hotword '" + hotword + "' detected");
                
                break;
            }
        }
    }
    
    /**
     * Process recognition result
     */
    private void processRecognitionResult(String text, float confidence, ArrayList<String> alternativesArray) {
        String lowerText = text.toLowerCase();
        
        // Add to conversation history
        conversationHistory.add(text);
        
        // Track current frame
        conversationContext.put("lastRecognizedText", text);
        conversationContext.put("recognitionConfidence", confidence);
        conversationContext.put("alternatives", alternativesArray);
        
        // First check for hotwords if enabled and not already in listening context
        if (isHotwordEnabled && !currentContext.equals("listening")) {
            for (String hotword : hotwords) {
                if (lowerText.contains(hotword.toLowerCase())) {
                    // Switch to listening context
                    setContext("listening");
                    
                    // Notify AI state
                    aiStateManager.storeMemory("Activated by hotword '" + hotword + "'");
                    
                    return;
                }
            }
        }
        
        // Try to match a command in current context
        CommandMatch match = findCommandMatch(lowerText);
        
        if (match != null) {
            Log.d(TAG, "Command matched: " + match.command + " with confidence " + match.confidence);
            
            // Track command usage
            String commandKey = currentContext + ":" + match.command;
            int count = commandUsageCounts.getOrDefault(commandKey, 0);
            commandUsageCounts.put(commandKey, count + 1);
            
            // Track phrase variation if this is a new variation
            if (!userPhraseVariations.containsKey(commandKey)) {
                userPhraseVariations.put(commandKey, new ArrayList<>());
            }
            if (!userPhraseVariations.get(commandKey).contains(text)) {
                userPhraseVariations.get(commandKey).add(text);
            }
            
            // Track parameter values
            if (!match.parameters.isEmpty()) {
                if (!userParameterValues.containsKey(commandKey)) {
                    userParameterValues.put(commandKey, new HashMap<>());
                }
                for (Map.Entry<String, String> param : match.parameters.entrySet()) {
                    userParameterValues.get(commandKey).put(param.getKey(), param.getValue());
                }
            }
            
            // Save learned data periodically
            if (adaptiveEnabled && Math.random() < 0.1) { // 10% chance to save on each command
                saveLearnedCommands();
            }
            
            // Notify listener of command recognition
            if (commandListener != null) {
                commandListener.onCommandRecognized(match.command, match.parameters, match.confidence);
            }
            
            // Handle built-in commands
            handleBuiltInCommand(match.command, match.parameters);
        } else {
            // No command matched
            Log.d(TAG, "No command matched: " + text);
            
            // If in listening context, try to handle as a conversational query
            if (currentContext.equals("listening")) {
                // Store in AI memory for potential learning
                aiStateManager.storeMemoryWithSentiment(
                        "unrecognized_command",
                        text,
                        0.5);
                
                // In a real implementation, this would dispatch to a conversational AI
                // For now, just notify the listener with a special "unknown" command
                if (commandListener != null) {
                    Map<String, String> params = new HashMap<>();
                    params.put("text", text);
                    commandListener.onCommandRecognized("unknown", params, confidence);
                }
            }
        }
    }
    
    /**
     * Command match result
     */
    private static class CommandMatch {
        String command;
        Map<String, String> parameters;
        double confidence;
        
        CommandMatch(String command, Map<String, String> parameters, double confidence) {
            this.command = command;
            this.parameters = parameters;
            this.confidence = confidence;
        }
    }
    
    /**
     * Find matching command for input text
     */
    private CommandMatch findCommandMatch(String input) {
        CommandMatch bestMatch = null;
        double bestConfidence = 0.0;
        
        // First try current context
        CommandMatch match = findCommandMatchInContext(currentContext, input);
        if (match != null && match.confidence > bestConfidence) {
            bestMatch = match;
            bestConfidence = match.confidence;
        }
        
        // Then try default context if not already in default
        if (!currentContext.equals("default")) {
            match = findCommandMatchInContext("default", input);
            if (match != null && match.confidence > bestConfidence) {
                bestMatch = match;
                bestConfidence = match.confidence;
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Find command match within a specific context
     */
    private CommandMatch findCommandMatchInContext(String context, String input) {
        Map<String, CommandMatcher> commands = contextualCommands.get(context);
        if (commands == null) return null;
        
        CommandMatch bestMatch = null;
        double bestConfidence = 0.0;
        
        for (CommandMatcher matcher : commands.values()) {
            for (int i = 0; i < matcher.patterns.size(); i++) {
                Pattern pattern = matcher.patterns.get(i);
                Matcher m = pattern.matcher(input);
                
                if (m.matches()) {
                    // Extract parameters if this is a regex pattern
                    Map<String, String> parameters = new HashMap<>();
                    if (matcher.isRegex) {
                        for (String paramName : matcher.parameterNames) {
                            try {
                                String value = m.group(paramName);
                                if (value != null) {
                                    parameters.put(paramName, value);
                                }
                            } catch (IllegalArgumentException e) {
                                // Parameter not found in this pattern, ignore
                            }
                        }
                    }
                    
                    // Calculate match confidence
                    // This is a simplified model - a real implementation would be more sophisticated
                    double confidence = 0.7; // Base confidence for a pattern match
                    
                    // Adjust by pattern specificity (longer patterns are more specific)
                    confidence += Math.min(0.2, pattern.pattern().length() / 100.0);
                    
                    // Adjust by command usage frequency
                    String commandKey = context + ":" + matcher.command;
                    int usageCount = commandUsageCounts.getOrDefault(commandKey, 0);
                    confidence += Math.min(0.1, usageCount / 50.0);
                    
                    // Apply pattern priority weight
                    confidence *= matcher.priorityWeight;
                    
                    if (confidence > bestConfidence) {
                        bestMatch = new CommandMatch(matcher.command, parameters, confidence);
                        bestConfidence = confidence;
                    }
                }
            }
        }
        
        return bestMatch;
    }
    
    /**
     * Handle built-in system commands
     */
    private void handleBuiltInCommand(String command, Map<String, String> parameters) {
        switch (command) {
            case "stop listening":
                stopListening();
                break;
                
            case "help":
                // Generate help based on current context
                generateContextHelp();
                break;
                
            case "reset":
                // Reset context and conversation
                resetState();
                break;
                
            case "volume up":
                increaseVolume();
                break;
                
            case "volume down":
                decreaseVolume();
                break;
                
            case "mute":
                setMute(true);
                break;
                
            case "unmute":
                setMute(false);
                break;
                
            // Other built-in commands would be handled here
            
            default:
                // Not a built-in command, handled by listener
                break;
        }
    }
    
    /**
     * Reset state
     */
    private void resetState() {
        conversationHistory.clear();
        conversationContext.clear();
        setContext("default");
    }
    
    /**
     * Generate help based on current context
     */
    private void generateContextHelp() {
        StringBuilder help = new StringBuilder();
        help.append("Commands in ");
        help.append(currentContext);
        help.append(" context:\n");
        
        // Add commands from current context
        List<String> examples = commandExamples.get(currentContext);
        if (examples != null) {
            for (String example : examples) {
                help.append("• ").append(example).append("\n");
            }
        }
        
        // Add default commands if not in default context
        if (!currentContext.equals("default")) {
            help.append("\nYou can also use these general commands:\n");
            List<String> defaultExamples = commandExamples.get("default");
            if (defaultExamples != null) {
                for (String example : defaultExamples) {
                    help.append("• ").append(example).append("\n");
                }
            }
        }
        
        // Store help message in context
        conversationContext.put("helpText", help.toString());
        
        // Let AI state know help was requested
        aiStateManager.storeMemory("User requested help in " + currentContext + " context");
    }
    
    /**
     * Increase system volume
     */
    private void increaseVolume() {
        audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI);
    }
    
    /**
     * Decrease system volume
     */
    private void decreaseVolume() {
        audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI);
    }
    
    /**
     * Set system mute state
     */
    private void setMute(boolean mute) {
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
    }
    
    /**
     * Set current context
     */
    public void setContext(String context) {
        if (contextualCommands.containsKey(context)) {
            this.currentContext = context;
            Log.d(TAG, "Context changed to: " + context);
            
            // Store context change in AI memory
            aiStateManager.storeMemory("Voice command context changed to " + context);
        } else {
            Log.e(TAG, "Unknown context: " + context);
        }
    }
    
    /**
     * Get current context
     */
    public String getContext() {
        return currentContext;
    }
    
    /**
     * Add a hotword
     */
    public void addHotword(String hotword) {
        if (hotword != null && !hotword.isEmpty()) {
            hotwords.add(hotword.toLowerCase());
            savePreferences();
        }
    }
    
    /**
     * Remove a hotword
     */
    public void removeHotword(String hotword) {
        hotwords.remove(hotword.toLowerCase());
        savePreferences();
    }
    
    /**
     * Get all hotwords
     */
    public Set<String> getHotwords() {
        return new HashSet<>(hotwords);
    }
    
    /**
     * Enable or disable hotword detection
     */
    public void setHotwordEnabled(boolean enabled) {
        isHotwordEnabled = enabled;
        savePreferences();
    }
    
    /**
     * Check if hotword detection is enabled
     */
    public boolean isHotwordEnabled() {
        return isHotwordEnabled;
    }
    
    /**
     * Set the preferred language
     */
    public void setPreferredLanguage(String language) {
        this.preferredLanguage = language;
        
        // Update recognizer intent
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        
        savePreferences();
    }
    
    /**
     * Get the preferred language
     */
    public String getPreferredLanguage() {
        return preferredLanguage;
    }
    
    /**
     * Enable or disable adaptive learning
     */
    public void setAdaptiveEnabled(boolean enabled) {
        this.adaptiveEnabled = enabled;
        savePreferences();
    }
    
    /**
     * Check if adaptive learning is enabled
     */
    public boolean isAdaptiveEnabled() {
        return adaptiveEnabled;
    }
    
    /**
     * Check if listening is active
     */
    public boolean isListening() {
        return isListening;
    }
    
    /**
     * Get conversation history
     */
    public List<String> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }
    
    /**
     * Get conversation context
     */
    public Map<String, Object> getConversationContext() {
        return new HashMap<>(conversationContext);
    }
    
    /**
     * Clean up resources
     */
    public void shutdown() {
        // Save learned commands
        if (adaptiveEnabled) {
            saveLearnedCommands();
        }
        
        // Save preferences
        savePreferences();
        
        // Release recognizer
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        
        // Shut down scheduler
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}
