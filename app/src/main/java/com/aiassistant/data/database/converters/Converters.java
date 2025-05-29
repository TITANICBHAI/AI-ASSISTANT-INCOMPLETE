package com.aiassistant.data.database.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Type converters for Room database
 */
public class Converters {
    
    /**
     * Convert timestamp to date
     * 
     * @param value The timestamp
     * @return The date
     */
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    
    /**
     * Convert date to timestamp
     * 
     * @param date The date
     * @return The timestamp
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
    
    /**
     * Convert JSON to string list
     * 
     * @param json The JSON
     * @return The list
     */
    @TypeConverter
    public static List<String> fromStringListJson(String json) {
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<List<String>>() {}.getType();
        return new Gson().fromJson(json, listType);
    }
    
    /**
     * Convert string list to JSON
     * 
     * @param list The list
     * @return The JSON
     */
    @TypeConverter
    public static String stringListToJson(List<String> list) {
        if (list == null) {
            return null;
        }
        
        return new Gson().toJson(list);
    }
    
    /**
     * Convert JSON to int list
     * 
     * @param json The JSON
     * @return The list
     */
    @TypeConverter
    public static List<Integer> fromIntListJson(String json) {
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<List<Integer>>() {}.getType();
        return new Gson().fromJson(json, listType);
    }
    
    /**
     * Convert int list to JSON
     * 
     * @param list The list
     * @return The JSON
     */
    @TypeConverter
    public static String intListToJson(List<Integer> list) {
        if (list == null) {
            return null;
        }
        
        return new Gson().toJson(list);
    }
    
    /**
     * Convert JSON to float list
     * 
     * @param json The JSON
     * @return The list
     */
    @TypeConverter
    public static List<Float> fromFloatListJson(String json) {
        if (json == null) {
            return new ArrayList<>();
        }
        
        Type listType = new TypeToken<List<Float>>() {}.getType();
        return new Gson().fromJson(json, listType);
    }
    
    /**
     * Convert float list to JSON
     * 
     * @param list The list
     * @return The JSON
     */
    @TypeConverter
    public static String floatListToJson(List<Float> list) {
        if (list == null) {
            return null;
        }
        
        return new Gson().toJson(list);
    }
    
    /**
     * Convert JSON to float array
     * 
     * @param json The JSON
     * @return The array
     */
    @TypeConverter
    public static float[] fromFloatArrayJson(String json) {
        if (json == null) {
            return new float[0];
        }
        
        Type arrayType = new TypeToken<float[]>() {}.getType();
        return new Gson().fromJson(json, arrayType);
    }
    
    /**
     * Convert float array to JSON
     * 
     * @param array The array
     * @return The JSON
     */
    @TypeConverter
    public static String floatArrayToJson(float[] array) {
        if (array == null) {
            return null;
        }
        
        return new Gson().toJson(array);
    }
}
