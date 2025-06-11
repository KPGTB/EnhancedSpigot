package dev.projectenhanced.enhancedspigot.util.listener;

import dev.projectenhanced.enhancedspigot.util.DependencyProvider;
import dev.projectenhanced.enhancedspigot.util.ReflectionUtil;
import dev.projectenhanced.enhancedspigot.util.TryCatchUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ListenerRegistry {
    public static void register(DependencyProvider dependencyProvider, JavaPlugin plugin, File jarFile, Package listenersPackage) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        ReflectionUtil.getAllClassesInPackage(jarFile,listenersPackage.getName(), EnhancedListener.class)
                .forEach(clazz -> {
                    EnhancedListener listener = TryCatchUtil.tryAndReturn(() -> (EnhancedListener) clazz.getDeclaredConstructor(DependencyProvider.class)
                            .newInstance(dependencyProvider));
                    pluginManager.registerEvents(listener, plugin);
                });
    }
}
