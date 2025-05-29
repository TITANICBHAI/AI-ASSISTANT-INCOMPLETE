import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Test class that simulates the Adaptive Learning System in a game environment
 * to demonstrate its capabilities for discovering strategies without predefined patterns.
 */
public class TestAdaptiveLearning {
    
    // Simplified mockups of the AdaptiveLearningSystem classes for testing
    static class AdaptiveLearningSystem {
        private Map<String, LearningSession> sessions = new HashMap<>();
        private Map<String, List<Strategy>> strategies = new HashMap<>();
        private Map<String, List<Pattern>> patterns = new HashMap<>();
        private Random random = new Random();
        
        public String createSession(String domain) {
            String sessionId = "session_" + System.currentTimeMillis();
            sessions.put(sessionId, new LearningSession(sessionId, domain));
            
            // Initialize collections for domain
            if (!strategies.containsKey(domain)) {
                strategies.put(domain, new ArrayList<>());
                patterns.put(domain, new ArrayList<>());
            }
            
            System.out.println("Created learning session: " + sessionId + " for domain: " + domain);
            return sessionId;
        }
        
        public void recordObservation(String sessionId, Map<String, Object> state, 
                                      List<String> actions, Map<String, Object> outcome) {
            LearningSession session = sessions.get(sessionId);
            if (session == null) return;
            
            Experience exp = new Experience(state, actions, outcome);
            session.experiences.add(exp);
            
            int score = outcome.containsKey("score") ? (Integer)outcome.get("score") : 0;
            
            System.out.println("Recorded experience with " + actions.size() + " actions, score: " + score);
            
            // Learn from all experiences, with quality proportional to score
            Strategy strategy = new Strategy(session.domain, new ArrayList<>(actions));
            strategy.score = score;
            strategies.get(session.domain).add(strategy);
            
            if (score >= 30) {
                System.out.println("Learned promising strategy with " + actions.size() + " actions (score: " + score + ")");
            }
            
            // Update pattern recognition after every experience
            updatePatternRecognition(session.domain);
        }
        
        private void updatePatternRecognition(String domain) {
            List<Strategy> domainStrategies = strategies.get(domain);
            List<Pattern> domainPatterns = patterns.get(domain);
            
            // Only perform pattern recognition with enough data
            if (domainStrategies.size() < 5) return;
            
            // Find action sequences that correlate with high scores
            Map<String, PatternStats> patternMap = new HashMap<>();
            
            // Examine each strategy
            for (Strategy strategy : domainStrategies) {
                List<String> actions = strategy.actions;
                float score = strategy.score;
                
                // Look for patterns of length 2 and 3
                for (int patternLength = 2; patternLength <= 3; patternLength++) {
                    if (actions.size() >= patternLength) {
                        for (int i = 0; i <= actions.size() - patternLength; i++) {
                            StringBuilder pattern = new StringBuilder();
                            for (int j = 0; j < patternLength; j++) {
                                if (j > 0) pattern.append(",");
                                pattern.append(actions.get(i + j));
                            }
                            
                            String patternStr = pattern.toString();
                            
                            PatternStats stats = patternMap.computeIfAbsent(
                                patternStr, k -> new PatternStats(patternStr)
                            );
                            
                            stats.totalOccurrences++;
                            stats.totalScore += score;
                            
                            if (score > stats.highestScore) {
                                stats.highestScore = score;
                            }
                        }
                    }
                }
            }
            
            // Convert good patterns to Pattern objects
            domainPatterns.clear();
            for (PatternStats stats : patternMap.values()) {
                // Only consider patterns that appear multiple times and have good scores
                if (stats.totalOccurrences >= 3 && stats.getAverageScore() >= 20) {
                    String[] actionArray = stats.patternString.split(",");
                    List<String> patternActions = new ArrayList<>();
                    for (String action : actionArray) {
                        patternActions.add(action);
                    }
                    
                    Pattern pattern = new Pattern(
                        domain, 
                        patternActions, 
                        stats.totalOccurrences,
                        stats.getAverageScore(),
                        stats.highestScore
                    );
                    
                    domainPatterns.add(pattern);
                    
                    if (stats.highestScore >= 33) {
                        System.out.println("Discovered promising pattern: " + stats.patternString + 
                                         " (avg score: " + stats.getAverageScore() + 
                                         ", highest: " + stats.highestScore + ")");
                    }
                }
            }
            
            // Sort patterns by score
            domainPatterns.sort((p1, p2) -> Float.compare(p2.avgScore, p1.avgScore));
        }
        
