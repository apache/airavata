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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.sharing.entities.EntityEntity;
import org.apache.airavata.sharing.entities.EntityPK;
import org.apache.airavata.sharing.entities.SharingEntity;
import org.apache.airavata.sharing.mappers.EntityMapper;
import org.apache.airavata.sharing.model.Entity;
import org.apache.airavata.sharing.model.EntitySearchField;
import org.apache.airavata.sharing.model.SearchCondition;
import org.apache.airavata.sharing.model.SearchCriteria;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.repositories.EntityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EntityService {
    private static final Logger logger = LoggerFactory.getLogger(EntityService.class);

    private final EntityRepository entityRepository;
    private final EntityMapper entityMapper;
    private final EntityManager entityManager;

    public EntityService(EntityRepository entityRepository, EntityMapper entityMapper, EntityManager entityManager) {
        this.entityRepository = entityRepository;
        this.entityMapper = entityMapper;
        this.entityManager = entityManager;
    }

    public Entity get(EntityPK pk) throws SharingRegistryException {
        EntityEntity entity = entityRepository.findById(pk).orElse(null);
        if (entity == null) return null;
        return entityMapper.toModel(entity);
    }

    public Entity create(Entity entity) throws SharingRegistryException {
        return update(entity);
    }

    public Entity update(Entity entity) throws SharingRegistryException {
        EntityEntity entityEntity = entityMapper.toEntity(entity);
        EntityEntity saved = entityRepository.save(entityEntity);
        return entityMapper.toModel(saved);
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
        return entityMapper.toModelList(entities);
    }

    public List<Entity> searchEntities(
            String domainId, List<String> groupIds, List<SearchCriteria> filters, int offset, int limit)
            throws SharingRegistryException {
        try {
            var cb = entityManager.getCriteriaBuilder();
            var query = cb.createQuery(EntityEntity.class);
            var entityRoot = query.from(EntityEntity.class);

            var predicates = new ArrayList<Predicate>();

            // Domain filter
            predicates.add(cb.equal(entityRoot.get("domainId"), domainId));

            // Extract PERMISSION_TYPE_ID filter for use in subquery (it's on SharingEntity, not EntityEntity)
            String permissionTypeFilter = null;
            if (filters != null) {
                for (var criteria : filters) {
                    if (criteria.getSearchField() == EntitySearchField.PERMISSION_TYPE_ID
                            && criteria.getValue() != null) {
                        permissionTypeFilter = criteria.getValue();
                        break;
                    }
                }
            }

            // Use subquery to check for sharing access (Hibernate 6 doesn't allow multiple roots)
            var sharingSubquery = query.subquery(String.class);
            var sharingRoot = sharingSubquery.from(SharingEntity.class);
            sharingSubquery.select(sharingRoot.get("entityId"));

            // Build subquery conditions
            var sharingConditions = new ArrayList<Predicate>();
            sharingConditions.add(cb.equal(sharingRoot.get("domainId"), domainId));
            sharingConditions.add(cb.equal(sharingRoot.get("entityId"), entityRoot.get("entityId")));
            sharingConditions.add(sharingRoot.get("groupId").in(groupIds));

            // Add permission type filter to subquery if specified
            if (permissionTypeFilter != null) {
                sharingConditions.add(cb.equal(sharingRoot.get("permissionTypeId"), permissionTypeFilter));
            }

            sharingSubquery.where(cb.and(sharingConditions.toArray(new Predicate[0])));

            // Entity must have a sharing record for one of the user's groups
            predicates.add(cb.exists(sharingSubquery));

            // Apply search criteria filters (excluding PERMISSION_TYPE_ID which is handled above)
            if (filters != null && !filters.isEmpty()) {
                for (var criteria : filters) {
                    if (criteria.getSearchField() == null
                            || criteria.getValue() == null
                            || criteria.getSearchCondition() == null) {
                        continue;
                    }

                    // Skip PERMISSION_TYPE_ID as it's handled in the subquery
                    if (criteria.getSearchField() == EntitySearchField.PERMISSION_TYPE_ID) {
                        continue;
                    }

                    var filterPredicate = buildPredicate(cb, entityRoot, criteria);
                    if (filterPredicate != null) {
                        predicates.add(filterPredicate);
                    }
                }
            }

            query.where(cb.and(predicates.toArray(new Predicate[0])));
            query.distinct(true);
            query.orderBy(cb.desc(entityRoot.get("originalEntityCreationTime")));

            var typedQuery = entityManager.createQuery(query);
            if (offset > 0) {
                typedQuery.setFirstResult(offset);
            }
            if (limit > 0) {
                typedQuery.setMaxResults(limit);
            }

            var entities = typedQuery.getResultList();
            return entities.stream().map(entity -> entityMapper.toModel(entity)).toList();
        } catch (Exception e) {
            logger.error("Error searching entities", e);
            throw new SharingRegistryException(String.format("Error searching entities: %s", e.getMessage()), e);
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

        return switch (condition) {
            case EQUAL -> cb.equal(fieldPath, value);
            case LIKE -> cb.like(cb.lower(fieldPath.as(String.class)), "%" + value.toLowerCase() + "%");
            case FULL_TEXT -> {
                // Full text search on fullText field
                if (field == EntitySearchField.FULL_TEXT) {
                    yield cb.like(cb.lower(root.get("fullText").as(String.class)), "%" + value.toLowerCase() + "%");
                }
                yield null;
            }
            case GTE -> {
                if (fieldPath.getJavaType() == Long.class || fieldPath.getJavaType() == long.class) {
                    try {
                        var longValue = Long.parseLong(value);
                        yield cb.greaterThanOrEqualTo(fieldPath.as(Long.class), longValue);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid number format for GTE condition: " + value);
                        yield null;
                    }
                }
                yield null;
            }
            case LTE -> {
                if (fieldPath.getJavaType() == Long.class || fieldPath.getJavaType() == long.class) {
                    try {
                        var longValue = Long.parseLong(value);
                        yield cb.lessThanOrEqualTo(fieldPath.as(Long.class), longValue);
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid number format for LTE condition: " + value);
                        yield null;
                    }
                }
                yield null;
            }
            case NOT -> cb.notEqual(fieldPath, value);
            default -> null;
        };
    }

    private Path<?> getFieldPath(Root<EntityEntity> root, EntitySearchField field) {
        return switch (field) {
            case NAME -> root.get("name");
            case DESCRIPTION -> root.get("description");
            case FULL_TEXT -> root.get("fullText");
            case PARRENT_ENTITY_ID -> root.get("parentEntityId");
            case OWNER_ID -> root.get("ownerId");
            case PERMISSION_TYPE_ID -> null; // Will be handled separately via join with SharingEntity
            case CREATED_TIME -> root.get("originalEntityCreationTime");
            case UPDATED_TIME -> root.get("updatedTime");
            case ENTITY_TYPE_ID -> root.get("entityTypeId");
            case SHARED_COUNT -> root.get("sharedCount");
            default -> null;
        };
    }
}
