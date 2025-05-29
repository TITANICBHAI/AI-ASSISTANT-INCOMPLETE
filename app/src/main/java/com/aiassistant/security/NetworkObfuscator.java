package com.aiassistant.security;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Obfuscates network traffic to prevent detection
 */
public class NetworkObfuscator {
    
    private static final String TAG = "NetworkObfuscator";
    private static final boolean ENABLE_LOGGING = false;
    
    // Singleton instance
    private static NetworkObfuscator instance;
    
    // Network configuration settings
    private boolean obfuscateTraffic = true;
    private boolean obfuscateHeaders = true;
    private boolean useRandomizedTiming = false;
    private boolean simulateHumanBehavior = false;
    private int obfuscationStrength = 2; // 0-3, with 3 being most aggressive
    
    // Context for network operations
    private Context context;
    
    // Random number generator for timing and packet sizing
    private final Random random = new SecureRandom();
    
    // Statistics for simulation
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private long lastRequestTime = 0;
    
    // User agent rotation
    private final List<String> userAgents = new ArrayList<>();
    private int currentUserAgentIndex = 0;
    
    // Timing characteristics for human simulation
    private final long minRequestInterval = 300; // 300ms min time between requests
    private final long maxRequestInterval = 3000; // 3000ms max time between requests
    
