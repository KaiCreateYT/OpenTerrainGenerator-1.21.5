package com.pg85.otg.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@Setting
public @interface ColorSetting {
    String DEFAULT_VALUE = "0xFFFFFF"; // Default color is white
    String value() default DEFAULT_VALUE;
}
