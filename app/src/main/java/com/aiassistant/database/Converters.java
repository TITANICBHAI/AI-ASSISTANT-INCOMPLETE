package com.aiassistant.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Type converters for Room database
 */
public class Converters {
    private static final Gson gson = new Gson();
    
    /**
     * Convert Date to timestamp
     * 
     * @param date The date to convert
     * @return The timestamp
     */
    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }
    
    /**
     * Convert timestamp to Date
     * 
     * @param value The timestamp to convert
     * @return The date
     */
    @TypeConverter
    public static Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }
    
    /**
     * Convert Map<String, Float> to JSON string
     * 
     * @param map The map to convert
     * @return The JSON string
     */
    @TypeConverter
    public static String fromStringFloatMap(Map<String, Float> map) {
        if (map == null) {
            return null;
        }
        return gson.toJson(map);
    }
    
    /**
     * Convert JSON string to Map<String, Float>
     * 
     * @param value The JSON string to convert
     * @return The map
     */
    @TypeConverter
    public static Map<String, Float> toStringFloatMap(String value) {
        if (value == null) {
            return new HashMap<>();
        }
        Type type = new TypeToken<Map<String, Float>>(){}.getType();
        return gson.fromJson(value, type);
    }
    
    /**
     * Convert float array to JSON string
     * 
     * @param array The array to convert
     * @return The JSON string
     */
    @TypeConverter
    public static String fromFloatArray(float[] array) {
        if (array == null) {
            return null;
        }
        return gson.toJson(array);
    }
    
    /**
     * Convert JSON string to float array
     * 
     * @param value The JSON string to convert
     * @return The array
     */
    @TypeConverter
    public static float[] toFloatArray(String value) {
        if (value == null) {
            return new float[0];
        }
        return gson.fromJson(value, float[].class);
    }
}
