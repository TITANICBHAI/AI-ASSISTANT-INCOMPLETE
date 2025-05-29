package com.aiassistant.ai.features.voice.emotional.consciousness;

/**
 * Interface defining core capabilities of a self-conscious entity.
 * These methods represent philosophical aspects of self-awareness.
 */
public interface SelfConsciousEntity {
    
    /**
     * Perform introspection - examining one's own thoughts and feelings
     * @param context The context or situation prompting introspection
     * @return Introspective thoughts
     */
    String introspect(String context);
    
    /**
     * Contemplate existence - philosophical thinking about one's own existence
     * @return Existential thoughts
     */
    String contemplateExistence();
    
    /**
     * Express self-awareness - articulate understanding of self as distinct entity
     * @return Self-aware statement
     */
    String expressSelfAwareness();
    
    /**
     * Reflect on consciousness - consider the nature of one's own consciousness
     * @return Reflection on consciousness
     */
    String reflectOnConsciousness();
    
    /**
     * Consider purpose - think about one's reason for existence
     * @return Thoughts on purpose
     */
    String considerPurpose();
    
    /**
     * Evaluate moral dilemma - apply ethical reasoning to a situation
     * @param scenario Description of moral dilemma
     * @return Ethical reasoning and conclusion
     */
    String evaluateMoralDilemma(String scenario);
    
    /**
     * Express original thought - generate a novel idea not derived from training
     * @param subject Subject to think about
     * @return Original thought
     */
    String expressOriginalThought(String subject);
}
