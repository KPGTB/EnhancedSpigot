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

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TryCatchUtil {
	private static Logger logger = Bukkit.getLogger();

	public static void usePluginLogger(JavaPlugin plugin) {
		logger = plugin.getLogger();
	}

	public static <T> T tryOrDefault(ITryCatchWithReturn<T> iface, T or, Consumer<Exception> catchHandler) {
		try {
			return iface.run();
		} catch (Exception e) {
			if (catchHandler != null) catchHandler.accept(e);
			else logger.log(Level.SEVERE, "Something went wrong!", e);
			return or;
		}
	}

	public static <T> T tryOrDefault(ITryCatchWithReturn<T> iface, T or) {
		return tryOrDefault(iface, or, null);
	}

	public static <T> T tryAndReturn(ITryCatchWithReturn<T> iface) {
		return tryOrDefault(iface, null);
	}

	public static void tryRun(ITryCatch iface, Consumer<Exception> catchHandler) {
		tryOrDefault(
			() -> {
				iface.run();
				return null;
			}, null, catchHandler
		);
	}

	public static void tryRun(ITryCatch iface) {
		tryRun(iface, null);
	}

	@FunctionalInterface public interface ITryCatchWithReturn<T> {
		T run() throws Exception;
	}

	@FunctionalInterface public interface ITryCatch {
		void run() throws Exception;
	}
}
