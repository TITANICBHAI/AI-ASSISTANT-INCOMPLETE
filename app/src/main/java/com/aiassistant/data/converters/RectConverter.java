package com.aiassistant.data.converters;

import android.graphics.Rect;
import androidx.room.TypeConverter;

import com.google.gson.Gson;

/**
 * Type converter for Room database to convert between Rect and String
 */
public class RectConverter {
    private static final Gson gson = new Gson();
    
    /**
     * Convert Rect to JSON String
     * @param rect Rect to convert
     * @return JSON string representation
     */
    @TypeConverter
    public static String fromRect(Rect rect) {
        if (rect == null) {
            return null;
        }
        return gson.toJson(rect);
    }
    
    /**
     * Convert JSON String to Rect
     * @param json JSON string to convert
     * @return Rect object
     */
    @TypeConverter
    public static Rect toRect(String json) {
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, Rect.class);
    }
}