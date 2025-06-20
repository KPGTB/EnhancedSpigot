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
import dev.projectenhanced.enhancedspigot.locale.EnhancedLocale;
import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
import dev.projectenhanced.enhancedspigot.util.SchedulerUtil;
import dev.projectenhanced.enhancedspigot.util.SemanticVersion;
import dev.projectenhanced.enhancedspigot.util.listener.ListenerRegistry;
import dev.projectenhanced.enhancedspigot.util.updater.IUpdater;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class EnhancedPlugin extends JavaPlugin {
	@Getter
	private DependencyProvider dependencyProvider;

	@Override
	public final void onEnable() {
		this.dependencyProvider = new DependencyProvider();
		this.dependencyProvider.register(
			this, getClass(), EnhancedPlugin.class,
			JavaPlugin.class
		);

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

	protected CommandController enableCommands(CommandLocale locale) {
		CommandController controller = new CommandController(
			this, locale, this.getFile(), this.getTag());
		controller.init();
		this.dependencyProvider.register(controller);
		return controller;
	}

	protected DatabaseController enableDatabase(DatabaseOptions options) {
		DatabaseController controller = new DatabaseController(
			this, this.getFile(), options);
		controller.connect();
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

	protected void registerListeners(Package listenerPackage) {
		ListenerRegistry.register(
			this.dependencyProvider, this, this.getFile(), listenerPackage);
	}

	protected void useBStats(int serviceId) {
		new Metrics(this, serviceId);
	}

	protected void useUpdater(IUpdater updater) {
		if (updater.hasUpdate(new SemanticVersion(this.getDescription()
													  .getVersion()))) {
			this.getLogger()
				.warning(
					"Detected new version of " + this.getName() + " plugin. Download it on " + updater.getDownloadLink());
		}
	}

	public String getTag() {
		return this.getName()
				   .toLowerCase();
	}
}
