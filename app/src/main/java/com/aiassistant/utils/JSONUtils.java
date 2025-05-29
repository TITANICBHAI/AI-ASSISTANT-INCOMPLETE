package com.aiassistant.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility class for JSON operations in the app
 */
public class JSONUtils {
    private static final String TAG = "JSONUtils";
    
    /**
     * Convert a JSON string to a Map
     * 
     * @param jsonString The JSON string to convert
     * @return Map containing the converted JSON data
     */
    public static Map<String, Object> jsonToMap(String jsonString) {
        Map<String, Object> map = new HashMap<>();
        
        if (jsonString == null || jsonString.isEmpty()) {
            return map;
        }
        
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            Iterator<String> keys = jsonObject.keys();
            
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);
                
                if (value instanceof JSONObject) {
                    map.put(key, jsonToMap(value.toString()));
                } else if (value instanceof JSONArray) {
                    map.put(key, jsonArrayToList((JSONArray) value));
                } else {
                    map.put(key, value);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error converting JSON to Map: " + e.getMessage());
        }
        
        return map;
    }
    
    /**
     * Convert a JSONArray to a List
     * 
     * @param jsonArray The JSONArray to convert
     * @return List containing the converted JSONArray data
     */
    private static List<Object> jsonArrayToList(JSONArray jsonArray) throws JSONException {
        List<Object> list = new ArrayList<>();
        
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            
            if (value instanceof JSONObject) {
                list.add(jsonToMap(value.toString()));
            } else if (value instanceof JSONArray) {
                list.add(jsonArrayToList((JSONArray) value));
            } else {
                list.add(value);
            }
        }
        
        return list;
    }
    
    /**
     * Convert a Map to a JSON string
     * 
     * @param map The Map to convert
     * @return JSON string representation of the map
     */
    public static String mapToJson(Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();
        
        try {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> valueMap = (Map<String, Object>) value;
                    jsonObject.put(key, new JSONObject(mapToJson(valueMap)));
                } else if (value instanceof List) {
                    jsonObject.put(key, listToJsonArray((List<?>) value));
                } else {
                    jsonObject.put(key, value);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error converting Map to JSON: " + e.getMessage());
        }
        
        return jsonObject.toString();
    }
    
    /**
     * Convert a List to a JSONArray
     * 
     * @param list The List to convert
     * @return JSONArray representation of the list
     */
    private static JSONArray listToJsonArray(List<?> list) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        
        for (Object value : list) {
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                jsonArray.put(new JSONObject(mapToJson(map)));
            } else if (value instanceof List) {
                jsonArray.put(listToJsonArray((List<?>) value));
            } else {
                jsonArray.put(value);
            }
        }
        
        return jsonArray;
    }
    
    /**
     * Safely get a string from a JSONObject
     * 
     * @param jsonObject The JSONObject to get the value from
     * @param key The key for the value
     * @param defaultValue The default value to return if key not found
     * @return The string value or default
     */
    public static String getString(JSONObject jsonObject, String key, String defaultValue) {
        try {
            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                return jsonObject.getString(key);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting string from JSON: " + e.getMessage());
        }
        return defaultValue;
    }
    
    /**
     * Safely get an int from a JSONObject
     * 
     * @param jsonObject The JSONObject to get the value from
     * @param key The key for the value
     * @param defaultValue The default value to return if key not found
     * @return The int value or default
     */
    public static int getInt(JSONObject jsonObject, String key, int defaultValue) {
        try {
            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                return jsonObject.getInt(key);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting int from JSON: " + e.getMessage());
        }
        return defaultValue;
    }
    
    /**
     * Safely get a long from a JSONObject
     * 
     * @param jsonObject The JSONObject to get the value from
     * @param key The key for the value
     * @param defaultValue The default value to return if key not found
     * @return The long value or default
     */
    public static long getLong(JSONObject jsonObject, String key, long defaultValue) {
        try {
            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                return jsonObject.getLong(key);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting long from JSON: " + e.getMessage());
        }
        return defaultValue;
    }
    
    /**
     * Safely get a double from a JSONObject
     * 
     * @param jsonObject The JSONObject to get the value from
     * @param key The key for the value
     * @param defaultValue The default value to return if key not found
     * @return The double value or default
     */
    public static double getDouble(JSONObject jsonObject, String key, double defaultValue) {
        try {
            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                return jsonObject.getDouble(key);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting double from JSON: " + e.getMessage());
        }
        return defaultValue;
    }
    
    /**
     * Safely get a boolean from a JSONObject
     * 
     * @param jsonObject The JSONObject to get the value from
     * @param key The key for the value
     * @param defaultValue The default value to return if key not found
     * @return The boolean value or default
     */
    public static boolean getBoolean(JSONObject jsonObject, String key, boolean defaultValue) {
        try {
            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                return jsonObject.getBoolean(key);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error getting boolean from JSON: " + e.getMessage());
        }
        return defaultValue;
    }
    
    /**
     * Merge two JSONObjects
     * 
     * @param source The source JSONObject
     * @param target The target JSONObject to merge into
     * @return The merged JSONObject
     */
    public static JSONObject mergeJSONObjects(JSONObject source, JSONObject target) {
        if (source == null) {
            return target;
        }
        if (target == null) {
            return source;
        }
        
        JSONObject merged = new JSONObject();
        try {
            // Copy all fields from target
            Iterator<String> targetKeys = target.keys();
            while (targetKeys.hasNext()) {
                String key = targetKeys.next();
                merged.put(key, target.get(key));
            }
            
            // Add or override with fields from source
            Iterator<String> sourceKeys = source.keys();
            while (sourceKeys.hasNext()) {
                String key = sourceKeys.next();
                merged.put(key, source.get(key));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error merging JSON objects: " + e.getMessage());
        }
        
        return merged;
    }
}
