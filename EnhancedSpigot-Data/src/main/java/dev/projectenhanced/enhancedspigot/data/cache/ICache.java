package dev.projectenhanced.enhancedspigot.data.cache;

public interface ICache<K,V> {
    V get(K key);
    void set(K key, V value);

    void invalidate(K key);
    void invalidateAll();

    boolean contains(K key);
}
