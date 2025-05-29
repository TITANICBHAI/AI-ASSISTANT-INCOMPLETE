package com.aiassistant.core.voice.analysis;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aiassistant.core.security.AccessControl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Analyzes behavioral voice patterns for authentication and anomaly detection
 */
public class BehavioralVoiceAnalyzer {
    private static final String TAG = "BehavioralVoice";
    
    // Analysis thresholds
    private static final float ANOMALY_THRESHOLD = 0.70f;
    private static final float STRESS_THRESHOLD = 0.65f;
    private static final float IDENTITY_THRESHOLD = 0.80f;
    
    // Max behavioral patterns to store
    private static final int MAX_PATTERNS = 10;
    
    private Context context;
    private AccessControl accessControl;
    private File profileDir;
    private Executor backgroundExecutor;
    private Handler mainHandler;
    private List<BehavioralAnalysisListener> listeners;
    private Map<String, List<float[]>> userPatterns;
    private boolean initialized;
    
    /**
     * Constructor
     * @param context Application context
     * @param accessControl Access control for security checks
     */
    public BehavioralVoiceAnalyzer(Context context, AccessControl accessControl) {
        this.context = context;
        this.accessControl = accessControl;
        this.profileDir = new File(context.getFilesDir(), "behavioral_profiles");
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.listeners = new ArrayList<>();
        this.userPatterns = new HashMap<>();
        this.initialized = false;
        
        // Create profile directory if it doesn't exist
        if (!profileDir.exists()) {
            profileDir.mkdirs();
        }
    }
    
