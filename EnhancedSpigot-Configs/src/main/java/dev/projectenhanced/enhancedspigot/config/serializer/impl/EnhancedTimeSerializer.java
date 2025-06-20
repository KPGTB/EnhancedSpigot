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
import dev.projectenhanced.enhancedspigot.util.time.EnhancedTime;

public class EnhancedTimeSerializer implements ISerializer<EnhancedTime> {
	@Override
	public Object serialize(EnhancedTime object, Class<? extends EnhancedTime> objectClass, EnhancedConfig config) {
		return object.toString();
	}

	@Override
	public void serializeTo(EnhancedTime object, Class<? extends EnhancedTime> objectClass, EnhancedConfig config, Object to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public EnhancedTime deserialize(Object serialized, Class<? extends EnhancedTime> targetClass, EnhancedConfig config) {
		return new EnhancedTime(String.valueOf(serialized));
	}

	@Override
	public void deserializeTo(Object serialized, Class<? extends EnhancedTime> targetClass, EnhancedConfig config, Object to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean convertToSection() {
		return false;
	}
}
