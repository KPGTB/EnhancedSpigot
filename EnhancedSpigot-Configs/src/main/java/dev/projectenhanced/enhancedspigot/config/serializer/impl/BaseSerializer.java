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

package dev.projectenhanced.enhancedspigot.config.serializer.impl;

import dev.projectenhanced.enhancedspigot.config.EnhancedConfig;
import dev.projectenhanced.enhancedspigot.config.annotation.Comment;
import dev.projectenhanced.enhancedspigot.config.annotation.DoNotRecover;
import dev.projectenhanced.enhancedspigot.config.annotation.Ignore;
import dev.projectenhanced.enhancedspigot.config.annotation.Serializer;
import dev.projectenhanced.enhancedspigot.config.serializer.ConfigSerializerRegistry;
import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;
import dev.projectenhanced.enhancedspigot.config.util.SectionUtil;
import dev.projectenhanced.enhancedspigot.util.TextCase;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseSerializer implements ISerializer<Object> {
	private final SerializationHandler serializationHandler = new SerializationHandler();
	private final DeserializationHandler deserializationHandler = new DeserializationHandler();

	@Override
	public Object serialize(Object object, Class<?> objectClass, EnhancedConfig config) {
		ConfigurationSection result = SectionUtil.createEmpty();
		this.serializeTo(object, objectClass, config, result);
		return result;
	}

	@Override
	public void serializeTo(Object object, Class<?> objectClass, EnhancedConfig config, Object to) {
		ConfigurationSection section = (ConfigurationSection) to;
		Arrays.stream(objectClass.getDeclaredFields())
			  .forEach(field -> {
				  Ignore ignoreAnn = field.getDeclaredAnnotation(Ignore.class);
				  Comment commentAnn = field.getDeclaredAnnotation(Comment.class);
				  Serializer serializerAnn = field.getDeclaredAnnotation(Serializer.class);
				  if (ignoreAnn != null) return;

				  String key = TextCase.camelToKebabCase(field.getName());

				  field.setAccessible(true);
				  Object value = TryCatchUtil.tryAndReturn(() -> field.get(object));
				  field.setAccessible(false);

				  section.set(key, serializationHandler.handleObject(value, serializerAnn, config));
				  if (commentAnn != null) SectionUtil.addComments(section, key, commentAnn.value());
			  });
	}

	@Override
	public Object deserialize(Object serialized, Class<?> targetClass, EnhancedConfig config) {
		Object result = this.getClassInstance(targetClass, config);
		this.deserializeTo(serialized, targetClass, config, result);
		return result;
	}

	@Override
	public void deserializeTo(Object serialized, Class<?> targetClass, EnhancedConfig config, Object to) {
		ConfigurationSection section = (ConfigurationSection) serialized;

		Arrays.stream(targetClass.getDeclaredFields())
			  .forEach(field -> {
				  Ignore ignoreAnn = field.getDeclaredAnnotation(Ignore.class);
				  Serializer serializerAnn = field.getDeclaredAnnotation(Serializer.class);
				  if (ignoreAnn != null) return;

				  String key = TextCase.camelToKebabCase(field.getName());
				  Object configValue = section.get(key);

				  field.setAccessible(true);
				  Type generic = field.getGenericType();
				  Type[] typeArgs = generic instanceof ParameterizedType ?
									((ParameterizedType) generic).getActualTypeArguments() :
									null;

				  TryCatchUtil.tryRun(() -> field.set(to, configValue != null ?
														  deserializationHandler.handleObject(configValue, field.getType(), typeArgs, serializerAnn, config) :
														  this.handleDefaults(field, config)));
				  field.setAccessible(false);
			  });
	}

	@Override
	public boolean convertToSection() {
		return true;
	}

	private Object handleDefaults(Field field, EnhancedConfig source) {
		if (field.getDeclaredAnnotation(DoNotRecover.class) != null) return null;

		Class<?> clazz = field.getDeclaringClass();
		Object newClassInstance = this.getClassInstance(clazz, source);

		return TryCatchUtil.tryAndReturn(() -> field.get(newClassInstance));
	}

	private Object getClassInstance(Class<?> clazz, EnhancedConfig source) {
		try {
			return getAccessibleInstance(clazz.getDeclaredConstructor());
		} catch (Exception e) {
		}

		Class<?> enclosing = clazz.getEnclosingClass();
		Object invoker = null;

		if (enclosing != null) {
			if (EnhancedConfig.class.isAssignableFrom(enclosing)) invoker = source;
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

	private boolean isEnclosedInConfig(Class<?> clazz) {
		Class<?> enclosing = clazz.getEnclosingClass();
		while (enclosing != null) {
			if (EnhancedConfig.class.isAssignableFrom(enclosing)) return true;
			enclosing = enclosing.getEnclosingClass();
		}
		return false;
	}

	class SerializationHandler {
		private Object handleObject(Object configValue, Serializer serializerAnn, EnhancedConfig config) {
			if (configValue == null) return null;
			if (configValue instanceof List) return handleList((List<?>) configValue, serializerAnn, config);
			if (configValue instanceof Map) return handleMap((Map<?, ?>) configValue, serializerAnn, config);

			ISerializer<?> serializer = ConfigSerializerRegistry.getInstance()
																.getSerializer(configValue.getClass());
			if (serializerAnn != null) serializer = TryCatchUtil.tryAndReturn(() -> serializerAnn.value()
																								 .getDeclaredConstructor()
																								 .newInstance());
			if (isEnclosedInConfig(configValue.getClass()))
				serializer = ConfigSerializerRegistry.CustomSerializers.BASE;

			return serializer == null ?
				   configValue :
				   useSerializer(serializer, configValue, config);
		}

		private List<Object> handleList(List<?> value, Serializer serializerAnn, EnhancedConfig config) {
			return value.stream()
						.map(element -> handleObject(element, serializerAnn, config))
						.collect(Collectors.toList());
		}

		private Map<Object, Object> handleMap(Map<?, ?> value, Serializer serializerAnn, EnhancedConfig config) {
			Map<Object, Object> result = new HashMap<>();
			value.forEach((k, v) -> {
				result.put(k, handleObject(v, serializerAnn, config));
			});
			return result;
		}

		@SuppressWarnings("unchecked")
		private <T> Object useSerializer(ISerializer<T> serializer, Object obj, EnhancedConfig config) {
			return serializer.serialize((T) obj, (Class<? extends T>) obj.getClass(), config);
		}
	}

	class DeserializationHandler {
		private Object handleObject(Object configValue, Class<?> clazz, Type[] typeArgs, Serializer serializerAnn, EnhancedConfig config) {
			if (configValue == null) return null;
			if (List.class.isAssignableFrom(clazz))
				return handleList((List<?>) configValue, typeArgs[0], serializerAnn, config);
			if (Map.class.isAssignableFrom(clazz))
				return handleMapDeserialization(configValue instanceof ConfigurationSection ?
												((ConfigurationSection) configValue).getValues(false) :
												(Map<?, ?>) configValue, typeArgs[1], serializerAnn, config);

			ISerializer<?> serializer = ConfigSerializerRegistry.getInstance()
																.getSerializer(clazz);
			if (serializerAnn != null) serializer = TryCatchUtil.tryAndReturn(() -> serializerAnn.value()
																								 .getDeclaredConstructor()
																								 .newInstance());
			if (isEnclosedInConfig(clazz)) serializer = ConfigSerializerRegistry.CustomSerializers.BASE;

			return serializer == null ?
				   configValue :
				   useDeserializer(serializer, configValue, clazz, config);
		}

		private List<Object> handleList(List<?> value, Type valueType, Serializer serializerAnn, EnhancedConfig config) {
			Map.Entry<Class<?>, Type[]> typeData = extractTypeData(valueType);

			return value.stream()
						.map(element -> handleObject(element, typeData.getKey(), typeData.getValue(), serializerAnn, config))
						.collect(Collectors.toList());
		}

		public Map<Object, Object> handleMapDeserialization(Map<?, ?> value, Type valueType, Serializer serializerAnn, EnhancedConfig config) {
			Map.Entry<Class<?>, Type[]> typeData = extractTypeData(valueType);
			Map<Object, Object> result = new HashMap<>();

			value.forEach((k, v) -> {
				result.put(k, handleObject(v, typeData.getKey(), typeData.getValue(), serializerAnn, config));
			});

			return result;
		}

		@SuppressWarnings("unchecked")
		private <T> Object useDeserializer(ISerializer<T> serializer, Object obj, Class<?> targetClass, EnhancedConfig config) {
			if (serializer.convertToSection() && !(obj instanceof MemorySection)) {
				obj = SectionUtil.create((Map<?, ?>) obj);
			}
			return serializer.deserialize((T) obj, (Class<? extends T>) targetClass, config);
		}

		private Map.Entry<Class<?>, Type[]> extractTypeData(Type type) {
			Class<?> clazz;
			Type[] typeArgs;
			if (type instanceof Class) {
				clazz = (Class<?>) type;
				typeArgs = null;
			} else if (type instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) type;
				clazz = (Class<?>) pt.getRawType();
				typeArgs = pt.getActualTypeArguments();
			} else {
				clazz = null;
				typeArgs = null;
			}
			return new AbstractMap.SimpleEntry<>(clazz, typeArgs);
		}
	}
}
