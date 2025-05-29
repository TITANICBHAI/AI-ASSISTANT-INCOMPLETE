package com.aiassistant.database.converters;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Type converter for Date objects to be stored in Room database.
 * Converts between Date objects and Long values (timestamp).
 */
public class DateTypeConverter {
    /**
     * Convert a timestamp to Date
     * @param value Timestamp in milliseconds
     * @return Date object or null if value is null
     */
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }
    
    /**
     * Convert a Date to timestamp
     * @param date Date object
     * @return Timestamp in milliseconds or null if date is null
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}