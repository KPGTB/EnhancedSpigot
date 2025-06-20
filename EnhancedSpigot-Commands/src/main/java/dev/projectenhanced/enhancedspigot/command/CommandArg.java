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

package dev.projectenhanced.enhancedspigot.command;

import dev.projectenhanced.enhancedspigot.command.filter.FilterWrapper;
import dev.projectenhanced.enhancedspigot.command.parser.IArgumentParser;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Info about command arg
 */
@AllArgsConstructor
@Getter
@Setter
public class CommandArg {
	private final Class<?> clazz;
	private final Class<? extends IArgumentParser<?>> customParser;
	private final FilterWrapper filters;
	private String name;

	public IArgumentParser<?> getCustomParser() {
		if (!hasCustomParser()) return null;
		return TryCatchUtil.tryOrDefault(customParser::newInstance, null);
	}

	public boolean hasCustomParser() {
		return customParser != null;
	}
}
