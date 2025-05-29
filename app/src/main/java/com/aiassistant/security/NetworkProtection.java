package com.aiassistant.security;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

/**
 * Network protection component that prevents detection through network analysis.
 * This class monitors network connections, obfuscates network traffic patterns,
 * and prevents network-based detection.
 */
public class NetworkProtection {
    private static final String TAG = "NetworkProtection";
    private static final boolean DEBUG = false;
    
    // Monitoring constants
    private static final int MONITORING_INTERVAL_MS = 30000; // 30 seconds
    private static final int CONNECTION_TIMEOUT_MS = 5000;   // 5 seconds
    private static final int MAX_MONITOR_CONNECTIONS = 5;
    
    // Network configuration
    private static final String[] COMMON_USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (iPad; CPU OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (Linux; Android 11; SM-G998B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
    };
    
    // Native method declarations
    private native void nativeInitNetworkProtection();
    private native void nativeObfuscateTraffic(String url, byte[] data);
    private native boolean nativeDetectNetworkMonitoring();
    private native void nativeSecureNetworkConnection(String host);

    private final Context mContext;
    private final SecureRandom mRandom;
    private final ConcurrentHashMap<String, Long> mConnections;
    private final ScheduledExecutorService mExecutor;
    private boolean mIsActive;
    private int mSecurityLevel;

    /**
     * Constructor initializes the network protection
     * @param context Application context
     */
    public NetworkProtection(Context context) {
        mContext = context;
        mRandom = new SecureRandom();
        mConnections = new ConcurrentHashMap<>();
        mExecutor = Executors.newScheduledThreadPool(1);
        mIsActive = false;
        mSecurityLevel = 0;
        
        // Initialize native components
        try {
            nativeInitNetworkProtection();
        } catch (UnsatisfiedLinkError e) {
            if (DEBUG) {
                Log.e(TAG, "Failed to initialize native network protection", e);
            }
        }
    }

    /**
     * Starts network protection
     * @param securityLevel Current security level (0-3)
     */
    public void start(int securityLevel) {
        if (mIsActive) {
            return;
        }
        
        mIsActive = true;
        mSecurityLevel = securityLevel;
        
        try {
            // Start network monitoring based on security level
            int interval = MONITORING_INTERVAL_MS / (securityLevel + 1);
            
            mExecutor.scheduleAtFixedRate(() -> {
                try {
                    monitorNetworkActivity();
                } catch (Exception e) {
                    if (DEBUG) {
                        Log.e(TAG, "Error monitoring network", e);
                    }
                }
            }, interval, interval, TimeUnit.MILLISECONDS);
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error starting network protection", e);
            }
        }
    }

