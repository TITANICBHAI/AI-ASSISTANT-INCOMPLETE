package com.aiassistant.core.ai.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Utility functions for tensor operations and conversions
 * Useful for RL algorithm implementations and data manipulation
 */
public class TensorUtils {
    private static final String TAG = "TensorUtils";
    
    /**
     * Convert a float array to a JSON array
     * @param array Input float array
     * @return JSON array representation
     */
    public static JSONArray floatArrayToJson(float[] array) {
        JSONArray jsonArray = new JSONArray();
        
        if (array != null) {
            for (float value : array) {
                jsonArray.put(value);
            }
        }
        
        return jsonArray;
    }
    
    /**
     * Convert a JSON array to a float array
     * @param jsonArray JSON array
     * @return Float array
     */
    public static float[] jsonToFloatArray(JSONArray jsonArray) {
        if (jsonArray == null) {
            return new float[0];
        }
        
        float[] array = new float[jsonArray.length()];
        
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                array[i] = (float) jsonArray.getDouble(i);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error converting JSON to float array", e);
            return new float[0];
        }
        
        return array;
    }
    
    /**
     * Convert an int array to a JSON array
     * @param array Input int array
     * @return JSON array representation
     */
    public static JSONArray intArrayToJson(int[] array) {
        JSONArray jsonArray = new JSONArray();
        
        if (array != null) {
            for (int value : array) {
                jsonArray.put(value);
            }
        }
        
        return jsonArray;
    }
    
    /**
     * Convert a JSON array to an int array
     * @param jsonArray JSON array
     * @return Int array
     */
    public static int[] jsonToIntArray(JSONArray jsonArray) {
        if (jsonArray == null) {
            return new int[0];
        }
        
        int[] array = new int[jsonArray.length()];
        
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                array[i] = jsonArray.getInt(i);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error converting JSON to int array", e);
            return new int[0];
        }
        
        return array;
    }
    
    /**
     * Convert a 2D tensor (float matrix) to a JSON array
     * @param tensor Input tensor
     * @return JSON array representation
     */
    public static JSONArray tensorToJson(float[][] tensor) {
        JSONArray jsonArray = new JSONArray();
        
        if (tensor != null) {
            for (float[] row : tensor) {
                jsonArray.put(floatArrayToJson(row));
            }
        }
        
        return jsonArray;
    }
    
    /**
     * Convert a JSON array to a 2D tensor (float matrix)
     * @param jsonArray JSON array
     * @return 2D tensor
     */
    public static float[][] jsonToTensor(JSONArray jsonArray) {
        if (jsonArray == null) {
            return new float[0][0];
        }
        
        try {
            float[][] tensor = new float[jsonArray.length()][];
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray rowArray = jsonArray.getJSONArray(i);
                tensor[i] = jsonToFloatArray(rowArray);
            }
            
            return tensor;
            
        } catch (JSONException e) {
            Log.e(TAG, "Error converting JSON to tensor", e);
            return new float[0][0];
        }
    }
    
    /**
     * Calculate the mean of a float array
     * @param array Input array
     * @return Mean value
     */
    public static float mean(float[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }
        
        float sum = 0;
        for (float value : array) {
            sum += value;
        }
        
        return sum / array.length;
    }
    
    /**
     * Calculate the standard deviation of a float array
     * @param array Input array
     * @param mean Mean value (pre-calculated)
     * @return Standard deviation
     */
    public static float standardDeviation(float[] array, float mean) {
        if (array == null || array.length <= 1) {
            return 0;
        }
        
        float sum = 0;
        for (float value : array) {
            float diff = value - mean;
            sum += diff * diff;
        }
        
        return (float) Math.sqrt(sum / (array.length - 1));
    }
    
    /**
     * Normalize an array (zero mean, unit variance)
     * @param array Input array
     * @return Normalized array
     */
    public static float[] normalize(float[] array) {
        if (array == null || array.length == 0) {
            return new float[0];
        }
        
        float mean = mean(array);
        float std = standardDeviation(array, mean);
        
        if (std < 1e-5f) {
            std = 1; // Avoid division by zero
        }
        
        float[] normalized = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            normalized[i] = (array[i] - mean) / std;
        }
        
        return normalized;
    }
    
    /**
     * Calculate dot product of two vectors
     * @param a First vector
     * @param b Second vector
     * @return Dot product
     */
    public static float dotProduct(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0;
        }
        
        float sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        
        return sum;
    }
    
    /**
     * Calculate Euclidean distance between two vectors
     * @param a First vector
     * @param b Second vector
     * @return Euclidean distance
     */
    public static float euclideanDistance(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return Float.MAX_VALUE;
        }
        
        float sum = 0;
        for (int i = 0; i < a.length; i++) {
            float diff = a[i] - b[i];
            sum += diff * diff;
        }
        
        return (float) Math.sqrt(sum);
    }
    
    /**
     * Apply softmax function to an array
     * @param array Input array
     * @return Softmax probabilities
     */
    public static float[] softmax(float[] array) {
        if (array == null || array.length == 0) {
            return new float[0];
        }
        
        // Find max value for numerical stability
        float max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        
        // Calculate softmax
        float[] result = new float[array.length];
        float sum = 0;
        
        for (int i = 0; i < array.length; i++) {
            result[i] = (float) Math.exp(array[i] - max);
            sum += result[i];
        }
        
        // Normalize
        for (int i = 0; i < array.length; i++) {
            result[i] /= sum;
        }
        
        return result;
    }
    
    /**
     * Apply ReLU activation function to an array
     * @param array Input array
     * @return ReLU-activated array
     */
    public static float[] relu(float[] array) {
        if (array == null) {
            return new float[0];
        }
        
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Math.max(0, array[i]);
        }
        
        return result;
    }
    
    /**
     * Find the index of the maximum value in an array
     * @param array Input array
     * @return Index of maximum value
     */
    public static int argmax(float[] array) {
        if (array == null || array.length == 0) {
            return -1;
        }
        
        int maxIndex = 0;
        float maxValue = array[0];
        
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }
        
        return maxIndex;
    }
}
