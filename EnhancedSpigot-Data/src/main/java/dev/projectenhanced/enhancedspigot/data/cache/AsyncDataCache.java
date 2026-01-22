/*
 * Copyright 2026 KPG-TB
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
import dev.projectenhanced.enhancedspigot.data.cache.iface.ICached;
import dev.projectenhanced.enhancedspigot.data.cache.iface.IForeignMapping;
import dev.projectenhanced.enhancedspigot.data.cache.iface.ISavableLifecycle;
import dev.projectenhanced.enhancedspigot.data.util.AsyncPriorityMap;
import dev.projectenhanced.enhancedspigot.data.util.PriorityCompletableUtil;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class AsyncDataCache<K, V extends ICached<K>> extends DataCache<K, V> implements IAsyncSavableCache<K, V> {
	@Getter private final AsyncPriorityMap asyncPriorityMap;

	@Getter
	@Setter
	private ExecutorService readExecutor;
	@Getter
	@Setter
	private ExecutorService writeExecutor;

	/**
	 * Automated constructor
	 * Works only when extending it or using anonymous class
	 *
	 * @param controller DatabaseController instance
	 * @param plugin     Plugin instance
	 */
	public AsyncDataCache(DatabaseController controller, JavaPlugin plugin) {
		this(controller, plugin, new AsyncPriorityMap(0));
	}

	public AsyncDataCache(DatabaseController controller, JavaPlugin plugin, AsyncPriorityMap asyncPriorityMap) {
		super(controller, plugin);
		this.readExecutor = controller.getReadExecutor();
		this.writeExecutor = controller.getWriteExecutor();
		this.asyncPriorityMap = asyncPriorityMap;
	}

	public AsyncDataCache(DatabaseController controller, Class<K> keyClass, Class<V> valueClass, JavaPlugin plugin) {
		this(controller, keyClass, valueClass, plugin, new AsyncPriorityMap(0));
	}

	public AsyncDataCache(DatabaseController controller, Class<K> keyClass, Class<V> valueClass, JavaPlugin plugin, AsyncPriorityMap asyncPriorityMap) {
		super(controller, plugin, keyClass, valueClass);
		this.readExecutor = controller.getReadExecutor();
		this.writeExecutor = controller.getWriteExecutor();
		this.asyncPriorityMap = asyncPriorityMap;
	}

	@Override
	public CompletableFuture<V> getAsync(K key) {
		return this.supplyAsync(() -> this.get(key), this.asyncPriorityMap.getGetPriority(), System.currentTimeMillis(), this.readExecutor);
	}

	@Override
	public CompletableFuture<V> getAsyncOrNull(K key) {
		return this.supplyAsync(() -> this.getOrNull(key), this.asyncPriorityMap.getGetPriority(), System.currentTimeMillis(), this.readExecutor);
	}

	@Override
	public CompletableFuture<V> loadAsync(K key) {
		return this.supplyAsync(() -> this.load(key), this.asyncPriorityMap.getLoadPriority(), System.currentTimeMillis(), this.readExecutor);
	}

	@Override
	public CompletableFuture<Collection<V>> loadAsyncAll() {
		return this.loadAsyncAll(false);
	}

	@Override
	public CompletableFuture<Collection<V>> loadAsyncAll(boolean ignoreCached) {
		long operationId = System.currentTimeMillis();
		return this.supplyAsync(() -> TryCatchUtil.tryOrDefault(this.dao::queryForAll, new ArrayList<V>()), this.asyncPriorityMap.getLoadAllPriority(), operationId, this.readExecutor)
			.thenCompose(entities -> CompletableFuture.allOf(entities.stream()
				.map(entity -> {
					if (ignoreCached && this.contains(entity.getKey())) return CompletableFuture.completedFuture(null);
					return this.runAsync(() -> this.loadValueIntoCache(entity.getKey(), entity), this.asyncPriorityMap.getLoadAllPriority(), operationId, this.readExecutor);
				})
				.toArray(CompletableFuture[]::new)))
			.thenApply(v -> this.values());
	}

	@Override
	public CompletableFuture<Void> modifyAsync(K key, Consumer<V> action) {
		return this.runAsync(() -> this.modify(key, action), this.asyncPriorityMap.getModifyPriority(), System.currentTimeMillis());
	}

	@Override
	public CompletableFuture<Void> modifyAsyncMultiple(Set<K> keys, Consumer<V> action) {
		return this.loopAsyncAll(
			value -> {
				if (!keys.contains(value.getKey())) return;
				action.accept(value);
				if (!this.contains(value.getKey())) this.saveValue(value);
			}, true
		);
	}

	@Override
	public CompletableFuture<Void> modifyAsyncAll(Consumer<V> action) {
		return this.loopAsyncAll(
			value -> {
				action.accept(value);
				if (!this.contains(value.getKey())) this.saveValue(value);
			}, true
		);
	}

	@Override
	public CompletableFuture<Void> loopAsyncAll(Consumer<V> action) {
		return this.loopAsyncAll(action, false);
	}

	public CompletableFuture<Void> loopAsyncAll(Consumer<V> action, boolean asModify) {
		long operationId = System.currentTimeMillis();
		return this.loopAsyncAll()
			.thenCompose(entities -> CompletableFuture.allOf(entities.stream()
				.map(entity -> this.runAsync(
					() -> action.accept(entity), this.asyncPriorityMap.getModifyAllPriority(), operationId, asModify ?
						this.writeExecutor :
						this.readExecutor
				))
				.toArray(CompletableFuture[]::new)));
	}

	@Override
	public CompletableFuture<Collection<V>> loopAsyncAll() {
		long operationId = System.currentTimeMillis();
		Set<V> values = new HashSet<>(this.values());

		return this.supplyAsync(() -> TryCatchUtil.tryOrDefault(this.dao::queryForAll, new ArrayList<V>()), this.asyncPriorityMap.getModifyAllPriority(), operationId, this.readExecutor)
			.thenCompose(entities -> CompletableFuture.allOf(entities.stream()
				.map(entity -> {
					if (this.contains(entity.getKey())) return CompletableFuture.completedFuture(null);
					return this.runAsync(
						() -> {
							this.loadValue(entity.getKey(), entity);
							values.add(entity);
						}, this.asyncPriorityMap.getLoadAllPriority(), operationId
					);
				})
				.toArray(CompletableFuture[]::new)))
			.thenApply(v -> values);
	}

	@Override
	public CompletableFuture<Void> saveAsync(K key) {
		return this.getAsyncOrNull(key)
			.thenCompose(value -> {
				if (value == null) return CompletableFuture.completedFuture(null);
				return this.saveAsyncValue(value);
			});
	}

	@Override
	public CompletableFuture<Void> saveAsyncValue(V value) {
		long operationId = System.currentTimeMillis();
		return this.saveToDbAsync(value, this.asyncPriorityMap.getSavePriority(), operationId);
	}

	@Override
	public CompletableFuture<Void> saveAsyncAll() {
		long operationId = System.currentTimeMillis();
		return CompletableFuture.allOf(this.values()
			.stream()
			.map(value -> this.saveToDbAsync(value, this.asyncPriorityMap.getSaveAllPriority(), operationId))
			.toArray(CompletableFuture[]::new));
	}

	protected CompletableFuture<Void> saveToDbAsync(V value, int priority, long operationId) {
		return this.runAsync(
				() -> {
					if (!this.contains(value.getKey())) return;
					if (value instanceof ISavableLifecycle) ((ISavableLifecycle) value).beforeSave(this.plugin);
				}, priority, operationId
			)
			.thenCompose(v -> {
				if (this.contains(value.getKey()) && value instanceof IForeignMapping) return this.javaToDbAsync((IForeignMapping) value, priority, operationId);
				return CompletableFuture.completedFuture(null);
			})
			.thenCompose(v -> this.runAsync(
				() -> {
					if (!this.contains(value.getKey())) return;
					TryCatchUtil.tryRun(() -> this.dao.update(value));
				}, priority, operationId
			));
	}

	@Override
	public CompletableFuture<V> createAsync(K key, V value) {
		return this.supplyAsync(() -> this.create(key, value), this.asyncPriorityMap.getCreatePriority(), System.currentTimeMillis());
	}

	@Override
	public CompletableFuture<V> createAsync(V value) {
		return this.supplyAsync(() -> this.create(value), this.asyncPriorityMap.getCreatePriority(), System.currentTimeMillis());
	}

	@Override
	public CompletableFuture<Void> removeAsync(K key) {
		return this.runAsync(() -> this.remove(key), this.asyncPriorityMap.getRemovePriority(), System.currentTimeMillis());
	}

	@Override
	public CompletableFuture<Void> removeAsyncIf(BiPredicate<K, V> predicate) {
		long operationId = System.currentTimeMillis();
		return CompletableFuture.allOf(this.entrySet()
			.stream()
			.map(entry -> {
				if (predicate.test(entry.getKey(), entry.getValue())) return this.runAsync(() -> this.remove(entry.getKey()), this.asyncPriorityMap.getRemovePriority(), operationId);
				return CompletableFuture.completedFuture(null);
			})
			.toArray(CompletableFuture[]::new));
	}

	@Override
	public CompletableFuture<Void> removeAsyncAll() {
		return this.runAsync(this::removeAll, this.asyncPriorityMap.getRemovePriority(), System.currentTimeMillis());
	}

	@Override
	public CompletableFuture<Boolean> existsAsync(K key) {
		return this.supplyAsync(() -> this.exists(key), this.asyncPriorityMap.getExistsPriority(), System.currentTimeMillis());
	}

	public CompletableFuture<Void> javaToDbAsync(IForeignMapping entity, int priority, long operationId) {
		return CompletableFuture.allOf(Stream.concat(
				entity.getForeignMapping()
					.entrySet()
					.stream()
					.map((entry) -> this.runAsync(
						() -> {
							entry.getKey()
								.removeIf(o -> !entry.getValue()
									.contains(o));
							entry.getValue()
								.forEach(obj -> {
									if (obj.getKey() != null) updateForeign(entry.getKey(), obj);
									else addToForeign(entry.getKey(), obj);
								});
						}, priority, operationId
					)), entity.getForeignMappers()
					.stream()
					.map(mapper -> this.runAsync(
						() -> {
							mapper.getForeign()
								.removeIf(o -> !mapper.getCache()
									.containsKey(this.extractKeyFromMapper(mapper, o)));
							mapper.getCache()
								.forEach((key, obj) -> {
									if (obj.getKey() != null) updateForeign(mapper.getForeign(), obj);
									else addToForeign(mapper.getForeign(), obj);
								});
						}, priority, operationId
					))
			)
			.toArray(CompletableFuture[]::new));
	}

	public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, int priority, long operationId) {
		return this.supplyAsync(supplier, priority, operationId, this.writeExecutor);
	}

	public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, int priority, long operationId, ExecutorService executor) {
		return PriorityCompletableUtil.supplyAsync(supplier, executor, priority, operationId);
	}

	public CompletableFuture<Void> runAsync(Runnable runnable, int priority, long operationId) {
		return this.runAsync(runnable, priority, operationId, this.writeExecutor);
	}

	public CompletableFuture<Void> runAsync(Runnable runnable, int priority, long operationId, ExecutorService executor) {
		return PriorityCompletableUtil.runAsync(runnable, executor, priority, operationId);
	}
}
