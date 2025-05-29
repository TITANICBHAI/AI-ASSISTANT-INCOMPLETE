package com.aiassistant.ai.features.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import java.util.List;

/**
 * Tactical Overlay Renderer
 * Handles the actual rendering of overlay elements on the screen
 */
public class TacticalOverlayRenderer {
    private static final String TAG = "OverlayRenderer";
    
    private final Context context;
    private final Paint paint;
    private TacticalOverlayFeature overlayFeature;
    
    /**
     * Constructor
     * @param context Application context
     * @param overlayFeature The tactical overlay feature to render
     */
    public TacticalOverlayRenderer(Context context, TacticalOverlayFeature overlayFeature) {
        this.context = context;
        this.overlayFeature = overlayFeature;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }
    
    /**
     * Set the tactical overlay feature to render
     * @param overlayFeature The tactical overlay feature
     */
    public void setOverlayFeature(TacticalOverlayFeature overlayFeature) {
        this.overlayFeature = overlayFeature;
    }
    
    /**
     * Render the overlay on a canvas
     * @param canvas Canvas to render on
     */
    public void render(Canvas canvas) {
        if (overlayFeature == null || !overlayFeature.isEnabled() || !overlayFeature.isOverlayVisible()) {
            return;
        }
        
        try {
            // Get all visible overlay elements
            List<TacticalOverlayFeature.OverlayElement> elements = overlayFeature.getOverlayElements();
            
            // Set global paint properties based on overlay settings
            paint.setAlpha((int)(255 * overlayFeature.getTransparencyLevel()));
            
            // Render each element
            for (TacticalOverlayFeature.OverlayElement element : elements) {
                if (element.isVisible()) {
                    renderElement(canvas, element);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error rendering overlay", e);
        }
    }
    
    /**
     * Render a single overlay element
     * @param canvas Canvas to render on
     * @param element Element to render
     */
    private void renderElement(Canvas canvas, TacticalOverlayFeature.OverlayElement element) {
        // Set element color
        paint.setColor(element.getColor());
        
        // Render based on shape type
        Object shape = element.getShape();
        
        if (shape instanceof PointF) {
            // Render point
            renderPoint(canvas, (PointF) shape, element);
        } else if (shape instanceof RectF) {
            // Render rectangle
            renderRect(canvas, (RectF) shape, element);
        } else if (shape instanceof List<?>) {
            // Render path
            try {
                @SuppressWarnings("unchecked")
                List<PointF> path = (List<PointF>) shape;
                renderPath(canvas, path, element);
            } catch (ClassCastException e) {
                Log.e(TAG, "Invalid path data", e);
            }
        }
        
        // Render label if needed
        if (element.getLabel() != null && !element.getLabel().isEmpty()) {
            renderLabel(canvas, element);
        }
    }
    
    /**
     * Render a point element
     * @param canvas Canvas to render on
     * @param point Point location
     * @param element Element data
     */
    private void renderPoint(Canvas canvas, PointF point, TacticalOverlayFeature.OverlayElement element) {
        // Determine radius based on element type
        float radius = 10.0f;
        
        // Adjust style based on element type
        switch (element.getType()) {
            case ENEMY:
                paint.setStyle(Paint.Style.FILL);
                radius = 15.0f;
                break;
                
            case ALLY:
                paint.setStyle(Paint.Style.FILL);
                radius = 12.0f;
                break;
                
            case STRATEGIC_POINT:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3.0f);
                radius = 20.0f;
                break;
                
            case OBJECTIVE:
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setStrokeWidth(2.0f);
                radius = 18.0f;
                break;
                
            default:
                paint.setStyle(Paint.Style.FILL);
                break;
        }
        
        // Draw the point
        canvas.drawCircle(point.x, point.y, radius, paint);
    }
    
    /**
     * Render a rectangle element
     * @param canvas Canvas to render on
     * @param rect Rectangle area
     * @param element Element data
     */
    private void renderRect(Canvas canvas, RectF rect, TacticalOverlayFeature.OverlayElement element) {
        // Adjust style based on element type
        switch (element.getType()) {
            case DANGER_ZONE:
                paint.setStyle(Paint.Style.FILL);
                paint.setAlpha((int)(128 * overlayFeature.getTransparencyLevel()));
                break;
                
            case RESOURCE:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.0f);
                break;
                
            default:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.0f);
                break;
        }
        
        // Draw the rectangle
        canvas.drawRect(rect, paint);
        
        // Reset alpha
        paint.setAlpha((int)(255 * overlayFeature.getTransparencyLevel()));
    }
    
    /**
     * Render a path element
     * @param canvas Canvas to render on
     * @param path List of points forming a path
     * @param element Element data
     */
    private void renderPath(Canvas canvas, List<PointF> path, TacticalOverlayFeature.OverlayElement element) {
        if (path.size() < 2) {
            return;
        }
        
        // Adjust style based on element type
        switch (element.getType()) {
            case OPTIMAL_PATH:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5.0f);
                break;
                
            default:
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.0f);
                break;
        }
        
        // Draw lines between points
        for (int i = 0; i < path.size() - 1; i++) {
            PointF p1 = path.get(i);
            PointF p2 = path.get(i + 1);
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
        }
    }
    
    /**
     * Render element label
     * @param canvas Canvas to render on
     * @param element Element data
     */
    private void renderLabel(Canvas canvas, TacticalOverlayFeature.OverlayElement element) {
        Object shape = element.getShape();
        PointF labelPos = new PointF(0, 0);
        
        // Determine label position based on shape
        if (shape instanceof PointF) {
            PointF point = (PointF) shape;
            labelPos.x = point.x;
            labelPos.y = point.y + 25.0f; // Below the point
        } else if (shape instanceof RectF) {
            RectF rect = (RectF) shape;
            labelPos.x = rect.centerX();
            labelPos.y = rect.top - 10.0f; // Above the rectangle
        } else if (shape instanceof List<?>) {
            try {
                @SuppressWarnings("unchecked")
                List<PointF> path = (List<PointF>) shape;
                if (!path.isEmpty()) {
                    // Use the first point of the path
                    PointF point = path.get(0);
                    labelPos.x = point.x;
                    labelPos.y = point.y - 10.0f; // Above the path
                }
            } catch (ClassCastException e) {
                Log.e(TAG, "Invalid path data", e);
                return;
            }
        }
        
        // Set text properties
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(16.0f);
        paint.setTextAlign(Paint.Align.CENTER);
        
        // Draw text
        canvas.drawText(element.getLabel(), labelPos.x, labelPos.y, paint);
    }
    
    /**
     * Create an overlay view that can be added to a view hierarchy
     * @return Overlay view
     */
    public View createOverlayView() {
        return new OverlayView(context);
    }
    
    /**
     * Overlay View class
     * A custom view that renders the tactical overlay
     */
    private class OverlayView extends View implements TacticalOverlayFeature.OverlayUpdateListener {
        public OverlayView(Context context) {
            super(context);
            
            // Register as a listener for overlay updates
            if (overlayFeature != null) {
                overlayFeature.addUpdateListener(this);
            }
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            
            // Render the overlay
            render(canvas);
        }
        
        @Override
        public void onOverlayUpdated(TacticalOverlayFeature overlay) {
            // Invalidate view to trigger redraw
            postInvalidate();
        }
        
        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            
            // Unregister as listener
            if (overlayFeature != null) {
                overlayFeature.removeUpdateListener(this);
            }
        }
    }
}
