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

package dev.projectenhanced.enhancedspigot.util.listener;

import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
import dev.projectenhanced.enhancedspigot.util.ReflectionUtil;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ListenerRegistry {
	public static void register(JavaPlugin plugin, String listenersPackage) {
		register(null, plugin, listenersPackage);
	}

	public static void register(DependencyProvider dependencyProvider, JavaPlugin plugin, String listenersPackage) {
		PluginManager pluginManager = Bukkit.getPluginManager();
		ReflectionUtil.getAllClassesInPackage(ReflectionUtil.getJarFile(plugin), listenersPackage, EnhancedListener.class)
			.forEach(clazz -> {
				EnhancedListener listener;
				if (dependencyProvider != null) {
					listener = TryCatchUtil.tryAndReturn(() -> (EnhancedListener) clazz.getDeclaredConstructor(DependencyProvider.class)
						.newInstance(dependencyProvider));
				} else {
					listener = TryCatchUtil.tryAndReturn(() -> (EnhancedListener) clazz.getDeclaredConstructor(JavaPlugin.class)
						.newInstance(plugin));
				}
				pluginManager.registerEvents(listener, plugin);
			});
	}
}
