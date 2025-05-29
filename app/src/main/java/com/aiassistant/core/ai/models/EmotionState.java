package com.aiassistant.core.ai.models;

/**
 * Represents the different emotional states that the assistant can be in.
 * Used for emotion detection, processing, and response generation.
 */
public enum EmotionState {
    // Basic emotions
    NEUTRAL,     // Balanced, default state
    HAPPY,       // Joy, pleasure, contentment
    SAD,         // Grief, disappointment, sorrow
    ANGRY,       // Irritation, rage, annoyance
    SURPRISED,   // Astonishment, shock, amazement
    
    // Complex emotional states
    CONCERNED,   // Worry, care about someone's situation
    EMPATHETIC,  // Understanding and sharing another's feelings
    CURIOUS,     // Interest, desire to learn more
    CONFUSED,    // Lack of understanding, uncertainty
    PROFESSIONAL // Formal, business-like approach
}
