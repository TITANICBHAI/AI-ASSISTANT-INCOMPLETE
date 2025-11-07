package com.aiassistant.data.converters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import androidx.room.TypeConverter;

import com.aiassistant.data.models.TouchPath;
import com.aiassistant.data.models.UIElement;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive type converters for Room database
 * Consolidated from multiple converter implementations
 */
public class Converters {
    private static final Gson gson = new Gson();
    
    // ============ Date Converters ============
    
    /**
     * Convert Date to timestamp
     * @param date Date object
     * @return Timestamp in milliseconds, or null if date is null
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
    
    /**
     * Convert timestamp to Date
     * @param timestamp Timestamp in milliseconds
     * @return Date object, or null if timestamp is null
     */
    @TypeConverter
    public static Date timestampToDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
    
    // ============ List Converters ============
    
    /**
     * Convert JSON string to List of Strings
     * @param json JSON string
     * @return List of Strings, or empty list if json is null
     */
    @TypeConverter
    public static List<String> fromStringListJson(String json) {
        if (json == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(json, listType);
    }
    
    /**
     * Convert List of Strings to JSON string
     * @param list List of Strings
     * @return JSON string, or null if list is null
     */
    @TypeConverter
    public static String stringListToJson(List<String> list) {
        return list == null ? null : gson.toJson(list);
    }
    
    /**
     * Convert JSON string to List of Integers
     * @param json JSON string
     * @return List of Integers, or empty list if json is null
     */
    @TypeConverter
    public static List<Integer> fromIntListJson(String json) {
        if (json == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<Integer>>() {}.getType();
        return gson.fromJson(json, listType);
    }
    
    /**
     * Convert List of Integers to JSON string
     * @param list List of Integers
     * @return JSON string, or null if list is null
     */
    @TypeConverter
    public static String intListToJson(List<Integer> list) {
        return list == null ? null : gson.toJson(list);
    }
    
    /**
     * Convert JSON string to List of Floats
     * @param json JSON string
     * @return List of Floats, or empty list if json is null
     */
    @TypeConverter
    public static List<Float> fromFloatListJson(String json) {
        if (json == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<Float>>() {}.getType();
        return gson.fromJson(json, listType);
    }
    
    /**
     * Convert List of Floats to JSON string
     * @param list List of Floats
     * @return JSON string, or null if list is null
     */
    @TypeConverter
    public static String floatListToJson(List<Float> list) {
        return list == null ? null : gson.toJson(list);
    }
    
    /**
     * Convert JSON string to List of TouchPaths
     * @param json JSON string
     * @return List of TouchPaths, or null if json is null
     */
    @TypeConverter
    public static List<TouchPath> stringToTouchPathList(String json) {
        if (json == null) {
            return null;
        }
        Type listType = new TypeToken<List<TouchPath>>() {}.getType();
        return gson.fromJson(json, listType);
    }
    
    /**
     * Convert List of TouchPaths to JSON string
     * @param paths List of TouchPaths
     * @return JSON string, or null if paths is null
     */
    @TypeConverter
    public static String touchPathListToString(List<TouchPath> paths) {
        return paths == null ? null : gson.toJson(paths);
    }
    
    /**
     * Convert JSON string to List of UIElements
     * @param json JSON string
     * @return List of UIElements, or null if json is null
     */
    @TypeConverter
    public static List<UIElement> stringToUIElementList(String json) {
        if (json == null) {
            return null;
        }
        Type listType = new TypeToken<List<UIElement>>() {}.getType();
        return gson.fromJson(json, listType);
    }
    
    /**
     * Convert List of UIElements to JSON string
     * @param elements List of UIElements
     * @return JSON string, or null if elements is null
     */
    @TypeConverter
    public static String uiElementListToString(List<UIElement> elements) {
        return elements == null ? null : gson.toJson(elements);
    }
    
    // ============ Map Converters ============
    
    /**
     * Convert JSON string to Map of String to String
     * @param json JSON string
     * @return Map of String to String, or empty map if json is null
     */
    @TypeConverter
    public static Map<String, String> jsonToMap(String json) {
        if (json == null) {
            return new HashMap<>();
        }
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * Convert Map of String to String to JSON string
     * @param map Map of String to String
     * @return JSON string, or null if map is null
     */
    @TypeConverter
    public static String mapToJson(Map<String, String> map) {
        return map == null ? null : gson.toJson(map);
    }
    
    /**
     * Convert JSON string to Map of String to Float
     * @param json JSON string
     * @return Map of String to Float, or empty map if json is null
     */
    @TypeConverter
    public static Map<String, Float> jsonToStringFloatMap(String json) {
        if (json == null) {
            return new HashMap<>();
        }
        Type type = new TypeToken<Map<String, Float>>() {}.getType();
        return gson.fromJson(json, type);
    }
    
    /**
     * Convert Map of String to Float to JSON string
     * @param map Map of String to Float
     * @return JSON string, or null if map is null
     */
    @TypeConverter
    public static String stringFloatMapToJson(Map<String, Float> map) {
        return map == null ? null : gson.toJson(map);
    }
    
    // ============ Array Converters ============
    
    /**
     * Convert float array to JSON string
     * @param array Float array
     * @return JSON string, or null if array is null
     */
    @TypeConverter
    public static String floatArrayToJson(float[] array) {
        return array == null ? null : gson.toJson(array);
    }
    
    /**
     * Convert JSON string to float array
     * @param json JSON string
     * @return Float array, or empty array if json is null
     */
    @TypeConverter
    public static float[] jsonToFloatArray(String json) {
        if (json == null) {
            return new float[0];
        }
        return gson.fromJson(json, float[].class);
    }
    
    // ============ Bitmap Converters ============
    
    /**
     * Convert Bitmap to byte array
     * @param bitmap Bitmap object
     * @return Byte array representation of bitmap, or null if bitmap is null
     */
    @TypeConverter
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
    
    /**
     * Convert byte array to Bitmap
     * @param byteArray Byte array representation of bitmap
     * @return Bitmap object, or null if byteArray is null
     */
    @TypeConverter
    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
    
    // ============ Rect Converters ============
    
    /**
     * Convert Rect to JSON string
     * @param rect Rect object
     * @return JSON string, or null if rect is null
     */
    @TypeConverter
    public static String rectToJson(Rect rect) {
        return rect == null ? null : gson.toJson(rect);
    }
    
    /**
     * Convert JSON string to Rect
     * @param json JSON string
     * @return Rect object, or null if json is null
     */
    @TypeConverter
    public static Rect jsonToRect(String json) {
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, Rect.class);
    }
}
