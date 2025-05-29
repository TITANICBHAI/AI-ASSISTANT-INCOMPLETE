
package com.aiassistant.core.gaming.vision;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.aiassistant.core.gaming.GameState;
import com.aiassistant.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * System for triggering frame capture based on game events
 * Detects significant events and triggers high-frequency capture
 */
public class EventBasedCapture {
    private static final String TAG = "EventBasedCapture";
    
    // Event types
    public static final String EVENT_GUNFIRE = "gunfire";
    public static final String EVENT_DAMAGE_TAKEN = "damage_taken";
    public static final String EVENT_DAMAGE_DEALT = "damage_dealt";
    public static final String EVENT_EXPLOSION = "explosion";
    public static final String EVENT_PLAYER_SPOTTED = "player_spotted";
    public static final String EVENT_HEALTH_CHANGE = "health_change";
    
    // Capture settings
    private static final int BURST_CAPTURE_COUNT = 10; // Number of frames to capture in burst mode
    private static final long BURST_CAPTURE_INTERVAL = 50; // Interval between burst captures (ms)
    
    // Current state
    private boolean burstModeActive = false;
    private int remainingBurstCaptures = 0;
    private long lastCaptureTime = 0;
    private Map<String, Long> eventCooldowns = new HashMap<>();
    
    // Dependencies
    private final Context context;
    private final Executor captureExecutor;
    
    // Listeners
    private final List<CaptureEventListener> listeners = new ArrayList<>();
    
    /**
     * Interface for capture event listeners
     */
    public interface CaptureEventListener {
        void onEventDetected(String eventType, Bitmap capturedFrame, long timestamp);
        void onBurstCaptureStarted(String eventType);
        void onBurstCaptureCompleted(String eventType, List<Bitmap> frames);
    }
    
    /**
     * Constructor
     * @param context Application context
     */
    public EventBasedCapture(Context context) {
        this.context = context.getApplicationContext();
        this.captureExecutor = Executors.newSingleThreadExecutor();
        
        // Initialize cooldowns
        eventCooldowns.put(EVENT_GUNFIRE, 0L);
        eventCooldowns.put(EVENT_DAMAGE_TAKEN, 0L);
        eventCooldowns.put(EVENT_DAMAGE_DEALT, 0L);
        eventCooldowns.put(EVENT_EXPLOSION, 0L);
        eventCooldowns.put(EVENT_PLAYER_SPOTTED, 0L);
        eventCooldowns.put(EVENT_HEALTH_CHANGE, 0L);
    }
    
