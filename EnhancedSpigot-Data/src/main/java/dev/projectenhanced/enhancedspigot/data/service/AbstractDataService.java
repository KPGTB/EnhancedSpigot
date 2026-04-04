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

package dev.projectenhanced.enhancedspigot.data.service;

import dev.projectenhanced.enhancedspigot.common.IDependencyProvider;
import dev.projectenhanced.enhancedspigot.common.stereotype.Service;
import dev.projectenhanced.enhancedspigot.data.DatabaseController;
import dev.projectenhanced.enhancedspigot.data.repository.entity.AbstractDataEntity;
import dev.projectenhanced.enhancedspigot.data.repository.iface.IAsyncDataRepository;
import dev.projectenhanced.enhancedspigot.data.repository.iface.IDataRepository;
import dev.projectenhanced.enhancedspigot.data.storage.IDataStorage;
import dev.projectenhanced.enhancedspigot.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractDataService<K, V extends AbstractDataEntity<K>> extends Service implements Listener {
	protected DatabaseController databaseController;

	protected IDataStorage<K, V> storage;
	protected IDataRepository<K, V> repository;
	protected SchedulerUtil.Task saveTask;

	public AbstractDataService(JavaPlugin plugin) {
		super(plugin);
		this.storage = this.initStorage();
		this.repository = this.initRepository();
		Bukkit.getPluginManager()
			.registerEvents(this, plugin);
	}

	public AbstractDataService(IDependencyProvider dependencyProvider) {
		super(dependencyProvider);
		this.databaseController = this.dependencyProvider.provide(DatabaseController.class);
		this.storage = this.initStorage();
		this.repository = this.initRepository();
		Bukkit.getPluginManager()
			.registerEvents(this, this.plugin);
	}

	protected abstract IDataStorage<K, V> initStorage();

	protected abstract IDataRepository<K, V> initRepository();

	protected void autosave(int ticks) {
		this.saveTask = SchedulerUtil.runTaskLater(
			this.plugin, () -> {
				if (this.repository instanceof IAsyncDataRepository<K, V>) ((IAsyncDataRepository<?, ?>) this.repository).saveAsyncAll();
				else this.repository.saveAll();

				this.autosave(ticks);
			}, ticks
		);
	}

	@Override
	public abstract void start();

	@Override
	public abstract void reload();

	@Override
	public abstract void close();

	@Override
	public int closeOrder() {
		return Integer.MAX_VALUE;
	}
}
