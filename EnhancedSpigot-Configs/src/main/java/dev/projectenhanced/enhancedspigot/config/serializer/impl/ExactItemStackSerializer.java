package dev.projectenhanced.enhancedspigot.config.serializer.impl;

import dev.projectenhanced.enhancedspigot.config.EnhancedConfig;
import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;
import dev.projectenhanced.enhancedspigot.util.item.EnhancedItemBuilder;
import dev.projectenhanced.enhancedspigot.util.trycatch.TryCatchUtil;
import org.bukkit.inventory.ItemStack;

public class ExactItemStackSerializer implements ISerializer<ItemStack> {
    @Override
    public Object serialize(ItemStack object, Class<? extends ItemStack> objectClass, EnhancedConfig config) {
        return TryCatchUtil.tryAndReturn(() -> EnhancedItemBuilder.Serializer.serializeToBase64(object));
    }

    @Override
    public void serializeTo(ItemStack object, Class<? extends ItemStack> objectClass, EnhancedConfig config, Object to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack deserialize(Object serialized, Class<? extends ItemStack> targetClass, EnhancedConfig config) {
        return TryCatchUtil.tryAndReturn(() -> EnhancedItemBuilder.Serializer.deserializeFromBase64(String.valueOf(serialized)));
    }

    @Override
    public void deserializeTo(Object serialized, Class<? extends ItemStack> targetClass, EnhancedConfig config, Object to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean convertToSection() {
        return false;
    }
}
