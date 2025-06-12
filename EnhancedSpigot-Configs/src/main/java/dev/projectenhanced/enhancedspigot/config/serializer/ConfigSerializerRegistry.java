package dev.projectenhanced.enhancedspigot.config.serializer;

import dev.projectenhanced.enhancedspigot.config.serializer.impl.*;
import dev.projectenhanced.enhancedspigot.util.time.EnhancedTime;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of serializers used in EnhancedConfig
 */
public class ConfigSerializerRegistry {
    private static final class Holder {
        static final ConfigSerializerRegistry INSTANCE = new ConfigSerializerRegistry();
    }

    private final Map<Class<?>, ISerializer<?>> serializers;

    protected ConfigSerializerRegistry() {
        this.serializers = new HashMap<>();

        this.registerSerializer(new WorldSerializer(), World.class);
        this.registerSerializer(new LocationSerializer(), Location.class);
        this.registerSerializer(new ItemStackSerializer(), ItemStack.class);
        this.registerSerializer(new EnhancedTimeSerializer(), EnhancedTime.class);
        this.registerSerializer(new EnumSerializer(), Enum.class);
    }

    public static ConfigSerializerRegistry getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Registers serializer
     * @param serializer Serializer instance
     * @param classes Classes for which is that serializer
     * @param <T>
     */
    @SafeVarargs
    public final <T> void registerSerializer(ISerializer<T> serializer, Class<? extends T>... classes) {
        for (Class<? extends T> clazz : classes) {
            this.serializers.put(clazz,serializer);
        }
    }

    /**
     * Gets registered serializer
     * @param clazz Class for which is that serializer
     * @return Serializer or null
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public <T> ISerializer<T> getSerializer(Class<? extends T> clazz) {
        ISerializer<T> serializer = (ISerializer<T>) this.serializers.get(clazz);
        if(serializer != null) return serializer;
        return this.serializers.entrySet()
                .stream()
                .filter(entry -> Enum.class.isAssignableFrom(entry.getKey()))
                .map(Map.Entry::getValue)
                .map(s -> (ISerializer<T>) s)
                .findAny().orElse(null);
    }

    public static class CustomSerializers {
        /**
         * Base serializer used for serializing simple objects (requires empty constructor)
         */
        public static final ISerializer<Object> BASE = new BaseSerializer();
        /**
         * Serializer for exact item stack copies - uses Base64 to serialize ItemStack
         */
        public static final ISerializer<ItemStack> EXACT_ITEMSTACK = new ExactItemStackSerializer();
    }
}
