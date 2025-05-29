package com.aiassistant.core.speech;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Advanced dialogue generation system that creates dynamic, contextual speech content
 * based on game situation, user relationship, and AI personality.
 */
public class DynamicDialogueGenerator {
    private static final String TAG = "DynamicDialogueGen";
    
    // Singleton instance
    private static DynamicDialogueGenerator instance;
    
    // Core components
    private Context context;
    private Random random = new Random();
    
    // Template storage
    private Map<String, List<DialogueTemplate>> combatTemplates = new HashMap<>();
    private Map<String, List<DialogueTemplate>> tacticalTemplates = new HashMap<>();
    private Map<String, List<DialogueTemplate>> instructionalTemplates = new HashMap<>();
    private Map<String, List<DialogueTemplate>> alertTemplates = new HashMap<>();
    private Map<String, List<DialogueTemplate>> encouragementTemplates = new HashMap<>();
    
    // Personality configuration
    private PersonalityProfile personalityProfile = new PersonalityProfile();
    
    // Context tracking
    private GameSituationContext currentContext = new GameSituationContext();
    
    /**
     * Get singleton instance
     */
    public static synchronized DynamicDialogueGenerator getInstance(Context context) {
        if (instance == null) {
            instance = new DynamicDialogueGenerator(context);
        }
        return instance;
    }
    
    /**
     * Constructor
     */
    private DynamicDialogueGenerator(Context context) {
        this.context = context.getApplicationContext();
        initializeDialogueTemplates();
    }
    
    /**
     * Initialize default dialogue templates
     */
    private void initializeDialogueTemplates() {
        // Combat templates
        initializeCombatTemplates();
        
        // Tactical templates
        initializeTacticalTemplates();
        
        // Instructional templates
        initializeInstructionalTemplates();
        
        // Alert templates
        initializeAlertTemplates();
        
        // Encouragement templates
        initializeEncouragementTemplates();
    }
    
    /**
     * Initialize combat dialogue templates
     */
    private void initializeCombatTemplates() {
        // Attack recommendations
        List<DialogueTemplate> attackTemplates = new ArrayList<>();
        
        attackTemplates.add(new DialogueTemplate(
            "Target the {targetType} at your {direction}. {targetWeakness}.",
            new String[]{"targetType", "direction", "targetWeakness"},
            70
        ));
        
        attackTemplates.add(new DialogueTemplate(
            "{targetType} detected. Engage with {recommendedWeapon} for optimal damage.",
            new String[]{"targetType", "recommendedWeapon"},
            80
        ));
        
        attackTemplates.add(new DialogueTemplate(
            "Now's your chance to strike! The {targetType} is vulnerable.",
            new String[]{"targetType"},
            60
        ));
        
        attackTemplates.add(new DialogueTemplate(
            "Tactical analysis indicates {attackStrategy} will be most effective against this {targetType}.",
            new String[]{"attackStrategy", "targetType"},
            85
        ));
        
        attackTemplates.add(new DialogueTemplate(
            "Focus fire on the {targetBodyPart} for critical damage.",
            new String[]{"targetBodyPart"},
            75
        ));
        
        combatTemplates.put("attack", attackTemplates);
        
        // Defense recommendations
        List<DialogueTemplate> defenseTemplates = new ArrayList<>();
        
        defenseTemplates.add(new DialogueTemplate(
            "Incoming attack! {defenseAction} to avoid damage.",
            new String[]{"defenseAction"},
            85
        ));
        
        defenseTemplates.add(new DialogueTemplate(
            "Enemy targeting you from {direction}. Recommend {defenseAction}.",
            new String[]{"direction", "defenseAction"},
            80
        ));
        
        defenseTemplates.add(new DialogueTemplate(
            "Danger level critical. {defenseAction} immediately!",
            new String[]{"defenseAction"},
            90
        ));
        
        defenseTemplates.add(new DialogueTemplate(
            "Your {vulnerableArea} is exposed. {defenseAction} to protect yourself.",
            new String[]{"vulnerableArea", "defenseAction"},
            75
        ));
        
        defenseTemplates.add(new DialogueTemplate(
            "Multiple hostiles converging. Recommended response: {defenseAction}.",
            new String[]{"defenseAction"},
            85
        ));
        
        combatTemplates.put("defense", defenseTemplates);
    }
    
