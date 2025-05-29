package com.aiassistant.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import com.aiassistant.data.models.UIElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for analyzing images and detecting UI elements
 */
public class ImageAnalysisUtils {
    private static final String TAG = "ImageAnalysisUtils";
    
    // Feature vector size for visual features
    private static final int FEATURE_VECTOR_SIZE = 64;
    
    // Detection confidence thresholds
    private static final float BUTTON_DETECTION_THRESHOLD = 0.6f;
    private static final float TEXT_DETECTION_THRESHOLD = 0.7f;
    private static final float ENEMY_DETECTION_THRESHOLD = 0.5f;
    
    /**
     * Extract UI elements from a screenshot
     * @param screenshot Screenshot bitmap
     * @return List of detected UI elements
     */
    public static List<UIElement> detectUIElements(Bitmap screenshot) {
        List<UIElement> elements = new ArrayList<>();
        
        if (screenshot == null) {
            return elements;
        }
        
        // In a real implementation, this would use ML models to detect UI elements
        // For now, implement a simple heuristic algorithm to detect likely UI elements
        
        try {
            // Get image dimensions
            int width = screenshot.getWidth();
            int height = screenshot.getHeight();
            
            // Detect rectangular shapes that might be buttons or UI containers
            detectRectangularShapes(screenshot, elements);
            
            // Detect text areas
            detectTextAreas(screenshot, elements);
            
            // In a game-focused context, we might also want to detect:
            detectGameElements(screenshot, elements);
            
            Log.d(TAG, "Detected " + elements.size() + " UI elements");
            
        } catch (Exception e) {
            Log.e(TAG, "Error detecting UI elements", e);
        }
        
        return elements;
    }
    
    /**
     * Extract visual features from a screenshot
     * @param screenshot Screenshot bitmap
     * @return Feature vector
     */
    public static float[] extractVisualFeatures(Bitmap screenshot) {
        float[] features = new float[FEATURE_VECTOR_SIZE];
        
        if (screenshot == null) {
            return features;
        }
        
        // In a real implementation, this would use a neural network to extract features
        // For now, implement a simple feature extraction algorithm
        
        try {
            // Get image dimensions
            int width = screenshot.getWidth();
            int height = screenshot.getHeight();
            
            // Resize for consistent feature extraction
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                    screenshot, 8, 8, true);
            
            // Calculate color features
            int index = 0;
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    if (index < FEATURE_VECTOR_SIZE) {
                        int pixel = scaledBitmap.getPixel(x, y);
                        // Use grayscale value as feature
                        float gray = 0.299f * Color.red(pixel) +
                                     0.587f * Color.green(pixel) +
                                     0.114f * Color.blue(pixel);
                        features[index++] = gray / 255.0f; // Normalize to 0-1
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting visual features", e);
        }
        
        return features;
    }
    
    /**
     * Calculate similarity between two feature vectors
     * @param features1 First feature vector
     * @param features2 Second feature vector
     * @return Similarity score (0-1)
     */
    public static float calculateFeatureSimilarity(float[] features1, float[] features2) {
        if (features1 == null || features2 == null ||
                features1.length != features2.length || features1.length == 0) {
            return 0.0f;
        }
        
        // Calculate cosine similarity
        float dotProduct = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;
        
        for (int i = 0; i < features1.length; i++) {
            dotProduct += features1[i] * features2[i];
            norm1 += features1[i] * features1[i];
            norm2 += features2[i] * features2[i];
        }
        
        // Avoid division by zero
        if (norm1 <= 0.0f || norm2 <= 0.0f) {
            return 0.0f;
        }
        
        return dotProduct / (float) Math.sqrt(norm1 * norm2);
    }
    
    /**
     * Detect rectangular shapes that might be buttons or UI containers
     * @param screenshot Screenshot bitmap
     * @param elements List to add detected elements to
     */
    private static void detectRectangularShapes(Bitmap screenshot, List<UIElement> elements) {
        int width = screenshot.getWidth();
        int height = screenshot.getHeight();
        
        // Simple algorithm to find rectangular areas with consistent color
        // This is a placeholder for a more sophisticated detection algorithm
        
        // Divide the image into a grid and check for rectangles
        int gridSize = Math.min(width, height) / 10;
        
        for (int startY = 0; startY < height; startY += gridSize) {
            for (int startX = 0; startX < width; startX += gridSize) {
                int endX = Math.min(startX + gridSize * 2, width);
                int endY = Math.min(startY + gridSize, height);
                
                // Check if this looks like a button or container
                float buttonConfidence = evaluateRectangleAsButton(
                        screenshot, startX, startY, endX, endY);
                
                if (buttonConfidence > BUTTON_DETECTION_THRESHOLD) {
                    // Create a UI element
                    UIElement element = new UIElement();
                    element.setBounds(startX, startY, endX, endY);
                    element.setType(UIElement.TYPE_BUTTON);
                    element.setConfidence(buttonConfidence);
                    element.setClickable(true);
                    
                    elements.add(element);
                }
            }
        }
    }
    
