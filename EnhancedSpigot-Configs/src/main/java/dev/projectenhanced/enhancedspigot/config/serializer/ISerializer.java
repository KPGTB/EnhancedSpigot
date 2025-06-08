package dev.projectenhanced.enhancedspigot.config.serializer;

import org.bukkit.plugin.java.JavaPlugin;

public interface ISerializer<T> {
    Object serialize(T object, JavaPlugin plugin);
    T deserialize(Object configValue, JavaPlugin plugin);
    boolean convertToSection();
}
