package com.aiassistant.core.ai;

import android.app.Application;
import android.util.Log;

import com.aiassistant.database.AppDatabase;

/**
 * Main Application class for the AI Assistant.
 * Initializes all core components and manages application lifecycle.
 */
public class AIAssistantApplication extends Application {
    private static final String TAG = "AIAssistantApplication";
    
    // Singleton instance
    private static AIAssistantApplication instance;
    
    // Core AI components
    private AIStateManager aiStateManager;
    
    // Database
    private AppDatabase appDatabase;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "Initializing AI Assistant Application");
        
        // Initialize database
        appDatabase = AppDatabase.getInstance(this);
        
        // Initialize AI components
        initializeAIComponents();
        
        Log.d(TAG, "AI Assistant Application initialized successfully");
    }
    
    /**
     * Initialize all AI components and managers
     */
    private void initializeAIComponents() {
        // Initialize the AI State Manager
        aiStateManager = new AIStateManager(this);
        
        Log.d(TAG, "AI components initialized");
    }
    
    /**
     * Get the singleton instance of the application
     * @return The application instance
     */
    public static AIAssistantApplication getInstance() {
        return instance;
    }
    
    /**
     * Get the AI State Manager
     * @return The AI State Manager
     */
    public AIStateManager getAIStateManager() {
        return aiStateManager;
    }
    
    /**
     * Get the application database
     * @return The database instance
     */
    public AppDatabase getDatabase() {
        return appDatabase;
    }
}
