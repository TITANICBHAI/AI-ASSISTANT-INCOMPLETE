
package com.aiassistant.core.gaming.vision;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;

import com.aiassistant.core.gaming.GameEntity;
import com.aiassistant.core.gaming.GameState;
import com.aiassistant.utils.ImageAnalysisUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyzer that combines information from multiple sequential frames
 * to improve detection reliability and reconstruct occluded information
 */
public class MultiFrameAnalyzer {
    private static final String TAG = "MultiFrameAnalyzer";
    
    // Settings
    private static final int MAX_SEQUENCE_LENGTH = 10; // Maximum frames to analyze as sequence
    private static final long MAX_FRAME_AGE_MS = 500; // Maximum age of frames to consider (ms)
    
    // Frame sequence
    private final List<FrameData> frameSequence = new ArrayList<>();
    
    // Entity tracking
    private final Map<String, EntityTrackingData> entityTracking = new HashMap<>();
    
    // Feature matching cache
    private final LruCache<String, List<Point>> featurePointCache = new LruCache<>(20);
    
    /**
     * Class to store frame data
     */
    private static class FrameData {
        public final Bitmap bitmap;
        public final GameState gameState;
        public final long timestamp;
        public final Map<String, Object> metadata = new HashMap<>();
        
        public FrameData(Bitmap bitmap, GameState gameState, long timestamp) {
            this.bitmap = bitmap.copy(bitmap.getConfig(), false);
            this.gameState = gameState.copy();
            this.timestamp = timestamp;
        }
        
        public void recycle() {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }
    
    /**
     * Class to store entity tracking data
     */
    private static class EntityTrackingData {
        public String entityId;
        public Rect lastBounds;
        public long lastSeen;
        public float confidence;
        public List<Pair<Long, Rect>> history = new ArrayList<>();
        
        public EntityTrackingData(String entityId, Rect bounds, long timestamp, float confidence) {
            this.entityId = entityId;
            this.lastBounds = bounds;
            this.lastSeen = timestamp;
            this.confidence = confidence;
        }
    }
    
    /**
     * Add frame to sequence
     * @param bitmap Frame bitmap
     * @param gameState Game state
     * @param timestamp Timestamp
     */
    public void addFrame(Bitmap bitmap, GameState gameState, long timestamp) {
        if (bitmap == null || bitmap.isRecycled() || gameState == null) {
            return;
        }
        
        // Add to sequence
        synchronized (frameSequence) {
            // Remove old frames
            long oldestAllowed = timestamp - MAX_FRAME_AGE_MS;
            
            while (!frameSequence.isEmpty() && frameSequence.get(0).timestamp < oldestAllowed) {
                frameSequence.get(0).recycle();
                frameSequence.remove(0);
            }
            
            // Add new frame
            frameSequence.add(new FrameData(bitmap, gameState, timestamp));
            
            // Limit sequence length
            while (frameSequence.size() > MAX_SEQUENCE_LENGTH) {
                frameSequence.get(0).recycle();
                frameSequence.remove(0);
            }
        }
        
        // Update entity tracking
        updateEntityTracking(gameState, timestamp);
    }
    
    /**
     * Analyze frame sequence to detect entities
     * @return Enhanced game state with improved entity detection
     */
    public GameState analyzeSequence() {
        synchronized (frameSequence) {
            if (frameSequence.isEmpty()) {
                return null;
            }
            
            // Start with the most recent frame
            FrameData latestFrame = frameSequence.get(frameSequence.size() - 1);
            GameState enhancedState = latestFrame.gameState.copy();
            
            // Enhance with multi-frame information
            enhanceWithMultiFrameInfo(enhancedState);
            
            return enhancedState;
        }
    }
    
    /**
     * Get reconstructed frame with enhanced visibility
     * @return Bitmap with enhanced visibility
     */
    public Bitmap getEnhancedVisibilityFrame() {
        synchronized (frameSequence) {
            if (frameSequence.isEmpty()) {
                return null;
            }
            
            // If only one frame, return it
            if (frameSequence.size() == 1) {
                return frameSequence.get(0).bitmap.copy(frameSequence.get(0).bitmap.getConfig(), false);
            }
            
            // Create enhanced frame from multiple frames
            return createEnhancedVisibilityFrame();
        }
    }
    
