package com.aiassistant.core.ai.detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.aiassistant.data.models.UIElement;
import com.aiassistant.services.AIAccessibilityService;
import com.aiassistant.utils.AccessibilityUtils;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Detects UI elements in games and applications using both accessibility services
 * and computer vision techniques with TensorFlow Lite
 */
public class UIElementDetector {
    private static final String TAG = "UIElementDetector";
    
    // Context
    private final Context context;
    
    // Tensorflow model
    private Interpreter tfLiteInterpreter;
    private ByteBuffer inputBuffer;
    private int[] inputDims = new int[]{1, 300, 300, 3}; // Default dims, will be updated when loaded
    private float[][] outputLocations;
    private float[] outputClasses;
    private float[] outputScores;
    private float[] numDetections;
    
    // Model parameters
    private static final int MAX_DETECTIONS = 10;
    private static final float DETECTION_THRESHOLD = 0.5f;
    private static final String MODEL_FILENAME = "ui_element_detector.tflite";
    
    // Processing state
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final List<UIElement> cachedElements = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    // Element mappings (for specific games)
    private final Map<String, Map<String, UIElement>> gameElementTemplates = new HashMap<>();
    
    /**
     * Constructor
     */
    public UIElementDetector(Context context) {
        this.context = context.getApplicationContext();
        
        // Initialize TensorFlow Lite model if available
        initTFLiteModel();
        
        // Load element templates for known games
        loadGameElementTemplates();
    }
    
    /**
     * Initialize TensorFlow Lite model
     */
    private void initTFLiteModel() {
        try {
            // Check if model file exists
            File modelFile = new File(context.getFilesDir(), MODEL_FILENAME);
            if (!modelFile.exists()) {
                Log.d(TAG, "TFLite model not found: " + MODEL_FILENAME);
                return;
            }
            
            // Load model
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(2);
            tfLiteInterpreter = new Interpreter(modelFile, options);
            
            // Get input dimensions
            int[] inputShape = tfLiteInterpreter.getInputTensor(0).shape();
            inputDims = inputShape;
            
            // Initialize input buffer
            int inputSize = inputDims[1] * inputDims[2] * inputDims[3];
            inputBuffer = ByteBuffer.allocateDirect(4 * inputSize);
            inputBuffer.order(ByteOrder.nativeOrder());
            
            // Initialize output arrays
            outputLocations = new float[1][MAX_DETECTIONS][4];
            outputClasses = new float[1][MAX_DETECTIONS];
            outputScores = new float[1][MAX_DETECTIONS];
            numDetections = new float[1];
            
            Log.d(TAG, "TFLite model initialized: " + MODEL_FILENAME);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing TFLite model: " + e.getMessage());
            tfLiteInterpreter = null;
        }
    }
    
    /**
     * Load game-specific UI element templates
     */
    private void loadGameElementTemplates() {
        // Free Fire element templates
        Map<String, UIElement> freeFireElements = new HashMap<>();
        freeFireElements.put("fire_button", new UIElement("fire_button", "button", 980, 500, 160, 160));
        freeFireElements.put("joystick", new UIElement("joystick", "joystick", 200, 650, 200, 200));
        freeFireElements.put("reload_button", new UIElement("reload_button", "button", 750, 500, 100, 100));
        freeFireElements.put("jump_button", new UIElement("jump_button", "button", 950, 700, 120, 120));
        freeFireElements.put("crouch_button", new UIElement("crouch_button", "button", 850, 700, 120, 120));
        freeFireElements.put("weapon_switch", new UIElement("weapon_switch", "button", 800, 350, 120, 100));
        gameElementTemplates.put("com.dts.freefireth", freeFireElements);
        
        // PUBG Mobile element templates
        Map<String, UIElement> pubgElements = new HashMap<>();
        pubgElements.put("fire_button", new UIElement("fire_button", "button", 1000, 550, 160, 160));
        pubgElements.put("joystick", new UIElement("joystick", "joystick", 250, 700, 200, 200));
        pubgElements.put("scope_button", new UIElement("scope_button", "button", 900, 600, 120, 120));
        pubgElements.put("jump_button", new UIElement("jump_button", "button", 950, 700, 120, 120));
        pubgElements.put("crouch_button", new UIElement("crouch_button", "button", 800, 700, 120, 120));
        pubgElements.put("prone_button", new UIElement("prone_button", "button", 650, 700, 120, 120));
        gameElementTemplates.put("com.tencent.ig", pubgElements);
    }
    
