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

package dev.projectenhanced.enhancedspigot.locale.bridge;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotBridge implements IPlatformBridge {
	private final BukkitAudiences adventure;

	public SpigotBridge(JavaPlugin plugin) {
		this.adventure = BukkitAudiences.create(plugin);
	}

	@Override
	public void sendMessage(CommandSender sender, Component component) {
		this.getAudience(sender)
			.sendMessage(component);
	}

	@Override
	public void sendActionBar(Player player, Component component) {
		this.getAudience(player)
			.sendActionBar(component);
	}

	@Override
	public void sendTitle(Player player, Component title, Component subtitle) {
		this.getAudience(player)
			.showTitle(Title.title(title, subtitle));
	}

	@Override
	public void sendRestrictedMessage(String permission, Component component) {
		this.adventure.permission(permission)
					  .sendMessage(component);
	}

	@Override
	public void sendRestrictedActionBar(String permission, Component component) {
		this.adventure.permission(permission)
					  .sendActionBar(component);
	}

	@Override
	public void sendRestrictedTitle(String permission, Component title, Component subtitle) {
		this.adventure.permission(permission)
					  .showTitle(Title.title(title, subtitle));
	}

	@Override
	public void sendMessageAll(Component component) {
		this.adventure.all()
					  .sendMessage(component);
	}

	@Override
	public void sendActionBarAll(Component component) {
		this.adventure.all()
					  .sendActionBar(component);
	}

	@Override
	public void sendTitleAll(Component title, Component subtitle) {
		this.adventure.all()
					  .showTitle(Title.title(title, subtitle));
	}

	@Override
	public void close() {
		if (adventure != null) adventure.close();
	}

	@Override
	public Audience getAudience(CommandSender sender) {
		return sender instanceof Player ?
			   this.adventure.player((Player) sender) :
			   this.adventure.sender(sender);
	}
}
