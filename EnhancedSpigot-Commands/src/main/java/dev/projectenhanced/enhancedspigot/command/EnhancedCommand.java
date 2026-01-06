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

import dev.projectenhanced.enhancedspigot.command.annotation.Aliases;
import dev.projectenhanced.enhancedspigot.command.annotation.Hidden;
import dev.projectenhanced.enhancedspigot.command.annotation.LongString;
import dev.projectenhanced.enhancedspigot.command.annotation.MainCommand;
import dev.projectenhanced.enhancedspigot.command.model.CommandArgument;
import dev.projectenhanced.enhancedspigot.command.model.CommandInfo;
import dev.projectenhanced.enhancedspigot.command.model.CommandPath;
import dev.projectenhanced.enhancedspigot.common.IDependencyProvider;
import dev.projectenhanced.enhancedspigot.common.annotation.Converter;
import dev.projectenhanced.enhancedspigot.common.annotation.Description;
import dev.projectenhanced.enhancedspigot.common.annotation.Filter;
import dev.projectenhanced.enhancedspigot.common.annotation.Ignore;
import dev.projectenhanced.enhancedspigot.common.annotation.Permission;
import dev.projectenhanced.enhancedspigot.common.annotation.WithoutPermission;
import dev.projectenhanced.enhancedspigot.common.converter.IStringConverter;
import dev.projectenhanced.enhancedspigot.common.converter.StringConverterRegistry;
import dev.projectenhanced.enhancedspigot.common.filter.FilterContainer;
import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
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
import java.util.Objects;

/**
 * Abstract class that handles process of creating commands
 */
public abstract class EnhancedCommand extends Command {
	protected final JavaPlugin plugin;
	protected final IDependencyProvider provider;

	private final String groupPath;
	private final CommandLocale locale;
	private final StringConverterRegistry converterRegistry;

	private final File commandsFile;
	private final YamlConfiguration commandsConf;

	private final Map<CommandPath, List<CommandInfo>> subCommands;
	private String cmdName;

	private EnhancedCommand(JavaPlugin plugin, IDependencyProvider provider, CommandLocale locale, String groupPath) {
		super("");

		this.plugin = plugin;
		this.provider = provider;

		this.groupPath = groupPath;
		this.locale = locale;
		this.converterRegistry = StringConverterRegistry.getInstance();

		this.commandsFile = new File(plugin.getDataFolder(), "commands.yml");
		this.commandsConf = YamlConfiguration.loadConfiguration(this.commandsFile);

		this.subCommands = new LinkedHashMap<>();
	}

	public EnhancedCommand(JavaPlugin plugin, CommandLocale locale, String groupPath) {
		this(plugin, null, locale, groupPath);
	}

	public EnhancedCommand(DependencyProvider provider, CommandLocale locale, String groupPath) {
		this(provider.provide(JavaPlugin.class), provider, locale, groupPath);
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

		Description descriptionAnn = getClass().getDeclaredAnnotation(Description.class);
		String description = descriptionAnn != null ?
			descriptionAnn.value() :
			"Command created using EnhancedCommand";
		description = String.valueOf(this.getCommandInfo("description", description));
		super.setDescription(description);

		Aliases aliasesAnn = getClass().getDeclaredAnnotation(Aliases.class);
		List<String> aliases = aliasesAnn != null ?
			Arrays.asList(aliasesAnn.value()) :
			new ArrayList<>();
		aliases = (List<String>) this.getCommandInfo("aliases", aliases);
		super.setAliases(aliases);

		Permission customGlobalPermissionAnn = getClass().getDeclaredAnnotation(Permission.class);
		String customGlobalPermission = customGlobalPermissionAnn != null ?
			customGlobalPermissionAnn.value() :
			null;
		boolean globalWithoutPermission = getClass().getDeclaredAnnotation(WithoutPermission.class) != null;

		this.setCommandInfo("command", "/" + cmdName);
		this.setCommandInfo("description", description);
		this.setCommandInfo("aliases", aliases);

		this.scanClass(new CommandPath(), this.getClass(), this, customGlobalPermission, globalWithoutPermission);
	}

