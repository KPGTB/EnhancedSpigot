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

public interface IDataStorage<K, V extends AbstractDataEntity<K>> {
	V get(K key);

	Set<K> keySet();

	Collection<V> values();

	Set<Map.Entry<K, V>> entrySet();

	void set(K key, V value);

	void invalidate(K key);

	void invalidateAll();

	boolean contains(K key);
}
