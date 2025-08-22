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

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.misc.TransactionManager;
import dev.projectenhanced.enhancedspigot.data.DatabaseController;
import dev.projectenhanced.enhancedspigot.data.cache.iface.IForeignMapping;
import dev.projectenhanced.enhancedspigot.data.cache.iface.IForeignMappingHandler;
import dev.projectenhanced.enhancedspigot.data.cache.iface.ISavableCache;
import dev.projectenhanced.enhancedspigot.data.cache.iface.ISavableLifecycle;
import dev.projectenhanced.enhancedspigot.util.SchedulerUtil;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DataCache<K, V> implements ISavableCache<K, V>, IForeignMappingHandler {

	private final Map<K, V> cache;
	@Getter private final Dao<V, K> dao;
	private final JavaPlugin plugin;

	/**
	 * Automated constructor
	 * Works only when extending it or using anonymous class
	 *
	 * @param controller DatabaseController instance
	 */
	@SuppressWarnings("unchecked")
	public DataCache(DatabaseController controller, JavaPlugin plugin) {
		this.cache = new HashMap<>();
		this.plugin = plugin;

		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		Type[] typeArgs = type.getActualTypeArguments();
		Class<K> keyClass = (Class<K>) typeArgs[0];
		Class<V> valueClass = (Class<V>) typeArgs[1];

		this.dao = controller.getDao(valueClass, keyClass);
	}

	public DataCache(DatabaseController controller, JavaPlugin plugin, Class<K> keyClass, Class<V> valueClass) {
		this.cache = new HashMap<>();
		this.dao = controller.getDao(valueClass, keyClass);
		this.plugin = plugin;
	}

	@Override
	public V get(K key) {
		return this.contains(key) ?
			this.cache.get(key) :
			this.load(key);
	}

	@Override
	public Set<K> keySet() {
		return this.cache.keySet();
	}

	@Override
	public Collection<V> values() {
		return this.cache.values();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return this.cache.entrySet();
	}

	@Override
	public void set(K key, V value) {
		this.cache.put(key, value);
	}

	@Override
	public void invalidate(K key) {
		this.cache.remove(key);
	}

	@Override
	public void invalidateAll() {
		this.cache.clear();
	}

	@Override
	public boolean contains(K key) {
		return this.cache.containsKey(key);
	}

	@Override
	public void remove(K key) {
		TryCatchUtil.tryRun(() -> this.dao.deleteById(key));
	}

	@Override
	public void removeAll() {
		TryCatchUtil.tryRun(() -> this.dao.deleteBuilder()
			.delete());
	}

	@Override
	public V load(K key) {
		V value = TryCatchUtil.tryAndReturn(() -> this.dao.queryForId(key));
		if (value == null) return null;
		if (value instanceof IForeignMapping) this.dbToJava(
			(IForeignMapping) value);
		if (value instanceof ISavableLifecycle)
			((ISavableLifecycle) value).afterLoad();
		this.cache.put(key, value);
		return value;
	}

	@Override
	public Set<V> loadAll() {
		return this.loadAll(false);
	}

	@Override
	public Set<V> loadAll(boolean ignoreCached) {
		TryCatchUtil.tryOrDefault(this.dao::queryForAll, new ArrayList<V>())
			.stream()
			.filter(entity -> !ignoreCached || !this.contains(
				TryCatchUtil.tryAndReturn(() -> this.dao.extractId(entity))))
			.forEach(value -> {
				if (value instanceof IForeignMapping) this.dbToJava(
					(IForeignMapping) value);
				if (value instanceof ISavableLifecycle)
					((ISavableLifecycle) value).afterLoad();
				this.cache.put(
					TryCatchUtil.tryAndReturn(() -> this.dao.extractId(value)),
					value
				);
			});
		return new HashSet<>(this.cache.values());
	}

	@Override
	public void modify(K key, Consumer<V> action) {
		boolean contains = this.contains(key);
		if (!exists(key) && !contains) return;

		action.accept(this.get(key));
		if (!contains) {
			this.save(key);
			this.invalidate(key);
		}
	}

	@Override
	public void modifyMultiple(Set<K> keys, Consumer<V> action) {
		this.cache.entrySet()
			.stream()
			.filter(entry -> keys.contains(entry.getKey()))
			.forEach(entry -> {
				action.accept(entry.getValue());
			});
		Set<K> oldKeys = new HashSet<>(this.keySet());
		this.loadAll(true);

		this.runInTransaction(() -> new ArrayList<>(this.values()).stream()
			.map(value -> new AbstractMap.SimpleEntry<K, V>(
				TryCatchUtil.tryAndReturn(() -> this.dao.extractId(value)),
				value
			))
			.filter(entry -> !oldKeys.contains(entry.getKey()))
			.filter(entry -> keys.contains(entry.getKey()))
			.forEach(entry -> {
				action.accept(entry.getValue());
				this.save(entry.getKey());
				this.invalidate(entry.getKey());
			}));
	}

	@Override
	public void modifyAll(Consumer<V> action) {
		this.cache.values()
			.forEach(action);
		Set<K> oldKeys = new HashSet<>(this.keySet());
		this.loadAll(true);

		this.runInTransaction(() -> new ArrayList<>(this.values()).stream()
			.map(value -> new AbstractMap.SimpleEntry<K, V>(
				TryCatchUtil.tryAndReturn(() -> this.dao.extractId(value)),
				value
			))
			.filter(entry -> !oldKeys.contains(entry.getKey()))
			.forEach(entry -> {
				action.accept(entry.getValue());
				this.save(entry.getKey());
				this.invalidate(entry.getKey());
			}));
	}

	@Override
	public void loopAll(Consumer<V> action) {
		this.loopAll()
			.forEach(action);
	}

	@Override
	public Set<V> loopAll() {
		Set<V> result = new HashSet<>(this.cache.values());

		Set<K> oldKeys = new HashSet<>(this.keySet());
		this.loadAll(true);

		this.runInTransaction(() -> new ArrayList<>(this.values()).stream()
			.map(value -> new AbstractMap.SimpleEntry<K, V>(
				TryCatchUtil.tryAndReturn(() -> this.dao.extractId(value)),
				value
			))
			.filter(entry -> !oldKeys.contains(entry.getKey()))
			.forEach(entry -> {
				result.add(entry.getValue());
				this.invalidate(entry.getKey());
			}));

		return result;
	}

	@Override
	public void save(K key) {
		if (!contains(key)) return;
		V value = this.get(key);
		this.saveValue(value);
	}

	@Override
	public void saveValue(V value) {
		if (value instanceof ISavableLifecycle)
			((ISavableLifecycle) value).beforeSave();
		this.runInTransaction(() -> {
			if (value instanceof IForeignMapping) this.javaToDb(
				(IForeignMapping) value);
			TryCatchUtil.tryRun(() -> this.dao.createOrUpdate(value));
		});
	}

	@Override
	public void saveAll() {
		this.runInTransaction(
			() -> new HashSet<>(this.cache.keySet()).forEach(this::save));
	}

	@Override
	public void create(K key, V value) {
		this.set(key, value);
		this.save(key);

		Consumer<SchedulerUtil.Task> task = (t) -> this.load(key);
		if (this.runningAsync()) {
			SchedulerUtil.runTaskLaterAsynchronously(plugin, task, 20);
		} else {
			SchedulerUtil.runTaskLater(plugin, task, 20);
		}
	}

	@Override
	public void create(V value) {
		this.saveValue(value);

		Consumer<SchedulerUtil.Task> task = (t) -> this.load(
			TryCatchUtil.tryAndReturn(() -> this.dao.extractId(value)));
		if (this.runningAsync()) {
			SchedulerUtil.runTaskLaterAsynchronously(plugin, task, 20);
		} else {
			SchedulerUtil.runTaskLater(plugin, task, 20);
		}
	}

	@Override
	public boolean exists(K key) {
		if (this.contains(key)) return true;
		return TryCatchUtil.tryOrDefault(() -> this.dao.idExists(key), false);
	}

	@Override
	public boolean runningAsync() {
		return false;
	}

	private void runInTransaction(Runnable runnable) {
		TryCatchUtil.tryRun(() -> TransactionManager.callInTransaction(
			this.dao.getConnectionSource(), () -> {
				runnable.run();
				return null;
			}
		));
	}

	@Override
	public void dbToJava(IForeignMapping entity) {
		entity.getForeignMapping()
			.forEach(this::addAllForeignToCollection);
	}

	@Override
	public void javaToDb(IForeignMapping entity) {
		entity.getForeignMapping()
			.forEach((foreign, collection) -> {
				foreign.removeIf(o -> !collection.contains(o));
				collection.forEach(obj -> {
					if (foreign.contains(obj)) updateForeign(foreign, obj);
					else addToForeign(foreign, obj);
				});
			});
	}

	@SuppressWarnings("unchecked")
	private <T> void addAllForeignToCollection(ForeignCollection<?> foreign, Collection<T> collection) {
		collection.clear();
		foreign.forEach(entity -> collection.add((T) entity));
	}

	@SuppressWarnings("unchecked")
	private <T> void updateForeign(ForeignCollection<T> foreign, Object obj) {
		TryCatchUtil.tryRun(() -> foreign.update((T) obj));
	}

	@SuppressWarnings("unchecked")
	private <T> void addToForeign(ForeignCollection<T> foreign, Object obj) {
		TryCatchUtil.tryRun(() -> foreign.add((T) obj));
	}
}
