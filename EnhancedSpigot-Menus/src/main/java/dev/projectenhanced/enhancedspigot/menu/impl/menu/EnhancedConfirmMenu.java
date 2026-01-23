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

package dev.projectenhanced.enhancedspigot.menu.impl.menu;

import dev.projectenhanced.enhancedspigot.config.annotation.Serializer;
import dev.projectenhanced.enhancedspigot.config.serializer.impl.BaseSerializer;
import dev.projectenhanced.enhancedspigot.menu.EnhancedMenu;
import dev.projectenhanced.enhancedspigot.menu.impl.menu.config.ConfigMenuSettings;
import dev.projectenhanced.enhancedspigot.menu.item.MenuItem;
import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
import dev.projectenhanced.enhancedspigot.util.SchedulerUtil;
import dev.projectenhanced.enhancedspigot.util.item.EnhancedItemBuilder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class EnhancedConfirmMenu extends EnhancedConfigMenu<EnhancedConfirmMenu.Configuration> {
	private final EnhancedMenu lastMenu;
	private final Consumer<Boolean> response;
	private final Player viewer;
	private final ItemStack infoItem;

	private boolean responded;

	public EnhancedConfirmMenu(DependencyProvider provider, Player viewer, Configuration settings, EnhancedMenu lastMenu, ItemStack infoItem, Consumer<Boolean> response) {
		super(provider, viewer, settings);
		this.viewer = viewer;
		this.lastMenu = lastMenu;
		this.response = response;
		this.infoItem = infoItem;

		this.responded = false;
		setCloseAction(e -> {
			if (!this.responded) {
				this.response.accept(false);
				if (this.lastMenu != null) SchedulerUtil.runTaskLater(plugin, () -> this.lastMenu.open(viewer), 3);
			}
		});
	}

	public EnhancedConfirmMenu(JavaPlugin plugin, Player viewer, Configuration settings, EnhancedMenu lastMenu, ItemStack infoItem, Consumer<Boolean> response) {
		super(plugin, viewer, settings);
		this.viewer = viewer;
		this.lastMenu = lastMenu;
		this.response = response;
		this.infoItem = infoItem;

		this.responded = false;
		setCloseAction(e -> {
			if (!this.responded) {
				this.response.accept(false);
				if (this.lastMenu != null) SchedulerUtil.runTaskLater(plugin, () -> this.lastMenu.open(viewer), 3);
			}
		});
	}

	@Override
	protected Map<String, List<Object>> provideObjects() {
		Map<String, List<Object>> map = new HashMap<>();
		map.put("info", new ArrayList<>(Arrays.asList(this.infoItem.clone())));
		return map;
	}

	@Override
	protected Map<String, Function<Object, MenuItem>> processObject() {
		Map<String, Function<Object, MenuItem>> map = new HashMap<>();
		map.put("info", (value) -> new MenuItem((ItemStack) value));
		return map;
	}

	@Override
	protected Map<String, Consumer<EnhancedMenu>> customStaticActions() {
		Map<String, Consumer<EnhancedMenu>> map = new HashMap<>();

		map.put(
			"[cancel]", (menu) -> {
				this.responded = true;
				this.response.accept(false);
				if (this.lastMenu != null) this.lastMenu.open(viewer);
			}
		);

		map.put(
			"[confirm]", (menu) -> {
				this.responded = true;
				this.response.accept(true);
				if (this.lastMenu != null) this.lastMenu.open(viewer);
			}
		);

		return map;
	}

	@Serializer(BaseSerializer.class) @Setter @AllArgsConstructor @NoArgsConstructor public static class Configuration extends ConfigMenuSettings {
		private String title = "Confirm Action";
		private int rows = 1;
		private int infoItemSlot = 4;

		@Serializer(BaseSerializer.class) private Map<String, StaticItem> staticItems = this.defaultStaticItems();

		private Map<String, StaticItem> defaultStaticItems() {
			Map<String, StaticItem> map = new HashMap<>();

			map.put(
				"2", new StaticItem(
					EnhancedItemBuilder.of(Material.REDSTONE)
						.displayName("<red>Cacnel")
						.build(), Arrays.asList("[cancel]")
				)
			);

			map.put(
				"6", new StaticItem(
					EnhancedItemBuilder.of(Material.EMERALD)
						.displayName("<green>Confirm")
						.build(), Arrays.asList("[confirm]")
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
		public Map<String, List<String>> dynamicSlots() {
			Map<String, List<String>> map = new HashMap<>();
			map.put("info", Arrays.asList(String.valueOf(this.infoItemSlot)));
			return map;
		}

		@Override
		protected Map<String, StaticItem> staticItems() {
			return this.staticItems;
		}
	}

}
