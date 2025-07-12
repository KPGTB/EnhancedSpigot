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
import dev.projectenhanced.enhancedspigot.menu.impl.menu.config.WriteMenuSettings;
import dev.projectenhanced.enhancedspigot.util.SchedulerUtil;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.function.Consumer;

public class EnhancedWriteMenu implements Listener {
	private final JavaPlugin plugin;
	private final WriteMenuSettings settings;
	private final EnhancedMenu lastMenu;
	private final Player viewer;
	private final Consumer<String> response;
	private boolean responded;

	public EnhancedWriteMenu(JavaPlugin plugin, WriteMenuSettings settings, EnhancedMenu lastMenu, Player player, Consumer<String> response) {
		this.plugin = plugin;
		this.settings = settings;
		this.lastMenu = lastMenu;
		this.viewer = player;
		this.response = response;
		this.responded = false;
	}

	/**
	 * Open Menu to player
	 */
	public void open() {
		new AnvilGUI.Builder().onClose(stateSnapshot -> {
				if (responded) return;
				response.accept(null);
				responded = true;
				if (this.lastMenu != null) SchedulerUtil.runTaskLater(
					this.plugin, (task) -> {
						this.lastMenu.open(stateSnapshot.getPlayer());
					}, 3
				);
			})
			.onClick((slot, stateSnapshot) -> {
				if (!slot.equals(AnvilGUI.Slot.OUTPUT)) {
					return Arrays.asList(AnvilGUI.ResponseAction.close());
				}

				response.accept(stateSnapshot.getText());
				responded = true;
				if (lastMenu != null) {
					lastMenu.open(stateSnapshot.getPlayer());
				}
				return Arrays.asList(AnvilGUI.ResponseAction.close());
			})
			.text(this.settings.getPlaceholder(this.viewer))
			.itemLeft(new ItemStack(Material.PAPER))
			.title(this.settings.getTitle(this.viewer))
			.plugin(this.plugin)
			.open(viewer);
	}
}
