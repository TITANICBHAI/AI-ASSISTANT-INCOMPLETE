package com.aiassistant.database.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Room TypeConverter for converting between JSON strings and various object types
 */
public class JsonConverter {
    private static final Gson gson = new Gson();
    
    /**
     * Convert a JSON string to a List of objects of a specified type
     * @param value JSON string
     * @param <T> The type of objects in the list
     * @return List of objects, or null if the input is null
     */
    @TypeConverter
    public static <T> List<T> fromJsonToList(String value, Class<T> clazz) {
        if (value == null) {
            return null;
        }
        Type listType = TypeToken.getParameterized(List.class, clazz).getType();
        return gson.fromJson(value, listType);
    }
    
    /**
     * Convert a List of objects to a JSON string
     * @param list List of objects
     * @return JSON string representation, or null if the input is null
     */
    @TypeConverter
    public static <T> String fromListToJson(List<T> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }
    
    /**
     * Convert a JSON string to an object of a specified type
     * @param value JSON string
     * @param clazz Class of the object
     * @param <T> Type of the object
     * @return Object instance, or null if the input is null
     */
    @TypeConverter
    public static <T> T fromJsonToObject(String value, Class<T> clazz) {
        if (value == null) {
            return null;
        }
        return gson.fromJson(value, clazz);
    }
    
    /**
     * Convert an object to a JSON string
     * @param object Object to convert
     * @return JSON string representation, or null if the input is null
     */
    @TypeConverter
    public static String fromObjectToJson(Object object) {
        if (object == null) {
            return null;
        }
        return gson.toJson(object);
    }
    
    /**
     * General TypeConverter for Room to convert List of objects to JSON
     * @param value List of any objects
     * @return JSON string
     */
    @TypeConverter
    public static String listToJson(List<?> value) {
        return fromListToJson(value);
    }
    
    /**
     * General TypeConverter for Room to convert JSON to List of objects
     * This is a simplified version that might not work for all complex types
     * due to type erasure. For complex generic types, use the typed versions.
     * @param value JSON string
     * @return List of objects
     */
    @TypeConverter
    public static List<?> jsonToList(String value) {
        if (value == null) {
            return null;
        }
        Type type = new TypeToken<List<?>>(){}.getType();
        return gson.fromJson(value, type);
    }
}