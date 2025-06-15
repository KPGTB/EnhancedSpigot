package dev.projectenhanced.enhancedspigot.util;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

@Getter
public enum VanillaEnchantment {
    PROTECTION("protection", "PROTECTION_ENVIRONMENTAL", "1.0.0"),
    FIRE_PROTECTION("fire_protection", "PROTECTION_FIRE", "1.0.0"),
    FEATHER_FALLING("feather_falling", "PROTECTION_FALL", "1.0.0"),
    BLAST_PROTECTION("blast_protection", "PROTECTION_EXPLOSIONS", "1.0.0"),
    PROJECTILE_PROTECTION("projectile_protection", "PROTECTION_PROJECTILE", "1.0.0"),
    RESPIRATION("respiration", "OXYGEN", "1.0.0"),
    AQUA_AFFINITY("aqua_affinity", "WATER_WORKER", "1.0.0"),
    THORNS("thorns", "THORNS", "1.0.0"),
    DEPTH_STRIDER("depth_strider", "DEPTH_STRIDER", "1.8.0"),
    FROST_WALKER("frost_walker", "FROST_WALKER", "1.9.0"),
    BINDING_CURSE("binding_curse", "BINDING_CURSE", "1.10.0"),
    SOUL_SPEED("soul_speed", "SOUL_SPEED", "1.16.0"),
    SWIFT_SNEAK("swift_sneak", "SWIFT_SNEAK", "1.19.0"),

    SHARPNESS("sharpness", "DAMAGE_ALL", "1.0.0"),
    SMITE("smite", "DAMAGE_UNDEAD", "1.0.0"),
    BANE_OF_ARTHROPODS("bane_of_arthropods", "DAMAGE_ARTHROPODS", "1.0.0"),
    KNOCKBACK("knockback", "KNOCKBACK", "1.0.0"),
    FIRE_ASPECT("fire_aspect", "FIRE_ASPECT", "1.0.0"),
    LOOTING("looting", "LOOT_BONUS_MOBS", "1.0.0"),
    SWEEPING_EDGE("sweeping_edge", "SWEEPING_EDGE", "1.11.0"),

    EFFICIENCY("efficiency", "DIG_SPEED", "1.0.0"),
    SILK_TOUCH("silk_touch", "SILK_TOUCH", "1.0.0"),
    UNBREAKING("unbreaking", "DURABILITY", "1.0.0"),
    FORTUNE("fortune", "LOOT_BONUS_BLOCKS", "1.0.0"),

    POWER("power", "ARROW_DAMAGE", "1.0.0"),
    PUNCH("punch", "ARROW_KNOCKBACK", "1.0.0"),
    FLAME("flame", "ARROW_FIRE", "1.0.0"),
    INFINITY("infinity", "ARROW_INFINITE", "1.0.0"),

    LUCK_OF_THE_SEA("luck_of_the_sea", "LUCK", "1.7.0"),
    LURE("lure", "LURE", "1.7.0"),

    LOYALTY("loyalty", "LOYALTY", "1.13.0"),
    IMPALING("impaling", "IMPALING", "1.13.0"),
    RIPTIDE("riptide", "RIPTIDE", "1.13.0"),
    CHANNELING("channeling", "CHANNELING", "1.13.0"),

    MULTISHOT("multishot", "MULTISHOT", "1.14.0"),
    QUICK_CHARGE("quick_charge", "QUICK_CHARGE", "1.14.0"),
    PIERCING("piercing", "PIERCING", "1.14.0"),

    MENDING("mending", "MENDING", "1.9.0"),
    VANISHING_CURSE("vanishing_curse", "VANISHING_CURSE", "1.11.0"),

    WIND_BURST("wind_burst", null, "1.20.5"),
    BREACH("breach", null, "1.21.0"),
    DENSITY("density", null, "1.21.0");

    private static final SemanticVersion SERVER_VERSION = SemanticVersion.getMinecraftVersion();

    private final String name;
    private final String legacyEnumName;
    private final SemanticVersion introducedInVersion;

    VanillaEnchantment(String name, String legacyEnumName, String introducedIn) {
        this.name = name;
        this.legacyEnumName = legacyEnumName;
        this.introducedInVersion = new SemanticVersion(introducedIn);
    }

    public Enchantment getEnchantment() {
        if (!SERVER_VERSION.isNewerOrEqual(introducedInVersion)) return null;

        if (SERVER_VERSION.isNewerOrEqual("1.20.5")) return Enchantment.getByKey(NamespacedKey.minecraft(name));
        return legacyEnumName != null ? Enchantment.getByName(legacyEnumName) : null;
    }
}
