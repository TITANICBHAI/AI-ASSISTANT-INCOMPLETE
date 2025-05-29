package com.aiassistant.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.aiassistant.core.ai.AIAction;
import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.ai.detection.UIElementDetector;
import com.aiassistant.core.game.GameAnalysisManager;
import com.aiassistant.core.game.GameDetector;
import com.aiassistant.core.interaction.AdvancedGameInteraction;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.GameType;
import com.aiassistant.data.models.UIElement;
import com.aiassistant.security.SecurityContext;
import com.aiassistant.utils.ScreenshotUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service that provides automated game interactions based on AI analysis.
 * This service monitors games in real-time, analyzes the screen content,
 * and uses the AdvancedGameInteraction system to perform intelligent actions.
 */
public class GameInteractionService extends Service {
    private static final String TAG = "GameInteractionService";
    
    // Core components
    private AdvancedGameInteraction gameInteraction;
    private GameAnalysisManager gameAnalysisManager;
    private UIElementDetector uiElementDetector;
    private GameDetector gameDetector;
    private AIStateManager aiStateManager;
    
    // Service state
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final AtomicBoolean isAutoPlaying = new AtomicBoolean(false);
    private String currentGamePackage;
    private GameType currentGameType;
    
    // Execution
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    // Analysis interval (ms)
    private long analysisInterval = 500;
    
    // Recent UI elements and actions
    private List<UIElement> lastDetectedElements = new ArrayList<>();
    private long lastAnalysisTime = 0;
    private long lastActionTime = 0;
    
    // Action settings
    private int actionCooldown = 1000; // ms between actions
    private float minActionConfidence = 0.7f; // minimum confidence for automated actions
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize core components
        gameInteraction = AdvancedGameInteraction.getInstance(this);
        aiStateManager = AIStateManager.getInstance();
        
        if (aiStateManager != null && aiStateManager.isInitialized()) {
            gameAnalysisManager = aiStateManager.getGameAnalysisManager();
            // Access other components through AI state manager
        } else {
            // Direct initialization if AI state manager is not available
            gameAnalysisManager = new GameAnalysisManager(this);
        }
        
        // Initialize detectors
        uiElementDetector = new UIElementDetector(this);
        gameDetector = new GameDetector(this);
        
        Log.d(TAG, "GameInteractionService created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Starting game interaction service");
        
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case "START":
                        startGameInteraction();
                        break;
                        
                    case "STOP":
                        stopGameInteraction();
                        break;
                        
                    case "PAUSE":
                        pauseGameInteraction();
                        break;
                        
                    case "RESUME":
                        resumeGameInteraction();
                        break;
                        
