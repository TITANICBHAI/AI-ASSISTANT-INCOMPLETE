package com.aiassistant.core.voice.authentication;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aiassistant.core.security.AccessControl;
import com.aiassistant.core.voice.analysis.BehavioralVoiceAnalyzer;
import com.aiassistant.core.voice.detection.SyntheticVoiceDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Multi-factor voice authentication system that integrates biometrics, behavior, 
 * and synthetic voice detection for enhanced security
 */
public class MultiFactorVoiceAuthenticator {
    private static final String TAG = "MultiFactorAuth";
    
    // Authentication thresholds
    private static final float LOW_SECURITY_THRESHOLD = 0.70f;
    private static final float MEDIUM_SECURITY_THRESHOLD = 0.80f;
    private static final float HIGH_SECURITY_THRESHOLD = 0.90f;
    
    // Authentication failure limits
    private static final int MAX_FAILURES_LOW = 5;
    private static final int MAX_FAILURES_MEDIUM = 3;
    private static final int MAX_FAILURES_HIGH = 2;
    
    private Context context;
    private AccessControl accessControl;
    private VoiceBiometricAuthenticator biometricAuth;
    private BehavioralVoiceAnalyzer behavioralAnalyzer;
    private SyntheticVoiceDetector syntheticDetector;
    
    private Executor backgroundExecutor;
    private Handler mainHandler;
    private List<AuthenticationListener> listeners;
    private SecurityLevel securityLevel;
    private long lastSuccessfulAuth;
    private int consecutiveFailures;
    private String currentUserId;
    private boolean initialized;
    
    /**
     * Constructor
     * @param context Application context
     * @param accessControl Access control for security checks
     */
    public MultiFactorVoiceAuthenticator(Context context, AccessControl accessControl) {
        this.context = context;
        this.accessControl = accessControl;
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.listeners = new ArrayList<>();
        this.securityLevel = SecurityLevel.MEDIUM; // Default security level
        this.lastSuccessfulAuth = 0;
        this.consecutiveFailures = 0;
        this.currentUserId = null;
        this.initialized = false;
    }
    
