package com.aiassistant.core.ai.memory;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background service for memory management
 * Handles periodic memory operations like consolidation and persistence
 */
public class MemoryService extends Service {
    private static final String TAG = "MemoryService";
    
    // Binder for client access
    private final IBinder binder = new MemoryBinder();
    
    // Memory manager reference
    private MemoryManager memoryManager;
    
    // Scheduler for periodic tasks
    private ScheduledExecutorService scheduler;
    
    // Main thread handler
    private Handler mainHandler;
    
    // Configuration
    private static final long PERSIST_INTERVAL = 15 * 60; // 15 minutes in seconds
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize components
        this.memoryManager = MemoryManager.getInstance(this);
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Start periodic tasks
        startPeriodicTasks();
        
        Log.d(TAG, "MemoryService created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "MemoryService started");
        
        // Make service sticky so it restarts if killed
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public void onDestroy() {
        // Cleanup
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        
        // Persist memory before shutdown
        memoryManager.persistAllMemories();
        
        Log.d(TAG, "MemoryService destroyed");
        super.onDestroy();
    }
    
    /**
     * Start periodic tasks
     */
    private void startPeriodicTasks() {
        // Schedule memory persistence
        scheduler.scheduleAtFixedRate(() -> {
            Log.d(TAG, "Running scheduled memory persistence");
            memoryManager.persistAllMemories();
        }, PERSIST_INTERVAL, PERSIST_INTERVAL, TimeUnit.SECONDS);
        
        // Schedule memory maintenance
        scheduler.scheduleAtFixedRate(this::performMemoryMaintenance, 5, 30, TimeUnit.MINUTES);
        
        Log.d(TAG, "Periodic memory tasks scheduled");
    }
    
    /**
     * Perform memory maintenance
     * Transfers important memories from short-term to long-term
     */
    private void performMemoryMaintenance() {
        Log.d(TAG, "Performing memory maintenance");
        
        // Run on background thread
        scheduler.execute(() -> {
            // Transfer important memories to long-term
            memoryManager.transferAllToLongTerm();
            
            // Clear out old short-term memories
            memoryManager.clearShortTermMemories();
            
            Log.d(TAG, "Memory maintenance completed");
        });
    }
    
    /**
     * Force persistence of all memories
     */
    public void forcePersistence() {
        scheduler.execute(() -> {
            Log.d(TAG, "Forcing memory persistence");
            memoryManager.persistAllMemories();
        });
    }
    
    /**
     * Binder class for client access
     */
    public class MemoryBinder extends Binder {
        /**
         * Get service instance
         * @return MemoryService instance
         */
        public MemoryService getService() {
            return MemoryService.this;
        }
    }
}
