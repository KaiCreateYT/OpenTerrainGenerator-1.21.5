package com.pg85.otg.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Setting
public @interface IntSetting {
    int DEFAULT_VALUE = 0; // Default value is 0
    int DEFAULT_MIN = Integer.MIN_VALUE; // Default minimum value
    int DEFAULT_MAX = Integer.MAX_VALUE; // Default maximum value
    int value() default DEFAULT_VALUE;
    int min() default DEFAULT_MIN;
    int max() default DEFAULT_MAX;
}
