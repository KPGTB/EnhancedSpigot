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

package dev.projectenhanced.enhancedspigot.plugin;

import dev.projectenhanced.enhancedspigot.command.CommandController;
import dev.projectenhanced.enhancedspigot.command.CommandLocale;
import dev.projectenhanced.enhancedspigot.config.EnhancedConfig;
import dev.projectenhanced.enhancedspigot.data.DatabaseController;
import dev.projectenhanced.enhancedspigot.data.connection.DatabaseOptions;
import dev.projectenhanced.enhancedspigot.item.ItemController;
import dev.projectenhanced.enhancedspigot.item.recipe.RecipeController;
import dev.projectenhanced.enhancedspigot.locale.EnhancedLocale;
import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
import dev.projectenhanced.enhancedspigot.util.SchedulerUtil;
import dev.projectenhanced.enhancedspigot.util.SemanticVersion;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import dev.projectenhanced.enhancedspigot.util.listener.ListenerRegistry;
import dev.projectenhanced.enhancedspigot.util.updater.IUpdater;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

public abstract class EnhancedPlugin extends JavaPlugin {
	@Getter protected DependencyProvider dependencyProvider;

	@Override
	public final void onEnable() {
		TryCatchUtil.usePluginLogger(this);

		this.dependencyProvider = new DependencyProvider();
		this.dependencyProvider.register(this, getClass(), EnhancedPlugin.class, JavaPlugin.class);

		boolean passRequirements = true;
		for (String requiredPlugin : this.requiredPlugins()) {
			if (!Bukkit.getPluginManager()
				.isPluginEnabled(requiredPlugin)) {
				passRequirements = false;
				this.getLogger()
					.severe("This plugin requires " + requiredPlugin + " to be enabled.");
			}
		}
		if (!passRequirements) {
			Bukkit.getPluginManager()
				.disablePlugin(this);
			return;
		}

		load();
		SchedulerUtil.runTaskLater(this, (task) -> postLoad(), 1);
	}

	@Override
	public final void onDisable() {
		unload();
		this.dependencyProvider.closeAll();
	}

	public abstract void load();

	public abstract void postLoad();

	public void reload() {
		this.dependencyProvider.reloadAll();
		this.reloadImpl();
	}

	protected abstract void reloadImpl();

	public abstract void unload();

	protected abstract List<String> requiredPlugins();

	protected CommandController enableCommands(CommandLocale locale) {
		CommandController controller = new CommandController(this.dependencyProvider, locale);
		controller.init();
		this.dependencyProvider.register(controller);
		return controller;
	}

	protected DatabaseController enableDatabase(DatabaseOptions options) {
		DatabaseController controller = new DatabaseController(this, options);
		if (this.getLogger()
			.getLevel() == Level.FINE) controller.setDebug(true);
		controller.connect();
		this.dependencyProvider.register(controller);
		return controller;
	}

	protected RecipeController enableRecipes() {
		RecipeController controller = new RecipeController(this.dependencyProvider);
		this.dependencyProvider.register(controller);
		return controller;
	}

	protected ItemController enableCustomItems() {
		ItemController controller = new ItemController(this.dependencyProvider);
		this.dependencyProvider.register(controller);
		return controller;
	}

	protected <T extends EnhancedLocale> T useLocale(T locale) {
		locale.init();
		this.dependencyProvider.register(locale);
		return locale;
	}

	protected <T extends EnhancedConfig> T useConfiguration(T config) {
		config.init();
		this.dependencyProvider.register(config);
		return config;
	}

	protected void registerListeners(String listenerPackage) {
		ListenerRegistry.register(this.dependencyProvider, this, listenerPackage);
	}

	protected Metrics useBStats(int serviceId) {
		return new Metrics(this, serviceId);
	}

	protected void useUpdater(IUpdater updater) {
		if (updater.hasUpdate(new SemanticVersion(this.getDescription()
			.getVersion()))) {
			this.getLogger()
				.warning("Detected new version of " + this.getName() + " plugin. Download it on " + updater.getDownloadLink());
		}
	}

	protected void showDebug() {
		DebugLogHandler.attachDebugLogger(this);
		this.getLogger()
			.setLevel(Level.FINE);
	}

	public String getTag() {
		return this.getName()
			.toLowerCase();
	}
}
