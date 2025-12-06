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
package org.apache.airavata.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.common.utils.JPAUtils;
import org.apache.airavata.profile.commons.utils.ProfileServiceJDBCConfig;
import org.apache.airavata.registry.core.utils.AppCatalogJDBCConfig;
import org.apache.airavata.registry.core.utils.ExpCatalogJDBCConfig;
import org.apache.airavata.registry.core.utils.ReplicaCatalogJDBCConfig;
import org.apache.airavata.registry.core.utils.WorkflowCatalogJDBCConfig;
import org.apache.airavata.sharing.db.utils.SharingRegistryJDBCConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for multiple JPA persistence units.
 * Spring Boot typically expects a single persistence unit, so we need
 * custom configuration for each of the 6 persistence units.
 */
@Configuration
@EnableTransactionManagement
public class JpaConfig {

    // Persistence unit names
    public static final String PROFILE_SERVICE_PU = "profile_service";
    public static final String APPCATALOG_PU = "appcatalog_data_new";
    public static final String EXPCATALOG_PU = "experiment_data_new";
    public static final String REPLICACATALOG_PU = "replicacatalog_data_new";
    public static final String WORKFLOWCATALOG_PU = "workflowcatalog_data_new";
    public static final String SHARING_REGISTRY_PU = "airavata-sharing-registry";

    @Bean(name = "profileServiceEntityManagerFactory")
    @Primary
    public EntityManagerFactory profileServiceEntityManagerFactory() {
        JDBCConfig jdbcConfig = new ProfileServiceJDBCConfig();
        return JPAUtils.getEntityManagerFactory(PROFILE_SERVICE_PU, jdbcConfig);
    }

    @Bean(name = "appCatalogEntityManagerFactory")
    public EntityManagerFactory appCatalogEntityManagerFactory() {
        JDBCConfig jdbcConfig = new AppCatalogJDBCConfig();
        return JPAUtils.getEntityManagerFactory(APPCATALOG_PU, jdbcConfig);
    }

    @Bean(name = "expCatalogEntityManagerFactory")
    public EntityManagerFactory expCatalogEntityManagerFactory() {
        JDBCConfig jdbcConfig = new ExpCatalogJDBCConfig();
        return JPAUtils.getEntityManagerFactory(EXPCATALOG_PU, jdbcConfig);
    }

    @Bean(name = "replicaCatalogEntityManagerFactory")
    public EntityManagerFactory replicaCatalogEntityManagerFactory() {
        JDBCConfig jdbcConfig = new ReplicaCatalogJDBCConfig();
        return JPAUtils.getEntityManagerFactory(REPLICACATALOG_PU, jdbcConfig);
    }

    @Bean(name = "workflowCatalogEntityManagerFactory")
    public EntityManagerFactory workflowCatalogEntityManagerFactory() {
        JDBCConfig jdbcConfig = new WorkflowCatalogJDBCConfig();
        return JPAUtils.getEntityManagerFactory(WORKFLOWCATALOG_PU, jdbcConfig);
    }

    @Bean(name = "sharingRegistryEntityManagerFactory")
    public EntityManagerFactory sharingRegistryEntityManagerFactory() {
        JDBCConfig jdbcConfig = new SharingRegistryJDBCConfig();
        return JPAUtils.getEntityManagerFactory(SHARING_REGISTRY_PU, jdbcConfig);
    }

    // Transaction managers for each persistence unit
    @Bean(name = "profileServiceTransactionManager")
    @Primary
    public PlatformTransactionManager profileServiceTransactionManager() {
        return new JpaTransactionManager(profileServiceEntityManagerFactory());
    }

    @Bean(name = "appCatalogTransactionManager")
    public PlatformTransactionManager appCatalogTransactionManager() {
        return new JpaTransactionManager(appCatalogEntityManagerFactory());
    }

    @Bean(name = "expCatalogTransactionManager")
    public PlatformTransactionManager expCatalogTransactionManager() {
        return new JpaTransactionManager(expCatalogEntityManagerFactory());
    }

    @Bean(name = "replicaCatalogTransactionManager")
    public PlatformTransactionManager replicaCatalogTransactionManager() {
        return new JpaTransactionManager(replicaCatalogEntityManagerFactory());
    }

    @Bean(name = "workflowCatalogTransactionManager")
    public PlatformTransactionManager workflowCatalogTransactionManager() {
        return new JpaTransactionManager(workflowCatalogEntityManagerFactory());
    }

    @Bean(name = "sharingRegistryTransactionManager")
    public PlatformTransactionManager sharingRegistryTransactionManager() {
        return new JpaTransactionManager(sharingRegistryEntityManagerFactory());
    }

    // EntityManager beans for injection into repositories
    // Note: EntityManagers are thread-safe when used with Spring's transaction management
    // We use prototype scope to create new instances as needed
    @Bean(name = "appCatalogEntityManager")
    @Scope("prototype")
    public EntityManager appCatalogEntityManager(@Qualifier("appCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "expCatalogEntityManager")
    @Scope("prototype")
    public EntityManager expCatalogEntityManager(@Qualifier("expCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "replicaCatalogEntityManager")
    @Scope("prototype")
    public EntityManager replicaCatalogEntityManager(@Qualifier("replicaCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "workflowCatalogEntityManager")
    @Scope("prototype")
    public EntityManager workflowCatalogEntityManager(@Qualifier("workflowCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "profileServiceEntityManager")
    @Scope("prototype")
    @Primary
    public EntityManager profileServiceEntityManager(@Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "sharingRegistryEntityManager")
    @Scope("prototype")
    public EntityManager sharingRegistryEntityManager(@Qualifier("sharingRegistryEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }
}