    /**
     * Evaluate if a rectangle might be a button
     * @param screenshot Screenshot bitmap
     * @param left Left coordinate
     * @param top Top coordinate
     * @param right Right coordinate
     * @param bottom Bottom coordinate
     * @return Confidence score (0-1)
     */
    private static float evaluateRectangleAsButton(
            Bitmap screenshot, int left, int top, int right, int bottom) {
        
        // In a real implementation, this would use ML to evaluate button-like characteristics
        // For now, use simple heuristics:
        
        // 1. Check shape (buttons are often wider than tall, but not extremely wide)
        int width = right - left;
        int height = bottom - top;
        float aspectRatio = (float) width / height;
        
        if (aspectRatio < 0.5f || aspectRatio > 5.0f) {
            return 0.1f; // Probably not a button
        }
        
        // 2. Check color consistency (buttons often have a consistent color)
        int centerX = (left + right) / 2;
        int centerY = (top + bottom) / 2;
        
        int centerColor = screenshot.getPixel(centerX, centerY);
        
        // Sample points within the rectangle
        int samplePoints = 10;
        int consistentColorCount = 0;
        
        for (int i = 0; i < samplePoints; i++) {
            int x = left + (int) (Math.random() * width);
            int y = top + (int) (Math.random() * height);
            
            int sampleColor = screenshot.getPixel(x, y);
            
            // Check if colors are similar
            if (colorDistance(centerColor, sampleColor) < 50) {
                consistentColorCount++;
            }
        }
        
        float colorConsistency = (float) consistentColorCount / samplePoints;
        
        // 3. Check for contrast with surroundings (buttons often stand out)
        float surroundingContrast = evaluateSurroundingContrast(
                screenshot, left, top, right, bottom);
        
        // Combine factors for final confidence
        return (colorConsistency * 0.5f + surroundingContrast * 0.5f);
    }
    
    /**
     * Evaluate contrast between a rectangle and its surroundings
     * @param screenshot Screenshot bitmap
     * @param left Left coordinate
     * @param top Top coordinate
     * @param right Right coordinate
     * @param bottom Bottom coordinate
     * @return Contrast score (0-1)
     */
    private static float evaluateSurroundingContrast(
            Bitmap screenshot, int left, int top, int right, int bottom) {
        
        int width = screenshot.getWidth();
        int height = screenshot.getHeight();
        
        // Calculate average color inside the rectangle
        int centerX = (left + right) / 2;
        int centerY = (top + bottom) / 2;
        int insideColor = screenshot.getPixel(centerX, centerY);
        
        // Calculate average color outside the rectangle (in surrounding area)
        int outsideLeft = Math.max(0, left - 10);
        int outsideTop = Math.max(0, top - 10);
        int outsideRight = Math.min(width - 1, right + 10);
        int outsideBottom = Math.min(height - 1, bottom + 10);
        
        // Sample points in the surrounding area
        int samplePoints = 10;
        int totalColorDistance = 0;
        
        for (int i = 0; i < samplePoints; i++) {
            // Randomly choose one of the four surrounding edges
            int edge = (int) (Math.random() * 4);
            int x, y;
            
            switch (edge) {
                case 0: // Top edge
                    x = left + (int) (Math.random() * (right - left));
                    y = outsideTop;
                    break;
                case 1: // Right edge
                    x = outsideRight;
                    y = top + (int) (Math.random() * (bottom - top));
                    break;
                case 2: // Bottom edge
                    x = left + (int) (Math.random() * (right - left));
                    y = outsideBottom;
                    break;
                default: // Left edge
                    x = outsideLeft;
                    y = top + (int) (Math.random() * (bottom - top));
                    break;
            }
            
            int outsideColor = screenshot.getPixel(x, y);
            totalColorDistance += colorDistance(insideColor, outsideColor);
        }
        
        // Normalize contrast score (max reasonable distance is 765 - full RGB difference)
        float averageContrast = Math.min(1.0f, totalColorDistance / (float) (samplePoints * 100));
        
        return averageContrast;
    }
    
    /**
     * Calculate distance between two colors
     * @param color1 First color
     * @param color2 Second color
     * @return Color distance
     */
    private static int colorDistance(int color1, int color2) {
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        
        // Simple RGB distance
        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
    }
    
