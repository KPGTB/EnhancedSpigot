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

package dev.projectenhanced.enhancedspigot.util.item;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ItemUtil {
	/**
	 * Compare ItemStacks ignoring some namespace keys
	 *
	 * @param is1    First ItemStack
	 * @param is2    Second ItemStack
	 * @param ignore List of NamespacedKey to ignore
	 * @return true if ItemStack are similar or false if items are null, items are AIR, items don't have meta, or items aren't similar
	 */
	public static boolean compareWithoutPDC(ItemStack is1, ItemStack is2, List<NamespacedKey> ignore) {
		if (is1 == null || is2 == null) return false;

		is1 = is1.clone();
		is2 = is2.clone();

		if (is1.getType()
			.equals(Material.AIR) || is2.getType()
			.equals(Material.AIR)) return false;

		ItemMeta im1 = is1.getItemMeta();
		ItemMeta im2 = is2.getItemMeta();

		if (im1 == null || im2 == null) return false;

		PersistentDataContainer pdc1 = im1.getPersistentDataContainer();
		PersistentDataContainer pdc2 = im2.getPersistentDataContainer();

		ignore.forEach(key -> {
			pdc1.remove(key);
			pdc2.remove(key);
		});

		is1.setItemMeta(im1);
		is2.setItemMeta(im2);

		return is1.isSimilar(is2);
	}

	/**
	 * Compare ItemStacks ignoring some namespace keys
	 *
	 * @param is1    First ItemStack
	 * @param is2    Second ItemStack
	 * @param ignore List of NamespacedKey to ignore
	 * @return true if ItemStack are similar or false if items are null, items are AIR, items don't have meta, or items aren't similar
	 * @since 2.2.0
	 */
	public static boolean compareWithoutPDC(ItemStack is1, ItemStack is2, NamespacedKey... ignore) {
		return compareWithoutPDC(
			is1, is2, Arrays.stream(ignore)
				.collect(Collectors.toList())
		);
	}

	/**
	 * Give items to player and drop items that won't fit
	 *
	 * @param player Player that should get items
	 * @param is     Items
	 */
	public static void giveItemToPlayer(Player player, ItemStack... is) {
		PlayerInventory inv = player.getInventory();
		HashMap<Integer, ItemStack> lostItems = inv.addItem(is);
		if (lostItems.isEmpty()) return;
		Location loc = player.getLocation();
		lostItems.values()
			.forEach(item -> {
				loc.getWorld()
					.dropItemNaturally(loc, item);
			});
	}

	/**
	 * Give items to player and drop items that won't fit
	 *
	 * @param player Player that should get items
	 * @param is     Items
	 * @since 2.2.0
	 */
	public static void giveItemToPlayer(Player player, List<ItemStack> is) {
		giveItemToPlayer(player, is.toArray(new ItemStack[0]));
	}

	public static boolean validateItem(ItemStack is) {
		return is != null && !is.getType()
			.isAir();
	}

	public static boolean validateBlock(Block block) {
		return block != null && !block.getType()
			.isAir();
	}
}
