package com.aiassistant.database.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts between Map<String, String> and String for storing action parameters in the database.
 */
public class ActionParametersConverter {
    private static final Gson gson = new Gson();
    
    /**
     * Converts a JSON string to a Map<String, String>
     * @param value The JSON string
     * @return The map of parameters, or null if the input is null
     */
    @TypeConverter
    public static Map<String, String> fromString(String value) {
        if (value == null) {
            return null;
        }
        
        Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        return gson.fromJson(value, mapType);
    }
    
    /**
     * Converts a Map<String, String> to a JSON string
     * @param parameters The map of parameters
     * @return The JSON string, or null if the input is null
     */
    @TypeConverter
    public static String toString(Map<String, String> parameters) {
        if (parameters == null) {
            return null;
        }
        
        return gson.toJson(parameters);
    }
}