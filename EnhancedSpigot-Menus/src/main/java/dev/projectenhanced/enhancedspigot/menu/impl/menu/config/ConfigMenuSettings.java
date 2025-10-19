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

import dev.projectenhanced.enhancedspigot.locale.ColorUtil;
import dev.projectenhanced.enhancedspigot.menu.EnhancedMenu;
import dev.projectenhanced.enhancedspigot.menu.item.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public abstract class ConfigMenuSettings {
	protected abstract String title();

	protected abstract int rows();

	public abstract boolean blockClick();

	public abstract Map<String, Set<String>> dynamicSlots();

	protected abstract Map<String, StaticItem> staticItems();

	public String getTitle(Player viewer, TagResolver... placeholders) {
		return ColorUtil.addPAPI(ColorUtil.convertMmToString(this.title(), placeholders), viewer);
	}

	public int getRows() {
		return Math.max(Math.min(this.rows(), 6), 1);
	}

	public Map<Integer, MenuItem> getStaticItems(EnhancedMenu menu, Player viewer, TagResolver... placeholders) {
		return getStaticItems(menu, viewer, new HashMap<>(), placeholders);
	}

	public Map<Integer, MenuItem> getStaticItems(EnhancedMenu menu, Player viewer, Map<String, Consumer<EnhancedMenu>> customActions, TagResolver... placeholders) {
		Map<Integer, MenuItem> result = new HashMap<>();

		this.staticItems()
			.forEach((slots, staticItem) -> {
				ItemStack is = staticItem.getItem()
					.clone();
				ColorUtil.modifyItem(is, viewer, placeholders);

				MenuItem item = new MenuItem(is);
				item.setClickAction((e, loc) -> {
					staticItem.getActions()
						.stream()
						.map(s -> MenuAction.parse(s, customActions))
						.forEach(action -> action.accept(menu));
				});

				this.parseSlots(slots)
					.forEach(slot -> {
						result.put(slot, item);
					});
			});

		return result;
	}

	public Map<String, Set<Integer>> getDynamicSlots() {
		Map<String, Set<Integer>> result = new HashMap<>();
		this.dynamicSlots()
			.forEach((id, slotsSet) -> {
				result.put(id, new HashSet<>());
				slotsSet.forEach((slots) -> {
					result.get(id)
						.addAll(this.parseSlots(slots));
				});
			});
		return result;
	}

	protected List<Integer> parseSlots(String slots) {
		String[] elements = slots.replace(" ", "")
			.split(",");
		List<Integer> result = new ArrayList<>();

		for (String element : elements) {
			if (element.contains("-")) {
				String[] split = element.split("-", 2);
				int first = Integer.parseInt(split[0]);
				int second = Integer.parseInt(split[1]);

				if (second < first) {
					int temp = first;
					first = second;
					second = temp;
				}

				for (int i = first; i <= second; i++) {
					result.add(i);
				}
			} else {
				result.add(Integer.parseInt(element));
			}
		}

		return result;
	}

	@Getter @NoArgsConstructor @AllArgsConstructor public static class StaticItem {
		private ItemStack item;
		private List<String> actions;
	}
}
