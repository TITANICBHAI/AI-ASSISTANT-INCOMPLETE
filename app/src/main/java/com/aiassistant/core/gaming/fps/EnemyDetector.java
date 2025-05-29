package com.aiassistant.core.gaming.fps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.aiassistant.core.ai.perception.VisualPerceptionManager;
import com.aiassistant.utils.BitmapUtils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Advanced enemy detection system for FPS games.
 * Uses computer vision techniques to identify potential enemies in game screenshots.
 */
public class EnemyDetector {
    private static final String TAG = "EnemyDetector";
    
    // Game-specific detection configurations
    private static final Map<String, GameDetectionConfig> GAME_CONFIGS = new HashMap<>();
    
    // Default detection parameters
    private float detectionThreshold = 0.65f;
    private int minEnemySize = 30;
    private boolean useDeepLearning = true;
    private boolean useColorAnalysis = true;
    private boolean useMotionAnalysis = true;
    private boolean useContourAnalysis = true;
    
    // Reference to perception manager for advanced detection
    private final VisualPerceptionManager perceptionManager;
    private final Context context;
    
    // Previous frame data for motion detection
    private Bitmap previousFrame = null;
    
    // Detection statistics for performance tuning
    private int totalDetectionCount = 0;
    private int successfulDetectionCount = 0;
    
    // Cache of recent detection results to smooth detection
    private List<Rect> lastDetectedEnemies = new ArrayList<>();
    private long lastDetectionTime = 0;
    private static final long DETECTION_CACHE_TTL_MS = 100; // Cache valid for 100ms
    
    static {
        // Initialize game-specific configurations
        
        // PUBG Mobile configuration
        GameDetectionConfig pubgConfig = new GameDetectionConfig();
        pubgConfig.redThreshold = 160;
        pubgConfig.redDominanceRatio = 1.4f;
        pubgConfig.useUniformColorDetection = true;
        pubgConfig.minBoundingSizePercent = 0.01f;
        pubgConfig.maxBoundingSizePercent = 0.2f;
        pubgConfig.headTargetOffsetPercent = 0.2f;
        GAME_CONFIGS.put("com.tencent.ig", pubgConfig);
        
        // Free Fire configuration
        GameDetectionConfig freeFireConfig = new GameDetectionConfig();
        freeFireConfig.redThreshold = 150;
        freeFireConfig.redDominanceRatio = 1.3f;
        freeFireConfig.useShapeAnalysis = true;
        freeFireConfig.minBoundingSizePercent = 0.015f;
        freeFireConfig.maxBoundingSizePercent = 0.25f;
        freeFireConfig.headTargetOffsetPercent = 0.23f;
        GAME_CONFIGS.put("com.dts.freefireth", freeFireConfig);
        
        // Call of Duty Mobile configuration
        GameDetectionConfig codConfig = new GameDetectionConfig();
        codConfig.redThreshold = 140;
        codConfig.redDominanceRatio = 1.5f;
        codConfig.useEdgeDetection = true;
        codConfig.minBoundingSizePercent = 0.02f;
        codConfig.maxBoundingSizePercent = 0.3f;
        codConfig.headTargetOffsetPercent = 0.18f;
        GAME_CONFIGS.put("com.activision.callofduty.shooter", codConfig);
    }
    
    /**
     * Constructor
     * @param context Application context
     * @param perceptionManager Visual perception manager for deep learning-based detection
     */
    public EnemyDetector(Context context, VisualPerceptionManager perceptionManager) {
        this.context = context;
        this.perceptionManager = perceptionManager;
    }
    
    /**
     * Set the detection threshold for enemy confidence
     * @param threshold Confidence threshold (0.0 to 1.0)
     */
    public void setDetectionThreshold(float threshold) {
        if (threshold >= 0.0f && threshold <= 1.0f) {
            this.detectionThreshold = threshold;
        }
    }
    
    /**
     * Set the minimum enemy size in pixels
     * @param minSize Minimum size in pixels
     */
    public void setMinEnemySize(int minSize) {
        if (minSize > 0) {
            this.minEnemySize = minSize;
        }
    }
    
    /**
     * Enable or disable deep learning-based detection
     * @param enabled True to enable, false to disable
     */
    public void setDeepLearningEnabled(boolean enabled) {
        this.useDeepLearning = enabled;
    }
    
