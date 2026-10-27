package com.pg85.otg.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Setting
public @interface LongSetting {
    long DEFAULT_VALUE = 0L; // Default value for long settings
    long DEFAULT_MIN = Long.MIN_VALUE; // Default minimum value for long settings
    long DEFAULT_MAX = Long.MAX_VALUE; // Default maximum value for long settings
    long value() default DEFAULT_VALUE;
    long min() default DEFAULT_MIN;
    long max() default DEFAULT_MAX;
}
