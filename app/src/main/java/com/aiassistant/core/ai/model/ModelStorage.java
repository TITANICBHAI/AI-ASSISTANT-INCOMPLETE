package com.aiassistant.core.ai.model;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.algorithms.RLAlgorithm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * Storage for AI models
 */
public class ModelStorage {
    
    private static final String TAG = "ModelStorage";
    
    private final Context context;
    
    /**
     * Constructor
     * 
     * @param context The application context
     */
    public ModelStorage(Context context) {
        this.context = context.getApplicationContext();
    }
    
    /**
     * Save a model
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @param algorithm The algorithm
     * @return Whether the save was successful
     */
    public boolean saveModel(String gameId, int algorithmType, RLAlgorithm algorithm) {
        try {
            // Create models directory if it doesn't exist
            File modelsDir = new File(context.getFilesDir(), "models");
            if (!modelsDir.exists()) {
                modelsDir.mkdirs();
            }
            
            // Create game directory if it doesn't exist
            File gameDir = new File(modelsDir, sanitizeFileName(gameId));
            if (!gameDir.exists()) {
                gameDir.mkdirs();
            }
            
            // Create model file
            File modelFile = new File(gameDir, "model_" + algorithmType + ".dat");
            
            // Get model weights
            float[] weights = algorithm.getWeights();
            if (weights == null || weights.length == 0) {
                Log.e(TAG, "No weights to save");
                return false;
            }
            
            try (FileOutputStream fos = new FileOutputStream(modelFile)) {
                // Write header (algorithm type, weight count)
                ByteBuffer headerBuffer = ByteBuffer.allocate(8);
                headerBuffer.putInt(algorithmType);
                headerBuffer.putInt(weights.length);
                fos.write(headerBuffer.array());
                
                // Write weights
                ByteBuffer weightBuffer = ByteBuffer.allocate(weights.length * 4);
                FloatBuffer floatBuffer = weightBuffer.asFloatBuffer();
                floatBuffer.put(weights);
                fos.write(weightBuffer.array());
                
                // Write hyperparameters
                ByteBuffer paramBuffer = ByteBuffer.allocate(12);
                paramBuffer.putFloat(algorithm.getLearningRate());
                paramBuffer.putFloat(algorithm.getDiscountFactor());
                paramBuffer.putFloat(algorithm.getExplorationRate());
                fos.write(paramBuffer.array());
            }
            
            Log.d(TAG, "Saved model for " + gameId + ", type " + algorithmType + 
                  ", weights: " + weights.length);
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving model: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Load a model
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @param algorithm The algorithm
     * @return Whether the load was successful
     */
    public boolean loadModel(String gameId, int algorithmType, RLAlgorithm algorithm) {
        try {
            File modelFile = getModelFile(gameId, algorithmType);
            if (!modelFile.exists()) {
                Log.d(TAG, "Model file does not exist: " + modelFile.getPath());
                return false;
            }
            
            try (FileInputStream fis = new FileInputStream(modelFile)) {
                // Read header
                byte[] headerBytes = new byte[8];
                if (fis.read(headerBytes) != headerBytes.length) {
                    Log.e(TAG, "Invalid model file (header)");
                    return false;
                }
                
                ByteBuffer headerBuffer = ByteBuffer.wrap(headerBytes);
                int savedAlgorithmType = headerBuffer.getInt();
                int weightCount = headerBuffer.getInt();
                
                if (savedAlgorithmType != algorithmType) {
                    Log.e(TAG, "Algorithm type mismatch: " + savedAlgorithmType + 
                          " vs " + algorithmType);
                    return false;
                }
                
                // Read weights
                byte[] weightBytes = new byte[weightCount * 4];
                if (fis.read(weightBytes) != weightBytes.length) {
                    Log.e(TAG, "Invalid model file (weights)");
                    return false;
                }
                
                ByteBuffer weightBuffer = ByteBuffer.wrap(weightBytes);
                float[] weights = new float[weightCount];
                for (int i = 0; i < weightCount; i++) {
                    weights[i] = weightBuffer.getFloat(i * 4);
                }
                
                // Read hyperparameters
                byte[] paramBytes = new byte[12];
                if (fis.read(paramBytes) != paramBytes.length) {
                    Log.e(TAG, "Invalid model file (params)");
                }
                
                ByteBuffer paramBuffer = ByteBuffer.wrap(paramBytes);
                float learningRate = paramBuffer.getFloat(0);
                float discountFactor = paramBuffer.getFloat(4);
                float explorationRate = paramBuffer.getFloat(8);
                
                // Set weights and hyperparameters
                algorithm.setWeights(weights);
                algorithm.setLearningRate(learningRate);
                algorithm.setDiscountFactor(discountFactor);
                algorithm.setExplorationRate(explorationRate);
                
                Log.d(TAG, "Loaded model for " + gameId + ", type " + algorithmType + 
                      ", weights: " + weights.length);
                
                return true;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading model: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if a model exists
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @return Whether the model exists
     */
    public boolean modelExists(String gameId, int algorithmType) {
        return getModelFile(gameId, algorithmType).exists();
    }
    
    /**
     * Delete a model
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @return Whether the deletion was successful
     */
    public boolean deleteModel(String gameId, int algorithmType) {
        File modelFile = getModelFile(gameId, algorithmType);
        if (modelFile.exists()) {
            return modelFile.delete();
        }
        return true;
    }
    
    /**
     * Delete all models for a game
     * 
     * @param gameId The game ID
     * @return Whether the deletion was successful
     */
    public boolean deleteAllModels(String gameId) {
        File gameDir = new File(new File(context.getFilesDir(), "models"), 
                              sanitizeFileName(gameId));
        if (gameDir.exists()) {
            File[] files = gameDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            return gameDir.delete();
        }
        return true;
    }
    
    /**
     * Get the model file
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @return The model file
     */
    private File getModelFile(String gameId, int algorithmType) {
        File gameDir = new File(new File(context.getFilesDir(), "models"), 
                              sanitizeFileName(gameId));
        return new File(gameDir, "model_" + algorithmType + ".dat");
    }
    
    /**
     * Sanitize a file name
     * 
     * @param name The name
     * @return The sanitized name
     */
    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
