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

package dev.projectenhanced.enhancedspigot.config.serializer.impl;

import dev.projectenhanced.enhancedspigot.config.EnhancedConfig;
import dev.projectenhanced.enhancedspigot.config.serializer.ISerializer;

import java.math.BigDecimal;

public class BigDecimalSerializer implements ISerializer<BigDecimal> {
	@Override
	public Object serialize(BigDecimal object, Class<? extends BigDecimal> objectClass, EnhancedConfig config) {
		return object.toPlainString();
	}

	@Override
	public void serializeTo(BigDecimal object, Class<? extends BigDecimal> objectClass, EnhancedConfig config, Object to) {
		throw new UnsupportedOperationException("serializeTo not implemented");
	}

	@Override
	public BigDecimal deserialize(Object serialized, Class<? extends BigDecimal> targetClass, EnhancedConfig config) {
		if (serialized instanceof BigDecimal) return (BigDecimal) serialized;
		if (serialized instanceof Double) return BigDecimal.valueOf((double) serialized);
		return BigDecimal.valueOf(Double.parseDouble(serialized.toString()));
	}

	@Override
	public void deserializeTo(Object serialized, Class<? extends BigDecimal> targetClass, EnhancedConfig config, Object to) {
		throw new UnsupportedOperationException("deserializeTo not implemented");
	}

	@Override
	public boolean convertToSection() {
		return false;
	}
}
