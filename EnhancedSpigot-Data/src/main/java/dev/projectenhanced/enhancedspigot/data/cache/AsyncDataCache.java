/*
 * Copyright 2025 KPG-TB
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.projectenhanced.enhancedspigot.data.cache;

import dev.projectenhanced.enhancedspigot.data.DatabaseController;
import dev.projectenhanced.enhancedspigot.data.cache.iface.IAsyncSavableCache;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AsyncDataCache<K, V> extends DataCache<K, V> implements IAsyncSavableCache<K, V> {
	private final JavaPlugin plugin;
	private ExecutorService executor;

	/**
	 * Automated constructor
	 * Works only when extending it or using anonymous class
	 *
	 * @param controller DatabaseController instance
	 * @param plugin     Plugin instance
	 */
	public AsyncDataCache(DatabaseController controller, JavaPlugin plugin) {
		super(controller, plugin);
		this.plugin = plugin;
		this.executor = controller.getExecutor();
	}

	public AsyncDataCache(DatabaseController controller, Class<K> keyClass, Class<V> valueClass, JavaPlugin plugin) {
		super(controller, plugin, keyClass, valueClass);
		this.plugin = plugin;
		this.executor = controller.getExecutor();
	}

	@Override
	public CompletableFuture<V> getAsync(K key) {
		return this.supplyAsync(() -> this.get(key));
	}

	@Override
	public CompletableFuture<V> loadAsync(K key) {
		return this.supplyAsync(() -> this.load(key));
	}

	@Override
	public CompletableFuture<Set<V>> loadAsyncAll() {
		return this.supplyAsync(this::loadAll);
	}

	@Override
	public CompletableFuture<Set<V>> loadAsyncAll(boolean ignoreCached) {
		return this.supplyAsync(() -> this.loadAll(ignoreCached));
	}

	@Override
	public CompletableFuture<Void> modifyAsync(K key, Consumer<V> action) {
		return this.runAsync(() -> this.modify(key, action));
	}

	@Override
	public CompletableFuture<Void> modifyAsyncMultiple(Set<K> keys, Consumer<V> action) {
		return this.runAsync(() -> this.modifyMultiple(keys, action));
	}

	@Override
	public CompletableFuture<Void> modifyAsyncAll(Consumer<V> action) {
		return this.runAsync(() -> this.modifyAll(action));
	}

	@Override
	public CompletableFuture<Void> saveAsync(K key) {
		return this.runAsync(() -> this.save(key));
	}

	@Override
	public CompletableFuture<Void> saveAsyncAll() {
		return this.runAsync(this::saveAll);
	}

	@Override
	public CompletableFuture<Void> createAsync(K key, V value) {
		return this.runAsync(() -> this.create(key, value));
	}

	@Override
	public CompletableFuture<Void> removeAsync(K key) {
		return this.runAsync(() -> this.remove(key));
	}

	@Override
	public CompletableFuture<Void> removeAsyncAll() {
		return this.runAsync(this::removeAll);
	}

	@Override
	public CompletableFuture<Boolean> existsAsync(K key) {
		return this.supplyAsync(() -> this.exists(key));
	}

	private <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
		if (DisableLock.IS_LOCKED) {
			supplier.get();
			return null;
		}
		return CompletableFuture.supplyAsync(supplier, this.executor);
	}

	private CompletableFuture<Void> runAsync(Runnable runnable) {
		if (DisableLock.IS_LOCKED) {
			runnable.run();
			return null;
		}
		return CompletableFuture.runAsync(runnable, this.executor);
	}

	@Override
	public ExecutorService getExecutor() {
		return this.executor;
	}

	@Override
	public void setExecutor(ExecutorService executorService) {
		this.executor = executorService;
	}
}
