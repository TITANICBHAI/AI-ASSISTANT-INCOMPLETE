package com.aiassistant.learning.algorithms;

import android.util.Log;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.data.models.GameState;
import com.aiassistant.core.ai.algorithms.RLAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * SARSA (State-Action-Reward-State-Action) reinforcement learning algorithm implementation
 */
public class SARSAAgent implements RLAlgorithm {
    private static final String TAG = "SARSAAgent";
    
    // Model parameters
    private Map<String, float[]> qTable; // Maps state hash to action values
    private float learningRate = 0.1f;
    private float discountFactor = 0.9f;
    private float explorationRate = 0.2f;
    
    private Random random;
    private Map<Integer, AIAction.ActionType> actionMapping = new HashMap<>();
    private AIAction lastAction = null;
    private String lastState = null;
    
    private static final int STATE_FEATURES = 50;
    private static final int NUM_ACTIONS = 10;
    
    /**
     * Constructor
     */
    public SARSAAgent() {
        qTable = new HashMap<>();
        random = new Random();
        
        // Initialize action mapping
        actionMapping.put(0, AIAction.ActionType.TAP);
        actionMapping.put(1, AIAction.ActionType.LONG_PRESS);
        actionMapping.put(2, AIAction.ActionType.SWIPE);
        actionMapping.put(3, AIAction.ActionType.DOUBLE_TAP);
        actionMapping.put(4, AIAction.ActionType.TEXT_INPUT);
        actionMapping.put(5, AIAction.ActionType.WAIT);
        actionMapping.put(6, AIAction.ActionType.BACK);
        actionMapping.put(7, AIAction.ActionType.HOME);
        actionMapping.put(8, AIAction.ActionType.MULTI_TOUCH);
        actionMapping.put(9, AIAction.ActionType.NOOP);
    }
    
    @Override
    public String getName() {
        return "SARSA";
    }
    
    @Override
    public AIAction selectAction(GameState gameState) {
        // Convert state to feature vector
        float[] features = gameState.toFeatureVector();
        
        // Get state hash
        String stateHash = hashState(features);
        
        // Get or initialize Q-values for this state
        float[] qValues = getQValues(stateHash);
        
        // Select action (epsilon-greedy)
        int actionIndex = selectActionIndex(qValues);
        
        // Map to AIAction
        AIAction action = createAction(actionMapping.get(actionIndex));
        
        // remember last state and action for learning
        lastState = stateHash;
        lastAction = action;
        
        return action;
    }
    
    @Override
    public void learn(GameState prevState, AIAction action, float reward, GameState nextState) {
        if (prevState == null || action == null || nextState == null) {
            return;
        }
        
        // Convert states to feature vectors
        float[] prevFeatures = prevState.toFeatureVector();
        float[] nextFeatures = nextState.toFeatureVector();
        
        // Get state hashes
        String prevStateHash = hashState(prevFeatures);
        String nextStateHash = hashState(nextFeatures);
        
        // Get Q-values
        float[] prevQValues = getQValues(prevStateHash);
        float[] nextQValues = getQValues(nextStateHash);
        
        // Map action to index
        int actionIndex = getActionIndex(action.getActionType());
        
        // Select next action for SARSA update
        int nextActionIndex = selectActionIndex(nextQValues);
        
        // SARSA update rule: Q(s,a) = Q(s,a) + α[r + γQ(s',a') - Q(s,a)]
        float q = prevQValues[actionIndex];
        float nextQ = nextQValues[nextActionIndex];
        
        float update = learningRate * (reward + discountFactor * nextQ - q);
        prevQValues[actionIndex] = q + update;
        
        // Update Q-table
        qTable.put(prevStateHash, prevQValues);
    }
    
