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

package dev.projectenhanced.enhancedspigot.data.repository.impl;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import dev.projectenhanced.enhancedspigot.data.DatabaseController;
import dev.projectenhanced.enhancedspigot.data.repository.entity.AbstractDataEntity;
import dev.projectenhanced.enhancedspigot.data.repository.entity.AbstractRealtimeChild;
import dev.projectenhanced.enhancedspigot.data.repository.entity.ForeignMapper;
import dev.projectenhanced.enhancedspigot.data.repository.entity.IDataEntityLifecycle;
import dev.projectenhanced.enhancedspigot.data.repository.entity.IForeignMapping;
import dev.projectenhanced.enhancedspigot.data.repository.iface.IDataRepository;
import dev.projectenhanced.enhancedspigot.data.repository.iface.IForeignMappingHandler;
import dev.projectenhanced.enhancedspigot.data.storage.IDataStorage;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class DataRepository<K, V extends AbstractDataEntity<K>> implements IForeignMappingHandler, IDataRepository<K, V> {

	@Getter protected final Dao<V, K> dao;
	protected final IDataStorage<K, V> cache;
	protected final JavaPlugin plugin;

	/**
	 * Automated constructor
	 * Works only when extending it or using anonymous class
	 *
	 * @param controller DatabaseController instance
	 */
	@SuppressWarnings("unchecked")
	public DataRepository(DatabaseController controller, IDataStorage<K, V> cache, JavaPlugin plugin) {
		this.cache = cache;
		this.plugin = plugin;

		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		Type[] typeArgs = type.getActualTypeArguments();
		Class<K> keyClass = (Class<K>) typeArgs[0];
		Class<V> valueClass = (Class<V>) typeArgs[1];

		this.dao = controller.getDao(valueClass, keyClass);
	}

	public DataRepository(DatabaseController controller, IDataStorage<K, V> cache, JavaPlugin plugin, Class<K> keyClass, Class<V> valueClass) {
		this.cache = cache;
		this.dao = controller.getDao(valueClass, keyClass);
		this.plugin = plugin;
	}

	@Override
	public IDataStorage<K, V> getCache() {
		return this.cache;
	}

	@Override
	public V get(K key) {
		return this.cache.contains(key) ?
			this.cache.get(key) :
			this.load(key);
	}

	@Override
	public V getOrNull(K key) {
		return this.cache.get(key);
	}

	@Override
	public void remove(K key) {
		TryCatchUtil.tryRun(() -> {
			this.dao.deleteById(key);
			this.cache.invalidate(key);
		});
	}

	@Override
	public void removeIf(BiPredicate<K, V> predicate) {
		this.cache.entrySet()
			.forEach(entry -> {
				if (predicate.test(entry.getKey(), entry.getValue())) this.remove(entry.getKey());
			});
	}

	@Override
	public void removeAll() {
		this.cache.invalidateAll();
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
				return !ignoreCached || !this.cache.contains(entity.getKey());
			})
			.forEach(value -> {
				this.loadValueIntoCache(value.getKey(), value);
			});
		return this.cache.values();
	}

	protected void processValue(V value) {
		TryCatchUtil.tryRun(() -> value.setRepository(this));
		if (value instanceof IForeignMapping) this.dbToJava((IForeignMapping) value);
		if (value instanceof IDataEntityLifecycle) ((IDataEntityLifecycle) value).afterLoad(this.plugin);
	}

	protected void loadValueIntoCache(K key, V value) {
		this.processValue(value);
		this.cache.set(key, value);
	}

	@Override
	public void modify(K key, Consumer<V> action) {
		boolean contains = this.cache.contains(key);
		if (!exists(key) && !contains) return;

		action.accept(this.get(key));
		if (!contains) {
			this.save(key);
			this.cache.invalidate(key);
		}
	}

	@Override
	public void modifyMultiple(Set<K> keys, Consumer<V> action) {
		this.loopAll(value -> {
			if (!keys.contains(value.getKey())) return;
			action.accept(value);
			if (!this.cache.contains(value.getKey())) this.saveValue(value);
		});
	}

	@Override
	public void modifyAll(Consumer<V> action) {
		this.loopAll(value -> {
			action.accept(value);
			if (!this.cache.contains(value.getKey())) this.saveValue(value);
		});
	}

	@Override
	public void loopAll(Consumer<V> action) {
		this.loopAll()
			.forEach(action);
	}

	@Override
	public Collection<V> loopAll() {
		Set<V> values = new HashSet<>(this.cache.values());
		TryCatchUtil.tryOrDefault(this.dao::queryForAll, new ArrayList<V>())
			.forEach(entity -> {
				if (this.cache.contains(entity.getKey())) return;
				this.processValue(entity);
				values.add(entity);
			});
		return values;
	}

	@Override
	public void save(K key) {
		if (!cache.contains(key)) return;
		V value = this.get(key);
		this.saveValue(value);
	}

	@Override
	public void saveAndInvalidate(K key) {
		this.save(key);
		this.cache.invalidate(key);
	}

	@Override
	public void saveValue(V value) {
		saveToDb(value);
	}

	protected void createInDb(V value) {
		if (value instanceof IDataEntityLifecycle) ((IDataEntityLifecycle) value).beforeSave(this.plugin);
		if (value instanceof IForeignMapping) this.javaToDb((IForeignMapping) value);
		TryCatchUtil.tryRun(() -> this.dao.create(value));
	}

	protected void saveToDb(V value) {
		if (value instanceof IDataEntityLifecycle) ((IDataEntityLifecycle) value).beforeSave(this.plugin);
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
		this.cache.set(key, value);
		this.createInDb(value);
		return this.load(key);
	}

	@Override
	public V create(V value) {
		this.createInDb(value);
		return this.load(value.getKey());
	}

	@Override
	public V loadOrCreate(K key, V defaultValue) {
		if (this.exists(key)) return this.load(key);
		return this.create(key, defaultValue);
	}

	@Override
	public boolean exists(K key) {
		if (this.cache.contains(key)) return true;
		return TryCatchUtil.tryOrDefault(() -> this.dao.idExists(key), false);
	}

	@Override
	public void dbToJava(IForeignMapping entity) {
		entity.getForeignMapping()
			.forEach((foreign, collection) -> this.addAllForeignToCollection(entity, foreign, collection));

		entity.getForeignMappers()
			.forEach(mapper -> this.addAllMapper(entity, mapper));
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
	public void refreshForeign(IForeignMapping parent, ForeignCollection<? extends AbstractDataEntity<Integer>> foreign, Collection<? extends AbstractDataEntity<Integer>> collection) {
		this.addAllForeignToCollection(parent, foreign, collection);
	}

	@Override
	public void refreshForeign(IForeignMapping parent, ForeignMapper<?> mapper) {
		this.addAllMapper(parent, mapper);
	}

	@SuppressWarnings("unchecked")
	private <T> void addAllForeignToCollection(IForeignMapping parent, ForeignCollection<?> foreign, Collection<T> collection) {
		collection.clear();
		foreign.forEach(entity -> {
			this.updateChild(parent, entity, foreign);
			collection.add((T) entity);
		});
	}

	@SuppressWarnings("unchecked")
	private <T> void updateChild(IForeignMapping entity, Object child, ForeignCollection<?> collection) {
		if (child instanceof AbstractDataEntity<?>) {
			AbstractDataEntity<T> dataChild = (AbstractDataEntity<T>) child;
			dataChild.setRepository((IDataRepository<T, ? extends AbstractDataEntity<T>>) this);
		}
		if (child instanceof AbstractRealtimeChild<?>) {
			AbstractRealtimeChild<T> realtimeChild = (AbstractRealtimeChild<T>) child;
			realtimeChild.setParentEntity((AbstractDataEntity<T>) entity);
			realtimeChild.setSourceCollection((ForeignCollection<? extends AbstractDataEntity<T>>) collection);
		}
	}

	private <T extends AbstractDataEntity<Integer>> void addAllMapper(IForeignMapping parent, ForeignMapper<T> mapper) {
		mapper.getCache()
			.clear();
		mapper.getForeign()
			.forEach(e -> {
				this.updateChild(parent, e, mapper.getForeign());
				mapper.getCache()
					.put(
						mapper.getKeyExtractor()
							.apply(e), e
					);
			});
	}

	@SuppressWarnings("unchecked")
	protected <T extends AbstractDataEntity<Integer>> String extractKeyFromMapper(ForeignMapper<T> mapper, Object obj) {
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
