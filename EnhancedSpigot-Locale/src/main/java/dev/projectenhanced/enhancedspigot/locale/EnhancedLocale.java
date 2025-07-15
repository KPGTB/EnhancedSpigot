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

package dev.projectenhanced.enhancedspigot.locale;

import dev.projectenhanced.enhancedspigot.locale.bridge.IPlatformBridge;
import dev.projectenhanced.enhancedspigot.locale.bridge.SpigotBridge;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import dev.projectenhanced.enhancedspigot.util.lifecycle.IClosable;
import dev.projectenhanced.enhancedspigot.util.lifecycle.IReloadable;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public abstract class EnhancedLocale implements IClosable, IReloadable {
	private final JavaPlugin plugin;
	private final LocaleSerializer serializer;
	private final File folder;
	private File file;
	private YamlConfiguration configuration;

	@Getter private String locale;
	@Getter private IPlatformBridge bridge;

	public EnhancedLocale(JavaPlugin plugin, String locale) {
		this.plugin = plugin;
		this.folder = new File(plugin.getDataFolder(), "locales");
		this.locale = locale;
		this.serializer = new LocaleSerializer();
	}

	/**
	 * Creates locale file and loads configuration
	 */
	public void init() {
		this.init(true);
	}

	protected void init(boolean main) {
		if (main) {
			/*
			if (Bukkit.getName()
					  .contains("Spigot")) bridge = new SpigotBridge(
				this.plugin);
			else bridge = new PaperBridge();
			 */
			this.bridge = new SpigotBridge(this.plugin);

			String trueLocale = this.locale;
			this.supportedLocales()
				.stream()
				.filter(s -> !s.equalsIgnoreCase(this.locale))
				.forEach(s -> {
					this.locale = s;
					this.file = new File(folder, locale + ".yml");
					init(false);
				});

			this.locale = trueLocale;
			this.file = new File(folder, locale + ".yml");
		}

		boolean newLocale = !this.file.exists();
		if (newLocale) {
			this.file.getParentFile()
				.mkdirs();
			TryCatchUtil.tryRun(this.file::createNewFile);
		}

		this.configuration = YamlConfiguration.loadConfiguration(this.file);

		if (newLocale) this.save();
		if (main) this.reload();
	}

	/**
	 * Reloads locales
	 */
	@Override
	public void reload() {
		this.configuration = YamlConfiguration.loadConfiguration(this.file);
		this.serializer.deserializeTo(
			this.configuration, this.getClass(), this, this);
		this.save();
	}

	/**
	 * Saves locales
	 */
	public void save() {
		this.serializer.serializeTo(
			this, this.getClass(), this, this.configuration);
		TryCatchUtil.tryRun(() -> this.configuration.save(this.file));
	}

	@Override
	public void close() {
		if (this.bridge != null) this.bridge.close();
	}

	public abstract List<String> supportedLocales();

	public abstract LocaleObject getPrefix();
}
