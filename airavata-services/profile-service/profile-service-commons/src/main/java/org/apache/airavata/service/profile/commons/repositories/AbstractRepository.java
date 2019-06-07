/**
 *
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
 */
package org.apache.airavata.service.profile.commons.repositories;

import org.apache.airavata.service.profile.commons.utils.JPAUtils;
import org.apache.airavata.service.profile.commons.utils.ObjectMapperSingleton;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Mapper mapper = ObjectMapperSingleton.getInstance();
        E entity = mapper.map(t, dbEntityGenericClass);
        E persistedCopy = JPAUtils.execute(entityManager -> entityManager.merge(entity));
        return mapper.map(persistedCopy, thriftGenericClass);
    }

    public boolean delete(Id id) {
        JPAUtils.execute(entityManager -> {
            E entity = entityManager.find(dbEntityGenericClass, id);
            entityManager.remove(entity);
            return entity;
        });
        return true;
    }

    public T get(Id id) {
        E entity = JPAUtils.execute(entityManager -> entityManager
                .find(dbEntityGenericClass, id));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        return mapper.map(entity, thriftGenericClass);
    }

    public List<T> select(String query) {
        List resultSet = (List) JPAUtils.execute(entityManager -> entityManager.createQuery(query).getResultList());
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<T> resultList = new ArrayList<>();
        resultSet.stream().forEach(rs -> resultList.add(mapper.map(rs, thriftGenericClass)));
        return resultList;
    }

    public List<T> select(String query, int limit, int offset) {
        List resultSet = (List) JPAUtils.execute(entityManager -> entityManager.createQuery(query).setFirstResult(offset)
                .setMaxResults(limit).getResultList());
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<T> resultList = new ArrayList<>();
        resultSet.stream().forEach(rs -> resultList.add(mapper.map(rs, thriftGenericClass)));
        return resultList;
    }

    public List<T> select(String query, int limit, int offset, Map<String, Object> queryParams) {
        List resultSet = (List) JPAUtils.execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(query);

            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {

                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }

            return jpaQuery.setFirstResult(offset).setMaxResults(limit).getResultList();

        });
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<T> resultList = new ArrayList<>();
        resultSet.stream().forEach(rs -> resultList.add(mapper.map(rs, thriftGenericClass)));
        return resultList;
    }

    public List<T> select(String query, Map<String, Object> queryParams) {
        List resultSet = (List) JPAUtils.execute(entityManager -> {
            Query jpaQuery = entityManager.createQuery(query);

            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {

                jpaQuery.setParameter(entry.getKey(), entry.getValue());
            }

            return jpaQuery.getResultList();

        });
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<T> resultList = new ArrayList<>();
        resultSet.stream().forEach(rs -> resultList.add(mapper.map(rs, thriftGenericClass)));
        return resultList;
    }
}