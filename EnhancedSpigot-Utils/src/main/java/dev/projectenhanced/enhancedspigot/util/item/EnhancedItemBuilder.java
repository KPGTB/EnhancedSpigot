package dev.projectenhanced.enhancedspigot.util.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.projectenhanced.enhancedspigot.util.SemanticVersion;
import dev.projectenhanced.enhancedspigot.util.VanillaEnchantment;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
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

        if(itemStack.getType() == Material.AIR || !itemStack.hasItemMeta()) {
            throw new IllegalArgumentException("Provided ItemStack is AIR or doesn't have Item Meta");
        }

        this.itemMeta = itemStack.getItemMeta();
    }

    public EnhancedItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    @Deprecated
    public EnhancedItemBuilder(MaterialData materialData) {
        this(new ItemStack(materialData.getItemType(), 1, materialData.getData()));
    }

    public EnhancedItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public EnhancedItemBuilder model(int model) {
        if(hasModelSupport()) {
            this.itemMeta.setCustomModelData(model);
        }
        return this;
    }

    public EnhancedItemBuilder damage(int damage) {
        if(isUsingNewDamage()) {
            if(this.itemMeta instanceof Damageable) ((Damageable) this.itemMeta).setDamage(damage);
        } else {
            this.itemStack.setDurability((short) damage);
        }
        return this;
    }

    public EnhancedItemBuilder unbreakable() {
        return this.unbreakable(true);
    }

    public EnhancedItemBuilder unbreakable(boolean state) {
        if(hasUnbreakableSupport()) {
            this.itemMeta.setUnbreakable(state);
        }
        return this;
    }

    public EnhancedItemBuilder enchant(Enchantment enchantment, int level) {
        this.itemMeta.addEnchant(enchantment,level,true);
        return this;
    }

    public EnhancedItemBuilder enchant(Map<Enchantment, Integer> enchantments) {
        enchantments.forEach(this::enchant);
        return this;
    }

    public EnhancedItemBuilder clearEnchantments() {
        this.itemMeta.getEnchants().forEach((enchantment, level) -> this.itemMeta.removeEnchant(enchantment));
        return this;
    }

    public EnhancedItemBuilder displayName(String name) {
        this.itemMeta.setDisplayName(name);
        return this;
    }

    public EnhancedItemBuilder lore(String... lines) {
        List<String> lore = new ArrayList<>();
        if(this.itemMeta.hasLore()) lore = this.itemMeta.getLore();
        lore.addAll(Arrays.asList(lines));
        this.itemMeta.setLore(lore);
        return this;
    }

    public EnhancedItemBuilder replaceLore(List<String> lines) {
        this.itemMeta.setLore(lines);
        return this;
    }

    public EnhancedItemBuilder enableFlags() {
        if(!SemanticVersion.getMinecraftVersion().isNewerOrEqual("1.20.5")) return this;

        Multimap<Attribute, AttributeModifier> modifiers = this.itemMeta.getAttributeModifiers();
        if(modifiers == null) {
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
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_DESTROYS,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_PLACED_ON,
                ItemFlag.HIDE_UNBREAKABLE
        );
        return this;
    }

    public EnhancedItemBuilder replaceFlags(List<ItemFlag> flags) {
        this.itemMeta.getItemFlags().forEach(this.itemMeta::removeItemFlags);
        flags.forEach(this.itemMeta::addItemFlags);
        return this;
    }

    public EnhancedItemBuilder glow() {
        this.enchant(this.itemStack.getType() == Material.BOW ? VanillaEnchantment.LUCK_OF_THE_SEA.getEnchantment() : VanillaEnchantment.INFINITY.getEnchantment(), 1);
        this.flag(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public EnhancedItemBuilder head(String owner) {
        if(!(this.itemMeta instanceof SkullMeta)) return this;

        if(isUsingNewHead()) {
             ((SkullMeta) this.itemMeta).setOwningPlayer(Bukkit.getOfflinePlayer(owner));
        } else {
            ((SkullMeta) this.itemMeta).setOwner(owner);
        }
        return this;
    }

    public EnhancedItemBuilder color(Color color) {
        if (this.itemMeta instanceof LeatherArmorMeta) ((LeatherArmorMeta) this.itemMeta).setColor(color);
        return this;
    }

    public EnhancedItemBuilder armorTrim(TrimMaterial material, TrimPattern pattern) {
        if(!hasTrimSupport()) return this;
        if(!(this.itemMeta instanceof ArmorMeta)) return this;
        ((ArmorMeta)this.itemMeta).setTrim(new ArmorTrim(material, pattern));
        return this;
    }

    public EnhancedItemBuilder attribute(Attribute attribute, AttributeModifier modifier) {
        if(!hasAttributeSupport()) return this;
        this.itemMeta.addAttributeModifier(attribute,modifier);
        return this;
    }

   public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        this.itemStack.setAmount(amount);
        return this.itemStack;
   }

    public static boolean hasTrimSupport() {
        return SemanticVersion.getMinecraftVersion().isNewerOrEqual("1.20.0");
    }

    public static boolean hasAttributeSupport() {
        return SemanticVersion.getMinecraftVersion().isNewerOrEqual("1.14.0");
    }

    public static boolean hasModelSupport() {
        return SemanticVersion.getMinecraftVersion().isNewerOrEqual("1.14.0");
    }

    public static boolean isUsingNewDamage() {
        return SemanticVersion.getMinecraftVersion().isNewerOrEqual("1.13.0");
    }

    public static boolean isUsingNewHead() {
        return SemanticVersion.getMinecraftVersion().isNewerOrEqual("1.13.0");
    }

    public static boolean hasUnbreakableSupport() {
        return SemanticVersion.getMinecraftVersion().isNewerOrEqual("1.13.0");
    }

    public static class Serializer {
        public static Map<String, Object> serialize(ItemStack itemStack) {
            Map<String, Object> result = new HashMap<>();
            if(itemStack == null) return result;

            result.put("material", itemStack.getType().name());
            if(itemStack.getType() == Material.AIR) return result;

            result.put("amount", itemStack.getAmount());
            if(!itemStack.hasItemMeta()) return result;

            ItemMeta itemMeta = itemStack.getItemMeta();
            result.put("name", itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : null);
            result.put("lore", itemMeta.getLore());
            result.put("model", hasModelSupport() && itemMeta.hasCustomModelData() ? itemMeta.getCustomModelData() : 0);

            result.put("damage",
                    isUsingNewDamage() ?
                            itemMeta instanceof Damageable ? ((Damageable) itemMeta).getDamage() : 0
                            :
                            itemStack.getDurability()
            );
            result.put("unbreakable", hasUnbreakableSupport() ? itemMeta.isUnbreakable() : false);

            result.put("enchantments", itemMeta.getEnchants()
                    .entrySet()
                    .stream()
                    .map(entry -> entry.getKey().getKey().getKey() + " " + entry.getValue())
                    .collect(Collectors.toList())
            );
            result.put("glow",
                    itemStack.getType() == Material.BOW ? itemMeta.hasEnchant(VanillaEnchantment.LUCK_OF_THE_SEA.getEnchantment()) : itemMeta.hasEnchant(VanillaEnchantment.INFINITY.getEnchantment())
            );
            result.put("item-flags", itemMeta.getItemFlags()
                    .stream()
                    .map(ItemFlag::name)
                    .collect(Collectors.toList())
            );

            result.put("head-owner",
                    itemStack.getType() == Material.PLAYER_HEAD ?
                            isUsingNewHead() ? ((SkullMeta)itemMeta).getOwningPlayer().getName() : ((SkullMeta)itemMeta).getOwner()
                            : null
            );

            if(itemMeta instanceof LeatherArmorMeta) {
                Color color = ((LeatherArmorMeta) itemMeta).getColor();
                result.put("armor-color", color.getRed() + " " + color.getGreen() + " " + color.getBlue());
            }

            if(hasTrimSupport() && itemMeta instanceof ArmorMeta) {
                ArmorTrim trim = ((ArmorMeta) itemMeta).getTrim();
                result.put("armor-trim", trim.getMaterial().getKey().getKey() + " " + trim.getPattern().getKey().getKey());
            }

            if(hasAttributeSupport() && itemMeta.getAttributeModifiers() != null) {
                result.put("attributes", itemMeta.getAttributeModifiers()
                        .entries()
                        .stream()
                        .map(entry ->
                                entry.getKey().name() + " " +
                                        entry.getValue().getUniqueId() + " " +
                                        entry.getValue().getName() + " " +
                                        entry.getValue().getAmount() + " " +
                                        entry.getValue().getOperation().name() +
                                        (entry.getValue().getSlot() != null ? " " + entry.getValue().getSlot().name() : "")
                        )
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

            return new String(Base64.getEncoder().encode(serializedObject));
        }

        public static ItemStack deserialize(Map<String,Object> serialized) {
            if(serialized.isEmpty() || !serialized.containsKey("material")) return null;

            EnhancedItemBuilder itemBuilder = new EnhancedItemBuilder(Material.valueOf(serialized.get("material").toString().toUpperCase()));

            validateAndCompute(serialized, "amount", (data) -> Integer.parseInt(String.valueOf(data)), itemBuilder::amount);
            validateAndCompute(serialized, "name", String::valueOf, itemBuilder::displayName);
            validateAndCompute(serialized, "lore", (data) -> (List<String>) data, itemBuilder::replaceLore);
            validateAndCompute(serialized, "model", (data) -> Integer.parseInt(String.valueOf(data)), itemBuilder::model);
            validateAndCompute(serialized, "damage", (data) -> Integer.parseInt(String.valueOf(data)), itemBuilder::damage);
            validateAndCompute(serialized, "unbreakable", (data) -> Boolean.parseBoolean(String.valueOf(data)), itemBuilder::unbreakable);
            validateAndCompute(serialized, "enchantments",
                    (data) -> {
                        List<String> raw = (List<String>) data;
                        Map<Enchantment, Integer> result = new HashMap<>();
                        raw.stream()
                                .map(s -> s.split(" ", 2))
                                .forEach((elements) -> result.put(
                                                Enchantment.getByKey(NamespacedKey.minecraft(elements[0].toLowerCase())),
                                                Integer.parseInt(elements[1])
                                        )
                                );

                        return result;
                    },
                    itemBuilder::enchant
            );
            validateAndCompute(serialized, "glow", (data) -> Boolean.parseBoolean(String.valueOf(data)), (state) -> {
                if(state) itemBuilder.glow();
            });
            validateAndCompute(serialized, "item-flags",
                    (data) -> ((List<String>)data).stream()
                            .map(s -> ItemFlag.valueOf(s.toUpperCase()))
                            .collect(Collectors.toList()),
                    itemBuilder::replaceFlags
            );
            validateAndCompute(serialized, "head-owner", String::valueOf, itemBuilder::head);
            validateAndCompute(serialized, "armor-color",
                    (data) -> {
                        String[] elements = String.valueOf(data).split(" ", 3);
                        return Color.fromRGB(
                                Integer.parseInt(elements[0]),
                                Integer.parseInt(elements[1]),
                                Integer.parseInt(elements[2])
                        );
                    },
                    itemBuilder::color
            );

            if(hasTrimSupport()) {
                validateAndCompute(serialized, "armor-trim",
                        (data) -> {
                            String[] elements = String.valueOf(data).split(" ", 2);
                            return new AbstractMap.SimpleEntry<>(
                                    Registry.TRIM_MATERIAL.get(NamespacedKey.minecraft(elements[0].toLowerCase())),
                                    Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(elements[1].toLowerCase()))
                            );
                        },
                        (entry) -> itemBuilder.armorTrim(entry.getKey(), entry.getValue())
                );
            }

            if(hasAttributeSupport()) {
                validateAndCompute(serialized, "attributes",
                        (data) -> {
                            Multimap<Attribute, AttributeModifier> result = HashMultimap.create();

                            ((List<String>)data).stream()
                                    .map(s -> s.split(" ", 6))
                                    .forEach(elements -> {
                                        Attribute attribute = Attribute.valueOf(elements[0].toUpperCase());
                                        UUID uuid = UUID.fromString(elements[1]);
                                        String name = elements[2];
                                        double amount = Double.parseDouble(elements[3]);
                                        AttributeModifier.Operation operation = AttributeModifier.Operation.valueOf(elements[4].toUpperCase());
                                        EquipmentSlot slot = elements.length == 6 ? EquipmentSlot.valueOf(elements[5].toUpperCase()) : null;
                                        result.put(
                                                attribute,
                                                new AttributeModifier(uuid,name,amount,operation,slot)
                                        );
                                    });

                            return result;
                        },
                        (data) -> data.forEach(itemBuilder::attribute)
                );
            }

            return itemBuilder.build();
        }

        public static ItemStack deserializeFromBase64(String base64) throws IOException, ClassNotFoundException {
            byte[] serializedObject = Base64.getDecoder().decode(base64);

            ByteArrayInputStream in = new ByteArrayInputStream(serializedObject);
            BukkitObjectInputStream is = new BukkitObjectInputStream(in);

            return (ItemStack) is.readObject();
        }

        protected static <T> void validateAndCompute(Map<String,Object> serialized, String key, Function<Object,T> modify, Consumer<T> action) {
            if(!serialized.containsKey(key)) return;
            action.accept(modify.apply(serialized.get(key)));
        }
    }
}
