package com.aiassistant.data.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Type converter for Map objects in Room database.
 */
public class MapConverter {
    private static final Gson gson = new Gson();
    
    /**
     * Convert a JSON string to a Map of String to String
     */
    @TypeConverter
    public static Map<String, String> fromString(String value) {
        if (value == null) {
            return new HashMap<>();
        }
        
        Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        return gson.fromJson(value, mapType);
    }
    
    /**
     * Convert a Map of String to String to a JSON string
     */
    @TypeConverter
    public static String fromMap(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        
        return gson.toJson(map);
    }
}