    /**
     * Initialize tactical dialogue templates
     */
    private void initializeTacticalTemplates() {
        // Position recommendations
        List<DialogueTemplate> positionTemplates = new ArrayList<>();
        
        positionTemplates.add(new DialogueTemplate(
            "Strategic position detected at {locationDescription}. This provides {tacticalAdvantage}.",
            new String[]{"locationDescription", "tacticalAdvantage"},
            80
        ));
        
        positionTemplates.add(new DialogueTemplate(
            "Move to {locationDescription} to gain {tacticalAdvantage}.",
            new String[]{"locationDescription", "tacticalAdvantage"},
            75
        ));
        
        positionTemplates.add(new DialogueTemplate(
            "Current position compromised. Relocate to {locationDescription}.",
            new String[]{"locationDescription"},
            85
        ));
        
        positionTemplates.add(new DialogueTemplate(
            "Enemies likely to approach from {direction}. Position yourself at {locationDescription}.",
            new String[]{"direction", "locationDescription"},
            80
        ));
        
        positionTemplates.add(new DialogueTemplate(
            "Tactical analysis complete. Optimal position: {locationDescription}.",
            new String[]{"locationDescription"},
            75
        ));
        
        tacticalTemplates.put("position", positionTemplates);
        
        // Resource management
        List<DialogueTemplate> resourceTemplates = new ArrayList<>();
        
        resourceTemplates.add(new DialogueTemplate(
            "{resourceType} levels critical at {percentRemaining}%. Locate supplies at {locationDescription}.",
            new String[]{"resourceType", "percentRemaining", "locationDescription"},
            85
        ));
        
        resourceTemplates.add(new DialogueTemplate(
            "Detected {resourceType} nearby. {collectionStrategy} to acquire.",
            new String[]{"resourceType", "collectionStrategy"},
            75
        ));
        
        resourceTemplates.add(new DialogueTemplate(
            "Current {resourceType} insufficient for upcoming challenge. Prioritize acquisition.",
            new String[]{"resourceType"},
            80
        ));
        
        resourceTemplates.add(new DialogueTemplate(
            "{resourceType} optimization recommended. {optimizationStrategy}.",
            new String[]{"resourceType", "optimizationStrategy"},
            70
        ));
        
        resourceTemplates.add(new DialogueTemplate(
            "Strategic analysis: {resourceType} will be critical in the next {timeFrame}.",
            new String[]{"resourceType", "timeFrame"},
            75
        ));
        
        tacticalTemplates.put("resource", resourceTemplates);
    }
    
