import com.aiassistant.core.ai.algorithms.DQNAlgorithm;
import com.aiassistant.data.models.AIAction;

public class TestDQN {
    public static void main(String[] args) {
        System.out.println("Testing DQN implementation...");
        
        // Create and initialize DQN
        DQNAlgorithm dqn = new DQNAlgorithm();
        dqn.initialize(10, 5);
        
        // Create a test state
        float[] state = new float[10];
        for (int i = 0; i < state.length; i++) {
            state[i] = (float)Math.random();
        }
        
        // Select an action with exploration (epsilon = 0.3)
        int action = dqn.selectAction(state, 0.3f);
        System.out.println("Selected action: " + action);
        
        // Create an AIAction based on this
        AIAction aiAction = new AIAction(100 * action, 200);
        System.out.println("AIAction: " + aiAction);
        
        // Execute action
        boolean result = aiAction.execute();
        System.out.println("Execution result: " + result);
        
        // Create next state
        float[] nextState = new float[10];
        for (int i = 0; i < nextState.length; i++) {
            nextState[i] = (float)Math.random();
        }
        
        // Update DQN
        dqn.update(state, action, 1.0f, nextState, false);
        
        System.out.println("DQN test completed.");
    }
}
