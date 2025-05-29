package com.aiassistant.ai.features.automation;

import java.util.Map;

/**
 * Represents a rule for the Auto AI system
 */
public class AutomationRule {
    private String name;
    private RuleCondition condition;
    private ActionGenerator actionGenerator;
    
    /**
     * Constructor
     * @param name Rule name
     * @param condition Condition that determines when rule applies
     * @param actionGenerator Generator for appropriate actions
     */
    public AutomationRule(String name, RuleCondition condition, ActionGenerator actionGenerator) {
        this.name = name;
        this.condition = condition;
        this.actionGenerator = actionGenerator;
    }
    
    /**
     * Get the rule name
     * @return Rule name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the rule condition
     * @return Rule condition
     */
    public RuleCondition getCondition() {
        return condition;
    }
    
    /**
     * Get the action generator
     * @return Action generator
     */
    public ActionGenerator getActionGenerator() {
        return actionGenerator;
    }
    
    /**
     * Interface for rule conditions
     */
    public interface RuleCondition {
        boolean evaluate(Map<String, Object> context);
    }
    
    /**
     * Interface for action generators
     */
    public interface ActionGenerator {
        AutoAction generateAction(AutoAISystem.AutoMode mode);
    }
}
