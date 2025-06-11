package dev.projectenhanced.enhancedspigot.config.serializer.impl;

import dev.projectenhanced.enhancedspigot.config.EnhancedConfig;
import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldSerializer implements ISerializer<World> {

    @Override
    public Object serialize(World object, Class<? extends World> objectClass, EnhancedConfig config) {
        return object.getName();
    }

    @Override
    public void serializeTo(World object, Class<? extends World> objectClass, EnhancedConfig config, Object to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public World deserialize(Object serialized, Class<? extends World> targetClass, EnhancedConfig config) {
        return Bukkit.getWorld(String.valueOf(serialized));
    }

    @Override
    public void deserializeTo(Object serialized, Class<? extends World> targetClass, EnhancedConfig config, Object to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean convertToSection() {
        return false;
    }
}