    /**
     * Stops network protection
     */
    public void stop() {
        if (!mIsActive) {
            return;
        }
        
        mIsActive = false;
        
        try {
            // Shutdown the executor
            mExecutor.shutdown();
            mExecutor.awaitTermination(500, TimeUnit.MILLISECONDS);
            
            // Clear connection tracking
            mConnections.clear();
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error stopping network protection", e);
            }
        }
    }

    /**
     * Secures a connection to the given URL by applying obfuscation
     * @param url URL to secure
     * @param data Data to be sent
     */
    public void secureConnection(String url, byte[] data) {
        if (url == null || url.isEmpty()) {
            return;
        }
        
        try {
            // Track this connection
            mConnections.put(url, System.currentTimeMillis());
            
            // Apply native obfuscation if available
            try {
                URL parsedUrl = new URL(url);
                nativeSecureNetworkConnection(parsedUrl.getHost());
                nativeObfuscateTraffic(url, data);
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native network security unavailable");
                }
                
                // Apply basic Java-level obfuscation
                // In a real implementation, this would do more
            }
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error securing connection", e);
            }
        }
    }

    /**
     * Gets a randomized user agent to prevent fingerprinting
     * @return A randomized user agent string
     */
    public String getRandomizedUserAgent() {
        try {
            int index = mRandom.nextInt(COMMON_USER_AGENTS.length);
            String baseAgent = COMMON_USER_AGENTS[index];
            
            // For higher security levels, further randomize the agent
            if (mSecurityLevel >= 2) {
                // Add some random version numbers or slight variations
                int randNum = mRandom.nextInt(10);
                return baseAgent.replace("Chrome/91", "Chrome/9" + randNum);
            }
            
            return baseAgent;
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error getting randomized user agent", e);
            }
            
            // Fallback to a default agent
            return COMMON_USER_AGENTS[0];
        }
    }

    /**
     * Monitors network activity for suspicious behavior
     */
    private void monitorNetworkActivity() {
        try {
            // First try native detection
            boolean suspiciousActivity = false;
            try {
                suspiciousActivity = nativeDetectNetworkMonitoring();
            } catch (UnsatisfiedLinkError e) {
                if (DEBUG) {
                    Log.d(TAG, "Native network monitoring detection unavailable");
                }
            }
            
            // If native detection doesn't work, do Java-based checks
            if (!suspiciousActivity) {
                // Check for unusual network interfaces
                List<NetworkInterface> interfaces = getNetworkInterfaces();
                for (NetworkInterface iface : interfaces) {
                    String name = iface.getName().toLowerCase();
                    if (name.contains("tun") || name.contains("tap") || 
                        name.contains("ppp") || name.contains("vpn")) {
                        suspiciousActivity = true;
                        break;
                    }
                }
                
                // Check current network state
                ConnectivityManager cm = (ConnectivityManager) 
                    mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                
                if (cm != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Network network = cm.getActiveNetwork();
                        if (network != null) {
                            NetworkCapabilities capabilities = 
                                cm.getNetworkCapabilities(network);
                            
                            if (capabilities != null) {
                                boolean hasVpn = capabilities.hasTransport(
                                    NetworkCapabilities.TRANSPORT_VPN);
                                
                                if (hasVpn) {
                                    suspiciousActivity = true;
                                }
                            }
                        }
                    } else {
                        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                        if (activeNetwork != null && 
                            activeNetwork.getType() == ConnectivityManager.TYPE_VPN) {
                            suspiciousActivity = true;
                        }
                    }
                }
            }
            
            // Handle suspicious activity if detected
            if (suspiciousActivity) {
                handleSuspiciousNetworkActivity();
            }
            
            // Clean up old connections
            cleanupOldConnections();
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error in network monitoring", e);
            }
        }
    }

    /**
     * Handles detected suspicious network activity
     */
    private void handleSuspiciousNetworkActivity() {
        // In a real implementation, this would take appropriate actions
        // such as notifying other security components or changing behavior
        
        // Increase security level if not already at max
        if (mSecurityLevel < 3) {
            mSecurityLevel++;
        }
        
        if (DEBUG) {
            Log.w(TAG, "Suspicious network activity detected! " +
                       "Increasing security level to " + mSecurityLevel);
        }
    }

    /**
     * Gets all network interfaces
     * @return List of network interfaces
     */
    private List<NetworkInterface> getNetworkInterfaces() {
        List<NetworkInterface> interfaces = new ArrayList<>();
        
        try {
            Enumeration<NetworkInterface> networkInterfaces = 
                NetworkInterface.getNetworkInterfaces();
            
            if (networkInterfaces != null) {
                interfaces = Collections.list(networkInterfaces);
            }
        } catch (SocketException e) {
            if (DEBUG) {
                Log.e(TAG, "Error getting network interfaces", e);
            }
        }
        
        return interfaces;
    }

    /**
     * Cleans up tracking for old connections
     */
    private void cleanupOldConnections() {
        long now = System.currentTimeMillis();
        long threshold = now - (CONNECTION_TIMEOUT_MS * 2);
        
        // Remove old connections
        List<String> keysToRemove = new ArrayList<>();
        for (Map.Entry<String, Long> entry : mConnections.entrySet()) {
            if (entry.getValue() < threshold) {
                keysToRemove.add(entry.getKey());
            }
        }
        
        // Remove the identified keys
        for (String key : keysToRemove) {
            mConnections.remove(key);
        }
        
        // Keep connection count under limit
        if (mConnections.size() > MAX_MONITOR_CONNECTIONS) {
            List<Map.Entry<String, Long>> entries = 
                new ArrayList<>(mConnections.entrySet());
            
            // Sort by time, oldest first
            Collections.sort(entries, 
                (a, b) -> a.getValue().compareTo(b.getValue()));
            
            // Remove oldest until within limit
            for (int i = 0; i < entries.size() - MAX_MONITOR_CONNECTIONS; i++) {
                mConnections.remove(entries.get(i).getKey());
            }
        }
    }
    
    /**
     * Gets a map of headers to use for HTTP requests to obfuscate traffic
     * @return Map of HTTP headers
     */
    public Map<String, String> getObfuscatedHeaders() {
        Map<String, String> headers = new HashMap<>();
        
        // Add randomized user agent
        headers.put("User-Agent", getRandomizedUserAgent());
        
        // Add other standard headers
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Language", "en-US,en;q=0.5");
        
        // For higher security levels, add more random headers
        if (mSecurityLevel >= 2) {
            headers.put("Cache-Control", "max-age=" + (60 * mRandom.nextInt(60)));
            headers.put("Upgrade-Insecure-Requests", "1");
        }
        
        return headers;
    }
    
    /**
     * Configures a HttpsURLConnection with security enhancements
     * @param connection The connection to configure
     */
    public void configureSecureConnection(HttpsURLConnection connection) {
        if (connection == null) {
            return;
        }
        
        try {
            // Set headers
            Map<String, String> headers = getObfuscatedHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            
            // Set timeouts
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
            connection.setReadTimeout(CONNECTION_TIMEOUT_MS);
            
            // Additional security settings
            connection.setInstanceFollowRedirects(false);
            
            // Higher security levels might add further protection
            if (mSecurityLevel >= 2) {
                connection.setRequestProperty("DNT", "1");
            }
            
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "Error configuring secure connection", e);
            }
        }
    }
}
