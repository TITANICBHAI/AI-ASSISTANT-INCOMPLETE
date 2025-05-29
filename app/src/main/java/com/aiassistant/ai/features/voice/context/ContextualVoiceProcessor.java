package com.aiassistant.ai.features.voice.context;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processor for context-aware voice commands that maintains
 * conversation state and interprets commands based on context.
 */
public class ContextualVoiceProcessor {
    private static final String TAG = "ContextualVoiceProc";
    
    // Android context
    private android.content.Context androidContext;
    
    // Conversation context
    private com.aiassistant.ai.features.voice.context.Context conversationContext;
    
    // Domain handlers for different conversation domains
    private Map<String, DomainHandler> domainHandlers;
    
    // Intent patterns for detecting command intents
    private List<IntentPattern> intentPatterns;
    
    /**
     * Constructor
     * @param context Android context
     */
    public ContextualVoiceProcessor(android.content.Context context) {
        this.androidContext = context;
        this.conversationContext = new com.aiassistant.ai.features.voice.context.Context();
        this.domainHandlers = new HashMap<>();
        this.intentPatterns = new ArrayList<>();
        
        // Initialize domain handlers and intent patterns
        initializeDomainHandlers();
        initializeIntentPatterns();
        
        Log.i(TAG, "ContextualVoiceProcessor initialized");
    }
    
    /**
     * Initialize domain handlers
     */
    private void initializeDomainHandlers() {
        // Register domain handlers for different conversation domains
        domainHandlers.put("general", new GeneralDomainHandler());
        domainHandlers.put("education", new EducationDomainHandler());
        domainHandlers.put("math", new MathDomainHandler());
        domainHandlers.put("physics", new PhysicsDomainHandler());
        domainHandlers.put("chemistry", new ChemistryDomainHandler());
        domainHandlers.put("jee", new JEEDomainHandler());
    }
    
    /**
     * Initialize intent patterns for command detection
     */
    private void initializeIntentPatterns() {
        // Domain switching intents
        intentPatterns.add(new IntentPattern(
            "switchDomain",
            "(?i)(switch|go|change)\\s+to\\s+(math|physics|chemistry|jee|education).*",
            2
        ));
        
        // Subject setting intents
        intentPatterns.add(new IntentPattern(
            "setSubject",
            "(?i)let('s|\\s+us)?\\s+talk\\s+about\\s+(.+)",
            2
        ));
        
        // Question asking intents
        intentPatterns.add(new IntentPattern(
            "askQuestion",
            "(?i)(solve|answer|calculate|compute|determine|find|evaluate)\\s+(.+)",
            2
        ));
        
        // JEE specific intents
        intentPatterns.add(new IntentPattern(
            "solveJEE",
            "(?i)(solve|answer|calculate|compute|determine|find|evaluate)\\s+((this|the)\\s+)?jee\\s+(question|problem)\\s*:?\\s*(.+)",
            5
        ));
        
        // Context inquiry intents
        intentPatterns.add(new IntentPattern(
            "askContext",
            "(?i)(what|which|where)\\s+(are\\s+we\\s+talking\\s+about|is\\s+our\\s+context|subject\\s+are\\s+we\\s+on).*",
            0
        ));
        
        // Reset context intents
        intentPatterns.add(new IntentPattern(
            "resetContext",
            "(?i)(reset|clear|restart)\\s+(context|conversation).*",
            0
        ));
    }
    
