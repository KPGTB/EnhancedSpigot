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

import java.util.ArrayList;
import java.util.List;

public class FloatStringConverter implements IStringConverter<Float> {
	@Override
	public String asString(Float object, JavaPlugin plugin) {
		return object.toString();
	}

	@Override
	public Float fromString(String string, JavaPlugin plugin) {
		return Float.parseFloat(string);
	}

	@Override
	public boolean canConvert(String string, JavaPlugin plugin) {
		try {
			this.fromString(string, plugin);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public List<String> complete(String arg, CommandSender sender, JavaPlugin plugin) {
		return new ArrayList<>();
	}
}
