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

package dev.projectenhanced.enhancedspigot.common.converter.java;

import dev.projectenhanced.enhancedspigot.common.converter.IStringConverter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanStringConverter implements IStringConverter<Boolean> {
	@Override
	public String asString(Boolean object, JavaPlugin plugin) {
		return object.toString();
	}

	@Override
	public Boolean fromString(String string, JavaPlugin plugin) {
		return string.equalsIgnoreCase("true") || string.equalsIgnoreCase("yes");
	}

	@Override
	public boolean canConvert(String string, JavaPlugin plugin) {
		return string != null && (string.equalsIgnoreCase("true") || string.equalsIgnoreCase("yes") || string.equalsIgnoreCase("false") || string.equalsIgnoreCase("no"));
	}

	@Override
	public List<String> complete(String arg, CommandSender sender, JavaPlugin plugin) {
		return Stream.of("true", "yes", "false", "no")
			.filter(m -> m.startsWith(arg))
			.collect(Collectors.toList());
	}
}
