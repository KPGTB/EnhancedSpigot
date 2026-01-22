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

package dev.projectenhanced.enhancedspigot.data.migration;

import com.j256.ormlite.dao.Dao;
import dev.projectenhanced.enhancedspigot.common.IDependencyProvider;
import dev.projectenhanced.enhancedspigot.common.stereotype.Controller;
import dev.projectenhanced.enhancedspigot.data.DatabaseController;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MigrationController extends Controller {
	private final DatabaseController databaseController;

	private final String tag;
	private final int currentVersion;

	private final Map<Integer, BiConsumer<JavaPlugin, DatabaseController>> migrations;
	private final Dao<MigrationEntity, String> migrationDao;

	public MigrationController(JavaPlugin plugin, DatabaseController databaseController, String tag, int currentVersion) {
		super(plugin);
		this.databaseController = databaseController;
		this.tag = tag;
		this.currentVersion = currentVersion;
		this.migrations = new HashMap<>();

		this.databaseController.registerEntity(MigrationEntity.class);
		this.migrationDao = this.databaseController.getDao(MigrationEntity.class, String.class);
		this.checkAndCreateInfo();
	}

	public MigrationController(IDependencyProvider provider, String tag, int currentVersion) {
		super(provider);
		this.databaseController = provider.provide(DatabaseController.class);
		this.tag = tag;
		this.currentVersion = currentVersion;
		this.migrations = new HashMap<>();

		this.databaseController.registerEntity(MigrationEntity.class);
		this.migrationDao = this.databaseController.getDao(MigrationEntity.class, String.class);
		this.checkAndCreateInfo();
	}

	public MigrationController registerMigration(int version, BiConsumer<JavaPlugin, DatabaseController> migration) {
		if (version > this.currentVersion) throw new IllegalArgumentException("Migration version out of range");
		this.migrations.put(version, migration);
		return this;
	}

	private void checkAndCreateInfo() {
		if (TryCatchUtil.tryAndReturn(() -> this.migrationDao.idExists(tag))) return;

		TryCatchUtil.tryRun(() -> this.migrationDao.create(new MigrationEntity(tag, currentVersion)));
	}

	@Override
	public void close() {

	}

	@Override
	public void reload() {

	}

	@Override
	public void start() {
		this.checkAndCreateInfo();
		MigrationEntity entity = TryCatchUtil.tryAndReturn(() -> this.migrationDao.queryForId(this.tag));
		int dbVersion = entity.getVersion();

		for (int i = dbVersion + 1; i <= this.currentVersion; i++) {
			int ver = i;
			if (!this.migrations.containsKey(ver)) continue;
			TryCatchUtil.tryRun(() -> this.migrations.get(ver)
				.accept(this.plugin, this.databaseController));
		}

		entity.setVersion(this.currentVersion);
		TryCatchUtil.tryRun(() -> this.migrationDao.update(entity));
	}
}
