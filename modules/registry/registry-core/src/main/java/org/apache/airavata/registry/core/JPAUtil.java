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
package org.apache.airavata.registry.core;

import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.cpi.ExperimentCatalogException;
import org.apache.airavata.registry.cpi.RegistryException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class JPAUtil {

    public static <R> R executeOnExpCatalog(Committer<EntityManager, R> committer) throws RegistryException, ExperimentCatalogException {
        EntityManager entityManager = ExpCatResourceUtils.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            R r = committer.commit(entityManager);
            entityManager.getTransaction().commit();
            return  r;
        }finally {
            if(entityManager != null && entityManager.isOpen()){
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
                entityManager.close();
            }
        }

    }

    public static void test() throws ExperimentCatalogException, RegistryException {
        executeOnExpCatalog((em) -> "");
    }
}
