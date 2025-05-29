package com.aiassistant.core.speech;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aiassistant.core.ai.neural.BaseTFLiteModel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Advanced voice emotion analyzer that detects emotional states from voice input.
 * Uses spectral and prosodic features to determine the emotional content of speech.
 */
public class VoiceEmotionAnalyzer {
    private static final String TAG = "VoiceEmotionAnalyzer";
    
    // Audio recording configuration
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT) * 3;
    
    // Emotion detection configuration
    private static final int EMOTION_DETECTION_INTERVAL_MS = 500; // Analyze every 500ms
    private static final int MIN_AUDIO_LEVEL_THRESHOLD = 200; // Minimum audio level to process
    
    // Singleton instance
    private static VoiceEmotionAnalyzer instance;
    
    // Core components
    private Context context;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    // Model components
    private BaseTFLiteModel emotionModel;
    private boolean isModelReady = false;
    
    // Emotion tracking
    private EmotionState currentEmotionState = new EmotionState();
    private List<EmotionListener> listeners = new ArrayList<>();
    
    /**
     * Get singleton instance
     */
    public static synchronized VoiceEmotionAnalyzer getInstance(Context context) {
        if (instance == null) {
            instance = new VoiceEmotionAnalyzer(context);
        }
        return instance;
    }
    
    /**
     * Constructor
     */
    private VoiceEmotionAnalyzer(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize emotion model
        initializeEmotionModel();
    }
    
    /**
     * Initialize the emotion detection model
     */
    private void initializeEmotionModel() {
        // This would use a real model in production
        // For now we'll use a simulated model
        isModelReady = true;
    }
    
    /**
     * Start analyzing voice emotions
     */
    public boolean startAnalysis() {
        if (isRecording) return true;
        
        try {
            audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            );
            
            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed");
                return false;
            }
            
            isRecording = true;
            audioRecord.startRecording();
            
            executorService.execute(this::processAudioStream);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error starting voice emotion analysis: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Process the audio stream to detect emotions
     */
    private void processAudioStream() {
        ByteBuffer audioBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        audioBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
        long lastProcessTime = System.currentTimeMillis();
        
        while (isRecording) {
            audioBuffer.clear();
            int readResult = audioRecord.read(audioBuffer, BUFFER_SIZE);
            
            if (readResult > 0) {
                // Check if it's time to process for emotions
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastProcessTime >= EMOTION_DETECTION_INTERVAL_MS) {
                    // Check if audio level is above threshold
                    if (getAudioLevel(audioBuffer, readResult) > MIN_AUDIO_LEVEL_THRESHOLD) {
                        analyzeEmotion(audioBuffer, readResult);
                    }
                    lastProcessTime = currentTime;
                }
            }
        }
    }
    
    /**
     * Calculate audio level from buffer
     */
    private int getAudioLevel(ByteBuffer buffer, int size) {
        buffer.rewind();
        int sum = 0;
        int readCount = 0;
        
        // Calculate RMS of audio signal
        while (buffer.remaining() >= 2 && readCount < size / 2) {
            short sample = buffer.getShort();
            sum += Math.abs(sample);
            readCount++;
        }
        
        buffer.rewind();
        return readCount > 0 ? sum / readCount : 0;
    }
    
    /**
     * Analyze emotion from audio data
     */
    private void analyzeEmotion(ByteBuffer audioBuffer, int size) {
        if (!isModelReady) return;
        
        try {
            // Extract audio features
            float[] features = extractAudioFeatures(audioBuffer, size);
            
            // Analyze emotions
            EmotionState emotionState = inferEmotions(features);
            
            // Update current emotion state
            updateEmotionState(emotionState);
        } catch (Exception e) {
            Log.e(TAG, "Error analyzing emotion: " + e.getMessage());
        }
    }
    
    /**
     * Extract audio features for emotion analysis
     */
    private float[] extractAudioFeatures(ByteBuffer buffer, int size) {
        // In a real implementation, this would extract MFCC, pitch, energy, etc.
        // For simplicity, we'll create some basic features here
        
        buffer.rewind();
        float[] features = new float[20]; // Sample feature vector
        
        // Calculate basic energy
        float energy = 0;
        int count = 0;
        while (buffer.remaining() >= 2 && count < size / 2) {
            short sample = buffer.getShort();
            energy += (sample * sample);
            count++;
        }
        energy = count > 0 ? energy / count : 0;
        features[0] = energy;
        
        buffer.rewind();
        // Calculate zero crossing rate
        float zcr = 0;
        short lastSample = 0;
        count = 0;
        while (buffer.remaining() >= 2 && count < size / 2) {
            short sample = buffer.getShort();
            if (count > 0) {
                if ((sample >= 0 && lastSample < 0) || (sample < 0 && lastSample >= 0)) {
                    zcr++;
                }
            }
            lastSample = sample;
            count++;
        }
        zcr = count > 0 ? zcr / count : 0;
        features[1] = zcr;
        
        // Other features would be calculated here in a real implementation
        
        return features;
    }
    
    /**
     * Infer emotions from audio features
     */
    private EmotionState inferEmotions(float[] features) {
        // In a real implementation, this would use the TensorFlow Lite model
        // For now, we'll simulate the model output
        
        EmotionState state = new EmotionState();
        
        // Energy (features[0]) often correlates with arousal
        float arousal = normalizeFeature(features[0], 0, 1000000);
        
        // Zero crossing rate (features[1]) can indicate speech properties
        float zcr = normalizeFeature(features[1], 0, 0.5f);
        
        // Simulated emotion detection based on simplified features
        state.arousalLevel = (int)(arousal * 100);
        
        // Simulated valence approximation
        int valence = 50; // Neutral by default
        if (arousal > 0.7f && zcr > 0.6f) {
            // High energy + high ZCR = likely anger/stress
            valence = 30;
            state.primaryEmotion = Emotion.ANGRY;
            state.emotionConfidence = 70;
        } else if (arousal > 0.7f && zcr < 0.4f) {
            // High energy + low ZCR = likely excitement/happiness
            valence = 70;
            state.primaryEmotion = Emotion.HAPPY;
            state.emotionConfidence = 75;
        } else if (arousal < 0.3f && zcr < 0.3f) {
            // Low energy + low ZCR = likely sadness
            valence = 30;
            state.primaryEmotion = Emotion.SAD;
            state.emotionConfidence = 65;
        } else if (arousal < 0.3f && zcr > 0.5f) {
            // Low energy + high ZCR = likely fear
            valence = 20;
            state.primaryEmotion = Emotion.FEARFUL;
            state.emotionConfidence = 60;
        } else {
            // Default to neutral
            state.primaryEmotion = Emotion.NEUTRAL;
            state.emotionConfidence = 50;
        }
        
        state.valenceLevel = valence;
        
        // Calculate secondary emotions
        calculateSecondaryEmotions(state);
        
        return state;
    }
    
    /**
     * Normalize feature to range 0-1
     */
    private float normalizeFeature(float value, float min, float max) {
        float normalized = (value - min) / (max - min);
        return Math.max(0, Math.min(1, normalized));
    }
    
    /**
     * Calculate secondary emotions based on primary emotion
     */
    private void calculateSecondaryEmotions(EmotionState state) {
        Map<Emotion, Integer> emotions = new HashMap<>();
        
        // Add primary emotion
        emotions.put(state.primaryEmotion, state.emotionConfidence);
        
        // Add related emotions with lower confidence
        switch (state.primaryEmotion) {
            case HAPPY:
                emotions.put(Emotion.EXCITED, state.emotionConfidence - 20);
                emotions.put(Emotion.NEUTRAL, 30);
                break;
            case SAD:
                emotions.put(Emotion.FEARFUL, state.emotionConfidence - 30);
                emotions.put(Emotion.NEUTRAL, 40);
                break;
            case ANGRY:
                emotions.put(Emotion.STRESSED, state.emotionConfidence - 10);
                emotions.put(Emotion.FRUSTRATED, state.emotionConfidence - 15);
                break;
            case FEARFUL:
                emotions.put(Emotion.STRESSED, state.emotionConfidence - 20);
                emotions.put(Emotion.SAD, state.emotionConfidence - 30);
                break;
            case NEUTRAL:
                emotions.put(Emotion.CALM, state.emotionConfidence + 10);
                // Add slight chances of other emotions
                emotions.put(Emotion.HAPPY, 20);
                emotions.put(Emotion.SAD, 20);
                break;
            default:
                // Add some neutral component to all emotions
                emotions.put(Emotion.NEUTRAL, 40);
        }
        
        // Set secondary emotions
        state.emotionDistribution = emotions;
    }
    
    /**
     * Update the current emotion state and notify listeners
     */
    private void updateEmotionState(EmotionState newState) {
        // Smooth emotion transitions
        if (currentEmotionState.primaryEmotion != Emotion.UNKNOWN) {
            smoothEmotionTransition(currentEmotionState, newState);
        } else {
            currentEmotionState = newState;
        }
        
        // Notify listeners on main thread
        final EmotionState finalState = new EmotionState(currentEmotionState);
        mainHandler.post(() -> {
            for (EmotionListener listener : listeners) {
                listener.onEmotionDetected(finalState);
            }
        });
    }
    
    /**
     * Smooth transition between emotion states to prevent rapid fluctuations
     */
    private void smoothEmotionTransition(EmotionState current, EmotionState newState) {
        // Simple smoothing - if confidence is not significantly higher, maintain current emotion
        if (newState.emotionConfidence > current.emotionConfidence + 15 ||
            current.primaryEmotion == newState.primaryEmotion) {
            // Significant confidence increase or same emotion - update primary
            current.primaryEmotion = newState.primaryEmotion;
            current.emotionConfidence = newState.emotionConfidence;
        } else {
            // Slight smoothing of confidence
            current.emotionConfidence = (current.emotionConfidence * 2 + newState.emotionConfidence) / 3;
        }
        
        // Smooth arousal and valence
        current.arousalLevel = (current.arousalLevel * 2 + newState.arousalLevel) / 3;
        current.valenceLevel = (current.valenceLevel * 2 + newState.valenceLevel) / 3;
        
        // Merge emotion distributions
        Map<Emotion, Integer> merged = new HashMap<>(current.emotionDistribution);
        for (Map.Entry<Emotion, Integer> entry : newState.emotionDistribution.entrySet()) {
            Emotion emotion = entry.getKey();
            Integer confidence = entry.getValue();
            
            if (merged.containsKey(emotion)) {
                // Weighted average with bias toward current state
                int currentValue = merged.get(emotion);
                merged.put(emotion, (currentValue * 2 + confidence) / 3);
            } else {
                merged.put(emotion, confidence);
            }
        }
        current.emotionDistribution = merged;
    }
    
    /**
     * Stop analyzing voice emotions
     */
    public void stopAnalysis() {
        isRecording = false;
        
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                Log.e(TAG, "Error stopping audio recording: " + e.getMessage());
            }
            audioRecord = null;
        }
    }
    
    /**
     * Register listener for emotion detection events
     */
    public void addEmotionListener(EmotionListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove emotion detection listener
     */
    public void removeEmotionListener(EmotionListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }
    
    /**
     * Check if analyzer is ready
     */
    public boolean isReady() {
        return isModelReady;
    }
    
    /**
     * Get current emotion state
     */
    public EmotionState getCurrentEmotionState() {
        return new EmotionState(currentEmotionState);
    }
    
    /**
     * Release resources
     */
    public void shutdown() {
        stopAnalysis();
        
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        listeners.clear();
    }
    
    /**
     * Voice emotion state
     */
    public static class EmotionState {
        public Emotion primaryEmotion = Emotion.UNKNOWN;
        public int emotionConfidence = 0; // 0-100
        public Map<Emotion, Integer> emotionDistribution = new HashMap<>();
        public int arousalLevel = 50; // 0-100, low to high emotional intensity
        public int valenceLevel = 50; // 0-100, negative to positive emotion
        
        public EmotionState() {
            // Default constructor
        }
        
        public EmotionState(EmotionState other) {
            this.primaryEmotion = other.primaryEmotion;
            this.emotionConfidence = other.emotionConfidence;
            this.emotionDistribution = new HashMap<>(other.emotionDistribution);
            this.arousalLevel = other.arousalLevel;
            this.valenceLevel = other.valenceLevel;
        }
        
        @Override
        public String toString() {
            return "EmotionState{" +
                   "primaryEmotion=" + primaryEmotion +
                   ", confidence=" + emotionConfidence +
                   ", arousal=" + arousalLevel +
                   ", valence=" + valenceLevel +
                   '}';
        }
    }
    
    /**
     * Recognized emotion types
     */
    public enum Emotion {
        UNKNOWN,
        NEUTRAL,
        HAPPY,
        SAD,
        ANGRY,
        FEARFUL,
        DISGUSTED,
        SURPRISED,
        CALM,
        EXCITED,
        STRESSED,
        FRUSTRATED,
        BORED
    }
    
    /**
     * Listener for emotion detection events
     */
    public interface EmotionListener {
        void onEmotionDetected(EmotionState emotionState);
    }
}
