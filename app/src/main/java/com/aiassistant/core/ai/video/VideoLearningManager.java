package com.aiassistant.core.ai.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.aiassistant.core.ai.model.TFLiteModelManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Manages video analysis for learning gameplay patterns.
 * Extracts frames from gameplay videos, processes them with AI models,
 * and learns patterns over time.
 */
public class VideoLearningManager {
    private static final String TAG = "VideoLearningManager";
    
    private final Context context;
    private final ExecutorService executorService;
    private final Map<String, List<GameplayPattern>> gamePatterns;
    private TFLiteModelManager modelManager;
    
    // Settings
    private int frameExtractionRate = 5; // Extract 1 frame every 5 seconds
    private boolean saveFramesToDisk = false;
    private String frameSaveDirectory = "gameplay_frames";
    
    /**
     * Constructor
     * 
     * @param context Application context
     */
    public VideoLearningManager(Context context) {
        this.context = context;
        this.executorService = Executors.newFixedThreadPool(2);
        this.gamePatterns = new HashMap<>();
    }
    
    /**
     * Set the TensorFlow Lite model manager
     * 
     * @param modelManager The model manager instance
     */
    public void setModelManager(TFLiteModelManager modelManager) {
        this.modelManager = modelManager;
    }
    
    /**
     * Process a gameplay video to learn patterns
     * 
     * @param videoUri URI of the video file
     * @param gameId Identifier of the game
     * @param listener Listener for analysis completion
     * @return Future representing the pending completion of the task
     */
    public Future<?> processGameplayVideo(Uri videoUri, String gameId, VideoAnalysisListener listener) {
        return executorService.submit(() -> {
            try {
                Log.d(TAG, "Starting video analysis for " + gameId);
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(context, videoUri);
                
                // Get video metadata
                String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long durationMs = Long.parseLong(durationStr);
                int durationSec = (int) (durationMs / 1000);
                
                // Extract frames at regular intervals
                List<Bitmap> frames = new ArrayList<>();
                for (int i = 0; i < durationSec; i += frameExtractionRate) {
                    long timeMs = i * 1000L;
                    Bitmap bitmap = retriever.getFrameAtTime(timeMs, MediaMetadataRetriever.OPTION_CLOSEST);
                    if (bitmap != null) {
                        frames.add(bitmap);
                        
                        // Save frames if enabled
                        if (saveFramesToDisk) {
                            saveFrameToDisk(bitmap, gameId, i);
                        }
                    }
                }
                
                retriever.release();
                
                // Process frames and identify patterns
                List<GameplayPattern> patterns = analyzeGameplayFrames(frames, gameId);
                
                // Store patterns
                if (!gamePatterns.containsKey(gameId)) {
                    gamePatterns.put(gameId, new ArrayList<>());
                }
                gamePatterns.get(gameId).addAll(patterns);
                
                // Notify listener
                if (listener != null) {
                    listener.onVideoAnalysisCompleted(gameId, patterns);
                }
                
                Log.d(TAG, "Completed video analysis for " + gameId + ", found " + patterns.size() + " patterns");
            } catch (Exception e) {
                Log.e(TAG, "Error processing gameplay video", e);
                if (listener != null) {
                    listener.onVideoAnalysisError(gameId, e);
                }
            }
        });
    }
    
    /**
     * Analyze extracted frames to identify gameplay patterns
     * 
     * @param frames List of video frames
     * @param gameId Game identifier
     * @return List of identified gameplay patterns
     */
    private List<GameplayPattern> analyzeGameplayFrames(List<Bitmap> frames, String gameId) {
        List<GameplayPattern> patterns = new ArrayList<>();
        
        if (frames.isEmpty() || modelManager == null) {
            return patterns;
        }
        
        // Analyze frame sequences to detect patterns
        for (int i = 0; i < frames.size() - 1; i++) {
            Bitmap currentFrame = frames.get(i);
            
            // Detect UI elements
            float[] uiScores = modelManager.runInference("ui_elements_detector", currentFrame);
            
            // Detect environment
            float[] envScores = modelManager.runInference("environment_detector", currentFrame);
            
            // Detect combat effects
            float[] combatScores = modelManager.runInference("combat_effects_detector", currentFrame);
            
            // Create pattern from multiple detections
            GameplayPattern pattern = new GameplayPattern();
            pattern.setGameId(gameId);
            pattern.setFrameIndex(i);
            pattern.setUiElements(convertScoresToFeatures(uiScores, 0.6f));
            pattern.setEnvironmentFeatures(convertScoresToFeatures(envScores, 0.6f));
            pattern.setCombatFeatures(convertScoresToFeatures(combatScores, 0.6f));
            
            // Check if pattern is significant
            if (pattern.isSignificant()) {
                patterns.add(pattern);
            }
        }
        
        return patterns;
    }
    
