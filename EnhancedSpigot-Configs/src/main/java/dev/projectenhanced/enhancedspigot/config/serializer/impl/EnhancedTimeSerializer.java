package dev.projectenhanced.enhancedspigot.config.serializer.impl;

import dev.projectenhanced.enhancedspigot.config.EnhancedConfig;
import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;
import dev.projectenhanced.enhancedspigot.util.time.EnhancedTime;

public class EnhancedTimeSerializer implements ISerializer<EnhancedTime> {
    @Override
    public Object serialize(EnhancedTime object, Class<? extends EnhancedTime> objectClass, EnhancedConfig config) {
        return object.toString();
    }

    @Override
    public void serializeTo(EnhancedTime object, Class<? extends EnhancedTime> objectClass, EnhancedConfig config, Object to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnhancedTime deserialize(Object serialized, Class<? extends EnhancedTime> targetClass, EnhancedConfig config) {
        return new EnhancedTime(String.valueOf(serialized));
    }

    @Override
    public void deserializeTo(Object serialized, Class<? extends EnhancedTime> targetClass, EnhancedConfig config, Object to) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean convertToSection() {
        return false;
    }
}