    /**
     * Update entity tracking with new game state
     * @param gameState Current game state
     * @param timestamp Timestamp
     */
    private void updateEntityTracking(GameState gameState, long timestamp) {
        // Clean up old tracking data
        long oldestAllowed = timestamp - MAX_FRAME_AGE_MS;
        List<String> toRemove = new ArrayList<>();
        
        for (Map.Entry<String, EntityTrackingData> entry : entityTracking.entrySet()) {
            if (entry.getValue().lastSeen < oldestAllowed) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (String id : toRemove) {
            entityTracking.remove(id);
        }
        
        // Track current entities
        if (gameState.getEntities() != null) {
            for (GameEntity entity : gameState.getEntities()) {
                String entityId = entity.getId();
                Rect bounds = new Rect(
                    entity.getX(),
                    entity.getY(),
                    entity.getX() + entity.getWidth(),
                    entity.getY() + entity.getHeight()
                );
                
                // Update existing tracking
                if (entityTracking.containsKey(entityId)) {
                    EntityTrackingData trackingData = entityTracking.get(entityId);
                    trackingData.lastBounds = bounds;
                    trackingData.lastSeen = timestamp;
                    trackingData.confidence = entity.getConfidence();
                    
                    // Add to history (limit size)
                    trackingData.history.add(new Pair<>(timestamp, bounds));
                    
                    while (trackingData.history.size() > 10) {
                        trackingData.history.remove(0);
                    }
                } else {
                    // Create new tracking
                    EntityTrackingData trackingData = new EntityTrackingData(
                        entityId, bounds, timestamp, entity.getConfidence());
                    trackingData.history.add(new Pair<>(timestamp, bounds));
                    entityTracking.put(entityId, trackingData);
                }
            }
        }
    }
    
    /**
     * Enhance game state with multi-frame information
     * @param gameState Game state to enhance
     */
    private void enhanceWithMultiFrameInfo(GameState gameState) {
        List<GameEntity> enhancedEntities = new ArrayList<>();
        
        // Add current entities
        if (gameState.getEntities() != null) {
            enhancedEntities.addAll(gameState.getEntities());
        }
        
        // Add entities from tracking that aren't in current frame
        long currentTime = System.currentTimeMillis();
        
        for (EntityTrackingData trackingData : entityTracking.values()) {
            // Skip if too old
            if (currentTime - trackingData.lastSeen > 300) {
                continue;
            }
            
            // Check if entity is already in the list
            boolean found = false;
            
            for (GameEntity entity : enhancedEntities) {
                if (entity.getId().equals(trackingData.entityId)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                // Create entity from tracking data
                GameEntity entity = new GameEntity();
                entity.setId(trackingData.entityId);
                entity.setX(trackingData.lastBounds.left);
                entity.setY(trackingData.lastBounds.top);
                entity.setWidth(trackingData.lastBounds.width());
                entity.setHeight(trackingData.lastBounds.height());
                entity.setConfidence(trackingData.confidence * 0.9f); // Slightly lower confidence for reconstructed
                entity.setReconstructed(true);
                
                enhancedEntities.add(entity);
            }
        }
        
        // Update entity list in game state
        gameState.setEntities(enhancedEntities);
    }
    
    /**
     * Create enhanced visibility frame by combining multiple frames
     * @return Enhanced visibility bitmap
     */
    private Bitmap createEnhancedVisibilityFrame() {
        if (frameSequence.isEmpty()) {
            return null;
        }
        
        // Use the latest frame as base
        FrameData latestFrame = frameSequence.get(frameSequence.size() - 1);
        Bitmap baseBitmap = latestFrame.bitmap;
        
        // Create output bitmap
        Bitmap output = Bitmap.createBitmap(
            baseBitmap.getWidth(),
            baseBitmap.getHeight(),
            baseBitmap.getConfig()
        );
        
        // Start with the base frame
        output.eraseColor(0);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output);
        canvas.drawBitmap(baseBitmap, 0, 0, null);
        
        // Enhance visibility by integrating previous frames
        enhanceVisibilityWithPreviousFrames(canvas, output);
        
        return output;
    }
    
    /**
     * Enhance visibility by integrating information from previous frames
     * @param canvas Canvas to draw on
     * @param output Output bitmap
     */
    private void enhanceVisibilityWithPreviousFrames(android.graphics.Canvas canvas, Bitmap output) {
        // Use only a few frames to avoid blurring
        int framesToUse = Math.min(3, frameSequence.size() - 1);
        
        if (framesToUse <= 0) {
            return;
        }
        
        // Get reference frame
        FrameData referenceFrame = frameSequence.get(frameSequence.size() - 1);
        
        // For each previous frame
        for (int i = frameSequence.size() - 2; i >= frameSequence.size() - 1 - framesToUse; i--) {
            FrameData previousFrame = frameSequence.get(i);
            
            // Align previous frame with current frame
            Pair<Matrix, Float> alignmentResult = alignFrames(
                previousFrame.bitmap,
                referenceFrame.bitmap
            );
            
            Matrix alignmentMatrix = alignmentResult.first;
            float alignmentQuality = alignmentResult.second;
            
            // Skip if alignment quality is poor
            if (alignmentQuality < 0.5f) {
                continue;
            }
            
            // Use semi-transparent paint for previous frames
            android.graphics.Paint paint = new android.graphics.Paint();
            paint.setAlpha(80); // ~30% opacity
            
            // Draw aligned previous frame
            canvas.drawBitmap(previousFrame.bitmap, alignmentMatrix, paint);
        }
    }
    
    /**
     * Align two frames using feature matching
     * @param sourceBitmap Source bitmap
     * @param targetBitmap Target bitmap
     * @return Transformation matrix and alignment quality
     */
    private Pair<Matrix, Float> alignFrames(Bitmap sourceBitmap, Bitmap targetBitmap) {
        Matrix transformMatrix = new Matrix();
        float alignmentQuality = 0.0f;
        
        try {
            // Generate hash keys for cache
            String sourceKey = Integer.toString(sourceBitmap.hashCode());
            String targetKey = Integer.toString(targetBitmap.hashCode());
            
            // Get feature points (from cache if available)
            List<Point> sourcePoints = featurePointCache.get(sourceKey);
            List<Point> targetPoints = featurePointCache.get(targetKey);
            
            if (sourcePoints == null) {
                sourcePoints = extractFeaturePoints(sourceBitmap);
                featurePointCache.put(sourceKey, sourcePoints);
            }
            
            if (targetPoints == null) {
                targetPoints = extractFeaturePoints(targetBitmap);
                featurePointCache.put(targetKey, targetPoints);
            }
            
            // Match feature points
            List<Pair<Point, Point>> matches = matchFeaturePoints(sourcePoints, targetPoints, sourceBitmap, targetBitmap);
            
            // Calculate alignment quality
            alignmentQuality = calculateAlignmentQuality(matches, sourcePoints.size(), targetPoints.size());
            
            // If we have enough matches, calculate transformation
            if (matches.size() >= 3) {
                // Calculate transformation using matched points
                transformMatrix = calculateTransformationMatrix(matches);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error aligning frames: " + e.getMessage());
        }
        
        return new Pair<>(transformMatrix, alignmentQuality);
    }
    
    /**
     * Extract feature points from bitmap
     * @param bitmap Bitmap to analyze
     * @return List of feature points
     */
    private List<Point> extractFeaturePoints(Bitmap bitmap) {
        // Simple feature extraction - look for corners and high contrast points
        List<Point> featurePoints = new ArrayList<>();
        
        // Downsample for performance
        int stepSize = Math.max(1, bitmap.getWidth() / 100);
        
        for (int x = stepSize; x < bitmap.getWidth() - stepSize; x += stepSize) {
            for (int y = stepSize; y < bitmap.getHeight() - stepSize; y += stepSize) {
                // Check if this is a feature point (high contrast)
                if (isFeaturePoint(bitmap, x, y, stepSize)) {
                    featurePoints.add(new Point(x, y));
                }
                
                // Limit number of points
                if (featurePoints.size() >= 100) {
                    break;
                }
            }
            
            if (featurePoints.size() >= 100) {
                break;
            }
        }
        
        return featurePoints;
    }
    
    /**
     * Check if point is a feature point
     * @param bitmap Bitmap to check
     * @param x X coordinate
     * @param y Y coordinate
     * @param step Step size
     * @return True if feature point
     */
    private boolean isFeaturePoint(Bitmap bitmap, int x, int y, int step) {
        // Calculate gradient in different directions
        int centerPixel = bitmap.getPixel(x, y);
        int leftPixel = bitmap.getPixel(Math.max(0, x - step), y);
        int rightPixel = bitmap.getPixel(Math.min(bitmap.getWidth() - 1, x + step), y);
        int topPixel = bitmap.getPixel(x, Math.max(0, y - step));
        int bottomPixel = bitmap.getPixel(x, Math.min(bitmap.getHeight() - 1, y + step));
        
        // Calculate color differences
        int diffLeft = colorDifference(centerPixel, leftPixel);
        int diffRight = colorDifference(centerPixel, rightPixel);
        int diffTop = colorDifference(centerPixel, topPixel);
        int diffBottom = colorDifference(centerPixel, bottomPixel);
        
        // Calculate corner score (Harris corner detector simplified)
        int gradientX = (diffRight - diffLeft);
        int gradientY = (diffBottom - diffTop);
        
        int cornerScore = gradientX * gradientX + gradientY * gradientY;
        
        // Check if corner score is high enough
        return cornerScore > 2000;
    }
    
    /**
     * Calculate difference between two colors
     * @param color1 First color
     * @param color2 Second color
     * @return Color difference
     */
    private int colorDifference(int color1, int color2) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
        return diff;
    }
    
    /**
     * Match feature points between two images
     * @param sourcePoints Source points
     * @param targetPoints Target points
     * @param sourceBitmap Source bitmap
     * @param targetBitmap Target bitmap
     * @return List of matched point pairs
     */
    private List<Pair<Point, Point>> matchFeaturePoints(
            List<Point> sourcePoints, List<Point> targetPoints, 
            Bitmap sourceBitmap, Bitmap targetBitmap) {
        
        List<Pair<Point, Point>> matches = new ArrayList<>();
        
        // For each source point, find best match in target
        for (Point sourcePoint : sourcePoints) {
            Point bestMatch = null;
            int bestMatchScore = Integer.MAX_VALUE;
            
            // Extract patch around source point
            int patchSize = 8;
            int[] sourcePatch = extractPatch(sourceBitmap, sourcePoint.x, sourcePoint.y, patchSize);
            
            for (Point targetPoint : targetPoints) {
                // Extract patch around target point
                int[] targetPatch = extractPatch(targetBitmap, targetPoint.x, targetPoint.y, patchSize);
                
                // Calculate patch difference
                int diffScore = calculatePatchDifference(sourcePatch, targetPatch);
                
                // Update best match
                if (diffScore < bestMatchScore && diffScore < 5000) {
                    bestMatch = targetPoint;
                    bestMatchScore = diffScore;
                }
            }
            
            // Add match if found
            if (bestMatch != null) {
                matches.add(new Pair<>(sourcePoint, bestMatch));
            }
            
            // Limit number of matches
            if (matches.size() >= 20) {
                break;
            }
        }
        
        return matches;
    }
    
    /**
     * Extract patch around point
     * @param bitmap Bitmap to extract from
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     * @param patchSize Patch size
     * @return Patch as array of pixels
     */
    private int[] extractPatch(Bitmap bitmap, int centerX, int centerY, int patchSize) {
        int halfSize = patchSize / 2;
        int[] patch = new int[patchSize * patchSize];
        
        for (int y = 0; y < patchSize; y++) {
            for (int x = 0; x < patchSize; x++) {
                int imgX = Math.min(Math.max(centerX - halfSize + x, 0), bitmap.getWidth() - 1);
                int imgY = Math.min(Math.max(centerY - halfSize + y, 0), bitmap.getHeight() - 1);
                
                patch[y * patchSize + x] = bitmap.getPixel(imgX, imgY);
            }
        }
        
        return patch;
    }
    
    /**
     * Calculate difference between two patches
     * @param patch1 First patch
     * @param patch2 Second patch
     * @return Difference score
     */
    private int calculatePatchDifference(int[] patch1, int[] patch2) {
        if (patch1.length != patch2.length) {
            return Integer.MAX_VALUE;
        }
        
        int diffSum = 0;
        
        for (int i = 0; i < patch1.length; i++) {
            diffSum += colorDifference(patch1[i], patch2[i]);
        }
        
        return diffSum;
    }
    
    /**
     * Calculate alignment quality
     * @param matches Matched points
     * @param numSourcePoints Number of source points
     * @param numTargetPoints Number of target points
     * @return Alignment quality (0-1)
     */
    private float calculateAlignmentQuality(List<Pair<Point, Point>> matches, int numSourcePoints, int numTargetPoints) {
        // Calculate match ratio
        float matchRatio = (float) matches.size() / Math.min(numSourcePoints, numTargetPoints);
        
        // Calculate consistency of transformation
        float transformConsistency = 1.0f;
        
        if (matches.size() >= 3) {
            // Calculate average translation
            float avgDx = 0, avgDy = 0;
            
            for (Pair<Point, Point> match : matches) {
                avgDx += match.second.x - match.first.x;
                avgDy += match.second.y - match.first.y;
            }
            
            avgDx /= matches.size();
            avgDy /= matches.size();
            
            // Calculate variance of translation
            float varDx = 0, varDy = 0;
            
            for (Pair<Point, Point> match : matches) {
                float dx = match.second.x - match.first.x;
                float dy = match.second.y - match.first.y;
                
                varDx += (dx - avgDx) * (dx - avgDx);
                varDy += (dy - avgDy) * (dy - avgDy);
            }
            
            varDx /= matches.size();
            varDy /= matches.size();
            
            // Calculate standard deviation
            float stdDev = (float) Math.sqrt(varDx + varDy);
            
            // Convert to consistency (higher stdDev = lower consistency)
            transformConsistency = Math.max(0, 1.0f - stdDev / 100.0f);
        }
        
        // Combine metrics
        return 0.7f * matchRatio + 0.3f * transformConsistency;
    }
    
    /**
     * Calculate transformation matrix from matched points
     * @param matches Matched points
     * @return Transformation matrix
     */
    private Matrix calculateTransformationMatrix(List<Pair<Point, Point>> matches) {
        Matrix matrix = new Matrix();
        
        if (matches.size() < 3) {
            return matrix; // Identity matrix
        }
        
        // For simplicity, use first three matches
        Point[] src = new Point[3];
        Point[] dst = new Point[3];
        
        for (int i = 0; i < 3; i++) {
            src[i] = matches.get(i).first;
            dst[i] = matches.get(i).second;
        }
        
        // Convert to float arrays for setPolyToPoly
        float[] srcPoints = new float[6];
        float[] dstPoints = new float[6];
        
        for (int i = 0; i < 3; i++) {
            srcPoints[i * 2] = src[i].x;
            srcPoints[i * 2 + 1] = src[i].y;
            dstPoints[i * 2] = dst[i].x;
            dstPoints[i * 2 + 1] = dst[i].y;
        }
        
        // Calculate transformation
        matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 3);
        
        return matrix;
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        synchronized (frameSequence) {
            for (FrameData frame : frameSequence) {
                frame.recycle();
            }
            frameSequence.clear();
        }
        
        entityTracking.clear();
        featurePointCache.evictAll();
    }
}
