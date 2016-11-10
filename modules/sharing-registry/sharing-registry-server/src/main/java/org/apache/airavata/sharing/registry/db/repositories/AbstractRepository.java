/*
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
 *
*/
package org.apache.airavata.sharing.registry.db.repositories;

import org.apache.airavata.sharing.registry.db.utils.DBConstants;
import org.apache.airavata.sharing.registry.db.utils.JPAUtils;
import org.apache.airavata.sharing.registry.db.utils.ObjectMapperSingleton;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public List<T> create(List<T> tList) throws SharingRegistryException {
        return update(tList);
    }

    public  T update(T t) throws SharingRegistryException {
        Mapper mapper = ObjectMapperSingleton.getInstance();
        E entity = mapper.map(t, dbEntityGenericClass);
        E persistedCopy = (new JPAUtils()).execute(entityManager -> entityManager.merge(entity));
        return mapper.map(persistedCopy, thriftGenericClass);
    }

    public  List<T> update(List<T> tList) throws SharingRegistryException {
        List<T> returnList = new ArrayList<>();
        for(T temp : tList)
            returnList.add(update(temp));
        return returnList;
    }

    public boolean delete(Id id) throws SharingRegistryException {
        (new JPAUtils()).execute(entityManager -> {
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
        E entity =  (new JPAUtils()).execute(entityManager -> entityManager
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
        String queryString = getSelectQuery(filters);
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS: limit;
        List resultSet =  (new JPAUtils()).execute(entityManager -> entityManager.createQuery(queryString).setFirstResult(offset)
                .setMaxResults(newLimit).getResultList());
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<T> gatewayList = new ArrayList<>();
        resultSet.stream().forEach(rs -> gatewayList.add(mapper.map(rs, thriftGenericClass)));
        return gatewayList;
    }

    public List<T> select(String queryString, int offset, int limit) throws SharingRegistryException {
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS: limit;
        List resultSet = (new JPAUtils()).execute(entityManager -> entityManager.createQuery(queryString).setFirstResult(offset)
                .setMaxResults(newLimit).getResultList());
        Mapper mapper = ObjectMapperSingleton.getInstance();
        List<T> gatewayList = new ArrayList<>();
        resultSet.stream().forEach(rs -> gatewayList.add(mapper.map(rs, thriftGenericClass)));
        return gatewayList;
    }

    public String getSelectQuery(Map<String, String> filters){
        String query = "SELECT DISTINCT p from " + dbEntityGenericClass.getSimpleName() + " as p";
        if(filters != null && filters.size() != 0){
            query += " WHERE ";
            for(String k : filters.keySet()){
                query += "p." + k + " = '" + filters.get(k) + "' AND ";
            }
            query = query.substring(0, query.length()-5);
        }

        query += " ORDER BY p.createdTime DESC";

        return query;
    }
}