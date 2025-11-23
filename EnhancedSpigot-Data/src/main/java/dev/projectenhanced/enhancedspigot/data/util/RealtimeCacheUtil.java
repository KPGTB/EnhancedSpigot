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

import com.j256.ormlite.dao.ForeignCollection;
import dev.projectenhanced.enhancedspigot.data.cache.RealtimeCache;
import dev.projectenhanced.enhancedspigot.data.cache.iface.IAsyncSavableCache;
import dev.projectenhanced.enhancedspigot.data.cache.iface.ICached;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;

import java.util.concurrent.CompletableFuture;

public class RealtimeCacheUtil {
	private static boolean isLiveCache(IAsyncSavableCache<?, ?> cache) {
		return cache instanceof RealtimeCache<?, ?>;
	}

	public static <V extends ICached<?>> CompletableFuture<Void> saveAsyncFull(IAsyncSavableCache<?, V> cache, V value, boolean highPriority) {
		if (cache instanceof RealtimeCache<?, ?>) {
			RealtimeCache<?, V> lc = (RealtimeCache<?, V>) cache;
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

	public static <K, T> void addPendingChange(IAsyncSavableCache<K, ?> cache, K key, String changeKey, T value, ForeignCollection<T> collection) {
		if (cache instanceof RealtimeCache<?, ?>) ((RealtimeCache<K, ?>) cache).addPendingChange(key, changeKey, value, collection);
	}

	public static <T> void createForeign(IAsyncSavableCache<?, ?> cache, T foreign, ForeignCollection<T> collection) {
		if (cache instanceof RealtimeCache<?, ?>) {
			RealtimeCache<?, ?> lc = (RealtimeCache<?, ?>) cache;
			lc.runAsync(
				() -> TryCatchUtil.tryRun(() -> collection.add(foreign)), lc.getAsyncRealtimePriorityMap()
					.getLiveActionPriority(), System.currentTimeMillis()
			);
		}
	}

	public static <T> void removeForeign(IAsyncSavableCache<?, ?> cache, T foreign, ForeignCollection<T> collection) {
		if (cache instanceof RealtimeCache<?, ?>) {
			RealtimeCache<?, ?> lc = (RealtimeCache<?, ?>) cache;
			lc.runAsync(
				() -> TryCatchUtil.tryRun(() -> collection.getDao()
					.delete(foreign)), lc.getAsyncRealtimePriorityMap()
					.getLiveActionPriority(), System.currentTimeMillis()
			);
		}
	}

	public static void clearForeign(IAsyncSavableCache<?, ?> cache, ForeignCollection<?> collection) {
		if (cache instanceof RealtimeCache<?, ?>) {
			RealtimeCache<?, ?> lc = (RealtimeCache<?, ?>) cache;
			lc.runAsync(
				() -> TryCatchUtil.tryRun(collection::clear), lc.getAsyncRealtimePriorityMap()
					.getLiveActionPriority(), System.currentTimeMillis()
			);
		}
	}
}
