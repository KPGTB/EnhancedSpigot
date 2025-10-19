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

import dev.projectenhanced.enhancedspigot.util.lifecycle.IClosable;
import dev.projectenhanced.enhancedspigot.util.lifecycle.IReloadable;

import java.util.HashMap;
import java.util.Map;

public class DependencyProvider {
	private final Map<Class<?>, Object> dependencies;

	public DependencyProvider() {
		this.dependencies = new HashMap<>();
	}

	public <T> T register(T dependency, Class<?>... classes) {
		for (Class<?> clazz : classes) {
			this.dependencies.put(clazz, dependency);
		}
		return dependency;
	}

	public <T> T register(T dependency) {
		return this.register(dependency, dependency.getClass());
	}

	public boolean isRegistered(Class<?> clazz) {
		return this.dependencies.containsKey(clazz);
	}

	@SuppressWarnings("unchecked")
	public <T> T provide(Class<T> clazz) {
		return (T) this.dependencies.get(clazz);
	}

	public void reloadAll() {
		dependencies.values()
			.stream()
			.filter(obj -> obj instanceof IReloadable)
			.map(obj -> (IReloadable) obj)
			.forEach(IReloadable::reload);
	}

	public void closeAll() {
		dependencies.values()
			.stream()
			.filter(obj -> obj instanceof IClosable)
			.map(obj -> (IClosable) obj)
			.forEach(IClosable::close);
	}
}
