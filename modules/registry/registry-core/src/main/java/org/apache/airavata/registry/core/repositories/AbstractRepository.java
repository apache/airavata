/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.airavata.registry.core.repositories;

import org.apache.airavata.registry.core.utils.Committer;
import org.apache.airavata.registry.core.utils.DBConstants;
import org.apache.airavata.registry.core.utils.ObjectMapperSingleton;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractRepository<T, E, Id> {
    private final static Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    private Class<T> thriftGenericClass;
    private Class<E> dbEntityGenericClass;

    public AbstractRepository(Class<T> thriftGenericClass, Class<E> dbEntityGenericClass) {
        this.thriftGenericClass = thriftGenericClass;
        this.dbEntityGenericClass = dbEntityGenericClass;
    }

    public T create(T t) {
        return update(t);
    }

    public T update(T t) {
        return mergeEntity(mapToEntity(t));
    }

    protected E mapToEntity(T t) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(t, dbEntityGenericClass);
    }

    protected T mergeEntity(E entity) {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        E persistedCopy = execute(entityManager -> entityManager.merge(entity));
        return mapper.map(persistedCopy, thriftGenericClass);
    }

    public boolean delete(Id id) {
        execute(entityManager -> {
            E entity = entityManager.find(dbEntityGenericClass, id);
            entityManager.remove(entity);
            return entity;
        });
        return true;
    }

    public T get(Id id) {
        E entity = execute(entityManager -> entityManager
                .find(dbEntityGenericClass, id));
        if(entity == null)
            return null;
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, thriftGenericClass);
    }

    public List<T> select(String query, int offset) {
        List resultSet = (List) execute(entityManager -> entityManager.createQuery(query).setFirstResult(offset)
                .getResultList());
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<T> gatewayList = new ArrayList<>();
        resultSet.stream().forEach(rs -> gatewayList.add(mapper.map(rs, thriftGenericClass)));
        return gatewayList;
    }

    public List<T> select(String query, int limit, int offset, Map<String, Object> queryParams) {
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS: limit;

        List resultSet = (List) execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(query);

            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {

                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }

            return jpaQuery.setFirstResult(offset).setMaxResults(newLimit).getResultList();

        });
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<T> gatewayList = new ArrayList<>();
        resultSet.stream().forEach(rs -> gatewayList.add(mapper.map(rs, thriftGenericClass)));
        return gatewayList;
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

            return ((Number)jpaQuery.getSingleResult()).intValue();
        });
        return scalarInt;
    }

    public <R> R execute(Committer<EntityManager, R> committer){
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
        } catch (Exception e) {
            logger.error("Failed to get EntityManager", e);
            throw new RuntimeException("Failed to get EntityManager", e);
        }
        try {
            entityManager.getTransaction().begin();
            R r = committer.commit(entityManager);
            entityManager.getTransaction().commit();
            return  r;
        } catch(Exception e) {
            logger.error("Failed to execute transaction", e);
            throw e;
        }finally {
            if (entityManager != null && entityManager.isOpen()) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                entityManager.close();
            }
        }
    }

    abstract protected EntityManager getEntityManager();

}
