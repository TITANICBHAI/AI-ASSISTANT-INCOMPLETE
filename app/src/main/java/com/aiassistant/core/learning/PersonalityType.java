package com.aiassistant.core.learning;

/**
 * Defines different personality types for the AI
 */
public enum PersonalityType {
    /**
     * Professional personality - formal, efficient, focused on precision
     */
    PROFESSIONAL,
    
    /**
     * Friendly personality - warm, approachable, relationship-focused
     */
    FRIENDLY,
    
    /**
     * Technical personality - detail-oriented, analytical, technical language
     */
    TECHNICAL,
    
    /**
     * Educational personality - teaching-focused, explanatory, patient
     */
    EDUCATIONAL,
    
    /**
     * Concise personality - brief, to-the-point, minimal extra information
     */
    CONCISE,
    
    /**
     * Casual personality - relaxed, informal, conversational
     */
    CASUAL;
    
    /**
     * Get personality name
     * @return Human-readable name
     */
    public String getDisplayName() {
        switch (this) {
            case PROFESSIONAL: return "Professional";
            case FRIENDLY: return "Friendly";
            case TECHNICAL: return "Technical";
            case EDUCATIONAL: return "Educational";
            case CONCISE: return "Concise";
            case CASUAL: return "Casual";
            default: return "Unknown";
        }
    }
    
    /**
     * Get personality description
     * @return Description of this personality type
     */
    public String getDescription() {
        switch (this) {
            case PROFESSIONAL:
                return "Formal, efficient, focuses on precision and results";
            case FRIENDLY:
                return "Warm, approachable, relationship-focused, supportive";
            case TECHNICAL:
                return "Detail-oriented, analytical, uses technical language";
            case EDUCATIONAL:
                return "Teaching-focused, explanatory, patient, thorough";
            case CONCISE:
                return "Brief, to-the-point, provides minimal extra information";
            case CASUAL:
                return "Relaxed, informal, conversational, approachable";
            default:
                return "Unknown personality type";
        }
    }
    
    /**
     * Get personality-specific training focus
     * @return Description of training focus for this personality
     */
    public String getTrainingFocus() {
        switch (this) {
            case PROFESSIONAL:
                return "Precision, efficiency, formal language patterns";
            case FRIENDLY:
                return "Empathetic responses, supportive language, relationship building";
            case TECHNICAL:
                return "Technical accuracy, detailed explanations, problem-solving";
            case EDUCATIONAL:
                return "Teaching capabilities, clear explanations, patience";
            case CONCISE:
                return "Brevity, core information extraction, efficiency";
            case CASUAL:
                return "Conversational flow, approachability, informal language";
            default:
                return "General training";
        }
    }
}