        public List<String> recommendActions(String sessionId, Map<String, Object> state) {
            LearningSession session = sessions.get(sessionId);
            if (session == null) return new ArrayList<>();
            
            String domain = session.domain;
            List<Strategy> domainStrategies = strategies.get(domain);
            List<Pattern> domainPatterns = patterns.get(domain);
            
            // Exploration rate decreases over time
            float explorationRate = Math.max(0.2f, 0.7f - (0.01f * session.experiences.size()));
            
            if (random.nextFloat() > explorationRate) {
                // Exploitation - use patterns and strategies
                
                // First try to use discovered patterns in a new combination
                if (!domainPatterns.isEmpty() && random.nextFloat() < 0.7f) {
                    return createActionsFromPatterns(domainPatterns);
                }
                
                // Otherwise use a good strategy
                if (!domainStrategies.isEmpty()) {
                    // Sort strategies by score
                    domainStrategies.sort((s1, s2) -> Float.compare(s2.score, s1.score));
                    
                    // Select a good strategy (weighted towards better ones)
                    int index = selectWeightedIndex(domainStrategies.size());
                    Strategy selected = domainStrategies.get(index);
                    
                    System.out.println("Using learned strategy with score " + selected.score);
                    return new ArrayList<>(selected.actions);
                }
            }
            
            // Exploration - try random actions
            List<String> randomActions = generateRandomActions();
            System.out.println("Exploring with random actions: " + randomActions);
            return randomActions;
        }
        
        private List<String> createActionsFromPatterns(List<Pattern> patterns) {
            List<String> actions = new ArrayList<>();
            
            // Select 1-2 patterns to combine
            int patternCount = 1 + random.nextInt(2);
            for (int i = 0; i < patternCount; i++) {
                if (patterns.isEmpty()) break;
                
                // Select pattern weighted by score
                int index = selectWeightedIndex(patterns.size());
                Pattern pattern = patterns.get(index);
                
                System.out.println("Using pattern with avg score " + pattern.avgScore + ": " + pattern.actions);
                
                // Add pattern actions
                for (String action : pattern.actions) {
                    actions.add(action);
                }
                
                // Sometimes add a random action between patterns
                if (i < patternCount - 1 && random.nextFloat() < 0.3f) {
                    actions.add("action_" + random.nextInt(5));
                }
            }
            
            return actions;
        }
        
        private int selectWeightedIndex(int size) {
            // Return indices biased towards 0 (better items should be sorted to front)
            return (int)(Math.pow(random.nextDouble(), 2) * size);
        }
        
        public void evolveStrategies(String domain) {
            List<Strategy> domainStrategies = strategies.get(domain);
            List<Pattern> domainPatterns = patterns.get(domain);
            
            if (domainStrategies.size() < 3) return;
            
            // Sort strategies by score first
            domainStrategies.sort((s1, s2) -> Float.compare(s2.score, s1.score));
            
            // Create several evolved strategies
            List<Strategy> newStrategies = new ArrayList<>();
            
            // Elite advancement - keep best strategies
            int eliteCount = Math.min(3, domainStrategies.size() / 5);
            
            // Different types of evolution
            // 1. Pattern-based strategies
            if (!domainPatterns.isEmpty()) {
                for (int i = 0; i < 2; i++) {
                    List<String> patternActions = createActionsFromPatterns(domainPatterns);
                    if (!patternActions.isEmpty()) {
                        Strategy patternStrategy = new Strategy(domain, patternActions);
                        patternStrategy.score = 0; // Will be evaluated when used
                        newStrategies.add(patternStrategy);
                    }
                }
            }
            
            // 2. Crossover strategies
            for (int i = 0; i < 3; i++) {
                // Select parents with bias towards better strategies
                int idx1 = selectWeightedIndex(Math.min(10, domainStrategies.size()));
                int idx2 = selectWeightedIndex(Math.min(10, domainStrategies.size()));
                
                Strategy parent1 = domainStrategies.get(idx1);
                Strategy parent2 = domainStrategies.get(idx2);
                
                Strategy child = crossover(parent1, parent2);
                newStrategies.add(child);
            }
            
            // 3. Mutation strategies
            for (int i = 0; i < 2; i++) {
                int idx = selectWeightedIndex(Math.min(5, domainStrategies.size()));
                Strategy parent = domainStrategies.get(idx);
                
                Strategy mutated = mutate(parent);
                newStrategies.add(mutated);
            }
            
            // Add new strategies
            for (Strategy strategy : newStrategies) {
                System.out.println("Evolved new strategy with " + strategy.actions.size() + " actions");
                domainStrategies.add(strategy);
            }
            
            // Prune strategies to prevent explosion
            if (domainStrategies.size() > 30) {
                // Re-sort by score
                domainStrategies.sort((s1, s2) -> Float.compare(s2.score, s1.score));
                
                // Keep top strategies and randomly selected others
                List<Strategy> keptStrategies = new ArrayList<>();
                
                // Keep top performers
                for (int i = 0; i < 10; i++) {
                    if (i < domainStrategies.size()) {
                        keptStrategies.add(domainStrategies.get(i));
                    }
                }
                
                // Keep some random strategies for diversity
                for (int i = 10; i < domainStrategies.size(); i++) {
                    if (random.nextFloat() < 0.2) {
                        keptStrategies.add(domainStrategies.get(i));
                    }
                }
                
                // Replace strategy list
                strategies.put(domain, keptStrategies);
            }
        }
        
