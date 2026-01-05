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

package dev.projectenhanced.enhancedspigot.common.filter;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public interface IFilter<T> {
	/**
	 * Filter method
	 *
	 * @param obj    Object that is filtered
	 * @param plugin Instance of JavaPlugin
	 * @param source Command sender / Player
	 * @return true if object pass the test
	 */
	boolean filter(T obj, JavaPlugin plugin, CommandSender source);

	/**
	 * Message which should be sent when filter doesn't pass
	 *
	 * @param obj    Object that must be filtered
	 * @param plugin Instance of EnhancedPlugin
	 * @param source Command sender / Player
	 * @return List of messages that can be sent
	 */
	default List<String> notPassMessage(T obj, JavaPlugin plugin, CommandSender source) {
		return new ArrayList<>();
	}

	/**
	 * Weight of the filter
	 *
	 * @return It declares which filter has the most priority to be sent on chat
	 */
	int weight();
}

