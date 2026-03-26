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

package dev.projectenhanced.enhancedspigot.util.leaderboard;

import dev.projectenhanced.enhancedspigot.common.IDependencyProvider;
import dev.projectenhanced.enhancedspigot.common.stereotype.Controller;
import dev.projectenhanced.enhancedspigot.util.Pair;
import dev.projectenhanced.enhancedspigot.util.SchedulerUtil;
import dev.projectenhanced.enhancedspigot.util.time.EnhancedTime;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LeaderboardController extends Controller {

	private final ConcurrentMap<String, LeaderboardData<?>> registeredLeaderboards;

	@Getter
	@Setter
	private EnhancedTime refreshRate;
	private SchedulerUtil.Task refreshTask;

	public LeaderboardController(JavaPlugin plugin) {
		super(plugin);
		this.registeredLeaderboards = new ConcurrentHashMap<>();
		this.refreshRate = new EnhancedTime("5m");
	}

	public LeaderboardController(IDependencyProvider dependencyProvider) {
		super(dependencyProvider);
		this.registeredLeaderboards = new ConcurrentHashMap<>();
		this.refreshRate = new EnhancedTime("5m");
	}

	public void register(String key, LeaderboardData<?> data) {
		this.registeredLeaderboards.put(key, data);
	}

	public Set<String> getRegisteredLeaderboards() {
		return this.registeredLeaderboards.keySet();
	}

	public void unregister(String key) {
		this.registeredLeaderboards.remove(key);
	}

	public void unregisterAll() {
		this.registeredLeaderboards.clear();
	}

	/*
	leaderboard_<key>_<placeholder>
	 */
	public String handlePlaceholder(OfflinePlayer offlinePlayer, String placeholder) {
		if (!placeholder.startsWith("leaderboard_")) return null;
		String[] parts = placeholder.split("_", 3);
		if (parts.length < 3) return null;
		String key = parts[1];
		String actualPlaceholder = parts[2];

		LeaderboardData<?> data = this.registeredLeaderboards.get(key);
		if (data == null) return null;
		return data.parsePlaceholder(offlinePlayer, actualPlaceholder);
	}

	public <V> List<String> asText(String key, @Nullable Pair<String, V> yourValue) {
		LeaderboardData<V> data = (LeaderboardData<V>) this.registeredLeaderboards.get(key);
		if (data == null) return null;
		return data.asText(yourValue);
	}

	public <V> LeaderboardData<V> getData(String key) {
		return (LeaderboardData<V>) this.registeredLeaderboards.get(key);
	}

	@Override
	public void close() {
		if (this.refreshTask != null) this.refreshTask.cancel();
	}

	@Override
	public void reload() {

	}

	@Override
	public void start() {
		this.refreshAll();
		this.autoRefresh();
	}

	public void autoRefresh() {
		this.refreshTask = SchedulerUtil.runTaskLater(
			this.plugin, () -> {
				this.refreshAll();
				this.autoRefresh();
			}, this.refreshRate.getTicks()
		);
	}

	public void refreshAll() {
		this.registeredLeaderboards.forEach((k, v) -> v.refresh(System.currentTimeMillis() + this.refreshRate.getMillis()));
	}
}
