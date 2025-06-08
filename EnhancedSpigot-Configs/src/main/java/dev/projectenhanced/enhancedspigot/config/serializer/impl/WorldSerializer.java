package dev.projectenhanced.enhancedspigot.config.serializer.impl;

import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldSerializer implements ISerializer<World> {
    @Override
    public Object serialize(World object, JavaPlugin plugin) {
        return object.getName();
    }

    @Override
    public World deserialize(Object configValue, JavaPlugin plugin) {
        return Bukkit.getWorld(String.valueOf(configValue));
    }

    @Override
    public boolean convertToSection() {
        return false;
    }
}
