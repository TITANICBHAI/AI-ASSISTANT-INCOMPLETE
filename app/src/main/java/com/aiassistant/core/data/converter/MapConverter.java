package com.aiassistant.core.data.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Type converter for Room database
 * Converts between Map objects and JSON strings for database storage
 */
public class MapConverter {
    private static final Gson gson = new Gson();
    
    /**
     * Convert string map to JSON string
     * @param stringMap Map of string to string
     * @return JSON string representation
     */
    @TypeConverter
    public static String fromStringMap(Map<String, String> stringMap) {
        if (stringMap == null) {
            return null;
        }
        return gson.toJson(stringMap);
    }
    
    /**
     * Convert JSON string to string map
     * @param jsonString JSON string representation
     * @return Map of string to string
     */
    @TypeConverter
    public static Map<String, String> toStringMap(String jsonString) {
        if (jsonString == null) {
            return new HashMap<>();
        }
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(jsonString, type);
    }
    
    /**
     * Convert float map to JSON string
     * @param floatMap Map of string to float
     * @return JSON string representation
     */
    @TypeConverter
    public static String fromFloatMap(Map<String, Float> floatMap) {
        if (floatMap == null) {
            return null;
        }
        return gson.toJson(floatMap);
    }
    
    /**
     * Convert JSON string to float map
     * @param jsonString JSON string representation
     * @return Map of string to float
     */
    @TypeConverter
    public static Map<String, Float> toFloatMap(String jsonString) {
        if (jsonString == null) {
            return new HashMap<>();
        }
        Type type = new TypeToken<Map<String, Float>>(){}.getType();
        return gson.fromJson(jsonString, type);
    }
}
