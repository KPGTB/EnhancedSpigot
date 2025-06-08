package dev.projectenhanced.enhancedspigot.config.util;

import dev.projectenhanced.enhancedspigot.util.SemanticVersion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.List;
import java.util.Map;

public class SectionUtil {
    public static ConfigurationSection createEmpty() {
        return new MemoryConfiguration();
    }

    public static ConfigurationSection create(Map<?,?> map) {
        ConfigurationSection section = createEmpty();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                section.createSection(entry.getKey().toString(), (Map<?, ?>) entry.getValue());
            } else {
                section.set(entry.getKey().toString(), entry.getValue());
            }
        }

        return section;
    }

    public static void addComments(ConfigurationSection section, String path, List<String> comments) {
        if(!SemanticVersion.getMinecraftVersion().isNewerOrEqual("1.19.0")) return; // TODO: Add support on lower versions
        section.setComments(path,comments);
    }
}
