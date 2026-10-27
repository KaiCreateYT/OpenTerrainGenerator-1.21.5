package com.pg85.otg.config.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Setting
public @interface FloatSetting {
    float DEFAULT_VALUE = 0.0f; // Default float value is 0.0
    float DEFAULT_MIN = Float.MIN_VALUE; // Default minimum value
    float DEFAULT_MAX = Float.MAX_VALUE; // Default maximum value
    float value() default DEFAULT_VALUE;
    float min() default DEFAULT_MIN;
    float max() default DEFAULT_MAX;
}
