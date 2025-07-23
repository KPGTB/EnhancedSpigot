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

package dev.projectenhanced.enhancedspigot.menu.impl.menu;

import dev.projectenhanced.enhancedspigot.menu.EnhancedMenu;
import dev.projectenhanced.enhancedspigot.menu.container.MenuContainer;
import dev.projectenhanced.enhancedspigot.menu.container.PagedMenuContainer;
import dev.projectenhanced.enhancedspigot.menu.impl.menu.config.ConfigMenuSettings;
import dev.projectenhanced.enhancedspigot.menu.item.MenuItem;
import dev.projectenhanced.enhancedspigot.util.Pair;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class EnhancedConfigMenu extends EnhancedMenu {
	protected final ConfigMenuSettings settings;
	protected final JavaPlugin plugin;
	protected final Player viewer;
	protected TagResolver[] placeholders;

	public EnhancedConfigMenu(JavaPlugin plugin, Player viewer, ConfigMenuSettings settings, TagResolver... placeholders) {
		super(
			settings.getTitle(viewer, placeholders), settings.getRows(),
			plugin
		);
		this.plugin = plugin;
		this.viewer = viewer;
		this.settings = settings;
		this.placeholders = placeholders;

		if (settings.blockClick()) this.blockClick();
	}

	protected abstract Map<String, List<Object>> provideObjects();

	protected abstract Map<String, Function<Object, MenuItem>> processObject();

	protected abstract Map<String, Consumer<EnhancedMenu>> customStaticActions();

	protected abstract void beforePrepare(MenuContainer container);

	protected abstract void afterPrepare(MenuContainer container);

	@Override
	public void prepareGui() {
		resetContainers();

		PagedMenuContainer container = new PagedMenuContainer(
			this, 0, 0, 9, this.getRows());

		this.beforePrepare(container);

		// Generate dynamic items

		List<MenuContainer> pages = new ArrayList<>();

		Map<String, List<Object>> objects = provideObjects();
		Map<String, Integer> itemsLeft = new HashMap<>();
		objects.forEach((key, list) -> itemsLeft.put(key, list.size()));

		while (itemsLeft.values()
			.stream()
			.anyMatch(num -> num > 0)) {
			MenuContainer page = new MenuContainer(container);

			this.settings.dynamicSlots()
				.forEach((key, slots) -> {
					int left = itemsLeft.get(key);
					Function<Object, MenuItem> func = this.processObject()
						.get(key);

					for (Integer slot : slots) {
						Object obj = objects.get(key)
							.get(0);
						objects.get(key)
							.remove(0);

						Pair<Integer, Integer> location = container.getContainerLocFromMenuLoc(
							slot);
						page.setItem(
							location.getFirst(), location.getSecond(),
							func.apply(obj)
						);

						left--;
						if (left <= 0) break;
					}

					itemsLeft.put(key, left);
				});

			pages.add(page);
		}

		// Generate static items

		if (pages.isEmpty()) pages.add(new MenuContainer(container));

		pages.forEach(page -> {
			this.settings.getStaticItems(
					this, this.viewer,
					this.customStaticActions(), this.placeholders
				)
				.forEach((slot, item) -> {
					Pair<Integer, Integer> location = container.getContainerLocFromMenuLoc(
						slot);
					page.setItem(
						location.getFirst(), location.getSecond(), item);
				});
		});

		pages.forEach(container::addPage);

		this.afterPrepare(container);

		addContainer(container);
	}
}
