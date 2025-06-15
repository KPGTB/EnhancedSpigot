package dev.projectenhanced.enhancedspigot.locale.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = LocaleDefaults.class)
public @interface LocaleDefault {
    String language();
    String[] def();
}
