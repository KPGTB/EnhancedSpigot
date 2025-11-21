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

package dev.projectenhanced.enhancedspigot.util.item;

import com.mojang.authlib.GameProfile;
import dev.projectenhanced.enhancedspigot.util.SemanticVersion;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkullUtil {
	private static final class Holder {
		private static final SkullUtil INSTANCE = new SkullUtil();
	}

	private final Map<String, GameProfile> cache = new HashMap<>();

	public static SkullUtil getInstance() {
		return Holder.INSTANCE;
	}

	public static boolean isUsingNewHead() {
		return SemanticVersion.getMinecraftVersion()
			.isNewerOrEqual("1.13");
	}

	public static boolean hasHeadProfile() {
		return SemanticVersion.getMinecraftVersion()
			.isNewerOrEqual("1.18.1");
	}

	public void fromName(SkullMeta meta, String name) {
		if (isUsingNewHead()) {
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(name));
		} else {
			meta.setOwner(name);
		}
	}

	public void fromUrl(SkullMeta meta, String url) {
		this.fromB64(
			meta, Base64.getEncoder()
				.encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes())
		);
	}

	public void fromB64(SkullMeta meta, String b64) {
		TryCatchUtil.tryRun(
			() -> {
				Method method = meta.getClass()
					.getDeclaredMethod("setProfile", GameProfile.class);
				method.setAccessible(true);
				method.invoke(meta, makeProfile(b64));
				method.setAccessible(false);
			}, (e) -> {
				TryCatchUtil.tryRun(() -> {
					Object profile = makeProfile(b64);

					if (SemanticVersion.getMinecraftVersion()
						.isNewerOrEqual("1.21")) {
						profile = Class.forName("net.minecraft.world.item.component.ResolvableProfile")
							.getConstructor(GameProfile.class)
							.newInstance((GameProfile) profile);
					}

					Field field = meta.getClass()
						.getDeclaredField("profile");
					field.setAccessible(true);
					field.set(meta, profile);
					field.setAccessible(false);
				});
			}
		);
	}

	private GameProfile makeProfile(String b64) {
		if (this.cache.containsKey(b64)) {
			return cache.get(b64);
		}

		UUID id = new UUID(
			b64.substring(b64.length() - 20)
				.hashCode(), b64.substring(b64.length() - 10)
			.hashCode()
		);

		return TryCatchUtil.tryOrDefault(
			() -> {
				Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
				Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");

				GameProfile fakeProfileInstance = (GameProfile) gameProfileClass.getConstructor(UUID.class, String.class)
					.newInstance(id, "es");
				Object propertyInstance = propertyClass.getConstructor(String.class, String.class)
					.newInstance("textures", b64);

				Method getProperties = fakeProfileInstance.getClass()
					.getMethod("getProperties");
				Object propertyMap = getProperties.invoke(fakeProfileInstance);

				Method putMethod = propertyMap.getClass()
					.getMethod("put", Object.class, Object.class);
				putMethod.invoke(propertyMap, "textures", propertyInstance);

				cache.put(b64, fakeProfileInstance);
				return fakeProfileInstance;
			}, null
		);
	}
}
