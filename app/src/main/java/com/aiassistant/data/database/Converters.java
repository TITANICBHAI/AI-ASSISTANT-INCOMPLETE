package com.aiassistant.data.database;

import androidx.room.TypeConverter;

import com.aiassistant.data.models.TouchPath;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * Type converters for Room database
 */
public class Converters {
    
    private static final Gson gson = new Gson();
    
    /**
     * Convert Date to timestamp
     * 
     * @param date The date
     * @return The timestamp
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
    
    /**
     * Convert timestamp to Date
     * 
     * @param timestamp The timestamp
     * @return The date
     */
    @TypeConverter
    public static Date timestampToDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
    
    /**
     * Convert float array to string
     * 
     * @param array The float array
     * @return The string representation
     */
    @TypeConverter
    public static String floatArrayToString(float[] array) {
        return array == null ? null : gson.toJson(array);
    }
    
    /**
     * Convert string to float array
     * 
     * @param value The string representation
     * @return The float array
     */
    @TypeConverter
    public static float[] stringToFloatArray(String value) {
        if (value == null) {
            return null;
        }
        Type type = new TypeToken<float[]>() {}.getType();
        return gson.fromJson(value, type);
    }
    
    /**
     * Convert touch path list to string
     * 
     * @param paths The touch path list
     * @return The string representation
     */
    @TypeConverter
    public static String touchPathListToString(List<TouchPath> paths) {
        return paths == null ? null : gson.toJson(paths);
    }
    
    /**
     * Convert string to touch path list
     * 
     * @param value The string representation
     * @return The touch path list
     */
    @TypeConverter
    public static List<TouchPath> stringToTouchPathList(String value) {
        if (value == null) {
            return null;
        }
        Type type = new TypeToken<List<TouchPath>>() {}.getType();
        return gson.fromJson(value, type);
    }
}
