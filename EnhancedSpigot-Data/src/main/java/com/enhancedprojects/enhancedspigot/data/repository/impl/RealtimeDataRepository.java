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

package com.enhancedprojects.enhancedspigot.data.repository.impl;

import com.enhancedprojects.enhancedspigot.data.DatabaseController;
import com.enhancedprojects.enhancedspigot.data.repository.entity.AbstractDataEntity;
import com.enhancedprojects.enhancedspigot.data.repository.entity.IDataEntityLifecycle;
import com.enhancedprojects.enhancedspigot.data.storage.IDataStorage;
import com.enhancedprojects.enhancedspigot.data.util.AsyncPriorityMap;
import com.enhancedprojects.enhancedspigot.data.util.DisableLock;
import com.enhancedprojects.enhancedspigot.util.SchedulerUtil;
import com.enhancedprojects.enhancedspigot.util.TryCatchUtil;
import com.j256.ormlite.dao.ForeignCollection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Save changes in foreign collections after a delay after reporting them to avoid large amounts of writes when saving the entire object
 * In this cache, all updates made to foreign collections should be reported using {@link #addPendingChange(Object, AbstractDataEntity, ForeignCollection)}
 * Adding or removing data from foreign collections should also be done using the ForeignCollection methods to ensure the changes are tracked
 * saving object won't save entire foreign collections, only the pending changes will be saved
 *
 * @param <K>
 * @param <V>
 */
public class RealtimeDataRepository<K, V extends AbstractDataEntity<K>> extends AsyncDataRepository<K, V> {
	private final long changesWaitingTIme;
	private final long changesMaxWaitingTime;
	@Getter private final AsyncRealtimePriorityMap asyncRealtimePriorityMap;

	private final ConcurrentMap<K, ConcurrentMap<String, PendingChange<?>>> pendingChanges;

	public RealtimeDataRepository(DatabaseController controller, IDataStorage<K, V> cache, JavaPlugin plugin, long changesWaitingTIme, long changesMaxWaitingTime, AsyncRealtimePriorityMap asyncRealtimePriorityMap) {
		super(controller, cache, plugin, asyncRealtimePriorityMap.getAsyncPriorityMap());
		this.changesWaitingTIme = changesWaitingTIme;
		this.changesMaxWaitingTime = changesMaxWaitingTime;
		this.asyncRealtimePriorityMap = asyncRealtimePriorityMap;
		this.pendingChanges = new ConcurrentHashMap<>();
	}

	public RealtimeDataRepository(DatabaseController controller, IDataStorage<K, V> cache, Class<K> keyClass, Class<V> valueClass, JavaPlugin plugin, long changesWaitingTIme, long changesMaxWaitingTime, AsyncRealtimePriorityMap asyncRealtimePriorityMap) {
		super(controller, cache, keyClass, valueClass, plugin, asyncRealtimePriorityMap.getAsyncPriorityMap());
		this.changesWaitingTIme = changesWaitingTIme;
		this.changesMaxWaitingTime = changesMaxWaitingTime;
		this.asyncRealtimePriorityMap = asyncRealtimePriorityMap;
		this.pendingChanges = new ConcurrentHashMap<>();
	}

	public <T extends AbstractDataEntity<?>> void addPendingChange(K entityKey, T foreignValue, ForeignCollection<T> foreignCollection) {
		String changeKey = String.valueOf(foreignValue.getKey());

		this.pendingChanges.putIfAbsent(entityKey, new ConcurrentHashMap<>());
		Map<String, PendingChange<?>> entityChanges = this.pendingChanges.get(entityKey);

		PendingChange<?> change;
		if (entityChanges.containsKey(changeKey)) {
			change = entityChanges.get(changeKey);
			change.task.cancel();

			if (System.currentTimeMillis() - change.firstChangeTime >= this.changesMaxWaitingTime) {
				change.saveAsync(this.asyncRealtimePriorityMap.progressPriority, System.currentTimeMillis());
				entityChanges.remove(changeKey);
				return;
			}
		} else {
			change = new PendingChange<>(foreignValue, foreignCollection, System.currentTimeMillis(), null);
		}

		SchedulerUtil.Task task = SchedulerUtil.runTaskLater(
			this.plugin, () -> {
				change.saveAsync(this.asyncRealtimePriorityMap.progressPriority, System.currentTimeMillis());
				entityChanges.remove(changeKey);
			}, this.changesWaitingTIme / 50
		);
		change.setTask(task);
		entityChanges.put(changeKey, change);
	}

	public void saveAllPendingSync() {
		this.pendingChanges.keySet()
			.forEach(this::saveAllPendingSync);
	}

	public void saveAllPendingSync(K key) {
		this.pendingChanges.getOrDefault(key, new ConcurrentHashMap<>())
			.values()
			.forEach(change -> {
				change.task.cancel();
				change.save();
			});
		this.pendingChanges.remove(key);
	}

	public CompletableFuture<Void> saveAllPending(K key, int priority) {
		if (DisableLock.IS_LOCKED) {
			this.saveAllPendingSync(key);
			return CompletableFuture.completedFuture(null);
		}

		long operationId = System.currentTimeMillis();
		return CompletableFuture.allOf(this.pendingChanges.getOrDefault(key, new ConcurrentHashMap<>())
				.values()
				.stream()
				.map(pendingChange -> {
					pendingChange.task.cancel();
					return pendingChange.saveAsync(priority, operationId);
				})
				.toArray(CompletableFuture[]::new))
			.thenRun(() -> this.pendingChanges.remove(key));
	}

	public void cancelAllPending(K key) {
		if (!this.pendingChanges.containsKey(key)) return;
		this.pendingChanges.get(key)
			.forEach((k, change) -> {
				change.task.cancel();
			});
		this.pendingChanges.remove(key);
	}

	public void cancelPending(K key, String changeKey) {
		if (!this.pendingChanges.containsKey(key)) return;
		PendingChange<?> change = this.pendingChanges.get(key)
			.get(changeKey);
		if (change == null) return;

		change.task.cancel();
		this.pendingChanges.get(key)
			.remove(changeKey);
	}

	@Override
	protected CompletableFuture<Void> saveToDbAsync(V value, int priority, long operationId) {
		return this.runAsync(
				() -> {
					if (!this.cache.contains(value.getKey())) return;
					if (value instanceof IDataEntityLifecycle) ((IDataEntityLifecycle) value).beforeSave(this.plugin);
				}, priority, operationId
			)
			.thenCompose(v -> this.runAsync(
				() -> {
					if (!this.cache.contains(value.getKey())) return;
					TryCatchUtil.tryRun(() -> this.dao.update(value));
				}, priority, operationId
			))
			.thenCompose(v -> this.saveAllPending(value.getKey(), priority));
	}

	@Override
	protected void saveToDb(V value) {
		if (value instanceof IDataEntityLifecycle) ((IDataEntityLifecycle) value).beforeSave(this.plugin);
		TryCatchUtil.tryRun(() -> this.dao.update(value));
		this.saveAllPending(
			value.getKey(), this.getAsyncPriorityMap()
				.getSavePriority()
		);
	}

	public void saveFull(V value) {
		super.saveToDb(value);
	}

	public CompletableFuture<Void> saveAsyncFull(V value, int priority) {
		return super.saveToDbAsync(value, priority, System.currentTimeMillis());
	}

	@Override
	public void remove(K key) {
		super.remove(key);
		this.cancelAllPending(key);
	}

	@Override
	public void removeAll() {
		super.removeAll();
		this.pendingChanges.keySet()
			.forEach(this::cancelAllPending);
	}

	@Getter @Builder @NoArgsConstructor @AllArgsConstructor public static class AsyncRealtimePriorityMap {
		private AsyncPriorityMap asyncPriorityMap;
		private int progressPriority;
		private int liveActionPriority;

		public AsyncRealtimePriorityMap(int generalPriority) {
			this.asyncPriorityMap = new AsyncPriorityMap(generalPriority);
			this.progressPriority = generalPriority;
			this.liveActionPriority = generalPriority;
		}
	}

	@Getter @Setter @AllArgsConstructor public class PendingChange<T> {
		private final T value;
		private final ForeignCollection<T> collection;
		private final long firstChangeTime;
		private SchedulerUtil.Task task;

		public void save() {
			TryCatchUtil.tryAndReturn(() -> this.collection.update(value));
		}

		public CompletableFuture<Void> saveAsync(int priority, long operationId) {
			return runAsync(this::save, priority, operationId);
		}
	}
}
