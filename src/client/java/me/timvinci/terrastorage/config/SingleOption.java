package me.timvinci.terrastorage.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation used by the client config, specifies which config properties should be made into single (big) option
 * buttons in the option screen.
 * options screen.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleOption { }
