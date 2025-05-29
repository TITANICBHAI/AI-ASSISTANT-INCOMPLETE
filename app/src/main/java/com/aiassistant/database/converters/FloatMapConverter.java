package com.aiassistant.database.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts between Map<String, Float> and String for storing float maps in the database.
 */
public class FloatMapConverter {
    private static final Gson gson = new Gson();
    
    /**
     * Converts a JSON string to a Map<String, Float>
     * @param value The JSON string
     * @return The float map, or null if the input is null
     */
    @TypeConverter
    public static Map<String, Float> fromString(String value) {
        if (value == null) {
            return null;
        }
        
        Type mapType = new TypeToken<Map<String, Float>>() {}.getType();
        return gson.fromJson(value, mapType);
    }
    
    /**
     * Converts a Map<String, Float> to a JSON string
     * @param map The float map
     * @return The JSON string, or null if the input is null
     */
    @TypeConverter
    public static String toString(Map<String, Float> map) {
        if (map == null) {
            return null;
        }
        
        return gson.toJson(map);
    }
}