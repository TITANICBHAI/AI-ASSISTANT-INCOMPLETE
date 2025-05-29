import com.aiassistant.core.ai.AIStateManager;
import com.aiassistant.core.ai.algorithms.MetaLearningAlgorithm;

/**
 * Simple test for AI core components
 */
public class TestAICore {
    
    /**
     * Main method
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Testing AI core components...");
        
        // Test AIStateManager
        testAIStateManager();
        
        // Test MetaLearningAlgorithm
        testMetaLearningAlgorithm();
        
        System.out.println("All tests completed.");
    }
    
    /**
     * Test AIStateManager
     */
    private static void testAIStateManager() {
        System.out.println("\nTesting AIStateManager:");
        
        // Get instance
        AIStateManager manager = AIStateManager.getInstance();
        
        // Check initial state
        System.out.println("Initial state: " + manager.getCurrentState());
        
        // Start AI
        manager.start("com.example.game");
        
        // Set mode
        manager.setMode(AIStateManager.MODE_AUTO);
        
        // Train models
        manager.trainModels("game123", 1000);
        
        // Stop AI
        manager.stop();
        
        System.out.println("AIStateManager test completed.");
    }
    
    /**
     * Test MetaLearningAlgorithm
     */
    private static void testMetaLearningAlgorithm() {
        System.out.println("\nTesting MetaLearningAlgorithm:");
        
        // Create algorithm
        int inputSize = 10;
        int numAlgorithms = 3;
        MetaLearningAlgorithm algorithm = new MetaLearningAlgorithm(inputSize, numAlgorithms);
        
        // Create feature vector
        float[] features = new float[inputSize];
        for (int i = 0; i < inputSize; i++) {
            features[i] = (float) (Math.random() * 2 - 1); // Random values between -1 and 1
        }
        
        // Select algorithm
        int selectedAlgorithm = algorithm.selectAlgorithm(features);
        System.out.println("Selected algorithm: " + selectedAlgorithm);
        
        // Update with positive reward
        algorithm.update(features, selectedAlgorithm, 0.8f);
        
        // Save and load
        String filePath = "meta_learning_model.dat";
        boolean saveResult = algorithm.save(filePath);
        System.out.println("Save result: " + saveResult);
        
        boolean loadResult = algorithm.load(filePath);
        System.out.println("Load result: " + loadResult);
        
        // Reset
        algorithm.reset();
        
        System.out.println("MetaLearningAlgorithm test completed.");
    }
}
