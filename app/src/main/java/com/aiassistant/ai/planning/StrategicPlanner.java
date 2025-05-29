package com.aiassistant.ai.planning;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;

import com.aiassistant.data.models.AIAction;
import com.aiassistant.ai.models.TouchPath;
import com.aiassistant.data.models.GameState;
import com.aiassistant.data.models.UIElement;
import com.aiassistant.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Strategic planner for goal-oriented behavior.
 * Analyzes game state and determines the best course of action to achieve goals.
 */
public class StrategicPlanner {
    private static final String TAG = Constants.TAG_PREFIX + "StrategicPlanner";
    
    // Context
    private Context context;
    
    // Planning strategies
    private final Map<String, PlanningStrategy> strategies;
    
    // Current goals
    private final List<Goal> activeGoals;
    
    // Exploration parameters
    private float explorationRate = 0.2f;
    private final Random random;
    
    // Action history
    private final List<AIAction> recentActions;
    private final Map<String, List<AIAction>> stateActionHistory;
    
    // Planning context
    private final Map<String, Object> planningContext;
    
    /**
     * Constructor
     */
    public StrategicPlanner() {
        strategies = new HashMap<>();
        activeGoals = new ArrayList<>();
        random = new Random();
        recentActions = new ArrayList<>();
        stateActionHistory = new HashMap<>();
        planningContext = new HashMap<>();
    }
    
    /**
     * Initialize the strategic planner
     * @param context Application context
     */
    public void initialize(Context context) {
        this.context = context;
        
        // Register strategies
        strategies.put("observe", new ObserveStrategy());
        strategies.put("explore", new ExploreStrategy());
        strategies.put("copilot", new CopilotStrategy());
        strategies.put("autonomous", new AutonomousStrategy());
        strategies.put("learn", new LearnStrategy());
        
        Log.i(TAG, "Strategic Planner initialized");
    }
    
    /**
     * Plan the next action based on the current state
     * @param gameState Current game state
     * @param uiElements UI elements
     * @param strategyName Strategy to use
     * @return Next action or null
     */
    public AIAction planNextAction(GameState gameState, List<UIElement> uiElements, String strategyName) {
        if (gameState == null || uiElements == null || strategyName == null) {
            return null;
        }
        
        // Get the strategy
        PlanningStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            strategy = strategies.get("observe"); // Default to observe
        }
        
        // Update planning context
        updatePlanningContext(gameState, uiElements);
        
        // Generate candidate actions
        List<AIAction> candidateActions = strategy.generateActions(gameState, uiElements, planningContext);
        
        // If no actions generated, try exploration
        if (candidateActions.isEmpty() && !"observe".equals(strategyName)) {
            PlanningStrategy exploreStrategy = strategies.get("explore");
            candidateActions = exploreStrategy.generateActions(gameState, uiElements, planningContext);
        }
        
        // If still no actions, return null
        if (candidateActions.isEmpty()) {
            return null;
        }
        
        // Evaluate and select the best action
        AIAction bestAction = evaluateAndSelectAction(candidateActions, gameState, uiElements);
        
        // Record the action
        if (bestAction != null) {
            recordAction(bestAction, gameState);
        }
        
