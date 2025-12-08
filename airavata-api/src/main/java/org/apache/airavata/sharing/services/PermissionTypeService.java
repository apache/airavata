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
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.airavata.api.thrift.handler.SharingRegistryServerHandler;
import org.apache.airavata.sharing.entities.PermissionTypeEntity;
import org.apache.airavata.sharing.entities.PermissionTypePK;
import org.apache.airavata.sharing.models.PermissionType;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.repositories.PermissionTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PermissionTypeService {
    @Autowired
    private PermissionTypeRepository permissionTypeRepository;

    @Autowired
    private Mapper mapper;

    @Autowired
    @Qualifier("sharingRegistryEntityManager")
    private EntityManager entityManager;

    public PermissionType get(PermissionTypePK pk) throws SharingRegistryException {
        PermissionTypeEntity entity = permissionTypeRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, PermissionType.class);
    }

    public PermissionType create(PermissionType permissionType) throws SharingRegistryException {
        return update(permissionType);
    }

    public PermissionType update(PermissionType permissionType) throws SharingRegistryException {
        PermissionTypeEntity entity = mapper.map(permissionType, PermissionTypeEntity.class);
        PermissionTypeEntity saved = permissionTypeRepository.save(entity);
        return mapper.map(saved, PermissionType.class);
    }

    public boolean delete(PermissionTypePK pk) throws SharingRegistryException {
        permissionTypeRepository.deleteById(pk);
        return true;
    }

    public boolean isExists(PermissionTypePK pk) throws SharingRegistryException {
        return permissionTypeRepository.existsById(pk);
    }

    public String getOwnerPermissionTypeIdForDomain(String domainId) throws SharingRegistryException {
        List<PermissionTypeEntity> entities = permissionTypeRepository.findByDomainIdAndName(
                domainId, SharingRegistryServerHandler.OWNER_PERMISSION_NAME);
        if (entities.size() != 1) {
            throw new SharingRegistryException("GLOBAL Permission inconsistency. Found " + entities.size()
                    + " records with " + SharingRegistryServerHandler.OWNER_PERMISSION_NAME + " name");
        }
        return entities.get(0).getPermissionTypeId();
    }

    public List<PermissionType> select(HashMap<String, String> filters, int offset, int limit)
            throws SharingRegistryException {
        // Use Criteria API for dynamic filtering
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PermissionTypeEntity> query = cb.createQuery(PermissionTypeEntity.class);
        Root<PermissionTypeEntity> root = query.from(PermissionTypeEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        if (filters != null) {
            for (String key : filters.keySet()) {
                String value = filters.get(key);
                predicates.add(cb.equal(root.get(key), value));
            }
        }
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<PermissionTypeEntity> typedQuery = entityManager.createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }

        List<PermissionTypeEntity> entities = typedQuery.getResultList();
        return entities.stream().map(e -> mapper.map(e, PermissionType.class)).toList();
    }
}
