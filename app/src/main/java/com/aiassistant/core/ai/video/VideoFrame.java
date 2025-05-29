package com.aiassistant.core.ai.video;

import java.util.List;

/**
 * Class representing a video frame with its metadata
 */
public class VideoFrame {
    private byte[] frameData;
    private long timestamp;
    private List<DetectedElement> detectedElements;
    
    /**
     * Represents an element detected in the video frame
     */
    public static class DetectedElement {
        private int type;
        private float confidence;
        private int x;
        private int y;
        private int width;
        private int height;
        private String label;
        
        public DetectedElement(int type, float confidence, int x, int y, int width, int height, String label) {
            this.type = type;
            this.confidence = confidence;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.label = label;
        }
        
        // Getters and setters
        public int getType() { return type; }
        public void setType(int type) { this.type = type; }
        
        public float getConfidence() { return confidence; }
        public void setConfidence(float confidence) { this.confidence = confidence; }
        
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        
        public int getWidth() { return width; }
        public void setWidth(int width) { this.width = width; }
        
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
    
    /**
     * Constructor
     */
    public VideoFrame(byte[] frameData, long timestamp) {
        this.frameData = frameData;
        this.timestamp = timestamp;
    }
    
    /**
     * Constructor with detected elements
     */
    public VideoFrame(byte[] frameData, List<DetectedElement> detectedElements, long timestamp) {
        this.frameData = frameData;
        this.detectedElements = detectedElements;
        this.timestamp = timestamp;
    }
    
    // Getters and setters
    public byte[] getFrameData() { return frameData; }
    public void setFrameData(byte[] frameData) { this.frameData = frameData; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public List<DetectedElement> getDetectedElements() { return detectedElements; }
    public void setDetectedElements(List<DetectedElement> detectedElements) { this.detectedElements = detectedElements; }
}
