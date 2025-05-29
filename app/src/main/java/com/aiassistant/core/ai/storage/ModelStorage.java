package com.aiassistant.core.ai.storage;

import android.content.Context;
import android.util.Log;

import com.aiassistant.core.ai.model.DQNModel;
import com.aiassistant.core.ai.model.RLModel;
import com.aiassistant.core.ai.model.SARSAModel;
import com.aiassistant.utils.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Storage for AI models
 */
public class ModelStorage {
    
    private static final String TAG = "ModelStorage";
    
    // Algorithm type constants
    public static final int ALGORITHM_DQN = 1;
    public static final int ALGORITHM_SARSA = 2;
    public static final int ALGORITHM_META = 3;
    
    // File name format: game_id_algorithm_type.model
    private static final String FILE_FORMAT = "%s_%d.model";
    
    private static ModelStorage instance;
    private final Context context;
    private final File storageDir;
    
    // Cache for loaded models
    private final Map<String, Object> modelCache;
    
    /**
     * Get the singleton instance
     * 
     * @param context The application context
     * @return The instance
     */
    public static synchronized ModelStorage getInstance(Context context) {
        if (instance == null) {
            instance = new ModelStorage(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Private constructor
     * 
     * @param context The application context
     */
    private ModelStorage(Context context) {
        this.context = context;
        this.storageDir = new File(context.getFilesDir(), Constants.MODEL_STORAGE_DIR);
        this.modelCache = new HashMap<>();
        
        // Create storage directory if it doesn't exist
        if (!storageDir.exists()) {
            boolean created = storageDir.mkdirs();
            if (!created) {
                Log.e(TAG, "Failed to create model storage directory");
            }
        }
    }
    
    /**
     * Save a reinforcement learning model
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @param model The model to save
     * @return True if saved successfully
     */
    public boolean saveRLModel(String gameId, int algorithmType, Object model) {
        if (gameId == null || model == null) {
            return false;
        }
        
        String fileName = String.format(FILE_FORMAT, gameId, algorithmType);
        File file = new File(storageDir, fileName);
        
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            
            oos.writeObject(model);
            
            // Update cache
            String cacheKey = getCacheKey(gameId, algorithmType);
            modelCache.put(cacheKey, model);
            
            Log.d(TAG, "Saved model: " + fileName);
            return true;
            
        } catch (IOException e) {
            Log.e(TAG, "Error saving model " + fileName + ": " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Load a reinforcement learning model
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @return The loaded model, or null if not found
     */
    public Object loadRLModel(String gameId, int algorithmType) {
        if (gameId == null) {
            return null;
        }
        
        // Check cache first
        String cacheKey = getCacheKey(gameId, algorithmType);
        if (modelCache.containsKey(cacheKey)) {
            return modelCache.get(cacheKey);
        }
        
        String fileName = String.format(FILE_FORMAT, gameId, algorithmType);
        File file = new File(storageDir, fileName);
        
        if (!file.exists()) {
            Log.d(TAG, "Model file doesn't exist: " + fileName);
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            
            Object model = ois.readObject();
            
            // Update cache
            modelCache.put(cacheKey, model);
            
            Log.d(TAG, "Loaded model: " + fileName);
            return model;
            
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading model " + fileName + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Check if a model exists
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @return True if model exists
     */
    public boolean modelExists(String gameId, int algorithmType) {
        if (gameId == null) {
            return false;
        }
        
        // Check cache first
        String cacheKey = getCacheKey(gameId, algorithmType);
        if (modelCache.containsKey(cacheKey)) {
            return true;
        }
        
        String fileName = String.format(FILE_FORMAT, gameId, algorithmType);
        File file = new File(storageDir, fileName);
        
        return file.exists();
    }
    
    /**
     * Delete a model
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @return True if deleted successfully
     */
    public boolean deleteModel(String gameId, int algorithmType) {
        if (gameId == null) {
            return false;
        }
        
        String fileName = String.format(FILE_FORMAT, gameId, algorithmType);
        File file = new File(storageDir, fileName);
        
        if (!file.exists()) {
            return true; // Already doesn't exist
        }
        
        boolean deleted = file.delete();
        
        if (deleted) {
            // Remove from cache
            String cacheKey = getCacheKey(gameId, algorithmType);
            modelCache.remove(cacheKey);
            
            Log.d(TAG, "Deleted model: " + fileName);
        } else {
            Log.e(TAG, "Failed to delete model: " + fileName);
        }
        
        return deleted;
    }
    
    /**
     * Delete all models for a game
     * 
     * @param gameId The game ID
     * @return True if deleted successfully
     */
    public boolean deleteAllModels(String gameId) {
        if (gameId == null) {
            return false;
        }
        
        boolean allDeleted = true;
        
        // Delete DQN model
        if (modelExists(gameId, ALGORITHM_DQN)) {
            allDeleted &= deleteModel(gameId, ALGORITHM_DQN);
        }
        
        // Delete SARSA model
        if (modelExists(gameId, ALGORITHM_SARSA)) {
            allDeleted &= deleteModel(gameId, ALGORITHM_SARSA);
        }
        
        // Delete meta-learning model
        if (modelExists(gameId, ALGORITHM_META)) {
            allDeleted &= deleteModel(gameId, ALGORITHM_META);
        }
        
        return allDeleted;
    }
    
    /**
     * Create a new model of the specified type
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @param stateSize The state size
     * @param actionSize The action size
     * @return The created model
     */
    public RLModel createNewModel(String gameId, int algorithmType, int stateSize, int actionSize) {
        if (gameId == null) {
            return null;
        }
        
        try {
            RLModel model;
            
            switch (algorithmType) {
                case ALGORITHM_DQN:
                    model = new DQNModel("dqn_" + gameId, gameId, stateSize, actionSize);
                    break;
                    
                case ALGORITHM_SARSA:
                    model = new SARSAModel("sarsa_" + gameId, gameId, stateSize, actionSize);
                    break;
                    
                default:
                    // Default to DQN
                    model = new DQNModel("dqn_" + gameId, gameId, stateSize, actionSize);
                    break;
            }
            
            // Save the model
            saveRLModel(gameId, algorithmType, model);
            
            return model;
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating new model: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get a cache key
     * 
     * @param gameId The game ID
     * @param algorithmType The algorithm type
     * @return The cache key
     */
    private String getCacheKey(String gameId, int algorithmType) {
        return gameId + "_" + algorithmType;
    }
    
    /**
     * Clear the model cache
     */
    public void clearCache() {
        modelCache.clear();
    }
}
