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

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@AllArgsConstructor public class AsyncJob<V> implements Runnable, Comparable<AsyncJob<?>>, CompletableFuture.AsynchronousCompletionTask {

	@Getter private final Integer priority;
	@Getter private final Long operationId;
	private final Supplier<V> supplier;
	private final CompletableFuture<V> future;

	@Override
	public void run() {
		try {
			if (!future.isDone()) future.complete(this.supplier.get());
		} catch (Throwable t) {
			this.future.completeExceptionally(t);
		}
	}

	@Override
	public int compareTo(AsyncJob o) {
		int priorityComp = -priority.compareTo(o.priority);
		return priorityComp != 0 ?
			priorityComp :
			this.operationId.compareTo(o.operationId);
	}
}