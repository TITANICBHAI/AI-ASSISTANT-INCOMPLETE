package com.aiassistant.core.learning;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aiassistant.core.security.AccessControl;
import com.aiassistant.learning.StructuredKnowledgeSystem;
import com.aiassistant.learning.SystemAccessLearningManager;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Central system for managing the AI's learning capabilities
 */
public class PersistentLearningSystem {
    private static final String TAG = "PersistentLearning";
    
    // How often to run background learning tasks (in milliseconds)
    private static final long LEARNING_INTERVAL_MS = 15 * 60 * 1000; // 15 minutes
    
    // How often to persist learning data (in milliseconds)
    private static final long PERSISTENCE_INTERVAL_MS = 30 * 60 * 1000; // 30 minutes
    
    private Context context;
    private AccessControl accessControl;
    private StructuredKnowledgeSystem knowledgeSystem;
    private InternalReasoningSystem reasoningSystem;
    private SelfDirectedLearningSystem selfLearningSystem;
    private SystemAccessLearningManager accessLearningManager;
    
    private ScheduledExecutorService scheduledExecutor;
    private Handler mainHandler;
    private long lastPersistenceTime;
    private boolean initialized;
    
    /**
     * Constructor
     * @param context Application context
     * @param accessControl Access control for security checks
     * @param knowledgeSystem Knowledge management system
     * @param reasoningSystem Reasoning system
     * @param selfLearningSystem Self-directed learning system
     * @param accessLearningManager System access learning manager
     */
    public PersistentLearningSystem(
            Context context,
            AccessControl accessControl,
            StructuredKnowledgeSystem knowledgeSystem,
            InternalReasoningSystem reasoningSystem,
            SelfDirectedLearningSystem selfLearningSystem,
            SystemAccessLearningManager accessLearningManager) {
        
        this.context = context;
        this.accessControl = accessControl;
        this.knowledgeSystem = knowledgeSystem;
        this.reasoningSystem = reasoningSystem;
        this.selfLearningSystem = selfLearningSystem;
        this.accessLearningManager = accessLearningManager;
        
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.lastPersistenceTime = 0;
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
        
        Log.d(TAG, "Initializing persistent learning system");
        
        try {
            // Verify access permission
            if (!verifyAccess(AccessControl.SecurityZone.LEARNING, AccessControl.PermissionLevel.ADMIN)) {
                Log.e(TAG, "Access denied during initialization");
                return false;
            }
            
            // Initialize all subsystems
            boolean knowledgeInitialized = knowledgeSystem.initialize();
            boolean reasoningInitialized = reasoningSystem.initialize();
            boolean selfLearningInitialized = selfLearningSystem.initialize();
            boolean accessLearningInitialized = accessLearningManager.initialize();
            
            if (!knowledgeInitialized || !reasoningInitialized || 
                !selfLearningInitialized || !accessLearningInitialized) {
                Log.e(TAG, "Failed to initialize one or more learning subsystems");
                return false;
            }
            
            // Load persisted data
            loadPersistedData();
            
            // Start the scheduled learning tasks
            startScheduledTasks();
            
            initialized = true;
            Log.d(TAG, "Persistent learning system initialized successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing persistent learning system", e);
            return false;
        }
    }
    
    /**
     * Start scheduled learning tasks
     */
    private void startScheduledTasks() {
        // Create a new scheduled executor
        scheduledExecutor = Executors.newScheduledThreadPool(1);
        
        // Schedule regular learning tasks
        scheduledExecutor.scheduleAtFixedRate(
                this::runScheduledLearningTasks,
                LEARNING_INTERVAL_MS, 
                LEARNING_INTERVAL_MS,
                TimeUnit.MILLISECONDS);
        
        Log.d(TAG, "Scheduled learning tasks started");
    }
    
    /**
     * Run scheduled learning tasks
     */
    private void runScheduledLearningTasks() {
        try {
            Log.d(TAG, "Running scheduled learning tasks");
            
            // Verify access permission
            if (!verifyAccess(AccessControl.SecurityZone.LEARNING, AccessControl.PermissionLevel.EXECUTE)) {
                Log.e(TAG, "Access denied for scheduled learning tasks");
                return;
            }
            
            // Process pending learning objectives
            selfLearningSystem.processPendingObjectives();
            
            // Update knowledge connections
            knowledgeSystem.updateConnections();
            
            // Refine reasoning patterns
            reasoningSystem.refineReasoningPatterns();
            
            // Optimize system access patterns
            accessLearningManager.optimizeAccessPatterns();
            
            // Check if it's time to persist data
            long now = System.currentTimeMillis();
            if (now - lastPersistenceTime > PERSISTENCE_INTERVAL_MS) {
                persistData();
                lastPersistenceTime = now;
            }
            
            Log.d(TAG, "Scheduled learning tasks completed");
        } catch (Exception e) {
            Log.e(TAG, "Error running scheduled learning tasks", e);
        }
    }
    