    /**
     * Detect UI elements in a screenshot
     * 
     * This method uses a combination of accessibility service information
     * and computer vision techniques to identify interactive elements.
     * 
     * @param screenshot The screenshot to analyze
     * @return List of detected UI elements
     */
    public List<UIElement> detectUIElements(Bitmap screenshot) {
        if (screenshot == null) {
            return new ArrayList<>();
        }
        
        // Only allow one detection process at a time
        if (isProcessing.compareAndSet(false, true)) {
            try {
                List<UIElement> detectedElements = new ArrayList<>();
                
                // First, try to get elements from accessibility tree
                List<UIElement> accessibilityElements = getElementsFromAccessibilityTree();
                detectedElements.addAll(accessibilityElements);
                
                // If we got enough elements from accessibility, we might not need vision
                if (detectedElements.size() < 3) {
                    // Next, try to detect from template matching (for known games)
                    String currentPackage = AIAccessibilityService.getInstance() != null ?
                            AIAccessibilityService.getInstance().getCurrentPackage() : null;
                            
                    if (currentPackage != null && gameElementTemplates.containsKey(currentPackage)) {
                        List<UIElement> templateElements = detectElementsFromTemplates(currentPackage, screenshot);
                        for (UIElement element : templateElements) {
                            // Avoid duplicates
                            boolean isDuplicate = false;
                            for (UIElement existing : detectedElements) {
                                if (elementsOverlap(element, existing)) {
                                    isDuplicate = true;
                                    break;
                                }
                            }
                            
                            if (!isDuplicate) {
                                detectedElements.add(element);
                            }
                        }
                    }
                }
                
                // If ML model is available and we still need more elements, use vision detection
                if (tfLiteInterpreter != null && detectedElements.size() < 5) {
                    List<UIElement> mlElements = detectElementsWithML(screenshot);
                    for (UIElement element : mlElements) {
                        // Avoid duplicates
                        boolean isDuplicate = false;
                        for (UIElement existing : detectedElements) {
                            if (elementsOverlap(element, existing)) {
                                isDuplicate = true;
                                break;
                            }
                        }
                        
                        if (!isDuplicate) {
                            detectedElements.add(element);
                        }
                    }
                }
                
                // Update cached elements
                cachedElements.clear();
                cachedElements.addAll(detectedElements);
                
                Log.d(TAG, "Detected " + detectedElements.size() + " UI elements");
                return detectedElements;
            } finally {
                isProcessing.set(false);
            }
        } else {
            // If already processing, return cached elements
            Log.d(TAG, "Returning cached elements (" + cachedElements.size() + ")");
            return new ArrayList<>(cachedElements);
        }
    }
    
    /**
     * Get UI elements from accessibility service
     */
    private List<UIElement> getElementsFromAccessibilityTree() {
        List<UIElement> elements = new ArrayList<>();
        
        AccessibilityNodeInfo rootNode = AccessibilityUtils.getRootInActiveWindow();
        if (rootNode == null) {
            return elements;
        }
        
        try {
            // Extract elements from accessibility tree
            processAccessibilityNode(rootNode, elements);
        } finally {
            rootNode.recycle();
        }
        
        return elements;
    }
    
