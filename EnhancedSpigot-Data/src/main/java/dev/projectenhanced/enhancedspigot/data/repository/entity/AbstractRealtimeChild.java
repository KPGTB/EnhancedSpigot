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

package dev.projectenhanced.enhancedspigot.data.repository.entity;

import com.j256.ormlite.dao.ForeignCollection;
import dev.projectenhanced.enhancedspigot.data.repository.impl.RealtimeDataRepository;
import dev.projectenhanced.enhancedspigot.data.util.RealtimeCacheUtil;

public abstract class AbstractRealtimeChild<K> extends AbstractDataEntity<K> {
	protected AbstractDataEntity<K> parentEntity = null;
	protected ForeignCollection<? extends AbstractDataEntity<K>> sourceCollection = null;

	@SuppressWarnings("unchecked")
	public <T extends AbstractDataEntity<?>> void markDirty() {
		this.validateState();
		RealtimeCacheUtil.addPendingChange(
			(RealtimeDataRepository<K, ? extends AbstractDataEntity<K>>) this.repository, this.parentEntity.getKey(), (T) this, (ForeignCollection<T>) this.sourceCollection);
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractDataEntity<?>> void markCreated() {
		this.validateState();
		RealtimeCacheUtil.createForeign((RealtimeDataRepository<K, ? extends AbstractDataEntity<K>>) this.repository, (T) this, (ForeignCollection<T>) this.sourceCollection);
	}

	@SuppressWarnings("unchecked")
	public <T extends AbstractDataEntity<?>> void markDeleted() {
		this.validateState();
		RealtimeCacheUtil.removeForeign((RealtimeDataRepository<K, ? extends AbstractDataEntity<K>>) this.repository, (T) this, (ForeignCollection<T>) this.sourceCollection);
	}

	private void validateState() {
		if (this.repository == null || !(this.repository instanceof RealtimeDataRepository<?, ?>)) {
			throw new IllegalStateException("Repository must be set and must be an instance of RealtimeDataRepository to mark entity as dirty.");
		}
		if (this.parentEntity == null) {
			throw new IllegalStateException("Parent entity must be set to mark entity as dirty.");
		}
		if (this.sourceCollection == null) {
			throw new IllegalStateException("Source collection must be set to mark entity as dirty.");
		}
	}

	public void setParentEntity(AbstractDataEntity<K> parentEntity) {
		if (this.parentEntity != null) {
			throw new IllegalStateException("Parent entity cannot be set after initialization.");
		}
		this.parentEntity = parentEntity;
	}

	public void setSourceCollection(ForeignCollection<? extends AbstractDataEntity<K>> sourceCollection) {
		if (this.sourceCollection != null) {
			throw new IllegalStateException("Source collection cannot be set after initialization.");
		}
		this.sourceCollection = sourceCollection;
	}
}
