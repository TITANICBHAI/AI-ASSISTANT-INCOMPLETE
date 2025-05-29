package com.aiassistant.database.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Converts between String[] and String for storing string arrays in the database.
 */
public class StringArrayConverter {
    private static final Gson gson = new Gson();
    
    /**
     * Converts a JSON string to a String[]
     * @param value The JSON string
     * @return The string array, or null if the input is null
     */
    @TypeConverter
    public static String[] fromString(String value) {
        if (value == null) {
            return null;
        }
        
        Type arrayType = new TypeToken<String[]>() {}.getType();
        return gson.fromJson(value, arrayType);
    }
    
    /**
     * Converts a String[] to a JSON string
     * @param array The string array
     * @return The JSON string, or null if the input is null
     */
    @TypeConverter
    public static String toString(String[] array) {
        if (array == null) {
            return null;
        }
        
        return gson.toJson(array);
    }
}