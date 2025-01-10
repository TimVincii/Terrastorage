package me.timvinci.terrastorage.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation specifying which properties should only be included in the server configuration file when running
 * in a server environment.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ServerExclusive { }