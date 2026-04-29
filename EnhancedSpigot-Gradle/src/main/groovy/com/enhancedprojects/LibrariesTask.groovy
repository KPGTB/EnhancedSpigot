/*
 * Copyright 2026 KPG-TB
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

package com.enhancedprojects

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class LibrariesTask extends DefaultTask {
    @Input
    abstract ListProperty<EnhancedModule> getEnabledModules();

    @Input
    abstract Property<Boolean> getExcludeDependencies()

    @TaskAction
    void run() {
        println 'libraries:'

        if (getExcludeDependencies().get()) {
            if (getEnabledModules().get().contains(EnhancedModule.Data)) {
                println '- com.j256.ormlite:ormlite-jdbc:6.1'
                println '- org.postgresql:postgresql:42.7.5'
                println '- com.zaxxer:HikariCP:4.0.3'
            }

            if (getEnabledModules().get().contains(EnhancedModule.Utils)) {
                println '- com.ezylang:EvalEx:3.5.0'
            }
        }
    }
}
