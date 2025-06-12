package dev.projectenhanced.enhancedspigot.config.serializer.impl;

import dev.projectenhanced.enhancedspigot.config.EnhancedConfig;
import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;

public class EnumSerializer implements ISerializer<Enum<?>> {
    @Override
    public Object serialize(Enum<?> object, Class<? extends Enum<?>> objectClass, EnhancedConfig config) {
        return object.name();
    }

    @Override
    public void serializeTo(Enum<?> object, Class<? extends Enum<?>> objectClass, EnhancedConfig config, Object to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enum<?> deserialize(Object serialized, Class<? extends Enum<?>> targetClass, EnhancedConfig config) {
        return this.getEnumFromName(targetClass, String.valueOf(serialized).toUpperCase());
    }

    @Override
    public void deserializeTo(Object serialized, Class<? extends Enum<?>> targetClass, EnhancedConfig config, Object to) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> T getEnumFromName(Class<?> clazz, String name) {
        return Enum.valueOf((Class<T>) clazz,name);
    }

    @Override
    public boolean convertToSection() {
        return false;
    }
}
