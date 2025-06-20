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

import dev.projectenhanced.enhancedspigot.locale.bridge.IPlatformBridge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public class LocaleObject {
	private final IPlatformBridge bridge;
	private final List<String> rawText;

	public LocaleObject(IPlatformBridge bridge, String rawText) {
		this.bridge = bridge;
		this.rawText = Arrays.asList(rawText);
	}

	public LocaleObject(IPlatformBridge bridge, String[] rawText) {
		this.bridge = bridge;
		this.rawText = Arrays.asList(rawText);
	}

	public Object toConfig() {
		return this.rawText.size() == 1 ?
			   this.rawText.get(0) :
			   this.rawText;
	}

	public LocaleObject addPAPI(Player player) {
		if (Bukkit.getPluginManager()
				  .isPluginEnabled("PlaceholdersAPI")) {
			List<String> copy = new ArrayList<>(this.rawText);
			this.rawText.clear();
			;
			copy.forEach(s -> this.rawText.add(PlaceholderAPI.setPlaceholders(player, s)));
		}
		return this;
	}

	public void to(CommandSender sender, SendType type, TagResolver... placeholders) {
		List<Component> components = this.asComponents(placeholders);
		switch (type) {
			case CHAT:
				components.forEach(comp -> this.bridge.sendMessage(sender, comp));
				break;
			case ACTIONBAR:
				components.forEach(comp -> this.bridge.sendActionBar((Player) sender, comp));
				break;
			case TITLE:
				this.bridge.sendTitle((Player) sender, components.get(0), components.size() > 1 ?
																		  components.get(1) :
																		  Component.text(""));
				break;
		}
	}

	public List<Component> asComponents(TagResolver... placeholders) {
		return this.rawText.stream()
						   .map(s -> MiniMessage.miniMessage()
												.deserialize(s, placeholders))
						   .collect(Collectors.toList());
	}

	public Component asComponent(TagResolver... placeholders) {
		List<Component> components = this.asComponents(placeholders);
		return components.isEmpty() ?
			   null :
			   components.get(0);
	}

	public List<String> asStrings(TagResolver... placeholders) {
		return this.rawText.stream()
						   .map(s -> TextUtil.convertMmToString(s, placeholders))
						   .collect(Collectors.toList());
	}

	public String asString(TagResolver... placeholders) {
		List<String> strings = this.asStrings(placeholders);
		return strings.isEmpty() ?
			   null :
			   strings.get(0);
	}

	public enum SendType {
		CHAT, ACTIONBAR, TITLE
	}
}
