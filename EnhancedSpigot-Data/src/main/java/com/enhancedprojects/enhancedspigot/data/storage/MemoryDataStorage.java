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

package com.enhancedprojects.enhancedspigot.data.storage;

import com.enhancedprojects.enhancedspigot.data.repository.entity.AbstractDataEntity;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MemoryDataStorage<K, V extends AbstractDataEntity<K>> implements IDataStorage<K, V> {
	private final ConcurrentMap<K, V> cache;

	public MemoryDataStorage() {
		this.cache = new ConcurrentHashMap<>();
	}

	@Override
	public V get(K key) {
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
}
