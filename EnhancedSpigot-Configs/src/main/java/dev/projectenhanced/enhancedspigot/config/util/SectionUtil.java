package dev.projectenhanced.enhancedspigot.config.util;

import dev.projectenhanced.enhancedspigot.util.SemanticVersion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SectionUtil {
    /**
     * Creates an empty ConfigurationSection
     * @return empty section
     */
    public static ConfigurationSection createEmpty() {
        return new MemoryConfiguration();
    }

    /**
     * Creates filled ConfigurationSection
     * @param map Section entries
     * @return New ConfigurationSection filled with entries
     */
    public static ConfigurationSection create(Map<?,?> map) {
        ConfigurationSection section = createEmpty();
        fillSection(section, map);
        return section;
    }

    /**
     * Fills ConfigurationSection with entries
     * @param section ConfigurationSection
     * @param map Entries
     */
    public static void fillSection(ConfigurationSection section, Map<?,?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                section.createSection(entry.getKey().toString(), (Map<?, ?>) entry.getValue());
            } else {
                section.set(entry.getKey().toString(), entry.getValue());
            }
        }
    }

    /**
     * Adds comments to ConfigurationSection
     * @param section ConfigurationSection
     * @param path Path of comment
     * @param comments Comment
     */
    public static void addComments(ConfigurationSection section, String path, String... comments) {
        addComments(section,path, Arrays.stream(comments).collect(Collectors.toList()));
    }

    /**
     * Adds comments to ConfigurationSection
     * @param section ConfigurationSection
     * @param path Path of comment
     * @param comments Comment
     */
    public static void addComments(ConfigurationSection section, String path, List<String> comments) {
        if(!SemanticVersion.getMinecraftVersion().isNewerOrEqual("1.19.0")) return; // TODO: Add support on lower versions
        section.setComments(path,comments);
    }
}
