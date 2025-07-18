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

package dev.projectenhanced.enhancedspigot.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.LruObjectCache;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.BaseConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import dev.projectenhanced.enhancedspigot.data.connection.DatabaseOptions;
import dev.projectenhanced.enhancedspigot.data.connection.IConnectionHandler;
import dev.projectenhanced.enhancedspigot.data.connection.MySQLConnectionHandler;
import dev.projectenhanced.enhancedspigot.data.connection.PostgreSQLConnectionHandler;
import dev.projectenhanced.enhancedspigot.data.connection.SQLiteConnectionHandler;
import dev.projectenhanced.enhancedspigot.data.persister.base.ItemStackPersister;
import dev.projectenhanced.enhancedspigot.data.persister.base.ListPersister;
import dev.projectenhanced.enhancedspigot.data.persister.base.LocationPersister;
import dev.projectenhanced.enhancedspigot.data.persister.base.MapPersister;
import dev.projectenhanced.enhancedspigot.data.persister.base.OfflinePlayerPersister;
import dev.projectenhanced.enhancedspigot.data.persister.base.WorldPersister;
import dev.projectenhanced.enhancedspigot.util.ReflectionUtil;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import dev.projectenhanced.enhancedspigot.util.lifecycle.IClosable;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter @Setter public class DatabaseController implements IClosable {
	private final JavaPlugin plugin;
	private final File jarFile;
	private final DatabaseOptions options;
	private IConnectionHandler handler;
	private BaseConnectionSource source;
	private ExecutorService executor;

	private boolean debug;
	private Map<Class<?>, Dao<?, ?>> daoMap;

	public DatabaseController(JavaPlugin plugin, DatabaseOptions options) {
		this.plugin = plugin;
		this.jarFile = ReflectionUtil.getJarFile(plugin);
		this.options = options;

		this.debug = false;
		this.daoMap = new HashMap<>();
	}

	public void connect() {
		IConnectionHandler handler = null;
		switch (this.options.getType()) {
			case MYSQL:
				this.handler = new MySQLConnectionHandler();
				this.executor = Executors.newFixedThreadPool(10);
				break;
			case POSTGRESQL:
				this.handler = new PostgreSQLConnectionHandler();
				this.executor = Executors.newFixedThreadPool(10);
				break;
			case SQLITE:
				this.handler = new SQLiteConnectionHandler(
					this.plugin.getDataFolder());
				this.executor = Executors.newSingleThreadExecutor();
				break;
			default:
				throw new UnsupportedOperationException(
					"Unsupported connection type: " + this.options.getType()
						.name());
		}
		this.handler.retrieveCredentials(this.options);
		this.source = TryCatchUtil.tryAndReturn(this.handler::connect);
		this.registerDefaultPersisters();

		if (!this.debug) LoggerFactory.setLogBackendFactory(
			LogBackendType.NULL);
	}

	/**
	 * Close database connection
	 */
	@SneakyThrows
	@Override
	public void close() {
		if (this.executor != null) this.executor.shutdownNow();
		if (this.source == null) return;
		this.source.close();
		this.source = null;
		this.handler.close();
		this.daoMap = new HashMap<>();
	}

	public void registerEntities(String packageName) {
		if (this.source == null) return;

		for (Class<?> clazz : ReflectionUtil.getAllClassesInPackage(
			this.jarFile, packageName)) {
			if (clazz.getDeclaredAnnotation(DatabaseTable.class) == null)
				continue;

			Field idField = Arrays.stream(clazz.getDeclaredFields())
				.filter(f -> {
					DatabaseField ann = f.getDeclaredAnnotation(
						DatabaseField.class);
					if (ann == null) return false;
					return ann.id() || ann.generatedId() || !ann.generatedIdSequence()
						.isEmpty();
				})
				.findAny()
				.orElse(null);

			if (idField == null) continue;

			try {
				TableUtils.createTableIfNotExists(this.source, clazz);
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}

			Dao<?, ?> dao;
			try {
				dao = DaoManager.createDao(this.source, clazz);
				int cache = this.options.getCacheCapacity();
				switch (cache) {
					case -1:
						break;
					case 0:
						dao.setObjectCache(true);
						break;
					default:
						dao.setObjectCache(new LruObjectCache(cache));
						break;
				}
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
			this.daoMap.put(clazz, dao);
		}
	}

	@SuppressWarnings("unchecked")
	public <T, Z> Dao<T, Z> getDao(Class<T> daoClass, Class<Z> idClass) {
		if (this.source == null) return null;

		Dao<?, ?> dao = this.daoMap.get(daoClass);
		return dao == null ?
			null :
			(Dao<T, Z>) dao;
	}

	/**
	 * Register all OrmLite persisters from specified package
	 *
	 * @param packageName Package where are stored all persisters
	 */
	public void registerPersisters(String packageName) {
		if (this.source == null) return;
		ReflectionUtil.getAllClassesInPackage(this.jarFile, packageName)
			.stream()
			.filter(DataPersister.class::isAssignableFrom)
			.map(clazz -> TryCatchUtil.tryAndReturn(
				() -> (DataPersister) clazz.getDeclaredConstructor()
					.newInstance()))
			.filter(Objects::nonNull)
			.forEach(DataPersisterManager::registerDataPersisters);
	}

	/**
	 * Register persisters
	 *
	 * @param persisters Persisters instance
	 */
	public void registerPersisters(DataPersister... persisters) {
		DataPersisterManager.registerDataPersisters(persisters);
	}

	private void registerDefaultPersisters() {
		registerPersisters(
			new ItemStackPersister(), new ListPersister(),
			new LocationPersister(), new MapPersister(),
			new OfflinePlayerPersister(), new WorldPersister()
		);
	}
}
