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
import dev.projectenhanced.enhancedspigot.data.DatabaseController;
import dev.projectenhanced.enhancedspigot.data.cache.iface.ForeignMapper;
import dev.projectenhanced.enhancedspigot.data.cache.iface.ICached;
import dev.projectenhanced.enhancedspigot.data.cache.iface.IForeignMapping;
import dev.projectenhanced.enhancedspigot.data.cache.iface.IForeignMappingHandler;
import dev.projectenhanced.enhancedspigot.data.cache.iface.ISavableCache;
import dev.projectenhanced.enhancedspigot.data.cache.iface.ISavableLifecycle;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class DataCache<K, V extends ICached<K>> implements ISavableCache<K, V>, IForeignMappingHandler {

	@Getter protected final Dao<V, K> dao;
	protected final ConcurrentMap<K, V> cache;
	protected final JavaPlugin plugin;

	/**
	 * Automated constructor
	 * Works only when extending it or using anonymous class
	 *
	 * @param controller DatabaseController instance
	 */
	@SuppressWarnings("unchecked")
	public DataCache(DatabaseController controller, JavaPlugin plugin) {
		this.cache = new ConcurrentHashMap<>();
		this.plugin = plugin;

		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		Type[] typeArgs = type.getActualTypeArguments();
		Class<K> keyClass = (Class<K>) typeArgs[0];
		Class<V> valueClass = (Class<V>) typeArgs[1];

		this.dao = controller.getDao(valueClass, keyClass);
	}

	public DataCache(DatabaseController controller, JavaPlugin plugin, Class<K> keyClass, Class<V> valueClass) {
		this.cache = new ConcurrentHashMap<>();
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
	public V getOrNull(K key) {
		return this.cache.get(key);
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
		TryCatchUtil.tryRun(() -> {
			this.dao.deleteById(key);
			this.invalidate(key);
		});
	}

	@Override
	public void removeIf(BiPredicate<K, V> predicate) {
		this.cache.forEach((key, value) -> {
			if (predicate.test(key, value)) this.remove(key);
		});
	}

	@Override
	public void removeAll() {
		this.invalidateAll();
		TryCatchUtil.tryRun(() -> this.dao.deleteBuilder()
			.delete());
	}

	@Override
	public V load(K key) {
		V value = TryCatchUtil.tryAndReturn(() -> this.dao.queryForId(key));
		if (value == null) return null;
		this.loadValueIntoCache(key, value);
		return value;
	}

	@Override
	public Collection<V> loadAll() {
		return this.loadAll(false);
	}

	@Override
	public Collection<V> loadAll(boolean ignoreCached) {
		TryCatchUtil.tryOrDefault(this.dao::queryForAll, new ArrayList<V>())
			.stream()
			.filter(entity -> {
				return !ignoreCached || !this.contains(entity.getKey());
			})
			.forEach(value -> {
				this.loadValueIntoCache(value.getKey(), value);
			});
		return this.cache.values();
	}

	protected void loadValue(K key, V value) {
		if (value instanceof IForeignMapping) this.dbToJava((IForeignMapping) value);
		if (value instanceof ISavableLifecycle) ((ISavableLifecycle) value).afterLoad(this.plugin);
	}

	protected void loadValueIntoCache(K key, V value) {
		this.loadValue(key, value);
		this.cache.put(key, value);
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
		this.loopAll(value -> {
			if (!keys.contains(value.getKey())) return;
			action.accept(value);
			if (!this.contains(value.getKey())) this.saveValue(value);
		});
	}

	@Override
	public void modifyAll(Consumer<V> action) {
		this.loopAll(value -> {
			action.accept(value);
			if (!this.contains(value.getKey())) this.saveValue(value);
		});
	}

	@Override
	public void loopAll(Consumer<V> action) {
		this.loopAll()
			.forEach(action);
	}

	@Override
	public Collection<V> loopAll() {
		Set<V> values = new HashSet<>(this.values());
		TryCatchUtil.tryOrDefault(this.dao::queryForAll, new ArrayList<V>())
			.forEach(entity -> {
				if (this.contains(entity.getKey())) return;
				this.loadValue(entity.getKey(), entity);
				values.add(entity);
			});
		return values;
	}

	@Override
	public void save(K key) {
		if (!contains(key)) return;
		V value = this.get(key);
		this.saveValue(value);
	}

	@Override
	public void saveValue(V value) {
		saveToDb(value);
	}

	protected void createInDb(V value) {
		if (value instanceof ISavableLifecycle) ((ISavableLifecycle) value).beforeSave(this.plugin);
		if (value instanceof IForeignMapping) this.javaToDb((IForeignMapping) value);
		TryCatchUtil.tryRun(() -> this.dao.create(value));
	}

	protected void saveToDb(V value) {
		if (value instanceof ISavableLifecycle) ((ISavableLifecycle) value).beforeSave(this.plugin);
		if (value instanceof IForeignMapping) this.javaToDb((IForeignMapping) value);
		TryCatchUtil.tryRun(() -> this.dao.update(value));
	}

	@Override
	public void saveAll() {
		this.cache.values()
			.forEach(this::saveToDb);
	}

	@Override
	public V create(K key, V value) {
		this.set(key, value);
		this.createInDb(value);
		return this.load(key);
	}

	@Override
	public V create(V value) {
		this.createInDb(value);
		return this.load(value.getKey());
	}

	@Override
	public boolean exists(K key) {
		if (this.contains(key)) return true;
		return TryCatchUtil.tryOrDefault(() -> this.dao.idExists(key), false);
	}

	@Override
	public void dbToJava(IForeignMapping entity) {
		entity.getForeignMapping()
			.forEach(this::addAllForeignToCollection);

		entity.getForeignMappers()
			.forEach(this::addAllMapper);
	}

	@Override
	public void javaToDb(IForeignMapping entity) {
		entity.getForeignMapping()
			.forEach((foreign, collection) -> {
				foreign.removeIf(o -> !collection.contains(o));
				collection.forEach(obj -> {
					if (obj.getKey() != null) updateForeign(foreign, obj);
					else addToForeign(foreign, obj);
				});
			});

		entity.getForeignMappers()
			.forEach(mapper -> {
				mapper.getForeign()
					.removeIf(o -> !mapper.getCache()
						.containsKey(this.extractKeyFromMapper(mapper, o)));
				mapper.getCache()
					.forEach((key, obj) -> {
						if (obj.getKey() != null) updateForeign(mapper.getForeign(), obj);
						else addToForeign(mapper.getForeign(), obj);
					});
			});
	}

	@Override
	public void refreshForeign(ForeignCollection<? extends ICached<Integer>> foreign, Collection<? extends ICached<Integer>> collection) {
		this.addAllForeignToCollection(foreign, collection);
	}

	@Override
	public void refreshForeign(ForeignMapper<?> mapper) {
		this.addAllMapper(mapper);
	}

	@SuppressWarnings("unchecked")
	private <T> void addAllForeignToCollection(ForeignCollection<?> foreign, Collection<T> collection) {
		collection.clear();
		foreign.forEach(entity -> collection.add((T) entity));
	}

	private <T extends ICached<Integer>> void addAllMapper(ForeignMapper<T> mapper) {
		mapper.getCache()
			.clear();
		mapper.getForeign()
			.forEach(e -> mapper.getCache()
				.put(
					mapper.getKeyExtractor()
						.apply(e), e
				));
	}

	@SuppressWarnings("unchecked")
	protected <T extends ICached<Integer>> String extractKeyFromMapper(ForeignMapper<T> mapper, Object obj) {
		return mapper.getKeyExtractor()
			.apply((T) obj);
	}

	@SuppressWarnings("unchecked")
	protected <T> void updateForeign(ForeignCollection<T> foreign, Object obj) {
		TryCatchUtil.tryRun(() -> foreign.update((T) obj));
	}

	@SuppressWarnings("unchecked")
	protected <T> void addToForeign(ForeignCollection<T> foreign, Object obj) {
		TryCatchUtil.tryRun(() -> foreign.add((T) obj));
	}
}
