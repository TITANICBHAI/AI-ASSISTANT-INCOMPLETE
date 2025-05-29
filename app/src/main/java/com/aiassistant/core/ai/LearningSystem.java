package com.aiassistant.core.ai;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.actions.AIAction;
import com.aiassistant.core.ai.algorithms.DQNAlgorithm;
import com.aiassistant.core.ai.algorithms.PPOAlgorithm;
import com.aiassistant.core.ai.algorithms.RLAlgorithm;
import com.aiassistant.core.ai.algorithms.SARSAAlgorithm;
import com.aiassistant.data.AppDatabase;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.TrainingData;
import com.aiassistant.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Learning system for reinforcement learning algorithms
 */
public class LearningSystem {
    
    private static final String TAG = "LearningSystem";
    
    // Algorithm types
    public static final int ALGORITHM_DQN = 0;
    public static final int ALGORITHM_PPO = 1;
    public static final int ALGORITHM_SARSA = 2;
    
    // Default algorithm
    public static final int DEFAULT_ALGORITHM = ALGORITHM_DQN;
    
    // Context
    private final Context context;
    
    // Database
    private final AppDatabase database;
    
    // Algorithms
    private final Map<String, Map<Integer, RLAlgorithm>> algorithms;
    
    // Random number generator
    private final Random random = new Random();
    
    /**
     * Constructor
     * 
     * @param context The context
     */
    public LearningSystem(Context context) {
        this.context = context.getApplicationContext();
        this.database = AppDatabase.getInstance(context);
        this.algorithms = new HashMap<>();
    }
    
    /**
     * Initialize algorithms for a game
     * 
     * @param gameId The game ID
     */
    public void initializeAlgorithms(String gameId) {
        if (gameId == null || gameId.isEmpty()) {
            Log.e(TAG, "Invalid game ID");
            return;
        }
        
        if (!algorithms.containsKey(gameId)) {
            algorithms.put(gameId, new HashMap<>());
        }
        
        Map<Integer, RLAlgorithm> gameAlgorithms = algorithms.get(gameId);
        
        // Initialize DQN
        if (!gameAlgorithms.containsKey(ALGORITHM_DQN)) {
            DQNAlgorithm dqn = new DQNAlgorithm();
            dqn.initialize(128, 10);
            gameAlgorithms.put(ALGORITHM_DQN, dqn);
        }
        
        // Initialize PPO
        if (!gameAlgorithms.containsKey(ALGORITHM_PPO)) {
            PPOAlgorithm ppo = new PPOAlgorithm();
            ppo.initialize(128, 10);
            gameAlgorithms.put(ALGORITHM_PPO, ppo);
        }
        
        // Initialize SARSA
        if (!gameAlgorithms.containsKey(ALGORITHM_SARSA)) {
            SARSAAlgorithm sarsa = new SARSAAlgorithm();
            sarsa.initialize(128, 10);
            gameAlgorithms.put(ALGORITHM_SARSA, sarsa);
        }
        
        Log.d(TAG, "Initialized algorithms for game " + gameId);
    }
    
    /**
     * Get an algorithm
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @return The algorithm
     */
    public RLAlgorithm getAlgorithm(String gameId, int algorithmType) {
        if (gameId == null || gameId.isEmpty()) {
            Log.e(TAG, "Invalid game ID");
            return null;
        }
        
        if (!algorithms.containsKey(gameId)) {
            initializeAlgorithms(gameId);
        }
        
        Map<Integer, RLAlgorithm> gameAlgorithms = algorithms.get(gameId);
        
        if (!gameAlgorithms.containsKey(algorithmType)) {
            Log.e(TAG, "Algorithm type " + algorithmType + " not found for game " + gameId);
            return gameAlgorithms.get(DEFAULT_ALGORITHM);
        }
        
        return gameAlgorithms.get(algorithmType);
    }
    
    /**
     * Choose an action
     * 
     * @param gameId The game ID
     * @param state The game state
     * @param availableActions The available actions
     * @param algorithmType The algorithm type
     * @return The chosen action
     */
    public AIAction chooseAction(String gameId, GameState state, List<AIAction> availableActions, int algorithmType) {
        RLAlgorithm algorithm = getAlgorithm(gameId, algorithmType);
        
        if (algorithm != null) {
            return algorithm.chooseAction(state, availableActions);
        }
        
        // Fallback to random choice
        if (availableActions != null && !availableActions.isEmpty()) {
            return availableActions.get(random.nextInt(availableActions.size()));
        }
        
        return null;
    }
    
