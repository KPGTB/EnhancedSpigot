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

package dev.projectenhanced.enhancedspigot.common.filter;

import dev.projectenhanced.enhancedspigot.common.annotation.Filter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor @Getter public class FilterContainer<T> {
	private final IFilter<T>[] orFilters;
	private final IFilter<T>[] andFilters;

	public FilterContainer(Class<? extends IFilter<T>>[] orFilters, Class<? extends IFilter<T>>[] andFilters) {
		this.orFilters = this.getFiltersInstance(orFilters);
		this.andFilters = this.getFiltersInstance(andFilters);
	}

	@SuppressWarnings("unchecked")
	public FilterContainer(Filter filterAnn) {
		this((Class<? extends IFilter<T>>[]) filterAnn.orFilters(), (Class<? extends IFilter<T>>[]) filterAnn.andFilters());
	}

	@SuppressWarnings("unchecked")
	private IFilter<T>[] getFiltersInstance(Class<? extends IFilter<T>>[] filters) {
		return Arrays.stream(filters)
			.map(clazz -> {
				try {
					return clazz.newInstance();
				} catch (Exception e) {
					return null;
				}
			})
			.filter(Objects::nonNull)
			.toArray(IFilter[]::new);
	}

	/**
	 * Check if object pass filters
	 *
	 * @param object
	 * @param source
	 * @return Empty list if pass, list of messages if not
	 */
	public List<String> passFilters(T object, JavaPlugin plugin, CommandSender source) {
		boolean passOr = true;
		boolean passAnd = true;

		IFilter<T> lastNotPassed = null;

		for (IFilter<T> filter : this.orFilters) {
			if (filter.filter(object, plugin, source)) {
				passOr = true;
				break;
			}

			passOr = false;
			if (lastNotPassed == null || filter.weight() > lastNotPassed.weight()) lastNotPassed = filter;
		}

		for (IFilter<T> filter : andFilters) {
			if (filter.filter(object, plugin, source)) continue;

			passAnd = false;
			if (lastNotPassed == null || filter.weight() > lastNotPassed.weight()) lastNotPassed = filter;
		}

		return passOr && passAnd ?
			new ArrayList<>() :
			lastNotPassed.notPassMessage(object, plugin, source);
	}
}