    /**
     * Add capture event listener
     * @param listener Listener to add
     */
    public void addListener(CaptureEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove capture event listener
     * @param listener Listener to remove
     */
    public void removeListener(CaptureEventListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Process a new frame and game state for event detection
     * @param bitmap Current frame bitmap
     * @param gameState Current game state
     * @param timestamp Frame timestamp
     */
    public void processFrame(Bitmap bitmap, GameState gameState, long timestamp) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        
        // Create a bitmap copy for processing
        final Bitmap bitmapCopy = bitmap.copy(bitmap.getConfig(), false);
        final GameState gameStateCopy = gameState.copy();
        final long captureTime = timestamp;
        
        captureExecutor.execute(() -> {
            try {
                // Check if we're in burst mode
                if (burstModeActive) {
                    processBurstCapture(bitmapCopy, gameStateCopy, captureTime);
                    return;
                }
                
                // Detect events
                String detectedEvent = detectEvent(bitmapCopy, gameStateCopy, captureTime);
                
                if (detectedEvent != null) {
                    // Check cooldown for this event type
                    long cooldownTime = eventCooldowns.getOrDefault(detectedEvent, 0L);
                    
                    if (captureTime - cooldownTime > getEventCooldownDuration(detectedEvent)) {
                        // Update cooldown
                        eventCooldowns.put(detectedEvent, captureTime);
                        
                        // Notify listeners
                        for (CaptureEventListener listener : listeners) {
                            listener.onEventDetected(detectedEvent, bitmapCopy, captureTime);
                        }
                        
                        // Start burst capture
                        startBurstCapture(detectedEvent);
                        
                        // Don't recycle the bitmap as it's passed to listeners
                        return;
                    }
                }
                
                // Recycle bitmap if not used
                if (!bitmapCopy.isRecycled()) {
                    bitmapCopy.recycle();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing frame: " + e.getMessage());
                
                // Ensure bitmap is recycled on error
                if (bitmapCopy != null && !bitmapCopy.isRecycled()) {
                    bitmapCopy.recycle();
                }
            }
        });
    }
    
    /**
     * Process a frame during burst capture mode
     * @param bitmap Frame bitmap
     * @param gameState Current game state
     * @param timestamp Frame timestamp
     */
    private void processBurstCapture(Bitmap bitmap, GameState gameState, long timestamp) {
        // Check if enough time has passed since last capture
        if (timestamp - lastCaptureTime < BURST_CAPTURE_INTERVAL) {
            // Recycle bitmap if not used
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return;
        }
        
        // Update last capture time
        lastCaptureTime = timestamp;
        
        // Decrement remaining captures
        remainingBurstCaptures--;
        
        // Notify listeners of captured frame
        for (CaptureEventListener listener : listeners) {
            listener.onEventDetected("burst_frame", bitmap, timestamp);
        }
        
        // Check if burst is complete
        if (remainingBurstCaptures <= 0) {
            burstModeActive = false;
            
            // Notify listeners
            for (CaptureEventListener listener : listeners) {
                listener.onBurstCaptureCompleted("burst_complete", new ArrayList<>());
            }
        }
        
        // Don't recycle bitmap as it's passed to listeners
    }
    
    /**
     * Start burst capture mode
     * @param eventType Type of event that triggered burst
     */
    private void startBurstCapture(String eventType) {
        burstModeActive = true;
        remainingBurstCaptures = BURST_CAPTURE_COUNT;
        lastCaptureTime = System.currentTimeMillis();
        
        // Notify listeners
        for (CaptureEventListener listener : listeners) {
            listener.onBurstCaptureStarted(eventType);
        }
        
        Log.d(TAG, "Started burst capture for event: " + eventType);
    }
    
    /**
     * Detect events in the current frame
     * @param bitmap Frame bitmap
     * @param gameState Current game state
     * @param timestamp Frame timestamp
     * @return Detected event type or null if no event detected
     */
    private String detectEvent(Bitmap bitmap, GameState gameState, long timestamp) {
        // Check for gunfire (muzzle flash, etc.)
        if (detectGunfire(bitmap, gameState)) {
            return EVENT_GUNFIRE;
        }
        
        // Check for damage indicators
        if (detectDamageTaken(bitmap, gameState)) {
            return EVENT_DAMAGE_TAKEN;
        }
        
        // Check for damage dealt
        if (detectDamageDealt(bitmap, gameState)) {
            return EVENT_DAMAGE_DEALT;
        }
        
        // Check for explosions
        if (detectExplosion(bitmap, gameState)) {
            return EVENT_EXPLOSION;
        }
        
        // Check for player spotted indicators
        if (detectPlayerSpotted(bitmap, gameState)) {
            return EVENT_PLAYER_SPOTTED;
        }
        
        // Check for health changes
        if (detectHealthChange(bitmap, gameState)) {
            return EVENT_HEALTH_CHANGE;
        }
        
        return null;
    }
    
    /**
     * Detect gunfire in frame
     * @param bitmap Frame bitmap
     * @param gameState Current game state
     * @return True if gunfire detected
     */
    private boolean detectGunfire(Bitmap bitmap, GameState gameState) {
        // Simplified implementation - look for bright spots near center (muzzle flash)
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 3;
        
        int brightPixels = 0;
        int totalPixels = 0;
        
        for (int x = centerX - radius; x < centerX + radius; x += 4) {
            for (int y = centerY - radius; y < centerY + radius; y += 4) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    int pixel = bitmap.getPixel(x, y);
                    
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = pixel & 0xFF;
                    
                    // Look for bright pixels (potential muzzle flash)
                    if (r > 200 && g > 200 && b > 180) {
                        brightPixels++;
                    }
                    
                    totalPixels++;
                }
            }
        }
        
        // If more than 5% of pixels in center region are very bright, likely gunfire
        return (double)brightPixels / totalPixels > 0.05;
    }
    
