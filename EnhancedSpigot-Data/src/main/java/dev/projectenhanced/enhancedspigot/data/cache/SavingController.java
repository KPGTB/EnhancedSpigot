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

package dev.projectenhanced.enhancedspigot.data.cache;

import com.google.common.collect.Sets;
import dev.projectenhanced.enhancedspigot.data.cache.iface.ISavable;
import dev.projectenhanced.enhancedspigot.util.SchedulerUtil;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class SavingController {
	private final Plugin plugin;
	private Set<SaveTask> saveTasks = Sets.newHashSet();

	public SavingController(Plugin plugin) {
		this.plugin = plugin;
	}

	public void addSavable(ISavable<?, ?> savable, int interval) {
		this.saveTasks.add(new SaveTask(this.plugin, savable, interval));
	}

	public void clearController() {
		for (SaveTask saveTask : this.saveTasks) {
			saveTask.stop();
		}
		this.saveTasks.clear();
	}

	public class SaveTask {
		private final ISavable<?, ?> savable;
		private final SchedulerUtil.Task bukkitTask;

		public SaveTask(Plugin plugin, ISavable<?, ?> savable, int interval) {
			this.savable = savable;
			this.bukkitTask = SchedulerUtil.runTaskTimerAsynchronously(plugin, task -> this.savable.saveAll(), interval, interval);
		}

		public void stop() {
			this.bukkitTask.cancel();
		}
	}

}