    /**
     * Get the singleton instance
     * 
     * @param context The Android context
     * @return The NetworkObfuscator instance
     */
    public static synchronized NetworkObfuscator getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkObfuscator(context);
        }
        return instance;
    }
    
    /**
     * Private constructor
     * 
     * @param context The Android context
     */
    private NetworkObfuscator(Context context) {
        this.context = context;
        
        // Initialize user agents for rotation
        initializeUserAgents();
        
        if (ENABLE_LOGGING) {
            Log.d(TAG, "NetworkObfuscator initialized");
        }
    }
    
    /**
     * Initialize user agents for rotation
     */
    private void initializeUserAgents() {
        // Common mobile user agents
        userAgents.add("Mozilla/5.0 (Android 10; Mobile; rv:68.0) Gecko/68.0 Firefox/68.0");
        userAgents.add("Mozilla/5.0 (Linux; Android 10; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.117 Mobile Safari/537.36");
        userAgents.add("Mozilla/5.0 (Linux; Android 9; SM-G960F Build/PPR1.180610.011; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.157 Mobile Safari/537.36");
        userAgents.add("Mozilla/5.0 (iPhone; CPU iPhone OS 13_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.5 Mobile/15E148 Safari/604.1");
        userAgents.add("Mozilla/5.0 (Linux; Android 8.0.0; SM-G960F Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36");
        userAgents.add("Mozilla/5.0 (Linux; Android 7.0; SM-G930V Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36");
        userAgents.add("Mozilla/5.0 (Linux; Android 10; Google Pixel 4 Build/QD1A.190821.014.C2; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/78.0.3904.108 Mobile Safari/537.36");
        userAgents.add("Mozilla/5.0 (Linux; Android 9; Mi A2 Build/PKQ1.180904.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36");
    }
    
    /**
     * Get a rotated user agent
     * 
     * @return A user agent string
     */
    public String getRotatedUserAgent() {
        if (userAgents.isEmpty()) {
            return "Mozilla/5.0 (Linux; Android 10; Generic) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.127 Mobile Safari/537.36";
        }
        
        synchronized (userAgents) {
            currentUserAgentIndex = (currentUserAgentIndex + 1) % userAgents.size();
            return userAgents.get(currentUserAgentIndex);
        }
    }
    
    /**
     * Get a randomized user agent
     * 
     * @return A user agent string
     */
    public String getRandomUserAgent() {
        if (userAgents.isEmpty()) {
            return "Mozilla/5.0 (Linux; Android 10; Generic) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.127 Mobile Safari/537.36";
        }
        
        synchronized (userAgents) {
            int index = random.nextInt(userAgents.size());
            return userAgents.get(index);
        }
    }
    
    /**
     * Check if network is available
     * 
     * @return Whether network is available
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    
    /**
     * Obfuscate a URL by adding random parameters
     * 
     * @param originalUrl The original URL
     * @return The obfuscated URL
     */
    public String obfuscateUrl(String originalUrl) {
        if (!obfuscateTraffic) {
            return originalUrl;
        }
        
        // Don't obfuscate if URL already has parameters
        if (originalUrl.contains("?")) {
            return originalUrl;
        }
        
        // Add random parameters for obfuscation
        StringBuilder obfuscatedUrl = new StringBuilder(originalUrl);
        obfuscatedUrl.append('?');
        
        // Add timestamp parameter
        obfuscatedUrl.append("t=").append(System.currentTimeMillis());
        
        // Add random ID
        obfuscatedUrl.append("&id=").append(UUID.randomUUID().toString().substring(0, 8));
        
        // Add obfuscation strength-dependent parameters
        for (int i = 0; i < obfuscationStrength; i++) {
            obfuscatedUrl.append("&").append(getRandomParameter());
        }
        
        return obfuscatedUrl.toString();
    }
    
    /**
     * Generate a random parameter
     * 
     * @return A random parameter key-value pair
     */
    private String getRandomParameter() {
        String[] paramKeys = {"ref", "src", "v", "mode", "type", "fmt", "s", "q", "p", "r"};
        int keyIndex = random.nextInt(paramKeys.length);
        
        String value;
        int valueType = random.nextInt(3);
        
        switch (valueType) {
            case 0: // Random string
                value = getRandomString(4 + random.nextInt(6));
                break;
            case 1: // Random number
                value = String.valueOf(random.nextInt(1000));
                break;
            case 2: // Random hex
                value = getRandomHexString(6 + random.nextInt(8));
                break;
            default:
                value = "0";
        }
        
        return paramKeys[keyIndex] + "=" + value;
    }
    
    /**
     * Obfuscate HTTP headers
     * 
     * @param originalHeaders The original headers
     * @return The obfuscated headers
     */
    public Map<String, String> obfuscateHeaders(Map<String, String> originalHeaders) {
        if (!obfuscateHeaders || originalHeaders == null) {
            return originalHeaders;
        }
        
        Map<String, String> headers = new HashMap<>(originalHeaders);
        
        // Add or replace User-Agent
        headers.put("User-Agent", simulateHumanBehavior ? getRotatedUserAgent() : getRandomUserAgent());
        
        // Add Accept header with common MIME types
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        
        // Add Accept-Language with weighted random languages
        headers.put("Accept-Language", "en-US,en;q=0.5");
        
        // Add Accept-Encoding with gzip and deflate
        headers.put("Accept-Encoding", "gzip, deflate");
        
        // Add or replace Connection header
        headers.put("Connection", "keep-alive");
        
        // Add or replace Cache-Control header
        headers.put("Cache-Control", "max-age=0");
        
        // Add randomized client hints
        if (obfuscationStrength >= 2) {
            headers.put("Sec-CH-UA", "\"Google Chrome\";v=\"" + (85 + random.nextInt(10)) + "\", \"Chromium\";v=\"" + (85 + random.nextInt(10)) + "\", \";Not A Brand\";v=\"99\"");
            headers.put("Sec-CH-UA-Mobile", random.nextBoolean() ? "?1" : "?0");
            headers.put("Sec-CH-UA-Platform", "\"Android\"");
        }
        
        // Add DNT header (50% of the time)
        if (random.nextBoolean()) {
            headers.put("DNT", "1");
        }
        
        // Add Referer header with previous domain simulation (for higher obfuscation levels)
        if (obfuscationStrength >= 2) {
            String[] commonReferrers = {
                "https://www.google.com/",
                "https://www.bing.com/search",
                "https://search.yahoo.com/",
                "https://duckduckgo.com/",
                "https://www.facebook.com/",
                "https://t.co/redirect",
                "https://www.reddit.com/"
            };
            
            headers.put("Referer", commonReferrers[random.nextInt(commonReferrers.length)]);
        }
        
        return headers;
    }
    
    /**
     * Apply request timing obfuscation
     * 
     * @throws InterruptedException If sleep is interrupted
     */
    public void applyTimingObfuscation() throws InterruptedException {
        if (!useRandomizedTiming) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        int requestCount = requestCounter.incrementAndGet();
        
        // Calculate time to wait based on obfuscation strategies
        long timeToWait = 0;
        
        if (lastRequestTime > 0) {
            long timeSinceLastRequest = currentTime - lastRequestTime;
            
            if (simulateHumanBehavior) {
                // Human behavior simulation
                if (timeSinceLastRequest < minRequestInterval) {
                    // Too fast for human, add delay
                    timeToWait = minRequestInterval - timeSinceLastRequest;
                    
                    // For bursts of activity, occasionally allow quick succession
                    if (random.nextInt(10) < 8) { // 80% chance of delay
                        timeToWait += random.nextInt(500); // Add additional random delay
                    }
                }
            } else {
                // Random jitter strategy
                if (obfuscationStrength >= 1) {
                    int jitterMagnitude = 100 * obfuscationStrength;
                    timeToWait = random.nextInt(jitterMagnitude);
                }
            }
        }
        
        // For very high obfuscation, add periodic larger delays to simulate user pauses
        if (simulateHumanBehavior && obfuscationStrength >= 3 && requestCount % 10 == 0) {
            // Every 10 requests, add a "thinking" pause
            timeToWait += 1000 + random.nextInt(2000);
        }
        
        // Apply the wait if needed
        if (timeToWait > 0) {
            Thread.sleep(timeToWait);
        }
        
        // Update the last request time
        lastRequestTime = System.currentTimeMillis();
    }
    
    /**
     * Obfuscate request data
     * 
     * @param data The original data
     * @return The obfuscated data
     */
    public byte[] obfuscateRequestData(byte[] data) {
        if (!obfuscateTraffic || data == null || data.length == 0) {
            return data;
        }
        
        try {
            // Generate a random key for this request
            byte[] key = new byte[16];
            random.nextBytes(key);
            
            // Create initialization vector
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            
            // Create obfuscation header with metadata
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Add a magic marker (to identify our obfuscated data)
            outputStream.write("AIAS".getBytes());
            
            // Add timestamp
            long timestamp = System.currentTimeMillis();
            outputStream.write(longToBytes(timestamp));
            
            // Add initialization vector
            outputStream.write(iv);
            
            // Add key with simple XOR masking
            byte[] maskedKey = xorMask(key, generateMask(timestamp));
            outputStream.write(maskedKey);
            
            // Encrypt the data
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] encryptedData = cipher.doFinal(data);
            
            // Add encrypted data
            outputStream.write(encryptedData);
            
            // Calculate and add checksum
            byte[] checksum = calculateSHA256(encryptedData);
            outputStream.write(checksum, 0, 4); // First 4 bytes of checksum
            
            // Add some padding to disguise size patterns
            int paddingSize = random.nextInt(100 * (obfuscationStrength + 1));
            byte[] padding = new byte[paddingSize];
            random.nextBytes(padding);
            outputStream.write(padding);
            
            // Write padding size at the end (last 4 bytes)
            outputStream.write(intToBytes(paddingSize));
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            if (ENABLE_LOGGING) {
                Log.e(TAG, "Error obfuscating request data: " + e.getMessage());
            }
            return data; // Return original data on error
        }
    }
    
    /**
     * Perform an HTTP request with obfuscation
     * 
     * @param urlString The URL
     * @param method The HTTP method
     * @param headers The headers
     * @param data The request data
     * @return The response
     */
    public Pair<Integer, byte[]> performObfuscatedRequest(String urlString, String method, 
                                                        Map<String, String> headers, byte[] data) {
        HttpURLConnection connection = null;
        try {
            // Apply timing obfuscation
            applyTimingObfuscation();
            
            // Obfuscate URL if needed
            String finalUrl = obfuscateUrl(urlString);
            
            // Obfuscate headers
            Map<String, String> finalHeaders = obfuscateHeaders(headers);
            
            // Prepare connection
            URL url = new URL(finalUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            
            // Set headers
            if (finalHeaders != null) {
                for (Map.Entry<String, String> entry : finalHeaders.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            
            // Write request data if applicable
            if (data != null && data.length > 0 && 
                    ("POST".equals(method) || "PUT".equals(method))) {
                connection.setDoOutput(true);
                
                // Obfuscate the data if enabled
                byte[] finalData = obfuscateTraffic ? obfuscateRequestData(data) : data;
                
                connection.getOutputStream().write(finalData);
            }
            
            // Get response
            int responseCode = connection.getResponseCode();
            
            // Read response data
            InputStream inputStream;
            if (responseCode >= 400) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            byte[] responseData = outputStream.toByteArray();
            
            return new Pair<>(responseCode, responseData);
        } catch (Exception e) {
            if (ENABLE_LOGGING) {
                Log.e(TAG, "Error performing obfuscated request: " + e.getMessage());
            }
            return new Pair<>(-1, null);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Set obfuscation strength
     * 
     * @param strength The obfuscation strength (0-3)
     */
    public void setObfuscationStrength(int strength) {
        if (strength >= 0 && strength <= 3) {
            this.obfuscationStrength = strength;
        }
    }
    
    /**
     * Set whether to obfuscate traffic
     * 
     * @param obfuscateTraffic Whether to obfuscate traffic
     */
    public void setObfuscateTraffic(boolean obfuscateTraffic) {
        this.obfuscateTraffic = obfuscateTraffic;
    }
    
    /**
     * Set whether to obfuscate headers
     * 
     * @param obfuscateHeaders Whether to obfuscate headers
     */
    public void setObfuscateHeaders(boolean obfuscateHeaders) {
        this.obfuscateHeaders = obfuscateHeaders;
    }
    
    /**
     * Set whether to use randomized timing
     * 
     * @param useRandomizedTiming Whether to use randomized timing
     */
    public void setUseRandomizedTiming(boolean useRandomizedTiming) {
        this.useRandomizedTiming = useRandomizedTiming;
    }
    
    /**
     * Set whether to simulate human behavior
     * 
     * @param simulateHumanBehavior Whether to simulate human behavior
     */
    public void setSimulateHumanBehavior(boolean simulateHumanBehavior) {
        this.simulateHumanBehavior = simulateHumanBehavior;
    }
    
    // Helper methods
    
    /**
     * Generate a random string
     * 
     * @param length The length of the string
     * @return The random string
     */
    private String getRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    /**
     * Generate a random hex string
     * 
     * @param length The length of the string
     * @return The random hex string
     */
    private String getRandomHexString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String chars = "0123456789abcdef";
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    /**
     * Convert a long to bytes
     * 
     * @param value The long value
     * @return The byte array
     */
    private byte[] longToBytes(long value) {
        return new byte[] {
            (byte)(value),
            (byte)(value >> 8),
            (byte)(value >> 16),
            (byte)(value >> 24),
            (byte)(value >> 32),
            (byte)(value >> 40),
            (byte)(value >> 48),
            (byte)(value >> 56)
        };
    }
    
    /**
     * Convert an int to bytes
     * 
     * @param value The int value
     * @return The byte array
     */
    private byte[] intToBytes(int value) {
        return new byte[] {
            (byte)(value),
            (byte)(value >> 8),
            (byte)(value >> 16),
            (byte)(value >> 24)
        };
    }
    
    /**
     * Calculate SHA-256 hash
     * 
     * @param data The data
     * @return The hash
     */
    private byte[] calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            return new byte[32]; // Return empty hash on error
        }
    }
    
    /**
     * Generate a mask based on timestamp
     * 
     * @param timestamp The timestamp
     * @return The mask
     */
    private byte[] generateMask(long timestamp) {
        byte[] mask = new byte[16];
        long seed = timestamp ^ 0x5A3C69F721EL;
        Random maskRandom = new Random(seed);
        maskRandom.nextBytes(mask);
        return mask;
    }
    
    /**
     * XOR mask data
     * 
     * @param data The data
     * @param mask The mask
     * @return The masked data
     */
    private byte[] xorMask(byte[] data, byte[] mask) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte)(data[i] ^ mask[i % mask.length]);
        }
        return result;
    }
    
    /**
     * Compress data with GZIP
     * 
     * @param data The data
     * @return The compressed data
     */
    private byte[] compressGzip(byte[] data) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteStream);
            gzipOutputStream.write(data);
            gzipOutputStream.close();
            return byteStream.toByteArray();
        } catch (IOException e) {
            return data; // Return original data on error
        }
    }
}