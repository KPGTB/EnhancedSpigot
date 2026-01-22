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

package dev.projectenhanced.enhancedspigot.item.recipe;

import dev.projectenhanced.enhancedspigot.common.IDependencyProvider;
import dev.projectenhanced.enhancedspigot.common.stereotype.Controller;
import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
import dev.projectenhanced.enhancedspigot.util.ReflectionUtil;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RecipeController extends Controller {
	private final String pluginTag;
	private final File jarFile;

	public RecipeController(JavaPlugin plugin) {
		super(plugin);

		this.pluginTag = this.plugin.getName()
			.toLowerCase()
			.replace("-", "_");
		this.jarFile = ReflectionUtil.getJarFile(this.plugin);
	}

	public RecipeController(DependencyProvider provider) {
		super(provider);

		this.pluginTag = this.plugin.getName()
			.toLowerCase()
			.replace("-", "_");
		this.jarFile = ReflectionUtil.getJarFile(this.plugin);
	}

	/**
	 * Register all recipes from package
	 *
	 * @param recipesPackage Package with recipes
	 */
	public void registerRecipes(String recipesPackage) {
		PluginManager pluginManager = Bukkit.getPluginManager();

		for (Class<?> clazz : ReflectionUtil.getAllClassesInPackage(jarFile, recipesPackage, EnhancedRecipe.class)) {
			String recipeName = clazz.getSimpleName()
				.toLowerCase();
			NamespacedKey recipeKey = new NamespacedKey(pluginTag, recipeName);

			EnhancedRecipe recipe;
			if (this.dependencyProvider != null) {
				recipe = (EnhancedRecipe) TryCatchUtil.tryAndReturn(() -> clazz.getDeclaredConstructor(NamespacedKey.class, IDependencyProvider.class)
					.newInstance(recipeKey, this.dependencyProvider));
			} else {
				recipe = (EnhancedRecipe) TryCatchUtil.tryAndReturn(() -> clazz.getDeclaredConstructor(NamespacedKey.class, JavaPlugin.class)
					.newInstance(recipeKey, this.plugin));
			}

			recipe.register();
			pluginManager.registerEvents(recipe, this.plugin);
		}
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
