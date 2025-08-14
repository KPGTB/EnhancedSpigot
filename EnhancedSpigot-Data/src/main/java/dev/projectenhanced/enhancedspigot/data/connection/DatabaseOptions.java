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

import dev.projectenhanced.enhancedspigot.config.annotation.Comment;
import dev.projectenhanced.enhancedspigot.config.annotation.Serializer;
import dev.projectenhanced.enhancedspigot.config.serializer.impl.BaseSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DatabaseOptions {
	@Comment("Supported types: SQLITE, MYSQL, POSTGRESQL") private ConnectionType type = ConnectionType.SQLITE;
	@Comment({"With these options, you can manage database cache (from OrmLite)", "The cache system saves the most recent database queries to improve performance. It's highly recommended to use!", "With the capacity option, you can manage the size of the cache per DAO. Greater - Better Server Performance - More RAM usage", "Capacity '0' changes the system to default. It means that there can be stored unlimited amount of objects, but they are removed after garbage collection.", "Test on your server which option is better for you. Disable only if you have a small amount of RAM!", "To disable, set to -1"}) private int cacheCapacity = 0;
	@Comment({"Configure only when using MySQL or PostgreSQL"})
	@Serializer(BaseSerializer.class)
	private Credentials credentials = new Credentials();
	@Comment({"Configure only when using MySQL or PostgreSQL", "Don't change if you don't know what it is!"})
	@Serializer(BaseSerializer.class)
	private HikariOptions hikariOptions = new HikariOptions();

	@Getter @Setter @NoArgsConstructor @AllArgsConstructor
	public static class Credentials {
		private String host = "localhost";
		private int port = 3306;
		private String username = "root";
		private String password = "";
		private String database = "minecraft";
	}

	@Getter @Setter @NoArgsConstructor @AllArgsConstructor
	public static class HikariOptions {
		private boolean enabled = true;
		private String poolName = "hikari";
		private int maximumPoolSize = 5;
		private int minimumIdle = 15;
		private long idleTimeout = 600000L;
		private long maxLifetime = 1800000L;
		private long connectionTimeout = 30000L;
		private boolean useSSL = false;
	}
}
