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

import dev.projectenhanced.enhancedspigot.locale.TextUtil;
import dev.projectenhanced.enhancedspigot.menu.EnhancedMenu;
import dev.projectenhanced.enhancedspigot.menu.impl.menu.config.CountMenuSettings;
import dev.projectenhanced.enhancedspigot.menu.item.MenuItem;
import dev.projectenhanced.enhancedspigot.util.SchedulerUtil;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class EnhancedCountMenu extends EnhancedConfigMenu {
	private final CountMenuSettings settings;

	private final EnhancedMenu lastMenu;
	private final Consumer<Double> response;
	private final Player viewer;

	private final double min;
	private final double max;
	private double value;
	private boolean responded;

	public EnhancedCountMenu(JavaPlugin plugin, Player viewer, CountMenuSettings settings, EnhancedMenu lastMenu, Consumer<Double> response, double defaultValue, double min, double max) {
		super(
			plugin, viewer, settings,
			Placeholder.unparsed("value", String.valueOf(defaultValue))
		);
		this.settings = settings;
		this.viewer = viewer;
		this.lastMenu = lastMenu;
		this.response = response;
		this.min = min;
		this.max = max;

		this.value = defaultValue;
		this.responded = false;

		setCloseAction(e -> {
			if (!this.responded) {
				this.response.accept(null);
				if (this.lastMenu != null) SchedulerUtil.runTaskLater(
					plugin, (task) -> this.lastMenu.open(viewer), 3);
			}
		});
	}

	@Override
	protected Map<String, List<Object>> provideObjects() {
		Map<String, List<Object>> map = new HashMap<>();

		map.put("increase-value", new ArrayList<>());
		map.put("decrease-value", new ArrayList<>());

		this.settings.getValues()
			.forEach(value -> {
				map.keySet()
					.forEach(key -> {
						map.get(key)
							.add(value);
					});
			});

		return map;
	}

	@Override
	protected Map<String, Function<Object, MenuItem>> processObject() {
		Map<String, Function<Object, MenuItem>> map = new HashMap<>();

		map.put(
			"increase-value", (value) -> {
				ItemStack is = this.settings.getDynamicItems()
					.getIncreaseValueItem();
				TextUtil.modifyItem(
					is, Placeholder.unparsed("value", String.valueOf(value)));
				MenuItem menuItem = new MenuItem(is);
				this.changeValue((double) value);
				return menuItem;
			}
		);
		map.put(
			"decrease-value", (value) -> {
				ItemStack is = this.settings.getDynamicItems()
					.getDecreaseValueItem();
				TextUtil.modifyItem(
					is, Placeholder.unparsed("value", String.valueOf(value)));
				MenuItem menuItem = new MenuItem(is);
				this.changeValue(-((double) value));
				return menuItem;
			}
		);

		return map;
	}

	@Override
	protected Map<String, Consumer<EnhancedMenu>> customStaticActions() {
		Map<String, Consumer<EnhancedMenu>> map = new HashMap<>();

		map.put(
			"accept", (menu) -> {
				this.responded = true;
				this.response.accept(this.value);
				if (this.lastMenu != null) this.lastMenu.open(viewer);
			}
		);

		return map;
	}

	private void changeValue(double addValue) {
		double newValue = Math.round((this.value + addValue) * 100.0) / 100.0;

		if (newValue < min) return;
		if (newValue > max) return;

		this.value = newValue;
		super.placeholders = new TagResolver[]{Placeholder.unparsed(
			"value", String.valueOf(newValue))};
		super.prepareGui();
	}
}
