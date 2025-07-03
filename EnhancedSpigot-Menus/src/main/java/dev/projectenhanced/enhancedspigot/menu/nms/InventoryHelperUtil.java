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

package dev.projectenhanced.enhancedspigot.menu.nms;

import org.bukkit.Bukkit;

public class InventoryHelperUtil {
	public static IInventoryHelper getInventoryHelper() {
		IInventoryHelper result;
		try {
			String version = Bukkit.getServer()
				.getClass()
				.getPackage()
				.getName()
				.split("\\.")[3];
			switch (version) {
				case "v1_14_R1":
				case "v1_16_R3":
				case "v1_16_R2":
				case "v1_16_R1":
				case "v1_15_R1":
					result = new InventoryHelper_1_14_1_16();
					break;
				case "v1_17_R1":
					result = new InventoryHelper_1_17();
					break;
				case "v1_18_R1":
					result = new InventoryHelper_1_18();
					break;
				case "v1_18_R2":
					result = new InventoryHelper_1_18_2();
					break;
				case "v1_19_R1":
					result = new InventoryHelper_1_19();
					break;
				case "v1_19_R2":
					result = new InventoryHelper_1_19_2();
					break;
				case "v1_19_R3":
					result = new InventoryHelper_1_19_3();
					break;
				case "v1_20_R1":
					result = new InventoryHelper_1_20();
					break;
				default:
					result = null;
					break;
			}
			if (result == null && version.startsWith("v1_2")) {
				result = new InventoryHelper_1_20();
			}
		} catch (Exception e) {
			result = new InventoryHelper_1_20();
		}
		return result;
	}
}
