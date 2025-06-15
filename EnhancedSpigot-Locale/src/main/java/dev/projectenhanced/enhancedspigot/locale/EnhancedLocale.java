package dev.projectenhanced.enhancedspigot.locale;

import dev.projectenhanced.enhancedspigot.locale.bridge.IPlatformBridge;
import dev.projectenhanced.enhancedspigot.locale.bridge.PaperBridge;
import dev.projectenhanced.enhancedspigot.locale.bridge.SpigotBridge;
import dev.projectenhanced.enhancedspigot.util.IClosable;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public abstract class EnhancedLocale implements IClosable {
    private final JavaPlugin plugin;
    private final LocaleSerializer serializer;
    private final File folder;
    private File file;
    private YamlConfiguration configuration;

    @Getter private String locale;
    @Getter private IPlatformBridge bridge;

    public EnhancedLocale(JavaPlugin plugin, String locale) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(),"locales");
        this.locale = locale;
        this.serializer = new LocaleSerializer();
    }

    /**
     * Creates locale file and loads configuration
     */
    public void init() {
        this.init(true);
    }

    protected void init(boolean main) {
        if(main) {
            if(Bukkit.getName().contains("Spigot")) bridge = new SpigotBridge(this.plugin);
            else bridge = new PaperBridge();

            String trueLocale = this.locale;
            this.supportedLocales().stream()
                    .filter(s -> !s.equalsIgnoreCase(this.locale))
                    .forEach(s -> {
                        this.locale = s;
                        this.file = new File(folder, locale + ".yml");
                        init(false);
                    });

            this.locale = trueLocale;
            this.file = new File(folder, locale + ".yml");
        }

        boolean newLocale = !this.file.exists();
        if(newLocale) {
            this.file.getParentFile().mkdirs();
            TryCatchUtil.tryRun(this.file::createNewFile);
        }

        this.configuration = YamlConfiguration.loadConfiguration(this.file);

        if(newLocale) this.save();
        if(main) this.reload();
    }

    /**
     * Reloads locales
     */
    public void reload() {
        this.serializer.deserializeTo(this.configuration, this.getClass(),this,this);
        this.save();
    }

    /**
     * Saves locales
     */
    public void save() {
        this.serializer.serializeTo(this,this.getClass(),this,this.configuration);
        TryCatchUtil.tryRun(() -> this.configuration.save(this.file));
    }

    @Override
    public void close() {
        if(this.bridge != null) this.bridge.close();
    }

    public abstract List<String> supportedLocales();
}
