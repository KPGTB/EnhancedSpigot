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

package dev.projectenhanced.enhancedspigot.util;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;
import java.util.function.Function;

public class SchedulerUtil {
	public static final boolean USING_FOLIA = checkFolia();
	private static final Class<?> PAPER_SCHEDULED_TASK = TryCatchUtil.tryOrDefault(
		() -> Class.forName(
			"io.papermc.paper.threadedregions.scheduler.ScheduledTask"), null,
		(e) -> {}
	);
	private static final Object PAPER_GLOBAL_REGION_SCHEDULER = TryCatchUtil.tryOrDefault(
		() -> Bukkit.class.getMethod("getGlobalRegionScheduler")
			.invoke(null), null, (e) -> {}
	);

	private static boolean checkFolia() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static void runTask(Plugin plugin, Runnable runnable) {
		if (USING_FOLIA) TryCatchUtil.tryRun(
			() -> PAPER_GLOBAL_REGION_SCHEDULER.getClass()
				.getMethod("execute", Plugin.class, Runnable.class)
				.invoke(PAPER_GLOBAL_REGION_SCHEDULER, plugin, runnable));
		else Bukkit.getScheduler()
			.runTask(plugin, runnable);
	}

	public static Task runTaskLater(Plugin plugin, Consumer<Task> consumer, long delayTicks) {
		return Task.of((task) -> USING_FOLIA ?
			TryCatchUtil.tryAndReturn(
				() -> PAPER_GLOBAL_REGION_SCHEDULER.getClass()
					.getMethod(
						"runDelayed", Plugin.class, Consumer.class,
						long.class
					)
					.invoke(
						PAPER_GLOBAL_REGION_SCHEDULER, plugin,
						(Consumer<Object>) st -> consumer.accept(task),
						delayTicks
					)) :
			Bukkit.getScheduler()
				.runTaskLater(plugin, () -> consumer.accept(task), delayTicks));
	}

	public static Task runTaskTimer(Plugin plugin, Consumer<Task> consumer, long delayTicks, long periodTicks) {
		return Task.of((task) -> USING_FOLIA ?
			TryCatchUtil.tryAndReturn(
				() -> PAPER_GLOBAL_REGION_SCHEDULER.getClass()
					.getMethod(
						"runAtFixedRate", Plugin.class, Consumer.class,
						long.class, long.class
					)
					.invoke(
						PAPER_GLOBAL_REGION_SCHEDULER, plugin,
						(Consumer<Object>) st -> consumer.accept(task),
						delayTicks < 1 ?
							1 :
							delayTicks, periodTicks
					)) :
			Bukkit.getScheduler()
				.runTaskTimer(
					plugin, () -> consumer.accept(task), delayTicks,
					periodTicks
				));
	}

	public static void runTaskAsynchronously(Plugin plugin, Runnable runnable) {
		if (USING_FOLIA) TryCatchUtil.tryRun(
			() -> PAPER_GLOBAL_REGION_SCHEDULER.getClass()
				.getMethod("execute", Plugin.class, Runnable.class)
				.invoke(PAPER_GLOBAL_REGION_SCHEDULER, plugin, runnable));
		else Bukkit.getScheduler()
			.runTaskAsynchronously(plugin, runnable);
	}

	public static Task runTaskLaterAsynchronously(Plugin plugin, Consumer<Task> consumer, long delayTicks) {
		return Task.of((task) -> USING_FOLIA ?
			TryCatchUtil.tryAndReturn(
				() -> PAPER_GLOBAL_REGION_SCHEDULER.getClass()
					.getMethod(
						"runDelayed", Plugin.class, Consumer.class,
						long.class
					)
					.invoke(
						PAPER_GLOBAL_REGION_SCHEDULER, plugin,
						(Consumer<Object>) st -> consumer.accept(task),
						delayTicks
					)) :
			Bukkit.getScheduler()
				.runTaskLaterAsynchronously(
					plugin, () -> consumer.accept(task),
					delayTicks
				));
	}

	public static Task runTaskTimerAsynchronously(Plugin plugin, Consumer<Task> consumer, long delayTicks, long periodTicks) {
		return Task.of((task) -> USING_FOLIA ?
			TryCatchUtil.tryAndReturn(
				() -> PAPER_GLOBAL_REGION_SCHEDULER.getClass()
					.getMethod(
						"runAtFixedRate", Plugin.class, Consumer.class,
						long.class, long.class
					)
					.invoke(
						PAPER_GLOBAL_REGION_SCHEDULER, plugin,
						(Consumer<Object>) st -> consumer.accept(task),
						delayTicks < 1 ?
							1 :
							delayTicks, periodTicks
					)) :
			Bukkit.getScheduler()
				.runTaskTimerAsynchronously(
					plugin, () -> consumer.accept(task), delayTicks,
					periodTicks
				));
	}

	public static void cancelAll(Plugin plugin) {
		if (USING_FOLIA) TryCatchUtil.tryAndReturn(
			() -> PAPER_GLOBAL_REGION_SCHEDULER.getClass()
				.getMethod("cancelTasks", Plugin.class)
				.invoke(PAPER_GLOBAL_REGION_SCHEDULER, plugin));
		else Bukkit.getScheduler()
			.cancelTasks(plugin);
	}

	@Setter @NoArgsConstructor public static class Task {
		private Object foliaTask;
		private BukkitTask bukkitTask;

		public static Task of(Function<Task, Object> func) {
			Task task = new Task();
			Object result = func.apply(task);
			if (PAPER_SCHEDULED_TASK != null && PAPER_SCHEDULED_TASK.isAssignableFrom(
				result.getClass())) task.setFoliaTask(result);
			else if (result instanceof BukkitTask) task.setBukkitTask(
				(BukkitTask) result);
			return task;
		}

		public void cancel() {
			if (foliaTask != null) TryCatchUtil.tryRun(
				() -> PAPER_SCHEDULED_TASK.getMethod("cancel")
					.invoke(foliaTask));
			else if (bukkitTask != null) bukkitTask.cancel();
		}
	}
}
