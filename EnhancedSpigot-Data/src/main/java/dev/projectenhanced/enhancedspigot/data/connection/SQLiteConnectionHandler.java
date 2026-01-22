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

package dev.projectenhanced.enhancedspigot.data.connection;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.db.SqliteDatabaseType;
import com.j256.ormlite.support.BaseConnectionSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class SQLiteConnectionHandler implements IConnectionHandler {

	private final String fileName = "database.db";
	private final File file;
	private HikariDataSource dataSource;

	public SQLiteConnectionHandler(File dataFolder) {
		this.file = new File(dataFolder, this.fileName);
	}

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.SQLITE;
	}

	@Override
	public void retrieveCredentials(DatabaseOptions options) {
		return;
	}

	@Override
	public BaseConnectionSource connect() throws IOException, SQLException {
		this.file.getParentFile()
			.mkdirs();
		if (!this.file.exists()) {
			this.file.createNewFile();
		}

		String connectionUrl = "jdbc:sqlite:" + this.file.getAbsolutePath();
		return new SqliteConnectionSource(connectionUrl, new SqliteDatabaseType());
	}

	@Override
	public BaseConnectionSource connectHikari(DatabaseOptions.HikariOptions options) throws IOException, SQLException {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:sqlite:" + this.file.getAbsolutePath());
		config.setConnectionInitSql("PRAGMA foreign_keys = ON;");

		TryCatchUtil.tryAndReturn(() -> Class.forName("org.sqlite.JDBC"));
		this.dataSource = new HikariDataSource(HikariHandler.configure(config, options));

		return new DataSourceConnectionSource(this.dataSource, this.dataSource.getJdbcUrl());
	}

	@Override
	public void close() {
		if (dataSource != null) this.dataSource.close();
	}
}
