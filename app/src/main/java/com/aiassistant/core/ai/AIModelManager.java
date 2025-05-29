package com.aiassistant.core.ai;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aiassistant.core.AIAssistantApplication;
import com.aiassistant.core.ai.algorithms.DQNAlgorithm;
import com.aiassistant.core.ai.algorithms.MetaLearningAlgorithm;
import com.aiassistant.core.ai.algorithms.PPOAlgorithm;
import com.aiassistant.core.ai.algorithms.QLearningAlgorithm;
import com.aiassistant.core.ai.algorithms.ReinforcementLearningAlgorithm;
import com.aiassistant.core.ai.algorithms.SARSAAlgorithm;
import com.aiassistant.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Manages AI models for the application.
 * Handles loading, saving, and switching between different AI model types.
 */
public class AIModelManager {
    private static final String TAG = "AIModelManager";
    private static final String MODELS_DIR = "ai_models";
    private static final int MAX_MEMORY_USAGE = 1024; // Maximum memory usage in MB
    
    // Reference to application context for file operations
    private final Context context;
    
    // Map of algorithm instances
    private final Map<String, ReinforcementLearningAlgorithm> algorithms = new HashMap<>();
    
    // Meta-learning algorithm for algorithm selection
    private MetaLearningAlgorithm metaLearningAlgorithm;
    
    // Current memory usage level (1-5)
    private int memoryUsageLevel = 3;
    
    /**
     * Constructor for AI Model Manager
     * @param application Application instance
     */
    public AIModelManager(@NonNull AIAssistantApplication application) {
        this.context = application.getApplicationContext();
        
        // Initialize algorithms
        initializeAlgorithms();
    }
    
    /**
     * Initialize all reinforcement learning algorithms
     */
    private void initializeAlgorithms() {
        Log.d(TAG, "Initializing AI algorithms");
        
        // Create algorithm instances
        metaLearningAlgorithm = new MetaLearningAlgorithm(context);
        PPOAlgorithm ppoAlgorithm = new PPOAlgorithm(context);
        DQNAlgorithm dqnAlgorithm = new DQNAlgorithm(context);
        SARSAAlgorithm sarsaAlgorithm = new SARSAAlgorithm(context);
        QLearningAlgorithm qLearningAlgorithm = new QLearningAlgorithm(context);
        
        // Add algorithms to map for easy access
        algorithms.put("meta", metaLearningAlgorithm);
        algorithms.put("ppo", ppoAlgorithm);
        algorithms.put("dqn", dqnAlgorithm);
        algorithms.put("sarsa", sarsaAlgorithm);
        algorithms.put("qlearning", qLearningAlgorithm);
        
        // Register algorithms with meta-learning algorithm
        metaLearningAlgorithm.registerAlgorithm(MetaLearningAlgorithm.ALGORITHM_PPO, ppoAlgorithm);
        metaLearningAlgorithm.registerAlgorithm(MetaLearningAlgorithm.ALGORITHM_DQN, dqnAlgorithm);
        metaLearningAlgorithm.registerAlgorithm(MetaLearningAlgorithm.ALGORITHM_SARSA, sarsaAlgorithm);
        metaLearningAlgorithm.registerAlgorithm(MetaLearningAlgorithm.ALGORITHM_Q_LEARNING, qLearningAlgorithm);
        
        // Load models from storage
        loadModelsFromStorage();
    }
    
    /**
     * Load saved models from storage
     */
    private void loadModelsFromStorage() {
        Log.d(TAG, "Loading models from storage");
        
        // Create models directory if it doesn't exist
        File modelsDir = new File(context.getFilesDir(), MODELS_DIR);
        if (!modelsDir.exists()) {
            if (!modelsDir.mkdirs()) {
                Log.e(TAG, "Failed to create models directory");
                return;
            }
        }
        
        // Load model for each algorithm
        for (Map.Entry<String, ReinforcementLearningAlgorithm> entry : algorithms.entrySet()) {
            String algorithmName = entry.getKey();
            ReinforcementLearningAlgorithm algorithm = entry.getValue();
            
            File modelFile = new File(modelsDir, algorithmName + ".model");
            if (modelFile.exists()) {
                try {
                    algorithm.loadModel(modelFile);
                    Log.d(TAG, "Loaded model for " + algorithmName);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading model for " + algorithmName, e);
                }
            } else {
                Log.d(TAG, "No saved model found for " + algorithmName);
            }
        }
    }
    