    /**
     * Initialize instructional dialogue templates
     */
    private void initializeInstructionalTemplates() {
        // Tutorial guidance
        List<DialogueTemplate> tutorialTemplates = new ArrayList<>();
        
        tutorialTemplates.add(new DialogueTemplate(
            "To perform {actionName}, {actionInstructions}.",
            new String[]{"actionName", "actionInstructions"},
            70
        ));
        
        tutorialTemplates.add(new DialogueTemplate(
            "Let me guide you. {actionInstructions}, then {nextActionInstructions}.",
            new String[]{"actionInstructions", "nextActionInstructions"},
            75
        ));
        
        tutorialTemplates.add(new DialogueTemplate(
            "For optimal results with {actionName}, remember to {actionTip}.",
            new String[]{"actionName", "actionTip"},
            65
        ));
        
        tutorialTemplates.add(new DialogueTemplate(
            "New ability detected: {abilityName}. To use it, {abilityInstructions}.",
            new String[]{"abilityName", "abilityInstructions"},
            80
        ));
        
        tutorialTemplates.add(new DialogueTemplate(
            "This challenge requires {requiredAction}. I suggest {actionInstructions}.",
            new String[]{"requiredAction", "actionInstructions"},
            75
        ));
        
        instructionalTemplates.put("tutorial", tutorialTemplates);
        
        // Puzzle solving
        List<DialogueTemplate> puzzleTemplates = new ArrayList<>();
        
        puzzleTemplates.add(new DialogueTemplate(
            "I notice {puzzleElement} which suggests {puzzleSolution}.",
            new String[]{"puzzleElement", "puzzleSolution"},
            70
        ));
        
        puzzleTemplates.add(new DialogueTemplate(
            "This puzzle requires {puzzleRequirement}. Look for {puzzleClue}.",
            new String[]{"puzzleRequirement", "puzzleClue"},
            75
        ));
        
        puzzleTemplates.add(new DialogueTemplate(
            "Consider the relationship between {puzzleElement1} and {puzzleElement2}.",
            new String[]{"puzzleElement1", "puzzleElement2"},
            65
        ));
        
        puzzleTemplates.add(new DialogueTemplate(
            "Based on previous patterns, the solution involves {puzzleSolution}.",
            new String[]{"puzzleSolution"},
            80
        ));
        
        puzzleTemplates.add(new DialogueTemplate(
            "The {puzzleType} puzzle typically requires {puzzleSolutionApproach}.",
            new String[]{"puzzleType", "puzzleSolutionApproach"},
            75
        ));
        
        instructionalTemplates.put("puzzle", puzzleTemplates);
    }
    
    /**
     * Initialize alert dialogue templates
     */
    private void initializeAlertTemplates() {
        // Danger alerts
        List<DialogueTemplate> dangerTemplates = new ArrayList<>();
        
        dangerTemplates.add(new DialogueTemplate(
            "Warning! {dangerType} detected at {direction}.",
            new String[]{"dangerType", "direction"},
            90
        ));
        
        dangerTemplates.add(new DialogueTemplate(
            "Critical alert: {dangerType} approaching. ETA: {timeFrame}.",
            new String[]{"dangerType", "timeFrame"},
            95
        ));
        
        dangerTemplates.add(new DialogueTemplate(
            "Sensors indicate {dangerType} nearby. Exercise caution.",
            new String[]{"dangerType"},
            85
        ));
        
        dangerTemplates.add(new DialogueTemplate(
            "Danger level {dangerLevel}. {dangerDescription}.",
            new String[]{"dangerLevel", "dangerDescription"},
            90
        ));
        
        dangerTemplates.add(new DialogueTemplate(
            "Immediate threat: {dangerType} at {direction}. Recommend {escapeStrategy}.",
            new String[]{"dangerType", "direction", "escapeStrategy"},
            95
        ));
        
        alertTemplates.put("danger", dangerTemplates);
        
        // Opportunity alerts
        List<DialogueTemplate> opportunityTemplates = new ArrayList<>();
        
        opportunityTemplates.add(new DialogueTemplate(
            "Opportunity detected: {opportunityType} at {direction}.",
            new String[]{"opportunityType", "direction"},
            80
        ));
        
        opportunityTemplates.add(new DialogueTemplate(
            "{opportunityType} available. Recommend immediate action.",
            new String[]{"opportunityType"},
            75
        ));
        
        opportunityTemplates.add(new DialogueTemplate(
            "Favorable conditions for {actionType}. {benefitDescription}.",
            new String[]{"actionType", "benefitDescription"},
            70
        ));
        
        opportunityTemplates.add(new DialogueTemplate(
            "Strategic opportunity: {opportunityDescription}. Available for {timeFrame}.",
            new String[]{"opportunityDescription", "timeFrame"},
            80
        ));
        
        opportunityTemplates.add(new DialogueTemplate(
            "Analysis indicates optimal moment for {actionType}. Proceed now for maximum effect.",
            new String[]{"actionType"},
            85
        ));
        
        alertTemplates.put("opportunity", opportunityTemplates);
    }
    
