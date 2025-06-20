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

import dev.projectenhanced.enhancedspigot.command.parser.custom.EnumParser;
import dev.projectenhanced.enhancedspigot.command.parser.custom.TimeParser;
import dev.projectenhanced.enhancedspigot.command.parser.java.*;
import dev.projectenhanced.enhancedspigot.command.parser.spigot.OfflinePlayerParser;
import dev.projectenhanced.enhancedspigot.command.parser.spigot.PlayerParser;
import dev.projectenhanced.enhancedspigot.command.parser.spigot.WorldParser;
import dev.projectenhanced.enhancedspigot.util.ReflectionUtil;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of ArgumentParsers that are used in commands
 */
public class CommandArgumentRegistry {
	private static final class Holder {
		static final CommandArgumentRegistry INSTANCE = new CommandArgumentRegistry();
	}

	private final HashMap<Class<?>, IArgumentParser<?>> parsers;

	/**
	 * Constructor of manager
	 */
	public CommandArgumentRegistry() {
		this.parsers = new HashMap<>();

		this.registerParsers(
			new BooleanParser(), new ByteParser(), new DoubleParser(),
			new FloatParser(), new IntegerParser(), new LongParser(),
			new ShortParser(), new StringParser()
		);
		this.registerParsers(
			new OfflinePlayerParser(), new PlayerParser(), new WorldParser(),
			new TimeParser()
		);
	}

	public static CommandArgumentRegistry getInstance() {
		return Holder.INSTANCE;
	}

	/**
	 * Register ArgumentParsers
	 *
	 * @param clazz  Class that is parsed
	 * @param parser Parser instance
	 */
	@SuppressWarnings("unchecked")
	public <T> void registerParser(Class<T> clazz, IArgumentParser<T> parser) {
		parsers.put(clazz, parser);
		if (!clazz.isPrimitive()) {
			Class<?> primitiveClazz = this.getPrimitive(clazz);
			if (primitiveClazz != null) registerParser(
				(Class<T>) primitiveClazz, parser);
		}
	}

	/**
	 * Register ArgumentParsers
	 *
	 * @param parsers Array of argument parsers
	 * @param <T>
	 */
	@SuppressWarnings("unchecked")
	public <T> void registerParsers(IArgumentParser<?>... parsers) {
		for (IArgumentParser<?> parser : parsers) {
			ParameterizedType type = (ParameterizedType) parser.getClass()
				.getGenericInterfaces()[0];
			Class<T> typeArgClazz = (Class<T>) type.getActualTypeArguments()[0];
			IArgumentParser<T> fixedParser = (IArgumentParser<T>) parser;
			registerParser(typeArgClazz, fixedParser);
		}
	}

	/**
	 * Register all ArgumentParsers from specified package
	 *
	 * @param parsersPackage Package where parsers are stored
	 * @param jarFile        File with that plugin
	 */
	@SuppressWarnings("unchecked")
	public <T> void registerParsers(String parsersPackage, File jarFile) {
		ReflectionUtil.getAllClassesInPackage(jarFile, parsersPackage)
			.stream()
			.filter(clazz -> IArgumentParser.class.isAssignableFrom(
				clazz) && !IArgumentParser.class.equals(clazz) && !clazz.equals(
				EnumParser.class))
			.forEach(clazz -> {
				ParameterizedType type = (ParameterizedType) clazz.getGenericInterfaces()[0];
				Class<T> typeArgClazz = (Class<T>) type.getActualTypeArguments()[0];
				IArgumentParser<T> parser = (IArgumentParser<T>) TryCatchUtil.tryAndReturn(
					clazz::newInstance);
				registerParser(typeArgClazz, parser);
			});
	}

	/**
	 * Unregister ParamParser
	 *
	 * @param clazz Class that should be unregistered
	 */
	public void unregisterParser(Class<?> clazz) {
		parsers.remove(clazz);
	}

	/**
	 * Get parser of specified class
	 *
	 * @param clazz Class that have parser
	 * @return IParamParser or null if there isn't any parsers of this class
	 */
	@SuppressWarnings("unchecked")
	public <T> IArgumentParser<T> getParser(Class<T> clazz) {
		return (IArgumentParser<T>) parsers.get(clazz);
	}

	/**
	 * Check if string can be covert to class
	 *
	 * @param s        String that you want to convert
	 * @param expected Class that is expected
	 * @return true if you can convert, or false if you can't
	 */
	@SuppressWarnings("unchecked")
	public <T, Z extends Enum<Z>> boolean canConvert(String s, Class<T> expected, JavaPlugin plugin) {
		if (expected.isEnum()) {
			Class<Z> enumClass = (Class<Z>) expected;
			EnumParser<Z> enumParser = new EnumParser<>(enumClass);
			return enumParser.canConvert(s);
		}
		IArgumentParser<T> parser = getParser(expected);
		if (parser == null) return false;
		return parser.canConvert(s, plugin);
	}

	/**
	 * Convert string to class
	 *
	 * @param s        String that you want to convert
	 * @param expected Class that is expected
	 * @return Class that is converted from string
	 */
	@SuppressWarnings("unchecked")
	public <T, Z extends Enum<Z>> T convert(String s, Class<T> expected, JavaPlugin plugin) {
		if (expected.isEnum()) {
			Class<Z> enumClass = (Class<Z>) expected;
			EnumParser<Z> enumParser = new EnumParser<>(enumClass);
			return (T) enumParser.convert(s);
		}
		IArgumentParser<T> parser = getParser(expected);
		if (!canConvert(s, expected, plugin) || parser == null) {
			throw new IllegalArgumentException(
				"You try convert string to class that you can't convert");
		}
		return parser.convert(s, plugin);
	}

	/**
	 * Get a list to tab completer
	 *
	 * @param s        Command argument
	 * @param sender   CommandSender
	 * @param expected Class that is expected
	 */
	@SuppressWarnings("unchecked")
	public <T, Z extends Enum<Z>> List<String> complete(String s, CommandSender sender, Class<T> expected, JavaPlugin plugin) {
		if (expected.isEnum()) {
			Class<Z> enumClass = (Class<Z>) expected;
			EnumParser<Z> enumParser = new EnumParser<>(enumClass);
			return enumParser.complete(s, sender);
		}
		IArgumentParser<T> parser = getParser(expected);
		if (parser == null) {
			throw new IllegalArgumentException(
				"You try convert string to class that you can't convert");
		}
		return parser.complete(s, sender, plugin);
	}

	private Class<?> getPrimitive(Class<?> clazz) {
		Map<Class<?>, Class<?>> map = new HashMap<>();
		map.put(Boolean.class, Boolean.TYPE);
		map.put(Character.class, Character.TYPE);
		map.put(Byte.class, Byte.TYPE);
		map.put(Short.class, Short.TYPE);
		map.put(Integer.class, Integer.TYPE);
		map.put(Long.class, Long.TYPE);
		map.put(Float.class, Float.TYPE);
		map.put(Double.class, Double.TYPE);

		return map.entrySet()
			.stream()
			.filter(entry -> entry.getKey()
				.isAssignableFrom(clazz))
			.map(Map.Entry::getValue)
			.findAny()
			.orElse(null);
	}
}
