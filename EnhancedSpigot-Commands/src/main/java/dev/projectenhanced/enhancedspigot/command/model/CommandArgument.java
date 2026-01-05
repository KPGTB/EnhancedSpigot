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

package dev.projectenhanced.enhancedspigot.command.model;

import dev.projectenhanced.enhancedspigot.common.converter.IStringConverter;
import dev.projectenhanced.enhancedspigot.common.filter.FilterContainer;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor @Getter @Setter public class CommandArgument<T> {
	private final Class<T> clazz;
	private final Class<? extends IStringConverter<T>> customConverter;
	private final FilterContainer<T> filters;
	private String name;

	public IStringConverter<T> getCustomConverter() {
		if (!hasCustomConverter()) return null;
		return TryCatchUtil.tryOrDefault(this.customConverter::newInstance, null);
	}

	public boolean hasCustomConverter() {
		return this.customConverter != null;
	}
}