    /**
     * Convert model output scores to feature vectors
     * 
     * @param scores Array of confidence scores
     * @param threshold Confidence threshold
     * @return Array representing detected features (1 if detected, 0 otherwise)
     */
    private int[] convertScoresToFeatures(float[] scores, float threshold) {
        int[] features = new int[scores.length];
        for (int i = 0; i < scores.length; i++) {
            features[i] = scores[i] >= threshold ? 1 : 0;
        }
        return features;
    }
    
    /**
     * Save a frame to disk for later analysis or debugging
     * 
     * @param bitmap The frame to save
     * @param gameId Game identifier
     * @param frameIndex Index of the frame in the sequence
     */
    private void saveFrameToDisk(Bitmap bitmap, String gameId, int frameIndex) {
        try {
            File directory = new File(context.getFilesDir(), frameSaveDirectory + "/" + gameId);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            File file = new File(directory, "frame_" + frameIndex + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Error saving frame to disk", e);
        }
    }
    
    /**
     * Get learned patterns for a specific game
     * 
     * @param gameId Game identifier
     * @return List of gameplay patterns
     */
    public List<GameplayPattern> getGamePatterns(String gameId) {
        return gamePatterns.getOrDefault(gameId, new ArrayList<>());
    }
    
    /**
     * Set the frame extraction rate
     * 
     * @param framesPerSecond Number of seconds between extracted frames
     */
    public void setFrameExtractionRate(int framesPerSecond) {
        this.frameExtractionRate = framesPerSecond;
    }
    
    /**
     * Enable or disable saving frames to disk
     * 
     * @param saveFrames true to save frames, false otherwise
     * @param directory Directory to save frames in (relative to app's files dir)
     */
    public void setSaveFramesToDisk(boolean saveFrames, String directory) {
        this.saveFramesToDisk = saveFrames;
        if (directory != null && !directory.isEmpty()) {
            this.frameSaveDirectory = directory;
        }
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        executorService.shutdown();
    }
    
    /**
     * Interface for video analysis callbacks
     */
    public interface VideoAnalysisListener {
        void onVideoAnalysisCompleted(String gameId, List<GameplayPattern> patterns);
        void onVideoAnalysisError(String gameId, Exception e);
    }
    
    /**
     * Class representing a gameplay pattern detected in video
     */
    public static class GameplayPattern {
        private String gameId;
        private int frameIndex;
        private int[] uiElements;
        private int[] environmentFeatures;
        private int[] combatFeatures;
        
        public String getGameId() {
            return gameId;
        }
        
        public void setGameId(String gameId) {
            this.gameId = gameId;
        }
        
        public int getFrameIndex() {
            return frameIndex;
        }
        
        public void setFrameIndex(int frameIndex) {
            this.frameIndex = frameIndex;
        }
        
        public int[] getUiElements() {
            return uiElements;
        }
        
        public void setUiElements(int[] uiElements) {
            this.uiElements = uiElements;
        }
        
        public int[] getEnvironmentFeatures() {
            return environmentFeatures;
        }
        
        public void setEnvironmentFeatures(int[] environmentFeatures) {
            this.environmentFeatures = environmentFeatures;
        }
        
        public int[] getCombatFeatures() {
            return combatFeatures;
        }
        
        public void setCombatFeatures(int[] combatFeatures) {
            this.combatFeatures = combatFeatures;
        }
        
        /**
         * Check if the pattern contains significant features
         * 
         * @return true if the pattern is significant, false otherwise
         */
        public boolean isSignificant() {
            int significantFeatures = 0;
            
            if (uiElements != null) {
                for (int feature : uiElements) {
                    significantFeatures += feature;
                }
            }
            
            if (environmentFeatures != null) {
                for (int feature : environmentFeatures) {
                    significantFeatures += feature;
                }
            }
            
            if (combatFeatures != null) {
                for (int feature : combatFeatures) {
                    significantFeatures += feature;
                }
            }
            
            // Pattern is significant if it has at least 3 detected features
            return significantFeatures >= 3;
        }
        
        /**
         * Convert the pattern to a feature vector for ML algorithms
         * 
         * @return Combined feature vector
         */
        public float[] toFeatureVector() {
            int uiSize = uiElements != null ? uiElements.length : 0;
            int envSize = environmentFeatures != null ? environmentFeatures.length : 0;
            int combatSize = combatFeatures != null ? combatFeatures.length : 0;
            
            float[] featureVector = new float[uiSize + envSize + combatSize];
            
            int index = 0;
            if (uiElements != null) {
                for (int feature : uiElements) {
                    featureVector[index++] = feature;
                }
            }
            
            if (environmentFeatures != null) {
                for (int feature : environmentFeatures) {
                    featureVector[index++] = feature;
                }
            }
            
            if (combatFeatures != null) {
                for (int feature : combatFeatures) {
                    featureVector[index++] = feature;
                }
            }
            
            return featureVector;
        }
    }
}
