package com.aiassistant.database.converters;

import android.graphics.Rect;
import androidx.room.TypeConverter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Type converter for Rect objects to be stored in Room database.
 * Converts between Rect objects and String values (JSON).
 */
public class RectTypeConverter {
    /**
     * Convert a JSON string to Rect
     * @param value JSON string representation of Rect
     * @return Rect object or null if value is null or invalid
     */
    @TypeConverter
    public static Rect fromString(String value) {
        if (value == null) {
            return null;
        }
        
        try {
            JSONObject json = new JSONObject(value);
            Rect rect = new Rect();
            rect.left = json.getInt("left");
            rect.top = json.getInt("top");
            rect.right = json.getInt("right");
            rect.bottom = json.getInt("bottom");
            return rect;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Convert a Rect to JSON string
     * @param rect Rect object
     * @return JSON string or null if rect is null
     */
    @TypeConverter
    public static String rectToString(Rect rect) {
        if (rect == null) {
            return null;
        }
        
        try {
            JSONObject json = new JSONObject();
            json.put("left", rect.left);
            json.put("top", rect.top);
            json.put("right", rect.right);
            json.put("bottom", rect.bottom);
            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}