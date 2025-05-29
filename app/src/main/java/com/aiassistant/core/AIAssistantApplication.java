package com.aiassistant.core;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @deprecated This is a transition class to avoid duplicate definitions.
 * Use the canonical implementation in com.aiassistant package instead.
 */
@Deprecated
public class AIAssistantApplication {
    // This class intentionally left empty to avoid duplicate definitions
    
    /**
     * The original functionality has been migrated to another class.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @interface MigratedClass {
        String value() default "";
    }
}
