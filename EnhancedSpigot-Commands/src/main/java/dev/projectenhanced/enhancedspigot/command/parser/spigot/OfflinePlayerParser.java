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

package dev.projectenhanced.enhancedspigot.command.parser.spigot;

import dev.projectenhanced.enhancedspigot.command.parser.IArgumentParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OfflinePlayerParser implements IArgumentParser<OfflinePlayer> {
	@Override
	public OfflinePlayer convert(String param, JavaPlugin plugin) {
		return Bukkit.getOfflinePlayer(param);
	}

	@Override
	public boolean canConvert(String param, JavaPlugin plugin) {
		return convert(param, plugin) != null;
	}

	@Override
	public List<String> complete(String arg, CommandSender sender, JavaPlugin plugin) {
		return Arrays.stream(Bukkit.getOfflinePlayers())
					 .map(OfflinePlayer::getName)
					 .filter(s -> s.startsWith(arg))
					 .limit(30)
					 .collect(Collectors.toList());
	}
}
