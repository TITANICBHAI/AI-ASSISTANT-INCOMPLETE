package com.aiassistant.data.converters;

import androidx.room.TypeConverter;

import com.aiassistant.core.ai.AIAction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Room TypeConverter to convert between Lists of AIAction objects and String representation
 * for database persistence.
 */
public class ActionSequenceConverter {

    private static final Gson gson = new Gson();
    
    @TypeConverter
    public static List<AIAction> fromString(String value) {
        if (value == null) {
            return null;
        }
        
        Type listType = new TypeToken<List<AIAction>>() {}.getType();
        return gson.fromJson(value, listType);
    }
    
    @TypeConverter
    public static String fromActionList(List<AIAction> actions) {
        if (actions == null) {
            return null;
        }
        
        return gson.toJson(actions);
    }
}