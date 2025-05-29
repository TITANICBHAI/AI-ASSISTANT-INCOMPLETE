package com.aiassistant.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.aiassistant.R;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.utils.Constants;
import com.aiassistant.utils.NotificationUtils;

import java.util.concurrent.TimeUnit;

/**
 * Service for detecting user inactivity
 * This service monitors user interaction with the device and triggers automated actions
 * when the user is inactive for a specified period of time.
 */
public class InactivityDetectionService extends Service implements SensorEventListener {
    private static final String TAG = "InactivityDetection";
    
    // Default inactivity timeout (2 minutes in milliseconds)
    private static final long DEFAULT_INACTIVITY_TIMEOUT = TimeUnit.MINUTES.toMillis(2);
    
    // Polling interval for checking screen state (10 seconds)
    private static final long SCREEN_STATE_CHECK_INTERVAL = TimeUnit.SECONDS.toMillis(10);
    
    // Motion detection threshold
    private static final float MOTION_THRESHOLD = 0.5f;
    
    // Service state
    private boolean isServiceRunning = false;
    private long inactivityTimeout = DEFAULT_INACTIVITY_TIMEOUT;
    private long lastActivityTimestamp = 0;
    
    // Sensors
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private PowerManager powerManager;
    private Handler handler;
    private Runnable inactivityCheckTask;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "InactivityDetectionService created");
        
        // Initialize sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        
        // Initialize handler for periodic checks
        handler = new Handler(Looper.getMainLooper());
        
        // Define the inactivity check task
        inactivityCheckTask = new Runnable() {
            @Override
            public void run() {
                checkInactivity();
                // Schedule the next check
                handler.postDelayed(this, SCREEN_STATE_CHECK_INTERVAL);
            }
        };
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("inactivity_timeout")) {
            inactivityTimeout = intent.getLongExtra("inactivity_timeout", DEFAULT_INACTIVITY_TIMEOUT);
        }
        
        Log.d(TAG, "Starting inactivity detection with timeout: " + inactivityTimeout + "ms");
        
        // Start as a foreground service
        startForeground(
                Constants.NOTIFICATION_ID_INACTIVITY,
                NotificationUtils.createForegroundServiceNotification(
                        this,
                        getString(R.string.monitoring_started),
                        "Monitoring for inactivity",
                        NotificationCompat.PRIORITY_LOW
                )
        );
        
        // Start monitoring
        startMonitoring();
        
        // Return sticky so service restarts if killed
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "InactivityDetectionService destroyed");
        stopMonitoring();
        super.onDestroy();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    /**
     * Start inactivity monitoring
     */
    private void startMonitoring() {
        if (!isServiceRunning) {
            Log.d(TAG, "Starting inactivity monitoring");
            
            // Register sensor listener
            if (accelerometer != null) {
                sensorManager.registerListener(
                        this,
                        accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL
                );
            }
            
            // Initialize the last activity timestamp
            updateLastActivityTimestamp();
            
            // Start the check task
            handler.post(inactivityCheckTask);
            
            isServiceRunning = true;
        }
    }
    
    /**
     * Stop inactivity monitoring
     */
    private void stopMonitoring() {
        if (isServiceRunning) {
            Log.d(TAG, "Stopping inactivity monitoring");
            
            // Unregister sensor listeners
            sensorManager.unregisterListener(this);
            
            // Remove callbacks
            handler.removeCallbacks(inactivityCheckTask);
            
            isServiceRunning = false;
        }
    }
    
    /**
     * Update the last activity timestamp to now
     */
    private void updateLastActivityTimestamp() {
        lastActivityTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Check if inactivity threshold has been reached
     */
    private void checkInactivity() {
        // First check if the screen is on
        if (!isScreenOn()) {
            Log.d(TAG, "Screen is off, not checking inactivity");
            return;
        }
        
        // Calculate elapsed time since last activity
        long currentTime = System.currentTimeMillis();
        long inactiveTime = currentTime - lastActivityTimestamp;
        
        if (inactiveTime >= inactivityTimeout) {
            Log.d(TAG, "Inactivity detected for " + inactiveTime + "ms, threshold: " + inactivityTimeout + "ms");
            onInactivityDetected();
            
            // Reset the timer after triggering
            updateLastActivityTimestamp();
        } else {
            Log.v(TAG, "User active, inactive time: " + inactiveTime + "ms");
        }
    }
    
    /**
     * Check if the device screen is on
     */
    private boolean isScreenOn() {
        if (powerManager != null) {
            return powerManager.isInteractive();
        }
        return true; // Default to true if we can't check
    }
    
    /**
     * Handle inactivity detection
     */
    private void onInactivityDetected() {
        Log.d(TAG, "User inactivity detected, triggering AI actions");
        
        // Get AI state manager and notify of inactivity
        AIStateManager aiManager = AIStateManager.getInstance(this);
        if (aiManager != null) {
            // This will trigger the AI to perform autonomous actions
            aiManager.onUserInactivityDetected();
        }
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Calculate total acceleration vector
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            
            double acceleration = Math.sqrt(x*x + y*y + z*z);
            
            // Detect significant motion (ignoring gravity)
            double accelDelta = Math.abs(acceleration - SensorManager.GRAVITY_EARTH);
            
            if (accelDelta > MOTION_THRESHOLD) {
                // User moved the device, update activity timestamp
                updateLastActivityTimestamp();
            }
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
    
    /**
     * Set the inactivity timeout
     * @param timeout The inactivity timeout in milliseconds
     */
    public void setInactivityTimeout(long timeout) {
        this.inactivityTimeout = timeout;
        Log.d(TAG, "Inactivity timeout updated to: " + timeout + "ms");
    }
}