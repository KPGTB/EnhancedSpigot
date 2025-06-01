package dev.projectenhanced.enhancedspigot.data.cache;

import dev.projectenhanced.enhancedspigot.data.DatabaseController;
import dev.projectenhanced.enhancedspigot.data.cache.iface.IAsyncCache;
import dev.projectenhanced.enhancedspigot.data.cache.iface.IAsyncSaveable;
import dev.projectenhanced.enhancedspigot.util.SchedulerUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.function.Consumer;

public class AsyncDataCache<K,V> extends DataCache<K,V> implements IAsyncCache<K,V>, IAsyncSaveable<K,V> {
    private final JavaPlugin plugin;

    public AsyncDataCache(DatabaseController controller, JavaPlugin plugin) {
        super(controller);
        this.plugin = plugin;
    }

    @Override
    public void getAsync(K key, Consumer<V> then) {
        this.runAsync(() -> then.accept(this.get(key)));
    }

    @Override
    public void loadAsync(K key, Consumer<V> then) {
        this.runAsync(() -> then.accept(this.load(key)));
    }

    @Override
    public void loadAsyncAll(Consumer<Set<V>> then) {
        this.runAsync(() -> then.accept(this.loadAll()));
    }

    @Override
    public void modifyAsync(K key, Consumer<V> action) {
        this.runAsync(() -> this.modify(key,action));
    }

    @Override
    public void modifyAsyncAll(Consumer<V> action) {
        this.runAsync(() -> this.modifyAll(action));
    }

    @Override
    public void saveAsync(K key) {
        this.runAsync(() -> this.save(key));
    }

    @Override
    public void saveAsyncAll() {
        this.runAsync(this::saveAll);
    }

    @Override
    public void removeAsync(K key) {
        this.runAsync(() -> this.remove(key));
    }

    @Override
    public void existsAsync(K key, Consumer<Boolean> then) {
        this.runAsync(() -> then.accept(this.exists(key)));
    }

    private void runAsync(Runnable runnable) {
        if(DisableLock.IS_LOCKED) {
            runnable.run();
            return;
        }
        SchedulerUtil.runTaskAsynchronously(this.plugin, runnable);
    }
}
