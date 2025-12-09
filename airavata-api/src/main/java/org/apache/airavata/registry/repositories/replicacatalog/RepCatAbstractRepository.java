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
package org.apache.airavata.registry.repositories.replicacatalog;

import com.github.dozermapper.core.Mapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.airavata.registry.repositories.AbstractRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.SharedEntityManagerCreator;

public class RepCatAbstractRepository<T, E, Id> extends AbstractRepository<T, E, Id> {

    private final EntityManagerFactory entityManagerFactory;
    private final Mapper mapper;

    private EntityManager entityManager;

    public RepCatAbstractRepository(
            Class<T> thriftGenericClass,
            Class<E> dbEntityGenericClass,
            @Qualifier("replicaCatalogEntityManagerFactory") EntityManagerFactory entityManagerFactory,
            Mapper mapper) {
        super(thriftGenericClass, dbEntityGenericClass);
        this.entityManagerFactory = entityManagerFactory;
        this.mapper = mapper;
    }

    @Override
    protected Mapper getMapper() {
        return mapper;
    }

    @Override
    protected EntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        }
        return entityManager;
    }
}
