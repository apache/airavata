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
package org.apache.airavata.credential.store.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.credential.store.utils.CredentialStoreJPAUtils;
import org.apache.airavata.credential.store.utils.CredentialStoreJPAUtils.Committer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract repository class for credential store entities.
 * Provides common CRUD operations and query methods.
 */
public abstract class AbstractCredentialStoreRepository<T, E, Id> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractCredentialStoreRepository.class);

    private Class<T> thriftGenericClass;
    private Class<E> dbEntityGenericClass;

    public AbstractCredentialStoreRepository(Class<T> thriftGenericClass, Class<E> dbEntityGenericClass) {
        this.thriftGenericClass = thriftGenericClass;
        this.dbEntityGenericClass = dbEntityGenericClass;
    }

    /**
     * Create a new entity
     * @param t the entity to create
     * @return the created entity
     */
    public T create(T t) {
        return update(t);
    }

    /**
     * Update an existing entity
     * @param t the entity to update
     * @return the updated entity
     */
    public T update(T t) {
        E entity = mapToEntity(t);
        E persistedCopy = execute(entityManager -> entityManager.merge(entity));
        return mapFromEntity(persistedCopy);
    }

    /**
     * Delete an entity by ID
     * @param id the ID of the entity to delete
     * @return true if deletion was successful
     */
    public boolean delete(Id id) {
        execute(entityManager -> {
            E entity = entityManager.find(dbEntityGenericClass, id);
            if (entity != null) {
                entityManager.remove(entity);
            }
            return entity;
        });
        return true;
    }

    /**
     * Get an entity by ID
     * @param id the ID of the entity to retrieve
     * @return the entity or null if not found
     */
    public T get(Id id) {
        E entity = execute(entityManager -> entityManager.find(dbEntityGenericClass, id));
        return mapFromEntity(entity);
    }

    /**
     * Check if an entity exists by ID
     * @param id the ID to check
     * @return true if the entity exists
     */
    public boolean exists(Id id) {
        E entity = execute(entityManager -> entityManager.find(dbEntityGenericClass, id));
        return entity != null;
    }

    /**
     * Get all entities
     * @return list of all entities
     */
    public List<T> getAll() {
        String queryString = "SELECT e FROM " + dbEntityGenericClass.getSimpleName() + " e";
        List<E> resultSet = execute(entityManager -> {
            Query query = entityManager.createQuery(queryString);
            return query.getResultList();
        });
        return mapFromEntityList(resultSet);
    }

    /**
     * Select entities using a custom query
     * @param queryString the JPQL query string
     * @return list of entities matching the query
     */
    public List<T> select(String queryString) {
        List<E> resultSet = execute(entityManager -> {
            Query query = entityManager.createQuery(queryString);
            return query.getResultList();
        });
        return mapFromEntityList(resultSet);
    }

    /**
     * Select entities using a custom query with parameters
     * @param queryString the JPQL query string
     * @param parameters the query parameters
     * @return list of entities matching the query
     */
    public List<T> select(String queryString, Map<String, Object> parameters) {
        List<E> resultSet = execute(entityManager -> {
            Query query = entityManager.createQuery(queryString);
            for (Map.Entry<String, Object> param : parameters.entrySet()) {
                query.setParameter(param.getKey(), param.getValue());
            }
            return query.getResultList();
        });
        return mapFromEntityList(resultSet);
    }

    /**
     * Select entities using a custom query with limit and offset
     * @param queryString the JPQL query string
     * @param limit the maximum number of results
     * @param offset the offset for pagination
     * @return list of entities matching the query
     */
    public List<T> select(String queryString, int limit, int offset) {
        List<E> resultSet = execute(entityManager -> {
            Query query = entityManager.createQuery(queryString);
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            return query.getResultList();
        });
        return mapFromEntityList(resultSet);
    }

    /**
     * Execute a custom operation with EntityManager
     * @param committer the operation to execute
     * @param <R> the return type
     * @return the result of the operation
     */
    protected <R> R execute(Committer<EntityManager, R> committer) {
        return CredentialStoreJPAUtils.execute(committer);
    }

    /**
     * Map from Thrift object to entity
     * @param t the Thrift object
     * @return the entity
     */
    protected abstract E mapToEntity(T t);

    /**
     * Map from entity to Thrift object
     * @param entity the entity
     * @return the Thrift object
     */
    protected abstract T mapFromEntity(E entity);

    /**
     * Map from entity list to Thrift object list
     * @param entities the entity list
     * @return the Thrift object list
     */
    protected List<T> mapFromEntityList(List<E> entities) {
        List<T> result = new ArrayList<>();
        for (E entity : entities) {
            result.add(mapFromEntity(entity));
        }
        return result;
    }
}
