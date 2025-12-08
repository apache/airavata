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
package org.apache.airavata.profile.repositories;

import com.github.dozermapper.core.Mapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.profile.utils.JPAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractRepository<T, E, Id> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    private Class<T> thriftGenericClass;
    private Class<E> dbEntityGenericClass;
    private EntityManager entityManager;

    public AbstractRepository(Class<T> thriftGenericClass, Class<E> dbEntityGenericClass) {
        this.thriftGenericClass = thriftGenericClass;
        this.dbEntityGenericClass = dbEntityGenericClass;
    }

    protected abstract Mapper getMapper();

    protected EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = SharedEntityManagerCreator.createSharedEntityManager(JPAUtils.getEntityManagerFactory());
        }
        return entityManager;
    }

    @Transactional
    public T create(T t) {
        return update(t);
    }

    @Transactional
    public T update(T t) {
        E entity = getMapper().map(t, dbEntityGenericClass);
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
        return getMapper().map(entity, thriftGenericClass);
    }

    @Transactional(readOnly = true)
    public List<T> select(String query) {
        EntityManager em = getEntityManager();
        List<?> resultSet = em.createQuery(query).getResultList();
        List<T> resultList = new ArrayList<>();
        for (Object rs : resultSet) {
            resultList.add(getMapper().map(rs, thriftGenericClass));
        }
        return resultList;
    }

    @Transactional(readOnly = true)
    public List<T> select(String query, int limit, int offset) {
        EntityManager em = getEntityManager();
        List<?> resultSet = em.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
        List<T> resultList = new ArrayList<>();
        for (Object rs : resultSet) {
            resultList.add(getMapper().map(rs, thriftGenericClass));
        }
        return resultList;
    }

    @Transactional(readOnly = true)
    public List<T> select(String query, int limit, int offset, Map<String, Object> queryParams) {
        EntityManager em = getEntityManager();
        Query jpaQuery = em.createQuery(query);

        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            jpaQuery.setParameter(entry.getKey(), entry.getValue());
        }

        List<?> resultSet = jpaQuery.setFirstResult(offset).setMaxResults(limit).getResultList();
        List<T> resultList = new ArrayList<>();
        for (Object rs : resultSet) {
            resultList.add(getMapper().map(rs, thriftGenericClass));
        }
        return resultList;
    }

    @Transactional(readOnly = true)
    public List<T> select(String query, Map<String, Object> queryParams) {
        EntityManager em = getEntityManager();
        Query jpaQuery = em.createQuery(query);

        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            jpaQuery.setParameter(entry.getKey(), entry.getValue());
        }

        List<?> resultSet = jpaQuery.getResultList();
        List<T> resultList = new ArrayList<>();
        for (Object rs : resultSet) {
            resultList.add(getMapper().map(rs, thriftGenericClass));
        }
        return resultList;
    }
}
