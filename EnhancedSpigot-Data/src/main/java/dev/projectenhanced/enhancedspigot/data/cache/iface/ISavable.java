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

import java.util.Set;
import java.util.function.Consumer;

public interface ISavable<K, V> {
	V load(K key);

	Set<V> loadAll();

	Set<V> loadAll(boolean ignoreCached);

	void modify(K key, Consumer<V> action);

	void modifyMultiple(Set<K> keys, Consumer<V> action);

	void modifyAll(Consumer<V> action);

	void loopAll(Consumer<V> action);

	Set<V> loopAll();

	void save(K key);

	void saveAll();

	void create(K key, V value);

	void remove(K key);

	void removeAll();

	boolean exists(K key);
}
