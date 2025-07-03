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

package dev.projectenhanced.enhancedspigot.menu.nms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class InventoryHelper_1_14_1_16 implements IInventoryHelper {
	@Override
	public void updateInventoryTitle(Player player, String title) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, InstantiationException {
		// 1.14 - 1.16
		//PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(ep.activeContainer.windowId, ep.activeContainer.getType(),new ChatMessage("Ciekaweee"));
		//ep.playerConnection.sendPacket(packet);
		//ep.updateInventory(ep.activeContainer);

		Object entityPlayer = getCraftBukkitClass(
			"entity.CraftPlayer").getMethod("getHandle")
			.invoke(player);

		Object activeContainer = entityPlayer.getClass()
			.getField("activeContainer")
			.get(entityPlayer);
		Object windowId = activeContainer.getClass()
			.getField("windowId")
			.get(activeContainer);
		Object type = activeContainer.getClass()
			.getMethod("getType")
			.invoke(activeContainer);
		Object chatMessage = getNMSClass("ChatMessage").getDeclaredConstructor(
				String.class, Object[].class)
			.newInstance(title, new Object[0]);

		Object packet = getNMSClass(
			"PacketPlayOutOpenWindow").getDeclaredConstructor(
				int.class, getNMSClass("Containers"),
				getNMSClass("IChatBaseComponent")
			)
			.newInstance(windowId, type, chatMessage);
		sendPacket(entityPlayer, packet);

		entityPlayer.getClass()
			.getMethod("updateInventory", getNMSClass("Container"))
			.invoke(entityPlayer, activeContainer);
	}

	private void sendPacket(Object entityPlayer, Object packet) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
		Object playerConnection = entityPlayer.getClass()
			.getField("playerConnection")
			.get(entityPlayer);
		playerConnection.getClass()
			.getMethod("sendPacket", getNMSClass("Packet"))
			.invoke(playerConnection, packet);
	}

	private String getVersion() {
		return Bukkit.getServer()
			.getClass()
			.getPackage()
			.getName()
			.split("\\.")[3];
	}

	private Class<?> getCraftBukkitClass(String name) throws ClassNotFoundException {
		return Class.forName(
			"org.bukkit.craftbukkit." + getVersion() + "." + name);
	}

	private Class<?> getNMSClass(String name) throws ClassNotFoundException {
		return Class.forName(
			"net.minecraft.server." + getVersion() + "." + name);
	}
}
