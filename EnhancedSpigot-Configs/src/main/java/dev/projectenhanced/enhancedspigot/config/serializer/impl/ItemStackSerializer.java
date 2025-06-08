package dev.projectenhanced.enhancedspigot.config.serializer.impl;

import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;
import dev.projectenhanced.enhancedspigot.config.util.SectionUtil;
import dev.projectenhanced.enhancedspigot.util.item.EnhancedItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public class ItemStackSerializer implements ISerializer<ItemStack> {
    @Override
    public Object serialize(ItemStack object, JavaPlugin plugin) {
        Map<String, Object> serializedItem = EnhancedItemBuilder.Serializer.serialize(object);
        return SectionUtil.create(serializedItem);
    }

    @Override
    public ItemStack deserialize(Object configValue, JavaPlugin plugin) {
        ConfigurationSection section = (ConfigurationSection) configValue;
        Map<String,Object> serializedItem = section.getValues(false);
        return EnhancedItemBuilder.Serializer.deserialize(serializedItem);
    }

    @Override
    public boolean convertToSection() {
        return true;
    }
}
