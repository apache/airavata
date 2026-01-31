/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.registry.services;

import org.apache.airavata.registry.entities.UserGroupSelectionEntity;
import org.apache.airavata.registry.repositories.UserGroupSelectionRepository;
import org.apache.airavata.common.utils.AiravataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for storing and retrieving user explicit group selections
 * when multiple groups have conflicting preferences/credentials.
 */
@Service
public class UserGroupSelectionService {

    private final UserGroupSelectionRepository selectionRepository;

    @Autowired
    public UserGroupSelectionService(UserGroupSelectionRepository selectionRepository) {
        this.selectionRepository = selectionRepository;
    }

    /**
     * Get the user's selected group ID for a resource/preference key, or null if not set.
     */
    @Transactional(readOnly = true)
    public String getSelectedGroupId(
            String userId,
            String domainId,
            String resourceType,
            String resourceId,
            String selectionKey) {
        UserGroupSelectionEntity entity = selectionRepository
                .findByUserIdAndDomainIdAndResourceTypeAndResourceIdAndSelectionKey(
                        userId, domainId, resourceType, resourceId, selectionKey);
        return entity != null ? entity.getSelectedGroupId() : null;
    }

    /**
     * Set the user's selected group for a resource/preference key.
     */
    @Transactional
    public void setSelection(
            String userId,
            String domainId,
            String resourceType,
            String resourceId,
            String selectionKey,
            String selectedGroupId) {
        long now = AiravataUtils.getUniqueTimestamp().getTime();
        UserGroupSelectionEntity existing = selectionRepository
                .findByUserIdAndDomainIdAndResourceTypeAndResourceIdAndSelectionKey(
                        userId, domainId, resourceType, resourceId, selectionKey);
        if (existing != null) {
            existing.setSelectedGroupId(selectedGroupId);
            existing.setUpdatedTime(now);
            selectionRepository.save(existing);
        } else {
            UserGroupSelectionEntity entity = new UserGroupSelectionEntity();
            entity.setUserId(userId);
            entity.setDomainId(domainId);
            entity.setResourceType(resourceType);
            entity.setResourceId(resourceId);
            entity.setSelectionKey(selectionKey);
            entity.setSelectedGroupId(selectedGroupId);
            entity.setCreatedTime(now);
            entity.setUpdatedTime(now);
            selectionRepository.save(entity);
        }
    }

    /**
     * Remove the user's selection for a resource/preference key.
     */
    @Transactional
    public void deleteSelection(
            String userId,
            String domainId,
            String resourceType,
            String resourceId,
            String selectionKey) {
        selectionRepository.deleteByUserIdAndDomainIdAndResourceTypeAndResourceIdAndSelectionKey(
                userId, domainId, resourceType, resourceId, selectionKey);
    }
}
