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

package dev.projectenhanced.enhancedspigot.util.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;

public class WorldAdapter extends TypeAdapter<World> {
	@Override
	public void write(JsonWriter out, World value) throws IOException {
		out.beginObject();
		out.name("world")
		   .value(value.getName());
		out.endObject();
	}

	@Override
	public World read(JsonReader in) throws IOException {
		in.beginObject();
		World result = null;
		if (in.hasNext()) {
			in.nextName();
			result = Bukkit.getWorld(in.nextString());
		}
		in.endObject();
		return result;
	}

	public static class Factory implements TypeAdapterFactory {
		@Override
		public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
			if (!World.class.isAssignableFrom(type.getRawType())) return null;
			return (TypeAdapter<T>) new WorldAdapter();
		}
	}
}
