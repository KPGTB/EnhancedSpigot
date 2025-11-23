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

package dev.projectenhanced.enhancedspigot.data.connection;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.SQLException;

/**
 * SqliteConnection Source which adds support for foreign keys.
 */
public class SqliteConnectionSource extends JdbcConnectionSource {
	public SqliteConnectionSource() {
	}

	public SqliteConnectionSource(String url) throws SQLException {
		super(url);
	}

	public SqliteConnectionSource(String url, DatabaseType databaseType) throws SQLException {
		super(url, databaseType);
	}

	public SqliteConnectionSource(String url, String username, String password) throws SQLException {
		super(url, username, password);
	}

	public SqliteConnectionSource(String url, String username, String password, DatabaseType databaseType) throws SQLException {
		super(url, username, password, databaseType);
	}

	@Override
	protected DatabaseConnection makeConnection(Logger logger) throws SQLException {
		DatabaseConnection conn = super.makeConnection(logger);
		conn.executeStatement("PRAGMA foreign_keys = ON", DatabaseConnection.DEFAULT_RESULT_FLAGS);
		return conn;
	}
}