    /**
     * Update an algorithm
     * 
     * @param gameId The game ID
     * @param state The game state
     * @param action The action
     * @param reward The reward
     * @param nextState The next state
     * @param done Whether the episode is done
     * @param algorithmType The algorithm type
     */
    public void update(String gameId, GameState state, AIAction action, float reward, GameState nextState, boolean done, int algorithmType) {
        if (gameId == null || gameId.isEmpty() || state == null || action == null || nextState == null) {
            Log.e(TAG, "Invalid update parameters");
            return;
        }
        
        // Update algorithm
        RLAlgorithm algorithm = getAlgorithm(gameId, algorithmType);
        if (algorithm != null) {
            algorithm.update(state, action, reward, nextState, done);
        }
        
        // Store training data
        storeTrainingData(gameId, state, action, reward, nextState, done);
    }
    
    /**
     * Store training data
     * 
     * @param gameId The game ID
     * @param state The game state
     * @param action The action
     * @param reward The reward
     * @param nextState The next state
     * @param done Whether the episode is done
     */
    private void storeTrainingData(String gameId, GameState state, AIAction action, float reward, GameState nextState, boolean done) {
        try {
            // Convert states and action to JSON
            String stateJson = ""; // In a real implementation, this would use Gson
            String actionJson = "";
            String nextStateJson = "";
            
            // Create training data
            TrainingData trainingData = new TrainingData();
            trainingData.setGameId(gameId);
            trainingData.setStateJson(stateJson);
            trainingData.setActionJson(actionJson);
            trainingData.setReward(reward);
            trainingData.setNextStateJson(nextStateJson);
            trainingData.setTerminal(done);
            trainingData.setSourceType(0); // 0 = user
            
            // Store in database
            database.trainingDataDao().insert(trainingData);
            
        } catch (Exception e) {
            Log.e(TAG, "Error storing training data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Train models
     * 
     * @param gameId The game ID
     * @param steps The number of steps
     */
    public void trainModels(String gameId, int steps) {
        if (gameId == null || gameId.isEmpty()) {
            Log.e(TAG, "Invalid game ID");
            return;
        }
        
        if (!algorithms.containsKey(gameId)) {
            initializeAlgorithms(gameId);
        }
        
        Map<Integer, RLAlgorithm> gameAlgorithms = algorithms.get(gameId);
        
        // Train each algorithm
        for (Map.Entry<Integer, RLAlgorithm> entry : gameAlgorithms.entrySet()) {
            int algorithmType = entry.getKey();
            RLAlgorithm algorithm = entry.getValue();
            
            Log.d(TAG, "Training algorithm " + algorithmType + " for game " + gameId + " with " + steps + " steps");
            
            try {
                algorithm.train(steps);
                
                // Save model
                String modelPath = context.getFilesDir() + "/models/" + gameId + "_" + algorithmType + ".model";
                algorithm.save(modelPath);
                
            } catch (Exception e) {
                Log.e(TAG, "Error training algorithm: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Load models
     * 
     * @param gameId The game ID
     */
    public void loadModels(String gameId) {
        if (gameId == null || gameId.isEmpty()) {
            Log.e(TAG, "Invalid game ID");
            return;
        }
        
        if (!algorithms.containsKey(gameId)) {
            initializeAlgorithms(gameId);
        }
        
        Map<Integer, RLAlgorithm> gameAlgorithms = algorithms.get(gameId);
        
        // Load each algorithm
        for (Map.Entry<Integer, RLAlgorithm> entry : gameAlgorithms.entrySet()) {
            int algorithmType = entry.getKey();
            RLAlgorithm algorithm = entry.getValue();
            
            try {
                String modelPath = context.getFilesDir() + "/models/" + gameId + "_" + algorithmType + ".model";
                algorithm.load(modelPath);
                
                Log.d(TAG, "Loaded model for algorithm " + algorithmType + " and game " + gameId);
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading model: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Reset models
     * 
     * @param gameId The game ID
     */
    public void resetModels(String gameId) {
        if (gameId == null || gameId.isEmpty()) {
            Log.e(TAG, "Invalid game ID");
            return;
        }
        
        if (algorithms.containsKey(gameId)) {
            algorithms.remove(gameId);
        }
        
        initializeAlgorithms(gameId);
        
        Log.d(TAG, "Reset models for game " + gameId);
    }
}
