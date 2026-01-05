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

package dev.projectenhanced.enhancedspigot.common.stereotype;

import dev.projectenhanced.enhancedspigot.common.IDependencyProvider;
import dev.projectenhanced.enhancedspigot.common.stereotype.lifecycle.IClosable;
import dev.projectenhanced.enhancedspigot.common.stereotype.lifecycle.IReloadable;
import dev.projectenhanced.enhancedspigot.common.stereotype.lifecycle.IStartable;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Component implements IStartable, IReloadable, IClosable {
	protected final JavaPlugin plugin;
	protected final IDependencyProvider dependencyProvider;

	public Component(JavaPlugin plugin) {
		this.plugin = plugin;
		this.dependencyProvider = null;
	}

	public Component(IDependencyProvider dependencyProvider) {
		this.dependencyProvider = dependencyProvider;
		this.plugin = this.dependencyProvider.provide(JavaPlugin.class);
	}
}
