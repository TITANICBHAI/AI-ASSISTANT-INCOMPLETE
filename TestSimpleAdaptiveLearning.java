import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Simplified test for the Adaptive Learning System that focuses specifically
 * on discovering a known winning pattern to demonstrate learning capability.
 */
public class TestSimpleAdaptiveLearning {
    // The winning sequence
    private static final List<String> WINNING_SEQUENCE = Arrays.asList("action_2", "action_0", "action_3");
    
    // Random generator
    private static final Random random = new Random();
    
    // Track performance
    private static int totalAttempts = 0;
    private static int highestScore = 0;
    private static List<String> bestStrategy = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("=== Simplified Adaptive Learning System Test ===");
        System.out.println("Goal: Discover the winning sequence: " + WINNING_SEQUENCE);
        System.out.println("Starting learning process...\n");
        
        // Population of strategies
        List<List<String>> population = new ArrayList<>();
        
        // Generate initial random strategies
        for (int i = 0; i < 10; i++) {
            population.add(generateRandomStrategy());
        }
        
        // Learning loop
        boolean solutionFound = false;
        int generationCount = 0;
        
        while (!solutionFound && generationCount < 20) {
            generationCount++;
            System.out.println("\n--- Generation " + generationCount + " ---");
            
            // Evaluate all strategies
            Map<List<String>, Integer> scores = new HashMap<>();
            
            for (List<String> strategy : population) {
                int score = evaluateStrategy(strategy);
                scores.put(strategy, score);
                
                if (score == 100) {
                    solutionFound = true;
                    System.out.println("\nSOLUTION FOUND in generation " + generationCount);
                    System.out.println("Winning strategy: " + strategy);
                    break;
                }
            }
            
            if (solutionFound) break;
            
            // Sort strategies by score
            population.sort((s1, s2) -> Integer.compare(scores.getOrDefault(s2, 0), scores.getOrDefault(s1, 0)));
            
            // Print top strategies
            System.out.println("Top strategies this generation:");
            for (int i = 0; i < Math.min(3, population.size()); i++) {
                List<String> strategy = population.get(i);
                System.out.println((i+1) + ". " + strategy + " (Score: " + scores.get(strategy) + ")");
            }
            
            // Create next generation
            List<List<String>> newPopulation = new ArrayList<>();
            
            // Elitism - keep top strategies
            int eliteCount = Math.min(2, population.size());
            for (int i = 0; i < eliteCount; i++) {
                newPopulation.add(new ArrayList<>(population.get(i)));
            }
            
            // Pattern-driven strategies
            if (generationCount > 3) {
                List<String> pattern = discoverPattern(population, scores);
                if (pattern != null && !pattern.isEmpty()) {
                    System.out.println("Discovered pattern: " + pattern);
                    
                    // Create strategies using the pattern
                    for (int i = 0; i < 3; i++) {
                        List<String> patternStrategy = new ArrayList<>(pattern);
                        
                        // Add some random actions
                        if (random.nextBoolean()) {
                            patternStrategy.add("action_" + random.nextInt(5));
                        }
                        if (random.nextBoolean() && random.nextBoolean()) {
                            patternStrategy.add(0, "action_" + random.nextInt(5));
                        }
                        
                        newPopulation.add(patternStrategy);
                    }
                }
            }
            
            // Crossover and Mutation
            while (newPopulation.size() < 15) {
                // Select parents
                List<String> parent1 = selectParent(population, scores);
                List<String> parent2 = selectParent(population, scores);
                
                // Crossover
                List<String> child = crossover(parent1, parent2);
                
                // Mutation
                if (random.nextFloat() < 0.3) {
                    mutate(child);
                }
                
                newPopulation.add(child);
            }
            
            // Add some random strategies for exploration
            for (int i = 0; i < 5; i++) {
                newPopulation.add(generateRandomStrategy());
            }
            
            // Replace population
            population = newPopulation;
        }
        
        System.out.println("\n=== Learning Complete ===");
        System.out.println("Total attempts: " + totalAttempts);
        
