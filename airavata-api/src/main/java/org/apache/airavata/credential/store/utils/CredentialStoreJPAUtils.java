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
package org.apache.airavata.credential.store.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.common.utils.JPAUtils;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.common.exception.ApplicationSettingsException;

/**
 * JPA utility class for credential store operations.
 * Provides EntityManager management and transaction handling.
 */
public class CredentialStoreJPAUtils {
    
    private static final String PERSISTENCE_UNIT_NAME = "credential_store_data";
    private static final JDBCConfig JDBC_CONFIG = new CredentialStoreJDBCConfig();
    private static final EntityManagerFactory factory = JPAUtils.getEntityManagerFactory(PERSISTENCE_UNIT_NAME, JDBC_CONFIG);
    private static EntityManager entityManagerInstance = null;

    public static synchronized EntityManager getEntityManager() {
        if (entityManagerInstance == null || !entityManagerInstance.isOpen()) {
            entityManagerInstance = factory.createEntityManager();
        }
        return entityManagerInstance;
    }

    public static <R> R execute(Committer<EntityManager, R> committer) {
        EntityManager entityManager = getEntityManager();
        try {
            entityManager.getTransaction().begin();
            R r = committer.commit(entityManager);
            entityManager.getTransaction().commit();
            return r;
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }

    /**
     * JDBC configuration for credential store database.
     */
    private static class CredentialStoreJDBCConfig implements JDBCConfig {
        
        @Override
        public String getDriver() {
            try {
                return ServerSettings.getCredentialStoreDBDriver();
            } catch (ApplicationSettingsException e) {
                throw new RuntimeException("Failed to get credential store DB driver", e);
            }
        }

        @Override
        public String getURL() {
            try {
                return ServerSettings.getCredentialStoreDBURL();
            } catch (ApplicationSettingsException e) {
                throw new RuntimeException("Failed to get credential store DB URL", e);
            }
        }

        @Override
        public String getUser() {
            try {
                return ServerSettings.getCredentialStoreDBUser();
            } catch (ApplicationSettingsException e) {
                throw new RuntimeException("Failed to get credential store DB user", e);
            }
        }

        @Override
        public String getPassword() {
            try {
                return ServerSettings.getCredentialStoreDBPassword();
            } catch (ApplicationSettingsException e) {
                throw new RuntimeException("Failed to get credential store DB password", e);
            }
        }

        @Override
        public String getValidationQuery() {
            return "SELECT 1";
        }
    }

    /**
     * Functional interface for committing operations with EntityManager.
     */
    @FunctionalInterface
    public interface Committer<T, R> {
        R commit(T t);
    }
}
