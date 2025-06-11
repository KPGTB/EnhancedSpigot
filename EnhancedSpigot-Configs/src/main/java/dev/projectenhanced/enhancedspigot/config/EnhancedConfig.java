package dev.projectenhanced.enhancedspigot.config;

import dev.projectenhanced.enhancedspigot.config.annotation.Comment;
import dev.projectenhanced.enhancedspigot.config.serializer.ConfigSerializerRegistry;
import dev.projectenhanced.enhancedspigot.util.trycatch.TryCatchUtil;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

/**
 * Representation of config managed by EnhancedSpigot<br />
 * Library automatically maps all the fields (except those with {@link dev.projectenhanced.enhancedspigot.config.annotation.Ignore} annotation)<br />
 * By inline initialization you can set default values<br />
 * Nested configuration can be made using inner classes and fields with their type<br />
 * <strong>All classes have to contain empty constructor for serialization purposes</strong><br />
 *<br />
 * Other useful annotations: {@link dev.projectenhanced.enhancedspigot.config.annotation}
 */
@Getter
public abstract class EnhancedConfig {
    private JavaPlugin plugin;
    private File configFile;
    private YamlConfiguration configuration;

    /**
     * Empty constructor for serialization purposes<br />
     * <strong>Don't use it in other way</strong>
     */
    protected EnhancedConfig() {}
    public EnhancedConfig(JavaPlugin plugin, String folderPath, String name) {
        this(plugin,new File(folderPath), name);
    }
    public EnhancedConfig(JavaPlugin plugin, File folder, String name) {
        this.plugin = plugin;
        this.configFile = new File(folder, name.concat(".yml"));
    }

    /**
     * Represents placeholders which can be used in header's comment
     * @return Map of placeholders used in header's comment
     */
    protected abstract Map<String, String> getCommentPlaceholders();

    /**
     * Creates config file and loads configuration
     */
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

    /**
     * Reloads config data
     */
    public void reload() {
        ConfigSerializerRegistry.CustomSerializers.BASE
                .deserializeTo(this.configuration, this.getClass(),this,this);
        this.save();
    }

    /**
     * Saves config
     */
    public void save() {
        ConfigSerializerRegistry.CustomSerializers.BASE
                .serializeTo(this,this.getClass(),this,this.configuration);
        this.generateHeader();
        TryCatchUtil.tryRun(() -> this.configuration.save(this.configFile));
    }

    private void generateHeader() {
        Comment commentAnn = this.getClass().getDeclaredAnnotation(Comment.class);
        if(commentAnn == null) return;

        String header = String.join("\n", commentAnn.value());
        for (Map.Entry<String, String> entry : getCommentPlaceholders().entrySet()) {
            header = header.replace(entry.getKey(), entry.getValue());
        }
        this.configuration.options().header(header);
    }
}
