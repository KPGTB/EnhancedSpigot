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

package dev.projectenhanced.enhancedspigot.util.updater;

import dev.projectenhanced.enhancedspigot.util.SemanticVersion;
import dev.projectenhanced.enhancedspigot.util.rest.client.EnhancedRequest;
import dev.projectenhanced.enhancedspigot.util.rest.client.EnhancedResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor public class GithubUpdater implements IUpdater {
	private final String user;
	private final String repo;

	@Override
	public boolean hasUpdate(SemanticVersion version) {
		try {
			EnhancedResponse response = EnhancedRequest.builder()
				.url(
					"https://api.github.com/repos/" + this.user + "/" + this.repo + "/releases/latest")
				.build()
				.send();
			SemanticVersion latest = new SemanticVersion(response.getAsJson()
				.get("tag_name")
				.getAsString());
			return latest.isNewerThan(version);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String getDownloadLink() {
		return "https://github.com/" + this.user + "/" + this.repo + "/releases";
	}
}
