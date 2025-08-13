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

package dev.projectenhanced.enhancedspigot.plugin.hook;

import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultHook {
	private final DependencyProvider provider;
	private final JavaPlugin plugin;

	protected VaultHook(DependencyProvider provider) {
		this.provider = provider;
		this.plugin = provider.provide(JavaPlugin.class);
	}

	public static VaultHook in(DependencyProvider provider) {
		return new VaultHook(provider);
	}

	public VaultHook economyHook(boolean optional) {
		this.vaultHook(Economy.class, "economy", optional);
		return this;
	}

	public VaultHook chatHook(boolean optional) {
		this.vaultHook(Chat.class, "chat", optional);
		return this;
	}

	public VaultHook permissionsHook(boolean optional) {
		this.vaultHook(Permission.class, "permissions", optional);
		return this;
	}

	private boolean dependencyCheck(boolean optional) {
		if (Bukkit.getServer()
			.getPluginManager()
			.getPlugin("Vault") == null) {
			if (!optional) {
				this.plugin.getLogger()
					.severe("Vault is required on this server");
				Bukkit.getPluginManager()
					.disablePlugin(this.plugin);
			}
			return false;
		}
		return true;
	}

	private <T> T serviceCheck(RegisteredServiceProvider<T> rsp, boolean optional, String serviceName) {
		if (rsp == null) {
			if (!optional) {
				this.plugin.getLogger()
					.severe(
						"Vault requires other plugin which can provide " + serviceName + " service!");
				Bukkit.getPluginManager()
					.disablePlugin(this.plugin);
			}
			return null;
		}
		return rsp.getProvider();
	}

	private <T> void vaultHook(Class<T> clazz, String serviceName, boolean optional) {
		if (!this.dependencyCheck(optional)) return;
		RegisteredServiceProvider<T> rsp = Bukkit.getServer()
			.getServicesManager()
			.getRegistration(clazz);
		T service = this.serviceCheck(rsp, optional, serviceName);
		if (service != null) this.provider.register(service, clazz);
	}
}
