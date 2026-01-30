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

package dev.projectenhanced.enhancedspigot.common.converter;

import dev.projectenhanced.enhancedspigot.common.converter.custom.EnumStringConverter;
import dev.projectenhanced.enhancedspigot.common.converter.java.*;
import dev.projectenhanced.enhancedspigot.common.converter.spigot.OfflinePlayerStringConverter;
import dev.projectenhanced.enhancedspigot.common.converter.spigot.PlayerStringConverter;
import dev.projectenhanced.enhancedspigot.common.converter.spigot.WorldStringConverter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringConverterRegistry {
	private static final class Holder {
		static final StringConverterRegistry INSTANCE = new StringConverterRegistry();
	}

	private final Map<Class<?>, IStringConverter<?>> converters;

	protected StringConverterRegistry() {
		this.converters = new HashMap<>();

		this.registerConverters(
			new BooleanStringConverter(), new ByteStringConverter(), new DoubleStringConverter(), new FloatStringConverter(), new IntegerStringConverter(), new LongStringConverter(),
			new ShortStringConverter(), new StringConverter()
		);
		this.registerConverters(new OfflinePlayerStringConverter(), new PlayerStringConverter(), new WorldStringConverter());
	}

	public static StringConverterRegistry getInstance() {
		return Holder.INSTANCE;
	}

	/**
	 * Register StringConverter
	 *
	 * @param clazz     Class that is parsed
	 * @param converter Converter instance
	 */
	@SuppressWarnings("unchecked")
	public <T> void registerConverter(Class<T> clazz, IStringConverter<T> converter) {
		this.converters.put(clazz, converter);
		if (!clazz.isPrimitive()) {
			Class<?> primitiveClazz = this.getPrimitive(clazz);
			if (primitiveClazz != null) this.registerConverter((Class<T>) primitiveClazz, converter);
		}
	}

	/**
	 * Register StringConverters
	 *
	 * @param converters Array of converters
	 * @param <T>
	 */
	@SuppressWarnings("unchecked")
	public <T> void registerConverters(IStringConverter<?>... converters) {
		for (IStringConverter<?> converter : converters) {
			ParameterizedType type = (ParameterizedType) converter.getClass()
				.getGenericInterfaces()[0];
			this.registerConverter((Class<T>) type.getActualTypeArguments()[0], (IStringConverter<T>) converter);
		}
	}

	/**
	 * Get converter of specified class
	 *
	 * @param clazz Class that is parsed
	 * @return IStringConverter or null if there isn't any converter of this class
	 */
	@SuppressWarnings("unchecked")
	public <T> IStringConverter<T> getConverter(Class<T> clazz) {
		return (IStringConverter<T>) this.converters.get(clazz);
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
		IStringConverter<?> converter = expected.isEnum() ?
			new EnumStringConverter<>((Class<Z>) expected) :
			this.getConverter(expected);

		return converter != null && converter.canConvert(s, plugin);
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
		IStringConverter<?> converter = expected.isEnum() ?
			new EnumStringConverter<>((Class<Z>) expected) :
			this.getConverter(expected);

		if (converter == null || !converter.canConvert(s, plugin)) {
			throw new IllegalArgumentException("You try convert string to class that you can't convert");
		}

		return (T) converter.fromString(s, plugin);
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
		IStringConverter<?> converter = expected.isEnum() ?
			new EnumStringConverter<>((Class<Z>) expected) :
			this.getConverter(expected);

		if (converter == null) {
			throw new IllegalArgumentException("You try convert string to class that you can't convert");
		}

		return converter.complete(s, sender, plugin);
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
