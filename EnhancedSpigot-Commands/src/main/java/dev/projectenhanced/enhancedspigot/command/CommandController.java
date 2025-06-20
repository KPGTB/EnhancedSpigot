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

package dev.projectenhanced.enhancedspigot.command;

import dev.projectenhanced.enhancedspigot.util.ReflectionUtil;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;

/**
 * CommandManager handles all commands in plugin
 */
public class CommandController {
	private final JavaPlugin plugin;
	private final CommandLocale locale;
	private final File jarFile;
	private final String pluginTag;

	/**
	 * Constructor of manager
	 *
	 * @param plugin    Instance od JavaPlugin
	 * @param jarFile   JAR file of plugin
	 * @param pluginTag Tag of plugin
	 */
	public CommandController(JavaPlugin plugin, CommandLocale locale, File jarFile, String pluginTag) {
		this.plugin = plugin;
		this.locale = locale;
		this.jarFile = jarFile;
		this.pluginTag = pluginTag;
	}

	/**
	 * Init commands file
	 */
	public void init() {
		File dataFolder = this.plugin.getDataFolder();
		dataFolder.mkdirs();
		File commandsFile = new File(dataFolder, "commands.yml");
		if (!commandsFile.exists()) {
			TryCatchUtil.tryRun(commandsFile::createNewFile);
		}
	}

	/**
	 * Register all commands from package
	 *
	 * @param commandsPackage Package with commands
	 */
	public void registerCommands(String commandsPackage) {
		Field f = TryCatchUtil.tryAndReturn(() -> Bukkit.getServer()
														.getClass()
														.getDeclaredField(
															"commandMap"));
		CommandMap commandMap = (CommandMap) TryCatchUtil.tryAndReturn(() -> f.get(
			Bukkit.getServer()));

		ReflectionUtil.getAllClassesInPackage(
						  jarFile,
						  commandsPackage,
						  EnhancedCommand.class
					  )
					  .forEach(clazz -> {
						  String[] groupPath = clazz.getName()
													.split("\\.");
						  StringBuilder finalPath = new StringBuilder();

						  for (int i = commandsPackage.split("\\.").length; i < (groupPath.length - 1); i++) {
							  finalPath.append(groupPath[i])
									   .append(".");
						  }

						  if (finalPath.length() > 0)
							  finalPath.deleteCharAt(finalPath.length() - 1);

						  EnhancedCommand command = (EnhancedCommand) TryCatchUtil.tryAndReturn(
							  () -> clazz.getDeclaredConstructor(
											 JavaPlugin.class,
											 CommandLocale.class,
											 String.class
										 )
										 .newInstance(
											 plugin,
											 this.locale,
											 finalPath.toString()
										 ));
						  TryCatchUtil.tryRun(command::prepareCommand);
						  commandMap.register(pluginTag, command);
					  });

		f.setAccessible(false);
	}
}
