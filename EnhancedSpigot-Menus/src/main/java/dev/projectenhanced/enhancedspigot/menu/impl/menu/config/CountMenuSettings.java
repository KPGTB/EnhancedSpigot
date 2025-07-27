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

package dev.projectenhanced.enhancedspigot.menu.impl.menu.config;

import dev.projectenhanced.enhancedspigot.util.item.EnhancedItemBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CountMenuSettings extends ConfigMenuSettings {
	private String title = "Response";
	private int rows = 3;

	@Getter private DynamicItems dynamicItems = new DynamicItems();
	@Getter private List<Double> values = Arrays.asList(
		0.01, 0.1, 0.5, 1.0, 10.0, 100.0);

	private Map<String, StaticItem> staticItems = this.defaultStaticItems();

	private Map<String, StaticItem> defaultStaticItems() {
		Map<String, StaticItem> map = new HashMap<>();

		map.put(
			"14", new StaticItem(
				new EnhancedItemBuilder(Material.EMERALD).displayName(
						"<gold><value>")
					.lore("<green><b>Click to accept")
					.build(), Arrays.asList("[accept]")
			)
		);

		return map;
	}

	@Override
	protected String title() {
		return this.title;
	}

	@Override
	protected int rows() {
		return this.rows;
	}

	@Override
	public boolean blockClick() {
		return true;
	}

	@Override
	public Map<String, Set<String>> dynamicSlots() {
		Map<String, Set<String>> map = new HashMap<>();
		map.put("decrease-value", this.dynamicItems.decreaseValueSlots);
		map.put("increase-value", this.dynamicItems.increaseValueSlots);
		return map;
	}

	@Override
	protected Map<String, StaticItem> staticItems() {
		return this.staticItems;
	}

	@Getter @NoArgsConstructor public class DynamicItems {
		private Set<String> decreaseValueSlots = new HashSet<>(
			Arrays.asList("1", "10", "19", "2", "11", "20"));
		private ItemStack decreaseValueItem = new EnhancedItemBuilder(
			Material.LIME_DYE).displayName("<red>-<value>")
			.build();

		private Set<String> increaseValueSlots = new HashSet<>(
			Arrays.asList("6", "15", "24", "7", "16", "25"));
		private ItemStack increaseValueItem = new EnhancedItemBuilder(
			Material.LIME_DYE).displayName("<red>+<value>")
			.build();
	}

}
