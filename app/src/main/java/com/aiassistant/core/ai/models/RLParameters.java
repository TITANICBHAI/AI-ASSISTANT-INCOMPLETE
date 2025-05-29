package com.aiassistant.core.ai.models;

/**
 * Parameters for reinforcement learning algorithms.
 * Allows customization of learning rates, discount factors, exploration rates, etc.
 */
public class RLParameters {
    
    // Core learning parameters
    private float learningRate = 0.001f;
    private float discountFactor = 0.99f;
    private float explorationRate = 0.1f;
    
    // Experience replay settings
    private int batchSize = 32;
    private int replayBufferSize = 10000;
    
    // Actor-critic parameters (for PPO)
    private float clipEpsilon = 0.2f;
    private int epochs = 4;
    private float valueCoefficient = 0.5f;
    private float entropyCoefficient = 0.01f;
    
    // Network update frequency
    private int targetNetworkUpdateFrequency = 1000;
    private int modelSaveFrequency = 5000;
    
    // State/observation parameters
    private boolean usePixelInput = true;
    private boolean useAccessibilityInfo = true;
    
    // Action space configuration
    private boolean useDiscreteActions = true;
    private int numActionBins = 10; // For discretizing continuous actions
    
    /**
     * Create default parameters
     */
    public RLParameters() {
        // Use default values
    }
    
    /**
     * Create parameters with custom learning rate and exploration
     * @param learningRate Learning rate
     * @param explorationRate Exploration rate
     */
    public RLParameters(float learningRate, float explorationRate) {
        this.learningRate = learningRate;
        this.explorationRate = explorationRate;
    }
    
    /**
     * Create default parameters
     * @return Default parameters
     */
    public static RLParameters createDefault() {
        return new RLParameters();
    }
    
    /**
     * Create parameters for fast-paced games (FPS, racing, etc.)
     * @return Optimized parameters for fast-paced games
     */
    public static RLParameters createForFastPacedGame() {
        RLParameters params = new RLParameters();
        params.learningRate = 0.0005f;
        params.discountFactor = 0.95f;
        params.explorationRate = 0.2f;
        params.batchSize = 64;
        params.replayBufferSize = 20000;
        params.usePixelInput = true;
        params.entropyCoefficient = 0.02f;
        return params;
    }
    
    /**
     * Create parameters for puzzle games
     * @return Optimized parameters for puzzle games
     */
    public static RLParameters createForPuzzleGame() {
        RLParameters params = new RLParameters();
        params.learningRate = 0.0002f;
        params.discountFactor = 0.99f;
        params.explorationRate = 0.05f;
        params.batchSize = 32;
        params.replayBufferSize = 5000;
        params.usePixelInput = true;
        params.entropyCoefficient = 0.005f;
        return params;
    }
    
    /**
     * Create parameters for RPG games
     * @return Optimized parameters for RPG games
     */
    public static RLParameters createForRpgGame() {
        RLParameters params = new RLParameters();
        params.learningRate = 0.0003f;
        params.discountFactor = 0.98f;
        params.explorationRate = 0.1f;
        params.batchSize = 48;
        params.replayBufferSize = 15000;
        params.usePixelInput = true;
        params.entropyCoefficient = 0.01f;
        return params;
    }
    
    /**
     * Get learning rate
     * @return Learning rate
     */
    public float getLearningRate() {
        return learningRate;
    }
    
    /**
     * Set learning rate
     * @param learningRate Learning rate
     * @return This instance (for chaining)
     */
    public RLParameters setLearningRate(float learningRate) {
        this.learningRate = learningRate;
        return this;
    }
    
    /**
     * Get discount factor
     * @return Discount factor
     */
    public float getDiscountFactor() {
        return discountFactor;
    }
    
    /**
     * Set discount factor
     * @param discountFactor Discount factor
     * @return This instance (for chaining)
     */
    public RLParameters setDiscountFactor(float discountFactor) {
        this.discountFactor = discountFactor;
        return this;
    }
    
    /**
     * Get exploration rate
     * @return Exploration rate
     */
    public float getExplorationRate() {
        return explorationRate;
    }
    
    /**
     * Set exploration rate
     * @param explorationRate Exploration rate
     * @return This instance (for chaining)
     */
    public RLParameters setExplorationRate(float explorationRate) {
        this.explorationRate = explorationRate;
        return this;
    }
    
    /**
     * Get batch size
     * @return Batch size
     */
    public int getBatchSize() {
        return batchSize;
    }
    
    /**
     * Set batch size
     * @param batchSize Batch size
     * @return This instance (for chaining)
     */
    public RLParameters setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
    
    /**
     * Get replay buffer size
     * @return Replay buffer size
     */
    public int getReplayBufferSize() {
        return replayBufferSize;
    }
    
    /**
     * Set replay buffer size
     * @param replayBufferSize Replay buffer size
     * @return This instance (for chaining)
     */
    public RLParameters setReplayBufferSize(int replayBufferSize) {
        this.replayBufferSize = replayBufferSize;
        return this;
    }
    
