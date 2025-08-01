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

package dev.projectenhanced.enhancedspigot.menu.impl.menu.config;

import dev.projectenhanced.enhancedspigot.locale.ColorUtil;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

@NoArgsConstructor public class WriteMenuSettings {
	private String title = "Response";
	private String placeholder = "Type here...";

	public String getTitle(Player viewer) {
		return ColorUtil.addPAPI(
			ColorUtil.convertMmToString(this.title), viewer);
	}

	public String getPlaceholder(Player viewer) {
		return ColorUtil.addPAPI(
			ColorUtil.convertMmToString(this.title), viewer);
	}
}
