package com.aiassistant.core.ai.video;

import android.graphics.Bitmap;

/**
 * Record of a processed video frame with analysis results.
 * Used for storing frame history for temporal analysis.
 */
public class FrameRecord {
    // Frame thumbnail (reduced size to save memory)
    private final Bitmap thumbnail;
    
    // Analysis results
    private final AnalysisResult result;
    
    // Timestamp when this frame was processed
    private final long timestamp;
    
    /**
     * Constructor
     * @param thumbnail Thumbnail of the frame
     * @param result Analysis results
     */
    public FrameRecord(Bitmap thumbnail, AnalysisResult result) {
        this.thumbnail = thumbnail;
        this.result = result;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get the frame thumbnail
     * @return Bitmap thumbnail
     */
    public Bitmap getThumbnail() {
        return thumbnail;
    }
    
    /**
     * Get the analysis results
     * @return Analysis results
     */
    public AnalysisResult getResult() {
        return result;
    }
    
    /**
     * Get the timestamp when this frame was processed
     * @return Timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Check if this frame has a specific metadata value
     * @param key Metadata key
     * @param value Expected value
     * @return True if the metadata matches
     */
    public boolean hasMetadata(String key, String value) {
        if (result == null) {
            return false;
        }
        
        String metadata = result.getMetadata(key);
        return value.equals(metadata);
    }
    
    /**
     * Check if this frame contains a specific UI element type
     * @param elementType UI element type
     * @return True if the element type is present
     */
    public boolean hasElementType(String elementType) {
        if (result == null || result.getUiElements() == null) {
            return false;
        }
        
        for (UIElement element : result.getUiElements()) {
            if (element.getType().equals(elementType)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Calculate age of this frame record
     * @return Age in milliseconds
     */
    public long getAge() {
        return System.currentTimeMillis() - timestamp;
    }
    
    /**
     * Release resources held by this record
     */
    public void recycle() {
        if (thumbnail != null && !thumbnail.isRecycled()) {
            thumbnail.recycle();
        }
    }
}
