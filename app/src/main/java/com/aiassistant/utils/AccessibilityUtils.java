package com.aiassistant.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.aiassistant.data.models.UIElement;
import com.aiassistant.services.AccessibilityDetectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Advanced utility class for accessibility functions with sophisticated game UI interaction
 */
public class AccessibilityUtils {
    private static final String TAG = "AccessibilityUtils";
    private static AccessibilityService service;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Constants for gesture timing
    private static final int DEFAULT_GESTURE_DURATION = 100; // milliseconds
    private static final int LONG_PRESS_DURATION = 500; // milliseconds
    private static final int SWIPE_DURATION = 300; // milliseconds
    private static final long GESTURE_COOLDOWN = 50; // milliseconds between gestures
    
    // Last gesture timestamp for rate limiting
    private static long lastGestureTime = 0;

    /**
     * Get root in active window
     * @return Root node
     */
    public static AccessibilityNodeInfo getRootInActiveWindow() {
        if (service == null) {
            return null;
        }
        return service.getRootInActiveWindow();
    }
    
    /**
     * Set the accessibility service
     * @param accessibilityService The service instance
     */
    public static void setService(AccessibilityService accessibilityService) {
        service = accessibilityService;
    }
    
    /**
     * Check if the accessibility service is enabled
     * @param context Context
     * @return True if enabled
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        int accessibilityEnabled = 0;
        final String serviceName = context.getPackageName() + "/" + AccessibilityDetectionService.class.getCanonicalName();
        
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
        
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            
            if (settingValue != null) {
                String[] services = settingValue.split(":");
                
                for (String service : services) {
                    if (service.equalsIgnoreCase(serviceName)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Find nodes by text
     * @param text Text to find
     * @return List of nodes
     */
    public static List<AccessibilityNodeInfo> findNodesByText(String text) {
        List<AccessibilityNodeInfo> results = new ArrayList<>();
        
        if (service == null) {
            return results;
        }
        
        AccessibilityNodeInfo rootNode = service.getRootInActiveWindow();
        if (rootNode == null) {
            return results;
        }
        
        // Search for matching nodes
        findNodesRecursive(rootNode, text, results);
        
        return results;
    }
    
