/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
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
package org.apache.airavata.interfaces;

import java.util.List;
import org.apache.airavata.sharing.registry.models.proto.SearchCriteria;

/**
 * Facade interface for sharing-registry operations needed by external modules.
 *
 * <p>This interface uses only primitive types and proto types already available
 * in the root module, avoiding any dependency on sharing-service JPA entities.
 * The sharing-service's {@code SharingService} implements this interface.
 */
public interface SharingFacade {

    // --- Domain operations ---

    String createDomain(String domainId, String name, String description) throws Exception;

    // --- Entity type operations ---

    String createEntityType(String entityTypeId, String domainId, String name, String description) throws Exception;

    // --- Permission type operations ---

    String createPermissionType(String permissionTypeId, String domainId, String name, String description)
            throws Exception;

    boolean isPermissionExists(String domainId, String permissionId) throws Exception;

    // --- Entity operations ---

    /**
     * Creates a sharing entity with the given attributes.
     *
     * @return the entity ID
     */
    String createEntity(
            String entityId,
            String domainId,
            String entityTypeId,
            String ownerId,
            String name,
            String description,
            String parentEntityId)
            throws Exception;

    /**
     * Updates the mutable metadata (name, description, parentEntityId) of an existing entity.
     */
    boolean updateEntityMetadata(
            String domainId, String entityId, String name, String description, String parentEntityId) throws Exception;

    boolean deleteEntity(String domainId, String entityId) throws Exception;

    // --- Access control ---

    boolean userHasAccess(String domainId, String userId, String entityId, String permissionTypeId) throws Exception;

    // --- Search ---

    /**
     * Searches entities and returns their IDs. Callers only need the entity IDs
     * from search results; this avoids exposing JPA entity types.
     */
    List<String> searchEntityIds(String domainId, String userId, List<SearchCriteria> filters, int offset, int limit)
            throws Exception;

    // --- Sharing ---

    boolean shareEntityWithGroups(
            String domainId,
            String entityId,
            List<String> groupList,
            String permissionTypeId,
            boolean cascadePermission)
            throws Exception;
}
