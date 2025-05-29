package com.aiassistant.database.converters;

import androidx.room.TypeConverter;

import com.aiassistant.data.models.UIElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Room TypeConverter for List<UIElement>
 */
public class UIElementListConverter {
    private static final Gson gson = new GsonBuilder().create();
    
    /**
     * Convert a JSON string to a List of UIElements
     * 
     * @param value JSON string representation of the list
     * @return The List of UIElements
     */
    @TypeConverter
    public static List<UIElement> fromString(String value) {
        if (value == null) {
            return null;
        }
        
        Type listType = new TypeToken<List<UIElement>>() {}.getType();
        return gson.fromJson(value, listType);
    }
    
    /**
     * Convert a List of UIElements to a JSON string
     * 
     * @param list The List of UIElements to convert
     * @return JSON string representation of the list
     */
    @TypeConverter
    public static String toString(List<UIElement> list) {
        if (list == null) {
            return null;
        }
        
        return gson.toJson(list);
    }
}
