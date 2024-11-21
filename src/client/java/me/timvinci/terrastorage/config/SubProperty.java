package me.timvinci.terrastorage.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation used by the client config, specifies which config properties are sub properties, and shouldn't be added
 * to the options screen as buttons.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SubProperty {
}
