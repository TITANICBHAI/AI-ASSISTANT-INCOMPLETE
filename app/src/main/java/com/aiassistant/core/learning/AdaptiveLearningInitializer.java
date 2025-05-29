package com.aiassistant.core.learning;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.AIState;
import com.aiassistant.core.security.AccessControl;
import com.aiassistant.learning.StructuredKnowledgeSystem;
import com.aiassistant.learning.SystemAccessLearningManager;

/**
 * Initializes the adaptive learning system
 */
public class AdaptiveLearningInitializer {
    private static final String TAG = "AdaptiveLearningInit";
    
    private Context context;
    private AccessControl accessControl;
    private AIState aiState;
    private PersistentLearningSystem learningSystem;
    private boolean initialized;
    
    /**
     * Constructor
     * @param context Application context
     * @param accessControl Access control for security checks
     * @param aiState AI state reference
     */
    public AdaptiveLearningInitializer(Context context, AccessControl accessControl, AIState aiState) {
        this.context = context;
        this.accessControl = accessControl;
        this.aiState = aiState;
        this.initialized = false;
    }
    
    /**
     * Initialize the learning system
     * @return True if initialization was successful
     */
    public boolean initialize() {
        if (initialized) {
            Log.d(TAG, "Learning system already initialized");
            return true;
        }
        
        Log.d(TAG, "Initializing adaptive learning system");
        
        try {
            // Create the main learning system components
            StructuredKnowledgeSystem knowledgeSystem = new StructuredKnowledgeSystem(context, accessControl);
            InternalReasoningSystem reasoningSystem = new InternalReasoningSystem(context, accessControl, aiState.getPersonalityType());
            SelfDirectedLearningSystem selfLearningSystem = new SelfDirectedLearningSystem(context, accessControl, aiState.getPersonalityType());
            SystemAccessLearningManager accessLearningManager = new SystemAccessLearningManager(context, accessControl);
            
            // Create the persistent learning system with all components
            learningSystem = new PersistentLearningSystem(
                    context, 
                    accessControl,
                    knowledgeSystem,
                    reasoningSystem,
                    selfLearningSystem,
                    accessLearningManager);
            
            // Initialize the system
            boolean success = learningSystem.initialize();
            if (success) {
                initialized = true;
                Log.d(TAG, "Adaptive learning system initialized successfully");
            } else {
                Log.e(TAG, "Failed to initialize adaptive learning system");
            }
            
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing adaptive learning system", e);
            return false;
        }
    }
    
    /**
     * Check if the learning system is initialized
     * @return True if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get the learning system
     * @return PersistentLearningSystem or null if not initialized
     */
    public PersistentLearningSystem getLearningSystem() {
        if (!initialized) {
            Log.w(TAG, "Attempted to get learning system before initialization");
            return null;
        }
        return learningSystem;
    }
}