    /**
     * Detect text areas
     * @param screenshot Screenshot bitmap
     * @param elements List to add detected elements to
     */
    private static void detectTextAreas(Bitmap screenshot, List<UIElement> elements) {
        // In a real implementation, this would use OCR or ML text detection
        // For now, just add simulated text elements
        
        int width = screenshot.getWidth();
        int height = screenshot.getHeight();
        
        // Look for areas that might contain text based on color patterns
        int gridSize = Math.min(width, height) / 15;
        
        for (int startY = 0; startY < height; startY += gridSize) {
            for (int startX = 0; startX < width; startX += gridSize * 3) {
                int endX = Math.min(startX + gridSize * 3, width);
                int endY = Math.min(startY + gridSize, height);
                
                // Check if this looks like text
                float textConfidence = evaluateAreaAsText(
                        screenshot, startX, startY, endX, endY);
                
                if (textConfidence > TEXT_DETECTION_THRESHOLD) {
                    // Create a UI element
                    UIElement element = new UIElement();
                    element.setBounds(startX, startY, endX, endY);
                    element.setType(UIElement.TYPE_TEXT);
                    element.setConfidence(textConfidence);
                    element.setClickable(false);
                    
                    elements.add(element);
                }
            }
        }
    }
    
    /**
     * Evaluate if an area might contain text
     * @param screenshot Screenshot bitmap
     * @param left Left coordinate
     * @param top Top coordinate
     * @param right Right coordinate
     * @param bottom Bottom coordinate
     * @return Confidence score (0-1)
     */
    private static float evaluateAreaAsText(Bitmap screenshot, int left, int top, int right, int bottom) {
        // In a real implementation, this would use OCR or text detection ML
        // For now, use a simple heuristic based on horizontal edges
        
        // Text typically has many horizontal edges (due to lines of text)
        int width = right - left;
        int height = bottom - top;
        int horizontalEdgeCount = 0;
        
        // Sample horizontal lines to detect edges
        int sampleLines = Math.min(10, height);
        int samplePointsPerLine = 10;
        
        for (int i = 0; i < sampleLines; i++) {
            int y = top + (i + 1) * height / (sampleLines + 1);
            int edgesInLine = 0;
            int lastColor = -1;
            
            for (int j = 0; j < samplePointsPerLine; j++) {
                int x = left + (j + 1) * width / (samplePointsPerLine + 1);
                int color = screenshot.getPixel(x, y);
                
                // If there's a significant color change, count it as an edge
                if (lastColor != -1 && colorDistance(color, lastColor) > 30) {
                    edgesInLine++;
                }
                
                lastColor = color;
            }
            
            // If a line has several edges, it might be a line of text
            if (edgesInLine >= 3) {
                horizontalEdgeCount++;
            }
        }
        
        // Text should have multiple lines with edges
        return Math.min(1.0f, horizontalEdgeCount / (float) sampleLines);
    }
    
    /**
     * Detect game-specific elements like enemies, items, etc.
     * @param screenshot Screenshot bitmap
     * @param elements List to add detected elements to
     */
    private static void detectGameElements(Bitmap screenshot, List<UIElement> elements) {
        // In a real implementation, this would use game-specific ML detection
        // For now, add a few simulated game elements
        
        int width = screenshot.getWidth();
        int height = screenshot.getHeight();
        
        // Try to detect enemies (as objects that might be different from the background)
        int gridSize = Math.min(width, height) / 8;
        
        for (int startY = gridSize; startY < height - gridSize; startY += gridSize) {
            for (int startX = gridSize; startX < width - gridSize; startX += gridSize) {
                int endX = Math.min(startX + gridSize, width);
                int endY = Math.min(startY + gridSize, height);
                
                // Check if this might be an enemy
                float enemyConfidence = evaluateAreaAsEnemy(
                        screenshot, startX, startY, endX, endY);
                
                if (enemyConfidence > ENEMY_DETECTION_THRESHOLD) {
                    // Create a UI element
                    UIElement element = new UIElement();
                    element.setBounds(startX, startY, endX, endY);
                    element.setType(UIElement.TYPE_ENEMY);
                    element.setConfidence(enemyConfidence);
                    element.setClickable(true); // Enemies are usually interactive
                    
                    elements.add(element);
                }
            }
        }
        
        // Add a simulated player (usually near center-bottom)
        int playerCenterX = width / 2;
        int playerCenterY = (int) (height * 0.7);
        int playerSize = width / 10;
        
        UIElement playerElement = new UIElement();
        playerElement.setBounds(
                playerCenterX - playerSize/2,
                playerCenterY - playerSize/2,
                playerCenterX + playerSize/2,
                playerCenterY + playerSize/2);
        playerElement.setType(UIElement.TYPE_PLAYER);
        playerElement.setConfidence(0.8f);
        playerElement.setClickable(false);
        
        elements.add(playerElement);
    }
    
