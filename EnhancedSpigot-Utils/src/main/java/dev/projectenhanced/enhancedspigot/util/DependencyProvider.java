package dev.projectenhanced.enhancedspigot.util;

import java.util.HashMap;
import java.util.Map;

public class DependencyProvider {
    private final Map<Class<?>, Object> dependencies;

    public DependencyProvider() {
        this.dependencies = new HashMap<>();
    }

    public DependencyProvider register(Object dependency, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            this.dependencies.put(clazz, dependency);
        }
        return this;
    }

    public DependencyProvider register(Object dependency) {
        return this.register(dependency, dependency.getClass());
    }

    public boolean isRegistered(Class<?> clazz) {
        return this.dependencies.containsKey(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T provide(Class<T> clazz) {
        return (T) this.dependencies.get(clazz);
    }
}
