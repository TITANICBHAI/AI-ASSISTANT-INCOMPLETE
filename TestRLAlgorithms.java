import com.aiassistant.core.ai.algorithms.RLAlgorithm;
import com.aiassistant.core.ai.algorithms.DQNAlgorithm;
import com.aiassistant.core.ai.algorithms.SARSAAlgorithm;
import com.aiassistant.core.ai.algorithms.PPOAlgorithm;
import com.aiassistant.data.models.AIAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Test all RL algorithm implementations
 */
public class TestRLAlgorithms {
    
    public static void main(String[] args) {
        System.out.println("\n=== RL Algorithms Test ===\n");
        
        // Create all algorithms
        List<RLAlgorithm> algorithms = new ArrayList<>();
        algorithms.add(new DQNAlgorithm());
        algorithms.add(new SARSAAlgorithm());
        algorithms.add(new PPOAlgorithm());
        
        // Test parameters
        int inputSize = 10;
        int outputSize = 4;
        float epsilon = 0.1f;
        
        // Initialize all algorithms
        System.out.println("Initializing algorithms...");
        for (RLAlgorithm algorithm : algorithms) {
            algorithm.initialize(inputSize, outputSize);
            System.out.println("  - " + algorithm.getName() + " initialized");
        }
        
        // Run simulation for each algorithm
        for (RLAlgorithm algorithm : algorithms) {
            System.out.println("\nTesting " + algorithm.getName() + ":");
            
            // Create random state
            float[] state = createRandomState(inputSize);
            
            // Run 5 steps
            for (int step = 0; step < 5; step++) {
                System.out.println("\nStep " + (step + 1) + ":");
                
                // Select action
                int action = algorithm.selectAction(state, epsilon);
                System.out.println("  Selected action: " + action);
                
                // Execute action
                AIAction aiAction = createAction(action);
                aiAction.execute();
                
                // Get reward
                float reward = 0.5f + (float)Math.random() * 0.5f; // Random reward between 0.5 and 1.0
                System.out.println("  Reward: " + reward);
                
                // Create next state
                float[] nextState = createRandomState(inputSize);
                
                // Update algorithm
                boolean done = (step == 4); // Last step is terminal
                algorithm.update(state, action, reward, nextState, done);
                
                // Update state
                state = nextState;
            }
            
            // Save model
            algorithm.save("model_" + algorithm.getName() + ".dat");
        }
        
        System.out.println("\n=== Test Complete ===");
    }
    
    /**
     * Create a random state
     * 
     * @param size The state size
     * @return The state
     */
    private static float[] createRandomState(int size) {
        float[] state = new float[size];
        for (int i = 0; i < size; i++) {
            state[i] = (float)Math.random() * 2 - 1; // Random values between -1 and 1
        }
        return state;
    }
    
    /**
     * Create an action based on action index
     * 
     * @param actionIndex The action index
     * @return The action
     */
    private static AIAction createAction(int actionIndex) {
        switch (actionIndex % 4) {
            case 0:
                return new AIAction(100, 200); // Tap
            case 1: {
                AIAction action = new AIAction(200, 300);
                action.setActionType(AIAction.ActionType.LONG_PRESS);
                action.setDuration(500);
                return action;
            }
            case 2: {
                AIAction action = new AIAction(300, 400);
                action.setActionType(AIAction.ActionType.SWIPE);
                action.setEndX(400);
                action.setEndY(500);
                return action;
            }
            default: {
                AIAction action = new AIAction();
                action.setActionType(AIAction.ActionType.WAIT);
                action.setDuration(200);
                return action;
            }
        }
    }
}
