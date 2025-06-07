package dev.projectenhanced.enhancedspigot.util;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class EnhancedPlugin extends JavaPlugin {
    @Getter private DependencyProvider dependencyProvider;

    @Override
    public final void onEnable() {
        this.dependencyProvider = new DependencyProvider();
        this.dependencyProvider.register(this, getClass(), EnhancedPlugin.class, JavaPlugin.class);

        load();
        SchedulerUtil.runTaskLater(this, (task) -> postLoad(), 1);
    }

    @Override
    public final void onDisable() {
        unload();
    }

    public abstract void load();
    public abstract void postLoad();
    public abstract void reload();
    public abstract void unload();
}
