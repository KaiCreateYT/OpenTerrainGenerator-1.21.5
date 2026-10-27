package com.pg85.otg.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Setting
public @interface DoubleSetting {
    double DEFAULT_VALUE = 0.0; // Default value is 0.0
    double DEFAULT_MIN = Double.MIN_VALUE; // Default minimum value
    double DEFAULT_MAX = Double.MAX_VALUE; // Default maximum value
    double value() default DEFAULT_VALUE;
    double min() default DEFAULT_MIN;
    double max() default DEFAULT_MAX;
}
