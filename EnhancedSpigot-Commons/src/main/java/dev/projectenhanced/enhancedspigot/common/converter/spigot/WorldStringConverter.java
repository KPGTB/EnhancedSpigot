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

package dev.projectenhanced.enhancedspigot.common.converter.spigot;

import dev.projectenhanced.enhancedspigot.common.converter.IStringConverter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;

public class WorldStringConverter implements IStringConverter<World> {

	@Override
	public String asString(World object, JavaPlugin plugin) {
		return object.getName();
	}

	@Override
	public World fromString(String string, JavaPlugin plugin) {
		return Bukkit.getWorld(string);
	}

	@Override
	public boolean canConvert(String string, JavaPlugin plugin) {
		return this.fromString(string, plugin) != null;
	}

	@Override
	public List<String> complete(String arg, CommandSender sender, JavaPlugin plugin) {
		return Bukkit.getWorlds()
			.stream()
			.map(World::getName)
			.filter(s -> s.startsWith(arg))
			.limit(30)
			.collect(Collectors.toList());
	}
}
