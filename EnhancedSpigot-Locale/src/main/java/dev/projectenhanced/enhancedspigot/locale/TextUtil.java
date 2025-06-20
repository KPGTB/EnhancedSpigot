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

package dev.projectenhanced.enhancedspigot.locale;

import dev.projectenhanced.enhancedspigot.util.SemanticVersion;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class TextUtil {
	/**
	 * Convert Component to String
	 *
	 * @param component Component
	 * @return String from component
	 */
	public static String convertComponentToString(Component component) {
		boolean isHexSupport = SemanticVersion.getMinecraftVersion()
											  .isNewerOrEqual("1.16");

		if (isHexSupport) {
			return LegacyComponentSerializer.builder()
											.hexColors()
											.useUnusualXRepeatedCharacterHexFormat()
											.build()
											.serialize(component);
		}
		return LegacyComponentSerializer.builder()
										.build()
										.serialize(component);
	}

	/**
	 * Convert mini message string to formatted string
	 *
	 * @param mm           Mini message string
	 * @param placeholders Placeholders
	 * @return formatted string
	 */
	public static String convertMmToString(String mm, TagResolver... placeholders) {
		return convertComponentToString(MiniMessage.miniMessage()
												   .deserialize(mm, placeholders));
	}

	/**
	 * Convert legacy string to component
	 *
	 * @param s Legacy string
	 * @return TextComponent from string
	 */
	public static TextComponent convertLegacyStringToComponent(String s) {
		boolean isHexSupport = SemanticVersion.getMinecraftVersion()
											  .isNewerOrEqual("1.16");

		if (isHexSupport) {
			return LegacyComponentSerializer.builder()
											.hexColors()
											.useUnusualXRepeatedCharacterHexFormat()
											.build()
											.deserialize(s);
		}
		return LegacyComponentSerializer.builder()
										.build()
										.deserialize(s);
	}

	public static void modifyItem(ItemStack is, TagResolver... placeholders) {
		modifyItem(is, null, placeholders);
	}

	public static void modifyItem(ItemStack is, Player player, TagResolver... placeholders) {
		if (is == null || is.getType() == Material.AIR || !is.hasItemMeta()) return;
		ItemMeta meta = is.getItemMeta();
		if (meta.hasDisplayName()) {
			String display = addPAPI(meta.getDisplayName(), player);
			meta.setDisplayName(convertMmToString(display, placeholders));
		}

		if (meta.hasLore()) {
			List<String> lore = meta.getLore()
									.stream()
									.map(s -> addPAPI(s, player))
									.map(s -> convertMmToString(s, placeholders))
									.collect(Collectors.toList());
			meta.setLore(lore);
		}

		is.setItemMeta(meta);
	}

	private static String addPAPI(String s, Player player) {
		if (Bukkit.getPluginManager()
				  .isPluginEnabled("PlaceholdersAPI") && player != null) {
			return PlaceholderAPI.setPlaceholders(player, s);
		}
		return s;
	}
}
