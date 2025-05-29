package com.aiassistant.data.converters;

import androidx.room.TypeConverter;

import com.aiassistant.data.models.TouchPath;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Converter for lists of TouchPath objects
 */
public class TouchPathListConverter {
    
    private static final Gson gson = new Gson();
    
    /**
     * Convert a list of touch paths to a JSON string
     * 
     * @param value The touch paths
     * @return The JSON string
     */
    @TypeConverter
    public static String fromList(List<TouchPath> value) {
        if (value == null) {
            return null;
        }
        
        Type listType = new TypeToken<List<TouchPath>>() {}.getType();
        return gson.toJson(value, listType);
    }
    
    /**
     * Convert a JSON string to a list of touch paths
     * 
     * @param value The JSON string
     * @return The touch paths
     */
    @TypeConverter
    public static List<TouchPath> toList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<List<TouchPath>>() {}.getType();
        return gson.fromJson(value, listType);
    }
}
