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

package dev.projectenhanced.enhancedspigot.util.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import dev.projectenhanced.enhancedspigot.util.gson.adapter.ItemStackAdapter;
import dev.projectenhanced.enhancedspigot.util.gson.adapter.LocationAdapter;
import dev.projectenhanced.enhancedspigot.util.gson.adapter.OfflinePlayerAdapter;
import dev.projectenhanced.enhancedspigot.util.gson.adapter.WorldAdapter;

import java.lang.reflect.Type;

public class GsonAdapterRegistry {
	private static final GsonAdapterRegistry INSTANCE = new GsonAdapterRegistry();
	private final GsonBuilder gsonBuilder;

	private GsonAdapterRegistry() {
		gsonBuilder = new GsonBuilder();
		registerDefaultAdapters();
	}

	public static GsonAdapterRegistry getInstance() {
		return INSTANCE;
	}

	private void registerDefaultAdapters() {
		registerAdapterFactory(new ItemStackAdapter.Factory());
		registerAdapterFactory(new LocationAdapter.Factory());
		registerAdapterFactory(new OfflinePlayerAdapter.Factory());
		registerAdapterFactory(new WorldAdapter.Factory());
	}

	public GsonAdapterRegistry registerAdapter(Type clazz, TypeAdapter<?> adapter) {
		gsonBuilder.registerTypeAdapter(clazz, adapter);
		return INSTANCE;
	}

	public GsonAdapterRegistry registerAdapterFactory(TypeAdapterFactory factory) {
		gsonBuilder.registerTypeAdapterFactory(factory);
		return INSTANCE;
	}

	public Gson getGson() {
		return gsonBuilder.create();
	}
}
