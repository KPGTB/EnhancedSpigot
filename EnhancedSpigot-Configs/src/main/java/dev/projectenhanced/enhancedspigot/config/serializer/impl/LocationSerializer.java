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
import dev.projectenhanced.enhancedspigot.config.util.SectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class LocationSerializer implements ISerializer<Location> {

	@Override
	public Object serialize(Location object, Class<? extends Location> objectClass, EnhancedConfig config) {
		ConfigurationSection section = SectionUtil.createEmpty();
		this.serializeTo(object, objectClass, config, section);
		return section;
	}

	@Override
	public void serializeTo(Location object, Class<? extends Location> objectClass, EnhancedConfig config, Object to) {
		ConfigurationSection section = (ConfigurationSection) to;
		section.set("world", object.getWorld()
								   .getName());
		section.set("x", object.getX());
		section.set("y", object.getY());
		section.set("z", object.getZ());
		section.set("yaw", object.getYaw());
		section.set("pitch", object.getPitch());
	}

	@Override
	public Location deserialize(Object serialized, Class<? extends Location> targetClass, EnhancedConfig config) {
		ConfigurationSection section = (ConfigurationSection) serialized;
		return new Location(Bukkit.getWorld(section.getString("world")), section.getDouble("x"), section.getDouble("y"), section.getDouble("z"), (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
	}

	@Override
	public void deserializeTo(Object serialized, Class<? extends Location> targetClass, EnhancedConfig config, Object to) {
		Location location = (Location) to;
		ConfigurationSection section = (ConfigurationSection) serialized;

		location.setWorld(Bukkit.getWorld(section.getString("world")));
		location.setX(section.getDouble("x"));
		location.setY(section.getDouble("y"));
		location.setZ(section.getDouble("z"));
		location.setYaw((float) section.getDouble("yaw"));
		location.setPitch((float) section.getDouble("pitch"));
	}

	@Override
	public boolean convertToSection() {
		return true;
	}
}
