package com.aiassistant.data.converters;

import androidx.room.TypeConverter;

import com.aiassistant.data.models.TouchPath;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Converter for TouchPath objects
 */
public class TouchPathConverter {
    
    private static final Gson gson = new Gson();
    
    /**
     * Convert a list of touch paths to a JSON string
     * 
     * @param touchPaths The touch paths
     * @return The JSON string
     */
    @TypeConverter
    public static String fromTouchPaths(List<TouchPath> touchPaths) {
        if (touchPaths == null) {
            return null;
        }
        
        Type type = new TypeToken<List<TouchPath>>() {}.getType();
        return gson.toJson(touchPaths, type);
    }
    
    /**
     * Convert a JSON string to a list of touch paths
     * 
     * @param json The JSON string
     * @return The touch paths
     */
    @TypeConverter
    public static List<TouchPath> toTouchPaths(String json) {
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<TouchPath>>() {}.getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * Convert a touch path to a JSON string
     * 
     * @param touchPath The touch path
     * @return The JSON string
     */
    @TypeConverter
    public static String fromTouchPath(TouchPath touchPath) {
        if (touchPath == null) {
            return null;
        }
        
        return gson.toJson(touchPath);
    }
    
    /**
     * Convert a JSON string to a touch path
     * 
     * @param json The JSON string
     * @return The touch path
     */
    @TypeConverter
    public static TouchPath toTouchPath(String json) {
        if (json == null) {
            return null;
        }
        
        return gson.fromJson(json, TouchPath.class);
    }
}
