package com.aiassistant.core.orchestration.components;

import android.content.Context;
import android.util.Log;
import com.aiassistant.core.orchestration.ComponentInterface;
import com.aiassistant.core.orchestration.ComponentStateSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiagnosticAnalyzer implements ComponentInterface {
    private static final String TAG = "DiagnosticAnalyzer";
    private final Context context;
    private boolean isRunning = false;
    private boolean isHealthy = true;
    private long lastHeartbeat = 0;
    private int stateVersion = 0;
    private List<Map<String, Object>> diagnostics = new ArrayList<>();
    
    public DiagnosticAnalyzer(Context context) {
        this.context = context.getApplicationContext();
    }
    
    @Override
    public String getComponentId() {
        return "DiagnosticAnalyzer";
    }
    
    @Override
    public String getComponentName() {
        return "Diagnostic Data Analyzer";
    }
    
    @Override
    public List<String> getCapabilities() {
        List<String> capabilities = new ArrayList<>();
        capabilities.add("diagnostic_analysis");
        capabilities.add("root_cause_analysis");
        capabilities.add("performance_analysis");
        return capabilities;
    }
    
    @Override
    public void initialize() {
        Log.d(TAG, "Initializing DiagnosticAnalyzer");
        diagnostics.clear();
        isHealthy = true;
    }
    
    @Override
    public void start() {
        Log.d(TAG, "Starting DiagnosticAnalyzer");
        isRunning = true;
        lastHeartbeat = System.currentTimeMillis();
    }
    
    @Override
    public void stop() {
        Log.d(TAG, "Stopping DiagnosticAnalyzer");
        isRunning = false;
    }
    
    @Override
    public ComponentStateSnapshot captureState() {
        Map<String, Object> state = new HashMap<>();
        state.put("isRunning", isRunning);
        state.put("isHealthy", isHealthy);
        state.put("lastHeartbeat", lastHeartbeat);
        state.put("diagnosticCount", diagnostics.size());
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
            Log.d(TAG, "Executing diagnostic analysis");
            
            Map<String, Object> diagnosis = analyzeDiagnostics(input);
            diagnostics.add(diagnosis);
            
            result.put("success", true);
            result.put("diagnosis", diagnosis);
            result.put("total_diagnostics", diagnostics.size());
            
            isHealthy = true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error executing diagnostic analysis", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            isHealthy = false;
        }
        return result;
    }
    
    private Map<String, Object> analyzeDiagnostics(Map<String, Object> input) {
        Map<String, Object> diagnosis = new HashMap<>();
        diagnosis.put("timestamp", System.currentTimeMillis());
        
        if (input == null || input.isEmpty()) {
            diagnosis.put("status", "no_data");
            diagnosis.put("recommendation", "No diagnostic data provided");
            return diagnosis;
        }
        
        if (input.containsKey("errors")) {
            Object errorsObj = input.get("errors");
            if (errorsObj instanceof List) {
                List<?> errors = (List<?>) errorsObj;
                diagnosis.put("error_count", errors.size());
                
                if (errors.size() > 0) {
                    diagnosis.put("severity", "critical");
                    diagnosis.put("root_cause", analyzeRootCause(errors));
                    diagnosis.put("recommendation", "Immediate attention required");
                } else {
                    diagnosis.put("severity", "low");
                    diagnosis.put("root_cause", "none");
                    diagnosis.put("recommendation", "System operating normally");
                }
            }
        } else {
            diagnosis.put("status", "healthy");
            diagnosis.put("severity", "low");
            diagnosis.put("recommendation", "No issues detected");
        }
        
        return diagnosis;
    }
    
    private String analyzeRootCause(List<?> errors) {
        if (errors.isEmpty()) {
            return "none";
        }
        
        Map<String, Integer> errorTypes = new HashMap<>();
        for (Object error : errors) {
            if (error instanceof Map) {
                Map<?, ?> errorMap = (Map<?, ?>) error;
                String type = String.valueOf(errorMap.get("type"));
                errorTypes.put(type, errorTypes.getOrDefault(type, 0) + 1);
            }
        }
        
        String mostCommonType = "unknown";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : errorTypes.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommonType = entry.getKey();
            }
        }
        
        return mostCommonType;
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
        return "DiagnosticAnalyzer[running=" + isRunning + ", healthy=" + isHealthy + 
               ", diagnostics=" + diagnostics.size() + "]";
    }
}
