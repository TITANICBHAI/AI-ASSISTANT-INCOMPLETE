package com.aiassistant.core.ai.model;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Simple neural network model class that can be serialized
 */
public class NeuralNetworkModel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private float[][] inputWeights;
    private float[][] hiddenWeights;
    private float[] inputBiases;
    private float[] hiddenBiases;
    private int inputSize;
    private int hiddenSize;
    private int outputSize;
    private String modelName;
    private String gameId;
    
    /**
     * Constructor
     * 
     * @param inputSize The input layer size
     * @param hiddenSize The hidden layer size
     * @param outputSize The output layer size
     * @param modelName The model name
     * @param gameId The game ID
     */
    public NeuralNetworkModel(int inputSize, int hiddenSize, int outputSize, String modelName, String gameId) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        this.modelName = modelName;
        this.gameId = gameId;
        
        // Initialize weights with small random values
        inputWeights = new float[inputSize][hiddenSize];
        hiddenWeights = new float[hiddenSize][outputSize];
        inputBiases = new float[hiddenSize];
        hiddenBiases = new float[outputSize];
        
        initializeWeights();
    }
    
    /**
     * Initialize weights with small random values
     */
    private void initializeWeights() {
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                inputWeights[i][j] = (float) (Math.random() * 0.2 - 0.1);
            }
        }
        
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                hiddenWeights[i][j] = (float) (Math.random() * 0.2 - 0.1);
            }
        }
        
        for (int i = 0; i < hiddenSize; i++) {
            inputBiases[i] = (float) (Math.random() * 0.2 - 0.1);
        }
        
        for (int i = 0; i < outputSize; i++) {
            hiddenBiases[i] = (float) (Math.random() * 0.2 - 0.1);
        }
    }
    
    /**
     * Predict output from input
     * 
     * @param input The input vector
     * @return The output vector
     */
    public float[] predict(float[] input) {
        if (input.length != inputSize) {
            throw new IllegalArgumentException("Input size does not match model input size");
        }
        
        // Forward pass through the network
        float[] hidden = new float[hiddenSize];
        float[] output = new float[outputSize];
        
        // Input to hidden layer
        for (int j = 0; j < hiddenSize; j++) {
            float sum = inputBiases[j];
            for (int i = 0; i < inputSize; i++) {
                sum += input[i] * inputWeights[i][j];
            }
            hidden[j] = sigmoid(sum);
        }
        
        // Hidden to output layer
        for (int k = 0; k < outputSize; k++) {
            float sum = hiddenBiases[k];
            for (int j = 0; j < hiddenSize; j++) {
                sum += hidden[j] * hiddenWeights[j][k];
            }
            output[k] = sigmoid(sum);
        }
        
        return output;
    }
    
    /**
     * Sigmoid activation function
     * 
     * @param x The input value
     * @return The sigmoid of x
     */
    private float sigmoid(float x) {
        return (float) (1.0 / (1.0 + Math.exp(-x)));
    }
    
    /**
     * Create a copy of the model with slightly adapted weights
     * 
     * @param adaptationRate The adaptation rate (0.0-1.0)
     * @param newGameId The new game ID
     * @return The adapted model
     */
    public NeuralNetworkModel createAdaptedModel(float adaptationRate, String newGameId) {
        NeuralNetworkModel newModel = new NeuralNetworkModel(
                inputSize, hiddenSize, outputSize, modelName, newGameId);
        
        // Adapt input weights
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                newModel.inputWeights[i][j] = (1 - adaptationRate) * inputWeights[i][j] +
                        adaptationRate * newModel.inputWeights[i][j];
            }
        }
        
        // Adapt hidden weights
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                newModel.hiddenWeights[i][j] = (1 - adaptationRate) * hiddenWeights[i][j] +
                        adaptationRate * newModel.hiddenWeights[i][j];
            }
        }
        
        // Adapt biases
        for (int i = 0; i < hiddenSize; i++) {
            newModel.inputBiases[i] = (1 - adaptationRate) * inputBiases[i] +
                    adaptationRate * newModel.inputBiases[i];
        }
        
        for (int i = 0; i < outputSize; i++) {
            newModel.hiddenBiases[i] = (1 - adaptationRate) * hiddenBiases[i] +
                    adaptationRate * newModel.hiddenBiases[i];
        }
        
        return newModel;
    }
    
    /**
     * Get the input weights
     * 
     * @return The input weights
     */
    public float[][] getInputWeights() {
        return deepCopy(inputWeights);
    }
    
    /**
     * Set the input weights
     * 
     * @param inputWeights The input weights
     */
    public void setInputWeights(float[][] inputWeights) {
        if (inputWeights.length != this.inputWeights.length ||
                inputWeights[0].length != this.inputWeights[0].length) {
            throw new IllegalArgumentException("Input weights dimensions don't match");
        }
        this.inputWeights = deepCopy(inputWeights);
    }
    
    /**
     * Get the hidden weights
     * 
     * @return The hidden weights
     */
    public float[][] getHiddenWeights() {
        return deepCopy(hiddenWeights);
    }
    
    /**
     * Set the hidden weights
     * 
     * @param hiddenWeights The hidden weights
     */
    public void setHiddenWeights(float[][] hiddenWeights) {
        if (hiddenWeights.length != this.hiddenWeights.length ||
                hiddenWeights[0].length != this.hiddenWeights[0].length) {
            throw new IllegalArgumentException("Hidden weights dimensions don't match");
        }
        this.hiddenWeights = deepCopy(hiddenWeights);
    }
    
    /**
     * Get the input biases
     * 
     * @return The input biases
     */
    public float[] getInputBiases() {
        return Arrays.copyOf(inputBiases, inputBiases.length);
    }
    
    /**
     * Set the input biases
     * 
     * @param inputBiases The input biases
     */
    public void setInputBiases(float[] inputBiases) {
        if (inputBiases.length != this.inputBiases.length) {
            throw new IllegalArgumentException("Input biases length doesn't match");
        }
        this.inputBiases = Arrays.copyOf(inputBiases, inputBiases.length);
    }
    
    /**
     * Get the hidden biases
     * 
     * @return The hidden biases
     */
    public float[] getHiddenBiases() {
        return Arrays.copyOf(hiddenBiases, hiddenBiases.length);
    }
    
    /**
     * Set the hidden biases
     * 
     * @param hiddenBiases The hidden biases
     */
    public void setHiddenBiases(float[] hiddenBiases) {
        if (hiddenBiases.length != this.hiddenBiases.length) {
            throw new IllegalArgumentException("Hidden biases length doesn't match");
        }
        this.hiddenBiases = Arrays.copyOf(hiddenBiases, hiddenBiases.length);
    }
    
    /**
     * Get the input size
     * 
     * @return The input size
     */
    public int getInputSize() {
        return inputSize;
    }
    
    /**
     * Get the hidden size
     * 
     * @return The hidden size
     */
    public int getHiddenSize() {
        return hiddenSize;
    }
    
    /**
     * Get the output size
     * 
     * @return The output size
     */
    public int getOutputSize() {
        return outputSize;
    }
    
    /**
     * Get the model name
     * 
     * @return The model name
     */
    public String getModelName() {
        return modelName;
    }
    
    /**
     * Set the model name
     * 
     * @param modelName The model name
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    /**
     * Get the game ID
     * 
     * @return The game ID
     */
    public String getGameId() {
        return gameId;
    }
    
    /**
     * Set the game ID
     * 
     * @param gameId The game ID
     */
    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
    
    /**
     * Make a deep copy of a 2D array
     * 
     * @param original The original array
     * @return The deep copy
     */
    private float[][] deepCopy(float[][] original) {
        float[][] copy = new float[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }
}
