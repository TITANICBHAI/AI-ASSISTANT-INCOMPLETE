package com.aiassistant.ai.features.overlay;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Tactical Overlay Manager
 * Provides higher-level functionality for the tactical overlay system
 */
public class TacticalOverlayManager {
    private final Context context;
    private final TacticalOverlayFeature overlayFeature;
    private final TacticalOverlayRenderer renderer;
    private View overlayView;
    
    /**
     * Constructor
     * @param context Application context
     * @param overlayFeature The tactical overlay feature
     */
    public TacticalOverlayManager(Context context, TacticalOverlayFeature overlayFeature) {
        this.context = context;
        this.overlayFeature = overlayFeature;
        this.renderer = new TacticalOverlayRenderer(context, overlayFeature);
    }
    
    /**
     * Attach the overlay to a container view
     * @param container Container to attach to
     */
    public void attachOverlay(ViewGroup container) {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
        }
        
        overlayView = renderer.createOverlayView();
        
        // Set layout parameters
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        
        container.addView(overlayView, params);
    }
    
    /**
     * Detach the overlay from its container
     */
    public void detachOverlay() {
        if (overlayView != null && overlayView.getParent() != null) {
            ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            overlayView = null;
        }
    }
    
    /**
     * Add a point marker to the overlay
     * @param id Marker ID
     * @param type Element type
     * @param x X coordinate
     * @param y Y coordinate
     * @param label Label text
     */
    public void addPointMarker(String id, TacticalOverlayFeature.OverlayElementType type, 
                              float x, float y, String label) {
        PointF point = new PointF(x, y);
        
        // Get color for this type
        int color = overlayFeature.getActiveColorScheme().getColorForType(type);
        
        // Create element
        TacticalOverlayFeature.OverlayElement element = 
            new TacticalOverlayFeature.OverlayElement(id, type, point, color);
        
        // Set label
        element.setLabel(label);
        
        // Add to overlay
        overlayFeature.addOverlayElement(element);
    }
    
    /**
     * Add a zone marker to the overlay
     * @param id Marker ID
     * @param type Element type
     * @param left Left coordinate
     * @param top Top coordinate
     * @param right Right coordinate
     * @param bottom Bottom coordinate
     * @param label Label text
     */
    public void addZoneMarker(String id, TacticalOverlayFeature.OverlayElementType type,
                             float left, float top, float right, float bottom, String label) {
        RectF rect = new RectF(left, top, right, bottom);
        
        // Get color for this type
        int color = overlayFeature.getActiveColorScheme().getColorForType(type);
        
        // Create element
        TacticalOverlayFeature.OverlayElement element = 
            new TacticalOverlayFeature.OverlayElement(id, type, rect, color);
        
        // Set label
        element.setLabel(label);
        
        // Add to overlay
        overlayFeature.addOverlayElement(element);
    }
    
    /**
     * Add a path marker to the overlay
     * @param id Marker ID
     * @param type Element type
     * @param points List of points
     * @param label Label text
     */
    public void addPathMarker(String id, TacticalOverlayFeature.OverlayElementType type,
                             List<PointF> points, String label) {
        // Get color for this type
        int color = overlayFeature.getActiveColorScheme().getColorForType(type);
        
        // Create element
        TacticalOverlayFeature.OverlayElement element = 
            new TacticalOverlayFeature.OverlayElement(id, type, new ArrayList<>(points), color);
        
        // Set label
        element.setLabel(label);
        
        // Add to overlay
        overlayFeature.addOverlayElement(element);
    }
    
    /**
     * Remove a marker from the overlay
     * @param id Marker ID
     */
    public void removeMarker(String id) {
        overlayFeature.removeOverlayElement(id);
    }
    
    /**
     * Clear all markers from the overlay
     */
    public void clearMarkers() {
        overlayFeature.clearOverlayElements();
    }
    
    /**
     * Set overlay visibility
     * @param visible true to show overlay, false to hide
     */
    public void setOverlayVisible(boolean visible) {
        overlayFeature.setOverlayVisible(visible);
        
        // Also update view visibility if available
        if (overlayView != null) {
            overlayView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
    
    /**
     * Set overlay display mode
     * @param mode Display mode
     */
    public void setDisplayMode(TacticalOverlayFeature.DisplayMode mode) {
        overlayFeature.setDisplayMode(mode);
    }
    
    /**
     * Set overlay transparency level
     * @param level Transparency level (0.0-1.0)
     */
    public void setTransparencyLevel(float level) {
        overlayFeature.setTransparencyLevel(level);
    }
    
    /**
     * Set overlay color scheme
     * @param schemeName Color scheme name
     */
    public void setColorScheme(String schemeName) {
        overlayFeature.setColorScheme(schemeName);
    }
    
    /**
     * Get the overlay feature
     * @return Tactical overlay feature
     */
    public TacticalOverlayFeature getOverlayFeature() {
        return overlayFeature;
    }
    
    /**
     * Get the overlay renderer
     * @return Tactical overlay renderer
     */
    public TacticalOverlayRenderer getRenderer() {
        return renderer;
    }
}
