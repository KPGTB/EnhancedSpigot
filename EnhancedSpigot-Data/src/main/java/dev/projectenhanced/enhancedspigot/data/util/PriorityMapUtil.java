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

import dev.projectenhanced.enhancedspigot.data.cache.RealtimeCache;

public class PriorityMapUtil {
	public static AsyncPriorityMap simple(int priority) {
		return new AsyncPriorityMap(priority);
	}

	public static RealtimeCache.AsyncRealtimePriorityMap simpleRealtime(int priority) {
		return new RealtimeCache.AsyncRealtimePriorityMap(priority);
	}

	public static AsyncPriorityMap userCache() {
		return AsyncPriorityMap.builder()
			.saveAllPriority(1)
			.loadAllPriority(3)
			.modifyAllPriority(3)
			.createPriority(5)
			.loadPriority(5)
			.modifyPriority(5)
			.removePriority(5)
			.savePriority(6)
			.existsPriority(50)
			.getPriority(100)
			.build();
	}

	public static RealtimeCache.AsyncRealtimePriorityMap userRealtimeCache() {
		return RealtimeCache.AsyncRealtimePriorityMap.builder()
			.asyncPriorityMap(userCache())
			.progressPriority(2)
			.liveActionPriority(4)
			.build();
	}

}
