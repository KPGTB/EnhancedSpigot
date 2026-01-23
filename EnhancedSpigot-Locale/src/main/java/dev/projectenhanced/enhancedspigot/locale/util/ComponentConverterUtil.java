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

package dev.projectenhanced.enhancedspigot.locale.util;

import com.google.gson.JsonElement;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class ComponentConverterUtil {
	private static final Object KYORI_GSON_SERIALIZER = TryCatchUtil.tryAndReturn(() -> Class.forName("net.kyori.adventure.text.serializer.gson.GsonComponentSerializer")
		.getMethod("gson")
		.invoke(null));
	private static final Object ENHANCED_GSON_SERIALIZER = GsonComponentSerializer.gson();

	/**
	 * Converts an Enhanced Component to a Kyori Component
	 *
	 * @param enhancedComponent - dev.projectenhanced.enhancedspigot.locale.lib.adventure.text.Component;
	 * @return net.kyori.adventure.text.Component
	 */
	public static Object enhancedToKyori(Component enhancedComponent) {
		return convert(enhancedComponent, KYORI_GSON_SERIALIZER, ENHANCED_GSON_SERIALIZER);
	}

	/**
	 * Converts a Kyori Component to an Enhanced Component
	 *
	 * @param kyoriComponent - net.kyori.adventure.text.Component
	 * @return dev.projectenhanced.enhancedspigot.locale.lib.adventure.text.Component;
	 */
	public static Component kyoriToEnhanced(Object kyoriComponent) {
		return (Component) convert(kyoriComponent, ENHANCED_GSON_SERIALIZER, KYORI_GSON_SERIALIZER);
	}

	private static Object convert(Object component, Object deserializer, Object serializer) {
		return TryCatchUtil.tryAndReturn(() -> {
			JsonElement serialized = (JsonElement) serializer.getClass()
				.getMethod("serializeToTree", component.getClass())
				.invoke(serializer, component);

			return deserializer.getClass()
				.getMethod("deserializeFromTree", JsonElement.class)
				.invoke(deserializer, serialized);
		});
	}

}
