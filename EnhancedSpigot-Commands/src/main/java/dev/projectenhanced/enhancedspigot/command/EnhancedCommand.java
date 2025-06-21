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

import dev.projectenhanced.enhancedspigot.command.annotation.*;
import dev.projectenhanced.enhancedspigot.command.filter.FilterWrapper;
import dev.projectenhanced.enhancedspigot.command.filter.IFilter;
import dev.projectenhanced.enhancedspigot.command.parser.CommandArgumentRegistry;
import dev.projectenhanced.enhancedspigot.command.parser.IArgumentParser;
import dev.projectenhanced.enhancedspigot.locale.LocaleObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Abstract class that handles process of creating commands
 */
public abstract class EnhancedCommand extends Command {
	private final JavaPlugin plugin;
	private final String groupPath;
	private final CommandLocale language;
	private final CommandArgumentRegistry parser;
	private final File commandsFile;
	private final YamlConfiguration commandsConf;
	private final Map<CommandPath, List<CommandInfo>> subCommands;
	private String cmdName;

	public EnhancedCommand(JavaPlugin plugin, CommandLocale locale, String groupPath) {
		super("");

		this.plugin = plugin;
		this.language = locale;
		this.parser = CommandArgumentRegistry.getInstance();
		this.groupPath = groupPath;

		this.commandsFile = new File(plugin.getDataFolder(), "commands.yml");
		this.commandsConf = YamlConfiguration.loadConfiguration(
			this.commandsFile);

		this.subCommands = new LinkedHashMap<>();
	}

	//
	//  Creating command
	//

	/**
	 * Prepare commands from that class
	 */
	@SuppressWarnings("unchecked")
	public final void prepareCommand() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
		this.cmdName = getClass().getSimpleName()
			.toLowerCase()
			.replace("command", "");
		super.setName(this.cmdName);

		Description descriptionAnn = getClass().getDeclaredAnnotation(
			Description.class);
		String description = descriptionAnn != null ?
			descriptionAnn.value() :
			"Command created using EnhancedCommand";
		description = String.valueOf(
			this.getCommandInfo("description", description));
		super.setDescription(description);

		Aliases aliasesAnn = getClass().getDeclaredAnnotation(Aliases.class);
		List<String> aliases = aliasesAnn != null ?
			Arrays.asList(aliasesAnn.value()) :
			new ArrayList<>();
		aliases = (List<String>) this.getCommandInfo("aliases", aliases);
		super.setAliases(aliases);

		Permission customGlobalPermissionAnn = getClass().getDeclaredAnnotation(
			Permission.class);
		String customGlobalPermission = customGlobalPermissionAnn != null ?
			customGlobalPermissionAnn.value() :
			null;
		boolean globalWithoutPermission = getClass().getDeclaredAnnotation(
			WithoutPermission.class) != null;

		this.setCommandInfo("command", "/" + cmdName);
		this.setCommandInfo("description", description);
		this.setCommandInfo("aliases", aliases);

