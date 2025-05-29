package com.aiassistant.data.converters;

import androidx.room.TypeConverter;

import com.aiassistant.core.ai.AIAction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Type converter for List objects in Room database.
 */
public class ListConverter {
    private static final Gson gson = new Gson();
    
    /**
     * Convert a JSON string to a List of Strings
     */
    @TypeConverter
    public static List<String> fromStringToStringList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }
    
    /**
     * Convert a List of Strings to a JSON string
     */
    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) {
            return null;
        }
        
        return gson.toJson(list);
    }
    
    /**
     * Convert a JSON string to a List of AIActions
     */
    @TypeConverter
    public static List<AIAction> fromStringToActionList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<List<AIAction>>() {}.getType();
        return gson.fromJson(value, listType);
    }
    
    /**
     * Convert a List of AIActions to a JSON string
     */
    @TypeConverter
    public static String fromActionList(List<AIAction> list) {
        if (list == null) {
            return null;
        }
        
        return gson.toJson(list);
    }
}