    /**
     * Process a new observation for learning
     * @param source Source of the observation
     * @param observation The observation data
     * @param context Additional context information
     */
    public void processObservation(String source, String observation, String context) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.LEARNING, AccessControl.PermissionLevel.WRITE)) {
            Log.e(TAG, "Access denied for processing observation");
            return;
        }
        
        // Process in background to avoid blocking
        scheduledExecutor.execute(() -> {
            try {
                // Let each system process the observation
                knowledgeSystem.processObservation(source, observation, context);
                reasoningSystem.processObservation(source, observation, context);
                selfLearningSystem.assessLearningOpportunity(source, observation, context);
                accessLearningManager.recordAccessPattern(source);
            } catch (Exception e) {
                Log.e(TAG, "Error processing observation", e);
            }
        });
    }
    
    /**
     * Process a user interaction for learning
     * @param interactionType Type of interaction
     * @param userInput User input
     * @param aiResponse AI response
     * @param feedback Optional user feedback
     */
    public void processInteraction(String interactionType, String userInput, 
                                  String aiResponse, String feedback) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.LEARNING, AccessControl.PermissionLevel.WRITE)) {
            Log.e(TAG, "Access denied for processing interaction");
            return;
        }
        
        // Process in background to avoid blocking
        scheduledExecutor.execute(() -> {
            try {
                // Let each system process the interaction
                knowledgeSystem.processInteraction(interactionType, userInput, aiResponse, feedback);
                reasoningSystem.processInteraction(interactionType, userInput, aiResponse, feedback);
                selfLearningSystem.assessInteraction(interactionType, userInput, aiResponse, feedback);
            } catch (Exception e) {
                Log.e(TAG, "Error processing interaction", e);
            }
        });
    }
    
    /**
     * Load persisted learning data
     */
    private void loadPersistedData() {
        File dataDir = new File(context.getFilesDir(), "learning_data");
        if (!dataDir.exists()) {
            Log.d(TAG, "No persisted data found");
            return;
        }
        
        try {
            // Load data for each component
            knowledgeSystem.loadData(dataDir);
            reasoningSystem.loadData(dataDir);
            selfLearningSystem.loadData(dataDir);
            accessLearningManager.loadData(dataDir);
            
            Log.d(TAG, "Successfully loaded persisted learning data");
        } catch (Exception e) {
            Log.e(TAG, "Error loading persisted data", e);
        }
    }
    
    /**
     * Persist learning data
     */
    private void persistData() {
        File dataDir = new File(context.getFilesDir(), "learning_data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        try {
            // Persist data for each component
            knowledgeSystem.persistData(dataDir);
            reasoningSystem.persistData(dataDir);
            selfLearningSystem.persistData(dataDir);
            accessLearningManager.persistData(dataDir);
            
            Log.d(TAG, "Successfully persisted learning data");
        } catch (Exception e) {
            Log.e(TAG, "Error persisting data", e);
        }
    }
    
    /**
     * Shutdown the learning system
     */
    public void shutdown() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.LEARNING, AccessControl.PermissionLevel.ADMIN)) {
            Log.e(TAG, "Access denied for shutdown");
            return;
        }
        
        Log.d(TAG, "Shutting down persistent learning system");
        
        // Persist data before shutdown
        persistData();
        
        // Shutdown scheduled executor
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
            }
        }
        
        // Shutdown all subsystems
        knowledgeSystem.shutdown();
        reasoningSystem.shutdown();
        selfLearningSystem.shutdown();
        accessLearningManager.shutdown();
        
        initialized = false;
        Log.d(TAG, "Persistent learning system shutdown complete");
    }
    
    /**
     * Get the knowledge system
     * @return Knowledge system
     */
    public StructuredKnowledgeSystem getKnowledgeSystem() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.LEARNING, AccessControl.PermissionLevel.READ_ONLY)) {
            Log.e(TAG, "Access denied for knowledge system");
            return null;
        }
        return knowledgeSystem;
    }
    
    /**
     * Get the reasoning system
     * @return Reasoning system
     */
    public InternalReasoningSystem getReasoningSystem() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.LEARNING, AccessControl.PermissionLevel.READ_ONLY)) {
            Log.e(TAG, "Access denied for reasoning system");
            return null;
        }
        return reasoningSystem;
    }
    
    /**
     * Get the self-directed learning system
     * @return Self-directed learning system
     */
    public SelfDirectedLearningSystem getSelfLearningSystem() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.LEARNING, AccessControl.PermissionLevel.READ_ONLY)) {
            Log.e(TAG, "Access denied for self learning system");
            return null;
        }
        return selfLearningSystem;
    }
    
    /**
     * Get the system access learning manager
     * @return System access learning manager
     */
    public SystemAccessLearningManager getAccessLearningManager() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.LEARNING, AccessControl.PermissionLevel.READ_ONLY)) {
            Log.e(TAG, "Access denied for access learning manager");
            return null;
        }
        return accessLearningManager;
    }
    
    /**
     * Verify access to a security zone
     * @param zone Security zone
     * @param level Required permission level
     * @return True if access is allowed
     */
    private boolean verifyAccess(AccessControl.SecurityZone zone, AccessControl.PermissionLevel level) {
        boolean hasAccess = accessControl.checkPermission(zone, level);
        if (!hasAccess) {
            Log.w(TAG, "Access denied to zone " + zone + " with level " + level);
        }
        return hasAccess;
    }
}