    /**
     * Get clip epsilon (for PPO)
     * @return Clip epsilon
     */
    public float getClipEpsilon() {
        return clipEpsilon;
    }
    
    /**
     * Set clip epsilon (for PPO)
     * @param clipEpsilon Clip epsilon
     * @return This instance (for chaining)
     */
    public RLParameters setClipEpsilon(float clipEpsilon) {
        this.clipEpsilon = clipEpsilon;
        return this;
    }
    
    /**
     * Get number of epochs
     * @return Number of epochs
     */
    public int getEpochs() {
        return epochs;
    }
    
    /**
     * Set number of epochs
     * @param epochs Number of epochs
     * @return This instance (for chaining)
     */
    public RLParameters setEpochs(int epochs) {
        this.epochs = epochs;
        return this;
    }
    
    /**
     * Get value coefficient
     * @return Value coefficient
     */
    public float getValueCoefficient() {
        return valueCoefficient;
    }
    
    /**
     * Set value coefficient
     * @param valueCoefficient Value coefficient
     * @return This instance (for chaining)
     */
    public RLParameters setValueCoefficient(float valueCoefficient) {
        this.valueCoefficient = valueCoefficient;
        return this;
    }
    
    /**
     * Get entropy coefficient
     * @return Entropy coefficient
     */
    public float getEntropyCoefficient() {
        return entropyCoefficient;
    }
    
    /**
     * Set entropy coefficient
     * @param entropyCoefficient Entropy coefficient
     * @return This instance (for chaining)
     */
    public RLParameters setEntropyCoefficient(float entropyCoefficient) {
        this.entropyCoefficient = entropyCoefficient;
        return this;
    }
    
    /**
     * Get target network update frequency
     * @return Target network update frequency
     */
    public int getTargetNetworkUpdateFrequency() {
        return targetNetworkUpdateFrequency;
    }
    
    /**
     * Set target network update frequency
     * @param targetNetworkUpdateFrequency Target network update frequency
     * @return This instance (for chaining)
     */
    public RLParameters setTargetNetworkUpdateFrequency(int targetNetworkUpdateFrequency) {
        this.targetNetworkUpdateFrequency = targetNetworkUpdateFrequency;
        return this;
    }
    
    /**
     * Get model save frequency
     * @return Model save frequency
     */
    public int getModelSaveFrequency() {
        return modelSaveFrequency;
    }
    
    /**
     * Set model save frequency
     * @param modelSaveFrequency Model save frequency
     * @return This instance (for chaining)
     */
    public RLParameters setModelSaveFrequency(int modelSaveFrequency) {
        this.modelSaveFrequency = modelSaveFrequency;
        return this;
    }
    
    /**
     * Check if using pixel input
     * @return True if using pixel input
     */
    public boolean isUsePixelInput() {
        return usePixelInput;
    }
    
    /**
     * Set whether to use pixel input
     * @param usePixelInput Whether to use pixel input
     * @return This instance (for chaining)
     */
    public RLParameters setUsePixelInput(boolean usePixelInput) {
        this.usePixelInput = usePixelInput;
        return this;
    }
    
    /**
     * Check if using accessibility info
     * @return True if using accessibility info
     */
    public boolean isUseAccessibilityInfo() {
        return useAccessibilityInfo;
    }
    
    /**
     * Set whether to use accessibility info
     * @param useAccessibilityInfo Whether to use accessibility info
     * @return This instance (for chaining)
     */
    public RLParameters setUseAccessibilityInfo(boolean useAccessibilityInfo) {
        this.useAccessibilityInfo = useAccessibilityInfo;
        return this;
    }
    
    /**
     * Check if using discrete actions
     * @return True if using discrete actions
     */
    public boolean isUseDiscreteActions() {
        return useDiscreteActions;
    }
    
    /**
     * Set whether to use discrete actions
     * @param useDiscreteActions Whether to use discrete actions
     * @return This instance (for chaining)
     */
    public RLParameters setUseDiscreteActions(boolean useDiscreteActions) {
        this.useDiscreteActions = useDiscreteActions;
        return this;
    }
    
    /**
     * Get number of action bins
     * @return Number of action bins
     */
    public int getNumActionBins() {
        return numActionBins;
    }
    
    /**
     * Set number of action bins
     * @param numActionBins Number of action bins
     * @return This instance (for chaining)
     */
    public RLParameters setNumActionBins(int numActionBins) {
        this.numActionBins = numActionBins;
        return this;
    }
    
    @Override
    public String toString() {
        return "RLParameters{" +
                "learningRate=" + learningRate +
                ", discountFactor=" + discountFactor +
                ", explorationRate=" + explorationRate +
                ", batchSize=" + batchSize +
                ", replayBufferSize=" + replayBufferSize +
                ", clipEpsilon=" + clipEpsilon +
                ", epochs=" + epochs +
                ", valueCoef=" + valueCoefficient +
                ", entropyCoef=" + entropyCoefficient +
                ", usePixelInput=" + usePixelInput +
                ", useAccessibilityInfo=" + useAccessibilityInfo +
                ", useDiscreteActions=" + useDiscreteActions +
                '}';
    }
}
