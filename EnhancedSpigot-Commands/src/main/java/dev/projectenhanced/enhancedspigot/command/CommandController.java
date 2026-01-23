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

package dev.projectenhanced.enhancedspigot.command;

import dev.projectenhanced.enhancedspigot.common.IDependencyProvider;
import dev.projectenhanced.enhancedspigot.common.converter.StringConverterRegistry;
import dev.projectenhanced.enhancedspigot.common.stereotype.Controller;
import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
import dev.projectenhanced.enhancedspigot.util.ReflectionUtil;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import dev.projectenhanced.enhancedspigot.util.time.EnhancedTime;
import dev.projectenhanced.enhancedspigot.util.time.TimeStringConverter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * CommandManager handles all commands in plugin
 */
public class CommandController extends Controller {
	private final CommandLocale locale;
	private final File jarFile;
	private final String pluginTag;
	private final List<Command> registeredCommands;

	public CommandController(JavaPlugin plugin, CommandLocale locale) {
		super(plugin);
		this.locale = locale;
		this.jarFile = ReflectionUtil.getJarFile(plugin);
		this.pluginTag = this.generateTag();
		this.registeredCommands = new ArrayList<>();

		StringConverterRegistry.getInstance()
			.registerConverter(EnhancedTime.class, new TimeStringConverter());
	}

	public CommandController(DependencyProvider provider, CommandLocale locale) {
		super(provider);
		this.locale = locale;
		this.jarFile = ReflectionUtil.getJarFile(this.plugin);
		this.pluginTag = this.generateTag();
		this.registeredCommands = new ArrayList<>();

		StringConverterRegistry.getInstance()
			.registerConverter(EnhancedTime.class, new TimeStringConverter());
	}

	protected String generateTag() {
		return this.plugin.getName()
			.toLowerCase()
			.replace("-", "_");
	}

	/**
	 * Register all commands from package
	 *
	 * @param commandsPackage Package with commands
	 */
	public void registerCommands(String commandsPackage) {
		this.applyInCommandMap(commandMap -> {
			ReflectionUtil.getAllClassesInPackage(jarFile, commandsPackage, EnhancedCommand.class)
				.forEach(clazz -> {
					String[] groupPath = clazz.getName()
						.split("\\.");
					StringBuilder finalPath = new StringBuilder();

					for (int i = commandsPackage.split("\\.").length; i < (groupPath.length - 1); i++) {
						finalPath.append(groupPath[i])
							.append(".");
					}

					if (!finalPath.isEmpty()) finalPath.deleteCharAt(finalPath.length() - 1);

					EnhancedCommand command;
					if (this.dependencyProvider != null) {
						command = (EnhancedCommand) TryCatchUtil.tryAndReturn(() -> clazz.getDeclaredConstructor(IDependencyProvider.class, CommandLocale.class, String.class)
							.newInstance(this.dependencyProvider, this.locale, finalPath.toString()));
					} else {
						command = (EnhancedCommand) TryCatchUtil.tryAndReturn(() -> clazz.getDeclaredConstructor(JavaPlugin.class, CommandLocale.class, String.class)
							.newInstance(this.plugin, this.locale, finalPath.toString()));
					}

					TryCatchUtil.tryRun(command::prepareCommand);
					commandMap.register(pluginTag, command);
					this.registeredCommands.add(command);
				});
		});
	}

	public void updateCommandLocale(CommandLocale newLocale) {
		this.locale.update(newLocale);
	}

	private void applyInCommandMap(Consumer<SimpleCommandMap> consumer) {
		Field f = TryCatchUtil.tryAndReturn(() -> Bukkit.getServer()
			.getClass()
			.getDeclaredField("commandMap"));
		f.setAccessible(true);
		SimpleCommandMap commandMap = (SimpleCommandMap) TryCatchUtil.tryAndReturn(() -> f.get(Bukkit.getServer()));
		consumer.accept(commandMap);
		f.setAccessible(false);
	}

	@Override
	public void start() {
		File dataFolder = this.plugin.getDataFolder();
		dataFolder.mkdirs();
		File commandsFile = new File(dataFolder, "commands.yml");
		if (!commandsFile.exists()) TryCatchUtil.tryRun(commandsFile::createNewFile);
	}

	@Override
	public void reload() {}

	@SuppressWarnings("unchecked")
	@Override
	public void close() {
		this.applyInCommandMap(commandMap -> {
			Field field = TryCatchUtil.tryAndReturn(() -> SimpleCommandMap.class.getDeclaredField("knownCommands"));
			field.setAccessible(true);
			Map<String, Command> knownCommands = (Map<String, Command>) TryCatchUtil.tryAndReturn(() -> field.get(commandMap));
			List<String> toRemove = new ArrayList<>();

			knownCommands.forEach((name, command) -> {
				if (this.registeredCommands.contains(command)) {
					System.out.println("Unregistering command: " + name);
					toRemove.add(name);
				}
			});

			toRemove.forEach(knownCommands::remove);
			field.setAccessible(false);
		});
	}
}
