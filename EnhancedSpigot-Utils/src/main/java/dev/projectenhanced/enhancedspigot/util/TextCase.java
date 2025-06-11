package dev.projectenhanced.enhancedspigot.util;

public class TextCase {
    public static String camelToKebabCase(String str) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1-$2";
        str = str.replaceAll(regex, replacement).toLowerCase();
        return str;
    }

    public static String camelToSneakCase(String str) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        str = str.replaceAll(regex, replacement).toLowerCase();
        return str;
    }
}
