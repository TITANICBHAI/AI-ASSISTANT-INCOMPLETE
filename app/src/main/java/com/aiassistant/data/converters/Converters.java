package com.aiassistant.data.converters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Type converters for Room database
 */
public class Converters {
    private static final Gson gson = new Gson();
    
    /**
     * Convert Date to timestamp
     * @param date Date
     * @return Timestamp
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
    
    /**
     * Convert timestamp to Date
     * @param timestamp Timestamp
     * @return Date
     */
    @TypeConverter
    public static Date timestampToDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }
    
    /**
     * Convert map to JSON string
     * @param map Map
     * @return JSON string
     */
    @TypeConverter
    public static String mapToJson(Map<String, String> map) {
        return map == null ? null : gson.toJson(map);
    }
    
    /**
     * Convert JSON string to map
     * @param json JSON string
     * @return Map
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
     * Convert float array to JSON string
     * @param array Float array
     * @return JSON string
     */
    @TypeConverter
    public static String floatArrayToJson(float[] array) {
        return array == null ? null : gson.toJson(array);
    }
    
    /**
     * Convert JSON string to float array
     * @param json JSON string
     * @return Float array
     */
    @TypeConverter
    public static float[] jsonToFloatArray(String json) {
        if (json == null) {
            return new float[0];
        }
        return gson.fromJson(json, float[].class);
    }
    
    /**
     * Convert bitmap to byte array
     * @param bitmap Bitmap
     * @return Byte array
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
     * Convert byte array to bitmap
     * @param byteArray Byte array
     * @return Bitmap
     */
    @TypeConverter
    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}
