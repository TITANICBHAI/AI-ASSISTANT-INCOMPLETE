package com.aiassistant.core.ai.execution;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.ScreenActionEntity;
import com.aiassistant.data.models.TouchPath;
import com.aiassistant.services.AccessibilityDetectionService;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manager for executing AI actions
 */
public class ActionExecutionManager {
    
    private static final String TAG = "ActionExecutionManager";
    
    private final Context context;
    private final Handler handler;
    private final Queue<AIAction> actionQueue;
    private final AtomicBoolean isExecuting;
    private long lastExecutionTime;
    private ActionExecutionListener listener;
    
    /**
     * Constructor
     * 
     * @param context The application context
     */
    public ActionExecutionManager(Context context) {
        this.context = context.getApplicationContext();
        this.handler = new Handler(Looper.getMainLooper());
        this.actionQueue = new LinkedList<>();
        this.isExecuting = new AtomicBoolean(false);
        this.lastExecutionTime = 0;
    }
    
    /**
     * Set a listener for action execution
     * 
     * @param listener The listener
     */
    public void setListener(ActionExecutionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Execute an AI action
     * 
     * @param action The action to execute
     * @return Whether the action was queued successfully
     */
    public boolean executeAction(AIAction action) {
        if (action == null) {
            return false;
        }
        
        // Add to queue
        actionQueue.offer(action);
        
        // Start processing if not already
        if (isExecuting.compareAndSet(false, true)) {
            processQueue();
        }
        
        return true;
    }
    
    /**
     * Process the action queue
     */
    private void processQueue() {
        if (actionQueue.isEmpty()) {
            isExecuting.set(false);
            return;
        }
        
        // Get next action
        AIAction action = actionQueue.poll();
        
        // Check for minimum time between actions
        long currentTime = System.currentTimeMillis();
        long timeSinceLast = currentTime - lastExecutionTime;
        
        if (timeSinceLast < 200) { // Minimum 200ms between actions
            // Wait for the remaining time
            handler.postDelayed(this::processQueue, 200 - timeSinceLast);
            return;
        }
        
        // Execute action
        if (executeActionImmediately(action)) {
            lastExecutionTime = System.currentTimeMillis();
            
            // Notify listener
            if (listener != null) {
                listener.onActionExecuted(action, true);
            }
        } else {
            // Notify listener of failure
            if (listener != null) {
                listener.onActionExecuted(action, false);
            }
        }
        
        // Continue with next action with delay
        handler.postDelayed(this::processQueue, 200);
    }
    
    /**
     * Execute an action immediately
     * 
     * @param action The action
     * @return Whether the action was executed
     */
    private boolean executeActionImmediately(AIAction action) {
        try {
            // Check if accessibility service is available
            AccessibilityDetectionService service = AccessibilityDetectionService.getInstance();
            if (service == null) {
                Log.e(TAG, "Accessibility service not available");
                return false;
            }
            
            // Convert to screen action
            ScreenActionEntity screenAction = action.toScreenAction();
            
            // Execute via accessibility service
            boolean result = service.executeAction(screenAction);
            
            Log.d(TAG, "Executed action: " + screenAction.getDescription() + ", result: " + result);
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Error executing action: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Execute a touch path
     * 
     * @param touchPath The touch path
     * @return Whether the touch path was executed
     */
    public boolean executeTouchPath(TouchPath touchPath) {
        if (touchPath == null || touchPath.getPointCount() < 2) {
            return false;
        }
        
        try {
            // Check if accessibility service is available
            AccessibilityDetectionService service = AccessibilityDetectionService.getInstance();
            if (service == null) {
                Log.e(TAG, "Accessibility service not available");
                return false;
            }
            
            return service.executeTouchPath(touchPath);
            
        } catch (Exception e) {
            Log.e(TAG, "Error executing touch path: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Clear the action queue
     */
    public void clearQueue() {
        actionQueue.clear();
    }
    
    /**
     * Check if any actions are executing or queued
     * 
     * @return Whether actions are executing or queued
     */
    public boolean isExecutingOrQueued() {
        return isExecuting.get() || !actionQueue.isEmpty();
    }
    
    /**
     * Get the number of queued actions
     * 
     * @return The queue size
     */
    public int getQueueSize() {
        return actionQueue.size();
    }
    
    /**
     * Interface for action execution listeners
     */
    public interface ActionExecutionListener {
        /**
         * Called when an action is executed
         * 
         * @param action The action
         * @param success Whether the execution was successful
         */
        void onActionExecuted(AIAction action, boolean success);
    }
}