    /**
     * Initialize encouragement dialogue templates
     */
    private void initializeEncouragementTemplates() {
        // Success feedback
        List<DialogueTemplate> successTemplates = new ArrayList<>();
        
        successTemplates.add(new DialogueTemplate(
            "Excellent! {accomplishmentDescription} shows impressive skill.",
            new String[]{"accomplishmentDescription"},
            70
        ));
        
        successTemplates.add(new DialogueTemplate(
            "Well done. {accomplishmentDescription} completed with {performanceLevel} efficiency.",
            new String[]{"accomplishmentDescription", "performanceLevel"},
            75
        ));
        
        successTemplates.add(new DialogueTemplate(
            "That was {performanceLevel}! Your {skillType} is improving.",
            new String[]{"performanceLevel", "skillType"},
            65
        ));
        
        successTemplates.add(new DialogueTemplate(
            "Success! {accomplishmentDescription} has {benefitDescription}.",
            new String[]{"accomplishmentDescription", "benefitDescription"},
            70
        ));
        
        successTemplates.add(new DialogueTemplate(
            "Impressive performance. {statisticDescription} exceeds previous record.",
            new String[]{"statisticDescription"},
            80
        ));
        
        encouragementTemplates.put("success", successTemplates);
        
        // Failure support
        List<DialogueTemplate> failureTemplates = new ArrayList<>();
        
        failureTemplates.add(new DialogueTemplate(
            "Don't worry. {failureDescription} happens to everyone. Try {improvementStrategy}.",
            new String[]{"failureDescription", "improvementStrategy"},
            75
        ));
        
        failureTemplates.add(new DialogueTemplate(
            "I noticed {errorDescription}. Next time, consider {improvementStrategy}.",
            new String[]{"errorDescription", "improvementStrategy"},
            70
        ));
        
        failureTemplates.add(new DialogueTemplate(
            "Learning opportunity: {lessonDescription}. Keep trying.",
            new String[]{"lessonDescription"},
            65
        ));
        
        failureTemplates.add(new DialogueTemplate(
            "That was challenging. Analysis suggests {improvementStrategy} for better results.",
            new String[]{"improvementStrategy"},
            70
        ));
        
        failureTemplates.add(new DialogueTemplate(
            "Progress requires persistence. {encouragementPhrase}. Try again with {improvementStrategy}.",
            new String[]{"encouragementPhrase", "improvementStrategy"},
            75
        ));
        
        encouragementTemplates.put("failure", failureTemplates);
    }
    
    /**
     * Generate dynamic dialogue for a specific game situation
     */
    public String generateCombatDialogue(Map<String, String> parameters, String combatType) {
        List<DialogueTemplate> templates = combatTemplates.get(combatType);
        if (templates == null || templates.isEmpty()) {
            return "Combat situation detected.";
        }
        
        return generateFromTemplates(templates, parameters);
    }
    
    /**
     * Generate tactical dialogue
     */
    public String generateTacticalDialogue(Map<String, String> parameters, String tacticalType) {
        List<DialogueTemplate> templates = tacticalTemplates.get(tacticalType);
        if (templates == null || templates.isEmpty()) {
            return "Tactical opportunity available.";
        }
        
        return generateFromTemplates(templates, parameters);
    }
    
    /**
     * Generate instructional dialogue
     */
    public String generateInstructionalDialogue(Map<String, String> parameters, String instructionType) {
        List<DialogueTemplate> templates = instructionalTemplates.get(instructionType);
        if (templates == null || templates.isEmpty()) {
            return "Let me explain how to proceed.";
        }
        
        return generateFromTemplates(templates, parameters);
    }
    
