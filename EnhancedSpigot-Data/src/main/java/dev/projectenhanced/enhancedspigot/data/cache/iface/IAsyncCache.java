package dev.projectenhanced.enhancedspigot.data.cache.iface;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public interface IAsyncCache<K,V> extends ICache<K,V> {
    CompletableFuture<V> getAsync(K key);

    ExecutorService getExecutor();
    void setExecutor(ExecutorService executorService);
}
