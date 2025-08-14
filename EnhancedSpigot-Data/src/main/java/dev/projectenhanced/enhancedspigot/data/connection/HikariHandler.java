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

import com.zaxxer.hikari.HikariConfig;

public class HikariHandler {
	public static HikariConfig configure(HikariConfig config, DatabaseOptions.HikariOptions options) {
		config.setPoolName(options.getPoolName());
		config.setMaximumPoolSize(options.getMaximumPoolSize());
		config.setMinimumIdle(options.getMinimumIdle());
		config.setMaxLifetime(options.getMaxLifetime());
		config.setConnectionTimeout(options.getConnectionTimeout());
		config.setIdleTimeout(options.getIdleTimeout());
		config.addDataSourceProperty("characterEncoding", "utf8");
		config.addDataSourceProperty("autoReconnect", "true");
		config.addDataSourceProperty("useSSL", options.isUseSSL());
		return config;
	}
}
