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
package org.apache.airavata.registry.repositories;

import com.github.dozermapper.core.Mapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.registry.utils.Committer;
import org.apache.airavata.registry.utils.DBConstants;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRepository<T, E, Id> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    private Class<T> thriftGenericClass;
    private Class<E> dbEntityGenericClass;

    public AbstractRepository(Class<T> thriftGenericClass, Class<E> dbEntityGenericClass) {
        this.thriftGenericClass = thriftGenericClass;
        this.dbEntityGenericClass = dbEntityGenericClass;
    }

    protected abstract Mapper getMapper();
    
    protected abstract EntityManager getEntityManager();

    @Transactional
    public T create(T t) {
        return update(t);
    }

    @Transactional
    public T update(T t) {
        return mergeEntity(mapToEntity(t));
    }

    protected E mapToEntity(T t) {
        return getMapper().map(t, dbEntityGenericClass);
    }

    protected T mergeEntity(E entity) {
        EntityManager em = getEntityManager();
        E persistedCopy = em.merge(entity);
        return getMapper().map(persistedCopy, thriftGenericClass);
    }

    @Transactional
    public boolean delete(Id id) {
        EntityManager em = getEntityManager();
        E entity = em.find(dbEntityGenericClass, id);
        if (entity != null) {
            em.remove(entity);
        }
        return true;
    }

    @Transactional(readOnly = true)
    public T get(Id id) {
        EntityManager em = getEntityManager();
        E entity = em.find(dbEntityGenericClass, id);
        if (entity == null) return null;
        return getMapper().map(entity, thriftGenericClass);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<T> select(String query, int offset) {
        EntityManager em = getEntityManager();
        List<?> resultSet = em.createQuery(query).setFirstResult(offset).getResultList();
        return resultSet.stream()
                .map(rs -> getMapper().map(rs, thriftGenericClass))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<T> select(String query, int limit, int offset, Map<String, Object> queryParams) {
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS : limit;
        EntityManager em = getEntityManager();
        Query jpaQuery = em.createQuery(query);

        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            jpaQuery.setParameter(entry.getKey(), entry.getValue());
        }

        List<?> resultSet = jpaQuery.setFirstResult(offset).setMaxResults(newLimit).getResultList();
        List<T> gatewayList = new ArrayList<>();
        for (Object rs : resultSet) {
            gatewayList.add(getMapper().map(rs, thriftGenericClass));
        }
        return gatewayList;
    }

    public boolean isExists(Id id) {
        return get(id) != null;
    }

    @Transactional(readOnly = true)
    public int scalarInt(String query, Map<String, Object> queryParams) {
        EntityManager em = getEntityManager();
        Query jpaQuery = em.createQuery(query);

        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            jpaQuery.setParameter(entry.getKey(), entry.getValue());
        }

        return ((Number) jpaQuery.getSingleResult()).intValue();
    }

    @Transactional
    public <R> R execute(Committer<EntityManager, R> committer) {
        EntityManager em = getEntityManager();
        return committer.commit(em);
    }

    @Transactional
    public void executeWithNativeQuery(String query, String... params) {
        EntityManager em = getEntityManager();
        Query nativeQuery = em.createNativeQuery(query);
        for (int i = 0; i < params.length; i++) {
            nativeQuery.setParameter((i + 1), params[i]);
        }
        nativeQuery.executeUpdate();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<?> selectWithNativeQuery(String query, String... params) {
        EntityManager em = getEntityManager();
        Query nativeQuery = em.createNativeQuery(query);
        for (int i = 0; i < params.length; i++) {
            nativeQuery.setParameter((i + 1), params[i]);
        }
        return nativeQuery.getResultList();
    }
}
