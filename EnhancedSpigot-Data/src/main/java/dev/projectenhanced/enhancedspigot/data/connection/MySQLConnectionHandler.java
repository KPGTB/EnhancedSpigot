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

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.jdbc.db.MysqlDatabaseType;
import com.j256.ormlite.support.BaseConnectionSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;

import java.io.IOException;
import java.sql.SQLException;

public class MySQLConnectionHandler implements IConnectionHandler {

	private DatabaseOptions.Credentials credentials;
	private HikariDataSource dataSource;

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.MYSQL;
	}

	@Override
	public void retrieveCredentials(DatabaseOptions options) {
		this.credentials = options.getCredentials();
	}

	@Override
	public BaseConnectionSource connect() throws IOException, SQLException {
		String url = "jdbc:mysql://" + this.credentials.getHost() + ":" + this.credentials.getPort() + "/" + this.credentials.getDatabase() + "?autoReconnect=true";
		return new JdbcPooledConnectionSource(url, credentials.getUsername(), credentials.getPassword(), new MysqlDatabaseType());
	}

	@Override
	public BaseConnectionSource connectHikari(DatabaseOptions.HikariOptions options) throws IOException, SQLException {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:mysql://" + this.credentials.getHost() + ":" + this.credentials.getPort() + "/" + this.credentials.getDatabase());
		config.setUsername(this.credentials.getUsername());
		config.setPassword(this.credentials.getPassword());

		TryCatchUtil.tryAndReturn(() -> Class.forName("com.mysql.cj.jdbc.Driver"));
		this.dataSource = new HikariDataSource(HikariHandler.configure(config, options));

		return new DataSourceConnectionSource(this.dataSource, this.dataSource.getJdbcUrl());
	}

	@Override
	public void close() {
		if (dataSource != null) this.dataSource.close();
	}
}
