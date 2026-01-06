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

package dev.projectenhanced.enhancedspigot.command;

import dev.projectenhanced.enhancedspigot.locale.LocaleObject;
import dev.projectenhanced.enhancedspigot.locale.annotation.InjectBridge;
import dev.projectenhanced.enhancedspigot.locale.annotation.LocaleDefault;
import dev.projectenhanced.enhancedspigot.locale.bridge.IPlatformBridge;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor @Getter public class CommandLocale {
	@LocaleDefault(language = "en", def = "<red>You don't have enough permissions to use this command")
	@LocaleDefault(language = "pl", def = "<red>Nie masz wystarczająco permisji, aby użyć tej komendy")
	LocaleObject noPermission;
	@LocaleDefault(language = "en", def = "<red>This command can be used only by players")
	@LocaleDefault(language = "pl", def = "<red>Ta komenda może być użyta tylko przez gracza")
	LocaleObject onlyPlayer;
	@LocaleDefault(language = "en", def = "<dark_green><st>─────┤</st> /<green><command> <aqua>• <dark_green>Help Page <dark_gray>(<gray><page><dark_gray>/<gray><pages><dark_gray>)")
	@LocaleDefault(language = "pl", def = "<dark_green><st>─────┤</st> /<green><command> <aqua>• <dark_green>Pomoc <dark_gray>(<gray><page><dark_gray>/<gray><pages><dark_gray>)")
	LocaleObject helpStart;
	@LocaleDefault(language = "en", def = "<dark_gray>» <red>There isn't any help for you.")
	@LocaleDefault(language = "pl", def = "<dark_gray>» <red>Brak pomocy")
	LocaleObject helpNoInfo;
	@LocaleDefault(language = "en", def = "<dark_gray>» <dark_green>/<green><command> <dark_gray>• <gray><description>")
	@LocaleDefault(language = "pl", def = "<dark_gray>» <dark_green>/<green><command> <dark_gray>• <gray><description>")
	LocaleObject helpLine;
	@LocaleDefault(language = "en", def = "<dark_green><st>─────┤</st> /<green><command> <aqua>• <dark_green>Help Page <dark_gray>(<gray><page><dark_gray>/<gray><pages><dark_gray>)")
	@LocaleDefault(language = "pl", def = "<dark_green><st>─────┤</st> /<green><command> <aqua>• <dark_green>Pomoc <dark_gray>(<gray><page><dark_gray>/<gray><pages><dark_gray>)")
	LocaleObject helpEnd;

	@InjectBridge IPlatformBridge bridge;

	public void update(CommandLocale newLocale) {
		this.noPermission = newLocale.noPermission;
		this.onlyPlayer = newLocale.onlyPlayer;
		this.helpStart = newLocale.helpStart;
		this.helpNoInfo = newLocale.helpNoInfo;
		this.helpLine = newLocale.helpLine;
		this.helpEnd = newLocale.helpEnd;
	}
}