    /**
     * Generate alert dialogue
     */
    public String generateAlertDialogue(Map<String, String> parameters, String alertType) {
        List<DialogueTemplate> templates = alertTemplates.get(alertType);
        if (templates == null || templates.isEmpty()) {
            return "Alert! Situation requires attention.";
        }
        
        return generateFromTemplates(templates, parameters);
    }
    
    /**
     * Generate encouragement dialogue
     */
    public String generateEncouragementDialogue(Map<String, String> parameters, String encouragementType) {
        List<DialogueTemplate> templates = encouragementTemplates.get(encouragementType);
        if (templates == null || templates.isEmpty()) {
            return "You're doing well. Keep it up.";
        }
        
        return generateFromTemplates(templates, parameters);
    }
    
    /**
     * Generate dialogue from a list of templates
     */
    private String generateFromTemplates(List<DialogueTemplate> templates, Map<String, String> parameters) {
        // Sort templates by confidence
        templates.sort((t1, t2) -> Integer.compare(t2.confidence, t1.confidence));
        
        // Find the best template that has all required parameters
        for (DialogueTemplate template : templates) {
            boolean hasAllParams = true;
            
            for (String param : template.requiredParams) {
                if (!parameters.containsKey(param)) {
                    hasAllParams = false;
                    break;
                }
            }
            
            if (hasAllParams) {
                return applyPersonalityToTemplate(fillTemplate(template, parameters));
            }
        }
        
        // If no template with all params, use the highest confidence one and handle missing params
        if (!templates.isEmpty()) {
            DialogueTemplate bestTemplate = templates.get(0);
            return applyPersonalityToTemplate(fillTemplateWithDefaults(bestTemplate, parameters));
        }
        
        return "I have information to share with you.";
    }
    
    /**
     * Fill template with provided parameters
     */
    private String fillTemplate(DialogueTemplate template, Map<String, String> parameters) {
        String result = template.template;
        
        for (String param : template.requiredParams) {
            String value = parameters.get(param);
            result = result.replace("{" + param + "}", value);
        }
        
        return result;
    }
    
    /**
     * Fill template with provided parameters, using defaults for missing ones
     */
    private String fillTemplateWithDefaults(DialogueTemplate template, Map<String, String> parameters) {
        String result = template.template;
        
        for (String param : template.requiredParams) {
            String value = parameters.containsKey(param) ? 
                parameters.get(param) : getDefaultForParameter(param);
            result = result.replace("{" + param + "}", value);
        }
        
        return result;
    }
    
    /**
     * Get default value for a parameter
     */
    private String getDefaultForParameter(String param) {
        switch (param) {
            case "targetType":
                return "enemy";
            case "direction":
                return "location";
            case "targetWeakness":
                return "Aim carefully";
            case "recommendedWeapon":
                return "your weapon";
            case "attackStrategy":
                return "direct attack";
            case "targetBodyPart":
                return "target";
            case "defenseAction":
                return "take cover";
            case "vulnerableArea":
                return "position";
            case "locationDescription":
                return "the strategic position";
            case "tacticalAdvantage":
                return "better visibility";
            case "resourceType":
                return "resources";
            case "percentRemaining":
                return "low";
            case "collectionStrategy":
                return "Move carefully";
            case "optimizationStrategy":
                return "Use sparingly";
            case "timeFrame":
                return "coming situation";
            case "actionName":
                return "this action";
            case "actionInstructions":
                return "follow the on-screen instructions";
            case "nextActionInstructions":
                return "proceed to the next step";
            case "actionTip":
                return "time your actions carefully";
            case "abilityName":
                return "new ability";
            case "abilityInstructions":
                return "follow the tutorial prompts";
            case "requiredAction":
                return "specific steps";
            case "puzzleElement":
                return "this pattern";
            case "puzzleSolution":
                return "a specific approach";
            case "puzzleRequirement":
                return "careful observation";
            case "puzzleClue":
                return "important details";
            case "puzzleElement1":
                return "the first element";
            case "puzzleElement2":
                return "the second element";
            case "puzzleType":
                return "current";
            case "puzzleSolutionApproach":
                return "systematic testing";
            case "dangerType":
                return "threat";
            case "dangerLevel":
                return "high";
            case "dangerDescription":
                return "proceed with caution";
            case "escapeStrategy":
                return "immediate evasion";
            case "opportunityType":
                return "advantage";
            case "actionType":
                return "strategic action";
            case "benefitDescription":
                return "this will improve your position";
            case "opportunityDescription":
                return "a beneficial situation";
            case "accomplishmentDescription":
                return "your performance";
            case "performanceLevel":
                return "excellent";
            case "skillType":
                return "technique";
            case "statisticDescription":
                return "your performance";
            case "failureDescription":
                return "this setback";
            case "improvementStrategy":
                return "a different approach";
            case "errorDescription":
                return "the challenge";
            case "lessonDescription":
                return "this provides valuable experience";
            case "encouragementPhrase":
                return "You're making progress";
            default:
                return "this";
        }
    }
    