    /**
     * Process an accessibility node and extract UI elements
     */
    private void processAccessibilityNode(AccessibilityNodeInfo node, List<UIElement> elements) {
        if (node == null) {
            return;
        }
        
        // Extract element info if clickable or has text
        if (node.isClickable() || (node.getText() != null && !node.getText().toString().isEmpty())) {
            Rect bounds = new Rect();
            node.getBoundsInScreen(bounds);
            
            // Only add if the element has a reasonable size
            if (bounds.width() > 20 && bounds.height() > 20) {
                UIElement element = new UIElement();
                
                // Generate unique ID
                element.setElementId(UUID.randomUUID().toString());
                
                // Set type based on class name
                String className = node.getClassName() != null ? node.getClassName().toString() : "";
                if (className.contains("Button")) {
                    element.setElementType("button");
                } else if (className.contains("ImageView")) {
                    element.setElementType("image");
                } else if (className.contains("TextView")) {
                    element.setElementType("text");
                } else if (className.contains("EditText")) {
                    element.setElementType("input");
                } else {
                    element.setElementType("view");
                }
                
                // Set position and size
                element.setX(bounds.left);
                element.setY(bounds.top);
                element.setWidth(bounds.width());
                element.setHeight(bounds.height());
                
                // Set properties
                element.setClickable(node.isClickable());
                element.setLongClickable(node.isLongClickable());
                
                if (node.getText() != null) {
                    element.setText(node.getText().toString());
                }
                
                if (node.getContentDescription() != null) {
                    element.setDescription(node.getContentDescription().toString());
                }
                
                if (node.getViewIdResourceName() != null) {
                    element.setResourceId(node.getViewIdResourceName());
                }
                
                elements.add(element);
            }
        }
        
        // Process children
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                processAccessibilityNode(child, elements);
                child.recycle();
            }
        }
    }
    
    /**
     * Detect UI elements using template matching for specific games
     */
    private List<UIElement> detectElementsFromTemplates(String packageName, Bitmap screenshot) {
        List<UIElement> elements = new ArrayList<>();
        
        // Get templates for this game
        Map<String, UIElement> templates = gameElementTemplates.get(packageName);
        if (templates == null) {
            return elements;
        }
        
        // For each template, create a new instance and add to list
        for (Map.Entry<String, UIElement> entry : templates.entrySet()) {
            UIElement template = entry.getValue();
            UIElement element = template.copy();
            element.setGameId(packageName);
            element.setConfidence(1.0f); // Template elements have perfect confidence
            elements.add(element);
        }
        
        return elements;
    }
    
    /**
     * Detect UI elements using ML model
     */
    private List<UIElement> detectElementsWithML(Bitmap screenshot) {
        List<UIElement> elements = new ArrayList<>();
        
        if (tfLiteInterpreter == null || screenshot == null) {
            return elements;
        }
        
        try {
            // Prepare input bitmap
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                    screenshot, inputDims[1], inputDims[2], true);
            
            // Fill input buffer
            inputBuffer.rewind();
            int[] pixels = new int[inputDims[1] * inputDims[2]];
            resizedBitmap.getPixels(pixels, 0, resizedBitmap.getWidth(), 0, 0, 
                    resizedBitmap.getWidth(), resizedBitmap.getHeight());
            
            for (int pixel : pixels) {
                // Extract RGB values and normalize
                float r = ((pixel >> 16) & 0xFF) / 255.0f;
                float g = ((pixel >> 8) & 0xFF) / 255.0f;
                float b = (pixel & 0xFF) / 255.0f;
                
                inputBuffer.putFloat(r);
                inputBuffer.putFloat(g);
                inputBuffer.putFloat(b);
            }
            
            // Prepare output map
            Map<Integer, Object> outputMap = new HashMap<>();
            outputMap.put(0, outputLocations);
            outputMap.put(1, outputClasses);
            outputMap.put(2, outputScores);
            outputMap.put(3, numDetections);
            
            // Run inference
            tfLiteInterpreter.runForMultipleInputsOutputs(new Object[]{inputBuffer}, outputMap);
            
            // Process results
            int numDetectionsInt = Math.round(numDetections[0]);
            for (int i = 0; i < numDetectionsInt; i++) {
                if (outputScores[0][i] > DETECTION_THRESHOLD) {
                    // Get bounding box
                    float top = outputLocations[0][i][0] * screenshot.getHeight();
                    float left = outputLocations[0][i][1] * screenshot.getWidth();
                    float bottom = outputLocations[0][i][2] * screenshot.getHeight();
                    float right = outputLocations[0][i][3] * screenshot.getWidth();
                    
                    Rect bounds = new Rect(
                            Math.round(left), 
                            Math.round(top), 
                            Math.round(right), 
                            Math.round(bottom));
                    
                    // Get class
                    int classId = Math.round(outputClasses[0][i]);
                    String elementType = getElementTypeFromClass(classId);
                    
                    // Create new element
                    UIElement element = new UIElement(
                            "ml_" + UUID.randomUUID().toString(),
                            elementType,
                            bounds,
                            outputScores[0][i]
                    );
                    
                    element.setClickable(true);
                    elements.add(element);
                }
            }
            
            // Clean up
            resizedBitmap.recycle();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in ML element detection: " + e.getMessage());
        }
        
        return elements;
    }
    
    /**
     * Check if two elements overlap (to avoid duplicates)
     */
    private boolean elementsOverlap(UIElement a, UIElement b) {
        Rect boundsA = a.getBounds();
        Rect boundsB = b.getBounds();
        
        return boundsA.intersect(boundsB);
    }
    
    /**
     * Get UI element type name from class ID
     */
    private String getElementTypeFromClass(int classId) {
        switch (classId) {
            case 0: return "button";
            case 1: return "joystick";
            case 2: return "slider";
            case 3: return "dpad";
            case 4: return "menu";
            case 5: return "healthbar";
            case 6: return "ammo";
            case 7: return "minimap";
            default: return "unknown";
        }
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        if (tfLiteInterpreter != null) {
            tfLiteInterpreter.close();
            tfLiteInterpreter = null;
        }
        
        cachedElements.clear();
    }
}