    /**
     * Process a voice command
     * @param command Command text
     * @return Response
     */
    public String processCommand(String command) {
        // Add command to conversation history
        conversationContext.addToHistory(command);
        
        // Try to match an intent pattern
        CommandIntent intent = matchIntent(command);
        
        if (intent != null) {
            Log.d(TAG, "Matched intent: " + intent.getIntentType());
            
            // Handle domain switching
            if (intent.getIntentType().equals("switchDomain")) {
                String newDomain = intent.getParameter();
                conversationContext.setDomain(newDomain);
                return "Switched to " + newDomain + " domain.";
            }
            
            // Handle subject setting
            if (intent.getIntentType().equals("setSubject")) {
                String subject = intent.getParameter();
                conversationContext.setCurrentSubject(subject);
                return "I'll talk about " + subject + " with you.";
            }
            
            // Handle generic question asking
            if (intent.getIntentType().equals("askQuestion")) {
                // Route to appropriate domain handler
                DomainHandler handler = getDomainHandler(conversationContext.getCurrentDomain());
                if (handler != null) {
                    return handler.handleQuestion(intent.getParameter(), conversationContext);
                }
            }
            
            // Handle JEE question solving
            if (intent.getIntentType().equals("solveJEE")) {
                // Switch to JEE domain and handle the question
                conversationContext.setDomain("jee");
                DomainHandler handler = getDomainHandler("jee");
                if (handler != null) {
                    return handler.handleQuestion(intent.getParameter(), conversationContext);
                }
            }
            
            // Handle context inquiry
            if (intent.getIntentType().equals("askContext")) {
                String domain = conversationContext.getCurrentDomain();
                String subject = conversationContext.getCurrentSubject();
                if (subject != null) {
                    return "We're talking about " + subject + " in the " + domain + " domain.";
                } else {
                    return "We're in the " + domain + " domain.";
                }
            }
            
            // Handle context reset
            if (intent.getIntentType().equals("resetContext")) {
                conversationContext.reset();
                return "I've reset our conversation context.";
            }
        }
        
        // If no specific intent matched, handle as general conversation
        // based on current domain
        DomainHandler handler = getDomainHandler(conversationContext.getCurrentDomain());
        if (handler != null) {
            return handler.handleGenericInput(command, conversationContext);
        }
        
        // Fallback
        return "I'm not sure how to respond to that.";
    }
    
    /**
     * Match command text to an intent pattern
     * @param command Command text
     * @return Matched intent or null if no match
     */
    private CommandIntent matchIntent(String command) {
        for (IntentPattern pattern : intentPatterns) {
            Pattern regex = Pattern.compile(pattern.getPattern());
            Matcher matcher = regex.matcher(command);
            
            if (matcher.matches()) {
                String parameter = "";
                if (pattern.getParameterGroup() > 0 && matcher.groupCount() >= pattern.getParameterGroup()) {
                    parameter = matcher.group(pattern.getParameterGroup());
                }
                
                return new CommandIntent(pattern.getIntentType(), parameter);
            }
        }
        
        return null;
    }
    
    /**
     * Get the domain handler for a specific domain
     * @param domain Domain name
     * @return Domain handler or null if not found
     */
    private DomainHandler getDomainHandler(String domain) {
        DomainHandler handler = domainHandlers.get(domain);
        if (handler == null) {
            // Fallback to general domain
            handler = domainHandlers.get("general");
        }
        return handler;
    }
    
    /**
     * Get the conversation context
     * @return Conversation context
     */
    public com.aiassistant.ai.features.voice.context.Context getConversationContext() {
        return conversationContext;
    }
    
    /**
     * Reset the conversation context
     */
    public void resetContext() {
        conversationContext.reset();
    }
    
    /**
     * Interface for domain-specific handlers
     */
    public interface DomainHandler {
        /**
         * Handle a question in this domain
         * @param question Question text
         * @param context Conversation context
         * @return Response
         */
        String handleQuestion(String question, com.aiassistant.ai.features.voice.context.Context context);
        
        /**
         * Handle generic input in this domain
         * @param input Input text
         * @param context Conversation context
         * @return Response
         */
        String handleGenericInput(String input, com.aiassistant.ai.features.voice.context.Context context);
    }
    
    /**
     * Class representing an intent pattern for matching commands
     */
    private static class IntentPattern {
        private String intentType;
        private String pattern;
        private int parameterGroup;
        
        public IntentPattern(String intentType, String pattern, int parameterGroup) {
            this.intentType = intentType;
            this.pattern = pattern;
            this.parameterGroup = parameterGroup;
        }
        
        public String getIntentType() {
            return intentType;
        }
        
        public String getPattern() {
            return pattern;
        }
        
        public int getParameterGroup() {
            return parameterGroup;
        }
    }
    
    /**
     * Class representing a matched command intent
     */
    private static class CommandIntent {
        private String intentType;
        private String parameter;
        
        public CommandIntent(String intentType, String parameter) {
            this.intentType = intentType;
            this.parameter = parameter;
        }
        
