package dev.projectenhanced.enhancedspigot.data.cache.iface;

import java.util.Set;
import java.util.function.Consumer;

public interface ISaveable<K,V> {
    V load(K key);
    Set<V> loadAll();

    void modify(K key, Consumer<V> action);
    void modifyAll(Consumer<V> action);

    void save(K key);
    void saveAll();

    void remove(K key);

    boolean exists(K key);
}