    /**
     * Evaluate if an area might be an enemy
     * @param screenshot Screenshot bitmap
     * @param left Left coordinate
     * @param top Top coordinate
     * @param right Right coordinate
     * @param bottom Bottom coordinate
     * @return Confidence score (0-1)
     */
    private static float evaluateAreaAsEnemy(Bitmap screenshot, int left, int top, int right, int bottom) {
        // In a real implementation, this would use ML object detection
        // For now, use simple heuristics:
        
        // 1. Enemies often have distinctive colors different from background
        float contrastScore = evaluateSurroundingContrast(screenshot, left, top, right, bottom);
        
        // 2. Enemies often have a consistent internal color or pattern
        float colorConsistency = evaluateColorConsistency(screenshot, left, top, right, bottom);
        
        // 3. In many games, enemies are often in the upper part of the screen
        float positionScore = 1.0f - ((float) top / screenshot.getHeight());
        
        // Combine factors
        return (contrastScore * 0.5f + colorConsistency * 0.3f + positionScore * 0.2f);
    }
    
    /**
     * Evaluate color consistency within an area
     * @param screenshot Screenshot bitmap
     * @param left Left coordinate
     * @param top Top coordinate
     * @param right Right coordinate
     * @param bottom Bottom coordinate
     * @return Consistency score (0-1)
     */
    private static float evaluateColorConsistency(Bitmap screenshot, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        
        int centerX = (left + right) / 2;
        int centerY = (top + bottom) / 2;
        int centerColor = screenshot.getPixel(centerX, centerY);
        
        // Sample points within the rectangle
        int samplePoints = 10;
        int consistentColorCount = 0;
        
        for (int i = 0; i < samplePoints; i++) {
            int x = left + (int) (Math.random() * width);
            int y = top + (int) (Math.random() * height);
            
            int sampleColor = screenshot.getPixel(x, y);
            
            // Check if colors are similar
            if (colorDistance(centerColor, sampleColor) < 50) {
                consistentColorCount++;
            }
        }
        
        return (float) consistentColorCount / samplePoints;
    }
    
    /**
     * Load a bitmap from a file path
     * @param filePath Path to the image file
     * @return Loaded bitmap, or null if loading failed
     */
    public static Bitmap loadBitmapFromFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            Log.e(TAG, "Empty file path provided to loadBitmapFromFile");
            return null;
        }
        
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.canRead()) {
                Log.e(TAG, "File does not exist or cannot be read: " + filePath);
                return null;
            }
            
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            
            return BitmapFactory.decodeFile(filePath, options);
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading bitmap from file: " + filePath, e);
            return null;
        }
    }
    
    /**
     * Calculate difference between two images
     * @param bitmap1 First bitmap
     * @param bitmap2 Second bitmap
     * @return Difference score (0-1), where 0 means identical and 1 means completely different
     */
    public static float calculateImageDifference(Bitmap bitmap1, Bitmap bitmap2) {
        if (bitmap1 == null || bitmap2 == null) {
            return 1.0f; // Maximum difference if either bitmap is null
        }
        
        try {
            // Resize both images to the same dimensions for comparison
            int width = 32;
            int height = 32;
            
            Bitmap scaledBitmap1 = Bitmap.createScaledBitmap(bitmap1, width, height, true);
            Bitmap scaledBitmap2 = Bitmap.createScaledBitmap(bitmap2, width, height, true);
            
            // Calculate pixel-by-pixel difference
            int diffPixelCount = 0;
            int totalPixels = width * height;
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel1 = scaledBitmap1.getPixel(x, y);
                    int pixel2 = scaledBitmap2.getPixel(x, y);
                    
                    int diff = colorDistance(pixel1, pixel2);
                    if (diff > 50) { // Threshold for considering pixels different
                        diffPixelCount++;
                    }
                }
            }
            
            // Return normalized difference score
            return (float) diffPixelCount / totalPixels;
            
        } catch (Exception e) {
            Log.e(TAG, "Error calculating image difference", e);
            return 1.0f; // Return maximum difference on error
        }
    }
    
    /**
     * Extract image features (alias for extractVisualFeatures)
     * @param bitmap Image to extract features from
     * @return Feature vector
     */
    public static float[] extractImageFeatures(Bitmap bitmap) {
        return extractVisualFeatures(bitmap);
    }
}