        public String getIntentType() {
            return intentType;
        }
        
        public String getParameter() {
            return parameter;
        }
    }
    
    /**
     * Handler for general domain
     */
    private class GeneralDomainHandler implements DomainHandler {
        @Override
        public String handleQuestion(String question, com.aiassistant.ai.features.voice.context.Context context) {
            // Generic question handling - could route to a general knowledge system
            return "To answer your question about " + question + ", I would need to search for information.";
        }
        
        @Override
        public String handleGenericInput(String input, com.aiassistant.ai.features.voice.context.Context context) {
            // Simple conversational response
            return "I understand your general input. You can ask me to switch to a specific domain like math, physics, chemistry, or JEE for more specialized assistance.";
        }
    }
    
    /**
     * Handler for education domain
     */
    private class EducationDomainHandler implements DomainHandler {
        @Override
        public String handleQuestion(String question, com.aiassistant.ai.features.voice.context.Context context) {
            // Handle education-related questions
            return "Your education question about " + question + " is important. You can ask me about specific subjects like math, physics, or chemistry.";
        }
        
        @Override
        public String handleGenericInput(String input, com.aiassistant.ai.features.voice.context.Context context) {
            // Suggest educational topics
            return "I can help with various educational topics. Would you like to discuss math, physics, chemistry, or JEE preparation?";
        }
    }
    
    /**
     * Handler for math domain
     */
    private class MathDomainHandler implements DomainHandler {
        @Override
        public String handleQuestion(String question, com.aiassistant.ai.features.voice.context.Context context) {
            // Math question handling could integrate with a math solver
            return "I'll solve the math problem: " + question + ". Would you like me to show the step-by-step solution?";
        }
        
        @Override
        public String handleGenericInput(String input, com.aiassistant.ai.features.voice.context.Context context) {
            // Math domain conversation
            return "We're discussing mathematics. You can ask me to solve specific math problems or equations.";
        }
    }
    
    /**
     * Handler for physics domain
     */
    private class PhysicsDomainHandler implements DomainHandler {
        @Override
        public String handleQuestion(String question, com.aiassistant.ai.features.voice.context.Context context) {
            // Physics question handling
            return "Your physics question about " + question + " involves understanding physical principles. Would you like me to explain the concepts or solve a specific problem?";
        }
        
        @Override
        public String handleGenericInput(String input, com.aiassistant.ai.features.voice.context.Context context) {
            // Physics domain conversation
            return "We're discussing physics. You can ask me questions about mechanics, thermodynamics, electromagnetism, or other physics topics.";
        }
    }
    
    /**
     * Handler for chemistry domain
     */
    private class ChemistryDomainHandler implements DomainHandler {
        @Override
        public String handleQuestion(String question, com.aiassistant.ai.features.voice.context.Context context) {
            // Chemistry question handling
            return "Your chemistry question about " + question + " deals with chemical principles. Would you like me to explain the concepts or solve a specific problem?";
        }
        
        @Override
        public String handleGenericInput(String input, com.aiassistant.ai.features.voice.context.Context context) {
            // Chemistry domain conversation
            return "We're discussing chemistry. You can ask me questions about chemical reactions, elements, organic chemistry, or other chemistry topics.";
        }
    }
    
    /**
     * Handler for JEE domain
     */
    private class JEEDomainHandler implements DomainHandler {
        @Override
        public String handleQuestion(String question, com.aiassistant.ai.features.voice.context.Context context) {
            // For JEE questions, route to the JEE solver
            try {
                com.aiassistant.ai.features.education.jee.JEESolver solver = 
                    new com.aiassistant.ai.features.education.jee.JEESolver();
                return solver.solveJEEQuestion(question);
            } catch (Exception e) {
                Log.e(TAG, "Error solving JEE question", e);
                return "I encountered an error while trying to solve this JEE question. Could you please rephrase it?";
            }
        }
        
        @Override
        public String handleGenericInput(String input, com.aiassistant.ai.features.voice.context.Context context) {
            // JEE domain conversation
            return "We're discussing JEE preparation. You can ask me to solve specific JEE problems in mathematics, physics, or chemistry.";
        }
    }
}
