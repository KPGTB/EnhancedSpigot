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

package dev.projectenhanced.enhancedspigot.menu.item;

import dev.projectenhanced.enhancedspigot.util.item.EnhancedItemBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

/**
 * MenuItem represents item in EnhancedMenu
 */
@Getter @Setter public class MenuItem {
	private ItemStack itemStack;
	private BiConsumer<InventoryClickEvent, ClickLocation> clickAction;

	/**
	 * Constructor of GuiItem
	 *
	 * @param itemStack ItemStack instance
	 */
	public MenuItem(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	/**
	 * Constructor of GuiItem
	 *
	 * @param builder ItemBuilder instance
	 * @since 1.5.0
	 */
	public MenuItem(EnhancedItemBuilder builder) {
		itemStack = builder.build();
	}

	/**
	 * @return ItemBuilder
	 */
	public EnhancedItemBuilder getBuilder() {
		return new EnhancedItemBuilder(itemStack);
	}

	/**
	 * @param builder ItemBuilder
	 */
	public void fromBuilder(EnhancedItemBuilder builder) {this.itemStack = builder.build();}

	public enum ClickLocation {TOP, BOTTOM, OUTSIDE}
}
