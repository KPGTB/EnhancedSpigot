package dev.projectenhanced

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class EnhancedSpigotLibrariesTask extends DefaultTask {
    @Input
    abstract Property<String> getDataModule()

    @Input
    abstract Property<Boolean> getExcludeDependencies()

    @TaskAction
    void run() {
        if (!getDataModule().get().isEmpty() && getExcludeDependencies().get()) {
            println 'libraries:'
            println '- com.j256.ormlite:ormlite-jdbc:6.1'
            println '- org.postgresql:postgresql:42.7.5'
            println '- com.zaxxer:HikariCP:4.0.3'
        } else {
            println 'libraries: []'
        }
    }
}
