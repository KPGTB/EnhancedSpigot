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

package dev.projectenhanced.enhancedspigot.data.util;

import com.j256.ormlite.dao.ForeignCollection;
import dev.projectenhanced.enhancedspigot.data.repository.entity.AbstractDataEntity;
import dev.projectenhanced.enhancedspigot.data.repository.iface.IAsyncDataRepository;
import dev.projectenhanced.enhancedspigot.data.repository.iface.IDataRepository;
import dev.projectenhanced.enhancedspigot.data.repository.impl.RealtimeDataRepository;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;

import java.util.concurrent.CompletableFuture;

public class RealtimeCacheUtil {
	private static boolean isRealtime(IDataRepository<?, ?> cache) {
		return cache instanceof RealtimeDataRepository<?, ?>;
	}

	public static <V extends AbstractDataEntity<?>> void saveSyncFull(IDataRepository<?, V> cache, V value) {
		if (cache instanceof RealtimeDataRepository<?, ?>) ((RealtimeDataRepository<?, V>) cache).saveFull(value);
		else cache.saveValue(value);
	}

	public static <V extends AbstractDataEntity<?>> CompletableFuture<Void> saveAsyncFull(IAsyncDataRepository<?, V> cache, V value, boolean highPriority) {
		if (cache instanceof RealtimeDataRepository<?, ?>) {
			RealtimeDataRepository<?, V> lc = (RealtimeDataRepository<?, V>) cache;
			return lc.saveAsyncFull(
				value, highPriority ?
					lc.getAsyncPriorityMap()
						.getSavePriority() :
					lc.getAsyncPriorityMap()
						.getSaveAllPriority()
			);
		}
		return cache.saveAsyncValue(value);
	}

	public static <K, T extends AbstractDataEntity<?>> void addPendingChange(IAsyncDataRepository<K, ?> cache, K key, T value, ForeignCollection<T> collection) {
		if (cache instanceof RealtimeDataRepository<?, ?>) ((RealtimeDataRepository<K, ?>) cache).addPendingChange(key, value, collection);
	}

	public static <T> void createForeign(IAsyncDataRepository<?, ?> cache, T foreign, ForeignCollection<T> collection) {
		if (cache instanceof RealtimeDataRepository<?, ?>) {
			RealtimeDataRepository<?, ?> lc = (RealtimeDataRepository<?, ?>) cache;
			lc.runAsync(
				() -> TryCatchUtil.tryRun(() -> collection.add(foreign)), lc.getAsyncRealtimePriorityMap()
					.getLiveActionPriority(), System.currentTimeMillis()
			);
		}
	}

	public static <T> void removeForeign(IAsyncDataRepository<?, ?> cache, T foreign, ForeignCollection<T> collection) {
		if (cache instanceof RealtimeDataRepository<?, ?>) {
			RealtimeDataRepository<?, ?> lc = (RealtimeDataRepository<?, ?>) cache;
			lc.runAsync(
				() -> TryCatchUtil.tryRun(() -> collection.getDao()
					.delete(foreign)), lc.getAsyncRealtimePriorityMap()
					.getLiveActionPriority(), System.currentTimeMillis()
			);
		}
	}

	public static void clearForeign(IAsyncDataRepository<?, ?> cache, ForeignCollection<?> collection) {
		if (cache instanceof RealtimeDataRepository<?, ?>) {
			RealtimeDataRepository<?, ?> lc = (RealtimeDataRepository<?, ?>) cache;
			lc.runAsync(
				() -> TryCatchUtil.tryRun(collection::clear), lc.getAsyncRealtimePriorityMap()
					.getLiveActionPriority(), System.currentTimeMillis()
			);
		}
	}
}