    /**
     * Enable or disable color analysis for detection
     * @param enabled True to enable, false to disable
     */
    public void setColorAnalysisEnabled(boolean enabled) {
        this.useColorAnalysis = enabled;
    }
    
    /**
     * Enable or disable motion analysis for detection
     * @param enabled True to enable, false to disable
     */
    public void setMotionAnalysisEnabled(boolean enabled) {
        this.useMotionAnalysis = enabled;
    }
    
    /**
     * Enable or disable contour analysis for detection
     * @param enabled True to enable, false to disable
     */
    public void setContourAnalysisEnabled(boolean enabled) {
        this.useContourAnalysis = enabled;
    }
    
    /**
     * Detect potential enemies in a game screenshot
     * @param screenshot Current screenshot bitmap
     * @param gamePackage Package name of the game (for game-specific optimizations)
     * @return List of rectangles containing potential enemies
     */
    public List<Rect> detectEnemies(Bitmap screenshot, String gamePackage) {
        if (screenshot == null) {
            return new ArrayList<>();
        }
        
        // Check if we have a recent detection that's still valid
        long currentTime = System.currentTimeMillis();
        if (!lastDetectedEnemies.isEmpty() && 
            (currentTime - lastDetectionTime) < DETECTION_CACHE_TTL_MS) {
            return new ArrayList<>(lastDetectedEnemies);
        }
        
        totalDetectionCount++;
        List<Rect> enemyRects = new ArrayList<>();
        
        try {
            // Get game-specific configuration if available
            GameDetectionConfig config = GAME_CONFIGS.get(gamePackage);
            if (config == null) {
                config = new GameDetectionConfig(); // Use default if no specific config
            }
            
            // Combine multiple detection methods for higher accuracy
            
            // Method 1: Deep learning-based detection (if enabled)
            if (useDeepLearning && perceptionManager != null) {
                List<Rect> deepLearningResults = detectEnemiesDeepLearning(screenshot, config);
                if (deepLearningResults != null && !deepLearningResults.isEmpty()) {
                    enemyRects.addAll(deepLearningResults);
                    Log.d(TAG, "Deep learning detection found " + deepLearningResults.size() + " enemies");
                }
            }
            
            // Method 2: Color-based detection (if enabled)
            if (useColorAnalysis) {
                List<Rect> colorResults = detectEnemiesColor(screenshot, config);
                if (colorResults != null && !colorResults.isEmpty()) {
                    mergeDetections(enemyRects, colorResults);
                    Log.d(TAG, "Color analysis found " + colorResults.size() + " enemies");
                }
            }
            
            // Method 3: Motion detection (if enabled and previous frame available)
            if (useMotionAnalysis && previousFrame != null) {
                List<Rect> motionResults = detectEnemiesMotion(screenshot, previousFrame, config);
                if (motionResults != null && !motionResults.isEmpty()) {
                    mergeDetections(enemyRects, motionResults);
                    Log.d(TAG, "Motion analysis found " + motionResults.size() + " enemies");
                }
            }
            
            // Method 4: Contour/shape detection (if enabled)
            if (useContourAnalysis) {
                List<Rect> contourResults = detectEnemiesContour(screenshot, config);
                if (contourResults != null && !contourResults.isEmpty()) {
                    mergeDetections(enemyRects, contourResults);
                    Log.d(TAG, "Contour analysis found " + contourResults.size() + " enemies");
                }
            }
            
            // Apply additional filtering
            filterDetections(enemyRects, screenshot, config);
            
            // Update detection statistics
            if (!enemyRects.isEmpty()) {
                successfulDetectionCount++;
            }
            
            // Update cached results
            lastDetectedEnemies = new ArrayList<>(enemyRects);
            lastDetectionTime = currentTime;
            
            // Store this frame for next motion detection
            if (previousFrame != null && previousFrame != screenshot) {
                previousFrame.recycle();
            }
            previousFrame = screenshot.copy(screenshot.getConfig(), true);
            
        } catch (Exception e) {
            Log.e(TAG, "Error detecting enemies", e);
        }
        
        return enemyRects;
    }
    