    /**
     * Detect damage taken in frame
     * @param bitmap Frame bitmap
     * @param gameState Current game state
     * @return True if damage taken detected
     */
    private boolean detectDamageTaken(Bitmap bitmap, GameState gameState) {
        // Look for red flashes at screen edges (common damage indicator)
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        int redPixels = 0;
        int totalPixels = 0;
        
        // Check screen edges
        for (int x = 0; x < width; x += 8) {
            for (int y = 0; y < height; y += 8) {
                // Only check pixels near the edges
                if (x < width * 0.1 || x > width * 0.9 || y < height * 0.1 || y > height * 0.9) {
                    int pixel = bitmap.getPixel(x, y);
                    
                    int r = (pixel >> 16) & 0xFF;
                    int g = (pixel >> 8) & 0xFF;
                    int b = pixel & 0xFF;
                    
                    // Look for predominantly red pixels
                    if (r > 150 && r > g * 2 && r > b * 2) {
                        redPixels++;
                    }
                    
                    totalPixels++;
                }
            }
        }
        
        // If more than 15% of edge pixels are red, likely damage indicator
        return (double)redPixels / totalPixels > 0.15;
    }
    
    /**
     * Detect damage dealt in frame
     * @param bitmap Frame bitmap
     * @param gameState Current game state
     * @return True if damage dealt detected
     */
    private boolean detectDamageDealt(Bitmap bitmap, GameState gameState) {
        // Look for hit markers or damage numbers
        // This would need to be customized based on the specific game
        // Simple implementation - look for specific colors in center region
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = Math.min(width, height) / 5;
        
        for (int x = centerX - radius; x < centerX + radius; x += 3) {
            for (int y = centerY - radius; y < centerY + radius; y += 3) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    int pixel = bitmap.getPixel(x, y);
                    
                    // Many games use white or red hit markers
                    if (isHitMarkerColor(pixel)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if a pixel color matches typical hit marker colors
     * @param pixel Pixel color
     * @return True if matches hit marker colors
     */
    private boolean isHitMarkerColor(int pixel) {
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = pixel & 0xFF;
        
        // White hit marker (common in FPS games)
        boolean isWhiteHitMarker = r > 240 && g > 240 && b > 240;
        
        // Red hit marker (common in damage numbers)
        boolean isRedHitMarker = r > 200 && g < 100 && b < 100;
        
        return isWhiteHitMarker || isRedHitMarker;
    }
    
    /**
     * Detect explosions in frame
     * @param bitmap Frame bitmap
     * @param gameState Current game state
     * @return True if explosion detected
     */
    private boolean detectExplosion(Bitmap bitmap, GameState gameState) {
        // Look for bright orange/yellow colors and high brightness variation
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        int explosionPixels = 0;
        int totalPixels = 0;
        
        for (int x = 0; x < width; x += 10) {
            for (int y = 0; y < height; y += 10) {
                int pixel = bitmap.getPixel(x, y);
                
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                
                // Detect orange/yellow (explosion colors)
                if (r > 200 && g > 100 && g < 200 && b < 100) {
                    explosionPixels++;
                }
                
                totalPixels++;
            }
        }
        
        // If more than 8% of sampled pixels are explosion colors, likely an explosion
        return (double)explosionPixels / totalPixels > 0.08;
    }
    
    /**
     * Detect player spotted indicators
     * @param bitmap Frame bitmap
     * @param gameState Current game state
     * @return True if player spotted indicator detected
     */
    private boolean detectPlayerSpotted(Bitmap bitmap, GameState gameState) {
        // Look for common player indicator colors (often red markers or outlines)
        // Different games have different indicators, this would need customization
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        int indicatorPixels = 0;
        int totalPixels = 0;
        
        for (int x = 0; x < width; x += 8) {
            for (int y = 0; y < height; y += 8) {
                int pixel = bitmap.getPixel(x, y);
                
                if (isPlayerIndicatorColor(pixel)) {
                    indicatorPixels++;
                }
                
                totalPixels++;
            }
        }
        
        // If more than 1% of pixels match indicator colors, likely player spotted
        return (double)indicatorPixels / totalPixels > 0.01;
    }
    
    /**
     * Check if a pixel color matches typical player indicator colors
     * @param pixel Pixel color
     * @return True if matches player indicator colors
     */
    private boolean isPlayerIndicatorColor(int pixel) {
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = pixel & 0xFF;
        
        // Red outline (common in many games)
        boolean isRedOutline = r > 220 && g < 50 && b < 50;
        
        // Orange/amber outline (common in some games)
        boolean isOrangeOutline = r > 220 && g > 120 && g < 180 && b < 60;
        
        return isRedOutline || isOrangeOutline;
    }
    
    /**
     * Detect health changes in frame
     * @param bitmap Frame bitmap
     * @param gameState Current game state
     * @return True if health change detected
     */
    private boolean detectHealthChange(Bitmap bitmap, GameState gameState) {
        // Look for changes in health bar region
        // This is highly game-specific and would need customization
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        // Health bars are commonly at bottom or top of screen
        int[] healthBarRegions = {
            // Bottom left (common for health)
            0, (int)(height * 0.8), (int)(width * 0.3), height,
            // Top right (common for squad health)
            (int)(width * 0.7), 0, width, (int)(height * 0.2)
        };
        
        for (int i = 0; i < healthBarRegions.length; i += 4) {
            int startX = healthBarRegions[i];
            int startY = healthBarRegions[i + 1];
            int endX = healthBarRegions[i + 2];
            int endY = healthBarRegions[i + 3];
            
            int healthBarPixels = 0;
            int totalPixels = 0;
            
            for (int x = startX; x < endX; x += 4) {
                for (int y = startY; y < endY; y += 4) {
                    int pixel = bitmap.getPixel(x, y);
                    
                    if (isHealthBarColor(pixel)) {
                        healthBarPixels++;
                    }
                    
                    totalPixels++;
                }
            }
            
            // If more than 10% of pixels in health bar region match health colors
            if (totalPixels > 0 && (double)healthBarPixels / totalPixels > 0.1) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if a pixel color matches typical health bar colors
     * @param pixel Pixel color
     * @return True if matches health bar colors
     */
    private boolean isHealthBarColor(int pixel) {
        int r = (pixel >> 16) & 0xFF;
        int g = (pixel >> 8) & 0xFF;
        int b = pixel & 0xFF;
        
        // Green (full health)
        boolean isGreenHealth = g > 180 && r < 120 && b < 120;
        
        // Red (low health)
        boolean isRedHealth = r > 180 && g < 120 && b < 120;
        
        // Yellow/Orange (medium health)
        boolean isYellowHealth = r > 180 && g > 180 && b < 80;
        
        return isGreenHealth || isRedHealth || isYellowHealth;
    }
    
    /**
     * Get cooldown duration for event type
     * @param eventType Event type
     * @return Cooldown duration in milliseconds
     */
    private long getEventCooldownDuration(String eventType) {
        switch (eventType) {
            case EVENT_GUNFIRE:
                return 500; // 0.5 seconds
            case EVENT_DAMAGE_TAKEN:
                return 1000; // 1 second
            case EVENT_DAMAGE_DEALT:
                return 300; // 0.3 seconds
            case EVENT_EXPLOSION:
                return 2000; // 2 seconds
            case EVENT_PLAYER_SPOTTED:
                return 1500; // 1.5 seconds
            case EVENT_HEALTH_CHANGE:
                return 1000; // 1 second
            default:
                return 500; // Default 0.5 seconds
        }
    }
}