    /**
     * Save models to storage
     */
    public void saveModelsToStorage() {
        Log.d(TAG, "Saving models to storage");
        
        // Create models directory if it doesn't exist
        File modelsDir = new File(context.getFilesDir(), MODELS_DIR);
        if (!modelsDir.exists()) {
            if (!modelsDir.mkdirs()) {
                Log.e(TAG, "Failed to create models directory");
                return;
            }
        }
        
        // Save model for each algorithm
        for (Map.Entry<String, ReinforcementLearningAlgorithm> entry : algorithms.entrySet()) {
            String algorithmName = entry.getKey();
            ReinforcementLearningAlgorithm algorithm = entry.getValue();
            
            File modelFile = new File(modelsDir, algorithmName + ".model");
            try {
                algorithm.saveModel(modelFile);
                Log.d(TAG, "Saved model for " + algorithmName);
            } catch (Exception e) {
                Log.e(TAG, "Error saving model for " + algorithmName, e);
            }
        }
    }
    
    /**
     * Clear all models
     */
    public void clearAllModels() {
        Log.d(TAG, "Clearing all models");
        
        // Delete model files
        File modelsDir = new File(context.getFilesDir(), MODELS_DIR);
        if (modelsDir.exists()) {
            File[] files = modelsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        Log.d(TAG, "Deleted model file: " + file.getName());
                    } else {
                        Log.e(TAG, "Failed to delete model file: " + file.getName());
                    }
                }
            }
        }
        
        // Reset algorithms
        for (ReinforcementLearningAlgorithm algorithm : algorithms.values()) {
            algorithm.resetModel();
        }
    }
    
    /**
     * Export models to a zip file
     * @param outputFile The output zip file
     * @return True if export was successful
     */
    public boolean exportModels(File outputFile) {
        Log.d(TAG, "Exporting models to " + outputFile.getAbsolutePath());
        
        try {
            // Save models before exporting
            saveModelsToStorage();
            
            // Create output directory if it doesn't exist
            if (!outputFile.getParentFile().exists()) {
                if (!outputFile.getParentFile().mkdirs()) {
                    Log.e(TAG, "Failed to create output directory");
                    return false;
                }
            }
            
            // Create zip file
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(outputFile));
            
            // Get models directory
            File modelsDir = new File(context.getFilesDir(), MODELS_DIR);
            if (!modelsDir.exists()) {
                Log.e(TAG, "Models directory does not exist");
                zipOut.close();
                return false;
            }
            
            // Add model files to zip
            File[] files = modelsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    addFileToZip(file, file.getName(), zipOut);
                }
            }
            
            zipOut.close();
            Log.d(TAG, "Models exported successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error exporting models", e);
            return false;
        }
    }
    
    /**
     * Export models to a content URI
     * @param context Context for content resolver
     * @param uri The output URI
     * @return True if export was successful
     */
    public boolean exportModelsToUri(Context context, Uri uri) {
        Log.d(TAG, "Exporting models to URI: " + uri);
        
        try {
            // Save models before exporting
            saveModelsToStorage();
            
            // Open output stream for URI
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            if (outputStream == null) {
                Log.e(TAG, "Failed to open output stream for URI");
                return false;
            }
            
            // Create zip file
            ZipOutputStream zipOut = new ZipOutputStream(outputStream);
            
            // Get models directory
            File modelsDir = new File(context.getFilesDir(), MODELS_DIR);
            if (!modelsDir.exists()) {
                Log.e(TAG, "Models directory does not exist");
                zipOut.close();
                return false;
            }
            
            // Add model files to zip
            File[] files = modelsDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    addFileToZip(file, file.getName(), zipOut);
                }
            }
            
            zipOut.close();
            Log.d(TAG, "Models exported successfully to URI");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error exporting models to URI", e);
            return false;
        }
    }
    
    /**
     * Add a file to a zip stream
     * @param file File to add
     * @param entryName Name for the zip entry
     * @param zipOut Zip output stream
     * @throws IOException If file operations fail
     */
    private void addFileToZip(File file, String entryName, ZipOutputStream zipOut) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(entryName);
            zipOut.putNextEntry(zipEntry);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zipOut.write(buffer, 0, length);
            }
            
            zipOut.closeEntry();
        }
    }
    
    /**
     * Import models from a zip file
     * @param inputFile The input zip file
     * @return True if import was successful
     */
    public boolean importModels(File inputFile) {
        Log.d(TAG, "Importing models from " + inputFile.getAbsolutePath());
        
        try {
            // Create models directory if it doesn't exist
            File modelsDir = new File(context.getFilesDir(), MODELS_DIR);
            if (!modelsDir.exists()) {
                if (!modelsDir.mkdirs()) {
                    Log.e(TAG, "Failed to create models directory");
                    return false;
                }
            }
            
            // Open zip file
            ZipInputStream zipIn = new ZipInputStream(new FileInputStream(inputFile));
            
            // Extract files from zip
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String fileName = entry.getName();
                File outputFile = new File(modelsDir, fileName);
                
                // Create output file
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zipIn.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            
            zipIn.close();
            
            // Load imported models
            loadModelsFromStorage();
            
            Log.d(TAG, "Models imported successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error importing models", e);
            return false;
        }
    }
    
    /**
     * Import models from a content URI
     * @param context Context for content resolver
     * @param uri The input URI
     * @return True if import was successful
     */
    public boolean importModelsFromUri(Context context, Uri uri) {
        Log.d(TAG, "Importing models from URI: " + uri);
        
        try {
            // Open input stream for URI
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for URI");
                return false;
            }
            
            // Create models directory if it doesn't exist
            File modelsDir = new File(context.getFilesDir(), MODELS_DIR);
            if (!modelsDir.exists()) {
                if (!modelsDir.mkdirs()) {
                    Log.e(TAG, "Failed to create models directory");
                    inputStream.close();
                    return false;
                }
            }
            
            // Open zip file
            ZipInputStream zipIn = new ZipInputStream(inputStream);
            
            // Extract files from zip
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String fileName = entry.getName();
                File outputFile = new File(modelsDir, fileName);
                
                // Create output file
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zipIn.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            
            zipIn.close();
            
            // Load imported models
            loadModelsFromStorage();
            
            Log.d(TAG, "Models imported successfully from URI");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error importing models from URI", e);
            return false;
        }
    }
    
    /**
     * Switch to a specific algorithm
     * @param algorithmIndex The algorithm index (0=Meta, 1=PPO, 2=DQN, 3=SARSA, 4=Q-Learning)
     */
    public void switchAlgorithm(int algorithmIndex) {
        Log.d(TAG, "Switching to algorithm index: " + algorithmIndex);
        
        // Convert index to MetaLearningAlgorithm constant
        int algorithmType;
        switch (algorithmIndex) {
            case 1: algorithmType = MetaLearningAlgorithm.ALGORITHM_PPO; break;
            case 2: algorithmType = MetaLearningAlgorithm.ALGORITHM_DQN; break;
            case 3: algorithmType = MetaLearningAlgorithm.ALGORITHM_SARSA; break;
            case 4: algorithmType = MetaLearningAlgorithm.ALGORITHM_Q_LEARNING; break;
            default: algorithmType = MetaLearningAlgorithm.ALGORITHM_AUTO;
        }
        
        // Update meta-learning algorithm settings
        metaLearningAlgorithm.setForceAlgorithmType(algorithmType);
    }
    
    /**
     * Set memory usage level for models
     * @param level Memory level (1-5)
     */
    public void setMemoryUsageLevel(int level) {
        if (level >= 1 && level <= 5) {
            memoryUsageLevel = level;
            
            // Calculate actual memory limit based on level
            int memoryLimit = calculateMemoryLimit(level);
            Log.d(TAG, "Setting memory limit to " + memoryLimit + " MB");
            
            // Update memory limits for all algorithms
            for (ReinforcementLearningAlgorithm algorithm : algorithms.values()) {
                algorithm.setMemoryLimit(memoryLimit);
            }
        }
    }
    
    /**
     * Calculate memory limit based on level
     * @param level Memory level (1-5)
     * @return Memory limit in MB
     */
    private int calculateMemoryLimit(int level) {
        switch (level) {
            case 1: return 150;  // Minimal
            case 2: return 300;  // Low
            case 3: return 500;  // Medium
            case 4: return 800;  // High
            case 5: return 1200; // Maximum
            default: return 500; // Default to medium
        }
    }
    
    /**
     * Get the meta-learning algorithm
     * @return The meta-learning algorithm instance
     */
    public MetaLearningAlgorithm getMetaLearningAlgorithm() {
        return metaLearningAlgorithm;
    }
    
    /**
     * Get a specific algorithm by name
     * @param name The algorithm name (meta, ppo, dqn, sarsa, qlearning)
     * @return The algorithm instance or null if not found
     */
    public ReinforcementLearningAlgorithm getAlgorithm(String name) {
        return algorithms.get(name.toLowerCase());
    }
    
    /**
     * Get a specific algorithm by index
     * @param index The algorithm index (0=Meta, 1=PPO, 2=DQN, 3=SARSA, 4=Q-Learning)
     * @return The algorithm instance or meta-learning algorithm if not found
     */
    public ReinforcementLearningAlgorithm getAlgorithmByIndex(int index) {
        switch (index) {
            case 1: return algorithms.get("ppo");
            case 2: return algorithms.get("dqn");
            case 3: return algorithms.get("sarsa");
            case 4: return algorithms.get("qlearning");
            default: return algorithms.get("meta");
        }
    }
    
    /**
     * Get all available algorithms
     * @return List of algorithm names
     */
    public List<String> getAvailableAlgorithms() {
        return new ArrayList<>(algorithms.keySet());
    }
    
    /**
     * Get current memory usage level
     * @return Memory usage level (1-5)
     */
    public int getMemoryUsageLevel() {
        return memoryUsageLevel;
    }
}
