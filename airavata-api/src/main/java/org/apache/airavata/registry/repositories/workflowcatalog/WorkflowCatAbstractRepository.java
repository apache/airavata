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
package org.apache.airavata.registry.repositories.workflowcatalog;

import com.github.dozermapper.core.Mapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.airavata.registry.repositories.AbstractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.SharedEntityManagerCreator;

public class WorkflowCatAbstractRepository<T, E, Id> extends AbstractRepository<T, E, Id> {

    @Autowired
    @Qualifier("workflowCatalogEntityManagerFactory")
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private Mapper mapper;

    private EntityManager entityManager;

    public WorkflowCatAbstractRepository(Class<T> thriftGenericClass, Class<E> dbEntityGenericClass) {
        super(thriftGenericClass, dbEntityGenericClass);
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
