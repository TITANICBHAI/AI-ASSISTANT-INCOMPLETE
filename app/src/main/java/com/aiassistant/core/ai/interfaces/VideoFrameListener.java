package com.aiassistant.core.ai.interfaces;

import com.aiassistant.core.ai.video.VideoFrame;
import com.aiassistant.core.ai.video.VideoFrame.DetectedElement;

import java.util.List;

/**
 * Interface for receiving processed video frame notifications
 * @deprecated Use {@link com.aiassistant.core.ai.video.VideoFrameListener} instead
 */
@Deprecated
public interface VideoFrameListener {
    /**
     * Called when a video frame has been processed
     * 
     * @param frameData The processed frame data
     * @param detectedElements The detected elements in the frame
     * @param timestamp The timestamp of the frame
     */
    default void onFrameProcessed(byte[] frameData, List<DetectedElement> detectedElements, long timestamp) {
        // Default implementation forwards to new method
        onFrameCaptured(new VideoFrame(frameData, detectedElements, timestamp));
    }
    
    /**
     * Called when a new frame is captured
     * 
     * @param frame The captured video frame
     */
    void onFrameCaptured(VideoFrame frame);
}
