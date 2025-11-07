package com.aiassistant.examples;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.orchestration.CentralAIOrchestrator;
import com.aiassistant.core.orchestration.ComponentInterface;
import com.aiassistant.core.orchestration.ComponentRegistry;
import com.aiassistant.core.orchestration.ComponentStateSnapshot;
import com.aiassistant.core.orchestration.EventRouter;
import com.aiassistant.core.orchestration.OrchestrationEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExampleCoordinatedComponent implements ComponentInterface {
    private static final String TAG = "ExampleComponent";
    
    private final Context context;
    private final String componentId;
    private final String componentName;
    private final CentralAIOrchestrator orchestrator;
    
    private boolean isInitialized = false;
    private boolean isRunning = false;
    private int stateVersion = 0;
    private Map<String, Object> currentState;
    
    public ExampleCoordinatedComponent(Context context, String componentId, String componentName) {
        this.context = context;
        this.componentId = componentId;
        this.componentName = componentName;
        this.orchestrator = CentralAIOrchestrator.getInstance();
        this.currentState = new HashMap<>();
    }
    
    @Override
    public String getComponentId() {
        return componentId;
    }
    
    @Override
    public String getComponentName() {
        return componentName;
    }
    
    @Override
    public List<String> getCapabilities() {
        return Arrays.asList("data_processing", "state_management", "event_handling");
    }
    
    @Override
    public void initialize() {
        if (isInitialized) {
            Log.w(TAG, componentId + " already initialized");
            return;
        }
        
        Log.i(TAG, "Initializing " + componentId);
        
        if (orchestrator != null && orchestrator.isInitialized()) {
            ComponentRegistry registry = orchestrator.getComponentRegistry();
            registry.registerComponent(componentId, componentName, getCapabilities());
            
            subscribeToEvents();
        }
        
        currentState.put("initialized", true);
        currentState.put("last_heartbeat", System.currentTimeMillis());
        
        isInitialized = true;
        Log.i(TAG, componentId + " initialized successfully");
    }
    
    @Override
    public void start() {
        if (!isInitialized) {
            initialize();
        }
        
        if (isRunning) {
            Log.w(TAG, componentId + " already running");
            return;
        }
        
        Log.i(TAG, "Starting " + componentId);
        
        if (orchestrator != null && orchestrator.isInitialized()) {
            ComponentRegistry registry = orchestrator.getComponentRegistry();
            registry.updateComponentStatus(componentId, ComponentRegistry.ComponentStatus.ACTIVE);
        }
        
        currentState.put("running", true);
        currentState.put("start_time", System.currentTimeMillis());
        stateVersion++;
        
        isRunning = true;
        Log.i(TAG, componentId + " started successfully");
    }
    
    @Override
    public void stop() {
        if (!isRunning) {
            Log.w(TAG, componentId + " not running");
            return;
        }
        
        Log.i(TAG, "Stopping " + componentId);
        
        if (orchestrator != null && orchestrator.isInitialized()) {
            ComponentRegistry registry = orchestrator.getComponentRegistry();
            registry.updateComponentStatus(componentId, ComponentRegistry.ComponentStatus.INACTIVE);
        }
        
        currentState.put("running", false);
        currentState.put("stop_time", System.currentTimeMillis());
        stateVersion++;
        
        isRunning = false;
        Log.i(TAG, componentId + " stopped");
    }
    
    @Override
    public ComponentStateSnapshot captureState() {
        Map<String, Object> stateCopy = new HashMap<>(currentState);
        ComponentStateSnapshot snapshot = new ComponentStateSnapshot(
            componentId,
            stateVersion,
            stateCopy
        );
        
        Log.d(TAG, componentId + " state captured: v" + stateVersion);
        return snapshot;
    }
    
    @Override
    public void restoreState(ComponentStateSnapshot snapshot) {
        Log.i(TAG, "Restoring " + componentId + " to state v" + snapshot.getVersion());
        
        currentState = new HashMap<>(snapshot.getState());
        stateVersion = snapshot.getVersion();
        
        Log.i(TAG, componentId + " state restored successfully");
    }
    
    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        long startTime = System.currentTimeMillis();
        
        if (!isRunning) {
            Log.w(TAG, componentId + " cannot execute - not running");
            return createErrorResult("Component not running");
        }
        
        Log.d(TAG, componentId + " executing with input: " + input);
        
        try {
            heartbeat();
            
            Map<String, Object> result = processInput(input);
            
            long latency = System.currentTimeMillis() - startTime;
            
            if (orchestrator != null && orchestrator.isInitialized()) {
                orchestrator.getFeedbackSystem().recordComponentExecution(
                    componentId,
                    latency,
                    0.9f,
                    true
                );
                
                orchestrator.getHealthMonitor().recordSuccess(componentId);
            }
            
            publishEvent("execution.completed", result);
            
            return result;
            
        } catch (Exception e) {
            Log.e(TAG, "Error in " + componentId + " execution", e);
            
            if (orchestrator != null && orchestrator.isInitialized()) {
                orchestrator.getHealthMonitor().recordError(componentId, "execution_exception");
            }
            
            publishEvent("execution.error", createErrorResult(e.getMessage()));
            
            return createErrorResult(e.getMessage());
        }
    }
    
    private Map<String, Object> processInput(Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("component", componentId);
        result.put("timestamp", System.currentTimeMillis());
        result.put("processed_input", input);
        
        return result;
    }
    
    private Map<String, Object> createErrorResult(String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("component", componentId);
        result.put("error", error);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    @Override
    public boolean isHealthy() {
        if (!isInitialized || !isRunning) {
            return false;
        }
        
        Long lastHeartbeat = (Long) currentState.get("last_heartbeat");
        if (lastHeartbeat == null) {
            return false;
        }
        
        long heartbeatAge = System.currentTimeMillis() - lastHeartbeat;
        return heartbeatAge < 30000;
    }
    
    @Override
    public void heartbeat() {
        currentState.put("last_heartbeat", System.currentTimeMillis());
        
        if (orchestrator != null && orchestrator.isInitialized()) {
            orchestrator.getHealthMonitor().recordHeartbeat(componentId);
        }
    }
    
    @Override
    public String getStatus() {
        if (!isInitialized) {
            return "NOT_INITIALIZED";
        } else if (!isRunning) {
            return "STOPPED";
        } else {
            return "RUNNING";
        }
    }
    
    private void subscribeToEvents() {
        if (orchestrator == null || !orchestrator.isInitialized()) {
            return;
        }
        
        EventRouter eventRouter = orchestrator.getEventRouter();
        
        eventRouter.subscribe("system.shutdown", event -> {
            Log.i(TAG, componentId + " received shutdown event");
            stop();
        });
        
        eventRouter.subscribe("component.restart." + componentId, event -> {
            Log.i(TAG, componentId + " received restart event");
            stop();
            start();
        });
    }
    
    private void publishEvent(String eventType, Map<String, Object> data) {
        if (orchestrator == null || !orchestrator.isInitialized()) {
            return;
        }
        
        OrchestrationEvent event = new OrchestrationEvent(
            componentId + "." + eventType,
            componentId,
            data
        );
        
        orchestrator.getEventRouter().publish(event);
    }
}
