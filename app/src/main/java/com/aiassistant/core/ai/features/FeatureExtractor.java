package com.aiassistant.core.ai.features;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.aiassistant.data.models.DetectedEnemy;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.UIElement;
import com.aiassistant.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Feature extraction system for machine learning
 */
public class FeatureExtractor {
    
    private static final String TAG = "FeatureExtractor";
    
    private final Context context;
    
    /**
     * Constructor
     * 
     * @param context The application context
     */
    public FeatureExtractor(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Extract features from a game state
     * 
     * @param gameState The game state
     * @return The feature vector
     */
    public float[] extractFeatures(GameState gameState) {
        if (gameState == null) {
            return new float[Constants.FEATURE_VECTOR_SIZE];
        }
        
        try {
            return gameState.toFeatureVector();
        } catch (Exception e) {
            Log.e(TAG, "Error extracting features: " + e.getMessage(), e);
            return new float[Constants.FEATURE_VECTOR_SIZE];
        }
    }
    
    /**
     * Extract color histogram features
     * 
     * @param bitmap The bitmap
     * @return The histogram features
     */
    public float[] extractColorHistogram(Bitmap bitmap) {
        if (bitmap == null) {
            return new float[64]; // 4x4x4 color bins (R,G,B)
        }
        
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            
            // Use 4 bins per channel (64 total bins)
            int[][][] histogram = new int[4][4][4];
            
            // Sample pixels (don't process every pixel for performance)
            int sampleStep = Math.max(1, Math.min(width, height) / 100);
            
            for (int y = 0; y < height; y += sampleStep) {
                for (int x = 0; x < width; x += sampleStep) {
                    int pixel = bitmap.getPixel(x, y);
                    
                    // Get channels and quantize to 4 bins
                    int r = (Color.red(pixel) * 4) / 256;
                    int g = (Color.green(pixel) * 4) / 256;
                    int b = (Color.blue(pixel) * 4) / 256;
                    
                    // Clamp values to valid bin indices (0-3)
                    r = Math.min(3, Math.max(0, r));
                    g = Math.min(3, Math.max(0, g));
                    b = Math.min(3, Math.max(0, b));
                    
                    // Increment histogram bin
                    histogram[r][g][b]++;
                }
            }
            
            // Normalize and flatten histogram
            float[] features = new float[64];
            int index = 0;
            
            // Calculate total sampled pixels for normalization
            int totalPixels = ((height + sampleStep - 1) / sampleStep) * 
                              ((width + sampleStep - 1) / sampleStep);
            
            for (int r = 0; r < 4; r++) {
                for (int g = 0; g < 4; g++) {
                    for (int b = 0; b < 4; b++) {
                        features[index++] = (float) histogram[r][g][b] / totalPixels;
                    }
                }
            }
            
            return features;
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting color histogram: " + e.getMessage(), e);
            return new float[64];
        }
    }
    
