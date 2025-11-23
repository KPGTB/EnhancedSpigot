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

package dev.projectenhanced.enhancedspigot.data.util;

import dev.projectenhanced.enhancedspigot.data.cache.DisableLock;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

public class PriorityCompletableUtil {
	public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier, ExecutorService executor, int priority, long operationId) {
		if (DisableLock.IS_LOCKED) {
			supplier.get();
			return null;
		}

		CompletableFuture<T> future = new CompletableFuture<>();
		executor.execute(new AsyncJob<>(priority, operationId, supplier, future));
		return future;
	}

	public static CompletableFuture<Void> runAsync(Runnable runnable, ExecutorService executor, int priority, long operationId) {
		return supplyAsync(
			() -> {
				runnable.run();
				return null;
			}, executor, priority, operationId
		);
	}
}
