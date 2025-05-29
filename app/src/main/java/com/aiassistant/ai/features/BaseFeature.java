package com.aiassistant.ai.features;

import android.content.Context;
import android.util.Log;

/**
 * Base abstract class for all AI features
 * Provides common functionality for feature implementations
 */
public abstract class BaseFeature implements AIFeature {
    private static final String TAG = "BaseFeature";
    
    protected final Context context;
    protected final String name;
    protected boolean enabled;
    
    /**
     * Constructor for base feature
     * @param context Application context
     * @param name Feature name
     */
    public BaseFeature(Context context, String name) {
        this.context = context;
        this.name = name;
        this.enabled = false;
    }
    
    @Override
    public boolean initialize() {
        Log.d(TAG, "Initializing feature: " + name);
        return true;
    }
    
    @Override
    public void update() {
        // Default implementation does nothing
    }
    
    @Override
    public void shutdown() {
        Log.d(TAG, "Shutting down feature: " + name);
        enabled = false;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        Log.d(TAG, "Feature " + name + " " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Get application context
     * @return Application context
     */
    protected Context getContext() {
        return context;
    }
}
