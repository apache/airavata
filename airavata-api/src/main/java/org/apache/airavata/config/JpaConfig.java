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
import org.apache.airavata.common.utils.JPAUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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

    @Autowired
    private AiravataServerProperties properties;

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
        var db = properties.getDatabase().getProfileService();
        return JPAUtils.getEntityManagerFactory(
                PROFILE_SERVICE_PU,
                db.getJdbcDriver(),
                db.getJdbcUrl(),
                db.getJdbcUser(),
                db.getJdbcPassword(),
                db.getValidationQuery());
    }

    @Bean(name = "appCatalogEntityManagerFactory")
    public EntityManagerFactory appCatalogEntityManagerFactory() {
        var db = properties.getDatabase().getAppCatalog();
        return JPAUtils.getEntityManagerFactory(
                APPCATALOG_PU,
                db.getJdbcDriver(),
                db.getJdbcUrl(),
                db.getJdbcUser(),
                db.getJdbcPassword(),
                db.getValidationQuery());
    }

    @Bean(name = "expCatalogEntityManagerFactory")
    public EntityManagerFactory expCatalogEntityManagerFactory() {
        var db = properties.getDatabase().getRegistry();
        return JPAUtils.getEntityManagerFactory(
                EXPCATALOG_PU,
                db.getJdbcDriver(),
                db.getJdbcUrl(),
                db.getJdbcUser(),
                db.getJdbcPassword(),
                properties.getDatabase().getValidationQuery());
    }

    @Bean(name = "replicaCatalogEntityManagerFactory")
    public EntityManagerFactory replicaCatalogEntityManagerFactory() {
        var db = properties.getDatabase().getReplicaCatalog();
        return JPAUtils.getEntityManagerFactory(
                REPLICACATALOG_PU,
                db.getJdbcDriver(),
                db.getJdbcUrl(),
                db.getJdbcUser(),
                db.getJdbcPassword(),
                db.getValidationQuery());
    }

    @Bean(name = "workflowCatalogEntityManagerFactory")
    public EntityManagerFactory workflowCatalogEntityManagerFactory() {
        var db = properties.getDatabase().getWorkflowCatalog();
        return JPAUtils.getEntityManagerFactory(
                WORKFLOWCATALOG_PU,
                db.getJdbcDriver(),
                db.getJdbcUrl(),
                db.getJdbcUser(),
                db.getJdbcPassword(),
                db.getValidationQuery());
    }

    @Bean(name = "sharingRegistryEntityManagerFactory")
    public EntityManagerFactory sharingRegistryEntityManagerFactory() {
        var db = properties.getDatabase().getSharingCatalog();
        return JPAUtils.getEntityManagerFactory(
                SHARING_REGISTRY_PU,
                db.getJdbcDriver(),
                db.getJdbcUrl(),
                db.getJdbcUser(),
                db.getJdbcPassword(),
                db.getValidationQuery());
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
    public EntityManager appCatalogEntityManager(
            @Qualifier("appCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "expCatalogEntityManager")
    @Scope("prototype")
    public EntityManager expCatalogEntityManager(
            @Qualifier("expCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "replicaCatalogEntityManager")
    @Scope("prototype")
    public EntityManager replicaCatalogEntityManager(
            @Qualifier("replicaCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "workflowCatalogEntityManager")
    @Scope("prototype")
    public EntityManager workflowCatalogEntityManager(
            @Qualifier("workflowCatalogEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "profileServiceEntityManager")
    @Scope("prototype")
    @Primary
    public EntityManager profileServiceEntityManager(
            @Qualifier("profileServiceEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    @Bean(name = "sharingRegistryEntityManager")
    @Scope("prototype")
    public EntityManager sharingRegistryEntityManager(
            @Qualifier("sharingRegistryEntityManagerFactory") EntityManagerFactory emf) {
        return emf.createEntityManager();
    }

    // Spring Data JPA Repository Configuration for each persistence unit
    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.profile.repositories",
            entityManagerFactoryRef = "profileServiceEntityManagerFactory",
            transactionManagerRef = "profileServiceTransactionManager")
    static class ProfileServiceJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.core.repositories.appcatalog",
            entityManagerFactoryRef = "appCatalogEntityManagerFactory",
            transactionManagerRef = "appCatalogTransactionManager")
    static class AppCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.core.repositories.expcatalog",
            entityManagerFactoryRef = "expCatalogEntityManagerFactory",
            transactionManagerRef = "expCatalogTransactionManager")
    static class ExpCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.core.repositories.replicacatalog",
            entityManagerFactoryRef = "replicaCatalogEntityManagerFactory",
            transactionManagerRef = "replicaCatalogTransactionManager")
    static class ReplicaCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.registry.core.repositories.workflowcatalog",
            entityManagerFactoryRef = "workflowCatalogEntityManagerFactory",
            transactionManagerRef = "workflowCatalogTransactionManager")
    static class WorkflowCatalogJpaRepositoriesConfig {}

    @Configuration
    @EnableJpaRepositories(
            basePackages = "org.apache.airavata.sharing.repositories",
            entityManagerFactoryRef = "sharingRegistryEntityManagerFactory",
            transactionManagerRef = "sharingRegistryTransactionManager")
    static class SharingRegistryJpaRepositoriesConfig {}
}
