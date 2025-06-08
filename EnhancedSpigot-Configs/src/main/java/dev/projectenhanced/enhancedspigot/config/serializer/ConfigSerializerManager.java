package dev.projectenhanced.enhancedspigot.config.serializer;

import dev.projectenhanced.enhancedspigot.config.serializer.impl.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ConfigSerializerManager {
    private static ConfigSerializerManager INSTANCE;

    private final Map<Class<?>, ISerializer<?>> serializers;

    protected ConfigSerializerManager() {
        this.serializers = new HashMap<>();

        this.registerSerializer(new LocationSerializer(), Location.class);
        this.registerSerializer(new ItemStackSerializer(), ItemStack.class);
        this.registerSerializer(new WorldSerializer(), World.class);
    }

    @SafeVarargs
    public final <T> void registerSerializer(ISerializer<T> serializer, Class<? extends T>... classes) {
        for (Class<? extends T> clazz : classes) {
            this.serializers.put(clazz,serializer);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ISerializer<T> getSerializer(Class<? extends T> clazz) {
        return (ISerializer<T>) this.serializers.get(clazz);
    }

    public static ConfigSerializerManager getInstance() {
        if(INSTANCE == null) INSTANCE = new ConfigSerializerManager();
        return INSTANCE;
    }

    public static class SpecialSerializers {
        public static final ISerializer<ItemStack> EXACT_ITEMSTACK = new ExactItemStackSerializer();
        public static final ConfigSerializer CONFIG = new ConfigSerializer();
    }
}