        private Strategy crossover(Strategy parent1, Strategy parent2) {
            List<String> childActions = new ArrayList<>();
            String domain = parent1.domain;
            
            if (parent1.actions.isEmpty() || parent2.actions.isEmpty()) {
                // Copy from non-empty parent
                if (!parent1.actions.isEmpty()) return new Strategy(domain, new ArrayList<>(parent1.actions));
                if (!parent2.actions.isEmpty()) return new Strategy(domain, new ArrayList<>(parent2.actions));
                return new Strategy(domain, new ArrayList<>());
            }
            
            // Two-point crossover for larger strategies
            if (parent1.actions.size() >= 3 && parent2.actions.size() >= 3) {
                int point1 = random.nextInt(parent1.actions.size());
                int point2 = random.nextInt(parent2.actions.size());
                
                // First part from parent1
                for (int i = 0; i < point1; i++) {
                    childActions.add(parent1.actions.get(i));
                }
                
                // Second part from parent2
                for (int i = point2; i < parent2.actions.size(); i++) {
                    childActions.add(parent2.actions.get(i));
                }
            }
            // One-point crossover for smaller strategies
            else {
                int point1 = parent1.actions.size() / 2;
                int point2 = parent2.actions.size() / 2;
                
                // First half from parent1
                for (int i = 0; i < point1; i++) {
                    childActions.add(parent1.actions.get(i));
                }
                
                // Second half from parent2
                for (int i = point2; i < parent2.actions.size(); i++) {
                    childActions.add(parent2.actions.get(i));
                }
            }
            
            return new Strategy(domain, childActions);
        }
        
        private Strategy mutate(Strategy parent) {
            List<String> mutatedActions = new ArrayList<>(parent.actions);
            
            if (mutatedActions.isEmpty()) {
                mutatedActions.add("action_" + random.nextInt(5));
                return new Strategy(parent.domain, mutatedActions);
            }
            
            // Apply 1-2 mutations
            int mutations = 1 + random.nextInt(2);
            for (int i = 0; i < mutations; i++) {
                int mutationType = random.nextInt(3);
                
                switch (mutationType) {
                    case 0: // Replace a random action
                        if (!mutatedActions.isEmpty()) {
                            int idx = random.nextInt(mutatedActions.size());
                            mutatedActions.set(idx, "action_" + random.nextInt(5));
                        }
                        break;
                        
                    case 1: // Insert a new action
                        int insertIdx = mutatedActions.isEmpty() ? 0 : random.nextInt(mutatedActions.size() + 1);
                        mutatedActions.add(insertIdx, "action_" + random.nextInt(5));
                        break;
                        
                    case 2: // Remove an action
                        if (!mutatedActions.isEmpty()) {
                            int removeIdx = random.nextInt(mutatedActions.size());
                            mutatedActions.remove(removeIdx);
                        }
                        break;
                }
            }
            
            return new Strategy(parent.domain, mutatedActions);
        }
        
        private List<String> generateRandomActions() {
            // Generate sequence of 2-4 actions
            int count = 2 + random.nextInt(3);
            List<String> actions = new ArrayList<>();
            
            for (int i = 0; i < count; i++) {
                actions.add("action_" + random.nextInt(5));
            }
            
            return actions;
        }
        
        static class PatternStats {
            public final String patternString;
            public int totalOccurrences = 0;
            public float totalScore = 0;
            public float highestScore = 0;
            
            public PatternStats(String patternString) {
                this.patternString = patternString;
            }
            
            public float getAverageScore() {
                return totalOccurrences > 0 ? totalScore / totalOccurrences : 0;
            }
        }
    }
    
    static class LearningSession {
        public final String id;
        public final String domain;
        public final List<Experience> experiences = new ArrayList<>();
        
        public LearningSession(String id, String domain) {
            this.id = id;
            this.domain = domain;
        }
    }
    
    static class Experience {
        public final Map<String, Object> state;
        public final List<String> actions;
        public final Map<String, Object> outcome;
        
