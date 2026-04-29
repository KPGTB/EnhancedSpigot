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

import org.gradle.api.provider.Property

abstract class Extension {
    abstract Property<String> getRelocationPath();

    abstract Property<Boolean> getExcludeDependencies();

    abstract Property<Boolean> getImportVault();

    abstract Property<Boolean> getImportPapi();

    abstract Property<List<EnhancedModule>> getEnabledModules();

    abstract Property<String> getModulesVersion();

    abstract Property<Map<EnhancedModule, String>> getOverrideVersions();
}
