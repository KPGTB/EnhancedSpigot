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

package dev.projectenhanced.enhancedspigot.data.cache.iface;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public interface ISavable<K, V extends ICached<K>> {
	V load(K key);

	Collection<V> loadAll();

	Collection<V> loadAll(boolean ignoreCached);

	void modify(K key, Consumer<V> action);

	void modifyMultiple(Set<K> keys, Consumer<V> action);

	void modifyAll(Consumer<V> action);

	void loopAll(Consumer<V> action);

	Collection<V> loopAll();

	void save(K key);

	void saveValue(V value);

	void saveAll();

	V create(K key, V value);

	V create(V value);

	void remove(K key);

	void removeIf(BiPredicate<K, V> predicate);

	void removeAll();

	boolean exists(K key);
}