        public Experience(Map<String, Object> state, List<String> actions, Map<String, Object> outcome) {
            this.state = state;
            this.actions = actions;
            this.outcome = outcome;
        }
    }
    
    static class Strategy {
        public final String domain;
        public final List<String> actions;
        public float score = 0.0f;
        
        public Strategy(String domain, List<String> actions) {
            this.domain = domain;
            this.actions = actions;
        }
    }
    
    static class Pattern {
        public final String domain;
        public final List<String> actions;
        public final int occurrences;
        public final float avgScore;
        public final float maxScore;
        
        public Pattern(String domain, List<String> actions, int occurrences, float avgScore, float maxScore) {
            this.domain = domain;
            this.actions = actions;
            this.occurrences = occurrences;
            this.avgScore = avgScore;
            this.maxScore = maxScore;
        }
    }
    
    // Game environment simulation
    static class GameEnvironment {
        private Map<String, Object> state = new HashMap<>();
        private Random random = new Random();
        
        // Solution pattern (unknown to the learning system)
        private final List<String> winningSequence = List.of("action_2", "action_0", "action_3");
        
        public GameEnvironment() {
            resetState();
        }
        
        public Map<String, Object> getState() {
            return new HashMap<>(state);
        }
        
        public Map<String, Object> executeActions(List<String> actions) {
            Map<String, Object> result = new HashMap<>();
            
            // Check for winning sequence anywhere in the actions
            boolean containsWinningSequence = false;
            if (actions.size() >= winningSequence.size()) {
                for (int i = 0; i <= actions.size() - winningSequence.size(); i++) {
                    boolean matches = true;
                    for (int j = 0; j < winningSequence.size(); j++) {
                        if (!actions.get(i + j).equals(winningSequence.get(j))) {
                            matches = false;
                            break;
                        }
                    }
                    if (matches) {
                        containsWinningSequence = true;
                        break;
                    }
                }
            }
            
            if (containsWinningSequence) {
                result.put("success", true);
                result.put("score", 100);
                System.out.println("SUCCESS! Winning sequence discovered: " + actions);
                return result;
            }
            
            // Check for partial matches at the beginning
            int matchCount = 0;
            for (int i = 0; i < Math.min(actions.size(), winningSequence.size()); i++) {
                if (actions.get(i).equals(winningSequence.get(i))) {
                    matchCount++;
                } else {
                    break; // Stop counting at first mismatch
                }
            }
            
            // Calculate score based on matches at the beginning
            int score = matchCount * 33; // 33 per match
            
            // Small random factor
            score += random.nextInt(5);
            
            result.put("success", false);
            result.put("score", score);
            
            if (score > 30) {
                System.out.println("Getting closer! Score: " + score);
            } else {
                System.out.println("Try again. Score: " + score);
            }
            
            return result;
        }
        
        public void resetState() {
            state.put("level", 1);
            state.put("player_position", 50);
            state.put("energy", 100);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Starting Adaptive Learning System Test");
        System.out.println("=====================================");
        System.out.println("This simulation demonstrates how the system can discover");
        System.out.println("unknown winning patterns through experimentation and learning");
        System.out.println("The winning pattern is: action_2, action_0, action_3");
        System.out.println();
        
        AdaptiveLearningSystem learningSystem = new AdaptiveLearningSystem();
        GameEnvironment gameEnv = new GameEnvironment();
        
        // Create learning session
        String sessionId = learningSystem.createSession("puzzle_game");
        
        // Run learning epochs
        boolean solutionFound = false;
        int maxEpochs = 30;
        
        for (int epoch = 1; epoch <= maxEpochs && !solutionFound; epoch++) {
            System.out.println("\nEpoch " + epoch + " -------------------------");
            
            // Get current state
            Map<String, Object> state = gameEnv.getState();
            
            // Get recommended actions
            List<String> actions = learningSystem.recommendActions(sessionId, state);
            
            // Execute actions
            Map<String, Object> outcome = gameEnv.executeActions(actions);
            
            // Record observation
            learningSystem.recordObservation(sessionId, state, actions, outcome);
            
            // Check if solution found
            if (Boolean.TRUE.equals(outcome.get("success"))) {
                solutionFound = true;
                System.out.println("\nSOLUTION FOUND after " + epoch + " epochs");
                System.out.println("Winning action sequence contained: " + winningSequence);
            }
            
            // Evolve strategies every epoch
            learningSystem.evolveStrategies("puzzle_game");
        }
        
        if (!solutionFound) {
            System.out.println("\nMax epochs reached without finding solution");
        }
        
        System.out.println("\nAdaptive Learning Test Complete");
    }
    
    private static final List<String> winningSequence = List.of("action_2", "action_0", "action_3");
}
