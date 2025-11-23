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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor public class AsyncPriorityMap {
	private int getPriority;
	private int loadPriority;
	private int loadAllPriority;
	private int savePriority;
	private int saveAllPriority;
	private int modifyPriority;
	private int modifyAllPriority;
	private int createPriority;
	private int removePriority;
	private int existsPriority;

	public AsyncPriorityMap(int generalPriority) {
		this.getPriority = generalPriority;
		this.loadPriority = generalPriority;
		this.loadAllPriority = generalPriority;
		this.savePriority = generalPriority;
		this.saveAllPriority = generalPriority;
		this.modifyPriority = generalPriority;
		this.modifyAllPriority = generalPriority;
		this.createPriority = generalPriority;
		this.removePriority = generalPriority;
		this.existsPriority = generalPriority;
	}
}
