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

package dev.projectenhanced.enhancedspigot.item;

import dev.projectenhanced.enhancedspigot.common.IDependencyProvider;
import dev.projectenhanced.enhancedspigot.common.stereotype.Controller;
import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
import dev.projectenhanced.enhancedspigot.util.ReflectionUtil;
import dev.projectenhanced.enhancedspigot.util.TextCase;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemController extends Controller {
	private final File jarFile;

	@Getter private final Map<String, EnhancedItem> customItems;

	public ItemController(JavaPlugin plugin) {
		super(plugin);
		this.jarFile = ReflectionUtil.getJarFile(plugin);
		this.customItems = new HashMap<>();
	}

	public ItemController(DependencyProvider provider) {
		super(provider);
		this.jarFile = ReflectionUtil.getJarFile(this.plugin);
		this.customItems = new HashMap<>();
	}

	/**
	 * Register all items from package
	 *
	 * @param itemsPackage Package that should be scanned
	 * @return List of items tag
	 */
	public List<String> registerItems(String itemsPackage) {
		PluginManager pluginManager = Bukkit.getPluginManager();
		List<String> tags = new ArrayList<>();

		for (Class<?> clazz : ReflectionUtil.getAllClassesInPackage(jarFile, itemsPackage, EnhancedItem.class)) {
			try {
				String itemName = TextCase.camelToSnakeCase(clazz.getSimpleName()
					.replace("Item", ""));

				EnhancedItem item;
				if (this.dependencyProvider != null) {
					item = (EnhancedItem) clazz.getDeclaredConstructor(IDependencyProvider.class, String.class)
						.newInstance(this.dependencyProvider, itemName);
				} else {
					item = (EnhancedItem) clazz.getDeclaredConstructor(JavaPlugin.class, String.class)
						.newInstance(this.plugin, itemName);
				}

				pluginManager.registerEvents(item, this.plugin);
				this.customItems.put(itemName, item);
				tags.add(itemName);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return tags;
	}

	/**
	 * Register item
	 *
	 * @param item Instance of EnhancedItem
	 * @return tag of item
	 */
	public String registerItem(EnhancedItem item) {
		PluginManager pluginManager = Bukkit.getPluginManager();

		pluginManager.registerEvents(item, this.plugin);
		this.customItems.put(item.getFullItemTag(), item);
		return item.getFullItemTag();
	}

	/**
	 * Unregister item
	 *
	 * @param fullItemName Full name of item
	 */
	public void unregisterItem(String fullItemName) {
		if (!this.customItems.containsKey(fullItemName)) {
			return;
		}
		EnhancedItem item = this.customItems.get(fullItemName);
		this.customItems.remove(fullItemName);
		HandlerList.unregisterAll(item);
	}

	/**
	 * Get custom item
	 *
	 * @param fullItemName Name of registered item
	 * @return ItemStack or null when not exists
	 */
	@Nullable
	public ItemStack getCustomItem(String fullItemName) {
		return customItems.get(fullItemName)
			.getItem()
			.clone();
	}

	/**
	 * Get custom item
	 *
	 * @param itemClass Class that contains item
	 * @return ItemStack or null when not exists
	 */
	@Nullable
	public ItemStack getCustomItem(Class<? extends EnhancedItem> itemClass) {
		String itemName = TextCase.camelToSnakeCase(itemClass.getSimpleName()
			.replace("Item", ""));
		return customItems.get(itemName)
			.getItem()
			.clone();
	}

	/**
	 * Get custom item
	 *
	 * @param fullItemName Name of registered item
	 * @return EnhancedItem or null when not exists
	 */
	@Nullable
	public EnhancedItem getCustomItemObj(String fullItemName) {
		return customItems.get(fullItemName);
	}

	/**
	 * Get custom item
	 *
	 * @param itemClass Class that contains item
	 * @return EnhancedItem or null when not exists
	 */
	@Nullable
	public EnhancedItem getCustomItemObj(Class<? extends EnhancedItem> itemClass) {
		String itemName = TextCase.camelToSnakeCase(itemClass.getSimpleName()
			.replace("Item", ""));
		return customItems.get(itemName);
	}

	@Override
	public void close() {

	}

	@Override
	public void reload() {

	}

	@Override
	public void start() {

	}
}
