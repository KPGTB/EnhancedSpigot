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

package dev.projectenhanced.enhancedspigot.config.serializer.adapter;

import dev.projectenhanced.enhancedspigot.config.annotation.Serializer;
import dev.projectenhanced.enhancedspigot.config.serializer.impl.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@Serializer(BaseSerializer.class) @AllArgsConstructor @NoArgsConstructor @Getter @Setter public class LocationAdapter {
	private String world;
	private double x;
	private double y;
	private double z;
	private double yaw;
	private double pitch;

	public LocationAdapter(String world, double x, double y, double z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = 0.0;
		this.pitch = 0.0;
	}

	public static LocationAdapter fromBukkit(Location bukkit) {
		return new LocationAdapter(
			bukkit.getWorld()
				.getName(), bukkit.getX(), bukkit.getY(), bukkit.getZ(), bukkit.getYaw(), bukkit.getPitch()
		);
	}

	public Location asBukkitLocation() {
		return new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z, (float) this.yaw, (float) this.pitch);
	}
}
