package com.aiassistant.core.orchestration.components;

import android.content.Context;
import android.util.Log;
import com.aiassistant.core.orchestration.ComponentInterface;
import com.aiassistant.core.orchestration.ComponentStateSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorDetector implements ComponentInterface {
    private static final String TAG = "ErrorDetector";
    private final Context context;
    private boolean isRunning = false;
    private boolean isHealthy = true;
    private long lastHeartbeat = 0;
    private int stateVersion = 0;
    private List<Map<String, Object>> detectedErrors = new ArrayList<>();
    
    public ErrorDetector(Context context) {
        this.context = context.getApplicationContext();
    }
    
    @Override
    public String getComponentId() {
        return "ErrorDetector";
    }
    
    @Override
    public String getComponentName() {
        return "Component Error Detector";
    }
    
    @Override
    public List<String> getCapabilities() {
        List<String> capabilities = new ArrayList<>();
        capabilities.add("error_detection");
        capabilities.add("anomaly_detection");
        capabilities.add("health_monitoring");
        return capabilities;
    }
    
    @Override
    public void initialize() {
        Log.d(TAG, "Initializing ErrorDetector");
        detectedErrors.clear();
        isHealthy = true;
    }
    
    @Override
    public void start() {
        Log.d(TAG, "Starting ErrorDetector");
        isRunning = true;
        lastHeartbeat = System.currentTimeMillis();
    }
    
    @Override
    public void stop() {
        Log.d(TAG, "Stopping ErrorDetector");
        isRunning = false;
    }
    
    @Override
    public ComponentStateSnapshot captureState() {
        Map<String, Object> state = new HashMap<>();
        state.put("isRunning", isRunning);
        state.put("isHealthy", isHealthy);
        state.put("lastHeartbeat", lastHeartbeat);
        state.put("errorCount", detectedErrors.size());
        return new ComponentStateSnapshot(getComponentId(), stateVersion++, state);
    }
    
    @Override
    public void restoreState(ComponentStateSnapshot snapshot) {
        if (snapshot != null && getComponentId().equals(snapshot.getComponentId())) {
            Map<String, Object> state = snapshot.getState();
            isRunning = (Boolean) state.getOrDefault("isRunning", false);
            isHealthy = (Boolean) state.getOrDefault("isHealthy", true);
            lastHeartbeat = (Long) state.getOrDefault("lastHeartbeat", 0L);
            Log.d(TAG, "State restored from snapshot version: " + snapshot.getVersion());
        }
    }
    
    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        try {
            Log.d(TAG, "Executing error detection");
            
            List<Map<String, Object>> errors = detectErrors(input);
            detectedErrors.addAll(errors);
            
            result.put("success", true);
            result.put("errors_found", errors.size());
            result.put("errors", errors);
            result.put("total_errors", detectedErrors.size());
            
            isHealthy = true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error executing error detection", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            isHealthy = false;
        }
        return result;
    }
    
    private List<Map<String, Object>> detectErrors(Map<String, Object> input) {
        List<Map<String, Object>> errors = new ArrayList<>();
        
        if (input == null) {
            return errors;
        }
        
        if (input.containsKey("component_status")) {
            Object status = input.get("component_status");
            if (status instanceof Map) {
                Map<String, Object> statusMap = (Map<String, Object>) status;
                for (Map.Entry<String, Object> entry : statusMap.entrySet()) {
                    if (entry.getValue() instanceof Boolean && !(Boolean) entry.getValue()) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("component", entry.getKey());
                        error.put("type", "health_check_failed");
                        error.put("severity", "medium");
                        error.put("timestamp", System.currentTimeMillis());
                        errors.add(error);
                    }
                }
            }
        }
        
        if (input.containsKey("exception")) {
            Map<String, Object> error = new HashMap<>();
            error.put("type", "exception");
            error.put("message", input.get("exception"));
            error.put("severity", "high");
            error.put("timestamp", System.currentTimeMillis());
            errors.add(error);
        }
        
        return errors;
    }
    
    @Override
    public boolean isHealthy() {
        return isHealthy && (System.currentTimeMillis() - lastHeartbeat < 300000);
    }
    
    @Override
    public void heartbeat() {
        lastHeartbeat = System.currentTimeMillis();
        Log.v(TAG, "Heartbeat updated");
    }
    
    @Override
    public String getStatus() {
        return "ErrorDetector[running=" + isRunning + ", healthy=" + isHealthy + 
               ", errors_detected=" + detectedErrors.size() + "]";
    }
}
