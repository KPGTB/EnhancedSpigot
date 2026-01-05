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

import lombok.Getter;

import java.util.Objects;

/**
 * Path to subcommand
 */
@Getter public class CommandPath implements Cloneable {
	private String pathStr;
	private String[] path;

	public CommandPath() {
		this.pathStr = "";
		this.path = new String[0];
	}

	public CommandPath(String[] path) {
		this.path = path;
		this.pathStr = String.join(" ", path);
	}

	public CommandPath(String pathStr) {
		this.pathStr = pathStr;
		this.path = pathStr.split(" ");
	}

	/**
	 * Add new element to sub command path
	 *
	 * @param element Element to add
	 */
	public void add(String element) {
		if (element.replace(" ", "")
			.isEmpty()) {
			return;
		}
		this.pathStr += (this.pathStr.isEmpty() ?
			"" :
			" ") + element;
		this.path = this.pathStr.split(" ");
	}

	public String getPermissionStr() {
		return String.join(".", path);
	}

	@Override
	public CommandPath clone() {
		try {
			return (CommandPath) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommandPath that = (CommandPath) o;
		return Objects.equals(pathStr, that.pathStr);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pathStr);
	}
}
