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
import com.j256.ormlite.jdbc.db.PostgresDatabaseType;

import java.io.IOException;
import java.sql.SQLException;

public class PostgreSQLConnectionHandler implements IConnectionHandler {

	private DatabaseOptions.Credentials credentials;

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.MYSQL;
	}

	@Override
	public void retrieveCredentials(DatabaseOptions options) {
		this.credentials = options.getCredentials();
	}

	@Override
	public JdbcPooledConnectionSource connect() throws IOException, SQLException {
		String url = "jdbc:postgresql://" + this.credentials.getHost() + ":" + this.credentials.getPort() + "/" + this.credentials.getDatabase();
		return new JdbcPooledConnectionSource(url, this.credentials.getUsername(), this.credentials.getPassword(), new PostgresDatabaseType());
	}
}
