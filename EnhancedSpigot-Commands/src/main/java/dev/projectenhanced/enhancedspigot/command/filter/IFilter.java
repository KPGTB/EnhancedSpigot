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

package dev.projectenhanced.enhancedspigot.command.filter;

import dev.projectenhanced.enhancedspigot.command.annotation.Filter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Filter to command's arguments. It can be used by annotation {@link Filter}
 */
public interface IFilter<T> {
	/**
	 * Filter method
	 *
	 * @param obj    Object that must be filtered
	 * @param plugin Instance of EnhancedPlugin
	 * @param sender Command sender
	 * @return true if object pass the test
	 */
	boolean filter(T obj, JavaPlugin plugin, CommandSender sender);

	/**
	 * Component message that should be sent when filter is not passed
	 *
	 * @param obj    Object that must be filtered
	 * @param plugin Instance of EnhancedPlugin
	 * @param sender Command sender
	 * @return Component that can be sent
	 */
	List<Component> notPassMessage(T obj, JavaPlugin plugin, CommandSender sender);

	/**
	 * Weight of the filter
	 *
	 * @return It declares which filter has the most priority to be sent on chat
	 */
	int weight();
}
