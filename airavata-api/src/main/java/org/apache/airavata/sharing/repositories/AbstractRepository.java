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
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.airavata.sharing.models.SharingRegistryException;
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

    // FIXME do a bulk insert
    @Transactional
    public List<T> create(List<T> tList) throws SharingRegistryException {
        return update(tList);
    }

    @Transactional
    public T update(T t) throws SharingRegistryException {
        E entity = getMapper().map(t, dbEntityGenericClass);
        EntityManager em = getEntityManager();
        E persistedCopy = em.merge(entity);
        return getMapper().map(persistedCopy, thriftGenericClass);
    }

    // FIXME do a bulk update
    @Transactional
    public List<T> update(List<T> tList) throws SharingRegistryException {
        List<T> returnList = new ArrayList<>();
        for (T temp : tList) returnList.add(update(temp));
        return returnList;
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
    public T get(Id id) throws SharingRegistryException {
        EntityManager em = getEntityManager();
        E entity = em.find(dbEntityGenericClass, id);
        if (entity == null) return null;
        return getMapper().map(entity, thriftGenericClass);
    }

    public boolean isExists(Id id) throws SharingRegistryException {
        return get(id) != null;
    }

    public List<T> get(List<Id> idList) throws SharingRegistryException {
        List<T> returnList = new ArrayList<>();
        for (Id id : idList) returnList.add(get(id));
        return returnList;
    }

    @Transactional(readOnly = true)
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
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS : limit;
        EntityManager em = getEntityManager();
        jakarta.persistence.Query q = em.createQuery(queryString);
        for (int i = 0; i < parameters.size(); i++) {
            q.setParameter(i + 1, parameters.get(i));
        }
        List<?> resultSet = q.setFirstResult(offset).setMaxResults(newLimit).getResultList();
        List<T> gatewayList = new ArrayList<>();
        for (Object rs : resultSet) {
            gatewayList.add(getMapper().map(rs, thriftGenericClass));
        }
        return gatewayList;
    }

    @Transactional(readOnly = true)
    public List<T> select(String queryString, Map<String, Object> queryParameters, int offset, int limit)
            throws SharingRegistryException {
        int newLimit = limit < 0 ? DBConstants.SELECT_MAX_ROWS : limit;
        EntityManager em = getEntityManager();
        Query q = em.createQuery(queryString);
        for (Map.Entry<String, Object> queryParam : queryParameters.entrySet()) {
            q.setParameter(queryParam.getKey(), queryParam.getValue());
        }
        List<?> resultSet = q.setFirstResult(offset).setMaxResults(newLimit).getResultList();
        List<T> gatewayList = new ArrayList<>();
        for (Object rs : resultSet) {
            gatewayList.add(getMapper().map(rs, thriftGenericClass));
        }
        return gatewayList;
    }

    @Transactional
    public <R> R execute(Committer<EntityManager, R> committer) throws SharingRegistryException {
        EntityManager em = getEntityManager();
        return committer.commit(em);
    }
}
