package com.aiassistant.core.voice.forensics;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aiassistant.core.security.AccessControl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Advanced audio forensics module for analyzing audio for security purposes
 */
public class AudioForensicsAnalyzer {
    private static final String TAG = "AudioForensics";
    
    // Forensic analysis thresholds
    private static final float TAMPERING_THRESHOLD = 0.65f;
    private static final float SPLICING_THRESHOLD = 0.70f;
    private static final float BACKGROUND_ANOMALY_THRESHOLD = 0.60f;
    
    private Context context;
    private AccessControl accessControl;
    private File forensicsDir;
    private File evidenceDir;
    private Executor backgroundExecutor;
    private Handler mainHandler;
    private List<ForensicsListener> listeners;
    private boolean initialized;
    private boolean autoSaveEvidence;
    
    /**
     * Constructor
     * @param context Application context
     * @param accessControl Access control for security checks
     */
    public AudioForensicsAnalyzer(Context context, AccessControl accessControl) {
        this.context = context;
        this.accessControl = accessControl;
        this.forensicsDir = new File(context.getFilesDir(), "audio_forensics");
        this.evidenceDir = new File(forensicsDir, "evidence");
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.listeners = new ArrayList<>();
        this.initialized = false;
        this.autoSaveEvidence = true;
        
        // Create forensics directories if they don't exist
        if (!forensicsDir.exists()) {
            forensicsDir.mkdirs();
        }
        if (!evidenceDir.exists()) {
            evidenceDir.mkdirs();
        }
    }
    
