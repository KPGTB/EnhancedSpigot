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

package dev.projectenhanced.enhancedspigot.locale;

import dev.projectenhanced.enhancedspigot.locale.annotation.Ignore;
import dev.projectenhanced.enhancedspigot.locale.annotation.InjectBridge;
import dev.projectenhanced.enhancedspigot.locale.annotation.LocaleDefault;
import dev.projectenhanced.enhancedspigot.util.TextCase;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

public class LocaleSerializer {
	private final SerializationHandler serializationHandler = new SerializationHandler();
	private final DeserializationHandler deserializationHandler = new DeserializationHandler();

	public ConfigurationSection serialize(Object object, Class<?> objectClass, EnhancedLocale locale) {
		ConfigurationSection result = new MemoryConfiguration();
		this.serializeTo(object, objectClass, locale, result);
		return result;
	}

	public void serializeTo(Object object, Class<?> objectClass, EnhancedLocale locale, ConfigurationSection to) {
		Arrays.stream(objectClass.getDeclaredFields())
			  .forEach(field -> {
				  Ignore ignoreAnn = field.getDeclaredAnnotation(Ignore.class);
				  LocaleDefault[] defaults = field.getDeclaredAnnotationsByType(LocaleDefault.class);
				  LocaleDefault def = Arrays.stream(defaults)
											.filter(ann -> ann.language()
															  .equalsIgnoreCase(locale.getLocale()))
											.findAny()
											.orElse(null);
				  InjectBridge bridgeAnn = field.getDeclaredAnnotation(InjectBridge.class);
				  if (ignoreAnn != null || bridgeAnn != null) return;

				  String key = TextCase.camelToKebabCase(field.getName());

				  field.setAccessible(true);
				  Object value = TryCatchUtil.tryAndReturn(() -> field.get(object));
				  if (value == null && LocaleObject.class.isAssignableFrom(field.getType()))
					  value = new LocaleObject(locale.getBridge(), def != null ?
																   def.def() :
																   new String[]{});
				  field.setAccessible(false);

				  to.set(key, serializationHandler.handleObject(value, locale));
			  });
	}

	public Object deserialize(ConfigurationSection serialized, Class<?> targetClass, EnhancedLocale locale) {
		Object result = this.getClassInstance(targetClass, locale);
		this.deserializeTo(serialized, targetClass, locale, result);
		return result;
	}

	public void deserializeTo(ConfigurationSection serialized, Class<?> targetClass, EnhancedLocale locale, Object to) {
		Arrays.stream(targetClass.getDeclaredFields())
			  .forEach(field -> {
				  Ignore ignoreAnn = field.getDeclaredAnnotation(Ignore.class);
				  LocaleDefault[] defaults = field.getDeclaredAnnotationsByType(LocaleDefault.class);
				  LocaleDefault def = Arrays.stream(defaults)
											.filter(ann -> ann.language()
															  .equalsIgnoreCase(locale.getLocale()))
											.findAny()
											.orElse(null);
				  InjectBridge bridgeAnn = field.getDeclaredAnnotation(InjectBridge.class);
				  if (ignoreAnn != null) return;

				  String key = TextCase.camelToKebabCase(field.getName());
				  Object configValue = serialized.get(key);

				  field.setAccessible(true);
				  if (bridgeAnn != null) {
					  TryCatchUtil.tryRun(() -> field.set(to, locale.getBridge()));
				  } else {
					  TryCatchUtil.tryRun(() -> field.set(to, configValue != null ?
															  deserializationHandler.handleObject(configValue, field.getType(), locale) :
															  LocaleObject.class.isAssignableFrom(field.getType()) ?
															  new LocaleObject(locale.getBridge(), def != null ?
																								   def.def() :
																								   new String[]{}) :
															  deserializationHandler.handleObject(new MemoryConfiguration(), field.getType(), locale)));
				  }
				  field.setAccessible(false);
			  });
	}

	private Object getClassInstance(Class<?> clazz, EnhancedLocale source) {
		try {
			return getAccessibleInstance(clazz.getDeclaredConstructor());
		} catch (Exception e) {
		}

		Class<?> enclosing = clazz.getEnclosingClass();
		Object invoker = null;

		if (enclosing != null) {
			if (EnhancedLocale.class.isAssignableFrom(enclosing)) invoker = source;
			else invoker = getClassInstance(enclosing, source);
		}

		Object finalInvoker = invoker;
		return TryCatchUtil.tryAndReturn(() -> finalInvoker != null ?
											   getAccessibleInstance(clazz.getDeclaredConstructor(enclosing), finalInvoker) :
											   getAccessibleInstance(clazz.getDeclaredConstructor()));
	}

	private Object getAccessibleInstance(Constructor<?> constructor, Object... initArgs) throws InvocationTargetException, InstantiationException, IllegalAccessException {
		constructor.setAccessible(true);
		Object result = constructor.newInstance(initArgs);
		constructor.setAccessible(false);
		return result;
	}

	class SerializationHandler {
		private Object handleObject(Object value, EnhancedLocale locale) {
			if (value == null) return null;
			if (value instanceof LocaleObject) return ((LocaleObject) value).toConfig();
			return serialize(value, value.getClass(), locale);
		}
	}

	class DeserializationHandler {
		@SuppressWarnings("unchecked")
		private Object handleObject(Object configValue, Class<?> clazz, EnhancedLocale locale) {
			if (configValue == null) return null;
			if (LocaleObject.class.isAssignableFrom(clazz)) {
				if (configValue instanceof List)
					return new LocaleObject(locale.getBridge(), (List<String>) configValue);
				return new LocaleObject(locale.getBridge(), String.valueOf(configValue));
			}
			return deserialize((ConfigurationSection) configValue, clazz, locale);
		}
	}
}
