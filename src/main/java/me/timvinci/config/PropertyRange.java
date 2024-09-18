package me.timvinci.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation used by the config classes to specify the range of integer properties.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyRange {
    int min();
    int max();
}
