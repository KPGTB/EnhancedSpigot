package dev.projectenhanced.enhancedspigot.data.cache.iface;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface IAsyncSavable<K,V> extends ISavable<K,V>{
    CompletableFuture<V> loadAsync(K key);
    CompletableFuture<Set<V>> loadAsyncAll();

    CompletableFuture<Void> modifyAsync(K key, Consumer<V> action);
    CompletableFuture<Void> modifyAsyncMultiple(Set<K> keys, Consumer<V> action);
    CompletableFuture<Void> modifyAsyncAll(Consumer<V> action);

    CompletableFuture<Void> saveAsync(K key);
    CompletableFuture<Void> saveAsyncAll();

    CompletableFuture<Void> createAsync(K key, V value);

    CompletableFuture<Void> removeAsync(K key);
    CompletableFuture<Void> removeAsyncAll();

    CompletableFuture<Boolean> existsAsync(K key);
}
