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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
@EqualsAndHashCode
public class SemanticVersion {
	private final int major;
	private final int minor;
	private final int patch;

	public SemanticVersion(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}

	/**
	 * Convert string to version
	 *
	 * @param version Version like 1.23.4
	 */
	public SemanticVersion(String version) {
		String[] versionNumbers = version.split("\\.");

		if (versionNumbers.length == 2) {
			versionNumbers = new String[]{versionNumbers[0], versionNumbers[1], "0"};
		}
		if (versionNumbers.length != 3) {
			throw new IllegalArgumentException("Wrong version format! Required MAJOR.MINOR.PATCH");
		}

		try {
			this.major = Integer.parseInt(versionNumbers[0]);
			this.minor = Integer.parseInt(versionNumbers[1]);
			this.patch = Integer.parseInt(versionNumbers[2]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Wrong version format! Required MAJOR.MINOR.PATCH");
		}
	}

	public static SemanticVersion getMinecraftVersion() {
		return new SemanticVersion(Bukkit.getBukkitVersion()
										 .split("-")[0]);
	}

	/**
	 * Check if version is newer than
	 *
	 * @param version Version that should be checked
	 * @return True if this object is newer than version from params
	 */
	public boolean isNewerThan(SemanticVersion version) {
		if (version.major < this.major) return true;
		if (version.major > this.major) return false;
		if (version.minor < this.minor) return true;
		if (version.minor > this.minor) return false;
		return version.patch < this.patch;
	}

	/**
	 * Check if version is newer than or equal
	 *
	 * @param version Version that should be checked
	 * @return True if this object is newer than or equal to version from params
	 */
	public boolean isNewerOrEqual(SemanticVersion version) {
		return isNewerThan(version) || this.equals(version);
	}

	/**
	 * Check if version is newer than
	 *
	 * @param version Version that should be checked
	 * @return True if this object is newer than version from params
	 */
	public boolean isNewerThan(String version) {
		return isNewerThan(new SemanticVersion(version));
	}

	/**
	 * Check if version is newer than or equal
	 *
	 * @param version Version that should be checked
	 * @return True if this object is newer than or equal to version from params
	 */
	public boolean isNewerOrEqual(String version) {
		return isNewerOrEqual(new SemanticVersion(version));
	}

	public boolean isNewerThanMinecraft() {
		return isNewerThan(getMinecraftVersion());
	}

	public boolean isNewerOrEqualMinecraft() {
		return isNewerOrEqual(getMinecraftVersion());
	}
}
