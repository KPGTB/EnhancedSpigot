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
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldSerializer implements ISerializer<World> {

	@Override
	public Object serialize(World object, Class<? extends World> objectClass, EnhancedConfig config) {
		return object.getName();
	}

	@Override
	public void serializeTo(World object, Class<? extends World> objectClass, EnhancedConfig config, Object to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public World deserialize(Object serialized, Class<? extends World> targetClass, EnhancedConfig config) {
		return Bukkit.getWorld(String.valueOf(serialized));
	}

	@Override
	public void deserializeTo(Object serialized, Class<? extends World> targetClass, EnhancedConfig config, Object to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean convertToSection() {
		return false;
	}
}
