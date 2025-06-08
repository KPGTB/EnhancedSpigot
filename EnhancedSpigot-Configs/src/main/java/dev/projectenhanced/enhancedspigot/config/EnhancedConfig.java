package dev.projectenhanced.enhancedspigot.config;

import dev.projectenhanced.enhancedspigot.config.serializer.ConfigSerializerManager;
import dev.projectenhanced.enhancedspigot.util.trycatch.TryCatchUtil;
import lombok.Getter;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

@Getter
public abstract class EnhancedConfig {
    private final File configFile;
    private final JavaPlugin plugin;
    private YamlConfiguration configuration;

    public EnhancedConfig(JavaPlugin plugin, String folderPath, String name) {
        this(plugin,new File(folderPath), name);
    }

    public EnhancedConfig(JavaPlugin plugin, File folder, String name) {
        this.plugin = plugin;
        this.configFile = new File(folder, name.concat(".yml"));
    }

    public void init() {
        boolean newConfig = !this.configFile.exists();
        if(newConfig) {
            this.configFile.getParentFile().mkdirs();
            TryCatchUtil.tryRun(this.configFile::createNewFile);
        }

        this.configuration = YamlConfiguration.loadConfiguration(this.configFile);
        if(newConfig) this.save();
        this.reload();
    }

    protected abstract Map<String, String> getCommentPlaceholders();

    public void reload() {
        ConfigSerializerManager.SpecialSerializers.CONFIG
                .deserializeToObject(this.configuration, this.plugin,this,this);
    }

    public void save() {
        ConfigSerializerManager.SpecialSerializers.CONFIG
                .serializeTo(this,this.plugin,this.configuration);
        TryCatchUtil.tryRun(() -> this.configuration.save(this.configFile));
    }
}