		scanClass(
			new CommandPath(), this.getClass(), this, customGlobalPermission,
			globalWithoutPermission
		);
	}

	private void scanClass(CommandPath path, Class<?> clazz, Object invoker, String customGlobalPermission, boolean globalWithoutPermission) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		for (Method method : clazz.getDeclaredMethods()) {
			// === Check if it is command
			if (method.isSynthetic()) {
				continue;
			}
			if (method.getDeclaredAnnotation(Ignore.class) != null) {
				continue;
			}
			if (method.getParameterCount() == 0) {
				continue;
			}

			// === Name & Path
			String name = method.getName()
				.toLowerCase();

			CommandPath newPath = path.clone();
			boolean mainCommand = method.getDeclaredAnnotation(
				MainCommand.class) != null;
			String methodPath = mainCommand ?
				"" :
				name;
			newPath.add(methodPath);
			if (!this.subCommands.containsKey(newPath)) {
				this.subCommands.put(newPath, new ArrayList<>());
			}
			List<CommandInfo> commands = this.subCommands.get(newPath);

			// === Description
			Description descriptionAnn = method.getDeclaredAnnotation(
				Description.class);
			String description = descriptionAnn != null ?
				descriptionAnn.value() :
				"Subcommand created using EnhancedSpigot";

			// === Permissions
			Permission customPermissionAnn = method.getDeclaredAnnotation(
				Permission.class);
			String customPermission = customPermissionAnn != null ?
				customPermissionAnn.value() :
				null;
			boolean withoutPermission = method.getDeclaredAnnotation(
				WithoutPermission.class) != null;

			List<String> permissions = new ArrayList<>();
			if (!withoutPermission && !globalWithoutPermission) {
				if (mainCommand) {
					permissions.add(
						("command." + this.groupPath + "." + this.cmdName + "." + newPath.getPermissionStr() + "." + name).replace(
							"..", "."));
				} else {
					permissions.add(
						("command." + this.groupPath + "." + this.cmdName + "." + newPath.getPermissionStr()).replace(
							"..", "."));
				}
				CommandPath permissionsPath = new CommandPath();
				int permissionsPathMaxLength = newPath.getPath().length;
				if (!mainCommand) {
					permissionsPathMaxLength--;
				}
				for (int i = 0; i < permissionsPathMaxLength; i++) {
					permissionsPath.add(newPath.getPath()[i]);
					permissions.add(
						("command." + this.groupPath + "." + this.cmdName + "." + permissionsPath.getPermissionStr() + ".*").replace(
							"..", "."));
				}
				permissions.add(
					("command." + this.groupPath + "." + this.cmdName + ".*").replace(
						"..", "."));
				if (!this.groupPath.isEmpty()) {
					permissions.add(
						("command." + this.groupPath + ".*").replace(
							"..",
							"."
						));
				}
				permissions.add("command.*");

				if (customGlobalPermission != null) {
					permissions.add(customGlobalPermission);
				}
				if (customPermission != null) {
					permissions.add(customPermission);
				}
			}

			// === Source
			Parameter[] parameters = method.getParameters();
			Parameter sourceParam = parameters[0];
			Class<?> sourceClass = sourceParam.getType();

			boolean playerRequired = Player.class.equals(sourceClass);
			if (!playerRequired && !CommandSender.class.equals(sourceClass)) {
				continue;
			}

			Filter sourceFiltersAnn = sourceParam.getDeclaredAnnotation(
				Filter.class);
			FilterWrapper sourceFilters = null;
			if (sourceFiltersAnn != null) {
				sourceFilters = new FilterWrapper(sourceFiltersAnn);
			}

			// === Arguments
			List<CommandArg> args = new LinkedList<>();
			for (int i = 1; i < parameters.length; i++) {
				Parameter param = parameters[i];

				String paramName = param.getName();
				Class<?> paramClass = param.getType();

				Filter paramFiltersAnn = param.getDeclaredAnnotation(
					Filter.class);
				FilterWrapper filters = null;
				if (paramFiltersAnn != null) {
					filters = new FilterWrapper(paramFiltersAnn);
				}

				Parser customParserAnn = param.getDeclaredAnnotation(
					Parser.class);
				Class<? extends IArgumentParser<?>> customParser = null;
				if (customParserAnn != null) {
					customParser = customParserAnn.value();
				}

				CommandArg arg = new CommandArg(
					paramClass, customParser, filters, paramName);
				args.add(arg);
			}

			// === Endless
			boolean endless = false;
			if (!args.isEmpty()) {
				Parameter lastParam = parameters[parameters.length - 1];
				if (lastParam.getType()
					.equals(String.class) && lastParam.getDeclaredAnnotation(
					LongString.class) != null) {
					endless = true;
					String paramName = lastParam.getName();
					String newParamName = "[" + paramName + "]";
					args.get(args.size() - 1)
						.setName(newParamName);
				}
			}

			// === Hidden
			boolean hidden = method.getDeclaredAnnotation(Hidden.class) != null;

			// === Save
			CommandInfo info = new CommandInfo(
				newPath, permissions, playerRequired, sourceFilters, args,
				invoker, method, endless, hidden, description
			);
			commands.add(info);

			String variantName = getCommandStr(info);
			info.setDescription(String.valueOf(
				this.getVariantInfo(
					variantName, "description",
					info.getDescription()
				)));

			setVariantInfo(variantName, "permissions", permissions);
			setVariantInfo(variantName, "onlyPlayer", playerRequired);
			setVariantInfo(variantName, "hidden", hidden);
		}

		// === Scan another classes
		for (Class<?> c : clazz.getDeclaredClasses()) {
			CommandPath newPath = path.clone();
			newPath.add(c.getSimpleName()
				.toLowerCase());

			scanClass(
				newPath, c, c.getDeclaredConstructor(clazz)
					.newInstance(invoker), customGlobalPermission,
				globalWithoutPermission
			);
		}

		saveCommandsFile();
	}

	//
	//  Overrides
	//

	@Override
	public final boolean execute(CommandSender sender, String commandLabel, String[] args) {
		List<CommandPath> possiblePaths = new ArrayList<>();

		this.subCommands.keySet()
			.forEach(path -> {
				String[] pathArr = path.getPath();
				if (args.length < pathArr.length) {
					return;
				}
				for (int i = 0; i < pathArr.length; i++) {
					if (!args[i].equalsIgnoreCase(pathArr[i])) {
						return;
					}
				}
				possiblePaths.add(path);
			});

		boolean found = false;
		CommandArg notPassArg = null;
		Object notPassObj = null;

		for (CommandPath path : possiblePaths) {
			List<CommandInfo> commands = this.subCommands.get(path);
			List<String> fixedArgs = new LinkedList<>(Arrays.asList(args)
				.subList(path.getPath().length, args.length));

			for (CommandInfo command : commands) {
				if (!command.isEndless() && command.getArgs()
					.size() != fixedArgs.size()) {
					continue;
				}
				if (command.isEndless() && command.getArgs()
					.size() > fixedArgs.size()) {
					continue;
				}

				boolean correctTypes = true;
				for (int i = 0; i < command.getArgs()
					.size(); i++) {
					if (command.isEndless() && command.getArgs()
						.size() == i + 1) {
						break;
					}
					CommandArg arg = command.getArgs()
						.get(i);

					if (arg.hasCustomParser()) {
						if (!arg.getCustomParser()
							.canConvert(fixedArgs.get(i), plugin)) {
							correctTypes = false;
							break;
						}
					} else {
						if (!parser.canConvert(
							fixedArgs.get(i), arg.getClazz(), plugin)) {
							correctTypes = false;
							break;
						}
					}

				}
				if (!correctTypes) {
					continue;
				}

				if (command.isPlayerRequired() && !(sender instanceof Player)) {
					found = true;
					continue;
				}

				if (!hasPermission(sender, command)) {
					this.language.getNoPermission()
						.to(sender, LocaleObject.SendType.CHAT);
					return false;
				}

				Object[] finalArgs = new Object[command.getArgs()
					.size() + 1];
				finalArgs[0] = sender;
				for (int i = 1; i < finalArgs.length; i++) {
					int j = i - 1;
					if (command.isEndless() && finalArgs.length == i + 1) {
						List<String> longStr = new ArrayList<>();
						for (int k = j; k < fixedArgs.size(); k++) {
							longStr.add(fixedArgs.get(k));
						}
						finalArgs[i] = String.join(" ", longStr);
						break;
					}
					CommandArg arg = command.getArgs()
						.get(j);
					if (arg.hasCustomParser()) {
						finalArgs[i] = arg.getCustomParser()
							.convert(fixedArgs.get(j), plugin);
					} else {
						finalArgs[i] = parser.convert(
							fixedArgs.get(j), arg.getClazz(), plugin);
					}

				}

				boolean passSourceFilters = passFilters(
					command.getSourceFilters(), sender, sender);
				if (!passSourceFilters) {
					found = true;
					notPassArg = new CommandArg(
						null, null, command.getSourceFilters(), "");
					notPassObj = sender;
					continue;
				}

				boolean passArgsFilters = true;
				for (int j = 1; j < finalArgs.length; j++) {
					CommandArg arg = command.getArgs()
						.get(j - 1);
					passArgsFilters = passFilters(
						arg.getFilters(), finalArgs[j], sender);
					if (!passArgsFilters) {
						found = true;
						notPassArg = arg;
						notPassObj = finalArgs[j];
						break;
					}
				}

				if (!passArgsFilters) {
					continue;
				}

				try {
					command.getMethod()
						.invoke(command.getMethodInvoker(), finalArgs);
					return true;
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		}

		if (found) {
			if (notPassArg != null && notPassObj != null) {
				sendFilterMessages(notPassArg.getFilters(), notPassObj, sender);
				return false;
			}
			this.language.getOnlyPlayer()
				.to(sender, LocaleObject.SendType.CHAT);
			return false;
		}
		sendHelp(sender);
		return true;
	}

	@Override
	public final List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new LinkedList<>();
		if (args.length == 0) return result;

		this.subCommands.forEach((path, commands) -> {
			commands.forEach(command -> {
				if (!hasPermission(sender, command)) {
					return;
				}
				if (command.isHidden()) {
					return;
				}
				if (command.isPlayerRequired() && !(sender instanceof Player)) {
					return;
				}
				if (args.length > command.getArgs()
					.size() + path.getPath().length && !command.isEndless()) {
					return;
				}

				boolean correctPath = true;
				boolean inPath = false;
				for (int i = 0; i < path.getPath().length; i++) {
					if (i < args.length - 1) {
						if (!args[i].equalsIgnoreCase(path.getPath()[i])) {
							correctPath = false;
							break;
						}
					}
					if (i == args.length - 1) {
						inPath = true;
						String resultPath = path.getPath()[i];
						if (resultPath.startsWith(args[args.length - 1])) {
							result.add(path.getPath()[i]);
						}
						break;
					}
				}

				if (!correctPath) {
					return;
				}

				if (inPath) {
					return;
				}

				List<String> fixedArgs = new ArrayList<>(Arrays.asList(args)
					.subList(path.getPath().length, args.length));

				boolean correctTypes = true;
				for (int i = 0; i < Math.min(
					fixedArgs.size() - 1, command.getArgs()
						.size() - 1
				); i++) {
					CommandArg arg = command.getArgs()
						.get(i);
					if (arg.hasCustomParser()) {
						if (!arg.getCustomParser()
							.canConvert(fixedArgs.get(i), plugin)) {
							correctTypes = false;
							break;
						}
					} else {
						if (!parser.canConvert(
							fixedArgs.get(i), arg.getClazz(), plugin)) {
							correctTypes = false;
							break;
						}
					}
				}

				if (!correctTypes) {
					return;
				}

				CommandArg finalArg = command.isEndless() ?
					command.getArgs()
						.get(command.getArgs()
							.size() - 1) :
					null;
				if (fixedArgs.size() <= command.getArgs()
					.size()) {
					finalArg = command.getArgs()
						.get(fixedArgs.size() - 1);
				}
				if (finalArg == null) {
					return;
				}
				List<String> complete;
				if (finalArg.hasCustomParser()) {
					complete = finalArg.getCustomParser()
						.complete(args[args.length - 1], sender, plugin);
				} else {
					complete = parser.complete(
						args[args.length - 1], sender, finalArg.getClazz(),
						plugin
					);
					;
				}
				result.add("<" + finalArg.getName() + ">");
				result.addAll(getCompleterThatPass(complete, finalArg, sender));
			});
		});

		return result;
	}

	//
	// Help Command
	//

	/**
	 * Send help message to sender
	 *
	 * @param sender Command Sender
	 */
	public void sendHelp(CommandSender sender) {
		List<Component> componentsToSend = new LinkedList<>();

		subCommands.forEach((path, commands) -> {
			commands.forEach(command -> {
				if (!hasPermission(sender, command)) {
					return;
				}
				if (command.isHidden()) {
					return;
				}
				if (command.isPlayerRequired() && !(sender instanceof Player)) {
					return;
				}

				componentsToSend.addAll(this.language.getHelpLine()
					.asComponents(
						Placeholder.parsed("command", getCommandStr(command)),
						Placeholder.unparsed(
							"description",
							command.getDescription()
						)
					));
			});
		});

		if (componentsToSend.isEmpty()) {
			componentsToSend.addAll(this.language.getHelpNoInfo()
				.asComponents());
		}

		componentsToSend.addAll(
			0, this.language.getHelpStart()
				.asComponents(Placeholder.parsed("command", cmdName))
		);
		componentsToSend.add(0, Component.text(" "));

		componentsToSend.addAll(this.language.getHelpEnd()
			.asComponents(Placeholder.parsed("command", cmdName)));
		componentsToSend.add(Component.text(" "));

		componentsToSend.forEach(comp -> this.language.getBridge()
			.sendMessage(sender, comp));
	}

	//
	// Utilities
	//

	private String getCommandStr(CommandInfo command) {
		CommandPath path = command.getPath();
		StringBuilder cmdStr = new StringBuilder("/");
		cmdStr.append(this.cmdName);
		if (path.getPath().length > 0) {
			cmdStr.append(" ")
				.append(path.getPathStr());
		}

		command.getArgs()
			.forEach(arg -> {
				cmdStr.append(" ")
					.append("<")
					.append(arg.getName())
					.append(">");
			});
		return cmdStr.toString();
	}

	private List<String> getCompleterThatPass(List<String> complete, CommandArg arg, CommandSender sender) {
		List<String> result = new ArrayList<>();
		complete.forEach(s -> {
			Object obj;
			if (arg.hasCustomParser()) {
				obj = arg.getCustomParser()
					.convert(s, plugin);
				;
			} else {
				obj = parser.convert(s, arg.getClazz(), plugin);
				;
			}
			if (passFilters(arg.getFilters(), obj, sender)) {
				result.add(s);
			}
		});
		return result;
	}

	private boolean hasPermission(CommandSender sender, CommandInfo command) {
		for (String permission : command.getPermissions()) {
			if (sender.hasPermission(permission)) {
				return true;
			}
		}
		return command.getPermissions()
			.isEmpty();
	}

	@SuppressWarnings("unchecked")
	private <T> boolean passFilters(FilterWrapper filters, T obj, CommandSender sender) {
		if (filters == null) {
			return true;
		}
		IFilter<T>[] orFilters = (IFilter<T>[]) filters.getOrFilters(
			obj.getClass());
		IFilter<T>[] andFilters = (IFilter<T>[]) filters.getAndFilters(
			obj.getClass());

		boolean passOr = true;
		boolean passAnd = true;

		for (IFilter<T> filter : orFilters) {
			if (filter.filter(obj, plugin, sender)) {
				passOr = true;
				break;
			}
			passOr = false;
		}

		for (IFilter<T> filter : andFilters) {
			if (!filter.filter(obj, plugin, sender)) {
				passAnd = false;
				break;
			}
		}
		return passOr && passAnd;
	}

	@SuppressWarnings("unchecked")
	private <T> void sendFilterMessages(FilterWrapper filters, T obj, CommandSender sender) {
		if (filters == null) {
			return;
		}
		List<Component> message = new ArrayList<>();
		int lastWeight = -1;

		IFilter<T>[] orFilters = (IFilter<T>[]) filters.getOrFilters(
			obj.getClass());
		IFilter<T>[] andFilters = (IFilter<T>[]) filters.getAndFilters(
			obj.getClass());

		boolean passOr = false;

		for (IFilter<T> filter : orFilters) {
			if (filter.filter(obj, plugin, sender)) {
				passOr = true;
				break;
			}
			if (filter.weight() > lastWeight) {
				message = filter.notPassMessage(obj, plugin, sender);
				lastWeight = filter.weight();
			}
		}

		if (passOr) {
			message = new ArrayList<>();
			lastWeight = -1;
		}

		for (IFilter<T> filter : andFilters) {
			if (!filter.filter(obj, plugin, sender)) {
				if (filter.weight() > lastWeight) {
					message = filter.notPassMessage(obj, plugin, sender);
					lastWeight = filter.weight();
				}
			}
		}

		message.forEach(comp -> this.language.getBridge()
			.sendMessage(sender, comp));
	}

	private void setCommandInfo(String key, Object value) {
		this.commandsConf.set(this.cmdName + "." + key, value);
	}

	private Object getCommandInfo(String key, Object def) {
		return this.commandsConf.get(this.cmdName + "." + key, def);
	}

	private void setVariantInfo(String variant, String key, Object value) {
		this.setCommandInfo(
			"variants." + variant.replace(".", "_") + "." + key,
			value
		);
	}

	public Object getVariantInfo(String variant, String key, Object def) {
		return this.getCommandInfo(
			"variants." + variant.replace(".", "_") + "." + key, def);
	}

	private void saveCommandsFile() {
		try {
			this.commandsConf.save(this.commandsFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
