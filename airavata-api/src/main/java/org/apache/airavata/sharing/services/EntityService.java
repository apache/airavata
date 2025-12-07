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
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.sharing.entities.EntityEntity;
import org.apache.airavata.sharing.entities.EntityPK;
import org.apache.airavata.sharing.entities.SharingEntity;
import org.apache.airavata.sharing.models.Entity;
import org.apache.airavata.sharing.models.EntitySearchField;
import org.apache.airavata.sharing.models.SearchCondition;
import org.apache.airavata.sharing.models.SearchCriteria;
import org.apache.airavata.sharing.models.SharingRegistryException;
import org.apache.airavata.sharing.repositories.EntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EntityService {
    private static final Logger logger = LoggerFactory.getLogger(EntityService.class);

    @Autowired
    private EntityRepository entityRepository;

    @Autowired
    private Mapper mapper;

    @PersistenceContext(unitName = "airavata-sharing-registry")
    private EntityManager entityManager;

    public Entity get(EntityPK pk) throws SharingRegistryException {
        EntityEntity entity = entityRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return mapper.map(entity, Entity.class);
    }

    public Entity create(Entity entity) throws SharingRegistryException {
        return update(entity);
    }

    public Entity update(Entity entity) throws SharingRegistryException {
        EntityEntity entityEntity = mapper.map(entity, EntityEntity.class);
        EntityEntity saved = entityRepository.save(entityEntity);
        return mapper.map(saved, Entity.class);
    }

    public boolean delete(EntityPK pk) throws SharingRegistryException {
        entityRepository.deleteById(pk);
        return true;
    }

    public boolean isExists(EntityPK pk) throws SharingRegistryException {
        return entityRepository.existsById(pk);
    }

    public List<Entity> getChildEntities(String domainId, String parentId) throws SharingRegistryException {
        List<EntityEntity> entities =
                entityRepository.findByDomainIdAndParentEntityIdOrderByOriginalEntityCreationTimeDesc(
                        domainId, parentId);
        return entities.stream().map(e -> mapper.map(e, Entity.class)).toList();
    }

    public List<Entity> searchEntities(
            String domainId, List<String> groupIds, List<SearchCriteria> filters, int offset, int limit)
            throws SharingRegistryException {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<EntityEntity> query = cb.createQuery(EntityEntity.class);
            Root<EntityEntity> entityRoot = query.from(EntityEntity.class);
            Root<SharingEntity> sharingRoot = query.from(SharingEntity.class);

            List<Predicate> predicates = new ArrayList<>();

            // Domain filter
            predicates.add(cb.equal(entityRoot.get("domainId"), domainId));
            predicates.add(cb.equal(sharingRoot.get("domainId"), domainId));

            // Join condition: entity.entityId = sharing.entityId AND entity.domainId = sharing.domainId
            predicates.add(cb.equal(entityRoot.get("entityId"), sharingRoot.get("entityId")));
            predicates.add(cb.equal(entityRoot.get("domainId"), sharingRoot.get("domainId")));

            // Group access filter - user must have access through one of the groups
            predicates.add(sharingRoot.get("groupId").in(groupIds));

            // Apply search criteria filters
            if (filters != null && !filters.isEmpty()) {
                for (SearchCriteria criteria : filters) {
                    if (criteria.getSearchField() == null
                            || criteria.getValue() == null
                            || criteria.getSearchCondition() == null) {
                        continue;
                    }

                    Predicate filterPredicate = buildPredicate(cb, entityRoot, criteria);
                    if (filterPredicate != null) {
                        predicates.add(filterPredicate);
                    }
                }
            }

            query.where(cb.and(predicates.toArray(new Predicate[0])));
            query.distinct(true);
            query.orderBy(cb.desc(entityRoot.get("originalEntityCreationTime")));

            TypedQuery<EntityEntity> typedQuery = entityManager.createQuery(query);
            if (offset > 0) {
                typedQuery.setFirstResult(offset);
            }
            if (limit > 0) {
                typedQuery.setMaxResults(limit);
            }

            List<EntityEntity> entities = typedQuery.getResultList();
            List<Entity> result = new ArrayList<>();
            for (EntityEntity entity : entities) {
                result.add(mapper.map(entity, Entity.class));
            }
            return result;
        } catch (Exception e) {
            logger.error("Error searching entities", e);
            SharingRegistryException ex = new SharingRegistryException("Error searching entities: " + e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    private Predicate buildPredicate(CriteriaBuilder cb, Root<EntityEntity> root, SearchCriteria criteria) {
        EntitySearchField field = criteria.getSearchField();
        String value = criteria.getValue();
        SearchCondition condition = criteria.getSearchCondition();

        if (field == null || value == null || condition == null) {
            return null;
        }

        Path<?> fieldPath = getFieldPath(root, field);
        if (fieldPath == null) {
            return null;
        }

        switch (condition) {
            case EQUAL:
                return cb.equal(fieldPath, value);
            case LIKE:
                return cb.like(cb.lower(fieldPath.as(String.class)), "%" + value.toLowerCase() + "%");
            case FULL_TEXT:
                // Full text search on fullText field
                if (field == EntitySearchField.FULL_TEXT) {
                    return cb.like(cb.lower(root.get("fullText").as(String.class)), "%" + value.toLowerCase() + "%");
                }
                return null;
            case GTE:
                if (fieldPath.getJavaType() == Long.class || fieldPath.getJavaType() == long.class) {
                    try {
                        Long longValue = Long.parseLong(value);
                        return cb.greaterThanOrEqualTo(fieldPath.as(Long.class), longValue);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid number format for GTE condition: " + value);
                        return null;
                    }
                }
                return null;
            case LTE:
                if (fieldPath.getJavaType() == Long.class || fieldPath.getJavaType() == long.class) {
                    try {
                        Long longValue = Long.parseLong(value);
                        return cb.lessThanOrEqualTo(fieldPath.as(Long.class), longValue);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid number format for LTE condition: " + value);
                        return null;
                    }
                }
                return null;
            case NOT:
                return cb.notEqual(fieldPath, value);
            default:
                return null;
        }
    }

    private Path<?> getFieldPath(Root<EntityEntity> root, EntitySearchField field) {
        switch (field) {
            case NAME:
                return root.get("name");
            case DESCRIPTION:
                return root.get("description");
            case FULL_TEXT:
                return root.get("fullText");
            case PARRENT_ENTITY_ID:
                return root.get("parentEntityId");
            case OWNER_ID:
                return root.get("ownerId");
            case PERMISSION_TYPE_ID:
                // This needs to be handled via join with SharingEntity
                return null; // Will be handled separately
            case CREATED_TIME:
                return root.get("originalEntityCreationTime");
            case UPDATED_TIME:
                return root.get("updatedTime");
            case ENTITY_TYPE_ID:
                return root.get("entityTypeId");
            case SHARED_COUNT:
                // This requires a subquery to count sharings
                return null; // Will be handled separately
            default:
                return null;
        }
    }
}