    /**
     * Initialize the authenticator
     * @return True if initialization successful
     */
    public boolean initialize() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.ADMIN)) {
            Log.e(TAG, "Access denied for initializing multi-factor voice authenticator");
            return false;
        }
        
        Log.d(TAG, "Initializing multi-factor voice authenticator");
        
        try {
            // Initialize biometric authenticator
            biometricAuth = new VoiceBiometricAuthenticator(context, accessControl);
            
            // Initialize behavioral analyzer
            behavioralAnalyzer = new BehavioralVoiceAnalyzer(context, accessControl);
            boolean behavioralInit = behavioralAnalyzer.initialize();
            
            // Initialize synthetic detector
            syntheticDetector = new SyntheticVoiceDetector(context, accessControl);
            boolean syntheticInit = syntheticDetector.initialize();
            
            // Add listeners
            biometricAuth.addAuthenticationListener(new VoiceBiometricAuthenticator.VoiceAuthenticationListener() {
                @Override
                public void onVoiceAuthenticated(float confidence) {
                    // We handle authentication in the multi-factor authenticate method
                }
                
                @Override
                public void onAlternativeAuthenticated(VoiceBiometricAuthenticator.AlternativeAuthType authType) {
                    // We handle authentication in the multi-factor authenticate method
                }
            });
            
            behavioralAnalyzer.addBehavioralAnalysisListener(new BehavioralVoiceAnalyzer.BehavioralAnalysisListener() {
                @Override
                public void onBehavioralAnomaly(String userId, BehavioralVoiceAnalyzer.BehavioralResult result) {
                    for (AuthenticationListener listener : listeners) {
                        mainHandler.post(() -> listener.onBehavioralAnomaly(userId, result.getAnomalyScore()));
                    }
                }
                
                @Override
                public void onStressDetected(String userId, BehavioralVoiceAnalyzer.BehavioralResult result) {
                    for (AuthenticationListener listener : listeners) {
                        mainHandler.post(() -> listener.onStressDetected(userId, result.getStressScore()));
                    }
                }
                
                @Override
                public void onIdentityMismatch(String userId, BehavioralVoiceAnalyzer.BehavioralResult result) {
                    for (AuthenticationListener listener : listeners) {
                        mainHandler.post(() -> listener.onIdentityMismatch(userId, result.getIdentityScore()));
                    }
                }
            });
            
            syntheticDetector.addSyntheticVoiceListener(new SyntheticVoiceDetector.SyntheticVoiceListener() {
                @Override
                public void onSyntheticVoiceDetected(SyntheticVoiceDetector.DetectionResult result) {
                    for (AuthenticationListener listener : listeners) {
                        mainHandler.post(() -> listener.onSyntheticVoiceDetected(
                                result.getSyntheticConfidence(), result.isCloned()));
                    }
                }
            });
            
            initialized = behavioralInit && syntheticInit;
            
            if (initialized) {
                Log.d(TAG, "Multi-factor voice authenticator initialized successfully");
            } else {
                Log.e(TAG, "Failed to initialize one or more components");
            }
            
            return initialized;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing multi-factor voice authenticator", e);
            return false;
        }
    }
    
    /**
     * Authenticate with voice using multiple factors
     * @param userId User ID
     * @param audioData Audio data
     * @param enrollmentText Text for enrollment if needed
     * @param listener Listener for authentication events
     * @param criticalCommand Whether this is for a critical command
     */
    public void authenticate(String userId, byte[] audioData, String enrollmentText, 
                            final AuthResultListener listener, boolean criticalCommand) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.EXECUTE)) {
            if (listener != null) {
                listener.onAuthenticationFailed("Access denied for voice authentication");
            }
            return;
        }
        
        // Check if initialized
        if (!initialized) {
            if (listener != null) {
                listener.onAuthenticationFailed("Multi-factor voice authenticator not initialized");
            }
            return;
        }
        
        // Set current user ID
        this.currentUserId = userId;
        
        Log.d(TAG, "Starting multi-factor voice authentication for user: " + userId + 
                (criticalCommand ? " (critical command)" : ""));
        
        // Reset factors for this authentication attempt
        final AuthFactors factors = new AuthFactors();
        
        // Step 1: Biometric authentication
        biometricAuth.authenticate(enrollmentText, new VoiceBiometricAuthenticator.AuthenticationListener() {
            @Override
            public void onAuthenticationRecordingFailed(String reason) {
                if (listener != null) {
                    listener.onAuthenticationFailed("Biometric recording failed: " + reason);
                }
            }
            
            @Override
            public void onAuthenticationSuccessful(float confidence) {
                // Biometric factor successful
                factors.biometricFactor = new Factor(true, confidence);
                
                // Continue with next factor
                continueWithSyntheticDetection(userId, audioData, enrollmentText, listener, criticalCommand, factors);
            }
            
            @Override
            public void onAuthenticationFailed(String reason) {
                // Biometric factor failed
                factors.biometricFactor = new Factor(false, 0);
                
                // Check if we should continue with other factors
                if (securityLevel == SecurityLevel.LOW) {
                    // In low security mode, continue with other factors
                    continueWithSyntheticDetection(userId, audioData, enrollmentText, listener, criticalCommand, factors);
                } else {
                    // In medium/high security, biometric is required
                    handleFailedAuthentication(listener, "Biometric authentication failed: " + reason);
                }
            }
            
            @Override
            public void onTooManyFailedAttempts(int attempts) {
                if (listener != null) {
                    listener.onTooManyFailedAttempts(attempts);
                }
            }
            
            @Override
            public void onAlternativeAuthRequired() {
                if (listener != null) {
                    listener.onAlternativeAuthRequired();
                }
            }
            
            @Override
            public void onEnrollmentRequired() {
                if (listener != null) {
                    listener.onEnrollmentRequired(enrollmentText);
                }
            }
            
            @Override
            public void onAlternativeAuthSuccessful(VoiceBiometricAuthenticator.AlternativeAuthType authType) {
                // Alternative auth counts as biometric success with reduced confidence
                factors.biometricFactor = new Factor(true, 0.7f);
                factors.usedAlternative = true;
                factors.alternativeType = authType;
                
                // Continue with next factor
                continueWithSyntheticDetection(userId, audioData, enrollmentText, listener, criticalCommand, factors);
            }
        }, criticalCommand);
    }
    
    /**
     * Continue authentication with synthetic voice detection
     */
    private void continueWithSyntheticDetection(String userId, byte[] audioData, String enrollmentText,
                                               final AuthResultListener listener, boolean criticalCommand,
                                               final AuthFactors factors) {
        // Step 2: Synthetic voice detection
        syntheticDetector.analyzeAudio(audioData, new SyntheticVoiceDetector.DetectionListener() {
            @Override
            public void onDetectionComplete(SyntheticVoiceDetector.DetectionResult result) {
                // Synthetic detection factor - inverted since we want to detect natural voices
                factors.syntheticFactor = new Factor(!result.isSynthetic() && !result.isCloned(), 
                        1.0f - Math.max(result.getSyntheticConfidence(), result.getClonedConfidence()));
                
                // If synthetic voice detected with high confidence in high security mode, fail immediately
                if ((result.isSynthetic() || result.isCloned()) && 
                    securityLevel == SecurityLevel.HIGH) {
                    handleFailedAuthentication(listener, "Synthetic voice detected");
                    return;
                }
                
                // Continue with next factor
                continueWithBehavioralAnalysis(userId, audioData, enrollmentText, listener, criticalCommand, factors);
            }
            
            @Override
            public void onDetectionFailed(String reason) {
                // Detection failed but continue with other factors
                factors.syntheticFactor = new Factor(true, 0.5f); // Neutral confidence
                
                // Continue with next factor
                continueWithBehavioralAnalysis(userId, audioData, enrollmentText, listener, criticalCommand, factors);
            }
        });
    }
    
    /**
     * Continue authentication with behavioral analysis
     */
    private void continueWithBehavioralAnalysis(String userId, byte[] audioData, String enrollmentText,
                                               final AuthResultListener listener, boolean criticalCommand,
                                               final AuthFactors factors) {
        // Step 3: Behavioral analysis
        behavioralAnalyzer.analyzeAudio(userId, audioData, new BehavioralVoiceAnalyzer.AnalysisListener() {
            @Override
            public void onAnalysisComplete(BehavioralVoiceAnalyzer.BehavioralResult result) {
                // Behavioral factor
                factors.behavioralFactor = new Factor(
                        result.isIdentityConfirmed() && !result.isAbnormalBehavior(),
                        result.getIdentityScore());
                
                // If behavioral analysis shows high anomaly in high security mode, fail
                if (result.isAbnormalBehavior() && securityLevel == SecurityLevel.HIGH) {
                    handleFailedAuthentication(listener, "Behavioral anomaly detected");
                    return;
                }
                
                // All factors collected, evaluate final result
                evaluateAuthenticationResult(userId, listener, criticalCommand, factors);
            }
            
            @Override
            public void onAnalysisFailed(String reason) {
                // If we have no behavioral profile yet, add this audio as the first pattern
                if (reason.contains("No behavioral profile found")) {
                    behavioralAnalyzer.addUserPattern(userId, audioData, 
                            new BehavioralVoiceAnalyzer.PatternListener() {
                        @Override
                        public void onPatternAdded(String userId, int patternCount) {
                            Log.d(TAG, "Added first behavioral pattern for user: " + userId);
                            // Use neutral behavioral score for first-time users
                            factors.behavioralFactor = new Factor(true, 0.7f);
                            evaluateAuthenticationResult(userId, listener, criticalCommand, factors);
                        }
                        
                        @Override
                        public void onPatternFailed(String reason) {
                            Log.e(TAG, "Failed to add behavioral pattern: " + reason);
                            // Continue with neutral behavioral score
                            factors.behavioralFactor = new Factor(true, 0.5f);
                            evaluateAuthenticationResult(userId, listener, criticalCommand, factors);
                        }
                    });
                } else {
                    // Analysis failed but continue with evaluation
                    factors.behavioralFactor = new Factor(true, 0.5f); // Neutral confidence
                    evaluateAuthenticationResult(userId, listener, criticalCommand, factors);
                }
            }
        });
    }
    
    /**
     * Evaluate final authentication result from all factors
     */
    private void evaluateAuthenticationResult(String userId, final AuthResultListener listener,
                                             boolean criticalCommand, AuthFactors factors) {
        // Calculate combined confidence score
        float combinedScore = calculateCombinedScore(factors, criticalCommand);
        
        // Determine required threshold based on security level and command criticality
        float requiredThreshold = getRequiredThreshold(criticalCommand);
        
        // Check if authentication is successful
        boolean success = isCombinedScoreSuccessful(factors, combinedScore, requiredThreshold);
        
        // If stress detected, record that in the result even if authentication passes
        boolean stressDetected = factors.behavioralFactor != null && 
                factors.behavioralFactor.confidence < 0.4f; // Simple stress detection proxy
        
        // Create authentication result
        AuthenticationResult result = new AuthenticationResult(
                success, combinedScore, factors, stressDetected);
        
        // Handle success or failure
        if (success) {
            handleSuccessfulAuthentication(userId, listener, result);
        } else {
            handleFailedAuthentication(listener, "Combined authentication score too low: " + 
                    combinedScore + " (required: " + requiredThreshold + ")");
        }
    }
    
    /**
     * Calculate combined confidence score from all factors
     * @param factors Authentication factors
     * @param criticalCommand Whether this is for a critical command
     * @return Combined confidence score (0-1)
     */
    private float calculateCombinedScore(AuthFactors factors, boolean criticalCommand) {
        // Weight factors differently based on security level
        float biometricWeight, syntheticWeight, behavioralWeight;
        
        switch (securityLevel) {
            case HIGH:
                biometricWeight = 0.5f;
                syntheticWeight = 0.3f;
                behavioralWeight = 0.2f;
                break;
                
            case MEDIUM:
                biometricWeight = 0.4f;
                syntheticWeight = 0.3f;
                behavioralWeight = 0.3f;
                break;
                
            case LOW:
            default:
                biometricWeight = 0.4f;
                syntheticWeight = 0.2f;
                behavioralWeight = 0.4f;
                break;
        }
        
        // For critical commands, increase weight of biometric and synthetic detection
        if (criticalCommand) {
            biometricWeight += 0.1f;
            syntheticWeight += 0.1f;
            behavioralWeight -= 0.2f;
        }
        
        // Normalize weights
        float sum = biometricWeight + syntheticWeight + behavioralWeight;
        biometricWeight /= sum;
        syntheticWeight /= sum;
        behavioralWeight /= sum;
        
        // Calculate weighted score
        float score = 0;
        float totalWeight = 0;
        
        if (factors.biometricFactor != null) {
            score += biometricWeight * factors.biometricFactor.confidence;
            totalWeight += biometricWeight;
        }
        
        if (factors.syntheticFactor != null) {
            score += syntheticWeight * factors.syntheticFactor.confidence;
            totalWeight += syntheticWeight;
        }
        
        if (factors.behavioralFactor != null) {
            score += behavioralWeight * factors.behavioralFactor.confidence;
            totalWeight += behavioralWeight;
        }
        
        // If not all factors are available, normalize by available weights
        return totalWeight > 0 ? score / totalWeight : 0;
    }
    
    /**
     * Get required threshold based on security level and command criticality
     * @param criticalCommand Whether this is for a critical command
     * @return Required threshold (0-1)
     */
    private float getRequiredThreshold(boolean criticalCommand) {
        switch (securityLevel) {
            case HIGH:
                return criticalCommand ? HIGH_SECURITY_THRESHOLD + 0.05f : HIGH_SECURITY_THRESHOLD;
                
            case MEDIUM:
                return criticalCommand ? MEDIUM_SECURITY_THRESHOLD + 0.05f : MEDIUM_SECURITY_THRESHOLD;
                
            case LOW:
            default:
                return criticalCommand ? LOW_SECURITY_THRESHOLD + 0.05f : LOW_SECURITY_THRESHOLD;
        }
    }
    
    /**
     * Check if combined score passes authentication
     * @param factors Authentication factors
     * @param combinedScore Combined confidence score
     * @param requiredThreshold Required threshold
     * @return True if authentication is successful
     */
    private boolean isCombinedScoreSuccessful(AuthFactors factors, float combinedScore, 
                                            float requiredThreshold) {
        // Check combined score against threshold
        if (combinedScore < requiredThreshold) {
            return false;
        }
        
        // Additional checks for medium and high security levels
        if (securityLevel == SecurityLevel.MEDIUM || securityLevel == SecurityLevel.HIGH) {
            // Biometric factor must be successful
            if (factors.biometricFactor == null || !factors.biometricFactor.success) {
                return false;
            }
            
            // For high security, synthetic factor must also be successful
            if (securityLevel == SecurityLevel.HIGH && 
                (factors.syntheticFactor == null || !factors.syntheticFactor.success)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Handle successful authentication
     * @param userId User ID
     * @param listener Authentication result listener
     * @param result Authentication result
     */
    private void handleSuccessfulAuthentication(String userId, final AuthResultListener listener,
                                              AuthenticationResult result) {
        // Reset consecutive failures
        consecutiveFailures = 0;
        
        // Record time of successful authentication
        lastSuccessfulAuth = System.currentTimeMillis();
        
        // Notify listener
        if (listener != null) {
            listener.onAuthenticationSuccessful(result);
        }
        
        // Notify all listeners
        for (AuthenticationListener authListener : listeners) {
            authListener.onUserAuthenticated(userId, result);
        }
        
        Log.d(TAG, "Multi-factor authentication successful for user: " + userId + 
                ", confidence: " + result.getCombinedConfidence());
        
        // If stress is detected, also notify listeners
        if (result.isStressDetected()) {
            for (AuthenticationListener authListener : listeners) {
                authListener.onStressDetected(userId, 
                        result.getFactors().behavioralFactor != null ? 
                        1.0f - result.getFactors().behavioralFactor.confidence : 0.5f);
            }
        }
    }
    
    /**
     * Handle failed authentication
     * @param listener Authentication result listener
     * @param reason Reason for failure
     */
    private void handleFailedAuthentication(final AuthResultListener listener, String reason) {
        // Increment consecutive failures
        consecutiveFailures++;
        
        // Notify listener
        if (listener != null) {
            listener.onAuthenticationFailed(reason);
            
            // Check if too many failures
            int maxFailures = getMaxFailures();
            if (consecutiveFailures >= maxFailures) {
                listener.onTooManyFailedAttempts(consecutiveFailures);
                listener.onAlternativeAuthRequired();
            }
        }
        
        Log.d(TAG, "Multi-factor authentication failed: " + reason + 
                " (consecutive failures: " + consecutiveFailures + ")");
    }
    
    /**
     * Get maximum allowed failures based on security level
     * @return Max failures
     */
    private int getMaxFailures() {
        switch (securityLevel) {
            case HIGH:
                return MAX_FAILURES_HIGH;
                
            case MEDIUM:
                return MAX_FAILURES_MEDIUM;
                
            case LOW:
            default:
                return MAX_FAILURES_LOW;
        }
    }
    
    /**
     * Start voice enrollment process
     * @param userId User ID
     * @param enrollmentText Text for user to speak during enrollment
     * @param listener Listener for enrollment events
     */
    public void startEnrollment(String userId, String enrollmentText, final EnrollmentListener listener) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.WRITE)) {
            if (listener != null) {
                listener.onEnrollmentFailed("Access denied for voice enrollment");
            }
            return;
        }
        
        // Set current user ID
        this.currentUserId = userId;
        
        // Start biometric enrollment
        biometricAuth.startEnrollment(enrollmentText, new VoiceBiometricAuthenticator.EnrollmentListener() {
            @Override
            public void onEnrollmentAttempt(int attemptNumber, int maxAttempts) {
                if (listener != null) {
                    listener.onEnrollmentAttempt(attemptNumber, maxAttempts);
                }
            }
            
            @Override
            public void onEnrollmentRecordingFailed(String reason) {
                if (listener != null) {
                    listener.onEnrollmentRecordingFailed(reason);
                }
            }
            
            @Override
            public void onEnrollmentComplete() {
                if (listener != null) {
                    listener.onEnrollmentComplete();
                }
            }
            
            @Override
            public void onEnrollmentFailed(String reason) {
                if (listener != null) {
                    listener.onEnrollmentFailed(reason);
                }
            }
        });
    }
    
    /**
     * Authenticate with alternative method (e.g., passcode, security question)
     * @param userId User ID
     * @param alternativeAuthData Data for alternative authentication (e.g., passcode)
     * @param authType Type of alternative authentication
     * @param listener Listener for authentication events
     */
    public void authenticateWithAlternative(String userId, String alternativeAuthData, 
                                           VoiceBiometricAuthenticator.AlternativeAuthType authType,
                                           final AuthResultListener listener) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.EXECUTE)) {
            if (listener != null) {
                listener.onAuthenticationFailed("Access denied for alternative authentication");
            }
            return;
        }
        
        // Set current user ID
        this.currentUserId = userId;
        
        // Use biometric authenticator for alternative auth
        biometricAuth.authenticateWithAlternative(alternativeAuthData, authType, 
                new VoiceBiometricAuthenticator.AuthenticationListener() {
            @Override
            public void onAuthenticationRecordingFailed(String reason) {
                // Not applicable for alternative auth
            }
            
            @Override
            public void onAuthenticationSuccessful(float confidence) {
                // Not applicable for alternative auth
            }
            
            @Override
            public void onAuthenticationFailed(String reason) {
                handleFailedAuthentication(listener, "Alternative authentication failed: " + reason);
            }
            
            @Override
            public void onTooManyFailedAttempts(int attempts) {
                if (listener != null) {
                    listener.onTooManyFailedAttempts(attempts);
                }
            }
            
            @Override
            public void onAlternativeAuthRequired() {
                if (listener != null) {
                    listener.onAlternativeAuthRequired();
                }
            }
            
            @Override
            public void onEnrollmentRequired() {
                // Not applicable for alternative auth
            }
            
            @Override
            public void onAlternativeAuthSuccessful(VoiceBiometricAuthenticator.AlternativeAuthType authType) {
                // Create simple auth factors with just biometric factor
                AuthFactors factors = new AuthFactors();
                factors.biometricFactor = new Factor(true, 0.7f); // Reduced confidence for alternative auth
                factors.usedAlternative = true;
                factors.alternativeType = authType;
                
                // Create authentication result with just this factor
                AuthenticationResult result = new AuthenticationResult(true, 0.7f, factors, false);
                
                handleSuccessfulAuthentication(userId, listener, result);
            }
        }, true);
    }
    
    /**
     * Set security level
     * @param level New security level
     */
    public void setSecurityLevel(SecurityLevel level) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.ADMIN)) {
            return;
        }
        
        this.securityLevel = level;
        Log.d(TAG, "Set security level to " + level);
    }
    
    /**
     * Get security level
     * @return Current security level
     */
    public SecurityLevel getSecurityLevel() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.READ_ONLY)) {
            return null;
        }
        
        return securityLevel;
    }
    
    /**
     * Reset failed authentication attempts
     */
    public void resetFailedAttempts() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.WRITE)) {
            return;
        }
        
        consecutiveFailures = 0;
        biometricAuth.resetFailedAttempts();
        Log.d(TAG, "Reset failed authentication attempts");
    }
    
    /**
     * Get time since last successful authentication
     * @return Time in milliseconds since last auth, or -1 if no successful auth yet
     */
    public long getTimeSinceLastAuth() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.READ_ONLY)) {
            return -1;
        }
        
        if (lastSuccessfulAuth == 0) {
            return -1;
        }
        
        return System.currentTimeMillis() - lastSuccessfulAuth;
    }
    
    /**
     * Check if authentication is still valid (within time limit)
     * @param maxValidTime Maximum valid time in milliseconds
     * @return True if auth is still valid
     */
    public boolean isAuthStillValid(long maxValidTime) {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.READ_ONLY)) {
            return false;
        }
        
        if (lastSuccessfulAuth == 0) {
            return false;
        }
        
        return (System.currentTimeMillis() - lastSuccessfulAuth) < maxValidTime;
    }
    
    /**
     * Add authentication listener
     * @param listener Listener to add
     */
    public void addAuthenticationListener(AuthenticationListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove authentication listener
     * @param listener Listener to remove
     */
    public void removeAuthenticationListener(AuthenticationListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Get biometric authenticator
     * @return VoiceBiometricAuthenticator
     */
    public VoiceBiometricAuthenticator getBiometricAuthenticator() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.READ_ONLY)) {
            return null;
        }
        
        return biometricAuth;
    }
    
    /**
     * Get behavioral analyzer
     * @return BehavioralVoiceAnalyzer
     */
    public BehavioralVoiceAnalyzer getBehavioralAnalyzer() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.READ_ONLY)) {
            return null;
        }
        
        return behavioralAnalyzer;
    }
    
    /**
     * Get synthetic detector
     * @return SyntheticVoiceDetector
     */
    public SyntheticVoiceDetector getSyntheticDetector() {
        // Verify access permission
        if (!verifyAccess(AccessControl.SecurityZone.VOICE, AccessControl.PermissionLevel.READ_ONLY)) {
            return null;
        }
        
        return syntheticDetector;
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
     * Security levels for authentication
     */
    public enum SecurityLevel {
        /**
         * Low security - flexible authentication, allows some failures
         */
        LOW,
        
        /**
         * Medium security - requires biometrics, limited failures
         */
        MEDIUM,
        
        /**
         * High security - requires all factors, very limited failures
         */
        HIGH
    }
    
    /**
     * Authentication factor result
     */
    public static class Factor {
        public boolean success;
        public float confidence;
        
        public Factor(boolean success, float confidence) {
            this.success = success;
            this.confidence = confidence;
        }
    }
    
    /**
     * Combined authentication factors
     */
    public static class AuthFactors {
        public Factor biometricFactor;
        public Factor syntheticFactor;
        public Factor behavioralFactor;
        public boolean usedAlternative;
        public VoiceBiometricAuthenticator.AlternativeAuthType alternativeType;
        
        public AuthFactors() {
            this.biometricFactor = null;
            this.syntheticFactor = null;
            this.behavioralFactor = null;
            this.usedAlternative = false;
            this.alternativeType = null;
        }
    }
    
    /**
     * Authentication result
     */
    public static class AuthenticationResult {
        private boolean success;
        private float combinedConfidence;
        private AuthFactors factors;
        private boolean stressDetected;
        
        public AuthenticationResult(boolean success, float combinedConfidence, 
                                  AuthFactors factors, boolean stressDetected) {
            this.success = success;
            this.combinedConfidence = combinedConfidence;
            this.factors = factors;
            this.stressDetected = stressDetected;
        }
        
        /**
         * Is authentication successful
         * @return True if successful
         */
        public boolean isSuccess() {
            return success;
        }
        
        /**
         * Get combined confidence score
         * @return Confidence score (0-1)
         */
        public float getCombinedConfidence() {
            return combinedConfidence;
        }
        
        /**
         * Get authentication factors
         * @return Authentication factors
         */
        public AuthFactors getFactors() {
            return factors;
        }
        
        /**
         * Is stress detected
         * @return True if stress is detected
         */
        public boolean isStressDetected() {
            return stressDetected;
        }
        
        /**
         * Was alternative authentication used
         * @return True if alternative auth was used
         */
        public boolean wasAlternativeUsed() {
            return factors != null && factors.usedAlternative;
        }
        
        /**
         * Get alternative authentication type
         * @return Alternative authentication type or null if not used
         */
        public VoiceBiometricAuthenticator.AlternativeAuthType getAlternativeType() {
            return factors != null ? factors.alternativeType : null;
        }
    }
    
    /**
     * Authentication result listener interface
     */
    public interface AuthResultListener {
        /**
         * Called when authentication is successful
         * @param result Authentication result
         */
        void onAuthenticationSuccessful(AuthenticationResult result);
        
        /**
         * Called when authentication fails
         * @param reason Reason for failure
         */
        void onAuthenticationFailed(String reason);
        
        /**
         * Called when too many failed attempts occur
         * @param attempts Number of failed attempts
         */
        void onTooManyFailedAttempts(int attempts);
        
        /**
         * Called when alternative authentication is required
         */
        void onAlternativeAuthRequired();
        
        /**
         * Called when enrollment is required
         * @param enrollmentText Text for enrollment
         */
        void onEnrollmentRequired(String enrollmentText);
    }
    
    /**
     * Authentication listener interface
     */
    public interface AuthenticationListener {
        /**
         * Called when user is authenticated
         * @param userId User ID
         * @param result Authentication result
         */
        void onUserAuthenticated(String userId, AuthenticationResult result);
        
        /**
         * Called when synthetic voice is detected
         * @param confidence Confidence score (0-1)
         * @param isCloned Whether the voice appears to be cloned
         */
        void onSyntheticVoiceDetected(float confidence, boolean isCloned);
        
        /**
         * Called when behavioral anomaly is detected
         * @param userId User ID
         * @param anomalyScore Anomaly score (0-1)
         */
        void onBehavioralAnomaly(String userId, float anomalyScore);
        
        /**
         * Called when stress is detected
         * @param userId User ID
         * @param stressScore Stress score (0-1)
         */
        void onStressDetected(String userId, float stressScore);
        
        /**
         * Called when identity mismatch is detected
         * @param userId User ID
         * @param identityScore Identity score (0-1)
         */
        void onIdentityMismatch(String userId, float identityScore);
    }
    
    /**
     * Enrollment listener interface
     */
    public interface EnrollmentListener {
        /**
         * Called when enrollment attempt starts
         * @param attemptNumber Current attempt number
         * @param maxAttempts Maximum number of attempts
         */
        void onEnrollmentAttempt(int attemptNumber, int maxAttempts);
        
        /**
         * Called when enrollment recording fails
         * @param reason Reason for failure
         */
        void onEnrollmentRecordingFailed(String reason);
        
        /**
         * Called when enrollment completes successfully
         */
        void onEnrollmentComplete();
        
        /**
         * Called when enrollment fails
         * @param reason Reason for failure
         */
        void onEnrollmentFailed(String reason);
    }
}
