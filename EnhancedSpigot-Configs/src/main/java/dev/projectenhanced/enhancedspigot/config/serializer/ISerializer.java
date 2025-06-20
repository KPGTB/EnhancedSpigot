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

package dev.projectenhanced.enhancedspigot.config.serializer;

import dev.projectenhanced.enhancedspigot.config.EnhancedConfig;

/**
 * EnhancedConfig Serializer
 *
 * @param <T>
 */
public interface ISerializer<T> {
	/**
	 * Serializes an object
	 *
	 * @param object      Object to serialize
	 * @param objectClass Class of that object
	 * @param config      Config instance
	 * @return Serialized object
	 */
	Object serialize(T object, Class<? extends T> objectClass, EnhancedConfig config);

	/**
	 * Serializes an object to another object
	 *
	 * @param object      Object to serialize
	 * @param objectClass Class of that object
	 * @param config      Config instance
	 * @param to          Usually ConfigurationSection
	 */
	void serializeTo(T object, Class<? extends T> objectClass, EnhancedConfig config, Object to);

	/**
	 * Deserializes an object
	 *
	 * @param serialized  Serialized object
	 * @param targetClass Class to which that object should be deserialized
	 * @param config      Config instance
	 * @return Deserialized object
	 */
	T deserialize(Object serialized, Class<? extends T> targetClass, EnhancedConfig config);

	/**
	 * Deserializes an object to another object
	 *
	 * @param serialized  Serialized object
	 * @param targetClass Class to which that object should be deserialized
	 * @param config      Config instance
	 * @param to          Object to which it should be deserialized
	 */
	void deserializeTo(Object serialized, Class<? extends T> targetClass, EnhancedConfig config, Object to);

	/**
	 * @return true if serialized parameter should be converted to ConfigurationSection
	 */
	boolean convertToSection();
}