    /**
     * Find nodes recursively
     * @param node Current node
     * @param text Text to match
     * @param results List to populate
     */
    private static void findNodesRecursive(AccessibilityNodeInfo node, String text, List<AccessibilityNodeInfo> results) {
        if (node == null) {
            return;
        }
        
        // Check if this node matches
        if (node.getText() != null && TextUtils.equals(node.getText(), text)) {
            results.add(AccessibilityNodeInfo.obtain(node));
        }
        
        // Check content description
        if (node.getContentDescription() != null && TextUtils.equals(node.getContentDescription(), text)) {
            results.add(AccessibilityNodeInfo.obtain(node));
        }
        
        // Check children nodes
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                findNodesRecursive(child, text, results);
                child.recycle();
            }
        }
    }
    
    /**
     * Click a node
     * @param node Node to click
     * @return True if successful
     */
    public static boolean clickNode(AccessibilityNodeInfo node) {
        if (node != null && node.isClickable()) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else if (node != null) {
            // If the node itself isn't clickable, try to find a clickable parent
            AccessibilityNodeInfo parent = node.getParent();
            while (parent != null) {
                if (parent.isClickable()) {
                    boolean result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    parent.recycle();
                    return result;
                }
                AccessibilityNodeInfo temp = parent;
                parent = parent.getParent();
                temp.recycle();
            }
            
            // If no clickable parent is found, try clicking using coordinates
            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);
            return clickAtPoint(bounds.centerX(), bounds.centerY());
        }
        return false;
    }
    
    /**
     * Input text to a node
     * @param node Node to input text
     * @param text Text to input
     * @return True if successful
     */
    public static boolean inputText(AccessibilityNodeInfo node, String text) {
        if (node != null && node.isEditable()) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
        return false;
    }
    
    /**
     * Click at a specific point on the screen
     * @param x X coordinate
     * @param y Y coordinate
     * @return True if successful
     */
    public static boolean clickAtPoint(float x, float y) {
        if (service == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false;
        }
        
        // Rate limit gestures
        if (!checkAndUpdateGestureTime()) {
            return false;
        }
        
        Log.d(TAG, "Clicking at point: " + x + ", " + y);
        
        final AtomicBoolean result = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                clickPath, 0, DEFAULT_GESTURE_DURATION));
        
        service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                result.set(true);
                latch.countDown();
            }
            
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                result.set(false);
                latch.countDown();
            }
        }, null);
        
        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Gesture interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
        
        return result.get();
    }
    
    /**
     * Perform a long press at a specific point
     * @param x X coordinate
     * @param y Y coordinate
     * @return True if successful
     */
    public static boolean longPressAtPoint(float x, float y) {
        if (service == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false;
        }
        
        // Rate limit gestures
        if (!checkAndUpdateGestureTime()) {
            return false;
        }
        
        Log.d(TAG, "Long press at point: " + x + ", " + y);
        
        final AtomicBoolean result = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                clickPath, 0, LONG_PRESS_DURATION));
        
        service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                result.set(true);
                latch.countDown();
            }
            
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                result.set(false);
                latch.countDown();
            }
        }, null);
        
        try {
            latch.await(LONG_PRESS_DURATION + 500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Gesture interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
        
        return result.get();
    }
    
    /**
     * Perform a swipe from one point to another
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param endX Ending X coordinate
     * @param endY Ending Y coordinate
     * @param duration Duration of the swipe in milliseconds
     * @return True if successful
     */
    public static boolean swipe(float startX, float startY, float endX, float endY, int duration) {
        if (service == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false;
        }
        
        // Rate limit gestures
        if (!checkAndUpdateGestureTime()) {
            return false;
        }
        
        Log.d(TAG, "Swiping from (" + startX + "," + startY + ") to (" + endX + "," + endY + ")");
        
        final AtomicBoolean result = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path swipePath = new Path();
        swipePath.moveTo(startX, startY);
        swipePath.lineTo(endX, endY);
        
        int swipeDuration = duration > 0 ? duration : SWIPE_DURATION;
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                swipePath, 0, swipeDuration));
        
        service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                result.set(true);
                latch.countDown();
            }
            
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                result.set(false);
                latch.countDown();
            }
        }, null);
        
        try {
            latch.await(swipeDuration + 500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Gesture interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
        
        return result.get();
    }
    
    /**
     * Perform a multi-touch gesture (two fingers) - often used in games for move+aim simultaneously
     * @param finger1StartX First finger start X
     * @param finger1StartY First finger start Y
     * @param finger1EndX First finger end X
     * @param finger1EndY First finger end Y
     * @param finger2StartX Second finger start X
     * @param finger2StartY Second finger start Y
     * @param finger2EndX Second finger end X
     * @param finger2EndY Second finger end Y
     * @param duration Duration of the gesture
     * @return True if successful
     */
    public static boolean multiTouchGesture(
            float finger1StartX, float finger1StartY, float finger1EndX, float finger1EndY,
            float finger2StartX, float finger2StartY, float finger2EndX, float finger2EndY,
            int duration) {
        if (service == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return false;
        }
        
        // Rate limit gestures
        if (!checkAndUpdateGestureTime()) {
            return false;
        }
        
        Log.d(TAG, "Performing multi-touch gesture");
        
        final AtomicBoolean result = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        
        // First finger path
        Path path1 = new Path();
        path1.moveTo(finger1StartX, finger1StartY);
        path1.lineTo(finger1EndX, finger1EndY);
        
        // Second finger path
        Path path2 = new Path();
        path2.moveTo(finger2StartX, finger2StartY);
        path2.lineTo(finger2EndX, finger2EndY);
        
        int gestureDuration = duration > 0 ? duration : SWIPE_DURATION;
        
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path1, 0, gestureDuration));
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path2, 0, gestureDuration));
        
        service.dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                result.set(true);
                latch.countDown();
            }
            
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                result.set(false);
                latch.countDown();
            }
        }, null);
        
        try {
            latch.await(gestureDuration + 500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Gesture interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
        
        return result.get();
    }
    
    /**
     * Perform a tap with precise timing - important for rhythm games or frame-perfect inputs
     * @param x X coordinate
     * @param y Y coordinate
     * @param delay Delay before performing the tap (ms)
     * @return True if successful
     */
    public static boolean scheduledTap(float x, float y, long delay) {
        if (service == null) {
            return false;
        }
        
        final AtomicBoolean result = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        
        mainHandler.postDelayed(() -> {
            boolean tapResult = clickAtPoint(x, y);
            result.set(tapResult);
            latch.countDown();
        }, delay);
        
        try {
            latch.await(delay + 1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Scheduled tap interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
        
        return result.get();
    }
    
    /**
     * Perform a sequence of taps with precise timing
     * @param points List of points to tap
     * @param delays List of delays between taps
     * @return True if all taps were successful
     */
    public static boolean tapSequence(List<Point> points, List<Long> delays) {
        if (service == null || points == null || delays == null || points.size() != delays.size() + 1) {
            return false;
        }
        
        boolean success = true;
        
        // First tap has no delay
        success &= clickAtPoint(points.get(0).x, points.get(0).y);
        
        // Subsequent taps with delays
        for (int i = 1; i < points.size(); i++) {
            try {
                Thread.sleep(delays.get(i-1));
            } catch (InterruptedException e) {
                Log.e(TAG, "Tap sequence interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                return false;
            }
            
            success &= clickAtPoint(points.get(i).x, points.get(i).y);
        }
        
        return success;
    }
    
    /**
     * Click on a UIElement (game button or control)
     * @param element The UI element to click
     * @return True if successful
     */
    public static boolean clickUIElement(UIElement element) {
        if (element == null) {
            return false;
        }
        
        int centerX = element.getX() + element.getWidth() / 2;
        int centerY = element.getY() + element.getHeight() / 2;
        
        Log.d(TAG, "Clicking UI element at (" + centerX + "," + centerY + "): " + element.getElementId());
        
        return clickAtPoint(centerX, centerY);
    }
    
    /**
     * Find a UI element by ID in the current screen
     * @param elementId Element ID to find
     * @param elements List of detected UI elements
     * @return The UI element or null
     */
    public static UIElement findElementById(String elementId, List<UIElement> elements) {
        if (elements == null || elementId == null) {
            return null;
        }
        
        for (UIElement element : elements) {
            if (elementId.equals(element.getElementId())) {
                return element;
            }
        }
        
        return null;
    }
    
    /**
     * Find elements by type (button, joystick, etc.)
     * @param type Element type
     * @param elements List of detected UI elements
     * @return List of matching elements
     */
    public static List<UIElement> findElementsByType(String type, List<UIElement> elements) {
        List<UIElement> results = new ArrayList<>();
        
        if (elements == null || type == null) {
            return results;
        }
        
        for (UIElement element : elements) {
            if (type.equals(element.getElementType())) {
                results.add(element);
            }
        }
        
        return results;
    }
    
    /**
     * Check and update gesture time for rate limiting
     * @return True if enough time has passed to allow a new gesture
     */
    private static boolean checkAndUpdateGestureTime() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastGestureTime < GESTURE_COOLDOWN) {
            Log.d(TAG, "Gesture rate limited");
            return false;
        }
        
        lastGestureTime = currentTime;
        return true;
    }
}
