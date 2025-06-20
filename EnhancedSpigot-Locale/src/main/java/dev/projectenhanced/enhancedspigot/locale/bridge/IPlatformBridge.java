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
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface IPlatformBridge {
	void sendMessage(CommandSender sender, Component component);

	void sendActionBar(Player player, Component component);

	void sendTitle(Player player, Component title, Component subtitle);

	void sendRestrictedMessage(String permission, Component component);

	void sendRestrictedActionBar(String permission, Component component);

	void sendRestrictedTitle(String permission, Component title, Component subtitle);

	void sendMessageAll(Component component);

	void sendActionBarAll(Component component);

	void sendTitleAll(Component title, Component subtitle);

	Audience getAudience(CommandSender sender);

	void close();
}
