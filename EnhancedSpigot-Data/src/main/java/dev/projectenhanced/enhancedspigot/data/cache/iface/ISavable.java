package dev.projectenhanced.enhancedspigot.data.cache.iface;

import java.util.Set;
import java.util.function.Consumer;

public interface ISavable<K,V> {
    V load(K key);
    Set<V> loadAll();

    void modify(K key, Consumer<V> action);
    void modifyMultiple(Set<K> keys, Consumer<V> action);
    void modifyAll(Consumer<V> action);

    void save(K key);
    void saveAll();

    void create(K key, V value);

    void remove(K key);
    void removeAll();

    boolean exists(K key);
}
