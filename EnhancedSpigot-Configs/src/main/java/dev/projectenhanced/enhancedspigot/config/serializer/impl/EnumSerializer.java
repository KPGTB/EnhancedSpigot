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
import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;

public class EnumSerializer implements ISerializer<Enum<?>> {
	@Override
	public Object serialize(Enum<?> object, Class<? extends Enum<?>> objectClass, EnhancedConfig config) {
		return object.name();
	}

	@Override
	public void serializeTo(Enum<?> object, Class<? extends Enum<?>> objectClass, EnhancedConfig config, Object to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enum<?> deserialize(Object serialized, Class<? extends Enum<?>> targetClass, EnhancedConfig config) {
		return this.getEnumFromName(targetClass, String.valueOf(serialized)
													   .toUpperCase());
	}

	@Override
	public void deserializeTo(Object serialized, Class<? extends Enum<?>> targetClass, EnhancedConfig config, Object to) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	private <T extends Enum<T>> T getEnumFromName(Class<?> clazz, String name) {
		return Enum.valueOf((Class<T>) clazz, name);
	}

	@Override
	public boolean convertToSection() {
		return false;
	}
}
