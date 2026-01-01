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
package org.apache.airavata.common.repositories;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Common base class for repository implementations across Airavata modules.
 *
 * <p>This class provides core CRUD operations that are common across all repository
 * implementations. Module-specific repositories should extend this class and add
 * module-specific query methods and exception handling.
 *
 * <p>The class uses a generic type system:
 * <ul>
 *   <li>T - The Thrift model type (e.g., ExperimentModel, ProcessModel)</li>
 *   <li>E - The JPA entity type (e.g., ExperimentEntity, ProcessEntity)</li>
 *   <li>Id - The primary key type (e.g., String, ProcessPK)</li>
 * </ul>
 *
 * <p>Subclasses must implement:
 * <ul>
 *   <li>{@link #mapToEntity(T)} - Maps a Thrift model to a JPA entity using MapStruct</li>
 *   <li>{@link #mapToModel(E)} - Maps a JPA entity to a Thrift model using MapStruct</li>
 *   <li>{@link #getEntityManager()} - Returns the EntityManager for this persistence unit</li>
 * </ul>
 *
 * @param <T> The Thrift model type
 * @param <E> The JPA entity type
 * @param <Id> The primary key type
 */
public abstract class AbstractRepository<T, E, Id> {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    protected final Class<T> thriftGenericClass;
    protected final Class<E> dbEntityGenericClass;

    /**
     * Constructor for the abstract repository.
     *
     * @param thriftGenericClass The Thrift model class
     * @param dbEntityGenericClass The JPA entity class
     */
    protected AbstractRepository(Class<T> thriftGenericClass, Class<E> dbEntityGenericClass) {
        this.thriftGenericClass = thriftGenericClass;
        this.dbEntityGenericClass = dbEntityGenericClass;
    }

    /**
     * Map a Thrift model to a JPA entity using MapStruct mapper.
     *
     * @param model The Thrift model
     * @return The JPA entity
     */
    protected abstract E mapToEntity(T model);

    /**
     * Map a JPA entity to a Thrift model using MapStruct mapper.
     *
     * @param entity The JPA entity
     * @return The Thrift model
     */
    protected abstract T mapToModel(E entity);

    /**
     * Get the EntityManager for this persistence unit.
     * Each module should provide its own EntityManager implementation.
     *
     * @return The EntityManager instance
     */
    protected abstract EntityManager getEntityManager();

    /**
     * Create a new entity. This is equivalent to update() for most implementations.
     *
     * @param t The Thrift model to persist
     * @return The persisted Thrift model
     */
    @Transactional
    public T create(T t) {
        return update(t);
    }

    /**
     * Update or create an entity.
     *
     * @param t The Thrift model to persist
     * @return The persisted Thrift model
     */
    @Transactional
    public T update(T t) {
        E entity = mapToEntity(t);
        return mergeEntity(entity);
    }

    /**
     * Merge an entity into the persistence context and map back to Thrift model.
     *
     * @param entity The JPA entity
     * @return The persisted Thrift model
     */
    protected T mergeEntity(E entity) {
        EntityManager em = getEntityManager();
        E persistedCopy = em.merge(entity);
        return mapToModel(persistedCopy);
    }

    /**
     * Delete an entity by its primary key.
     *
     * @param id The primary key
     * @return true if the entity was deleted, false if it didn't exist
     */
    @Transactional
    public boolean delete(Id id) {
        EntityManager em = getEntityManager();
        E entity = em.find(dbEntityGenericClass, id);
        if (entity != null) {
            em.remove(entity);
            return true;
        }
        return false;
    }

    /**
     * Retrieve an entity by its primary key.
     *
     * @param id The primary key
     * @return The Thrift model, or null if not found
     */
    @Transactional(readOnly = true)
    public T get(Id id) {
        EntityManager em = getEntityManager();
        E entity = em.find(dbEntityGenericClass, id);
        if (entity == null) {
            return null;
        }
        return mapToModel(entity);
    }

    /**
     * Check if an entity exists by its primary key.
     *
     * @param id The primary key
     * @return true if the entity exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isExists(Id id) {
        return get(id) != null;
    }
}
