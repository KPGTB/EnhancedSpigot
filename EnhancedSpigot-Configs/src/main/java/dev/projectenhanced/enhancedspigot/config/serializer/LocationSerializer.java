package dev.projectenhanced.enhancedspigot.config.serializer;

import dev.projectenhanced.enhancedspigot.config.util.SectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class LocationSerializer implements ISerializer<Location> {
    @Override
    public Object serialize(Location object, JavaPlugin plugin) {
        ConfigurationSection section = SectionUtil.createEmpty();
        section.set("world", object.getWorld().getName());
        section.set("x", object.getX());
        section.set("y", object.getY());
        section.set("z", object.getZ());
        section.set("yaw", object.getYaw());
        section.set("pitch", object.getPitch());
        return section;
    }

    @Override
    public Location deserialize(Object configValue, JavaPlugin plugin) {
        ConfigurationSection section = (ConfigurationSection) configValue;
        return new Location(
                Bukkit.getWorld(section.getString("world")),
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw"),
                (float) section.getDouble("pitch")
        );
    }

    @Override
    public boolean convertToSection() {
        return true;
    }
}
