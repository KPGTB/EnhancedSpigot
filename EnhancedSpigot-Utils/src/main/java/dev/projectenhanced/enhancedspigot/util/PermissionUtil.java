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

package dev.projectenhanced.enhancedspigot.util;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Comparator;
import java.util.function.Function;

public class PermissionUtil {
	/**
	 * Retrieves a value from a player's permissions based on a specified prefix.
	 *
	 * @param player           The player whose permissions are to be checked.
	 * @param permissionPrefix The prefix of the permission to look for.
	 * @param def              The default value to return if no valid permission is found.
	 * @param converter        A function to convert the permission string to the desired type.
	 * @param comparator       A comparator to determine the "highest" value among found permissions.
	 * @param <T>              The type of the value to be returned.
	 * @return The highest value found from the player's permissions with the specified prefix, or the default value if none found.
	 */
	public static <T> T getValueFromPermission(Player player, String permissionPrefix, T def, Function<String, T> converter, Comparator<T> comparator) {
		T result = def;

		for (PermissionAttachmentInfo permInfo : player.getEffectivePermissions()) {
			if (!permInfo.getValue()) continue;
			String permission = permInfo.getPermission();
			if (!permission.startsWith(permissionPrefix)) continue;

			String rawValue = permission.substring(permissionPrefix.length());
			T value;
			try {
				value = converter.apply(rawValue);
			} catch (Exception ignored) {
				continue;
			}

			if (comparator.compare(value, result) > 0) result = value;
		}

		return result;
	}
}
