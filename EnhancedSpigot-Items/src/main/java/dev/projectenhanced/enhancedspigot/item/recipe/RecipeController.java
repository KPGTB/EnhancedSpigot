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

package dev.projectenhanced.enhancedspigot.item.recipe;

import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
import dev.projectenhanced.enhancedspigot.util.ReflectionUtil;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RecipeController {
	private final DependencyProvider provider;
	private final JavaPlugin plugin;
	private final String pluginTag;
	private final File jarFile;

	/**
	 * Constructor of manager
	 *
	 * @param provider DependencyProvider
	 */
	public RecipeController(DependencyProvider provider) {
		this.provider = provider;
		this.plugin = provider.provide(JavaPlugin.class);
		this.pluginTag = plugin.getName()
			.toLowerCase()
			.replace("-", "_");
		this.jarFile = ReflectionUtil.getJarFile(plugin);
	}

	/**
	 * Register all recipes from package
	 *
	 * @param recipesPackage Package with recipes
	 */
	public void registerRecipes(String recipesPackage) {
		PluginManager pluginManager = Bukkit.getPluginManager();

		for (Class<?> clazz : ReflectionUtil.getAllClassesInPackage(
			jarFile, recipesPackage, EnhancedRecipe.class)) {
			String recipeName = clazz.getSimpleName()
				.toLowerCase();
			NamespacedKey recipeKey = new NamespacedKey(pluginTag, recipeName);

			EnhancedRecipe recipe = (EnhancedRecipe) TryCatchUtil.tryAndReturn(
				() -> clazz.getDeclaredConstructor(
						NamespacedKey.class,
						DependencyProvider.class
					)
					.newInstance(recipeKey, provider));
			recipe.register();
			pluginManager.registerEvents(recipe, this.plugin);
		}
	}
}
