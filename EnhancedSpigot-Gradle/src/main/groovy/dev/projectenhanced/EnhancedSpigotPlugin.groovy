/*
 * Copyright 2025 KPG-TB
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.projectenhanced

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

class EnhancedSpigotPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def ext = project.extensions.create("enhancedspigot", EnhancedSpigotExtension);
        ext.relocationPath.convention("")
        ext.excludeDependencies.convention(true)
        ext.importVault.convention(false);
        ext.importPapi.convention(false);
        ext.commandsModule.convention("")
        ext.configsModule.convention("")
        ext.dataModule.convention("")
        ext.itemsModule.convention("")
        ext.localeModule.convention("")
        ext.menusModule.convention("")
        ext.pluginModule.convention("")
        ext.utilsModule.convention("")

        project.getPluginManager().apply("io.freefair.lombok")
        project.getPluginManager().apply("com.gradleup.shadow")

        project.tasks.register("enhancedSpigotLibraries", EnhancedSpigotLibrariesTask) {
            group = 'enhancedspigot'
            description = 'Print all libraries required in plugin.yml'
            dataModule.set(ext.dataModule)
            excludeDependencies.set(ext.excludeDependencies)
        }

        project.afterEvaluate {
            project.repositories {
                mavenCentral()
                mavenLocal()
                maven { url "https://nexus.projectenhanced.dev/repository/maven-snapshots" }
                if (!ext.menusModule.get().isEmpty()) maven { url 'https://repo.codemc.io/repository/maven-snapshots' }
                if (ext.importVault.get()) maven { url 'https://jitpack.io' }
                if (ext.importPapi.get()) maven { url = 'https://repo.extendedclip.com/releases/' }
            }

            project.dependencies {
                if (!ext.utilsModule.get().isEmpty()) implementation "dev.projectenhanced:EnhancedSpigot-Utils:" + ext.utilsModule.get()
                if (!ext.configsModule.get().isEmpty()) implementation "dev.projectenhanced:EnhancedSpigot-Configs:" + ext.configsModule.get()
                if (!ext.localeModule.get().isEmpty()) implementation "dev.projectenhanced:EnhancedSpigot-Locale:" + ext.localeModule.get()
                if (!ext.dataModule.get().isEmpty()) implementation "dev.projectenhanced:EnhancedSpigot-Data:" + ext.dataModule.get()
                if (!ext.commandsModule.get().isEmpty()) implementation "dev.projectenhanced:EnhancedSpigot-Commands:" + ext.commandsModule.get()
                if (!ext.menusModule.get().isEmpty()) implementation "dev.projectenhanced:EnhancedSpigot-Menus:" + ext.menusModule.get()
                if (!ext.itemsModule.get().isEmpty()) implementation "dev.projectenhanced:EnhancedSpigot-Items:" + ext.itemsModule.get()
                if (!ext.pluginModule.get().isEmpty()) implementation "dev.projectenhanced:EnhancedSpigot-Plugin:" + ext.pluginModule.get()

                if (ext.importVault.get()) compileOnly "com.github.MilkBowl:VaultAPI:1.7"
                if (ext.importPapi.get()) compileOnly 'me.clip:placeholderapi:2.11.7'
            }

            project.tasks.withType(JavaCompile).configureEach {
                if (!ext.commandsModule.get().isEmpty()) options.compilerArgs << "-parameters"
            }

            project.tasks.named("shadowJar").configure {
                relocate(
                        "dev.projectenhanced.enhancedspigot",
                        ext.relocationPath.get().isEmpty() ?
                                project.group.toString() + "." + project.name.toLowerCase() + ".lib"
                                : ext.relocationPath.get()

                )

                if (ext.excludeDependencies.get()) {
                    exclude("com/j256/ormlite/**")
                    exclude("org/postgresql/**")
                    exclude("com/zaxxer/**")
                    exclude("org/slf4j/**")
                    exclude("org/checkerframework/**")
                }

                archiveFileName = project.name + "-" + project.version.toString() + ".jar"
            }
        }
    }
}
