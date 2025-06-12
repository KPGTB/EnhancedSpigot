package dev.projectenhanced.enhancedspigot.data.cache.iface;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ICache<K,V> {
    V get(K key);

    Set<K> keySet();
    Collection<V> values();
    Set<Map.Entry<K,V>> entrySet();

    void set(K key, V value);

    void invalidate(K key);
    void invalidateAll();

    boolean contains(K key);
}
