package com.aiassistant.ai.features.overlay;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.aiassistant.ai.features.BaseFeature;
import com.aiassistant.security.SecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tactical Overlay System
 * - Provides visual overlays with strategic information
 * - Highlights tactical elements in the game
 * - Shows optimal paths, danger zones, and objectives
 * - Adapts display based on game context
 */
public class TacticalOverlayFeature extends BaseFeature {
    private static final String TAG = "TacticalOverlay";
    private static final String FEATURE_NAME = "tactical_overlay_system";
    
    // Overlay element types
    public enum OverlayElementType {
        OBJECTIVE,       // Mission objectives
        DANGER_ZONE,     // Dangerous areas
        OPTIMAL_PATH,    // Optimal movement paths
        RESOURCE,        // Important resources
        ENEMY,           // Enemy positions
        ALLY,            // Ally positions
        STRATEGIC_POINT, // Strategic locations
        CUSTOM           // Custom overlay element
    }
    
    // Display mode for the overlay
    public enum DisplayMode {
        MINIMAL,         // Only critical information
        STANDARD,        // Balanced display
        DETAILED,        // All available information
        COMBAT_FOCUSED,  // Focus on combat elements
        EXPLORATION      // Focus on exploration elements
    }
    
    // Current overlay elements
    private final List<OverlayElement> overlayElements;
    
    // Current display mode
    private DisplayMode displayMode;
    
    // Transparency level (0.0-1.0)
    private float transparencyLevel;
    
    // Color schemes for different game contexts
    private final Map<String, ColorScheme> colorSchemes;
    
    // Currently active color scheme
    private ColorScheme activeColorScheme;
    
    // Overlay visibility flag (can be toggled by user)
    private boolean overlayVisible;
    
    // Overlay update listeners
    private final List<OverlayUpdateListener> updateListeners;
    
    /**
     * Constructor
     * @param context Application context
     */
    public TacticalOverlayFeature(Context context) {
        super(context, FEATURE_NAME);
        this.overlayElements = new CopyOnWriteArrayList<>();
        this.displayMode = DisplayMode.STANDARD;
        this.transparencyLevel = 0.7f;
        this.colorSchemes = new HashMap<>();
        this.overlayVisible = true;
        this.updateListeners = new ArrayList<>();
        
        // Initialize default color schemes
        initializeColorSchemes();
    }
    
    @Override
    public boolean initialize() {
        boolean success = super.initialize();
        if (success) {
            try {
                // Set default color scheme
                this.activeColorScheme = colorSchemes.get("default");
                
                Log.d(TAG, "Tactical overlay system initialized with " + 
                      colorSchemes.size() + " color schemes");
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize tactical overlay", e);
                return false;
            }
        }
        return false;
    }
    
