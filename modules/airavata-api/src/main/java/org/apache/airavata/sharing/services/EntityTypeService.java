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
import org.apache.airavata.sharing.entities.EntityTypeEntity;
import org.apache.airavata.sharing.entities.EntityTypePK;
import org.apache.airavata.sharing.mappers.EntityTypeMapper;
import org.apache.airavata.sharing.model.EntityType;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.repositories.EntityTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EntityTypeService {
    private final EntityTypeRepository entityTypeRepository;
    private final EntityTypeMapper entityTypeMapper;
    private final EntityManager entityManager;

    public EntityTypeService(
            EntityTypeRepository entityTypeRepository, EntityTypeMapper entityTypeMapper, EntityManager entityManager) {
        this.entityTypeRepository = entityTypeRepository;
        this.entityTypeMapper = entityTypeMapper;
        this.entityManager = entityManager;
    }

    public EntityType get(EntityTypePK pk) throws SharingRegistryException {
        EntityTypeEntity entity = entityTypeRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return entityTypeMapper.toModel(entity);
    }

    public EntityType create(EntityType entityType) throws SharingRegistryException {
        return update(entityType);
    }

    public EntityType update(EntityType entityType) throws SharingRegistryException {
        EntityTypeEntity entity = entityTypeMapper.toEntity(entityType);
        EntityTypeEntity saved = entityTypeRepository.save(entity);
        return entityTypeMapper.toModel(saved);
    }

    public boolean delete(EntityTypePK pk) throws SharingRegistryException {
        entityTypeRepository.deleteById(pk);
        return true;
    }

    public boolean isExists(EntityTypePK pk) throws SharingRegistryException {
        return entityTypeRepository.existsById(pk);
    }

    public List<EntityType> select(HashMap<String, String> filters, int offset, int limit)
            throws SharingRegistryException {
        // Use Criteria API for dynamic filtering
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(EntityTypeEntity.class);
        var root = query.from(EntityTypeEntity.class);

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
        return entityTypeMapper.toModelList(entities);
    }
}
