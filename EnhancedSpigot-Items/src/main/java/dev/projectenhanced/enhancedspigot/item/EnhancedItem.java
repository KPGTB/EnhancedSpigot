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

package dev.projectenhanced.enhancedspigot.item;

import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class EnhancedItem implements Listener {
	protected final DependencyProvider provider;
	@Getter private final String fullItemTag;

	private final List<UUID> deathPlayersCache;
	private final Map<UUID, Map<Integer, ItemStack>> toReturnCache;
	@Getter
	@Setter
	private boolean blockDroppingItem;

	public EnhancedItem(DependencyProvider provider, String fullItemTag) {
		this.provider = provider;
		this.fullItemTag = fullItemTag;
		this.deathPlayersCache = new ArrayList<>();
		this.toReturnCache = new HashMap<>();
		this.blockDroppingItem = false;
	}

	/**
	 * Get ItemStack of custom item
	 *
	 * @return ItemStack of custom item
	 */
	public abstract ItemStack getItem();

	/**
	 * Check if ItemStack is similar to item from this object
	 *
	 * @param is ItemStack to compare
	 * @return true if items are similar
	 */
	public boolean isSimilar(ItemStack is) {
		if (is == null || is.getType()
			.equals(Material.AIR)) {
			return false;
		}
		return this.getItem()
			.isSimilar(is);
	}

	public void onUse(PlayerInteractEvent event) {}

	public void onClick(InventoryClickEvent event, InternalClickType type) {}

	public void onDrop(PlayerDropItemEvent event) {}

	public void onDeath(PlayerDeathEvent event) {}

	public void onBreak(PlayerItemBreakEvent event) {}

	public void onConsume(PlayerItemConsumeEvent event) {}

	public void onHeld(PlayerItemHeldEvent event, boolean old) {}

	public void onPickup(EntityPickupItemEvent event) {}

	public void onDrag(InventoryDragEvent event) {}

	public void onDamage(EntityDamageByEntityEvent event, boolean off) {}

	public void onRespawn(PlayerRespawnEvent event) {}

	public void onSwap(PlayerSwapHandItemsEvent event, boolean toOff) {}

	@EventHandler
	public final void onUseListener(PlayerInteractEvent event) {
		ItemStack is = event.getItem();

		if (is == null || is.getType()
			.equals(Material.AIR)) {
			return;
		}

		if (this.isSimilar(is)) {
			this.onUse(event);
		}
	}

	@EventHandler
	public final void onClickListener(InventoryClickEvent event) {
		ItemStack clicked = event.getCurrentItem();

		if (clicked != null && !clicked.getType()
			.equals(Material.AIR)) {
			if (this.isSimilar(clicked)) {
				this.onClick(event, InternalClickType.CURRENT);

				if (blockDroppingItem && event.isShiftClick()) {
					event.setCancelled(true);
				}
			}
		}

		Inventory clickedInv = event.getClickedInventory();
		ItemStack cursor = event.getCursor();
		if (cursor != null && !cursor.getType()
			.equals(Material.AIR)) {
			if (this.isSimilar(cursor)) {
				this.onClick(event, InternalClickType.CURSOR);

				if (blockDroppingItem && (clickedInv == null || clickedInv.getType() != InventoryType.PLAYER)) {
					event.setCancelled(true);
				}
			}
		}

		if (event.getClick() == ClickType.NUMBER_KEY) {
			ItemStack hotbar = event.getWhoClicked()
				.getInventory()
				.getItem(event.getHotbarButton());

			if (hotbar != null && !hotbar.getType()
				.equals(Material.AIR)) {
				if (this.isSimilar(hotbar)) {
					this.onClick(event, InternalClickType.HOTBAR);

					if (blockDroppingItem && (clickedInv == null || clickedInv.getType() != InventoryType.PLAYER)) {
						event.setCancelled(true);
					}
				}
			}
		}

		if (event.getClick() == ClickType.SWAP_OFFHAND) {
			ItemStack offhand = event.getWhoClicked()
				.getInventory()
				.getItemInOffHand();

			if (offhand != null && !offhand.getType()
				.equals(Material.AIR)) {
				if (this.isSimilar(offhand)) {
					this.onClick(event, InternalClickType.OFFHAND);

					if (blockDroppingItem && (clickedInv == null || clickedInv.getType() != InventoryType.PLAYER)) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public final void onDropListener(PlayerDropItemEvent event) {
		ItemStack is = event.getItemDrop()
			.getItemStack();
		;

		if (is == null || is.getType()
			.equals(Material.AIR)) {
			return;
		}

		if (this.isSimilar(is)) {
			this.onDrop(event);

			if (blockDroppingItem) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public final void onDeathListener(PlayerDeathEvent event) {
		List<ItemStack> drops = event.getDrops();
		Player player = event.getEntity();
		UUID uuid = player.getUniqueId();

		for (ItemStack is : drops) {
			if (is == null || is.getType()
				.equals(Material.AIR)) {
				continue;
			}

			if (this.isSimilar(is)) {
				this.deathPlayersCache.add(uuid);
				this.onDeath(event);
				break;
			}
		}

		if (blockDroppingItem) {
			PlayerInventory inv = player.getInventory();
			ItemStack[] invContent = inv.getContents();

			for (int i = 0; i < invContent.length; i++) {
				ItemStack is = invContent[i];
				if (is == null || is.getType()
					.equals(Material.AIR)) {
					continue;
				}

				if (this.isSimilar(is)) {
					if (!this.toReturnCache.containsKey(uuid)) {
						this.toReturnCache.put(uuid, new HashMap<>());
					}
					this.toReturnCache.get(uuid)
						.put(i, is);
				}
			}

			drops.removeIf(this::isSimilar);
		}
	}

	@EventHandler
	public final void onBreakListener(PlayerItemBreakEvent event) {
		ItemStack is = event.getBrokenItem();

		if (is == null || is.getType()
			.equals(Material.AIR)) {
			return;
		}

		if (this.isSimilar(is)) {
			this.onBreak(event);
		}
	}

	@EventHandler
	public final void onConsumeListener(PlayerItemConsumeEvent event) {
		ItemStack is = event.getItem();

		if (is == null || is.getType()
			.equals(Material.AIR)) {
			return;
		}

		if (this.isSimilar(is)) {
			this.onConsume(event);
		}
	}

	@EventHandler
	public final void onHeldListener(PlayerItemHeldEvent event) {
		PlayerInventory inv = event.getPlayer()
			.getInventory();
		ItemStack newItem = inv.getItem(event.getNewSlot());

		if (newItem != null && !newItem.getType()
			.equals(Material.AIR)) {
			if (this.isSimilar(newItem)) {
				this.onHeld(event, false);
			}
		}

		ItemStack oldItem = inv.getItem(event.getPreviousSlot());

		if (oldItem != null && !oldItem.getType()
			.equals(Material.AIR)) {
			if (this.isSimilar(oldItem)) {
				this.onHeld(event, true);
			}
		}
	}

	@EventHandler
	public final void onPickupListener(EntityPickupItemEvent event) {
		ItemStack is = event.getItem()
			.getItemStack();

		if (is == null || is.getType()
			.equals(Material.AIR)) {
			return;
		}

		if (this.isSimilar(is)) {
			this.onPickup(event);
		}
	}

	@EventHandler
	public final void onDragListener(InventoryDragEvent event) {
		Collection<ItemStack> items = event.getNewItems()
			.values();

		for (ItemStack is : items) {
			if (is == null || is.getType()
				.equals(Material.AIR)) {
				continue;
			}

			if (this.isSimilar(is)) {
				this.onDrag(event);

				if (this.blockDroppingItem) {
					event.setCancelled(true);
				}
				break;
			}
		}
	}

	@EventHandler
	public final void onDamageListener(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getDamager();
		PlayerInventory inv = player.getInventory();

		ItemStack main = inv.getItemInMainHand();
		ItemStack off = inv.getItemInOffHand();

		if (main != null && !main.getType()
			.equals(Material.AIR)) {
			if (this.isSimilar(main)) {
				this.onDamage(event, false);
			}
		}

		if (off != null && !off.getType()
			.equals(Material.AIR)) {
			if (this.isSimilar(off)) {
				this.onDamage(event, true);
			}
		}
	}

	@EventHandler
	public final void onRespawnListener(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		if (this.deathPlayersCache.contains(uuid)) {
			this.deathPlayersCache.remove(uuid);
			this.onRespawn(event);
		}
		if (this.toReturnCache.containsKey(uuid)) {
			PlayerInventory inv = player.getInventory();
			this.toReturnCache.get(uuid)
				.forEach(inv::setItem);
			this.toReturnCache.remove(uuid);
		}
	}

	@EventHandler
	public final void onSwapListener(PlayerSwapHandItemsEvent event) {
		ItemStack mainItem = event.getMainHandItem();

		if (mainItem != null && !mainItem.getType()
			.equals(Material.AIR)) {
			if (this.isSimilar(mainItem)) {
				this.onSwap(event, false);
			}
		}

		ItemStack offItem = event.getOffHandItem();

		if (offItem != null && !offItem.getType()
			.equals(Material.AIR)) {
			if (this.isSimilar(offItem)) {
				this.onSwap(event, true);
			}
		}
	}

	public enum InternalClickType {

		/**
		 * Clicked item InventoryClickEvent#getCurrentItem()
		 */
		CURRENT,
		/**
		 * Clicked item on cursor InventoryClickEvent#getCursor()
		 */
		CURSOR,
		/**
		 * Item clicked using 1-9 keys
		 */
		HOTBAR,
		/**
		 * When pressing F
		 */
		OFFHAND
	}

	@lombok.Builder public static class Builder {
		private Consumer<PlayerInteractEvent> onUseAction;
		private BiConsumer<InventoryClickEvent, InternalClickType> onClickAction;
		private Consumer<PlayerDropItemEvent> onDropAction;
		private Consumer<PlayerDeathEvent> onDeathAction;
		private Consumer<PlayerItemBreakEvent> onBreakAction;
		private Consumer<PlayerItemConsumeEvent> onConsumeAction;
		private BiConsumer<PlayerItemHeldEvent, Boolean> onHeldAction;
		private Consumer<EntityPickupItemEvent> onPickupAction;
		private Consumer<InventoryDragEvent> onDragAction;
		private BiConsumer<EntityDamageByEntityEvent, Boolean> onDamageAction;
		private Consumer<PlayerRespawnEvent> onRespawnAction;
		private BiConsumer<PlayerSwapHandItemsEvent, Boolean> onSwapAction;
		private Predicate<ItemStack> isSimilarAction;
		private boolean blockDroppingItem;

		/**
		 * Register custom item
		 *
		 * @return EnhancedItem instance
		 */
		public EnhancedItem register(DependencyProvider provider, ItemStack itemStack, String itemName) {
			EnhancedItem item = new EnhancedItem(provider, itemName) {
				@Override
				public ItemStack getItem() {
					return itemStack;
				}

				@Override
				public void onUse(PlayerInteractEvent event) {
					if (onUseAction != null) {
						onUseAction.accept(event);
					}
				}

				@Override
				public void onClick(InventoryClickEvent event, InternalClickType type) {
					if (onClickAction != null) {
						onClickAction.accept(event, type);
					}
				}

				@Override
				public void onDrop(PlayerDropItemEvent event) {
					if (onDropAction != null) {
						onDropAction.accept(event);
					}
				}

				@Override
				public void onDeath(PlayerDeathEvent event) {
					if (onDeathAction != null) {
						onDeathAction.accept(event);
					}
				}

				@Override
				public void onBreak(PlayerItemBreakEvent event) {
					if (onBreakAction != null) {
						onBreakAction.accept(event);
					}
				}

				@Override
				public void onConsume(PlayerItemConsumeEvent event) {
					if (onConsumeAction != null) {
						onConsumeAction.accept(event);
					}
				}

				@Override
				public void onHeld(PlayerItemHeldEvent event, boolean old) {
					if (onHeldAction != null) {
						onHeldAction.accept(event, old);
					}
				}

				@Override
				public void onPickup(EntityPickupItemEvent event) {
					if (onPickupAction != null) {
						onPickupAction.accept(event);
					}
				}

				@Override
				public void onDrag(InventoryDragEvent event) {
					if (onDragAction != null) {
						onDragAction.accept(event);
					}
				}

				@Override
				public void onDamage(EntityDamageByEntityEvent event, boolean off) {
					if (onDamageAction != null) {
						onDamageAction.accept(event, off);
					}
				}

				@Override
				public void onRespawn(PlayerRespawnEvent event) {
					if (onRespawnAction != null) {
						onRespawnAction.accept(event);
					}
				}

				@Override
				public void onSwap(PlayerSwapHandItemsEvent event, boolean toOff) {
					if (onSwapAction != null) {
						onSwapAction.accept(event, toOff);
					}
				}

				@Override
				public boolean isSimilar(ItemStack is) {
					if (isSimilarAction != null) {
						return isSimilarAction.test(is);
					}
					return super.isSimilar(is);
				}
			};
			item.setBlockDroppingItem(this.blockDroppingItem);
			provider.provide(ItemController.class)
				.registerItem(item);
			return item;
		}
	}
}
