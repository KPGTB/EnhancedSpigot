package dev.projectenhanced.enhancedspigot.config.serializer.impl;

import dev.projectenhanced.enhancedspigot.config.EnhancedConfig;
import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;
import dev.projectenhanced.enhancedspigot.config.util.SectionUtil;
import dev.projectenhanced.enhancedspigot.util.item.EnhancedItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializer implements ISerializer<ItemStack> {

    @Override
    public Object serialize(ItemStack object, Class<? extends ItemStack> objectClass, EnhancedConfig config) {
        ConfigurationSection section = SectionUtil.createEmpty();
        this.serializeTo(object,objectClass,config,section);
        return section;
    }

    @Override
    public void serializeTo(ItemStack object, Class<? extends ItemStack> objectClass, EnhancedConfig config, Object to) {
        ConfigurationSection section = (ConfigurationSection) to;
        SectionUtil.fillSection(section, EnhancedItemBuilder.Serializer.serialize(object));
    }

    @Override
    public ItemStack deserialize(Object serialized, Class<? extends ItemStack> targetClass, EnhancedConfig config) {
        ConfigurationSection section = (ConfigurationSection) serialized;
        return EnhancedItemBuilder.Serializer.deserialize(section.getValues(false));
    }

    @Override
    public void deserializeTo(Object serialized, Class<? extends ItemStack> targetClass, EnhancedConfig config, Object to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean convertToSection() {
        return true;
    }
}
