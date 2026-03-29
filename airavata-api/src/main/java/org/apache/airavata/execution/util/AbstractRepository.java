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
package org.apache.airavata.execution.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.common.db.EntityManagerFactoryHolder;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRepository<T, E, Id> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    private Class<E> dbEntityGenericClass;

    public AbstractRepository(Class<T> thriftGenericClass, Class<E> dbEntityGenericClass) {
        this.dbEntityGenericClass = dbEntityGenericClass;
    }

    /** Convert a JPA entity to the Thrift/model object. */
    protected abstract T toModel(E entity);

    /** Convert a Thrift/model object to a JPA entity. */
    protected abstract E toEntity(T model);

    public T create(T t) {
        return update(t);
    }

    public T update(T t) {
        return mergeEntity(mapToEntity(t));
    }

    protected E mapToEntity(T t) {
        return toEntity(t);
    }

    protected T mergeEntity(E entity) {
        return execute(entityManager -> {
            E persistedCopy = entityManager.merge(entity);
            return toModel(persistedCopy);
        });
    }

    public boolean delete(Id id) {
        return execute(entityManager -> {
            E entity = entityManager.find(dbEntityGenericClass, id);
            if (entity != null) {
                entityManager.remove(entity);
                return true;
            }
            return false;
        });
    }

    public T get(Id id) {
        EntityManager entityManager = getEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.unwrap(org.hibernate.Session.class).setHibernateFlushMode(org.hibernate.FlushMode.MANUAL);
            E entity = entityManager.find(dbEntityGenericClass, id);
            if (entity == null) {
                entityManager.getTransaction().rollback();
                return null;
            }
            initializeEntity(entity);
            T result = toModel(entity);
            entityManager.getTransaction().rollback(); // read-only, no flush needed
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to get entity", e);
        } finally {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }

    /**
     * Hook for subclasses to force-initialize lazy collections before mapping.
     * Hibernate 6 may not eagerly initialize {@code @ElementCollection} maps on entities
     * loaded via association fetching, even when {@code FetchType.EAGER} is declared.
     * Override this method to call {@link Hibernate#initialize} on such collections.
     */
    protected void initializeEntity(E entity) {
        // default: no-op
    }

    public List<T> select(String query, int offset) {
        return execute(entityManager -> {
            List resultSet =
                    entityManager.createQuery(query).setFirstResult(offset).getResultList();
            List<T> gatewayList = new ArrayList<>();
            resultSet.stream().forEach(rs -> gatewayList.add(toModel((E) rs)));
            return gatewayList;
        });
    }

    public List<T> select(String query, int limit, int offset, Map<String, Object> queryParams) {
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS : limit;

        return execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(query);

            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {

                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }

            @SuppressWarnings("unchecked")
            List<E> resultSet =
                    jpaQuery.setFirstResult(offset).setMaxResults(newLimit).getResultList();
            resultSet.forEach(this::initializeEntity);
            List<T> list = new ArrayList<>();
            resultSet.forEach(rs -> list.add(toModel(rs)));
            return list;
        });
    }

    public boolean isExists(Id id) {
        return get(id) != null;
    }

    public int scalarInt(String query, Map<String, Object> queryParams) {

        int scalarInt = execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(query);

            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {

                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }

            return ((Number) jpaQuery.getSingleResult()).intValue();
        });
        return scalarInt;
    }

    public <R> R execute(Committer<EntityManager, R> committer) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
        } catch (Exception e) {
            logger.error("Failed to get EntityManager", e);
            throw new RuntimeException("Failed to get EntityManager", e);
        }
        try {
            // Use MANUAL flush to prevent Hibernate from auto-flushing cascade-persist
            // on eagerly-loaded entity graphs. Explicit flush() before commit handles writes.
            entityManager.unwrap(org.hibernate.Session.class).setHibernateFlushMode(org.hibernate.FlushMode.MANUAL);
            entityManager.getTransaction().begin();
            R r = committer.commit(entityManager);
            entityManager.flush();
            entityManager.getTransaction().commit();
            return r;
        } catch (Exception e) {
            logger.error("Failed to execute transaction", e);
            throw e;
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                entityManager.close();
            }
        }
    }

    public void executeWithNativeQuery(String query, String... params) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
        } catch (Exception e) {
            logger.error("Failed to get EntityManager", e);
            throw new RuntimeException("Failed to get EntityManager", e);
        }
        try {
            Query nativeQuery = entityManager.createNativeQuery(query);
            for (int i = 0; i < params.length; i++) {
                nativeQuery.setParameter((i + 1), params[i]);
            }
            entityManager.getTransaction().begin();
            nativeQuery.executeUpdate();
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Failed to execute transaction", e);
            throw e;
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                entityManager.close();
            }
        }
    }

    public List selectWithNativeQuery(String query, String... params) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
        } catch (Exception e) {
            logger.error("Failed to get EntityManager", e);
            throw new RuntimeException("Failed to get EntityManager", e);
        }
        try {
            Query nativeQuery = entityManager.createNativeQuery(query);
            for (int i = 0; i < params.length; i++) {
                nativeQuery.setParameter((i + 1), params[i]);
            }
            return nativeQuery.getResultList();
        } catch (Exception e) {
            logger.error("Failed to execute transaction", e);
            throw e;
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                entityManager.close();
            }
        }
    }

    protected EntityManager getEntityManager() {
        return EntityManagerFactoryHolder.createEntityManager();
    }
}