    /**
     * Detect enemies using deep learning approach
     */
    private List<Rect> detectEnemiesDeepLearning(Bitmap screenshot, GameDetectionConfig config) {
        List<Rect> results = new ArrayList<>();
        
        try {
            // Use VisualPerceptionManager for object detection
            if (perceptionManager != null) {
                Map<String, List<Rect>> detections = perceptionManager.detectObjects(screenshot);
                
                // Extract person/enemy detections
                List<Rect> personDetections = detections.get("person");
                if (personDetections != null && !personDetections.isEmpty()) {
                    for (Rect detection : personDetections) {
                        // Filter by confidence if available
                        results.add(detection);
                    }
                }
                
                // Also check custom enemy class if available
                List<Rect> enemyDetections = detections.get("enemy");
                if (enemyDetections != null && !enemyDetections.isEmpty()) {
                    for (Rect detection : enemyDetections) {
                        results.add(detection);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in deep learning detection", e);
        }
        
        return results;
    }
    
    /**
     * Detect enemies using color analysis
     */
    private List<Rect> detectEnemiesColor(Bitmap screenshot, GameDetectionConfig config) {
        List<Rect> results = new ArrayList<>();
        
        try {
            int width = screenshot.getWidth();
            int height = screenshot.getHeight();
            
            // Get the main game area (excluding UI elements)
            Rect gameArea = new Rect(
                    width / 10,           // Left
                    height / 6,           // Top
                    width * 9 / 10,       // Right
                    height * 5 / 6        // Bottom
            );
            
            // Sample points in the game area
            int sampleStepX = Math.max(1, gameArea.width() / 25);
            int sampleStepY = Math.max(1, gameArea.height() / 25);
            
            // Find unusual colors that might represent enemies
            for (int y = gameArea.top; y < gameArea.bottom; y += sampleStepY) {
                for (int x = gameArea.left; x < gameArea.right; x += sampleStepX) {
                    int pixel = screenshot.getPixel(x, y);
                    
                    // Check for colors that might represent enemies based on config
                    if (isEnemyColor(pixel, config)) {
                        // Found potential enemy, look for its boundaries
                        Rect potentialEnemy = expandColorRegion(screenshot, x, y, gameArea, config);
                        
                        // Check minimum size
                        if (potentialEnemy.width() > minEnemySize && potentialEnemy.height() > minEnemySize) {
                            results.add(potentialEnemy);
                        }
                    }
                }
            }
            
            // Merge overlapping detections
            results = mergeOverlappingRects(results);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in color detection", e);
        }
        
        return results;
    }
    
    /**
     * Detect enemies using motion detection between frames
     */
    private List<Rect> detectEnemiesMotion(Bitmap currentFrame, Bitmap previousFrame, GameDetectionConfig config) {
        List<Rect> results = new ArrayList<>();
        
        try {
            // Convert bitmaps to OpenCV Mats
            Mat currentMat = new Mat();
            Mat prevMat = new Mat();
            Utils.bitmapToMat(currentFrame, currentMat);
            Utils.bitmapToMat(previousFrame, prevMat);
            
            // Calculate absolute difference between frames
            Mat diffMat = new Mat();
            Core.absdiff(currentMat, prevMat, diffMat);
            
            // Convert to grayscale
            Mat grayDiff = new Mat();
            Imgproc.cvtColor(diffMat, grayDiff, Imgproc.COLOR_RGB2GRAY);
            
            // Apply threshold to identify significant changes
            Mat threshMat = new Mat();
            Imgproc.threshold(grayDiff, threshMat, 20, 255, Imgproc.THRESH_BINARY);
            
            // Apply morphological operations to clean up the mask
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
            Imgproc.morphologyEx(threshMat, threshMat, Imgproc.MORPH_CLOSE, kernel);
            
            // Find contours in the movement mask
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(threshMat, contours, hierarchy, 
                    Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            
            // Filter contours and convert to rectangles
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                
                // Filter by size
                if (area > minEnemySize * minEnemySize) {
                    org.opencv.core.Rect rect = Imgproc.boundingRect(contour);
                    
                    // Convert OpenCV Rect to Android Rect
                    Rect androidRect = new Rect(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
                    
                    // Check if it's a valid enemy candidate
                    if (validateEnemyCandidate(currentFrame, androidRect, config)) {
                        results.add(androidRect);
                    }
                }
                
                contour.release();
            }
            
            // Clean up
            currentMat.release();
            prevMat.release();
            diffMat.release();
            grayDiff.release();
            threshMat.release();
            kernel.release();
            hierarchy.release();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in motion detection", e);
        }
        
        return results;
    }
    
    /**
     * Detect enemies using contour/shape analysis
     */
    private List<Rect> detectEnemiesContour(Bitmap screenshot, GameDetectionConfig config) {
        List<Rect> results = new ArrayList<>();
        
        try {
            // Convert bitmap to OpenCV Mat
            Mat rgbaMat = new Mat();
            Utils.bitmapToMat(screenshot, rgbaMat);
            
            // Process for shape detection
            if (config.useShapeAnalysis) {
                // Convert to grayscale
                Mat grayMat = new Mat();
                Imgproc.cvtColor(rgbaMat, grayMat, Imgproc.COLOR_RGB2GRAY);
                
                // Apply Gaussian blur
                Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 0);
                
                // Apply Canny edge detector
                Mat edges = new Mat();
                Imgproc.Canny(grayMat, edges, 50, 150);
                
                // Find contours
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(edges, contours, hierarchy, 
                        Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                
                // Filter contours by shape characteristics
                for (MatOfPoint contour : contours) {
                    double area = Imgproc.contourArea(contour);
                    
                    // Minimum size check
                    if (area > minEnemySize * minEnemySize) {
                        // Convert to MatOfPoint2f for more accurate analysis
                        MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
                        
                        // Check shape characteristics
                        double perimeter = Imgproc.arcLength(contour2f, true);
                        double circularity = 4 * Math.PI * area / (perimeter * perimeter);
                        
                        // Rectangular check
                        org.opencv.core.Rect rect = Imgproc.boundingRect(contour);
                        double rectangularity = area / (rect.width * rect.height);
                        
                        // Convert to Android rect
                        Rect androidRect = new Rect(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
                        
                        // Human figures in FPS games often have specific shape characteristics
                        // Most human figures have moderate circularity and rectangularity
                        if ((circularity > 0.2 && circularity < 0.8) && 
                            (rectangularity > 0.3 && rectangularity < 0.9) &&
                            validateEnemyCandidate(screenshot, androidRect, config)) {
                            
                            results.add(androidRect);
                        }
                        
                        contour2f.release();
                    }
                    
                    contour.release();
                }
                
                // Clean up
                grayMat.release();
                edges.release();
                hierarchy.release();
            }
            
            // Process for edge detection
            if (config.useEdgeDetection) {
                // Convert to HSV for better color segmentation
                Mat hsvMat = new Mat();
                Imgproc.cvtColor(rgbaMat, hsvMat, Imgproc.COLOR_RGB2HSV);
                
                // Create mask for common enemy colors/highlights
                Mat colorMask = new Mat();
                
                // Create a mask for common enemy colors
                Core.inRange(hsvMat, 
                        new Scalar(0, 100, 100),   // Lower bound for reddish colors
                        new Scalar(10, 255, 255),  // Upper bound for reddish colors
                        colorMask);
                
                // Also detect higher-red spectrum (wraps around in HSV)
                Mat colorMask2 = new Mat();
                Core.inRange(hsvMat, 
                        new Scalar(170, 100, 100), // Lower bound for reddish colors
                        new Scalar(180, 255, 255), // Upper bound for reddish colors
                        colorMask2);
                
                // Combine masks
                Core.bitwise_or(colorMask, colorMask2, colorMask);
                
                // Find contours in the mask
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(colorMask, contours, hierarchy, 
                        Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
                
                // Process contours
                for (MatOfPoint contour : contours) {
                    // Check size
                    if (Imgproc.contourArea(contour) > minEnemySize * minEnemySize) {
                        org.opencv.core.Rect rect = Imgproc.boundingRect(contour);
                        Rect androidRect = new Rect(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
                        
                        if (validateEnemyCandidate(screenshot, androidRect, config)) {
                            results.add(androidRect);
                        }
                    }
                    
                    contour.release();
                }
                
                // Clean up
                hsvMat.release();
                colorMask.release();
                colorMask2.release();
                hierarchy.release();
            }
            
            // Clean up
            rgbaMat.release();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in contour detection", e);
        }
        
        return results;
    }
    
    /**
     * Check if a color is likely to represent an enemy based on game-specific config
     */
    private boolean isEnemyColor(int pixel, GameDetectionConfig config) {
        // Extract RGB components
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);
        
        // Check for reddish colors (common enemy indicators)
        boolean isReddish = r > config.redThreshold && 
                r > g * config.redDominanceRatio && 
                r > b * config.redDominanceRatio;
        
        // Check for bright outlines
        boolean isBright = (r + g + b) > 600;
        
        // Check for high contrast
        boolean isHighContrast = Math.max(Math.max(r, g), b) - Math.min(Math.min(r, g), b) > 150;
        
        return isReddish || (isBright && isHighContrast);
    }
    
    /**
     * Expand a color region to find boundaries of a potential enemy
     */
    private Rect expandColorRegion(Bitmap bitmap, int startX, int startY, Rect bounds, GameDetectionConfig config) {
        int left = startX;
        int right = startX;
        int top = startY;
        int bottom = startY;
        
        int startColor = bitmap.getPixel(startX, startY);
        
        // Expand left
        for (int x = startX - 1; x >= bounds.left; x--) {
            if (isColorSimilar(bitmap.getPixel(x, startY), startColor)) {
                left = x;
            } else {
                break;
            }
        }
        
        // Expand right
        for (int x = startX + 1; x < bounds.right; x++) {
            if (isColorSimilar(bitmap.getPixel(x, startY), startColor)) {
                right = x;
            } else {
                break;
            }
        }
        
        // Expand up
        for (int y = startY - 1; y >= bounds.top; y--) {
            if (isColorSimilar(bitmap.getPixel(startX, y), startColor)) {
                top = y;
            } else {
                break;
            }
        }
        
        // Expand down
        for (int y = startY + 1; y < bounds.bottom; y++) {
            if (isColorSimilar(bitmap.getPixel(startX, y), startColor)) {
                bottom = y;
            } else {
                break;
            }
        }
        
        // Create rectangle with some margin
        int margin = 2;
        left = Math.max(bounds.left, left - margin);
        top = Math.max(bounds.top, top - margin);
        right = Math.min(bounds.right, right + margin);
        bottom = Math.min(bounds.bottom, bottom + margin);
        
        return new Rect(left, top, right, bottom);
    }
    
    /**
     * Check if two colors are similar (within tolerance)
     */
    private boolean isColorSimilar(int color1, int color2) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        
        // Calculate Euclidean distance in RGB space
        double distance = Math.sqrt(
                Math.pow(r2 - r1, 2) + 
                Math.pow(g2 - g1, 2) + 
                Math.pow(b2 - b1, 2)
        );
        
        // Consider similar if distance is less than threshold
        return distance < 50;
    }
    
    /**
     * Validate if a rectangle is likely to contain an enemy
     */
    private boolean validateEnemyCandidate(Bitmap bitmap, Rect rect, GameDetectionConfig config) {
        // Size validation
        int imageArea = bitmap.getWidth() * bitmap.getHeight();
        float rectAreaPercent = (rect.width() * rect.height()) / (float) imageArea;
        
        // Check if size is within reasonable bounds for an enemy
        if (rectAreaPercent < config.minBoundingSizePercent || 
            rectAreaPercent > config.maxBoundingSizePercent) {
            return false;
        }
        
        // Check aspect ratio (human figures typically have height > width)
        float aspectRatio = rect.width() / (float) rect.height();
        if (aspectRatio > 1.2 || aspectRatio < 0.2) {
            return false;
        }
        
        // Check color variance in the region (enemies typically have some texture/detail)
        double variance = calculateColorVariance(bitmap, rect);
        if (variance < 50) { // Too uniform is likely UI or background
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate color variance in a region
     */
    private double calculateColorVariance(Bitmap bitmap, Rect rect) {
        List<Integer> redValues = new ArrayList<>();
        List<Integer> greenValues = new ArrayList<>();
        List<Integer> blueValues = new ArrayList<>();
        
        // Sample pixels in the region
        int sampleStepX = Math.max(1, rect.width() / 10);
        int sampleStepY = Math.max(1, rect.height() / 10);
        
        for (int y = rect.top; y < rect.bottom; y += sampleStepY) {
            for (int x = rect.left; x < rect.right; x += sampleStepX) {
                if (x >= 0 && y >= 0 && x < bitmap.getWidth() && y < bitmap.getHeight()) {
                    int pixel = bitmap.getPixel(x, y);
                    redValues.add(Color.red(pixel));
                    greenValues.add(Color.green(pixel));
                    blueValues.add(Color.blue(pixel));
                }
            }
        }
        
        // Calculate variance for each channel
        double redVar = calculateVariance(redValues);
        double greenVar = calculateVariance(greenValues);
        double blueVar = calculateVariance(blueValues);
        
        // Return the maximum variance
        return Math.max(Math.max(redVar, greenVar), blueVar);
    }
    
    /**
     * Calculate variance of a list of values
     */
    private double calculateVariance(List<Integer> values) {
        if (values.isEmpty()) {
            return 0;
        }
        
        // Calculate mean
        double sum = 0;
        for (int value : values) {
            sum += value;
        }
        double mean = sum / values.size();
        
        // Calculate variance
        double squaredDiffSum = 0;
        for (int value : values) {
            squaredDiffSum += Math.pow(value - mean, 2);
        }
        
        return squaredDiffSum / values.size();
    }
    
    /**
     * Merge overlapping rectangles to avoid duplicate detections
     */
    private List<Rect> mergeOverlappingRects(List<Rect> rects) {
        List<Rect> merged = new ArrayList<>();
        
        for (Rect rect : rects) {
            boolean shouldAdd = true;
            
            for (int i = 0; i < merged.size(); i++) {
                Rect existing = merged.get(i);
                
                if (Rect.intersects(rect, existing)) {
                    // Calculate overlap percentage
                    Rect intersection = new Rect();
                    intersection.setIntersect(rect, existing);
                    
                    float intersectionArea = intersection.width() * intersection.height();
                    float rectArea = rect.width() * rect.height();
                    float existingArea = existing.width() * existing.height();
                    
                    // If significant overlap, merge them
                    if (intersectionArea > 0.3 * Math.min(rectArea, existingArea)) {
                        merged.set(i, new Rect(
                                Math.min(rect.left, existing.left),
                                Math.min(rect.top, existing.top),
                                Math.max(rect.right, existing.right),
                                Math.max(rect.bottom, existing.bottom)
                        ));
                        
                        shouldAdd = false;
                        break;
                    }
                }
            }
            
            if (shouldAdd) {
                merged.add(rect);
            }
        }
        
        return merged;
    }
    
    /**
     * Merge new detections with existing ones, avoiding duplicates
     */
    private void mergeDetections(List<Rect> existingList, List<Rect> newList) {
        for (Rect newRect : newList) {
            boolean isDuplicate = false;
            
            for (Rect existingRect : existingList) {
                if (Rect.intersects(newRect, existingRect)) {
                    // Calculate overlap percentage
                    Rect intersection = new Rect();
                    intersection.setIntersect(newRect, existingRect);
                    
                    float intersectionArea = intersection.width() * intersection.height();
                    float newArea = newRect.width() * newRect.height();
                    float existingArea = existingRect.width() * existingRect.height();
                    
                    // If significant overlap, consider it a duplicate
                    if (intersectionArea > 0.3 * Math.min(newArea, existingArea)) {
                        isDuplicate = true;
                        break;
                    }
                }
            }
            
            if (!isDuplicate) {
                existingList.add(newRect);
            }
        }
    }
    
    /**
     * Apply additional filtering to detected enemies
     */
    private void filterDetections(List<Rect> detections, Bitmap screenshot, GameDetectionConfig config) {
        // Remove detections that are too small
        detections.removeIf(rect -> 
            rect.width() < minEnemySize || rect.height() < minEnemySize);
        
        // Remove detections in UI areas (usually top and bottom of screen)
        int uiMarginTop = screenshot.getHeight() / 6;
        int uiMarginBottom = screenshot.getHeight() / 6;
        
        detections.removeIf(rect -> 
            rect.top < uiMarginTop || rect.bottom > screenshot.getHeight() - uiMarginBottom);
    }
    
    /**
     * Configuration class for game-specific detection parameters
     */
    private static class GameDetectionConfig {
        // Color detection parameters
        public int redThreshold = 150;
        public float redDominanceRatio = 1.5f;
        
        // Detection methods to use
        public boolean useUniformColorDetection = false;
        public boolean useShapeAnalysis = false;
        public boolean useEdgeDetection = false;
        
        // Size constraints (as percentage of screen)
        public float minBoundingSizePercent = 0.01f;
        public float maxBoundingSizePercent = 0.2f;
        
        // Head targeting offset (percentage from top of bounding box)
        public float headTargetOffsetPercent = 0.2f;
    }
}
