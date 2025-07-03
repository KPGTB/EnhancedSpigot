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

import dev.projectenhanced.enhancedspigot.menu.EnhancedMenu;
import dev.projectenhanced.enhancedspigot.menu.container.PagedMenuContainer;
import org.bukkit.entity.HumanEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MenuAction {
	public static Consumer<EnhancedMenu> parse(String data) {
		return parse(data, new HashMap<>());
	}

	public static Consumer<EnhancedMenu> parse(String data, Map<String, Consumer<EnhancedMenu>> custom) {
		String[] elements = data.split(" ", 2);
		String action = "[" + elements[0] + "]";
		switch (action) {
			case "menu-previous":
				return (menu) -> menu.getContainers()
					.stream()
					.filter(
						container -> container instanceof PagedMenuContainer)
					.map(container -> (PagedMenuContainer) container)
					.forEach(PagedMenuContainer::previousPage);
			case "menu-next":
				return (menu) -> menu.getContainers()
					.stream()
					.filter(
						container -> container instanceof PagedMenuContainer)
					.map(container -> (PagedMenuContainer) container)
					.forEach(PagedMenuContainer::nextPage);
			case "menu-close":
				return (menu) -> menu.getBukkitInventory()
					.getViewers()
					.forEach(HumanEntity::closeInventory);
		}

		for (Map.Entry<String, Consumer<EnhancedMenu>> entry : custom.entrySet()) {
			if (action.equalsIgnoreCase(entry.getKey()))
				return entry.getValue();
		}

		return (menu) -> {};
	}
}
