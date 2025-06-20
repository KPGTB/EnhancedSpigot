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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Info about command
 */
@AllArgsConstructor
@Getter
@Setter
public class CommandInfo {
	private final CommandPath path;
	private final List<String> permissions;
	private final boolean playerRequired;
	private final FilterWrapper sourceFilters;
	private final List<CommandArg> args;
	private final Object methodInvoker;
	private final Method method;
	private final boolean endless;
	private final boolean hidden;
	private String description;
}
