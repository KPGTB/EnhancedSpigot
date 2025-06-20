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

package dev.projectenhanced.enhancedspigot.config.serializer.impl;

import dev.projectenhanced.enhancedspigot.config.EnhancedConfig;
import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;
import dev.projectenhanced.enhancedspigot.config.util.SectionUtil;
import dev.projectenhanced.enhancedspigot.util.item.EnhancedItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializer implements ISerializer<ItemStack> {

	@Override
	public Object serialize(ItemStack object, Class<? extends ItemStack> objectClass, EnhancedConfig config) {
		ConfigurationSection section = SectionUtil.createEmpty();
		this.serializeTo(object, objectClass, config, section);
		return section;
	}

	@Override
	public void serializeTo(ItemStack object, Class<? extends ItemStack> objectClass, EnhancedConfig config, Object to) {
		ConfigurationSection section = (ConfigurationSection) to;
		SectionUtil.fillSection(section, EnhancedItemBuilder.Serializer.serialize(object));
	}

	@Override
	public ItemStack deserialize(Object serialized, Class<? extends ItemStack> targetClass, EnhancedConfig config) {
		ConfigurationSection section = (ConfigurationSection) serialized;
		return EnhancedItemBuilder.Serializer.deserialize(section.getValues(false));
	}

	@Override
	public void deserializeTo(Object serialized, Class<? extends ItemStack> targetClass, EnhancedConfig config, Object to) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean convertToSection() {
		return true;
	}
}
