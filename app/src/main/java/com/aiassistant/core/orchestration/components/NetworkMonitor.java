package com.aiassistant.core.orchestration.components;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.aiassistant.core.orchestration.ComponentInterface;
import com.aiassistant.core.orchestration.ComponentStateSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkMonitor implements ComponentInterface {
    private static final String TAG = "NetworkMonitor";
    private final Context context;
    private boolean isRunning = false;
    private boolean isHealthy = true;
    private long lastHeartbeat = 0;
    private int stateVersion = 0;
    private String lastNetworkType = "unknown";
    private boolean isConnected = false;
    
    public NetworkMonitor(Context context) {
        this.context = context.getApplicationContext();
    }
    
    @Override
    public String getComponentId() {
        return "NetworkMonitor";
    }
    
    @Override
    public String getComponentName() {
        return "Network Status Monitor";
    }
    
    @Override
    public List<String> getCapabilities() {
        List<String> capabilities = new ArrayList<>();
        capabilities.add("network_monitoring");
        capabilities.add("connectivity_detection");
        capabilities.add("bandwidth_estimation");
        return capabilities;
    }
    
    @Override
    public void initialize() {
        Log.d(TAG, "Initializing NetworkMonitor");
        checkNetworkStatus();
        isHealthy = true;
    }
    
    @Override
    public void start() {
        Log.d(TAG, "Starting NetworkMonitor");
        isRunning = true;
        lastHeartbeat = System.currentTimeMillis();
    }
    
    @Override
    public void stop() {
        Log.d(TAG, "Stopping NetworkMonitor");
        isRunning = false;
    }
    
    @Override
    public ComponentStateSnapshot captureState() {
        Map<String, Object> state = new HashMap<>();
        state.put("isRunning", isRunning);
        state.put("isHealthy", isHealthy);
        state.put("lastHeartbeat", lastHeartbeat);
        state.put("lastNetworkType", lastNetworkType);
        state.put("isConnected", isConnected);
        return new ComponentStateSnapshot(getComponentId(), stateVersion++, state);
    }
    
    @Override
    public void restoreState(ComponentStateSnapshot snapshot) {
        if (snapshot != null && getComponentId().equals(snapshot.getComponentId())) {
            Map<String, Object> state = snapshot.getState();
            isRunning = (Boolean) state.getOrDefault("isRunning", false);
            isHealthy = (Boolean) state.getOrDefault("isHealthy", true);
            lastHeartbeat = (Long) state.getOrDefault("lastHeartbeat", 0L);
            lastNetworkType = (String) state.getOrDefault("lastNetworkType", "unknown");
            isConnected = (Boolean) state.getOrDefault("isConnected", false);
            Log.d(TAG, "State restored from snapshot version: " + snapshot.getVersion());
        }
    }
    
    @Override
    public Map<String, Object> execute(Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        try {
            Log.d(TAG, "Executing network monitoring");
            
            checkNetworkStatus();
            
            result.put("success", true);
            result.put("network_type", lastNetworkType);
            result.put("is_connected", isConnected);
            result.put("network_available", isConnected);
            
            isHealthy = true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error executing network monitoring", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            isHealthy = false;
        }
        return result;
    }
    
    private void checkNetworkStatus() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                
                if (isConnected) {
                    lastNetworkType = getNetworkTypeName(activeNetwork.getType());
                } else {
                    lastNetworkType = "none";
                }
            } else {
                isConnected = false;
                lastNetworkType = "unknown";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking network status", e);
            isConnected = false;
            lastNetworkType = "error";
        }
    }
    
    private String getNetworkTypeName(int type) {
        switch (type) {
            case ConnectivityManager.TYPE_WIFI:
                return "wifi";
            case ConnectivityManager.TYPE_MOBILE:
                return "mobile";
            case ConnectivityManager.TYPE_ETHERNET:
                return "ethernet";
            default:
                return "other";
        }
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
        return "NetworkMonitor[running=" + isRunning + ", healthy=" + isHealthy + 
               ", connected=" + isConnected + ", type=" + lastNetworkType + "]";
    }
}
