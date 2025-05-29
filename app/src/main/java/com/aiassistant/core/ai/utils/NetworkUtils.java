package com.aiassistant.core.ai.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Utility functions for neural network operations
 * Provides common functionality used by all RL algorithms
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    
    // Flag for verbose logging (useful for debugging)
    public static boolean VERBOSE_LOGGING = false;
    
    // Random number generator
    private static final Random random = new Random();
    
    /**
     * Initialize a neural network layer with Xavier/Glorot initialization
     * @param inputs Number of inputs
     * @param outputs Number of outputs
     * @return Initialized weights
     */
    public static float[][] initializeLayerWeights(int inputs, int outputs) {
        float[][] weights = new float[inputs][outputs];
        float range = (float) Math.sqrt(6.0 / (inputs + outputs));
        
        for (int i = 0; i < inputs; i++) {
            for (int j = 0; j < outputs; j++) {
                weights[i][j] = (random.nextFloat() * 2.0f - 1.0f) * range;
            }
        }
        
        return weights;
    }
    
    /**
     * Initialize bias values (to zero)
     * @param size Size of bias vector
     * @return Initialized biases
     */
    public static float[] initializeBiases(int size) {
        return new float[size];
    }
    
    /**
     * Save a neural network to a JSON file
     * @param context Application context
     * @param filename Filename
     * @param architecture Network architecture (layer sizes)
     * @param weights Network weights
     * @param biases Network biases
     * @return True if successful
     */
    public static boolean saveNetwork(Context context, String filename, 
                                     int[] architecture, float[][][] weights, float[][] biases) {
        try {
            JSONObject json = new JSONObject();
            
            // Save architecture
            json.put("architecture", TensorUtils.intArrayToJson(architecture));
            
            // Save weights
            JSONArray weightsArray = new JSONArray();
            for (float[][] layerWeights : weights) {
                weightsArray.put(TensorUtils.tensorToJson(layerWeights));
            }
            json.put("weights", weightsArray);
            
            // Save biases
            JSONArray biasesArray = new JSONArray();
            for (float[] layerBiases : biases) {
                biasesArray.put(TensorUtils.floatArrayToJson(layerBiases));
            }
            json.put("biases", biasesArray);
            
            // Create models directory if it doesn't exist
            File modelsDir = new File(context.getFilesDir(), "models");
            if (!modelsDir.exists()) {
                modelsDir.mkdirs();
            }
            
            // Write to file
            File file = new File(modelsDir, filename);
            FileWriter writer = new FileWriter(file);
            writer.write(json.toString());
            writer.close();
            
            Log.d(TAG, "Saved neural network to " + file.getAbsolutePath());
            return true;
            
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Error saving neural network", e);
            return false;
        }
    }
    
    /**
     * Load a neural network from a JSON file
     * @param context Application context
     * @param filename Filename
     * @param architecture Output parameter for network architecture
     * @param weights Output parameter for network weights
     * @param biases Output parameter for network biases
     * @return True if successful
     */
    public static boolean loadNetwork(Context context, String filename, 
                                     int[] architecture, float[][][] weights, float[][] biases) {
        try {
            // Read file
            File file = new File(new File(context.getFilesDir(), "models"), filename);
            if (!file.exists()) {
                Log.e(TAG, "Network file not found: " + file.getAbsolutePath());
                return false;
            }
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            
            // Parse JSON
            JSONObject json = new JSONObject(builder.toString());
            
            // Load architecture
            JSONArray archArray = json.getJSONArray("architecture");
            int[] arch = TensorUtils.jsonToIntArray(archArray);
            System.arraycopy(arch, 0, architecture, 0, Math.min(arch.length, architecture.length));
            
            // Load weights
            JSONArray weightsArray = json.getJSONArray("weights");
            for (int i = 0; i < Math.min(weightsArray.length(), weights.length); i++) {
                JSONArray layerArray = weightsArray.getJSONArray(i);
                weights[i] = TensorUtils.jsonToTensor(layerArray);
            }
            
            // Load biases
            JSONArray biasesArray = json.getJSONArray("biases");
            for (int i = 0; i < Math.min(biasesArray.length(), biases.length); i++) {
                JSONArray layerArray = biasesArray.getJSONArray(i);
                biases[i] = TensorUtils.jsonToFloatArray(layerArray);
            }
            
            Log.d(TAG, "Loaded neural network from " + file.getAbsolutePath());
            return true;
            
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Error loading neural network", e);
            return false;
        }
    }
    
    /**
     * Perform forward propagation through a neural network
     * @param input Input vector
     * @param weights Network weights
     * @param biases Network biases
     * @return Network output
     */
    public static float[] forwardPropagate(float[] input, float[][][] weights, float[][] biases) {
        if (input == null || weights == null || biases == null || 
            weights.length == 0 || biases.length == 0) {
            return new float[0];
        }
        
        // Current activation (starts with input)
        float[] activation = input;
        
        // Forward through each layer
        for (int layer = 0; layer < weights.length; layer++) {
            float[][] layerWeights = weights[layer];
            float[] layerBiases = biases[layer];
            
            // Calculate pre-activations
            float[] preActivations = new float[layerBiases.length];
            for (int j = 0; j < preActivations.length; j++) {
                preActivations[j] = layerBiases[j];
                for (int i = 0; i < Math.min(activation.length, layerWeights.length); i++) {
                    if (j < layerWeights[i].length) {
                        preActivations[j] += activation[i] * layerWeights[i][j];
                    }
                }
            }
            
            // Apply activation function
            if (layer == weights.length - 1) {
                // Output layer - use softmax for policy networks
                activation = TensorUtils.softmax(preActivations);
            } else {
                // Hidden layers - use ReLU
                activation = TensorUtils.relu(preActivations);
            }
        }
        
        return activation;
    }
    
    /**
     * Check if a model file exists
     * @param context Application context
     * @param filename Filename
     * @return True if the file exists
     */
    public static boolean modelExists(Context context, String filename) {
        File file = new File(new File(context.getFilesDir(), "models"), filename);
        return file.exists();
    }
    
    /**
     * Delete a model file
     * @param context Application context
     * @param filename Filename
     * @return True if the file was deleted successfully
     */
    public static boolean deleteModel(Context context, String filename) {
        File file = new File(new File(context.getFilesDir(), "models"), filename);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
    
    /**
     * Get a list of all model files
     * @param context Application context
     * @return Array of filenames
     */
    public static String[] listModels(Context context) {
        File modelsDir = new File(context.getFilesDir(), "models");
        if (!modelsDir.exists()) {
            modelsDir.mkdirs();
            return new String[0];
        }
        
        return modelsDir.list();
    }
    
    /**
     * Quantize network weights to reduce memory usage
     * @param weights Network weights
     * @param bits Number of bits per weight (e.g., 8)
     * @return Quantized weights
     */
    public static float[][][] quantizeWeights(float[][][] weights, int bits) {
        if (weights == null || weights.length == 0 || bits < 1) {
            return weights;
        }
        
        float[][][] quantizedWeights = new float[weights.length][][];
        
        for (int l = 0; l < weights.length; l++) {
            float[][] layerWeights = weights[l];
            quantizedWeights[l] = new float[layerWeights.length][];
            
            // Find min and max values for each layer
            float minVal = Float.MAX_VALUE;
            float maxVal = -Float.MAX_VALUE;
            
            for (float[] neuronWeights : layerWeights) {
                for (float weight : neuronWeights) {
                    minVal = Math.min(minVal, weight);
                    maxVal = Math.max(maxVal, weight);
                }
            }
            
            // Calculate scale factor
            float scale = (maxVal - minVal) / ((1 << bits) - 1);
            if (scale < 1e-6f) {
                scale = 1e-6f; // Avoid division by zero
            }
            
            // Quantize weights
            for (int i = 0; i < layerWeights.length; i++) {
                float[] neuronWeights = layerWeights[i];
                quantizedWeights[l][i] = new float[neuronWeights.length];
                
                for (int j = 0; j < neuronWeights.length; j++) {
                    int quantized = Math.round((neuronWeights[j] - minVal) / scale);
                    quantizedWeights[l][i][j] = minVal + quantized * scale;
                }
            }
        }
        
        return quantizedWeights;
    }
    
    /**
     * Prune network weights to reduce model size
     * @param weights Network weights
     * @param biases Network biases
     * @param pruningThreshold Threshold below which weights are set to zero
     * @return Number of pruned weights
     */
    public static int pruneWeights(float[][][] weights, float[][] biases, float pruningThreshold) {
        if (weights == null || weights.length == 0) {
            return 0;
        }
        
        int prunedCount = 0;
        
        for (int l = 0; l < weights.length; l++) {
            float[][] layerWeights = weights[l];
            
            for (int i = 0; i < layerWeights.length; i++) {
                float[] neuronWeights = layerWeights[i];
                
                for (int j = 0; j < neuronWeights.length; j++) {
                    if (Math.abs(neuronWeights[j]) < pruningThreshold) {
                        neuronWeights[j] = 0.0f;
                        prunedCount++;
                    }
                }
            }
        }
        
        return prunedCount;
    }
}
