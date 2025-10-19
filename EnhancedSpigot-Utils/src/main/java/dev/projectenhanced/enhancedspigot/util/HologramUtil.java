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

package dev.projectenhanced.enhancedspigot.util;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;

public class HologramUtil {
	public static final double LINE_HEIGHT = 0.25;
	public static final double SCAN_AREA = 10.0;

	private static final String ID_KEY = "es_holo_id";
	private static final String LINE_KEY = "es_holo_line";

	public static String createHologram(JavaPlugin plugin, Location location, List<String> lines) {
		String genId = "es_holo_" + System.currentTimeMillis() + "_" + Math.random();
		createHologram(plugin, genId, location, lines);
		return genId;
	}

	public static void createHologram(JavaPlugin plugin, String id, Location location, List<String> lines) {
		Location entityLoc = location.clone()
			.add(0, LINE_HEIGHT * lines.size(), 0);

		for (int i = 0; i < lines.size(); i++) {
			ArmorStand armorStand = entityLoc.getWorld()
				.spawn(entityLoc.clone(), ArmorStand.class);

			armorStand.setSmall(true);
			armorStand.setInvisible(true);
			armorStand.setMarker(true);
			armorStand.setAI(false);
			armorStand.setInvulnerable(true);
			armorStand.setGravity(false);
			armorStand.setVisible(false);
			armorStand.setCustomNameVisible(true);
			armorStand.setCustomName(lines.get(i));

			PDCUtil.setData(armorStand, plugin, ID_KEY, id);
			PDCUtil.setData(armorStand, plugin, LINE_KEY, i);

			entityLoc.subtract(0, LINE_HEIGHT, 0);
		}
	}

	public static List<ArmorStand> getEntities(JavaPlugin plugin, Location location) {
		return getEntities(plugin, null, location);
	}

	public static List<ArmorStand> getEntities(JavaPlugin plugin, String id, Location location) {
		return location.getWorld()
			.getNearbyEntities(location, SCAN_AREA, SCAN_AREA, SCAN_AREA)
			.stream()
			.filter(e -> isHologram(plugin, e) && (id == null || getHologramId(plugin, e).equalsIgnoreCase(id)))
			.map(e -> (ArmorStand) e)
			.collect(Collectors.toList());
	}

	public static void removeHologram(JavaPlugin plugin, String id, Location location) {
		getEntities(plugin, id, location).forEach(ArmorStand::remove);
	}

	public static void removeHologram(JavaPlugin plugin, Location location) {
		getEntities(plugin, location).forEach(ArmorStand::remove);
	}

	public static void updateHologram(JavaPlugin plugin, String id, Location location, List<String> lines) {
		removeHologram(plugin, id, location);
		createHologram(plugin, id, location, lines);
	}

	public static boolean isHologram(JavaPlugin plugin, Entity entity) {
		return entity instanceof ArmorStand && PDCUtil.hasData(entity, plugin, ID_KEY, String.class) && PDCUtil.hasData(entity, plugin, LINE_KEY, Integer.class);
	}

	public static String getHologramId(JavaPlugin plugin, Entity entity) {
		if (!isHologram(plugin, entity)) return null;
		return PDCUtil.getData(entity, plugin, ID_KEY, String.class);
	}

	public static Integer getHologramLineNumber(JavaPlugin plugin, Entity entity) {
		if (!isHologram(plugin, entity)) return -1;
		return PDCUtil.getData(entity, plugin, LINE_KEY, Integer.class);
	}
}
