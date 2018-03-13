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
package org.apache.airavata.sharing.registry.db.repositories;

import org.apache.airavata.sharing.registry.db.utils.Committer;
import org.apache.airavata.sharing.registry.db.utils.DBConstants;
import org.apache.airavata.sharing.registry.db.utils.JPAUtils;
import org.apache.airavata.sharing.registry.db.utils.ObjectMapperSingleton;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
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

    public AbstractRepository(Class<T> thriftGenericClass, Class<E> dbEntityGenericClass){
        this.thriftGenericClass = thriftGenericClass;
        this.dbEntityGenericClass = dbEntityGenericClass;
    }

    public T create(T t) throws SharingRegistryException {
        return update(t);
    }

    //FIXME do a bulk insert
    public List<T> create(List<T> tList) throws SharingRegistryException {
        return update(tList);
    }

    public  T update(T t) throws SharingRegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        E entity = mapper.map(t, dbEntityGenericClass);
        E persistedCopy = execute(entityManager -> entityManager.merge(entity));
        return mapper.map(persistedCopy, thriftGenericClass);
    }

    //FIXME do a bulk update
    public  List<T> update(List<T> tList) throws SharingRegistryException {
        List<T> returnList = new ArrayList<>();
        for(T temp : tList)
            returnList.add(update(temp));
        return returnList;
    }

    public boolean delete(Id id) throws SharingRegistryException {
        execute(entityManager -> {
             E entity = entityManager.find(dbEntityGenericClass, id);
             entityManager.remove(entity);
             return entity;
         });
        return true;
    }

    public boolean delete(List<Id> idList) throws SharingRegistryException {
        for(Id id : idList)
            delete(id);
        return true;
    }

    public T get(Id id) throws SharingRegistryException {
        E entity = execute(entityManager -> entityManager
                .find(dbEntityGenericClass, id));
        Mapper mapper = ObjectMapperSingleton.getInstance();
        if(entity == null)
            return null;
        return mapper.map(entity, thriftGenericClass);
    }

    public boolean isExists(Id id) throws SharingRegistryException {
        return get(id) != null;
    }

    public List<T> get(List<Id> idList) throws SharingRegistryException {
        List<T> returnList = new ArrayList<>();
        for(Id id : idList)
            returnList.add(get(id));
        return returnList;
    }

    public List<T> select(Map<String, String> filters, int offset, int limit) throws SharingRegistryException {
        String query = "SELECT DISTINCT p from " + dbEntityGenericClass.getSimpleName() + " as p";
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
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS: limit;
        List resultSet = execute(entityManager -> {
            javax.persistence.Query q = entityManager.createQuery(queryString);
            for (int i = 0; i < parameters.size(); i++) {
                q.setParameter(i + 1, parameters.get(i));
            }
            return q.setFirstResult(offset).setMaxResults(newLimit).getResultList();
        });
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<T> gatewayList = new ArrayList<>();
        resultSet.stream().forEach(rs -> gatewayList.add(mapper.map(rs, thriftGenericClass)));
        return gatewayList;
    }

    public List<T> select(String queryString, Map<String,Object> queryParameters, int offset, int limit) throws SharingRegistryException {
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS: limit;
        List resultSet = execute(entityManager -> {
            Query q =  entityManager.createQuery(queryString);
            for(Map.Entry<String, Object> queryParam : queryParameters.entrySet()){
                q.setParameter(queryParam.getKey(), queryParam.getValue());
            }
            return q.setFirstResult(offset).setMaxResults(newLimit).getResultList();
        });
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<T> gatewayList = new ArrayList<>();
        resultSet.stream().forEach(rs -> gatewayList.add(mapper.map(rs, thriftGenericClass)));
        return gatewayList;
    }

    public <R> R execute(Committer<EntityManager, R> committer) throws SharingRegistryException {
        EntityManager entityManager = JPAUtils.getEntityManager();
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
}