    /**
     * Apply personality traits to generated dialogue
     */
    private String applyPersonalityToTemplate(String text) {
        if (personalityProfile.formalityLevel > 70) {
            // More formal language
            text = text.replaceAll("(?i)get ", "obtain ");
            text = text.replaceAll("(?i)use ", "utilize ");
            text = text.replaceAll("(?i)go ", "proceed ");
        } else if (personalityProfile.formalityLevel < 30) {
            // More casual language
            text = text.replaceAll("(?i)obtain ", "get ");
            text = text.replaceAll("(?i)utilize ", "use ");
            text = text.replaceAll("(?i)proceed ", "go ");
        }
        
        if (personalityProfile.supportiveness > 70) {
            // More supportive language
            if (!text.contains("you can") && !text.contains("You're") && 
                !text.contains("well done") && !text.contains("excellent")) {
                text += " You can do this.";
            }
        }
        
        if (personalityProfile.detailOrientation > 70) {
            // More detailed language
            if (!text.contains("specifically") && !text.contains("precisely")) {
                text = text.replaceAll("\\.", ". To be specific, ");
            }
        } else if (personalityProfile.detailOrientation < 30) {
            // More concise language
            text = text.replaceAll("(?i)in order to ", "to ");
            text = text.replaceAll("(?i)due to the fact that ", "because ");
        }
        
        return text;
    }
    
    /**
     * Update current game situation context
     */
    public void updateGameContext(GameSituationContext context) {
        if (context != null) {
            this.currentContext = context;
        }
    }
    
    /**
     * Update personality profile
     */
    public void updatePersonalityProfile(PersonalityProfile profile) {
        if (profile != null) {
            this.personalityProfile = profile;
        }
    }
    
    /**
     * Dialogue template structure
     */
    private static class DialogueTemplate {
        public final String template;
        public final String[] requiredParams;
        public final int confidence;
        
        DialogueTemplate(String template, String[] requiredParams, int confidence) {
            this.template = template;
            this.requiredParams = requiredParams;
            this.confidence = confidence;
        }
    }
    
    /**
     * Game situation context
     */
    public static class GameSituationContext {
        public String situationType = "NEUTRAL"; // COMBAT, EXPLORATION, PUZZLE, etc.
        public int dangerLevel = 0; // 0-100
        public String environmentType = "NEUTRAL"; // INDOOR, OUTDOOR, URBAN, etc.
        public int playerHealth = 100; // 0-100
        public Map<String, Object> contextualParameters = new HashMap<>();
    }
    
    /**
     * Personality profile for AI speech generation
     */
    public static class PersonalityProfile {
        public int formalityLevel = 50; // 0-100, formal vs casual
        public int supportiveness = 70; // 0-100, supportive vs neutral
        public int detailOrientation = 50; // 0-100, detailed vs concise
        public int assertiveness = 50; // 0-100, assertive vs tentative
        
        // Future expansion: humor, empathy, etc.
    }
}
