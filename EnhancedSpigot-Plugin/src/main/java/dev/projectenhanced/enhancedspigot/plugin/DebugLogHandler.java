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

package dev.projectenhanced.enhancedspigot.plugin;

import org.bukkit.plugin.Plugin;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class DebugLogHandler extends Handler {
	private static final String DEFAULT_DEBUG_PREFIX_FORMAT = "[%1$s-%2$s] %3$s";
	private static final String DEFAULT_DEBUG_LOG_PREFIX = "DEBUG";
	private final Plugin plugin;
	private final String pluginName;

	private DebugLogHandler(final Plugin plugin) {
		this.plugin = plugin;
		final String prefix = plugin.getDescription()
			.getPrefix();
		if (prefix != null) {
			this.pluginName = prefix;
		} else {
			this.pluginName = plugin.getDescription()
				.getName();
		}
	}

	/**
	 * Attaches a DebugLogHandler to the given plugin's logger.
	 *
	 * @param plugin the plugin to add debug logging for.
	 */
	public static void attachDebugLogger(final Plugin plugin) {
		plugin.getLogger()
			.addHandler(new DebugLogHandler(plugin));
	}

	@Override
	public void publish(final LogRecord record) {
		if (plugin.getLogger()
			.getLevel()
			.intValue() <= record.getLevel()
			.intValue() && record.getLevel()
			.intValue() < Level.INFO.intValue()) {
			record.setLevel(Level.INFO);
			record.setMessage(String.format(
				DEFAULT_DEBUG_PREFIX_FORMAT, pluginName,
				DEFAULT_DEBUG_LOG_PREFIX, record.getMessage()
			));
		}
	}

	@Override
	public void flush() {}

	@Override
	public void close() throws SecurityException {}
}
