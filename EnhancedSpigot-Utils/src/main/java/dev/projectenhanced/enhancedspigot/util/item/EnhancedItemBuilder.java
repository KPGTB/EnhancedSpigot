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

package dev.projectenhanced.enhancedspigot.util.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.projectenhanced.enhancedspigot.util.SemanticVersion;
import dev.projectenhanced.enhancedspigot.util.VanillaEnchantment;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.material.MaterialData;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnhancedItemBuilder {
	private final ItemStack itemStack;
	private final ItemMeta itemMeta;
	private int amount;

	public EnhancedItemBuilder(ItemStack itemStack) {
		this.itemStack = itemStack;
		this.amount = itemStack.getAmount();

		if (itemStack.getType() == Material.AIR || itemStack.getItemMeta() == null) {
			throw new IllegalArgumentException(
				"Provided ItemStack is AIR or doesn't have Item Meta");
		}

		this.itemMeta = itemStack.getItemMeta();
	}

	public EnhancedItemBuilder(Material material) {
		this(new ItemStack(material));
	}

	@Deprecated
	public EnhancedItemBuilder(MaterialData materialData) {
		this(new ItemStack(
			materialData.getItemType(), 1,
			materialData.getData()
		));
	}

	public static EnhancedItemBuilder of(ItemStack itemStack) {
		return new EnhancedItemBuilder(itemStack);
	}

	public static EnhancedItemBuilder of(Material material) {
		return new EnhancedItemBuilder(material);
	}

	@Deprecated
	public static EnhancedItemBuilder of(MaterialData materialData) {
		return new EnhancedItemBuilder(materialData);
	}

	public static boolean hasTrimSupport() {
		return SemanticVersion.getMinecraftVersion()
			.isNewerOrEqual("1.20");
	}

	public static boolean hasAttributeSupport() {
		return SemanticVersion.getMinecraftVersion()
			.isNewerOrEqual("1.14");
	}

	public static boolean hasModelSupport() {
		return SemanticVersion.getMinecraftVersion()
			.isNewerOrEqual("1.14");
	}

	public static boolean isUsingNewDamage() {
		return SemanticVersion.getMinecraftVersion()
			.isNewerOrEqual("1.13");
	}

	public static boolean hasUnbreakableSupport() {
		return SemanticVersion.getMinecraftVersion()
			.isNewerOrEqual("1.13");
	}

	public EnhancedItemBuilder amount(int amount) {
		this.amount = amount;
		return this;
	}

	public EnhancedItemBuilder model(int model) {
		if (hasModelSupport()) {
			this.itemMeta.setCustomModelData(model);
		}
		return this;
	}

	public EnhancedItemBuilder damage(int damage) {
		if (isUsingNewDamage()) {
			if (this.itemMeta instanceof Damageable)
				((Damageable) this.itemMeta).setDamage(damage);
		} else {
			this.itemStack.setDurability((short) damage);
		}
		return this;
	}

	public EnhancedItemBuilder unbreakable() {
		return this.unbreakable(true);
	}

	public EnhancedItemBuilder unbreakable(boolean state) {
		if (hasUnbreakableSupport()) {
			this.itemMeta.setUnbreakable(state);
		}
		return this;
	}

	public EnhancedItemBuilder enchant(Enchantment enchantment, int level) {
		this.itemMeta.addEnchant(enchantment, level, true);
		return this;
	}

	public EnhancedItemBuilder enchant(Map<Enchantment, Integer> enchantments) {
		enchantments.forEach(this::enchant);
		return this;
	}

	public EnhancedItemBuilder clearEnchantments() {
		this.itemMeta.getEnchants()
			.forEach((enchantment, level) -> this.itemMeta.removeEnchant(
				enchantment));
		return this;
	}

	public EnhancedItemBuilder displayName(String name) {
		this.itemMeta.setDisplayName(name);
		return this;
	}

	public EnhancedItemBuilder lore(String... lines) {
		List<String> lore = new ArrayList<>();
		if (this.itemMeta.hasLore()) lore = this.itemMeta.getLore();
		lore.addAll(Arrays.asList(lines));
		this.itemMeta.setLore(lore);
		return this;
	}

	public EnhancedItemBuilder replaceLore(List<String> lines) {
		this.itemMeta.setLore(lines);
		return this;
	}

	public EnhancedItemBuilder enableFlags() {
		if (!SemanticVersion.getMinecraftVersion()
			.isNewerOrEqual("1.20.5")) return this;

		Multimap<Attribute, AttributeModifier> modifiers = this.itemMeta.getAttributeModifiers();
		if (modifiers == null) {
			modifiers = HashMultimap.create();
			this.itemMeta.setAttributeModifiers(modifiers);
		}
		return this;
	}

	public EnhancedItemBuilder flag(ItemFlag... flag) {
		this.itemMeta.addItemFlags(flag);
		return this;
	}

	public EnhancedItemBuilder hideAttributes() {
		this.flag(
			ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS,
			ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_PLACED_ON,
			ItemFlag.HIDE_UNBREAKABLE
		);
		return this;
	}

	public EnhancedItemBuilder replaceFlags(List<ItemFlag> flags) {
		this.itemMeta.getItemFlags()
			.forEach(this.itemMeta::removeItemFlags);
		flags.forEach(this.itemMeta::addItemFlags);
		return this;
	}

	public EnhancedItemBuilder glow() {
		this.enchant(
			this.itemStack.getType() == Material.BOW ?
				VanillaEnchantment.LUCK_OF_THE_SEA.getEnchantment() :
				VanillaEnchantment.INFINITY.getEnchantment(), 1
		);
		this.flag(ItemFlag.HIDE_ENCHANTS);
		return this;
	}

	public EnhancedItemBuilder head(String data) {
		if (!(this.itemMeta instanceof SkullMeta)) return this;

		String[] elements = data.split(":", 2);
		if (elements.length == 1) {
			SkullUtil.getInstance()
				.fromName((SkullMeta) this.itemMeta, elements[0]);
		} else {
			switch (elements[0]) {
				case "b64":
					SkullUtil.getInstance()
						.fromB64((SkullMeta) this.itemMeta, elements[1]);
					break;
				case "url":
					SkullUtil.getInstance()
						.fromUrl((SkullMeta) this.itemMeta, elements[1]);
			}
		}

		return this;
	}

	public EnhancedItemBuilder color(Color color) {
		if (this.itemMeta instanceof LeatherArmorMeta)
			((LeatherArmorMeta) this.itemMeta).setColor(color);
		return this;
	}

	public EnhancedItemBuilder armorTrim(TrimMaterial material, TrimPattern pattern) {
		if (!hasTrimSupport()) return this;
		if (!(this.itemMeta instanceof ArmorMeta)) return this;
		((ArmorMeta) this.itemMeta).setTrim(new ArmorTrim(material, pattern));
		return this;
	}

	public EnhancedItemBuilder attribute(Attribute attribute, AttributeModifier modifier) {
		if (!hasAttributeSupport()) return this;
		this.itemMeta.addAttributeModifier(attribute, modifier);
		return this;
	}

	public ItemStack build() {
		this.itemStack.setItemMeta(this.itemMeta);
		this.itemStack.setAmount(amount);
		return this.itemStack;
	}

	public static class Serializer {
		public static Map<String, Object> serialize(ItemStack itemStack) {
			Map<String, Object> result = new HashMap<>();
			if (itemStack == null) return result;

			result.put(
				"material", itemStack.getType()
					.name()
			);
			if (itemStack.getType() == Material.AIR) return result;

			result.put("amount", itemStack.getAmount());
			if (!itemStack.hasItemMeta()) return result;

			ItemMeta itemMeta = itemStack.getItemMeta();
			result.put(
				"name", itemMeta.hasDisplayName() ?
					itemMeta.getDisplayName() :
					null
			);
			result.put("lore", itemMeta.getLore());
			result.put(
				"model", hasModelSupport() && itemMeta.hasCustomModelData() ?
					itemMeta.getCustomModelData() :
					null
			);

			int damage = isUsingNewDamage() ?
				itemMeta instanceof Damageable ?
					((Damageable) itemMeta).getDamage() :
					0 :
				itemStack.getDurability();
			result.put(
				"damage", damage == 0 ?
					null :
					damage
			);

			boolean unbreakable = hasUnbreakableSupport() && itemMeta.isUnbreakable();
			result.put(
				"unbreakable", unbreakable ?
					true :
					null
			);

			result.put(
				"enchantments", itemMeta.getEnchants()
					.entrySet()
					.stream()
					.map(entry -> entry.getKey()
						.getKey()
						.getKey() + " " + entry.getValue())
					.collect(Collectors.toList())
			);
			boolean isGlowing = itemStack.getType() == Material.BOW ?
				itemMeta.hasEnchant(
					VanillaEnchantment.LUCK_OF_THE_SEA.getEnchantment()) :
				itemMeta.hasEnchant(
					VanillaEnchantment.INFINITY.getEnchantment());
			result.put(
				"glow", isGlowing ?
					true :
					null
			);
			if (isGlowing) {
				((List<String>) result.get("enchantments")).remove(
					itemStack.getType() == Material.BOW ?
						VanillaEnchantment.LUCK_OF_THE_SEA.getEnchantment()
							.getKey()
							.getKey() :
						VanillaEnchantment.INFINITY.getEnchantment()
							.getKey()
							.getKey() + " 1");
			}
			if (((List<String>) result.get("enchantments")).isEmpty()) {
				result.remove("enchantments");
			}

			result.put(
				"item-flags", itemMeta.getItemFlags()
					.stream()
					.map(ItemFlag::name)
					.collect(Collectors.toList())
			);
			if (((List<String>) result.get("item-flags")).isEmpty()) {
				result.remove("item-flags");
			}

			result.put(
				"head-owner", itemStack.getType() == Material.PLAYER_HEAD ?
					SkullUtil.isUsingNewHead() ?
						((SkullMeta) itemMeta).getOwningPlayer()
							.getName() :
						((SkullMeta) itemMeta).getOwner() :
					null
			);

			if (itemMeta instanceof LeatherArmorMeta) {
				Color color = ((LeatherArmorMeta) itemMeta).getColor();
				result.put(
					"armor-color",
					color.getRed() + " " + color.getGreen() + " " + color.getBlue()
				);
			}

			if (hasTrimSupport() && itemMeta instanceof ArmorMeta) {
				ArmorTrim trim = ((ArmorMeta) itemMeta).getTrim();
				if (trim != null) {
					result.put(
						"armor-trim", trim.getMaterial()
							.getKey()
							.getKey() + " " + trim.getPattern()
							.getKey()
							.getKey()
					);
				}
			}

			if (hasAttributeSupport() && itemMeta.getAttributeModifiers() != null) {
				result.put(
					"attributes", itemMeta.getAttributeModifiers()
						.entries()
						.stream()
						.map(entry -> entry.getKey()
							.name() + " " + entry.getValue()
							.getUniqueId() + " " + entry.getValue()
							.getName() + " " + entry.getValue()
							.getAmount() + " " + entry.getValue()
							.getOperation()
							.name() + (entry.getValue()
							.getSlot() != null ?
							" " + entry.getValue()
								.getSlot()
								.name() :
							""))
				);
			}

			return result;
		}

		public static String serializeToBase64(ItemStack itemStack) throws IOException {
			ByteArrayOutputStream io = new ByteArrayOutputStream();
			BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
			os.writeObject(itemStack);
			os.flush();

			byte[] serializedObject = io.toByteArray();

			return new String(Base64.getEncoder()
				.encode(serializedObject));
		}

		public static ItemStack deserialize(Map<String, Object> serialized) {
			if (serialized.isEmpty() || !serialized.containsKey("material"))
				return null;

			EnhancedItemBuilder itemBuilder = new EnhancedItemBuilder(
				Material.valueOf(serialized.get("material")
					.toString()
					.toUpperCase()));

			validateAndCompute(
				serialized, "amount",
				(data) -> Integer.parseInt(String.valueOf(data)),
				itemBuilder::amount
			);
			validateAndCompute(
				serialized, "name", String::valueOf, itemBuilder::displayName);
			validateAndCompute(
				serialized, "lore",
				(data) -> (List<String>) data, itemBuilder::replaceLore
			);
			validateAndCompute(
				serialized, "model",
				(data) -> Integer.parseInt(String.valueOf(data)),
				itemBuilder::model
			);
			validateAndCompute(
				serialized, "damage",
				(data) -> Integer.parseInt(String.valueOf(data)),
				itemBuilder::damage
			);
			validateAndCompute(
				serialized, "unbreakable",
				(data) -> Boolean.parseBoolean(String.valueOf(data)),
				itemBuilder::unbreakable
			);
			validateAndCompute(
				serialized, "enchantments", (data) -> {
					List<String> raw = (List<String>) data;
					Map<Enchantment, Integer> result = new HashMap<>();
					raw.stream()
						.map(s -> s.split(" ", 2))
						.forEach((elements) -> result.put(
							Enchantment.getByKey(NamespacedKey.minecraft(
								elements[0].toLowerCase())),
							Integer.parseInt(elements[1])
						));

					return result;
				}, itemBuilder::enchant
			);
			validateAndCompute(
				serialized, "glow",
				(data) -> Boolean.parseBoolean(String.valueOf(data)),
				(state) -> {
					if (state) itemBuilder.glow();
				}
			);
			validateAndCompute(
				serialized, "item-flags",
				(data) -> ((List<String>) data).stream()
					.map(s -> ItemFlag.valueOf(s.toUpperCase()))
					.collect(Collectors.toList()), itemBuilder::replaceFlags
			);
			validateAndCompute(
				serialized, "head", String::valueOf, itemBuilder::head);
			validateAndCompute(
				serialized, "armor-color", (data) -> {
					String[] elements = String.valueOf(data)
						.split(" ", 3);
					return Color.fromRGB(
						Integer.parseInt(elements[0]),
						Integer.parseInt(elements[1]),
						Integer.parseInt(elements[2])
					);
				}, itemBuilder::color
			);

			if (hasTrimSupport()) {
				validateAndCompute(
					serialized, "armor-trim", (data) -> {
						String[] elements = String.valueOf(data)
							.split(" ", 2);
						return new AbstractMap.SimpleEntry<>(
							Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(
								elements[0].toLowerCase())),
							Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(
								elements[1].toLowerCase()))
						);
					}, (entry) -> itemBuilder.armorTrim(
						entry.getKey(),
						entry.getValue()
					)
				);
			}

			if (hasAttributeSupport()) {
				validateAndCompute(
					serialized, "attributes", (data) -> {
						Multimap<Attribute, AttributeModifier> result = HashMultimap.create();

						((List<String>) data).stream()
							.map(s -> s.split(" ", 6))
							.forEach(elements -> {
								Attribute attribute = Attribute.valueOf(
									elements[0].toUpperCase());
								UUID uuid = UUID.fromString(elements[1]);
								String name = elements[2];
								double amount = Double.parseDouble(elements[3]);
								AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(
									elements[4].toUpperCase());
								EquipmentSlot slot = elements.length == 6 ?
									EquipmentSlot.valueOf(
										elements[5].toUpperCase()) :
									null;
								result.put(
									attribute,
									new AttributeModifier(
										uuid, name, amount,
										operation, slot
									)
								);
							});

						return result;
					}, (data) -> data.forEach(itemBuilder::attribute)
				);
			}

			return itemBuilder.build();
		}

		public static ItemStack deserializeFromBase64(String base64) throws IOException, ClassNotFoundException {
			byte[] serializedObject = Base64.getDecoder()
				.decode(base64);

			ByteArrayInputStream in = new ByteArrayInputStream(
				serializedObject);
			BukkitObjectInputStream is = new BukkitObjectInputStream(in);

			return (ItemStack) is.readObject();
		}

		protected static <T> void validateAndCompute(Map<String, Object> serialized, String key, Function<Object, T> modify, Consumer<T> action) {
			if (!serialized.containsKey(key)) return;
			action.accept(modify.apply(serialized.get(key)));
		}
	}
}