    @Override
    public void update() {
        if (!isEnabled() || !overlayVisible) return;
        
        // Apply security context for this operation
        SecurityContext.getInstance().setCurrentFeatureActive(FEATURE_NAME);
        
        try {
            // Update overlay elements
            updateOverlayElements();
            
            // Notify listeners about updates
            notifyUpdateListeners();
            
            Log.v(TAG, "Tactical overlay updated with " + overlayElements.size() + " elements");
        } catch (Exception e) {
            Log.e(TAG, "Error updating tactical overlay", e);
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    @Override
    public void shutdown() {
        // Clear all overlay elements
        overlayElements.clear();
        
        // Clear update listeners
        updateListeners.clear();
        
        super.shutdown();
    }
    
    /**
     * Add a new overlay element
     * @param element Overlay element to add
     */
    public void addOverlayElement(OverlayElement element) {
        if (element != null) {
            // Remove any existing element with the same ID
            removeOverlayElement(element.getId());
            
            // Add the new element
            overlayElements.add(element);
            Log.d(TAG, "Added overlay element: " + element.getId());
        }
    }
    
    /**
     * Remove an overlay element by ID
     * @param elementId Element ID to remove
     * @return true if element was removed, false otherwise
     */
    public boolean removeOverlayElement(String elementId) {
        boolean removed = false;
        
        for (int i = 0; i < overlayElements.size(); i++) {
            if (overlayElements.get(i).getId().equals(elementId)) {
                overlayElements.remove(i);
                removed = true;
                Log.d(TAG, "Removed overlay element: " + elementId);
                break;
            }
        }
        
        return removed;
    }
    
    /**
     * Clear all overlay elements
     */
    public void clearOverlayElements() {
        overlayElements.clear();
        Log.d(TAG, "Cleared all overlay elements");
    }
    
    /**
     * Get all overlay elements
     * @return List of all overlay elements
     */
    public List<OverlayElement> getOverlayElements() {
        return new ArrayList<>(overlayElements);
    }
    
    /**
     * Get overlay elements by type
     * @param type Element type
     * @return List of overlay elements of the specified type
     */
    public List<OverlayElement> getOverlayElementsByType(OverlayElementType type) {
        List<OverlayElement> elements = new ArrayList<>();
        
        for (OverlayElement element : overlayElements) {
            if (element.getType() == type) {
                elements.add(element);
            }
        }
        
        return elements;
    }
    
    /**
     * Set display mode
     * @param mode Display mode
     */
    public void setDisplayMode(DisplayMode mode) {
        this.displayMode = mode;
        Log.d(TAG, "Display mode set to: " + mode);
    }
    
    /**
     * Get current display mode
     * @return Current display mode
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }
    
    /**
     * Set transparency level
     * @param level Transparency level (0.0-1.0)
     */
    public void setTransparencyLevel(float level) {
        this.transparencyLevel = Math.max(0.0f, Math.min(1.0f, level));
        Log.d(TAG, "Transparency level set to: " + transparencyLevel);
    }
    
    /**
     * Get current transparency level
     * @return Current transparency level
     */
    public float getTransparencyLevel() {
        return transparencyLevel;
    }
    
    /**
     * Set active color scheme
     * @param schemeName Color scheme name
     * @return true if scheme was found and set, false otherwise
     */
    public boolean setColorScheme(String schemeName) {
        ColorScheme scheme = colorSchemes.get(schemeName);
        if (scheme != null) {
            this.activeColorScheme = scheme;
            Log.d(TAG, "Color scheme set to: " + schemeName);
            return true;
        }
        return false;
    }
    
    /**
     * Get active color scheme
     * @return Active color scheme
     */
    public ColorScheme getActiveColorScheme() {
        return activeColorScheme;
    }
    
    /**
     * Add a new color scheme
     * @param name Scheme name
     * @param scheme Color scheme
     */
    public void addColorScheme(String name, ColorScheme scheme) {
        colorSchemes.put(name, scheme);
        Log.d(TAG, "Added color scheme: " + name);
    }
    
    /**
     * Set overlay visibility
     * @param visible true to show overlay, false to hide
     */
    public void setOverlayVisible(boolean visible) {
        this.overlayVisible = visible;
        Log.d(TAG, "Overlay visibility set to: " + visible);
    }
    
    /**
     * Check if overlay is visible
     * @return true if visible, false otherwise
     */
    public boolean isOverlayVisible() {
        return overlayVisible;
    }
    
    /**
     * Add overlay update listener
     * @param listener Listener to add
     */
    public void addUpdateListener(OverlayUpdateListener listener) {
        if (listener != null && !updateListeners.contains(listener)) {
            updateListeners.add(listener);
        }
    }
    
    /**
     * Remove overlay update listener
     * @param listener Listener to remove
     */
    public void removeUpdateListener(OverlayUpdateListener listener) {
        updateListeners.remove(listener);
    }
    
    /**
     * Initialize default color schemes
     */
    private void initializeColorSchemes() {
        // Default color scheme
        ColorScheme defaultScheme = new ColorScheme(
            0xFF00FF00, // Objective color (green)
            0xFFFF0000, // Danger zone color (red)
            0xFF0000FF, // Optimal path color (blue)
            0xFFFFFF00, // Resource color (yellow)
            0xFFFF6600, // Enemy color (orange)
            0xFF00FFFF, // Ally color (cyan)
            0xFFFF00FF  // Strategic point color (magenta)
        );
        colorSchemes.put("default", defaultScheme);
        
        // Combat color scheme
        ColorScheme combatScheme = new ColorScheme(
            0xFFFFFF00, // Objective color (yellow)
            0xFFFF0000, // Danger zone color (red)
            0xFF00FF00, // Optimal path color (green)
            0xFF0000FF, // Resource color (blue)
            0xFFFF0000, // Enemy color (red)
            0xFF00FF00, // Ally color (green)
            0xFFFFFF00  // Strategic point color (yellow)
        );
        colorSchemes.put("combat", combatScheme);
        
        // Stealth color scheme
        ColorScheme stealthScheme = new ColorScheme(
            0x8000FF00, // Objective color (translucent green)
            0x80FF0000, // Danger zone color (translucent red)
            0x800000FF, // Optimal path color (translucent blue)
            0x80FFFF00, // Resource color (translucent yellow)
            0x80FF0000, // Enemy color (translucent red)
            0x8000FFFF, // Ally color (translucent cyan)
            0x80FF00FF  // Strategic point color (translucent magenta)
        );
        colorSchemes.put("stealth", stealthScheme);
        
        // Set active color scheme to default
        activeColorScheme = defaultScheme;
    }
    
    /**
     * Update overlay elements
     * This would normally update based on game state
     */
    private void updateOverlayElements() {
        // This would be implemented to update elements based on game state
        // For now, it's just a stub
    }
    
    /**
     * Notify all update listeners
     */
    private void notifyUpdateListeners() {
        for (OverlayUpdateListener listener : updateListeners) {
            try {
                listener.onOverlayUpdated(this);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }
    
    /**
     * Overlay Element class
     * Represents a single element in the tactical overlay
     */
    public static class OverlayElement {
        private final String id;
        private final OverlayElementType type;
        private Object shape; // Can be PointF, RectF, List<PointF>, etc.
        private int color;
        private String label;
        private int priority;
        private boolean visible;
        private Map<String, Object> metadata;
        
        /**
         * Constructor for point element
         * @param id Element ID
         * @param type Element type
         * @param point Point location
         * @param color Element color
         */
        public OverlayElement(String id, OverlayElementType type, PointF point, int color) {
            this.id = id;
            this.type = type;
            this.shape = point;
            this.color = color;
            this.label = id;
            this.priority = 1;
            this.visible = true;
            this.metadata = new HashMap<>();
        }
        
        /**
         * Constructor for rectangle element
         * @param id Element ID
         * @param type Element type
         * @param rect Rectangle area
         * @param color Element color
         */
        public OverlayElement(String id, OverlayElementType type, RectF rect, int color) {
            this.id = id;
            this.type = type;
            this.shape = rect;
            this.color = color;
            this.label = id;
            this.priority = 1;
            this.visible = true;
            this.metadata = new HashMap<>();
        }
        
        /**
         * Constructor for path element
         * @param id Element ID
         * @param type Element type
         * @param path List of points forming a path
         * @param color Element color
         */
        public OverlayElement(String id, OverlayElementType type, List<PointF> path, int color) {
            this.id = id;
            this.type = type;
            this.shape = path;
            this.color = color;
            this.label = id;
            this.priority = 1;
            this.visible = true;
            this.metadata = new HashMap<>();
        }
        
        public String getId() {
            return id;
        }
        
        public OverlayElementType getType() {
            return type;
        }
        
        public Object getShape() {
            return shape;
        }
        
        public void setShape(Object shape) {
            this.shape = shape;
        }
        
        public int getColor() {
            return color;
        }
        
        public void setColor(int color) {
            this.color = color;
        }
        
        public String getLabel() {
            return label;
        }
        
        public void setLabel(String label) {
            this.label = label;
        }
        
        public int getPriority() {
            return priority;
        }
        
        public void setPriority(int priority) {
            this.priority = priority;
        }
        
        public boolean isVisible() {
            return visible;
        }
        
        public void setVisible(boolean visible) {
            this.visible = visible;
        }
        
        public Map<String, Object> getMetadata() {
            return metadata;
        }
        
        public void setMetadata(String key, Object value) {
            metadata.put(key, value);
        }
        
        public Object getMetadata(String key) {
            return metadata.get(key);
        }
    }
    
    /**
     * Color Scheme class
     * Defines colors for different overlay element types
     */
    public static class ColorScheme {
        private final int objectiveColor;
        private final int dangerZoneColor;
        private final int optimalPathColor;
        private final int resourceColor;
        private final int enemyColor;
        private final int allyColor;
        private final int strategicPointColor;
        
        /**
         * Constructor
         * @param objectiveColor Color for objectives
         * @param dangerZoneColor Color for danger zones
         * @param optimalPathColor Color for optimal paths
         * @param resourceColor Color for resources
         * @param enemyColor Color for enemies
         * @param allyColor Color for allies
         * @param strategicPointColor Color for strategic points
         */
        public ColorScheme(
            int objectiveColor,
            int dangerZoneColor,
            int optimalPathColor,
            int resourceColor,
            int enemyColor,
            int allyColor,
            int strategicPointColor
        ) {
            this.objectiveColor = objectiveColor;
            this.dangerZoneColor = dangerZoneColor;
            this.optimalPathColor = optimalPathColor;
            this.resourceColor = resourceColor;
            this.enemyColor = enemyColor;
            this.allyColor = allyColor;
            this.strategicPointColor = strategicPointColor;
        }
        
        /**
         * Get color for element type
         * @param type Element type
         * @return Color for the specified type
         */
        public int getColorForType(OverlayElementType type) {
            switch (type) {
                case OBJECTIVE:
                    return objectiveColor;
                case DANGER_ZONE:
                    return dangerZoneColor;
                case OPTIMAL_PATH:
                    return optimalPathColor;
                case RESOURCE:
                    return resourceColor;
                case ENEMY:
                    return enemyColor;
                case ALLY:
                    return allyColor;
                case STRATEGIC_POINT:
                    return strategicPointColor;
                default:
                    return 0xFFFFFFFF; // White for custom elements
            }
        }
        
        public int getObjectiveColor() {
            return objectiveColor;
        }
        
        public int getDangerZoneColor() {
            return dangerZoneColor;
        }
        
        public int getOptimalPathColor() {
            return optimalPathColor;
        }
        
        public int getResourceColor() {
            return resourceColor;
        }
        
        public int getEnemyColor() {
            return enemyColor;
        }
        
        public int getAllyColor() {
            return allyColor;
        }
        
        public int getStrategicPointColor() {
            return strategicPointColor;
        }
    }
    
    /**
     * Overlay Update Listener interface
     * For notifying UI components about overlay updates
     */
    public interface OverlayUpdateListener {
        /**
         * Called when overlay is updated
         * @param overlay The tactical overlay feature
         */
        void onOverlayUpdated(TacticalOverlayFeature overlay);
    }
}
