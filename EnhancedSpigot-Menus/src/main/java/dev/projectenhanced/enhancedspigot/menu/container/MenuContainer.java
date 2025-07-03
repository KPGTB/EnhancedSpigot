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

package dev.projectenhanced.enhancedspigot.menu.container;

import dev.projectenhanced.enhancedspigot.menu.EnhancedMenu;
import dev.projectenhanced.enhancedspigot.menu.item.MenuItem;
import dev.projectenhanced.enhancedspigot.util.Pair;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Menu container contains specific box of items from GUI
 */
@Getter public class MenuContainer {
	private final EnhancedMenu menu;
	private final PagedMenuContainer pagedMenuContainer;
	private final Map<Pair<Integer, Integer>, MenuItem> items;

	private final int x;
	private final int y;
	private final int width;
	private final int height;

	/**
	 * Constructor for container that is part of Menu
	 *
	 * @param menu   Instance of EnhancedMenu
	 * @param x      X position in KGui (0-8)
	 * @param y      Y position in KGui (0-[KGui rows - 1])
	 * @param width  Width of container (1-9)
	 * @param height Height of container (1-[KGui rows])
	 */
	public MenuContainer(EnhancedMenu menu, int x, int y, int width, int height) {
		this.menu = menu;
		this.pagedMenuContainer = null;
		this.items = new HashMap<>();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		if ((x - 1) + width >= 9) {
			throw new IllegalArgumentException("Container is too wide!");
		}
		if (x < 0 || y < 0 || width < 1 || height < 0) {
			throw new IllegalArgumentException("Container is too small!");
		}
		if ((y - 1) + height >= this.menu.getRows()) {
			throw new IllegalArgumentException("Container is too big!");
		}

		this.fill(new MenuItem(new ItemStack(Material.AIR)));
	}

	/**
	 * Constructor for container that is part of PagedMenuContainer
	 *
	 * @param pagedMenuContainer Instance of PagedMenuContainer
	 */
	public MenuContainer(PagedMenuContainer pagedMenuContainer) {
		this.menu = null;
		this.pagedMenuContainer = pagedMenuContainer;
		this.x = pagedMenuContainer.getX();
		this.y = pagedMenuContainer.getY();
		this.width = pagedMenuContainer.getWidth();
		this.height = pagedMenuContainer.getHeight();
		this.items = new HashMap<>();

		this.fill(new MenuItem(new ItemStack(Material.AIR)));
	}

	/**
	 * Set item in container
	 *
	 * @param x    X position in container (0-[width-1])
	 * @param y    Y position in container (0-[height-1])
	 * @param item Instance of MenuItem
	 */
	public void setItem(int x, int y, MenuItem item) {
		if (x < 0 || x >= width) {
			throw new IllegalArgumentException("x is out of bounds!");
		}
		if (y < 0 || y >= height) {
			throw new IllegalArgumentException("y is out of bounds!");
		}
		this.items.put(new Pair<>(x, y), item);
	}

	/**
	 * Remove item from container
	 *
	 * @param x X position in container (0-[width-1])
	 * @param y Y position in container (0-[height-1])
	 */
	public void removeItem(int x, int y) {
		this.items.remove(new Pair<>(x, y));
	}

	/**
	 * Get item from container
	 *
	 * @param loc Item's location
	 * @return Item from container or null
	 */
	public MenuItem getItem(Pair<Integer, Integer> loc) {
		return this.items.get(loc);
	}

	/**
	 * Get item from container
	 *
	 * @param x X position in container (0-[width-1])
	 * @param y Y position in container (0-[height-1])
	 * @return Item from container or null
	 */
	public MenuItem getItem(int x, int y) {
		return this.getItem(new Pair<>(x, y));
	}

	/**
	 * Fill all slots in container with items
	 *
	 * @param item Instance of MenuItem
	 */
	public void fill(MenuItem item) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				this.setItem(x, y, item);
			}
		}
	}

	/**
	 * Fill all empty slots in container with items
	 *
	 * @param item Instance of MenuItem
	 */
	public void fillEmptySlots(MenuItem item) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (getItem(x, y) != null && !getItem(x, y).getItemStack()
					.getType()
					.equals(Material.AIR)) {
					continue;
				}
				this.setItem(x, y, item);
			}
		}
	}

	/**
	 * Calculate slot in gui from position in container
	 *
	 * @param x X position in container (0-[width-1])
	 * @param y Y position in container (0-[height-1])
	 * @return Slot in GUI
	 */
	public int getMenuLocFromContainerLoc(int x, int y) {
		int slot = 0;
		slot += ((this.y + y) * 9);
		slot += (this.x + x);
		return slot;
	}

	/**
	 * Calculate slot in gui from position in container
	 *
	 * @param loc Instance of GuiItemLocation
	 * @return Slot in GUI
	 */
	public int getMenuLocFromContainerLoc(Pair<Integer, Integer> loc) {
		return this.getMenuLocFromContainerLoc(loc.getFirst(), loc.getSecond());
	}

	/**
	 * Calculate location in container from position in gui
	 *
	 * @param slot Slot in GUI
	 * @return Instance of GuiItemLocation
	 */
	public Pair<Integer, Integer> getContainerLocFromMenuLoc(int slot) {
		int globalY = Math.floorDiv(slot, 9);
		int globalX = Math.floorMod(slot, 9);

		int containerX = globalX - this.x;
		int containerY = globalY - this.y;

		return new Pair<>(containerX, containerY);
	}
}