        return bestAction;
    }
    
    /**
     * Update planning context
     * @param gameState Current game state
     * @param uiElements UI elements
     */
    private void updatePlanningContext(GameState gameState, List<UIElement> uiElements) {
        planningContext.put("packageName", gameState.getPackageName());
        planningContext.put("activityName", gameState.getActivityName());
        planningContext.put("timestamp", gameState.getTimestamp());
        planningContext.put("stateId", gameState.getStateId());
        planningContext.put("elementCount", uiElements.size());
        
        // Count element types
        int buttons = 0;
        int textViews = 0;
        int editTexts = 0;
        int images = 0;
        int clickables = 0;
        
        for (UIElement element : uiElements) {
            if (element.isButton()) buttons++;
            if (element.isTextview()) textViews++;
            if (element.isEdittext()) editTexts++;
            if (element.isImageview()) images++;
            if (element.isClickable()) clickables++;
        }
        
        planningContext.put("buttonCount", buttons);
        planningContext.put("textViewCount", textViews);
        planningContext.put("editTextCount", editTexts);
        planningContext.put("imageCount", images);
        planningContext.put("clickableCount", clickables);
    }
    
    /**
     * Evaluate and select the best action
     * @param candidateActions Candidate actions
     * @param gameState Current game state
     * @param uiElements UI elements
     * @return Best action
     */
    private AIAction evaluateAndSelectAction(List<AIAction> candidateActions, GameState gameState, UIElement[] uiElements) {
        // If only one action, return it
        if (candidateActions.size() == 1) {
            return candidateActions.get(0);
        }
        
        // Explore with some probability
        if (random.nextFloat() < explorationRate) {
            return candidateActions.get(random.nextInt(candidateActions.size()));
        }
        
        // Otherwise, evaluate and return the highest-scoring action
        AIAction bestAction = null;
        float bestScore = Float.NEGATIVE_INFINITY;
        
        for (AIAction action : candidateActions) {
            float score = evaluateActionUtility(action, gameState, uiElements);
            
            // Update action expected reward
            action.setExpectedReward(score);
            
            if (score > bestScore) {
                bestScore = score;
                bestAction = action;
            }
        }
        
        return bestAction;
    }
    
    /**
     * Evaluate action utility
     * @param action Action to evaluate
     * @param gameState Current game state
     * @param uiElements UI elements
     * @return Utility score
     */
    private float evaluateActionUtility(AIAction action, GameState gameState, UIElement[] uiElements) {
        float score = action.getConfidence();
        
        // Check for cycling behavior
        boolean isCycling = false;
        if (recentActions.size() >= 5) {
            List<AIAction> lastFive = recentActions.subList(recentActions.size() - 5, recentActions.size());
            
            // Check if we're doing the same action repeatedly
            int sameTypeCount = 0;
            for (AIAction pastAction : lastFive) {
                if (pastAction.getActionType() == action.getActionType()) {
                    sameTypeCount++;
                }
            }
            
            if (sameTypeCount >= 3) {
                // Likely cycling, reduce score
                score *= 0.5f;
                isCycling = true;
            }
        }
        
        // If cycling, explore new actions
        if (isCycling) {
            // Check if we've already tried this action in this state
            String stateId = gameState.getStateId();
            List<AIAction> stateActions = stateActionHistory.get(stateId);
            
            if (stateActions != null) {
                boolean triedBefore = false;
                
                for (AIAction pastAction : stateActions) {
                    if (areActionsEquivalent(action, pastAction)) {
                        triedBefore = true;
                        break;
                    }
                }
                
                if (triedBefore) {
                    // We've tried this before and it led to cycling, penalize heavily
                    score *= 0.2f;
                }
            }
        }
        
        return score;
    }
    
    /**
     * Check if two actions are equivalent
     * @param a1 First action
     * @param a2 Second action
     * @return True if equivalent
     */
    private boolean areActionsEquivalent(AIAction a1, AIAction a2) {
        if (a1.getActionType() != a2.getActionType()) {
            return false;
        }
        
        switch (a1.getActionType()) {
            case AIAction.ACTION_TYPE_TAP:
                // Check if tapping the same element or very close coordinates
                if (a1.getTargetElementId() > 0 && a2.getTargetElementId() > 0) {
                    return a1.getTargetElementId() == a2.getTargetElementId();
                } else if (a1.getTargetLocation() != null && a2.getTargetLocation() != null) {
                    Point p1 = a1.getTargetLocation();
                    Point p2 = a2.getTargetLocation();
                    int dx = p1.x - p2.x;
                    int dy = p1.y - p2.y;
                    return (dx * dx + dy * dy) < 100; // Within 10 pixels
                }
                break;
                
            case AIAction.ACTION_TYPE_TEXT_INPUT:
                // Check if inputting the same text
                return a1.getTargetText() != null && a1.getTargetText().equals(a2.getTargetText());
                
            case AIAction.ACTION_TYPE_BACK:
            case AIAction.ACTION_TYPE_HOME:
            case AIAction.ACTION_TYPE_RECENT_APPS:
                // These are always equivalent
                return true;
                
            case AIAction.ACTION_TYPE_SWIPE:
                // Check if swiping in the same direction
                if (a1.getTouchPaths() != null && !a1.getTouchPaths().isEmpty() &&
                    a2.getTouchPaths() != null && !a2.getTouchPaths().isEmpty()) {
                    TouchPath p1 = a1.getTouchPaths().get(0);
                    TouchPath p2 = a2.getTouchPaths().get(0);
                    
                    if (p1.getPoints().size() >= 2 && p2.getPoints().size() >= 2) {
                        Point start1 = p1.getPoints().get(0);
                        Point end1 = p1.getPoints().get(p1.getPoints().size() - 1);
                        Point start2 = p2.getPoints().get(0);
                        Point end2 = p2.getPoints().get(p2.getPoints().size() - 1);
                        
                        int dx1 = end1.x - start1.x;
                        int dy1 = end1.y - start1.y;
                        int dx2 = end2.x - start2.x;
                        int dy2 = end2.y - start2.y;
                        
                        // Check if in same general direction
                        return (dx1 * dx2 > 0 && dy1 * dy2 > 0);
                    }
                }
                break;
        }
        
        return false;
    }
    
    /**
     * Record an action
     * @param action Action to record
     * @param gameState Game state
     */
    private void recordAction(AIAction action, GameState gameState) {
        // Add to recent actions
        recentActions.add(action);
        if (recentActions.size() > 20) {
            recentActions.remove(0);
        }
        
        // Add to state action history
        String stateId = gameState.getStateId();
        if (!stateActionHistory.containsKey(stateId)) {
            stateActionHistory.put(stateId, new ArrayList<>());
        }
        stateActionHistory.get(stateId).add(action);
    }
    
    /**
     * Add a goal
     * @param goal Goal to add
     */
    public void addGoal(Goal goal) {
        if (goal != null) {
            activeGoals.add(goal);
        }
    }
    
    /**
     * Remove a goal
     * @param goal Goal to remove
     */
    public void removeGoal(Goal goal) {
        activeGoals.remove(goal);
    }
    
    /**
     * Get active goals
     * @return Active goals
     */
    public List<Goal> getActiveGoals() {
        return new ArrayList<>(activeGoals);
    }
    
    /**
     * Clear active goals
     */
    public void clearGoals() {
        activeGoals.clear();
    }
    
    /**
     * Set exploration rate
     * @param rate Exploration rate (0-1)
     */
    public void setExplorationRate(float rate) {
        this.explorationRate = Math.max(0.0f, Math.min(1.0f, rate));
    }
    
    /**
     * Get exploration rate
     * @return Exploration rate
     */
    public float getExplorationRate() {
        return explorationRate;
    }
    
    /**
     * Clear action history
     */
    public void clearActionHistory() {
        recentActions.clear();
        stateActionHistory.clear();
    }
    
    /**
     * Planning strategy interface
     */
    private interface PlanningStrategy {
        /**
         * Generate candidate actions
         * @param gameState Current game state
         * @param uiElements UI elements
         * @param context Planning context
         * @return List of candidate actions
         */
        List<AIAction> generateActions(GameState gameState, List<UIElement> uiElements, Map<String, Object> context);
    }
    
    /**
     * Observe strategy - passive observation only
     */
    private class ObserveStrategy implements PlanningStrategy {
        @Override
        public List<AIAction> generateActions(GameState gameState, List<UIElement> uiElements, Map<String, Object> context) {
            // No actions in observe mode
            return new ArrayList<>();
        }
    }
    
    /**
     * Explore strategy - actively explores the UI
     */
    private class ExploreStrategy implements PlanningStrategy {
        @Override
        public List<AIAction> generateActions(GameState gameState, List<UIElement> uiElements, Map<String, Object> context) {
            List<AIAction> actions = new ArrayList<>();
            
            // Find clickable elements
            for (UIElement element : uiElements) {
                if (element.isClickable()) {
                    // Create tap action
                    int centerX = element.getX() + element.getWidth() / 2;
                    int centerY = element.getY() + element.getHeight() / 2;
                    
                    AIAction tapAction = AIAction.createTapElementAction(
                        element.getElementId(),
                        centerX,
                        centerY,
                        0.5f // Medium confidence
                    );
                    
                    actions.add(tapAction);
                }
            }
            
            // Add swipe actions in cardinal directions
            int screenWidth = 1080; // Default, should be replaced with actual screen width
            int screenHeight = 1920; // Default, should be replaced with actual screen height
            
            if (context.containsKey("screenWidth") && context.containsKey("screenHeight")) {
                screenWidth = (int) context.get("screenWidth");
                screenHeight = (int) context.get("screenHeight");
            }
            
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;
            
            // Swipe up
            actions.add(AIAction.createSwipeAction(
                centerX, centerY + 200,
                centerX, centerY - 200,
                300, 0.3f
            ));
            
            // Swipe down
            actions.add(AIAction.createSwipeAction(
                centerX, centerY - 200,
                centerX, centerY + 200,
                300, 0.3f
            ));
            
            // Swipe left
            actions.add(AIAction.createSwipeAction(
                centerX + 200, centerY,
                centerX - 200, centerY,
                300, 0.3f
            ));
            
            // Swipe right
            actions.add(AIAction.createSwipeAction(
                centerX - 200, centerY,
                centerX + 200, centerY,
                300, 0.3f
            ));
            
            // Add back action
            actions.add(AIAction.createBackAction(0.1f));
            
            return actions;
        }
    }
    
    /**
     * Copilot strategy - assists but doesn't take control
     */
    private class CopilotStrategy implements PlanningStrategy {
        @Override
        public List<AIAction> generateActions(GameState gameState, List<UIElement> uiElements, Map<String, Object> context) {
            List<AIAction> actions = new ArrayList<>();
            
            // Analyze the UI to find likely next actions
            List<UIElement> interactiveElements = findInteractiveElements(uiElements);
            
            // Sort by relevance (buttons, then other clickables)
            interactiveElements.sort((e1, e2) -> {
                boolean isButton1 = e1.isButton();
                boolean isButton2 = e2.isButton();
                
                if (isButton1 && !isButton2) return -1;
                if (!isButton1 && isButton2) return 1;
                
                // If both are buttons or both are not, sort by position (top to bottom, left to right)
                int yDiff = e1.getY() - e2.getY();
                if (yDiff != 0) return yDiff;
                
                return e1.getX() - e2.getX();
            });
            
            // Create tap actions for interactive elements
            for (UIElement element : interactiveElements) {
                int centerX = element.getX() + element.getWidth() / 2;
                int centerY = element.getY() + element.getHeight() / 2;
                
                float confidence = element.isButton() ? 0.8f : 0.6f;
                
                AIAction tapAction = AIAction.createTapElementAction(
                    element.getElementId(),
                    centerX,
                    centerY,
                    confidence
                );
                
                actions.add(tapAction);
            }
            
            // Suggest appropriate swipe actions based on content
            if (isScrollableContent(uiElements)) {
                int screenWidth = 1080; // Default
                int screenHeight = 1920; // Default
                
                if (context.containsKey("screenWidth") && context.containsKey("screenHeight")) {
                    screenWidth = (int) context.get("screenWidth");
                    screenHeight = (int) context.get("screenHeight");
                }
                
                int centerX = screenWidth / 2;
                int centerY = screenHeight / 2;
                
                // Swipe up (scroll down)
                actions.add(AIAction.createSwipeAction(
                    centerX, centerY + 200,
                    centerX, centerY - 200,
                    300, 0.7f
                ));
            }
            
            // Add back action with low confidence
            actions.add(AIAction.createBackAction(0.1f));
            
            return actions;
        }
        
        private List<UIElement> findInteractiveElements(List<UIElement> uiElements) {
            List<UIElement> interactive = new ArrayList<>();
            
            for (UIElement element : uiElements) {
                if (element.isClickable() || element.isButton() || 
                    (element.isTextview() && isProbablyLink(element))) {
                    interactive.add(element);
                }
            }
            
            return interactive;
        }
        
        private boolean isProbablyLink(UIElement element) {
            String text = element.getText();
            if (text == null) return false;
            
            return text.contains("http") || text.contains("www") || 
                   text.contains(".com") || text.contains(".org") || text.contains(".net");
        }
        
        private boolean isScrollableContent(List<UIElement> uiElements) {
            // Check if any element is scrollable
            for (UIElement element : uiElements) {
                if (element.isScrollable()) {
                    return true;
                }
            }
            
            // Check if there are many elements (likely scrollable content)
            int elementCount = uiElements.size();
            return elementCount > 10;
        }
    }
    
    /**
     * Autonomous strategy - takes full control
     */
    private class AutonomousStrategy implements PlanningStrategy {
        @Override
        public List<AIAction> generateActions(GameState gameState, List<UIElement> uiElements, Map<String, Object> context) {
            List<AIAction> actions = new ArrayList<>();
            
            // Check active goals first
            for (Goal goal : activeGoals) {
                if (goal.isAchievable(gameState, uiElements)) {
                    AIAction action = goal.getNextAction(gameState, uiElements);
                    if (action != null) {
                        actions.add(action);
                    }
                }
            }
            
            // If no goals are actionable, use goal inference
            if (actions.isEmpty()) {
                Goal inferredGoal = inferGoal(gameState, uiElements, context);
                if (inferredGoal != null && inferredGoal.isAchievable(gameState, uiElements)) {
                    AIAction action = inferredGoal.getNextAction(gameState, uiElements);
                    if (action != null) {
                        actions.add(action);
                    }
                }
            }
            
            // If still no actions, fall back to copilot strategy
            if (actions.isEmpty()) {
                PlanningStrategy copilotStrategy = new CopilotStrategy();
                actions = copilotStrategy.generateActions(gameState, uiElements, context);
            }
            
            return actions;
        }
        
        private Goal inferGoal(GameState gameState, List<UIElement> uiElements, Map<String, Object> context) {
            // This would use a more sophisticated goal inference system
            // For now, create a simple exploration goal
            return new ExplorationGoal();
        }
        
        /**
         * Simple exploration goal
         */
        private class ExplorationGoal implements Goal {
            @Override
            public boolean isAchievable(GameState gameState, List<UIElement> uiElements) {
                return true; // Always achievable
            }
            
            @Override
            public AIAction getNextAction(GameState gameState, List<UIElement> uiElements) {
                // Find an interesting interactive element
                UIElement bestElement = null;
                float bestScore = 0.0f;
                
                for (UIElement element : uiElements) {
                    if (element.isClickable()) {
                        float score = evaluateElementInterest(element);
                        if (score > bestScore) {
                            bestScore = score;
                            bestElement = element;
                        }
                    }
                }
                
                if (bestElement != null) {
                    int centerX = bestElement.getX() + bestElement.getWidth() / 2;
                    int centerY = bestElement.getY() + bestElement.getHeight() / 2;
                    
                    return AIAction.createTapElementAction(
                        bestElement.getElementId(),
                        centerX,
                        centerY,
                        0.7f
                    );
                }
                
                // If no interesting element found, try scrolling
                int screenWidth = 1080; // Default
                int screenHeight = 1920; // Default
                
                int centerX = screenWidth / 2;
                int centerY = screenHeight / 2;
                
                return AIAction.createSwipeAction(
                    centerX, centerY + 200,
                    centerX, centerY - 200,
                    300, 0.5f
                );
            }
            
            private float evaluateElementInterest(UIElement element) {
                float score = 0.0f;
                
                // Buttons are interesting
                if (element.isButton()) score += 0.5f;
                
                // Elements with text are interesting
                if (element.getText() != null && !element.getText().isEmpty()) {
                    score += 0.3f;
                    
                    // Even more interesting if text contains certain keywords
                    String text = element.getText().toLowerCase();
                    if (text.contains("start") || text.contains("play") || 
                        text.contains("next") || text.contains("continue") ||
                        text.contains("ok") || text.contains("done")) {
                        score += 0.2f;
                    }
                }
                
                // Centered elements are interesting
                int centerX = element.getX() + element.getWidth() / 2;
                int centerY = element.getY() + element.getHeight() / 2;
                
                // Assume screen is 1080x1920 (update as needed)
                int screenWidth = 1080;
                int screenHeight = 1920;
                
                float distFromCenterX = Math.abs(centerX - screenWidth / 2) / (float) screenWidth;
                float distFromCenterY = Math.abs(centerY - screenHeight / 2) / (float) screenHeight;
                float centerScore = 1.0f - Math.max(distFromCenterX, distFromCenterY);
                
                score += 0.2f * centerScore;
                
                return score;
            }
        }
    }
    
    /**
     * Learn strategy - optimized for learning
     */
    private class LearnStrategy implements PlanningStrategy {
        @Override
        public List<AIAction> generateActions(GameState gameState, List<UIElement> uiElements, Map<String, Object> context) {
            // Learning strategy is similar to autonomous but with more exploration
            PlanningStrategy autonomousStrategy = new AutonomousStrategy();
            List<AIAction> actions = autonomousStrategy.generateActions(gameState, uiElements, context);
            
            // Add some random exploration
            PlanningStrategy exploreStrategy = new ExploreStrategy();
            List<AIAction> exploreActions = exploreStrategy.generateActions(gameState, uiElements, context);
            
            // Mix in exploration actions
            for (AIAction action : exploreActions) {
                // Slightly boost confidence to increase chance of selection
                action.setConfidence(action.getConfidence() * 1.2f);
                actions.add(action);
            }
            
            return actions;
        }
    }
    
    /**
     * Goal interface
     */
    public interface Goal {
        /**
         * Check if the goal is achievable
         * @param gameState Current game state
         * @param uiElements UI elements
         * @return True if achievable
         */
        boolean isAchievable(GameState gameState, List<UIElement> uiElements);
        
        /**
         * Get the next action to achieve the goal
         * @param gameState Current game state
         * @param uiElements UI elements
         * @return Next action or null
         */
        AIAction getNextAction(GameState gameState, List<UIElement> uiElements);
    }
}