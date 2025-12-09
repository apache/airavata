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
package org.apache.airavata.sharing.services;

import com.github.dozermapper.core.Mapper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.airavata.sharing.entities.SharingEntity;
import org.apache.airavata.sharing.entities.SharingPK;
import org.apache.airavata.sharing.models.Sharing;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.models.SharingType;
import org.apache.airavata.sharing.repositories.SharingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SharingService {
    private final SharingRepository sharingRepository;
    private final PermissionTypeService permissionTypeService;
    private final Mapper mapper;

    public SharingService(
            SharingRepository sharingRepository, PermissionTypeService permissionTypeService, Mapper mapper) {
        this.sharingRepository = sharingRepository;
        this.permissionTypeService = permissionTypeService;
        this.mapper = mapper;
    }

    public Sharing get(SharingPK pk) throws SharingRegistryException {
        SharingEntity entity = sharingRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, Sharing.class);
    }

    public Sharing create(Sharing sharing) throws SharingRegistryException {
        return update(sharing);
    }

    public Sharing update(Sharing sharing) throws SharingRegistryException {
        SharingEntity entity = mapper.map(sharing, SharingEntity.class);
        SharingEntity saved = sharingRepository.save(entity);
        return mapper.map(saved, Sharing.class);
    }

    public boolean delete(SharingPK pk) throws SharingRegistryException {
        sharingRepository.deleteById(pk);
        return true;
    }

    public boolean isExists(SharingPK pk) throws SharingRegistryException {
        return sharingRepository.existsById(pk);
    }

    public List<Sharing> select(Map<String, String> filters, int offset, int limit) throws SharingRegistryException {
        // For complex filters, use Criteria API in service if needed
        // For now, return all if no filters
        List<SharingEntity> entities = sharingRepository.findAll();
        return entities.stream().map(e -> mapper.map(e, Sharing.class)).toList();
    }

    public List<Sharing> getIndirectSharedChildren(String domainId, String parentId, String permissionTypeId)
            throws SharingRegistryException {
        List<SharingEntity> entities = sharingRepository.findIndirectSharedChildren(
                domainId, parentId, SharingType.INDIRECT_CASCADING.toString(), permissionTypeId);
        return entities.stream().map(e -> mapper.map(e, Sharing.class)).toList();
    }

    public List<Sharing> getCascadingPermissionsForEntity(String domainId, String entityId)
            throws SharingRegistryException {
        List<String> sharingTypes =
                Arrays.asList(SharingType.DIRECT_CASCADING.toString(), SharingType.INDIRECT_CASCADING.toString());
        List<SharingEntity> entities =
                sharingRepository.findCascadingPermissionsForEntity(domainId, entityId, sharingTypes);
        return entities.stream().map(e -> mapper.map(e, Sharing.class)).toList();
    }

    public boolean hasAccess(String domainId, String entityId, List<String> groupIds, List<String> permissionTypeIds)
            throws SharingRegistryException {
        return sharingRepository.hasAccess(domainId, entityId, permissionTypeIds, groupIds);
    }

    public int getSharedCount(String domainId, String entityId) throws SharingRegistryException {
        String ownerPermissionTypeId = permissionTypeService.getOwnerPermissionTypeIdForDomain(domainId);
        return sharingRepository.getSharedCount(
                domainId, entityId, ownerPermissionTypeId, SharingType.INDIRECT_CASCADING.toString());
    }

    public void removeAllIndirectCascadingPermissionsForEntity(String domainId, String entityId)
            throws SharingRegistryException {
        sharingRepository.removeAllIndirectCascadingPermissionsForEntity(
                domainId, entityId, SharingType.INDIRECT_CASCADING.toString());
    }
}
