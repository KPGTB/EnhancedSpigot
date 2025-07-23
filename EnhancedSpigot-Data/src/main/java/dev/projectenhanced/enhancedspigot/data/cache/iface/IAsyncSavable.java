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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface IAsyncSavable<K, V> extends ISavable<K, V> {
	CompletableFuture<V> loadAsync(K key);

	CompletableFuture<Set<V>> loadAsyncAll();

	CompletableFuture<Set<V>> loadAsyncAll(boolean ignoreCached);

	CompletableFuture<Void> modifyAsync(K key, Consumer<V> action);

	CompletableFuture<Void> modifyAsyncMultiple(Set<K> keys, Consumer<V> action);

	CompletableFuture<Void> modifyAsyncAll(Consumer<V> action);

	CompletableFuture<Void> loopAsyncAll(Consumer<V> action);

	CompletableFuture<Set<V>> loopAsyncAll();

	CompletableFuture<Void> saveAsync(K key);

	CompletableFuture<Void> saveAsyncAll();

	CompletableFuture<Void> createAsync(K key, V value);

	CompletableFuture<Void> removeAsync(K key);

	CompletableFuture<Void> removeAsyncAll();

	CompletableFuture<Boolean> existsAsync(K key);
}
