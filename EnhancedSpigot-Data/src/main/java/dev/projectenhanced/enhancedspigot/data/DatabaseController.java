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

package dev.projectenhanced.enhancedspigot.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.BaseConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;
import dev.projectenhanced.enhancedspigot.common.stereotype.Controller;
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
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;
import org.postgresql.util.PSQLException;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Getter @Setter public class DatabaseController extends Controller {
	private final File jarFile;
	private final DatabaseOptions options;
	private IConnectionHandler handler;
	private BaseConnectionSource source;
	private ExecutorService executor;

	private boolean debug;
	private Map<Class<?>, Dao<?, ?>> daoMap;

	public DatabaseController(JavaPlugin plugin, DatabaseOptions options) {
		super(plugin);
		this.jarFile = ReflectionUtil.getJarFile(plugin);
		this.options = options;

		this.debug = false;
		this.daoMap = new HashMap<>();
	}

	public void connect() {
		switch (this.options.getType()) {
			case MYSQL:
				this.handler = new MySQLConnectionHandler();
				break;
			case POSTGRESQL:
				this.handler = new PostgreSQLConnectionHandler();
				break;
			case SQLITE:
				this.handler = new SQLiteConnectionHandler(this.plugin.getDataFolder());
				break;
			default:
				throw new UnsupportedOperationException("Unsupported connection type: " + this.options.getType()
					.name());
		}
		this.executor = new ThreadPoolExecutor(this.options.getThreads(), this.options.getThreads(), 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>());
		this.handler.retrieveCredentials(this.options);
		this.source = TryCatchUtil.tryAndReturn(() -> this.options.getHikariOptions()
			.isEnabled() ?
			this.handler.connectHikari(this.options.getHikariOptions()) :
			this.handler.connect());
		this.registerDefaultPersisters();

		if (!this.debug) LoggerFactory.setLogBackendFactory(LogBackendType.NULL);
	}

	public void registerEntities(String packageName) {
		if (this.source == null) return;
		ReflectionUtil.getAllClassesInPackage(this.jarFile, packageName)
			.forEach(this::registerEntity);
	}

	public void registerEntity(Class<?> clazz) {
		if (this.source == null) return;
		if (clazz.getDeclaredAnnotation(DatabaseTable.class) == null) return;

		Field idField = Arrays.stream(clazz.getDeclaredFields())
			.filter(f -> {
				DatabaseField ann = f.getDeclaredAnnotation(DatabaseField.class);
				if (ann == null) return false;
				return ann.id() || ann.generatedId() || !ann.generatedIdSequence()
					.isEmpty();
			})
			.findAny()
			.orElse(null);
		if (idField == null) return;

		TryCatchUtil.tryRun(
			() -> TableUtils.createTableIfNotExists(this.source, clazz), (ex) -> {
				Throwable cause = ex.getCause();
				if (cause instanceof PSQLException && cause.getMessage()
					.contains("already exists")) {
					return;
				}

				this.plugin.getLogger()
					.log(Level.SEVERE, "Something went wrong!", ex);
			}
		);

		Dao<?, ?> dao = TryCatchUtil.tryAndReturn(() -> DaoManager.createDao(this.source, clazz));
		TryCatchUtil.tryRun(() -> dao.setObjectCache(false));
		this.daoMap.put(clazz, dao);
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
			.map(clazz -> TryCatchUtil.tryAndReturn(() -> (DataPersister) clazz.getDeclaredConstructor()
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
		registerPersisters(new ItemStackPersister(), new ListPersister(), new LocationPersister(), new MapPersister(), new OfflinePlayerPersister(), new WorldPersister());
	}

	@Override
	public void start() {

	}

	@Override
	public void reload() {

	}

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
}
