package com.aiassistant.database.converters;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Type converter for Date objects in Room database.
 */
public class DateConverter {
    
    /**
     * Convert from timestamp to Date
     * @param value The timestamp value
     * @return The Date object
     */
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    
    /**
     * Convert from Date to timestamp
     * @param date The Date object
     * @return The timestamp value
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}