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

package dev.projectenhanced.enhancedspigot.command.parser;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Interface that is used to parse String to Class
 *
 * @param <T> Class that should be parsed
 */
public interface IArgumentParser<T> {
	/**
	 * Convert String to Class
	 *
	 * @param param  String that should be converted
	 * @param plugin Instance of JavaPlugin
	 * @return Converted string
	 */
	T convert(String param, JavaPlugin plugin);

	/**
	 * Check if String can be converted to Class
	 *
	 * @param param  String that should be checked
	 * @param plugin Instance of JavaPlugin
	 * @return true if string can be converted or false if can't
	 */
	boolean canConvert(String param, JavaPlugin plugin);

	/**
	 * Prepare list of Strings that can be used to tab completer
	 *
	 * @param arg    Argument that is written by player
	 * @param sender CommandSender
	 * @param plugin Instance of JavaPlugin
	 * @return List of strings to tab completer. It can be empty.
	 */
	List<String> complete(String arg, CommandSender sender, JavaPlugin plugin);
}