	private void scanClass(CommandPath path, Class<?> clazz, Object invoker, String customGlobalPermission, boolean globalWithoutPermission) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isSynthetic() || method.getDeclaredAnnotation(Ignore.class) != null || method.getParameterCount() == 0) continue;
			this.handleMethod(method, invoker, path.clone(), customGlobalPermission, globalWithoutPermission);
		}

		// === Scan another classes
		for (Class<?> c : clazz.getDeclaredClasses()) {
			if (c.getDeclaredAnnotation(Ignore.class) != null) continue;

			CommandPath newPath = path.clone();
			newPath.add(c.getSimpleName()
				.toLowerCase());

			this.scanClass(
				newPath, c, c.getDeclaredConstructor(clazz)
					.newInstance(invoker), customGlobalPermission, globalWithoutPermission
			);
		}

		this.saveCommandsFile();
	}

	private void handleMethod(Method method, Object invoker, CommandPath path, String customGlobalPermission, boolean globalWithoutPermission) {
		// === Name & Path
		String name = method.getName()
			.toLowerCase();

		boolean mainCommand = method.getDeclaredAnnotation(MainCommand.class) != null;
		String methodPath = mainCommand ?
			"" :
			name;
		path.add(methodPath);
		this.subCommands.putIfAbsent(path, new ArrayList<>());
		List<CommandInfo> commands = this.subCommands.get(path);

		// === Description
		Description descriptionAnn = method.getDeclaredAnnotation(Description.class);
		String description = descriptionAnn != null ?
			descriptionAnn.value() :
			"Subcommand created using EnhancedSpigot";

		// === Permissions
		List<String> permissions = this.generatePermissions(method, name, path, mainCommand, customGlobalPermission, globalWithoutPermission);

		// === Source
		Parameter[] parameters = method.getParameters();
		Parameter sourceParam = parameters[0];
		Class<?> sourceClass = sourceParam.getType();

		boolean playerRequired = Player.class.equals(sourceClass);
		if (!playerRequired && !CommandSender.class.equals(sourceClass)) return;

		Filter sourceFiltersAnn = sourceParam.getDeclaredAnnotation(Filter.class);
		FilterContainer<CommandSender> sourceFilters = null;
		if (sourceFiltersAnn != null) sourceFilters = new FilterContainer<>(sourceFiltersAnn);

		// === Arguments
		List<CommandArgument<?>> args = new LinkedList<>();
		for (int i = 1; i < parameters.length; i++) {
			args.add(this.handleParameter(parameters[i]));
		}

		// === Endless
		boolean endless = false;
		if (!args.isEmpty()) {
			Parameter lastParam = parameters[parameters.length - 1];
			if (lastParam.getType()
				.equals(String.class) && lastParam.getDeclaredAnnotation(LongString.class) != null) {
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
		CommandInfo info = new CommandInfo(path, permissions, playerRequired, sourceFilters, args, invoker, method, endless, hidden, description);
		commands.add(info);

		String variantName = getCommandStr(info);
		info.setDescription(String.valueOf(this.getVariantInfo(variantName, "description", info.getDescription())));

		setVariantInfo(variantName, "description", info.getDescription());
		setVariantInfo(variantName, "permissions", permissions);
		setVariantInfo(variantName, "onlyPlayer", playerRequired);
		setVariantInfo(variantName, "hidden", hidden);
	}

	@SuppressWarnings("unchecked")
	private <T> CommandArgument<T> handleParameter(Parameter parameter) {
		String paramName = parameter.getName();
		Class<T> paramClass = (Class<T>) parameter.getType();

		Filter paramFiltersAnn = parameter.getDeclaredAnnotation(Filter.class);
		FilterContainer<T> filters = null;
		if (paramFiltersAnn != null) filters = new FilterContainer<>(paramFiltersAnn);

		Converter customConverterAnn = parameter.getDeclaredAnnotation(Converter.class);
		Class<? extends IStringConverter<T>> customConverter = null;
		if (customConverterAnn != null) customConverter = (Class<? extends IStringConverter<T>>) customConverterAnn.value();

		return new CommandArgument<T>(paramClass, customConverter, filters, paramName);
	}

	private List<String> generatePermissions(Method method, String name, CommandPath path, boolean mainCommand, String customGlobalPermission, boolean globalWithoutPermission) {
		Permission customPermissionAnn = method.getDeclaredAnnotation(Permission.class);
		String customPermission = customPermissionAnn != null ?
			customPermissionAnn.value() :
			null;
		boolean withoutPermission = method.getDeclaredAnnotation(WithoutPermission.class) != null;

		List<String> permissions = new ArrayList<>();
		if (!withoutPermission && !globalWithoutPermission) {
			if (mainCommand) {
				permissions.add(("command." + this.groupPath + "." + this.cmdName + "." + path.getPermissionStr() + "." + name).replace("..", "."));
			} else {
				permissions.add(("command." + this.groupPath + "." + this.cmdName + "." + path.getPermissionStr()).replace("..", "."));
			}
			CommandPath permissionsPath = new CommandPath();
			int permissionsPathMaxLength = path.getPath().length;
			if (!mainCommand) {
				permissionsPathMaxLength--;
			}
			for (int i = 0; i < permissionsPathMaxLength; i++) {
				permissionsPath.add(path.getPath()[i]);
				permissions.add(("command." + this.groupPath + "." + this.cmdName + "." + permissionsPath.getPermissionStr() + ".*").replace("..", "."));
			}
			permissions.add(("command." + this.groupPath + "." + this.cmdName + ".*").replace("..", "."));
			if (!this.groupPath.isEmpty()) {
				permissions.add(("command." + this.groupPath + ".*").replace("..", "."));
			}
			permissions.add("command.*");

			if (customGlobalPermission != null) {
				permissions.add(customGlobalPermission);
			}
			if (customPermission != null) {
				permissions.add(customPermission);
			}
		}

		return permissions;
	}

	//
	//  Command Execution
	//

	@Override
	public final boolean execute(CommandSender sender, String commandLabel, String[] args) {
		List<CommandPath> possiblePaths = this.getPossiblePaths(args);

		boolean found = false;
		List<String> failedFilters = new ArrayList<>();

		for (CommandPath path : possiblePaths) {
			List<CommandInfo> commands = this.subCommands.get(path);
			List<String> actualArgs = new LinkedList<>(Arrays.asList(args)
				.subList(path.getPath().length, args.length));

			for (CommandInfo command : commands) {
				if (!this.validateAllArgsTypes(command, actualArgs)) continue;

				if (command.isPlayerRequired() && !(sender instanceof Player)) {
					found = true;
					continue;
				}

				if (!this.hasPermission(sender, command)) {
					this.locale.getNoPermission()
						.to(sender);
					return false;
				}

				Object[] finalArgs = this.getFinalArgs(command, actualArgs, sender);

				List<String> passSourceFilters = command.getSourceFilters()
					.passFilters(sender, this.plugin, sender);
				if (!passSourceFilters.isEmpty()) {
					found = true;
					failedFilters = passSourceFilters;
					continue;
				}

				List<String> passArgsFilters = new ArrayList<>();
				for (int j = 1; j < finalArgs.length; j++) {
					passArgsFilters = this.validateArgument(command, j - 1, finalArgs[j], this.plugin, sender);
					if (!passArgsFilters.isEmpty()) {
						found = true;
						failedFilters = passArgsFilters;
						break;
					}
				}

				if (!passArgsFilters.isEmpty()) continue;

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
			if (!failedFilters.isEmpty()) {
				failedFilters.forEach(sender::sendMessage);
				return false;
			}
			this.locale.getOnlyPlayer()
				.to(sender);
			return false;
		}
		this.sendHelp(sender, commandLabel);
		return true;
	}

	private List<CommandPath> getPossiblePaths(String[] args) {
		return this.subCommands.keySet()
			.stream()
			.filter(path -> {
				String[] pathArr = path.getPath();
				if (args.length < pathArr.length) return false;
				for (int i = 0; i < pathArr.length; i++) {
					if (!args[i].equalsIgnoreCase(pathArr[i])) {
						return false;
					}
				}
				return true;
			})
			.toList();
	}

	private boolean validateAllArgsTypes(CommandInfo command, List<String> actualArgs) {
		if (!command.isEndless() && command.getArgs()
			.size() != actualArgs.size()) return false;
		if (command.isEndless() && command.getArgs()
			.size() > actualArgs.size()) return false;

		return this.validateArgsTypesUntil(
			command, actualArgs, command.getArgs()
				.size()
		);
	}

	private boolean validateArgsTypesUntil(CommandInfo command, List<String> actualArgs, int untilIdx) {
		for (int i = 0; i < untilIdx; i++) {
			if (command.isEndless() && command.getArgs()
				.size() == i + 1) {
				break;
			}
			CommandArgument<?> arg = command.getArgs()
				.get(i);

			if (arg.hasCustomConverter()) {
				if (!arg.getCustomConverter()
					.canConvert(actualArgs.get(i), plugin)) return false;
			} else {
				if (!converterRegistry.canConvert(actualArgs.get(i), arg.getClazz(), plugin)) return false;
			}
		}

		return true;
	}

	public Object[] getFinalArgs(CommandInfo command, List<String> actualArgs, CommandSender sender) {
		Object[] result = new Object[command.getArgs()
			.size() + 1];
		result[0] = sender;

		for (int resultIdx = 1; resultIdx < result.length; resultIdx++) {
			int argIdx = resultIdx - 1;

			if (command.isEndless() && result.length == resultIdx + 1) {
				result[resultIdx] = String.join(" ", actualArgs.subList(argIdx, actualArgs.size()));
				break;
			}

			CommandArgument<?> arg = command.getArgs()
				.get(argIdx);
			if (arg.hasCustomConverter()) {
				result[resultIdx] = arg.getCustomConverter()
					.fromString(actualArgs.get(argIdx), plugin);
			} else {
				result[resultIdx] = this.converterRegistry.convert(actualArgs.get(argIdx), arg.getClazz(), plugin);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <T> List<String> validateArgument(CommandInfo command, int argIdx, T object, JavaPlugin plugin, CommandSender sender) {
		CommandArgument<T> argument = (CommandArgument<T>) command.getArgs()
			.get(argIdx);
		return argument.getFilters()
			.passFilters(object, plugin, sender);
	}

	//
	//  Tab Completer
	//

	@Override
	public final List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new LinkedList<>();
		if (args.length == 0) return result;
		int lastArgIndex = args.length - 1;

		this.subCommands.forEach((path, commands) -> {
			commands.forEach(command -> {
				if (!hasPermission(sender, command) || command.isHidden()) return;
				if (command.isPlayerRequired() && !(sender instanceof Player)) return;
				if (args.length > command.getArgs()
					.size() + path.getPath().length && !command.isEndless()) {
					return;
				}

				// === Check path
				for (int i = 0; i < path.getPath().length; i++) {
					String currentPath = path.getPath()[i];
					if (i < lastArgIndex) {
						if (!args[i].equalsIgnoreCase(currentPath)) return;
					} else {
						if (currentPath.startsWith(args[lastArgIndex])) result.add(currentPath);
						return;
					}
				}

				// === Check args
				List<String> actualArgs = new LinkedList<>(Arrays.asList(args)
					.subList(path.getPath().length, args.length));
				int lastCommandArgIndex = Math.min(
					actualArgs.size(), command.getArgs()
						.size()
				) - 1;

				if (!this.validateArgsTypesUntil(command, actualArgs, lastCommandArgIndex)) return;

				CommandArgument<?> lastArg = command.getArgs()
					.get(lastCommandArgIndex);

				List<String> complete = lastArg.hasCustomConverter() ?
					lastArg.getCustomConverter()
						.complete(args[lastArgIndex], sender, this.plugin) :
					this.converterRegistry.complete(args[args.length - 1], sender, lastArg.getClazz(), this.plugin);

				result.add("<" + lastArg.getName() + ">");
				result.addAll(this.getCompleterThatPassFilters(complete, lastArg, sender));
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
	public void sendHelp(CommandSender sender, String label) {
		List<Component> componentsToSend = new LinkedList<>();

		subCommands.forEach((path, commands) -> {
			commands.forEach(command -> {
				if (!hasPermission(sender, command)) return;
				if (command.isHidden()) return;
				if (command.isPlayerRequired() && !(sender instanceof Player)) return;

				componentsToSend.addAll(this.locale.getHelpLine()
					.asComponents(Placeholder.parsed("command", getCommandStr(command)), Placeholder.unparsed("description", command.getDescription())));
			});
		});

		if (componentsToSend.isEmpty()) componentsToSend.addAll(this.locale.getHelpNoInfo()
			.asComponents());

		componentsToSend.addAll(
			0, this.locale.getHelpStart()
				.asComponents(Placeholder.parsed("command", cmdName))
		);
		componentsToSend.add(0, Component.text(" "));

		componentsToSend.addAll(this.locale.getHelpEnd()
			.asComponents(Placeholder.parsed("command", cmdName)));
		componentsToSend.add(Component.text(" "));

		componentsToSend.forEach(comp -> this.locale.getBridge()
			.sendMessage(sender, comp));
	}

	//
	// Utilities
	//

	private String getCommandStr(CommandInfo command) {
		CommandPath path = command.getPath();
		StringBuilder cmdStr = new StringBuilder();
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

	private <T> List<String> getCompleterThatPassFilters(List<String> complete, CommandArgument<T> arg, CommandSender sender) {
		List<String> result = new ArrayList<>();
		complete.forEach(s -> {
			T obj = arg.hasCustomConverter() ?
				arg.getCustomConverter()
					.fromString(s, plugin) :
				this.converterRegistry.convert(s, arg.getClazz(), plugin);

			if (arg.getFilters()
				.passFilters(obj, this.plugin, sender)
				.isEmpty()) result.add(s);
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

	private void setCommandInfo(String key, Object value) {
		this.commandsConf.set(this.cmdName + "." + key, value);
	}

	private Object getCommandInfo(String key, Object def) {
		return this.commandsConf.get(this.cmdName + "." + key, def);
	}

	private void setVariantInfo(String variant, String key, Object value) {
		this.setCommandInfo("variants." + variant.replace(".", "_") + "." + key, value);
	}

	public Object getVariantInfo(String variant, String key, Object def) {
		return this.getCommandInfo("variants." + variant.replace(".", "_") + "." + key, def);
	}

	private void saveCommandsFile() {
		try {
			this.commandsConf.save(this.commandsFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || getClass() != object.getClass()) return false;
		EnhancedCommand that = (EnhancedCommand) object;
		return Objects.equals(groupPath, that.groupPath) && Objects.equals(cmdName, that.cmdName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(groupPath, cmdName);
	}
}