    /**
     * Extract edge features (simplified edge detection)
     * 
     * @param bitmap The bitmap
     * @return The edge features
     */
    public float[] extractEdgeFeatures(Bitmap bitmap) {
        if (bitmap == null) {
            return new float[8]; // 8 edge direction bins
        }
        
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            
            // Divide image into a 4x4 grid for spatial binning
            int gridSize = 4;
            float[][] edgeStrength = new float[gridSize][gridSize];
            int[][] pixelCount = new int[gridSize][gridSize];
            
            // Sample pixels
            int sampleStep = Math.max(1, Math.min(width, height) / 50);
            
            for (int y = 1; y < height - 1; y += sampleStep) {
                for (int x = 1; x < width - 1; x += sampleStep) {
                    // Compute horizontal and vertical gradients using Sobel-like approach
                    int pixelLeft = bitmap.getPixel(x - 1, y);
                    int pixelRight = bitmap.getPixel(x + 1, y);
                    int pixelTop = bitmap.getPixel(x, y - 1);
                    int pixelBottom = bitmap.getPixel(x, y + 1);
                    
                    // Convert to grayscale and calculate gradients
                    int grayLeft = (Color.red(pixelLeft) + Color.green(pixelLeft) + Color.blue(pixelLeft)) / 3;
                    int grayRight = (Color.red(pixelRight) + Color.green(pixelRight) + Color.blue(pixelRight)) / 3;
                    int grayTop = (Color.red(pixelTop) + Color.green(pixelTop) + Color.blue(pixelTop)) / 3;
                    int grayBottom = (Color.red(pixelBottom) + Color.green(pixelBottom) + Color.blue(pixelBottom)) / 3;
                    
                    int gradientX = grayRight - grayLeft;
                    int gradientY = grayBottom - grayTop;
                    
                    // Calculate edge strength (magnitude of gradient)
                    float edgeMagnitude = (float) Math.sqrt(gradientX * gradientX + gradientY * gradientY);
                    
                    // Map pixel to grid cell
                    int gridX = (x * gridSize) / width;
                    int gridY = (y * gridSize) / height;
                    
                    // Ensure valid grid indices
                    gridX = Math.min(gridSize - 1, Math.max(0, gridX));
                    gridY = Math.min(gridSize - 1, Math.max(0, gridY));
                    
                    // Accumulate edge strength in grid cell
                    edgeStrength[gridY][gridX] += edgeMagnitude;
                    pixelCount[gridY][gridX]++;
                }
            }
            
            // Normalize and flatten to feature vector
            float[] features = new float[gridSize * gridSize];
            int index = 0;
            
            for (int y = 0; y < gridSize; y++) {
                for (int x = 0; x < gridSize; x++) {
                    if (pixelCount[y][x] > 0) {
                        features[index] = edgeStrength[y][x] / (pixelCount[y][x] * 255.0f);
                    }
                    index++;
                }
            }
            
            return features;
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting edge features: " + e.getMessage(), e);
            return new float[16];
        }
    }
    
    /**
     * Extract movement features
     * 
     * @param previousState The previous state
     * @param currentState The current state
     * @return The movement features
     */
    public float[] extractMovementFeatures(GameState previousState, GameState currentState) {
        if (previousState == null || currentState == null) {
            return new float[8]; // 8 movement features
        }
        
        try {
            float[] features = new float[8];
            int index = 0;
            
            // Time delta between states (normalized to [0,1] for typical frame intervals)
            long timeDelta = currentState.getTimestamp() - previousState.getTimestamp();
            features[index++] = Math.min(1.0f, timeDelta / 1000.0f);
            
            // Enemy movement features
            List<DetectedEnemy> prevEnemies = previousState.getEnemies();
            List<DetectedEnemy> currEnemies = currentState.getEnemies();
            
            if (prevEnemies != null && currEnemies != null && 
                !prevEnemies.isEmpty() && !currEnemies.isEmpty()) {
                
                // Calculate average enemy movement vectors
                float avgDx = 0;
                float avgDy = 0;
                int matchCount = 0;
                
                // For simplicity, just match enemies by index if counts are the same
                // In a real implementation, would need to match by ID or position
                if (prevEnemies.size() == currEnemies.size()) {
                    for (int i = 0; i < prevEnemies.size(); i++) {
                        DetectedEnemy prev = prevEnemies.get(i);
                        DetectedEnemy curr = currEnemies.get(i);
                        
                        avgDx += curr.getCenterX() - prev.getCenterX();
                        avgDy += curr.getCenterY() - prev.getCenterY();
                        matchCount++;
                    }
                } else {
                    // Simple heuristic for different counts: match closest enemies
                    for (DetectedEnemy prev : prevEnemies) {
                        DetectedEnemy closest = null;
                        float minDist = Float.MAX_VALUE;
                        
                        for (DetectedEnemy curr : currEnemies) {
                            float dx = curr.getCenterX() - prev.getCenterX();
                            float dy = curr.getCenterY() - prev.getCenterY();
                            float dist = dx * dx + dy * dy;
                            
                            if (dist < minDist) {
                                minDist = dist;
                                closest = curr;
                            }
                        }
                        
                        if (closest != null && minDist < 200 * 200) { // Max 200px movement
                            avgDx += closest.getCenterX() - prev.getCenterX();
                            avgDy += closest.getCenterY() - prev.getCenterY();
                            matchCount++;
                        }
                    }
                }
                
                if (matchCount > 0) {
                    avgDx /= matchCount;
                    avgDy /= matchCount;
                    
                    // Normalize by screen dimensions
                    float normDx = avgDx / currentState.getScreenWidth();
                    float normDy = avgDy / currentState.getScreenHeight();
                    
                    features[index++] = normDx;
                    features[index++] = normDy;
                    features[index++] = (float) Math.sqrt(normDx * normDx + normDy * normDy); // Magnitude
                } else {
                    index += 3; // Skip these features
                }
                
                // Enemy count change
                features[index++] = (float)(currEnemies.size() - prevEnemies.size()) / 10.0f;
            } else {
                index += 4; // Skip these features
            }
            
            // Combat state change
            features[index++] = currentState.isInCombat() ? 
                    (previousState.isInCombat() ? 0.0f : 1.0f) : // 1 = combat started
                    (previousState.isInCombat() ? -1.0f : 0.0f); // -1 = combat ended
            
            // Health change
            features[index++] = (currentState.getPlayerHealth() - previousState.getPlayerHealth()) / 100.0f;
            
            return features;
            
        } catch (Exception e) {
            Log.e(TAG, "Error extracting movement features: " + e.getMessage(), e);
            return new float[8];
        }
    }
    
    /**
     * Create a combined feature vector
     * 
     * @param stateFeatures The state features
     * @param colorFeatures The color features
     * @param edgeFeatures The edge features
     * @param movementFeatures The movement features
     * @return The combined features
     */
    public float[] combineFeatures(float[] stateFeatures, float[] colorFeatures, 
                                  float[] edgeFeatures, float[] movementFeatures) {
        int totalSize = (stateFeatures != null ? stateFeatures.length : 0) +
                        (colorFeatures != null ? colorFeatures.length : 0) +
                        (edgeFeatures != null ? edgeFeatures.length : 0) +
                        (movementFeatures != null ? movementFeatures.length : 0);
        
        float[] combined = new float[totalSize];
        int index = 0;
        
        // Copy state features
        if (stateFeatures != null) {
            System.arraycopy(stateFeatures, 0, combined, index, stateFeatures.length);
            index += stateFeatures.length;
        }
        
        // Copy color features
        if (colorFeatures != null) {
            System.arraycopy(colorFeatures, 0, combined, index, colorFeatures.length);
            index += colorFeatures.length;
        }
        
        // Copy edge features
        if (edgeFeatures != null) {
            System.arraycopy(edgeFeatures, 0, combined, index, edgeFeatures.length);
            index += edgeFeatures.length;
        }
        
        // Copy movement features
        if (movementFeatures != null) {
            System.arraycopy(movementFeatures, 0, combined, index, movementFeatures.length);
        }
        
        return combined;
    }
}
