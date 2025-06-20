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

package dev.projectenhanced.enhancedspigot.command.parser.java;

import dev.projectenhanced.enhancedspigot.command.parser.IArgumentParser;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooleanParser implements IArgumentParser<Boolean> {
	@Override
	public Boolean convert(String param, JavaPlugin plugin) {
		return param.equalsIgnoreCase("true") || param.equalsIgnoreCase("yes");
	}

	@Override
	public boolean canConvert(String param, JavaPlugin plugin) {
		return param != null && (param.equalsIgnoreCase("true") || param.equalsIgnoreCase("yes") || param.equalsIgnoreCase(
			"false") || param.equalsIgnoreCase("no"));
	}

	@Override
	public List<String> complete(String arg, CommandSender sender, JavaPlugin plugin) {
		return Stream.of("true", "yes", "false", "no")
					 .filter(m -> m.startsWith(arg))
					 .collect(Collectors.toList());
	}
}
