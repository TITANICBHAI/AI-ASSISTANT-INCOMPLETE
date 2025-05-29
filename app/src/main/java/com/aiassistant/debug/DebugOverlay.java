package com.aiassistant.debug;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Debug overlay for displaying real-time debug information
 * on screen during development and testing.
 */
public class DebugOverlay extends View {
    private static final int MAX_LINES = 10;
    private final Paint textPaint;
    private final Queue<String> logLines;
    private final List<DebugMetric> metrics;
    
    private boolean enabled = false;
    
    /**
     * Constructor
     * @param context Context
     */
    public DebugOverlay(Context context) {
        super(context);
        
        textPaint = new Paint();
        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(24f);
        textPaint.setAntiAlias(true);
        
        logLines = new LinkedList<>();
        metrics = new ArrayList<>();
    }
    
    /**
     * Enable or disable the overlay
     * @param enabled true to enable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setVisibility(enabled ? VISIBLE : GONE);
        invalidate();
    }
    
    /**
     * Add a log line to the overlay
     * @param line Line to add
     */
    public void addLogLine(String line) {
        if (!enabled) return;
        
        logLines.add(line);
        while (logLines.size() > MAX_LINES) {
            logLines.poll();
        }
        
        invalidate();
    }
    
    /**
     * Add or update a metric
     * @param name Metric name
     * @param value Metric value
     */
    public void updateMetric(String name, String value) {
        if (!enabled) return;
        
        // Check if metric already exists
        for (DebugMetric metric : metrics) {
            if (metric.name.equals(name)) {
                metric.value = value;
                invalidate();
                return;
            }
        }
        
        // Add new metric
        metrics.add(new DebugMetric(name, value));
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!enabled) return;
        
        // Draw log lines
        int y = 50;
        for (String line : logLines) {
            canvas.drawText(line, 20, y, textPaint);
            y += 30;
        }
        
        // Draw metrics
        y = 50;
        int metricsX = getWidth() - 300;
        for (DebugMetric metric : metrics) {
            canvas.drawText(metric.name + ": " + metric.value, metricsX, y, textPaint);
            y += 30;
        }
    }
    
    /**
     * Debug metric class
     */
    private static class DebugMetric {
        String name;
        String value;
        
        DebugMetric(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
