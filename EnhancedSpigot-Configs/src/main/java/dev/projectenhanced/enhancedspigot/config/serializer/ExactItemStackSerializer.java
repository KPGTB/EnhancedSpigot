package dev.projectenhanced.enhancedspigot.config.serializer;

import dev.projectenhanced.enhancedspigot.util.item.EnhancedItemBuilder;
import dev.projectenhanced.enhancedspigot.util.trycatch.TryCatchUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ExactItemStackSerializer implements ISerializer<ItemStack> {
    @Override
    public Object serialize(ItemStack object, JavaPlugin plugin) {
        return TryCatchUtil.tryAndReturn(() -> EnhancedItemBuilder.Serializer.serializeToBase64(object));
    }

    @Override
    public ItemStack deserialize(Object configValue, JavaPlugin plugin) {
        return TryCatchUtil.tryAndReturn(() -> EnhancedItemBuilder.Serializer.deserializeFromBase64(String.valueOf(configValue)));
    }

    @Override
    public boolean convertToSection() {
        return false;
    }
}