    @Override
    public void loadModel(byte[] modelData) {
        if (modelData == null || modelData.length == 0) {
            Log.w(TAG, "Empty model data");
            return;
        }
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(modelData);
             DataInputStream dis = new DataInputStream(bais)) {
            
            // Read parameters
            learningRate = dis.readFloat();
            discountFactor = dis.readFloat();
            explorationRate = dis.readFloat();
            
            // Read Q-table
            int tableSize = dis.readInt();
            qTable.clear();
            
            for (int i = 0; i < tableSize; i++) {
                String stateHash = dis.readUTF();
                float[] values = new float[NUM_ACTIONS];
                
                for (int j = 0; j < NUM_ACTIONS; j++) {
                    values[j] = dis.readFloat();
                }
                
                qTable.put(stateHash, values);
            }
            
            Log.d(TAG, "Loaded SARSA model with " + qTable.size() + " states");
            
        } catch (IOException e) {
            Log.e(TAG, "Error loading model: " + e.getMessage(), e);
        }
    }
    
    @Override
    public byte[] saveModel() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            
            // Write parameters
            dos.writeFloat(learningRate);
            dos.writeFloat(discountFactor);
            dos.writeFloat(explorationRate);
            
            // Write Q-table
            dos.writeInt(qTable.size());
            
            for (Map.Entry<String, float[]> entry : qTable.entrySet()) {
                dos.writeUTF(entry.getKey());
                
                float[] values = entry.getValue();
                for (int i = 0; i < NUM_ACTIONS; i++) {
                    dos.writeFloat(values[i]);
                }
            }
            
            dos.flush();
            return baos.toByteArray();
            
        } catch (IOException e) {
            Log.e(TAG, "Error saving model: " + e.getMessage(), e);
            return new byte[0];
        }
    }
    
    @Override
    public void setParameters(Map<String, Float> parameters) {
        if (parameters.containsKey("learningRate")) {
            learningRate = parameters.get("learningRate");
        }
        
        if (parameters.containsKey("discountFactor")) {
            discountFactor = parameters.get("discountFactor");
        }
        
        if (parameters.containsKey("explorationRate")) {
            explorationRate = parameters.get("explorationRate");
        }
    }
    
    @Override
    public Map<String, Float> getParameters() {
        Map<String, Float> params = new HashMap<>();
        params.put("learningRate", learningRate);
        params.put("discountFactor", discountFactor);
        params.put("explorationRate", explorationRate);
        return params;
    }
    
    @Override
    public void reset() {
        qTable.clear();
        lastAction = null;
        lastState = null;
    }
    
    /**
     * Hash a state vector to a string
     * 
     * @param stateVector The state vector
     * @return The state hash
     */
    private String hashState(float[] stateVector) {
        // Simple discretization and hashing
        StringBuilder sb = new StringBuilder();
        
        for (float value : stateVector) {
            // Discretize to 10 levels
            int level = Math.min(9, Math.max(0, (int) (value * 10)));
            sb.append(level);
        }
        
        return sb.toString();
    }
    
    /**
     * Get Q-values for a state
     * 
     * @param stateHash The state hash
     * @return The Q-values
     */
    private float[] getQValues(String stateHash) {
        // Get existing or initialize new
        float[] qValues = qTable.get(stateHash);
        
        if (qValues == null) {
            qValues = new float[NUM_ACTIONS];
            // Initialize with small random values
            for (int i = 0; i < NUM_ACTIONS; i++) {
                qValues[i] = 0.01f * random.nextFloat();
            }
            qTable.put(stateHash, qValues);
        }
        
        return qValues;
    }
    
    /**
     * Select an action index using epsilon-greedy policy
     * 
     * @param qValues The Q-values
     * @return The action index
     */
    private int selectActionIndex(float[] qValues) {
        // Epsilon-greedy action selection
        if (random.nextFloat() < explorationRate) {
            // Random action
            return random.nextInt(NUM_ACTIONS);
        } else {
            // Greedy action (highest Q-value)
            int bestAction = 0;
            float bestValue = qValues[0];
            
            for (int i = 1; i < NUM_ACTIONS; i++) {
                if (qValues[i] > bestValue) {
                    bestValue = qValues[i];
                    bestAction = i;
                }
            }
            
            return bestAction;
        }
    }
    
    /**
     * Get the index for an action type
     * 
     * @param actionType The action type
     * @return The action index
     */
    private int getActionIndex(AIAction.ActionType actionType) {
        for (Map.Entry<Integer, AIAction.ActionType> entry : actionMapping.entrySet()) {
            if (entry.getValue() == actionType) {
                return entry.getKey();
            }
        }
        
        // Default to NOOP
        return 9;
    }
    
    /**
     * Create an action from action type
     * 
     * @param actionType The action type
     * @return The action
     */
    private AIAction createAction(AIAction.ActionType actionType) {
        switch (actionType) {
            case TAP:
                return new AIAction(random.nextFloat() * 1080, random.nextFloat() * 2340);
                
            case LONG_PRESS:
                return new AIAction(
                        random.nextFloat() * 1080,
                        random.nextFloat() * 2340,
                        500 + random.nextInt(1500)
                );
                
            case SWIPE:
                return new AIAction(
                        random.nextFloat() * 1080,
                        random.nextFloat() * 2340,
                        random.nextFloat() * 1080,
                        random.nextFloat() * 2340,
                        100 + random.nextInt(400)
                );
                
            case WAIT:
                return new AIAction(1000 + random.nextInt(4000));
                
            case BACK:
            case HOME:
            case NOOP:
            default:
                return new AIAction(actionType);
        }
    }
}