        if (!solutionFound) {
            System.out.println("Solution not found. Best score: " + highestScore);
            System.out.println("Best strategy found: " + bestStrategy);
        }
    }
    
    /**
     * Generate a random strategy (sequence of actions)
     */
    private static List<String> generateRandomStrategy() {
        int length = 2 + random.nextInt(4); // 2-5 actions
        List<String> strategy = new ArrayList<>(length);
        
        for (int i = 0; i < length; i++) {
            strategy.add("action_" + random.nextInt(5));
        }
        
        return strategy;
    }
    
    /**
     * Evaluate a strategy by checking against the winning sequence
     */
    private static int evaluateStrategy(List<String> strategy) {
        totalAttempts++;
        
        // Check if strategy contains winning sequence
        for (int i = 0; i <= strategy.size() - WINNING_SEQUENCE.size(); i++) {
            boolean match = true;
            for (int j = 0; j < WINNING_SEQUENCE.size(); j++) {
                if (!strategy.get(i + j).equals(WINNING_SEQUENCE.get(j))) {
                    match = false;
                    break;
                }
            }
            
            if (match) {
                return 100; // Perfect match!
            }
        }
        
        // Check for prefix match
        int matchCount = 0;
        for (int i = 0; i < Math.min(strategy.size(), WINNING_SEQUENCE.size()); i++) {
            if (strategy.get(i).equals(WINNING_SEQUENCE.get(i))) {
                matchCount++;
            } else {
                break; // Stop at first mismatch
            }
        }
        
        int score = matchCount * 33;
        
        // Track best strategy
        if (score > highestScore) {
            highestScore = score;
            bestStrategy = new ArrayList<>(strategy);
        }
        
        return score;
    }
    
    /**
     * Select a parent for crossover, biased towards better strategies
     */
    private static List<String> selectParent(List<List<String>> population, Map<List<String>, Integer> scores) {
        // Tournament selection
        int tournamentSize = 3;
        List<String> best = null;
        int bestScore = -1;
        
        for (int i = 0; i < tournamentSize; i++) {
            int idx = random.nextInt(population.size());
            List<String> candidate = population.get(idx);
            int score = scores.getOrDefault(candidate, 0);
            
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        
        return best;
    }
    
    /**
     * Perform crossover between two parent strategies
     */
    private static List<String> crossover(List<String> parent1, List<String> parent2) {
        List<String> child = new ArrayList<>();
        
        // Simple crossover - take beginning from parent1, end from parent2
        int crossPoint = parent1.isEmpty() ? 0 : random.nextInt(parent1.size() + 1);
        
        // Copy from parent1 up to crosspoint
        for (int i = 0; i < crossPoint; i++) {
            if (i < parent1.size()) {
                child.add(parent1.get(i));
            }
        }
        
        // Copy rest from parent2
        int parent2Start = parent2.isEmpty() ? 0 : random.nextInt(parent2.size());
        for (int i = parent2Start; i < parent2.size(); i++) {
            child.add(parent2.get(i));
        }
        
        return child;
    }
    
    /**
     * Mutate a strategy by changing, adding, or removing actions
     */
    private static void mutate(List<String> strategy) {
        int mutationType = random.nextInt(3);
        
        switch (mutationType) {
            case 0: // Change a random action
                if (!strategy.isEmpty()) {
                    int idx = random.nextInt(strategy.size());
                    strategy.set(idx, "action_" + random.nextInt(5));
                }
                break;
                
            case 1: // Add a random action
                int addIdx = strategy.isEmpty() ? 0 : random.nextInt(strategy.size() + 1);
                strategy.add(addIdx, "action_" + random.nextInt(5));
                break;
                
            case 2: // Remove a random action
                if (!strategy.isEmpty()) {
                    int idx = random.nextInt(strategy.size());
                    strategy.remove(idx);
                }
                break;
        }
    }
    
    /**
     * Try to discover patterns in successful strategies
     */
    private static List<String> discoverPattern(List<List<String>> population, Map<List<String>, Integer> scores) {
        // Find top scoring strategies
        List<List<String>> topStrategies = new ArrayList<>();
        for (List<String> strategy : population) {
            if (scores.getOrDefault(strategy, 0) >= 66) { // At least two matches
                topStrategies.add(strategy);
            }
        }
        
        if (topStrategies.size() < 2) return null;
        
        // Look for common prefix
        for (int prefixLength = Math.min(3, WINNING_SEQUENCE.size()); prefixLength >= 2; prefixLength--) {
            Map<String, Integer> prefixCounts = new HashMap<>();
            
            for (List<String> strategy : topStrategies) {
                if (strategy.size() >= prefixLength) {
                    StringBuilder prefix = new StringBuilder();
                    for (int i = 0; i < prefixLength; i++) {
                        prefix.append(strategy.get(i));
                        if (i < prefixLength - 1) {
                            prefix.append(",");
                        }
                    }
                    
                    String prefixStr = prefix.toString();
                    prefixCounts.put(prefixStr, prefixCounts.getOrDefault(prefixStr, 0) + 1);
                }
            }
            
            // Find most common prefix
            String mostCommonPrefix = null;
            int maxCount = 0;
            
            for (Map.Entry<String, Integer> entry : prefixCounts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostCommonPrefix = entry.getKey();
                }
            }
            
            // If a common prefix is found in multiple high-scoring strategies
            if (mostCommonPrefix != null && maxCount >= 2) {
                String[] actions = mostCommonPrefix.split(",");
                List<String> pattern = new ArrayList<>();
                for (String action : actions) {
                    pattern.add(action);
                }
                return pattern;
            }
        }
        
        return null;
    }
}
