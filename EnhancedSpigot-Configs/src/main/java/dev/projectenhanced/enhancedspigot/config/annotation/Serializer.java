package dev.projectenhanced.enhancedspigot.config.annotation;

import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to use custom serializer for a field
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Serializer {
    Class<? extends ISerializer<?>> value();
}
