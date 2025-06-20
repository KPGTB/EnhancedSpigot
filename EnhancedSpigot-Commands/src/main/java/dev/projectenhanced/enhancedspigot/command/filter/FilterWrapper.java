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

package dev.projectenhanced.enhancedspigot.command.filter;

import dev.projectenhanced.enhancedspigot.command.annotation.Filter;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Objects;

@AllArgsConstructor
@Getter
public class FilterWrapper {
	private final Class<? extends IFilter<?>>[] orFilters;
	private final Class<? extends IFilter<?>>[] andFilters;

	public FilterWrapper(Filter filterAnn) {
		this.orFilters = filterAnn.orFilters();
		this.andFilters = filterAnn.andFilters();
	}

	public <T> IFilter<T>[] getOrFilters(Class<T> expected) {
		return getInstancesArray(orFilters, expected);
	}

	public <T> IFilter<T>[] getAndFilters(Class<T> expected) {
		return getInstancesArray(andFilters, expected);
	}

	@SuppressWarnings("unchecked")
	private <T> IFilter<T>[] getInstancesArray(Class<? extends IFilter<?>>[] filterClasses, Class<T> expected) {
		return Arrays.stream(filterClasses)
					 .filter(clazz -> {
						 if (clazz.getGenericInterfaces().length == 0)
							 return false;
						 ParameterizedType type = (ParameterizedType) clazz.getGenericInterfaces()[0];
						 if (type.getActualTypeArguments().length == 0)
							 return false;
						 Class<T> typeArgClazz = (Class<T>) type.getActualTypeArguments()[0];
						 return typeArgClazz.isAssignableFrom(expected);
					 })
					 .map(clazz -> TryCatchUtil.tryOrDefault(
						 clazz::newInstance,
						 null
					 ))
					 .filter(Objects::nonNull)
					 .toArray(IFilter[]::new);
	}
}
