package com.aiassistant.ai.features.copilot;

import java.util.Map;

/**
 * Represents a strategy for the Copilot system
 */
public class CopilotStrategy {
    private String name;
    private String description;
    private AdviceGenerator adviceGenerator;
    
    /**
     * Constructor
     * @param name Strategy name
     * @param description Strategy description
     * @param adviceGenerator Generator for advice
     */
    public CopilotStrategy(String name, String description, AdviceGenerator adviceGenerator) {
        this.name = name;
        this.description = description;
        this.adviceGenerator = adviceGenerator;
    }
    
    /**
     * Get the strategy name
     * @return Strategy name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the strategy description
     * @return Strategy description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the advice generator
     * @return Advice generator
     */
    public AdviceGenerator getAdviceGenerator() {
        return adviceGenerator;
    }
    
    /**
     * Interface for advice generators
     */
    public interface AdviceGenerator {
        String generateAdvice(String game, Map<String, Object> gameState);
    }
}
