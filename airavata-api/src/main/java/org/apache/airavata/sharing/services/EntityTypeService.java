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
import org.apache.airavata.sharing.entities.EntityTypeEntity;
import org.apache.airavata.sharing.entities.EntityTypePK;
import org.apache.airavata.sharing.models.EntityType;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.repositories.EntityTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EntityTypeService {
    @Autowired
    private EntityTypeRepository entityTypeRepository;

    @Autowired
    private Mapper mapper;

    @Autowired
    @Qualifier("sharingRegistryEntityManager")
    private EntityManager entityManager;

    public EntityType get(EntityTypePK pk) throws SharingRegistryException {
        EntityTypeEntity entity = entityTypeRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, EntityType.class);
    }

    public EntityType create(EntityType entityType) throws SharingRegistryException {
        return update(entityType);
    }

    public EntityType update(EntityType entityType) throws SharingRegistryException {
        EntityTypeEntity entity = mapper.map(entityType, EntityTypeEntity.class);
        EntityTypeEntity saved = entityTypeRepository.save(entity);
        return mapper.map(saved, EntityType.class);
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<EntityTypeEntity> query = cb.createQuery(EntityTypeEntity.class);
        Root<EntityTypeEntity> root = query.from(EntityTypeEntity.class);

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

        TypedQuery<EntityTypeEntity> typedQuery = entityManager.createQuery(query);
        if (offset > 0) {
            typedQuery.setFirstResult(offset);
        }
        if (limit > 0) {
            typedQuery.setMaxResults(limit);
        }

        List<EntityTypeEntity> entities = typedQuery.getResultList();
        return entities.stream().map(e -> mapper.map(e, EntityType.class)).toList();
    }
}
