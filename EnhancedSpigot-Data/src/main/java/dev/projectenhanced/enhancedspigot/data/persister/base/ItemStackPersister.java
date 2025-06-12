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

package dev.projectenhanced.enhancedspigot.data.persister.base;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.LongStringType;
import dev.projectenhanced.enhancedspigot.util.item.EnhancedItemBuilder;
import lombok.SneakyThrows;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;

public class ItemStackPersister extends LongStringType {
    private static final ItemStackPersister SINGLETON = new ItemStackPersister();

    public ItemStackPersister() {
        super(SqlType.LONG_STRING, new Class[]{ItemStack.class});
    }

    public static ItemStackPersister getSingleton() {
        return SINGLETON;
    }

    @SneakyThrows
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
        return EnhancedItemBuilder.Serializer.serializeToBase64((ItemStack) javaObject);
    }

    @SneakyThrows
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        return EnhancedItemBuilder.Serializer.deserializeFromBase64(String.valueOf(sqlArg));
    }
}
