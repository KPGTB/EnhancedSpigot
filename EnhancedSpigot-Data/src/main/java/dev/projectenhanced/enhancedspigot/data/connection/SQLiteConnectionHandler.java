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

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.jdbc.db.SqliteDatabaseType;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class SQLiteConnectionHandler implements IConnectionHandler {

	private final String fileName = "database.db";
	private final File file;

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
	public JdbcPooledConnectionSource connect() throws IOException, SQLException {
		this.file.getParentFile()
				 .mkdirs();
		if (!this.file.exists()) {
			this.file.createNewFile();
		}

		String connectionUrl = "jdbc:sqlite:" + this.file.getAbsolutePath();
		return new JdbcPooledConnectionSource(connectionUrl, new SqliteDatabaseType());
	}
}
