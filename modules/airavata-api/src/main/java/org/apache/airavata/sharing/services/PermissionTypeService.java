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

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
// SharingRegistryServerHandler moved to thrift-api module - update import if needed
import org.apache.airavata.sharing.entities.PermissionTypeEntity;
import org.apache.airavata.sharing.entities.PermissionTypePK;
import org.apache.airavata.sharing.mappers.PermissionTypeMapper;
import org.apache.airavata.sharing.model.PermissionType;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.repositories.PermissionTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PermissionTypeService {
    private static final Logger logger = LoggerFactory.getLogger(PermissionTypeService.class);
    private static final String OWNER_PERMISSION_NAME = "OWNER";

    private final PermissionTypeRepository permissionTypeRepository;
    private final PermissionTypeMapper permissionTypeMapper;
    private final EntityManager entityManager;

    public PermissionTypeService(
            PermissionTypeRepository permissionTypeRepository,
            PermissionTypeMapper permissionTypeMapper,
            EntityManager entityManager) {
        this.permissionTypeRepository = permissionTypeRepository;
        this.permissionTypeMapper = permissionTypeMapper;
        this.entityManager = entityManager;
    }

    public PermissionType get(PermissionTypePK pk) throws SharingRegistryException {
        PermissionTypeEntity entity = permissionTypeRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return permissionTypeMapper.toModel(entity);
    }

    public PermissionType create(PermissionType permissionType) throws SharingRegistryException {
        return update(permissionType);
    }

    public PermissionType update(PermissionType permissionType) throws SharingRegistryException {
        PermissionTypeEntity entity = permissionTypeMapper.toEntity(permissionType);
        PermissionTypeEntity saved = permissionTypeRepository.save(entity);
        return permissionTypeMapper.toModel(saved);
    }

    public boolean delete(PermissionTypePK pk) throws SharingRegistryException {
        permissionTypeRepository.deleteById(pk);
        return true;
    }

    public boolean isExists(PermissionTypePK pk) throws SharingRegistryException {
        return permissionTypeRepository.existsById(pk);
    }

    /**
     * Gets the OWNER permission type ID for a domain.
     * <p>
     * The OWNER permission must be created when the domain is created via
     * {@link org.apache.airavata.service.SharingRegistryService#createDomain(Domain)}.
     * If no OWNER permission exists, this indicates a data inconsistency that needs to be resolved.
     *
     * @param domainId The domain ID
     * @return The permission type ID for the OWNER permission
     * @throws SharingRegistryException If no OWNER permission exists or if there are multiple
     *                                  OWNER permissions (data inconsistency)
     */
    public String getOwnerPermissionTypeIdForDomain(String domainId) throws SharingRegistryException {
        List<PermissionTypeEntity> entities =
                permissionTypeRepository.findByDomainIdAndName(domainId, OWNER_PERMISSION_NAME);

        if (entities.isEmpty()) {
            String message = String.format(
                    "OWNER permission not found for domain '%s'. "
                            + "This indicates the domain was not created properly. "
                            + "Domains must be created via SharingRegistryService.createDomain() "
                            + "which automatically creates the OWNER permission.",
                    domainId);
            logger.error(message);
            throw new SharingRegistryException(message);
        }

        if (entities.size() > 1) {
            String message = String.format(
                    "Data inconsistency detected: Found %d OWNER permission records for domain '%s'. "
                            + "Expected exactly 1. This indicates duplicate permission creation.",
                    entities.size(), domainId);
            logger.error(message);
            throw new SharingRegistryException(message);
        }

        return entities.get(0).getPermissionTypeId();
    }

    public List<PermissionType> select(HashMap<String, String> filters, int offset, int limit)
            throws SharingRegistryException {
        // Use Criteria API for dynamic filtering
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(PermissionTypeEntity.class);
        var root = query.from(PermissionTypeEntity.class);

        var predicates = new ArrayList<Predicate>();
        if (filters != null) {
            for (var entry : filters.entrySet()) {
                predicates.add(cb.equal(root.get(entry.getKey()), entry.getValue()));
            }
        }
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        var typedQuery = entityManager.createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }

        var entities = typedQuery.getResultList();
        return permissionTypeMapper.toModelList(entities);
    }
}