    /**
     * Initialize the analyzer
     * @return True if initialization was successful
     */
    public boolean initialize() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.ADMIN)) {
            Log.e(TAG, "Access denied for initializing behavioral voice analyzer");
            return false;
        }
        
        Log.d(TAG, "Initializing behavioral voice analyzer");
        
        try {
            // Look for existing profiles
            File[] profileFiles = profileDir.listFiles((dir, name) -> name.endsWith(".profile"));
            
            if (profileFiles != null) {
                for (File profileFile : profileFiles) {
                    String userId = profileFile.getName().replace(".profile", "");
                    loadUserProfile(userId);
                }
                
                Log.d(TAG, "Loaded " + userPatterns.size() + " user profiles");
            }
            
            initialized = true;
            Log.d(TAG, "Behavioral voice analyzer initialized successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing behavioral voice analyzer", e);
            return false;
        }
    }
    
    /**
     * Load user profile
     * @param userId User ID
     * @return True if loaded successfully
     */
    private boolean loadUserProfile(String userId) {
        try {
            File profileFile = new File(profileDir, userId + ".profile");
            
            if (!profileFile.exists()) {
                Log.d(TAG, "Profile file not found for user: " + userId);
                return false;
            }
            
            byte[] fileData = new byte[(int) profileFile.length()];
            
            try (FileInputStream fis = new FileInputStream(profileFile)) {
                // Read data
                int bytesRead = fis.read(fileData);
                if (bytesRead != fileData.length) {
                    Log.e(TAG, "Error reading profile file");
                    return false;
                }
                
                // Parse data
                ByteBuffer buffer = ByteBuffer.wrap(fileData).order(ByteOrder.LITTLE_ENDIAN);
                
                // Read pattern count
                int patternCount = buffer.getInt();
                
                // Read each pattern
                List<float[]> patterns = new ArrayList<>();
                
                for (int i = 0; i < patternCount; i++) {
                    // Read feature count
                    int featureCount = buffer.getInt();
                    
                    // Read features
                    float[] features = new float[featureCount];
                    for (int j = 0; j < featureCount; j++) {
                        features[j] = buffer.getFloat();
                    }
                    
                    patterns.add(features);
                }
                
                // Store patterns
                userPatterns.put(userId, patterns);
                
                Log.d(TAG, "Loaded " + patternCount + " behavioral patterns for user: " + userId);
                
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user profile: " + userId, e);
            return false;
        }
    }
    
    /**
     * Save user profile
     * @param userId User ID
     * @return True if saved successfully
     */
    private boolean saveUserProfile(String userId) {
        try {
            List<float[]> patterns = userPatterns.get(userId);
            
            if (patterns == null || patterns.isEmpty()) {
                Log.e(TAG, "No patterns to save for user: " + userId);
                return false;
            }
            
            File profileFile = new File(profileDir, userId + ".profile");
            
            try (FileOutputStream fos = new FileOutputStream(profileFile)) {
                // Calculate total buffer size
                int patternCount = patterns.size();
                int totalFeatureCount = 0;
                
                for (float[] pattern : patterns) {
                    totalFeatureCount += pattern.length;
                }
                
                // Allocate buffer
                int bufferSize = 4 + (4 + 4 * patterns.get(0).length) * patternCount;
                ByteBuffer buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.LITTLE_ENDIAN);
                
                // Write pattern count
                buffer.putInt(patternCount);
                
                // Write each pattern
                for (float[] pattern : patterns) {
                    // Write feature count
                    buffer.putInt(pattern.length);
                    
                    // Write features
                    for (float feature : pattern) {
                        buffer.putFloat(feature);
                    }
                }
                
                // Write buffer to file
                fos.write(buffer.array());
            }
            
            Log.d(TAG, "Saved " + patterns.size() + " behavioral patterns for user: " + userId);
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving user profile: " + userId, e);
            return false;
        }
    }
    
    /**
     * Add behavioral pattern for user
     * @param userId User ID
     * @param audioData Audio data to extract pattern from
     * @param listener Listener for the operation
     */
    public void addUserPattern(String userId, byte[] audioData, final PatternListener listener) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.WRITE)) {
            if (listener != null) {
                listener.onPatternFailed("Access denied for adding behavioral pattern");
            }
            return;
        }
        
        Log.d(TAG, "Adding behavioral pattern for user: " + userId);
        
        backgroundExecutor.execute(() -> {
            try {
                // Extract behavioral features
                float[] features = extractBehavioralFeatures(audioData);
                
                if (features == null || features.length == 0) {
                    if (listener != null) {
                        mainHandler.post(() -> listener.onPatternFailed("Failed to extract behavioral features"));
                    }
                    return;
                }
                
                // Get or create pattern list for user
                List<float[]> patterns = userPatterns.computeIfAbsent(userId, k -> new ArrayList<>());
                
                // Add new pattern
                patterns.add(features);
                
                // Limit pattern count
                while (patterns.size() > MAX_PATTERNS) {
                    patterns.remove(0); // Remove oldest pattern
                }
                
                // Save updated profile
                boolean saved = saveUserProfile(userId);
                
                if (saved) {
                    if (listener != null) {
                        mainHandler.post(() -> listener.onPatternAdded(userId, patterns.size()));
                    }
                } else {
                    if (listener != null) {
                        mainHandler.post(() -> listener.onPatternFailed("Failed to save user profile"));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error adding behavioral pattern", e);
                if (listener != null) {
                    mainHandler.post(() -> listener.onPatternFailed("Error: " + e.getMessage()));
                }
            }
        });
    }
    
    /**
     * Analyze audio for behavioral patterns
     * @param userId User ID to match against
     * @param audioData Audio data to analyze
     * @param listener Listener for analysis results
     */
    public void analyzeAudio(String userId, byte[] audioData, final AnalysisListener listener) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.EXECUTE)) {
            if (listener != null) {
                listener.onAnalysisFailed("Access denied for behavioral voice analysis");
            }
            return;
        }
        
        // Check if user profile exists
        if (!userPatterns.containsKey(userId)) {
            if (listener != null) {
                listener.onAnalysisFailed("No behavioral profile found for user: " + userId);
            }
            return;
        }
        
        Log.d(TAG, "Analyzing audio for behavioral patterns for user: " + userId);
        
        backgroundExecutor.execute(() -> {
            try {
                // Extract behavioral features
                float[] features = extractBehavioralFeatures(audioData);
                
                if (features == null || features.length == 0) {
                    if (listener != null) {
                        mainHandler.post(() -> listener.onAnalysisFailed("Failed to extract behavioral features"));
                    }
                    return;
                }
                
                // Get patterns for user
                List<float[]> patterns = userPatterns.get(userId);
                
                // Calculate behavioral metrics
                BehavioralResult result = analyzeBehavior(features, patterns);
                
                if (listener != null) {
                    mainHandler.post(() -> listener.onAnalysisComplete(result));
                }
                
                // Notify listeners if anomaly detected
                if (result.anomalyScore >= ANOMALY_THRESHOLD) {
                    for (BehavioralAnalysisListener analysisListener : listeners) {
                        mainHandler.post(() -> analysisListener.onBehavioralAnomaly(userId, result));
                    }
                }
                
                // Notify listeners if stress detected
                if (result.stressScore >= STRESS_THRESHOLD) {
                    for (BehavioralAnalysisListener analysisListener : listeners) {
                        mainHandler.post(() -> analysisListener.onStressDetected(userId, result));
                    }
                }
                
                // Notify listeners if identity confirmation failed
                if (result.identityScore < IDENTITY_THRESHOLD) {
                    for (BehavioralAnalysisListener analysisListener : listeners) {
                        mainHandler.post(() -> analysisListener.onIdentityMismatch(userId, result));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing audio", e);
                if (listener != null) {
                    mainHandler.post(() -> listener.onAnalysisFailed("Error: " + e.getMessage()));
                }
            }
        });
    }
    
    /**
     * Extract behavioral features from audio data
     * @param audioData Raw audio data
     * @return Extracted features or null if extraction failed
     */
    private float[] extractBehavioralFeatures(byte[] audioData) {
        try {
            // Convert byte[] to short[] for audio processing
            short[] shortAudio = new short[audioData.length / 2];
            ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortAudio);
            
            // In a real implementation, this would extract specialized behavioral features including:
            // - Speaking rate: speed and rhythm patterns
            // - Pausing patterns: frequency and duration of pauses
            // - Vocal intensity: volume patterns
            // - Vocal stress indicators: jitter and shimmer
            // - Idiolect patterns: characteristic word usage
            
            // For this example, we'll extract simplified features:
            
            // Extract speaking rate features
            float[] speakingRateFeatures = extractSpeakingRateFeatures(shortAudio);
            
            // Extract pausing pattern features
            float[] pausingFeatures = extractPausingFeatures(shortAudio);
            
            // Extract vocal intensity features
            float[] intensityFeatures = extractIntensityFeatures(shortAudio);
            
            // Extract stress indicator features
            float[] stressFeatures = extractStressFeatures(shortAudio);
            
            // Combine all features
            int totalLength = speakingRateFeatures.length + pausingFeatures.length +
                    intensityFeatures.length + stressFeatures.length;
            
            float[] allFeatures = new float[totalLength];
            int offset = 0;
            
            System.arraycopy(speakingRateFeatures, 0, allFeatures, offset, speakingRateFeatures.length);
            offset += speakingRateFeatures.length;
            
            System.arraycopy(pausingFeatures, 0, allFeatures, offset, pausingFeatures.length);
            offset += pausingFeatures.length;
            
            System.arraycopy(intensityFeatures, 0, allFeatures, offset, intensityFeatures.length);
            offset += intensityFeatures.length;
            
            System.arraycopy(stressFeatures, 0, allFeatures, offset, stressFeatures.length);
            
            Log.d(TAG, "Extracted " + allFeatures.length + " behavioral features");
            
            return allFeatures;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting behavioral features", e);
            return null;
        }
    }
    
    /**
     * Extract speaking rate features
     * @param audio Audio data
     * @return Speaking rate features
     */
    private float[] extractSpeakingRateFeatures(short[] audio) {
        // In a real implementation, this would extract detailed speaking rate features
        // For now, create simplified features
        
        // Find energy peaks to estimate syllable-like units
        int frameSize = 160; // 10ms at 16kHz
        int frameCount = audio.length / frameSize;
        float[] frameEnergies = new float[frameCount];
        
        // Calculate frame energies
        for (int i = 0; i < frameCount; i++) {
            float energy = 0;
            int start = i * frameSize;
            int end = Math.min(start + frameSize, audio.length);
            
            for (int j = start; j < end; j++) {
                energy += audio[j] * audio[j];
            }
            
            frameEnergies[i] = energy / (end - start);
        }
        
        // Find energy peaks (simplified syllable detection)
        List<Integer> peaks = new ArrayList<>();
        for (int i = 1; i < frameCount - 1; i++) {
            if (frameEnergies[i] > frameEnergies[i - 1] && 
                frameEnergies[i] > frameEnergies[i + 1] &&
                frameEnergies[i] > 1000) { // Energy threshold
                peaks.add(i);
            }
        }
        
        // Calculate speaking rate features
        float[] features = new float[3];
        
        // Feature 1: Speaking rate (syllables per second)
        float duration = audio.length / 16000.0f; // Duration in seconds
        features[0] = peaks.size() / duration;
        
        // Feature 2: Variance in inter-syllable intervals
        if (peaks.size() >= 2) {
            float[] intervals = new float[peaks.size() - 1];
            for (int i = 0; i < peaks.size() - 1; i++) {
                intervals[i] = peaks.get(i + 1) - peaks.get(i);
            }
            
            float mean = 0;
            for (float interval : intervals) {
                mean += interval;
            }
            mean /= intervals.length;
            
            float variance = 0;
            for (float interval : intervals) {
                float diff = interval - mean;
                variance += diff * diff;
            }
            variance /= intervals.length;
            
            features[1] = variance / mean; // Coefficient of variation
        } else {
            features[1] = 0;
        }
        
        // Feature 3: Speaking burst length (average syllables per utterance)
        features[2] = peaks.size() / Math.max(1, countUtterances(frameEnergies));
        
        return features;
    }
    
    /**
     * Count utterances in audio based on energy
     * @param energies Frame energies
     * @return Number of utterances
     */
    private int countUtterances(float[] energies) {
        // Find silence periods to separate utterances
        float silenceThreshold = 500; // Energy threshold for silence
        int minSilenceFrames = 15; // 150ms silence to separate utterances
        
        int utteranceCount = 0;
        int silenceFrames = 0;
        boolean inUtterance = false;
        
        for (float energy : energies) {
            if (energy < silenceThreshold) {
                silenceFrames++;
                if (inUtterance && silenceFrames >= minSilenceFrames) {
                    inUtterance = false;
                }
            } else {
                if (!inUtterance) {
                    inUtterance = true;
                    utteranceCount++;
                }
                silenceFrames = 0;
            }
        }
        
        return Math.max(1, utteranceCount);
    }
    
    /**
     * Extract pausing features
     * @param audio Audio data
     * @return Pausing features
     */
    private float[] extractPausingFeatures(short[] audio) {
        // In a real implementation, this would extract detailed pausing pattern features
        // For now, create simplified features
        
        // Find silence periods
        int frameSize = 160; // 10ms at 16kHz
        int frameCount = audio.length / frameSize;
        boolean[] silenceFrames = new boolean[frameCount];
        
        // Detect silence frames
        float silenceThreshold = 500; // Energy threshold for silence
        for (int i = 0; i < frameCount; i++) {
            float energy = 0;
            int start = i * frameSize;
            int end = Math.min(start + frameSize, audio.length);
            
            for (int j = start; j < end; j++) {
                energy += audio[j] * audio[j];
            }
            energy /= (end - start);
            
            silenceFrames[i] = energy < silenceThreshold;
        }
        
        // Find pause lengths
        List<Integer> pauseLengths = new ArrayList<>();
        int currentPause = 0;
        
        for (boolean isSilence : silenceFrames) {
            if (isSilence) {
                currentPause++;
            } else if (currentPause > 0) {
                if (currentPause >= 5) { // Minimum 50ms to count as pause
                    pauseLengths.add(currentPause);
                }
                currentPause = 0;
            }
        }
        
        // Add final pause if any
        if (currentPause >= 5) {
            pauseLengths.add(currentPause);
        }
        
        // Calculate pause features
        float[] features = new float[4];
        
        // Feature 1: Pause frequency (pauses per second)
        float duration = audio.length / 16000.0f; // Duration in seconds
        features[0] = pauseLengths.size() / duration;
        
        // Feature 2: Average pause duration (in seconds)
        if (!pauseLengths.isEmpty()) {
            float sum = 0;
            for (int length : pauseLengths) {
                sum += length;
            }
            features[1] = (sum / pauseLengths.size()) * 0.01f; // Convert frames to seconds
        } else {
            features[1] = 0;
        }
        
        // Feature 3: Variance in pause duration
        if (pauseLengths.size() >= 2) {
            float mean = features[1] * 100; // Mean in frames
            float variance = 0;
            for (int length : pauseLengths) {
                float diff = length - mean;
                variance += diff * diff;
            }
            variance /= pauseLengths.size();
            features[2] = variance / mean; // Coefficient of variation
        } else {
            features[2] = 0;
        }
        
        // Feature 4: Silence ratio
        int silenceCount = 0;
        for (boolean isSilence : silenceFrames) {
            if (isSilence) silenceCount++;
        }
        features[3] = (float) silenceCount / frameCount;
        
        return features;
    }
    
    /**
     * Extract vocal intensity features
     * @param audio Audio data
     * @return Vocal intensity features
     */
    private float[] extractIntensityFeatures(short[] audio) {
        // In a real implementation, this would extract detailed intensity features
        // For now, create simplified features
        
        // Calculate frame energies
        int frameSize = 160; // 10ms at 16kHz
        int frameCount = audio.length / frameSize;
        float[] frameEnergies = new float[frameCount];
        
        for (int i = 0; i < frameCount; i++) {
            float energy = 0;
            int start = i * frameSize;
            int end = Math.min(start + frameSize, audio.length);
            
            for (int j = start; j < end; j++) {
                energy += audio[j] * audio[j];
            }
            
            frameEnergies[i] = energy / (end - start);
        }
        
        // Calculate intensity features
        float[] features = new float[3];
        
        // Feature 1: Average energy
        float sum = 0;
        for (float energy : frameEnergies) {
            sum += energy;
        }
        features[0] = sum / frameCount;
        
        // Feature 2: Energy variance
        float mean = features[0];
        float variance = 0;
        for (float energy : frameEnergies) {
            float diff = energy - mean;
            variance += diff * diff;
        }
        variance /= frameCount;
        features[1] = variance / mean; // Coefficient of variation
        
        // Feature 3: Energy range (max/min ratio)
        float min = Float.MAX_VALUE;
        float max = 0;
        for (float energy : frameEnergies) {
            if (energy > 0) { // Avoid silence frames
                min = Math.min(min, energy);
                max = Math.max(max, energy);
            }
        }
        features[2] = min > 0 ? max / min : 0;
        
        return features;
    }
    
    /**
     * Extract stress indicator features
     * @param audio Audio data
     * @return Stress indicator features
     */
    private float[] extractStressFeatures(short[] audio) {
        // In a real implementation, this would extract detailed stress features
        // including jitter, shimmer, and other vocal stress indicators
        // For now, create simplified features
        
        // Extract pitch-like information
        int frameSize = 320; // 20ms at 16kHz
        int frameCount = audio.length / frameSize;
        float[] framePitches = new float[frameCount];
        
        for (int i = 0; i < frameCount; i++) {
            int start = i * frameSize;
            int end = Math.min(start + frameSize, audio.length);
            
            framePitches[i] = estimateFramePitch(audio, start, end);
        }
        
        // Calculate stress features
        float[] features = new float[4];
        
        // Feature 1: Pitch variation (jitter proxy)
        float pitchSum = 0;
        int pitchCount = 0;
        for (float pitch : framePitches) {
            if (pitch > 0) { // Only count valid pitch frames
                pitchSum += pitch;
                pitchCount++;
            }
        }
        
        if (pitchCount >= 2) {
            float meanPitch = pitchSum / pitchCount;
            float pitchVariance = 0;
            
            for (float pitch : framePitches) {
                if (pitch > 0) {
                    float diff = pitch - meanPitch;
                    pitchVariance += diff * diff;
                }
            }
            pitchVariance /= pitchCount;
            
            features[0] = (float) Math.sqrt(pitchVariance) / meanPitch; // Normalized standard deviation
        } else {
            features[0] = 0;
        }
        
        // Feature 2: Energy fluctuation (shimmer proxy)
        int frameSize2 = 160; // 10ms at 16kHz
        int frameCount2 = audio.length / frameSize2;
        float[] frameEnergies = new float[frameCount2];
        
        for (int i = 0; i < frameCount2; i++) {
            float energy = 0;
            int start = i * frameSize2;
            int end = Math.min(start + frameSize2, audio.length);
            
            for (int j = start; j < end; j++) {
                energy += audio[j] * audio[j];
            }
            
            frameEnergies[i] = energy / (end - start);
        }
        
        float shimmer = 0;
        int shimmerCount = 0;
        
        for (int i = 1; i < frameCount2; i++) {
            if (frameEnergies[i] > 1000 && frameEnergies[i-1] > 1000) { // Only voiced frames
                float diff = Math.abs(frameEnergies[i] - frameEnergies[i-1]);
                shimmer += diff / Math.max(frameEnergies[i], frameEnergies[i-1]);
                shimmerCount++;
            }
        }
        
        features[1] = shimmerCount > 0 ? shimmer / shimmerCount : 0;
        
        // Feature 3: High-frequency energy ratio (stress indicator)
        float lowEnergy = 0;
        float highEnergy = 0;
        
        for (int i = 0; i < audio.length; i++) {
            // Simple high-pass filtering
            if (i > 0 && i < audio.length - 1) {
                float highComponent = (audio[i] - 0.5f * audio[i-1] - 0.5f * audio[i+1]);
                highEnergy += highComponent * highComponent;
                lowEnergy += audio[i] * audio[i] - highComponent * highComponent;
            }
        }
        
        features[2] = (lowEnergy > 0) ? highEnergy / lowEnergy : 0;
        
        // Feature 4: Speaking rate variation (stress indicator)
        features[3] = features[0] * 2 + features[1]; // Combined stress metric
        
        return features;
    }
    
    /**
     * Estimate pitch for a frame of audio
     * @param audio Full audio data
     * @param start Start index
     * @param end End index
     * @return Estimated pitch or 0 if unvoiced
     */
    private float estimateFramePitch(short[] audio, int start, int end) {
        // In a real implementation, this would use a proper pitch estimation algorithm
        // For simplicity, use a basic autocorrelation approach
        
        int length = end - start;
        if (length < 32) return 0; // Too short
        
        // Check if frame has enough energy to be voiced
        float energy = 0;
        for (int i = start; i < end; i++) {
            energy += audio[i] * audio[i];
        }
        energy /= length;
        
        if (energy < 1000) return 0; // Likely silence
        
        // Calculate autocorrelation
        int maxLag = length / 2;
        float[] autocorr = new float[maxLag];
        
        for (int lag = 0; lag < maxLag; lag++) {
            float sum = 0;
            for (int i = 0; i < length - lag; i++) {
                sum += audio[start + i] * audio[start + i + lag];
            }
            autocorr[lag] = sum / (length - lag);
        }
        
        // Find peak in autocorrelation
        int peakLag = 0;
        float peakValue = autocorr[0] * 0.5f; // Threshold at half of zero-lag value
        
        for (int lag = 10; lag < maxLag; lag++) { // Start at 10 to avoid very high frequencies
            if (autocorr[lag] > peakValue && 
                autocorr[lag] > autocorr[lag-1] && 
                autocorr[lag] > autocorr[lag+1]) {
                peakLag = lag;
                peakValue = autocorr[lag];
                break;
            }
        }
        
        if (peakLag > 0) {
            // Convert lag to frequency
            float pitch = 16000.0f / peakLag; // Sample rate / lag
            return pitch;
        } else {
            return 0; // No clear pitch
        }
    }
    
    /**
     * Analyze behavior based on extracted features
     * @param features Current features
     * @param patterns User's stored patterns
     * @return Behavioral analysis result
     */
    private BehavioralResult analyzeBehavior(float[] features, List<float[]> patterns) {
        // Calculate similarity scores with each stored pattern
        float[] similarityScores = new float[patterns.size()];
        
        for (int i = 0; i < patterns.size(); i++) {
            float[] pattern = patterns.get(i);
            similarityScores[i] = calculatePatternSimilarity(features, pattern);
        }
        
        // Find best and average similarity
        float bestSimilarity = 0;
        float totalSimilarity = 0;
        
        for (float score : similarityScores) {
            bestSimilarity = Math.max(bestSimilarity, score);
            totalSimilarity += score;
        }
        
        float avgSimilarity = totalSimilarity / similarityScores.length;
        
        // Calculate behavioral metrics
        
        // Identity score: how well the voice matches known patterns
        float identityScore = bestSimilarity;
        
        // Anomaly score: how different from normal patterns
        float anomalyScore = 1.0f - avgSimilarity;
        
        // Stress score: based on stress features
        float stressScore = 0;
        if (features.length >= 12) { // Assuming last 4 features are stress features
            stressScore = (features[features.length - 1] + features[features.length - 2]) / 2;
            stressScore = Math.min(1.0f, stressScore); // Cap at 1.0
        }
        
        // Deception score: experimental combination of stress and anomaly
        float deceptionScore = (stressScore * 0.7f + anomalyScore * 0.3f);
        
        // Create result
        BehavioralResult result = new BehavioralResult(
                identityScore, anomalyScore, stressScore, deceptionScore);
        
        Log.d(TAG, "Behavioral analysis: Identity=" + identityScore + 
                ", Anomaly=" + anomalyScore + ", Stress=" + stressScore + 
                ", Deception=" + deceptionScore);
        
        return result;
    }
    
    /**
     * Calculate similarity between feature vectors
     * @param features1 First feature vector
     * @param features2 Second feature vector
     * @return Similarity score (0-1)
     */
    private float calculatePatternSimilarity(float[] features1, float[] features2) {
        // Use the smaller length
        int length = Math.min(features1.length, features2.length);
        
        if (length == 0) {
            return 0.0f;
        }
        
        // Calculate weighted Euclidean distance
        float sumSquaredDiff = 0;
        float[] weights = new float[length];
        
        // Speaking rate features are more important (first 3)
        for (int i = 0; i < Math.min(3, length); i++) {
            weights[i] = 2.0f;
        }
        
        // Pausing features next most important (next 4)
        for (int i = 3; i < Math.min(7, length); i++) {
            weights[i] = 1.5f;
        }
        
        // Stress features also important (last 4)
        for (int i = Math.max(0, length - 4); i < length; i++) {
            weights[i] = 1.5f;
        }
        
        // Remaining features normal weight
        for (int i = 7; i < Math.max(0, length - 4); i++) {
            weights[i] = 1.0f;
        }
        
        // Calculate weighted distance
        for (int i = 0; i < length; i++) {
            float diff = features1[i] - features2[i];
            sumSquaredDiff += weights[i] * diff * diff;
        }
        
        float distance = (float) Math.sqrt(sumSquaredDiff / length);
        
        // Convert distance to similarity score (0-1)
        // Using an exponential falloff function
        float similarity = (float) Math.exp(-distance * 2.0);
        
        return similarity;
    }
    
    /**
     * Clear user profile
     * @param userId User ID
     * @return True if cleared successfully
     */
    public boolean clearUserProfile(String userId) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.WRITE)) {
            return false;
        }
        
        try {
            // Remove from memory
            userPatterns.remove(userId);
            
            // Delete profile file
            File profileFile = new File(profileDir, userId + ".profile");
            boolean deleted = profileFile.delete();
            
            Log.d(TAG, "Cleared behavioral profile for user: " + userId + 
                    (deleted ? " (file deleted)" : " (file not found)"));
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error clearing user profile: " + userId, e);
            return false;
        }
    }
    
    /**
     * Add behavioral analysis listener
     * @param listener Listener to add
     */
    public void addBehavioralAnalysisListener(BehavioralAnalysisListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove behavioral analysis listener
     * @param listener Listener to remove
     */
    public void removeBehavioralAnalysisListener(BehavioralAnalysisListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Verify access to a security zone
     * @param zone Security zone
     * @param level Required permission level
     * @return True if access is allowed
     */
    private boolean verifyAccess(AccessControl.SecurityZone zone, AccessControl.PermissionLevel level) {
        boolean hasAccess = accessControl.checkPermission(zone, level);
        if (!hasAccess) {
            Log.w(TAG, "Access denied to zone " + zone + " with level " + level);
        }
        return hasAccess;
    }
    
    /**
     * Pattern operation listener interface
     */
    public interface PatternListener {
        /**
         * Called when pattern is added successfully
         * @param userId User ID
         * @param patternCount Total pattern count
         */
        void onPatternAdded(String userId, int patternCount);
        
        /**
         * Called when pattern operation fails
         * @param reason Reason for failure
         */
        void onPatternFailed(String reason);
    }
    
    /**
     * Behavioral analysis listener interface
     */
    public interface AnalysisListener {
        /**
         * Called when analysis completes
         * @param result Analysis result
         */
        void onAnalysisComplete(BehavioralResult result);
        
        /**
         * Called when analysis fails
         * @param reason Reason for failure
         */
        void onAnalysisFailed(String reason);
    }
    
    /**
     * Behavioral analysis listener interface
     */
    public interface BehavioralAnalysisListener {
        /**
         * Called when behavioral anomaly is detected
         * @param userId User ID
         * @param result Analysis result
         */
        void onBehavioralAnomaly(String userId, BehavioralResult result);
        
        /**
         * Called when stress is detected
         * @param userId User ID
         * @param result Analysis result
         */
        void onStressDetected(String userId, BehavioralResult result);
        
        /**
         * Called when identity mismatch is detected
         * @param userId User ID
         * @param result Analysis result
         */
        void onIdentityMismatch(String userId, BehavioralResult result);
    }
    
    /**
     * Behavioral analysis result
     */
    public static class BehavioralResult {
        private float identityScore;
        private float anomalyScore;
        private float stressScore;
        private float deceptionScore;
        
        public BehavioralResult(float identityScore, float anomalyScore, 
                              float stressScore, float deceptionScore) {
            this.identityScore = identityScore;
            this.anomalyScore = anomalyScore;
            this.stressScore = stressScore;
            this.deceptionScore = deceptionScore;
        }
        
        /**
         * Get identity score
         * @return Score (0-1)
         */
        public float getIdentityScore() {
            return identityScore;
        }
        
        /**
         * Get anomaly score
         * @return Score (0-1)
         */
        public float getAnomalyScore() {
            return anomalyScore;
        }
        
        /**
         * Get stress score
         * @return Score (0-1)
         */
        public float getStressScore() {
            return stressScore;
        }
        
        /**
         * Get deception score
         * @return Score (0-1)
         */
        public float getDeceptionScore() {
            return deceptionScore;
        }
        
        /**
         * Is identity confirmed
         * @return True if identity score is above threshold
         */
        public boolean isIdentityConfirmed() {
            return identityScore >= IDENTITY_THRESHOLD;
        }
        
        /**
         * Is behavior abnormal
         * @return True if anomaly score is above threshold
         */
        public boolean isAbnormalBehavior() {
            return anomalyScore >= ANOMALY_THRESHOLD;
        }
        
        /**
         * Is user stressed
         * @return True if stress score is above threshold
         */
        public boolean isStressed() {
            return stressScore >= STRESS_THRESHOLD;
        }
        
        /**
         * Get behavioral status description
         * @return Description string
         */
        public String getStatusDescription() {
            StringBuilder sb = new StringBuilder();
            
            if (!isIdentityConfirmed()) {
                sb.append("Identity verification failed. ");
            }
            
            if (isStressed()) {
                sb.append("User appears stressed. ");
            }
            
            if (isAbnormalBehavior()) {
                sb.append("Abnormal behavioral patterns detected. ");
            }
            
            if (sb.length() == 0) {
                sb.append("Normal behavioral patterns detected.");
            }
            
            return sb.toString();
        }
    }
}
