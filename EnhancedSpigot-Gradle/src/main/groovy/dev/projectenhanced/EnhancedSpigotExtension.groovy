package dev.projectenhanced

import org.gradle.api.provider.Property

abstract class EnhancedSpigotExtension {
    abstract Property<String> getRelocationPath();
    abstract Property<Boolean> getExcludeDependencies();
    abstract Property<Boolean> getImportVault();
    abstract Property<Boolean> getImportPapi();

    abstract Property<String> getCommandsModule();
    abstract Property<String> getConfigsModule();
    abstract Property<String> getDataModule();
    abstract Property<String> getItemsModule();
    abstract Property<String> getLocaleModule();
    abstract Property<String> getMenusModule();
    abstract Property<String> getPluginModule();
    abstract Property<String> getUtilsModule();
}
