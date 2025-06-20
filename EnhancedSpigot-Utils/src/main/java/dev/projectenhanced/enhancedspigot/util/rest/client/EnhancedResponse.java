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

package dev.projectenhanced.enhancedspigot.util.rest.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public class EnhancedResponse {
	private final int status;
	private final String message;
	private final Map<String, List<String>> headers;

	private final String rawOutput;
	@Setter
	private Gson gson = new Gson();

	public String getAsString() {
		return this.rawOutput;
	}

	public JsonObject getAsJson() {
		return this.getAsClass(JsonObject.class);
	}

	public <T> T getAsClass(Class<T> clazz) {
		return gson.fromJson(this.rawOutput, clazz);
	}
}