                    case "AUTO_PLAY":
                        boolean autoPlay = intent.getBooleanExtra("auto_play", false);
                        setAutoPlay(autoPlay);
                        break;
                }
            }
        }
        
        // If service was restarted after being killed
        if (!isRunning.get() && !isPaused.get()) {
            startGameInteraction();
        }
        
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        stopGameInteraction();
        executorService.shutdown();
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    /**
     * Start the game interaction monitoring
     */
    private void startGameInteraction() {
        if (isRunning.compareAndSet(false, true)) {
            Log.d(TAG, "Game interaction started");
            isPaused.set(false);
            
            // Load settings from AI state manager
            loadSettings();
            
            // Start analysis loop
            scheduleNextAnalysis();
        }
    }
    
    /**
     * Stop the game interaction monitoring
     */
    private void stopGameInteraction() {
        if (isRunning.compareAndSet(true, false)) {
            Log.d(TAG, "Game interaction stopped");
            handler.removeCallbacksAndMessages(null);
        }
    }
    
    /**
     * Pause the game interaction temporarily
     */
    private void pauseGameInteraction() {
        if (!isPaused.getAndSet(true)) {
            Log.d(TAG, "Game interaction paused");
            handler.removeCallbacksAndMessages(null);
        }
    }
    
    /**
     * Resume the game interaction after being paused
     */
    private void resumeGameInteraction() {
        if (isPaused.compareAndSet(true, false) && isRunning.get()) {
            Log.d(TAG, "Game interaction resumed");
            scheduleNextAnalysis();
        }
    }
    
    /**
     * Enable/disable auto-play mode
     */
    private void setAutoPlay(boolean enabled) {
        isAutoPlaying.set(enabled);
        Log.d(TAG, "Auto-play mode set to: " + enabled);
    }
    
    /**
     * Load settings from AI state manager
     */
    private void loadSettings() {
        if (aiStateManager != null && aiStateManager.isInitialized()) {
            try {
                String intervalStr = aiStateManager.getUserPreference("game_analysis_interval");
                if (intervalStr != null && !intervalStr.isEmpty()) {
                    analysisInterval = Integer.parseInt(intervalStr);
                }
                
                String cooldownStr = aiStateManager.getUserPreference("action_cooldown");
                if (cooldownStr != null && !cooldownStr.isEmpty()) {
                    actionCooldown = Integer.parseInt(cooldownStr);
                }
                
                String confidenceStr = aiStateManager.getUserPreference("min_action_confidence");
                if (confidenceStr != null && !confidenceStr.isEmpty()) {
                    minActionConfidence = Float.parseFloat(confidenceStr);
                }
                
                String humanizeStr = aiStateManager.getUserPreference("humanize_inputs");
                if (humanizeStr != null && !humanizeStr.isEmpty()) {
                    gameInteraction.setHumanizeInputs(Boolean.parseBoolean(humanizeStr));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading settings: " + e.getMessage());
            }
        }
    }
    
    /**
     * Schedule the next game analysis
     */
    private void scheduleNextAnalysis() {
        if (isRunning.get() && !isPaused.get()) {
            handler.postDelayed(this::analyzeGameState, analysisInterval);
        }
    }
    
    /**
     * Main analysis loop - analyze game state and perform actions
     */
    private void analyzeGameState() {
        if (!isRunning.get() || isPaused.get()) {
            return;
        }
        
        // Apply security context for all analysis operations
        SecurityContext.getInstance().setCurrentFeatureActive("game_analysis");
        
        try {
            // Only analyze at the configured interval
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastAnalysisTime < analysisInterval) {
                scheduleNextAnalysis();
                return;
            }
            
            lastAnalysisTime = currentTime;
            
            // Run analysis in the background
            executorService.execute(() -> {
                try {
                    // Get screenshot
                    Bitmap screenshot = ScreenshotUtils.takeScreenshot(this);
                    if (screenshot == null) {
                        handler.post(this::scheduleNextAnalysis);
                        return;
                    }
                    
                    // Detect current game
                    String packageName = AIAccessibilityService.getInstance() != null ?
                            AIAccessibilityService.getInstance().getCurrentPackage() : null;
                            
                    if (packageName != null && !packageName.equals(currentGamePackage)) {
                        // Game changed, update settings
                        currentGamePackage = packageName;
                        gameInteraction.updateGamePackage(packageName);
                        currentGameType = gameDetector.detectGameType(packageName);
                        Log.d(TAG, "Detected game: " + packageName + ", type: " + currentGameType);
                    }
                    
                    // Only proceed if we're in a game
                    if (currentGameType == GameType.UNKNOWN) {
                        handler.post(this::scheduleNextAnalysis);
                        return;
                    }
                    
                    // Detect UI elements
                    List<UIElement> uiElements = uiElementDetector.detectUIElements(screenshot);
                    lastDetectedElements = uiElements;
                    
                    // Analyze game state
                    GameState gameState = gameAnalysisManager.analyzeGameState(screenshot, uiElements);
                    
                    // If auto-play is enabled, determine and perform best action
                    if (isAutoPlaying.get()) {
                        performAutomatedAction(screenshot, gameState, uiElements);
                    }
                    
                    // Schedule next analysis
                    handler.post(this::scheduleNextAnalysis);
                } catch (Exception e) {
                    Log.e(TAG, "Error in game analysis: " + e.getMessage());
                    handler.post(this::scheduleNextAnalysis);
                }
            });
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Perform an automated action based on game analysis
     */
    private void performAutomatedAction(Bitmap screenshot, GameState gameState, List<UIElement> uiElements) {
        try {
            // Check cooldown period
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastActionTime < actionCooldown) {
                return;
            }
            
            // Get recommended action from game analysis manager
            AIAction recommendedAction = gameAnalysisManager.getRecommendedAction(gameState, uiElements);
            
            // Only perform the action if it has high enough confidence
            if (recommendedAction != null && recommendedAction.getConfidence() >= minActionConfidence) {
                boolean actionPerformed = executeAction(recommendedAction, uiElements);
                
                if (actionPerformed) {
                    lastActionTime = currentTime;
                    Log.d(TAG, "Performed automated action: " + recommendedAction.getActionType());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error performing automated action: " + e.getMessage());
        }
    }
    
    /**
     * Execute an AI action using the game interaction system
     */
    private boolean executeAction(AIAction action, List<UIElement> uiElements) {
        if (action == null) {
            return false;
        }
        
        SecurityContext.getInstance().setCurrentFeatureActive("game_interaction");
        
        try {
            switch (action.getActionType()) {
                case AIAction.ACTION_TAP:
                    // Simple tap
                    if (action.getPoints().size() >= 1) {
                        Point p = action.getPoints().get(0);
                        return gameInteraction.tap(p.x, p.y);
                    }
                    break;
                    
                case AIAction.ACTION_LONG_PRESS:
                    // Long press
                    if (action.getPoints().size() >= 1) {
                        Point p = action.getPoints().get(0);
                        return gameInteraction.longPress(p.x, p.y);
                    }
                    break;
                    
                case AIAction.ACTION_SWIPE:
                    // Swipe
                    if (action.getPoints().size() >= 2) {
                        Point p1 = action.getPoints().get(0);
                        Point p2 = action.getPoints().get(1);
                        return gameInteraction.swipe(p1.x, p1.y, p2.x, p2.y, action.getDuration());
                    }
                    break;
                    
                case AIAction.ACTION_MULTI_TOUCH:
                    // Multi-touch gesture
                    if (action.getPoints().size() >= 4) {
                        Point p1 = action.getPoints().get(0);
                        Point p2 = action.getPoints().get(1);
                        Point p3 = action.getPoints().get(2);
                        Point p4 = action.getPoints().get(3);
                        return gameInteraction.multiTouchGesture(
                                p1.x, p1.y, p2.x, p2.y,
                                p3.x, p3.y, p4.x, p4.y,
                                action.getDuration());
                    }
                    break;
                    
                case AIAction.ACTION_TAP_ELEMENT:
                    // Tap a specific UI element
                    if (action.getTargetElementId() != null) {
                        // Find element by ID
                        for (UIElement element : uiElements) {
                            if (action.getTargetElementId().equals(element.getElementId())) {
                                return gameInteraction.clickUIElement(element);
                            }
                        }
                    } else if (action.getPoints().size() >= 1) {
                        // Fallback to coordinates if element ID is not provided
                        Point p = action.getPoints().get(0);
                        return gameInteraction.tap(p.x, p.y);
                    }
                    break;
                    
                case AIAction.ACTION_COMBO:
                    // Execute a predefined combo
                    if (action.getComboName() != null) {
                        return gameInteraction.performCombo(action.getComboName());
                    }
                    break;
                    
                case AIAction.ACTION_COMBAT_MANEUVER:
                    // Execute a complex combat maneuver
                    if (action.getCombatManeuver() != null && action.getPoints().size() >= 1) {
                        Point target = action.getPoints().get(0);
                        return gameInteraction.performCombatManeuver(action.getCombatManeuver(), target);
                    }
                    break;
                    
                case AIAction.ACTION_WAIT:
                    // Wait for specified duration
                    try {
                        Thread.sleep(action.getDuration());
                        return true;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    break;
            }
            
            return false;
        } finally {
            SecurityContext.getInstance().clearCurrentFeatureActive();
        }
    }
    
    /**
     * Get the last detected UI elements
     */
    public List<UIElement> getLastDetectedElements() {
        return new ArrayList<>(lastDetectedElements);
    }
    
    /**
     * Get the current game package
     */
    public String getCurrentGamePackage() {
        return currentGamePackage;
    }
    
    /**
     * Get the current game type
     */
    public GameType getCurrentGameType() {
        return currentGameType;
    }
    
    /**
     * Check if auto-play is enabled
     */
    public boolean isAutoPlayEnabled() {
        return isAutoPlaying.get();
    }
    
    /**
     * Check if the service is running
     */
    public boolean isServiceRunning() {
        return isRunning.get();
    }
    
    /**
     * Check if the service is paused
     */
    public boolean isServicePaused() {
        return isPaused.get();
    }
}
