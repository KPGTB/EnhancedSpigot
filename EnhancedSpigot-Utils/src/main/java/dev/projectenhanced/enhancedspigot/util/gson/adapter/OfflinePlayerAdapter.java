/*
 *    Copyright 2023 KPG-TB
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
import org.bukkit.OfflinePlayer;

import java.io.IOException;
import java.util.UUID;

public class OfflinePlayerAdapter extends TypeAdapter<OfflinePlayer> {
    public static class Factory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!OfflinePlayer.class.isAssignableFrom(type.getRawType())) return null;
            return (TypeAdapter<T>) new OfflinePlayerAdapter();
        }
    }

    @Override
    public void write(JsonWriter out, OfflinePlayer value) throws IOException {
        out.beginObject();
        out.name("uuid").value(value.getUniqueId().toString());
        out.endObject();
    }

    @Override
    public OfflinePlayer read(JsonReader in) throws IOException {
        in.beginObject();
        OfflinePlayer result = null;
        if(in.hasNext()) {
            in.nextName();
            result = Bukkit.getOfflinePlayer(UUID.fromString(in.nextString()));
        }
        in.endObject();
        return result;
    }
}
