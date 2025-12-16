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
package org.apache.airavata.sharing.repositories;

import com.github.dozermapper.core.Mapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.utils.Committer;
import org.apache.airavata.sharing.utils.DBConstants;
import org.apache.airavata.sharing.utils.JPAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractRepository<T, E, Id> {

    private EntityManager entityManager;

    protected EntityManager getEntityManager() {
        if (entityManager == null) {
            EntityManagerFactory factory = JPAUtils.getEntityManagerFactory();
            entityManager = SharedEntityManagerCreator.createSharedEntityManager(factory);
        }
        return entityManager;
    }

    private static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    private Class<T> thriftGenericClass;
    private Class<E> dbEntityGenericClass;

    public AbstractRepository(Class<T> thriftGenericClass, Class<E> dbEntityGenericClass) {
        this.thriftGenericClass = thriftGenericClass;
        this.dbEntityGenericClass = dbEntityGenericClass;
    }

    protected abstract Mapper getMapper();

    @Transactional
    public T create(T t) throws SharingRegistryException {
        return update(t);
    }

    /**
     * Bulk create entities. Uses batch processing for better performance.
     *
     * @param tList List of Thrift models to persist
     * @return List of persisted Thrift models
     * @throws SharingRegistryException if the operation fails
     */
    @Transactional
    public List<T> create(List<T> tList) throws SharingRegistryException {
        if (tList == null || tList.isEmpty()) {
            return new ArrayList<>();
        }
        EntityManager em = getEntityManager();
        List<T> result = new ArrayList<>();
        int batchSize = 50; // Process in batches to avoid memory issues

        for (int i = 0; i < tList.size(); i++) {
            E entity = getMapper().map(tList.get(i), dbEntityGenericClass);
            em.persist(entity);
            result.add(getMapper().map(entity, thriftGenericClass));

            // Flush and clear every batchSize entities to manage memory
            if ((i + 1) % batchSize == 0) {
                em.flush();
                em.clear();
            }
        }

        // Final flush for remaining entities
        if (tList.size() % batchSize != 0) {
            em.flush();
        }

        return result;
    }

    @Transactional
    public T update(T t) throws SharingRegistryException {
        E entity = getMapper().map(t, dbEntityGenericClass);
        EntityManager em = getEntityManager();
        E persistedCopy = em.merge(entity);
        return getMapper().map(persistedCopy, thriftGenericClass);
    }

    /**
     * Bulk update entities. Uses batch processing for better performance.
     *
     * @param tList List of Thrift models to update
     * @return List of updated Thrift models
     * @throws SharingRegistryException if the operation fails
     */
    @Transactional
    public List<T> update(List<T> tList) throws SharingRegistryException {
        if (tList == null || tList.isEmpty()) {
            return new ArrayList<>();
        }
        EntityManager em = getEntityManager();
        List<T> result = new ArrayList<>();
        int batchSize = 50; // Process in batches to avoid memory issues

        for (int i = 0; i < tList.size(); i++) {
            E entity = getMapper().map(tList.get(i), dbEntityGenericClass);
            E merged = em.merge(entity);
            result.add(getMapper().map(merged, thriftGenericClass));

            // Flush and clear every batchSize entities to manage memory
            if ((i + 1) % batchSize == 0) {
                em.flush();
                em.clear();
            }
        }

        // Final flush for remaining entities
        if (tList.size() % batchSize != 0) {
            em.flush();
        }

        return result;
    }

    @Transactional
    public boolean delete(Id id) throws SharingRegistryException {
        EntityManager em = getEntityManager();
        E entity = em.find(dbEntityGenericClass, id);
        if (entity != null) {
            em.remove(entity);
        }
        return true;
    }

    @Transactional
    public boolean delete(List<Id> idList) throws SharingRegistryException {
        for (Id id : idList) delete(id);
        return true;
    }

    @Transactional(readOnly = true)
    public T get(Id id) {
        EntityManager em = getEntityManager();
        E entity = em.find(dbEntityGenericClass, id);
        if (entity == null) return null;
        return getMapper().map(entity, thriftGenericClass);
    }

    public boolean isExists(Id id) {
        return get(id) != null;
    }

    public List<T> get(List<Id> idList) {
        return idList.stream().map(this::get).toList();
    }

    @Transactional(readOnly = true)
    public List<T> select(Map<String, String> filters, int offset, int limit) throws SharingRegistryException {
        var query = "SELECT DISTINCT p from " + dbEntityGenericClass.getSimpleName() + " as p";
        var parameters = new ArrayList<String>();
        var parameterCount = 1;
        if (filters != null && !filters.isEmpty()) {
            query += " WHERE ";
            for (var entry : filters.entrySet()) {
                query += "p." + entry.getKey() + " = ?" + parameterCount + " AND ";
                parameters.add(entry.getValue());
                parameterCount++;
            }
            query = query.substring(0, query.length() - 5);
        }

        query += " ORDER BY p.createdTime DESC";
        var newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS : limit;
        var em = getEntityManager();
        var q = em.createQuery(query);
        for (var i = 0; i < parameters.size(); i++) {
            q.setParameter(i + 1, parameters.get(i));
        }
        var resultSet = q.setFirstResult(offset).setMaxResults(newLimit).getResultList();
        return resultSet.stream()
                .map(rs -> getMapper().map(rs, thriftGenericClass))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<T> select(String queryString, Map<String, Object> queryParameters, int offset, int limit)
            throws SharingRegistryException {
        var newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS : limit;
        var em = getEntityManager();
        var q = em.createQuery(queryString);
        for (var queryParam : queryParameters.entrySet()) {
            q.setParameter(queryParam.getKey(), queryParam.getValue());
        }
        var resultSet = q.setFirstResult(offset).setMaxResults(newLimit).getResultList();
        return resultSet.stream()
                .map(rs -> getMapper().map(rs, thriftGenericClass))
                .toList();
    }

    @Transactional
    public <R> R execute(Committer<EntityManager, R> committer) throws SharingRegistryException {
        EntityManager em = getEntityManager();
        return committer.commit(em);
    }
}
