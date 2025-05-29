package com.aiassistant.database.converters;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Type converter for float arrays in Room database
 */
public class FloatArrayConverter {
    
    /**
     * Convert float array to String for database storage
     * Format: comma-separated values
     * @param floatArray Array to convert
     * @return String representation or null
     */
    @TypeConverter
    public static String fromFloatArray(float[] floatArray) {
        if (floatArray == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < floatArray.length; i++) {
            sb.append(floatArray[i]);
            if (i < floatArray.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
    
    /**
     * Convert String from database to float array
     * @param string String to convert
     * @return float array or null
     */
    @TypeConverter
    public static float[] toFloatArray(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        
        String[] parts = string.split(",");
        float[] result = new float[parts.length];
        
        for (int i = 0; i < parts.length; i++) {
            try {
                result[i] = Float.parseFloat(parts[i]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                result[i] = 0f;
            }
        }
        
        return result;
    }
}