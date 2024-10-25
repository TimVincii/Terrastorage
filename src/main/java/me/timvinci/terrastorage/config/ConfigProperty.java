package me.timvinci.terrastorage.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation used by the config classes to specify the key and comment of properties.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigProperty {
    String key();
    String comment();
}

