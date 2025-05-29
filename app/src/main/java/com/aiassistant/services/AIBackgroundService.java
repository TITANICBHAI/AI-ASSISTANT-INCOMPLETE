package com.aiassistant.services;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @deprecated This is a transition class to avoid duplicate definitions.
 * This class will be removed in a future version.
 * Use BackgroundMonitoringService instead.
 */
@Deprecated
public class AIBackgroundService {
    
    /**
     * Check if service is running
     * @return True if running
     */
    public static boolean isServiceRunning() {
        // This is a placeholder method
        return false;
    }
    
    /**
     * The original functionality has been migrated to another class.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @interface MigratedClass {
        String value() default "";
    }
}
