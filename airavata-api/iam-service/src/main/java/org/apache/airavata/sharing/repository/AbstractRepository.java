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
package org.apache.airavata.sharing.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.db.Committer;
import org.apache.airavata.db.EntityManagerFactoryHolder;
import org.apache.airavata.sharing.model.SharingRegistryException;
import org.apache.airavata.sharing.util.DBConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRepository<E, Id> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    private Class<E> entityClass;

    public AbstractRepository(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    public E create(E entity) throws SharingRegistryException {
        return update(entity);
    }

    public List<E> create(List<E> entities) throws SharingRegistryException {
        return update(entities);
    }

    public E update(E entity) throws SharingRegistryException {
        E persistedCopy = execute(entityManager -> entityManager.merge(entity));
        return persistedCopy;
    }

    public List<E> update(List<E> entities) throws SharingRegistryException {
        List<E> returnList = new ArrayList<>();
        for (E entity : entities) returnList.add(update(entity));
        return returnList;
    }

    public boolean delete(Id id) throws SharingRegistryException {
        execute(entityManager -> {
            E entity = entityManager.find(entityClass, id);
            entityManager.remove(entity);
            return entity;
        });
        return true;
    }

    public boolean delete(List<Id> idList) throws SharingRegistryException {
        for (Id id : idList) delete(id);
        return true;
    }

    public E get(Id id) throws SharingRegistryException {
        EntityManager testEM = EntityManagerFactoryHolder.getTestEntityManager();
        if (testEM != null && testEM.isOpen()) {
            testEM.clear();
            return testEM.find(entityClass, id);
        }
        return execute(entityManager -> entityManager.find(entityClass, id));
    }

    public boolean isExists(Id id) throws SharingRegistryException {
        return get(id) != null;
    }

    public List<E> get(List<Id> idList) throws SharingRegistryException {
        List<E> returnList = new ArrayList<>();
        for (Id id : idList) returnList.add(get(id));
        return returnList;
    }

    @SuppressWarnings("unchecked")
    public List<E> select(Map<String, String> filters, int offset, int limit) throws SharingRegistryException {
        String query = "SELECT DISTINCT p from " + getEntityName() + " as p";
        ArrayList<String> parameters = new ArrayList<>();
        int parameterCount = 1;
        if (filters != null && filters.size() != 0) {
            query += " WHERE ";
            for (String k : filters.keySet()) {
                query += "p." + k + " = ?" + parameterCount + " AND ";
                parameters.add(filters.get(k));
                parameterCount++;
            }
            query = query.substring(0, query.length() - 5);
        }

        query += " ORDER BY p.createdTime DESC";
        String queryString = query;
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS : limit;
        List resultSet = execute(entityManager -> {
            jakarta.persistence.Query q = entityManager.createQuery(queryString);
            for (int i = 0; i < parameters.size(); i++) {
                q.setParameter(i + 1, parameters.get(i));
            }
            return q.setFirstResult(offset).setMaxResults(newLimit).getResultList();
        });
        return new ArrayList<>(resultSet);
    }

    @SuppressWarnings("unchecked")
    public List<E> select(String queryString, Map<String, Object> queryParameters, int offset, int limit)
            throws SharingRegistryException {
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS : limit;
        List resultSet = execute(entityManager -> {
            Query q = entityManager.createQuery(queryString);
            for (Map.Entry<String, Object> queryParam : queryParameters.entrySet()) {
                q.setParameter(queryParam.getKey(), queryParam.getValue());
            }
            return q.setFirstResult(offset).setMaxResults(newLimit).getResultList();
        });
        return new ArrayList<>(resultSet);
    }

    public <R> R execute(Committer<EntityManager, R> committer) throws SharingRegistryException {
        EntityManager testEM = EntityManagerFactoryHolder.getTestEntityManager();
        if (testEM != null && testEM.isOpen()) {
            try {
                R r = committer.commit(testEM);
                testEM.flush();
                return r;
            } catch (Exception e) {
                throw new SharingRegistryException("Failed to execute in test transaction: " + e.getMessage());
            }
        }

        EntityManager entityManager = EntityManagerFactoryHolder.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            R r = committer.commit(entityManager);
            entityManager.getTransaction().commit();
            return r;
        } finally {
            if (entityManager.isOpen()) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                entityManager.close();
            }
        }
    }

    /**
     * Returns the JPA entity name, respecting any explicit {@code @Entity(name = "...")} annotation.
     * Falls back to the simple class name when no explicit name is set.
     */
    private String getEntityName() {
        Entity annotation = entityClass.getAnnotation(Entity.class);
        if (annotation != null && !annotation.name().isEmpty()) {
            return annotation.name();
        }
        return entityClass.getSimpleName();
    }
}
