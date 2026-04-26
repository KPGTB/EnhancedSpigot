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

package com.enhancedprojects.enhancedspigot.data.util;

import com.enhancedprojects.enhancedspigot.data.repository.impl.RealtimeDataRepository;

public class PriorityMapUtil {
	public static AsyncPriorityMap simple(int priority) {
		return new AsyncPriorityMap(priority);
	}

	public static RealtimeDataRepository.AsyncRealtimePriorityMap simpleRealtime(int priority) {
		return new RealtimeDataRepository.AsyncRealtimePriorityMap(priority);
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

	public static RealtimeDataRepository.AsyncRealtimePriorityMap userRealtimeCache() {
		return RealtimeDataRepository.AsyncRealtimePriorityMap.builder()
			.asyncPriorityMap(userCache())
			.progressPriority(2)
			.liveActionPriority(4)
			.build();
	}

}