    /**
     * Initialize the analyzer
     * @return True if initialization successful
     */
    public boolean initialize() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.ADMIN)) {
            Log.e(TAG, "Access denied for initializing audio forensics analyzer");
            return false;
        }
        
        Log.d(TAG, "Initializing audio forensics analyzer");
        
        try {
            // No special initialization needed
            initialized = true;
            Log.d(TAG, "Audio forensics analyzer initialized successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing audio forensics analyzer", e);
            return false;
        }
    }
    
    /**
     * Analyze audio for forensic evidence
     * @param audioData Audio data
     * @param userId User ID (optional, for evidence collection)
     * @param listener Listener for analysis results
     */
    public void analyzeAudio(byte[] audioData, String userId, final ForensicAnalysisListener listener) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.EXECUTE)) {
            if (listener != null) {
                listener.onAnalysisFailed("Access denied for audio forensics analysis");
            }
            return;
        }
        
        // Check if initialized
        if (!initialized) {
            if (listener != null) {
                listener.onAnalysisFailed("Audio forensics analyzer not initialized");
            }
            return;
        }
        
        Log.d(TAG, "Starting audio forensics analysis" + 
                (userId != null ? " for user: " + userId : ""));
        
        backgroundExecutor.execute(() -> {
            try {
                // Extract audio features
                AudioFeatures features = extractForensicFeatures(audioData);
                
                if (features == null) {
                    if (listener != null) {
                        mainHandler.post(() -> listener.onAnalysisFailed("Failed to extract forensic features"));
                    }
                    return;
                }
                
                // Perform forensic analysis
                ForensicResult result = performForensicAnalysis(features);
                
                // If any anomalies detected and auto-save is enabled, save evidence
                if (autoSaveEvidence && result.hasAnomalies()) {
                    saveForensicEvidence(audioData, features, result, userId);
                }
                
                // Notify listener
                if (listener != null) {
                    mainHandler.post(() -> listener.onAnalysisComplete(result));
                }
                
                // Notify all listeners if critical anomalies detected
                if (result.hasCriticalAnomalies()) {
                    for (ForensicsListener forensicsListener : listeners) {
                        mainHandler.post(() -> forensicsListener.onForensicAnomalyDetected(result));
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
     * Extract forensic features from audio data
     * @param audioData Raw audio data
     * @return Extracted features or null if extraction failed
     */
    private AudioFeatures extractForensicFeatures(byte[] audioData) {
        try {
            // Convert byte[] to short[] for audio processing
            short[] shortAudio = new short[audioData.length / 2];
            ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortAudio);
            
            // In a real implementation, this would extract specialized forensic features including:
            // - Signal discontinuities: for detecting splicing
            // - Compression artifacts: for detecting recompression
            // - Background noise profile: for detecting environment inconsistency
            // - Spectral analysis: for detecting manipulation
            
            // For this example, we'll extract simplified features:
            
            // Extract signal continuity features
            SignalContinuityFeatures continuityFeatures = extractContinuityFeatures(shortAudio);
            
            // Extract compression artifact features
            CompressionFeatures compressionFeatures = extractCompressionFeatures(shortAudio);
            
            // Extract background noise features
            BackgroundNoiseFeatures noiseFeatures = extractNoiseFeatures(shortAudio);
            
            // Extract spectral manipulation features
            SpectralFeatures spectralFeatures = extractSpectralFeatures(shortAudio);
            
            // Create and return the combined features
            return new AudioFeatures(
                    continuityFeatures,
                    compressionFeatures,
                    noiseFeatures,
                    spectralFeatures);
        } catch (Exception e) {
            Log.e(TAG, "Error extracting forensic features", e);
            return null;
        }
    }
    
    /**
     * Extract signal continuity features
     * @param audio Audio data
     * @return Signal continuity features
     */
    private SignalContinuityFeatures extractContinuityFeatures(short[] audio) {
        SignalContinuityFeatures features = new SignalContinuityFeatures();
        
        // Analyze signal for phase continuity
        int frameSize = 512;
        int hopSize = 256;
        int frameCount = (audio.length - frameSize) / hopSize + 1;
        List<Integer> discontinuityPoints = new ArrayList<>();
        
        // Simple algorithm to detect abrupt changes in phase
        double[] lastPhase = null;
        
        for (int i = 0; i < frameCount; i++) {
            int start = i * hopSize;
            int end = Math.min(start + frameSize, audio.length);
            
            double[] phase = calculatePhase(audio, start, end);
            
            if (lastPhase != null) {
                double phaseDifference = calculatePhaseDifference(lastPhase, phase);
                if (phaseDifference > 2.0) { // Threshold for abrupt phase change
                    discontinuityPoints.add(start);
                    features.phaseContinuityScore = Math.min(features.phaseContinuityScore, 
                            Math.max(0.0f, 1.0f - (float)(phaseDifference / 10.0)));
                }
            }
            
            lastPhase = phase;
        }
        
        features.discontinuityPoints = discontinuityPoints;
        
        // Analyze amplitude envelope for continuity
        float[] envelope = calculateAmplitudeEnvelope(audio);
        float envelopeContinuityScore = calculateEnvelopeContinuity(envelope);
        features.amplitudeContinuityScore = envelopeContinuityScore;
        
        // Overall continuity score
        features.overallContinuityScore = (features.phaseContinuityScore + features.amplitudeContinuityScore) / 2;
        
        return features;
    }
    
    /**
     * Calculate phase of audio segment
     * @param audio Audio data
     * @param start Start index
     * @param end End index
     * @return Phase values
     */
    private double[] calculatePhase(short[] audio, int start, int end) {
        // In a real implementation, this would use FFT to calculate phase
        // For simplicity, use a basic approach
        
        int length = end - start;
        double[] phase = new double[length / 2];
        
        // Simple zero-crossing based phase approximation
        boolean positive = audio[start] >= 0;
        int crossings = 0;
        
        for (int i = start + 1; i < end; i++) {
            boolean newPositive = audio[i] >= 0;
            if (newPositive != positive) {
                // Zero crossing
                positive = newPositive;
                if (crossings < phase.length) {
                    phase[crossings] = i - start;
                }
                crossings++;
            }
        }
        
        return phase;
    }
    
    /**
     * Calculate difference between phase arrays
     * @param phase1 First phase array
     * @param phase2 Second phase array
     * @return Phase difference metric
     */
    private double calculatePhaseDifference(double[] phase1, double[] phase2) {
        int length = Math.min(phase1.length, phase2.length);
        
        if (length == 0) {
            return 0;
        }
        
        // Calculate average difference
        double sumDiff = 0;
        int count = 0;
        
        for (int i = 0; i < length; i++) {
            if (phase1[i] > 0 && phase2[i] > 0) {
                double diff = Math.abs(phase1[i] - phase2[i]) / Math.max(phase1[i], phase2[i]);
                sumDiff += diff;
                count++;
            }
        }
        
        return count > 0 ? sumDiff / count : 0;
    }
    
    /**
     * Calculate amplitude envelope of audio
     * @param audio Audio data
     * @return Amplitude envelope
     */
    private float[] calculateAmplitudeEnvelope(short[] audio) {
        int frameSize = 256;
        int frameCount = (audio.length + frameSize - 1) / frameSize;
        float[] envelope = new float[frameCount];
        
        for (int i = 0; i < frameCount; i++) {
            int start = i * frameSize;
            int end = Math.min(start + frameSize, audio.length);
            
            // Calculate RMS amplitude
            float sumSquared = 0;
            for (int j = start; j < end; j++) {
                sumSquared += audio[j] * audio[j];
            }
            
            envelope[i] = (float) Math.sqrt(sumSquared / (end - start));
        }
        
        return envelope;
    }
    
    /**
     * Calculate envelope continuity score
     * @param envelope Amplitude envelope
     * @return Continuity score (0-1)
     */
    private float calculateEnvelopeContinuity(float[] envelope) {
        if (envelope.length < 3) {
            return 1.0f;
        }
        
        // Calculate average change rate
        float sumChanges = 0;
        for (int i = 1; i < envelope.length; i++) {
            sumChanges += Math.abs(envelope[i] - envelope[i - 1]);
        }
        float avgChange = sumChanges / (envelope.length - 1);
        
        // Find abrupt changes
        int abruptChanges = 0;
        for (int i = 1; i < envelope.length; i++) {
            float change = Math.abs(envelope[i] - envelope[i - 1]);
            if (change > avgChange * 5) { // Threshold for abrupt change
                abruptChanges++;
            }
        }
        
        // Calculate continuity score
        return Math.max(0.0f, 1.0f - abruptChanges / (float) envelope.length * 10);
    }
    
    /**
     * Extract compression artifact features
     * @param audio Audio data
     * @return Compression features
     */
    private CompressionFeatures extractCompressionFeatures(short[] audio) {
        CompressionFeatures features = new CompressionFeatures();
        
        // Calculate high-frequency energy ratio
        // Low ratio often indicates compression
        float highFreqEnergy = 0;
        float totalEnergy = 0;
        
        // Simple high-pass filtering
        for (int i = 2; i < audio.length; i++) {
            float highComp = audio[i] - 0.5f * audio[i-1] - 0.5f * audio[i-2];
            highFreqEnergy += highComp * highComp;
            totalEnergy += audio[i] * audio[i];
        }
        
        features.highFrequencyRatio = totalEnergy > 0 ? highFreqEnergy / totalEnergy : 0;
        
        // Analyze spectral flatness
        // Compressed audio often has flatter spectrum
        float[] spectralFlatness = calculateSpectralFlatness(audio);
        features.spectralFlatness = spectralFlatness[0];
        features.spectralFlatnessVariation = spectralFlatness[1];
        
        // Check for quantization artifacts
        // Compressed audio often shows more rounding to specific values
        float quantizationScore = calculateQuantizationScore(audio);
        features.quantizationScore = quantizationScore;
        
        // Calculate compression likelihood score
        features.compressionLikelihood = 
                (1.0f - features.highFrequencyRatio) * 0.3f + 
                features.spectralFlatness * 0.4f + 
                features.quantizationScore * 0.3f;
        
        return features;
    }
    
    /**
     * Calculate spectral flatness measures
     * @param audio Audio data
     * @return Array with [flatness, variation]
     */
    private float[] calculateSpectralFlatness(short[] audio) {
        int frameSize = 512;
        int frameCount = audio.length / frameSize;
        
        if (frameCount < 2) {
            return new float[] { 0.5f, 0 };
        }
        
        float[] flatnessValues = new float[frameCount];
        
        for (int i = 0; i < frameCount; i++) {
            int start = i * frameSize;
            int end = Math.min(start + frameSize, audio.length);
            
            // Calculate simple spectral flatness using variance
            float mean = 0;
            for (int j = start; j < end; j++) {
                mean += Math.abs(audio[j]);
            }
            mean /= (end - start);
            
            float variance = 0;
            for (int j = start; j < end; j++) {
                float diff = Math.abs(audio[j]) - mean;
                variance += diff * diff;
            }
            variance /= (end - start);
            
            // Normalize to 0-1 range
            flatnessValues[i] = 1.0f - Math.min(1.0f, (float) Math.sqrt(variance) / mean / 2);
        }
        
        // Calculate average flatness
        float avgFlatness = 0;
        for (float value : flatnessValues) {
            avgFlatness += value;
        }
        avgFlatness /= flatnessValues.length;
        
        // Calculate variation in flatness
        float variationSum = 0;
        for (float value : flatnessValues) {
            variationSum += Math.abs(value - avgFlatness);
        }
        float flatnessVariation = variationSum / flatnessValues.length;
        
        return new float[] { avgFlatness, flatnessVariation };
    }
    
    /**
     * Calculate quantization artifact score
     * @param audio Audio data
     * @return Quantization score (0-1)
     */
    private float calculateQuantizationScore(short[] audio) {
        // Create histogram of values
        Map<Short, Integer> histogram = new HashMap<>();
        for (short sample : audio) {
            histogram.put(sample, histogram.getOrDefault(sample, 0) + 1);
        }
        
        // Calculate histogram irregularity
        // Compressed audio often has unusual peaks in histogram
        int[] counts = new int[histogram.size()];
        int index = 0;
        for (int count : histogram.values()) {
            counts[index++] = count;
        }
        
        // Sort counts
        java.util.Arrays.sort(counts);
        
        // Calculate ratio of top 10% counts to total
        int topIndex = Math.max(0, counts.length - counts.length / 10);
        int topSum = 0;
        for (int i = topIndex; i < counts.length; i++) {
            topSum += counts[i];
        }
        
        // Calculate score (higher is more likely to be compressed)
        float totalCounts = audio.length;
        return topSum / totalCounts;
    }
    
    /**
     * Extract background noise features
     * @param audio Audio data
     * @return Background noise features
     */
    private BackgroundNoiseFeatures extractNoiseFeatures(short[] audio) {
        BackgroundNoiseFeatures features = new BackgroundNoiseFeatures();
        
        // Find probable silence regions (low energy regions)
        List<short[]> silenceRegions = findSilenceRegions(audio);
        
        if (silenceRegions.isEmpty()) {
            // No silence regions found, use defaults
            features.noiseLevel = 0.1f;
            features.noiseConsistency = 0.9f;
            features.noiseSpectrumVariation = 0.1f;
            return features;
        }
        
        // Calculate noise level (RMS of silence regions)
        float totalNoiseEnergy = 0;
        for (short[] region : silenceRegions) {
            float regionEnergy = 0;
            for (short sample : region) {
                regionEnergy += sample * sample;
            }
            totalNoiseEnergy += Math.sqrt(regionEnergy / region.length);
        }
        features.noiseLevel = totalNoiseEnergy / silenceRegions.size() / 100; // Normalize
        
        // Calculate noise consistency across regions
        if (silenceRegions.size() >= 2) {
            float[] regionEnergies = new float[silenceRegions.size()];
            for (int i = 0; i < silenceRegions.size(); i++) {
                float energy = 0;
                for (short sample : silenceRegions.get(i)) {
                    energy += sample * sample;
                }
                regionEnergies[i] = (float) Math.sqrt(energy / silenceRegions.get(i).length);
            }
            
            // Calculate variance in noise level
            float meanEnergy = 0;
            for (float energy : regionEnergies) {
                meanEnergy += energy;
            }
            meanEnergy /= regionEnergies.length;
            
            float varianceSum = 0;
            for (float energy : regionEnergies) {
                float diff = energy - meanEnergy;
                varianceSum += diff * diff;
            }
            float variance = varianceSum / regionEnergies.length;
            
            // Consistency is inverse of normalized variance
            features.noiseConsistency = 1.0f - Math.min(1.0f, (float) Math.sqrt(variance) / meanEnergy);
        } else {
            features.noiseConsistency = 1.0f;
        }
        
        // Calculate spectral variation of noise
        features.noiseSpectrumVariation = calculateNoiseSpectralVariation(silenceRegions);
        
        return features;
    }
    
    /**
     * Find silence regions in audio
     * @param audio Audio data
     * @return List of silence regions
     */
    private List<short[]> findSilenceRegions(short[] audio) {
        List<short[]> regions = new ArrayList<>();
        
        // Calculate overall RMS energy
        float totalEnergy = 0;
        for (short sample : audio) {
            totalEnergy += sample * sample;
        }
        float rmsEnergy = (float) Math.sqrt(totalEnergy / audio.length);
        
        // Define silence threshold as 10% of RMS energy
        float silenceThreshold = rmsEnergy * 0.1f;
        
        // Find continuous silence regions
        int minSilenceLength = 400; // 25ms at 16kHz
        List<Integer> silenceStarts = new ArrayList<>();
        List<Integer> silenceLengths = new ArrayList<>();
        
        boolean inSilence = false;
        int silenceStart = 0;
        int silenceLength = 0;
        
        for (int i = 0; i < audio.length; i++) {
            boolean isSilent = Math.abs(audio[i]) < silenceThreshold;
            
            if (isSilent) {
                if (!inSilence) {
                    // Start of new silence region
                    inSilence = true;
                    silenceStart = i;
                    silenceLength = 1;
                } else {
                    // Continue silence region
                    silenceLength++;
                }
            } else {
                if (inSilence) {
                    // End of silence region
                    inSilence = false;
                    if (silenceLength >= minSilenceLength) {
                        silenceStarts.add(silenceStart);
                        silenceLengths.add(silenceLength);
                    }
                }
            }
        }
        
        // Add final silence region if any
        if (inSilence && silenceLength >= minSilenceLength) {
            silenceStarts.add(silenceStart);
            silenceLengths.add(silenceLength);
        }
        
        // Extract silence regions
        for (int i = 0; i < silenceStarts.size(); i++) {
            int start = silenceStarts.get(i);
            int length = silenceLengths.get(i);
            
            short[] region = new short[length];
            System.arraycopy(audio, start, region, 0, length);
            regions.add(region);
        }
        
        return regions;
    }
    
    /**
     * Calculate spectral variation of noise across regions
     * @param silenceRegions Silence regions
     * @return Spectral variation score (0-1)
     */
    private float calculateNoiseSpectralVariation(List<short[]> silenceRegions) {
        if (silenceRegions.size() < 2) {
            return 0.1f;
        }
        
        // Calculate simple spectral features for each region
        float[][] spectralFeatures = new float[silenceRegions.size()][3];
        
        for (int i = 0; i < silenceRegions.size(); i++) {
            short[] region = silenceRegions.get(i);
            
            // Calculate zero crossing rate
            int crossings = 0;
            for (int j = 1; j < region.length; j++) {
                if ((region[j] > 0 && region[j - 1] < 0) || 
                    (region[j] < 0 && region[j - 1] > 0)) {
                    crossings++;
                }
            }
            spectralFeatures[i][0] = (float) crossings / region.length;
            
            // Calculate spectral centroid proxy
            float weightedSum = 0;
            float sum = 0;
            for (int j = 0; j < region.length; j++) {
                float weight = j / (float) region.length;
                sum += Math.abs(region[j]);
                weightedSum += Math.abs(region[j]) * weight;
            }
            spectralFeatures[i][1] = sum > 0 ? weightedSum / sum : 0;
            
            // Calculate spectral spread proxy
            float centroid = spectralFeatures[i][1];
            float spreadSum = 0;
            for (int j = 0; j < region.length; j++) {
                float weight = j / (float) region.length;
                float diff = weight - centroid;
                spreadSum += Math.abs(region[j]) * diff * diff;
            }
            spectralFeatures[i][2] = sum > 0 ? spreadSum / sum : 0;
        }
        
        // Calculate average features
        float[] avgFeatures = new float[3];
        for (int i = 0; i < spectralFeatures.length; i++) {
            for (int j = 0; j < 3; j++) {
                avgFeatures[j] += spectralFeatures[i][j];
            }
        }
        for (int j = 0; j < 3; j++) {
            avgFeatures[j] /= spectralFeatures.length;
        }
        
        // Calculate average difference from mean
        float totalDiff = 0;
        for (int i = 0; i < spectralFeatures.length; i++) {
            for (int j = 0; j < 3; j++) {
                totalDiff += Math.abs(spectralFeatures[i][j] - avgFeatures[j]);
            }
        }
        
        // Normalize and invert (lower variation means more consistent)
        return 1.0f - Math.min(1.0f, totalDiff / (spectralFeatures.length * 3) * 10);
    }
    
    /**
     * Extract spectral manipulation features
     * @param audio Audio data
     * @return Spectral features
     */
    private SpectralFeatures extractSpectralFeatures(short[] audio) {
        SpectralFeatures features = new SpectralFeatures();
        
        // Analyze spectral balance
        features.spectralBalance = calculateSpectralBalance(audio);
        
        // Check for frequency-domain anomalies
        features.frequencyAnomalyScore = detectFrequencyAnomalies(audio);
        
        // Analyze harmonic structure
        features.harmonicConsistency = calculateHarmonicConsistency(audio);
        
        // Calculate overall manipulation likelihood
        features.manipulationLikelihood = 
                (1.0f - features.spectralBalance) * 0.3f + 
                features.frequencyAnomalyScore * 0.4f + 
                (1.0f - features.harmonicConsistency) * 0.3f;
        
        return features;
    }
    
    /**
     * Calculate spectral balance score
     * @param audio Audio data
     * @return Spectral balance score (0-1, higher means more balanced)
     */
    private float calculateSpectralBalance(short[] audio) {
        // Divide spectrum into bands and calculate energy in each
        int frameSize = 512;
        int frameCount = audio.length / frameSize;
        
        if (frameCount < 1) {
            return 0.5f;
        }
        
        int numBands = 4;
        float[] bandEnergies = new float[numBands];
        
        for (int frame = 0; frame < frameCount; frame++) {
            int start = frame * frameSize;
            int end = Math.min(start + frameSize, audio.length);
            
            // Calculate simple frequency bands using basic filtering
            for (int i = start; i < end; i++) {
                // Very crude spectral analysis - just for demonstration
                int bandIndex = (i - start) * numBands / frameSize;
                bandEnergies[bandIndex] += audio[i] * audio[i];
            }
        }
        
        // Normalize band energies
        float totalEnergy = 0;
        for (float energy : bandEnergies) {
            totalEnergy += energy;
        }
        
        if (totalEnergy <= 0) {
            return 0.5f;
        }
        
        for (int i = 0; i < bandEnergies.length; i++) {
            bandEnergies[i] /= totalEnergy;
        }
        
        // Calculate deviation from ideal balance
        // In a real implementation, this would use proper psychoacoustic models
        // For now, use a simple model where energy decreases with frequency
        float[] idealBalance = { 0.4f, 0.3f, 0.2f, 0.1f };
        
        float balanceDeviation = 0;
        for (int i = 0; i < numBands; i++) {
            balanceDeviation += Math.abs(bandEnergies[i] - idealBalance[i]);
        }
        
        // Convert to balance score (0-1)
        return 1.0f - Math.min(1.0f, balanceDeviation);
    }
    
    /**
     * Detect frequency domain anomalies
     * @param audio Audio data
     * @return Anomaly score (0-1)
     */
    private float detectFrequencyAnomalies(short[] audio) {
        // In a real implementation, this would perform FFT and look for
        // unusual patterns like missing frequencies, isolated peaks, etc.
        // For demonstration, use a simplified approach.
        
        int frameSize = 512;
        int hopSize = 256;
        int frameCount = (audio.length - frameSize) / hopSize + 1;
        
        if (frameCount < 2) {
            return 0.1f;
        }
        
        // Calculate temporal coherence of frequency content
        float[] prevFreqProfile = null;
        float coherenceSum = 0;
        int coherenceCount = 0;
        
        for (int frame = 0; frame < frameCount; frame++) {
            int start = frame * hopSize;
            int end = Math.min(start + frameSize, audio.length);
            
            // Calculate simplified frequency profile
            float[] freqProfile = new float[8]; // 8 crude frequency bands
            for (int i = start; i < end; i++) {
                int bandIndex = (i - start) * freqProfile.length / frameSize;
                freqProfile[bandIndex] += audio[i] * audio[i];
            }
            
            // Normalize
            float sum = 0;
            for (float value : freqProfile) {
                sum += value;
            }
            if (sum > 0) {
                for (int i = 0; i < freqProfile.length; i++) {
                    freqProfile[i] /= sum;
                }
            }
            
            // Compare with previous frame
            if (prevFreqProfile != null) {
                float frameDiff = 0;
                for (int i = 0; i < freqProfile.length; i++) {
                    frameDiff += Math.abs(freqProfile[i] - prevFreqProfile[i]);
                }
                coherenceSum += frameDiff;
                coherenceCount++;
            }
            
            prevFreqProfile = freqProfile;
        }
        
        // Calculate average coherence
        float avgCoherence = coherenceCount > 0 ? coherenceSum / coherenceCount : 0;
        
        // Convert to anomaly score (higher coherence means lower anomaly)
        // But abrupt changes can indicate manipulation
        return Math.min(1.0f, avgCoherence * 5);
    }
    
    /**
     * Calculate harmonic consistency
     * @param audio Audio data
     * @return Harmonic consistency score (0-1)
     */
    private float calculateHarmonicConsistency(short[] audio) {
        // In a real implementation, this would analyze harmonic structure
        // using proper pitch detection and harmonic analysis
        // For demonstration, use a simplified approach
        
        int frameSize = 512;
        int hopSize = 256;
        int frameCount = (audio.length - frameSize) / hopSize + 1;
        
        if (frameCount < 2) {
            return 0.5f;
        }
        
        // Calculate autocorrelation for each frame
        float[][] autocorrelations = new float[frameCount][frameSize / 2];
        
        for (int frame = 0; frame < frameCount; frame++) {
            int start = frame * hopSize;
            int end = Math.min(start + frameSize, audio.length);
            int length = end - start;
            
            // Calculate autocorrelation
            for (int lag = 0; lag < frameSize / 2; lag++) {
                float sum = 0;
                for (int i = 0; i < length - lag; i++) {
                    sum += audio[start + i] * audio[start + i + lag];
                }
                autocorrelations[frame][lag] = sum / (length - lag);
            }
            
            // Normalize
            float maxVal = 0;
            for (int i = 0; i < frameSize / 2; i++) {
                maxVal = Math.max(maxVal, Math.abs(autocorrelations[frame][i]));
            }
            if (maxVal > 0) {
                for (int i = 0; i < frameSize / 2; i++) {
                    autocorrelations[frame][i] /= maxVal;
                }
            }
        }
        
        // Measure consistency of peak locations across frames
        Map<Integer, Integer> peakCounts = new HashMap<>();
        int totalPeaks = 0;
        
        for (int frame = 0; frame < frameCount; frame++) {
            // Find peaks in autocorrelation
            for (int i = 1; i < frameSize / 2 - 1; i++) {
                if (autocorrelations[frame][i] > 0.3f && // Threshold
                    autocorrelations[frame][i] > autocorrelations[frame][i-1] &&
                    autocorrelations[frame][i] > autocorrelations[frame][i+1]) {
                    
                    // Found a peak
                    int peakBin = i / 4; // Group similar peaks
                    peakCounts.put(peakBin, peakCounts.getOrDefault(peakBin, 0) + 1);
                    totalPeaks++;
                }
            }
        }
        
        // Calculate consistency based on peak distribution
        float consistency;
        if (totalPeaks == 0) {
            consistency = 0.5f; // No peaks found
        } else {
            // Analyze peak distribution
            int maxCount = 0;
            for (int count : peakCounts.values()) {
                maxCount = Math.max(maxCount, count);
            }
            
            // Calculate ratio of most common peak to total peaks
            consistency = (float) maxCount / totalPeaks;
        }
        
        return consistency;
    }
    
    /**
     * Perform forensic analysis on audio features
     * @param features Audio features
     * @return Forensic analysis result
     */
    private ForensicResult performForensicAnalysis(AudioFeatures features) {
        ForensicResult result = new ForensicResult();
        
        // Analyze signal continuity for splicing detection
        if (features.continuity.overallContinuityScore < 0.7f) {
            // Likely splicing detected
            result.splicingDetected = true;
            result.splicingConfidence = 1.0f - features.continuity.overallContinuityScore;
            result.discontinuityPoints = features.continuity.discontinuityPoints;
        }
        
        // Analyze compression artifacts
        if (features.compression.compressionLikelihood > 0.7f) {
            // Likely compression detected
            result.compressionDetected = true;
            result.compressionConfidence = features.compression.compressionLikelihood;
        }
        
        // Analyze background noise
        if (features.noise.noiseConsistency < 0.7f) {
            // Background noise inconsistency detected
            result.backgroundInconsistency = true;
            result.backgroundInconsistencyConfidence = 1.0f - features.noise.noiseConsistency;
        }
        
        // Analyze spectral manipulation
        if (features.spectral.manipulationLikelihood > 0.6f) {
            // Likely spectral manipulation detected
            result.spectralManipulation = true;
            result.spectralManipulationConfidence = features.spectral.manipulationLikelihood;
        }
        
        // Calculate overall tampering likelihood
        result.tamperingLikelihood = calculateTamperingLikelihood(features);
        
        // Determine if audio appears manipulated
        result.audioManipulated = result.tamperingLikelihood > TAMPERING_THRESHOLD;
        
        Log.d(TAG, "Forensic analysis complete: tampering=" + result.tamperingLikelihood + 
                ", splicing=" + (result.splicingDetected ? result.splicingConfidence : "none") + 
                ", compression=" + (result.compressionDetected ? result.compressionConfidence : "none") + 
                ", background=" + (result.backgroundInconsistency ? result.backgroundInconsistencyConfidence : "none") + 
                ", spectral=" + (result.spectralManipulation ? result.spectralManipulationConfidence : "none"));
        
        return result;
    }
    
    /**
     * Calculate overall tampering likelihood
     * @param features Audio features
     * @return Tampering likelihood (0-1)
     */
    private float calculateTamperingLikelihood(AudioFeatures features) {
        // Weighted combination of all evidence
        float tamperingScore = 0;
        
        // Splice detection has highest weight (40%)
        tamperingScore += (1.0f - features.continuity.overallContinuityScore) * 0.4f;
        
        // Compression has moderate weight (20%)
        tamperingScore += features.compression.compressionLikelihood * 0.2f;
        
        // Background inconsistency has moderate weight (20%)
        tamperingScore += (1.0f - features.noise.noiseConsistency) * 0.2f;
        
        // Spectral manipulation has moderate weight (20%)
        tamperingScore += features.spectral.manipulationLikelihood * 0.2f;
        
        return tamperingScore;
    }
    
    /**
     * Save forensic evidence for later analysis
     * @param audioData Original audio data
     * @param features Extracted features
     * @param result Forensic analysis result
     * @param userId User ID (optional, for organization)
     */
    private void saveForensicEvidence(byte[] audioData, AudioFeatures features, 
                                     ForensicResult result, String userId) {
        try {
            // Create evidence folder with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                    .format(new Date());
            String folderName = "evidence_" + timestamp + 
                    (userId != null ? "_" + userId : "");
            File evidenceFolder = new File(evidenceDir, folderName);
            evidenceFolder.mkdirs();
            
            // Save original audio data
            File audioFile = new File(evidenceFolder, "audio.raw");
            try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                fos.write(audioData);
            }
            
            // Save analysis results in JSON format
            File resultFile = new File(evidenceFolder, "analysis.json");
            try (FileOutputStream fos = new FileOutputStream(resultFile)) {
                StringBuilder json = new StringBuilder();
                json.append("{\n");
                json.append("  \"timestamp\": \"").append(timestamp).append("\",\n");
                if (userId != null) {
                    json.append("  \"userId\": \"").append(userId).append("\",\n");
                }
                json.append("  \"tamperingLikelihood\": ").append(result.tamperingLikelihood).append(",\n");
                json.append("  \"audioManipulated\": ").append(result.audioManipulated).append(",\n");
                json.append("  \"splicingDetected\": ").append(result.splicingDetected).append(",\n");
                json.append("  \"splicingConfidence\": ").append(result.splicingConfidence).append(",\n");
                json.append("  \"compressionDetected\": ").append(result.compressionDetected).append(",\n");
                json.append("  \"compressionConfidence\": ").append(result.compressionConfidence).append(",\n");
                json.append("  \"backgroundInconsistency\": ").append(result.backgroundInconsistency).append(",\n");
                json.append("  \"backgroundInconsistencyConfidence\": ").append(result.backgroundInconsistencyConfidence).append(",\n");
                json.append("  \"spectralManipulation\": ").append(result.spectralManipulation).append(",\n");
                json.append("  \"spectralManipulationConfidence\": ").append(result.spectralManipulationConfidence).append(",\n");
                
                // Add discontinuity points if any
                if (result.discontinuityPoints != null && !result.discontinuityPoints.isEmpty()) {
                    json.append("  \"discontinuityPoints\": [");
                    for (int i = 0; i < result.discontinuityPoints.size(); i++) {
                        json.append(result.discontinuityPoints.get(i));
                        if (i < result.discontinuityPoints.size() - 1) {
                            json.append(", ");
                        }
                    }
                    json.append("],\n");
                }
                
                // Add feature details
                json.append("  \"features\": {\n");
                json.append("    \"continuity\": {\n");
                json.append("      \"overallScore\": ").append(features.continuity.overallContinuityScore).append(",\n");
                json.append("      \"phaseScore\": ").append(features.continuity.phaseContinuityScore).append(",\n");
                json.append("      \"amplitudeScore\": ").append(features.continuity.amplitudeContinuityScore).append("\n");
                json.append("    },\n");
                json.append("    \"compression\": {\n");
                json.append("      \"likelihood\": ").append(features.compression.compressionLikelihood).append(",\n");
                json.append("      \"highFreqRatio\": ").append(features.compression.highFrequencyRatio).append(",\n");
                json.append("      \"spectralFlatness\": ").append(features.compression.spectralFlatness).append(",\n");
                json.append("      \"quantizationScore\": ").append(features.compression.quantizationScore).append("\n");
                json.append("    },\n");
                json.append("    \"noise\": {\n");
                json.append("      \"consistency\": ").append(features.noise.noiseConsistency).append(",\n");
                json.append("      \"level\": ").append(features.noise.noiseLevel).append(",\n");
                json.append("      \"spectrumVariation\": ").append(features.noise.noiseSpectrumVariation).append("\n");
                json.append("    },\n");
                json.append("    \"spectral\": {\n");
                json.append("      \"manipulationLikelihood\": ").append(features.spectral.manipulationLikelihood).append(",\n");
                json.append("      \"spectralBalance\": ").append(features.spectral.spectralBalance).append(",\n");
                json.append("      \"frequencyAnomaly\": ").append(features.spectral.frequencyAnomalyScore).append(",\n");
                json.append("      \"harmonicConsistency\": ").append(features.spectral.harmonicConsistency).append("\n");
                json.append("    }\n");
                json.append("  }\n");
                json.append("}");
                
                fos.write(json.toString().getBytes());
            }
            
            Log.d(TAG, "Saved forensic evidence to " + evidenceFolder.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error saving forensic evidence", e);
        }
    }
    
    /**
     * Get list of saved evidence folders
     * @return Array of evidence folder names
     */
    public String[] getEvidenceFolders() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.READ_ONLY)) {
            return new String[0];
        }
        
        if (!evidenceDir.exists()) {
            return new String[0];
        }
        
        File[] evidenceFolders = evidenceDir.listFiles(File::isDirectory);
        if (evidenceFolders == null) {
            return new String[0];
        }
        
        String[] folderNames = new String[evidenceFolders.length];
        for (int i = 0; i < evidenceFolders.length; i++) {
            folderNames[i] = evidenceFolders[i].getName();
        }
        
        return folderNames;
    }
    
    /**
     * Set whether to automatically save evidence
     * @param autoSave True to auto-save evidence
     */
    public void setAutoSaveEvidence(boolean autoSave) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.WRITE)) {
            return;
        }
        
        this.autoSaveEvidence = autoSave;
        Log.d(TAG, "Set auto-save evidence to " + autoSave);
    }
    
    /**
     * Delete evidence folder
     * @param folderName Evidence folder name
     * @return True if deleted successfully
     */
    public boolean deleteEvidenceFolder(String folderName) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.ADMIN)) {
            return false;
        }
        
        File folder = new File(evidenceDir, folderName);
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }
        
        // Delete all files in folder
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        
        // Delete folder
        boolean success = folder.delete();
        if (success) {
            Log.d(TAG, "Deleted evidence folder: " + folderName);
        } else {
            Log.e(TAG, "Failed to delete evidence folder: " + folderName);
        }
        
        return success;
    }
    
    /**
     * Add forensics listener
     * @param listener Listener to add
     */
    public void addForensicsListener(ForensicsListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove forensics listener
     * @param listener Listener to remove
     */
    public void removeForensicsListener(ForensicsListener listener) {
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
     * Audio features used for forensic analysis
     */
    public static class AudioFeatures {
        public SignalContinuityFeatures continuity;
        public CompressionFeatures compression;
        public BackgroundNoiseFeatures noise;
        public SpectralFeatures spectral;
        
        public AudioFeatures(SignalContinuityFeatures continuity, CompressionFeatures compression,
                           BackgroundNoiseFeatures noise, SpectralFeatures spectral) {
            this.continuity = continuity;
            this.compression = compression;
            this.noise = noise;
            this.spectral = spectral;
        }
    }
    
    /**
     * Signal continuity features
     */
    public static class SignalContinuityFeatures {
        public float phaseContinuityScore;
        public float amplitudeContinuityScore;
        public float overallContinuityScore;
        public List<Integer> discontinuityPoints;
        
        public SignalContinuityFeatures() {
            this.phaseContinuityScore = 1.0f;
            this.amplitudeContinuityScore = 1.0f;
            this.overallContinuityScore = 1.0f;
            this.discontinuityPoints = new ArrayList<>();
        }
    }
    
    /**
     * Compression artifact features
     */
    public static class CompressionFeatures {
        public float highFrequencyRatio;
        public float spectralFlatness;
        public float spectralFlatnessVariation;
        public float quantizationScore;
        public float compressionLikelihood;
        
        public CompressionFeatures() {
            this.highFrequencyRatio = 0.5f;
            this.spectralFlatness = 0.5f;
            this.spectralFlatnessVariation = 0.5f;
            this.quantizationScore = 0.5f;
            this.compressionLikelihood = 0.5f;
        }
    }
    
    /**
     * Background noise features
     */
    public static class BackgroundNoiseFeatures {
        public float noiseLevel;
        public float noiseConsistency;
        public float noiseSpectrumVariation;
        
        public BackgroundNoiseFeatures() {
            this.noiseLevel = 0.1f;
            this.noiseConsistency = 0.9f;
            this.noiseSpectrumVariation = 0.1f;
        }
    }
    
    /**
     * Spectral features
     */
    public static class SpectralFeatures {
        public float spectralBalance;
        public float frequencyAnomalyScore;
        public float harmonicConsistency;
        public float manipulationLikelihood;
        
        public SpectralFeatures() {
            this.spectralBalance = 0.8f;
            this.frequencyAnomalyScore = 0.2f;
            this.harmonicConsistency = 0.8f;
            this.manipulationLikelihood = 0.2f;
        }
    }
    
    /**
     * Forensic analysis result
     */
    public static class ForensicResult {
        public boolean audioManipulated;
        public float tamperingLikelihood;
        
        public boolean splicingDetected;
        public float splicingConfidence;
        
        public boolean compressionDetected;
        public float compressionConfidence;
        
        public boolean backgroundInconsistency;
        public float backgroundInconsistencyConfidence;
        
        public boolean spectralManipulation;
        public float spectralManipulationConfidence;
        
        public List<Integer> discontinuityPoints;
        
        public ForensicResult() {
            this.audioManipulated = false;
            this.tamperingLikelihood = 0.0f;
            this.splicingDetected = false;
            this.splicingConfidence = 0.0f;
            this.compressionDetected = false;
            this.compressionConfidence = 0.0f;
            this.backgroundInconsistency = false;
            this.backgroundInconsistencyConfidence = 0.0f;
            this.spectralManipulation = false;
            this.spectralManipulationConfidence = 0.0f;
            this.discontinuityPoints = null;
        }
        
        /**
         * Check if any anomalies were detected
         * @return True if any anomalies were detected
         */
        public boolean hasAnomalies() {
            return splicingDetected || compressionDetected || 
                   backgroundInconsistency || spectralManipulation;
        }
        
        /**
         * Check if critical anomalies were detected
         * @return True if critical anomalies were detected
         */
        public boolean hasCriticalAnomalies() {
            return (splicingDetected && splicingConfidence >= SPLICING_THRESHOLD) ||
                   (tamperingLikelihood >= TAMPERING_THRESHOLD);
        }
        
        /**
         * Get forensic analysis summary
         * @return Human-readable summary
         */
        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            
            if (audioManipulated) {
                sb.append("Audio manipulation detected (confidence: ")
                  .append(String.format("%.1f", tamperingLikelihood * 100))
                  .append("%).");
            } else {
                sb.append("No significant audio manipulation detected.");
            }
            
            if (splicingDetected) {
                sb.append("\nSplicing detected (confidence: ")
                  .append(String.format("%.1f", splicingConfidence * 100))
                  .append("%)");
                if (discontinuityPoints != null && !discontinuityPoints.isEmpty()) {
                    sb.append(" at ");
                    for (int i = 0; i < Math.min(3, discontinuityPoints.size()); i++) {
                        float timePos = discontinuityPoints.get(i) / 16000.0f;
                        sb.append(String.format("%.2fs", timePos));
                        if (i < Math.min(2, discontinuityPoints.size() - 1)) {
                            sb.append(", ");
                        }
                    }
                    if (discontinuityPoints.size() > 3) {
                        sb.append(" and ").append(discontinuityPoints.size() - 3).append(" more points");
                    }
                }
                sb.append(".");
            }
            
            if (compressionDetected) {
                sb.append("\nCompression artifacts detected (confidence: ")
                  .append(String.format("%.1f", compressionConfidence * 100))
                  .append("%).");
            }
            
            if (backgroundInconsistency) {
                sb.append("\nBackground noise inconsistency detected (confidence: ")
                  .append(String.format("%.1f", backgroundInconsistencyConfidence * 100))
                  .append("%).");
            }
            
            if (spectralManipulation) {
                sb.append("\nSpectral manipulation detected (confidence: ")
                  .append(String.format("%.1f", spectralManipulationConfidence * 100))
                  .append("%).");
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Forensic analysis listener interface
     */
    public interface ForensicAnalysisListener {
        /**
         * Called when analysis completes
         * @param result Analysis result
         */
        void onAnalysisComplete(ForensicResult result);
        
        /**
         * Called when analysis fails
         * @param reason Reason for failure
         */
        void onAnalysisFailed(String reason);
    }
    
    /**
     * Forensics listener interface
     */
    public interface ForensicsListener {
        /**
         * Called when forensic anomaly is detected
         * @param result Forensic analysis result
         */
        void onForensicAnomalyDetected(ForensicResult result);
    }
}
